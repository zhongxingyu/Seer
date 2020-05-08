 package controllers;
 
 import http.ResponseCache;
 import com.google.common.collect.Lists;
 import dto.VehicleDTO;
 import models.JVehicle;
 import org.bson.types.ObjectId;
 import org.joda.time.DateTime;
 import play.data.Form;
 import play.data.validation.Constraints;
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
 public class JVehicles extends Controller {
 
 	@BodyParser.Of(BodyParser.Json.class)
 	@ResponseCache.NoCacheResponse
 	public static Result save() {
 		final Form<CreateVehicleForm> form = Form.form(CreateVehicleForm.class).bind(request().body().asJson());
 		if(form.hasErrors()) {
 			return badRequest(form.errorsAsJson());
 		}
 		final CreateVehicleForm createVehicleForm = form.get();
 		return created(toJson(VehicleDTO.of(JVehicle.save(createVehicleForm.to()))));
 
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result active(final String userId) {
 		final JVehicle vehicle = JVehicle.active(userId);
 		if(vehicle == null) {
 			return ok();
 		}
 		return ok(toJson(VehicleDTO.of(JVehicle.active(userId))));
 	}
 
 	@ResponseCache.NoCacheResponse
 	public static Result history(final String userId) {
 		return ok(toJson(VehicleDTO.of(JVehicle.history(userId))));
 	}
 
 	public static Result deactivate() {
 		final Form<String> form = Form.form(String.class).bind(request().body().asJson());
 		if(form.hasErrors()) {
 			return badRequest(form.errorsAsJson());
 		}
 		final String id = form.data().get("id");
 		JVehicle.deactivate(id);
 		return ok();
 	}
 
 	public static class CreateVehicleForm {
 
 		public ObjectId userId;
 		@Constraints.Required(message = "Le type de véhicule est requis.")
 		public String vehicleType;
 		@Constraints.Required(message = "La marque est requise.")
 		public String brand;
 		@Constraints.Required(message = "La puissance fiscale/cylindrée est requise.")
 		public Integer power;
 		@Constraints.Required(message = "L'immatriculation est requise.")
 		public String matriculation;
 		@Constraints.Required(message = "Le mois de validité est requis.")
 		public Integer month;
 		@Constraints.Required(message = "L'année de validité est requise.")
 		public Integer year;
 
 		public List<ValidationError> validate() {
 			final List<ValidationError> errors = Lists.newArrayList();
 			if(TimeUtils.firstDateOfMonth(year, month).isBefore(TimeUtils.firstDateOfMonth(DateTime.now()))) {
				errors.add(new ValidationError("validityDate", "La date de validité doit être supérieure ou égale au mois en cours."));
 			}
 			return errors.isEmpty() ? null : errors;
 		}
 
 		public JVehicle to() {
 			JVehicle vehicle = new JVehicle();
 			vehicle.userId = this.userId;
 			vehicle.vehicleType = this.vehicleType;
 			vehicle.brand = this.brand;
 			vehicle.power = this.power;
 			vehicle.matriculation = this.matriculation;
 			vehicle.startDate = TimeUtils.firstDateOfMonth(year, month);
 			return vehicle;
 		}
 	}
 }
