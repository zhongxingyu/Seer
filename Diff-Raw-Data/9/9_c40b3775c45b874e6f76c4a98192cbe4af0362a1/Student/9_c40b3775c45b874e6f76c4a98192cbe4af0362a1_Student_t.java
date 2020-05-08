 package org.avangarde.gnosis.entity;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import org.avangarde.gnosis.vo.*;
 
 /**
  *
  * @author Alexander
  */
 @Entity
 @Table(name = "student")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "Student.findAll", query = "SELECT s FROM Student s"),
     @NamedQuery(name = "Student.findByUserName", query = "SELECT s FROM Student s WHERE s.userName = :userName"),
     @NamedQuery(name = "Student.findByEmail", query = "SELECT s FROM Student s WHERE s.email = :email"),
     @NamedQuery(name = "Student.findByCareer", query = "SELECT s FROM Student s WHERE s.career = :career"),})
 public class Student implements Serializable, IEntity<StudentVo> {
 
     private static final long serialVersionUID = 1L;
     @Id
     @Column(name = "id")
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private int id;
     @Column(name = "userName")
     private String userName;
     @Column(name = "firstName")
     private String firstName;
     @Column(name = "lastName")
     private String lastName;
     @Column(name = "email")
     private String email;
     @Column(name = "password")
     private String password;
     @Column(name = "career")
     private String career;
     @Column(name = "url_Photo")
     private String urlPhoto;
     @JoinTable(name = "student_studygroup", joinColumns = {
         @JoinColumn(name = "Student_studentId", referencedColumnName = "studentId")}, inverseJoinColumns = {
         @JoinColumn(name = "StudyGroup_idStudyGroup", referencedColumnName = "idStudyGroup")})
     @ManyToMany
     private List<Studygroup> studygroupList;
     @JoinTable(name = "student_has_tutor_subject", joinColumns = {
         @JoinColumn(name = "Student_studentId", referencedColumnName = "studentId")}, inverseJoinColumns = {
         @JoinColumn(name = "Tutor_Subject_Tutor_tutorId", referencedColumnName = "Tutor_tutorId"),
         @JoinColumn(name = "Tutor_Subject_Subject_code", referencedColumnName = "Subject_code")})
     @ManyToMany
     private List<TutorSubject> tutorSubjectList;
     @JoinTable(name = "student_subject", joinColumns = {
         @JoinColumn(name = "Student_studentId", referencedColumnName = "studentId")}, inverseJoinColumns = {
         @JoinColumn(name = "Subject_code", referencedColumnName = "code")})
     @ManyToMany
     private List<Subject> subjectList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "student")
     private List<Topic> topicList;
     @JoinColumn(name = "Program_code", referencedColumnName = "code", insertable = false, updatable = false)
     @ManyToOne(optional = false)
     private Program program;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "student")
     private List<Event> eventList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "student")
     private List<Tutor> tutorList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "student")
     private List<LikeDislike> likeDislikeList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "student")
     private List<Comment> commentList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "student")
     private List<Activity> activityList;
     @OneToMany(cascade = CascadeType.ALL, mappedBy = "student")
     private List<Publication> publicationList;
 
     public Student() {
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getUserName() {
         return userName;
     }
 
     public void setUserName(String userName) {
         this.userName = userName;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getCareer() {
         return career;
     }
 
     public void setCareer(String career) {
         this.career = career;
     }
 
     public String getUrlPhoto() {
         return urlPhoto;
     }
 
     public void setUrlPhoto(String urlPhoto) {
         this.urlPhoto = urlPhoto;
     }
 
     @XmlTransient
     public List<Studygroup> getStudygroupList() {
         return studygroupList;
     }
 
     public void setStudygroupList(List<Studygroup> studygroupList) {
         this.studygroupList = studygroupList;
     }
 
     @XmlTransient
     public List<TutorSubject> getTutorSubjectList() {
         return tutorSubjectList;
     }
 
     public void setTutorSubjectList(List<TutorSubject> tutorSubjectList) {
         this.tutorSubjectList = tutorSubjectList;
     }
 
     @XmlTransient
     public List<Subject> getSubjectList() {
         return subjectList;
     }
 
     public void setSubjectList(List<Subject> subjectList) {
         this.subjectList = subjectList;
     }
 
     @XmlTransient
     public List<Topic> getTopicList() {
         return topicList;
     }
 
     public void setTopicList(List<Topic> topicList) {
         this.topicList = topicList;
     }
 
     public Program getProgram() {
         return program;
     }
 
     public void setProgram(Program program) {
         this.program = program;
     }
 
     @XmlTransient
     public List<Event> getEventList() {
         return eventList;
     }
 
     public void setEventList(List<Event> eventList) {
         this.eventList = eventList;
     }
 
     @XmlTransient
     public List<Tutor> getTutorList() {
         return tutorList;
     }
 
     public void setTutorList(List<Tutor> tutorList) {
         this.tutorList = tutorList;
     }
 
     @XmlTransient
     public List<LikeDislike> getLikeDislikeList() {
         return likeDislikeList;
     }
 
     public void setLikeDislikeList(List<LikeDislike> likeDislikeList) {
         this.likeDislikeList = likeDislikeList;
     }
 
     @XmlTransient
     public List<Comment> getCommentList() {
         return commentList;
     }
 
     public void setCommentList(List<Comment> commentList) {
         this.commentList = commentList;
     }
 
     @XmlTransient
     public List<Activity> getActivityList() {
         return activityList;
     }
 
     public void setActivityList(List<Activity> activityList) {
         this.activityList = activityList;
     }
 
     @XmlTransient
     public List<Publication> getPublicationList() {
         return publicationList;
     }
 
     public void setPublicationList(List<Publication> publicationList) {
         this.publicationList = publicationList;
     }
 
     @Override
     public StudentVo toVo() {
         StudentVo vo = new StudentVo();
 
         //atributos
         vo.setCareer(getCareer());
         vo.setEmail(getEmail());
         vo.setFirstName(getFirstName());
         vo.setLastName(getLastName());
         vo.setId(getId());
         vo.setPassword(getPassword());
        vo.setProgramId(getProgram().getCode());
         vo.setUrlPhoto(getUrlPhoto());
         vo.setUserName(getUserName());
 
         //Listas
 
         ArrayList<StudygroupVo> listStudygroupVo = new ArrayList<StudygroupVo>();
         for (Studygroup entity : getStudygroupList()) {
              listStudygroupVo.add(entity.toVo());
         }
         
         
         
         ArrayList<TutorSubjectVo> listTutorSubjectVo = new ArrayList<TutorSubjectVo>();
         for (TutorSubject entity : getTutorSubjectList()) {
             listTutorSubjectVo.add(entity.toVo());
         }
         
         ArrayList<SubjectVo> listSubjectVo = new ArrayList<SubjectVo>();
         for (Subject entity : getSubjectList()) {
             listSubjectVo.add(entity.toVo());
         }
         
         ArrayList<TopicVo> listTopicVo = new ArrayList<TopicVo>();
         for (Topic entity : getTopicList()) {
             listTopicVo.add(entity.toVo());
         }
         
         ArrayList<EventVo> listEventVo = new ArrayList<EventVo>();
         for (Event entity : getEventList()) {
             listEventVo.add(entity.toVo());
         }
         
         ArrayList<TutorVo> listTutorVo = new ArrayList<TutorVo>();
         for (Tutor entity : getTutorList()) {
             listTutorVo.add(entity.toVo());
         }
         
         ArrayList<LikeDislikeVo> listLikeDislikeVo = new ArrayList<LikeDislikeVo>();
         for (LikeDislike entity : getLikeDislikeList()) {
             listLikeDislikeVo.add(entity.toVo());
         }
         
         ArrayList<CommentVo> listCommentVo = new ArrayList<CommentVo>();
         for (Comment entity : getCommentList()) {
             listCommentVo.add(entity.toVo());
         }
         
         ArrayList<ActivityVo> listActivityVo = new ArrayList<ActivityVo>();
         for (Activity entity : getActivityList()) {
             listActivityVo.add(entity.toVo());
         }
         
         ArrayList<PublicationVo> listPublicationVo = new ArrayList<PublicationVo>();
         for (Publication entity : getPublicationList()) {
             listPublicationVo.add(entity.toVo());
         }
              
         vo.setStudygroupList(listStudygroupVo);
         vo.setTutorSubjectList(listTutorSubjectVo);
         vo.setSubjectList(listSubjectVo);
         vo.setTopicList(listTopicVo);
         vo.setEventList(listEventVo);
         vo.setTutorList(listTutorVo);
         vo.setLikeDislikeList(listLikeDislikeVo);
         vo.setCommentList(listCommentVo);
         vo.setActivityList(listActivityVo);
         vo.setPublicationList(listPublicationVo);
         
         return vo;
 
     }
 }
