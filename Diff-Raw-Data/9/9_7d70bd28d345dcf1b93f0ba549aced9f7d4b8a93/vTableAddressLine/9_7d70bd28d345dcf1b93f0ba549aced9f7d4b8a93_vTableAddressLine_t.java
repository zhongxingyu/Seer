 package oop;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.File;
 import java.io.FileWriter;
 
 public class vTableAddressLine{
 	//String vTableClass;
 	String methodname;
 	String typecast;
 	String classname;
 	String vTableLine;
 	//String parameters;
 	boolean overloaded;
 	
 	vTableClass parent;
 	vTableLayoutLine matchinglayout;
 	vTableMethodLayoutLine matchingmethod;
 	
 	public vTableAddressLine(vTableClass parentable){
 		parent = parentable;
 		overloaded = false;
 	}
 	
 	public void setMethodName(String methodnamable){
 		methodname = methodnamable;
 	}
 	
 	public void setTypeCast(String typecastable){
 		typecast = typecastable;
 	}
 	
 	public void setClassName(String classnamable){
 		classname = classnamable;
 	}
 	
 	public void setOverload(){
 		overloaded = true;
 		//parameters = parameterssource;
 	}
 	
 	public void setMatching(vTableLayoutLine layout, vTableMethodLayoutLine method){
 		matchinglayout = layout;
 		matchingmethod = method;
 	}
 	
 	/*
 	//obsolete
 	public void setVTableClass(String classable){
 		vTableClass = classable;
 	}
 	
 	public void createVTableLine(){
 		//methodname((anyspecialcasting)&class::methodname);
 
 		if(methodname.equals("__isa")){
 			vTableLine = methodname + "(";
 			vTableLine = vTableLine + "&" + "__" + classname + "::" + "__class()" + ")";
 		}
 		else{
 			vTableLine = ", \r" + methodname + "(";
 			if(!(typecast == null)){
 				vTableLine = vTableLine + "(" + typecast + ")"; 
 			}
 			vTableLine = vTableLine + "&" + classname + "::" + methodname + ")";
 		}
 	}
 	*/
 	public void writeFile(BufferedWriter writer){
 		if(methodname.equals("__isa")){
 			vTableLine = methodname + "(";
 			vTableLine = vTableLine + "__" + classname + "::" + "__class()" + ")";
 		}
 		else{
 			if(overloaded == true){
 				vTableLine = ", \r" + methodname + "_" + matchingmethod.parameters.replace(",", "_") + "(";
 			}
 			else{
 				vTableLine = ", \r" + methodname + "(";
 			}
 			
 			if(!(typecast == null)){
				vTableLine = vTableLine + "" + typecast + ""; 
 			}
 			
 			if(overloaded == true){
 				vTableLine = vTableLine + "&__" + classname + "::" + methodname + "_" 
 				+ matchingmethod.parameters.replace(",", "_") + ")";
 			}
 			else{
 				vTableLine = vTableLine + "&__" + classname + "::" + methodname + ")";
 			}
 			//vTableLine = vTableLine + "&__" + classname + "::" + methodname + ")";
 		}
 		
 		try {
 			//FileWriter writee = new FileWriter(file);
 			//BufferedWriter writer = new BufferedWriter(writee);
 			writer.write(vTableLine);
 			writer.flush();
 			//writer.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void printLine(){
 		if(methodname.equals("__isa")){
 			vTableLine = methodname + "(";
 			vTableLine = vTableLine + "__" + classname + "::" + "__class()" + ")";
 		}
 		else{
 			if(overloaded == true){
 				vTableLine = ", \r" + methodname + "_" + matchingmethod.parameters.replace(",", "_") + "(";
 			}
 			else{
 				vTableLine = ", \r" + methodname + "(";
 			}
 			
 			if(!(typecast == null)){
 				vTableLine = vTableLine + "(" + typecast + ")"; 
 			}
 			
 			if(overloaded == true){
 				vTableLine = vTableLine + "&__" + classname + "::" + methodname + "_" 
 				+ matchingmethod.parameters.replace(",", "_") + ")";
 			}
 			else{
 				vTableLine = vTableLine + "&__" + classname + "::" + methodname + ")";
 			}
 			//vTableLine = vTableLine + "&__" + classname + "::" + methodname + ")";
 		}
 		
 		System.out.print(vTableLine);
 	}
 }
