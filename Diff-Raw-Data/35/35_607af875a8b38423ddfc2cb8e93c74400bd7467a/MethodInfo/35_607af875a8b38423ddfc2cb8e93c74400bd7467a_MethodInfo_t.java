 package org.jackie.jclassfile.model;
 
import org.jackie.jclassfile.util.TypeDescriptor;
import org.jackie.jclassfile.util.MethodDescriptor;

 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 
 /**
  * @author Patrik Beno
  */
 public class MethodInfo extends MemberInfo {
    /*
 method_info {
     	u2 access_flags;
     	u2 name_index;
     	u2 descriptor_index;
     	u2 attributes_count;
     	attribute_info attributes[attributes_count];
     }
     */
 
 	public MethodInfo(ClassFile classfile) {
 		super(classfile);
 	}

	public MethodDescriptor methodDescriptor() {
		return new MethodDescriptor(descriptor());
	}

 }
