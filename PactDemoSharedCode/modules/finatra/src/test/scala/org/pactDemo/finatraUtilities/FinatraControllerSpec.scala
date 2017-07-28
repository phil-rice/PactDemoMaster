package org.pactDemo.finatraUtilities

import com.twitter.finatra.http.{Controller, EmbeddedHttpServer}
import com.twitter.inject.server.{FeatureTest, FeatureTestMixin}
import org.pactDemo.utilities.PactDemoSpec

trait FinatraControllerSpec extends PactDemoSpec with FeatureTestMixin   {

  def controllerUnderTest: Controller

  private lazy val c = {
    val result = controllerUnderTest
    if (result == null) throw new NullPointerException("You need to make the controllerUnderTest a def")
    result
  }
  lazy val server = new EmbeddedHttpServer(new FinatraServer(0, c)) //the port is ignored

}
