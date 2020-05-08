 package siver.experiments;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import repast.simphony.engine.environment.RunEnvironment;
 import repast.simphony.engine.schedule.ISchedule;
 import repast.simphony.engine.schedule.ScheduleParameters;
 import repast.simphony.parameter.Parameters;
 import repast.simphony.random.RandomHelper;
 import repast.simphony.ui.RSApplication;
 import siver.context.SiverContextCreator;
 import siver.cox.brains.BasicBrain;
 import siver.cox.brains.CoxBrain;
 import siver.ui.UserPanel;
 
 public class InprogressExperiment extends ExperimentalDatum {
 	private static final double TICK_TIMEOUT = 4*60*60; // a 4 hour session is experiment maximum
 	
 	private static InprogressExperiment instance;
 	private Integer experiment_id, schedule_id;
 	private int experiment_run_id;
 	private int crash_count;
 	private ArrayList<BoatRecord> inprogress_records;
 	private int random_seed;
 	private Class<? extends CoxBrain> brain_type;
 	
 	public static void start() {
 		if(instance() == null) {
 			instance = new InprogressExperiment();
 		} else {
 			throw new RuntimeException("Trying to start new Inprogress Experiment while one is already in progress.");
 		}
 		
 		initializeDBConnection();
 		BoatRecord.initializeDBConnection();
 		try {
 			instance().findRandomSeedScheduleIdAndBrainType();
 			instance().create_experiment_run();
 			instance().scheduleLaunches();
 			if(instance().isAutomated()) {
 				//set up a scheduled method to run when after a certain number of ticks has passed so experiments don't take too long
 				ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
 				ScheduleParameters simTimeoutParams = ScheduleParameters.createOneTime(TICK_TIMEOUT);
 				schedule.schedule(simTimeoutParams, instance(), "stopSim");
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}  
 		
 	}
 	
 	public static void end() {
 		//flush any remaining boat records to the database
 		try {
 			for(BoatRecord br : instance().inprogress_records) {
 				br.flush();
 			}
 			instance().flush();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			//make sure all DB connections are closed and set the instance to null
 			instance = null;
 			closeDBConnection();
 			BoatRecord.closeDBConnection();
 		}
 	}
 	
 	public static InprogressExperiment instance() {
 		return instance;
 	}
 	
 	private void create_experiment_run() throws SQLException {
 		PreparedStatement insertExperimentRun = null;
 		String sql = "INSERT INTO experiment_runs(experiment_id, random_seed, crash_count, brain_type)"
                 + "VALUES(?, ?, ?, ?)";
         insertExperimentRun = conn.prepareStatement(sql);
 		if(isAutomated()) {
 			insertExperimentRun.setInt(1, experiment_id);
 		} else {
 			insertExperimentRun.setNull(1, java.sql.Types.INTEGER);
 		}
 	    insertExperimentRun.setInt(2, random_seed);
 	    insertExperimentRun.setInt(3, 0);
	    if(brain_type != null) {
	    	insertExperimentRun.setString(4, brain_type.getSimpleName());
	    } else {
	    	insertExperimentRun.setNull(4, java.sql.Types.VARCHAR);
	    }
 	    insertExperimentRun.executeUpdate();
 	    ResultSet keys = insertExperimentRun.getGeneratedKeys();
 	    keys.first();
 	    this.experiment_run_id = keys.getInt(1);
 	    
 	    insertExperimentRun.close();
 	}
 	
 	private InprogressExperiment() {
 		Parameters params = RunEnvironment.getInstance().getParameters(); 
 		this.experiment_id = (Integer)params.getValue("ExperimentId");
 		if(this.experiment_id <= 0) this.experiment_id = null;
 		crash_count = 0;
 		this.inprogress_records = new ArrayList<BoatRecord>();
 	}
 	
 	private void findRandomSeedScheduleIdAndBrainType() {
 		if(isAutomated()) {
 			PreparedStatement getRandomSeed = null;
 			String sql = "SELECT random_seed, schedule_id, brain_type FROM experiments WHERE ID = ?";
 			try {
 				getRandomSeed = conn.prepareStatement(sql);
 				getRandomSeed.setInt(1, experiment_id);
 				ResultSet rseed = getRandomSeed.executeQuery();
 				rseed.first();
 				random_seed = rseed.getInt(1);
 				schedule_id = rseed.getInt(2);
 				String coxBrainClassName =  rseed.getString(3);
 				getRandomSeed.close();
 				
 				if(!coxBrainClassName.startsWith("siver.")) {
 					coxBrainClassName = BasicBrain.class.getPackage().getName()+"."+coxBrainClassName;
 				}
 				brain_type = (Class<? extends CoxBrain>) Class.forName(coxBrainClassName);
 				RandomHelper.setSeed(random_seed);
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			random_seed = RandomHelper.getSeed();
 			schedule_id = null;
 		}
 	}
 	
 	public static void addBoatRecord(BoatRecord br) {
 		if(instance()!= null) {
 			instance().inprogress_records.add(br);
 		}
 	}
 	
 	public static void removeBoatRecord(BoatRecord br) {
 		if(instance()!= null) {
 			instance().inprogress_records.remove(br);
 		}
 	}
 	
 	public static void incrementCrashCount() {
 		if(instance() != null) {
 			instance().crash_count++;
 		}
 	}
 	
 	private boolean isAutomated() {
 		return experiment_id != null;
 	}
 	
 	public int experiment_run_id() {
 		return this.experiment_run_id;
 	}
 	
 	private void flush() throws SQLException {
 		PreparedStatement finishExperimentRun = null;
 		String sql = "UPDATE experiment_runs " +
 				     "SET flushed = ?, crash_count = ?, tick_count = ? " + 
 					 "WHERE id = ?";
 		finishExperimentRun = conn.prepareStatement(sql);
 		if(isAutomated()) {
 			finishExperimentRun.setInt(1, experiment_id);
 		} else {
 			finishExperimentRun.setNull(1, java.sql.Types.INTEGER);
 		}
 		finishExperimentRun.setBoolean(1, true);
 		finishExperimentRun.setInt(2, crash_count);
 		finishExperimentRun.setInt(3, SiverContextCreator.getTickCount());
 		finishExperimentRun.setInt(4, experiment_run_id);
 		finishExperimentRun.executeUpdate();
 	    finishExperimentRun.close();
                 
 	}
 	
 	private void scheduleLaunches() {
 		//remove any old user control panel that may be there to prevent manual launches mid automated experiment run
 		if(RSApplication.getRSApplicationInstance() != null) {
 			//make sure we're in a mode where there is an RS Application instance (so not running in batch mode)
 			RSApplication.getRSApplicationInstance().removeCustomUserPanel();
 		}
 		if(isAutomated()) {
 			PreparedStatement stmt = null;
 			String sql = "SELECT launch_tick, desired_gear, speed_multiplier, distance_to_cover, id " +
 						"FROM scheduled_launches WHERE schedule_id = ?";
 			try {
 				stmt = conn.prepareStatement(sql);
 				stmt.setInt(1, schedule_id);
 				ResultSet launchData = stmt.executeQuery();
 				ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
 				while(launchData.next()) {
 					int launch_tick = launchData.getInt(1);
 					int desired_gear = launchData.getInt(2);
 					double speed_multiplier = launchData.getDouble(3);
 					double distance_to_cover = launchData.getDouble(4);
 					int schedule_launch_id = launchData.getInt(5);
 					
 					ScheduleParameters params = ScheduleParameters.createOneTime(launch_tick);
 					schedule.schedule(params, SiverContextCreator.getBoatHouse(), "launch", 
 							schedule_launch_id,desired_gear, speed_multiplier, distance_to_cover, brain_type);
 				}
 				stmt.close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		} else {
 			//if the experiment run does not have an automated schedule
 			//then add a control panel so boat's may be launched manually
 			RSApplication.getRSApplicationInstance().addCustomUserPanel(new UserPanel()); //and add a new one
 		}
 	}
 	
 	public void stopSim() {
 		SiverContextCreator.stopSim();
 	}
 }
