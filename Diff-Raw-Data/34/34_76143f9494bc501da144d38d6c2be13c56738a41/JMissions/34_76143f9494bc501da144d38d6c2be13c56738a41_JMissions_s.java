 package controllers;
 
 import com.google.common.collect.Lists;
 import constants.MissionType;
 import dto.CustomerDTO;
 import dto.MissionDTO;
 import dto.MissionFullDTO;
 import http.ResponseCache;
 import models.JCustomer;
 import models.JMission;
 import models.JUser;
 import org.apache.commons.lang3.StringUtils;
 import org.bson.types.ObjectId;
 import org.joda.time.DateTime;
 import play.data.Form;
 import play.data.validation.ValidationError;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import java.util.List;
 import java.util.Locale;
 
 import static play.libs.Json.toJson;
 
 /**
  * @author f.patin
  */
 public class JMissions extends Controller {
 
 	@ResponseCache.NoCacheResponse
 	public static Result absences() {
 		final List<JMission> missions = JMission.getAbsencesMissions();
 		return ok(toJson(MissionDTO.of(missions)));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result claimable(final String username) {
 		final List<JMission> missions = JMission.getClaimableMissions(JUser.affectedMissions(username));
 		return ok(toJson(MissionDTO.of(missions)));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result craMissions(final String username, final Long startDate, final Long endDate) {
 		final List<JMission> missions = JMission.craMissions(JUser.affectedMissions(username, new DateTime(startDate), new DateTime(endDate)));
 		return ok(toJson(MissionDTO.of(missions)));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result customerMissions(final String customerId) {
 		return ok(toJson(MissionDTO.of(JMission.customerMission(ObjectId.massageToObjectId(customerId)))));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result customerByMissionId(final String id) {
 		return ok(toJson(CustomerDTO.of(JCustomer.fetch(JMission.fetch(ObjectId.massageToObjectId(id)).customerId))));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result fetch(final String id) {
 		return ok(toJson(MissionFullDTO.of(JMission.fetch(ObjectId.massageToObjectId(id)))));
 	}
 
 	@BodyParser.Of(BodyParser.Json.class)
 	@ResponseCache.NoCacheResponse
 	public static Result save() {
 		final Form<CustomerMissionForm> form = Form.form(CustomerMissionForm.class).bind(request().body().asJson());
 		if(form.hasErrors()) {
 			return badRequest(form.errorsAsJson());
 		}
 		final CustomerMissionForm missionForm = form.get();
 		return created(toJson(MissionFullDTO.of(JMission.save(missionForm.to()))));
 	}
 
 	public static class CustomerMissionForm {
 		public ObjectId customerId;
 		public ObjectId id;
 		public String code;
 		public String label;
 		public String description;
 
 		public List<ValidationError> validate() {
 			final List<ValidationError> errors = Lists.newArrayList();
 			if(customerId == null) {
 				errors.add(new ValidationError("customer", "Le client est obligatoire."));
 			}
 			if(StringUtils.isBlank(this.code)) {
 				errors.add(new ValidationError("code", "Le code est obligatoire."));
 			}
 			if(StringUtils.isBlank(this.label)) {
 				errors.add(new ValidationError("label", "Le libellé est obligatoire."));
 			}
 			return errors.isEmpty() ? null : errors;
 		}
 
 		public JMission to() {
 			final JMission mission = new JMission();
 			mission.id = this.id;
 			mission.customerId = this.customerId;
 			mission.code = this.code.toUpperCase(Locale.FRANCE);
 			mission.label = this.label;
 			mission.description = this.description;
 			mission.missionType = MissionType.customer.name();
 			return mission;
 		}
 	}
 
 
 }
