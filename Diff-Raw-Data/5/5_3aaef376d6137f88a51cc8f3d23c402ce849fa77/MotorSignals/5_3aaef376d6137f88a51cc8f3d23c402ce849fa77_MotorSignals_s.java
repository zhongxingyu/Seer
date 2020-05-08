 package remote.control.motorsignals;
 
 public class MotorSignals implements MotorSignalsInterface
 {
 	private AbstractSignalAlgorithm	pitchAlgorithm;
 	private AbstractSignalAlgorithm	rollAlgorithm;
 
 	@Override
 	public void setPitchAlgorithm(AbstractSignalAlgorithm si)
 	{
 		pitchAlgorithm = si;
 	}
 
 	public AbstractSignalAlgorithm getPitchAlgorithm()
 	{
 		return this.pitchAlgorithm;
 	}
 
 	public AbstractSignalAlgorithm getRollAlgorithm()
 	{
 		return this.rollAlgorithm;
 	}
 
 	@Override
 	public void setRollAlgorithm(AbstractSignalAlgorithm si)
 	{
 		rollAlgorithm = si;
 	}
 
 	@Override
 	public int[] convert(float pitch, float roll, int exp)
 	{
 		int max = (int) Math.pow(2, exp);
 		float[] cPitch = pitchAlgorithm.convert(pitch);
 		float[] cRoll = rollAlgorithm.convert(roll);
 		int lMDirection = 1;
 		int rMDirection = 1;
 		float lmRaw = cPitch[0] * (1 - cRoll[0]);
 		float rmRaw = cPitch[1] * (1 - cRoll[1]);
 		if (0f > lmRaw)
 		{
 			lMDirection = -1;
			lmRaw = -1;
 		}
 		if (0f > rmRaw)
 		{
 			rMDirection = -1;
			rmRaw = -1;
 		}
 		int lMotor = (int) Math
 			.floor(Math.scalb(lmRaw, exp));
 		int rMotor = (int) Math
 			.floor(Math.scalb(rmRaw, exp));
 		if (max == lMotor)
 		{
 			lMotor = lMotor - 1;
 		}
 		if (max == rMotor)
 		{
 			rMotor = rMotor - 1;
 		}
 		return new int[] { lMotor, lMDirection, rMotor, rMDirection };
 	}
 }
