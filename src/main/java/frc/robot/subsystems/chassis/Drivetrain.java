package frc.robot.subsystems.chassis;

import com.revrobotics.CANPIDController;
import com.revrobotics.CANSparkMax;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.commands.chassis.OutputCalculator;
import frc.robot.OI;
import frc.robot.RobotMap;
import frc.lib.pathfinder.pathCreator.PathGenerator;
import frc.lib.pathfinder.pathCreator.SmoothPosition;
import frc.lib.pathfinder.kinematics.*;
import frc.lib.pathfinder.kinematics.RobotTracing;
import java.util.ArrayList;
import frc.lib.util.AngleMath;
import frc.robot.Robot;
import com.revrobotics.CANEncoder;
import edu.wpi.first.wpilibj.XboxController;
import com.revrobotics.ControlType;
import edu.wpi.first.wpilibj.GenericHID.Hand;

public class Drivetrain extends Subsystem {

    public CANSparkMax leftMotorA, leftMotorB, rightMotorA, rightMotorB;
    public static DifferentialDrive drivetrain;

    public OutputCalculator outputCalculator;
    public static double P, I, D, V;
    public static RobotTracing robotPath;
    public ArrayList<Double> velocity, leftDistance, rightDistance;

    public CANPIDController pidControllerLeft, pidControllerrRight;
    public CANEncoder encoderLeft, encoderRight;

    static final double turnTolerance = 0.1f;

    public Drivetrain() {
        leftMotorA = new CANSparkMax(RobotMap.leftMotorAPort, RobotMap.brushless);
        leftMotorB = new CANSparkMax(RobotMap.leftMotorBPort, RobotMap.brushless);
        rightMotorA = new CANSparkMax(RobotMap.rightMotorAPort, RobotMap.brushless);
        rightMotorB = new CANSparkMax(RobotMap.rightMotorBPort, RobotMap.brushless);

        drivetrain = new DifferentialDrive(leftMotorA, rightMotorA);
        
        leftMotorA.setInverted(true);
        rightMotorA.setInverted(true);
        leftMotorB.follow(leftMotorA);
        rightMotorB.follow(rightMotorA);

        leftMotorA.getPIDController().setP(RobotMap.chassisVelocityPValue);
        leftMotorA.getPIDController().setI(RobotMap.chassisVelocityIValue);
        leftMotorA.getPIDController().setD(RobotMap.chassisVelocityDValue);
        leftMotorA.getPIDController().setIZone(RobotMap.chassisVelocityIZValue);
        leftMotorA.getPIDController().setFF(RobotMap.chassisVelocityFFValue);
        leftMotorA.getPIDController().setOutputRange(RobotMap.chassisVelocityMinOutput, RobotMap.chassisVelocityMaxOutput);
        
        rightMotorA.getPIDController().setP(RobotMap.chassisVelocityPValue);
        rightMotorA.getPIDController().setI(RobotMap.chassisVelocityIValue);
        rightMotorA.getPIDController().setD(RobotMap.chassisVelocityDValue);
        rightMotorA.getPIDController().setIZone(RobotMap.chassisVelocityIZValue);
        rightMotorA.getPIDController().setFF(RobotMap.chassisVelocityFFValue);
        rightMotorA.getPIDController().setOutputRange(RobotMap.chassisVelocityMinOutput, RobotMap.chassisVelocityMaxOutput);

        outputCalculator = new OutputCalculator(RobotMap.pValue, RobotMap.dValue, RobotMap.vValue,
                 RobotMap.wheelDiameter, RobotMap.ticksInARevolution);
        PathGenerator.createDataSet();
        SmoothPosition.smoothPath(PathGenerator.finalPoints, SmoothPosition.dataWeightA, SmoothPosition.smoothWeightB,
                SmoothPosition.tolerance);
        KinematicsCalculator.calculuateCurvature();
        KinematicsCalculator.calculateVelocities();

        KinematicsCalculator.rateLimiter();
        SmoothVelocity.smoothVelocity(KinematicsCalculator.velocity, SmoothVelocity.dataWeightA,
                SmoothVelocity.smoothWeightB, SmoothVelocity.tolerance);
        velocity = new ArrayList(SmoothVelocity.smoothVelocity(KinematicsCalculator.velocity,
                SmoothVelocity.dataWeightA, SmoothVelocity.smoothWeightB, SmoothVelocity.tolerance));

        robotPath = new RobotTracing(SmoothPosition.newPathPoints, 2);
        robotPath.leftRight(SmoothPosition.newPathPoints, 2);

        KinematicsCalculator.calculateLeftDistance(robotPath.leftPath);
        KinematicsCalculator.calculateRightDistance(robotPath.rightPath);
        leftDistance = new ArrayList(KinematicsCalculator.leftDistance);
        rightDistance = new ArrayList(KinematicsCalculator.rightDistance);
        KinematicsCalculator.calculateLeftVelocities(robotPath.leftPath);
        KinematicsCalculator.calculateRightVelocities(robotPath.rightPath);
        SmartDashboard.putNumber("Drivetrain: Heading ", leftDistance.size());

        SmoothVelocity.smoothLeftVelocity(KinematicsCalculator.leftVelocity, SmoothVelocity.dataWeightA,
                SmoothVelocity.smoothWeightB, SmoothVelocity.tolerance);
        SmoothVelocity.smoothRightVelocity(KinematicsCalculator.rightVelocity, SmoothVelocity.dataWeightA,
                SmoothVelocity.smoothWeightB, SmoothVelocity.tolerance);
    }

