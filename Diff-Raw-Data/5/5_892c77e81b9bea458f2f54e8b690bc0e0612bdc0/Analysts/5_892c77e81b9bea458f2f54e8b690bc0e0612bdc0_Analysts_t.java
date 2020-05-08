 package controllers;
 
 import com.avaje.ebean.Page;
 import models.Analyst;
 import models.Desk;
 import models.Note;
 import models.Rank;
 import models.User;
 import models.S3File;
 import play.data.Form;
 
 import play.mvc.*;
 import views.html.Analysts.*;
 import utils.Utils;
 
 import java.io.File;
 import java.nio.file.Files;
 import org.joda.time.DateTime;
 
 /**
  * Analysts controller with methods to list, create, edit, update and delete.
  * There are methods to list and modify desks, list and modify notes, and show and upload a profile and CV.
  *
  * Date:        16/10/13
  * Time:        13:35
  *
  * @author      Sav Balac
  * @version     1.2
  */
 @Security.Authenticated(Secured.class) // All methods will require the user to be logged in
 public class Analysts extends AbstractController {
 
     /**
      * Displays a paginated list of analysts.
      *
      * @param page          Current page number (starts from 0).
      * @param sortBy        Column to be sorted.
      * @param order         Sort order (either asc or desc).
      * @param filter        Filter applied on primary desk name.
      * @param search        Search applied on last name.
      * @return Result  The list page or all analysts as JSON.
      */
     public static Result list(int page, String sortBy, String order, String filter, String search) {
 
         // Return data in HTML or JSON as requested
         if (request().accepts("text/html")) {
             // Get a page of analysts and render the list page
             Page<Analyst> pageAnalysts = Analyst.page(page, Application.RECORDS_PER_PAGE, sortBy, order, filter, search);
             return ok(listAnalysts.render(pageAnalysts, sortBy, order, filter, search, getLoggedInUser()));
         } else if (request().accepts("application/json") || request().accepts("text/json")) {
             return ok(Analyst.getAllAsJson(getLoggedInUser()));
         } else {
             return badRequest();
         }
     }
 
 
     /**
      * Creates a new analyst.
      *
      * @return Result  The edit page.
      */
     public static Result create() {
         return edit(0L);
     }
 
 
     /**
      * Displays a form to create a new or edit an existing analyst.
      *
      * @param id  Id of the analyst to edit.
      * @return Result  The edit page or the analyst as JSON.
      */
     public static Result edit(Long id) {
 
         // New analysts have id 0 and don't exist
         Form<Analyst> analystForm;
         Analyst analyst;
         if (id <= 0L) {
             analyst = new Analyst();
         } else {
             // Check analyst exists and return if not
             analyst = Analyst.find.byId(id);
             if (analyst == null) {
                 return noAnalyst(id);
             }
         }
         analystForm = Form.form(Analyst.class).fill(analyst);
 
         // Return data in HTML or JSON as requested
         if (request().accepts("text/html")) {
             return ok(editAnalyst.render(((id<0)?(0L):(id)), analystForm, getLoggedInUser()));
         } else if (request().accepts("application/json") || request().accepts("text/json")) {
             return ok(analyst.toJson(getLoggedInUser()));
         } else {
             return badRequest();
         }
     }
 
 
     /**
      * Returns either the list page or a JSON message when the analyst doesn't exist.
      *
      * @param  id  Id of the analyst.
      * @return Result  The list page or a JSON message.
      */
     private static Result noAnalyst(Long id) {
         // Return data in HTML or JSON as requested
         if (request().accepts("text/html")) {
             return list(0, "lastname", "asc", "", "");
         } else if (request().accepts("application/json") || request().accepts("text/json")) {
             return ok(getErrorAsJson("Analyst: " + id + " does not exist."));
         } else {
             return badRequest();
         }
     }
 
 
     /**
      * Updates the analyst from the form.
      *
      * @param id  Id of the analyst to update.
      * @return Result  The list page or the edit page if in error (in JSON if requested).
      */
     public static Result update(Long id) {
         User loggedInUser = getLoggedInUser();
         Form<Analyst> analystForm = null;
         try {
             analystForm = Form.form(Analyst.class).bindFromRequest(); // Get the form data
             // Check if there are errors
             if (analystForm.hasErrors()) {
                 // Return data in HTML or JSON as requested
                 if (request().accepts("text/html")) {
                     return badRequest(editAnalyst.render(id, analystForm, loggedInUser)); // Return to the editAnalyst page
                 } else if (request().accepts("application/json") || request().accepts("text/json")) {
                     return ok(getErrorsAsJson(analystForm));
                 } else {
                     return badRequest();
                 }
             } else {
                 Analyst newAnalyst = analystForm.get(); // Get the analyst data
 
                 // Updates have a non-zero id
                 if (id == 0) {
                     newAnalyst.createOn = Utils.getCurrentDateTime();
                 } else {
                     // Check analyst exists and return if not
                     Analyst existingAnalyst = Analyst.find.byId(id);
                     if (existingAnalyst == null) {
                         return noAnalyst(id);
                     }
 
                     // Check id supplied by the form is the same as the id parameter (only possible via JSON)
                     if (!newAnalyst.analystId.equals(id)) {
                         return ok(getErrorAsJson("Analyst id in the data (" + newAnalyst.analystId + ") " +
                                                  "does not match the analyst id in the URL (" + id + ")."));
                     }
 
                     // Check if the status has changed and, if so, do some processing (yet to be defined)
                     if (!newAnalyst.status.equals(existingAnalyst.status)) {
                         System.out.println("***** Status id changed from: " + existingAnalyst.status.statusId +
                                                                     " to: " + newAnalyst.status.statusId);
                     }
 
                     // If updating from HTML, disabled checkboxes have an associated disabled field with the original value
                     // Checkboxes, if unchecked or disabled, return null
                     if (request().accepts("text/html")) {
                         if (analystForm.field("disEmailverified").value() != null) {
                             newAnalyst.emailverified = (analystForm.field("disEmailverified").value().equals("true"));
                         } else {
                             newAnalyst.emailverified =
                                 (analystForm.field("emailverified").value() == null) ? (false) : (newAnalyst.emailverified);
                         }
                         if (analystForm.field("disPhoneverified").value() != null) {
                             newAnalyst.phoneVerified = (analystForm.field("disPhoneverified").value().equals("true"));
                         } else {
                             newAnalyst.phoneVerified =
                                 (analystForm.field("phoneVerified").value() == null) ? (false) : (newAnalyst.phoneVerified);
                         }
                         if (analystForm.field("disContractSigned").value() != null) {
                             newAnalyst.contractSigned = (analystForm.field("disContractSigned").value().equals("true"));
                         } else {
                             newAnalyst.contractSigned =
                                 (analystForm.field("contractSigned").value() == null) ? (false) : (newAnalyst.contractSigned);
                         }
 
                     // Updating from JSON. The following fields are invisible or disabled, so ignore the form values
                     } else {
 
                         // If the logged-in user is not an admin user or manager,
                         //     phone and emailAlternate are invisible.
                         if (!loggedInUser.isAdminOrManager()) {
                             newAnalyst.phone            = existingAnalyst.phone;
                             newAnalyst.emailAlternate   = existingAnalyst.emailAlternate;
 
                             // If the logged-in user is not an admin user, manager or staff,
                             //     paypalAccountEmail and all address fields are invisible;
                             //     rank, emailverified, phoneVerified, contractSigned and wikiUsername are disabled;
                             //     status values "Deleted" and those starting with "Removed" are removed from the list.
                             if (!loggedInUser.isStaff()) {
                                 newAnalyst.paypalAccountEmail   = existingAnalyst.paypalAccountEmail;
                                 newAnalyst.address1             = existingAnalyst.address1;
                                 newAnalyst.address2             = existingAnalyst.address2;
                                 newAnalyst.city                 = existingAnalyst.city;
                                 newAnalyst.state                = existingAnalyst.state;
                                 newAnalyst.zip                  = existingAnalyst.zip;
                                 newAnalyst.country              = existingAnalyst.country;
                                 newAnalyst.countryOfResidence   = existingAnalyst.countryOfResidence;
                                 newAnalyst.rank                 = existingAnalyst.rank;
                                 newAnalyst.emailverified        = existingAnalyst.emailverified;
                                 newAnalyst.phoneVerified        = existingAnalyst.phoneVerified;
                                 newAnalyst.contractSigned       = existingAnalyst.contractSigned;
                                 newAnalyst.wikiUsername         = existingAnalyst.wikiUsername;
                                // Don't change the status if the supplied status is deleted or removed
                                // Don't allow an existing status of deleted or removed to be changed
                                if (newAnalyst.status.isDeletedOrRemoved() ||
                                    existingAnalyst.status.isDeletedOrRemoved()) {
                                     newAnalyst.status = existingAnalyst.status;
                                 }
                             }
                         }
                     }
                 }
 
                 // Save if a new analyst, otherwise update, and show a message
                 newAnalyst.saveOrUpdate();
                 String fullName = analystForm.get().firstname + " " + analystForm.get().lastname;
                 String msg;
                 if (id == 0) {
                     msg = "Analyst: " + fullName + " created.";
                 } else {
                     msg = "Analyst: " + fullName + " updated.";
                 }
                 return actionSuccessful(id, msg, PAGE_TYPE_LIST);
             }
         } catch (Exception e) {
             // Log an error
             Utils.eHandler("Analysts.update(" + id + ")", e);
 
             // Return data in HTML or JSON as requested
             if (request().accepts("text/html")) {
                 // Show a message and return to the editAnalyst page
                 showSaveError(e); // Method in AbstractController
                 return badRequest(editAnalyst.render(id, analystForm, loggedInUser));
             } else if (request().accepts("application/json") || request().accepts("text/json")) {
                 String msg;
                 if (id == 0) {
                     msg = "Analyst not created.";
                 } else {
                     msg = "Analyst: " + id + " not updated.";
                 }
                 msg += " Error: " + e.getMessage();
                 return ok(getErrorAsJson(msg));
             } else {
                 return badRequest();
             }
         }
     }
 
 
     /**
      * Return the list/edit page or JSON when data is successfully created/updated/deleted.
      *
      * @param id       The analyst id.
      * @param msg      The created/updated/deleted message.
      * @return Result  A result containing the response, either as HTML or JSON.
      */
     private static Result actionSuccessful(Long id, String msg, int pageType) {
         // Return data in HTML or JSON as requested
         if (request().accepts("text/html")) {
             flash(Utils.KEY_SUCCESS, msg);
             // Go to the list or edit page
             switch (pageType) {
                 case PAGE_TYPE_LIST: // Redirect to remove the analyst from the query string
                     return redirect(controllers.routes.Analysts.list(0, "lastname", "asc", "", ""));
                 case PAGE_TYPE_EDIT:
                     return redirect(controllers.routes.Analysts.edit(id));
                 default:
                     return null;
             }
         } else if (request().accepts("application/json") || request().accepts("text/json")) {
             return ok(getSuccessAsJson(msg));
         } else {
             return badRequest();
         }
     }
 
 
     /**
      * Deletes the analyst.
      *
      * @param id  Id of the analyst to delete.
      * @return Result  The list page or a message in JSON.
      */
     public static Result delete(Long id) {
         try {
             // Check analyst exists and return if not
             Analyst analyst = Analyst.find.byId(id);
             if (analyst == null) {
                 return noAnalyst(id);
             }
             String fullName = analyst.getFullName();
 
             // Delete desks and notes
             analyst.delAllDesks();
             analyst.delAllNotes();
 
             // Delete profile image and CV (no foreign keys)
             if (analyst.profileImage != null) {
                 S3File profileImage = S3File.find.byId(analyst.profileImage.id);
                 if (profileImage != null) {
                     profileImage.delete();
                 }
             }
             if (analyst.cvDocument != null) {
                 S3File cvDocument = S3File.find.byId(analyst.cvDocument.id);
                 if (cvDocument != null) {
                     cvDocument.delete();
                 }
             }
 
             // Delete the analyst
             analyst.delete();
             String msg = "Analyst: " + fullName + " deleted.";
             return actionSuccessful(id, msg, PAGE_TYPE_LIST);
         } catch (Exception e) {
             // Log an error
             Utils.eHandler("Analysts.delete(" + id + ")", e);
             String msg = "Analyst: " + id + " not deleted. Error: " + e.getMessage();
             // Return data in HTML or JSON as requested
             if (request().accepts("text/html")) {
                 showSaveError(e);
                 return redirect(controllers.routes.Analysts.list(0, "lastname", "asc", "", ""));
             } else if (request().accepts("application/json") || request().accepts("text/json")) {
                 return ok(getErrorAsJson(msg));
             } else {
                 return badRequest();
             }
         }
     }
 
 
     /**
      * Displays a (usually embedded) form to display and upload a profile image.
      *
      * @param id  Id of the analyst.
      * @return Result  The profile image template or JSON.
      */
     public static Result editProfileImage(Long id) {
         Analyst analyst = Analyst.find.byId(id);
         // Return data in HTML or JSON as requested
         if (request().accepts("text/html")) {
             return ok(tagAnalystImage.render(analyst)); // This template handles null analysts
         } else if (request().accepts("application/json") || request().accepts("text/json")) {
             if (analyst == null) {
                 return noAnalyst(id);
             }
             return ok(analyst.getProfileImageAsJson());
         } else {
             return badRequest();
         }
     }
 
 
     /**
      * Displays a (usually embedded) form to display and upload a CV document.
      *
      * @param id  Id of the analyst.
      * @return Result  The CV document template or JSON.
      */
     public static Result editCvDocument(Long id) {
         Analyst analyst = Analyst.find.byId(id);
         // Return data in HTML or JSON as requested
         if (request().accepts("text/html")) {
             return ok(tagAnalystCV.render(analyst)); // This template handles null analysts
         } else if (request().accepts("application/json") || request().accepts("text/json")) {
             if (analyst == null) {
                 return noAnalyst(id);
             }
             return ok(analyst.getCvDocumentAsJson());
         } else {
             return badRequest();
         }
     }
 
 
     /**
      * Uploads the profile image.
      *
      * @param id  Id of the analyst.
      * @return Result  Contains "OK" if successful or an error message.
      */
     public static Result uploadProfileImage(Long id) {
         return checkUpload(id, "profile");
     }
 
 
     /**
      * Uploads the CV document.
      *
      * @param id  Id of the analyst.
      * @return Result  Contains "OK" if successful or an error message.
      */
     public static Result uploadCvDocument(Long id) {
         return checkUpload(id, "document");
     }
 
 
     /**
      * Calls the uploadFile method and returns data as requested.
      *
      * @param id        Id of the analyst.
      * @param fileType  "profile" or "document".
      * @return Result   Contains "OK" if successful or an error message.
      */
     private static Result checkUpload(Long id, String fileType) {
         // Upload the file and return data as text or JSON as requested (browser calls use Ajax and test if "OK")
         return getAjaxResponse(uploadFile(id, fileType)); // getAjaxResponse() is in AbstractController
     }
 
 
     /**
      * Uploads a file to AWS S3 and sets the analyst field value.
      *
      * @param id        Id of the analyst
      * @param fileType  "profile" or "document".
      * @return String   Contains "OK" if successful or an error message.
      */
     public static String uploadFile(Long id, String fileType) {
         File file = null;
         try {
             // Uploads to new analysts should be prevented by the view, but test for JSON
             if (id == 0) {
                 return "ERROR: Analyst must be saved before uploading files.";
             }
 
             // Get the analyst
             Analyst analyst = Analyst.find.byId(id);
             if (analyst == null) {
                 return "ERROR: Analyst not found. File not saved.";
             }
 
             boolean hasForm = false;
             String fileName = null;
             String contentType = null;
 
             // Get the file details from the form
             Http.MultipartFormData.FilePart filePart = null;
             Http.MultipartFormData body = request().body().asMultipartFormData();
             if (body != null) {
                 hasForm = true;
                 filePart = body.getFile(fileType);
                 if (filePart != null) {
                     fileName = filePart.getFilename();
                     file = filePart.getFile();
                     contentType = filePart.getContentType();
                 }
             }
 
             // JSON requests may not have a form
             if (!hasForm && (request().accepts("application/json") || request().accepts("text/json"))) {
                 file = request().body().asRaw().asFile();
                 // Raw files don't have the correct file name and content type
                 if (fileType.equals("profile")) {
                     fileName = "Profile";
                 } else {
                     fileName = "CV";
                 }
                 contentType = Files.probeContentType(file.toPath()); // Usually null, but get for the test below
             }
             filePart = null;
 
             // If uploading a profile image, check it is an image
             if (fileType.equals("profile") && contentType != null) {
                 if (!contentType.startsWith("image/")) {
                     return "ERROR: File " + fileName + " is not an image. File not saved.";
                 }
             }
 
             // Check the file exists
             if (file != null) {
 
                 // Check if the file exceeds the maximum allowable size
                 if (Files.size(file.toPath()) > Utils.MAX_FILE_SIZE) {
                     return "ERROR: File " + fileName + " exceeds the maximum size allowed of " +
                            Utils.MAX_FILE_SIZE_STRING + ". File not saved.";
                 }
 
                 // Check if the analyst already has a file and delete it
                 if (fileType.equals("profile")) {
                     if (analyst.profileImage != null) {
                         S3File profileImage = S3File.find.byId(analyst.profileImage.id);
                         profileImage.delete();
                     }
                 } else {
                     if (analyst.cvDocument != null) {
                         S3File cvDocument = S3File.find.byId(analyst.cvDocument.id);
                         cvDocument.delete();
                     }
                 }
 
                 // Save the new file to AWS S3
                 S3File s3File = new S3File();
                 s3File.name = fileName;
                 s3File.file = file;
                 s3File.save();
 
                 // Save the s3File to the analyst, update and show a message
                 if (fileType.equals("profile")) {
                     analyst.profileImage = s3File;
                 } else {
                     analyst.cvDocument = s3File;
                 }
                 analyst.update();
                 String msg = "File: " + fileName + " uploaded to analyst: " + analyst.getFullName();
                 if (request().accepts("text/html")) {
                     flash(Utils.KEY_SUCCESS, msg);
                     return "OK"; // The Ajax call from editAnalyst checks for "OK"
                 } else {
                     return msg; // for JSON
                 }
             } else { // File not found
                 return "ERROR: Please select a file.";
             }
         } catch (Exception e) {
             Utils.eHandler("Analysts.uploadFile(" + id + ", " + fileType + ")", e);
             return "ERROR: " + String.format("%s Changes not saved.", e.getMessage());
         } finally {
             // Free up resources
             if (file != null) {
                 file = null;
             }
             System.gc();
         }
     }
 
 
     /**
      * Displays a (usually embedded) form to display the desks an analyst is assigned to.
      *
      * @param id  Id of the analyst.
      * @return Result  The desks template or JSON.
      */
     public static Result editDesks(Long id) {
         Analyst analyst = Analyst.find.byId(id);
         // Return data in HTML or JSON as requested
         if (request().accepts("text/html")) {
             return ok(tagListDeskAnalysts.render(analyst)); // This template handles null analysts
         } else if (request().accepts("application/json") || request().accepts("text/json")) {
             if (analyst == null) {
                 return noAnalyst(id);
             }
             return ok(analyst.getDesksAsJson());
         } else {
             return badRequest();
         }
     }
 
 
     /**
      * Assigns the analyst to a desk.
      *
      * @param id       Id of the analyst.
      * @param deskId   Id of the desk.
      * @return Result  Contains "OK" if successful or an error message.
      */
     public static Result addDesk(Long id, Long deskId) {
         return changeDesk(id, deskId, "add");
     }
 
 
     /**
      * Deletes a desk from the analyst.
      *
      * @param id       Id of the analyst.
      * @param deskId   Id of the desk.
      * @return Result  Contains "OK" if successful or an error message.
      */
     public static Result delDesk(Long id, Long deskId) {
         return changeDesk(id, deskId, "delete");
     }
 
 
     /**
      * Calls the updateDesk method and returns data as requested.
      *
      * @param id       Id of the analyst.
      * @param groupId  Id of the group.
      * @param action   "add" or "delete".
      * @return Result  Contains "OK" if successful or an error message.
      */
     private static Result changeDesk(Long id, Long groupId, String action) {
         // Update the desk and return data as text or JSON as requested (browser calls use Ajax and test if "OK")
         return getAjaxResponse(updateDesk(id, groupId, action));
     }
 
 
     /**
      * Adds or deletes a desk to/from the analyst.
      *
      * @param id       Id of the analyst.
      * @param deskId   Id of the desk.
      * @param action   "add" or "delete".
      * @return Result  Contains "OK" if successful or an error message.
      */
     private static String updateDesk(Long id, Long deskId, String action) {
         Analyst analyst = Analyst.find.byId(id);
         Desk desk = Desk.find.byId(deskId);
         try {
             if (analyst == null) {
                 return "ERROR: Analyst not found. Changes not saved.";
             }
             if (desk == null) {
                 return "ERROR: Desk not found. Changes not saved.";
             } else {
                 // Add, delete or return an error
                 if (action.equals("add")) {
                     analyst.addDesk(desk);
                 } else if (action.equals("delete")) {
                     analyst.delDesk(desk);
                 } else {
                     return "ERROR: incorrect action, use add or delete";
                 }
                 if (request().accepts("text/html")) {
                     return "OK"; // "OK" is used by the calling Ajax function
                 } else {
                     return "Analyst: " + id + ", " + action + " desk: " + deskId + " was successful."; // for JSON
                 }
             }
         }
         catch (Exception e) {
             Utils.eHandler("Analysts.changeDesk(" + id + ", " + deskId + ", " + action + ")", e);
             return "ERROR: " + e.getMessage();
         }
     }
 
 
     /**
      * Displays a (usually embedded) form to display the notes that have been written about an analyst.
      *
      * @param id  Id of the analyst.
      * @return Result  The notes template or JSON.
      */
     public static Result editNotes(Long id) {
         Analyst analyst = Analyst.find.byId(id);
         // Return data in HTML or JSON as requested
         if (request().accepts("text/html")) {
             return ok(tagListNotes.render(analyst)); // This template handles null analysts
         } else if (request().accepts("application/json") || request().accepts("text/json")) {
             if (analyst == null) {
                 return noAnalyst(id);
             }
             return ok(analyst.getNotesAsJson());
         } else {
             return badRequest();
         }
     }
 
 
     /**
      * Creates a new note.
      *
      * @return Result  The edit page.
      */
     public static Result createNote(Long aId) {
         return editNote(aId, 0L);
     }
 
 
     /**
      * Displays a form to create a new or edit an existing note.
      *
      * @param aId Id of the analyst.
      * @param nId Id of the note to edit.
      * @return Result  The edit page or the note as JSON.
      */
     public static Result editNote(Long aId, Long nId) {
 
         // Check analyst exists and return if not
         Analyst analyst = Analyst.find.byId(aId);
         if (analyst == null) {
             return noAnalyst(aId);
         }
 
         // New notes have id 0 and don't exist
         Form<Note> noteForm;
         Note note;
         if (nId <= 0L) {
             note = new Note();
             note.analyst = analyst; // Set the analyst
         }
         else {
             // Check note exists and return if not
             note = Note.find.byId(nId);
             if (note == null) {
                 return noNote(aId, nId);
             }
         }
         noteForm = Form.form(Note.class).fill(note);
 
         // Return data in HTML or JSON as requested
         if (request().accepts("text/html")) {
             return ok(editNote.render(((nId<0)?(0L):(nId)), aId, noteForm, getLoggedInUser()));
         } else if (request().accepts("application/json") || request().accepts("text/json")) {
             return ok(note.toJson());
         } else {
             return badRequest();
         }
 
     }
 
 
     /**
      * Returns either the edit analyst page or a JSON message when the note doesn't exist.
      *
      * @param aId Id of the analyst.
      * @param nId Id of the note.
      * @return Result  The edit page or a JSON message.
      */
     private static Result noNote(Long aId, Long nId) {
         // Return data in HTML or JSON as requested
         if (request().accepts("text/html")) {
             return edit(aId);
         } else if (request().accepts("application/json") || request().accepts("text/json")) {
             return ok(getErrorAsJson("Note: " + nId + " for analyst: " + aId + " does not exist."));
         } else {
             return badRequest();
         }
     }
 
 
     /**
      * Updates the note from the form.
      *
      * @param aId  Id of the analyst.
      * @param nId  Id of the note to edit.
      * @return Result  The edit analyst page, edit note page if in error, or JSON.
      */
     public static Result updateNote(Long aId, Long nId) {
         User loggedInUser = getLoggedInUser();
         Form<Note> noteForm = null;
         try {
             noteForm = Form.form(Note.class).bindFromRequest(); // Get the form data
             // Check if there are errors
             if (noteForm.hasErrors()) {
                 // Return data in HTML or JSON as requested
                 if (request().accepts("text/html")) {
                     return badRequest(editNote.render(nId, aId, noteForm, loggedInUser)); // Return to the editNote page
                 } else if (request().accepts("application/json") || request().accepts("text/json")) {
                     return ok(getErrorsAsJson(noteForm));
                 } else {
                     return badRequest();
                 }
             } else {
                 // Check analyst exists and return if not
                 Analyst analyst = Analyst.find.byId(aId);
                 if (analyst == null) {
                     return noAnalyst(aId);
                 }
 
                 // Get the note data
                 Note note = noteForm.get();
 
                 // Get the current date and time
                 DateTime now = Utils.getCurrentDateTime();
 
                 // Save if a new note and add the note to the analyst, otherwise update, and show a message
                 String msg;
                 if (nId == 0) {
                     note.analyst = analyst;
                     note.user = loggedInUser;
                     note.createdDt = now;
                     note.save();
                     analyst.addNote(note);
                     msg = "Note: " + note.title + " created.";
                 } else {
                     // Check note exists and return if not
                     Note existingNote = Note.find.byId(nId);
                     if (existingNote == null) {
                         return noNote(aId, nId);
                     }
                     note.createdDt = existingNote.createdDt; // Ensure the created datetime isn't overwritten
                     note.updatedBy = loggedInUser;
                     note.updatedDt = now;
                     note.update();
                     msg = "Note: " + note.title + " updated.";
                 }
                 return actionSuccessful(aId, msg, PAGE_TYPE_EDIT);
             }
         } catch (Exception e) {
             // Log an error
             Utils.eHandler("Analysts.updateNote(" + aId + ", " + nId + ")", e);
 
             // Return data in HTML or JSON as requested
             if (request().accepts("text/html")) {
                 showSaveError(e);
                 return badRequest(editNote.render(nId, aId, noteForm, loggedInUser));
             } else if (request().accepts("application/json") || request().accepts("text/json")) {
                 String msg;
                 if (nId == 0) {
                     msg = "Note for analyst: " + aId + " not created.";
                 } else {
                     msg = "Note: " + nId + " for analyst: " + aId + " not updated.";
                 }
                 msg += " Error: " + e.getMessage();
                 return ok(getErrorAsJson(msg));
             } else {
                 return badRequest();
             }
         }
     }
 
 
     /**
      * Calls the delNoteAjax method and returns data as requested. This version is called from the note list via Ajax.
      *
      * @param aId      Id of the analyst.
      * @param noteId   Id of the note.
      * @return Result  Contains "OK" if successful or an error message.
      */
     public static Result delNote(Long aId, Long noteId) {
         // Delete the note and return data as text or JSON as requested (browser calls use Ajax and test if "OK")
         return getAjaxResponse(delNoteFromAjax(aId, noteId));
     }
 
 
     /**
      * Deletes a note from the analyst. This version is called from the note list via Ajax.
      *
      * @param aId      Id of the analyst.
      * @param noteId   Id of the note.
      * @return String  Contains "OK" if successful or an error message.
      */
     private static String delNoteFromAjax(Long aId, Long noteId) {
         Analyst analyst = Analyst.find.byId(aId);
         Note note = Note.find.byId(noteId);
         try {
             if (analyst == null) {
                 return "ERROR: Analyst not found. Changes not saved.";
             }
             if (note == null) {
                 return "ERROR: Note not found. Changes not saved.";
             } else {
                 // Remove the note from the analyst and delete it
                 analyst.delNote(note);
                 note.delete();
                 return "OK"; // "OK" is used by the calling Ajax function
             }
         }
         catch (Exception e) {
             Utils.eHandler("Analysts.delNote(" + aId + ", " + noteId + ")", e);
             return "ERROR: " + e.getMessage();
         }
     }
 
 
     /**
      * Deletes the analyst. This version is called from the editNote page.
      *
      * @param aId      Id of the analyst.
      * @param noteId   Id of the note.
      * @return Result  The edit analyst page or JSON.
      */
     public static Result deleteNote(Long aId, Long noteId) {
         try {
             // Check analyst exists and return if not
             Analyst analyst = Analyst.find.byId(aId);
             if (analyst == null) {
                 return noAnalyst(aId);
             }
 
             // Check note exists and return if not
             Note note = Note.find.byId(noteId);
             if (note == null) {
                 return noNote(aId, noteId);
             }
 
             // Remove the note from the analyst and delete it
             String msg = "Note: " + note.title + " deleted.";
             analyst.delNote(note);
             note.delete();
             return actionSuccessful(aId, msg, PAGE_TYPE_EDIT);
         } catch (Exception e) {
             // Log an error
             Utils.eHandler("Analysts.deleteNote(" + aId + ", " + noteId + ")", e);
             String msg = "Note: " + noteId + " not deleted from analyst: " + aId + ". Error: " + e.getMessage();
             // Return data in HTML or JSON as requested
             if (request().accepts("text/html")) {
                 showSaveError(e);
                 return edit(aId);
             } else if (request().accepts("application/json") || request().accepts("text/json")) {
                 return ok(getErrorAsJson(msg));
             } else {
                 return badRequest();
             }
         }
     }
 
 
     /**
      * Returns all ranks. Only used for JSON requests.
      *
      * @return Result  All ranks as JSON.
      */
     public static Result listRanks() {
         // If HTML requested, go to the home page
         if (request().accepts("text/html")) {
             return redirect(controllers.routes.Application.index());
         } else if (request().accepts("application/json") || request().accepts("text/json")) {
             return ok(Rank.getAllAsJson());
         } else {
             return badRequest();
         }
     }
 
 
     /**
      * Returns all desks. Only used for JSON requests.
      *
      * @return Result  All desks as JSON.
      */
     public static Result listDesks() {
         // If HTML requested, go to the home page
         if (request().accepts("text/html")) {
             return redirect(controllers.routes.Application.index());
         } else if (request().accepts("application/json") || request().accepts("text/json")) {
             return ok(Desk.getAllAsJson());
         } else {
             return badRequest();
         }
     }
 
 
     /**
      * Returns all statuses. Only used for JSON requests.
      *
      * @return Result  All statuses as JSON.
      */
     public static Result listStatuses() {
         // If HTML requested, go to the home page
         if (request().accepts("text/html")) {
             return redirect(controllers.routes.Application.index());
         } else if (request().accepts("application/json") || request().accepts("text/json")) {
             return ok(models.Status.getAllAsJson(getLoggedInUser()));
         } else {
             return badRequest();
         }
     }
 
 
 }
