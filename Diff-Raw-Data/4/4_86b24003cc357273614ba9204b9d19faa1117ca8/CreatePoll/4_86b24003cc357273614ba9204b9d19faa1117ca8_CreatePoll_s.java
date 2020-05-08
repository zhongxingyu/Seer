 package controllers;
 import java.util.ArrayList;
 import models.Poll;
 import play.mvc.Controller;
import api.Response.CreatePollResponse;
import api.Request.CreatePollRequest;
 import api.entities.PollJSON;
 import com.google.gson.Gson;
 import api.entities.ChoiceJSON;
 import java.util.LinkedList;
 import api.helpers.GsonHelper;
 import java.util.List;
 
 public class CreatePoll extends Controller {
 	public static void index(String email, String question, String[] answer) {
 		String choices;
 		if (answer == null) {
 			answer = new String[] { "", "" };
 		}
 		List<ChoiceJSON> choices_json = new LinkedList<ChoiceJSON>();
 		for(String c: answer) {
 			ChoiceJSON cJSON = new ChoiceJSON();
 			cJSON.text = c;
 			choices_json.add(cJSON);
 		}
 		choices = GsonHelper.toJson(choices_json);
 		render(email, question, answer, choices);
 
 	}
 
 	public static void success(String token, String adminkey) {
 		render(token, adminkey);
 	}
 
 	public static void submit(String email, String question, String[] answer,
 			String type) {
 		// Remove empty lines from the answers
 		ArrayList<String> answers = new ArrayList<String>();
 		if (answer != null) {
 			for (String o : answer) {
 				if (o != null && !o.isEmpty()) {
 					answers.add(o);
 				}
 			}
 		}
 		answer = answers.toArray(new String[] {});
 
 		// Validate that the question and answers are there.
 		validation.required(email);
 		validation.email(email);
 		validation.required(question);
 		validation.required(type);
 		validation.required(answer).message("validation.required.atLeastTwo");
 
 		// Validate that we have at least 2 answer options.
 		if (answer.length < 2) {
 			validation.addError("answer", "validation.required.atLeastTwo",
 					answer);
 		}
 
 		// If we have an error, go to newpollform.
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			index(email, question, answer);
 			return;
 		}
 
 		// Finally, we're ready to send it to the server, and see if it likes it
 		// or not.
 
 		Poll p = new Poll();
 		p.question = question;
 		p.answers = answer;
 		p.email = email;
 		if (type.equals("multiple")) {
 			p.multipleAllowed = true;
 		}
 
 		try {
 			//CreatePollResponse response = (CreatePollResponse) APIClient.send(new CreatePollRequest(p));
 			//PollJSON poll = response.poll;
 			
 			/* ToDo: Authentication */
 			
 			//String token = poll.token;
 			// TODO: Fix this..
 			String token = null;
 			String adminkey = null;
 	
 			// Redirect to success
 			success(token, adminkey);
 		} catch (Exception e) {
 			// It failed!
 			// TODO: Tell the user!
 			e.printStackTrace();
 		}
 	}
 }