    public void initDefaultCommand() {
        // setDefaultCommand(new ArcadeDrive());
    }

    public void resetPath() {
        PathGenerator.newPoints.clear();
        PathGenerator.newVectors.clear();
        PathGenerator.finalPoints.clear();
        PathGenerator.newNumOPoints.clear();
        SmoothPosition.newPathPoints.clear();
        SmoothPosition.pathPoints.clear();
        KinematicsCalculator.curvature.clear();
        KinematicsCalculator.distance.clear();
        KinematicsCalculator.leftDistance.clear();
        KinematicsCalculator.leftVelocity.clear();
        KinematicsCalculator.outputs.clear();
        KinematicsCalculator.rightDistance.clear();
        KinematicsCalculator.rightVelocity.clear();
        KinematicsCalculator.velocity.clear();
        SmoothVelocity.leftVelocities.clear();
        SmoothVelocity.rightVelocities.clear();
        TimeStepCalculator.timeOutlined.clear();
        velocity.clear();
        leftDistance.clear();
        rightDistance.clear();
    }

    public void addPoint(double xValue, double yValue) {
        PathGenerator.addPoint(xValue, yValue);
    }

    public void generatePath() {
        outputCalculator = new OutputCalculator(RobotMap.pValue, RobotMap.dValue, RobotMap.vValue,
                RobotMap.wheelDiameter, RobotMap.ticksInARevolution);

        PathGenerator.createDataSet();
        SmoothPosition.smoothPath(PathGenerator.finalPoints, SmoothPosition.dataWeightA, SmoothPosition.smoothWeightB,
                SmoothPosition.tolerance);
        KinematicsCalculator.calculuateCurvature();
        KinematicsCalculator.calculateVelocities();

        KinematicsCalculator.rateLimiter();
        SmoothVelocity.smoothVelocity(KinematicsCalculator.velocity, SmoothVelocity.dataWeightA,
                SmoothVelocity.smoothWeightB, SmoothVelocity.tolerance);
        velocity = new ArrayList(SmoothVelocity.smoothVelocity(KinematicsCalculator.velocity,
                SmoothVelocity.dataWeightA, SmoothVelocity.smoothWeightB, SmoothVelocity.tolerance));
        robotPath = new RobotTracing(SmoothPosition.newPathPoints, 2);
        robotPath.leftRight(SmoothPosition.newPathPoints, 2);

        KinematicsCalculator.calculateLeftDistance(robotPath.leftPath);
        KinematicsCalculator.calculateRightDistance(robotPath.rightPath);
        leftDistance = new ArrayList(KinematicsCalculator.leftDistance);
        rightDistance = new ArrayList(KinematicsCalculator.rightDistance);
        KinematicsCalculator.calculateLeftVelocities(robotPath.leftPath);
        KinematicsCalculator.calculateRightVelocities(robotPath.rightPath);

        SmoothVelocity.smoothLeftVelocity(KinematicsCalculator.leftVelocity, SmoothVelocity.dataWeightA,
                SmoothVelocity.smoothWeightB, SmoothVelocity.tolerance);
        SmoothVelocity.smoothRightVelocity(KinematicsCalculator.rightVelocity, SmoothVelocity.dataWeightA,
                SmoothVelocity.smoothWeightB, SmoothVelocity.tolerance);
    }

    public void arcadeDrive(double speed, double rotateValue) {
        drivetrain.arcadeDrive(speed, rotateValue);
    }

    public void curvatureDrive(double speed, double rotateValue) {
        drivetrain.curvatureDrive(speed, rotateValue, OI.driverController.getAButton());
    }

    public double distanceInFeet(double encoderValue) {
        return encoderValue * (((RobotMap.wheelDiameter / 12) * Math.PI) / RobotMap.ticksInARevolution);
    }

