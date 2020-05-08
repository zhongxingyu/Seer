 package oop;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class vClassConstructor {
 	String parameters;
 	String visibility;
 	
 	vTableClass parent;
 	
 	public vClassConstructor(vTableClass parentable){
 		parameters = null;
 		parent = parentable;
 		visibility = "public";
 	}
 	
 	public void addParameter(String param){
 		if (parameters == null){
 			parameters = param;
 		}
 		else{
 			parameters = parameters + ", " + param;
 		}
 	}
 	
 	public void setVisibility(String visible){
 		visibility = visible;
 	}
 	
 	public void writeFile(BufferedWriter writer){
 		try {
 			//FileWriter writee = new FileWriter(file);
 			//BufferedWriter writer = new BufferedWriter(writee);
			writer.write("__" + parent.classname + "(");
 			if (parameters == null){
 				writer.write(");\r");
 			}
 			else{
 				writer.write(parameters + ");\r");
 			}
 			writer.flush();
 			//writer.closhe();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 	
 	public void printLine(){
 		System.out.print("__" + parent.classname + "(");
 		if (parameters == null){
 			System.out.print(");\r");
 		}
 		else{
 			System.out.print(parameters + ");\r");
 		}
 	}
 }
