 package SRM147;
 
 public class CCipher {
 	
 	public static String decode(String cipherText, int shift) {
 		char[] tmp = cipherText.toCharArray();
 		for (int i=0 ; i<tmp.length ; i++) {
 			tmp[i] -= shift;
			if(!Character.isLetter(tmp[i])){
				tmp[i] += 26;
			}
 		}
 		return new String(tmp);
 	}
 
 	public static void main(String[] args) {
		System.out.println(decode("AAABBBCCC", 2));
 
 	}
 
 }
