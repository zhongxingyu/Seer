 package Common;
 public class Encryptor {
 	
 	public static String encrypt(String input, int times){ //encrypting multiple times
 		String ret = input;
 		for (int i = 0; i<times; i++){
 			ret = encrypt(ret);
 		}
 		return ret;
 	}
 	
 	public static String decrypt(String input, int times){ //decrypting multiple times
 		String ret = input;
 		for (int i = 0; i<times; i++){
 			ret = decrypt(ret);
 		}
 		return ret;
 	}
 
 	public static String encrypt(String input){
	    if (input==null) return null;
 		String even = "";
         String odd = "";
 
         for (int x=0; x < input.length(); x+=2)
         {
             even += (char)(input.charAt (x)+1);
         }
 
         for (int x=1; x < input.length(); x+=2)
         {
             odd += (char)(input.charAt (x)+1);
         }
 
         return even + odd;
 	}
 	
 	public static String decrypt(String input){
	    if (input==null) return null;
 		String end = "";
 
         if (input.length()%2 == 0)
         {
             for (int x = 0; x<input.length()/2; x++)
             {
 
                 end +=(char)(input.charAt(x)-1);
                 x+= (input.length()/2);
                 end+=(char)(input.charAt(x)-1);
                 x-= (input.length()/2);
             }
             return end;
         }
 
         for (int x = 0; x<input.length()/2; x++)
         {
             end+=(char)(input.charAt(x)-1);
             end+=(char)(input.charAt(input.length()/2+x+1)-1);
 
         }
 
         end += (char)(input.charAt(input.length()/2)-1);
 
         return end;
 	}
 	
 }
