 
 /*
   File: FlagAndSelectionHandlerTest.java 
   
   Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
   
   The Cytoscape Consortium is: 
   - Institute for Systems Biology
   - University of California San Diego
   - Memorial Sloan-Kettering Cancer Center
   - Pasteur Institute
   - Agilent Technologies
   
   This library is free software; you can redistribute it and/or modify it
   under the terms of the GNU Lesser General Public License as published
   by the Free Software Foundation; either version 2.1 of the License, or
   any later version.
   
   This library is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
   MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
   documentation provided hereunder is on an "as is" basis, and the
   Institute for Systems Biology and the Whitehead Institute 
   have no obligations to provide maintenance, support,
   updates, enhancements or modifications.  In no event shall the
   Institute for Systems Biology and the Whitehead Institute 
   be liable to any party for direct, indirect, special,
   incidental or consequential damages, including lost profits, arising
   out of the use of this software and its documentation, even if the
   Institute for Systems Biology and the Whitehead Institute 
   have been advised of the possibility of such damage.  See
   the GNU Lesser General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License
   along with this library; if not, write to the Free Software Foundation,
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 
 
 //------------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //--------------------------------------------------------------------------------------
 package cytoscape.view.unitTests;
 //--------------------------------------------------------------------------------------
 import junit.framework.*;
 import java.io.*;
 import java.util.*;
 
 import giny.model.*;
 import giny.view.*;
 import cytoscape.Cytoscape;
 import cytoscape.data.FlagFilter;
 import cytoscape.view.FlagAndSelectionHandler;
 import cytoscape.unitTests.AllTests;
 //------------------------------------------------------------------------------
 public class FlagAndSelectionHandlerTest extends TestCase {
 
     FlagFilter filter;
     Node node1;
     Node node2;
     Edge edge1;
     Edge edge2;
     GraphPerspective gp;
     GraphView view;
     NodeView nodeView1;
     NodeView nodeView2;
     EdgeView edgeView1;
     EdgeView edgeView2;
     FlagAndSelectionHandler handler;
 //------------------------------------------------------------------------------
 public FlagAndSelectionHandlerTest (String name) 
 {
     super (name);
 }
 //------------------------------------------------------------------------------
 public void setUp () throws Exception
 {
   RootGraph rootGraph = Cytoscape.getRootGraph();
     node1 = rootGraph.getNode(rootGraph.createNode());
     node2 = rootGraph.getNode(rootGraph.createNode());
     edge1 = rootGraph.getEdge(rootGraph.createEdge(node1, node2));
     edge2 = rootGraph.getEdge(rootGraph.createEdge(node2, node1));
     Node[] nodeArray = {node1, node2};
     Edge[] edgeArray = {edge1, edge2};
     gp = rootGraph.createGraphPerspective(nodeArray, edgeArray);
     filter = new FlagFilter(gp);
     view = new ding.view.DGraphView(gp);
     nodeView1 = view.getNodeView(node1);
     nodeView2 = view.getNodeView(node2);
     edgeView1 = view.getEdgeView(edge1);
     edgeView2 = view.getEdgeView(edge2);
     //set an initial state to make sure the handler synchronizes properly
     filter.setFlagged(node1, true);
     edgeView2.setSelected(true);
     handler = new FlagAndSelectionHandler(filter, view);
     assertTrue( filter.isFlagged(edge2) );
     assertTrue( nodeView1.isSelected() );
     filter.unflagAllNodes();
     filter.unflagAllEdges();
 }
 //------------------------------------------------------------------------------
 public void tearDown () throws Exception
 {
 }
 //-------------------------------------------------------------------------
 /**
  * Tests that the view is properly modified when the filter is changed.
  */
 public void testFilterToView() throws Exception {
     checkState(false, false, false, false);
     filter.setFlagged(node1, true);
     checkState(true, false, false, false);
     filter.setFlagged(edge2, true);
     checkState(true, false, false, true);
     filter.flagAllNodes();
     checkState(true, true, false, true);
     filter.flagAllEdges();
     checkState(true, true, true, true);
     filter.setFlagged(node2, false);
     checkState(true, false, true, true);
     filter.setFlagged(edge1, false);
     checkState(true, false, false, true);
     filter.unflagAllEdges();
     checkState(true, false, false, false);
     filter.unflagAllNodes();
     checkState(false, false, false, false);
 }
 //-------------------------------------------------------------------------
 /**
  * Tests that the filter is properly modified when the view is changed.
  */
 public void testViewToFilter() throws Exception {
     checkState(false, false, false, false);
     nodeView1.setSelected(true);
     checkState(true, false, false, false);
     edgeView2.setSelected(true);
     checkState(true, false, false, true);
     nodeView2.setSelected(true);
     checkState(true, true, false, true);
     edgeView1.setSelected(true);
     checkState(true, true, true, true);
     nodeView2.setSelected(false);
     checkState(true, false, true, true);
     edgeView1.setSelected(false);
     checkState(true, false, false, true);
     edgeView2.setSelected(false);
     checkState(true, false, false, false);
     nodeView1.setSelected(false);
     checkState(false, false, false, false);
 }
 //-------------------------------------------------------------------------
 /**
  * Checks that the current state of the filter and the view match the state
  * defined by the arguments.
  */
 public void checkState(boolean n1, boolean n2, boolean e1, boolean e2) {
     assertTrue( filter.isFlagged(node1) == n1 );
     assertTrue( filter.isFlagged(node2) == n2 );
     assertTrue( filter.isFlagged(edge1) == e1 );
     assertTrue( filter.isFlagged(edge2) == e2 );
     assertTrue( nodeView1.isSelected() == n1 );
     assertTrue( nodeView2.isSelected() == n2 );
     assertTrue( edgeView1.isSelected() == e1 );
     assertTrue( edgeView2.isSelected() == e2 );
 }
 //-------------------------------------------------------------------------
 public static void main (String[] args) 
 {
   junit.textui.TestRunner.run (new TestSuite (FlagAndSelectionHandlerTest.class));
 }
 //------------------------------------------------------------------------------
 }
 
 
