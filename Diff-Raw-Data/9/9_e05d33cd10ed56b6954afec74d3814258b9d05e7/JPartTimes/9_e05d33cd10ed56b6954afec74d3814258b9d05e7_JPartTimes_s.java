 package controllers;
 
 import http.ResponseCache;
 import com.google.common.collect.Lists;
 import dto.PartTimeDTO;
 import models.JCra;
 import models.JPartTime;
 import org.apache.commons.collections.CollectionUtils;
 import org.bson.types.ObjectId;
 import org.joda.time.DateTime;
 import play.data.Form;
 import play.data.validation.ValidationError;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 import utils.time.TimeUtils;
 
 import java.util.List;
 
 import static play.libs.Json.toJson;
 
 /**
  * @author f.patin
  */
 public class JPartTimes extends Controller {
 
 	@BodyParser.Of(BodyParser.Json.class)
 	@ResponseCache.NoCacheResponse
 	public static Result addPartTimes() {
 		final Form<CreateForm> form = Form.form(CreateForm.class).bind(request().body().asJson());
 		if(form.hasErrors()) {
 			return badRequest(form.errorsAsJson());
 		}
 		final CreateForm createForm = form.get();
 		final List<JPartTime> pts = createForm.to();
 		JPartTime.deactivateAll(pts.get(0).userId);
 		for(JPartTime pt : pts) {
 			JCra.unapplyPartTime(pt);
 		}
 		return created(toJson(PartTimeDTO.of(JPartTime.addPartTimes(pts))));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result active(final String userId) {
 		return ok(toJson(PartTimeDTO.of(JPartTime.activeByUser(userId))));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result history(final String userId) {
 		return ok(toJson(PartTimeDTO.of(JPartTime.history(userId))));
 	}
 
 	@BodyParser.Of(BodyParser.Json.class)
 	public static Result deactivate() {
 		final Form<String> form = Form.form(String.class).bind(request().body().asJson());
 		if(form.hasErrors()) {
 			return badRequest(form.errorsAsJson());
 		}
 		final String id = form.data().get("id");
 		final JPartTime oldPartTime = JPartTime.deactivate(id);
 		if(Boolean.FALSE.equals(JPartTime.existActive(oldPartTime.userId))) {
 			JCra.unapplyPartTime(oldPartTime);
 		}
 		return ok();
 	}
 
 	public static class PartTimeWeekDay {
 		public Integer dayOfWeek;
 		public String momentOfDay;
 	}
 
 	public static class CreateForm {
 
 		public ObjectId userId;
 		public Long startDate;
 		public Long endDate;
 		public List<PartTimeWeekDay> daysOfWeek = Lists.newArrayList();
 		public Integer frequency;
 
 		public List<ValidationError> validate() {
 			final List<ValidationError> errors = Lists.newArrayList();
 			if(startDate == null) {
 				errors.add(new ValidationError("startDate", "Le date de début est requise."));
 			} else {
 				if(new DateTime(startDate).isBefore(TimeUtils.firstDateOfMonth(DateTime.now()))) {
 					errors.add(new ValidationError("startDate", "Le temps partiel ne peut pas commencer dans le passé."));
 				}
 				if(endDate != null && new DateTime(endDate).isBefore(new DateTime(startDate))) {
					errors.add(new ValidationError("endDate", "La date de fin doit être postérieur à la date de début."));
 				}
 			}
 			if(frequency == null) {
 				errors.add(new ValidationError("frequency", "La fréquence est requise."));
 			}
 			if(CollectionUtils.isEmpty(daysOfWeek)) {
 				errors.add(new ValidationError("daysOfWeek", "Vous devez choisir au moins un jour de la semaine."));
 			}
 			return errors.isEmpty() ? null : errors;
 		}
 
 		public List<JPartTime> to() {
 			final List<JPartTime> pts = Lists.newArrayListWithCapacity(daysOfWeek.size());
 			for(PartTimeWeekDay wd : daysOfWeek) {
 				final JPartTime pt = new JPartTime(userId, TimeUtils.toNextDayOfWeek(new DateTime(startDate), wd.dayOfWeek), frequency);
 				if(endDate != null) {
 					pt.endDate = TimeUtils.toPreviousDayOfWeek(new DateTime(endDate), wd.dayOfWeek);
 				}
 				pt.dayOfWeek = wd.dayOfWeek;
 				pt.momentOfDay = wd.momentOfDay;
 				pts.add(pt);
 			}
 			return pts;
 		}
 	}
 }
