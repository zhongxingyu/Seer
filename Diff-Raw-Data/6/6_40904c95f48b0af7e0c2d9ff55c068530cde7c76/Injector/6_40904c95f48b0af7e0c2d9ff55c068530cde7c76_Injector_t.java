 // Copyright © 2012 Steve McCoy under the MIT license.
 package edu.unh.cs.tact;
 
 import java.util.*;
 import org.apache.bcel.*;
 import org.apache.bcel.classfile.*;
 import org.apache.bcel.generic.*;
 
 import static edu.unh.cs.tact.Util.*;
 
 class Injector{
 	public final ConstantPoolGen cp;
 	public final InstructionFactory f;
 	public final MethodGen mg;
 	public final InstructionList list;
 	public final InstructionHandle begin, end;
 
 	public Injector(ConstantPoolGen cp, InstructionFactory f, MethodGen mg){
 		this.f = notNull(f, "f");
 		this.mg = notNull(mg, "mg");
 		this.list = mg.getInstructionList();
 		this.begin = list.getStart();
 		this.end = list.getEnd();
 		this.cp = notNull(cp, "cp");
 	}
 
 	public boolean insertChecks(){
 		boolean changed = false;
 		InstructionHandle end = this.end.getNext();
 		for(InstructionHandle h = begin; h != end; h = h.getNext()){
 			CheckInserter ins = getInserter(h);
 			if(ins == null)
 				continue;
 
 			String chk = "check";
 			String guard = ins.guardName();
 			if(guard != null){
 				// TODO: guards other than this
 				chk = "guardByThis";
 			}
 
 			ins.insertCheck(chk);
 			changed = true;
 		}
 
 		if(changed)
 			mg.setMaxStack();
 
 		return changed;
 	}
 
 	private boolean isArrayStore(Instruction h){
 		return h instanceof AASTORE
 			|| h instanceof BASTORE
 			|| h instanceof CASTORE
 			|| h instanceof DASTORE
 			|| h instanceof FASTORE
 			|| h instanceof IASTORE
 			|| h instanceof LASTORE
 			|| h instanceof SASTORE
 			;
 	}
 
 	private void insertCheckCall(InstructionHandle h, String chk){
 		assert h != null;
 		assert chk != null;
 		list.insert(
 			h,
 			f.createInvoke(
 				"edu.unh.cs.tact.Checker",
 				chk,
 				Type.VOID,
 				new Type[]{ Type.OBJECT },
 				Constants.INVOKESTATIC
 			)
 		);
 	}
 
 	private boolean isReadOnly(FieldInstruction pf){
 		JavaClass jc = classFor(pf);
 		if(jc == null)
 			return false;
 		Field f = fieldFor(jc, pf);
 		if(f == null)
 			return false;
 
 		for(AnnotationEntry ae : f.getAnnotationEntries()){
 			if(ae.getAnnotationType().equals("Ledu/unh/cs/tact/ReadOnly;"))
 				return true;
 		}
 		return false;
 	}
 
 	private JavaClass classFor(FieldInstruction fi){
 		ReferenceType rt = fi.getReferenceType(cp);
 		if(!(rt instanceof ObjectType))
 			return null;
 
 		ObjectType ot = (ObjectType)rt;
 		try{
 			return Repository.lookupClass(ot.getClassName());
 		}catch(ClassNotFoundException e){
 			throw new RuntimeException(e);
 		}
 	}
 
 	private Field fieldFor(JavaClass jc, FieldInstruction fi){
 		for(Field f : jc.getFields()){
 			if(f.getName().equals(fi.getFieldName(cp)))
 				return f;
 		}
 		return null;
 	}
 
 	private boolean isReadOnly(ArrayInstruction pa){
 		return false;
 	}
 
 	private interface CheckInserter{
 		void insertCheck(String chk);
 		String guardName();
 	}
 
 	CheckInserter getInserter(InstructionHandle h){
 		Instruction code = h.getInstruction();
 		if(code instanceof PUTFIELD){
 			FieldInstruction fi = (FieldInstruction)code;
 			if(fi.getFieldName(cp).equals("this$0"))
 				return null; // skip inner class's outer reference
 			return new RefCheckInserter(fi, h);
 		}else if(code instanceof PUTSTATIC){
 			return new StaticRefCheckInserter((PUTSTATIC)code, h);
 		}else if(isArrayStore(code)){
 			return new ArrayCheckInserter((ArrayInstruction)code, h);
 		}else if(code instanceof INVOKESPECIAL
 			&& ((INVOKESPECIAL)code).getMethodName(cp).equals("<init>")
 			&& isForNew(h)){ // ignore super's ctors
 			return new ConstructCheckInserter(h);
 		}
 		return null;
 	}
 
 	private boolean isForNew(InstructionHandle h){
 		int stk = -1;
 		while(h != null){
 			Instruction code = h.getInstruction();
 
 			if(code instanceof DUP)
 				stk++; // BCEL bug: dup does not push two words.
 			else if(code instanceof StackProducer)
 				stk += ((StackProducer)code).produceStack(cp);
 			if(code instanceof StackConsumer)
 				stk -= ((StackConsumer)code).consumeStack(cp);
 
 			if(stk == 0)
 				return code instanceof NEW;
 
 			h = h.getPrev();
 		}
 
 		return false;
 	}
 	
 	private class RefCheckInserter implements CheckInserter{
 		FieldInstruction pf;
 		InstructionHandle h;
 		RefCheckInserter(FieldInstruction pf, InstructionHandle h){
 			this.pf = pf;
 			this.h = h;
 		}
 
 		public void insertCheck(String chk){
			if(mg.getName().equals("<init>") && chk.equals("guardByThis"))
				return;

 			switch(pf.getType(cp).getSize()){
 			case 1:
 				insertCheck32(chk);
 				break;
 			case 2:
 				insertCheck64(chk);
 				break;
 			default:
 				assert false : "A different size of field???";
 			}
 		}
 
 		public void insertCheck32(String chk){
 			list.insert(h, new SWAP());
 			list.insert(h, f.createDup(1));
 			insertCheckCall(h, chk);
 			list.insert(h, new SWAP());
 		}
 
 		public void insertCheck64(String chk){
 			list.insert(h, new DUP2_X1());
 			list.insert(h, new POP2());
 			list.insert(h, new DUP_X2());
 			insertCheckCall(h, chk);
 		}
 
 		public String guardName(){
 			JavaClass jc = classFor(pf);
 			if(jc == null)
 				return null;
 	
 			Field f = fieldFor(jc, pf);
 			if(f == null)
 				return null;
 	
 			for(AnnotationEntry ae : f.getAnnotationEntries()){
 				if(!ae.getAnnotationType().equals("Ledu/unh/cs/tact/GuardedBy;"))
 					continue;
 	
 				for(ElementValuePair ev : ae.getElementValuePairs())
 					if(ev.getNameString().equals("value"))
 						return ev.getValue().stringifyValue();
 			}
 			return null;
 		}
 	}
 
 	private class StaticRefCheckInserter extends RefCheckInserter{
 		PUTSTATIC ps;
 		StaticRefCheckInserter(PUTSTATIC ps, InstructionHandle h){
 			super(ps, h);
 			this.ps = ps;
 		}
 
 		@Override public void insertCheck32(String chk){
 			int i = ps.getIndex();
 			Constant c = cp.getConstant(i);
 			if(!(c instanceof ConstantFieldref))
 				throw new AssertionError("Flawed static field check");
 
 			ConstantFieldref cfr = (ConstantFieldref)c;
 			list.insert(h, new LDC_W(cfr.getClassIndex()));
 			insertCheckCall(h, chk);
 		}
 
 		@Override public void insertCheck64(String chk){
 			// Field's size doesn't matter; we just get the class ref.
 			insertCheck32(chk);
 		}
 	}
 
 	private class ArrayCheckInserter implements CheckInserter{
 		ArrayInstruction pa;
 		InstructionHandle h;
 		ArrayCheckInserter(ArrayInstruction pa, InstructionHandle h){
 			this.pa = pa;
 			this.h = h;
 		}
 
 		public void insertCheck(String chk){
 			switch(pa.getType(cp).getSize()){
 			case 1:
 				insertCheck32(chk);
 				break;
 			case 2:
 				insertCheck64(chk);
 				break;
 			default:
 				assert false : "A different size of field???";
 			}
 		}
 
 		public void insertCheck32(String chk){
 			list.insert(h, new DUP2_X1());
 			list.insert(h, new POP2());
 			list.insert(h, new DUP_X2());
 			insertCheckCall(h, chk);
 		}
 	
 		public void insertCheck64(String chk){
 			list.insert(h, new DUP2_X2());
 			list.insert(h, new POP2());
 			list.insert(h, new DUP2());
 			list.insert(h, new POP());
 			insertCheckCall(h, chk);
 			list.insert(h, new DUP2_X2());
 			list.insert(h, new POP2());
 		}
 
 		public String guardName(){
 			return null;
 		}
 	}
 
 	private class ConstructCheckInserter implements CheckInserter{
 		InstructionHandle h;
 		ConstructCheckInserter(InstructionHandle h){
 			this.h = h.getNext(); // ref should be on top after call to <init>
 			assert this.h != null;
 		}
 
 		public void insertCheck(String chk){
 			list.insert(h, new DUP());
 			insertCheckCall(h, chk);
 		}
 
 		public String guardName(){
 			return null;
 		}
 	}
 }
