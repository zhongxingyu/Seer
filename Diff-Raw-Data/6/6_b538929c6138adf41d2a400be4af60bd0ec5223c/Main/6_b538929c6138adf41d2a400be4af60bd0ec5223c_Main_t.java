 package assignment4;
 
 import a4_scanner.Scanner;
 import console.Console;
 import bus.uigen.OEFrame;
 import bus.uigen.ObjectEditor;
 import util.misc.ThreadSupport;
 
 public class Main {
 	public static void main(String[] args) {
 		Scanner scanner = new Scanner("");
 		OEFrame editor = ObjectEditor.edit(scanner);
 		String[] animate = new String[5];
 		animate[0] = "MoVe 050 {saY \"hi1!\"}";
 		animate[1] = "REPEAT 5 {MOVE +10}";
 		animate[2] = "wait 02";
 		animate[3] = "RotateLeftArm +50";
 		animate[4] = "MoVe 050 {saY \"hi5!\"}";
 		for (int i = 0; i < animate.length; i++) {
			scanner.setString(animate[i]);
			editor.refresh();
			ThreadSupport.sleep(3000);
 		}
 	}
 }
