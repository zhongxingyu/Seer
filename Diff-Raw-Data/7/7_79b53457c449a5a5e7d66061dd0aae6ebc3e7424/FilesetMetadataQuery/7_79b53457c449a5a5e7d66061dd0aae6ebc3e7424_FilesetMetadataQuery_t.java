 package pt.utl.ist.fenix.tools.file;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.tree.BaseElement;
 
 
 public class FilesetMetadataQuery implements XMLSerializable,Serializable {
 	private int start=0;
 	private int pageSize=10;
 	private List<MetadataQueryComponent> queries;
 
 	public FilesetMetadataQuery()
 	{
 		queries=new ArrayList<MetadataQueryComponent>();
 	}
 
 	public FilesetMetadataQuery addNextQuery(String queryField,String searchString,ConjunctionType nextQueryConjunction)
 	{
 		MetadataQuery query=new MetadataQuery();
 		query.setField(queryField);
 		query.setSearchString(searchString);
 		MetadataQueryComponent nextComponentOfQuery=new MetadataQueryComponent();
 		nextComponentOfQuery.setQuery(query);
 		nextComponentOfQuery.setNextQueryConjunction(nextQueryConjunction);
 		queries.add(nextComponentOfQuery);
 		return this;
 	}
 
 	public FilesetMetadataQuery addToPreviousQuery(ConjunctionType conjunctionWithPreviousQuery,String queryField,String searchString)
 	{
 		if(queries.size()>0)
 		{
 			queries.get(queries.size()-1).setNextQueryConjunction(conjunctionWithPreviousQuery);
 		}
 		MetadataQuery query=new MetadataQuery();
 		query.setField(queryField);
 		query.setSearchString(searchString);
 		MetadataQueryComponent nextComponentOfQuery=new MetadataQueryComponent();
 		nextComponentOfQuery.setQuery(query);
 		queries.add(nextComponentOfQuery);
 		return this;
 	}
 	
 	public Iterator<MetadataQueryComponent> iterator()
 	{
 		return queries.iterator();
 	}
 	
 	/**
 	 * @return the pageSize
 	 */
 	public int getPageSize() {
 		return pageSize;
 	}
 
 
 	/**
 	 * @param pageSize the pageSize to set
 	 */
 	public void setPageSize(int pageSize) {
 		this.pageSize = pageSize;
 	}
 
 
 	/**
 	 * @return the start
 	 */
 	public int getStart() {
 		return start;
 	}
 
 
 	/**
 	 * @param start the start to set
 	 */
 	public void setStart(int start) {
 		this.start = start;
 	}
 
 
 	
 	public static class MetadataQuery implements XMLSerializable,Serializable
 	{
 		public static final String ANY_FIELD="ANY";
 		private String field;
 		private String searchString;
 		/**
 		 * @return the field
 		 */
 		public String getField() {
 			return field;
 		}
 		/**
 		 * @param field the field to set
 		 */
 		public void setField(String field) {
 			this.field = field;
 		}
 		/**
 		 * @return the searchString
 		 */
 		public String getSearchString() {
 			return searchString;
 		}
 		/**
 		 * @param searchString the searchString to set
 		 */
 		public void setSearchString(String searchString) {
 			this.searchString = searchString;
 		}
 
 		public void fromXMLString(String xml) {
 			try {
 				fromXML(org.dom4j.DocumentHelper.parseText(xml).getRootElement());
 			}
 			catch (DocumentException e) {
 	            throw new RuntimeException("Error parsing xml string : "+xml,e);
 			}	
 		}
 		public String toXMLString() {
 			return toXML().asXML();
 		}
 		public void fromXML(Element el) {
 			setField(el.elementText("field"));
 			setSearchString(el.elementText("searchstring"));
 		}
 		public Element toXML() {
 			Element el=new BaseElement("query");
 			el.addElement("field").setText(getField());
 			el.addElement("searchstring").setText(getSearchString());
 			return el;
 		}
 		
 	}
 
	public static enum ConjunctionType
 	{
		OR,
		AND;
 
 	}
 	
 	public static class MetadataQueryComponent implements XMLSerializable,Serializable
 	{
 		private MetadataQuery query;
 		private ConjunctionType nextQueryConjunction;
 		/**
 		 * @return the nextQueryConjunction
 		 */
 		public ConjunctionType getNextQueryConjunction() {
 			return nextQueryConjunction;
 		}
 		/**
 		 * @param nextQueryConjunction the nextQueryConjunction to set
 		 */
 		public void setNextQueryConjunction(ConjunctionType nextQueryConjunction) {
 			this.nextQueryConjunction = nextQueryConjunction;
 		}
 		/**
 		 * @return the query
 		 */
 		public MetadataQuery getQuery() {
 			return query;
 		}
 		/**
 		 * @param query the query to set
 		 */
 		public void setQuery(MetadataQuery query) {
 			this.query = query;
 		}
 		
 		public void fromXMLString(String xml) {
 			try {
 				fromXML(org.dom4j.DocumentHelper.parseText(xml).getRootElement());
 			}
 			catch (DocumentException e) {
 	            throw new RuntimeException("Error parsing xml string : "+xml,e);
 			}	
 		}
 		public String toXMLString() {
 			return toXML().asXML();
 		}
 		public void fromXML(Element el) {
 			if(el.attribute("nextqueryconjunction")!=null)
 			{
 				String nextQueryConjunctionVal=el.attributeValue("nextqueryconjunction");
 				for(ConjunctionType enumVal:ConjunctionType.values())
 					if(enumVal.name().equals(nextQueryConjunctionVal))
 						setNextQueryConjunction(enumVal);
 			}
 			MetadataQuery query=new MetadataQuery();
 			query.fromXML((Element)el.elements().get(0));
 			setQuery(query);
 		}
 		public Element toXML() {
 			Element el=new BaseElement("metadataquerycomponent");
 			if(getNextQueryConjunction()!=null)
 				el.addAttribute("nextqueryconjunction", getNextQueryConjunction().name());
 			el.add(getQuery().toXML());
 			return el;
 		}
 	}
 
 	public void fromXMLString(String xml) {
 		try {
 			fromXML(org.dom4j.DocumentHelper.parseText(xml).getRootElement());
 		}
 		catch (DocumentException e) {
             throw new RuntimeException("Error parsing xml string : "+xml,e);
 		}
 	}
 
 	public String toXMLString() {
 		return toXML().asXML();
 	}
 	
 	public Element toXML()
 	{
 		Element el=new BaseElement("filesetmetadataquery");
 		el.addAttribute("start",""+getStart());
 		el.addAttribute("pagesize",""+getPageSize());
 		for(MetadataQueryComponent queryComponent:queries)
 		{
 			el.add(queryComponent.toXML());
 		}
 		return el;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void fromXML(Element el)
 	{
 		setStart(Integer.parseInt(el.attributeValue("start")));
 		setPageSize(Integer.parseInt(el.attributeValue("pagesize")));
 		for(Element elQueryComponent:(List<Element>)el.elements())
 		{
 			MetadataQueryComponent queryComponent=new MetadataQueryComponent();
 			queryComponent.fromXML(elQueryComponent);
 			queries.add(queryComponent);
 		}
 	}
 
 }
