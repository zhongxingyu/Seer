 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is S23M.
  *
  * The Initial Developer of the Original Code is
  * The S23M Foundation.
  * Portions created by the Initial Developer are
  * Copyright (C) 2012 The S23M Foundation.
  * All Rights Reserved.
  *
  * Contributor(s):
  * Andrew Shewring
  * ***** END LICENSE BLOCK ***** */
 package org.s23m.cell.platform.api;
 
 import static org.s23m.cell.S23MKernel.coreGraphs;
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.s23m.cell.Set;
 import org.s23m.cell.api.models.S23MSemanticDomains;
 import org.s23m.cell.platform.S23MPlatform;
 import org.s23m.cell.platform.api.models.CellEngineering;
 import org.s23m.cell.platform.api.models.Organization;
 import org.s23m.cell.platform.testfoundation.AgencyTestFoundation;
 
 public class GraphQueriesTest extends TestCase {
 
 	private static boolean agencyTestFoundationInitialised = false;
 
 	@Override
 	@Before
 	protected void setUp() throws Exception {
 		S23MPlatform.boot();
 		if (!agencyTestFoundationInitialised) {
 			AgencyTestFoundation.instantiateFeature();
 			agencyTestFoundationInitialised = true;
 		}
 	}
 
 	@Test
 	public void testTopologicalVisibilitySort() {
 		// TODO
 	}
 
 	@Test
 	public void testPartitionModelByVisibilityArrows() {
 		// TODO
 	}
 
 	@Test
 	public void testFilterConnectedComponents() {
 		final Set eightElements = GraphQueries.filterConnectedComponents(create8ComponentGraph());
 		final Set twoElements = GraphQueries.filterConnectedComponents(create2ComponentGraph());
 		final Set oneElement = GraphQueries.filterConnectedComponents(create1ComponentGraph());
 
		assertEquals(8, eightElements.size());
		assertEquals(2, twoElements.size());
		assertEquals(1, oneElement.size());

 	}
 
 	private Set create8ComponentGraph() {
 		final Set root = AgencyTestFoundation.test1.filter(CellEngineering.organization).extractFirst();
 		final Set graph = root.addConcrete(Organization.cell, Instantiation.addDisjunctSemanticIdentitySet("g", "g", Instantiation.toSemanticDomain(AgencyTestFoundation.test1)));
 
 		final Set one = createVertex(graph, "1");
 		final Set two = createVertex(graph, "2");
 		final Set three = createVertex(graph, "3");
 		final Set four = createVertex(graph, "4");
 		final Set five = createVertex(graph, "5");
 		final Set six = createVertex(graph, "6");
 		final Set seven = createVertex(graph, "7");
 		final Set eight = createVertex(graph, "8");
 
 		return graph;
 	}
 	private Set create2ComponentGraph() {
 		final Set root = AgencyTestFoundation.test1.filter(CellEngineering.organization).extractFirst();
 		final Set graph = root.addConcrete(Organization.cell, Instantiation.addDisjunctSemanticIdentitySet("g", "g", Instantiation.toSemanticDomain(AgencyTestFoundation.test1)));
 
 		final Set one = createVertex(graph, "1");
 		final Set two = createVertex(graph, "2");
 		final Set three = createVertex(graph, "3");
 		final Set four = createVertex(graph, "4");
 		final Set five = createVertex(graph, "5");
 		final Set six = createVertex(graph, "6");
 		final Set seven = createVertex(graph, "7");
 		final Set eight = createVertex(graph, "8");
 
 		addEdge(one, five);
 
 		addEdge(three, six);
 		addEdge(three, eight);
 
 		addEdge(four, five);
 
 		addEdge(five, two);
 		addEdge(five, seven);
 
 		addEdge(six, eight);
 
 		return graph;
 	}
 	private Set create1ComponentGraph() {
 		final Set root = AgencyTestFoundation.test1.filter(CellEngineering.organization).extractFirst();
 		final Set graph = root.addConcrete(Organization.cell, Instantiation.addDisjunctSemanticIdentitySet("g", "g", Instantiation.toSemanticDomain(AgencyTestFoundation.test1)));
 
 		final Set one = createVertex(graph, "1");
 		final Set two = createVertex(graph, "2");
 		final Set three = createVertex(graph, "3");
 		final Set four = createVertex(graph, "4");
 		final Set five = createVertex(graph, "5");
 		final Set six = createVertex(graph, "6");
 		final Set seven = createVertex(graph, "7");
 		final Set eight = createVertex(graph, "8");
 
 		addEdge(one, five);
 		addEdge(one, six);
 
 		addEdge(three, six);
 		addEdge(three, eight);
 
 		addEdge(four, five);
 
 		addEdge(five, two);
 		addEdge(five, seven);
 		addEdge(five, eight);
 
 		addEdge(six, seven);
 		addEdge(six, eight);
 
 		return graph;
 	}
 
 	private Set createVertex(final Set container, final String label) {
 		return container.addConcrete(Organization.cell, Instantiation.addDisjunctSemanticIdentitySet(label, label, Instantiation.toSemanticDomain(AgencyTestFoundation.test1)));
 	}
 
 	private void addEdge(final Set from, final Set to) {
 		Instantiation.arrow(coreGraphs.edge,
 			Instantiation.addDisjunctSemanticIdentitySet("test-edge", "test-edge", Instantiation.toSemanticDomain(AgencyTestFoundation.test1)),
 			from,
 			from,
 			S23MSemanticDomains.minCardinality_NOTAPPLICABLE,
 			S23MSemanticDomains.maxCardinality_NOTAPPLICABLE,
 			S23MSemanticDomains.isNavigable_FALSE,
 			S23MSemanticDomains.isContainer_FALSE,
 			to,
 			to,
 			S23MSemanticDomains.minCardinality_NOTAPPLICABLE,
 			S23MSemanticDomains.maxCardinality_NOTAPPLICABLE,
 			S23MSemanticDomains.isNavigable_TRUE,
 			S23MSemanticDomains.isContainer_FALSE
 		);
 	}
 }
