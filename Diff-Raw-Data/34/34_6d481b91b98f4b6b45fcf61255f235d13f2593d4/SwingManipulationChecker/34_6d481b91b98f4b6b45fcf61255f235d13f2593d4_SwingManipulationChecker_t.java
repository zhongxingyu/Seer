 package commons.swing;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.objectweb.asm.AnnotationVisitor;
 import org.objectweb.asm.Attribute;
 import org.objectweb.asm.ClassReader;
 import org.objectweb.asm.ClassVisitor;
 import org.objectweb.asm.FieldVisitor;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.MethodVisitor;
 
 public class SwingManipulationChecker {
 
	public final static class SwingInvocationClassVisitor implements ClassVisitor {
 
 		private static final String JAVAX_SWING = "javax/swing";
 		private final List<String> swingInvocations;
 
 		public SwingInvocationClassVisitor(List<String> swingInvocations) {
 			this.swingInvocations = swingInvocations;
 		}
 
 		@Override
 		public void visit(int arg0, int arg1, String arg2, String arg3,
 				String arg4, String[] arg5) {
 
 		}
 
 		@Override
 		public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
 			return null;
 		}
 
 		@Override
 		public void visitAttribute(Attribute arg0) {
 
 		}
 
 		@Override
 		public void visitEnd() {
 
 		}
 
 		@Override
 		public FieldVisitor visitField(int arg0, String arg1, String arg2,
 				String arg3, Object arg4) {
 			return null;
 		}
 
 		@Override
 		public void visitInnerClass(String arg0, String arg1, String arg2,
 				int arg3) {
 
 		}
 
 		@Override
 		public MethodVisitor visitMethod(int arg0, String arg1, String arg2,
 				String arg3, String[] arg4) {
 			
 			return new MethodVisitor() {
 			
 				@Override
 				public void visitVarInsn(int arg0, int arg1) {
 					
 			
 				}
 			
 				@Override
 				public void visitTypeInsn(int arg0, String arg1) {
 					
 			
 				}
 			
 				@Override
 				public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2,
 						String arg3) {
 					
 			
 				}
 			
 				@Override
 				public void visitTableSwitchInsn(int arg0, int arg1, Label arg2,
 						Label[] arg3) {
 					
 			
 				}
 			
 				@Override
 				public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1,
 						boolean arg2) {
 					
 					return null;
 				}
 			
 				@Override
 				public void visitMultiANewArrayInsn(String arg0, int arg1) {
 					
 			
 				}
 			
 				@Override
 				public void visitMethodInsn(int arg0, String arg1, String arg2, String arg3) {
 					if (arg1.startsWith(JAVAX_SWING)){
 						swingInvocations.add(arg1 + " " + arg2 + " " + arg3);
 					}
 				}
 			
 				@Override
 				public void visitMaxs(int arg0, int arg1) {
 					
 			
 				}
 			
 				@Override
 				public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
 					
 			
 				}
 			
 				@Override
 				public void visitLocalVariable(String arg0, String arg1, String arg2,
 						Label arg3, Label arg4, int arg5) {
 					
 			
 				}
 			
 				@Override
 				public void visitLineNumber(int arg0, Label arg1) {
 					
 			
 				}
 			
 				@Override
 				public void visitLdcInsn(Object arg0) {
 					
 			
 				}
 			
 				@Override
 				public void visitLabel(Label arg0) {
 					
 			
 				}
 			
 				@Override
 				public void visitJumpInsn(int arg0, Label arg1) {
 					
 			
 				}
 			
 				@Override
 				public void visitIntInsn(int arg0, int arg1) {
 					
 			
 				}
 			
 				@Override
 				public void visitInsn(int arg0) {
 					
 			
 				}
 			
 				@Override
 				public void visitIincInsn(int arg0, int arg1) {
 					
 			
 				}
 			
 				@Override
 				public void visitFieldInsn(int arg0, String arg1, String arg2, String arg3) {
 					
 			
 				}
 			
 				@Override
 				public void visitEnd() {
 					
 			
 				}
 			
 				@Override
 				public void visitCode() {
 					
 			
 				}
 			
 				@Override
 				public void visitAttribute(Attribute arg0) {
 					
 			
 				}
 			
 				@Override
 				public AnnotationVisitor visitAnnotationDefault() {
 					
 					return null;
 				}
 			
 				@Override
 				public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
 					
 					return null;
 				}
 
 				@Override
 				public void visitFrame(int arg0, int arg1, Object[] arg2,
 						int arg3, Object[] arg4) {
 					
 				}
 			};
 		}
 
 		@Override
 		public void visitOuterClass(String arg0, String arg1, String arg2) {
 			
 
 		}
 
 		@Override
 		public void visitSource(String arg0, String arg1) {
 			
 
 		}
 
 	}
 
 	public List<String> detectIllegalSwingManipulations(Class<?> class1) {
 
 		List<String> swingInvocations = new ArrayList<String>();
 
 		
 		internalDetect(class1, swingInvocations, 0);
 		
 		return swingInvocations;
 	}
 
 	private void internalDetect(Class<?> class1, List<String> swingInvocations, int level) {
 		InputStream classBytecode = class1.getResourceAsStream(class1.getSimpleName() + (level == 0? "": "$" + level ) + ".class");
 		if (classBytecode == null)
 			return;
 
 		collectSwingInvocations(swingInvocations, classBytecode);
 		internalDetect(class1, swingInvocations, level + 1);
 	}
 
 	private void collectSwingInvocations(List<String> swingInvocations,
 			InputStream classBytecode) {
 		try{
 			new ClassReader(classBytecode).accept(new SwingInvocationClassVisitor(swingInvocations ), 0);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}finally{
 			try {
 				classBytecode.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
