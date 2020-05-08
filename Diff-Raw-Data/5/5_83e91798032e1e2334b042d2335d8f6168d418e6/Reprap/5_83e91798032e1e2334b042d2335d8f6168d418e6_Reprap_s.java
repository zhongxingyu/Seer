 package org.reprap.machines;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import org.reprap.CartesianPrinter;
 import org.reprap.ReprapException;
 import org.reprap.comms.Communicator;
 import org.reprap.comms.snap.SNAPAddress;
 import org.reprap.comms.snap.SNAPCommunicator;
 import org.reprap.devices.GenericExtruder;
 import org.reprap.devices.GenericStepperMotor;
 import org.reprap.devices.pseudo.LinePrinter;
 import org.reprap.gui.Previewer;
 
 /**
  * 
  * A Reprap printer is a 3-D cartesian printer with one or more
  * extruders
  *
  */
 public class Reprap implements CartesianPrinter {
 	private final int localNodeNumber = 0;
 	private final int baudRate = 19200;
 
 	private Communicator communicator;
 	private Previewer previewer = null;
 
 	private GenericStepperMotor motorX;
 	private GenericStepperMotor motorY;
 	private GenericStepperMotor motorZ;
 
 	private LinePrinter layer;
 	
 	double scaleX, scaleY, scaleZ;
 	
 	double currentZ;
 	
 	private int speed = 236;
 	private int speedExtruder = 200;
 	
 	private GenericExtruder extruder;  ///< Only one supported for now
 
 	final boolean dummyZ = true;  // Don't perform Z operations
 	
 	public Reprap(Properties config) throws Exception {
 		int axes = Integer.parseInt(config.getProperty("AxisCount"));
 		if (axes != 3)
 			throw new Exception("A Reprap printer must contain 3 axes");
 		int extruders = Integer.parseInt(config.getProperty("ExtruderCount"));
 		if (extruders < 1)
 			throw new Exception("A Reprap printer must contain at least one extruder");
 		
 		String commPortName = config.getProperty("Port");
 		
 		SNAPAddress myAddress = new SNAPAddress(localNodeNumber); 
 		communicator = new SNAPCommunicator(commPortName, baudRate, myAddress);
 		
 		motorX = new GenericStepperMotor(communicator,
 				new SNAPAddress(config.getProperty("Axis1Address")),
 				Integer.parseInt(config.getProperty("Axis1Torque")));
 		motorY = new GenericStepperMotor(communicator,
 				new SNAPAddress(config.getProperty("Axis2Address")),
 				Integer.parseInt(config.getProperty("Axis2Torque")));
 		motorZ = new GenericStepperMotor(communicator,
 				new SNAPAddress(config.getProperty("Axis3Address")),
 				Integer.parseInt(config.getProperty("Axis3Torque")));
 		
 		extruder = new GenericExtruder(communicator,
 				new SNAPAddress(config.getProperty("Extruder1Address")),
 				Integer.parseInt(config.getProperty("Extruder1Beta")),
 				Integer.parseInt(config.getProperty("Extruder1Rz")));
 
 		layer = new LinePrinter(motorX, motorY, extruder);
 
 		// TODO This should be from calibration
 		// Assume 400 steps per turn, 1.5mm travel per turn
 		scaleX = scaleY = scaleZ = 400.0 / 1.5;
 		
 		if (!dummyZ) {
 			currentZ = convertToPositionZ(motorZ.getPosition());
 		}
 	}
 	
 	public void calibrate() {
 	}
 
 	public void printSegment(double startX, double startY, double startZ, double endX, double endY, double endZ) throws ReprapException, IOException {
 		moveTo(startX, startY, startZ);
 		printTo(endX, endY, endZ);
 	}
 
 	public void moveTo(double x, double y, double z) throws ReprapException, IOException {
 		if (isCancelled()) return;
 
 		layer.moveTo(convertToStepX(x), convertToStepY(y), speed);
 		if (z != currentZ) {
 			if (!dummyZ) motorZ.seekBlocking(speed, convertToStepZ(z));
 			currentZ = z;
 		}
 	}
 
 	public void printTo(double x, double y, double z) throws ReprapException, IOException {
 		if (isCancelled()) return;
		
 		EnsureNotEmpty();
 		EnsureHot();
 
 		if ((x != convertToPositionX(layer.getCurrentX()) || y != convertToPositionY(layer.getCurrentY())) && z != currentZ)
 			throw new ReprapException("Reprap cannot print a line across 3 axes simultaneously");
 
 		if (previewer != null)
 			previewer.addSegment(convertToPositionX(layer.getCurrentX()),
 					convertToPositionY(layer.getCurrentY()), currentZ,
 					x, y, z);
 
 		if (isCancelled()) return;
 		
 		
 		if (x == convertToPositionX(layer.getCurrentX()) && y == convertToPositionY(layer.getCurrentY()) && z != currentZ) {
 			// Print a simple vertical extrusion
 			// TODO extrusion speed should be based on actual head speed
 			// which depends on the angle of the line
 			extruder.setExtrusion(speedExtruder);
 			if (!dummyZ) motorZ.seekBlocking(speed, convertToStepZ(z));
 			extruder.setExtrusion(0);
 			currentZ = z;
 			return;
 		}
 
 		// Otherwise printing only in X/Y plane
 		layer.printTo(convertToStepX(x), convertToStepY(y), speed, speedExtruder);
 	}
 
 	public void selectMaterial(int materialIndex) {
 		if (isCancelled()) return;
 
 		if (previewer != null)
 			previewer.setMaterial(materialIndex);
 
 		if (isCancelled()) return;
 		// TODO Select new material
 	}
 
 	protected int convertToStepX(double n) {
 		return (int)(n * scaleX);
 	}
 
 	protected int convertToStepY(double n) {
 		return (int)(n * scaleY);
 	}
 
 	protected int convertToStepZ(double n) {
 		return (int)(n * scaleZ);
 	}
 
 	protected double convertToPositionX(int n) {
 		return n / scaleX;
 	}
 
 	protected double convertToPositionY(int n) {
 		return n / scaleY;
 	}
 
 	protected double convertToPositionZ(int n) {
 		return n / scaleZ;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.reprap.Printer#terminate()
 	 */
 	public void terminate() throws Exception {
 		motorX.setIdle();
 		motorY.setIdle();
 		motorX.setIdle();
 		extruder.setExtrusion(0);
 		extruder.setTemperature(0);
 	}
 	
 	public void dispose() {
 		motorX.dispose();
 		motorY.dispose();
 		motorZ.dispose();
 		extruder.dispose();
 		communicator.close();
 		communicator.dispose();
 	}
 
 	/**
 	 * @return Returns the speed.
 	 */
 	public int getSpeed() {
 		return speed;
 	}
 	/**
 	 * @param speed The speed to set.
 	 */
 	public void setSpeed(int speed) {
 		this.speed = speed;
 	}
 	/**
 	 * @return Returns the speedExtruder.
 	 */
 	public int getExtruderSpeed() {
 		return speedExtruder;
 	}
 	/**
 	 * @param speedExtruder The speedExtruder to set.
 	 */
 	public void setExtruderSpeed(int speedExtruder) {
 		this.speedExtruder = speedExtruder;
 	}
 	
 	public void setPreviewer(Previewer previewer) {
 		this.previewer = previewer;
 	}
 
 	public void setTemperature(int temperature) throws Exception {
 		extruder.setTemperature(temperature);
 	}
 
 	private void EnsureNotEmpty() {
 		if (!extruder.isEmpty()) return;
 		
 		while (extruder.isEmpty() && !isCancelled()) {
 			if (previewer != null)
 				previewer.setMessage("Extruder is out of feedstock.  Waiting for refill.");
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 			}
 		}
 		if (previewer != null) previewer.setMessage(null);
 	}
 	
 	private void EnsureHot() {
 		double threshold = extruder.getTemperatureTarget() * 0.95;
 		
 		if (extruder.getTemperature() >= threshold)
 			return;
 
 		while(extruder.getTemperature() < threshold && !isCancelled()) {
 			if (previewer != null) previewer.setMessage("Waiting for extruder to reach working temperature (" + Math.round(extruder.getTemperature()) + ")");
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 			}
 		}
 		if (previewer != null) previewer.setMessage(null);
 		
 	}
 
 	public boolean isCancelled() {
 		if (previewer == null)
 			return false;
 		return previewer.isCancelled();
 	}
 	
 	public void initialise() {
 		if (previewer != null)
 			previewer.reset();
 	}
 }
 
 
