 package controllers;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import java.util.ArrayList;
 import java.util.List;
 import models.ClassificationSerializer;
 import models.SavedScheme;
 import models.User;
 import play.db.jpa.NoTransaction;
 import play.db.jpa.Transactional;
 import play.mvc.Controller;
 import play.mvc.With;
 import ru.ifmo.ailab.e3soos.facts.Classification;
 import ru.ifmo.ailab.e3soos.facts.Requirements;
 import ru.ifmo.ailab.e3soos.facts.Schema;
 import utils.Result;
 import utils.RuleRunner;
 
 @With(Secure.class)
 public class Application extends Controller {
 
     private static Gson gson;
 
     static {
         GsonBuilder builder = new GsonBuilder().
                 registerTypeAdapter(Classification.class, new ClassificationSerializer());
         gson = builder.create();
     }
 
     @Transactional(readOnly=true)
     public static void dashboard() {
         User user = User.find("byEmail", Security.connected()).first();
         List<SavedScheme> schemes = SavedScheme.find("byUser", user).fetch();
         render(user, schemes);
     }
 
     @Transactional(readOnly=true)
     public static void synthesis() {
         User user = User.find("byEmail", Security.connected()).first();
         render(user);
     }
 
     @NoTransaction
    public static void runSynthesis(Requirements requirements) throws InterruptedException {
        Thread.sleep(3000);
        error();
         if(requirements != null) {
             List<String> schemes = new ArrayList<String>();
             Classification classification = RuleRunner.classify(requirements);
             for(Schema scheme : RuleRunner.synthesis(classification)) {
                 schemes.add(scheme.toString());
             }
             renderJSON("{\"classification\": " + gson.toJson(classification) + ", "
                     + "\"schemes\": " + gson.toJson(schemes) + "}");
         }
         badRequest();
     }
 
     @NoTransaction
     public static void runSynthesisWithLogs(Requirements requirements) {
         if(requirements != null) {
             Classification classification = RuleRunner.classify(requirements);
 
             Result result = RuleRunner.synthesisWithLogs(classification);
             result.setData("classification", classification);
             renderJSON(result, new ClassificationSerializer());
         }
         badRequest();
     }
 
     public static void saveScheme(String code, String comment) throws InterruptedException {
         if(code != null && !code.isEmpty()) {
             User user = User.find("byEmail", Security.connected()).first();
             SavedScheme scheme = new SavedScheme(user, code, comment);
             scheme.save();
             renderJSON(scheme.id);
         }
         badRequest();
     }
 
     public static void deleteScheme(long id) throws InterruptedException {
         SavedScheme scheme = SavedScheme.findById(id);
         scheme.delete();
         ok();
         badRequest();
     }
 }
