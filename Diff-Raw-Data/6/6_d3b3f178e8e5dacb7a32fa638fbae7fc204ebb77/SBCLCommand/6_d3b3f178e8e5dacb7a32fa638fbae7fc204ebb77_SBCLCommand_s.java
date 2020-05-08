 package com.github.ashtonkem.command;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import com.github.ashtonkem.configuration.SourceLayout;
 
 public class SBCLCommand extends LispCommand {
 
 	private Collection<String> expressions;
 
	private SourceLayout layout;

 	public SBCLCommand(boolean Finalize, SourceLayout layout) {
 		super(Finalize, layout);
 		expressions = new LinkedList<String>();
 		// Disabling the debugger prevents the SBCL process from spewing
 		// stacktraces to STDOUT waiting for console
 		// response.
 		this.addExpression("(sb-ext:disable-debugger)");
 		this.addExpression("(require :asdf)");
 		// Helper function, to avoid repeating formats everywhere.
 		this.addExpression("(defun to-string (arg) (format nil \"~A\" arg))");
 		String asdfInit = "";
 		// Initialize ASDF so it places our fasls inside of target/fasl, as we
 		// expect.
 		asdfInit += "(funcall 'asdf::initialize-output-translations ";
 		asdfInit += "(list :output-translations :disable-cache :ignore-inherited-configuration ";
 		asdfInit += "(list (to-string *default-pathname-defaults*) (merge-pathnames \"target/fasl/**/*.*\" (to-string *default-pathname-defaults*)))))";
 		this.addExpression(asdfInit);
 	}
 
 	public void addExpression(String exp) {
 		expressions.add(exp);
 	}
 
 	public void setCoreName(String s) {
 		addExpression("(sb-ext:save-lisp-and-die \"target/" + s
 				+ ".core\" :executable t)");
 
 	}
 
 	public void setMainPackage(String s) {

 		addExpression("(asdf:operate 'asdf:load-op :" + s + " :verbose nil)");

 	}
 
 	public Iterator<String> iterator() {
 		return expressions.iterator();
 	}
 
 	@Override
 	public void includeFasls() {
 		for (File f : layout.faslFiles()) {
 			ExpressionBuilder builder = new ExpressionBuilder("load");
 			builder.addString(f.getPath());
 			this.addExpression(builder.getExpression());
 		}
 
 	}
 
 	@Override
 	public void includeSource() {
 		for (File f : layout.asdFiles()) {
 			this.addPackage(f);
 		}
 	}
 
 	@Override
 	public void addPackage(File f) {
 		this.addExpression("(pushnew (truename \"" + f.getParent()
 				+ "\") asdf::*central-registry*)");
 	}
 }
