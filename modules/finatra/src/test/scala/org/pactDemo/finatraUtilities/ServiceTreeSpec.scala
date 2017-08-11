package org.pactDemo.finatraUtilities

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import org.pactDemo.utilities.PactDemoSpec
import org.scalatest.mockito.MockitoSugar

case class MockRequest(someUrl: String)

object MockRequest {

  implicit object MockRequestToRequest extends ToRequest[MockRequest] {
    override def apply(v1: MockRequest): Request = Request(v1.someUrl)
  }

}


case class MockResponse(request: MockRequest, statusCode: Int, bodyContent: String, contentType: Option[String])

object MockResponse {

  implicit object MockResponseFromResponse extends FromResponse[MockRequest, MockResponse] {
    override def apply(v1: MockRequest, v2: Response): MockResponse = MockResponse(v1, v2.status.code, v2.contentString, v2.contentType)
  }

}

trait ServiceLanguageFixture extends MockitoSugar {
  implicit val adapter = NullSl4jLoggingAdapter
  implicit val logger = new SimpleLogMe

  type FinagleService = Request => Future[Response]
  type MockService = MockRequest => Future[MockResponse]
  val baseUrl = "someBaseUrl:80"

  import ServiceLanguage._

  def withMocks(fn: (ServiceTree[MockRequest, MockResponse, ServiceDescription], FinagleService) => Unit): Unit = {
    val finagleService = mock[FinagleService]
    val root = RootServiceTree[Request, Response, ServiceDescription](ServiceDescription("MockHttp"), () => finagleService)
    val tree = root >--< logging("name", "prefix") >--< addHostName(baseUrl) >--< objectify[MockRequest, MockResponse] >--< logging("secondName", "secondPrefix")
    fn(tree, finagleService)
  }

}

class ServiceTreeSpec extends PactDemoSpec with ServiceLanguageFixture {

  import org.mockito.Mockito._
  import Futures._

  behavior of "ServiceTree"


  val response = Response(Status.EnhanceYourCalm)
  response.contentType = "text/someType"
  response.contentString = "someContentString"

  it should "allow service trees to be built" in {
    withMocks { (tree, http) =>
      val captor = capture[Request]
      when(http.apply(captor.capture())) thenReturn Future.value(response)

      val mockRequest = MockRequest("someUrl")
      tree.service(mockRequest).await shouldBe MockResponse(mockRequest, 420, "someContentString", Some("text/someType"))
      val request = captor.getValue
      request.uri shouldBe "someUrl"
    }
  }

  private val mockHttpName = "MockHttp"
  private val loggingClientName = "LoggingClient(name,prefix)"
  private val addHostName = "AddHostNameService(someBaseUrl)"
  private val objectifyName = "GenericCustomClient"
  private val secondLoggingClientName = "LoggingClient(secondName,secondPrefix)"
  it should "allow trees to be mapped and folded" in {
    withMocks { (tree, http) =>
      tree.map(_.description).foldToList shouldBe List(mockHttpName, loggingClientName, addHostName, objectifyName, secondLoggingClientName)
      tree.map(_.description).foldToListOfTrees.map(_.payload) shouldBe List(mockHttpName, loggingClientName, addHostName, objectifyName, secondLoggingClientName)
    }
  }

  it should "allow 'mapFromTree'" in {
    withMocks { (tree, http) =>
      tree.mapFromTree(_.payload.description).foldToList shouldBe List(mockHttpName, loggingClientName, addHostName, objectifyName, secondLoggingClientName)
    }
  }

  it should "allow services to be found" in {
    withMocks { (tree, http) =>
      tree.filter(_.service.getClass == classOf[LoggingClient[_, _]]).map(_.payload.description) shouldBe List(loggingClientName, secondLoggingClientName)
      tree.collect { case x: DelegateTree0[_, _, ServiceDescription] if x.service.getClass == classOf[LoggingClient[_, _]] => x }.map(_.payload.description) shouldBe List(loggingClientName, secondLoggingClientName)
      tree.findAll[LoggingClient[_, _]].map(_.payload.description) shouldBe List(loggingClientName, secondLoggingClientName)
    }
  }

  it should "allow foldToListOfTreesAndDepth" in {
    withMocks { (tree, http) =>
      tree.foldToListOfTreesAndDepth.map { case (t, d) => (t.payload.description, d) } shouldBe List((mockHttpName, 4), (loggingClientName, 3), (addHostName, 2), (objectifyName, 1), (secondLoggingClientName, 0))
    }
  }

  it should "allow findAll ignoring the actual types of the Req and Res" in {
    withMocks { (tree, http) =>
      tree.findAll[LoggingClient[_, _]].map(_.payload.description) shouldBe List(loggingClientName, secondLoggingClientName)
      tree.findAll[LoggingClient[Request, Response]].map(_.payload.description) shouldBe List(loggingClientName, secondLoggingClientName)
      tree.findAll[LoggingClient[String, Int]].map(_.payload.description) shouldBe List(loggingClientName, secondLoggingClientName)
    }
  }

  it should "allow findAllWithReqRes with ReqRes" in {
    withMocks { (tree, http) =>
      tree.findAllWithReqRes[Request, Response].map(_.payload.description) shouldBe List(mockHttpName, loggingClientName, addHostName)
      tree.findAllWithReqRes[MockRequest, MockResponse].map(_.payload.description) shouldBe List(objectifyName, secondLoggingClientName)
    }
  }
  it should "allow findAllWithServiceReqRes " in {
    withMocks { (tree, http) =>
      tree.findAllTreesWithServiceReqRes[Request, Response, LoggingClient[Request, Response]].map(_.payload.description) shouldBe List(loggingClientName)
      tree.findAllTreesWithServiceReqRes[MockRequest, MockResponse, LoggingClient[MockRequest, MockResponse]].map(_.payload.description) shouldBe List(secondLoggingClientName)
    }
  }
}