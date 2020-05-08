 /*
  * AbstractDatatypeElement.java
  *
  * Created on 19. September 2006, 01:53
  *
  * Copyright (C) 2007
  * German Research Center for Artificial Intelligence (DFKI GmbH) Saarbruecken
  * Hochschule fuer Technik und Wirtschaft (HTW) des Saarlandes
  * Developed by Oliver Fourman, Ingo Zinnikus, Matthias Klusch
  *
  * The code is free for non-commercial use only.
  * You can redistribute it and/or modify it under the terms
  * of the Mozilla Public License version 1.1  as
  * published by the Mozilla Foundation at
  * http://www.mozilla.org/MPL/MPL-1.1.txt
  */
 
 package de.dfki.dmas.owls2wsdl.core;
 
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.Vector;
 
import org.exolab.castor.xml.schema.Particle;

 /**
  * 
  * @author Oliver
  */
 public class AbstractDatatypeElement implements java.io.Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1586366217195610633L;
 
 	/**
 	 * Describes the source of the resulting type element.
 	 * (OBJECT|DATATYPE|RESTRICTION-ON-OBJECT|RESTRICTION-ON-DATATYPE|META)
 	 */
 	private String _owlSrc;
 
 	private String _name;
 	private String _type; // url of AbstractDatatype
 
 	private int _level; // hierarchical level of property
 	private String _inheritedBy;
 
 	// Vector of restriction tuples (AbstractElementRestrictionElement)
 	private Vector<AbstractDatatypeRestrictionElement> _restrictions;
 
 	//
 	private Vector<String> _complementRange;
 
 	// Resulting Range !!! of individuals AND classes: check with OWL Model!
 	// wird zus�tzlich gepflegt. aus restrictions und direkt definierten
 	// wertebereich bzw. konstanten
 	private Vector<String> _restrictedRange;
 
 	/** Creates a new instance of AbstractDatatypeElement */
 	public AbstractDatatypeElement() {
 		this._complementRange = new Vector<String>();
 		this._restrictions = new Vector<AbstractDatatypeRestrictionElement>();
 		this._restrictedRange = new Vector<String>();
 	}
 
 	public AbstractDatatypeElement(String owlsource, String name, String type) {
 		this();
 		// name = this.convertName2LocalName(name);
 		this._owlSrc = owlsource; // COMPLEX, SIMPLE or DATATPYE, OBJECT (OR
 									// META INFORMATION)
 		this._name = name;
 		this._type = type;
 		this._level = 0;
 		this._inheritedBy = null;
 		System.out.println("[C] AbstractDatatypeElement (" + owlsource + ", "
 				+ name + ", " + type + ")");
 	}
 
 	public AbstractDatatypeElement(String owlsource, String name, String type,
 			int level, String inheritedBy) {
 		this();
 		// name = this.convertName2LocalName(name);
 		this._owlSrc = owlsource; // DATATPYE, OBJECT, RESTRICTION (OR META
 									// INFORMATION)
 		this._name = name;
 		this._type = type;
 		this._level = level;
 		this._inheritedBy = inheritedBy;
 		System.out.println("[C] AbstractDatatypeElement (" + owlsource + ", "
 				+ name + ", " + type + ")");
 	}
 
 	public String getOwlSource() {
 		return this._owlSrc;
 	}
 
 	public String getName() {
 		return this._name;
 	}
 
 	public String getType() {
 		return this._type;
 	}
 
 	public int getLevel() {
 		return this._level;
 	}
 
 	public String getInheritedBy() {
 		return this._inheritedBy;
 	}
 
 	public Vector<String> getComplementRange() {
 		return this._complementRange;
 	}
 
 	public Vector<AbstractDatatypeRestrictionElement> getRestrictions() {
 		return this._restrictions;
 	}
 
 	private boolean containsRestrictionType(String typeString) {
 		for (Iterator<AbstractDatatypeRestrictionElement> it = this._restrictions
 				.iterator(); it.hasNext();) {
 			AbstractDatatypeRestrictionElement restrictionElem = it.next();
 			if (typeString.equals(restrictionElem.getRestrictionType())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private AbstractDatatypeRestrictionElement getRestrictionElement(
 			String typeString) {
 		for (Iterator<AbstractDatatypeRestrictionElement> it = this._restrictions
 				.iterator(); it.hasNext();) {
 			AbstractDatatypeRestrictionElement restrictionElem = it.next();
 			if (typeString.equals(restrictionElem.getRestrictionType())) {
 				return restrictionElem;
 			}
 		}
 		return null;
 	}
 
 	// FOR XSD GENERATION
 
 	/**
 	 * in case that restrictions have a minor level use this to add element to
 	 * schema
 	 */
 	public int computeLevel() {
 		int clevel = this._level;
 		for (Iterator<AbstractDatatypeRestrictionElement> it = this._restrictions
 				.iterator(); it.hasNext();) {
 			AbstractDatatypeRestrictionElement restrictionElem = it.next();
 			if (restrictionElem.getLevel() < clevel) {
 				clevel = restrictionElem.getLevel();
 			}
 		}
 		return clevel;
 	}
 
 	public int getMinOccurs() {
 		if (this.containsRestrictionType("MinCardinality")) {
 			return Integer.parseInt(this
 					.getRestrictionElement("MinCardinality")
 					.getRestrictionValue());
 		} else if (this.containsRestrictionType("Cardinality")) {
 			return Integer.parseInt(this.getRestrictionElement("Cardinality")
 					.getRestrictionValue());
 		} else {
			return 0;
 		}
 	}
 
 	public int getMaxOccurs() {
 		if (this.containsRestrictionType("MaxCardinality")) {
 			return Integer.parseInt(this
 					.getRestrictionElement("MaxCardinality")
 					.getRestrictionValue());
 		} else if (this.containsRestrictionType("Cardinality")) {
 			return Integer.parseInt(this.getRestrictionElement("Cardinality")
 					.getRestrictionValue());
 		} else {
			return Particle.UNBOUNDED;
 		}
 	}
 
 	public String getLocalName() {
 		int index = this._name.indexOf("#");
 		return this._name.substring(index + 1);
 	}
 
 	public String getLocalTypeName() {
 		int index = this._type.indexOf("#");
 		return this._type.substring(index + 1);
 	}
 
 	public String getInheritedByLocalName() {
 		if (this._inheritedBy != null) {
 			int index = this._inheritedBy.indexOf("#");
 			return this._inheritedBy.substring(index + 1);
 		} else {
 			return "";
 		}
 	}
 
 	// private String convertName2LocalName(String name) {
 	// int index = name.indexOf("#");
 	// return name.substring(index+1);
 	// }
 
 	public Vector<String> getRestrictedRange() {
 		return this._restrictedRange;
 	}
 
 	public void setOwlSource(String owlsource) {
 		this._owlSrc = owlsource;
 	}
 
 	public void setName(String name) {
 		this._name = name;
 	}
 
 	public void setType(String type) {
 		this._type = type;
 	}
 
 	public void setLevel(int level) {
 		this._level = level;
 	}
 
 	public void setInheritedBy(String inheritedBy) {
 		this._inheritedBy = inheritedBy;
 	}
 
 	public void setComplementRange(Vector<String> complementRange) {
 		this._complementRange = complementRange;
 	}
 
 	public void setRestrictions(
 			Vector<AbstractDatatypeRestrictionElement> restrictions) {
 		this._restrictions = restrictions;
 	}
 
 	public void setRestrictedRange(Vector<String> restrictedRange) {
 		this._restrictedRange = restrictedRange;
 	}
 
 	/**
 	 * In UNION case we won't remove the hasValue element restriction
 	 */
 	public void removeHasValueElement() {
 		for (int i = 0; i < this._restrictions.size(); i++) {
 			String curtype = this._restrictions.get(i).getRestrictionType();
 			if (curtype.equals("hasValue")) {
 				this._restrictions.removeElementAt(i);
 			}
 		}
 	}
 
 	public void addRestrictionElement(
 			AbstractDatatypeRestrictionElement restrictionElement) {
		System.out.println("[i] addRestrictionElement to " + this +  ": "
 				+ restrictionElement.toString());
 
 		if (restrictionElement.getRestrictionType().equals("MinCardinality")) {
 			for (int i = 0; i < this._restrictions.size(); i++) {
 				String curtype = this._restrictions.get(i).getRestrictionType();
 				if (curtype.equals("MinCardinality")) {
 					this._restrictions.removeElementAt(i);
 				}
 			}
 		} else if (restrictionElement.getRestrictionType().equals(
 				"MaxCardinality")) {
 			for (int i = 0; i < this._restrictions.size(); i++) {
 				String curtype = this._restrictions.get(i).getRestrictionType();
 				if (curtype.equals("MaxCardinality")) {
 					this._restrictions.removeElementAt(i);
 				}
 			}
 		} else if (restrictionElement.getRestrictionType()
 				.equals("Cardinality")) {
 			for (int i = 0; i < this._restrictions.size(); i++) {
 				String curtype = this._restrictions.get(i).getRestrictionType();
 				if (curtype.equals("Cardinality")) {
 					this._restrictions.removeElementAt(i);
 				}
 			}
 		}
 
 		this._restrictions.add(restrictionElement);
 	}
 
 	public void clearAllValuesFromRestrictions() {
 		System.out.println("[i] clearAllValuesFromRestrictions");
 		for (int i = 0; i < this._restrictions.size(); i++) {
 			String curtype = this._restrictions.get(i).getRestrictionType();
 			if (curtype.equals("allValuesFrom")) {
 				System.out.println("[i] remove old/obsolete restriction: "
 						+ this._restrictions.get(i).toString());
 				this._restrictions.remove(i);
 				--i; // more elements possible
 			}
 		}
 	}
 
 	public void clearSomeValuesFromRestrictions() {
 		for (int i = 0; i < this._restrictions.size(); i++) {
 			String curtype = this._restrictions.get(i).getRestrictionType();
 			if (curtype.equals("someValuesFrom")) {
 				System.out.println("[i] remove old/obsolete restriction: "
 						+ this._restrictions.get(i).toString());
 				this._restrictions.remove(i);
 				--i; // more elements possible
 			}
 		}
 	}
 
 	// public void addSomeValuesFromIndividual(String uri) {
 	// this._allValuesFromIndividuals.add(uri);
 	// }
 	//
 	// public void addAllValuesFromIndividual(String uri) {
 	// this._someValuesFromIndividuals.add(uri);
 	// }
 
 	public void addToComplementRange(String uri) {
 		this._complementRange.add(uri);
 	}
 
 	public void addToRestrictedRange(String uri) {
 		if (!this._restrictedRange.contains(uri)) {
 			System.out.println("[i] Element " + this.getName()
 					+ ", addToRestrictedRange: " + uri);
 			this._restrictedRange.add(uri);
 		} else {
 			System.out.println("[i] Element " + this.getName()
 					+ " already contains restricted range: " + uri);
 		}
 	}
 
 	public void removeComplementsFromRestrictedRange() {
 		// System.out.println("SIZE OF RESTRICTEDRANGE VECTOR: "+this._restrictedRange.size());
 		for (Iterator<String> it2 = this._complementRange.iterator(); it2
 				.hasNext();) {
 			String key = it2.next().toString();
 			// System.out.println("INDEX TO REMOVE: "+this._restrictedRange.indexOf(key));
 			// this._restrictedRange.removeElementAt(this._restrictedRange.indexOf(key));
 			this._restrictedRange.remove(key);
 		}
 	}
 
 	public void emptyRestrictedRange() {
 		System.out.println("[i] emptyRestrictedRange for Element " + this);
 		this._restrictedRange.removeAllElements();
 	}
 
 	public String toString() {
 		return "[" + this._owlSrc + ", " + this._level + "] " + this._name
 				+ " (" + this._type + ")";
 	}
 
 	public void printData() {
 		System.out.println("             NAME: " + this._name + " ("
 				+ this._owlSrc + ")");
 		System.out.println("             TYPE: " + this._type);
 		if (this._inheritedBy != null) {
 			System.out.println("            LEVEL: " + this._level);
 			System.out.println("     INHERITED BY: " + this._inheritedBy);
 		}
 		for (int i = 0; i < this._restrictions.size(); i++) {
 			AbstractDatatypeRestrictionElement relem = this._restrictions
 					.get(i);
 			System.out.println("      RESTRICTION: "
 					+ relem.getRestrictionType() + " Val: "
 					+ relem.getRestrictionValue() + " InheritedBy: "
 					+ relem.getInheritedBy() + " Level: " + relem.getLevel());
 		}
 		for (Iterator<String> it = this._restrictedRange.iterator(); it
 				.hasNext();) {
 			String val = it.next().toString();
 			System.out.println("RESTRICTION RANGE: " + val);
 		}
 		for (Iterator<String> it = this._complementRange.iterator(); it
 				.hasNext();) {
 			String val = it.next().toString();
 			System.out.println(" COMPLEMENT RANGE: " + val);
 		}
 	}
 
 	public boolean isPrimitive() {
 		if (this._type.contains("#")) {
 			return this._type.split("#")[0]
 					.equals("http://www.w3.org/2001/XMLSchema");
 		} else {
 			System.out
 					.println("[AbstractDatatypeElement] isPrimitive = false ("
 							+ this._type + ")");
 			return false;
 		}
 	}
 }
 
 class AbstractDatatypeRestrictionElementTypeComparer implements
 		Comparator<AbstractDatatypeRestrictionElement> {
 	public int compare(AbstractDatatypeRestrictionElement obj1,
 			AbstractDatatypeRestrictionElement obj2) {
 		String s1 = obj1.getRestrictionType(); // getUrl();
 		String s2 = obj2.getRestrictionType();
 
 		return s1.compareTo(s2);
 	}
 }
