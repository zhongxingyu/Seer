 package com.github.rd4j.analysis;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.objectweb.asm.ClassAdapter;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodAdapter;
 import org.objectweb.asm.MethodVisitor;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.Type;
 import org.objectweb.asm.commons.EmptyVisitor;
 import org.objectweb.asm.tree.LocalVariableNode;
 import org.objectweb.asm.tree.MethodNode;
 
 public class ParameterTypeReader {
 	
 	static Set<String> methodNamesToIgnore = new HashSet<String>();
 	static {
 		for(Method m : Object.class.getMethods()) {
 			methodNamesToIgnore.add(m.getName());
 		}
 	}
 	
 	public static class MethodVisitorForStuff extends MethodAdapter   {
 
 		public MethodVisitorForStuff(boolean isStatic, String name) {
 			super(new EmptyVisitor());
 		}
 
 		@Override
 		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
 			
 		}
 	}
 
 	
 	public static class ClassVisitorForStuff extends ClassAdapter {
 		final Map <String, MethodParameter[]> map;
 		Class<?> clazz;
 		List<MethodNode> nodes = new ArrayList<MethodNode>();
 		
 		public ClassVisitorForStuff(Class<?> clazz, Map <String, MethodParameter[]> map) {
 			super(new EmptyVisitor());
 			this.map = map;
 			this.clazz = clazz;
 		}
 
 		@Override
 		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
 			
 			MethodNode node = new MethodNode(access,
 	                  name,
 	                  desc,
 	                  signature,
 	                  exceptions);
 			nodes.add(node);
			
 			return node;
 		}
 
 		@Override
 		public void visitEnd() {
			Map<String,Method> nameToMethod = new HashMap<String,Method>();
 			for(Method m : clazz.getMethods()) {
 				if(methodNamesToIgnore.contains(m.getName()))
 					continue;
 				
 				if(nameToMethod.containsKey(m.getName())) {
 					throw new RuntimeException("polymorphic methods not supported.  "+m.getName()+" already exists in class");
 				}
				nameToMethod.put(m.getName(), m);
 			}
 			
 			// now that we've got all the nodes
 			for(MethodNode node : nodes) {
 				boolean isStatic = (node.access & Opcodes.ACC_STATIC) != 0;
 				Type [] argTypes = Type.getArgumentTypes(node.desc);
 				int start = 0;
 				if(!isStatic)
 					start ++;
 				
 				List<MethodParameter> parameters = new ArrayList<MethodParameter>();
				Method methodRef = nameToMethod.get(node.name);
				if (methodRef == null)
 					continue;
				
				java.lang.reflect.Type[] parameterTypes = methodRef.getGenericParameterTypes();
 				
 				for(int i = 0; i<argTypes.length; i++) {
 					LocalVariableNode local = (LocalVariableNode) node.localVariables.get(i+start);
 					if(local.index != i+start) {
 						throw new RuntimeException("index mismatch");
 					}
 					
 					MethodParameter p = new MethodParameter(local.name, parameterTypes[i]);
 					parameters.add(p);
 					
 				}
 				
 				map.put(node.name, parameters.toArray(new MethodParameter[parameters.size()]));
 			}
 				
 			super.visitEnd();
 		}
 	}
 
 	public static Map<String,MethodParameter[]> getMethodParameters(Class<?> c) {
 		Map <String, MethodParameter[]> methodMap = new HashMap <String, MethodParameter[]>();
 		
 		InputStream is = getClassAsInputStream(c);
 		ClassReader cr;
 		try {
 			cr = new ClassReader(is);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		
 		ClassVisitorForStuff v = new ClassVisitorForStuff(c, methodMap);
 		cr.accept(v, 0);
 
 		return methodMap;
 	}
 
 	private static String getPathToClass(Class <?> c) {
 		return c.getName().replace(".", "/")+".class";
 	}
 	
 	public static InputStream getClassAsInputStream(Class <?> c) {
 		String classPath = getPathToClass(c);
 		InputStream is = c.getClassLoader().getResourceAsStream(classPath);
 		return is;
 	}
 
 
 }
