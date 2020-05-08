 package org.jackie.jclassfile.model;
 
 import org.jackie.jclassfile.constantpool.ConstantPool;
 import org.jackie.jclassfile.constantpool.Task;
 import static org.jackie.jclassfile.constantpool.ConstantPool.constantPool;
 import org.jackie.jclassfile.constantpool.impl.Utf8;
 import org.jackie.jclassfile.attribute.AttributeSupport;
 import org.jackie.jclassfile.code.ConstantPoolSupport;
 import static org.jackie.utils.Assert.doAssert;
 import org.jackie.utils.XDataInput;
 import org.jackie.utils.XDataOutput;
 import org.jackie.utils.Log;
 
 /**
  * @author Patrik Beno
  */
 public abstract class AttributeInfo extends Base implements ConstantPoolSupport {
 	/*
 	attribute_info {
 			 u2 attribute_name_index;
 			 u4 attribute_length;
 			 u1 info[attribute_length];
 		 }
 		 */
 
 	protected AttributeSupport owner;
 
 	protected Utf8 name;
 	Task resolver;
 
 
 	/// constructors ///
 
 
 	protected AttributeInfo(AttributeSupport owner, Utf8 name) {
 		this.owner = owner;
 		this.name = name;
 	}
 
 	protected AttributeInfo(AttributeSupport owner) {
 		this.owner = owner;
 		this.name = Utf8.create(getClass().getSimpleName());

 	}
 
 	/// properties ///
 
 	public AttributeSupport owner() {
 		return owner;
 	}
 
 	public String name() {
 		return name.value();
 	}
 
 
 	/// owner/pool management ///
 
 
 	public void detach() {
 		owner = null;
 	}
 
 	/// load & save ///
 
 	public void load(XDataInput in, ConstantPool pool) {
 		// name is expected to be loaded by attribute resolver (and passed in constructor)
		resolver = readConstantDataOrGetResolver(in, constantPool());
 		if (resolver != null) { // todo debug
 			resolver.execute();
 		}
 	}
 
 	public void save(XDataOutput out) {
 		Log.debug("Saving attribute %s (%s)", name(), getClass());
 		name.writeReference(out);
 		writeData(out);
 	}
 
 	public void registerConstants(ConstantPool pool) {
 		name = pool.register(name);
 	}
 
 	protected int readLength(XDataInput in, Integer expected) {
 		int len = in.readInt();
 		if (expected != null) {
 			doAssert(len == expected, "Invalid length: %s. Expected: %s", len, expected);
 		}
 		return len;
 	}
 
 	protected void writeLength(XDataOutput out, int length) {
 		out.writeInt(length);
 	}
 
 	protected abstract Task readConstantDataOrGetResolver(XDataInput in, ConstantPool pool);
 
 	protected abstract void writeData(XDataOutput out);
 
 	///
 
 	public String toString() {
 		return String.format("%s {%s}", name.value(), valueToString());
 	}
 
 	protected String valueToString() {
 		return "";
 	}
 }
