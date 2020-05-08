 package com.github.easydoc.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * An easydoc documentation entry (the one within 
  * easydoc-start and easydoc-end tags).
  * This is called a "doc".
  * 
  * @author Viktor Kazakov (weekens@gmail.com)
  *
  */
 public class Doc implements Comparable<Doc> {
 	/**
 	 * An actual doc text.
 	 */
 	private StringBuilder text = new StringBuilder();
 	
 	/**
 	 * A raw params of this doc.
 	 * 
 	 * TODO: for now, we put the parse results in the model.
 	 * This is wrong. We should use some other entities for that.
 	 * The model shouldn't be in the transient state.
 	 */
 	private Map<String, String> params = new HashMap<String, String>();
 	
 	/**
 	 * A user-defined id.
 	 * This can be used afterwards to reference this doc. 
 	 */
 	private String id;
 	
 	/**
 	 * A parent doc for this doc (if present).
 	 */
 	private Doc parent;
 	
 	/**
 	 * A child docs for this doc.
 	 */
 	private List<Doc> children = new ArrayList<Doc>();
 	
 	/**
 	 * A link to the source file, where this doc originates.
 	 */
 	private SourceLink sourceLink;
 	
 	/**
 	 * A doc's weight. The higher the weight, the lower this 
 	 * doc will be among the siblings.
 	 */
 	private Integer weight;
 	
 	private List<Directive> directives = Collections.emptyList();
 
 	public String getText() {
 		return text.toString();
 	}
 
 	public void setText(String text) {
 		this.text = new StringBuilder(text);
 	}
 	
 	public Map<String, String> getParams() {
 		return params;
 	}
 
 	public void setParams(Map<String, String> params) {
 		this.params = params;
 	}
 
 	public void appendText(String value) {
 		text.append(value);
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public Doc getParent() {
 		return parent;
 	}
 
 	public void setParent(Doc parent) {
 		this.parent = parent;
 	}
 	
 	public List<Doc> getChildren() {
 		return children;
 	}
 	
 	public void addChild(Doc child) {
 		children.add(child);
 	}
 
 	public SourceLink getSourceLink() {
 		return sourceLink;
 	}
 
 	public void setSourceLink(SourceLink sourceLink) {
 		this.sourceLink = sourceLink;
 	}
 
 	public Integer getWeight() {
 		return weight;
 	}
 
 	public void setWeight(Integer weight) {
 		this.weight = weight;
 	}
 
 	public List<Directive> getDirectives() {
 		return directives;
 	}
 
 	public void setDirectives(List<Directive> directives) {
 		this.directives = directives;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("Doc [text=%s, params=%s, id=%s, parent=%s, sourceLink=%s, weight=%d]", 
 				text, params, id,
 				(parent != null ? parent.getId() : null), sourceLink, weight);
 	}
 
 	@Override
 	public int compareTo(Doc doc) {
 		Integer thisWeight = (weight != null ? weight : 0);
 		return thisWeight.compareTo(doc.getWeight() != null ? doc.getWeight() : 0);
 	}
 }
