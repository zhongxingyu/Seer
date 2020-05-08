 package com.uah.main.dokeos;
 
 
 import com.uah.commons.DataBaseConnectionParameters;
 import com.uah.commons.DataBaseManagement;
 import com.uah.converters.DokeosConverter;
 import com.uah.dao.dokeos.*;
 import com.uah.dto.dokeos.*;
 import com.uah.exceptions.ConnectionParametersException;
 import com.uah.exceptions.OperationNotSupportedException;
 import com.uah.graph.MavselEdge;
 import com.uah.graph.MavselGraphManager;
 import com.uah.graph.MavselVertex;
 import com.uah.items.*;
 import edu.uci.ics.jung.graph.Graph;
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Pablo Sicilia
  * @version Mavsel Tool 1.0
  */
 public class DokeosLMS extends DataBaseManagement implements LMS {
 
     /**************************************************************************
     *                              ATTRIBUTES
     **************************************************************************/
     private DokeosConverter converter;
 
     
     /**************************************************************************
     *                              PUBLIC METHODS
     ***************************************************************************/
     
     /**
      * Constructor
      */
     public DokeosLMS() {
         this.converter = new DokeosConverter();
     }
 
     /**
      * Constructor
      *
      * @param dataBaseURL
      * @param dataBasePort
      * @param dataBaseName
      * @param dataBaseUser
      * @param dataBaseUserPass
      */
     public DokeosLMS(String dataBaseURL, String dataBasePort,
             String dataBaseName, String dataBaseUser, String dataBaseUserPass) {
         try {
             // Database parameters
             DataBaseConnectionParameters dbConnectionParams = new DataBaseConnectionParameters(dataBaseURL, dataBasePort,
                     dataBaseName, dataBaseUser, dataBaseUserPass);
             // Checking database`s parameters
             dbConnectionParams.checkParams();
 
             // Configuring database and initializing connection
             initDataBaseConnection(dbConnectionParams);
 
             this.converter = new DokeosConverter();
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      *
      * @param dataBaseURL
      * @param dataBasePort
      * @param dataBaseName
      * @param dataBaseUser
      * @param dataBaseUserPass
      * @throws ConnectionParametersException
      */
     @Override
     public void configureLMS(String dataBaseURL, String dataBasePort,
            String dataBaseName, String dataBaseUser, String dataBaseUserPass) throws ConnectionParametersException {
 
         // Saving database`s parameters
         DataBaseConnectionParameters dbConnectionParams = new DataBaseConnectionParameters(dataBaseURL, dataBasePort,
                 dataBaseName, dataBaseUser, dataBaseUserPass);
 
         // Checking database`s parameters
         dbConnectionParams.checkParams();
 
         // Configuring database and initializing connection
         initDataBaseConnection(dbConnectionParams);
 
     }
 
     /**
      *
      * @param connection
      */
     @Override
     public void configureLMS(Connection connection) {
 
         // Configuring database and initializing connection
         initDataBaseConnection(connection);
 
     }
 
     /**
      *
      * @param id
      * @return
      */
     @Override
     public Course getCourse(String id) {
         Course course;
         CourseDokeosDAO courseDAO = new CourseDokeosDAO(connection);
         CourseDokeosDTO courseDTO = courseDAO.selectCourse(id);
 
         course = converter.getDokeoseCourseItemFromCourseDTO(courseDTO);
 
         return course;
     }
 
     /**
      *
      * @return
      */
     @Override
     public List<Course> getCourse() {
         CourseDokeosDAO courseDAO = new CourseDokeosDAO(connection);
         List<CourseDokeosDTO> coursesDTO = courseDAO.selectCourse();
 
         return converter.getDokeoseCourseItemFromCourseDTO(coursesDTO);
     }
 
     /**
      *
      * @return
      */
     @Override
     public List<Forum> getForums() {
 
         CourseDokeosDAO courseDAO = new CourseDokeosDAO(connection);
         ForumDokeosDAO forumDAO = new ForumDokeosDAO(connection);
 
         List<CourseDokeosDTO> listeCourseDTO = courseDAO.selectCourses();
         List<ForumDokeosDTO> forumsDTO = new ArrayList();
         List<ForumDokeosDTO> forumsDTOTemp;
         List<Forum> forums;
 
         for (CourseDokeosDTO courseDTO : listeCourseDTO) {
             forumsDTOTemp = forumDAO.selectForums(courseDTO.getDb_name());
             forumsDTO.addAll(forumsDTOTemp);
         }
 
 
         forums = converter.getDokeosForumsItemFromForumsDTO(forumsDTO);
 
         return forums;
     }
 
     /**
      *
      * @return
      */
     @Override
     public Forum getForum(String idForum) {
         CourseDokeosDAO courseDAO = new CourseDokeosDAO(connection);
         ForumDokeosDAO forumDAO = new ForumDokeosDAO(connection);
 
         List<CourseDokeosDTO> listeCourseDTO = courseDAO.selectCourses();
         List<ForumDokeosDTO> forumsDTO = new ArrayList();
         List<ForumDokeosDTO> forumsDTOTemp = new ArrayList();
         ForumDokeosDTO forumDTOTemp = new ForumDokeosDTO();
         
         List<Forum> forums;
 
         for (CourseDokeosDTO courseDTO : listeCourseDTO) {
             forumDTOTemp = forumDAO.selectForumsById(courseDTO.getDb_name(), idForum);
             forumsDTOTemp.add(forumDTOTemp);
             forumsDTO.addAll(forumsDTOTemp);
         }
 
         forums = converter.getDokeosForumsItemFromForumsDTO(forumsDTO);
 
         return forums.get(0);
     }
 
     /**
      *
      * @param course
      * @return
      */
     @Override
     public List<Forum> getForums(Course course) {
 
         List<ForumDokeosDTO> forumsDTO;
         List<Forum> forums;
         ForumDokeosDAO forumDAO = new ForumDokeosDAO(connection);
         forumsDTO = forumDAO.selectForums(course.getConfigExtraData());
 
         forums = converter.getDokeosForumsItemFromForumsDTO(forumsDTO);
 
         return forums;
     }
 
     /**
      *
      * @return
      */
     @Override
     public List<Discussion> getDiscussions() {
         CourseDokeosDAO courseDAO = new CourseDokeosDAO(connection);
         DiscussionDokeosDAO discussionDAO = new DiscussionDokeosDAO(connection);
 
         List<CourseDokeosDTO> listeCourseDTO = courseDAO.selectCourses();
         List<DiscussionDokeosDTO> discussionsDTO = new ArrayList();
         List<DiscussionDokeosDTO> discussionsDTOTemp;
         List<Discussion> discussions;
 
 
         for (CourseDokeosDTO courseDTO : listeCourseDTO) {
             discussionsDTOTemp = discussionDAO.selectDiscussions(courseDTO.getDb_name());
             discussionsDTO.addAll(discussionsDTOTemp);
         }
 
         discussions = converter.getDokeosDiscussionsItemFromDiscussionsDTO(discussionsDTO);
 
         return discussions;
     }
 
     /**
      *
      * @return
      */
     @Override
     public Discussion getDiscussion(String idDiscussion) {
         CourseDokeosDAO courseDAO = new CourseDokeosDAO(connection);
         DiscussionDokeosDAO discussionDAO = new DiscussionDokeosDAO(connection);
 
         List<CourseDokeosDTO> listeCourseDTO = courseDAO.selectCourses();
         List<DiscussionDokeosDTO> discussionsDTO = new ArrayList();
         List<DiscussionDokeosDTO> discussionsDTOTemp = new ArrayList();
         DiscussionDokeosDTO discussionDTOTemp = new DiscussionDokeosDTO();
         List<Discussion> discussions;
 
         for (CourseDokeosDTO courseDTO : listeCourseDTO) {
             discussionDTOTemp = discussionDAO.selectDiscussionsById(
                     courseDTO.getDb_name(), idDiscussion);
             discussionsDTO.add(discussionDTOTemp);
             discussionsDTO.addAll(discussionsDTOTemp);
         }
 
         discussions = converter.getDokeosDiscussionsItemFromDiscussionsDTO(discussionsDTO);
 
         return discussions.get(0);
     }
 
     /**
      *
      * @param forum
      * @return
      */
     @Override
     public List<Discussion> getDiscussions(Forum forum) {
         DiscussionDokeosDAO discussionDAO = new DiscussionDokeosDAO(connection);
 
         List<DiscussionDokeosDTO> discussionsDTO;
         List<Discussion> discussions;
 
         discussionsDTO = discussionDAO.selectDiscussions(forum.getConfigExtraData(), forum.getId());
         discussions = converter.getDokeosDiscussionsItemFromDiscussionsDTO(discussionsDTO);
 
         return discussions;
     }
 
     /**
      *
      * @return
      */
     @Override
     public List<Post> getPosts() {
         CourseDokeosDAO courseDAO = new CourseDokeosDAO(connection);
         PostDokeosDAO postDAO = new PostDokeosDAO(connection);
 
         List<CourseDokeosDTO> listeCourseDTO = courseDAO.selectCourses();
         List<PostDokeosDTO> postsDTOTemp;
         List<PostDokeosDTO> postsDTO = new ArrayList();
         List<Post> posts;
 
 
         for (CourseDokeosDTO courseDTO : listeCourseDTO) {
             postsDTOTemp = postDAO.selectPosts(courseDTO.getDb_name());
             postsDTO.addAll(postsDTOTemp);
         }
 
         posts = converter.getDokeosPostsItemFromPostsDTO(postsDTO);
 
         return posts;
     }
 
     @Override
     public Post getPost(String idPost) {
         CourseDokeosDAO courseDAO = new CourseDokeosDAO(connection);
         PostDokeosDAO postDAO = new PostDokeosDAO(connection);
 
         List<CourseDokeosDTO> listeCourseDTO = courseDAO.selectCourses();
         List<PostDokeosDTO> postsDTOTemp = new ArrayList();
         List<PostDokeosDTO> postsDTO = new ArrayList();
         List<Post> posts;
         PostDokeosDTO postDTO;
 
         for (CourseDokeosDTO courseDTO : listeCourseDTO) {
             postDTO = postDAO.selectPostById(courseDTO.getDb_name(), idPost);
             postsDTOTemp.add(postDTO);
             postsDTO.addAll(postsDTOTemp);
         }
 
         posts = converter.getDokeosPostsItemFromPostsDTO(postsDTO);
 
         return posts.get(0);
     }
 
     /**
      *
      * @param discussion
      * @return
      */
     @Override
     public List<Post> getPosts(Discussion discussion) {
         PostDokeosDAO postDAO = new PostDokeosDAO(connection);
 
 
         List<PostDokeosDTO> postsDTOTemp;
         List<PostDokeosDTO> postsDTO = new ArrayList();
         List<Post> posts;
 
         postsDTOTemp = postDAO.selectPosts(discussion.getConfigExtraData(), discussion.getId());
         postsDTO.addAll(postsDTOTemp);
 
 
         posts = converter.getDokeosPostsItemFromPostsDTO(postsDTO);
 
         return posts;
     }
 
     /**
      *
      * @return
      */
     @Override
     public List<Participant> getParticipants() {
         List<ParticipantDokeosDTO> participantsDTO;
         List<Participant> participants;
         ParticipantDokeosDAO participantDAO = new ParticipantDokeosDAO(connection);
         participantsDTO = participantDAO.selectParticipants();
 
         participants = converter.getDokeosParticipantsItemFromParticipantsDTO(participantsDTO);
 
         return participants;
     }
 
 
     /**
      *
      * @param idParticipant
      * @return
      */
     @Override
     public Participant getParticipant(String idParticipant) {
         ParticipantDokeosDTO participantDTO;
         Participant participant;
         ParticipantDokeosDAO participantDAO = new ParticipantDokeosDAO(connection);
         participantDTO = participantDAO.selectParticipant(idParticipant);
 
         participant = converter.getDokeosParticipantItemFromParticipantDTO(participantDTO);
 
         return participant;
     }
 
     /**
      *
      * @param course
      * @return
      */
     @Override
     public Graph<MavselVertex, MavselEdge> getParticipantForumGraph(Course course) {
         MavselGraphManager gManager = new MavselGraphManager();
 
         return gManager.getParticipantForumGraph(course, this);
     }
 
     /**
      *
      * @param forums
      * @return
      */
     @Override
     public Graph<MavselVertex, MavselEdge> getParticipantForumGraph(List<Forum> forums) {
         MavselGraphManager gManager = new MavselGraphManager();
 
         return gManager.getParticipantForumGraph(forums, this);
     }
 
     @Override
     public void getForumRaitingInRFile(String filename) throws OperationNotSupportedException {
         //aqui lanzar la excepcion
         throw new OperationNotSupportedException("DOKEOS does not support rating forums.");
 
     }
         
     /**
      * 
      * @return 
      */
     @Override
     public Connection getConnection() {
         return connection;
     }
     
     
     /**
      * 
      * @return null, Dokeos does not support rating
      */
     @Override
     public List<PostRating> getPostRating(){
         
         return null;
     }
     
     
     /**
      * 
      * @param idPost
      * @return null, Dokeos does not support rating
      */
     @Override
     public List<PostRating> getPostRating(String idPost){
         
         return null;
     }
 }
