 package org.dlw.ai.blackboard.rule;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.persistence.OneToMany;
 
 /**
 * This interface defines the signature knowledge source object. Any default
 * implementation e.g.
 * {@link org.dlw.ai.blackboard.knowledge.primitive.CommonPrefixKnowledgeSource}
 * extends {@link InferenceEngine} ultimately through the implementation of this
 * interface and the extension {@link org.dlw.ai.blackboard.BlackboardContext}
 * directly within the implementation.
  * 
  * @author <a href="mailto:dlwhitehurst@gmail.com">David L. Whitehurst</a>
  * @version 1.0.0-RC (hibernate-mysql branch)
  * 
  */
 @Entity
 @Table(name="ruleset")
 public class RuleSet {
 
     /**
      * unique serial identifier
      */
     private static final long serialVersionUID = 3094361637466019949L;
 
     /**
      * Attribute id or primary key
      */
     private Long id;
     
     /**
      * Attribute name of KnowledgeSource (type extension)
      */
     private String name;
     
 
     /**
      * Attribute to hold rules for KnowledgeSource
      */
     private List<Rule> rules = new ArrayList<Rule>();
     
 
     /**
      * @param rules
      *            the rules to set
      */
     public void setRules(List<Rule> rules) {
         this.rules = rules;
     }
 
     /**
      * @return the rules
      */
     @OneToMany(mappedBy="rset")
     public List<Rule> getRules() {
         return rules;
     }
 
     /**
      * @return the id
      */
     @Id @GeneratedValue(strategy=GenerationType.AUTO)
     public Long getId() {
         return id;
     }
 
     /**
      * @param id the id to set
      */
     public void setId(Long id) {
         this.id = id;
     }
 
     /**
      * @return the name
      */
     @Column(name="name",nullable=false,length=50)    
     public String getName() {
         return name;
     }
 
     /**
      * @param name the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
     
 }
