 package mantech.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
import javax.persistence.IdClass;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.PrimaryKeyJoinColumn;
 import javax.persistence.Table;
 
 /**
  * @author Long Nguyen
  * @version $Id: Complaint.java,v 1.0 2011/09/06 19:10:06 longnguyen Exp $
  */
 @Entity
 @Table(name = "complaint")
 public class Complaint implements Serializable {
 
   private static final long serialVersionUID = -7703177115415708202L;
 
   @Id
   @Column(name = "id")
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int id;
 
   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "userid", referencedColumnName = "id")
   private User user;
 
   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "equipment_id", referencedColumnName = "id")
   private Equipment equipment;
 
   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "status_id", referencedColumnName = "id", insertable = false)
   private ComplaintStatus status;
 
   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "priority_id", referencedColumnName = "id")
   private CategoryPriority priority;
 
   @Column(name = "title", nullable = false, length = 140)
   private String title;
 
   @Column(name = "[content]", nullable = false)
   private String content;
 
   @Column(name = "enddate")
   private Date endDate;
 
   @Column(name = "createdate", insertable = false)
   private Date createDate;
 
   @OneToOne(fetch = FetchType.LAZY)
   @PrimaryKeyJoinColumn(name = "complaint_id")
   private Assignment assignment;
 
   @OneToMany(fetch = FetchType.EAGER)
   @JoinColumn(name = "id")
   private List<AssignmentDetail> details;
 
   public Complaint() {}
 
   public int getId() {
     return id;
   }
 
   public void setId(int id) {
     this.id = id;
   }
 
   public User getUser() {
     return user;
   }
 
   public void setUser(User user) {
     this.user = user;
   }
 
   public Equipment getEquipment() {
     return equipment;
   }
 
   public void setEquipment(Equipment equipment) {
     this.equipment = equipment;
   }
 
   public ComplaintStatus getStatus() {
     return status;
   }
 
   public void setStatus(ComplaintStatus status) {
     this.status = status;
   }
 
   public CategoryPriority getPriority() {
     return priority;
   }
 
   public void setPriority(CategoryPriority priority) {
     this.priority = priority;
   }
 
   public String getTitle() {
     return title;
   }
 
   public void setTitle(String title) {
     this.title = title;
   }
 
   public String getContent() {
     return content;
   }
 
   public void setContent(String content) {
     this.content = content;
   }
 
   public Date getEndDate() {
     return endDate;
   }
 
   public void setEndDate(Date endDate) {
     this.endDate = endDate;
   }
 
   public Date getCreateDate() {
     return createDate;
   }
 
   public void setCreateDate(Date createDate) {
     this.createDate = createDate;
   }
 
   public Assignment getAssignment() {
     return assignment;
   }
 
   public void setAssignment(Assignment assignment) {
     this.assignment = assignment;
   }
 
   public List<AssignmentDetail> getDetails() {
     return details;
   }
 
   public void setDetails(List<AssignmentDetail> details) {
     this.details = details;
   }
 
 }
