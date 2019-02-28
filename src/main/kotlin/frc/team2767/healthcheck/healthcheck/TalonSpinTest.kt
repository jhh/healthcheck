package frc.team2767.healthcheck.healthcheck

import com.ctre.phoenix.motorcontrol.ControlMode.PercentOutput
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.Timer
import frc.team2767.healthcheck.healthcheck.TalonSpinTest.State.*
import kotlinx.html.TagConsumer
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.tr
import mu.KotlinLogging
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger {}

@Suppress("MemberVisibilityCanBePrivate")
class TalonSpinTest(private val group: TalonGroup) : Test("Talon Spins") {
    var percentOutput = 0.0
    var currentRange = 0.0..0.0
    var speedRange = 0..0
    var warmUp = 0.25
    var duration = 2.0

    private var state = State.STARTING
    private var startTime = 0.0
    private var iterations = 0
    private var iteration = 0
    private lateinit var talonCurrents: Map<TalonSRX, DoubleArray>
    private lateinit var talonSpeeds: Map<TalonSRX, IntArray>

    override fun execute() {
        when (state) {
            STARTING -> {
                name = "speed test at ${percentOutput * 12.0} volts"
                logger.info { "$name starting" }
                iterations = (duration / group.healthCheck.period).roundToInt()
                talonCurrents = group.talons.associateWith { DoubleArray(iterations) }
                talonSpeeds = group.talons.associateWith { IntArray(iterations) }
                group.talons.forEach { it.set(PercentOutput, percentOutput) }
                startTime = Timer.getFPGATimestamp()
                state = WARMING
            }

            WARMING -> if (Timer.getFPGATimestamp() - startTime >= warmUp) {
                state = RUNNING
            }

            RUNNING -> {
                talonCurrents.forEach { talon, currents -> currents[iteration] = talon.outputCurrent }
                talonSpeeds.forEach { talon, speeds -> speeds[iteration] = talon.selectedSensorVelocity }
                if (++iteration == iterations) state = STOPPING
            }

            STOPPING -> {
                group.talons.forEach { it.set(PercentOutput, 0.0) }

                talonCurrents.forEach { talon, currents ->
                    logger.info { "talon ${talon.deviceID} average current = ${currents.average()}" }
                }
                talonSpeeds.forEach { talon, speeds ->
                    logger.info { "talon ${talon.deviceID} average speed = ${speeds.average()}" }
                }
                logger.info { "spin test finished" }
                state = STOPPED
            }

            STOPPED -> logger.info { "speed test stopped" }

        }
    }

    override fun isFinished() = state == STOPPED

    override fun reportHeader(tagConsumer: TagConsumer<Appendable>) {
        tagConsumer.tr {
            th { +"talon ID" }
            th { +"Setpoint (volts)" }
            th { +"Duration (sec)" }
            th { +"Current (amps)" }
            th { +"Speed (ticks/100ms)" }
        }
    }

    override fun results(tagConsumer: TagConsumer<Appendable>) {
        group.talons.forEach {
            tagConsumer.tr {
                td { +"${it.deviceID}" }
                td { +"%.2f".format(percentOutput * 12.0) }
                td { +"%.2f".format(duration) }
                td { +"%.2f".format(talonCurrents[it]?.average()) }
                td { +"%.2f".format(talonSpeeds[it]?.average()) }
            }
        }
    }

    private enum class State {
        STARTING,
        WARMING,
        RUNNING,
        STOPPING,
        STOPPED
    }
}


