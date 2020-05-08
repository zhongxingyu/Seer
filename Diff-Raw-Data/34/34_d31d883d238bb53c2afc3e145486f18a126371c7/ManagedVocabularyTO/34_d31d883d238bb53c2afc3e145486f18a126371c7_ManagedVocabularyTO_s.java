 package es.uc3m.inf.kr.oslcrm.to;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 
 
 
 
 
 
 
 
 import org.eclipse.lyo.oslc4j.core.OSLC4JConstants;
 import org.eclipse.lyo.oslc4j.core.annotation.OslcDescription;
 import org.eclipse.lyo.oslc4j.core.annotation.OslcName;
 import org.eclipse.lyo.oslc4j.core.annotation.OslcNamespace;
 import org.eclipse.lyo.oslc4j.core.annotation.OslcPropertyDefinition;
 import org.eclipse.lyo.oslc4j.core.annotation.OslcRange;
 import org.eclipse.lyo.oslc4j.core.annotation.OslcReadOnly;
 import org.eclipse.lyo.oslc4j.core.annotation.OslcRepresentation;
 import org.eclipse.lyo.oslc4j.core.annotation.OslcResourceShape;
 import org.eclipse.lyo.oslc4j.core.annotation.OslcTitle;
 import org.eclipse.lyo.oslc4j.core.annotation.OslcValueType;
 import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
 import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
 import org.eclipse.lyo.oslc4j.core.model.Representation;
 import org.eclipse.lyo.oslc4j.core.model.ValueType;
 
 import es.uc3m.inf.kr.oslcrm.Constants;
 import es.uc3m.inf.kr.oslcrm.SKOS;
 
 @OslcNamespace(Constants.REQUIREMENTS_MANAGEMENT_NAMESPACE)
 @OslcName("VocabularyManagement") 
 @OslcResourceShape(title = "Vocabulary management Resource Shape", describes = Constants.KM_TYPE_VOCABULARY_ELEMENT)
 public class ManagedVocabularyTO extends AbstractResource{
 
 	   private final List<Person>   authors = new ArrayList<Person>();
 	   private Date creationDate;
 	   private Date modifiedDate;
 	   private String rdfsLabel;
 	   private String prefLabel;
 	   private String altLabel;
 	   private String hiddenLabel;
 	   private String changeNote;
 	   private String editorialNote;
 	   private String historyNote;
 	   private String scopeNote;
 	   private String notation;
 	   private String subject;
 	   private String level;
 	   private String inScheme;
 	   private final List<ManagedVocabularyTO>   narrower = new ArrayList<ManagedVocabularyTO>();
 	   //private VocabularyTO related;
 	   
 	   
 	   
 	   public void addContributor(final Person contributor){
 	        this.authors.add(contributor);
 	    }
 	   
	    @OslcDescription("The person(s) who are responsible for the work needed to manage a requiremen.")
	    @OslcName("author")
	    @OslcPropertyDefinition(Constants.DCTERMS_CREATOR)
	    @OslcRepresentation(Representation.Inline)
	    @OslcValueType(ValueType.LocalResource)
	    @OslcRange(Constants.FOAF_PERSON)
	    @OslcTitle("Contributors")
 	    public List<Person> getAuthors(){
 	        return authors;
 	    }
 	    
 	    
 	    @OslcDescription("The data in which the element was created")
 	    @OslcName("created")
 	    @OslcPropertyDefinition(Constants.DCTERMS_CREATED)
 	    @OslcRepresentation(Representation.Inline)
 	    @OslcValueType(ValueType.DateTime)
 	    @OslcTitle("Date")
 	    public Date getCreationDate(){
 	    	return this.creationDate;
 	    }
 	    
 	    public void setCreationDate(Date date){
 	    	this.creationDate = date;
 	    }
 	    
 
 	    @OslcDescription("The data in which the element was modified")
 	    @OslcName("modified")
 	    @OslcPropertyDefinition(Constants.DCTERMS_MODIFIED)
 	    @OslcRepresentation(Representation.Inline)
 	    @OslcValueType(ValueType.DateTime)
 	    @OslcTitle("DateModified")
 	    public Date getModifiedDate(){
 	    	return this.creationDate;
 	    }
 	    
 	    public void setModifiedDate(Date date){
 	    	this.creationDate = date;
 	    }
 	    
 	    
 	    @OslcDescription("A default Linked Data Label")
 	    @OslcName("label")
 	    @OslcPropertyDefinition(OslcConstants.RDFS_NAMESPACE + "label")
 	    @OslcRepresentation(Representation.Inline)
 	    @OslcValueType(ValueType.String)
 	    @OslcTitle("label")
 	    public String getRDFSLabel(){
 	    	return this.rdfsLabel;
 	    }
 	    
 	    public void setRDFSLabel(String rdfsLabel){
 	    	this.rdfsLabel = rdfsLabel;
 	    }
 
 	    @OslcDescription("A preferred Label")
 	    @OslcName("prefLabel")
 	    @OslcPropertyDefinition(SKOS.PREF_LABEL)
 	    @OslcRepresentation(Representation.Inline)
 	    @OslcValueType(ValueType.String)
 	    @OslcReadOnly
 	    @OslcTitle("prefLabel")
 		public String getPrefLabel() {	    	
 			return prefLabel;
 		}
 
 		public void setPrefLabel(String prefLabel) {
 			this.prefLabel = prefLabel;
 		}
 
 		@OslcDescription("An alternative Label")
 	    @OslcName("altLabel")
 	    @OslcPropertyDefinition(SKOS.ALT_LABEL)
 	    @OslcRepresentation(Representation.Inline)
 	    @OslcValueType(ValueType.String)
 	    @OslcReadOnly
 	    @OslcTitle("altLabel")
 		public String getAltLabel() {
 			return altLabel;
 		}
 
 		public void setAltLabel(String altLabel) {
 			this.altLabel = altLabel;
 		}
 
 		@OslcDescription("A hidden Label")
 	    @OslcName("hiddenLabel")
 	    @OslcPropertyDefinition(SKOS.HIDDEN_LABEL)
 	    @OslcRepresentation(Representation.Inline)
 	    @OslcValueType(ValueType.String)
 	    @OslcReadOnly
 	    @OslcTitle("hiddenLabel")
 		public String getHiddenLabel() {
 			return hiddenLabel;
 		}
 
 		public void setHiddenLabel(String hiddenLabel) {
 			this.hiddenLabel = hiddenLabel;
 		}
 
 		@OslcDescription("A change note")
 	    @OslcName("changeNote")
 	    @OslcPropertyDefinition(SKOS.CHANGE_NOTE)
 	    @OslcRepresentation(Representation.Inline)
 	    @OslcValueType(ValueType.String)
 	    @OslcReadOnly
 	    @OslcTitle("changeNote")
 		public String getChangeNote() {
 			return changeNote;
 		}
 
 		public void setChangeNote(String changeNote) {
 			this.changeNote = changeNote;
 		}
 
 		@OslcDescription("An editorial note")
 	    @OslcName("editorialNote")
 	    @OslcPropertyDefinition(SKOS.EDITORIAL_NOTE)
 	    @OslcRepresentation(Representation.Inline)
 	    @OslcValueType(ValueType.String)
 	    @OslcReadOnly
 	    @OslcTitle("editorialNote")
 		public String getEditorialNote() {
 			return editorialNote;
 		}
 
 		public void setEditorialNote(String editorialNote) {
 			this.editorialNote = editorialNote;
 		}
 
 		@OslcDescription("A history note")
 	    @OslcName("historyNote")
 	    @OslcPropertyDefinition(SKOS.HISTORY_NOTE)
 	    @OslcRepresentation(Representation.Inline)
 	    @OslcValueType(ValueType.String)
 	    @OslcReadOnly
 	    @OslcTitle("historyNote")
 		public String getHistoryNote() {
 			return historyNote;
 		}
 
 		public void setHistoryNote(String historyNote) {
 			this.historyNote = historyNote;
 		}
 
 		@OslcDescription("A scope note")
 	    @OslcName("scopeNote")
 	    @OslcPropertyDefinition(SKOS.SCOPE_NOTE)
 	    @OslcRepresentation(Representation.Inline)
 	    @OslcValueType(ValueType.String)
 	    @OslcReadOnly
 	    @OslcTitle("scopeNote")
 		public String getScopeNote() {
 			return scopeNote;
 		}
 
 		public void setScopeNote(String scopeNote) {
 			this.scopeNote = scopeNote;
 		}
 
 		@OslcDescription("A notation code")
 	    @OslcName("notation")
 	    @OslcPropertyDefinition(SKOS.NOTATION)
 	    @OslcRepresentation(Representation.Inline)
 	    @OslcValueType(ValueType.String)
 	    @OslcReadOnly
 	    @OslcTitle("notation")
 		public String getNotation() {
 			return notation;
 		}
 
 		public void setNotation(String notation) {
 			this.notation = notation;
 		}
 
 		@OslcDescription("A subject code")
 	    @OslcName("subject")
 	    @OslcPropertyDefinition(OslcConstants.DCTERMS_NAMESPACE + "subject")
 	    @OslcRepresentation(Representation.Inline)
 	    @OslcValueType(ValueType.String)
 	    @OslcReadOnly
 	    @OslcTitle("subject")
 		public String getSubject() {
 			return subject;
 		}
 
 		public void setSubject(String subject) {
 			this.subject = subject;
 		}
 
 		@OslcDescription("A level code")
 	    @OslcName("level")
 	    @OslcPropertyDefinition(Constants.KM_NAMESPACE + "level")
 	    @OslcRepresentation(Representation.Inline)
 	    @OslcValueType(ValueType.String)
 	    @OslcReadOnly
 	    @OslcTitle("level")
 		public String getLevel() {
 			return level;
 		}
 
 		public void setLevel(String level) {
 			this.level = level;
 		}
 	    
 		@OslcDescription("An scheme URI")
 	    @OslcName("inScheme")
 	    @OslcPropertyDefinition(Constants.KM_NAMESPACE + "inScheme")
 	    @OslcRepresentation(Representation.Inline)
 	    @OslcValueType(ValueType.Resource)
 	    @OslcReadOnly
 	    @OslcTitle("inScheme")
 		public String getInScheme() {
 			return inScheme;
 		}
 
 		public void setInScheme(String inScheme) {
 			this.inScheme = inScheme;
 		}
 
 		public String getRdfsLabel() {
 			return rdfsLabel;
 		}
 
 		public void setRdfsLabel(String rdfsLabel) {
 			this.rdfsLabel = rdfsLabel;
 		}
 
 		@OslcDescription("An narrower element URI")
 	    @OslcName("narrower")
 	    @OslcPropertyDefinition(SKOS.NARROWER)
 	    @OslcRepresentation(Representation.Inline)
 	    @OslcValueType(ValueType.Resource)
 	    @OslcReadOnly
 	    @OslcTitle("narrower")
 		public List<ManagedVocabularyTO> getNarrower() {
 			return narrower;
 		}
 		
 		
 }
