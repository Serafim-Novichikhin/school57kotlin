package ru.tbank.education.school.lesson8.homework.payments

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import ru.tbank.education.school.lesson8.homework.library.Book
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.time.YearMonth

class PaymentProcessorTest {
    private lateinit var processor: PaymentProcessor
    private lateinit var logCapture: ByteArrayOutputStream
    @BeforeEach
    fun setUp() {
        processor = PaymentProcessor()
        logCapture = ByteArrayOutputStream()
        System.setOut(PrintStream(logCapture))
    }

//    amount: Int,
//    cardNumber: String,
//    expiryMonth: Int,
//    expiryYear: Int,
//    currency: String,
//    customerId: String

    @ParameterizedTest(name = "Проверка на хороших данных: amount: {0}, cardNumber: {1}, expiryMonth: {2}, expiryYear: {3}, currency: {4}, customerId: {5}. Ожидаемое поведение: SUCCESS")
    @CsvSource(
        "1, 41234567891011, 1, 1, RUB, 1",
        "100, 41234567891011, 1, 1, EUR, 2",
        "100, 41234567891011, 1, 1, GBP, 1",
        "100, 41234567891011, 1, 1, JPY, 1",
        "100, 41234567891011, 1, 1, USD, 1",
        "100, 41234567891011, 1, 1, ABC, 1", // Почему это не ошибка?
        "100, 4000000000000002, 11, 0, USD, 1",
    )
    fun `successful data validation`(amount: Int, cardNumber: String, expiryMonth: Int, plusYears: Int, currency: String, customerId: String) {
        val res = processor.processPayment(amount, cardNumber, expiryMonth, YearMonth.now().year + plusYears, currency, customerId).status
        assertEquals("SUCCESS", res)

    }
    @ParameterizedTest(name = "Проверка на плохих данных: amount: {0}, cardNumber: {1}, expiryMonth: {2}, expiryYear: {3}, currency: {4}, customerId: {5}. Ожидаемое поведение: {6}")
    @CsvSource(
        //amount cardNumber expiryMonth plusYears currency customerId expected_throw
        "0, 41234567891011, 1, 1, USD, 1, THROW",
        "100, 44440000000006, 1, 1, EUR, 1, REJECTED",
        "1000000000, 41234567891011, 13, 1, GBP, 1, THROW",
        "100, 41234567891011, 1, -1, JPY, 1, THROW",
        "100, 41234567891011, 1, 1, RUB, '', THROW",
        "100, 123, 1, 1, EUR, 1, THROW",
        "100, 41234567891011, 1, 1, '', 1, THROW",
        "1, 41234567891011, 1, 1, EUR, 2, FAILED", // Почему это ошибка? (1 % 17 != 0)
        "10000000, 41234567891011, 1, 1, RUB, 1, FAILED",
        "1000, 0000000000000000, 1, 1, RUB, 1, FAILED",
        "100, 5500000000000004, 12, 1, USD, 1, FAILED",
        "1000, 41234567891011, -1, 1, GBP, 1, THROW",
        "100, 4000000000000002, 10, 0, USD, 1, THROW",
        "100, 4000000000000002, 0, 0, USD, 1, THROW",
        "100, 4000000000000002, 13, 0, USD, 1, THROW",
        "100, 40000000000000a2, 11, 3, USD, 1, THROW",
        "100, '12345678901234567890', 11, 3, USD, 1, THROW",
        "100, '              ', 11, 3, USD, 1, THROW",
        "100, 5500000000000004, 12, 1, USD, 1, FAILED",
        "100001, 4000000000000002, 12, 1, USD, 1, FAILED",


        )
    fun `testing on bad data`(amount: Int, cardNumber: String, expiryMonth: Int, plusYears: Int, currency: String, customerId: String, expected: String) {
        if (expected == "THROW") {
            assertThrows(IllegalArgumentException::class.java) {
                processor.processPayment(
                    amount,
                    cardNumber,
                    expiryMonth,
                    YearMonth.now().year + plusYears,
                    currency,
                    customerId
                )

            }
        } else {
            val res = processor.processPayment(
                amount,
                cardNumber,
                expiryMonth,
                YearMonth.now().year + plusYears,
                currency,
                customerId
            ).status
            assertEquals(expected, res)


        }
    }

    @ParameterizedTest(name = "Проверка уровней скидок и ограничений на данных: points: {0}, baseAmount: {1}")
    @CsvSource(
        "100, 0, 0, true",
        "10000, 100, 20, false",
        "5000, 100, 15, false",
        "2000, 100, 10, false",
        "500, 100, 5, false",
        "100, 100, 0, false",

        )
    fun `Discount testing`(points: Int, baseAmount: Int, expected: Int, expected_throw: Boolean) {
        if (expected_throw) {
            assertThrows(IllegalArgumentException::class.java) {
                processor.calculateLoyaltyDiscount(points, baseAmount)
            }
        } else {
            val res = processor.calculateLoyaltyDiscount(points, baseAmount)
            assertEquals(expected, res)


        }
    }

    @Test
    fun `bulkProcess with empty list must return empty result and logs info`() {
        val results = processor.bulkProcess(emptyList())
        assertTrue(results.isEmpty())
        assertTrue(logCapture.toString().contains("INFO"))
        assertTrue(logCapture.toString().contains("No payments to process"))
    }

    @Test
    fun `bulkProcess with all valid payments must return all success`() {
        val payments = listOf(
            PaymentData(100, "4000000000000002", 12, YearMonth.now().year + 1, "USD", "1"),
            PaymentData(200, "4000000000000010", 12, YearMonth.now().year + 1, "RUB", "2")
        )
        val results = processor.bulkProcess(payments)

        assertEquals(2, results.size)
        results.forEach { assertEquals("SUCCESS", it.status) }
        assertTrue(logCapture.toString().contains("[SUMMARY] Processed 2 payments: 2 success, 0 fail"))
    }

    @Test
    fun `Another tests with bulkProcess`() {
        val payments = listOf(
            PaymentData(100, "4000000000000002", 12, YearMonth.now().year + 1, "USD", "1"),
            PaymentData(-50, "4000000000000010", 12, YearMonth.now().year + 1, "RUB", "2"),
            PaymentData(200, "1234567812345678", 12, YearMonth.now().year + 1, "EUR", "3"),
            PaymentData(100_001, "4000000000000036", 12, YearMonth.now().year + 1, "USD", "4")
        )
        val results = processor.bulkProcess(payments)
        assertEquals(4, results.size)
        assertEquals("SUCCESS", results[0].status)
        assertEquals("REJECTED", results[1].status)
        assertEquals("REJECTED", results[2].status)
        assertEquals("FAILED", results[3].status)
        assertTrue(logCapture.toString().contains("[SUMMARY] Processed 4 payments: 1 success, 2 fail"))
        assertTrue(logCapture.toString().contains("REJECTED", ignoreCase = true))
        assertTrue(logCapture.toString().contains("[FRAUD_BLOCKED] Suspicious card detected: 1234567812345678"))
    }


}