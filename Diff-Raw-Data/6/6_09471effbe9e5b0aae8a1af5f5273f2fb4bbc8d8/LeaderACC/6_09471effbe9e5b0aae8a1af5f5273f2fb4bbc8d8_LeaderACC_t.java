 package ACC_NEW;
 
 import cz.cuni.mff.d3s.deeco.annotations.In;
 import cz.cuni.mff.d3s.deeco.annotations.Out;
 import cz.cuni.mff.d3s.deeco.annotations.InOut;
 import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
 import cz.cuni.mff.d3s.deeco.annotations.Process;
 import cz.cuni.mff.d3s.deeco.knowledge.Component;
 import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;
 
 public class LeaderACC extends Component {
 
 	public String name;
 	
 	public Double lPos = 0.0;          
 	public Double lSpeed = 0.0;        
 	public Double creationTime = 0.0;  
 	
 	public Double leaderGas = 0.0;
 	public Double leaderBrake = 0.0;
 	
 	 public Double lastSpeedError = 0.0;
 	public Double integratorSpeedError = 0.0;
 	public Double es = 0.0;
 	
 	protected static final double kp = 0.05;
 	protected static final double ki = 0.000228325;
 	protected static final double kt = 0.01;
 	protected static final double secNanoSecFactor = 1000000000;
 	
 	
 	public LeaderACC() {
 		name = "L";
 		ACCDatabase.driverBehaviour();
 	}
 	
 	
 	@Process
 	@PeriodicScheduling(100)
 	public static void speedControl(
 		@In("lPos") Double lPos,
 		@In("lSpeed") Double lSpeed,
 		@In("creationTime") Double creationTime,
 		
 		@Out("leaderGas") OutWrapper<Double> lGas,
 		@Out("leaderBrake") OutWrapper<Double> lBrake,
 	
 		@InOut("integratorSpeedError") OutWrapper<Double> integratorSpeedError,
 		@InOut("es") OutWrapper<Double> es	
 	) {
 	
 		double currentTime = System.nanoTime()/secNanoSecFactor;
 		double timePeriod = creationTime > 0.0 ? currentTime - creationTime : 0.0; // this just to not divide by zero in the PID formula
 
 		double speedError = ACCDatabase.driverSpeed.get(lPos) - lSpeed;
 		integratorSpeedError.value += (ki * speedError + kt * es.value) * timePeriod;
		double pid = timePeriod == 0.0 ? 0.0 : kp * speedError + integratorSpeedError.value;
 		es.value = saturate(pid) - pid;
 		pid = saturate(pid);
 		
 		if(pid >= 0){
 			lGas.value = pid;
 			lBrake.value = 0.0;
 		}else{
 			lGas.value = 0.0;
 			lBrake.value = -pid;
 		}
 		lastSpeedError.value = speedError;
 	}
 		
 		
 	private static double saturate(double pid) {
 		if(pid > 1) pid = 1;
 		else if(pid < -1) pid = -1;
 		return pid;
 	}
 
 }
