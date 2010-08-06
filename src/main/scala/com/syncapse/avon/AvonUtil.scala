package com.syncapse.avon

import org.opensaml.xml.util.Base64
import xml.Elem
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.entity.BufferedHttpEntity
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.{NameValuePair, HttpResponse, HttpHost}
import java.util.ArrayList
import org.slf4j.LoggerFactory
import org.apache.http.util._
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.conn.scheme.{PlainSocketFactory, Scheme, SchemeRegistry}
import org.apache.http.auth.{UsernamePasswordCredentials, AuthScope}
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager

object UserTypes {
  lazy private val random = new Random

  case object REP extends UserType {val name = "REP"}
  case object SL extends UserType {val name = "SL"}
  case object ZM extends UserType {val name = "ZM"}
  case object DIV extends UserType {val name = "DIV"}
  case object CUST extends UserType {val name = "CUST"}
  case object ALUM extends UserType {val name = "ALUM"}
  case object CORP extends UserType {val name = "CORP"}

  val values = REP :: SL :: ZM :: DIV :: CUST :: ALUM :: CORP :: Nil

  def randomUserType = values.apply(random.nextInt(values.length))
}

sealed trait UserType {def name: String}


trait UserInfo {
  def username: String

  def email: String

  def userType: UserType

  def accountId: Long
}

object AvonUtil {

  lazy val host = System.getProperty("test.host", "www.avonconnects.co.uk")
  lazy val port = System.getProperty("test.port", "80").toInt
  lazy val prefix = System.getProperty("test.prefix", "")
  lazy val httpHost = new HttpHost(host, port)

  def makeLoginRequest(client: HttpClient, info: Any) = info match {
    case (_, xml: String) =>
      val loginPost: HttpPost = new HttpPost(prefix + "/login.jspa")
      val params : java.util.List[NameValuePair] = new ArrayList
      params.add(new BasicNameValuePair("SAMLResponse", base64encode(xml)))
      loginPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"))
      Benchmark.measure("login") {
        val response: HttpResponse = client.execute(httpHost, loginPost)
        response.getEntity.consumeContent
        response.getStatusLine.getStatusCode
      }
  }

  def makeGetGroupsRequest(client: HttpClient, ui: UserInfo) = {
    val groupsGet = new HttpGet(prefix + "/people/"+ui.username+"?view=groups")
    Benchmark.measure("groups") {
      val response = client.execute(httpHost, groupsGet)
      response.getEntity.consumeContent
      response.getStatusLine.getStatusCode
    }
  }

  def makeGetCommunitiesRequest(client: HttpClient) = {
    val url: String = prefix + "/index.jspa"
    val communityGet = new HttpGet(url)

    Benchmark.measure("communities") {
      val response = client.execute(httpHost, communityGet)
      response.getEntity.consumeContent
      response.getStatusLine.getStatusCode
    }
  }

  def base64encode(xml: String) = {
    Base64.encodeBytes(xml.getBytes("utf-8"), Base64.DONT_BREAK_LINES)
  }

