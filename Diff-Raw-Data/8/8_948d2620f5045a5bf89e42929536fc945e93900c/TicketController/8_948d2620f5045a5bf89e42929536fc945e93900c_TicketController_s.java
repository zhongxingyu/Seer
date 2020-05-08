 package org.einherjer.twitter.tickets.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletResponse;
 
 import lombok.Getter;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.einherjer.twitter.tickets.model.Attachment;
 import org.einherjer.twitter.tickets.model.SimpleJson;
 import org.einherjer.twitter.tickets.model.Ticket;
 import org.einherjer.twitter.tickets.model.Ticket.TicketPriority;
 import org.einherjer.twitter.tickets.model.Ticket.TicketStatus;
 import org.einherjer.twitter.tickets.model.Ticket.TicketType;
 import org.einherjer.twitter.tickets.service.TicketNotFoundException;
 import org.einherjer.twitter.tickets.service.TicketService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.Assert;
 import org.springframework.util.FileCopyUtils;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 
 @Controller
 public class TicketController {
 
     @Autowired
     private TicketService ticketService;
 
     //abandoned effort to send a Content-Length header. At this point the the header bytes are missing and the output string escapes double quotes, and maybe other issues.
     //Anyway it's a good example of how can we use ResponseEntity and use jackson to serialize the object representing the body
     //note that we need to instantiate a new ObjectMapper (the "new" is important, we cannot use Jackson2ObjectMapperFactoryBean.getObjector or @Autowire the objectMapper
     //from the servlet context (which BTW needs to declare it as @Bean and "public", not "private" as of now), cause this seems to cause some exception with Spring Security,
     //need to investigate more, anyway this works albeit of the duplicated code)
     //    public ObjectMapper objectMapper() {
     //        ObjectMapper objectMapper = new ObjectMapper();
     //        objectMapper.registerModule(new JodaModule());
     //        objectMapper.registerModule(new SimpleModule() {
     //            {
     //                addSerializer(DateTime.class, new StdSerializer<DateTime>(DateTime.class) {
     //                    @Override
     //                    public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
     //                        jgen.writeString(DateTimeFormat.forPattern("MM/dd/yyyy-HH:mm").print(value));
     //                    }
     //                });
     //                addDeserializer(DateTime.class, new StdDeserializer<DateTime>(DateTime.class) {
     //                    @Override
     //                    public DateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
     //                        String str = jp.getText().trim();
     //                        if (str.length() == 0) {
     //                            return null;
     //                        }
     //                        return DateTimeFormat.forPattern("MM/dd/yyyy-HH:mm").parseDateTime(str);
     //                    }
     //                });
     //            }
     //        });
     //        return objectMapper;
     //    }
     //    @RequestMapping(value = "/tickets", method = RequestMethod.GET)
     //    public ResponseEntity<String> getTickets() throws JsonProcessingException, UnsupportedEncodingException {
     //        Iterable<Ticket> tickets = ticketService.findAll();
     //        String body = this.objectMapper().writeValueAsString(tickets);
     //        HttpHeaders responseHeaders = new HttpHeaders();
     //        responseHeaders.set("Content-Length", Integer.toString(body.getBytes("UTF-8").length));
     //        return new ResponseEntity<String>(body, responseHeaders, HttpStatus.OK);
     //    }
 
     @RequestMapping(value = "/tickets", method = RequestMethod.GET)
     public @ResponseBody Iterable<Ticket> getTickets() {
         return ticketService.findAllForRole();
     }
     
     /**
      * Get ticket types
      */
     @RequestMapping(value = "/ticketTypes", method = RequestMethod.GET)
     public @ResponseBody List<Map<String, Object>> getTicketTypes() {
         List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
         for (final TicketType type : Ticket.TicketType.values()) {
             result.add(new SimpleJson()
                 .append("value", type.toString())
                 .append("description", type.getDescription()));
         }
         return result;
     }
     
     /**
      * Get ticket priorities
      */
     @RequestMapping(value = "/ticketPriorities", method = RequestMethod.GET)
     public @ResponseBody List<Map<String, Object>> getTicketPriorities() {
         List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
         for (final TicketPriority priority : Ticket.TicketPriority.values()) {
             result.add(new SimpleJson()
                 .append("value", priority.toString())
                 .append("description", priority.getDescription()));
         }
         return result;
     }
 
     /**
      * Get ticket statuses
      */
     @RequestMapping(value = "/ticketStatuses", method = RequestMethod.GET)
     public @ResponseBody List<Map<String, Object>> getTicketStatuses() {
         List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
         for (final TicketStatus status : Ticket.TicketStatus.values()) {
             result.add(new SimpleJson()
                 .append("value", status.toString())
                 .append("description", status.getDescription()));
         }
         return result;
     }
 
     /**
      * Get ticket
      */
     @RequestMapping(value = "/tickets/{project}-{number}", method = RequestMethod.GET)
     public @ResponseBody Ticket getTicket(@PathVariable("project") String projectPrefix, @PathVariable("number") Integer ticketNumber) {
         return ticketService.find(projectPrefix, ticketNumber);
     }
 
     /**
      * Add ticket
      * 
      * Example request:
      * POST /api/tickets HTTP/1.1
      * Host: localhost:8080
      * Content-Type: application/json
      * { "project":{"prefix":"PR1"}, "title":"t", "description":"d", "status":"OPEN", "assignee":{"username":"user@twitter.com"} }
      */
     @RequestMapping(value = "/tickets", method = RequestMethod.POST)
     public @ResponseBody SimpleJson postTicket(@RequestBody Ticket jsonBody) throws TicketNotFoundException {
         this.validateTicketDTO(jsonBody);
         Assert.notNull(jsonBody.getProject(), "Project cannot be null");
         Assert.notNull(jsonBody.getProject().getPrefix(), "Project cannot be null");
         final Ticket ticket = ticketService.save(jsonBody.getProject().getPrefix(), -1, jsonBody);
         return new SimpleJson().append("number", ticket.getTicketId());
         //usually a POST will return HttpStatus.CREATED and an empty body, but in this case we include the ticket id that the server generated in the response (we can still change the status code from OK to CREATED using the approach of getTickets()
     }
 
     /**
      * Update ticket
      * 
      * Example request:
      * PUT /api/tickets/PR1-1 HTTP/1.1
      * Host: localhost:8080
      * Content-Type: application/json
      * { "title":"t", "description":"d", "status":"OPEN", "assignee":{"username":"user@twitter.com"} }
      */
     @RequestMapping(value = "/tickets/{project}-{number}", method = RequestMethod.PUT)
     public ResponseEntity<String> putTicket(
             @RequestBody Ticket jsonBody,
             @PathVariable("project") String projectPrefix, 
             @PathVariable("number") Integer ticketNumber) throws TicketNotFoundException {
         this.validateTicketDTO(jsonBody);
         ticketService.save(projectPrefix, ticketNumber, jsonBody);
         return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
     }
     
     private void validateTicketDTO(Ticket jsonBody){
         Assert.notNull(jsonBody.getTitle(), "Title cannot be null");
         Assert.notNull(jsonBody.getDescription(), "Description cannot be null");
         Assert.notNull(jsonBody.getStatus(), "Status cannot be null");
         Assert.notNull(jsonBody.getAssignee(), "Assignee cannot be null");
         Assert.notNull(jsonBody.getAssignee().getUsername(), "Assignee cannot be null");
         Assert.notNull(jsonBody.getType(), "Type cannot be null");
         Assert.notNull(jsonBody.getPriority(), "Priority cannot be null");
     }
     
     /**
      * Patch ticket
      * 
      * Example request:
      * PATCH /api/tickets/PR1-1 HTTP/1.1
      * Host: localhost:8080
      * Content-Type: application/json
      * { "status":"CANCELLED" }
      */
     @RequestMapping(value = "/tickets/{project}-{number}", method = RequestMethod.PATCH)
     public ResponseEntity<String> patchTicket(
             @RequestBody Ticket jsonBody,
             @PathVariable("project") String projectPrefix, 
             @PathVariable("number") Integer ticketNumber) throws TicketNotFoundException {
         ticketService.patch(projectPrefix, ticketNumber, jsonBody);
         return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
     }
     
     @ExceptionHandler(TicketNotFoundException.class)
     @ResponseStatus(HttpStatus.BAD_REQUEST)
     public @ResponseBody ExceptionBody handleTicketNotFoundException(TicketNotFoundException e) {
         return new ExceptionBody(e.getMessage(), ExceptionUtils.getStackTrace(e));
     }
 
     /**
      * Change priority
      * 
      * Example request:
      * POST /api/tickets/PR1-1/priority HTTP/1.1
      * Host: localhost:8080
      * Content-Type: application/json
      * {"priority":"HIGH", "text": "comment"} (with comment)
      *  OR
      *  {"priority":"HIGH"} (no comment)
      */
     @RequestMapping(value = "/tickets/{project}-{number}/priority", method = RequestMethod.POST)
     public ResponseEntity<String> changePriority(
             @PathVariable("project") String projectPrefix,
             @PathVariable("number") Integer ticketNumber,
             @RequestBody SimpleJson jsonBody) throws TicketNotFoundException {
         ticketService.changePriority(projectPrefix, ticketNumber,
                 TicketPriority.valueOf(jsonBody.get("priority").toString()),
                 jsonBody.get("text") == null ? null : jsonBody.get("text").toString());
         return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
     }
     
     /**
      * Add Comment
      * 
      * Example request:
      * POST /api/tickets/PR1-1/comment HTTP/1.1
      * Host: localhost:8080
      * Content-Type: application/json
      * {"text":"comment text"}
      */
     @RequestMapping(value = "/tickets/{project}-{number}/comment", method = RequestMethod.POST)
     public ResponseEntity<String> addComment(
             @PathVariable("project") String projectPrefix,
             @PathVariable("number") Integer ticketNumber,
             @RequestBody TextJson jsonBody) {
         ticketService.addComment(projectPrefix, ticketNumber, jsonBody.getText());
         return new ResponseEntity<String>(HttpStatus.CREATED);
     }
     
     @Getter
     public static class TextJson {
         private String text;
     }
     
     /**
      * Cancel
      * 
      * Example request:
      * POST /api/tickets/PR1-1/cancel HTTP/1.1
      * Host: localhost:8080
      * Content-Type: application/json
      * {"text":"cancelation reason"} OR {} (empty json if no cancellation reason)
      */
     @RequestMapping(value = "/tickets/{project}-{number}/cancel", method = RequestMethod.POST)
     public ResponseEntity<String> cancel(
             @PathVariable("project") String projectPrefix, 
             @PathVariable("number") Integer ticketNumber,
             @RequestBody SimpleJson jsonBody) {
         ticketService.cancel(projectPrefix, ticketNumber, jsonBody.get("text") != null ? jsonBody.get("text").toString() : null);
         return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
     }
 
     /**
      * Reject
      * 
      * Example request:
      * POST /api/tickets/PR1-1/reject HTTP/1.1
      * Host: localhost:8080
      * Content-Type: application/json
      * {"text":"rejection message"}
      */
     @RequestMapping(value = "/tickets/{project}-{number}/reject", method = RequestMethod.POST)
     public ResponseEntity<String> reject(
             @PathVariable("project") String projectPrefix, 
             @PathVariable("number") Integer ticketNumber,
             @RequestBody TextJson jsonBody) {
         ticketService.reject(projectPrefix, ticketNumber, jsonBody.getText());
         return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
     }
    
     /**
      * Approve
      * 
      * Example request:
      * POST /api/tickets/PR1-1/approve HTTP/1.1
      * Host: localhost:8080
      * Content-Type: application/json
      * {"text":"approval reason"} OR {} (empty json if no approval reason)
      */
     @RequestMapping(value = "/tickets/{project}-{number}/approve", method = RequestMethod.POST)
     public ResponseEntity<String> approve(
             @PathVariable("project") String projectPrefix, 
             @PathVariable("number") Integer ticketNumber,
             @RequestBody SimpleJson jsonBody) {
         ticketService.approve(projectPrefix, ticketNumber, jsonBody.get("text") != null ? jsonBody.get("text").toString() : null);
         return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
     }
     
     /**
      * Done
      * 
      * Example request:
      * POST /api/tickets/PR1-1/done HTTP/1.1
      * Host: localhost:8080
      * Content-Type: application/json
      * no body
      */
     @RequestMapping(value = "/tickets/{project}-{number}/done", method = RequestMethod.POST)
     public ResponseEntity<String> markAsDone(
             @PathVariable("project") String projectPrefix, 
             @PathVariable("number") Integer ticketNumber) {
         ticketService.done(projectPrefix, ticketNumber);
         return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
     }
     
     /**
      * Add attachment
      * 
      * Example request:
      * POST /api/tickets/PR1-1/attachment HTTP/1.1
      * Content-Type: multipart/form-data
      * --edt7Tfrdusa7r3lNQc79vXuhIIMlatb7PQg7Vp
      * Content-Disposition: form-data; name="files[]"; filename="palacio_salvo.jpg"
      * Content-Type: image/jpeg
      * ... File Data ...
      */
     @RequestMapping(value = "/tickets/{project}-{number}/attachment", method = RequestMethod.POST)
     public @ResponseBody Set<Attachment> uploadAttachment(
             @PathVariable("project") String projectPrefix,
             @PathVariable("number") Integer ticketNumber,
     // this way is simpler but doesn't support multiple files
     //          @RequestPart("files[]") MultipartFile file) throws IOException {
     //        ticketService.addAttachment(projectPrefix, ticketNumber, file.getOriginalFilename(), file.getBytes());
     //    }
             MultipartHttpServletRequest request) throws IOException {
         MultipartFile mpf = null;
         Iterator<String> itr = request.getFileNames();
         while (itr.hasNext()) {
             mpf = request.getFile(itr.next());
             ticketService.addAttachment(projectPrefix, ticketNumber, mpf.getOriginalFilename(), mpf.getSize() / 1024 + " Kb", mpf.getContentType(), mpf.getBytes());
         }
        return ticketService.find(projectPrefix, ticketNumber).getAttachments();
     }
 
     /**
      * Download attachment 
      */
     @RequestMapping(value = "/tickets/{project}-{number}/attachment/{attachId}", method = RequestMethod.GET)
     public void downloadAttachment(
             @PathVariable("project") String projectPrefix,
             @PathVariable("number") Integer ticketNumber,
             @PathVariable("attachId") Long attachmentId,
             HttpServletResponse response) throws IOException {
         Attachment attachment = ticketService.getAttachment(projectPrefix, ticketNumber, attachmentId);
         response.setContentType(attachment.getFileType());
         response.setHeader("Content-disposition", "attachment; filename=\"" + attachment.getFileName() + "\"");
         FileCopyUtils.copy(attachment.getBytes(), response.getOutputStream());
     }
     
     /**
      * Delete attachment
      */
     @RequestMapping(value = "/tickets/{project}-{number}/attachment/{attachId}", method = RequestMethod.DELETE)
    public @ResponseBody Set<Attachment> deleteAttachment(
             @PathVariable("project") String projectPrefix,
             @PathVariable("number") Integer ticketNumber,
             @PathVariable("attachId") Long attachmentId) {
         ticketService.deleteAttachment(projectPrefix, ticketNumber, attachmentId);
        return ticketService.find(projectPrefix, ticketNumber).getAttachments();
     }
 
 }
