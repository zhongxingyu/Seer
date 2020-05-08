 package org.jackie.jclassfile.constantpool.impl;
 
 import org.jackie.jclassfile.constantpool.Constant;
 import org.jackie.jclassfile.constantpool.CPEntryType;
 import org.jackie.jclassfile.constantpool.ConstantPool;
 import org.jackie.jclassfile.constantpool.Task;
 
 import java.io.DataOutput;
 import java.io.IOException;
 import java.io.DataInput;
 
 /**
  * @author Patrik Beno
  */
 public class StringRef extends Constant {
 	/*
 CONSTANT_String_info {
     	u1 tag;
     	u2 string_index;
     }
 	 */
 
 	Utf8 value;
 
 	StringRef(ConstantPool pool) {
 		super(pool);
 	}
 
 	StringRef(ConstantPool pool, String value) {
 		super(pool);
 		this.value = factory().getUtf8(value);
 	}
 
 	public CPEntryType type() {
 		return CPEntryType.STRING;
 	}
 
 	protected Task readConstantDataOrGetResolver(DataInput in) throws IOException {
 		final int index = in.readUnsignedShort();
 		return new Task() {
 			public void execute() throws IOException {
 				value = pool.getConstant(index, Utf8.class);
 			}
 		};
 	}
 
 	protected void writeConstantData(DataOutput out) throws IOException {
		out.writeInt(value.getIndex());
 	}
 
 	public boolean equals(Object o) {
 		if (this == o) return true;
 		if (o == null || getClass() != o.getClass()) return false;
 
 		StringRef stringRef = (StringRef) o;
 
 		if (value != null ? !value.equals(stringRef.value) : stringRef.value != null) return false;
 
 		return true;
 	}
 
 	public int hashCode() {
 		return (value != null ? value.hashCode() : 0);
 	}
 }
