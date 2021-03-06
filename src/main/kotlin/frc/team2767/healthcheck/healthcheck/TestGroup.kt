package frc.team2767.healthcheck.healthcheck

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import frc.team2767.healthcheck.healthcheck.TestGroup.State.*
import kotlinx.html.TagConsumer
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.table
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

abstract class TestGroup(val healthCheck: HealthCheck) : Test {
    override var name = "name not set"

    protected val tests = mutableListOf<Test>()
    private var state = STARTING
    private lateinit var iterator: Iterator<Test>
    private lateinit var currentTest: Test


    override fun execute() = when (state) {
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

    override fun isFinished() = state == State.STOPPED

    override fun report(tagConsumer: TagConsumer<Appendable>) {
        tagConsumer.div {
            h2 { +name }
            table {
                val test = tests.first() as Reportable
                test.reportHeader(tagConsumer)
                tests.map { it as Reportable }.forEach { it.reportRows(tagConsumer) }
            }
        }
    }


    override fun toString(): String {
        return "TestGroup(name='$name', tests=$tests)"
    }


    private enum class State {
        STARTING,
        RUNNING,
        STOPPED
    }

}


class TalonGroup(healthCheck: HealthCheck) : TestGroup(healthCheck) {
    var talons = emptyList<TalonSRX>()


    fun timedTest(init: TalonTimedTest.() -> Unit): Test {
        val spinTest = TalonTimedTest(this)
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
