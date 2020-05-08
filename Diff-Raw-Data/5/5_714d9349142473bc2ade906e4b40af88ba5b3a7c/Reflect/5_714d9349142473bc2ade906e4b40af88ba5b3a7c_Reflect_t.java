 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.sourceforge.argparse4j.ArgumentParsers;
 import net.sourceforge.argparse4j.impl.Arguments;
 import net.sourceforge.argparse4j.inf.ArgumentParser;
 import net.sourceforge.argparse4j.inf.ArgumentParserException;
 import net.sourceforge.argparse4j.inf.Namespace;
 import reflect.CodeExceptions;
 import reflect.android.api.Class;
 import reflect.android.api.Classes;
 import reflect.android.api.Method;
 import reflect.android.api.Param;
 import reflect.android.api.Property;
 import reflect.cpp.CppWriter;
 
 public class Reflect {
 
 	private File m_inc;
 	private File m_src;
 	private List<String> m_classes;
 
 	public Reflect(File inc, File src) {
 		m_inc = inc;
 		m_src = src;
 		m_classes = null;
 	}
 
 	private void setKnownClasses(List<String> classes) {
 		m_classes  = classes;
 	}
 
 	private static final String spaces = "                                                            ";
 	private void printClass(String clazz, int curr, int max) throws IOException
 	{
 		Class klazz = Classes.forName(clazz);
 		if (klazz == null) return;
 
 		String supah = klazz.getSuper();
 		String[] interfaces = klazz.getInterfaces();
 		System.out.print("class " + clazz);
 		if (supah != null || interfaces.length > 0)
 		{
 			if (clazz.length() < spaces.length())
 				System.out.print(spaces.substring(clazz.length()));
 		}
 		if (supah != null)
 			System.out.print(" extends " + supah);
 		if (interfaces.length > 0) {
 			System.out.print(" implements ");
 			boolean first = true;
 			for (String iface: interfaces) {
 				if (first) first = false;
 				else System.out.print(", ");
 				System.out.print(iface);
 			}
 		}
 		System.out.println(" (" + curr + "/" + max + ")");
 		CppWriter writer = new CppWriter(klazz, m_classes);
 		writer.printHeader(m_inc);
 		writer.printSource(m_src);
 	}
 
 	public static void main(String[] args)
 	{
 
 		CodeExceptions.readExceptions();
 
 		ArgumentParser parser = ArgumentParsers.newArgumentParser("Reflect")
 				.defaultHelp(true)
 				.description("Create JINI bindings for given class(es). CLASS can be in form java.lang.Class to generate binding for one class only or java.lang.* to generate it for all java.lang classes (but not java.class.reflect classes). When a subclass is provided ($ is present), it will be changed to the outer-most class.")
 				.epilog("Either --all or at least one class is needed.");
 		parser.addArgument("-a", "--android")
 				.metavar("api")
 				.type(Integer.class)
 				.dest("targetAPI")
 				.required(true)
 				.help("Android API Level (e.g. -a 17 for Android 4.2)");
 		parser.addArgument("--dest")
 				.metavar("dir")
 				.type(File.class)
 				.setDefault(new File("./code"))
 				.dest("dest")
 				.help("The output directory");
 		parser.addArgument("--inc")
 				.metavar("dir")
				.type(File.class)
 				.dest("inc")
 				.help("The output dir for .hpp files (default: $dest" + File.separator + "inc)");
 		parser.addArgument("--src")
 				.metavar("dir")
				.type(File.class)
 				.dest("src")
 				.help("The output dir for .cpp files (default: $dest" + File.separator + "src)");
 		parser.addArgument("--preserve-refs")
 				.action(Arguments.storeTrue())
 				.setDefault(false)
 				.dest("refs")
 				.help("If set, will preserve methods and properties, whose types are not builtin, in java.lang package nor on the list of classes");
 		parser.addArgument("--parents")
 				.action(Arguments.storeTrue())
 				.setDefault(false)
 				.help("Generate classes for the superclass and interfaces classes");
 		parser.addArgument("--all-deps")
 				.action(Arguments.storeTrue())
 				.setDefault(false)
 				.help("Generate classes for the superclass, interfaces, field and parameter classes; implies --parent and --preserve-refs");
 		parser.addArgument("--all")
 				.action(Arguments.storeTrue())
 				.setDefault(false)
 				.help("Generates bindings for all the classes in the API; implies --all-deps, --parent and --preserve-refs");
 		parser.addArgument("files")
 				.metavar("CLASS")
 				.type(String.class)
 				.nargs("*")
 				.help("Class to generate binding for");
 
 		Namespace ns = null;
 		try {
 			ns = parser.parseArgs(args);
 			if (!ns.getBoolean("all") && ns.getList("files").size() == 0)
 				throw new ArgumentParserException("Either --all or at least one class is needed.", parser);
 		} catch (ArgumentParserException e) {
 			parser.handleError(e);
 			System.exit(1);
 		}
 		File inc = (File)ns.get("inc");
 		File src = (File)ns.get("src");
 		File dest = (File)ns.get("dest");
 		boolean all = ns.getBoolean("all");
 		boolean deps = ns.getBoolean("all_deps");
 		boolean parents = ns.getBoolean("parents");
 		boolean refs = ns.getBoolean("refs");
 		final List<String> list = ns.getList("files");
 		List<String> classes = new LinkedList<String>();
 
 		if (inc == null) inc = new File(dest, "inc");
 		if (src == null) src = new File(dest, "src");
 		if (all) deps = true; // deps is a subset of deps
 		if (deps) parents = true; // parents is a subset of deps
 		if (deps) refs = true; //if deps (or all) is present, we do not want to look for limits, as there should be none
 
 		try {		
 			System.out.print("API Level : "); System.out.println(ns.getInt("targetAPI"));
 			System.out.print("Headers   : "); System.out.println(inc.getCanonicalPath());
 			System.out.print("Sources   : "); System.out.println(src.getCanonicalPath());
 			System.out.print("Mode      : "); System.out.println(all ? "Entire API" : deps ? "All dependencies" : parents ? "Parents" : "Classes");
 			System.out.print("Unk. refs : "); System.out.println(refs ? "preserved" : "methods removed");
 			System.out.print("Classes   : "); if (all) System.out.println("all"); else System.out.println(list);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		
 		int sdk = ns.getInt("targetAPI");
 
 		try {
 			if (!Classes.setTargetApi(sdk))
 			{
 				System.err.println("Could not initiate android-" + sdk + " environment");
 				return;
 			}
 
 			final Reflect reflect = new Reflect(inc, src);
 
 			for (String item: list) {
 				if (item.endsWith(".*")) {
 					String [] pkg = Classes.packageClasses(item.substring(0, item.length()-2));
 					for (String clazz: pkg)
 						classes.add(clazz);
 					continue;
 				}
 				classes.add(item);
 			}
 
 			if (all)
 			{
 				classes.clear();
 				String[] api_classes = Classes.classNames();
 				for (String clazz: api_classes)
 					if (clazz.indexOf('$') == -1)
 						classes.add(clazz);
 			}
 			else if (parents) // || deps, see above
 			{
 				int i = 0;
 				while (i < classes.size())
 					addClass(classes, Classes.forName(classes.get(i++)), deps);
 			}
 
 			int curr = 0;
 			if (!refs)
 				reflect.setKnownClasses(classes);
 
 			for (String s: classes)
 			{
 				reflect.printClass(s, ++curr, classes.size());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	static void addSignature(List<String> classes, String type)
 	{
 		int array = 0;
 		while (array < type.length() && type.charAt(array) == '[')
 			++array;
 
 		if (array < type.length() && type.charAt(array) == 'L')
 			addClass(classes, type.substring(array + 1, type.length() - 1));
 	}
 
 	static void addClass(List<String> classes, String className)
 	{
 		int pos = className.indexOf('$');
 		if (pos != -1)
 			className = className.substring(0, pos);
 
 		for (String s: classes)
 			if (s.equals(className)) return;
 
 		classes.add(className);
 	}
 
 	static void addClass(List<String> classes, Class c, boolean all) {
 		if (c == null) return;
 		final String sup = c.getSuper();
 		final String[] ifaces = c.getInterfaces();
 		if (sup != null) addClass(classes, sup);
 		for (String iface: ifaces)
 			addClass(classes, iface);
 
 		if (all) {
 			for (Property prop: c.getProperties())
 				addSignature(classes, prop.getSignature());
 			for (Method method: c.getMethods()) {
 				addSignature(classes, method.getReturnType());
 				for (Param param: method.getParameterTypes())
 					addSignature(classes, param.getSignature());
 			}
 		}
 
 		for (Class sub: c.getClasses())
 			addClass(classes, sub, all);
 	}
 }
