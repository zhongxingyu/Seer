 package controllers;
 
 import caches.ResponseCache;
 import constants.AbsenceType;
 import dto.AbsenceDTO;
 import exceptions.AbsenceAlreadyExistException;
 import models.JAbsence;
 import models.JUser;
 import org.bson.types.ObjectId;
 import org.joda.time.DateTime;
 import play.data.Form;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 
 import java.util.List;
 
 import static org.joda.time.DateTimeConstants.DECEMBER;
 import static org.joda.time.DateTimeConstants.JANUARY;
 import static org.joda.time.DateTimeConstants.JUNE;
 import static org.joda.time.DateTimeConstants.MAY;
 import static play.libs.Json.toJson;
 
 /**
  * @author f.patin
  */
 public class JAbsences extends Controller {
 
 	@BodyParser.Of(BodyParser.Json.class)
 	public static Result create() {
 		final Form<CreateAbsenceForm> form = Form.form(CreateAbsenceForm.class).bind(request().body().asJson());
 		if (form.hasErrors()) {
 			return badRequest(form.errorsAsJson());
 		}
 		final CreateAbsenceForm createAbsenceForm = form.get();
 		final JAbsence absence = createAbsenceForm.to();
 		try {
 			return created(toJson(AbsenceDTO.of(JAbsence.create(absence))));
 		} catch (AbsenceAlreadyExistException e) {
			return internalServerError(toJson(e.getMessage()));
 		}
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result history(final String username) {
 		return historyByYear(username, DateTime.now().getYear());
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result historyByYear(final String username, final Integer year) {
 		final ObjectId userId = JUser.id(username);
 		final List<JAbsence> absences = JAbsence.fetch(userId, year, JANUARY, year, DECEMBER);
 		return ok(toJson(AbsenceDTO.of(absences)));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result historyCP(final String username, final Integer year) {
 		final ObjectId userId = JUser.id(username);
 		final List<JAbsence> absences = JAbsence.fetch(userId, year, JUNE, year + 1, MAY, AbsenceType.CP);
 
 		return ok(toJson(AbsenceDTO.of(absences)));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result historyRTT(final String username, final Integer year) {
 		final ObjectId userId = JUser.id(username);
 		final List<JAbsence> absences = JAbsence.fetch(userId, year, JANUARY, year, DECEMBER, AbsenceType.RTT);
 		return ok(toJson(AbsenceDTO.of(absences)));
 	}
 
 	public static class CreateAbsenceForm {
 
 		public String username;
 		public String missionId;
 		public Long startDate;
 		public Boolean startMorning;
 		public Boolean startAfternoon;
 		public Long endDate;
 		public Boolean endMorning;
 		public Boolean endAfternoon;
 		public String comment;
 
 		public JAbsence to() {
 			final JAbsence holiday = new JAbsence();
 			holiday.userId = JUser.id(this.username);
 			holiday.startDate = new DateTime(this.startDate);
 			holiday.startMorning = this.startMorning;
 			holiday.startAfternoon = this.startAfternoon;
 			holiday.endDate = new DateTime(this.endDate);
 			holiday.endMorning = this.endMorning;
 			holiday.endAfternoon = this.endAfternoon;
 			holiday.missionId = ObjectId.massageToObjectId(this.missionId);
 			holiday.comment = this.comment;
 			return holiday;
 		}
 	}
 }
