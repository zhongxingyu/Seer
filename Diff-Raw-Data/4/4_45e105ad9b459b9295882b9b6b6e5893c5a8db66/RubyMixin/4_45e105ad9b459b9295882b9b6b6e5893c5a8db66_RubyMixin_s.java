 package org.eclipse.dltk.ruby.internal.parser.mixin;
 
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.mixin.IMixinParser;
 import org.eclipse.dltk.core.mixin.IMixinRequestor;
 import org.eclipse.dltk.core.mixin.MixinSourceElementRequestor;
 import org.eclipse.dltk.ruby.internal.parser.RubySourceElementParser;
 
 public class RubyMixin implements IMixinParser {
 
 	public final static String INSTANCE_SUFFIX = "%"; // suffix for instance
 	// classes
 
 	public final static String VIRTUAL_SUFFIX = "%v"; // suffix for virtual
 	// classes
 
 	private IMixinRequestor requestor;
 
 	public void parserSourceModule(char[] contents, boolean signature,
 			ISourceModule module) {

 		long start = System.currentTimeMillis();
 		ModuleDeclaration moduleDeclaration = RubySourceElementParser
 				.parseModule(null, contents, null);
 		long end = System.currentTimeMillis();		
 //		System.out.println("RubyMixin: parsing took " + (end - start));
 		start = end;
 		RubyMixinBuildVisitor visitor = new RubyMixinBuildVisitor(
 				moduleDeclaration, module, signature, requestor);
 		try {
 			moduleDeclaration.traverse(visitor);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		end = System.currentTimeMillis();
 //		System.out.println("RubyMixin: traversing took " + (end - start) + " signature=" + signature);
 	}
 
 	public void setRequirestor(IMixinRequestor requestor) {
 		this.requestor = requestor;
 	}
 }
