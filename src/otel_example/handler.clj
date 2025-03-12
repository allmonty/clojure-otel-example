(ns otel-example.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [steffan-westcott.clj-otel.api.metrics.instrument :as instrument]
            [steffan-westcott.clj-otel.api.trace.span :as span]
            [schema.core :as s]))

(s/defschema Pizza
  {:name s/Str
   (s/optional-key :description) s/Str
   :size (s/enum :L :M :S)
   :origin {:country (s/enum :FI :PO)
            :city s/Str}})

(defonce
  request-count
  (delay (instrument/instrument {:name        "app.otel-example.request-count"
                                 :instrument-type :counter
                                 :unit        "{requests}"
                                 :description "The number of requests calculated"})))

(defn do-final-thing
  [{:keys [x y]}]
  (span/add-event! "Calculation completed" {:result (* x y)})
  (instrument/add! @request-count {:value 1})
  (ok {:result (* x y)}))

(defn do-another-thing
  [data]
  (span/with-span! "Do another thing"
    (Thread/sleep 2000)
    data))

(defn do-something
  [data]
  (span/with-span! "Do something"
    (Thread/sleep 1000)
    data))

(defn operation-handler
  [x y]
  (span/add-event! "Start operation")
  (-> {:x x :y y}
      (do-something)
      (do-another-thing)
      (do-final-thing)))

(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "otel-example"
                    :description "OTel example"}
             :tags [{:name "api", :description "some apis"}]}}}

    (context "/api" []
      :tags ["api"]

      (GET "/operation-1" []
        :return {:result s/Any}
        :query-params [x :- Long, y :- Long]
        :summary "does something"
        (span/with-span! "Operation 1"
          (operation-handler x y)))

      (POST "/echo" []
        :return Pizza
        :body [pizza Pizza]
        :summary "echoes a Pizza"
        (ok pizza)))))
