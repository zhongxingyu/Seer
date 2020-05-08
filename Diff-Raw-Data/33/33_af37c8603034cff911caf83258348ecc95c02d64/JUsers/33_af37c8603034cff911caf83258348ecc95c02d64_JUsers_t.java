 package controllers;
 
 import com.google.common.collect.Lists;
 import constants.MissionAllowanceType;
 import dto.AffectedMissionDTO;
 import dto.AffectedMissionFullDTO;
 import dto.EmployeeDTO;
 import http.ResponseCache;
 import models.JAffectedMission;
 import models.JUser;
 import org.apache.commons.lang3.StringUtils;
 import org.bson.types.ObjectId;
 import org.joda.time.DateTime;
 import play.data.Form;
 import play.data.validation.ValidationError;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 import static play.libs.Json.toJson;
 
 /**
  * @author f.patin
  */
 public class JUsers extends Controller {
 
 	@ResponseCache.NoCacheResponse
 	public static Result employees() {
 		return ok(toJson(EmployeeDTO.of(JUser.employees())));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result managers() {
 		return ok(toJson(EmployeeDTO.of(JUser.managers())));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result users() {
 		List<JUser> users = Lists.newArrayList(JUser.employees());
 		users.addAll(JUser.managers());
 		return ok(toJson(EmployeeDTO.of(users)));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result all() {
 		return ok(toJson(EmployeeDTO.of(JUser.all())));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result fetch(final String username) {
 		return ok(toJson(EmployeeDTO.of(JUser.fetch(username))));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result affectedMissions(final String username, final Long startDate, final Long endDate) {
 		final List<JAffectedMission> affectedMissions = JUser.affectedMissions(JUser.id(username), new DateTime(startDate), new DateTime(endDate));
 		return ok(toJson(AffectedMissionDTO.of(affectedMissions)));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result allAffectedMissions(final String username) {
 		final List<JAffectedMission> affectedMissions = JUser.affectedMissions(JUser.id(username));
 		return ok(toJson(AffectedMissionFullDTO.of(affectedMissions)));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result customerAffectedMissions(final String username) {
 		final List<JAffectedMission> affectedMissions = JUser.customerAffectedMissions(JUser.id(username));
 		return ok(toJson(AffectedMissionDTO.of(affectedMissions)));
 	}
 
 	@BodyParser.Of(BodyParser.Json.class)
 	@ResponseCache.NoCacheResponse
 	public static Result save() {
 		final Form<UserForm> form = Form.form(UserForm.class).bind(request().body().asJson());
 		if(form.hasErrors()) {
 			return badRequest(form.errorsAsJson());
 		}
 		final UserForm userForm = form.get();
 		return created(toJson(EmployeeDTO.of(JUser.save(userForm.to()))));
 	}
    @BodyParser.Of(BodyParser.Json.class)
    @ResponseCache.NoCacheResponse
    public static Result resetPwd(final String username) {
        JUser.resetPwd(username);
        return ok();
    }
 

    @BodyParser.Of(BodyParser.Json.class)
 	@ResponseCache.NoCacheResponse
 	public static Result saveAffectedMission() {
 		final Form<AffectedMissionForm> form = Form.form(AffectedMissionForm.class).bind(request().body().asJson());
 		if(form.hasErrors()) {
 			return badRequest(form.errorsAsJson());
 		}
 		final AffectedMissionForm affectedMissionForm = form.get();
 		final ObjectId userId = affectedMissionForm.userId;
 		final JAffectedMission am = affectedMissionForm.to();
 		final List<JAffectedMission> affectedMissions = JUser.affectedMissions(affectedMissionForm.userId);
 		JUser.updateAffectedMissions(userId, am);
 		return created("Mission sauvegardée");
 	}
 
 	public static class UserForm {
 		public ObjectId id;
 		public String username;
 		public String trigramme;
 		public String firstName;
 		public String lastName;
 		public String email;
 
 		public List<ValidationError> validate() {
 			final List<ValidationError> errors = Lists.newArrayList();
 			if(StringUtils.isBlank(username)) {
 				errors.add(new ValidationError("username", "Le username est obligatoire."));
 			}
 			if(StringUtils.isBlank(trigramme)) {
 				errors.add(new ValidationError("username", "Le trigramme est obligatoire."));
 			}
 			if(StringUtils.isBlank(firstName)) {
 				errors.add(new ValidationError("username", "Le prénom est obligatoire."));
 			}
 			if(StringUtils.isBlank(lastName)) {
 				errors.add(new ValidationError("username", "Le nom est obligatoire."));
 			}
 			if(StringUtils.isBlank(email)) {
 				errors.add(new ValidationError("username", "L'email' est obligatoire."));
 			}
 			return errors.isEmpty() ? null : errors;
 		}
 
 		public JUser to() {
 			final JUser user = new JUser();
 			user.id = this.id;
 			user.username = this.username;
 			user.trigramme = this.trigramme;
 			user.firstName = this.firstName;
 			user.lastName = this.lastName;
 			user.email = this.email;
 			return user;
 		}
 	}
 
 	public static class AffectedMissionForm {
 		public ObjectId userId;
 		public ObjectId missionId;
 		public Long startDate;
 		public Long endDate;
 		public Boolean feeZone;
 		public String feeAmount;
 
 		public List<ValidationError> validate() {
 			final List<ValidationError> errors = Lists.newArrayList();
 			if(missionId == null) {
 				errors.add(new ValidationError("mission", "La mission est obligatoire."));
 			}
 			if(startDate == null) {
 				errors.add(new ValidationError("startDate", "La date de début est obligatoire."));
 			}
 			if(StringUtils.isBlank(this.feeAmount) && !Boolean.TRUE.equals(this.feeZone)){
 				errors.add(new ValidationError("fee", "Vous devez sélectionnez un type de frais."));
 			}
 			return errors.isEmpty() ? null : errors;
 		}
 
 		public JAffectedMission to() {
 			final JAffectedMission affectedMission = new JAffectedMission();
 			affectedMission.missionId = this.missionId;
 			affectedMission.startDate = new DateTime(this.startDate);
 			if(this.endDate != null) {
 				affectedMission.endDate = new DateTime(this.endDate);
 			}
 			if(StringUtils.isNotBlank(this.feeAmount)) {
 				affectedMission.feeAmount = new BigDecimal(this.feeAmount.replace(',', '.'));
 				affectedMission.allowanceType = MissionAllowanceType.FIXED.name();
 			} else if(feeZone != null) {
 				affectedMission.allowanceType = MissionAllowanceType.ZONE.name();
 			} else {
 				affectedMission.allowanceType = MissionAllowanceType.NONE.name();
 			}
 			return affectedMission;
 		}
 	}
 
 }
