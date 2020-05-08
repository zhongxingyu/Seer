 
 public class SubMessage {
 	private String title;
 	private boolean isSigned;
 	private boolean isBigEndian;
 	private float scalar;
 	private String units;
 	private int columnIndex;
 
	public SubMessage(String title, boolean isSigned, boolean isBigEndian, float scalar, String units) {
 		this.title = title;
 		this.isSigned = isSigned;
 		this.isBigEndian = isBigEndian;
 		this.scalar = scalar;
 		this.units = units;
 	}
 	
 	public float getValue(byte one, byte two) {		
 		String hexString = isBigEndian ? hex(one) + hex(two) : hex(two) + hex(one);
 		int hexValue = Integer.parseInt(hexString,16);
		
		if(title.equals("Yaw Rate")) hexValue -= 0x8000;
		if(title.equals("Yaw Acceleration")) hexValue -= 0x8000;
		if(title.equals("Lateral Acceleration")) hexValue -= 0x8000;
		if(title.equals("Longitudinal Acceleration")) hexValue -= 0x8000;
		if(title.equals("Brake Pressure 0")) hexValue -= 409.6;
		if(title.equals("Brake Pressure 1")) hexValue -= 409.6;
 		
 		if(isSigned) {
 			return (((short)hexValue) * scalar);
 		}
 		else {
 			return (hexValue * scalar);
 		}
 	}
 	
 	public int getColumnIndex() {
 		return this.columnIndex;
 	}
 	
 	public void setColumnIndex(int val) {
 		this.columnIndex = val;
 	}
 	
 	public String hex(byte num) {
 		return String.format("%02x", num);
 	}
 	
 	public String getTitle() {
 		return this.title;
 	}
 	
 	public String getUnits() {
 		return this.units;
 	}
 }
