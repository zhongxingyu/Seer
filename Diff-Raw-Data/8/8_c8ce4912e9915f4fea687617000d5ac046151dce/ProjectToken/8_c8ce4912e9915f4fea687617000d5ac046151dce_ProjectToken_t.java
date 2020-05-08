 package models;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 @Entity
 public final class ProjectToken extends Model {
 
     private static final long serialVersionUID = -3394657858296880119L;
 
     @ManyToOne(targetEntity = Project.class)
     public Project project;
     
     @Required
     public String text;
 
     public ProjectToken() {
         // do nothing
     }
     
     public ProjectToken(final String someText) {
         this.text = someText;
     }
     
     
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = super.hashCode();
         result = prime * result 
             + ((this.text == null) ? 0 : this.text.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
         if (this == obj) {
             return true;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         
         ProjectToken other = (ProjectToken) obj;
         if (this.text == null) {
             if (other.text != null) {
                 return false;
            } 
            
            return true;
         }
         
         if (!this.text.equals(other.text)) {
             return false;
         }
         
         return true;
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("ProjectToken [text=");
         builder.append(this.text);
         builder.append("]");
         return builder.toString();
     }
 }
