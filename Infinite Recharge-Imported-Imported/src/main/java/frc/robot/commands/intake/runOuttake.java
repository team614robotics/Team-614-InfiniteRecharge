package frc.robot.commands.intake;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.OI;
import frc.robot.Robot;
import frc.robot.RobotMap;
import edu.wpi.first.wpilibj.GenericHID.Hand;

/**
 *
 */
public class RunOuttake extends Command {
	public RunOuttake() {
		requires(Robot.m_intake);
	}

	// Called just before this Command runs the first time
	protected void initialize() {
		// Robot.arm.sparkMaxB.setInverted(true);
		Robot.m_intake.intakeMotor.set(0);
	}

	// Called repeatedly when this Command is scheduled to run
	protected void execute() {
	//  SmartDashboard.putNumber("Left Trigger",OI.operatorController.getTriggerAxis(Hand.kLeft));
	//  SmartDashboard.putNumber("Right Trigger",OI.operatorController.getTriggerAxis(Hand.kRight));
     //Robot.m_intake.intakeMotor.set(-1.0 * SmartDashboard.getNumber("Intake Speed", 0));
     Robot.m_intake.intakeMotor.set(-.5);
	}

	// Make this return true when this Command no longer needs to run execute()
	protected boolean isFinished() {
		return false;
	}

	// Called once after isFinished returns true
	protected void end() {
		Robot.m_intake.intakeMotor.set(0);
	}

	// Called when another command which requires one or more of the same
	// subsystems is scheduled to run
	protected void interrupted() {
		Robot.m_intake.intakeMotor.set(0);
	}
}