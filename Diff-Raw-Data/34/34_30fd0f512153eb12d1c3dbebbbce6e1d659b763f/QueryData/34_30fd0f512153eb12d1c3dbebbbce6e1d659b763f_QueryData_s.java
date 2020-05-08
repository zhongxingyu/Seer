 package org.vamdc.portal.session.queryBuilder;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.EnumSet;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.portal.session.queryBuilder.forms.QueryForm;
 
 @Name("queryData")
 @Scope(ScopeType.CONVERSATION)
 public class QueryData {
 
 
	private Collection<QueryForm> forms=new ArrayList<QueryForm>();
 	
 	private String comments="";
 	
 	private String editedQueryId;
	
	private volatile boolean formsChanged=false;
 
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
		synchronized(forms){
			return forms;
		}
 	}
 
 	public void addForm(QueryForm form){
		synchronized(forms){
			forms.add(form);
			formsChanged=true;
		}
 	}
 	
 	public void deleteForm(QueryForm form){
		synchronized(forms){
			forms.remove(form);
			formsChanged=true;
		}
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
 
	public boolean isFormsChanged() {
		System.out.println("form changed "+formsChanged);
		return formsChanged;
	}
	
	public void formsReloaded(){
		this.formsChanged = false;
	}

 }
