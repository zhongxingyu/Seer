 package controllers;
 
 import models.User;
 import models.Project;
 import models.Image;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.pipeline.*;
 import be.objectify.deadbolt.actions.Restrict;
 import play.Logger;
 import forms.*;
 import play.libs.*;
 import play.*;
 import java.util.Map;
 import java.util.List;
 
 public class Pipeline extends Controller {
 
     final static Form<PickerParams>     pickerForm   = form(PickerParams.class);
     final static Form<FilterParams>     filterForm   = form(FilterParams.class);
     final static Form<GenerationParams> rotateForm   = form(GenerationParams.class);
     final static Form<ClassifierParams> classifyForm = form(ClassifierParams.class);
 
     public static @Restrict(Application.USER_ROLE) Result create(Long projectId) {
         final User user = Application.getLocalUser(session());
         // Get the project
         models.Project project = models.Project.findByIdWithOwner(projectId, user);
         if(project !=null) {
             // Make a new pipeline
             models.Pipeline newPipeline = new models.Pipeline(project);
             newPipeline.save();
             // Send the user to the image upload screen
             return ok(imageSelect.render(newPipeline, null));
         } else {
             return badRequest();
         }
 
     }
 
     public static @Restrict(Application.USER_ROLE) Result resume(Long id) {
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user);
         if(pipeline == null) {
             return badRequest();
         }
         // Figure out what state we were in
         if(pipeline.getStatus() == models.Pipeline.SELECT_IMAGES)
             return ok(imageSelect.render(pipeline, null));
         else if(pipeline.getStatus() == models.Pipeline.CONFIG_PICKER)
             return ok(picker.render(pipeline, null));
         else if(pipeline.getStatus() == models.Pipeline.CONFIG_FILTERS)
             return ok(filter.render(pipeline, null));
         else if(pipeline.getStatus() == models.Pipeline.CONFIG_GENERATION)
             return ok(rotate.render(pipeline, null));
         else if(pipeline.getStatus() == models.Pipeline.CONFIG_CLASSIFIER)
             return ok(classify.render(pipeline, null));
         else if(pipeline.getStatus() == models.Pipeline.START_RUN)
             return ok(startRun.render(pipeline, null));
         else if(pipeline.getStatus() == models.Pipeline.RUNNING)
             return ok(status.render(pipeline));
         else if(pipeline.getStatus() == models.Pipeline.COMPLETE)
             return ok(results.render(pipeline));
         else if(pipeline.getStatus() == models.Pipeline.ERROR)
             return ok(error.render(pipeline));
         else 
             return notFound();
     
     }
 
     public static @Restrict(Application.USER_ROLE) Result pickParticles(Long id) {
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user);        
         if(pipeline != null && pipeline.getStatus() >= models.Pipeline.CONFIG_PICKER) {
             return ok(picker.render(pipeline, null));
         } else {
             return badRequest();
         }
     }
 
     public static @Restrict(Application.USER_ROLE) Result doPickParticles(Long id) {
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user);        
         if(pipeline == null || pipeline.getStatus() < models.Pipeline.CONFIG_PICKER) {
             return badRequest();
         }
 
         // Bind the form
         Form<PickerParams> filledForm = pickerForm.bindFromRequest();
         if (filledForm.hasErrors()) {
             return badRequest(picker.render(pipeline, "Oops! You're missing the expected particle size or tolerance"));
         }
 
         // Check on the boxSize
         PickerParams params = filledForm.get();
         params.validateBoxSize();
 
         // Build some JSON
         pipeline.setPickerParams(params.toJSONString());
 
         if(pipeline.getStatus() == models.Pipeline.CONFIG_PICKER) {
             pipeline.setStatus(models.Pipeline.CONFIG_FILTERS);
         }
         pipeline.save();
         return ok(filter.render(pipeline, null));
     }
 
     public static @Restrict(Application.USER_ROLE) Result filter(Long id) {
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user);        
         if(pipeline != null && pipeline.getStatus() >= models.Pipeline.CONFIG_FILTERS) {
             return ok(filter.render(pipeline, null));
         } else {
             return badRequest();
         }
     }
 
     public static @Restrict(Application.USER_ROLE) Result doFilter(Long id) {
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user);        
         if(pipeline == null || pipeline.getStatus() < models.Pipeline.CONFIG_FILTERS) {
             return badRequest();
         }
 
         // Bind the form
         Form<FilterParams> filledForm = filterForm.bindFromRequest();
         if (filledForm.hasErrors()) {
             return badRequest(filter.render(pipeline, "Oops! You're missing some filtering parameters"));
         }
 
         // Check on the boxSize
         FilterParams params = filledForm.get();
 
         // Build some JSON
         pipeline.setFilterParams(params.toJSONString());
 
         if(pipeline.getStatus() == models.Pipeline.CONFIG_FILTERS) {
             pipeline.setStatus(models.Pipeline.CONFIG_GENERATION);
         }
         pipeline.save();
         return ok(rotate.render(pipeline, null));
     }
 
     public static @Restrict(Application.USER_ROLE) Result rotate(Long id) {
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user);    
         if(pipeline != null && pipeline.getStatus() >= models.Pipeline.CONFIG_GENERATION) {
             return ok(rotate.render(pipeline, null));
         } else {
             return badRequest();
         }
     }
 
     public static @Restrict(Application.USER_ROLE) Result doRotate(Long id) {
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user);        
         if(pipeline == null || pipeline.getStatus() < models.Pipeline.CONFIG_GENERATION) {
             return badRequest();
         }
 
         // Bind the form
         Form<GenerationParams> filledForm = rotateForm.bindFromRequest();
         if (filledForm.hasErrors()) {
             return badRequest(rotate.render(pipeline, "Oops! You didn't specify how many rotations to generate."));
         }
 
         // Check on the boxSize
         GenerationParams params = filledForm.get();
 
         // Build some JSON
         pipeline.setGenerationParams(params.toJSONString());
 
         if(pipeline.getStatus() == models.Pipeline.CONFIG_GENERATION) {
             pipeline.setStatus(models.Pipeline.CONFIG_CLASSIFIER);
         }
         pipeline.save();
         return ok(classify.render(pipeline, null));
     }
 
     public static @Restrict(Application.USER_ROLE) Result classify(Long id) {
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user);    
         if(pipeline != null && pipeline.getStatus() >= models.Pipeline.CONFIG_CLASSIFIER) {
             return ok(classify.render(pipeline, null));
         } else {
             return badRequest();
         }
     }
 
     public static @Restrict(Application.USER_ROLE) Result doClassify(Long id) {
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user);        
         if(pipeline == null || pipeline.getStatus() < models.Pipeline.CONFIG_CLASSIFIER) {
             return badRequest();
         }
 
         // Bind the form
         Form<ClassifierParams> filledForm = classifyForm.bindFromRequest();
         if (filledForm.hasErrors()) {
             return badRequest(classify.render(pipeline, "Oops! You didn't specify some classfication parameters"));
         }
 
         // Check on the boxSize
         ClassifierParams params = filledForm.get();
 
         // Build some JSON
         pipeline.setClassifierParams(params.toJSONString());
 
         if(pipeline.getStatus() == models.Pipeline.CONFIG_CLASSIFIER) {
             pipeline.setStatus(models.Pipeline.START_RUN);
         }
         pipeline.save();
         return ok(startRun.render(pipeline, null));
     }
 
     public static @Restrict(Application.USER_ROLE) Result startRun(Long id) {
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user);    
         if(pipeline != null && pipeline.getStatus() >= models.Pipeline.START_RUN) {
             return ok(startRun.render(pipeline, null));
         } else {
             return badRequest();
         }
     }
 
     public static @Restrict(Application.USER_ROLE) Result doStartRun(Long id) {
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         final models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user);    
         if(pipeline != null && pipeline.getStatus() == models.Pipeline.START_RUN) {
             // Get the host config
             String host = Play.application().configuration().getString("pipeline.host");
             int port = Play.application().configuration().getInt("pipeline.port");
 
             // Prep the request
             String url = "http://"+host+":"+port+"/pipeline/create";
 
             try {
                 // Post the request
                 F.Promise<WS.Response> promise = WS.url(url).post(pipeline.getParamsJson());
 
                 // Get the response (sync, play async is broken :( )
                 WS.Response response = promise.get(); 
                 Logger.warn("Pipeline response:"+response.getStatus()+" -> "+response.getBody());
                 if(response.getStatus() == 200) {
                     pipeline.setGuardianId(response.getBody());
                     pipeline.setStatus(models.Pipeline.RUNNING);
                     pipeline.save();
                     return ok(status.render(pipeline));
                 } 
             } catch(Exception e) {
                 Logger.error("Couldn't connect to the pipeline host -> "+host+":"+port);
             }
             return badRequest(startRun.render(pipeline, "Oops, couldn't start the pipeline. Double check all your parameters and try again."));
         } else if(pipeline != null && pipeline.getStatus() == models.Pipeline.RUNNING) {
             return ok(status.render(pipeline));
         } else {
             return badRequest();
         }
     }
 
     public static @Restrict(Application.USER_ROLE) Result status(Long id) {
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user);    
         if(pipeline != null && pipeline.getStatus() >= models.Pipeline.RUNNING) {
             return ok(status.render(pipeline));
         } else {
             return badRequest();
         }
     }
     
     public static @Restrict(Application.USER_ROLE) Result progress(Long id) {
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user);    
         if(pipeline != null && pipeline.getStatus() >= models.Pipeline.RUNNING) {
             // Get the host config
             String host = Play.application().configuration().getString("pipeline.host");
             int port = Play.application().configuration().getInt("pipeline.port");
 
             // Prep the request
             String url = "http://"+host+":"+port+"/pipeline/"+pipeline.getGuardianId()+"/status";
 
             try {
                 // Post the request
                 F.Promise<WS.Response> promise = WS.url(url).get();
 
                 // Get the response (sync, play async is broken :( )
                 WS.Response response = promise.get(); 
                 if(response.getStatus() == 200) {
                     return ok(response.getBody());
                 } 
             } catch(Exception e) {
                 Logger.error("Couldn't connect to the pipeline host -> "+host+":"+port);
             }
             return internalServerError("{}");
         } else {
             return badRequest();
         }
     }
 
     public static @Restrict(Application.USER_ROLE) Result results(Long id) {
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user);    
         if(pipeline != null && pipeline.getStatus() >= models.Pipeline.RUNNING) {
            if(pipeline.getResults() == null) {
                 // Get the host config
                 String host = Play.application().configuration().getString("pipeline.host");
                 int port = Play.application().configuration().getInt("pipeline.port");
 
                 // Prep the request
                 String url = "http://"+host+":"+port+"/pipeline/"+pipeline.getGuardianId()+"/results";
 
                 try {
                     // Post the request
                     F.Promise<WS.Response> promise = WS.url(url).get();
 
                     // Get the response (sync, play async is broken :( )
                     WS.Response response = promise.get(); 
                     if(response.getStatus() == 200) {
                         pipeline.setStatus(models.Pipeline.COMPLETE);
                         pipeline.setResults(response.getBody());
                         pipeline.save();
 
                         // Destroy the pipeline, it's not needed anymore
                         String destroyUrl = "http://"+host+":"+port+"/pipeline/"+pipeline.getGuardianId()+"/destroy";
                         WS.url(destroyUrl).post("");
                        return ok(results.render(pipeline));
 
                     } else {
                         Logger.error("Results unavailable: Response status "+response.getStatus());
                     }
                 } catch(Exception e) {
                     Logger.error("Couldn't connect to the pipeline host -> "+host+":"+port);
                 }
                 return internalServerError("{}");
             } 
             // If results are actually available
             return ok(results.render(pipeline));
             
         } else {
             return badRequest();
         }
     }
 
     public static @Restrict(Application.USER_ROLE) Result selectImages(Long id) {
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user);
         if(pipeline != null && pipeline.getStatus() >= models.Pipeline.SELECT_IMAGES) {
             return ok(imageSelect.render(pipeline, null));
         } else {
             return badRequest();
         }
     }
 
     public static @Restrict(Application.USER_ROLE) Result doSelectImages(Long id) {
         Map<String, String[]> formData = request().body().asFormUrlEncoded();
         // Get the user
         final User user = Application.getLocalUser(session());
         // Get the pipeline
         models.Pipeline pipeline = models.Pipeline.findByIdWithOwner(id, user); 
 
         if(pipeline == null) {
             return badRequest();
         }
 
         if((formData == null 
             || !formData.containsKey("images") 
             || formData.get("images").length == 0) 
             && pipeline.getImages().isEmpty()) {
             Logger.error("No new images selected");
             // We don't have preexisting images and didn't get any valid images
             return badRequest(imageSelect.render(pipeline, "Oops! You didn't select any images."));
         } else if((formData == null 
             || !formData.containsKey("images") 
             || formData.get("images").length == 0)
             && !pipeline.getImages().isEmpty()) {
             // We already have images, but didn't get any new ones.
             // Let this slide
             return ok(picker.render(pipeline, null));
         } else if(!pipeline.getImages().isEmpty()) {
             // New images are coming in, clear the old ones
             pipeline.clearImages();
         }
 
         for(String imageString : formData.get("images")){
             try {
                 Long imageId = Long.parseLong(imageString);
                 Image image = Image.findByIdWithOwner(imageId, user);
                 if(image != null) {
                     Logger.warn("Found image "+image);
                     pipeline.addImage(image);
                 } else {
                     Logger.error("No imageId for "+imageString);
                     return badRequest(imageSelect.render(pipeline, "Oops! Couldn't find one of the images you selected."));
                 }
             } catch (NumberFormatException e) {
                 Logger.error("No imageId for "+imageString);
                 return badRequest(imageSelect.render(pipeline,  "Oops! Couldn't find one of the images you selected."));
             }
         }
 
         // OK to proceed to next step
         if(pipeline.getStatus() == models.Pipeline.SELECT_IMAGES) {
             pipeline.setStatus(models.Pipeline.CONFIG_PICKER);
         }
         pipeline.save();
         return ok(picker.render(pipeline, null));
         //return ok(pipeline.getImages().toString());
     }
 
 }
