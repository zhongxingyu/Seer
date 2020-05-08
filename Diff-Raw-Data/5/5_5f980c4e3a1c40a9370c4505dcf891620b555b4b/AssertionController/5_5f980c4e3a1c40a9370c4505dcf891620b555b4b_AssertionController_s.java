 package controllers;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 import models.BadgeAssertion;
 import models.BadgeClass;
 import models.IdentityHash;
 import models.IdentityObject;
 import models.IdentityType;
 import models.VerificationObject;
 import models.VerificationType;
 import play.mvc.*;
 import views.html.*;
 
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security;
 
 //@Security.Authenticated(Secured.class)
 public class AssertionController extends Controller {
 
 	public static Result assertions() {
 		// list assertions
 
 		List<BadgeAssertion> assertionsList = BadgeAssertion.find.all();
 
 		return ok(assertions.render(assertionsList));
 
 	}
 
 	public static BadgeAssertion createBadgeAssertionAPI(String identity,
 			String badgeId, String evidence) {
 
 		// check badge existance
 		Long badgeIdLong = Long.parseLong(badgeId);
 		BadgeClass bc = BadgeClass.find.byId(badgeIdLong);
 		if (bc == null) {
 			// DEAD END
 		}
 
 		// check valid URLs
 		URL badgeURL = null;
 		try {
 			badgeURL = new URL(routes.BadgeController.getJson(badgeIdLong)
 					.absoluteURL(request()));
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		URL exidenceURL = null;
 		try {
 			exidenceURL = new URL(evidence);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 
 		IdentityHash ih = new IdentityHash(identity);
 
 		boolean hashed = true;
 		IdentityObject io = new IdentityObject(ih, IdentityType.email, hashed,
 				ih.getSalt());
 		io.save();
 
 		VerificationType vt = VerificationType.hosted;
 
 		URL fakeURL = null;
 		try {
			fakeURL = new URL("http://pacific-brushlands-7687.herokuapp.com/assertion/1.json");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 
 		VerificationObject vo = new VerificationObject(vt, fakeURL); // TRICY!!
 		vo.save();
 		BadgeAssertion ba = new BadgeAssertion(io.id, badgeURL, vo.id,
 				exidenceURL);
 		ba.save();
 
 		// get REAL vo url
 		URL thisURL = null;
 		try {
 			thisURL = new URL(routes.AssertionController.getAssertion(ba.uid)
 					.absoluteURL(request()));
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		vo.url = thisURL; // replace after creation
 		return ba;
 //		return ok(Json.toJson(ba));
 	}
 
 	@BodyParser.Of(play.mvc.BodyParser.Json.class)
 	public static Result getAssertion(Long id) {
 		BadgeAssertion ba = BadgeAssertion.find.byId(id);
 		return ok(Json.toJson(ba));
 	}
 
 }
