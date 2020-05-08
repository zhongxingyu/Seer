 package org.jackie.compiler.util;
 
 import org.jackie.utils.Assert;
 
 import org.objectweb.asm.Type;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 /**
  * @author Patrik Beno
  */
 public class ClassName {
 
 	static private final char PACKAGE_SEP = '.';
 	static private final char CLASS_SEP = '$';
 	static private final String ARRAY_DIM_FLAG = "[]";
 
 	String fqname;    	// org.jackie.SomeClass$Nested$1Local[][]
 	String pckgname;		// org.jackie
 	String name;			// SomeClass$Nested$1Local
 	String simplename;  	// Local
 
 	// arrays
 	ClassName componentType; // component type of the array
 	Integer dimensions;		 // # of dimensions of the array
 
 	List<String> packages;
 	List<String> classes;
 	private boolean array;
 
 
 	public ClassName(Class cls) {
 		fqname = unwrapComponentClass(cls).getName() + dup(ARRAY_DIM_FLAG, dimensions=countArrayDimenstions(cls));
 	}
 
 	public ClassName(Type asmtype) {
 		fqname = asmtype.getClassName();
 	}
 
 	public ClassName(String fqname) {
 		this.fqname = fqname;
 	}
 
 	public ClassName(String fqname, int dimensions) {
 		this.fqname = fqname + dup(ARRAY_DIM_FLAG, dimensions);
 		this.dimensions = dimensions;
 	}
 
 
 	public String getFQName() {
 		return fqname;
 	}
 
 	public String getPackageFQName() {
 		if (pckgname != null) {
 			return pckgname;
 		}
 
 		int i = fqname.lastIndexOf(PACKAGE_SEP);
 		pckgname = (i != -1) ? fqname.substring(0, i) : "";
 		name = (i != -1) ? fqname.substring(i + 1) : fqname;
 
 		return pckgname;
 	}
 
 	public PackageName getPackageName() {
 		return new PackageName(getPackageFQName());
 	}
 
 	public String getName() {
 		if (name != null) {
 			return name;
 		}
 
 		int i = fqname.lastIndexOf(PACKAGE_SEP);
 		int arraypart = fqname.endsWith(ARRAY_DIM_FLAG) ? fqname.indexOf(ARRAY_DIM_FLAG) : fqname.length();
 		int arraydims = (fqname.length() - arraypart) / 2;
 
		name = fqname.substring(i+1, arraypart);
 		dimensions = arraydims;
 
 		return name;
 	}
 
 	public String getSimpleName() {
 		if (simplename != null) {
 			return simplename;
 		}
 
 		getName(); // force parsing
 
 		int i = name.lastIndexOf(CLASS_SEP);
 		String candidate = (i != -1) ? name.substring(i+1) : name;
 
 		simplename = getLocalName(candidate);
 		return simplename;
 	}
 
 	public ClassName getComponentType() {
 		if (getDimensions() == 0) {
 			return null;
 		}
 
 		componentType = new ClassName(fqname.substring(0, fqname.length()-getDimensions()*2));
 		return componentType;
 	}
 
 	public int getDimensions() {
 		if (dimensions != null) {
 			return dimensions;
 		}
 		getName(); // force parsing
 		return dimensions;
 	}
 
 	public List<String> getPackages() {
 		if (packages != null) {
 			return packages;
 		}
 
 		packages = split(getPackageFQName(), PACKAGE_SEP);
 		return packages;
 	}
 
 	public List<String> getClassNames() {
 		if (classes != null) {
 			return classes;
 		}
 
 		classes = split(getName(), CLASS_SEP);
 		return classes;
 	}
 
 	public String getLocalName(String binaryNamePart) {
 		int offset = 0;
 		for (; offset < binaryNamePart.length(); offset++) {
 			if (Character.isJavaIdentifierStart(binaryNamePart.charAt(offset))) {
 				break;
 			}
 		}
 		return binaryNamePart.substring(offset);
 	}
 
 	public Iterator<String> getPackagePathIterator() {
 		return new Iterator<String>() {
 			int pos;
 			int next;
 			public boolean hasNext() {
 				if (next == -1) { return false; }
 				next = getPackageFQName().indexOf(PACKAGE_SEP, pos);
 				return next != -1;
 			}
 
 			public String next() {
 				if (!hasNext()) { throw new NoSuchElementException(); }
 				pos = next+1;
 				next = getPackageFQName().indexOf(PACKAGE_SEP, pos);
 				return getPackageFQName().substring(0, next);
 			}
 
 			public void remove() {
 				throw Assert.unsupported();
 			}
 		};
 	}
 
 	protected List<String> split(String name, char separator) {
 		List<String> parts = new ArrayList<String>();
 		for (int offset=0; offset<name.length();) {
 			int i = name.indexOf(separator);
 			String part = (i != -1) ? name.substring(offset, i) : name.substring(offset);
 			parts.add(part);
 			offset = i+1;
 		}
 		return parts;
 	}
 
 	protected int countArrayDimenstions(Class cls) {
 		int count = 0;
 		while (cls.isArray()) {
 			count++;
 			cls = cls.getComponentType();
 		}
 		return count;
 	}
 
 	protected String dup(String pattern, int count) {
 		StringBuilder sb = new StringBuilder();
 		while (count-- > 0) {
 			sb.append(pattern);
 		}
 		return sb.toString();
 	}
 
 	public boolean isArray() {
 		return getDimensions()>0;
 	}
 
 	protected Class unwrapComponentClass(Class cls) {
 		Class c = cls;
 		while (c.isArray()) {
 			c = c.getComponentType();
 		}
 		return c;
 	}
 }
