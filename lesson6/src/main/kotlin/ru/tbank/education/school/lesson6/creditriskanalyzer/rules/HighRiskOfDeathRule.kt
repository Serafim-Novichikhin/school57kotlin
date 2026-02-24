package ru.tbank.education.school.lesson6.creditriskanalyzer.rules

import ru.tbank.education.school.lesson6.creditriskanalyzer.models.Client
import ru.tbank.education.school.lesson6.creditriskanalyzer.models.PaymentRisk
import ru.tbank.education.school.lesson6.creditriskanalyzer.models.Region
import ru.tbank.education.school.lesson6.creditriskanalyzer.models.ScoringResult
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.UUID

fun println(str: String) {
    val writer = OutputStreamWriter(System.out, StandardCharsets.UTF_8)
    writer.write((str+"\n"))
    writer.flush()
}

enum class Gender {
    MALE, FEMALE, MOSTLYMALE, MOSTLYFEMALE
}
class HighRiskOfDeathRule() : ScoringRule {

    override val ruleName: String = "Hidh-Risk Of Death"

    override fun evaluate(client: Client): ScoringResult {
        val consonant_letters = arrayOf("б", "в", "г", "д", "ж", "з", "й", "к", "л", "м", "н", "п", "р", "с", "т", "ф", "х", "ц", "ч", "ш", "щ", "ь", "ъ")
        val age = client.age
        val regeon = client.region
        val fullName = arrayOf(client.fullName.split(" "))
        val gender = when {
            fullName.size > 2 -> if (fullName[2].last() == "ч") Gender.MALE else Gender.FEMALE
            else -> if (fullName[0].last() in consonant_letters) Gender.MOSTLYMALE else Gender.MOSTLYFEMALE
        }
        val spread = 7
        val base = mapOf(Region.MOSCOW to Pair(68, 78), Region.KAZAN to Pair(64, 71), Region.NOVOSIBIRSK to Pair(66, 78), Region.SPB to Pair(71, 79), Region.OTHER to Pair(68, 78))
        return ScoringResult(ruleName, when(gender) {
            Gender.MALE -> if (age < base[regeon]!!.first * (spread - 1) / spread) PaymentRisk.LOW else if (age <= base[regeon]!!.first * (spread + 1) / spread) PaymentRisk.MEDIUM else PaymentRisk.HIGH
            Gender.FEMALE -> if (age < base[regeon]!!.second * (spread - 1) / spread) PaymentRisk.LOW else if (age <= base[regeon]!!.second * (spread + 1) / spread) PaymentRisk.MEDIUM else PaymentRisk.HIGH
            Gender.MOSTLYMALE -> if (age < (base[regeon]!!.first + 3) * (spread - 1) / spread) PaymentRisk.LOW else if (age <= (base[regeon]!!.first + 3) * (spread + 1) / spread) PaymentRisk.MEDIUM else PaymentRisk.HIGH
            else -> if (age < (base[regeon]!!.second - 3) * (spread - 1) / spread) PaymentRisk.LOW else if (age <= (base[regeon]!!.second - 3) * (spread + 1) / spread) PaymentRisk.MEDIUM else PaymentRisk.HIGH
        })
    }
}

fun main() {
    val highRiskOfDeathRule = HighRiskOfDeathRule()
    for (i in 20..120)
        println("Возраст: " + i + ", Степень риска скорой смерти: " + highRiskOfDeathRule.evaluate(Client(UUID.randomUUID(), "Иванов Иван Иванович", i, Region.OTHER)).score)
}

