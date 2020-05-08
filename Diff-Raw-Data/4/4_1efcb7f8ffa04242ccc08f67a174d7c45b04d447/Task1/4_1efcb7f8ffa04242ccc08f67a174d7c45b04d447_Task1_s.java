 package tasks;
 
 import java.util.Date;
 import java.util.Locale;
 
 import models.PatientInfo;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 
 
 public class Task1 extends Task{
 	//final Logger log = Logger.getLogger(Task1.class);
 	
 	public Task1(String name) {
 		super(name);
 		Form form = new Form("form") {
 	        protected void onSubmit() {}
 	    };
 	    add(form);
	    PatientInfo person = new PatientInfo("Alvin", "111", "No varnings", new Date());
 	    
 	    form.add(new RequiredTextField("personName", new PropertyModel(person, "name")));
 	    
 		form.add( new Button("button1", new Model<String>("Svenska")){
 			 static final long serialVersionUID = 1L;
 				@Override
 				public void onSubmit() {					
 		        	getSession().setLocale(new Locale("sv", "SE"));	
 		        	info("Locale is now se");
 		        	//log.info("Locale is: " +getSession().getLocale().toString());
 		        }
 		 });
 	}
 	
 	
 
 }
