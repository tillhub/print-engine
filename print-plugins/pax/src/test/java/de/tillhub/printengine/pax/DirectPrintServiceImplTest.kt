package de.tillhub.printengine.pax

import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import de.tillhub.printengine.pax.DirectPrintService.DirectPrintListener
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.robolectric.RobolectricTest
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@RobolectricTest
class DirectPrintServiceImplTest :
    FunSpec({
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

            // Reported to the listener for the printer state, and rethrown so the caller of
            // startPrintJob does not get a Success for a receipt that was never sent.
            val thrown = shouldThrow<RemoteException> {
                target.print("payload", 50, listener)
            }

            thrown shouldBe ex
            verify {
                listener.onFailed(ex)
            }
        }

        test("checkStatus + exception") {
            val ex = RemoteException()
            every { requestMessenger.send(any()) } throws ex

            // A failed status check only leaves the state unknown; it runs from the controller's
            // constructor, so it must not throw.
            target.checkStatus(listener)

            verify {
                listener.onFailed(ex)
            }
        }
    })
