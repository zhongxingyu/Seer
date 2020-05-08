 package com.vitaltech.bioink;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 
 import android.util.Log;
 
 public class DataProcess {
 	//List of current users
 	public ConcurrentHashMap<String,User> users;
 	public List<MergedUser> merged;
 	private Scene scene;
 	private Timer utimer = new Timer();
 	private CalculationTimer task;
 	private int uinterval = 0;
 
 	public final float MIN_HR = 0;
 	public final float MAX_HR = 250;
 	public final float MIN_RESP = 0;
 	public final float MAX_RESP = 70;
 	public final float MIN_HRV = 0;
 	public final float MAX_HRV = 200;	
 
 	//Distance that determines if two users are similar
 	public final float mdis = 0.08f;
	public final float udis = 0.06f;
 	
 	//Positioning range constants
 	private final float minPos = -1;
 	private final float maxPos = 1;
 	
 	//Heart rate range constant
 	private float minHR = MIN_HR;
 	private float maxHR = MAX_HR;
 	
 	//Respiration rate 
 	private float minResp = MIN_RESP;
 	private float maxResp = MAX_RESP;
 	
 	//Heart rate variability
 	private float minHRV = MIN_HRV;
 	private float maxHRV = MAX_HRV;
 	
 	//R to R interval
 	private float minRR = -1250;
 	private float maxRR = 1250;
 	
 	private BiometricType colorMapper = BiometricType.RESPIRATION;
 	private BiometricType energyMapper = BiometricType.HEARTRATE;
 	
 	private boolean DEBUG = MainActivity.DEBUG;
 	
 	//Data Processing constructor
 	public DataProcess(int updateInterval){
 		users = new ConcurrentHashMap<String,User>(23);
 		merged = new ArrayList<MergedUser>();
 		//Collections.synchronizedList(new ArrayList<MergedUser>());
 		uinterval = updateInterval;
 		resume();
 	}
 	
 	public void pause(){
 		task.cancel();
 	}
 	
 	public void resume(){
 		task = new CalculationTimer();
 		utimer.schedule(task, 0, uinterval);
 	}
 	
 	public void addScene(Scene ss){
 		this.scene = ss;
 	}
 	
 	public void setMinHR(float value){
 		if (value < MIN_HR)
 			minHR = MIN_HR;
 		else if (value > MAX_HR)
 			minHR = MAX_HR;
 		else
 			minHR = value;
 	}
 	
 	public void setMaxHR(float value){
 		if (value < minHR)
 			maxHR = MIN_HR;
 		else if (value > MAX_HR)
 			maxHR = MAX_HR;
 		else
 			maxHR = value;
 	}
 	
 	public void setMinResp(float value){
 		if (value < MIN_RESP)
 			minResp = MIN_RESP;
 		else if (value > MAX_RESP)
 			minResp = MAX_RESP;
 		else
 			minResp = value;
 	}
 	
 	public void setMaxResp(float value){
 		if (value < MIN_RESP)
 			maxResp = MIN_RESP;
 		else if (value > MAX_RESP)
 			maxResp = MAX_RESP;
 		else
 			maxResp = value;
 	}
 	
 	public void setColor(BiometricType t){
 		colorMapper = t;
 	}
 	
 	public void setEnergy(BiometricType t){
 		energyMapper = t;
 	}
 	
 	//Method allows bluetooth mod to push data into data Process mod
 	//User is specified by its id string
 	public void push(String uid, BiometricType dtype, float value){
 		synchronized(users){
 			if(!users.containsKey(uid)){
 				User tmp = new User();
 				tmp.id = uid;
 				users.put(uid,tmp); // insert into the dictionary if it does not exist
 			}
 		}
 		
 		if(value < getMin(dtype))
 			value = getMin(dtype);
 		if(value > getMax(dtype))
 			value = getMax(dtype);
 		
 		switch(dtype){
 		case HEARTRATE:
 			users.get(uid).heartrate = value;
 			break;
 		case RESPIRATION:
 			users.get(uid).respiration = value;
 			break;
 		case HRV:
 			users.get(uid).hrv = value;
 			users.get(uid).hrv_active = true;
 			break;
 		case RR:
 			users.get(uid).addRR(value);
 			break;
 		default:
 			break;
 		}
 		
 	}
 	
 	private float getMin(BiometricType t){
 		switch(t){
 			case HEARTRATE: return minHR;
 			case RESPIRATION: return minResp;
 			case HRV: return minHRV;
 			case RR: return minRR;
 			default: return 0;
 		}
 	}
 	
 	private float getMax(BiometricType t){
 		switch(t){
 			case HEARTRATE: return maxHR;
 			case RESPIRATION: return maxResp;
 			case HRV: return maxHRV;
 			case RR: return maxRR;
 			default: return 0;
 		}
 	}
 	
 	// returns the proportion from [0,1] where a given user's biometric type t
 	// falls within the minimum and maximum thresholds for that biometric type
 	private float getProportion(User u, BiometricType t){
 		float min = getMin(t);
 		float max = getMax(t);
 		float temp;
 		
 		if(min == max){
 			// do not divide by 0
 			temp = (u.get(t) - min)/(.0000001f);
 		}else{
 			temp = (u.get(t) - min)/(max-min);
 		}
 		
 		return Math.max(Math.min(temp, 1), 0);
 	}
 	
 	//map respiration rate from [minhr, maxhr] to [0, 1]
 	public float mapEnergy(String uid){
 		float temp = getProportion(users.get(uid),energyMapper);
 		scene.update(uid, DataType.ENERGY, temp);
 		return temp;
 	}
 	
 	//map respiration rate from [minresp, maxresp] to [0, 1]
 	public float mapColor(String uid){
 		float temp = getProportion(users.get(uid),colorMapper);
 		scene.update(uid, DataType.COLOR, temp);
 		return temp;
 	}
 	
 	//map position of given biometrics from their own intervals to [minpos, maxpos]
 	public void mapPosition(String uid){
 		float uhr = users.get(uid).heartrate;
 		float ure = users.get(uid).respiration;
 		float uhv = users.get(uid).hrv;
 		
 		if(DEBUG){
 			//Log.d("dp", "hrv: " + uhv);
 		}
 		
 		if(users.get(uid).hrv_active){
 			map3DPosition(uid, uhr, ure, uhv);
 		}else{
 			map2DPosition(uid, uhr, ure);
 		}
 	}
 	
 	public void mapMergedPosition(MergedUser mu){
 		float uhr = mu.heartrate;
 		float ure = mu.respiration;
 		float uhv = mu.hrv;
 		
 		for(String uu: mu.members){
 			
 			if(users.get(uu).hrv_active){
 				map3DPosition(uu, uhr, ure, uhv);
 			}else{
 				map2DPosition(uu, uhr, ure);
 			}
 			
 			if(mu.members.indexOf(uu) == 0){
 				//first element on the member list
 				scene.update(uu, DataType.VOLUME, 1.5f * mu.members.size());
 			}else{
 				scene.update(uu, DataType.VOLUME, 0.01f);
 			}
 		}
 	}
 	
 	public void map3DPosition(String uid, float uhr, float ure, float uhv){
 		float ratio = 1;
 		float x = 0;
 		float y = 0;
 		float z = 0;
 		//mapped coordinate values
 		float cx = 0;
 		float cy = 0;
 		float cz = 0;
 		//absolute values of calculated coordinates
 		float abx = 0;
 		float aby = 0;
 		float abz = 0;
 		float magnitude = 0;
 		
 		//map user heart rate to [-1,1]
 		x = uhr - ((maxHR + minHR) / 2);
 		x = x / ((maxHR - minHR) / 2);
 		
 		//map user respiration rate to [-1,1]
 		y = ure - ((maxResp + minResp) / 2);
 		y = y / ((maxResp - minResp) / 2);
 		
 		//map hrv to [-1,1]
 		z = uhv - ((maxHRV + minHRV) / 2);
 		z = z / ((maxHRV - minHRV) / 2);
 		
 		//map cube to r=1 sphere 
 		abx = Math.abs(x);
 		aby = Math.abs(y);
 		abz = Math.abs(z);
 		
 		//individual cases for the six faces of the cube
 		//case A: upper most
 		if(y > 0 && y > abx && y > abz){
 			cy = 1;
 			cx = abx / aby;
 			cz = abz / aby;
 		}
 		
 		//case B: lower most
 		if((-y > 0) && (-y > abx) && (-y > abz)){
 			cy = 1;
 			cx = abx / aby;
 			cz = abz / aby;
 		}
 		
 		//case C: right most
 		if(x > 0 && x > aby && x > abz){
 			cx = 1;
 			cy = aby / abx;
 			cz = abz / abx;
 		}
 		
 		//case D: left most
 		if((-x > 0) && (-x > aby) && (-x > abz)){
 			cx = 1;
 			cy = aby / abx;
 			cz = abz / abx;
 		}
 		
 		//case E: front
 		if(z > 0 && z > abx && z > aby){
 			cz = 1;
 			cx = abx / abz;
 			cy = aby / abz;
 		}
 		
 		//case F: back
 		if((-z > 0) && (-z > abx) && (-z > aby)){
 			cz = 1;
 			cx = abx / abz;
 			cy = aby / abz;
 		}
 		
 		//individual cases for pairs of coordinates being equal
 		if(abx == aby && abx != abz){
 			if(abx > abz){
 				cx = 1;
 				cy = 1;
 				cz = abz / abx;
 			}//else case already being man handled
 		}
 		
 		if(abx == abz && abx != aby){
 			if(abx > aby){
 				cx = 1;
 				cz = 1;
 				cy = aby / abx;
 			}
 		}
 		
 		if(aby == abz && aby != abx){
 			if(aby > abx){
 				cy = 1;
 				cz = 1;
 				cx = aby / abx;
 			}
 		}
 		
 		//maps square to sphere using ratio
 		//special case, all 3 coordinates are equal 
 		if(abx == aby && aby == abz){
 			magnitude = (float) Math.sqrt(3);
 		}else{
 			magnitude = cx * cx + cy * cy + cz * cz;
 			magnitude = (float) Math.sqrt(magnitude);
 		}
 		
 		if(magnitude == 0){
 			ratio = 1;
 		}else{
 			ratio = 1 / magnitude;
 		}
 		
 		x = x * ratio;
 		y = y * ratio;
 		z = z * ratio;
 		
 		//scale to display sphere
 		y = y * (maxPos - minPos) / 2;
 		y = y + ((maxPos + minPos) / 2);
 		x = x * (maxPos - minPos) / 2;
 		x = x + ((maxPos + minPos) / 2);
 		z = z * (maxPos - minPos) / 2;
 		z = z + ((maxPos + minPos) / 2);
 		
 		/*Log.d("dp", uid + " x: " + x);
 		Log.d("dp", uid + " y: " + y);
 		Log.d("dp", uid + " z: " + z);*/
 		
 		//validation
 		y = Math.max(Math.min(y, maxPos), minPos);
 		x = Math.max(Math.min(x, maxPos), minPos);
 		z = Math.max(Math.min(z, maxPos), minPos);
 		
 		//update x and y values
 		scene.update(uid, DataType.X, x);
 		scene.update(uid, DataType.Y, y);
 		scene.update(uid, DataType.Z, z);
 	}
 	
 	public void map2DPosition(String uid, float uhr, float ure){
 		float ratio = 1;
 		float x = 0;
 		float y = 0;
 		//mapped coordinate values
 		float cx = 0;
 		float cy = 0;
 		//absolute values of calculated coordinates
 		float abx = 0;
 		float aby = 0;
 		float magnitude = 0;
 		
 		//map user heart rate to [-1,1]
 		x = uhr - ((maxHR + minHR) / 2);
 		x = x / ((maxHR - minHR) / 2);
 		
 		//map user respiration rate to [-1,1]
 		y = ure - ((maxResp + minResp) / 2);
 		y = y / ((maxResp - minResp) / 2);
 		
 		//map cube to r=1 sphere 
 		abx = Math.abs(x);
 		aby = Math.abs(y);
 		
 		//case A: upper most
 		if(y > 0 && y > abx){
 			cy = 1;
 			cx = abx / aby;
 		}
 		
 		//case B: lower most
 		if((-y > 0) && (-y > abx)){
 			cy = 1;
 			cx = abx / aby;
 		}
 		
 		//case C: right most
 		if(x > 0 && x > aby){
 			cx = 1;
 			cy = aby / abx;
 		}
 		
 		//case D: left most
 		if((-x > 0) && (-x > aby)){
 			cx = 1;
 			cy = aby / abx;
 		}
 		
 		//maps square to circle using ratio
 		if(abx == aby){
 			magnitude = (float) Math.sqrt(2);
 		}else{
 			magnitude = cx * cx + cy * cy;
 			magnitude = (float) Math.sqrt(magnitude);
 		}
 		
 		if(magnitude == 0){
 			ratio = 1;
 		}else{
 			ratio = 1 / magnitude;
 		}
 		
 		x = x * ratio;
 		y = y * ratio;
 		
 		//scale to display sphere
 		y = y * (maxPos - minPos) / 2;
 		y = y + ((maxPos + minPos) / 2);
 		x = x * (maxPos - minPos) / 2;
 		x = x + ((maxPos + minPos) / 2);
 
 		/*Log.d("dp", "x: " + x);
 		Log.d("dp", "y: " + y);*/
 		
 		//validation
 		y = Math.max(Math.min(y, maxPos), minPos);
 		x = Math.max(Math.min(x, maxPos), minPos);
 		
 		//update x and y values
 		scene.update(uid, DataType.X, x);
 		scene.update(uid, DataType.Y, y);
 	}
 	
 	/*
 	 * This method calls for the biometric and positional mappings and pushes the values
 	 * onto the rendering engine
 	 */
 	public void calculateTargets(){
 		updateMergedUsers();
 		
 		splitUsers();
 		
 		mergeUsers();
 		
 		mapBiometrics();
 	}
 	
 	//This method goes through every group of merged users 
 	//and calculates the average of their biometrics 
 	private void updateMergedUsers(){
 		for(MergedUser mu: merged){
 			updateMergedMetrics(mu);
 		}
 	}
 	
 	private void splitUsers(){
 		List<MergedUser> removedmus = new ArrayList<MergedUser>();
 		
 		for(MergedUser mu: merged){
 			String removeduu = "";
 			boolean changed = false;
 			
 			for(String uu: mu.members){
 				float dis = distance(mu, uu);
 				
 				//user is no longer within mdis of the average
				if(dis > udis){
 					users.get(uu).merged = false;
 					scene.update(uu, DataType.VOLUME, 1);
 					removeduu = uu;
 					changed = true;
 					break;
 				}
 			}
 			
 			//if an user was removed, need to recalculate the average of the metrics
 			if(changed){
 				mu.members.remove(removeduu);
 				if(mu.members.size() < 2){
 					//the users aren't similar anymore, the MU structured needs to be removed
 					for(String uu: mu.members){
 						users.get(uu).merged =  false;
 						scene.update(uu, DataType.VOLUME, 1);
 					}
 					mu.members.clear();
 					removedmus.add(mu);
 				}else{
 					updateMergedMetrics(mu);
 				}
 			}
 		}
 		
 		for(MergedUser mus:removedmus){
 			merged.remove(mus);
 		}
 		
 	}
 	
 	private void mergeUsers(){
 		//cycle through non-merged users
 		synchronized(users){
 			Collection<User> c = users.values();
 			
 			for(User user : c){
 				if(!user.merged){
 					for(MergedUser mu: merged){
 						float dis = distance(mu, user.id);
 						//the user is close to a merged user
 						if(dis <= mdis){
 							//Log.d("dp", "Added mu: " + user.id);
 							user.merged = true;
 							mu.members.add(user.id);
 							updateMergedMetrics(mu);
 							break;
 						}
 					}
 				}
 			}
 			
 			//cycle through pairs of unmerged users
 			//untested method for traversing values on a hash table twice
 			for(User u1: c){
 				for(User u2: c){
 					float dis = distance(u1.id, u2.id);
 					//new pair of similar users discovered
 					if(dis <= mdis && (u1.id != u2.id) && !u1.merged && !u2.merged){
 						u1.merged = true;
 						u2.merged = true;
 						//Log.d("dp", "Added mu: " + u1.id);
 						//Log.d("dp", "Added mu: " + u2.id);
 						MergedUser temp = new MergedUser(u1.id, u2.id);
 						updateMergedMetrics(temp);
 						merged.add(temp);
 					}
 				}
 			}
 		}
 		
 		//cycle through pairs of merged user structures
 		boolean flag = false;
 		MergedUser deletemu = null;
 		for(MergedUser mu1: merged){
 			for(MergedUser mu2: merged){
 				float dis = distance(mu1, mu2);
 				//new pair of similar groups of users that need to be multi merged
 				if(dis <= mdis && (mu1 != mu2)){
 					
 					for(String uu: mu2.members){
 						mu1.members.add(uu);
 					}
 					
 					updateMergedMetrics(mu1);
 					deletemu = mu2;
 					flag = true;
 					break;
 				}
 			}
 			if(flag) break;
 		}
 		if(flag){
 			deletemu.members.clear();
 			merged.remove(deletemu);
 		}
 	}
 	
 	public void mapBiometrics(){
 		synchronized(users){
 			Collection<User> c = users.values();
 			for(User user : c){
 				mapColor(user.id);
 				mapEnergy(user.id);
 				user.calculateHRV();
 				
 				if(!user.merged){
 					mapPosition(user.id);
 				}
 			}
 		}
 		
 		for(MergedUser mu: merged){
 			mapMergedPosition(mu);
 		}
 	}
 	
 	public float distance(String u1, String u2){
 		return distance(users.get(u1),users.get(u2));
 	}
 	
 	public float distance(MergedUser mu, String uu){
 		return distance(mu,users.get(uu));
 	}
 	
 	public float distance(User u1, User u2){
 		float dd = 0;
 		
 		float hr1 = getProportion(u1,BiometricType.HEARTRATE);
 		float hr2 = getProportion(u2,BiometricType.HEARTRATE);
 		float re1 = getProportion(u1,BiometricType.RESPIRATION);
 		float re2 = getProportion(u2,BiometricType.RESPIRATION);
 		float hv1 = getProportion(u1,BiometricType.HRV);
 		float hv2 = getProportion(u2,BiometricType.HRV);
 		
 		dd = (hr1 - hr2) * (hr1 - hr2) + (re1 - re2) * (re1 - re2) + (hv1 - hv2) * (hv1 - hv2);
 		dd = (float) Math.sqrt(dd);
 		
 		return dd;
 	}
 	
 	public void updateMergedMetrics(MergedUser mu){
 		float hr = 0;
 		float re = 0;
 		float hv = 0;
 		
 		for(String uu: mu.members){
 			hr = hr + users.get(uu).heartrate;
 			re = re + users.get(uu).respiration;
 			hv = hv + users.get(uu).hrv;
 		}
 		
 		mu.heartrate = hr / mu.members.size();
 		mu.respiration = re / mu.members.size();
 		mu.hrv = hv / mu.members.size();
 	}
 	
 	/*
 	 * This class runs the mappings based on the timer interval
 	 */
 	private class CalculationTimer extends TimerTask {
 		public void run(){
 			calculateTargets();
 		}
 	}
 	
 	public void quitDP(){
 		//stop the timer
 		utimer.cancel();
 		//clear the list storing the RR-intervals
 		Collection<User> c = users.values();
 		for(User user : c){
 			user.rrq.clear();
 		}
 	}
 	
 }
