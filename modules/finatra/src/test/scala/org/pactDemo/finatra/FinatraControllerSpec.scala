package org.pactDemo.finatra

import com.twitter.finatra.http.{Controller, EmbeddedHttpServer}
import com.twitter.inject.server.{FeatureTest, FeatureTestMixin}
import org.pactDemo.utilities.PactDemoSpec
import org.scalatest.BeforeAndAfterAll

trait FinatraControllerSpec extends PactDemoSpec with FeatureTestMixin with BeforeAndAfterAll {

  def controllerUnderTest: Controller

  private lazy val c = {
    val result = controllerUnderTest
    if (result == null) throw new NullPointerException("You need to make the controllerUnderTest a def")
    result
  }
  lazy val server = new EmbeddedHttpServer(new FinatraServer(0, c)) //the port is ignored

  override def beforeAll(): Unit = {

  }

  override def afterAll(): Unit = {
    server.close
  }

}
