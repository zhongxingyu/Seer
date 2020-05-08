 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class RegularExplab3 {
 
 	// static String testStr="01010101";
 
 	static String testStr = "ac";
 
 	public static void main(String[] args) {
 		File file = new File(
				"/home/student/workspace/CompilerLab/src/expression.txt");
 		int ch;
 		String exp = null;
 		StringBuffer strContent = new StringBuffer("");
 		FileInputStream fin = null;
 
 		try {
 			FileInputStream fstream = new FileInputStream(
					"/home/student/workspace/CompilerLab/src/expression.txt");
 			String str = null;
 			int test = 1;
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			while ((str = br.readLine()) != null) {
 //				System.out.println("LINE NO: " + test + " ");
 //				System.out.println(str);
 				exp = str;
 				test++;
 
 				// Pattern matching
 				Pattern pattern = Pattern.compile(exp);
 				Matcher matcher = pattern.matcher(testStr);
 
 				System.out.println("expression " + exp);
 				if (matcher.matches()) {
 					System.out.println("The string " + testStr
 							+ " matches with the expression!!!");
 				} else {
 					System.out
 							.println("The string does not matche with the expression!!!");
 				}
 
 			}
 			in.close();
 
 		} catch (Exception e) {
 			System.err.println(e);
 		}
 		//	
 
 	}
 }
