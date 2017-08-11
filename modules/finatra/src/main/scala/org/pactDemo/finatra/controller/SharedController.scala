package org.pactDemo.finatra.controller

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.pactDemo.finatra.structure.ServiceTree


class SharedController(tree: ServiceTree[_,_,_]) extends Controller{

  get("/ping"){_: Request  => response.ok("pong")}

}
