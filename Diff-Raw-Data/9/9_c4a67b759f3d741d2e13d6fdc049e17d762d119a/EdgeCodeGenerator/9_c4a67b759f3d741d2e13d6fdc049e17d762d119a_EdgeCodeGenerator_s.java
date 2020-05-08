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
 
 import de.uni_koblenz.jgralab.schema.EdgeClass;
 
 
 /**
  * TODO add comment
  * 
  * @author ist@uni-koblenz.de
  * 
  */
 public class EdgeCodeGenerator extends GraphElementCodeGenerator<EdgeClass> {
 
 	
 	public EdgeCodeGenerator(EdgeClass edgeClass, String schemaPackageName,
 			CodeGeneratorConfiguration config) {
 		super(edgeClass, schemaPackageName, config, false);
 		rootBlock.setVariable("baseClassName", "EdgeImpl");
 		rootBlock.setVariable("proxyClassName", "EdgeProxy");
 		rootBlock.setVariable("graphElementClass", "Edge");
 	}
 
 	@Override
 	protected CodeBlock createConstructor() {
 		CodeList code = (CodeList) super.createConstructor();
 		code.setVariable("implOrProxy", currentCycle.isMemOrDiskImpl() ? "Impl" : "Proxy");
 		if (currentCycle.isDiskbasedImpl()) {
 			code.addNoIndent(new CodeSnippet("/** Constructor only to be used by Background-Storage backend */"));
 			code.addNoIndent(new CodeSnippet(
 				true,
 				"public #simpleClassName##implOrProxy#(int id, #jgDiskImplPackage#.EdgeContainer container, #jgPackage#.Graph g) throws java.io.IOException {",
 				"\tsuper(id, container, g);",
 				"}"));
 		}
 		return code;
 	}
 	
 	protected CodeBlock createLoadAttributeContainer() {
 		if (currentCycle.isMemOrDiskImpl()) {
			addImports("#jgDiskimplPackage#.GraphDatabaseBaseImpl");
 			return new CodeSnippet(
 					true,
 					"protected InnerAttributeContainer loadAttributeContainer() {",
 					"\treturn (InnerAttributeContainer) container.backgroundStorage.getEdgeAttributeContainer(GraphDatabaseBaseImpl.convertToLocalId(elementId));",
 					"}"
 			);
 		} else {
 			return null;
 		}
 
 	}
 
 	@Override
 	protected CodeList createBody() {
 		CodeList code = (CodeList) super.createBody();
 	    createMethodsForBinaryEdge(code);
 		return code;
 	}
 	
 	protected void createMethodsForBinaryEdge(CodeList code) {
 		//to be overwritten in the binary edge codegen
 	}
 
 	
 
 
 }
