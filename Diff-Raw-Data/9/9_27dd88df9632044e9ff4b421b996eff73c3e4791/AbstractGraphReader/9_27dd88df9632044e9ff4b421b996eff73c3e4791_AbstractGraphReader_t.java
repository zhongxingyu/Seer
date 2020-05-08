 
 /*
   File: AbstractGraphReader.java 
   
   Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
   
   The Cytoscape Consortium is: 
   - Institute for Systems Biology
   - University of California San Diego
   - Memorial Sloan-Kettering Cancer Center
   - Institut Pasteur
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
 
 package cytoscape.data.readers;
 
 import cytoscape.data.CyAttributes;
 import cytoscape.Cytoscape;
 import cytoscape.util.CyNetworkNaming;
 
 import giny.model.RootGraph;
 import giny.view.GraphView;
 
import java.io.File;
 import java.io.IOException;
 
 
 public abstract class AbstractGraphReader implements GraphReader {
 
 	protected String fileName;
 
 	public AbstractGraphReader(String fileName) {
 		this.fileName = fileName;
 	}
 
 	public abstract void read() throws IOException;
 
 	/**
 	 * @deprecated Use read() instead.  Will be removed Dec 2006.
 	 */
 	public void read(boolean canonicalizeNodeNames) throws IOException {
 		read();
 	}
 
 	/**
 	 * @deprecated Use Cytoscape.getRootGraph() instead. Will be removed Dec 2006.
 	 */
 	public RootGraph getRootGraph() {
 		return Cytoscape.getRootGraph();
 	}
 
 	/**
 	 * @deprecated Use Cytoscape.getNodeAttributes() instead. Will be removed Dec 2006.
 	 */
 	public CyAttributes getNodeAttributes() {
 		return Cytoscape.getNodeAttributes();
 	}
 
 	/**
 	 * @deprecated Use Cytoscape.getEdgeAttributes() instead. Will be removed Dec 2006.
 	 */
 	public CyAttributes getEdgeAttributes() {
 		return Cytoscape.getEdgeAttributes();
 	}
 
 	public void layout(GraphView myView) {
 	}
 
 	public int[] getNodeIndicesArray() {
 		return null;
 	}
 
 	public int[] getEdgeIndicesArray() {
 		return null;
 	}
 
 	public String getNetworkName() {
 		String t = "";
 		if ( fileName != null ) {
			File tempFile = new File(fileName);
			t = tempFile.getName();
 		}
 		return CyNetworkNaming.getSuggestedNetworkTitle(t);
 	}
 }
