package frc.team2767.healthcheck.command

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.command.Command
import frc.team2767.healthcheck.healthcheck.HealthCheck
import frc.team2767.healthcheck.healthcheck.TalonGroup
import frc.team2767.healthcheck.healthcheck.healthCheck

class HealthCheckCommand : Command() {

    private val talonsUnderTest = listOf(TalonSRX(6))

    private lateinit var healthCheck: HealthCheck

    override fun initialize() {
        healthCheck = healthCheck {


            talonCheck {
                name = "SOB Talons Spin Tests"
                talons = talonsUnderTest

                spinTests()
            }

            talonCheck {
                name = "SOB Talons Position Tests"
                talons = talonsUnderTest

                positionTest {
                    percentOutput = 0.25
                    encoderTarget = 2000
                    encoderTimeOutCount = 1000
                    controlMode = ControlMode.PercentOutput
                }


            }
        }
    }

    private fun TalonGroup.spinTests() {
        spinTest {
            percentOutput = 0.25
            currentRange = 0.0..0.5
            speedRange = 500..600
        }

        spinTest {
            percentOutput = -0.25
            currentRange = 0.0..0.5
            speedRange = 500..600
        }

        spinTest {
            percentOutput = -0.5
            currentRange = 0.0..0.5
            speedRange = 500..600
        }

        spinTest {
            warmUp = 0.5
            duration = 2.0
            percentOutput = 0.75
            currentRange = 0.6..1.2
            speedRange = 1000..1100
        }
    }

    override fun execute() {
        healthCheck.execute()
    }

    override fun isFinished() = healthCheck.isFinished()

    override fun end() {
        healthCheck.report()
    }
}