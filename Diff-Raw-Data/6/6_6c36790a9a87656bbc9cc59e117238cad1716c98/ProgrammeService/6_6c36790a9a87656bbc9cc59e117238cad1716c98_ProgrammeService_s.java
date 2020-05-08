 package no.hials.muldvarpweb.service;
 
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import no.hials.muldvarpweb.domain.Course;
 import no.hials.muldvarpweb.domain.LibraryItem;
 import no.hials.muldvarpweb.domain.Programme;
 import no.hials.muldvarpweb.domain.Quiz;
 import no.hials.muldvarpweb.domain.Video;
 
 /**
  * Service class for the Programme entities.
  * 
  * 
  * @author johan
  */
 @Stateless
 @Path("programme")
 public class ProgrammeService {
     
     @PersistenceContext
     EntityManager em;
     
     /**
      * This function merges and persists a Programme item .
      * 
      * @param newProgramme The Programme to be added.
      */
     public void addProgramme(Programme newProgramme){
         newProgramme = em.merge(newProgramme);
         em.persist(newProgramme);
     }
     
     /**
      * Function that returns all Programmes based on 
      * 
      * @return Programmes
      */
     @GET
     @Produces({MediaType.APPLICATION_JSON})
     public List<Programme> findProgrammes() {
         return em.createQuery("SELECT v from Programme v", Programme.class).getResultList();
     }
     
     @GET
     @Path("courses/{id}")
     @Produces({MediaType.APPLICATION_JSON})
     public List<Course> findCoursesInProgrammes(@PathParam("id") Integer id) {
         TypedQuery<Course> q = em.createQuery("SELECT v.courses from Programme v where v.id = :id", Course.class);
         q.setParameter("id", id);
         return q.getResultList();
     }
     
     @GET
     @Path("quiz/{id}")
     @Produces({MediaType.APPLICATION_JSON})
     public List<Quiz> findQuizzesInProgrammes(@PathParam("id") Integer id) {
         TypedQuery<Quiz> q = em.createQuery("SELECT q.quizzes from Programme q where q.id = :id", Quiz.class);
         q.setParameter("id", id);
         return q.getResultList();
     }
         
     /**
      * Function that returns all Programmes corresponding to one ID.
      * 
      * @param id 
      * @return Programmes
      */
     @GET
     @Path("{id}")
     @Produces({MediaType.APPLICATION_JSON})
     public Programme getProgrammes(@PathParam("id") Integer id) {
         TypedQuery<Programme> q = em.createQuery("Select v from Programme v where v.id = :id", Programme.class);
         q.setParameter("id", id);
         
         return q.getSingleResult();
     }
 
     public void editProgramme(Programme selected) {
        selected = em.merge(selected);
        em.persist(selected);
     }
 
     public void removeCourseFromProgramme(Programme selected, Course c) {
         selected.removeCourse(c);
         editProgramme(selected);
     }
 
     public void removeProgramme(Programme p) {
         p = em.merge(p);
         em.remove(p);
     }
 
     public Programme setCourses(Programme selected, List<Course> target, List<Course> oldItems) {
 //        for(Course c : oldItems) {
 //            for(Course cc : target) {
 //                boolean found = false;
 //                if(c.getId().equals(cc.getId())) {
 //                    found = true;
 //                    break;
 //                }
 //                if(!found) {
 //                    cc.removeProgramme(selected);
 //                }
 //            }
 //        }
         selected.setCourses(target);
         return persist(selected);
     }
     
     public Programme persist(Programme p) {
         if(p.getId() == null)
             em.persist(p);
         else
             p = em.merge(p);
         
         return p;
     }
 
 //    public Programme addCourses(Programme selected, List<Course> target) {
 //        for(int k = 0; k > target.size(); k++) {
 //            boolean found = false;
 //            for(int i = 0; i > selected.getCourses().size(); i++) {
 //                if(target.get(k).getId().equals(selected.getCourses().get(i).getId())) {
 //                    found = true;
 //                    break;
 //                }
 //            }
 //            boolean add = true;
 //            if(!found) {
 //                selected.removeCourse(target.get(k));
 //                add = false;
 //            }
 //            if(add) {
 //                selected.addCourse(target.get(k));
 //            }
 //        }
 //        return persist(selected);
 //    }
     
     
     public void addVideo(Programme course, Video video){
         course.addVideo(video);
         persist(course);
     }
     
     public Programme setVideos(Programme c, List<Video> v) {
         c.setVideos(v);
         return persist(c);
     }
     
     public void editVideo(Programme course, Video video){
         course.addVideo(video);
         persist(course);
     }
     
     public void removeVideo(Programme course, Video video){
         
         course.removeVideo(video);
         persist(course);
     }
     
     public void addDocument(Programme course, LibraryItem document) {
         course.addDocument(document);
         //document.addCourse(course);
        em.merge(course);
     }
     
     public void editDocument(Programme course, LibraryItem document) {
         course.addDocument(document);
         persist(course);
     }
     
     public Programme setDocuments(Programme c, List<LibraryItem> d) {
         c.setDocuments(d);
         return persist(c);
     }
     
     public void removeDocument(Programme course, LibraryItem document) {
         course.removeDocument(document);
         persist(course);
     }
     
     public Programme setQuizzes(Programme selected, List<Quiz> target) {
         selected.setQuizzes(target);
         return persist(selected);
     }
 }
