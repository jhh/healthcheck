package frc.team2767.healthcheck


import edu.wpi.first.wpilibj.TimedRobot
import edu.wpi.first.wpilibj.command.Scheduler
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import frc.team2767.healthcheck.command.HealthCheckCommand
import mu.KotlinLogging


private val logger = KotlinLogging.logger {}


class Robot : TimedRobot() {

    override fun robotInit() {
        SmartDashboard.putData(HealthCheckCommand())
    }

    override fun teleopPeriodic() {
        Scheduler.getInstance().run()
    }
}
