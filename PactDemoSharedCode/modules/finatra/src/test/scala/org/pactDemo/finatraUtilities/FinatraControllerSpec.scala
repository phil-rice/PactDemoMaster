package org.pactDemo.finatraUtilities

import com.twitter.finatra.http.{Controller, EmbeddedHttpServer}
import com.twitter.inject.server.FeatureTestMixin
import org.pactDemo.utilities.PactDemoSpec

trait FinatraControllerSpec extends PactDemoSpec with FeatureTestMixin {

  def controllerUnderTest: Controller

  private val c = controllerUnderTest
  if (c == null) throw new NullPointerException("You need to make the controllerUnderTest a def")
  val server = new EmbeddedHttpServer(new FinatraServer(0, c)) //the port is ignored

}
