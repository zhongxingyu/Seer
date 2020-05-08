 package com.xtihha.study.restlet.resource;
 
 import org.restlet.ext.json.JsonRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Get;
 import org.restlet.resource.ServerResource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.xtihha.study.restlet.domain.Student;
 import com.xtihha.study.restlet.manager.ServiceManager;
 
 /**
  * @author zhangxiaohu
  * @created 2013-7-14
  */
 
 public class StudentResource extends ServerResource {
 
     private ServiceManager serviceManager;
 
     private static final Logger logger = LoggerFactory.getLogger(StudentResource.class);
 
     @Get
     public Representation get() {
         String idStr = (String) getRequest().getAttributes().get("id");
         long id = Long.parseLong(idStr);
         Student student = serviceManager.getStudent(id);
        if (student == null) {
            String value = new StringBuilder().append("{").append("\"error\":").append("\"invalid param\"").append("}").toString();
            return new JsonRepresentation(value);
        }
 
         String value = new StringBuilder().append("{").append("\"data\":{").append("\"id\":")
                 .append(student.getId()).append(",").append("\"name\":").append("\"")
                 .append(student.getName()).append("\"").append(",").append("\"department\":")
                 .append("\"").append(student.getDepartment()).append("\"").append("}").append("}")
                 .toString();
 
         logger.info(value);
 
         JsonRepresentation ret = new JsonRepresentation(value);
         return ret;
     }
 
     public void setServiceManager(ServiceManager serviceManager) {
         this.serviceManager = serviceManager;
     }
 
 }
