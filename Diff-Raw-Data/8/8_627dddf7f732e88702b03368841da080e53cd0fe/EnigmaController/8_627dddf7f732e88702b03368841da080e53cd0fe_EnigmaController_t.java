 package controllers;
 
 import global.AssociatedPage;
 import global.CurrentRequest;
 import global.Sesame;
 
 import java.io.StringWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import models.Answer;
 import models.Clue;
 import models.Enigma;
 
 import org.openrdf.query.QueryEvaluationException;
 import org.openrdf.query.QueryLanguage;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.object.ObjectConnection;
 import org.openrdf.rio.RDFWriter;
 
 import pages.EnigmaCreatePage;
 import pages.EnigmaEditPage;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 public class EnigmaController extends Controller {
 
 	@AssociatedPage("enigmacreate")
 	public static Result create(String sid) throws RepositoryException, QueryEvaluationException {
 		Form<forms.Enigma> formEnigma = form(forms.Enigma.class);
 		ObjectConnection oc = Sesame.getObjectConnection();
 
 		models.Step step = oc.getObject(models.Step.class, models.Step.URI + sid);
 		
 		formEnigma.fill(new forms.Enigma());
 
 		((EnigmaCreatePage)CurrentRequest.page()).setMenuParameters(step);// Menu's parameters
 		return ok(views.html.dashboard.createEnigma.render(step, formEnigma));
 	}
 
 	@AssociatedPage("enigmacreate")
 	public static Result submitCreateForm(String sid) throws RepositoryException, QueryEvaluationException {
 		Form<forms.Enigma> formEnigma = form(forms.Enigma.class).bindFromRequest();
 		ObjectConnection oc = Sesame.getObjectConnection();
 		models.Step step = oc.getObject(models.Step.class, models.Step.URI + sid);
 
 		if (formEnigma.hasErrors()) {
 			((EnigmaCreatePage)CurrentRequest.page()).setMenuParameters(step);// Menu's parameters
 			return badRequest(views.html.dashboard.createEnigma.render(step, formEnigma));
 		} else {
 			Enigma enigma = formToEnigma(formEnigma.get());
 			enigma.setStep(step);
 			enigma.setNumber(step.getEnigmas().size() + 1);
 			
 			String eid = UUID.randomUUID().toString();
 			oc.addObject(Enigma.URI + eid, enigma);
 			
 			enigma = oc.getObject(Enigma.class, Enigma.URI + eid);
 			
 			for (models.Clue clue: formToClues(formEnigma.get())) {
 				clue.setEnigma(enigma);
 				String cid = UUID.randomUUID().toString();
 				oc.addObject(models.Clue.URI + cid, clue);
 			}
 			
 			models.Answer answer = formToAnswer(formEnigma.get());
 			answer.setEnigma(enigma);
 			String aid = UUID.randomUUID().toString();
 			oc.addObject(models.Answer.URI + aid, answer);
 			
 			return redirect(routes.HuntController.show(step.getHunt().getId()));
 		}
 	}
 
 	public static Result delete(String eid) {
 		return ok();
 	}
 
 	@AssociatedPage("enigmaedit")
 	public static Result update(String eid) throws RepositoryException,	QueryEvaluationException {
 		ObjectConnection oc = Sesame.getObjectConnection();
 
 		Enigma enigma = oc.getObject(Enigma.class, Enigma.URI + eid);
 		
 		forms.Enigma formEnigma = new forms.Enigma();
 		formEnigma.description = enigma.getDescription();
 		formEnigma.answer = new forms.Enigma.Answer();
 		if (enigma.getAnswer() instanceof models.GeolocatedAnswer) {
 			models.GeolocatedAnswer a = (models.GeolocatedAnswer)enigma.getAnswer();
 			formEnigma.answer.type = forms.Enigma.Answer.GeolocatedAnswer;
 			formEnigma.answer.accuracy = a.getPosition().getAccuracy();
 			formEnigma.answer.labelPosition = a.getPosition().getPlace();
 			formEnigma.answer.position = a.getPosition().toTemplateString();
 		} else if (enigma.getAnswer() instanceof models.TextAnswer) {
 			models.TextAnswer a = (models.TextAnswer)enigma.getAnswer();
 			formEnigma.answer.type = forms.Enigma.Answer.TextAnswer;
 			formEnigma.answer.possibleTexts = new ArrayList<String>(a.getLabels());
 		} else {
 			throw new RuntimeException("Erreur dans la réponse");
 		}
 				
 		formEnigma.clues = new ArrayList<forms.Enigma.Clue>();
 		for (models.Clue clue: enigma.getClues()) {
 			forms.Enigma.Clue formClue = new forms.Enigma.Clue();
 			if (clue instanceof models.FileClue) {
 				models.FileClue c = (models.FileClue)clue;
 				formClue.type = forms.Enigma.Clue.FileClue;
 				formClue.fileDescription = c.getDescription();
 				formClue.fileLink = c.getFile().toString();
 			} else if (clue instanceof models.TextClue) {
 				models.TextClue c = (models.TextClue)clue;
 				formClue.type = forms.Enigma.Clue.TextClue;
 				formClue.textDescription = c.getDescription();
 			} else {
 				throw new RuntimeException("Erreur dans l'indice");
 			}
 			formEnigma.clues.add(formClue);
 		}
 		
 		((EnigmaEditPage)CurrentRequest.page()).setMenuParameters(enigma);// Menu's parameters
 		return ok(views.html.dashboard.updateEnigma.render(enigma, form(forms.Enigma.class).fill(formEnigma)));
 	}
 
 	@AssociatedPage("enigmaedit")
 	public static Result submitUpdateForm(String eid) throws RepositoryException, QueryEvaluationException {
 		Form<forms.Enigma> formEnigma = form(forms.Enigma.class).bindFromRequest();
 		ObjectConnection oc = Sesame.getObjectConnection();
 		Enigma enigma = oc.getObject(Enigma.class, Enigma.URI + eid);
 
 		if (formEnigma.hasErrors()) {
 			((EnigmaEditPage)CurrentRequest.page()).setMenuParameters(enigma);// Menu's parameters
 			return badRequest(views.html.dashboard.updateEnigma.render(enigma, formEnigma));
 		} else {
 			fillEnigma(enigma, formEnigma.get());
 			oc.addObject(Enigma.URI + eid, enigma);
 			enigma = oc.getObject(Enigma.class, Enigma.URI + eid);
 			
 			// Suppression des anciens indices
 			for (models.Clue oldClue: enigma.getClues()) {
 				oldClue.reset();
 				oc.removeDesignation(oldClue, models.Clue.class);
 			}
 			
 			for (models.Clue clue: formToClues(formEnigma.get())) {
 				clue.setEnigma(enigma);
 				String cid = UUID.randomUUID().toString();
 				oc.addObject(models.Clue.URI + cid, clue);
 			}
 			
 			// Suppression de l'ancienne réponse
 			models.Answer oldAnswer = enigma.getAnswer();
 			oldAnswer.reset();
 			oc.removeDesignation(oldAnswer, models.Answer.class);
 			
 			models.Answer answer = formToAnswer(formEnigma.get());
 			answer.setEnigma(enigma);
 			String aid = UUID.randomUUID().toString();
 			oc.addObject(models.Answer.URI + aid, answer);
 			
 			return redirect(routes.HuntController.show(enigma.getStep().getHunt().getId()));
 		}
 	}
 
 	private static Answer formToAnswer(forms.Enigma form) {
 		Answer answer = null;
 		if (form.answer.type == forms.Enigma.Answer.TextAnswer) {
 			models.TextAnswer a = new models.TextAnswer();
 			Set<String> labels = new HashSet<String>();
 			for (String label: form.answer.possibleTexts) {
 				if (label.length() > 0) {
 					labels.add(label);					
 				}
 			}
 			a.setLabels(labels);
 			answer = a;
 		} else {
 			models.GeolocatedAnswer a = new models.GeolocatedAnswer();
 			a.setPosition(models.Position.createFrom(form.answer.position, form.answer.accuracy, form.answer.labelPosition));
 			answer = a;
 		}
 		
 		return answer;
 	}
 
 	private static List<Clue> formToClues(forms.Enigma form) {
 		List<Clue> clues = new ArrayList<Clue>();
 		if (form.clues == null) {
 			return clues;
 		}
 		
 		int index = 1;
 		for (forms.Enigma.Clue c: form.clues) {
 			if (c.type == forms.Enigma.Clue.TextClue) {
 				models.TextClue clue = new models.TextClue();
 				clue.setDescription(c.textDescription);
 				clue.setNumber(index);
 				clues.add(clue);
 			} else {
 				models.FileClue clue = new models.FileClue();
 				try {
 					clue.setFile(new URI(c.fileLink));
 					clue.setDescription(c.fileDescription);
 					clue.setNumber(index);
 					clues.add(clue);
 				} catch (URISyntaxException e) {
 					// Ne rien faire...
 				}
 			}
 			index++;
 		}
 		
 		return clues;
 	}
 
 	private static Enigma formToEnigma(forms.Enigma form) {
 		Enigma e = new Enigma();
 		fillEnigma(e, form);
 
 		return e;
 	}
 	
 	private static void fillEnigma(Enigma enigma, forms.Enigma form) {
 		enigma.setDescription(form.description);
 	}
 
 	// ???
 	public static Result edit(String eid) {
 		return ok();
 	}
 
 	public static Result showRDF(String eid, String format) {
 		ObjectConnection oc = Sesame.getObjectConnection();
 		StringWriter strw = new StringWriter();
 		try {
 			RDFWriter writer = Sesame.getWriter(strw, format);
 			String queryString = "DESCRIBE <" + Enigma.URI + eid + ">";
 			oc.prepareGraphQuery(QueryLanguage.SPARQL, queryString).evaluate(writer);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return notFound();
 		}
 		return ok(strw.toString());
 	}
 	
 	public static Result showClueRDF(String cid, String format) {
 		ObjectConnection oc = Sesame.getObjectConnection();
 		StringWriter strw = new StringWriter();
 		try {
 			RDFWriter writer = Sesame.getWriter(strw, format);
 			String queryString = "DESCRIBE <" + models.Clue.URI + cid + ">";
 			oc.prepareGraphQuery(QueryLanguage.SPARQL, queryString).evaluate(writer);
 		} catch (Exception e) {
 			System.out.println("Exception : " + e);
 			return notFound();
 		}
 		return ok(strw.toString());
 	}
 		
 	public static Result showAnswerRDF(String aid, String format) {
 		ObjectConnection oc = Sesame.getObjectConnection();
 		StringWriter strw = new StringWriter();
 		try {
 			RDFWriter writer = Sesame.getWriter(strw, format);
 			String queryString = "DESCRIBE <" + models.Answer.URI + aid + ">";
 			oc.prepareGraphQuery(QueryLanguage.SPARQL, queryString).evaluate(writer);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return notFound();
 		}
 		return ok(strw.toString());
 	}
 }
