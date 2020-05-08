 package edu.lmu.cs.headmaster.ws.resource;
 
 import java.util.List;
 
 import javax.annotation.security.RolesAllowed;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import edu.lmu.cs.headmaster.ws.domain.Event;
 import edu.lmu.cs.headmaster.ws.domain.Student;
 import edu.lmu.cs.headmaster.ws.domain.StudentRecord;
 import edu.lmu.cs.headmaster.ws.types.ClassYear;
 import edu.lmu.cs.headmaster.ws.util.ServiceException;
 
 /**
  * The JAX-RS interface for operating on student resources.
  */
 @Consumes(MediaType.APPLICATION_JSON)
 @Produces(MediaType.APPLICATION_JSON)
 public interface StudentResource {
     
     /**
      * Possible resource error messages.
      */
     String STUDENT_OVERSPECIFIED = "student.overspecified";
     String STUDENT_INCONSISTENT = "student.inconsistent";
     String STUDENT_NOT_FOUND = "student.not.found";
 
     /**
      * Returns students according to the search parameters
      * 
      * @param query the query
      * @param skip the number of initial results to skip
      * @param max the maximum number of results to display
      * 
      * @return the (paginated) set of students matching the query parameters
      */
     @GET
     List<Student> getStudents(@QueryParam("q") String query,
             @QueryParam("active") @DefaultValue("true") Boolean active,
             @QueryParam("class") ClassYear classYear,
             @QueryParam("classFrom") Integer expectedGraduationYearFrom,
             @QueryParam("classTo") Integer expectedGraduationYearTo,
             @QueryParam("skip") @DefaultValue("0") int skip,
             @QueryParam("max") @DefaultValue("50") int max);
 
     /**
      * Creates a student for which the server will generate the id.
      *
      * @param student the student object to create. The student must have a null id.
      * @return A response with HTTP 201 on success, or a response with HTTP 400 and message
      * <code>student.overspecified</code> if the student's id is not null.
      */
     @POST
     Response createStudent(Student student);
 
     /**
      * Supposed to save the representation of the student with the given id.
      * Inconsistent data should result in HTTP 400, while a successful PUT
      * should return Response.noContent.
      * 
      * @param id the id of the student to save.
      * @return A response with HTTP 204 no content on success, or a response
      *         with HTTP 400 and message <code>student.inconsistent</code> if
      *         checked data does not have the save id as requested in the URL.
      */
     @PUT
     @Path("{id}")
     Response createOrUpdateStudent(@PathParam("id") Long id, Student student);
 
     /**
      * Returns the student with the given id.
      *
      * @param id the id of the requested student.
      * @return the student with the given id.
      * @throws ServiceException if there is no student with the given id, causing the framework
      * to generate an HTTP 404.
      */
     @GET
     @Path("{id}")
     Student getStudentById(@PathParam("id") Long id);
 
     /**
      * Returns the events attended by the student with the given id.
      *
      * @param id the id of the requested student.
      * @return the events attended by the student with the given id.
      * @throws ServiceException if there is no student with the given id, causing the framework
      * to generate an HTTP 404.
      */
     @GET
     @Path("{id}/attendance")
     List<Event> getStudentAttendanceById(@PathParam("id") Long id);
 
     /**
      * Returns the student record for the student with the given id.
      */
     @GET
     @Path("{id}/record")
    @RolesAllowed({ "headmaster", "FACULTY", "STAFF" })
     StudentRecord getStudentRecordById(@PathParam("id") Long id);
 
     /**
      * Updates the record for the student with the given id.
      */
     @PUT
     @Path("{id}/record")
    @RolesAllowed({ "HEADMASTER", "FACULTY", "STAFF" })
     Response updateStudentRecord(@PathParam("id") Long id, StudentRecord studentRecord);
 
 }
