package services

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
