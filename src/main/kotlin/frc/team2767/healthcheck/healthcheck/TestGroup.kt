package frc.team2767.healthcheck.healthcheck

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import frc.team2767.healthcheck.healthcheck.TestGroupState.*
import kotlinx.html.TagConsumer
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.table
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

abstract class TestGroup(val healthCheck: HealthCheck) {
    var name = "name not set"

    protected val tests = mutableListOf<Test>()
    private var state = STARTING
    private lateinit var iterator: Iterator<Test>
    private lateinit var currentTest: Test

    fun execute() = when (state) {
        STARTING -> {
            logger.info { "$name starting" }
            check(tests.isNotEmpty()) { "no tests in test group '$name'" }
            iterator = tests.iterator()
            currentTest = iterator.next()
            state = RUNNING
        }

        RUNNING -> if (!currentTest.isFinished()) {
            currentTest.execute()
        } else if (iterator.hasNext()) {
            currentTest = iterator.next()
        } else {
            logger.info { "$name finished" }
            state = STOPPED
        }

        STOPPED -> throw IllegalStateException()
    }

    fun isFinished() = state == STOPPED

    fun report(tagConsumer: TagConsumer<Appendable>) {
        tagConsumer.div {
            h2 { +name }
            table(classes = "testCards") {
                tests.first().reportHeader(tagConsumer)
                tests.forEach { it.results(tagConsumer) }
            }
        }

    }

}

class TalonGroup(healthCheck: HealthCheck) : TestGroup(healthCheck) {
    var talons = emptyList<TalonSRX>()


    fun spinTest(init: TalonSpinTest.() -> Unit): Test {
        val spinTest = TalonSpinTest(this)
        spinTest.init()
        tests.add(spinTest)
        return spinTest
    }

    fun positionTest(init: TalonPositionTest.() -> Unit): Test {
        val positionTest = TalonPositionTest(this)
        positionTest.init()
        tests.add(positionTest)
        return positionTest
    }
}

private enum class TestGroupState {
    STARTING,
    RUNNING,
    STOPPED
}
