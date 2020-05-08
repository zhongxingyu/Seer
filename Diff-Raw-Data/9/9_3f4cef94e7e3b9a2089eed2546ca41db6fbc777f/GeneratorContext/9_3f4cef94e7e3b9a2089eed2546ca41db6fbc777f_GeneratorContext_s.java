 package codeGenerator;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import compiler.*;
 
 public class GeneratorContext {
 	public StringBuilder code;
 	public ArrayList<Variable> globalVariableList;
 	public HashMap<String,Type> globalTypeMap;
 	public HashMap<String,Function> globalFunctionMap;
 	public HashMap<String,Procedure> globalProcedureMap;
 	public Procedure currentProcedureOrFunction;
 	
 	public LabelManager labelManager;
 	
 	public RegisterManager registerManager;
 	
 	public boolean generate=false;
 	
 	public boolean haveWriteTextSegment=false;
 	private void Reset() 
 	{
 		code=new StringBuilder();
 		globalVariableList.clear();
 		globalTypeMap.clear();
 		globalFunctionMap.clear();
 		globalProcedureMap.clear();
 		currentProcedureOrFunction=null;
 		haveWriteTextSegment=false;
 		registerManager.clear();
 		labelManager.clear();
 		
 		globalTypeMap.put("integer", new IntegerType());
 		globalTypeMap.put("char", new CharType());
 		globalTypeMap.put("real", new RealType());
 	}
 	public GeneratorContext() 
 	{
 		code=new StringBuilder();
 		globalVariableList=new ArrayList<Variable>();
 		globalTypeMap=new HashMap<String,Type>();
 		globalFunctionMap=new HashMap<String,Function>();
 		globalProcedureMap=new HashMap<String,Procedure>();
 		registerManager = new RegisterManager();
 		labelManager = new LabelManager();
 		
 		Reset();
 	}
 	public String globalVariableListToString() {
 		String tmp="";
 		for (int i=0;i<globalVariableList.size();++i) {
 			Variable t=globalVariableList.get(i);
 			tmp+=String.format("\n  %s ",t.toString());
 		}
 		return tmp;
 	}
 	public Label getIOBuf() {
 		return new Label("io_buf");
 	}
 	public boolean addGlobalVariable(Variable v) {
 		for (int i=0;i<globalVariableList.size();i++)
 			if (globalVariableList.get(i).name.equals(v.name))
 				return false;
 		globalVariableList.add(v);
 		return true;
 	}
 	public Variable getComponent(String t) {
 		if (currentProcedureOrFunction!=null) {
 			for (int i=0;i<currentProcedureOrFunction.localvariable_list.size();++i) {
 				if (currentProcedureOrFunction.localvariable_list.get(i).name.equals(t))
 					return currentProcedureOrFunction.localvariable_list.get(i);
 			}
 			for (int i=0;i<currentProcedureOrFunction.parameter_list.size();++i) {
 				if (currentProcedureOrFunction.parameter_list.get(i).name.equals(t))
 					return currentProcedureOrFunction.parameter_list.get(i);
 			}
 		}
 		for (int i=0;i<globalVariableList.size();++i) {
 			if (globalVariableList.get(i).name.equals(t))
 				return globalVariableList.get(i);
 		}
 		return null;
 	}
 	public Register moveVariablePointerToReg(String t) throws GenerateException { //return reg,ensure t exists
		System.out.println(String.format("VariableName=%s\n",t));
 		Register dstRegister=registerManager.getFreeRegister();
 		if (currentProcedureOrFunction!=null) {
 			for (int i=0;i<currentProcedureOrFunction.localvariable_list.size();++i) {
 				if (currentProcedureOrFunction.localvariable_list.get(i).name.equals(t)) {
 					code.append(String.format("lea %s,[rbp-%d]\n",dstRegister.toString(),currentProcedureOrFunction.localvariable_list.get(i).offset*8));
 					return dstRegister;
 				}
 			}
 			for (int i=0;i<currentProcedureOrFunction.parameter_list.size();++i) {
 				if (currentProcedureOrFunction.parameter_list.get(i).name.equals(t)) {
 					if (currentProcedureOrFunction.parameter_list.get(i).isVar) {
 						code.append(String.format("lea %s,[rbp+%d]\n",dstRegister.toString(),currentProcedureOrFunction.parameter_list.get(i).offset*8));
 						code.append(String.format("mov %s,[%s]\n", dstRegister,dstRegister));
 					}else {
 						code.append(String.format("lea %s,[rbp+%d]\n",dstRegister.toString(),currentProcedureOrFunction.parameter_list.get(i).offset*8));
 					}
 					return dstRegister;
 				}
 			}
 		}
 		for (int i=0;i<globalVariableList.size();++i) {
 			if (globalVariableList.get(i).name.equals(t)) {
 				code.append(String.format("mov %s,%s\n",dstRegister.toString(),t));
 				return dstRegister;
 			}
 		}
 		throw new GenerateException("Ask for some undefined variable!\n");
 	}
 }
