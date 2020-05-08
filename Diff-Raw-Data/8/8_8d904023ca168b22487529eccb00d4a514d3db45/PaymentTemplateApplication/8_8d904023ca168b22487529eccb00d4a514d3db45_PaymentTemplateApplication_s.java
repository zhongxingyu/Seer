 package controllers;
 
 import java.util.List;
 
 import models.PaymentTemplate;
 import play.Logger.ALogger;
 import play.data.Form;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security;
 import views.html.payment_templates.createForm;
 import views.html.payment_templates.editForm;
 import views.html.payment_templates.list;
 import static play.data.Form.*;
 
 /**
  * Manage a database of paymentTemplates
  */
 @Security.Authenticated(Secured.class)
 public class PaymentTemplateApplication extends Controller {
 
 	static ALogger logger = play.Logger.of(PaymentTemplateApplication.class);
 	
     /**
      * This result directly redirect to application home.
      */
     public static Result GO_HOME = redirect(
         routes.PaymentTemplateApplication.list(0, "name", "asc", "")
     );
     
     /**
      * Handle default path requests, redirect to paymentTemplates list
      */
     public static Result index() {
         return GO_HOME;
     }
 
     /**
      * Display the paginated list of paymentTemplates.
      *
      * @param page Current page number (starts from 0)
      * @param sortBy Column to be sorted
      * @param order Sort order (either asc or desc)
      * @param filter Filter applied on paymentTemplate names
      */
     public static Result list(int page, String sortBy, String order, String filter) {
         return ok(
             list.render(
                 PaymentTemplate.page(page, 10, sortBy, order, filter),
                 sortBy, order, filter
             )
         );
     }
     
     public static Result apiList(){
     	
     	List<PaymentTemplate> paymentTemplates = PaymentTemplate.find.findList();
     	
     	for(PaymentTemplate p : paymentTemplates){
    		p.payee.credential = null;
    		p.payer.credential = null;
     	}
     	
     	return ok(Json.toJson(paymentTemplates));
     }
     
     /**
      * Display the 'edit form' of a existing PaymentTemplate.
      *
      * @param id Id of the paymentTemplate to edit
      */
     public static Result edit(Long id) {
         Form<PaymentTemplate> paymentTemplateForm = form(PaymentTemplate.class).fill(
             PaymentTemplate.find.byId(id)
         );
         return ok(
             editForm.render(id, paymentTemplateForm)
         );
     }
     
     /**
      * Handle the 'edit form' submission 
      *
      * @param id Id of the paymentTemplate to edit
      */
     public static Result update(Long id) {
         Form<PaymentTemplate> paymentTemplateForm = form(PaymentTemplate.class).bindFromRequest();
         if(paymentTemplateForm.hasErrors()) {
             return badRequest(editForm.render(id, paymentTemplateForm));
         }
         paymentTemplateForm.get().update(id);
         
         logger.debug(paymentTemplateForm.get().payer.name);
         
         flash("success", "Payment Type has been updated");
         return GO_HOME;
     }
     
     /**
      * Display the 'new paymentTemplate form'.
      */
     public static Result create() {
         Form<PaymentTemplate> paymentTemplateForm = form(PaymentTemplate.class);
         return ok(
             createForm.render(paymentTemplateForm)
         );
     }
     
     /**
      * Handle the 'new paymentTemplate form' submission 
      */
     public static Result save() {
         Form<PaymentTemplate> paymentTemplateForm = form(PaymentTemplate.class).bindFromRequest();
         if(paymentTemplateForm.hasErrors()) {
             return badRequest(createForm.render(paymentTemplateForm));
         }
         paymentTemplateForm.get().save();
         flash("success", "Payment Type has been created");
         return GO_HOME;
     }
     
     /**
      * Handle paymentTemplate deletion
      */
     public static Result delete(Long id) {
         PaymentTemplate.find.ref(id).delete();
         flash("success", "Payment Type has been deleted");
         return GO_HOME;
     }
     
 
 }
             
