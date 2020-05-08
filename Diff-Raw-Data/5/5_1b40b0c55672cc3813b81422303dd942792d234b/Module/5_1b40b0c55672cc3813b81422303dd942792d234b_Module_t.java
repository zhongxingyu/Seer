 package uk.ac.ic.doc.gander.model;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.python.pydev.parser.jython.SimpleNode;
 
 import uk.ac.ic.doc.gander.model.build.BuildableScope;
 
 public class Module implements Scope, BuildableScope {
 
 	private Map<String, Class> classes = new HashMap<String, Class>();
 	private Map<String, Function> functions = new HashMap<String, Function>();
 
 	private String name;
 	private org.python.pydev.parser.jython.ast.Module module;
 	private Package parent;
 
 	public Module(org.python.pydev.parser.jython.ast.Module module,
 			String name, Package parent) {
 		this.module = module;
 		this.name = name;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see uk.ac.ic.doc.cfg.model.IModelElement#getName()
 	 */
 	public String getName() {
 		return name;
 	}
 
 	public String getFullName() {
		if (parent != null)
			return parent.getFullName() + "." + getName();
		else
			return getName();
 	}
 
 	public SimpleNode getAst() {
 		return module;
 	}
 
 	public Package getParentPackage() {
 		return parent;
 	}
 
 	public Scope getParentScope() {
 		return getParentPackage();
 	}
 
 	public Map<String, Package> getPackages() {
 		return Collections.emptyMap();
 	}
 
 	public Map<String, Module> getModules() {
 		return Collections.emptyMap();
 	}
 
 	public Map<String, Class> getClasses() {
 		return Collections.unmodifiableMap(classes);
 	}
 
 	public Map<String, Function> getFunctions() {
 		return Collections.unmodifiableMap(functions);
 	}
 
 	public Scope lookup(String token) {
 		// FIXME: This order is arbitrary. Really we should record which
 		// function/class definition came last and only use that one
 		Scope subItem = getClasses().get(token);
 		if (subItem == null)
 			subItem = getFunctions().get(token);
 
 		return subItem;
 	}
 
 	public void addPackage(Package pkg) {
 		throw new Error("A module cannot contain a package");
 	}
 
 	public void addModule(Module module) {
 		throw new Error("A module cannot contain another module");
 	}
 
 	public void addClass(Class klass) {
 		classes.put(klass.getName(), klass);
 	}
 
 	public void addFunction(Function function) {
 		functions.put(function.getName(), function);
 	}
 }
