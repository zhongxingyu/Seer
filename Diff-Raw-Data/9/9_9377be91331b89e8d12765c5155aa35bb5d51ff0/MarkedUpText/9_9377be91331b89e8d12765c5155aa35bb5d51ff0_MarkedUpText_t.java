 package model;
 
 import java.util.LinkedList;
 import java.util.List;
 import javax.swing.tree.*;
 import java.util.Date;
 
 public class MarkedUpText extends DefaultMutableTreeNode implements Element, Nameable, Deletable {
 	private List<Comment> comments;
         private List<TagInstance> tagInstances;
 	private SourceText sourceText;
         private String name;
         private Project project;
 	
         /**
          * 
          * @param sourceText
          * @param folder 
          */
 	public MarkedUpText(SourceText sourceText, Project project){
             super(null,false);
             this.project = project;
             this.tagInstances = new LinkedList<TagInstance>();
             this.comments = new LinkedList<Comment>();
             this.sourceText = sourceText;
             this.name = sourceText.getPath();
 	}
  
 	@Override
 	public List<TagInstance> searchTags(List<TagType> tagTypes) {
             List<TagInstance> returnList = new LinkedList<TagInstance>();
             for(TagType t : tagTypes){
                     for(TagInstance u : tagInstances){
                         if(t == u.getTagType()){
                             returnList.add(u);
                         }
                     }
                 }
             return returnList;
         }
 
 	@Override
 	public List<Comment> searchComments(List<User> users) {
             List<Comment> returnList = new LinkedList<Comment>();
             for(User t : users){
                     for(Comment c : comments){
                         if(t.getName().equals(c.getOwner())){
                             returnList.add(c);
                         }
                     }
                 }
             return returnList;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
        
        public String getPrettyName(){
            return name.substring(name.lastIndexOf("/") + 1);
            //If you don't want the file extension:
            //return name.substring(name.lastIndexOf("/") + 1, name.lastIndexOf(".")); 
        }
 
 	@Override
 	public void rename(String name) {
 		this.name = name;
                 // TODO: Do stuff with files
 	}
 
 	@Override
 	public void delete() {
 		// TODO Auto-generated method stub
 		
 	}
         
         @Override
         public String toString(){
            return this.getPrettyName();
         }
         
         public SourceText getSourceText(){
             return sourceText;
         }
         
         /**
          * Use this method to add Tags to a text
          * @param tagType
          * @param selection 
          */
         public boolean addTag(TagType tagType, TextSection selection){
             Date dateAdded = new Date();
             Date dateModified = new Date();
             tagInstances.add(new TagInstance(project.getCurrentUser().getName(), dateAdded, dateModified,
             selection, this, tagType));
             return true;
         }
        
        /**
          * Use this method to add comments to a text
          * 
          * @param comment
          * @param selection 
          */ 
        public void addComment(String comment, TextSection selection){
             Date dateAdded = new Date();
             Date dateModified = new Date();
             comments.add(new Comment(project.getCurrentUser().getName(), dateAdded, dateModified,
             selection, comment, sourceText.getPath()));
         }
        
        public List<TagInstance> getTags(){
            return tagInstances;
        }
        
        public List<Comment> getComments(){
            return comments;
        }
 }
