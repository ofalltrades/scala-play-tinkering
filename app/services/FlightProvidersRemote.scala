package services

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.ws._
import akka.actor.ActorSystem

import services.{ JakesSanerJsonService => SaneJson }

object FlightProvidersRemote {
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
