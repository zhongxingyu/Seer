 // Copyright © 2012 Steve McCoy under the MIT license.
 package edu.unh.cs.tact;
 
 import java.io.*;
 import java.util.*;
 import org.apache.bcel.*;
 import org.apache.bcel.classfile.*;
 import org.apache.bcel.generic.*;
 import org.apache.bcel.verifier.*;
 
 class Main{
 	static boolean loud = false;
 
 	public static void main(String[] args) throws Exception{
 		for(String arg : args){
 			if(arg.equals("-break")){
 				BreakMe.codeToBreak();
 				return;
 			}else if(arg.equals("-work")){
 				BreakMe.codeThatWorks();
 				return;
 			}else if(arg.equals("-pi")){
 				BreakMe.pi();
 				return;
 			}else if(arg.equals("-loud")){
 				loud = true;
 				continue;
 			}
 			inject(arg);
 		}
 	}
 
 	private static void inject(String fname) throws Exception{
 		InputStream file = null;
 		try{
 			file = new BufferedInputStream(new FileInputStream(fname));
 		}catch(FileNotFoundException e){
 			System.err.printf("I failed to open \"%s\": %s\n",
 				fname, e.getLocalizedMessage());
 			System.exit(1);
 		}
 
 		JavaClass jc = new ClassParser(file, fname).parse();
 		Method[] methods = jc.getMethods();
 		ConstantPoolGen cp = new ConstantPoolGen(jc.getConstantPool());
 		InstructionFactory insf = new InstructionFactory(cp);
 
 		for(int i = 0; i < methods.length; i++){
 			if(methods[i].isNative())
 				continue;
 
 			MethodGen mg = new MethodGen(methods[i], jc.getClassName(), cp);
 			boolean changed = new BasicBlock(cp, insf, mg.getInstructionList()).insertChecks();
 			methods[i] = mg.getMethod();
 
 			if(loud && changed)
 				System.out.println(methods[i].getCode());
 		}
 
 		jc.setConstantPool(cp.getFinalConstantPool());
 		jc.dump(fname);
 	}
 }
