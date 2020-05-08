 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package edu.toronto.cs.cidb.hpoa.taxonomy;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.net.URL;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 
 import edu.toronto.cs.cidb.hpoa.utils.graph.DAG;
 import edu.toronto.cs.cidb.hpoa.utils.graph.DAGNode;
 import edu.toronto.cs.cidb.hpoa.utils.graph.IDAGNode;
 import edu.toronto.cs.cidb.hpoa.utils.maps.CounterMap;
 import edu.toronto.cs.cidb.solr.SolrScriptService;
 
 public abstract class AbstractTaxonomy extends DAG<TaxonomyTerm> implements
 		Taxonomy {
 	public final static String PARENT_ID_REGEX = "^([A-Z]{2}\\:[0-9]{7})\\s*!\\s*.*";
 
 	private final static String TERM_MARKER = "[Term]";
 
	private final static String END_OF_TERM_LIST_MARKER = "[Typedef]";

 	private final static String FIELD_NAME_VALUE_SEPARATOR = "\\s*:\\s+";
 
 	private final Map<String, String> alternateIdMapping = new HashMap<String, String>();
 
 	private IDAGNode root;
 
 	private final Map<String, Set<String>> ancestorCache = new HashMap<String, Set<String>>();
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.toronto.cs.cidb.hpoa.taxonomy.Taxonomy#load(edu.toronto.cs.cidb.solr
 	 * .SolrScriptService)
 	 */
 	@SuppressWarnings("unchecked")
 	public int load(SolrScriptService source) {
 		// Make sure we can read the data
 		if (source == null) {
 			return -1;
 		}
 		// Load data
 		clear();
 		TermData data = new TermData();
 		SolrDocumentList results = source.search("*:*");
 		for (SolrDocument result : results) {
 			for (String name : result.getFieldNames()) {
 				Object val = result.get(name);
 				if (val instanceof Collection<?>) {
 					data.put(name, (Collection) val);
 				} else {
 					data.addTo(name, (String) val);
 				}
 			}
 			if (data.isValid()) {
 				this.createTaxonomyTerm(data);
 			}
 		}
 		cleanArcs();
 		// How much did we load:
 		return size();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.toronto.cs.cidb.hpoa.taxonomy.Taxonomyy#load(java.io.File)
 	 */
 	public int load(File source) {
 		// Make sure we can read the data
 		if (source == null) {
 			return -1;
 		}
 		// Load data
 		clear();
 		TermData data = new TermData();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(source));
 			String line;
 			while ((line = in.readLine()) != null) {
				if (line.trim().equalsIgnoreCase(END_OF_TERM_LIST_MARKER)) {
					break;
				}
 				if (line.trim().equalsIgnoreCase(TERM_MARKER)) {
 					if (data.isValid()) {
 						this.createTaxonomyTerm(data);
 					}
 					data.clear();
 					continue;
 				}
 				String pieces[] = line.split(FIELD_NAME_VALUE_SEPARATOR, 2);
 				if (pieces.length != 2) {
 					continue;
 				}
 				String name = pieces[0], value = pieces[1];
 				data.addTo(name, value);
 			}
 			if (data.isValid()) {
 				this.createTaxonomyTerm(data);
 			}
 			in.close();
 		} catch (NullPointerException ex) {
 			ex.printStackTrace();
 			System.err.println("File does not exist");
 		} catch (FileNotFoundException ex) {
 			ex.printStackTrace();
 			System.err.println("Could not locate source file: "
 					+ source.getAbsolutePath());
 		} catch (IOException ex) {
 			// TODO Auto-generated catch block
 			ex.printStackTrace();
 		}
 		cleanArcs();
 		// How much did we load:
 		return size();
 	}
 
 	private void cleanArcs() {
 		Set<IDAGNode> roots = new HashSet<IDAGNode>();
 		// Redo all links
 		for (DAGNode n : getNodes()) {
 			if (n.getParents().size() == 0) {
 				roots.add(n);
 				continue;
 			}
 			for (String parentId : n.getParents()) {
 				DAGNode p = getTerm(parentId);
 				if (p != null) {
 					p.addChild(n);
 				} else {
 					System.err.println("[WARNING] Node with id " + n.getId()
 							+ " has parent " + parentId + ", but no node "
 							+ parentId + " exists in the graph!\n");
 				}
 			}
 		}
 		if (roots.size() == 0) {
 			System.err
 					.println("Something's wrong, this directed graph is DEFINITELY not acyclic!");
 		} else if (roots.size() == 1) {
 			for (IDAGNode n : roots) {
 				this.root = n;
 			}
 		} else {
 			this.root = new TaxonomyTerm("", "FAKE ROOT");
			this.addNode((TaxonomyTerm) this.root);
			this.alternateIdMapping.put(this.root.getId(), this.root.getId());
 			for (IDAGNode n : roots) {
 				this.root.addChild(n);
 				n.addParent(this.root);
 			}
 		}
 	}
 
 	protected void createTaxonomyTerm(TermData data) {
 		TaxonomyTerm term = new TaxonomyTerm(data);
 		this.addNode(term);
 		this.alternateIdMapping.put(term.getId(), term.getId());
 		for (String altId : data.safeGet(TermData.ALT_ID_FIELD_NAME)) {
 			this.alternateIdMapping.put(altId, term.getId());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.toronto.cs.cidb.hpoa.taxonomy.Taxonomy#getRealId(java.lang.String)
 	 */
 	public String getRealId(String id) {
 		return this.alternateIdMapping.get(id);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.toronto.cs.cidb.hpoa.taxonomy.Taxonomy#getTerm(java.lang.String)
 	 */
 	public TaxonomyTerm getTerm(String id) {
 		String realId = this.getRealId(id);
 		if (realId != null) {
 			return (TaxonomyTerm) this.getNode(realId);
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.toronto.cs.cidb.hpoa.taxonomy.Taxonomy#getName(java.lang.String)
 	 */
 	public String getName(String id) {
 		DAGNode node = this.getTerm(id);
 		if (node != null) {
 			return node.getName();
 		}
 		return id;
 	}
 
 	public void printAltMapping(PrintStream out) {
 		printAltMapping(out, false);
 	}
 
 	public void printAltMapping(PrintStream out, boolean all) {
 		for (String key : this.alternateIdMapping.keySet()) {
 			if (all || !key.equals(this.alternateIdMapping.get(key))) {
 				out.println(key + " -> " + this.alternateIdMapping.get(key));
 			}
 
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.toronto.cs.cidb.hpoa.taxonomy.Taxonomy#getRootId()
 	 */
 	public String getRootId() {
 		return this.root.getId();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see edu.toronto.cs.cidb.hpoa.taxonomy.Taxonomy#getRoot()
 	 */
 	public IDAGNode getRoot() {
 		return this.root;
 	}
 
 	@Override
 	public boolean removeNode(String id) {
 		return super.removeNode(id);
 	}
 
 	@Override
 	public boolean removeNode(TaxonomyTerm node) {
 		return super.removeNode(node);
 	}
 
 	protected Set<String> findAncestors(String id) {
 		Set<String> result = new HashSet<String>();
 		if (this.getTerm(id) == null) {
 			return result;
 		}
 		Set<String> front = new HashSet<String>();
 		Set<String> newFront = new HashSet<String>();
 		front.add(this.getRealId(id));
 		result.add(this.getRealId(id));
 		while (!front.isEmpty()) {
 			for (String nextTermId : front) {
 
 				for (String parentTermId : this.getTerm(nextTermId)
 						.getParents()) {
 					if (!result.contains(parentTermId)) {
 						newFront.add(parentTermId);
 						result.add(parentTermId);
 					}
 				}
 			}
 			front.clear();
 			front.addAll(newFront);
 			newFront.clear();
 		}
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * edu.toronto.cs.cidb.hpoa.taxonomy.Taxonomy#getAncestors(java.lang.String)
 	 */
 	public Set<String> getAncestors(String termId) {
 		if (this.ancestorCache.get(termId) == null) {
 			this.ancestorCache.put(termId, this.findAncestors(termId));
 		}
 		return this.ancestorCache.get(termId);
 	}
 
 	public File getInputFileHandler(String inputLocation, boolean forceUpdate) {
 		try {
 			File result = new File(inputLocation);
 			if (!result.exists()) {
 				String name = inputLocation.substring(inputLocation
 						.lastIndexOf('/') + 1);
 				result = getTemporaryFile(name);
 				if (!result.exists()) {
 					result.createNewFile();
 					BufferedInputStream in = new BufferedInputStream((new URL(
 							inputLocation)).openStream());
 					OutputStream out = new FileOutputStream(result);
 					IOUtils.copy(in, out);
 					out.flush();
 					out.close();
 				}
 			}
 			return result;
 		} catch (IOException ex) {
 			ex.printStackTrace();
 			return null;
 		}
 	}
 
 	protected File getTemporaryFile(String name) {
 		return getInternalFile(name, "tmp");
 	}
 
 	protected File getInternalFile(String name, String dir) {
 		File parent = new File("", dir);
 		if (!parent.exists()) {
 			parent.mkdirs();
 		}
 		System.out.println(parent.getAbsolutePath());
 		return new File(parent, name);
 	}
 
 	public void display() {
 		display(System.out);
 	}
 
 	public void display(File out) {
 		try {
 			PrintStream p = new PrintStream(out);
 			display(p);
 			p.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			display(System.out);
 		}
 	}
 
 	protected void display(PrintStream out) {
 		Map<String, Boolean> visited = new HashMap<String, Boolean>();
 		Set<String> crt = new TreeSet<String>();
 		Set<String> next = new TreeSet<String>();
 		crt.add(this.getRootId());
 		CounterMap<Integer> h = new CounterMap<Integer>();
 
 		int min = this.size(), max = 0;
 		double avg = 0;
 
 		while (!crt.isEmpty()) {
 			for (String id : crt) {
 				if (Boolean.TRUE.equals(visited.get(id))) {
 					continue;
 				}
 				TaxonomyTerm term = this.getTerm(id);
 				int p = term.getParents().size();
 				h.addTo(p);
 				if (min > p) {
 					min = p;
 				}
 				if (max < p) {
 					max = p;
 				}
 				avg += p;
 				out.println(term);
 				visited.put(id, true);
 				next.addAll(term.getChildren());
 			}
 			crt.clear();
 			crt.addAll(next);
 			next.clear();
 		}
 		avg /= this.size();
 		out.println(h);
 		out.println();
 		out.println("SIZE " + this.size());
 		out.println("MIN: " + min);
 		out.println("MAX: " + max);
 		out.println("AVG: " + avg);
 	}
 }
