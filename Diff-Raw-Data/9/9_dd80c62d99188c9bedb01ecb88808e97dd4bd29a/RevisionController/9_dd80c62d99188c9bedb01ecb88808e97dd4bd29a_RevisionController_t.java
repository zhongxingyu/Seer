 package controllers;
 
 import java.text.ParseException;
 import java.util.Date;
 import java.util.List;
 
 import models.Act;
 import models.Author;
 import models.Competence;
 import models.Revision;
 import models.Term;
 import play.mvc.Controller;
 import play.mvc.With;
 import tools.Utility;
 
 @With(Secure.class)
 public class RevisionController extends Controller {
 
 	/**
 	 * Create a new revision of an existing act.
 	 * 
 	 * @param actId
 	 * @param termIds
 	 * @throws ParseException
 	 */
 	public static void addRevision(long actId, List<Long> termIds,
 			String orDate, String comDate, boolean isLast, String expDate)
 			throws ParseException {
 		Act act = Act.findById(actId);
 		int revisionCount = act.getRevisionCount() + 1;
 
 		Author author = Author.find("byName", session.get("username")).first();
 
 		Date orderDate = Utility.stringToDate(orDate);
 		Date commencementDate = Utility.stringToDate(comDate);
 		Revision revision = new Revision(act, revisionCount, orderDate,
 				commencementDate, author);
 		if (isLast) {
 			Date expiringDate = Utility.stringToDate(expDate);
 			revision.expiringDate = expiringDate;
 			revision.act.save();
 		}
 		revision.save();
 
 		for (Long id : termIds) {
 			Term oldTerm = Term.findById(id);
 			Term newTerm = new Term(oldTerm.name, oldTerm.source, author,
 					new Date(), revision);
 			newTerm.replaces = oldTerm;
 			newTerm.save();
 
 			oldTerm.replacedBy = newTerm;
 			oldTerm.save();
 
 			// copy competences
 			for (Competence c : oldTerm.competences) {
 				Competence copy = new Competence(c.actor, c.action,
 						c.passiveActor, c.term, author);
 				copy.save();
 			}
 		}
 
 		flash.success("Revision %s von %s erstellt.", revisionCount, act.name);
 		Application.listRevisions(act.id);
 	}
 
 	public static void formAddRevision(long actId) {
 		Act act = Act.findById(actId);
 		List<Term> terms = act.getTerms();
 		render(act, terms);
 	}
 
 	public static void formEditRevision(long revId) {
 		Revision rev = Revision.findById(revId);
 		render(rev);
 	}
 
 	public static void editRevision(long revId, String orDate, String comDate,
 			boolean isLast, String expDate) {
 		Revision revision = Revision.findById(revId);
 		revision.orderDate = Utility.stringToDate(orDate);
 		revision.commencementDate = Utility.stringToDate(comDate);
 		if (isLast)
 			revision.expiringDate = Utility.stringToDate(expDate);
		else if (revision.isLastRevision)
 			revision.expiringDate = null;
 		revision.save();
 
 		Act act = revision.act;
 		flash.success("Revision %s von %s editiert.", revision.revisionCount,
 				act.name);
 		Application.listRevisions(act.id);
 	}
 }
