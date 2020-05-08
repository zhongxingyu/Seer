 package controllers;
 
 import static play.data.Form.form;
 
 import java.util.List;
 
 import models.Transaction;
 import play.data.Form;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.transactionFormCreate;
 import views.html.transactionFormEdit;
 import views.html.transactionFormList;
 
 public class Application extends Controller {
   
 	
 	/**
 	 * This result directly redirect to application home.
 	 */
 	public static Result GO_HOME = redirect(
 		routes.Application.list(0, "date", "desc", "")
 	);
 	
 	/**
 	 * This result directly redirect to application home.
 	 */
 	public static Result GO_ACCOUNTS = redirect(
 		routes.Accounts.list(0, "owner", "asc", "")
 	);
 
     
     public static Result index() {
         return GO_HOME;
     }
     
 
     /**
      * Display the paginated list of transactions.
      *
      * @param page Current page number (starts from 0)
      * @param sortBy Column to be sorted
      * @param order Sort order (either asc or desc)
      * @param filter Filter applied on transaction reference
      */
     public static Result list(int page, String sortBy, String order, String filter) {
     	
         return ok(
                 transactionFormList.render(  //requires import views.html.*;
                     Transaction.page(page, 10, sortBy, order, filter),
                     sortBy, order, filter
                 )
             );
     	
     }
     
     /**
      * Display the paginated list of transactions according to a fileter.
      *
      * @param page Current page number (starts from 0)
      * @param sortBy Column to be sorted
      * @param order Sort order (either asc or desc)
      * @param filterBy Column to be filtered 
      * @param filter Filter applied on the column
      */
     public static Result query(int page, String sortBy, String order, String filterBy, String filter) {
     	
         return ok(
                 transactionFormList.render(  //requires import views.html.*;
                     Transaction.pageByCustomColumn(page, 10, sortBy, order, filterBy, filter),
                     sortBy, order, filter
                 )
             );
     	
     }
     
     public static Result listTransactionsForAccount(String filter) {
     	//TODO: Allow for more than 10 objects -> hasNext()?
     	List<Transaction> transactionList = Transaction.pageByCustomColumn(0, 10, "date", "desc", "accountFrom", filter).getList();
     	return ok(Json.toJson(transactionList));
     }
     
    public static Result listCreditForAccount(String filter) {
    	//TODO: Allow for more than 10 objects -> hasNext()?
    	List<Transaction> transactionList = Transaction.pageByCustomColumn(0, 1000, "date", "desc", "accountTo", filter).getList();
    	return ok(Json.toJson(transactionList));
    }
    
    public static Result listDebitForAccount(String filter) {
    	//TODO: Allow for more than 10 objects -> hasNext()?
    	List<Transaction> transactionList = Transaction.pageByCustomColumn(0, 1000, "date", "desc", "accountFrom", filter).getList();
    	return ok(Json.toJson(transactionList));
    }
    
     
     /**
      * Display the 'edit form' of a existing Transaction.
      *
      * @param id Id of the computer to edit
      */
     public static Result edit(Long id) {
         Form<Transaction> transactionForm = form(Transaction.class).fill(
         		Transaction.find.byId(id)
             );
             return ok(
                 transactionFormEdit.render(id, transactionForm)
             );
     }
     
     
     /**
      * Handle the 'edit form' submission 
      *
      * @param id Id of the transaction to edit
      */
     public static Result update(Long id) {
         Form<Transaction> transactionForm = form(Transaction.class).bindFromRequest();
         if(transactionForm.hasErrors()){
         	return badRequest(transactionFormEdit.render(id, transactionForm));
         }
         transactionForm.get().update(id);
         flash("success", "Transaction " + transactionForm.get().id + " has been updated");
         return GO_HOME;
     }
     
 
     /**
      * Handle transaction deletion
      */
     public static Result delete(Long id) {
         Transaction.find.ref(id).delete();
         flash("success", "Transaction has been deleted");
         return GO_HOME;
     }
 
     
     
     /**
      * Display the 'new transaction form'.
      */
     public static Result create() {
         Form<Transaction> transactionForm = form(Transaction.class);
         return ok(
         	transactionFormCreate.render(transactionForm)
         );
     }
     
     /**
      * Handle the 'new transaction form' submission 
      */
     public static Result save(){
     	Form<Transaction> transactionForm = form(Transaction.class).bindFromRequest();
     	if(transactionForm.hasErrors()) { //TODO: Create custom validation?
         	return badRequest(transactionFormCreate.render(transactionForm));
         }
         transactionForm.get().save();
         flash("success", "Transaction with ID=" + transactionForm.get().id + " has been created");
         return GO_HOME;
     }
     
   
 }
