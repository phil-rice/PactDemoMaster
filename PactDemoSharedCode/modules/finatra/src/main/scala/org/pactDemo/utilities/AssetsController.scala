package org.pactDemo.utilities

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class AssetsController extends Controller{
  get ("/"){request: Request => response.ok.file("/public/index.html")}
  get ("/index.html"){request: Request => response.ok.file("/public/index.html")}

  get("/assets/:*") { request: Request =>
    response.ok.file("/public/" + request.params("*"))
  }
}
