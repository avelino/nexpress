(ns advanced-example
  (:require [nexpress.core :as nx]))

;; Usando apenas a API Express diretamente para evitar problemas com a integração
(def express (js/require "express"))
(def app (express))
(def session (js/require "express-session"))
(def multer (js/require "multer"))
(def path (js/require "path"))

;; Configurar middlewares padrão
(.use app (.json express))
(.use app (.urlencoded express #js {:extended true}))

;; Middlewares customizados
;; Logger simples
(.use app (fn [req res next]
            (let [start-time (js/Date.now)]
              (.on res "finish"
                   (fn []
                     (let [method (.-method req)
                           url (.-url req)
                           status (.-statusCode res)
                           duration (- (js/Date.now) start-time)]
                       (println method url status (str duration "ms")))))
              (next))))

;; Configuração de sessão
(.use app (session #js {:secret "your-secret-key"
                        :resave false
                        :saveUninitialized true
                        :cookie #js {:maxAge (* 60 60 1000) ;; 1 hour
                                     :httpOnly true}}))

;; Configuração para upload de arquivos
(def storage (.diskStorage multer
                           #js {:destination "uploads/"
                                :filename (fn [req file cb]
                                            (cb nil (str (js/Date.now) "-" (.-originalname file))))}))
(def upload (multer #js {:storage storage
                         :limits #js {:fileSize (* 5 1024 1024)  ;; 5MB
                                      :files 5}}))

;; Servir arquivos estáticos
(.use app (.static express "examples/public"))

;; Rota principal com contador de sessão
(.get app "/" (fn [req res]
                (let [views (or (.. req -session -views) 0)
                      updated-views (inc views)]
                  (set! (.. req -session -views) updated-views)
                  (.json res #js {:message (str "You've visited this page " updated-views " times!")
                                  :sessionID (.. req -session -id)}))))

;; Rota para o formulário de upload
(.get app "/upload-form" (fn [req res]
                           (.sendFile res
                                      (.join path (js/process.cwd) "examples/public/upload-form.html")
                                      (fn [err]
                                        (when err
                                          (js/console.error "Error serving file:", err)
                                          (.status res 500)
                                          (.send res "Error serving upload form"))))))

;; Rota para upload de arquivo
(.post app "/upload" (.single upload "file")
       (fn [req res]
         (let [file (.-file req)]
           (if file
             (.json res #js {:success true
                             :file #js {:originalname (.-originalname file)
                                        :mimetype (.-mimetype file)
                                        :filename (.-filename file)
                                        :path (.-path file)
                                        :size (.-size file)}})
             (-> res
                 (.status 400)
                 (.json #js {:success false
                             :error "No file uploaded"}))))))

;; Rota para demonstração de tratamento de erros
(.get app "/error" (fn [req res]
                     (throw (js/Error. "This is a deliberate error for testing"))))

;; Middleware para tratamento de erros (deve ser o último)
(.use app (fn [err req res next]
            (js/console.error "Error:", (.-message err))
            (js/console.error (.-stack err))
            (let [status (or (.-status err) (.-statusCode err) 500)
                  message (or (.-message err) "Internal Server Error")]
              (.status res status)
              (.json res #js {:error true
                              :message message
                              :stack (.-stack err)}))))

;; Iniciar o servidor na porta 3000
(.listen app 3000 "0.0.0.0"
         (fn []
           (println "Advanced example server is running on http://localhost:3000")
           (println "Try these routes:")
           (println "  GET  / - Session counter example")
           (println "  GET  /upload-form - HTML form for file upload testing")
           (println "  POST /upload - File upload endpoint")
           (println "  GET  /error - Error handling example")))

;; Para executar este exemplo:
;; nbb -cp src examples/advanced-example.cljs
