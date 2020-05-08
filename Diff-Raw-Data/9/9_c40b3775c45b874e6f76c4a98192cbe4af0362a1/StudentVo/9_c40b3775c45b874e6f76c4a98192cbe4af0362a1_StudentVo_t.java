 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.avangarde.gnosis.vo;
 
 import java.util.List;
 
 /**
  *
  * @author Zergio
  */
 public class StudentVo implements IValueObject {
     //Atributos nativos
     private int id;
     private String userName;
     private String firstName;
     private String lastName;
     private String email;
     private String password;
     private String career;
     private String urlPhoto;
     //Atributo externo
     private Integer programCode;
     //Vos Adicionales
     private List<StudygroupVo> studygroupList;
     private List<TutorSubjectVo> tutorSubjectList;
     private List<SubjectVo> subjectList;
     private List<TopicVo> topicList;
     private List<EventVo> eventList;
     private List<TutorVo> tutorList;
     private List<LikeDislikeVo> likeDislikeList;
     private List<CommentVo> commentList;
     private List<ActivityVo> activityList;
     private List<PublicationVo> publicationList;
     //kaskdlaj
     public List<ActivityVo> getActivityList() {
         return activityList;
     }
 
     public void setActivityList(List<ActivityVo> activityList) {
         this.activityList = activityList;
     }
 
     public String getCareer() {
         return career;
     }
 
     public void setCareer(String career) {
         this.career = career;
     }
 
     public List<CommentVo> getCommentList() {
         return commentList;
     }
 
     public void setCommentList(List<CommentVo> commentList) {
         this.commentList = commentList;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public List<EventVo> getEventList() {
         return eventList;
     }
 
     public void setEventList(List<EventVo> eventList) {
         this.eventList = eventList;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public List<LikeDislikeVo> getLikeDislikeList() {
         return likeDislikeList;
     }
 
     public void setLikeDislikeList(List<LikeDislikeVo> likeDislikeList) {
         this.likeDislikeList = likeDislikeList;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public Integer getProgramCode() {
         return programCode;
     }
 
    public void setProgramId(Integer programId) {
         this.programCode = programCode;
     }
 
     public List<PublicationVo> getPublicationList() {
         return publicationList;
     }
 
     public void setPublicationList(List<PublicationVo> publicationList) {
         this.publicationList = publicationList;
     }
 
     public List<StudygroupVo> getStudygroupList() {
         return studygroupList;
     }
 
     public void setStudygroupList(List<StudygroupVo> studygroupList) {
         this.studygroupList = studygroupList;
     }
 
     public List<SubjectVo> getSubjectList() {
         return subjectList;
     }
 
     public void setSubjectList(List<SubjectVo> subjectList) {
         this.subjectList = subjectList;
     }
 
     public List<TopicVo> getTopicList() {
         return topicList;
     }
 
     public void setTopicList(List<TopicVo> topicList) {
         this.topicList = topicList;
     }
 
     public List<TutorVo> getTutorList() {
         return tutorList;
     }
 
     public void setTutorList(List<TutorVo> tutorList) {
         this.tutorList = tutorList;
     }
 
     public List<TutorSubjectVo> getTutorSubjectList() {
         return tutorSubjectList;
     }
 
     public void setTutorSubjectList(List<TutorSubjectVo> tutorSubjectList) {
         this.tutorSubjectList = tutorSubjectList;
     }
 
     public String getUrlPhoto() {
         return urlPhoto;
     }
 
     public void setUrlPhoto(String urlPhoto) {
         this.urlPhoto = urlPhoto;
     }
 
     public String getUserName() {
         return userName;
     }
 
     public void setUserName(String userName) {
         this.userName = userName;
     }
     
     
 }
