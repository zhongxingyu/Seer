 package org.cc.response;
 
 import com.fasterxml.jackson.annotation.JsonProperty;
 import org.springframework.validation.BindException;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.ObjectError;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Daneel Yaitskov
  */
 public class CloudInvalidArgsResponse extends CloudErrorResponse {
 
     /**
      * invalid parameter => error details.
      */
     @JsonProperty("errors")
     private List<InvalidField> errors;
 
     public CloudInvalidArgsResponse(BindException e) {
         super(e);
         errors = new ArrayList<InvalidField>(e.getErrorCount());
         for (FieldError error : e.getFieldErrors()) {
             errors.add(new InvalidField(error.getField(), error.getDefaultMessage()));
         }
     }
 }
