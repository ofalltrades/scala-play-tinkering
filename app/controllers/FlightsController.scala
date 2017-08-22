package controllers

import javax.inject._

import play.api.mvc._
import play.api.libs.ws._

import scala.concurrent.ExecutionContext.Implicits.global

import services.{ FlightProvidersRemote => providers }

@Singleton
class FlightsController @Inject() (cc: ControllerComponents, ws: WSClient) extends AbstractController(cc) {
  // only returning json resp right now; for testing deduping algorithm;
  //   frontend has not been created yet
  def index = Action.async {
    val aggFlightsURI = "https://private-7e668-flightaggregationapi.apiary-mock.com/flights"
    val singleProvFlightsURI = "https://private-6516c-singleproviderapi.apiary-mock.com/flights"

    providers.dedupedFlights(ws.url(aggFlightsURI), ws.url(singleProvFlightsURI)) map {
      uniqFlights => Ok(uniqFlights)
    }
  }
}
