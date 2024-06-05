package de.tillhub.printengine.pax

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class PaxUtilsTest : FunSpec({

    test("printTextOptimizer") {
     val result = PaxUtils.printTextOptimizer(" €")
     result shouldBe "€"
    }

    test("chunkForPrinting") {
     val result = PaxUtils.chunkForPrinting(
      text = "1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n11\n12\n13\n14\n15\n16\n17",
      chunkSize = 4
     )
     result shouldBe listOf(
         "1\n2\n3\n4\n",
         "5\n6\n7\n8\n",
         "9\n10\n11\n12\n",
         "13\n14\n15\n16\n",
         "17\n"
     )
    }

    test("formatCode") {
     val result = PaxUtils.formatCode("barcode", 32)

     result shouldBe "            barcode             "
    }
})
