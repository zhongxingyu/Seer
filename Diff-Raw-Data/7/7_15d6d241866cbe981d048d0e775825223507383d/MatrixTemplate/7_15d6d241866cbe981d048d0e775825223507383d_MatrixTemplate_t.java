 package org.synyx.minos.skillz.domain;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 
 import org.synyx.hades.domain.auditing.AbstractAuditable;
 import org.synyx.minos.core.domain.User;
 
 
 /**
 * A template to create {@link SkillMatrix} instances from. Used to enumerate a list of categories for different type of
 * subjects.
  * 
  * @author Oliver Gierke - gierke@synyx.de
  */
 @Entity
 public class MatrixTemplate extends AbstractAuditable<User, Long> {
 
     private static final long serialVersionUID = -2162561996804943402L;
 
     @Column(unique = true)
     private String name;
 
     private boolean isDefault;
 
     @ManyToMany
     private List<Category> categories = new ArrayList<Category>();
 
 
     protected MatrixTemplate() {
 
     }
 
 
     /**
      * Creates a new {@link MatrixTemplate} with the given name.
      * 
      * @param name
      */
     public MatrixTemplate(String name) {
 
         this.name = name;
     }
 
 
     /**
      * Returns the name of the template.
      * 
      * @return the name
      */
     public String getName() {
 
         return name;
     }
 
 
     /**
      * Sets the name of the template.
      * 
      * @param name the name to set
      */
     public void setName(String name) {
 
         this.name = name;
     }
 
 
     /**
      * @return the categories
      */
     public List<Category> getCategories() {
 
         return Collections.unmodifiableList(categories);
     }
 
 
     /**
      * @param categories the categories to set
      */
     public MatrixTemplate setCategories(List<Category> categories) {
 
         if (categories == null) {
            this.categories = new ArrayList<Category>();
         } else {
             this.categories = categories;
         }
 
         return this;
     }
 
 
     /**
      * @return the isDefault
      */
     public boolean isDefault() {
 
         return isDefault;
     }
 
 
     /**
      * @param isDefault the isDefault to set
      */
     public void setDefault(boolean isDefault) {
 
         this.isDefault = isDefault;
     }
 
 
     /**
      * Adds the given {@link Category} to the template.
      * 
      * @param category
      * @return
      */
     public MatrixTemplate add(Category category) {
 
         this.categories.add(category);
         return this;
     }
 }
