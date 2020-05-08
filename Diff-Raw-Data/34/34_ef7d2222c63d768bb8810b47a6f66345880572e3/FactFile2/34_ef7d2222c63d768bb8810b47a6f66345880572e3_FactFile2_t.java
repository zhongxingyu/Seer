 package Task1;
 
import java.util.Date;

 import javax.swing.JOptionPane;
 
 public class FactFile2 {
 	public static void main(String argss[]){
		/*
		 * Basic input from user with Name, DOB and a list of subjects user is doing
		 */
 		String name = JOptionPane.showInputDialog("Enter your name please");
 		String DOB = JOptionPane.showInputDialog("Please input your DOB \n(dd/mm/yy)");
 		int age = getAge(DOB);
 		String subjects = JOptionPane.showInputDialog("Enter subjects currently doing seperateed by commas");
 		
 		JOptionPane.showMessageDialog(null, "Name: " + name + "\nDOB: " + DOB + "\nAge:" + age + "\nSubjects doing: " + subjects); 
 	}
	
	@SuppressWarnings("deprecation")
 	public static int getAge(String DOB){
		Date date = new Date();
		DOB = DOB.replace("/", "");
		int age = 0;
		int day = Integer.parseInt(DOB.substring(0, 2));
		int month = Integer.parseInt(DOB.substring(2,4));
		int year = Integer.parseInt(DOB.substring(4, 6));
		age = date.getYear() - year;
		
		if(month > date.getMonth() + 1){
			age--;
		}else if(month == date.getMonth() + 1){
			if(day > date.getDate()){
				age--;
			}else if(day == date.getDate()){ //Didn't actually think that months would start at 0 but dates start from 1??? :S
				System.out.println("Happy Birthday!");
			}
		}
		
		return age;
 	}
 }
