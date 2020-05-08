 package com.karlhammar.ontometrics.plugins.structural;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.semanticweb.owlapi.apibinding.OWLManager;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLClassExpression;
 
 
 /**
  * This is a singleton class used to perform OWLAPI opening operations in the
  * structural OntoMetrics plugin only once, as opposed to for every 
  * individual structural check.
  * @author Karl Hammar <karl@karlhammar.com>
  *
  */
 public class StructuralSingletonOWLAPI {
 
 	// Member variables.
 	private Logger logger = Logger.getLogger(getClass().getName());
 	private static StructuralSingletonOWLAPI ref;
 	private OWLOntology ontology;
 	List<Integer> heights;
 	
 	public List<Integer> getHeights() {
 		return this.heights;
 	}
 	
 	protected void calculateHeights()
 	{
 		this.heights = new ArrayList<Integer>();
 		// First, find all leaf nodes
 		Set<OWLClass> leaves = new HashSet<OWLClass>();
 		Set<OWLClass> allClasses = ontology.getClassesInSignature();
 		Iterator<OWLClass> iter = allClasses.iterator();
 	    while (iter.hasNext()) {
 	    	OWLClass i = iter.next();
 	    	if (i.getSubClasses(ontology).size() <= 0)
 	    		leaves.add(i);
 	    }
 	    
 	    // Find longest path for each node to a top level class
 	    iter = leaves.iterator();
 	    while (iter.hasNext())
 	    {
 	    	OWLClass leafNode = iter.next();
 	    	heights.add(calculateTallestHeightFromNode(ontology,leafNode));
 	    }
 	}
 	
 	/*
 	 * A recursive method that returns the longest possible superclass path from
 	 * a given node to a top-level node (one that has no superclasses)
 	 * 
 	 * @param  o 	The ontology that is walked through.
 	 * @param  c	The node that this recursion calculates.
 	 * @return 		The cardinality of the longest found path.
 	 */
 	private int calculateTallestHeightFromNode(OWLOntology o, OWLClass c)
 	{
 		Set<OWLClassExpression> superClasses = c.getSuperClasses(o);
 		
 		// If there are no superclasses, stop recursing.
 		if (superClasses.size() <= 0)
 		{
 			return 0;
 		}
 		else
 		{
 			List<Integer> parentHeights = new ArrayList<Integer>();
 			Iterator<OWLClassExpression> iter = superClasses.iterator();
 		    while (iter.hasNext()) {
 		    	OWLClassExpression i = iter.next();
 		    	
 		    	// We don't want anonymous classes, only explicitly stated 
 		    	// superclass relations
 		    	if (!i.isAnonymous())
 		    	{
 		    		OWLClass superClass = i.asOWLClass();
 		    		parentHeights.add(calculateTallestHeightFromNode(o,superClass));
 		    	}
 		    }
 		    
		    // If ALL superclasses are anonymous expressions, consider top reached
		    if (parentHeights.size() == 0) {
		    	parentHeights.add(0);
		    }
		    
 		    // Get and return only the longest path
 		    Collections.sort(parentHeights, Collections.reverseOrder());
 		    int longestParentHeight = parentHeights.get(parentHeights.size()-1);
 		    return (longestParentHeight + 1);
 		}
 	}
 	
 
 	/**
 	 * Private constructor. Loads ontology document.
 	 * @param ontologyFile
 	 */
 	private StructuralSingletonOWLAPI(File ontologyFile) {
 		try {
 			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
 			this.ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
 		}
 		catch (OWLOntologyCreationException e) {
 			logger.severe(e.getMessage());
 		}
 	}
 	
 	/**
 	 * Getter for ontology member.
 	 * @return
 	 */
 	public OWLOntology getOntology() {
 		return this.ontology;
 	}
 
 	/**
 	 * Getter for singleton. Instantiates if it does not already exist, otherwise
 	 * returns existing instance.
 	 * @param ontologyFile The File instance referring to ontology to calculate metrics over.
 	 * @return New or existing Singleton instance.
 	 */
 	public static synchronized StructuralSingletonOWLAPI getSingletonObject(File ontologyFile) {
 		if (ref == null) {
 			ref = new StructuralSingletonOWLAPI(ontologyFile);
 		}
 		return ref;
 	}
 
 	/**
 	 * Don't try this!
 	 */
 	public Object clone() throws CloneNotSupportedException {
 		throw new CloneNotSupportedException();
 	}
 }
