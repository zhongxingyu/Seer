 /*
  * Copyright (C) 2013 midnightBITS
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use, copy,
  * modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
  
 package reflect.java;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
import reflect.CodeExceptions;

 class StringPair {
 	String value;
 	String nextToken;
 	StringPair(String val, String t)
 	{
 		value = val;
 		nextToken = t;
 	}
 	public String toString()
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append(value);
 		sb.append(" ");
 		sb.append(nextToken);
 		return sb.toString();
 	}
 }
 
 public abstract class SourceCodeParamsHint {
 
 	private String m_package;
 	private Vector<String> m_imports = new Vector<String>();
 	private static Map<String, String> s_builtins = new HashMap<String, String>();
 	
 	static {
 		s_builtins.put("boolean", "Z");
 		s_builtins.put("byte", "B");
 		s_builtins.put("char", "C");
 		s_builtins.put("short", "S");
 		s_builtins.put("int", "I");
 		s_builtins.put("long", "J");
 		s_builtins.put("float", "F");
 		s_builtins.put("double", "D");
 		s_builtins.put("void", "V");
 	}
 
 	public static String builtin(String typeName) {
 		if (s_builtins.containsKey(typeName))
 			return s_builtins.get(typeName);
 		return null;
 	}
 	protected abstract File getSourceRoot(String className);
 	protected abstract boolean hasClass(String className);
 	protected abstract ClassHinter getClass(String className);
 
 
 	private class Code extends Tokenizer {
 
 		Code(FileReader rd) throws IOException
 		{
 			super(rd);
 		}
 
 		private void skipTo(String tok) throws IOException {
 			String t = nextToken();
 			while (t != null && !t.equals(tok))
 				t = nextToken();
 		}
 
 		private void skipBlocksTo(String tok) throws IOException {
 			String t = nextToken();
 			while (t != null && !t.equals(tok))
 			{
 				if (t.equals("{") && !skipBlock()) return;
 				if (t.equals("<") && !skipGenerics()) return;
 				t = nextToken();
 			}
 		}
 
 		private boolean skipBlock(String open, String close) throws IOException {
 			boolean print = false;
 			int depth = 1;
 			String t = nextToken();
 			while (t != null)
 			{
 				if (print)
 					System.out.println(t);
 				if (t.equals(open)) ++depth;
 				if (t.equals(close))
 				{
 					if (--depth == 0)
 						return true;
 				}
 				t = nextToken();
 			}
 			return false;
 		}
 
 		private boolean skipBlock() throws IOException {
 			return skipBlock("{", "}");
 		}
 
 		private boolean skipGenerics() throws IOException {
 			return skipBlock("<", ">");
 		}
 
 		private boolean skipIndex() throws IOException {
 			return skipBlock("[", "]");
 		}
 
 		String ignoreAnnotation() throws IOException
 		{
 			nextToken();
 			String t = nextToken();
 			if (t.equals("("))
 			{
 				skipTo(")");
 				if (t != null) t = nextToken();
 			}
 			return t;
 		}
 
 		private boolean readPackage() throws IOException
 		{
 			m_package = nextToken();
 			if (m_package == null || m_package.equals(";"))
 			{
 				m_package = null;
 				return false;
 			}
 			skipTo(";");
 			return true;
 		}
 
 		private boolean readImport() throws IOException
 		{
 			final String imp = nextToken();
 			if (imp == null || imp.equals(";"))
 				return false;
 
 			addImport(imp);
 
 			skipTo(";");
 			return true;
 		}
 
 	}
 
 	private void addImport(String imp)
 	{
 		if (imp == null) return;
 		m_imports.add(imp);
 	}
 
 	private class GenericsReader {
 		protected Code m_tok;
 		protected String m_ctor;
 		protected String m_type;
 		protected Map<String, String> m_aliases = null;
 		GenericsReader(Code tok, String ctor, String type) {
 			m_tok = tok;
 			m_ctor = ctor;
 			m_type = type;
 		}
 
 		private String tryClassName(String className) {
 			return hasClass(className) ? "L" + className + ";" : null;
 		}
 
 		private String tryInheritance(String className, String subClass) {
 			try {
 				Class<?> cand = Class.forName(className);
 				if (cand != null)
 				{
 					String _cand = null;
 					Class<?> supah = cand.getSuperclass();
 					if (supah != null)
 					{
 						final String superClass = supah.getName();
 						_cand = tryClassName(superClass + "$" + subClass);
 						if (_cand == null) _cand = tryInheritance(superClass, subClass);
 						if (_cand != null) return _cand;
 					}
 					Class<?>[] ifaces = cand.getInterfaces();
 					for (Class<?> c: ifaces)
 					{
 						final String superClass = c.getName();
 						_cand = tryClassName(superClass + "$" + subClass);
 						if (_cand == null) _cand = tryInheritance(superClass, subClass);
 						if (_cand != null) return _cand;
 					}
 				}
 			} catch (ClassNotFoundException e) {
 			}
 			return null;
 		}
 
 		private String tryImport(String typeName) throws IOException {
 			String shortName = typeName.split("\\.")[0];
 			typeName = typeName.substring(shortName.length()).replace(".", "$");
 			shortName = "." + shortName;
 			for (String imp: m_imports)
 			{
 				if (imp.endsWith(shortName))
 				{
 					String quick = tryClassName(imp + typeName);
 					if (quick != null)
 						return quick;
 
 					//now, where is the package, and where are the classes...
 					return "L" + imp + typeName + ";"; // TODO: temporary
 					//m_tok.onError(typeName + " is not accessible from " + imp);
 				}
 				
 				if (imp.endsWith(".*"))
 				{
 					String cand = tryClassName(imp.substring(0, imp.length()-2) + shortName);
 					if (cand != null)
 						return cand;
 				}
 			}
 			return null;
 		}
 
 		private String tryInterface(String typeName) {
 			if (typeName.length() > 2 && typeName.charAt(0) == 'I' && Character.isUpperCase(typeName.charAt(1)))
 				return "!<iface>." + typeName.replace(".", "$");
 			return null;
 		}
 
 		protected String resolve(String typeName) throws IOException {
 			if (s_builtins.containsKey(typeName))
 				return s_builtins.get(typeName);
 			
 			if (m_aliases != null && m_aliases.containsKey(typeName))
 				return m_aliases.get(typeName);
 
 			String cand = null;
 			if (typeName.equals(m_ctor))
 				cand = "L" + m_type + ";";
 
 			if (cand == null)
 			{
 				String[] parts = typeName.split("\\.");
 				int i = 1;
 				while (cand == null && i < parts.length)
 				{
 					cand = tryClassName(typeName);
 					++i;
 				}
 			}
 			if (cand == null) cand = tryClassName(m_type + "$" + typeName.replace(".", "$"));
 			if (cand == null) cand = tryInheritance(m_type, typeName.replace(".", "$"));
 			if (cand == null) cand = tryClassName(m_package + "." + typeName.replace(".", "$"));
 			if (cand == null) cand = tryClassName("java.lang." + typeName.replace(".", "$"));
 			if (cand == null) cand = tryImport(typeName);
			if (cand == null) cand = CodeExceptions.get(m_type, typeName);
 			if (cand == null) cand = tryInterface(typeName);
 
 			if (cand == null)
 			{
 				return "!<unk>." + typeName.replace(".", "$");
 			}
 
 			return cand;
 		}
 
 		protected void setAlias(String generic, String resolved) {
 			if (m_aliases == null)
 				m_aliases = new HashMap<String, String>();
 			m_aliases.put(generic, resolved);
 		}
 
 		public boolean readGenerics() throws IOException {
 			String t = m_tok.nextToken();
 			while (t != null) {
 				if (t.equals(">"))
 					return true;
 
 				String alias = t;
 				
 				t = m_tok.nextToken(); // ">" "," "extends"
 				if (t.equals("extends")) {
 					String name = m_tok.nextToken();
 					final String resolved = resolve(name);
 					setAlias(alias, resolved == null ? "?" + name : resolved);
 					t = m_tok.nextToken();
 					if (t.equals("<"))
 					{
 						if (!m_tok.skipGenerics())
 							return false;
 						t = m_tok.nextToken();
 					}
 				} else {
 					setAlias(alias, "Ljava/lang/Object;");
 				}
 
 				if (t.equals(","))
 					t = m_tok.nextToken();
 				if (t.equals(">"))
 					return true;
 
 				t = m_tok.nextToken();
 			}
 			return false;
 		}
 	}
 
 	private class MethodReader extends GenericsReader {
 		private ClassHinter m_parent;
 
 		MethodReader(Code tok, ClassHinter parent, String ctor, String type, GenericsReader parent_reader) {
 			super(tok, ctor, type);
 			m_parent = parent;
 			if (parent_reader.m_aliases != null) {
 				for (Map.Entry<String, String> e: parent_reader.m_aliases.entrySet())
 					setAlias(e.getKey(), e.getValue());
 			}
 		}
 
 		private StringPair readType(String firstToken, String nextToken, boolean methodParam) throws IOException {
 			if (nextToken.equals("<"))
 			{
 				if (!m_tok.skipGenerics())
 					return new StringPair(null, null);
 				nextToken = m_tok.nextToken();
 			}
 
 			String resolved = resolve(firstToken);
 
 			StringBuilder sb = new StringBuilder();
 
 			if (resolved == null)
 				sb.append("?");
 
 			while (nextToken != null && nextToken.equals("["))
 			{
 				sb.append("[");
 				if (!m_tok.skipIndex())
 					return new StringPair(null, null);
 				nextToken = m_tok.nextToken();
 			}
 
 			int dots = 0;
 			// the StreamTokenizer will convert a "lone" dot into a 0.0 number
 			while (nextToken != null && (nextToken.equals(".") || nextToken.equals("0.0")))
 			{
 				++dots;
 				nextToken = m_tok.nextToken();
 			}
 
 			if (dots != 0 && dots != 3)
 				return new StringPair(null, null);
 			if (dots == 3) // the "T... param" becomes "[T"
 				sb.append("[");
 
 			if (methodParam)
 			{
 				String t = m_tok.nextToken();
 				int depth = 0;
 				while (t.equals("["))
 				{
 					depth++;
 					if (!m_tok.skipIndex())
 						return new StringPair(null, null);
 					t = m_tok.nextToken();
 				}
 				m_tok.pushBack();
 				while (depth-- > 0)
 					sb.append("[");
 			}
 
 			sb.append(resolved == null ? firstToken : resolved);
 			return new StringPair(sb.toString(), nextToken);
 		}
 
 		private StringPair readType(String firstToken, boolean methodParam) throws IOException {
 			String t = m_tok.nextToken();
 			if (t.equals("<") || t.equals("["))
 				return readType(firstToken, t, methodParam);
 			int dots = 0;
 			if (firstToken.endsWith("..."))
 			{
 				if (firstToken.endsWith("...."))
 					return new StringPair(null, null);
 
 				dots = 3;
 				firstToken = firstToken.substring(0, firstToken.length()-3);
 			}
 			String resolved = resolve(firstToken);
 
 			StringBuilder sb = new StringBuilder();
 			if (resolved == null)
 				sb.append("?");
 			if (methodParam)
 			{
 				String t1 = m_tok.nextToken();
 				int depth = 0;
 				while (t1.equals("["))
 				{
 					depth++;
 					if (!m_tok.skipIndex())
 						return new StringPair(null, null);
 					t1 = m_tok.nextToken();
 				}
 				m_tok.pushBack();
 				while (depth-- > 0)
 					sb.append("[");
 			}
 			if (dots == 3)
 				sb.append("[");
 			if (resolved != null)
 				sb.append(resolved);
 			else
 				sb.append(firstToken);
 
 			return new StringPair(sb.toString(), t);
 		}
 		
 		private StringPair readCtorOrSelfType() throws IOException {
 			StringPair _type = new StringPair("V", "<init>"); //assume it's a constructor
 			String t = m_tok.nextToken();
 			if (t.equals("<") || t.equals("[")) // definitely not a ctor
 				_type = readType(m_ctor, t, false);
 			else if (t.equals("(")) // ctor would now have a open parenthesis
 				m_tok.pushBack();
 			else
 				_type = new StringPair("L" + m_type + ";", t);
 			return _type;
 		}
 
 		private boolean readMethod(String retType, String name) throws IOException {
 			Vector<String> types = new Vector<String>();
 			Vector<String> names = new Vector<String>();
 			String t = m_tok.nextToken();
 			while (t != null)
 			{
 				if (t.equals(")"))
 				{
 					t = m_tok.nextToken();
 					while (t != null && !t.equals(";") && !t.equals("{"))
 						t = m_tok.nextToken();
 					if (t == null)
 						return false;
 					if (t.equals("{") && !m_tok.skipBlock())
 						return false;
 					MethodGroupHinter _group = m_parent.getMethodGroup(name);
 					if (_group == null)
 						return true;
 					MethodHinter _meth = _group.find(retType, types);
 					if (_meth != null)
 						_meth.setHints(names);
 					return true;
 				}
 
 				StringPair param = readType(t, true);
 				if (param.nextToken == null)
 					break;
 
 				if (param.nextToken.equals(",") || param.nextToken.equals(")"))
 				{
 					t = param.nextToken;
 					param.nextToken = null;
 				}
 				else
 				{
 					t = m_tok.nextToken();
 					if (t.equals(","))
 						t = m_tok.nextToken();
 				}
 
 				types.add(param.value);
 				names.add(param.nextToken);
 			}
 			return false;
 		}
 
 		public String read() throws IOException {
 			String currentToken = m_tok.nextToken();
     		StringPair _type;
 
     		if (currentToken.equals("<")) // it's the <? extends Xyz> ? function(? _x);
     		{
     			if (!readGenerics())
     				return null;
     			currentToken = m_tok.nextToken();
     		}
 
     		if (currentToken.equals(m_ctor)) //might be Type(...) or Type prop; or Type meth(...);
     		{
     			_type = readCtorOrSelfType();
     		}
     		else
     			_type = readType(currentToken, false);
 
     		if (_type.nextToken == null) return null;
 
    			currentToken = m_tok.nextToken();
 
     		if (currentToken == null)
     			return null;
 
     		if (currentToken.equals("("))
     		{
         		//System.out.println("    " + _type.toString() + " " + currentToken);
     			if (!readMethod(_type.value, _type.nextToken))
     				return null;
 
     			return m_tok.nextToken();
     		}
 
     		if (!currentToken.equals(";")) m_tok.skipBlocksTo(";");
     		
     		return m_tok.nextToken();
 			
 		}
 	}
 
 	private boolean readEnum(Code tok, String ctor, String typeName) throws IOException {
 		//System.out.println("    >>> Reading an enum " + ctor + " (" + typeName +")");
 		final String type;
 		if (m_package != null)
 			type = m_package + "." + typeName;
 		else
 			type = typeName;
 
 		GenericsReader class_generics = new GenericsReader(tok, ctor, type);
 		String t = tok.nextToken();
 		if (t.equals("<"))
 			class_generics.readGenerics();
 
 		if (!t.equals("{")) tok.skipTo("{");
 
 		//skip EnumConstants, if any
 		final String name = tok.nextToken();
 		if (name == null) return false;
 		if (name.equals("}")) return true;
 
 		t = tok.nextToken();
 		if (t == null) return false;
 		if (t.equals(";")) //only one enum, nothing fancy
 			return readClass(tok, ctor, typeName);
 
 		if (t.equals(",") || t.equals("(")) // skip to EnumBodyDeclarations, if any
 		{
 			if (!name.equals(ctor))
 			{
 				t = tok.nextToken();
 				while (t != null && !t.equals(";") && !t.equals("}"))
 				{
 					if (t.equals("{") && !tok.skipBlock()) return false;
 					if (t.equals("<") && !tok.skipGenerics()) return false;
 					t = tok.nextToken();
 				}
 				if (t.equals("}")) return true;
 				return readClassBody(tok, ctor, typeName, type, class_generics, tok.nextToken());
 			} // else we are already two tokens down the EnumBodyDeclarations.
 		}
 
 		// We are already two tokens down the EnumBodyDeclarations. restart?
 
 		tok.pushBack();
 		return readClassBody(tok, ctor, typeName, type, class_generics, name);
 	}
 
 	private boolean readClass(Code tok, String ctor, String typeName) throws IOException {
 		final String type;
 		if (m_package != null)
 			type = m_package + "." + typeName;
 		else
 			type = typeName;
 
 		GenericsReader class_generics = new GenericsReader(tok, ctor, type);
 		String t = tok.nextToken();
 		if (t.equals("<"))
 			class_generics.readGenerics();
 
 		if (!t.equals("{")) tok.skipTo("{");
 
 		return readClassBody(tok, ctor, typeName, type, class_generics, tok.nextToken());
 	}
 	private boolean readClassBody(Code tok, String ctor, String typeName, String type, GenericsReader class_generics, String firstToken) throws IOException {
 
 		ClassHinter _class = getClass(type);
 		if (_class == null)
 		{
 			if (!tok.skipBlock())
 				return false;
 			return true;
 		}
 
 		String t = firstToken;
 
 		while (t != null)
 		{
 			if (t.equals(";"))
 			{
 				t = tok.nextToken();
 				continue;
 			}
 
 			if (t.equals("}"))
 			{
 				_class.finished();
 				return true;
 			}
 
     		if (t.equals("@")) {
     			t = tok.ignoreAnnotation();
     			if (t == null) return false;
     		}
 
     		if (t.equals("enum"))
     		{
     			t = tok.nextToken();
     			if (t == null)
     				return false;
 
     			if (!readEnum(tok, t, typeName + "$" + t))
     				return false;
 
     			t = tok.nextToken();
     			if (t != null && t.equals(";")) t = tok.nextToken();
     			continue;
     		}
 
     		if (t.equals("class") || t.equals("interface"))
     		{
     			t = tok.nextToken();
     			if (t == null)
     				return false;
 
     			if (!readClass(tok, t, typeName + "$" + t))
     				return false;
 
     			t = tok.nextToken();
     			if (t != null && t.equals(";")) t = tok.nextToken();
     			continue;
     		}
 
     		if (t.equals("{")) // static { ... }
     		{
     			if (!tok.skipBlock())
     				return false;
     			t = tok.nextToken();
     			continue;
     		}
 
     		// a method or a property
     		// method:   <type> <name> "(" [<type> <name> ["," <type> <name>]*] ")" <block>
     		// property: <type> <name> [";" | "="]
 
     		tok.pushBack();
     		t = new MethodReader(tok, _class, ctor, type, class_generics).read();
 		}
 		return false;
 	}
 
 	public boolean read(File java) throws IOException
 	{
 	    FileReader rd = new FileReader(java);
 	    Code tok = new Code(rd);
 	    try {
 	    	String t = tok.nextToken();
 	    	while (t != null) {
 	    		if (t.equals("@")) {
 	    			t = tok.ignoreAnnotation();
 	    			if (t == null) return false;
 	    		}
 
 	    		if (t.equals("package")) {
 	    			if (!tok.readPackage())
 	    				return false;
 	    			t = tok.nextToken();
 	    			continue;
 	    		}
 
 	    		if (t.equals("import")) {
 	    			if (!tok.readImport())
 	    				return false;
 	    			t = tok.nextToken();
 	    			continue;
 	    		}
 	    		
 	    		if (t.equals("enum"))
 	    		{
 	    			t = tok.nextToken();
 	    			if (t == null)
 	    				return false;
 
 	    			if (!readEnum(tok, t, t))
 	    				return false;
 
 	    			t = tok.nextToken();
 	    			if (t != null && t.equals(";")) t = tok.nextToken();
 	    			continue;
 	    		}
 
 	    		if (t.equals("class") || t.equals("interface"))
 	    		{
 	    			t = tok.nextToken();
 	    			if (t == null)
 	    				return false;
 
 	    			if (!readClass(tok, t, t))
 	    				return false;
 
 	    			t = tok.nextToken();
 	    			if (t != null && t.equals(";")) t = tok.nextToken();
 	    			continue;
 	    		}
 
 	    		t = tok.nextToken();
 	    	}
 	    } finally {
 	    	rd.close();
 	    }
 		return true;
 	}
 
 	public void getHints(String className) {
 
 		File java = null;
 		final File root = getSourceRoot(className);
 		if (root != null)
 		{
 			String name = className.split("\\$")[0].replace('.', File.separatorChar) + ".java";
 			java = new File(root, name);
 		}
 
 		if (java == null || !java.isFile())
 			return;
 
 		try {
 			if (!read(java))
 				return;
 		} catch (IOException ex) {
 			return;
 		}
 	}
 }
