package com.syncapse.avon

import org.specs.Specification
import java.lang.String
import xml.{Elem, XML}
import org.opensaml.xml.util.Base64

class AvonUtilSpec extends Specification {
  "Must be valid XML" in {
    val xml: String = AvonUtil.samlResponseString(UserInfo.Test0)
    XML.loadString(xml)
  }

  "Must be valid XML from base64 encoded String" in {
    val xml: String = AvonUtil.samlResponseString(UserInfo.Test1)
    val encoded: String = AvonUtil.base64encode(xml)

    val bytes: Array[Byte] = Base64.decode(encoded)

    XML.loadString(new String(bytes, "utf-8"))
  }


}