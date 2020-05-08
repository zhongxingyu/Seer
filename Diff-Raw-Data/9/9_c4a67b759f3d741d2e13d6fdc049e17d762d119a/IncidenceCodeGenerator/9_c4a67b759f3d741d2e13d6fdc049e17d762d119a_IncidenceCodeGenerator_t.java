 package de.uni_koblenz.jgralab.codegenerator;
 
 
 import java.util.TreeSet;
 
 import de.uni_koblenz.jgralab.Direction;
 import de.uni_koblenz.jgralab.schema.IncidenceClass;
 
 public class IncidenceCodeGenerator extends TypedElementCodeGenerator<IncidenceClass> {
 
 	
 	public IncidenceCodeGenerator(IncidenceClass metaClass, String schemaPackageName,
 			CodeGeneratorConfiguration config) {
 		super(metaClass,schemaPackageName, metaClass.getPackageName(), config);
 		rootBlock.setVariable("connectedVertexClass", absoluteName(metaClass.getVertexClass()));
 		rootBlock.setVariable("connectedEdgeClass", absoluteName(metaClass.getEdgeClass()));
 		rootBlock.setVariable("baseClassName", "IncidenceImpl");
 		rootBlock.setVariable("proxyClassName", "IncidenceProxy");
 		rootBlock.setVariable("graphElementClass", "Incidence");
 		rootBlock.setVariable("ownElementClass", "Incidence");
 	//	System.out.println("Create incidence class code " + metaClass.getFileName());
 		interfaces.add("Incidence");
 	}
 
 	
 	protected boolean hasProxySupport() {
 		return true;
 	}
 	
 	@Override
 	protected CodeBlock createHeader() {
 		CodeList code = new CodeList();
 		CodeSnippet snippet = new CodeSnippet();
 		snippet.add("/**");
 		snippet.add(" * Connects #inClass# (#inMin#, #inMax#) with #outClass# (#outMin#,#outMax#)");
 		snippet.add(" */");
 		if (aec.getDirection() == Direction.VERTEX_TO_EDGE) {
 			snippet.setVariable("inClass", aec.getVertexClass().getQualifiedName());
 			snippet.setVariable("inMin", Integer.toString(aec.getMinVerticesAtEdge()));
 			snippet.setVariable("inMax", Integer.toString(aec.getMaxVerticesAtEdge()));
 			snippet.setVariable("outClass", aec.getEdgeClass().getQualifiedName());
 			snippet.setVariable("outMin", Integer.toString(aec.getMinEdgesAtVertex()));
 			snippet.setVariable("outMax", Integer.toString(aec.getMaxEdgesAtVertex()));
 		} else {
 			snippet.setVariable("inClass", aec.getEdgeClass().getQualifiedName());
 			snippet.setVariable("inMin", Integer.toString(aec.getMinEdgesAtVertex()));
 			snippet.setVariable("inMax", Integer.toString(aec.getMaxEdgesAtVertex()));
 			snippet.setVariable("outClass", aec.getVertexClass().getQualifiedName());
 			snippet.setVariable("outMin", Integer.toString(aec.getMinVerticesAtEdge()));
 			snippet.setVariable("outMax", Integer.toString(aec.getMaxVerticesAtEdge()));
 		}
 
 		code.addNoIndent(snippet);
 		code.addNoIndent(super.createHeader());
 		return code;
 	}
 	
 	
 	@Override
 	protected CodeBlock createConstructor() {
 		if (currentCycle.isMembasedImpl()) {
 			return createInMemoryConstructor();
 		} else {
 			return createDiskBasedConstructor();
 		}
 	}
 	
 	private CodeBlock createInMemoryConstructor() {
 		CodeList code = new CodeList();
 
 		addImports("#usedJgImplPackage#.VertexImpl");
 		addImports("#usedJgImplPackage#.EdgeImpl");
 		addImports("#jgPackage#.Edge");
 		addImports("#jgPackage#.Vertex");
 		addImports("#jgPackage#.Direction");
 		code.setVariable("implOrProxy", currentCycle.isMemOrDiskImpl() ? "Impl" : "Proxy");
 		code.addNoIndent(new CodeSnippet(
 						true,
 						"public #simpleClassName##implOrProxy#(int id, Vertex vertex, Edge edge) {",
						"\tsuper(id, (VertexImpl)vertex, (EdgeImpl)edge);",
 						"}"));
 		return code;
 	}
 
 	private CodeBlock createDiskBasedConstructor() {
 		CodeList code = new CodeList();
 
 		addImports("#usedJgImplPackage#.VertexImpl");
 		addImports("#usedJgImplPackage#.EdgeImpl");
 		addImports("#jgPackage#.Edge");
 		addImports("#jgPackage#.Vertex");
 		addImports("#jgPackage#.Direction");
 		code.setVariable("implOrProxy", currentCycle.isMemOrDiskImpl() ? "Impl" : "Proxy");
 
 		code.addNoIndent(new CodeSnippet(
 						true,
 						"public #simpleClassName##implOrProxy#(GraphDatabaseBaseImpl localGraphDatabase, long globalId, long vertexId, long edgeId) {",
 						"\tsuper(localGraphDatabase, globalId, vertexId, edgeId);"));
 		code.addNoIndent(new CodeSnippet("}"));
 		if (currentCycle.isDiskbasedImpl()) {
 			code.addNoIndent(new CodeSnippet("/** Constructor only to be used by Background-Storage backend */"));
 			code.addNoIndent(new CodeSnippet(
 				true,
 				"public #simpleClassName#Impl(GraphDatabaseBaseImpl localGraphDatabase, long globalId, #jgDiskImplPackage#.IncidenceContainer container) {",
 				"\tsuper(localGraphDatabase, globalId, container);",
 				"}"));
 		}
 		return code;
 	}
 	
 	
 	
 	/**
 	 * creates the body of the class file, that are methods and attributes
 	 */
 	@Override
 	protected CodeList createBody() {
 		CodeList code = (CodeList) super.createBody();
 
 		if (config.hasTypeSpecificMethodsSupport() && !currentCycle.isClassOnly()) {
 			code.add(createNextMethods());
 		}
 		code.add(createGetDirectionMethod());
 		return code;
 	}
 
 	
 	
 	
 	/**
 	 * Creates <code>getNext#ownElementClass#ClassName()</code> methods
 	 * @return the CodeBlock that contains the methods
 	 */
 	private CodeBlock createNextMethods() {
 		CodeList code = new CodeList();
 
 		TreeSet<IncidenceClass> superClasses = new TreeSet<IncidenceClass>();
 		superClasses.addAll(aec.getAllSuperClasses());
 		superClasses.add(aec);
 
 		if (config.hasTypeSpecificMethodsSupport()) {
 			for (IncidenceClass sc : superClasses) {
 				if (sc.isInternal()) {
 					continue;
 				}
 				code.addNoIndent(createNextMethod(sc, true, false));
 				code.addNoIndent(createNextMethod(sc, false, false));
 				if (config.hasMethodsForSubclassesSupport()) {
 					if (!sc.isAbstract()) {
 						code.addNoIndent(createNextMethod(sc, true, true));
 						code.addNoIndent(createNextMethod(sc, false, true));
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
 	 * @param atVertex 
 	 *            toggles if "getNextXYIncidenceAtVertex" or "getNextXYIncidenceAtEdge"
 	 *            methods should be created
 	 * @param withTypeFlag
 	 *            toggles if the "no subclasses"-parameter will be created
 	 * @return the CodeBlock that contains the method
 	 */
 	private CodeBlock createNextMethod(IncidenceClass mc, boolean atVertex, boolean withTypeFlag) {
 		CodeSnippet code = new CodeSnippet(true);
 		code.setVariable("mcQualifiedName", mc.getQualifiedName());
 		code.setVariable("mcFileName", absoluteName(mc));
 		code.setVariable("mcCamelName", camelCase(mc.getRolename()));
 		code.setVariable("formalParams", (withTypeFlag ? "boolean noSubClasses"	: ""));
 		code.setVariable("actualParams", (withTypeFlag ? ", noSubClasses" : ""));
 		code.setVariable("connectedElement", atVertex ? "Vertex" : "Edge" );
 
 		if (currentCycle.isAbstract()) {
 			code.add("/**",
 					 " * @return the next #mcQualifiedName# incidence in the Lambda-sequence of the #connectedElement#");
 			if (withTypeFlag) {
 				code.add(" * @param noSubClasses if set to <code>true</code>, no subclasses of #mcName# are accepted");
 			}
 			code.add(" */",
 					 "public #mcFileName# getNext#mcCamelName#At#connectedElement#(#formalParams#);");
 		}
 		if (currentCycle.isMemOrDiskImpl()) {
 			code.add("@Override",
 					 "public #mcFileName# getNext#mcCamelName#At#connectedElement#(#formalParams#) {",
 					 "\treturn (#mcFileName#)getNextIncidenceAt#connectedElement#(#mcFileName#.class#actualParams#);",
 					 "}");
 		}
 		return code;
 	}
 
 
 	private CodeBlock createGetDirectionMethod() {
 		CodeSnippet code = new CodeSnippet(true);
 		code.setVariable("direction", aec.getDirection().toString());
 		if (currentCycle.isMemOrDiskImpl()) {
 			code.add("@Override",
 					 "public Direction getDirection() {",
 					 "\treturn #direction#;",
 					 "}");
 		}
 		return code;
 	}
 	
 	
 }
