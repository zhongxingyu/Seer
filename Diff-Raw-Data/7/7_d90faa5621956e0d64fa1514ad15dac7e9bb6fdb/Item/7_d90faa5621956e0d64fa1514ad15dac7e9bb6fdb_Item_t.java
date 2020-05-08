 package de.avdclan.arma2mapconverter;
 
 public class Item {
 	private Position position;
 	private String azimut;
 	private String special;
 	private String id;
 	private String vehicle;
 	private String typeClass;
 	private String side;
 	private String leader;
 	private String skill;
 	private String init;
 	private String type;
 	private String markerType = "\"DOT\"";
 	private String name;
 	private String presenceCondition = "true";
 	private String rank = "\"PRIVATE\"";
 	private String text;
 	private String colorName;
 	private String fillName;
 	private String a;
 	private String b;
 	private String angle;
 	private String rectangular = "false";
 	private String activationBy = "\"NONE\"";
 	private String repeating = "false";
 	private String age = "UNKNOWN";
 	private String expCond = "true";
 	private String expActiv = "";
 	private String expDesactiv = "";
 	private String interruptable = "true";
 	private String activationType = "NOT PRESENT";
 	private String timeoutMin = "0";
 	private String timeoutMid = "0";
 	private String timeoutMax = "0";
 
 	public Item(String typeClass) {
 		this.typeClass = typeClass;
 	}
 
 	public String getTimeoutMin() {
 		return timeoutMin;
 	}
 
 	public void setTimeoutMin(String timeoutMin) {
 		this.timeoutMin = timeoutMin;
 	}
 
 	public String getTimeoutMid() {
 		return timeoutMid;
 	}
 
 	public void setTimeoutMid(String timeoutMid) {
 		this.timeoutMid = timeoutMid;
 	}
 
 	public String getTimeoutMax() {
 		return timeoutMax;
 	}
 
 	public void setTimeoutMax(String timeoutMax) {
 		this.timeoutMax = timeoutMax;
 	}
 
 	public String getActivationType() {
 		return activationType;
 	}
 
 	public void setActivationType(String activationType) {
 		this.activationType = activationType;
 	}
 
 	public String getInterruptable() {
 		if(interruptable.equals("0"))
 			return "false";
 		else if(interruptable.equals("1"))
 			return "true";
 		else
 			return interruptable;
 	}
 
 	public void setInterruptable(String interruptable) {
 		this.interruptable = interruptable;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public String getRectangular() {
 		if(rectangular.equals("0"))
 			return "false";
 		else if(rectangular.equals("1"))
 			return "true";
 		else
 			return rectangular;
 
 	}
 
 	public void setRectangular(String rectangular) {
 		this.rectangular = rectangular;
 	}
 
 	public String getActivationBy() {
 		return activationBy;
 	}
 
 	public void setActivationBy(String activationBy) {
 		this.activationBy = activationBy;
 	}
 
 	public String getRepeating() {
 		if(repeating.equals("0"))
 			return "false";
 		else if(repeating.equals("1"))
 			return "true";
 		else
 			return repeating;
 	}
 
 	public void setRepeating(String repeating) {
 		this.repeating = repeating;
 	}
 
 	public String getAge() {
 		return age;
 	}
 
 	public void setAge(String age) {
 		this.age = age;
 	}
 
 	public String getExpCond() {
 		return expCond;
 	}
 
 	public void setExpCond(String expCond) {
 		this.expCond = expCond;
 	}
 
 	public String getExpActiv() {
 		return expActiv;
 	}
 
 	public void setExpActiv(String expActiv) {
 		this.expActiv = expActiv;
 	}
 
 	public String getExpDesactiv() {
 		return expDesactiv;
 	}
 
 	public void setExpDesactiv(String expDesactiv) {
 		this.expDesactiv = expDesactiv;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
 
 	public String getColorName() {
 		return colorName;
 	}
 
 	public void setColorName(String colorName) {
 		this.colorName = colorName;
 	}
 
 	public String getFillName() {
 		return fillName;
 	}
 
 	public void setFillName(String fillName) {
 		this.fillName = fillName;
 	}
 
 	public String getA() {
		if(a == null) { 
			return "0";
		}
 		return a;
 	}
 
 	public void setA(String a) {
 		this.a = a;
 	}
 
 	public String getB() {
		if(b == null) { 
			return "0";
		}
 		return b;
 	}
 
 	public void setB(String b) {
 		this.b = b;
 	}
 
 	public String getAngle() {
 		return angle;
 	}
 
 	public void setAngle(String angle) {
 		this.angle = angle;
 	}
 
 	public String getRank() {
 		return rank;
 	}
 
 	public void setRank(String rank) {
 		this.rank = rank;
 	}
 
 	public Position getPosition() {
 		return position;
 	}
 
 	public void setPosition(Position position) {
 		this.position = position;
 	}
 
 	public String getAzimut() {
 		return azimut;
 	}
 
 	public void setAzimut(String azimut) {
 		this.azimut = azimut;
 	}
 
 	public String getSpecial() {
 		return special;
 	}
 
 	public void setSpecial(String special) {
 		this.special = special;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getVehicle() {
 		return vehicle;
 	}
 
 	public void setVehicle(String vehicle) {
 		this.vehicle = vehicle;
 	}
 
 	public String getTypeClass() {
 		return typeClass;
 	}
 
 	public void setTypeClass(String typeClass) {
 		this.typeClass = typeClass;
 	}
 
 	public String getSide() {
 		return side.replaceAll("\"", "");
 	}
 
 	public void setSide(String side) {
 		this.side = side;
 	}
 
 	public String getLeader() {
 		return leader;
 	}
 
 	public void setLeader(String leader) {
 		this.leader = leader;
 	}
 
 	public String getSkill() {
 		return skill;
 	}
 
 	public void setSkill(String skill) {
 		this.skill = skill;
 	}
 
 	public String getInit() {
 		return init;
 	}
 
 	public void setInit(String init) {
 		this.init = init;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public String getMarkerType() {
 		return markerType;
 	}
 
 	public void setMarkerType(String markerType) {
 		this.markerType = markerType;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getPresenceCondition() {
 		return presenceCondition;
 	}
 
 	public void setPresenceCondition(String presenceCondition) {
 		this.presenceCondition = presenceCondition;
 	}
 
 }
