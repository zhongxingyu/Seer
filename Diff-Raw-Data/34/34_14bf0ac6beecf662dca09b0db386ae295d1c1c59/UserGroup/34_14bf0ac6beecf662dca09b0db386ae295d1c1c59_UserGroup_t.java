 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.jpa.entity.user;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 
 import org.openide.util.NbBundle;
 
 import java.io.Serializable;
 
 import java.util.HashSet;
 import java.util.Set;
 
import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 import de.cismet.cids.jpa.entity.common.CommonEntity;
 import de.cismet.cids.jpa.entity.common.Domain;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mscholl
  * @version  $Revision$, $Date$
  */
 @Entity
 @Table(name = "cs_ug")
 @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
 public class UserGroup extends CommonEntity implements Serializable {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final UserGroup NO_GROUP;
 
     static {
         NO_GROUP = new UserGroup();
         NO_GROUP.setId(-1);
         NO_GROUP.setName(NbBundle.getMessage(UserGroup.class, "UserGroup.<clinit>.NO_GROUP.name")); // NOI18N
         final Domain domain = new Domain();
         domain.setName("LOCAL");                                                                    // NOI18N
         NO_GROUP.setDomain(domain);
     }
 
     //~ Instance fields --------------------------------------------------------
 
     @Id
     @SequenceGenerator(
         name = "cs_ug_sequence",
         sequenceName = "cs_ug_sequence",
         allocationSize = 1
     )
     @GeneratedValue(
         strategy = GenerationType.SEQUENCE,
         generator = "cs_ug_sequence"
     )
     @Column(name = "id")
     private Integer id;
 
     @Column(name = "name")
     private String name;
 
     @Column(name = "descr")
     private String description;
 
    @Column(name = "prio")
    private int priority;

     @ManyToOne(
         optional = false,
        fetch = FetchType.EAGER,
        cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }
     )
     @JoinColumn(
         name = "domain",
         nullable = false
     )
     @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
     private Domain domain;
 
    @ManyToMany(
        fetch = FetchType.EAGER,
        cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH }
    )
     @JoinTable(
         name = "cs_ug_membership",
         joinColumns = { @JoinColumn(name = "ug_id") },
         inverseJoinColumns = { @JoinColumn(name = "usr_id") }
     )
     @Fetch(FetchMode.SUBSELECT)
     @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
     private Set<User> users;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new UserGroup object.
      */
     public UserGroup() {
         users = new HashSet<User>();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getName() {
         return name;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  name  DOCUMENT ME!
      */
     public void setName(final String name) {
         this.name = name;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  description  DOCUMENT ME!
      */
     public void setDescription(final String description) {
         this.description = description;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Domain getDomain() {
         return domain;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  domain  DOCUMENT ME!
      */
     public void setDomain(final Domain domain) {
         this.domain = domain;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         return getName() + "(" + getId() + ")"; // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Set<User> getUsers() {
         return users;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  users  DOCUMENT ME!
      */
     public void setUsers(final Set<User> users) {
         this.users = users;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Integer getId() {
         return id;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setId(final Integer id) {
         this.id = id;
     }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getPriority() {
        return priority;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  priority  DOCUMENT ME!
     */
    public void setPriority(final int priority) {
        this.priority = priority;
    }
 }
