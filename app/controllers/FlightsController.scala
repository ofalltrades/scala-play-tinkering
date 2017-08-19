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
    // only returning json resp right now; for testing deduping algorithm;
    //   frontend has not been created yet
    def index = Action.async {
      val aggFlightsURI = "https://private-7e668-flightaggregationapi.apiary-mock.com/flights"
      val singleProvFlightsURI = "https://private-6516c-singleproviderapi.apiary-mock.com/flights"

      dedupedFlights(ws.url(aggFlightsURI), ws.url(singleProvFlightsURI)) map {
        uniqFlights => Ok(uniqFlights)
      }
    }

    // this is just here temporarily; should be moved to some sort of helper module
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
      // I might add comments here in real life--probably if whoever reviewed
      //   the code found it hard to follow. However, if you look at the
      //   raw response, it would be faily easy to follow.
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

// this is just here temporarily; would be its own file
package jakelib {
  import com.fasterxml.jackson.databind.ObjectMapper
  import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
  import com.fasterxml.jackson.module.scala.DefaultScalaModule

  // This is fairly naive, but it makes my life easier here.
  //   It could be useful if polished and not reliant on jackson.
  //   I need a #fromJson method so that I can build a map out of
  //   json string pared by #toJson, as jackson cannot digest a
  //   string with escape sequences.
  object JakesSanerJsonService {
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)

    def toJson(value: Any) : String = {
      mapper.writeValueAsString(value)
    }

    def toJson(value: Map[Symbol, Any]) : String = {
      toJson(value.asInstanceOf[Map[String, Any]])
    }

    // Need #fromJson method that strips indigestible parts of string:
    //   mapper.readValue[Map[String, T]](fromJson(json))
    def toMap[T](json : String)(implicit m : Manifest[T]) = {
      mapper.readValue[Map[String, T]](json)
    }
  }
}
