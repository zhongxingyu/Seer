 package org.gemsjax.shared.communication.message.collaborateablefile;
 
 import java.util.Set;
 
 import org.gemsjax.shared.collaboration.Collaborateable;
 import org.gemsjax.shared.communication.CommunicationConstants;
 import org.gemsjax.shared.communication.message.collaborateablefile.CollaborateableType;
 
 /**
  * Create a new {@link CollaborateableType}
  * @author Hannes Dorfmann
  *
  */
 public class NewCollaborateableFileMessage extends ReferenceableCollaborateableFileMessage {
 	
 	
 	public static final String TAG = "new";
 	public static final String ATTRIBUTE_NAME="name";
 	public static final String ATTRIBUTE_PERMISSION="permission";
 	public static final String ATTRIBUTE_TYPE="type";
 	
 	
 	public static final String SUBTAG_KEYWORDS="keywords";
 	public static final String SUBTAG_COLLABORATORS="collaborators";
 	
 	public static final String SUBSUBTAG_ADD_COLLABORATOR="add";
 	public static final String ATTRIBUTE_COLLABORATOR_ID="id";
 	
 	private Collaborateable.Permission permission;
 	private String name;
 	
 	private Set<Integer> collaboratorIds;
 	private CollaborateableType type;
 	private String keywords;
 	
 	public NewCollaborateableFileMessage(String referenceId, String name, CollaborateableType type, Set<Integer> collaborators, Collaborateable.Permission permission, String keywords)
 	{
 		super(referenceId);
 		this.permission = permission;
 		this.name = name;
 		this.keywords = keywords;
 		this.collaboratorIds = collaborators;
		this.type = type;
		
 	}
 	
 	
 	public CollaborateableType getCollaborateableType()
 	{
 		return type;
 	}
 	
 
 	public Collaborateable.Permission getPermission() {	
 		return permission;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 
 	public Set<Integer> getCollaboratorIds() {
 		return collaboratorIds;
 	}
 	
 	public String getKeywords(){
 		return keywords;
 	}
 	
 	public CollaborateableType getType(){
 		return type;
 	}
 	
 	
 	@Override
 	public String toXml() {
 		String x=super.openingXml();
 		
 		x+="<"+TAG+" "+ATTRIBUTE_NAME+"=\""+name +"\" " +ATTRIBUTE_PERMISSION+"=\""+permission+"\" "+ATTRIBUTE_TYPE+"=\""+type.toConstant()+"\">";
 		
 		
 		x+= "<"+SUBTAG_KEYWORDS+">";
 		if (keywords!=null && !keywords.isEmpty())x+=keywords;
 		x+="</"+SUBTAG_KEYWORDS+">";
 		
 		x+="<"+SUBTAG_COLLABORATORS+">";
 		if (collaboratorIds!=null && !collaboratorIds.isEmpty()){
 				for (Integer id : collaboratorIds)
 					x+="<"+SUBSUBTAG_ADD_COLLABORATOR+" "+ATTRIBUTE_COLLABORATOR_ID+"=\""+id+"\" />";
 		}
 		
 		x+="</"+SUBTAG_COLLABORATORS+">";
 		
 		x+="</"+TAG+">"+super.closingXml();
 		return x;
 	}
 
 }
