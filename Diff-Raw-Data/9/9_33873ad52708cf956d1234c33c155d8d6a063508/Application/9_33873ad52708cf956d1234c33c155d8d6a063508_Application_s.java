 package controllers;
 
 import models.Assessment;
 import models.ResearchSubject;
 import play.Logger;
 import play.mvc.Controller;
 
 public class Application extends Controller {
 
     public static void index() {
         render();
     }
 
     public static void start() {
         render();
     }
 
     public static void instructions(String pp_code, String pp_code_repeated) throws Exception {
        if (pp_code == null || pp_code.equals("") || !pp_code.equals(pp_code_repeated)) {
            throw new Exception("wrong pp_codes");
         }
         Logger.info("Proefpersoon with code %s is here", pp_code);
         ResearchSubject subject = new ResearchSubject();
         subject.setPpCode(pp_code);
         subject.save();
         Logger.info("Proefpersoon with code %s is stored with id %s", subject.getPpCode(), subject.getId());
         render(subject);
     }
 
     public static void interview(long subjectId) {
         render(subjectId);
     }
 
     // called with ajax
     public static void postAssessmentData(String scaleLabel, int value, String time, long subjectId) {
         Logger.info(String.format("Incoming assessment data: scale = %s, value = %s, time = %s, id = %s", scaleLabel, value, time, subjectId));
         ResearchSubject subject = getSubjectWithId(subjectId);
         Assessment assessment = subject.getAssessmentWithLabel(scaleLabel);
 
         assessment.setTime(time).setValue(value);
         assessment.setAssessmentOrder(subject.getNextInOrder());
 
         subject.save();
         Logger.info(String.format("Stored subject %s", subject));
         Logger.info("Returning subjectId " + subject.getId());
         renderJSON(subject.getId());
     }
 
     private static ResearchSubject getSubjectWithId(long subjectId) {
         ResearchSubject subject = ResearchSubject.findById(subjectId);
         if (subject == null) {
             subject = new ResearchSubject();
         }
         return subject;
     }
 }
