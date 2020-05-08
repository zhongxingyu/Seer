 /*
  * JGraLab - The Java Graph Laboratory
  * 
  * Copyright (C) 2006-2010 Institute for Software Technology
  *                         University of Koblenz-Landau, Germany
  *                         ist@uni-koblenz.de
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 3 of the License, or (at your
  * option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses>.
  * 
  * Additional permission under GNU GPL version 3 section 7
  * 
  * If you modify this Program, or any covered work, by linking or combining
  * it with Eclipse (or a modified version of that program or an Eclipse
  * plugin), containing parts covered by the terms of the Eclipse Public
  * License (EPL), the licensors of this Program grant you additional
  * permission to convey the resulting work.  Corresponding Source for a
  * non-source form of such a combination shall include the source code for
  * the parts of JGraLab used as well as that of the covered work.
  */
 
 package de.uni_koblenz.jgralab.codegenerator;
 
 import java.util.Collection;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import de.uni_koblenz.jgralab.Direction;
 import de.uni_koblenz.jgralab.schema.Attribute;
 import de.uni_koblenz.jgralab.schema.BinaryEdgeClass;
 import de.uni_koblenz.jgralab.schema.EdgeClass;
 import de.uni_koblenz.jgralab.schema.GraphClass;
 import de.uni_koblenz.jgralab.schema.GraphElementClass;
 import de.uni_koblenz.jgralab.schema.IncidenceClass;
 import de.uni_koblenz.jgralab.schema.RecordDomain;
 import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
 import de.uni_koblenz.jgralab.schema.VertexClass;
 
 /**
  * TODO add comment
  * 
  * @author ist@uni-koblenz.de
  * 
  */
 public class SubordinateGraphCodeGenerator extends AttributedElementCodeGenerator<GraphClass> {
 
 	public SubordinateGraphCodeGenerator(GraphClass graphClass, String schemaPackageName,
 			String schemaName, CodeGeneratorConfiguration config) {
 		super(graphClass, schemaPackageName, config);
 		rootBlock.setVariable("graphElementClass", "Graph");
 		rootBlock.setVariable("schemaName", schemaName);
 		rootBlock.setVariable("theGraph", "this");
 		rootBlock.setVariable("simpleImplClassName", rootBlock.getVariable("simpleClassName") + "SubordinateImpl");
 		//rootBlock.setVariable("simpleClassName", rootBlock.getVariable("simpleClassName") + "Subordinate");
 	}
 	
 	public Vector<JavaSourceFromString> createJavaSources() {
 		String implClassName = rootBlock.getVariable("simpleImplClassName");
 		Vector<JavaSourceFromString> javaSources = new Vector<JavaSourceFromString>(2);
 
 		currentCycle = getNextCycle();
 		while (currentCycle != null) {
 			createCode();
 			if (currentCycle.isMemOrDiskImpl()) {
 				System.out.println("Writing code to file " + implClassName);
 				javaSources.add(new JavaSourceFromString(implClassName,
 						rootBlock.getCode()));
 				System.out.println("Written‚ code to file " + implClassName);
 			} 
 			currentCycle = getNextCycle();
 		}
 		return javaSources;
 	}
 
 	@Override
 	protected CodeBlock createHeader() {
 		return super.createHeader();
 	}
 
 	@Override
 	protected CodeList createBody() {
 		CodeList code = (CodeList) super.createBody();
 		if (currentCycle.isMemOrDiskImpl()) {
 			addImports("#usedJgImplPackage#.#baseClassName#");
 			rootBlock.setVariable("baseClassName", "SubordinateGraphImpl");
 		}
 		code.add(createGraphElementClassMethods());
 		code.add(createIteratorMethods());
 		code.add(createCreateRecordsMethods());
 		return code;
 	}
 
 	/**
 	 * Create "create"-methods for each RecordDomain defined in the schema.
 	 * 
 	 * @param createClass
 	 * @return
 	 */
 	private CodeBlock createCreateRecordsMethods() {
 		CodeList code = new CodeList();
 
 		if (currentCycle.isAbstract()) {
 			if (aec.getSchema().getRecordDomains().size() > 0) {
 				addImports("java.util.Map");
 				addImports("#jgPackage#.GraphIO");
 				addImports("#jgPackage#.GraphIOException");
 			}
 			for (RecordDomain rd : aec.getSchema().getRecordDomains()) {
 				CodeSnippet cs = new CodeSnippet(true);
 				cs.add("public #rcname# create#rname#(GraphIO io) throws GraphIOException;");
 				cs.add("");
 
 				cs.add("public #rcname# create#rname#(Map<String, Object> fields);");
 				cs.add("");
 
 				cs.add("public #rcname# create#rname#(#parawtypes#);");
 				cs.setVariable("parawtypes", buildParametersOutput(rd.getComponents(), true));
 				cs.setVariable("rcname", rd.getJavaClassName(schemaRootPackageName));
 				cs.setVariable("rname", rd.getUniqueName());
 				cs.add("");
 				code.addNoIndent(cs);
 			}
 		}
 
 		if (currentCycle.isMemOrDiskImpl()) {
 			if (aec.getSchema().getRecordDomains().size() > 0) {
 				addImports("java.util.Map");
 			}
 			for (RecordDomain rd : aec.getSchema().getRecordDomains()) {
 				CodeSnippet cs = new CodeSnippet(true);
 				cs.add("public #rcname# create#rname#(GraphIO io) throws GraphIOException {");
 				cs.add("\treturn getCompleteGraph().create#rname#(io);");
 				cs.add("}");
 				cs.add("");
 
 				cs.add("public #rcname# create#rname#(Map<String, Object> fields) {");
 				cs.add("\treturn getCompleteGraph().create#rname#(fields);");
 				cs.add("}");
 				cs.add("");
 
 				cs.setVariable("parawtypes", buildParametersOutput(rd.getComponents(), true));
 				cs.setVariable("parawotypes", buildParametersOutput(rd.getComponents(), false));
 
 				cs.add("");
 				cs.add("public #rcname# create#rname#(#parawtypes#) {");
 				cs.add("\treturn getCompleteGraph().create#rname#(#parawtypes#);");
 				cs.add("}");
 				cs.add("");
 
 				cs.setVariable("rcname", rd.getJavaClassName(schemaRootPackageName));
 				cs.setVariable("rname", rd.getUniqueName());
 				cs.setVariable("rtype",	rd.getJavaAttributeImplementationTypeName(schemaRootPackageName));
 				cs.setVariable("rstdtype",rd.getStandardJavaAttributeImplementationTypeName(schemaRootPackageName));
 				cs.setVariable("rsavememtype",rd.getSavememJavaAttributeImplementationTypeName(schemaRootPackageName));
 				code.addNoIndent(cs);
 			}
 		}
 		return code;
 	}
 
 	/**
 	 * 
 	 * @param components
 	 * @param withTypes
 	 * @return
 	 */
 	private String buildParametersOutput(
 			Collection<RecordComponent> components, boolean withTypes) {
 		StringBuilder parameters = new StringBuilder();
 		int count = 0;
 		int size = components.size();
 		for (RecordComponent entry : components) {
 			parameters.append(
 					(withTypes ? entry.getDomain()
 							.getJavaAttributeImplementationTypeName(
 									schemaRootPackageName) : "")).append(" _")
 					.append(entry.getName());
 			count++;
 			if (size != count) {
 				parameters.append(", ");
 			}
 		}
 		return parameters.toString();
 	}
 
 	@Override
 	protected CodeBlock createConstructor() {
 		if (currentCycle.isMembasedImpl()) {
 			return createInMemoryConstructor();
 		} else {
 			return createDiskBasedConstructor();
 		}
 	}	
 	
 	private CodeBlock createDiskBasedConstructor() {
 		addImports("#schemaPackageName#.#schemaName#");
 		addImports("#jgDiskImplPackage#.GraphDatabaseBaseImpl");
 		addImports("#jgDiskImplPackage#.RemoteGraphDatabaseAccess");
 		CodeSnippet code = new CodeSnippet(true);
 		code.add("",
 				 "public #simpleImplClassName#(long globalSubgraphId, GraphDatabaseBaseImpl localGraphDatabase,	RemoteGraphDatabaseAccess storingGraphDatabase) {",
 				 "\tsuper(globalSubgraphId, localGraphDatabase, storingGraphDatabase);",
 				 "}",
 				 "",
 				 "");	
 		return code;
 		
 	}
 
 
 	private CodeBlock createInMemoryConstructor() {
 		addImports("de.uni_koblenz.jgralab.Vertex");
 		addImports("de.uni_koblenz.jgralab.Edge");
 		addImports("#schemaPackageName#.#schemaName#");
 		CodeSnippet code = new CodeSnippet(true);
 		code.add("",
 				 "public #simpleImplClassName#(Vertex vertex) {",
 				 "\tsuper(vertex);",
 				 "}",
 				 "",
 				 "public #simpleImplClassName#(Edge edge) {",
 				 "\tsuper(edge);",
 				 "}",
 				 "",
 				 "");	
 		return code;
 	}
 	
 	
 	
 
 	private CodeBlock createGraphElementClassMethods() {
 		CodeList code = new CodeList();
 
 		GraphClass gc = (GraphClass) aec;
 		TreeSet<GraphElementClass<?,?>> sortedClasses = new TreeSet<GraphElementClass<?,?>>();
 		sortedClasses.addAll(gc.getGraphElementClasses());
 		for (GraphElementClass<?,?> gec : sortedClasses) {
 			if (!gec.isInternal()) {
 				CodeList gecCode = new CodeList();
 				code.addNoIndent(gecCode);
 
 				gecCode.addNoIndent(new CodeSnippet(
 								true,
 								"// ------------------------ Code for #ecQualifiedName# ------------------------"));
 
 				gecCode.setVariable("ecSimpleName", gec.getSimpleName());
 				gecCode.setVariable("ecUniqueName", gec.getUniqueName());
 				gecCode.setVariable("ecQualifiedName", gec.getQualifiedName());
 				gecCode.setVariable("ecSchemaVariableName", gec.getVariableName());
 				gecCode.setVariable("ecJavaClassName", schemaRootPackageName+ "." + gec.getQualifiedName());
 				gecCode.setVariable("ecType", (gec instanceof VertexClass ? "Vertex" : "Edge"));
 				gecCode.setVariable("ecTypeInComment", (gec instanceof VertexClass ? "vertex" : "edge"));
 				gecCode.setVariable("ecCamelName", camelCase(gec.getUniqueName()));
 				gecCode.setVariable("ecImplName", (gec.isAbstract() ? "**ERROR**" : camelCase(gec
 								.getQualifiedName()) + "Impl"));
 
 				gecCode.addNoIndent(createGetFirstMethods(gec));
 				gecCode.addNoIndent(createFactoryMethods(gec));
 			}
 		}
 
 		return code;
 	}
 
 	private CodeBlock createGetFirstMethods(GraphElementClass<?,?> gec) {
 		CodeList code = new CodeList();
 		if (config.hasTypeSpecificMethodsSupport()) {
 			code.addNoIndent(createGetFirstMethod(gec, false));
 			if (config.hasMethodsForSubclassesSupport()) {
 				if (!gec.isAbstract()) {
 					code.addNoIndent(createGetFirstMethod(gec, true));
 				}
 			}
 		}
 		return code;
 	}
 
 	private CodeBlock createGetFirstMethod(GraphElementClass<?,?> gec, boolean withTypeFlag) {
 		CodeSnippet code = new CodeSnippet(true);
 		if (currentCycle.isAbstract()) {
 			code.add("/**",
 					 " * @return the first #ecSimpleName# #ecTypeInComment# in this graph");
 			if (withTypeFlag) {
 				code.add(" * @param noSubClasses if set to <code>true</code>, no subclasses of #ecSimpleName# are accepted");
 			}
 			code.add(" */",
 					 "public #ecJavaClassName# getFirst#ecCamelName#(#formalParams#);");
 		} else {
 			code.add("public #ecJavaClassName# getFirst#ecCamelName#(#formalParams#) {",
 					"\treturn (#ecJavaClassName#)getFirst#ecType#(#schemaName#.instance().#ecSchemaVariableName##actualParams#);",
 				 	"}");
 			code.setVariable("actualParams", (withTypeFlag ? ", noSubClasses"	: ""));
 		}
 		code.setVariable("formalParams", (withTypeFlag ? "boolean noSubClasses"	: ""));
 		return code;
 	}
 
 	private CodeBlock createFactoryMethods(GraphElementClass<?,?> gec) {
 		if (gec.isAbstract()) {
 			return null;
 		}
 		CodeList code = new CodeList();
 		code.addNoIndent(createFactoryMethod(gec, false));
 		if (currentCycle.isMemOrDiskImpl()) {
 			code.addNoIndent(createFactoryMethod(gec, true));
 		}
 		return code;
 	}
 
 	private CodeBlock createFactoryMethod(GraphElementClass<?,?> gec, boolean withId) {
 		CodeSnippet code = new CodeSnippet(true);
 
 		if (currentCycle.isAbstract()) {
 			code.add("/**",
 					 " * Creates a new #ecUniqueName# #ecTypeInComment# in this graph.",
 					 " *");
 			if (withId) {
 				code.add(" * @param id the <code>id</code> of the #ecTypeInComment#");
 			}
 			if (gec instanceof EdgeClass) {
 				code.add(" * @param alpha the start vertex of the edge",
 					  	 " * @param omega the target vertex of the edge");
 			}
 			code.add("*/",
 					 "public #ecJavaClassName# create#ecCamelName#(#formalParams#);");
 		}
 		if (currentCycle.isMemOrDiskImpl()) {
 			// "\t#ecJavaClassName# new#ecType# = (#ecJavaClassName#) create#ecType#(#ecJavaClassName#.class, #newActualParams#, this#additionalParams#);",
 
 			code.add("public #ecJavaClassName# create#ecCamelName#(#formalParams#) {",
 					 "\t#ecJavaClassName# new#ecType# = (#ecJavaClassName#) create#ecType#(#ecJavaClassName#.class);",
 					 "\treturn new#ecType#;", "}");
 			code.setVariable("additionalParams", "");
 		}
 
 		//TODO: For binary Edge constructor
 		
 		if (gec instanceof BinaryEdgeClass) {
 			String fromClass = null;
 			String toClass = null;
 			for (IncidenceClass ic : gec.getAllIncidenceClasses()) {
 				if (!ic.isAbstract()) {
 					if (ic.getDirection() == Direction.EDGE_TO_VERTEX) {
 						toClass = ic.getVertexClass().getQualifiedName();
 					} else {
 						fromClass = ic.getVertexClass().getQualifiedName();
 					}
 				}
 			}
 			if (fromClass.equals("Vertex")) {
 				code.setVariable("fromClass", rootBlock.getVariable("jgPackage") + "." + "Vertex");
 			} else {
 				code.setVariable("fromClass", schemaRootPackageName + "." + fromClass);
 			}
 			if (toClass.equals("Vertex")) {
 				code.setVariable("toClass", rootBlock.getVariable("jgPackage") + "." + "Vertex");
 			} else {
 				code.setVariable("toClass", schemaRootPackageName + "."	+ toClass);
 			}
 			code.setVariable("formalParams", (withId ? "int id, " : "")	+ "#fromClass# alpha, #toClass# omega");
 			code.setVariable("addActualParams", ", alpha, omega");
 			code.setVariable("additionalParams", ", alpha, omega");
 		} else {
 			code.setVariable("formalParams", (withId ? "int id" : ""));
 			code.setVariable("addActualParams", "");
 		}
 		code.setVariable("newActualParams", (withId ? "id" : "0"));
 		return code;
 	}
 
 	private CodeBlock createIteratorMethods() {
 		GraphClass gc = (GraphClass) aec;
 
 		CodeList code = new CodeList();
 		if (!config.hasTypeSpecificMethodsSupport()) {
 			return code;
 		}
 		CodeBlock block = createIteratorMethods(gc.getVertexClasses());
 		block.setVariable("elemClassName", "Vertex");
 		block.setVariable("elemClassLowName", "vertex"); 
 		block.setVariable("elemClassPluralName", "Vertices");
 		if (currentCycle.isMemOrDiskImpl()) {
 			addImports("#jgImplPackage#.VertexIterable");
 		}
 		code.add(block);
 		block = createIteratorMethods(gc.getEdgeClasses());
 		block.setVariable("elemClassName", "Edge");
 		block.setVariable("elemClassLowName", "edge"); 
 		block.setVariable("elemClassPluralName", "Edges");
 		if (currentCycle.isMemOrDiskImpl()) {
 			addImports("#jgImplPackage#.EdgeIterable");
 		}
 		code.add(block);
 		return code;
 	}
 	
 	protected CodeBlock createIteratorMethods(Iterable<? extends GraphElementClass<?,?>> set) {
 		CodeList code = new CodeList();
 		for (GraphElementClass<?,?> gec : set) {
 			if (gec.isInternal()) {
 				continue;
 			}
 			code.addNoIndent(createIteratorMethods(gec));
 		}
 		return code;
 	}
 	
 	
 	
 	protected CodeBlock createIteratorMethods(GraphElementClass<?,?> gec) {
 		CodeList code = new CodeList();
 		CodeSnippet s = new CodeSnippet(true);
 		code.addNoIndent(s);
 		s.setVariable("elemQualifiedName", gec.getQualifiedName());
 		s.setVariable("elemJavaClassName", schemaRootPackageName + "." + gec.getQualifiedName());
 		s.setVariable("elemCamelName", camelCase(gec.getUniqueName()));
 		
 		if (currentCycle.isAbstract()) {
 			s.add("/**");
 			s.add(" * @return an Iterable for all #elemClassPluralName# of this graph that are of type #elemQualifiedName# or subtypes.");
 			s.add(" */");
 			s.add("public Iterable<#elemJavaClassName#> get#elemCamelName##elemClassPluralName#();");
 		}
 		if (currentCycle.isMemOrDiskImpl()) {
 			s.add("public Iterable<#elemJavaClassName#> get#elemCamelName##elemClassPluralName#() {");
 			s.add("\treturn new #elemClassName#Iterable<#elemJavaClassName#>(this, #elemJavaClassName#.class);");
 			s.add("}");
 		}
 		s.add("");
 		return code;
 	}
 	
 	
 	//Overwritten methods from AttributedElement to delegate to the complete graph
 	protected CodeBlock createGetter(Attribute attr) {
 		CodeSnippet code = new CodeSnippet(true);
 		code.setVariable("name", attr.getName());
 		code.setVariable("typeCast", attr.getDomain().getJavaClassName(schemaRootPackageName));
 		code.setVariable("type", attr.getDomain().getJavaAttributeImplementationTypeName(schemaRootPackageName));
 		code.setVariable("isOrGet",
 				attr.getDomain().getJavaClassName(schemaRootPackageName)
 						.equals("Boolean") ? "is" : "get");
 
 		switch (currentCycle) {
 		case ABSTRACT:
 			code.add("public #type# #isOrGet#_#name#() ;");
 			break;
 		case MEMORYBASED:
 			code.add("public #type# #isOrGet#_#name#()  {", 
 					"\treturn ((#schemaPackageName#.#simpleClassName#)getParentGraph()).get_#name#();",
 					"}");
 			break;
 		case DISKBASED:
 			code.add("public #type# #isOrGet#_#name#()  {",
 					"\ttry {",
 					"\t\treturn (#typeCast#) localGraphDatabase.getGraphAttribute(\"#name#\");",
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
 		case MEMORYBASED:
 			code.add("public void set_#name#(#type# _#name#)  {",
 					"\t((#schemaPackageName#.#simpleClassName#)getParentGraph()).set_#name#(_#name#);",
 					"}");
 			break;
 		case DISKBASED:
 			code.add("public void set_#name#(#type# _#name#)  {",
 					"\ttry {",
 					"\t\tlocalGraphDatabase.setGraphAttribute(\"#name#\", _#name#);",
					"\t} catch (java.rmi.RemoteException ex) {",
 					"\t\tthrow new RuntimeException(ex);",
 					"\t}",
 					"}");
 			break;
 		}
 		return code;
 	}
 
 	@Override
 	protected CodeBlock createFields(Set<Attribute> attrSet) {
 		return null;
 	}
 
 	
 }
