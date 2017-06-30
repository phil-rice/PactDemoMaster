package org.pactDemo.provider

import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.inject.server.FeatureTest
import org.pactDemo.utilities.FinatraServer

class ProviderFeatureSpec extends FeatureTest {

  override val server = new EmbeddedHttpServer(new FinatraServer(9000, new ProviderController))

  test("ExampleServer#perform feature") {
    server.httpGet(
      path = "/id/1",
      andExpect = Status.Ok)
  }
}
