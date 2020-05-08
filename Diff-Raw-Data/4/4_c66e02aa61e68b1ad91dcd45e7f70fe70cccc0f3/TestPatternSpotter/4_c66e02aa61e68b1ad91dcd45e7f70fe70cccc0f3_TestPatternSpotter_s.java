 // TestPatternSpotter.java
 // Jeremy Singer
 // 10 Nov 08
 
 package uk.ac.glasgow.jsinger.nanopatterns;
 
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassAdapter;
 import org.objectweb.asm.tree.ClassNode;
 import org.objectweb.asm.tree.MethodNode;
 import org.objectweb.asm.tree.AbstractInsnNode;
 import org.objectweb.asm.commons.EmptyVisitor;
 
 import java.util.List;
 import java.io.FileInputStream;
 
 public class TestPatternSpotter {
 
     public static void main(String [] args) {
 
 	
 	try {
 	    if (args.length != 1) {
 		System.err.println("usage: java TestPatternSpotter classname\n (or) java TestPatternSpotter -help\n (or) java -jar np.jar classname|-help\n");
 		System.exit(-1);
 	    }
 
 	    if (args[0].equals("-help")) {
 	      System.out.println(
 		"This program (either TestPatternSpotter class or \nnp.jar Java archive) detects fundamental nano-patterns in bytecode \nclass files. See the paper at \n  http://www.dcs.gla.ac.uk/~jsinger/pdfs/nanopatterns.pdf \nfor more details about nano-patterns."
 		);
 	      System.exit(0);
 	    }
 
 	    String className = args[0];
 	    FileInputStream f = new FileInputStream(className);
 	    ClassReader cr = new ClassReader(f);
 	    ClassNode cn = new ClassNode();
 	    cr.accept(cn, ClassReader.SKIP_DEBUG);
 
 	    System.out.println("class method typesig numInstrs noparams void recursive samename leaf objCreator thisInstanceFieldReader thisInstanceFieldWriter otherInstanceFieldReader otherInstanceFieldWriter staticFieldReader staticFieldWriter typeManipulator straightLine looper switcher exceptions localReader localWriter arrCreator arrReader arrWriter polymorphic singleReturner multipleReturner client jdkClient tailCaller");
 	    
 	    List methods = cn.methods;
 	    for (int i = 0; i < methods.size(); ++i) {
 		MethodNode method = (MethodNode) methods.get(i);
 		RecursivePatternSpotter rps =
 		    new RecursivePatternSpotter(new EmptyVisitor(),
 						cn.name,
 						method.name,
 						method.desc);
 		OOAccessPatternSpotter ops = 
 		    new OOAccessPatternSpotter(new EmptyVisitor());
 		TypeManipulatorPatternSpotter tps = 
 		    new TypeManipulatorPatternSpotter(new EmptyVisitor());
 		ControlFlowPatternSpotter cps = 
 		    new ControlFlowPatternSpotter(new EmptyVisitor());
 		ArrayAccessPatternSpotter aps = 
 		    new ArrayAccessPatternSpotter(new EmptyVisitor());
 		PolymorphicPatternSpotter pps = 
 		    new PolymorphicPatternSpotter(new EmptyVisitor());
 		ReturnPatternSpotter retps =
 		    new ReturnPatternSpotter(new EmptyVisitor());
 		MethodPatternSpotter mps = 
 		    new MethodPatternSpotter(new EmptyVisitor());
 		// check following
 		// properties directly from
 		// method descriptor
 		boolean noParams = false;
 		boolean noReturn = false;
 		boolean throwsExceptions = false;
 		if (method.desc.startsWith("()")) {
 		    noParams = true;
 		}
 		if (method.desc.endsWith(")V")) {
 		    noReturn = true;
 		}
 		if (method.exceptions.size() > 0) {
 		    throwsExceptions = true;
 		}
 		if (method.instructions.size() > 0) {
  		    for (int j = 0; j < method.instructions.size(); ++j) {
 			Object insn = method.instructions.get(j);
 			((AbstractInsnNode) insn).accept(rps);
 			((AbstractInsnNode) insn).accept(ops);
 			((AbstractInsnNode) insn).accept(tps);
 			((AbstractInsnNode) insn).accept(cps);
 			((AbstractInsnNode) insn).accept(aps);
 			((AbstractInsnNode) insn).accept(mps);
			
			
 		    }
 		    int numInstrs = method.instructions.size();
 		    System.out.print("" +
 				     cn.name + " " + 
 				     method.name + " " +
 				     method.desc + " " +
 				     numInstrs);
 		    
 		    printBooleanValue(noParams);
 		    printBooleanValue(noReturn);
 		    printBooleanValue(rps.isRecursive());
 		    printBooleanValue(rps.isSameNameCaller());
 		    printBooleanValue(rps.isLeaf());
 		    printBooleanValue(ops.isObjectCreator());
 		    printBooleanValue(ops.isThisInstanceFieldReader());
 		    printBooleanValue(ops.isThisInstanceFieldWriter());
 		    printBooleanValue(ops.isOtherInstanceFieldReader());
 		    printBooleanValue(ops.isOtherInstanceFieldWriter());
 		    printBooleanValue(ops.isStaticFieldReader());
 		    printBooleanValue(ops.isStaticFieldWriter());
 		    printBooleanValue(tps.isTypeManipulator());
 		    printBooleanValue(cps.isStraightLineCode());
 		    printBooleanValue(cps.isLoopingCode());
 		    printBooleanValue(cps.isSwitcher());
 		    printBooleanValue(throwsExceptions);
 		    printBooleanValue(aps.isLocalVarReader());
 		    printBooleanValue(aps.isLocalVarWriter());
 		    printBooleanValue(aps.isArrayCreator());
 		    printBooleanValue(aps.isArrayReader());
 		    printBooleanValue(aps.isArrayWriter());
 		    printBooleanValue(pps.isPolymorphic());
 		    printBooleanValue(retps.isSingleReturner());
 		    printBooleanValue(retps.isMultipleReturner());
 		    printBooleanValue(mps.isClient());
 		    printBooleanValue(mps.isJdkClient());
 		    printBooleanValue(mps.isTailCaller());
 		    System.out.println("");
 		}
 	    }
 	    
 	}
 	catch (Exception e) {
 	    System.err.println(e);
 	}
     }
 
     /**
      * trivial support for MP-tool style reporting
      * of exhibited nanopatterns
      */
     public static void printBooleanValue(boolean value) {
 	if (value) {
 	    System.out.print(" 1");
 	}
 	else {
 	    System.out.print(" 0");
 	}
     }
     
 }
