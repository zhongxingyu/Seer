 package org.vamdc.portal.session.queryBuilder;
 
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.Collections;
 import java.util.EnumSet;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.portal.session.queryBuilder.forms.QueryForm;
 
 @Name("queryData")
 @Scope(ScopeType.CONVERSATION)
 public class QueryData {
 
 
	private Collection<QueryForm> forms=Collections.synchronizedList(new ArrayList<QueryForm>());
 	
 	private String comments="";
 	
 	private String editedQueryId;
 
 	public Collection<Restrictable> getKeywords(){
 		EnumSet<Restrictable> result = EnumSet.noneOf(Restrictable.class);
 		for (QueryForm form:forms){
 			result.addAll(form.getKeywords());
 		}
 		return result;
 			
 	}
 
 	public String getQueryString(){
 		
 		return "select * where "+getFormsQuery(); 
 	}
 	
 	private String getFormsQuery(){
 		String result = "";
 		for (QueryForm form:forms){
 			String queryPart = form.getQueryPart();
 			if (queryPart.length()>0){
 				if (result.length()>0)
 					result+=" AND "+queryPart;
 				else
 					result=queryPart;
 			}
 		}
 		return result;
 	}
 
 	public boolean isValid(){
 		return getKeywords().size()>0;
 	}
 
 	public Collection<QueryForm> getForms(){
		return forms;
 	}
 
 	public void addForm(QueryForm form){
		forms.add(form);
 	}
 	
 	public void deleteForm(QueryForm form){
		forms.remove(form);
 	}
 
 	public String getComments() {
 		return comments;
 	}
 
 	public void setComments(String comments) {
 		this.comments = comments;
 	}
 	
 	public String getEditQueryId() {
 		return editedQueryId;
 	}
 
 	public void setEditQueryId(String editQueryId) {
 		this.editedQueryId = editQueryId;
 	}
 
 	public void loadQuery(String queryString) {
 		// TODO load forms contents from the query string.
 		
 	}
 
 }
