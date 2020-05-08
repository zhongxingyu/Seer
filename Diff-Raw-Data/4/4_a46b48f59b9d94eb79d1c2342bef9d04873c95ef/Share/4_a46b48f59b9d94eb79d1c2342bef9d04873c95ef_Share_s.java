 package x1.stomp.model;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 import javax.persistence.Version;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 import org.hibernate.validator.constraints.NotEmpty;
 
 @Entity
 @XmlRootElement
 @Table(name = "share", uniqueConstraints = @UniqueConstraint(columnNames = "key"))
 public class Share implements Serializable {
   private static final long serialVersionUID = -6219237799499789827L;
 
   @Id
  @GeneratedValue
   private Long id;
 
   @Version
   private Long version;
 
   @NotNull
   @Size(min = 1, max = 25)
   @Pattern(regexp = "[A-Z0-9.]*", message = "must contain only letters and spaces")
   @Column
   private String key;
 
   @NotNull
   @NotEmpty
   @Size(min = 1, max = 80)
   @Column(length = 80)
   private String name;
 
   @XmlTransient
   public Long getId() {
     return id;
   }
 
   public void setId(Long id) {
     this.id = id;
   }
 
   public String getKey() {
     return key;
   }
 
   public void setKey(String key) {
     this.key = key;
   }
 
   public String getName() {
     return name;
   }
 
   public void setName(String name) {
     this.name = name;
   }
 
   @XmlTransient
   public Long getVersion() {
     return version;
   }
 
   public void setVersion(Long version) {
     this.version = version;
   }
 
   @Override
   public String toString() {
     return "<Share [id=" + id + ", key=" + key + ", name=" + name + "]>";
   }
 }
