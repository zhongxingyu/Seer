 package org.jackie.jclassfile.attribute.std;
 
 import org.jackie.jclassfile.attribute.AttributeHelper;
 import org.jackie.jclassfile.attribute.AttributeProvider;
 import org.jackie.jclassfile.attribute.AttributeSupport;
 import org.jackie.jclassfile.code.CodeParser;
 import org.jackie.jclassfile.code.Instruction;
 import org.jackie.jclassfile.code.ConstantPoolSupport;
 import org.jackie.jclassfile.constantpool.Task;
 import org.jackie.jclassfile.constantpool.ConstantPool;
 import org.jackie.jclassfile.constantpool.impl.ClassRef;
 import org.jackie.jclassfile.model.AttributeInfo;
 import org.jackie.jclassfile.util.Helper;
 import org.jackie.utils.IOHelper;
 import org.jackie.utils.Countdown;
 import org.jackie.utils.XDataInput;
 import org.jackie.utils.XDataOutput;
 import org.jackie.utils.ByteArrayDataOutput;
 import org.jackie.utils.Log;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Patrik Beno
  */
 public class Code extends AttributeInfo implements AttributeSupport, ConstantPoolSupport {
 
 	static public class Provider implements AttributeProvider {
 		public String name() {
 			return "Code";
 		}
 		public AttributeInfo createAttribute(AttributeSupport owner) {
 			return new Code(owner);
 		}
 	}
 
 	/*
 Code_attribute {
     	u2 attribute_name_index;
     	u4 attribute_length;
     	u2 max_stack;
     	u2 max_locals;
     	u4 code_length;
     	u1 code[code_length];
     	u2 exception_table_length;
     	{    	u2 start_pc;
     	      	u2 end_pc;
     	      	u2  handler_pc;
     	      	u2  catch_type;
     	}	exception_table[exception_table_length];
     	u2 attributes_count;
     	attribute_info attributes[attributes_count];
     }
     */
 
 	class ExceptionTableItem {
 		int startpc;
 		int endpc;
 		int handlerpc;
 		ClassRef exception;
 	}
 
 	int maxStack;
 	int maxLocals;
 
 	Instruction instructions;
 	List<ExceptionTableItem> exceptions;
 	List<AttributeInfo> attributes;
 
 
 	public Code(AttributeSupport owner) {
 		super(owner);
 	}
 
 	public Instruction instructions() {
 		return instructions;
 	}
 
 	protected Task readConstantDataOrGetResolver(XDataInput in, ConstantPool pool) {
 		int attrlen = readLength(in);
 
 		metastuff: {
 			maxStack = in.readUnsignedShort();
 			maxLocals = in.readUnsignedShort();
 		}
 
 		code: {
 			int len = in.readInt();
 			CodeParser parser = new CodeParser(in, len, pool);
 			instructions = parser.parse();
 		}
 
 		exceptions: {
 			int exlen = in.readUnsignedShort();
 			exceptions = new ArrayList<ExceptionTableItem>(exlen);
 			for (Countdown c = new Countdown(exlen); c.next();) {
 				ExceptionTableItem item = new ExceptionTableItem();
 				item.startpc = in.readUnsignedShort();
 				item.endpc = in.readUnsignedShort();
 				item.handlerpc = in.readUnsignedShort();
				item.exception = pool.getConstant(in.readUnsignedShort(), ClassRef.class);
 				exceptions.add(item);
 			}
 		}
 
 		attributes: {
 			attributes = AttributeHelper.loadAttributes(owner, in);
 			AttributeHelper.qdhRemoveUnsupportedAttributes(attributes);
 		}
 
 		return null;
 	}
 
 	public void registerConstants(ConstantPool pool) {
 		super.registerConstants(pool);
 		for (Instruction insn : instructions) {
 			insn.registerConstants(pool);
 		}
 		for (ExceptionTableItem e : exceptions) {
			e.exception = pool.register(e.exception);
 		}
 		for (AttributeInfo a : attributes) {
 			a.registerConstants(pool);
 		}
 	}
 
 	protected void writeData(XDataOutput out) {
 		int codesize = calculateCodeSize();
 
 		// render attributes first: need their length for header
 		byte[] attrs;
 		attributes: {
 			ByteArrayDataOutput tmpout = new ByteArrayDataOutput();
 			tmpout.writeShort(attributes.size());
 			for (AttributeInfo a : attributes) {
 				a.save(tmpout);
 			}
 			IOHelper.close(tmpout);
 			attrs = tmpout.toByteArray();
 		}
 
 		writeLength(out, 2+2+4+codesize+2+exceptions.size()*8+attrs.length);
 
 		out.writeShort(maxStack);
 		out.writeShort(maxLocals);
 
 		Log.debug("Instructions (%s):", instructions.asList().size());
 		out.writeInt(codesize);
 		for (Instruction insn : instructions) {
 			insn.save(out);
 		}
 
 		out.writeShort(exceptions.size());
 		for (ExceptionTableItem item : exceptions) {
 			out.writeShort(item.startpc);
 			out.writeShort(item.endpc);
 			out.writeShort(item.handlerpc);
 			Helper.writeConstantReference(item.exception, out);
 		}
 
 		// attributes
 		out.write(attrs);
 	}
 	public List<AttributeInfo> attributes() {
 		return attributes;
 	}
 
 	public void addAttribute(AttributeInfo attribute) {
 		attributes.add(attribute);
 	}
 
 	int calculateCodeSize() {
 		int size = 0;
 		for (Instruction insn : instructions) {
 			size += insn.size();
 		}
 		return size;
 	}
 }
