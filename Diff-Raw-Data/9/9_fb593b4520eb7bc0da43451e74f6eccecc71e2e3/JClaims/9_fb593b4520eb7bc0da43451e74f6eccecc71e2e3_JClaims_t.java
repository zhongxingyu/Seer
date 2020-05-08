 package controllers;
 
 import caches.ResponseCache;
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import dto.ClaimDTO;
 import models.JClaim;
 import models.JMission;
 import models.JUser;
 import org.bson.types.ObjectId;
 import org.joda.time.DateTime;
 import play.data.Form;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import javax.annotation.Nullable;
 import java.math.BigDecimal;
 import java.util.List;
 
 import static play.libs.Json.toJson;
 
 /**
  * @author f.patin
  */
 public class JClaims extends Controller {
 
 	@ResponseCache.NoCacheResponse
 	public static Result history(final String username, final Integer year, final Integer month) {
 		final ObjectId userId = JUser.id(username);
 		final ImmutableList<JClaim> claims = JClaim.forUser(userId, year, month);
 		final ImmutableList<ObjectId> missionIds = ImmutableList.copyOf(Collections2.transform(claims, new Function<JClaim, ObjectId>() {
 			@Nullable
 			@Override
 			public ObjectId apply(@Nullable final JClaim claim) {
 				return claim.missionId;
 			}
 		}));
 		final ImmutableMap<ObjectId, JMission> missions = JMission.codeAndMissionType(missionIds);
 		return ok(toJson(ClaimDTO.of(claims, missions.values().asList())));
 	}
 
 	@BodyParser.Of(BodyParser.Json.class)
 	public static Result create() {
 		final Form<CreateClaimForm> form = Form.form(CreateClaimForm.class).bind(request().body().asJson());
 		if (form.hasErrors()) {
 			return badRequest(form.errorsAsJson());
 		}
 
 		final CreateClaimForm createClaimForm = form.get();
 		final JClaim claim = createClaimForm.to();
 		return ok(toJson(ClaimDTO.of(JClaim.create(claim))));
 
 	}
 
	public static Result remove(final String id){
 		return ok(toJson(JClaim.delete(id)));
 	}
 	public static class CreateClaimForm {
 
 		public String username;
 		public String missionId;
 		public Long date;
 		public String claimType;
 		public String amount;
 		public String kilometer;
 		public String journey;
 		public String comment;
 
 		public JClaim to() {
 			final JClaim claim = new JClaim();
 			claim.userId = JUser.id(this.username);
 			claim.missionId = ObjectId.massageToObjectId(this.missionId);
 			claim.date = new DateTime(this.date);
 			claim.claimType = this.claimType;
 			if (this.amount != null) {
 				claim.amount = new BigDecimal(this.amount);
 			}
 			if (this.kilometer != null) {
 				claim.kilometer = new BigDecimal(this.kilometer);
 			}
 			claim.journey = this.journey;
 			claim.comment = this.comment;
 			return claim;
 		}
 	}
 
 }
