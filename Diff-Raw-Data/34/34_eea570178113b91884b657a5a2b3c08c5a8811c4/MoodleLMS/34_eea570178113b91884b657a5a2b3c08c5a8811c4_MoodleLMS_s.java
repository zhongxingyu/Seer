 package com.uah.main.moodle;
 
 import com.uah.commons.DataBaseConnectionParameters;
 import com.uah.commons.DataBaseManagement;
 import com.uah.commons.FileRProcessor;
 import com.uah.converters.MoodleConverter;
 import com.uah.dao.moodle.*;
 import com.uah.dto.modle.*;
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
 public class MoodleLMS extends DataBaseManagement implements LMS {
 
     /**************************************************************************
     *                              ATTRIBUTES
     **************************************************************************/
     private MoodleConverter converter;
     private MavselGraphManager graphManager;
 
     
     /**************************************************************************
     *                              PUBLIC METHODS
     ***************************************************************************/
     
     /**
      * Constructor
      */
     public MoodleLMS() {
         this.converter = new MoodleConverter();
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
     public MoodleLMS(String dataBaseURL, String dataBasePort,
             String dataBaseName, String dataBaseUser, String dataBaseUserPass) {
         try {
             // Database parameters
             DataBaseConnectionParameters dbConnectionParams = new DataBaseConnectionParameters(dataBaseURL, dataBasePort,
                     dataBaseName, dataBaseUser, dataBaseUserPass);
             // Checking database`s parameters
             dbConnectionParams.checkParams();
 
             // Configuring database and initializing connection
             initDataBaseConnection(dbConnectionParams);
 
             this.converter = new MoodleConverter();
             this.graphManager = new MavselGraphManager();
 
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
     public List<Course> getCourse() {
         CourseMoodleDAO courseDAO = new CourseMoodleDAO(connection);
         List<CourseMoodleDTO> coursesDTO = courseDAO.selectCourse();
 
         return converter.getMoodleCourseItemFromCourseDTO(coursesDTO);
     }
 
     /**
      *
      * @param id
      * @return
      */
     @Override
     public Course getCourse(String id) {
         Course course;
         CourseMoodleDAO courseDAO = new CourseMoodleDAO(connection);
         CourseMoodleDTO courseDTO = courseDAO.selectCourse(id);
 
         course = converter.getMoodleCourseItemFromCourseDTO(courseDTO);
 
         return course;
     }
 
     /**
      *
      * @return
      */
     @Override
     public List<Forum> getForums() {
         List<ForumMoodleDTO> forumsDTO;
         List<Forum> forums;
         ForumMoodleDAO forumDAO = new ForumMoodleDAO(connection);
         forumsDTO = forumDAO.selectForums();
 
         forums = converter.getMoodleForumsItemFromForumsDTO(forumsDTO);
 
         return forums;
     }
     
     /**
      * 
      * @param idForum
      * @return 
      */
     @Override
     public Forum getForum(String idForum) {
         List<ForumMoodleDTO> forumsDTO = new ArrayList();
         ForumMoodleDTO forumDTO;
         List<Forum> forums;
         ForumMoodleDAO forumDAO = new ForumMoodleDAO(connection);
         forumDTO = forumDAO.selectForumsById(idForum);
 
         forumsDTO.add(forumDTO);
         forums = converter.getMoodleForumsItemFromForumsDTO(forumsDTO);
 
         return forums.get(0);
     }
 
     /**
      *
      * @param course
      * @return
      */
     @Override
     public List<Forum> getForums(Course course) {
         List<ForumMoodleDTO> forumsDTO;
         List<Forum> forums;
         ForumMoodleDAO forumDAO = new ForumMoodleDAO(connection);
         forumsDTO = forumDAO.selectForums(course.getId());
 
         forums = converter.getMoodleForumsItemFromForumsDTO(forumsDTO);
 
         return forums;
     }
 
     /**
      *
      * @return
      */
     @Override
     public List<Discussion> getDiscussions() {
         List<DiscussionMoodleDTO> discussionsDTO;
         List<Discussion> discussions;
         DiscussionMoodleDAO discussionDAO = new DiscussionMoodleDAO(connection);
         discussionsDTO = discussionDAO.selectDiscussions();
 
         discussions = converter.getMoodleDiscussionsItemFromDiscussionsDTO(discussionsDTO);
 
         return discussions;
     }
 
     /**
      *
      * @param idDiscussion
      * @return
      */
     @Override
     public Discussion getDiscussion(String idDiscussion) {
         List<DiscussionMoodleDTO> discussionsDTO = new ArrayList();
         DiscussionMoodleDTO discussionDTO;
         List<Discussion> discussions;
         DiscussionMoodleDAO discussionDAO = new DiscussionMoodleDAO(connection);
         
         discussionDTO = discussionDAO.selectDiscussionsById(idDiscussion);
         discussionsDTO.add(discussionDTO);
         
         discussions = converter.getMoodleDiscussionsItemFromDiscussionsDTO(discussionsDTO);
 
         return discussions.get(0);
     }
 
     /**
     *
     * @param idForum
     * @return
      */
     @Override
     public List<Discussion> getDiscussions(Forum forum) {
         List<DiscussionMoodleDTO> discussionsDTO;
         List<Discussion> discussions;
         DiscussionMoodleDAO discussionDAO = new DiscussionMoodleDAO(connection);
         discussionsDTO = discussionDAO.selectDiscussions(forum.getId());
 
         discussions = converter.getMoodleDiscussionsItemFromDiscussionsDTO(discussionsDTO);
 
         return discussions;
     }
 
     /**
      *
      * @return
      */
     @Override
     public List<Post> getPosts() {
         List<PostMoodleDTO> postsDTO;
         List<Post> posts;
         PostMoodleDAO postDAO = new PostMoodleDAO(connection);
         postsDTO = postDAO.selectPosts();
 
         posts = converter.getMoodlePostsItemFromPostsDTO(postsDTO);
 
         return posts;
     }
 
     /**
      *
      * @param idPost
      * @return
      */
     @Override
     public Post getPost(String idPost) {
         List<PostMoodleDTO> postsDTO = new ArrayList();
         PostMoodleDTO postDTO;
         List<Post> posts;
         PostMoodleDAO postDAO = new PostMoodleDAO(connection);
         
         postDTO = postDAO.selectPostsById(idPost);
         postsDTO.add(postDTO);
         
         posts = converter.getMoodlePostsItemFromPostsDTO(postsDTO);
 
         return posts.get(0);
     }
 
     /**
      *
      * @param idDiscussion
      * @return
      */
     @Override
     public List<Post> getPosts(Discussion discussion) {
         List<PostMoodleDTO> postsDTO;
         List<Post> posts;
         PostMoodleDAO postDAO = new PostMoodleDAO(connection);
         postsDTO = postDAO.selectPosts(discussion.getId());
 
         posts = converter.getMoodlePostsItemFromPostsDTO(postsDTO);
 
         return posts;
     }
 
     /**
      *
      * @return
      */
     @Override
     public List<Participant> getParticipants() {
         List<ParticipantMoodleDTO> participantsDTO;
         List<Participant> participants;
         ParticipantMoodleDAO participantDAO = new ParticipantMoodleDAO(connection);
         participantsDTO = participantDAO.selectParticipants();
 
         participants = converter.getMoodleParticipantsItemFromParticipantsDTO(participantsDTO);
 
         return participants;
     }
 
     /**
      *
      * @param idParticipant
      * @return
      */
     @Override
     public Participant getParticipant(String idParticipant) {
         ParticipantMoodleDTO participantDTO;
         Participant participant;
         ParticipantMoodleDAO participantDAO = new ParticipantMoodleDAO(connection);
         participantDTO = participantDAO.selectParticipant(idParticipant);
 
         participant = converter.getMoodleParticipantItemFromParticipantDTO(participantDTO);
 
         return participant;
     }
 
     
     /**
      * 
      * @return 
      */
     @Override
     public List<PostRating> getPostRating(){
         List<PostRatingMoodleDTO> ratingsDTO;
         List<PostRating> ratingsItem;
         PostRatingMoodleDAO dao = new PostRatingMoodleDAO(connection);
 
         ratingsDTO = dao.selectPostRating();
 
         ratingsItem = converter.getMoodlePostRatingsFromPostRatingsDTO(ratingsDTO);
         
         return ratingsItem;
     }
     
     
     /**
      * 
      * @param idPost
      * @return 
      */
     @Override
     public List<PostRating> getPostRating(String idPost){
         List<PostRatingMoodleDTO> ratingsDTO;
         List<PostRating> ratingsItem;
         PostRatingMoodleDAO dao = new PostRatingMoodleDAO(connection);
 
         ratingsDTO = dao.selectPostRating(idPost);
         ratingsItem = converter.getMoodlePostRatingsFromPostRatingsDTO(ratingsDTO);
         
         return ratingsItem;
     }
     
         
     /**
      *
      * @return
      */
     @Override
     public void getForumRaitingInRFile(String filename) throws OperationNotSupportedException {
 
         FileRProcessor fileRProcessor = new FileRProcessor();
         fileRProcessor.printIntoFile(filename, getPostRating());
     }
 
     /**
      *
      * @param course
      * @return
      */
     @Override
     public Graph<MavselVertex, MavselEdge> getParticipantForumGraph(Course course) {
         graphManager = new MavselGraphManager();
 
         return graphManager.getParticipantForumGraph(course, this);
     }
 
     /**
      *
      * @param forums
      * @return
      */
     @Override
     public Graph<MavselVertex, MavselEdge> getParticipantForumGraph(List<Forum> forums) {
         graphManager = new MavselGraphManager();
 
         return graphManager.getParticipantForumGraph(forums, this);
     }
     /*
      * private MavselVertex searchVertex (MavselVertex vertex){
      *
      * Collection<MavselVertex> vertices = graph.getVertices(); Iterator iter =
      * vertices.iterator();
      *
      * MavselVertex compareVertex = null; while(iter.hasNext()){ compareVertex =
      * (MavselVertex)iter.next(); if
      * (compareVertex.getId().equalsIgnoreCase(vertex.getId()) &&
      * vertex.getType() == compareVertex.getType()){
      *
      * //Vertex alredy exists return vertex; } }
      *
      * //Vertex does not exists return null; }
      *
      */
     
     /**
      * 
      * @return 
      */
     @Override
     public Connection getConnection() {
         return connection;
     }
 }
