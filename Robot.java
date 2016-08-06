
package org.usfirst.frc.team2643.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.RobotDrive.MotorType;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

import org.usfirst.frc.team2643.robot.commands.ExampleCommand;
import org.usfirst.frc.team2643.robot.subsystems.ExampleSubsystem;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot 
{

	public static final ExampleSubsystem exampleSubsystem = new ExampleSubsystem();
	public static OI oi;
	
	public static Talon rFrontMotor = new Talon(9);
	public static Talon lFrontMotor = new Talon(7);
	public static Talon rBackMotor = new Talon(8);
	public static Talon lBackMotor = new Talon(6);
	
	public static Joystick stick = new Joystick(0);
	
	public static NetworkTable table = NetworkTable.getTable("GRIP");

	public static double distance;
	
	public static int state = 0;
	public static int width = 0;
	public static int waitTime = 0;
	public static int turnTime = 0;
	
	public static RobotDrive drive = new RobotDrive(lBackMotor, lFrontMotor, rBackMotor, rFrontMotor);
	
	public static double[] widthX = table.getNumberArray("myContoursReport/width", new double[0]);
	
	boolean isInverted = true;
	
	Timer timer = new Timer();
	
    Command autonomousCommand;
    SendableChooser chooser;

    public void robotInit() 
    {
		oi = new OI();
        chooser = new SendableChooser();
        chooser.addDefault("Default Auto", new ExampleCommand());
        SmartDashboard.putData("Auto mode", chooser);
        
        drive.setInvertedMotor(MotorType.kFrontLeft, isInverted);
        drive.setInvertedMotor(MotorType.kFrontRight, isInverted);
        drive.setInvertedMotor(MotorType.kRearLeft, isInverted);
        drive.setInvertedMotor(MotorType.kRearLeft, isInverted);
    }

    public void disabledInit()
    {

    }
	
	public void disabledPeriodic() 
	{
		Scheduler.getInstance().run();
	}

    public void autonomousInit() 
    {
        autonomousCommand = (Command) chooser.getSelected();

        if (autonomousCommand != null) autonomousCommand.start();
    }

    public void autonomousPeriodic()
    {
        Scheduler.getInstance().run();
    }

    public void teleopInit() 
    {

        if (autonomousCommand != null) autonomousCommand.cancel();
    }

    public void teleopPeriodic() 
    {
    	Scheduler.getInstance().run();
    	
    	distanceOfObject();
    	
    	if(stick.getRawButton(1))
    	{
    		autoMove();
    	}
    	else if(stick.getRawButton(2))
    	{
    		drive.arcadeDrive(stick);
    	}
    }

    private double distanceOfObject()
    {
    	//double[] widthX = table.getNumberArray("myContoursReport/width", new double[0]);
    	double widthOfTargetIRL = 0.0;
    	int resolution = 640;
    	double FOV = 0.0;
    	
    	for(int x = 0; x < widthX.length; x++)
    	{
    		if(widthX[x] > widthX[width])
    		{
    			width = x;
    		}
    	}
    	
    	distance = widthOfTargetIRL * resolution / ( (2 * widthX[width]) * Math.tan(FOV / 2) );
    	System.out.println("Distance: " + distance);
    	return distance;
    }
    
    private void autoMove()
    {    	
    	switch(state)
    	{
    		case 0:
    			drive.drive(0.6, 0.0);
    			
    			if(distance <= 10.0) //also needs to find if it is to the right
    			{
					timer.start();
					
    				if(widthX[width] > 360 && timer.get() >= waitTime)
    				{ 
    					drive.drive(0.0, 0.0);
    					timer.stop();
    					timer.reset();
    					state = 1;
    				}
    				else if(widthX[width] <= 360 && timer.get() >- waitTime)
    				{
    					drive.drive(0.0, 0.0);
    					timer.stop();
    					timer.reset();
    					state = 2;
    				}
    			}
    		break;
    		
    		case 1:
    			timer.start();
    			drive.drive(0.0, 0.5);
    			
    			if(timer.get() >= turnTime)
    			{
    				drive.drive(0.0, 0.0);
    				timer.stop();
    				timer.reset();
    				state = 0;
    			}
    		break;
    		
    		case 2:
    			timer.start();
    			drive.drive(0.0, -0.5);
    			
    			if(timer.get() >= turnTime)
    			{
    				drive.drive(0.0, 0.0);
    				timer.stop();
    				timer.reset();
    				state = 0;
    			}
    			
    	}
    }
    
    public void testPeriodic()
    {
        LiveWindow.run();
    }
}
