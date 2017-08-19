package controllers {
  import javax.inject._

  import play.api.mvc._
  import play.api.libs.ws._
  import play.api.libs.json._

  import scala.concurrent.Future
  import akka.actor.ActorSystem

  import scala.concurrent.ExecutionContext.Implicits.global // FIGURE OUT HOW TO GET RID OF THIS

  import jakelib.{ JakesSanerJsonService => SaneJson }

  @Singleton
  class FlightsController @Inject() (cc: ControllerComponents, ws: WSClient) extends AbstractController(cc) {
    def index = Action.async {
      val aggFlightsURI = "https://private-7e668-flightaggregationapi.apiary-mock.com/flights"
      val singleProvFlightsURI = "https://private-6516c-singleproviderapi.apiary-mock.com/flights"

      dedupedFlights(ws.url(aggFlightsURI), ws.url(singleProvFlightsURI)) map {
        uniqFlights => Ok(uniqFlights)
      }
    }

    def getAggregatedFlights(request : WSRequest) : Future[String] = {
      implicit val system : ActorSystem = ActorSystem()
      request.get().map { resp => resp.body }(system.dispatcher)
    }

    def getSingleProvFlights(request : WSRequest) : Future[String] = {
      implicit val system : ActorSystem = ActorSystem()

      request
        .addQueryStringParameters("provider" -> "delta")
        .get().map { resp => resp.body }(system.dispatcher)
    }

    def dedupedFlights(req1: WSRequest, req2: WSRequest) : Future[String] = {
      for {
        singleProvFlightsJson <- getSingleProvFlights(req1)
        allAggFlightsJson <- getAggregatedFlights(req2)
      } yield {
        val flights = {
          SaneJson.toMap[Seq[Map[String, String]]](singleProvFlightsJson).apply("flights") ++
          SaneJson.toMap[Seq[Map[String, String]]](allAggFlightsJson).apply("flights")
        } groupBy {
          flight => flight("name") + flight("flightNumber").toString
        } map {
          case (k, v) => SaneJson.toJson(v.head)
        }

        SaneJson.toJson("{ \"flights\": " + flights.mkString(", ") + "}")
      }
    }
  }
}

package jakelib {
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
  import com.fasterxml.jackson.module.scala.DefaultScalaModule

  // This is fairly naive, but it makes my life easier here
  object JakesSanerJsonService {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)

    def toJson(value: Any) : String = {
      mapper.writeValueAsString(value)
    }

    def toJson(value: Map[Symbol, Any]) : String = {
      toJson(value.asInstanceOf[Map[String, Any]])
    }

    def toMap[T](json : String)(implicit m : Manifest[T]) = {
      mapper.readValue[Map[String, T]](json)
    }
  }
}
