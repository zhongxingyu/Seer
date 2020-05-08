 
 public class MyString {
 //    private char[] m_string;
 	
 //	MyString(){
 //		m_string[0] = '0';
 //	}
 //	
 //	MyString(char[] string){
 //		m_string = string;
 //	}
 	
	public static int totalLenght (char[] string){
 		int len = 0;
 		char buff;
 		try{
 		    for(;;){
 				buff = string[len];
 				++len;
 			}
 		} catch (ArrayIndexOutOfBoundsException e) {
 		} // error handling
 
 		return len;
 		
 	}
 	
 	public void show(){
 		
 		
 	}
 	
 	
 }
