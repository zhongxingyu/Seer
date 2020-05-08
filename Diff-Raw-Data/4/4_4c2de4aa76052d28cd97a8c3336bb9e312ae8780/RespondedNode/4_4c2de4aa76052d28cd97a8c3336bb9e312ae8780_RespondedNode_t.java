 package org.vamdc.portal.entity.query;
 
 import java.io.Serializable;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
 @Entity
 public class RespondedNode implements Serializable{
 
 
 	private static final long serialVersionUID = 6642943670608347366L;
 	
 	private Integer recordID;
 	private String ivoaID;
 	//private Map<CountHeader, Integer> resultCount;
 	private Query query;
 	
 	@Id @GeneratedValue
 	public Integer getRecordID() { return recordID; }
 	public void setrecordID(Integer recordID) { this.recordID = recordID; }
 	
 	public String getNodeIvoaID() { return ivoaID; }
 	public void setNodeIvoaID(String ivoaID) { this.ivoaID = ivoaID; }
 	
 	//public Map<CountHeader, Integer> getResultCount() { return resultCount;	}
 	//public void setResultCount(Map<CountHeader, Integer> resultCount) { this.resultCount = resultCount;	}
 	
 	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name="queryID")
 	public Query getQuery(){ return query; }
 	public void setQuery(Query query){ this.query=query; }
 
 }
