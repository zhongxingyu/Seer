 package controllers;
 
 import java.util.List;
 
 import javax.persistence.Query;
 
 import form.SigninForm;
 import form.SignupForm;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.home.*;
 import views.html.search.*;
 import play.data.DynamicForm;
 import play.data.Form;
 import play.db.jpa.JPA;
 import play.db.jpa.Transactional;
 import model.User;
 import model.UserBizMap;
 
 public class Home extends Controller {
 
 	public static Result index() {
 		return ok(signin.render(new SigninForm()));
 	}
 
 	public static Result showSignup() {
 		return ok(signup.render(new SignupForm()));
 	}
 
 	@play.db.jpa.Transactional
 	public static Result signup() {
 		Form<SignupForm> aForm=new Form(SignupForm.class);
 		SignupForm form=aForm.bindFromRequest().get();
 		boolean valid=form.fValidate();
 
 		if (valid){
 			//check email existed or not
 			Query aQuery=JPA.em().createQuery("from User where email=?");
 		    aQuery.setParameter(1, form.email);
 		    List<User> users=aQuery.getResultList();{
 		    	if (users!=null && users.size()>0){
 		    		valid=false;
 		    		form.userExistError="User with this email already exists";
 		    	}
 		    }
 			
 		}
 		if (!valid)
			return ok(signup.render(form));
 		else{
 			//persist
 			User aUser=new User();
 			aUser.lName=form.lname;
 			aUser.fName=form.fname;
 			aUser.email=form.email;
 			aUser.pwd=form.pwd;
 		
 			JPA.em().persist(aUser);
 			 flash("success", "Signup successfully. Please login to use the system.");
			 return redirect("/");
 		}
 	}
 
 	@play.db.jpa.Transactional(readOnly = true)
 	public static Result signin() {
 		Form<SigninForm> form=new Form(SigninForm.class).bindFromRequest();
 		SigninForm loginForm=form.get();
         boolean valid=loginForm.fValidate();
         User loginUser=null;
 		if (valid){
 			//authenticate user
 			 Query aQuery=JPA.em().createQuery("from User where email=? and pwd=?");
 		        aQuery.setParameter(1, loginForm.email);
 		        aQuery.setParameter(2, loginForm.pwd);
 		        List<User> users=aQuery.getResultList();
 		        if(users==null || users.size()<=0){
 		        	loginForm.authError="Invalid sign in email and password combination.";
 		        	valid=false;
 		        }else{
 		        	valid=true;
 		        	loginUser=users.get(0);
 		        }
 		}
 		if(valid){
 			session().put(util.Constants.SESSION_USERID, loginUser.id.toString());
 			 Query aQuery=JPA.em().createQuery("from UserBizMap where Id.uId=?");
 			 aQuery.setParameter(1, loginUser.id);
 			 List<UserBizMap> map=aQuery.getResultList();
 			 if (map.size()>0){
 				 return redirect("/myBiz");
 			 }else{
 				 return  redirect("/showSearch");
 			 }
 		}else{
 			return ok(signin.render(loginForm));
 		}
 	}
 	
 	public static Result logout() {
 		 session().remove(util.Constants.SESSION_USERID);
 		 return  redirect("/");
 	}
 }
 
