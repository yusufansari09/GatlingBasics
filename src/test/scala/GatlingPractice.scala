
import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

import scala.util.Random

class GatlingPractice extends Simulation {

	val httpProtocol = http
		.baseUrl("http://cheeze-flight-booker.herokuapp.com")
		.inferHtmlResources()
		.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36")
  	.silentResources

	val headers_0 = Map(
		"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
		"Accept-Encoding" -> "gzip, deflate",
		"Accept-Language" -> "en-US,en;q=0.9",
		"Cache-Control" -> "max-age=0",
		"If-None-Match" -> """W/"97d0cac184c429c13cff97a253aede91"""",
		"Upgrade-Insecure-Requests" -> "1")

	val headers_3 = Map(
		"Accept" -> "image/avif,image/webp,image/apng,image/*,*/*;q=0.8",
		"Accept-Encoding" -> "gzip, deflate",
		"Accept-Language" -> "en-US,en;q=0.9",
		"Cache-Control" -> "no-cache",
		"Pragma" -> "no-cache")

	val headers_4 = Map(
		"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
		"Accept-Encoding" -> "gzip, deflate",
		"Accept-Language" -> "en-US,en;q=0.9",
		"Upgrade-Insecure-Requests" -> "1")

	val headers_12 = Map(
		"Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
		"Accept-Encoding" -> "gzip, deflate",
		"Accept-Language" -> "en-US,en;q=0.9",
		"Cache-Control" -> "max-age=0",
		"Origin" -> "http://cheeze-flight-booker.herokuapp.com",
		"Upgrade-Insecure-Requests" -> "1")

//val csvFeeder = csv("/data/PassengerInfo.csv") //to use this uncomment resources under directory in gatling.conf

//val customeSeparatorFile = separatedValues("myFeederFile.txt", '#')
//val csvFeeders1 = csv("/data/PassengerInfo.csv").batch
//val csvFeeders2 = csv("/data/PassengerInfo.csv").batch(200)

	val randomEmailFeeder = Iterator.continually(Map("randomEmail" -> (Random.alphanumeric.take(20).mkString + "@ggmail.com")))
	val randomNameFeeder = Iterator.continually(Map("randomName" -> (Random.alphanumeric.filter(_.isLetter).take(5).mkString.toLowerCase)))

	val scn = scenario("GatlingPractice")

  	.exec(flushHttpCache) //flush cache
  	.exec(flushSessionCookies) //flush session cookie - like, what will happen when you close the browser
  	.exec(flushCookieJar) //flush all cookies

		.exec(http("HomePage")
			.get("/")
			.headers(headers_0)
			.resources(http("request_1")
			.get("/assets/application-2534172286055efef05dbb34d2da8fc2.js"),
            http("request_2")
			.get("/assets/application-c99cbb3caf78d16bb1482ca2e41d7a9c.css"),
            http("request_3")
			.get("/favicon.ico")
			.headers(headers_3))
		.check(status.in(200,201,202,304))
		.check(status.not(404)))
		.pause(7)


		.exec(http("SearchFlight")
			.get("/flights?utf8=%E2%9C%93&from=2&to=1&date=2015-01-02&num_passengers=1&commit=search")
			.headers(headers_4)
			.resources(http("request_5")
			.get("/assets/application-2534172286055efef05dbb34d2da8fc2.js"),
            http("request_6")
			.get("/assets/application-c99cbb3caf78d16bb1482ca2e41d7a9c.css"),
            http("request_7")
			.get("/favicon.ico"))
			.check(currentLocationRegex(".*num_passengers=1.*")))
		.pause(5)


		.exec(http("SelectFlight")
			.get("/bookings/new?utf8=%E2%9C%93&flight_id=64&commit=Select+Flight&num_passengers=1")
			.headers(headers_4)
			.resources(http("request_9")
			.get("/assets/application-2534172286055efef05dbb34d2da8fc2.js"),
            http("request_10")
			.get("/assets/application-c99cbb3caf78d16bb1482ca2e41d7a9c.css"),
            http("request_11")
			.get("/favicon.ico"))
		.check(css("h1:contains('Book Flight')").exists)  //Check if the page heading contains Book Flight
		.check(substring("Email").find.exists)  //check if String Email is present in the page
		.check(substring("Email").count.is(1))  //check if the count of Email is 1
			.check(css("input[name='authenticity_token']","value").saveAs("authToken")) //retrieve the auth token value from response body (name="authenticity_token" value="FtXi/O4Jf8okWMeOnjdJL0XMxG+aomo35kfiI793navVl98pvQ82BTBLOR9oEs8hI2nYEZFHAE6UH3vqYgV1pQ==" />) and pass it in other step

			.check(bodyString.saveAs("BODY"))) //Save the entire response in BODY variable
  	.exec{
			session =>																//Print the entire response which is in body variable as String
				println(session("BODY").as[String])
				session
		}
		.pause(11)

//		.feed(csvFeeder) // feed the csv file containing name and email

//			.feed(csvFeeder, 2) //if we have two pair of data to be filled when we had selected two passengers. So below also we should have used {name1}, {email1} and {name2}, {email2}

  	.feed(randomEmailFeeder)
  	.feed(randomNameFeeder)
		.exec(http("BookFlights")
			.post("/bookings")
			.headers(headers_12)
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${authToken}")
			.formParam("booking[flight_id]", "64")
			.formParam("booking[passengers_attributes][0][name]", "${randomName}")
			.formParam("booking[passengers_attributes][0][email]", "${randomEmail}")
			.formParam("commit", "Book Flight")
			.resources(http("request_13")
			.get("/favicon.ico"))
			.check(status.is(500)))
  		.exec{
				session =>
					println(session("randomName").as[String])
					session
			}

	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
  	.assertions(
			global.responseTime.max.lt(1000), //the cumulative response time of all the requests in your simulation should be less than 1000
			forAll.responseTime.max.lt(1000),  //response time of each request should not exceed 1000
			details("BookFlights").responseTime.max.lt(1000), //the resopnse time of bookFlight request should not exceed thousand

			global.successfulRequests.percent.is(100)  //our successful request percentage should be 100
		)
}