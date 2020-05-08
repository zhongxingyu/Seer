 
 public class SubMessage {
 	private String title;
 	private boolean isSigned;
 	private boolean isBigEndian;
 	private float scalar;
 	private String units;
	private float offset;
 	private int columnIndex;
 
	public SubMessage(String title, boolean isSigned, boolean isBigEndian, float scalar, float offset, String units) {
 		this.title = title;
 		this.isSigned = isSigned;
 		this.isBigEndian = isBigEndian;
 		this.scalar = scalar;
		this.offset = offset;
 		this.units = units;
 	}
 	
 	public float getValue(byte one, byte two) {		
 		String hexString = isBigEndian ? hex(one) + hex(two) : hex(two) + hex(one);
 		int hexValue = Integer.parseInt(hexString,16);
				
		hexValue = (int)(((float)hexValue) - offset);
 		
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
