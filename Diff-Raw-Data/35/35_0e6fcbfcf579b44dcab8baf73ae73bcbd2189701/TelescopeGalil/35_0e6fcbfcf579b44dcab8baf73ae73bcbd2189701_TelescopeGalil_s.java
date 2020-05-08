 package edu.ucsb.deepspace;
 
 public class TelescopeGalil implements TelescopeInterface {
 	
 	private Stage stage;
 	private CommGalil protocol;
 	
 	private GalilAxis az, el;
 	private double AZ_ENC = 1000d*1024d;
 	private double EL_ENC = 4000d;
 	
 	private String relative = "PR", absolute = "PA";
 	
 	public TelescopeGalil(Stage stage, CommGalil protocol) {
 		this.stage = stage;
 		this.protocol = protocol;
 		az = new GalilAxis(Axis.AZ, AZ_ENC);
 		el = new GalilAxis(Axis.EL, EL_ENC);
 		
 	}
 
 	@Override
 	public void move(MoveCommand mc) {
 		int azEnc = (int) calcEncVal(mc, Axis.AZ);
 		int elEnc = (int) calcEncVal(mc, Axis.EL);
 		String out = "";
 		switch (mc.getMode()) {
 			case RELATIVE:
 				out = relative;
 				break;
 			case ABSOLUTE:
 				out = absolute;
 				break;
 			default:
 				assert false; //This can only be reached if another move mode is added.
 		}
 		out += " " + azEnc*az.polarity + "," + elEnc*el.polarity;
 		System.out.println(out);
 		stage.setGoalPos(goalUserDeg(mc, Axis.AZ), Axis.AZ);
 		stage.setGoalPos(goalUserDeg(mc, Axis.EL), Axis.EL);
 		protocol.sendRead(out);
 		protocol.sendRead("BG");
 	}
 
 	@Override
 	public boolean validMove(MoveCommand mc, double minAz, double maxAz, double minEl, double maxEl) {
		double goalAz = goalUserDeg(mc, Axis.AZ);
 		System.out.println("goalAz: " + goalAz);
 		
		double goalEl = goalUserDeg(mc, Axis.EL);
 		System.out.println("goalEl: " + goalEl);
 		
 		boolean validAz = (minAz <= goalAz && goalAz <= maxAz);
 		boolean validEl = (minEl <= goalEl && goalEl <= maxEl);
 		if (validAz && validEl) {
 			return true;
 		}
 		return false;
 	}
 	
 	private double calcEncVal(MoveCommand mc, Axis axis) {
 		GalilAxis temp = picker(axis);
 		double goal = 0;
 		Double amount = mc.getAmount(axis);
 		if (amount == null) {
 			switch (mc.getMode()) {
 				case RELATIVE:
 					goal = 0; break;
 				case ABSOLUTE:
 					goal = temp.currentEncPos(); break;
 				default:
 					assert false; //This is never reached unless a new move mode is added.
 			}
 			return goal;
 		}
 		
 		switch (mc.getType()) {
 			case ENCODER:
 				goal = amount; break;
 			case DEGREE:
 				switch (mc.getMode()) {
 					case RELATIVE:
 						goal = temp.convDegToEnc(amount); break;
 					case ABSOLUTE:
 						goal = temp.userDegToEnc(amount); break;
 					default:
 						assert false; //This is never reached unless a new move mode is added.
 				}
 				break;
 			default:
 				assert false; //This is never reached unless a new move type is added.
 		}
 		
 		return goal;
 	}
 	
 	
 	//Not sure if it can handle null MC
 	private double goalUserDeg(MoveCommand mc, Axis axis) {
 		double goal = 0;
 		GalilAxis temp = picker(axis);
 		Double amount = mc.getAmount(axis);
 		if (amount == null) {
 			goal = temp.userPos();
 			return goal;
 		}
 		switch (mc.getMode()) {
 			case RELATIVE:
 				switch (mc.getType()) {
 					case ENCODER:
 						goal = temp.userPos() + temp.convEncToDeg(amount); break;
 					case DEGREE:
 						goal = temp.userPos() + amount; break;
 				}
 				break;
 			case ABSOLUTE:
 				switch (mc.getType()) {
 					case ENCODER:
 						goal = temp.convEncToDeg(amount); break;
 					case DEGREE:
 						goal = amount; break;
 				}
 				break;
 		}
 		return goal;
 	}
 
 	@Override
 	public void setVelocity(double vel, Axis axis) {
 		GalilAxis temp = picker(axis);
 		if (stage.validateSpeed(vel, axis)) {
 			temp.setVelocity(vel);
 		}
 		else {
 			stage.statusArea("This velocity fall outside the maximum values specified.\n");
 		}
 	}
 	
 	@Override
 	public void setAccel(double acc, Axis axis) {
 		GalilAxis temp = picker(axis);
 		if (stage.validateAccel(acc, axis)) {
 			temp.setAccel(acc);
 		}
 		else {
 			stage.statusArea("This acceleration fall outside the maximum values specified.\n");
 		}
 	}
 
 	@Override
 	public boolean isMoving() {
 		boolean azMoving = az.isMoving();
 		boolean elMoving = el.isMoving();
 		return azMoving || elMoving;
 	}
 
 	@Override
 	public void stop(Axis axis) {
 		GalilAxis temp = picker(axis);
 		temp.stop();
 	}
 
 	@Override
 	public void setOffsets(double azOffset, double elOffset) {
 		az.offset = azOffset;
 		el.offset = elOffset;
 	}
 
 	@Override
 	public double getOffset(Axis axis) {
 		GalilAxis temp = picker(axis);
 		return temp.offset;
 	}
 
 	@Override
 	public void calibrate(Coordinate c) {
 		az.calibrate(c.getAz(), stage.encPos(Axis.AZ));
 		el.calibrate(c.getEl(), stage.encPos(Axis.EL));
 	}
 
 	@Override
 	public double getUserPos(Axis axis) {
 		return getAbsolutePos(axis) % 360;
 	}
 
 	@Override
 	public double getAbsolutePos(Axis axis) {
 		GalilAxis temp = picker(axis);
 		double currentEncPos = stage.encPos(axis);
 		return temp.encToAbsDeg(currentEncPos);
 	}
 
 	@Override
 	public void scan(ScanCommand azSc, ScanCommand elSc, boolean fraster) {
 			if (azSc!=null && elSc!=null) 
 				rasterScan(azSc, elSc, fraster);
 			else if(azSc!=null && elSc==null) 
 				
 				azScan(azSc);
 			else{
 				elScan(elSc);
 			}
 	}
 	public void scan(ScanCommand azSc, ScanCommand elSc) {
 		scan(azSc, elSc, false);
 	
 }
 
 	@Override
 	public void stopScanning() {
 		// All scans are on thread 0
 		pause();
 		protocol.sendRead("ST");
 		pause();
 	}
 
 	@Override
 	public void motorControl(boolean azOnOff, boolean elOnOff) {
 		if (azOnOff) {
 			az.motorOn();
 		}
 		else {
 			az.motorOff();
 		}
 		if (elOnOff) {
 			el.motorOn();
 		}
 		else {
 			el.motorOff();
 		}
 	}
 
 
 	
 	@Override
 	public void queryMotorState() {
 		String response = protocol.sendRead("MG _MOA, _MOB");
 		String[] temp = response.split(" ");
 		az.motorState = (0 == Double.parseDouble(temp[1]));
 		el.motorState = (0 == Double.parseDouble(temp[2]));
 	}
 
 	@Override
 	public void motorToggle(Axis axis) {
 		GalilAxis temp = picker(axis);
 		if (temp.motorState) {
 			temp.motorOff();
 		}
 		else {
 			temp.motorOn();
 		}
 	}
 
 	@Override
 	public boolean isIndexing(Axis axis) {
 		GalilAxis temp = picker(axis);
 		return temp.indexing;
 	}
 	
 	@Override
 	public double rpm(double vel, Axis axis) {
 		GalilAxis temp = picker(axis);
 		double encPerRev = temp.encPerDeg*360d;
 		double rpm = (vel / encPerRev) * 60d;
 		return rpm;
 	}
 	
 	@Override
 	public void setSpeedByRpm(double rpm, Axis axis) {
 		GalilAxis temp = picker(axis);
 		double speed = (rpm / 60d) * 360d * temp.encPerDeg;
 		temp.setVelocity(speed);
 	}
 
 	@Override
 	public void index(Axis axis) {
 		protocol.initialize();
 		switch (axis) {
 			case AZ:
 				az.indexing = true;
 				protocol.sendRead("XQ #HOMEAZ,1");
 				waitWhileMoving(axis);
 				az.indexing = false;
 				break;
 			case EL:
 				el.indexing = true;
 				protocol.sendRead("XQ #HOMEB,2");
 				waitWhileMoving(axis);
 				az.indexing = false;
 				break;
 			default:
 				System.out.println("TelescopeGalil.index error. reached end of switch statement");
 		}
 	}
 	
 	private GalilAxis picker(Axis axis) {
 		switch (axis) {
 			case AZ:
 				return az;
 			case EL:
 				return el;
 		}
 		throw new Error("This should never happen.  TelescopeGalil.picker reached the end.");
 	}
 	
 	private void waitWhileMoving(Axis axis) {
 		GalilAxis temp = picker(axis);
 		sleep(500);
 		while (temp.isMoving()) {
 			sleep(500);
 		}
 	}
 	
 	private void sleep(long timeInMS) {
 		try {
 			Thread.sleep(timeInMS);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	public void rasterScan(ScanCommand azSc, ScanCommand elSc, boolean fraster) {
 		//el.indexing = true;
 		int el_inc= 10;
 	
 		
 		//all lines are roughly the same distance so I only calculate the distance of the bottom ones
 		double deltaX = Math.abs((az.absDegToEnc(azSc.getMax())-az.absDegToEnc(azSc.getMin())));
 		//el_inc is basically the slope, so I can use slope formula to get y1
 		double deltaY  = Math.abs(el_inc*deltaX);
 		double lineNums = el.absDegToEnc(elSc.getMax())/el_inc;
 		double azSpeed =  (int)lineNums*deltaX/azSc.getTime();
 		double elSpeed =  (int)lineNums*deltaY/elSc.getTime();
 		System.out.println(azSpeed);
 		
 		
 	
 		//min az
 		
 		protocol.sendRead("minAz = "+GalilCalc.round(az.absDegToEnc(azSc.getMin()), 4));
 		System.out.println(az.absDegToEnc(azSc.getMin()));
 		System.out.println(az.absDegToEnc(azSc.getMax()));
 		System.out.println(el.absDegToEnc(elSc.getMin()));
 		System.out.println(el.absDegToEnc(elSc.getMax()));
 		pause();
 		System.out.println(el.absDegToEnc(elSc.getMax()));
 		pause();
 		protocol.sendRead("minEl= "+GalilCalc.round(+el.absDegToEnc(elSc.getMin()),4));
 		pause();
 		//max az
 		protocol.sendRead("maxAz = "+GalilCalc.round(az.absDegToEnc(azSc.getMax()),4));
 		// EL Inc
 		pause();
 		protocol.sendRead("maxEl="+GalilCalc.round(el.absDegToEnc(elSc.getMax()), 4));
 		pause();
 		protocol.sendRead("lineNum = "+el_inc);
 		pause();
 		protocol.sendRead("time = "+azSc.getTime());
 		
 		//number of increments
 		//protocol.sendRead("V6 = "+elSc.getReps());
 		pause();
 		System.out.println("time"+azSc.getTime());
 		
 		az.scanning = true;
 		el.scanning = true;
 		if (fraster){
 			protocol.sendRead("HX 0;XQ #FRASTER,0");
 			System.out.println("fraster");
 		}
 		else{
 			protocol.sendRead("HX 0;XQ #RASTER,0");
 			System.out.println("raster");
 		}
 		pause();
 		waitWhileMoving(Axis.AZ);
 		waitWhileMoving(Axis.EL);
 		pause();
 		az.scanning = false;
 		el.scanning = false;
 		System.out.println("Min az:" + azSc.getMin() +" maxAz: "+ azSc.getMax()+" minEl:"+elSc.getMin()+" maxEl:"+elSc.getMax());
 				
 		
 		}
 	
 	public void azScan(ScanCommand azSc) {
 		
 		
 	
 		//protocol.sendRead("PA "+GalilCalc.round(az.absDegToEnc(azSc.getMin()),4)+" ;BG A;AM;WT 200");
 		pause();
 		protocol.sendRead("time="+ GalilCalc.round(azSc.getTime(), 4));
 		pause();
 		//min az
 		protocol.sendRead("minAz = "+GalilCalc.round(az.absDegToEnc(azSc.getMin()),4));
 		pause();
 		//max az
 		protocol.sendRead("maxAz = "+GalilCalc.round(az.absDegToEnc(azSc.getMax()),4));
 		pause();
 		
 		
 		az.scanning = true;
 		protocol.sendRead("HX 0;XQ #AZSCAN,0");
 		waitWhileMoving(Axis.AZ);
 		az.scanning = false;		
 
 
 	}
 	public void elScan(ScanCommand elSc) {
 		
 		double elDelta =  el.convDegToEnc((Math.abs(elSc.getMin()-elSc.getMax())));
 		
 		//protocol.sendRead("PA 0,"+el.absDegToEnc(elSc.getMin())+" ;BG A;AM;WT 200");
 		pause();
 		protocol.sendRead("time="+ GalilCalc.round(elSc.getTime(), 4));
 		pause();
 		//min el
 		protocol.sendRead("minEl = "+GalilCalc.round(el.absDegToEnc(elSc.getMin()),4));
 		pause();
 		System.out.println("dd"+el.convEncToDeg(GalilCalc.round(el.absDegToEnc(elSc.getMax()),4)));
 		//max el
 		protocol.sendRead("maxEl="+GalilCalc.round(el.absDegToEnc(elSc.getMax()),4));
 		pause();
 		
 		
 		el.scanning = true;
 		protocol.sendRead("HX 0;XQ #ELSCAN,0");
 		waitWhileMoving(Axis.EL);
 		System.out.println("scanning");
 		el.scanning = false;		
 
 
 	}
 	
 	public void safty(double minAz, double maxAz,double minEl, double maxEl){
 		
 		protocol.sendRead("ElMinLim="+GalilCalc.round(el.absDegToEnc(minEl), 4));
 		pause();
 		protocol.sendRead("ElMaxLim="+GalilCalc.round(el.absDegToEnc(maxEl),4));
 		pause();
 		protocol.sendRead("AzMinLim="+GalilCalc.round(az.absDegToEnc(minAz),4));
 		pause();
 		protocol.sendRead("AzMaxLim="+GalilCalc.round(az.absDegToEnc(maxAz),4));
 		pause();
 		protocol.sendRead("XQ #SAFTY");
 		pause();
 		System.out.println(protocol.sendRead("MG _BLa, _BLb, _FLa, _FLb"));
 		pause();
 		
 		
 		
 	}
 		//waitWhileMoving(axis);
 		//az.indexing = false;
 	
 	public double getScanTime(ScanCommand sc, double max_vel, double max_acc, double max_distance, Axis axis ){
 		double distance;
 		switch (axis){
 		case EL:
 			distance =getDistance(sc.getMax(), sc.getMin(), Axis.EL);
 			break;
 		case AZ:
 			 distance =getDistance(sc.getMax(), sc.getMin(), Axis.AZ);
 			break;
 		default:
 			 distance =0;
 			
 		
 		}
 		
 		double time = distance/max_vel;
 		double tot_distance = (distance + max_acc*time*time);
 		if (tot_distance>max_distance)
 			return 0.0;
 		return time;
 			
 		
 	
 		
 	
 	}
 	public double getDistance(double max, double min, Axis axis){
 		switch (axis){
 		case AZ:
 			return az.convDegToEnc(max)-az.convDegToEnc(min);
 		case EL:
 			return el.convDegToEnc(max)-el.convDegToEnc(min);
 		default:
 			return 0;
 		}
 	}
 	
 	public double convEncToDeg(double enc, Axis axis){
 		switch (axis){
 		case AZ:
 			return az.convEncToDeg(enc);
 		case EL:
 			return el.convEncToDeg(enc);
 		default:
 			return 0;
 		}
 	}
 	
 
 	
 
 
 	private void pause() {
 
 		try {
 			Thread.sleep(500);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private class GalilAxis {
 		private Axis axis;
 		private boolean indexing = false;
 		private boolean scanning = false;
 		private boolean motorState = true;
 		private String abbrev;
 		private double encPerDeg;
 		private double offset;
 		private int polarity;
 		
 		private GalilAxis(Axis axis, double encPerRev) {
 			this.axis = axis;
 			abbrev = axis.getAbbrev();
 			encPerDeg = encPerRev / 360d;
 			this.polarity = axis.getPolarity();
 		}
 		
 		private void motorOn() {
 			protocol.sendRead("SH" + abbrev);
 			motorState = true;
 		}
 		
 		private void motorOff() {
 			protocol.sendRead("MO" + abbrev);
 			motorState = false;
 		}
 		
 		private void stop() {
 			
 			protocol.sendRead("ST" + abbrev);
 			
 		}
 		
 		private boolean isMoving() {
 			boolean flag = true;
 			String temp = protocol.sendRead("MG _BG" + abbrev);
 			if (temp.contains(" 0.0000")) {
 				flag =  false;
 			}
 			protocol.read();
 			return flag;
 		}
 		
 		/**
 		 * Converts a number of encoder pulses into degrees.
 		 * @param enc
 		 * @return
 		 */
 		private double convEncToDeg(double enc) {
 			return enc / encPerDeg;
 		}
 		
 		/**
 		 * Converts degrees into encoder pulses.
 		 * @param deg
 		 * @return
 		 */
 		private double convDegToEnc(double deg) {
 			return deg * encPerDeg;
 		}
 		
 		/**
 		 * Converts an encoder value into the absolute position. <P>
 		 * Inverse of absDegToEnc()
 		 * @param enc value of encoder
 		 * @return absolute degree position
 		 */
 		private double encToAbsDeg(double enc) {
 			return offset + convEncToDeg(enc);
 		}
 		
 		/**
 		 * Converts an absolute degree position into an encoder value. <P>
 		 * Inverse of encToAbsDeg
 		 * @param deg position in degrees
 		 * @return encoder value
 		 */
 		private double absDegToEnc(double deg) {
 			return convDegToEnc(deg - offset);
 		}
 		
 		/**
 		 * Converts user degrees into encoder value. <P>
 		 * First converts user degrees into absolute degrees.
 		 * @param deg
 		 * @return
 		 */
 		private double userDegToEnc(double deg) {
 			double absDeg = userDegToAbsDeg(deg);
 			return absDegToEnc(absDeg);
 		}
 		
 		/**
 		 * Converts user degrees into absolute degrees.
 		 * @param deg
 		 * @return 
 		 */
 		private double userDegToAbsDeg(double deg) {
 			return absolutePos() - userPos() + deg;
 		}
 		
 		/**
 		 * 
 		 * @return the absolute position in degrees
 		 */
 		private double absolutePos() {
 			double encPos = stage.encPos(axis);
 			double absDeg = encToAbsDeg(encPos);
 			return absDeg;
 		}
 		
 		/**
 		 * The position of the actuator in degrees. <P>
 		 * Prevents the user from ever seeing a position like 540 degrees.
 		 * @return absolutePos() % 360
 		 */
 		private double userPos() {
 			return absDegToUserDeg(absolutePos());
 		}
 		
 		/**
 		 * Converts absolute degrees into user degrees.
 		 * @param deg
 		 * @return
 		 */
 		private double absDegToUserDeg(double deg) {
 			return deg % 360;
 		}
 		
 		/**
 		 * Calibrates the galil.<P>
 		 * Once indexed, this method basically says "encoder count X corresponds to degree Y".
 		 * Then, when power is lost, once the galil has been indexed, it will know where it is.
 		 * @param degVal
 		 * @param encPos
 		 */
 		private void calibrate(double degVal, double encPos) {
 			offset = degVal - convEncToDeg(encPos);
 		}
 		
 		private double currentEncPos() {
 			return stage.encPos(axis);
 		}
 		
 		private void setVelocity(double vel) {
 			protocol.sendRead("SP" + abbrev + "=" + (int) vel);
 		}
 		
 		private void setAccel(double acc) {
 			protocol.sendRead("AC" + abbrev + "=" + (int) acc);
 		}
 		
 		
 		
 		
 			
 				
 				
 				
 
 
 		
 		
 	}
 	
 
 }