  def httpClient: HttpClient = {
    val schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), port));
    new DefaultHttpClient(new ThreadSafeClientConnManager(schemeRegistry))
  }

  def samlResponseString(ui: UserInfo): String =  {
    val id = "testCase-" + System.currentTimeMillis

    def samlResponseStringXml(id: String, ui: UserInfo) = {
      val xml = <samlp:Response xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol"
                                Destination="http://http://avon-uk2.hosted.jivesoftware.com/login.jspa" ID={id} InResponseTo="_f8beaf3fec8467b2215ea2ea62cb0e7b" IssueInstant="2010-07-02T17:19:48Z" Version="2.0">
        <saml:Issuer xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion">
          http://www.avon.com
        </saml:Issuer>
        <samlp:Status>
            <samlp:StatusCode Value="urn:oasis:names:tc:SAML:2.0:status:Success"/>
        </samlp:Status>
        <saml:Assertion xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" ID="lkanddpokkmkalfnakkchododfogekehnppecimp"
                        IssueInstant="2010-07-02T17:19:48Z" Version="2.0">
          <saml:Issuer>
            http://www.avon.com
          </saml:Issuer>
          <ds:Signature xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
            <ds:SignedInfo>
                <ds:CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
                <ds:SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/>
              <ds:Reference URI="#s2e24c3954dd965b2adf72c759a0fb97d9f34d7da9">
                <ds:Transforms>
                    <ds:Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/>
                    <ds:Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/>
                </ds:Transforms>
                  <ds:DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
                <ds:DigestValue>
                  +/O3V1+a01Uh0/aBq2T8ozsuSRA=
                </ds:DigestValue>
              </ds:Reference>
            </ds:SignedInfo>
            <ds:SignatureValue>
              JFEE6540UTnCawsLGzcTA+kcW2PKm5/7OrJk5cCz7Gsc75AchcstrISL5shIOH78jU0NZnhMgDzB
              BR6411Xywx3W0i0Ne8kFXwFAxq7XrwkqhZWBhayLHrecYViEZ3LFtug4p4GSqC+nmdLZRuUU+Ez+ LOt0rxtH8jnDhDcv7Ns=
            </ds:SignatureValue>
            <ds:KeyInfo>
              <ds:X509Data>
                <ds:X509Certificate>
                  MIICQDCCAakCBEeNB0swDQYJKoZIhvcNAQEEBQAwZzELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNh
                  bGlmb3JuaWExFDASBgNVBAcTC1NhbnRhIENsYXJhMQwwCgYDVQQKEwNTdW4xEDAOBgNVBAsTB09ZW5TU08xDTALBgNVBAMTBHRlc3QwHhcNMDgwMTE1MTkxOTM5WhcNMTgwMTEyMTkxOTM5WjBnMQsw
                  CQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEUMBIGA1UEBxMLU2FudGEgQ2xhcmExDDABgNVBAoTA1N1bjEQMA4GA1UECxMHT3BlblNTTzENMAsGA1UEAxMEdGVzdDCBnzANBgkqhkiG9w0B
                  AQEFAAOBjQAwgYkCgYEArSQc/U75GB2AtKhbGS5piiLkmJzqEsp64rDxbMJ+xDrye0EN/q1U5OfRkDsaN/igkAvV1cuXEgTL6RlafFPcUX7QxDhZBhsYF9pbwtMzi4A4su9hnxIhURebGEmxKW9qJNY
                  Js0Vo5+IgjxuEWnjnnVgHTs1+mq5QYTA7E6ZyL8CAwEAATANBgkqhkiG9w0BAQQFAAOBgQB3Pw/QzPKTPTYi9upbFXlrAKMwtFf2OW4yvGWWvlcwcNSZJmTJ8ARvVYOMEVNbsT4OFcfu2/PeYoAdiDA
                  cGy/F2Zuj8XJJpuQRSE6PtQqBuDEHjjmOQJ0rV/r8mO1ZCtHRhpZ5zYRjhRC9eCbjx9VrFax0JD/FfwWigmrW0Y0Q==
                </ds:X509Certificate>
              </ds:X509Data>
            </ds:KeyInfo>
          </ds:Signature>
          <saml:Subject>
            <saml:NameID Format="urn:oasis:names:tc:SAML:2.0:nameid-format:transient"
                         NameQualifier="http://www.avon.com">
              moblabffekijjffaegejcbdohkndegeicgbdanbe
            </saml:NameID>
            <saml:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer">
                <saml:SubjectConfirmationData InResponseTo="_f8beaf3fec8467b2215ea2ea62cb0e7b"
                                              NotOnOrAfter="2010-07-02T17:29:48Z"
                                              Recipient="http://avon-uk2.hosted.jivesoftware.com/login.jspa"/>
            </saml:SubjectConfirmation>
          </saml:Subject>
          <saml:Conditions NotBefore="2010-07-02T17:14:48Z" NotOnOrAfter="2010-07-02T17:29:48Z">
            <saml:AudienceRestriction>
              <saml:Audience>
                http://avon-romania.uat4.hosted.jivesoftware.com/login.jspa
              </saml:Audience>
            </saml:AudienceRestriction>
          </saml:Conditions>
          <saml:AuthnStatement AuthnInstant="2010-07-02T17:19:48Z"
                               SessionIndex="lkanddpokkmkalfnakkchododfogekehnppecimp">
            <saml:AuthnContext>
              <saml:AuthnContextClassRef>
                urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport
              </saml:AuthnContextClassRef>
            </saml:AuthnContext>
          </saml:AuthnStatement>
          <saml:AttributeStatement>
            <saml:Attribute Name="Username">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">{ui.username}</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="Email">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">{ui.email}</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="FirstName">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">{ui.username}First</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="LastName">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">{ui.username}Last</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="UserType">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">{ui.userType.name}</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="AccountId">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">{ui.accountId}</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="CustomerId">
                <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string"/>
            </saml:Attribute>
            <saml:Attribute Name="ParentRepId">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">3094235</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="UserEnabled">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">true</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="Birthday">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">552614400000</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="City">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">1 DECEMBRIE</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="County">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">ILFOV</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="DaytimePhone">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">567890</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="Division">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">06</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="EveningPhone">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">255429</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="LengthOfAssociation">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">19</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="ManagedDivisionsList">
                <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string"/>
            </saml:Attribute>
            <saml:Attribute Name="ManagedZonesList">
                <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string"/>
            </saml:Attribute>
            <saml:Attribute Name="MobilePhone">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">255429</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="PostalCode">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">077005</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="PreferredLanguageCode">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">ro_RO</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="RepAppointmentDate">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">1168819200000</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="RepClubLevelCode">
                <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string"/>
            </saml:Attribute>
            <saml:Attribute Name="SLAppointmentDate">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">1169510400000</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="SLLengthOfAssociation">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">54</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="State">
                <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string"/>
            </saml:Attribute>
            <saml:Attribute Name="Trendsetter">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">N</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="Zone">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">047</saml:AttributeValue>
            </saml:Attribute>
            <saml:Attribute Name="SLLevelCode">
              <saml:AttributeValue xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="xs:string">UL</saml:AttributeValue>
            </saml:Attribute>
          </saml:AttributeStatement>
          <Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
            <SignedInfo>
                <CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments"/>
                <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/>
              <Reference URI="#lkanddpokkmkalfnakkchododfogekehnppecimp">
                <Transforms>
                    <Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/>
                </Transforms>
                  <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
                <DigestValue>
                  uIJICitXtwXw8LnyshDjhmMPPvM=
                </DigestValue>
              </Reference>
            </SignedInfo>
            <SignatureValue>
              VNusrJRb7HX0afCst9J9V9j3ClUtbyzkzuTtPTCigNU/TRI1ssGeojPZpGVK6jHTDY+/NQoa4vPm
              svtUNM/v/Xr5T9W+euI5HMcu6LdTmISi1nA1N/WgDfc3wN+WAgC+3N+dRuKM5cHUp8wPnOMJoKId
              6BshDgFh+1ohVbgscrg=
            </SignatureValue>
            <KeyInfo>
              <KeyValue>
                <RSAKeyValue>
                  <Modulus>
                    o3awA8NQtkdrxE5rv1EC7eNG6uyKnfg6sae249/p9cl03BxzVxDlm9Rn0tQeH417/U5r654ch9KK
                    DcoLxHaYdCQzTH6EwBQ+5iSLmjSca2aT4waZhzSC61XxzLCr8E23nIdYanRLTKkMTT2zLv761f7B
                    8iTN8yXN1ZxuwL2Jrz8=
                  </Modulus>
                  <Exponent>
                    AQAB
                  </Exponent>
                </RSAKeyValue>
              </KeyValue>
            </KeyInfo>
          </Signature>
        </saml:Assertion>
      </samlp:Response>;
      xml
    }
    val content = samlResponseStringXml(id, ui)
    content.toString
  }
}