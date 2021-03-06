package com.wix.sms.cellact.it

import com.google.api.client.http.javanet.NetHttpTransport
import com.wix.sms.SmsErrorException
import com.wix.sms.cellact.testkit.CellactDriver
import com.wix.sms.cellact.{CellactSmsGateway, Credentials}
import com.wix.sms.model.{Sender, SmsGateway}
import org.specs2.mutable.SpecWithJUnit
import org.specs2.specification.Scope

class CellactSmsGatewayIT extends SpecWithJUnit {
  private val cellactPort = 10007

  val driver = new CellactDriver(port = cellactPort)
  step {
    driver.start()
  }

  sequential

  trait Ctx extends Scope {
    val requestFactory = new NetHttpTransport().createRequestFactory()

    val someCredentials = Credentials(
      company = "some company",
      username = "some username",
      password = "some password"
    )
    val someDestPhone = "+12125551234"
    val someSenderPhone = "+12125554321"
    val somePlainText = "some plain text"
    val someUnicodeText = "some יוניקוד text"
    val someMessageId = "someMessageId"

    val cellact: SmsGateway = new CellactSmsGateway(
      requestFactory = requestFactory,
      endpoint = s"http://localhost:$cellactPort/",
      credentials = someCredentials
    )

    driver.reset()
  }

  "sendPlain" should {
    "successfully yield a message ID on valid request" in new Ctx {
      driver.aSendPlainFor(
        credentials = someCredentials,
        source = someSenderPhone,
        destPhone = someDestPhone,
        text = somePlainText
      ) returns(
        msgId = someMessageId
      )

      cellact.sendPlain(
        sender = Sender(
          phone = Some(someSenderPhone)
        ),
        destPhone = someDestPhone,
        text = somePlainText
      ) must beASuccessfulTry(
        check = ===(someMessageId)
      )
    }

    "gracefully fail on error" in new Ctx {
      val someReturnCode = "some code"
      val someReturnMessage = "some message"

      driver.aSendPlainFor(
        credentials = someCredentials,
        source = someSenderPhone,
        destPhone = someDestPhone,
        text = somePlainText
      ) failsWith(
        code = someReturnCode,
        message = someReturnMessage
      )

      cellact.sendPlain(
        sender = Sender(
          phone = Some(someSenderPhone)
        ),
        destPhone = someDestPhone,
        text = somePlainText
      ) must beAFailedTry.like {
        case e: SmsErrorException => e.message must (contain(someReturnCode) and contain(someReturnMessage))
      }
    }
  }

  "sendUnicode" should {
    "successfully yield a message ID on valid request" in new Ctx {
      driver.aSendUnicodeFor(
        credentials = someCredentials,
        source = someSenderPhone,
        destPhone = someDestPhone,
        text = someUnicodeText
      ) returns(
        msgId = someMessageId
      )

      cellact.sendUnicode(
        sender = Sender(
          phone = Some(someSenderPhone)
        ),
        destPhone = someDestPhone,
        text = someUnicodeText
      ) must beASuccessfulTry(
        check = ===(someMessageId)
      )
    }

    "gracefully fail on error" in new Ctx {
      val someReturnCode = "some code"
      val someReturnMessage = "some message"

      driver.aSendUnicodeFor(
        credentials = someCredentials,
        source = someSenderPhone,
        destPhone = someDestPhone,
        text = someUnicodeText
      ) failsWith(
        code = someReturnCode,
        message = someReturnMessage
      )

      cellact.sendUnicode(
        sender = Sender(
          phone = Some(someSenderPhone)
        ),
        destPhone = someDestPhone,
        text = someUnicodeText
      ) must beAFailedTry.like {
        case e: SmsErrorException => e.message must (contain(someReturnCode) and contain(someReturnMessage))
      }
    }
  }

  step {
    driver.stop()
  }
}
