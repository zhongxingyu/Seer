 package edu.northwestern.bioinformatics.studycalendar.restlets;
 
 import org.restlet.Context;
 import org.restlet.data.*;
 import org.restlet.resource.Representation;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.Variant;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class UserActionsResource extends AbstractPscResource {
     protected final Logger log = LoggerFactory.getLogger(getClass());
 
     @Override
     public void init(Context context, Request request, Response response) {
         super.init(context, request, response);
         setAllAuthorizedFor(Method.POST);
        setReadable(false);
         getVariants().add(new Variant(MediaType.APPLICATION_JSON));
     }
 
     @Override public boolean allowPost() { return true; }
 
     @Override
     public void acceptRepresentation(Representation representation) throws ResourceException {
         if (representation.getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
 //            UserActionJSONRepresentation json = createJSONRepresentation(representation);
 
             getResponse().setStatus(Status.SUCCESS_CREATED);
 //
             getResponse().setLocationRef(String.format(
                 "user-actions/%s", Reference.encode("80460809-2e90-4ea5-ac29-ca23639bf460")));
         } else {
             throw new ResourceException(
                 Status.CLIENT_ERROR_BAD_REQUEST, "Unsupported content type: " + representation.getMediaType());
         }
     }
 //
 //    private UserActionJSONRepresentation createJSONRepresentation(Representation representation) throws ResourceException {
 //        try {
 //            return new UserActionJSONRepresentation(representation.getText());
 //        } catch (JSONException e) {
 //            throw new StudyCalendarSystemException("Problem parsing json representation", e);
 //        } catch (IOException e) {
 //            throw new StudyCalendarSystemException("Problem parsing json representation", e);
 //        }
 //    }
 //
 //    private class UserActionPost {
 //        private JSONObject json;
 //
 //        private UserActionPost(JSONObject json) {
 //            this.json = json;
 //        }
 //
 //        public UserAction apply() {
 //
 //        }
 //    }
 //
 //    private class UserActionJSONRepresentation {
 //        private JSONObject wrapper;
 //
 //        private UserActionJSONRepresentation(String json) throws JSONException {
 //            this.wrapper = new JSONObject(json);
 //        }
 //    }
 }