    public void velocityBasedDrive(XboxController m_driverController) {
        if ((m_driverController.getY(Hand.kLeft) > 0.05)
                && (m_driverController.getX(Hand.kRight) > 0.05 || m_driverController.getX(Hand.kRight) < -0.05)) {
            double setPointLeft = Math.sqrt(Math.abs(m_driverController.getY(Hand.kLeft)))
                    * m_driverController.getY(Hand.kLeft) * 4000
                    + Math.sqrt(Math.abs(m_driverController.getX(Hand.kRight))) * m_driverController.getX(Hand.kRight)
                            * 3800;
            double setPointRight = Math.sqrt(Math.abs(m_driverController.getY(Hand.kLeft)))
                    * m_driverController.getY(Hand.kLeft) * 4000
                    - Math.sqrt(Math.abs(m_driverController.getX(Hand.kRight))) * m_driverController.getX(Hand.kRight)
                            * 3800;
            // double setPointLeft = m_driverController.getY(Hand.kLeft)*650;
            // double setPointRight = m_driverController.getY(Hand.kLeft)*650;
            leftMotorA.getPIDController().setReference(setPointLeft, ControlType.kVelocity);
            rightMotorA.getPIDController().setReference(setPointRight, ControlType.kVelocity);

        } else if ((m_driverController.getY(Hand.kLeft) < -0.05)
                && (m_driverController.getX(Hand.kRight) > 0.05 || m_driverController.getX(Hand.kRight) < -0.05)) {
            double setPointLeft = Math.sqrt(Math.abs(m_driverController.getY(Hand.kLeft)))
                    * m_driverController.getY(Hand.kLeft) * 4000
                    - Math.sqrt(Math.abs(m_driverController.getX(Hand.kRight))) * m_driverController.getX(Hand.kRight)
                            * 3800;
            double setPointRight = Math.sqrt(Math.abs(m_driverController.getY(Hand.kLeft)))
                    * m_driverController.getY(Hand.kLeft) * 4000
                    + Math.sqrt(Math.abs(m_driverController.getX(Hand.kRight))) * m_driverController.getX(Hand.kRight)
                            * 3800;
            // double setPointLeft = m_driverController.getY(Hand.kLeft)*650;
            // double setPointRight = m_driverController.getY(Hand.kLeft)*650;
            leftMotorA.getPIDController().setReference(setPointLeft, ControlType.kVelocity);
            rightMotorA.getPIDController().setReference(setPointRight, ControlType.kVelocity);

        } else if ((m_driverController.getY(Hand.kLeft) > 0.05 || m_driverController.getY(Hand.kLeft) < -0.05)) {
            double setPointLeft = Math.sqrt(Math.abs(m_driverController.getY(Hand.kLeft)))
                    * m_driverController.getY(Hand.kLeft) * 4000;
            double setPointRight = Math.sqrt(Math.abs(m_driverController.getY(Hand.kLeft)))
                    * m_driverController.getY(Hand.kLeft) * 4000;
            // double setPointLeft = m_driverController.getY(Hand.kLeft)*650;
            // double setPointRight = m_driverController.getY(Hand.kLeft)*650;
            leftMotorA.getPIDController().setReference(setPointLeft, ControlType.kVelocity);
            rightMotorA.getPIDController().setReference(setPointRight, ControlType.kVelocity);

        } else if ((m_driverController.getX(Hand.kRight) > 0.05 || m_driverController.getX(Hand.kRight) < -0.05)) {
            double setPointLeft = -Math.sqrt(Math.abs(m_driverController.getX(Hand.kRight)))
                    * m_driverController.getX(Hand.kRight) * 3800;
            double setPointRight = Math.sqrt(Math.abs(m_driverController.getX(Hand.kRight)))
                    * m_driverController.getX(Hand.kRight) * 3800;
            // double setPointLeft = m_driverController.getY(Hand.kLeft)*650;
            // double setPointRight = m_driverController.getY(Hand.kLeft)*650;
            leftMotorA.getPIDController().setReference(setPointLeft, ControlType.kVelocity);
            rightMotorA.getPIDController().setReference(setPointRight, ControlType.kVelocity);
        } else {
            leftMotorA.set(0);
            rightMotorA.set(0);
        }
    }

    public void resetDrivetrain() {
        Robot.m_navX.reset();
        leftMotorA.set(0);
        leftMotorB.set(0);
        rightMotorA.set(0);
        rightMotorB.set(0);
    }

    public double getAngle() {
        return AngleMath.boundDegrees(Robot.m_navX.getAngle());
    }
}