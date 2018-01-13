package utils

import java.net.URI

import play.api.mvc.RequestHeader

trait SameOriginCheck {
  /**
    * Checks that the WebSocket comes from the same origin.  This is necessary to protect
    * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
    *
    * See https://tools.ietf.org/html/rfc6455#section-1.3 and
    * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
    */
  def sameOriginCheck(implicit rh: RequestHeader): Boolean = {
    // The Origin header is the domain the request originates from.
    // https://tools.ietf.org/html/rfc6454#section-7
//    println("Checking the ORIGIN ")

    rh.headers.get("Origin") match {
      case Some(originValue) if originMatches(originValue) =>
//        println(s"originCheck: originValue = $originValue")
        true

      case Some(badOrigin) =>
//        println(s"originCheck: rejecting request because Origin header value ${badOrigin} is not in the same origin")
        false

      case None =>
//        println("originCheck: rejecting request because no Origin header found")
        false
    }
  }

  /**
    * Returns true if the value of the Origin header contains an acceptable value.
    */
  private def originMatches(origin: String): Boolean = {
    try {
      val url = new URI(origin)
      url.getHost == "localhost" &&
      (url.getPort match { case 9000 | 3000 | 19001 => true; case _ => false })
    } catch {
      case e: Exception => false
    }
  }
}
