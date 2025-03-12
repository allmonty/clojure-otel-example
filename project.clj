 (defproject otel-example "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [metosin/compojure-api "2.0.0-alpha30"]
                 [com.github.steffan-westcott/clj-otel-api "0.2.7"]]
  :ring {:handler otel-example.handler/app}
  :uberjar-name "server.jar"
  :profiles {:dev {:dependencies [[javax.servlet/javax.servlet-api "3.1.0"]]
                   :plugins [[lein-ring "0.12.5"]]
                   :jvm-opts ["-javaagent:opentelemetry-javaagent.jar"
                                "-Dotel.resource.attributes=service.name=otel-example-service"
                                "-Dotel.metrics.exporter=prometheus"
                                "-Dotel.logs.exporter=none"]}})
