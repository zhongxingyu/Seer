 package de.uni_koblenz.jgralab.codegenerator;
 
 
 import java.util.Set;
 import java.util.TreeSet;
 
 import de.uni_koblenz.jgralab.Direction;
 import de.uni_koblenz.jgralab.schema.Attribute;
 import de.uni_koblenz.jgralab.schema.GraphElementClass;
 import de.uni_koblenz.jgralab.schema.IncidenceClass;
 import de.uni_koblenz.jgralab.schema.VertexClass;
 
 public abstract class GraphElementCodeGenerator<MetaClass extends GraphElementClass<MetaClass, ?,?,?>> extends AttributedElementCodeGenerator<MetaClass> {
 
 
 	protected RolenameCodeGenerator<MetaClass> rolenameGenerator;
 	
 	public GraphElementCodeGenerator(MetaClass metaClass, String schemaPackageName,
 			CodeGeneratorConfiguration config, boolean createVertexClass) {
 		super(metaClass, schemaPackageName, config);
 		if (createVertexClass) {
 			rootBlock.setVariable("ownElementClass", "Vertex");	
 			rootBlock.setVariable("dualElementClass", "Edge");	
 		} else {
 			rootBlock.setVariable("ownElementClass", "Edge");
 			rootBlock.setVariable("dualElementClass", "Vertex");	
 		}
 		rolenameGenerator = new RolenameCodeGenerator<MetaClass>(metaClass, false);
 	}
 	
 	protected boolean hasProxySupport() {
 		return true;
 	}
 
 	
 
 	
 	
 	@Override
 	protected CodeBlock createConstructor() {
 		CodeList code = new CodeList();
 		addImports("#jgPackage#.#ownElementClass#");
 		switch (currentCycle) {
 		case MEMORYBASED:
 			code.setVariable("graphOrDatabase", "#jgPackage#.Graph");
 			break;
 		case DISKBASED:
 		case DISKPROXIES:
 			code.setVariable("graphOrDatabase", "#jgDiskImplPackage#.GraphDatabaseBaseImpl");
 			break;
 		case DISKV2BASED:
 		case DISKV2PROXIES:
			code.setVariable("graphOrDatabase", "#jgDiskv2ImplPackage#.GraphDatabaseBaseImpl");
 			break;
 		case DISTRIBUTED:
 		case DISTRIBUTEDPROXIES:
 			code.setVariable("graphOrDatabase", "#jgDistributedImplPackage#.GraphDatabaseBaseImpl");
 			break;
 		default:
 			throw new RuntimeException("Unhandled case");
 		}
 		code.setVariable("additionalProxyFormalParams", currentCycle.isImplementationVariant() ? "" : ", #jgImplPackage#.RemoteGraphDatabaseAccess remoteDb");
 		code.setVariable("additionalProxyActualParams", currentCycle.isImplementationVariant() ? "" : ",remoteDb");
 		code.addNoIndent(new CodeSnippet(
 						true,
 						"public #simpleClassName##implOrProxy#(long id, #graphOrDatabase# g#additionalProxyFormalParams#) throws java.io.IOException {",
 						"\tsuper(" + (currentCycle.isMembasedImpl()?"(int)":"") + " id, g#additionalProxyActualParams#);"));
 		if (currentCycle.isDiskbasedImpl())
 			code.addNoIndent(new CodeSnippet("\tattributeContainer = new InnerAttributeContainer();"));
 		if (hasDefaultAttributeValues()) {
 			code.addNoIndent(new CodeSnippet("\tinitializeAttributesWithDefaultValues();"));
 		}
 		code.add(createSpecialConstructorCode());
 		code.addNoIndent(new CodeSnippet("}"));
 		return code;
 	}
 	
 
 	
 	protected CodeSnippet createIncidenceClassCommentInHeader(IncidenceClass ic) {
 		CodeSnippet snippet = new CodeSnippet();
 		snippet.add(" *   Role: '#rolename#', ConnectedClass: '#gecName#");
 		snippet.setVariable("rolename", ic.getRolename());
 		snippet.setVariable("gecName", ic.getOtherGraphElementClass(aec).getQualifiedName());
 		return snippet;
 	}
 	
 	
 	
 	/**
 	 * creates the body of the class file, that are methods and attributes
 	 */
 	@Override
 	protected CodeList createBody() {
 		CodeList code = (CodeList) super.createBody();
 		if (currentCycle.isDiskbasedImpl()) {
 			code.add(createGetIncidenceClassForRolenameMethod());
 			code.addNoIndent(createLoadAttributeContainer());
 			code.add(createWriteAttributesMethod(aec.getAttributeList(), "attributeContainer."));
 			code.add(createWriteAttributeToStringMethod(aec.getAttributeList(), "attributeContainer."));
 			code.add(createReadAttributesMethod(aec.getAttributeList(), "attributeContainer."));
 			code.add(createReadAttributesFromStringMethod(aec.getAttributeList(), "attributeContainer."));
 		}	
 		if (currentCycle.isMembasedImpl() || currentCycle.isDistributedImpl() || currentCycle.isDiskv2basedImpl()) {
 			code.add(createGetIncidenceClassForRolenameMethod());
 			code.add(createWriteAttributesMethod(aec.getAttributeList(), ""));
 			code.add(createWriteAttributeToStringMethod(aec.getAttributeList(), ""));
 			code.add(createReadAttributesMethod(aec.getAttributeList(), ""));
 			code.add(createReadAttributesFromStringMethod(aec.getAttributeList(), ""));
 		}
 		if (currentCycle.isProxies()) {
 			code.add(createGetIncidenceClassForRolenameMethod());
 		}
 		if (config.hasTypeSpecificMethodsSupport() && !currentCycle.isClassOnly()) {
 			code.add(createNextMethods());
 			code.add(createFirstIncidenceMethods());
 
 		}
 		code.add(createCompatibilityMethods());
 		return code;
 	}
 	
 	
 	protected CodeBlock createCompatibilityMethods() {
 		return null;
 	}
 
 	protected abstract CodeBlock createLoadAttributeContainer();
 
 	
 	protected CodeBlock createFields(Set<Attribute> attrSet) {
 		CodeList code = new CodeList();
 		if (currentCycle.isDiskbasedImpl())
 			code.add(new CodeSnippet("class InnerAttributeContainer extends #jgDiskImplPackage#.AttributeContainer {"));
 		for (Attribute attr : attrSet) {
 			code.add(createField(attr));
 		}
 		if (currentCycle.isDiskbasedImpl()) {
 			code.add(new CodeSnippet("}"));
 			code.add(new CodeSnippet("InnerAttributeContainer attributeContainer = null;"));
 			code.add(new CodeSnippet("public InnerAttributeContainer getAttributeContainer() {",
 										"\treturn attributeContainer;",
 									 "}"));
 		}	
 		if (currentCycle.isProxies()) {
 			code.add(new CodeSnippet("}"));
 			code.add(new CodeSnippet("public InnerAttributeContainer getAttributeContainer() {",
 										"\tthrow new UnsupportedOperationException();",
 									 "}"));
 		}	
 		return code;
 	}
 	
 
 	protected CodeBlock createGetter(Attribute attr) {
 		CodeSnippet code = new CodeSnippet(true);
 		code.setVariable("name", attr.getName());
 		code.setVariable("type", attr.getDomain()
 				.getJavaAttributeImplementationTypeName(schemaRootPackageName));
 		code.setVariable("typeClass", attr.getDomain()
 				.getJavaClassName(schemaRootPackageName));
 		code.setVariable("isOrGet",
 				attr.getDomain().getJavaClassName(schemaRootPackageName)
 						.equals("Boolean") ? "is" : "get");
 
 		switch (currentCycle) {
 		case ABSTRACT:
 			code.add("public #type# #isOrGet#_#name#();");
 			break;
 		case MEMORYBASED:
 		case DISKV2BASED:
 		case DISTRIBUTED:
 			code.add("public #type# #isOrGet#_#name#()  {",
 					 "\treturn _#name#;",
 					 "}");
 			break;
 		case DISKBASED:
 			code.add("public #type# #isOrGet#_#name#()  {",
 					 "\tif (attributeContainer == null) {",
 					 "\t\tattributeContainer = loadAttributeContainer();",
 					 "\t}",
 					 "\treturn attributeContainer._#name#;",
 					 "}");
 			break;
 		case DISTRIBUTEDPROXIES:	
 		case DISKPROXIES:
 		case DISKV2PROXIES:
 			code.add(
 					"@SuppressWarnings(\"unchecked\")",
 					"public #type# #isOrGet#_#name#()  {",
 					"\ttry {",
 					 "\t\treturn (#typeClass#) storingGraphDatabase.get#edgeOrVertex#Attribute(elementId, \"#name#\");",
 					 "\t} catch (java.rmi.RemoteException ex) {",
 					 "\t\tthrow new RuntimeException(ex);",
 					 "\t}",
 					 "}");
 			break;
 		}
 		return code;
 	}
 
 	protected CodeBlock createSetter(Attribute attr) {
 		CodeSnippet code = new CodeSnippet(true);
 		code.setVariable("name", attr.getName());
 		code.setVariable("type", attr.getDomain()
 				.getJavaAttributeImplementationTypeName(schemaRootPackageName));
 		code.setVariable("dname", attr.getDomain().getSimpleName());
 
 		switch (currentCycle) {
 		case ABSTRACT:
 			code.add("public void set_#name#(#type# _#name#);");
 			break;
 		case DISTRIBUTED:	
 		case MEMORYBASED:
 		case DISKV2BASED:
 			code.add("public void set_#name#(#type# new_#name#) {",
 					 "\t_#name# = new_#name#;", 
 					 "\tgraphModified();", "}");
 			break;
 		case DISKBASED:
 			code.add("public void set_#name#(#type# _#name#) {",
 					 "\tif (attributeContainer == null) {",
 					 "\t\tattributeContainer = loadAttributeContainer();",
 					 "\t}",
 					 "\tattributeContainer._#name# = _#name#;", 
 					 "\tgraphModified();", "}");
 			break;
 		case DISTRIBUTEDPROXIES:	
 		case DISKPROXIES:
 		case DISKV2PROXIES:
 			code.add("public void set_#name#(#type# _#name#)  {",
 					 "\ttry {",
 					 "\t\tstoringGraphDatabase.set#edgeOrVertex#Attribute(elementId, \"#name#\", _#name#);",
 					 "\t} catch (java.rmi.RemoteException ex) {",
 					 "\t\tthrow new RuntimeException(ex);",
 					 "\t}",
 					 "}");
 			break;
 		}
 		return code;
 	}
 	
 	
 	
 	private CodeBlock createGetIncidenceClassForRolenameMethod() {
 		addImports("#jgSchemaPackage#.exception.SchemaException");
 		addImports("#jgSchemaPackage#.IncidenceClass");
 		CodeList code = new CodeList();
 		CodeSnippet snippet = new CodeSnippet();
 		snippet.add("@Override");
 		snippet.add("public IncidenceClass getIncidenceClassForRolename(String rolename) {");
 		code.addNoIndent(snippet);
 		for (IncidenceClass ic : aec.getAllIncidenceClasses()) {
 			if (!ic.isInternal()) {
 				snippet = new CodeSnippet();
 				snippet.setVariable("rolename", ic.getRolename());
 				snippet.setVariable("schemaVariable", ic.getVariableName());
 				snippet.add("\tif (rolename.equals(\"#rolename#\"))");
 				snippet.add("\t\t return #schemaPackageName#.#schemaName#.instance().#schemaVariable#;");
 				code.addNoIndent(snippet);
 			}	
 		}
 		snippet = new CodeSnippet();
 		snippet.add("\tthrow new SchemaException(\"There is no incidence class with rolename \"+rolename+\"at this element!\");");
 		snippet.add("}");
 		code.addNoIndent(snippet);
 
 		
 		return code;
 	}
 	
 
 	private CodeBlock createFirstIncidenceMethods() {
 		CodeList code = new CodeList();
 		//Iterate all IncidenceClasses known at this vertex class
 		for (IncidenceClass ic : aec.getAllIncidenceClasses()) {
 			boolean incidenceDefinedForExactlyThisGraphElementClass = ic.getConnectedGraphElementClassOfOwnType(aec) == aec;
 			boolean incidenceDefinedForVertexAsDirectSuperclass = (ic.getConnectedGraphElementClassOfOwnType(aec) == aec.getDefaultClass()) 
 			        && (aec.getDefaultClass().isDirectSuperClassOf(aec));
 			//if incidence class is connected to exactly this vertex or to vertex and this class is a direct subclass of vertex or if the 
 			//implementation should be created
 			boolean createMethod = (!currentCycle.isAbstract()) || incidenceDefinedForExactlyThisGraphElementClass || incidenceDefinedForVertexAsDirectSuperclass;
 
 			if (createMethod) {
 				//addImports("#jgPackage#.Direction");
 				if (config.hasTypeSpecificMethodsSupport()) {
 					if (!ic.isInternal()) {
 						code.addNoIndent(createFirstIncidenceMethod(ic));
 						code.addNoIndent(createIncidenceIteratorMethod(ic));
 					}	
 				}
 			}
 		}
 		return code;
 	}
 	
 		
 	
 	
 	private CodeBlock createFirstIncidenceMethod(IncidenceClass ic) {
 		CodeSnippet s = new CodeSnippet();
 		s.setVariable("incidenceClassName", ic.getRolename());
 		s.setVariable("qualifiedIncidenceClassName", schemaRootPackageName + "." +  ic.getQualifiedName());
 		if (currentCycle.isAbstract()) {
 			//create interface
 			s.add("/*",
 				  " * @return the first #incidenceClassName# incidence at this #ownElementClass# ");
 			s.add("*/",
 				  "public #qualifiedIncidenceClassName# getFirst_#incidenceClassName#();"); 	
 		} else {
 			s.add("@Override",
 			     "public #qualifiedIncidenceClassName# getFirst_#incidenceClassName#() {");
 			s.add("\treturn getFirstIncidence(#qualifiedIncidenceClassName#.class);");
 			s.add("}");
 			
 		}
 		return s;
 	}
 	
 	
 	/**
 	 * Creates <code>getNext#ownElementClass#ClassName()</code> methods
 	 * @return the CodeBlock that contains the methods
 	 */
 	private CodeBlock createNextMethods() {
 		CodeList code = new CodeList();
 
 		TreeSet<MetaClass> superClasses = new TreeSet<MetaClass>();
 		superClasses.addAll(aec.getAllSuperClasses());
 		superClasses.add(aec);
 
 		if (config.hasTypeSpecificMethodsSupport()) {
 			for (MetaClass sc : superClasses) {
 				if (sc.isInternal()) {
 					continue;
 				}
 				code.addNoIndent(createNextMethod(sc, false));
 				if (config.hasMethodsForSubclassesSupport()) {
 					if (!sc.isAbstract()) {
 						code.addNoIndent(createNextMethod(sc, true));
 					}
 				}
 			}
 		}
 		return code;
 	}
 	
 	
 	/**
 	 * Creates <code>getNext#ownElementClass#ClassName()</code> method for given
 	 * #ownElementClass#
 	 * 
 	 * @param createClass
 	 *            if set to true, the method bodies will also be created
 	 * @param withTypeFlag
 	 *            toggles if the "no subclasses"-parameter will be created
 	 * @return the CodeBlock that contains the method
 	 */
 	private CodeBlock createNextMethod(MetaClass mc, boolean withTypeFlag) {
 		CodeSnippet code = new CodeSnippet(true);
 		code.setVariable("mcQualifiedName", absoluteName(mc));
 		code.setVariable("mcCamelName", camelCase(mc.getUniqueName()));
 		code.setVariable("formalParams", (withTypeFlag ? "boolean noSubClasses"	: ""));
 		code.setVariable("actualParams", (withTypeFlag ? ", noSubClasses" : ""));
 
 		if (currentCycle.isAbstract()) {
 			code.add("/**",
 					 " * @return the next #mcQualifiedName# #ownElementClass# in the global #ownElementClass# sequence");
 			if (withTypeFlag) {
 				code.add(" * @param noSubClasses if set to <code>true</code>, no subclasses of #mcName# are accepted");
 			}
 			code.add(" */",
 					 "public #mcQualifiedName# getNext#mcCamelName#(#formalParams#);");
 		}
 		if (currentCycle.isImplementationVariant() || currentCycle.isProxies()) {
 			code.add("@Override",
 					 "public #mcQualifiedName# getNext#mcCamelName#(#formalParams#) {",
 					 "\treturn (#mcQualifiedName#)getNext#ownElementClass#(#mcQualifiedName#.class#actualParams#);",
 					 "}");
 		}
 		return code;
 	}
 	
 
 	protected abstract CodeBlock createIncidenceIteratorMethod(IncidenceClass ic);
 
 
 	
 }
