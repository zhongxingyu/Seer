 package org.jackie.compilerimpl;
 
 import org.jackie.compiler.Compiler;
 import org.jackie.compiler.context.CompilerContext;
 import org.jackie.compiler.extension.ExtensionManager;
 import org.jackie.compiler.filemanager.FileManager;
 import org.jackie.compiler.filemanager.FileObject;
 import org.jackie.compiler.typeregistry.TypeRegistry;
 import org.jackie.compilerimpl.ext.ExtensionManagerImpl;
 import org.jackie.compilerimpl.filemanager.MultiFileManager;
 import org.jackie.compilerimpl.javacintegration.JavacCompiler;
 import org.jackie.compilerimpl.jmodelimpl.JClassImpl;
 import org.jackie.compilerimpl.typeregistry.CompilerWorkspaceRegistry;
 import org.jackie.compilerimpl.typeregistry.JClassRegistry;
 import org.jackie.compilerimpl.typeregistry.MultiRegistry;
 import org.jackie.compilerimpl.typeregistry.PrimitiveTypeRegistry;
 import static org.jackie.context.ContextManager.*;
 import org.jackie.jclassfile.model.ClassFile;
 import org.jackie.jvm.JClass;
 import org.jackie.utils.ClassName;
 import org.jackie.utils.IOHelper;
 import org.jackie.utils.Log;
 
 import java.nio.ByteBuffer;
 import java.nio.channels.WritableByteChannel;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Patrik Beno
  */
 public class CompilerImpl implements Compiler {
 
 	protected FileManager sources;
 
 	protected FileManager workspace;
 
 	protected List<FileManager> dependencies;
 
 	public void compile() {
 		newContext();
 		try {
 			context().set(CompilerContext.class, new CompilerContext());
 			context().set(ExtensionManager.class, new ExtensionManagerImpl());
 
 			compileJavaSources();
 
 			context().set(TypeRegistry.class, new CompilerWorkspaceRegistry(
 					new JClassRegistry(workspace),
 					new MultiRegistry(toRegistryList(dependencies))
 			));
 
 			compileByteCode();
 
 			save();
 
 		} finally {
 			closeContext();
 		}
 	}
 
 	protected void save() {
 	}
 
 	protected List<TypeRegistry> toRegistryList(List<FileManager> dependencies) {
 		List<TypeRegistry> regs = new ArrayList<TypeRegistry>();
 		for (FileManager d : dependencies) {
 			regs.add(new JClassRegistry(d));
 		}
 		regs.add(new PrimitiveTypeRegistry());
 		return regs;
 	}
 
 	void compileJavaSources() {
 		Log.enter();
 
		List<String> options = Arrays.asList(/*"-g", "-source", "1.5", "-target", "1.5"*/);
 		JavacCompiler javac = new JavacCompiler(options,
 				sources, new MultiFileManager(dependencies), workspace);
 		javac.compile();
 
 		Log.leave();
 	}
 
 	void compileByteCode() {
 		for (JClass jcls : context(TypeRegistry.class).jclasses()) {
 			compile((JClassImpl) jcls);
 		}
 	}
 
 	void compile(JClassImpl jclass) {
 		newContext();
 		try {
 			ClassFile classfile = new ClassFile();
 
 			// compilation / bytecode rendering:
 			jclass.compile(classfile);
 
 			// create new object in the workspace (overwrite if neccessary)
 			ClassName clsname = new ClassName(jclass.getFQName());
 			workspace.remove(clsname.getPathName());
 			FileObject fo = workspace.create(clsname.getPathName());
 
 			// save data into newly allocated file object
 			WritableByteChannel out = fo.getOutputChannel();
 			IOHelper.write(
 					ByteBuffer.wrap(classfile.toByteArray()),
 					out);
 			IOHelper.close(out);
 
 
 		} finally {
 			closeContext();
 		}
 	}
 }
