 package Task1;
 
 import javax.swing.JOptionPane;
 
 public class FactFile2 {
 	public static void main(String argss[]){
 		String name = JOptionPane.showInputDialog("Enter your name please");
 		String DOB = JOptionPane.showInputDialog("Please input your DOB \n(dd/mm/yy)");
 		int age = getAge(DOB);
 		String subjects = JOptionPane.showInputDialog("Enter subjects currently doing seperateed by commas");
 		
 		JOptionPane.showMessageDialog(null, "Name: " + name + "\nDOB: " + DOB + "\nAge:" + age + "\nSubjects doing: " + subjects); 
 	}
 	public static int getAge(String DOB){
		DOB.replace("/", "");
		int dob = Integer.parseInt(DOB);
		int day = DOB.;
		System.out.println(day);
		return dob;
 	}
 }
