 package org.jackie.compilerimpl.bytecode;
 
 import org.jackie.jclassfile.model.Type;
 import org.jackie.jclassfile.util.ClassNameHelper;
 import org.jackie.jclassfile.util.MethodDescriptor;
 import org.jackie.jclassfile.util.TypeDescriptor;
 import org.jackie.jvm.JClass;
 import org.jackie.jvm.extension.builtin.ArrayType;
 import org.jackie.jvm.extension.builtin.PrimitiveType;
 import org.jackie.jvm.structure.JMethod;
 import org.jackie.jvm.structure.JParameter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Patrik Beno
  */
 public abstract class ByteCodeBuilder {
 
 	static public void execute(ByteCodeBuilder builder) {
 		builder.init();
 		builder.run();
 	}
 
 	protected ByteCodeBuilder() {
 	}
 
 	protected void init() {
 	}
 
 	protected abstract void run();
 
 	protected String toBinaryClassName(JClass jclass) {
 		return ClassNameHelper.toBinaryClassName(jclass.getFQName());
 	}
 
 	protected TypeDescriptor getTypeDescriptor(JClass jclass) {
		PrimitiveType primitive = jclass.extensions().get(PrimitiveType.class);
 		ArrayType array = jclass.extensions().get(ArrayType.class);
 		TypeDescriptor d = new TypeDescriptor(
 				primitive != null ? Type.forClass(primitive.getPrimitiveClass()) : Type.CLASS,
 				array != null ? array.getDimensions() : 0,
 				primitive == null ? toBinaryClassName(jclass) : null
 		);
 		return d;
 	}
 
 	protected MethodDescriptor getMethodDescriptor(JMethod jmethod) {
 		List<TypeDescriptor> params = new ArrayList<TypeDescriptor>();
 		for (JParameter p : jmethod.getParameters()) {
 			params.add(getTypeDescriptor(p.getType()));
 		}
 		TypeDescriptor rtype = getTypeDescriptor(jmethod.getType());
 
 		return new MethodDescriptor(rtype, params);
 	}
 
 }
