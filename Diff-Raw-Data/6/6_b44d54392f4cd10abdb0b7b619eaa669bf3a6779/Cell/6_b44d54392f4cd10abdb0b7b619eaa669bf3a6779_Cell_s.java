 package bloom.filters;
 
 public class Cell {
 	//Constants
 	public static String INITIAL_ID_SUM = "    "; //32 bits
 	public static int INITIAL_HASH_SUM = INITIAL_ID_SUM.hashCode();
 	
 	//Private Members
 	private String idSum;
 	private int hashSum;
 	private byte count;
 	
 	public Cell(){
 		idSum = INITIAL_ID_SUM;
 		hashSum = INITIAL_HASH_SUM;
 		count = 0;
 	}
 	
 	public void add(String key){
 		idSum = XOR(idSum,key);
 		hashSum = hashSum ^ key.hashCode();
 		count++;
 	}
 	
 	public void remove(String key){
 		idSum = XOR(idSum,key);
 		hashSum = hashSum ^ key.hashCode();
		count--;
 	}
 	
 	public boolean isEmpty(){
 		return idSum.equals(INITIAL_ID_SUM) && hashSum == INITIAL_HASH_SUM && count == 0;
 	}
 	
 	public boolean isPure(){
 		return !isEmpty() && hashSum == (extractKey().hashCode()^INITIAL_HASH_SUM) && Math.abs(count) == 1;
 	}
 	
 	public String extractKey(){
 		return XOR(INITIAL_ID_SUM, idSum);
 	}	
 	
 	public static Cell subtract(Cell c1, Cell c2){
 		Cell c = new Cell();
 		c.idSum = XOR(XOR(c1.idSum, c2.idSum), INITIAL_ID_SUM);
 		c.hashSum = c1.hashSum ^ c2.hashSum ^ INITIAL_HASH_SUM;
 		c.count = (byte) (c1.count - c2.count);
 		return c;
 	}
 	
 	//TODO pad the strings to the same size
 	public static String XOR(String s1, String s2) {
 		StringBuilder sb = new StringBuilder();
 		assert s1.length() == s2.length(); //TODO replace with exception
 		
 		for(int i=0; i<s1.length() && i<s2.length();i++){
 		    sb.append((char)(s1.charAt(i) ^ s2.charAt(i)));
 		}
 		String result = sb.toString();
 		return result;
 	}
 	
 	public String getIdSum() {
 		return idSum;
 	}
 
 	public void setIdSum(String idSum) {
 		this.idSum = idSum;
 	}
 
 	public int getHashSum() {
 		return hashSum;
 	}
 
 	public void setHashSum(int hashSum) {
 		this.hashSum = hashSum;
 	}
 	
 	public int getCount() {
 		return count;
 	}
 	
 	public void setCount(byte count) {
 		this.count = count;
 	}
 }
