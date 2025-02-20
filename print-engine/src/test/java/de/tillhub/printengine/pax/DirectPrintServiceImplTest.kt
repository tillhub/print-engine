package de.tillhub.printengine.pax

import android.os.Handler
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import androidx.core.os.bundleOf
import de.tillhub.printengine.pax.DirectPrintService.DirectPrintListener
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.robolectric.shadows.ShadowLooper

@ExperimentalCoroutinesApi
@RobolectricTest
class DirectPrintServiceImplTest : FunSpec({
    lateinit var requestMessenger: Messenger
    lateinit var target: DirectPrintServiceImpl

    lateinit var listener: DirectPrintListener

    beforeTest {
        listener = mockk(relaxed = true)
        requestMessenger = mockk()

        target = DirectPrintServiceImpl(requestMessenger)
    }

    test("checkStatus") {
        var message: Message? = null
        every { requestMessenger.send(any()) } answers {
            message = firstArg()
        }

        target.checkStatus(listener)

        message?.what shouldBe 2
        message?.replyTo.shouldBeInstanceOf<Messenger>()
    }

    test("print") {
        var message: Message? = null
        every { requestMessenger.send(any()) } answers {
            message = firstArg()
        }

        target.print("payload", 50, listener)

        message?.what shouldBe 1
        message?.data?.getInt("grey") shouldBe 50
        message?.data?.getString("html") shouldBe "payload"
        message?.data?.getBoolean("autoCrop") shouldBe true
        message?.replyTo.shouldBeInstanceOf<Messenger>()
    }

    test("print + exception") {
        val ex = RemoteException()
        every { requestMessenger.send(any()) } throws ex

        target.print("payload", 50, listener)

        verify {
            listener.onFailed(ex)
        }
    }

    test("print + status check") {
        ShadowLooper.
        requestMessenger = Messenger(object : Handler() {
            override fun handleMessage(msg: Message) {
                val response = Message.obtain(null, 1, 0, 0).apply {
                    data = bundleOf(
                        "status" to 2
                    )
                }

                msg.replyTo.send(response)
            }
        })
        target = DirectPrintServiceImpl(requestMessenger)
//        every { requestMessenger.send(any()) } answers {
//            val message = firstArg<Message>()
//
//            val response = Message.obtain(null, 1, 0, 0).apply {
//                data = bundleOf(
//                    "status" to 2
//                )
//            }
//
//            message.replyTo.send(response)
//        }

        target.print("payload", 50, listener)

        verify {
            listener.onStatus(2)
        }
    }
})
