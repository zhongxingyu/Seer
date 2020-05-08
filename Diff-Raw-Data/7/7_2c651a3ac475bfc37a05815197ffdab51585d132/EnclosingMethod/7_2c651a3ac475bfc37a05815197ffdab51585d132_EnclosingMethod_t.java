 package org.jackie.jclassfile.attribute.std;
 
 import org.jackie.jclassfile.attribute.AttributeProvider;
 import org.jackie.jclassfile.attribute.AttributeSupport;
 import org.jackie.jclassfile.constantpool.Task;
 import org.jackie.jclassfile.constantpool.ConstantPool;
 import org.jackie.jclassfile.constantpool.impl.ClassRef;
 import org.jackie.jclassfile.constantpool.impl.NameAndType;
 import org.jackie.jclassfile.model.AttributeInfo;
import org.jackie.jclassfile.util.Helper;
 import org.jackie.utils.XDataInput;
 import org.jackie.utils.XDataOutput;
 
 /**
  * @author Patrik Beno
  */
 public class EnclosingMethod extends AttributeInfo {
 
 	static public class Provider implements AttributeProvider {
 		public String name() {
 			return "EnclosingMethod";
 		}
 		public AttributeInfo createAttribute(AttributeSupport owner) {
 			return new EnclosingMethod(owner);
 		}
 	}
 
 
 	/*
 EnclosingZethod_attributeC{
 	u2 attribute_name_index
 	u4 attribute_length
 	u2 class_index
 	u2 method_index
 }
 	 */
 
 	ClassRef cls;
 	NameAndType method;
 
 	public EnclosingMethod(AttributeSupport owner) {
 		super(owner);
 	}
 
 	protected Task readConstantDataOrGetResolver(XDataInput in, ConstantPool pool) {
 		readLength(in, 4);
 		cls = pool.getConstant(in.readUnsignedShort(), ClassRef.class);
 		method = pool.getConstant(in.readUnsignedShort(), NameAndType.class);
 		return null;
 	}
 
 	protected void writeData(XDataOutput out) {
 		writeLength(out, 4);
 		cls.writeReference(out);
		Helper.writeConstantReference(method, out);
 	}
 
 	@Override
 	public void registerConstants(ConstantPool pool) {
 		super.registerConstants(pool);
 		cls = pool.register(cls);
		if (method != null) { method = pool.register(method); }
 	}
 }
