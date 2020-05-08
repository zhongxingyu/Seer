 /**
  * 
  */
 package AP2DX.usarsim.specialized;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import AP2DX.Message;
 import AP2DX.usarsim.UsarSimMessage;
 import AP2DX.usarsim.UsarSimMessage.UsarMessageField;
 
 /**
  * @author Jasper Timmer
  * 
  */
 public final class StateMessage extends UsarSimMessage {
 
 	@UsarMessageField(name = "Time")
 	private float time;
 
 	@UsarMessageField(name = "FrontSteer")
 	private float frontSteer;
 
 	@UsarMessageField(name = "RearSteer")
 	private float rearSteer;
 
 	@UsarMessageField(name = "LightToggle")
 	private boolean lightToggle;
 
 	@UsarMessageField(name = "LightIntensity")
 	private int lightIntensity;
 
 	@UsarMessageField(name = "Battery")
 	private int battery;
 
 	@UsarMessageField(name = "SternPlaneAngle")
 	private float sternPlaneAngle;
 
 	@UsarMessageField(name = "RudderAngle")
 	private float rudderAngle;
 
 	public StateMessage(UsarSimMessage msg) {
 		super(msg.getMessageString());
 		this.parseMessage();
 	}
 
 	/**
 	 * @see AP2DX.Message#parseMessage()
 	 */
 	@Override
 	public void parseMessage() {
 		String groupPatternStr = "\\{(\\w+) ([a-zA-Z0-9,._\\-]+)\\}";
 		Pattern groupPattern = Pattern.compile(groupPatternStr);
 		Matcher groupMatcher = groupPattern.matcher(this.getMessageString());
 
 		if (groupMatcher.find()) {
 			//this.links.add(new MissionStateLink(groupMatcher.group(1), groupMatcher.group(2), groupMatcher.group(3)));
 		}
 	}
 
 	/**
 	 * @return the time
 	 */
 	public float getTime() {
 		return time;
 	}
 
 	/**
 	 * @return the frontSteer
 	 */
 	public float getFrontSteer() {
 		return frontSteer;
 	}
 
 	/**
 	 * @return the rearSteer
 	 */
 	public float getRearSteer() {
 		return rearSteer;
 	}
 
 	/**
 	 * @return the lightToggle
 	 */
 	public boolean isLightToggle() {
 		return lightToggle;
 	}
 
 	/**
 	 * @return the lightIntensity
 	 */
 	public int getLightIntensity() {
 		return lightIntensity;
 	}
 
 	/**
 	 * @return the battery
 	 */
 	public int getBattery() {
 		return battery;
 	}
 
 	/**
 	 * @return the SternPlaneAngle
 	 */
 	public float SternPlaneAngle() {
 		return sternPlaneAngle;
 	}
 
 	/**
 	 * @return the RudderAngle
 	 */
 	public float getRudderAngle() {
 		return rudderAngle;
 	}
 
 	/**
 	 * @return the type
 	 */
 	public Message.MessageType getType() {
 		return this.type;
 	}
 
 	/**
 	 * @return the sternPlaneAngle
 	 */
 	public float getSternPlaneAngle() {
 		return sternPlaneAngle;
 	}
 
 	/**
 	 * @param type the type to set
 	 */
	public MessageType setType(Message.MessageType type) {
 		this.type = type;
		return type;
 	}
 
 	/**
 	 * @param time the time to set
 	 */
 	public void setTime(float time) {
 		this.time = time;
 	}
 
 	/**
 	 * @param frontSteer the frontSteer to set
 	 */
 	public void setFrontSteer(float frontSteer) {
 		this.frontSteer = frontSteer;
 	}
 
 	/**
 	 * @param rearSteer the rearSteer to set
 	 */
 	public void setRearSteer(float rearSteer) {
 		this.rearSteer = rearSteer;
 	}
 
 	/**
 	 * @param lightToggle the lightToggle to set
 	 */
 	public void setLightToggle(boolean lightToggle) {
 		this.lightToggle = lightToggle;
 	}
 
 	/**
 	 * @param lightIntensity the lightIntensity to set
 	 */
 	public void setLightIntensity(int lightIntensity) {
 		this.lightIntensity = lightIntensity;
 	}
 
 	/**
 	 * @param battery the battery to set
 	 */
 	public void setBattery(int battery) {
 		this.battery = battery;
 	}
 
 	/**
 	 * @param sternPlaneAngle the sternPlaneAngle to set
 	 */
 	public void setSternPlaneAngle(float sternPlaneAngle) {
 		this.sternPlaneAngle = sternPlaneAngle;
 	}
 
 	/**
 	 * @param rudderAngle the rudderAngle to set
 	 */
 	public void setRudderAngle(float rudderAngle) {
 		this.rudderAngle = rudderAngle;
 	}
 
 }
