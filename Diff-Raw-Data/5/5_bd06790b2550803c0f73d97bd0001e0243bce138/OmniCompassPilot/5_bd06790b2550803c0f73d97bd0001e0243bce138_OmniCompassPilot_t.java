 package lejos.robotics.navigation;
 
 
 import lejos.robotics.DirectionFinder;
 import lejos.util.Delay;
import lejos.util.SimplePID;
 
 public class OmniCompassPilot extends SimpleOmniPilot
 {
 	protected DirectionFinder    _compass;
 
 	protected float              _targetFacing    = 0;
 	protected float              _targetDirection = 0;
 	protected boolean            _travel          = false;
 	protected boolean            _regulate        = true;
 	protected DirectionRegulator _regulator       = null;
 
 	public OmniCompassPilot(DirectionFinder compass, OmniMotor... motors)
 	{
 		super(motors);
 		_compass = compass;
 		_compass.resetCartesianZero();
 		
 		_regulator = new DirectionRegulator();
 		_regulator.start();
 	}
 
 	public void rotateTo(float heading)
 	{
 		rotateTo(heading, false);
 	}
 
 	public void rotateTo(float heading, boolean immediateReturn)
 	{
 		if(Float.isNaN(heading))
 			stop();
 		else if(_targetFacing != heading || _travel != false)
 		{
 			//setRegulation(true);
 			synchronized(_regulator)
 			{
 				_travel = false;
 				_targetFacing = heading;
 				_regulator.reset();
 			}
 		}
 			
 		while(!(immediateReturn || Math.abs(_regulator.getError()) < 3))
 			Thread.yield();
 	}
 
 	@Override
 	public void rotate(float angle, boolean immediateReturn)
 	{
 		rotateTo(_compass.getDegreesCartesian() + angle, immediateReturn);
 	}
 
 	@Override
 	public void stop()
 	{
 		//setRegulation(true);
 		synchronized(_regulator)
 		{
 			_travel = false;
 			_regulator.reset();
 		}
 		super.stop();
 	}
 
 	@Override
 	public void travel(float heading)
 	{
 		if(Float.isNaN(heading))
 			stop();
 		else if(_targetDirection != heading || _travel != true)
 		{
 			//setRegulation(true);
 			synchronized(_regulator)
 			{
 				_travel = true;
 				_targetDirection = heading;
 				_regulator.reset();
 			}
 		}
 
 	}
 
 	public void setDirectionFinder(DirectionFinder df)
 	{
 		float oldHeading = _compass.getDegreesCartesian();
 		float newHeading = df.getDegreesCartesian();
 		if(!Float.isNaN(oldHeading) && !Float.isNaN(newHeading))
 		{
 			_targetFacing = newHeading - oldHeading + _targetFacing;
 		}
 		_compass = df;
 		if(_regulator != null)
 			_regulator.reset();
 	}
 /*
 	public void setRegulation(boolean on)
 	{
 		if(on == (_regulator != null))
 			return;
 
 		if(_regulator == null)
 		{
 			_regulator = new DirectionRegulator();
 			_regulator.setDaemon(true);
 			_regulator.start();
 		}
 
 		else
 			synchronized(_regulator)
 			{
 				_regulator = null;
 			}
 	}
 */
 	private class DirectionRegulator extends Thread
 	{
 		public DirectionRegulator()
         {
         	super();
         	setDaemon(true);
         }
 		private float[] getTravelSpeedsWithBias(float rotationBias, float error)
 		{
 			float[] travelSpeeds = getMotorTravelVelocities(_targetDirection + error);
 			travelSpeeds = limitToMaxSpeeds(travelSpeeds);
 			
 			float[] rotationSpeeds = getMotorRotationVelocities(rotationBias);
 			float[] speeds = new float[_motors.length];
 			for(int i = 0; i < _motors.length; i++)
 			{
 				speeds[i] = travelSpeeds[i] + rotationSpeeds[i];
 			}
 			return speeds;
 		}
 
 		private SimplePID pid = new SimplePID(0.05, 0.00001, 0);
 
 		private float getError()
 		{
 			float err = _compass.getDegreesCartesian() - _targetFacing;
 			
 			// Handles the wrap-around problem:
 			while(err <= -180)
 				err += 360;
 			while(err > 180)
 				err -= 360;
 			return err;
 		}
 
 		public synchronized void reset()
 		{
 			//pid.reset();
 		}
 
 		@Override
 		public void run()
 		{
 			pid.start();
 			reset();
 			while(true)
 			{
 				Delay.msDelay(10);
 				synchronized(_regulator)
 				{
 					float error = getError();
 
 					if(Float.isNaN(error))
 						continue;
 
 					float rotationBias = (float) pid.getOutput(error);
 					
 					float[] speeds;
 
 					if(_travel)
 						speeds = getTravelSpeedsWithBias(rotationBias*_turnSpeed, error);
 					else
 						speeds = getMotorRotationVelocities(rotationBias*_turnSpeed);
 					
 					setMotorSpeeds(speeds);
 				}
 			}
 			//OmniCompassPilot.super.stop();
 		}
 	}
 }
