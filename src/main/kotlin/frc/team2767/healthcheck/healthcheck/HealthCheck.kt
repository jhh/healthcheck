package frc.team2767.healthcheck.healthcheck

import edu.wpi.first.wpilibj.TimedRobot
import frc.team2767.healthcheck.healthcheck.HealthCheck.State.*
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import mu.KotlinLogging
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


private const val HTML_PATH = "/var/local/natinst/www/healthcheck/index.html"

private val logger = KotlinLogging.logger {}

class HealthCheck {
    var period = TimedRobot.kDefaultPeriod

    private val testGroups = mutableListOf<TestGroup>()
    private var state = STARTING
    private lateinit var iterator: Iterator<TestGroup>
    private lateinit var currentTestGroup: TestGroup


    fun talonCheck(init: TalonGroup.() -> Unit): TalonGroup {
        val test = TalonGroup(this)
        test.init()
        testGroups.add(test)
        return test
    }

    fun execute() = when (state) {
        STARTING -> {
            check(testGroups.isNotEmpty()) { "no tests groups" }
            iterator = testGroups.iterator()
            currentTestGroup = iterator.next()
            state = RUNNING
        }

        RUNNING -> if (!currentTestGroup.isFinished()) {
            currentTestGroup.execute()
        } else if (iterator.hasNext()) {
            currentTestGroup = iterator.next()
        } else {
            state = STOPPING
        }

        STOPPING -> {
            logger.info { "health check finished" }
            state = STOPPED
        }

        STOPPED -> throw IllegalStateException()
    }

    fun isFinished() = state == STOPPED

    fun report() {
        File(HTML_PATH).writer().use { writer ->
            writer.appendln("<!DOCTYPE html>")
            val tagConsumer = writer.appendHTML()
            tagConsumer.html {
                attributes["lang"] = "en"
                head {
                    title { +"Health Check" }
                    style {
                        unsafe {
                            raw(HealthCheck::class.java.getResource("/healthcheck.css").readText())
                        }
                    }
                }
                body {
                    h1 { +"Health Check  ${SimpleDateFormat("HH:mm:ss").format(Date())}" }
                    testGroups.forEach { it.report(tagConsumer) }
                }
            }
        }
        logger.info { "health check report: http://10.27.67.2/healthcheck/index.html" }
    }

    override fun toString(): String {
        return "HealthCheck(testGroups=$testGroups)"
    }


    private enum class State {
        STARTING,
        RUNNING,
        STOPPING,
        STOPPED
    }

}


fun healthCheck(init: HealthCheck.() -> Unit): HealthCheck = HealthCheck().apply(init)