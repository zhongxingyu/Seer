 /**
  *  Andromeda, a galaxy extension language.
  *  Copyright (C) 2010 J. 'gex' Finis  (gekko_tgh@gmx.de, sc2mod.com)
  * 
  *  Because of possible Plagiarism, Andromeda is not yet
  *	Open Source. You are not allowed to redistribute the sources
  *	in any form without my permission.
  *  
  */
 package com.sc2mod.andromeda.codetransform;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.Map.Entry;
 
 import com.sc2mod.andromeda.environment.AbstractFunction;
 import com.sc2mod.andromeda.environment.Environment;
 import com.sc2mod.andromeda.environment.Function;
 import com.sc2mod.andromeda.environment.Method;
 import com.sc2mod.andromeda.environment.Signature;
 import com.sc2mod.andromeda.environment.types.RecordType;
 import com.sc2mod.andromeda.environment.variables.FieldDecl;
 import com.sc2mod.andromeda.environment.variables.FieldSet;
 import com.sc2mod.andromeda.environment.variables.GlobalVarDecl;
 import com.sc2mod.andromeda.environment.variables.LocalVarDecl;
 import com.sc2mod.andromeda.environment.variables.VarDecl;
 import com.sc2mod.andromeda.notifications.CompilationError;
 import com.sc2mod.andromeda.parsing.AndromedaFileInfo;
 import com.sc2mod.andromeda.program.Log;
 import com.sc2mod.andromeda.program.Options;
 import com.sc2mod.andromeda.program.Program;
 import com.sc2mod.andromeda.syntaxNodes.Type;
 import com.sc2mod.andromeda.syntaxNodes.TypeCategory;
 
 /**
  * Class for finding unused content
  * @author J. 'gex' Finis
  *
  */
 //XPilot: no longer remove variables that are not written to
 public class UnusedFinder {
 
 	
 	public static void process(Options options, Environment env){
 		
 		checkUnusedGlobals(options,env);
 		checkUnusedFields(options,env);
 		checkUncalledFunctions(options,env);
 	}
 	
 
 
 	private static void handleUnusedFunction(AbstractFunction vd){
 		switch(handleUnusedFunctions){
 		case Options.EXCEPTION_ERROR:
 			throw new CompilationError(vd.getDefinition(), "The " + vd.getDescription() + " is never called");
 		case Options.EXCEPTION_IGNORE:
 			break;
 		case Options.EXCEPTION_REMOVE:
 			vd.setCreateCode(false);
 			break;
 		case Options.EXCEPTION_WARNING:
 			Program.log.warning(vd.getDefinition(), "The " + vd.getDescription() + " is never called");
 			break;
 		}
 	}
 
 	private static void checkFunction(AbstractFunction f, int inclusionType){
 		//Strcall functions are never uncalled
 		if(f.isStrcall()) return;
 		//methods that are called virtually should not be omitted
 		if(f.getInvocationCount()==0 /* && !(f instanceof Method && f.isCalledVirtually()) */) {
 			boolean isLib;
 			
 			switch(inclusionType){
 			case AndromedaFileInfo.TYPE_LANGUAGE:
 			case AndromedaFileInfo.TYPE_NATIVE:
 				return;
 			case AndromedaFileInfo.TYPE_LIBRARY:
 				isLib = true;
 				break;
 			default:
 				isLib = false;
 			}
 			if(isLib){
 				//System.out.println(f.getDescription());
 				f.setCreateCode(false);
 			} else {
 				handleUnusedFunction(f);
 			}
 		}
 	}
 	private static int handleUnusedFunctions;
 	private static void checkUncalledFunctions(Options options, Environment env) {
 		//Functions
 		handleUnusedFunctions = options.handleUncalledFunctions;		
 		for(Entry<String, LinkedHashMap<Signature, LinkedList<Function>>> e : env.getFunctions().getFunctionTable().entrySet()){
 			for(Entry<Signature, LinkedList<Function>> e2: e.getValue().entrySet()){
 				for(Function f: e2.getValue()){
 					checkFunction(f,f.getScope().getInclusionType());
 				}
 			}
 		}
 		
 		//Methods
 		handleUnusedFunctions = options.handleUncalledMethods;
 		for(RecordType r: env.typeProvider.getRecordTypes()){
 			int inclusionType = r.getScope().getInclusionType();
 			if(inclusionType==AndromedaFileInfo.TYPE_NATIVE) continue;
 			LinkedHashMap<String, LinkedHashMap<Signature, AbstractFunction>> methods = r.getMethods().getMethodTable();
 			for(Entry<String, LinkedHashMap<Signature, AbstractFunction>> meths: methods.entrySet()){
 				for(Entry<Signature, AbstractFunction> keyvalue: meths.getValue().entrySet()){
 					checkFunction(keyvalue.getValue(),inclusionType);
 				}
 			}
 		}
 	}
 
 	private static void handleUnusedVar(int handlingStrategy, VarDecl vd, String varType, String accessType, boolean mayRemove){
 		switch(handlingStrategy){
 		case Options.EXCEPTION_ERROR:
 			throw new CompilationError(vd.getDefinition(), "The " + varType + " " + vd.getUid() + " is never " + accessType);
 		case Options.EXCEPTION_IGNORE:
 			break;
 		case Options.EXCEPTION_REMOVE:
 			if(mayRemove){
 				vd.setCreateCode(false);
 				break;
 			}
 			Program.log.warning(vd.getDefinition(), "Removing unused " + varType + "s is not possible yet. Variable NOT removed.");
 		case Options.EXCEPTION_WARNING:
 			Program.log.warning(vd.getDefinition(), "The " + varType + " " + vd.getUid() + " is never " + accessType);
 			break;
 		}
 	}
 	
 	static void checkForUnusedLocals(Function f, Options options){
 		//Check for unused locals
 		LocalVarDecl[] locals = f.getLocals();
 		if(locals == null) return;
 		for(LocalVarDecl vd : locals){
 			if(vd.getNumReadAccesses()==0){
 				handleUnusedVar(options.handleUnreadLocals,vd,"local variable","read",false);
 			}
 			else if(vd.getNumWriteAccesses()==0){
 				handleUnusedVar(options.handleUnwrittenLocals,vd,"local variable","written",false);
 			}
 		}
 	}
 	
 	private static void checkUnusedGlobals(Options options, Environment env){
 		int handleUnused = options.handleUnusedGlobals;
 		LinkedHashMap<String, ArrayList<GlobalVarDecl>> variables = env.getGlobalVariables().getVarSet();
 		 
 		for(Entry<String, ArrayList<GlobalVarDecl>> e: variables.entrySet()){
 			for(GlobalVarDecl decl: e.getValue()){
 				
 				int inclusionType = decl.getScope().getInclusionType();
 				boolean isLib;
 				switch(inclusionType){
 				case AndromedaFileInfo.TYPE_LANGUAGE:
 				case AndromedaFileInfo.TYPE_NATIVE:
 					continue;
 				case AndromedaFileInfo.TYPE_LIBRARY:
 					isLib = true;
 					break;
 				default:
 					isLib = false;
 				}
 
 				//System.out.println(decl.getGeneratedName());
 				
 				//XPilot: don't remove a variable if it is written to
 				if(decl.getNumReadAccesses()==0 && decl.getNumReadAccesses() == 0){
 					if(isLib||decl.getNumInlines()>0){
 						decl.setCreateCode(false);
 					} else {
 						handleUnusedVar(handleUnused,decl,"global variable","read",true);
 					}
 				} else if(decl.getNumWriteAccesses()==0&&decl.getType().getCategory()!=com.sc2mod.andromeda.environment.types.Type.ARRAY){
 					throw new CompilationError(decl.getDefinition(), "The global variable " + decl.getUid() + " is read but never initialized");
 				}
 				
 			}
 		}
 	}
 	
 	private static void checkUnusedFields(Options options, Environment env) {
 
 		//Fields
 		for(RecordType r: env.typeProvider.getRecordTypes()){
 			int inclusionType = r.getScope().getInclusionType();
 			boolean isLib;
 			switch(inclusionType){
 			case AndromedaFileInfo.TYPE_LANGUAGE:
 			case AndromedaFileInfo.TYPE_NATIVE:
 				continue;
 			case AndromedaFileInfo.TYPE_LIBRARY:
 				isLib = true;
 				break;
 			default:
 				isLib = false;
 			}
 			
 			int handleUnused = options.handleUnusedStaticFields;
 			FieldSet fi = r.getFields();
 			ArrayList<FieldDecl> fields = fi.getStaticClassFields();
 			for(FieldDecl decl: fields){
 				//XPilot: don't remove a variable if it is written to
 				if(decl.getNumReadAccesses()==0 && decl.getNumWriteAccesses() == 0){
 					if(isLib|| decl.getNumInlines()>0){
 						decl.setCreateCode(false);
 					} else {
 						handleUnusedVar(handleUnused,decl,"static field","read",true);
 					}
 				} else if(decl.getNumWriteAccesses()==0){
 					throw new CompilationError(decl.getDefinition(), "The static field " + decl.getUid() + " is read but never initialized.");
 					//System.out.println("The static field " + decl.getUid() + " is read but never initialized.");
 				}
 			}
 			
 
 			handleUnused = options.handleUnusedFields;
 			fields = fi.getNonStaticClassFields();
 			for(FieldDecl decl: fields){
 				//XPilot: don't remove a variable if it is written to
 				if(decl.getNumReadAccesses()==0 && decl.getNumWriteAccesses() == 0){
 					if(isLib){
 						//Fields cannot be removed, so do nothing in a lib
 					} else {
 						handleUnusedVar(handleUnused,decl,"field","read",false);
 					}
 				} else if(decl.getNumWriteAccesses()==0){
 					throw new CompilationError(decl.getDefinition(), "The field " + decl.getUid() + " is read but never initialized.");
 					//System.out.println("The field " + decl.getUid() + " is read but never initialized.");
 				}
 			}
 		}
 	}
 }
