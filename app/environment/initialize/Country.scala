/*
 * Copyright [2016] Maikalal Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package environment.initialize

//Code for upoading the Country code to Orient db
import java.io.File

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.libs.ws._
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.util.{Failure, Success}

object CountryInializer {

  val user = "admin"
  val password = "admin"
  val uploadFlag = false

  case class Country(short_name_en: String, alpha_2_code: String, alpha_3_code: String, numeric_code: Int)


  private def uploadCountry(c: Country, ws: WSClient) = {
    val data = Json.obj(
      "@class" -> "Country",
      "alpha_2_code" -> c.alpha_2_code,
      "alpha_3_code" -> c.alpha_3_code,
      "numeric_code" -> c.numeric_code,
      "short_name_en" -> c.short_name_en,
      "status" -> "A"
    )
    val f: Future[WSResponse] = GenericHelper.databaseUploadUrl("Country", ws).post(data)
    f onSuccess {
      case response => println("Record created -> \n" + response.body)
    }
    f onFailure {
      case t => print("An error has occurred -> \n" + t.getMessage)
    }
    f
  }

  def readDataFromCountryFile(countryFile: File): List[Country] = {
    val src = Source.fromFile(countryFile)
    val countries = src.getLines.drop(1).map { l =>
      val s: Array[String] = l.split(",")
      Country(alpha_2_code = s(0), alpha_3_code = s(1), numeric_code = s(2).toInt, short_name_en = s(3))
    }.toList
    countries
  }

  def uploadCountries(countryFile: File, ws: WSClient = NingWSClient()) = {
    //"/home/pratimsc/codes/learning/attend01/public/reference/world_countries.csv"
    val countries: List[Country] = readDataFromCountryFile(countryFile)
    val result: List[Future[WSResponse]] = countries.map(uploadCountry(_, ws))
    val f: Future[List[WSResponse]] = Future.sequence(result)
    f.onComplete {
      case Success(result) => ws.close()
      case Failure(ex) => ws.close()
    }
    Await.result(f, Duration.Inf)
  }

}
