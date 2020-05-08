 package com.kwc.testen.rest;
 
 import com.kwc.testen.db.TestResultRepository;
 import com.kwc.testen.model.TestResult;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 import java.sql.SQLException;
 import java.util.List;
 
 /**
  * @author Marius Kristensen
  */
 @Path("/tests/")
 public class TestRestService {
 
     @GET
     @Path("/")
     @Produces("application/json")
     public List<TestResult> getAllTestResults() {
         TestResultRepository repository = new TestResultRepository();
         return repository.getAllTestResults();
     }
 
     @GET
     @Path("/{param}")
     @Produces("application/json")
     public TestResult getOneTestResult(@PathParam("param") int msg) throws SQLException {
         TestResultRepository repository = new TestResultRepository();
         return repository.getTestResult(msg);
     }
 
     @POST
     @Path("/post")
     @Consumes("application/json")
     public Response postTestResult(TestResult testResult) {
 
         String result = "Test result added";
         System.out.println("    ########    " + result);
         return Response.status(201).entity(result).build();
     }
 
 }
