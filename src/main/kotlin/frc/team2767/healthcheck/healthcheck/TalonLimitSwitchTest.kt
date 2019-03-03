package frc.team2767.healthcheck.healthcheck

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import frc.team2767.healthcheck.healthcheck.TalonLimitSwitchTest.State.*
import kotlinx.html.TagConsumer
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.tr
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Suppress("MemberVisibilityCanBePrivate")
class TalonLimitSwitchTest(private val group: TalonGroup) : Test, Reportable {
    override var name = "limit switch test"
    var percentOutput = 0.0
    var peakVoltage = 12.0
    var encoderTarget = 0
    var normallyOpen = true
    var reverseLimit = true


    private var state = STARTING
    private lateinit var talon: TalonSRX
    private var passed = false

    override fun execute() {
        when (state) {
            STARTING -> {
                if (group.talons.size != 1) {
                    logger.error { "limit switch test valid for one talon, has ${group.talons.size}, skipping" }
                    state = STOPPED
                    return
                }
                logger.info { "$name starting" }
                talon = group.talons.first()
                state = if (reverseLimit) REVERSE else FORWARD
            }

            REVERSE -> {}

            STOPPED -> logger.info { "limit switch test stopped" }
        }
    }

    override fun isFinished() = state == STOPPED

    override fun report(tagConsumer: TagConsumer<Appendable>) = reportTable(tagConsumer)

    override fun reportHeader(tagConsumer: TagConsumer<Appendable>) {
        tagConsumer.tr {
            th { +"talon ID" }
            th { +"Check" }
            th { +"Passed" }
        }
    }

    override fun reportRows(tagConsumer: TagConsumer<Appendable>) {
        tagConsumer.tr {
            td { +"${talon.deviceID}" }
            td { +name }
            td { +"$passed" }
        }

    }

    @Suppress("unused")
    private enum class State {
        STARTING,
        REVERSE,
        FORWARD,
        STOPPED
    }
}