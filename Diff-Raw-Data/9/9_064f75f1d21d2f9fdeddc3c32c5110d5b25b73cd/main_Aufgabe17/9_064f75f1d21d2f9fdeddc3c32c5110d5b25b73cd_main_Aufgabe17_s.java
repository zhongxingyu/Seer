 
 public class main_Aufgabe17 {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
       char[] test = generateString("Hallo", 6);
 
       String original = new String(test);
       System.out.println(original);
       System.out.println(MyString.lenght(test));
 		
 	}
 
     public static char[] generateString(String s, int capacity){
 		char[] result = new char[capacity];
 		for (int i=0; i<s.length(); ++i)
 		    result[i] = s.charAt(i);
		result[s.length()] = '\u0000';
 		return result;
 	}	
 	
 	
 }
