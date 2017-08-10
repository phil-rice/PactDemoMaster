package org.pactDemo.finatraUtilities

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller


class SharedController(tree: ServiceTree[_,_,_]) extends Controller{

  get("/ping"){_: Request  => response.ok("pong")}

}
