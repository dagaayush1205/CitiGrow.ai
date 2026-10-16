package com.example.citigrow

import com.example.citigrow.data.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

/** Also executable on the JVM with the SDK's real org.json implementation. */
class GrowRulesTest {
    private fun student() = JSONObject("""{"age":15,"edu":"10","state":"Tamil Nadu","income":"unknown","discovery":{"traits":{"practical":3,"tech":2,"outdoors":2},"experiences":["pump"],"constraints":{"nearHome":true,"budget":"limited"}},"assessment":{"levels":{"practical":3,"numeracy":2}}}""")
    private fun solar() = JSONObject("""{"title":"Solar Technician","w":{"practical":3,"tech":2,"outdoors":2},"need":{"practical":2,"numeracy":2},"exp":["pump"],"relocate":false,"cost":"low"}""")
    @Test fun freshProfileHasNoInventedMatch() { assertNull(GrowRules.match(JSONObject(), solar())) }
    @Test fun livedExperienceAndNearHomeImproveMatch() {
        val p = student(); val withExperience = GrowRules.match(p, solar())!!
        p.obj("discovery").put("experiences", JSONArray()); p.obj("discovery").obj("constraints").put("nearHome", false)
        assertTrue(withExperience.percent > GrowRules.match(p, solar())!!.percent)
        assertTrue(withExperience.why.contains("everyday experience"))
        assertTrue(withExperience.percent in 30..96)
    }
    @Test fun skillGapsAreExplained() {
        val p = student(); p.obj("assessment").obj("levels").put("numeracy", 1)
        assertEquals(listOf("numeracy"), GrowRules.match(p, solar())!!.gaps)
    }
    @Test fun preparationAdaptsToDailyTime() {
        assertEquals(20, GrowRules.weeks(60, 30)); assertEquals(40, GrowRules.weeks(60, 15))
        assertEquals(10, GrowRules.weeks(60, 60)); assertTrue(GrowRules.weeks(60, 0) > 0)
    }
    @Test fun unknownIncomeRequiresInformation() {
        val opp = JSONObject("""{"rules":[{"t":"income","v":"₹3.5 lakh"}]}""")
        assertEquals("More information needed", GrowRules.eligibility(student(), opp).status)
    }
    @Test fun unmetRequirementWinsOverMissingInformation() {
        val opp = JSONObject("""{"rules":[{"t":"age","min":18},{"t":"income","v":"₹3.5 lakh"}]}""")
        assertEquals("Does not meet a requirement", GrowRules.eligibility(student(), opp).status)
    }
    @Test fun beingInClassDoesNotMeanPassingClass() {
        val opp = JSONObject("""{"rules":[{"t":"pass","v":10}]}""")
        assertEquals("More information needed", GrowRules.eligibility(student(), opp).status)
    }
    @Test fun confirmationsCanBeUnknownYesOrNo() {
        val p = student(); val opp = JSONObject("""{"id":"o1","rules":[{"t":"confirm","id":"exam","q":"Passed the exam?"}]}""")
        assertEquals("More information needed", GrowRules.eligibility(p, opp).status)
        p.obj("confirms").put("o1:exam", true); assertEquals("Appears eligible", GrowRules.eligibility(p, opp).status)
        p.obj("confirms").put("o1:exam", false); assertEquals("Does not meet a requirement", GrowRules.eligibility(p, opp).status)
    }
    @Test fun assessmentAggregatesQuestionsBySkill() {
        val items = JSONArray("""[{"skill":"numeracy","a":0},{"skill":"numeracy","a":1},{"skill":"reading","a":2}]""").objects()
        val levels = GrowRules.assess(items, listOf(0, 0, 2)).obj("levels")
        assertEquals(2, levels.optInt("numeracy")); assertEquals(3, levels.optInt("reading"))
        assertEquals(1, GrowRules.assess(items, listOf(2, 2, 0)).obj("levels").optInt("numeracy"))
    }
    @Test fun cachedLessonCompletionIsIdempotent() {
        val done = JSONArray(); done.addUnique("c4|0"); done.addUnique("c4|0"); assertEquals(1, done.length())
    }
}
