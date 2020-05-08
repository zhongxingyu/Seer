 package at.ac.tuwien.dse.fairsurgeries.web.actors;
 
 import java.math.BigInteger;
 import java.sql.Date;
 import java.util.Arrays;
 
 import org.joda.time.format.DateTimeFormat;
 import org.springframework.amqp.core.AmqpTemplate;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.i18n.LocaleContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import at.ac.tuwien.dse.fairsurgeries.domain.Doctor;
 import at.ac.tuwien.dse.fairsurgeries.domain.OPSlot;
 import at.ac.tuwien.dse.fairsurgeries.domain.Reservation;
 import at.ac.tuwien.dse.fairsurgeries.domain.SurgeryType;
 import at.ac.tuwien.dse.fairsurgeries.general.Constants;
 import at.ac.tuwien.dse.fairsurgeries.service.DoctorService;
 import at.ac.tuwien.dse.fairsurgeries.service.HospitalService;
 import at.ac.tuwien.dse.fairsurgeries.service.LogEntryService;
 import at.ac.tuwien.dse.fairsurgeries.service.OPSlotService;
 import at.ac.tuwien.dse.fairsurgeries.service.PatientService;
 import at.ac.tuwien.dse.fairsurgeries.web.MessageController;
 
 @Controller
 @RequestMapping("/actors/doctor")
 public class ActorDoctorController {
 
 	@Autowired
 	private AmqpTemplate template;
 	@Autowired
 	private LogEntryService logEntryService;
 	@Autowired
 	private HospitalService hospitalService;
 	@Autowired
 	private OPSlotService opSlotService;
 	@Autowired
 	private DoctorService doctorService;
 	@Autowired
 	private PatientService patientService;
 
 	@RequestMapping(value = "{doctorId}", method = RequestMethod.GET)
 	public String showMenu(@PathVariable BigInteger doctorId, Model uiModel) {
 		logEntryService.log(Constants.Component.Frontend.toString(), "Starting ActorDoctor . () for ID: " + doctorId);
 
 		Doctor doctor = doctorService.findDoctor(doctorId);
 
 		if (doctor != null) {
 			uiModel.addAttribute("doctor", doctor);
 
 			return "actors/doctor/showmenu";
 		} else {
 			return "redirect:/resourceNotFound";
 		}
 	}
 
 	@RequestMapping(value = "/viewslots", method = RequestMethod.POST)
 	public String viewSlots(@ModelAttribute Doctor doctor, Model uiModel) {
 		logEntryService.log(Constants.Component.Frontend.toString(), "Starting ActorDoctor . viewSlots() for doctor: " + doctor);
 		logEntryService.log(Constants.Component.Frontend.toString(), "uiModel: " + uiModel);
 
 		return "actors/doctor/viewslots";
 	}
 
 	@RequestMapping(value = "/viewnotifications", method = RequestMethod.POST)
 	public String viewNotifications(@ModelAttribute Doctor doctor, Model uiModel) {
 		logEntryService.log(Constants.Component.Frontend.toString(), "Starting ActorDoctor . viewNotifications() for doctor: " + doctor);
 		logEntryService.log(Constants.Component.Frontend.toString(), "uiModel: " + uiModel);
 
 		return "actors/doctor/viewnotifications";
 	}
 
 	@RequestMapping(value = "/manageslots", method = RequestMethod.POST)
 	public String manageSlots(@ModelAttribute Doctor doctor, Model uiModel) {
 		logEntryService.log(Constants.Component.Frontend.toString(), "Starting ActorDoctor . manageSlots() for doctor: " + doctor);
 		logEntryService.log(Constants.Component.Frontend.toString(), "uiModel: " + uiModel);
 		uiModel.addAttribute("opSlot", new OPSlot());
 		uiModel.addAttribute("opSlots", opSlotService.findByDoctor(doctor));
 
 		uiModel.addAttribute("OPSlot__datefrom_date_format", DateTimeFormat.patternForStyle("M-", LocaleContextHolder.getLocale()));
 		uiModel.addAttribute("OPSlot__dateto_date_format", DateTimeFormat.patternForStyle("M-", LocaleContextHolder.getLocale()));
 
 		/*
 		 * uiModel.addAttribute("doctors", doctorService.findAllDoctors());
 		 * uiModel.addAttribute("hospitals",
 		 * hospitalService.findAllHospitals()); uiModel.addAttribute("patients",
 		 * patientService.findAllPatients());
 		 * uiModel.addAttribute("surgerytypes",
 		 * Arrays.asList(SurgeryType.values()));
 		 */
 
 		return "actors/doctor/manageslots";
 	}
 
 	@RequestMapping(value = "/reservations", method = RequestMethod.POST)
 	public String manageReservation(@ModelAttribute Doctor doctor, Model uiModel) {
 		logEntryService.log(Constants.Component.Frontend.toString(), "Starting ActorDoctorController . manageReservations()");
 		logEntryService.log(Constants.Component.Frontend.toString(), "doctor: " + doctor);
 
 		Reservation reservation = new Reservation();
 		reservation.setRadius(34.);
 
 		uiModel.addAttribute("doctors", Arrays.asList(doctor));
 		uiModel.addAttribute("reservation", reservation);
 		uiModel.addAttribute("surgeryTypes", Arrays.asList(SurgeryType.values()));
 		uiModel.addAttribute("patients", patientService.findAllPatients());
		uiModel.addAttribute("OPSlot__datefrom_date_format", DateTimeFormat.patternForStyle("M-", LocaleContextHolder.getLocale()));
		uiModel.addAttribute("OPSlot__dateto_date_format", DateTimeFormat.patternForStyle("M-", LocaleContextHolder.getLocale()));
 
 		return "actors/doctor/reservations";
 	}
 
 	@RequestMapping(value = "/doreservation", method = RequestMethod.POST, produces = "text/html")
 	public String doReservation(@ModelAttribute Reservation reservation, Model uiModel) {
 		logEntryService.log(Constants.Component.Frontend.toString(), "Starting ActorDoctorController . doReservation()");
 
 		logEntryService.log("TEST", "patient: " + reservation.getPatient());
 		logEntryService.log("TEST", "doctor: " + reservation.getDoctor());
 		logEntryService.log("TEST", "type: " + reservation.getSurgeryType());
 		logEntryService.log("TEST", "radius: " + reservation.getRadius());
 		logEntryService.log("TEST", "from: " + reservation.getDateFrom());
 		logEntryService.log("TEST", "to: " + reservation.getDateTo());
 		
 		if (reservation != null) {
 			MessageController.sendMessage(template, Constants.Queue.MatcherIn, reservation);
 		}
 
 		return "actors/doctor/viewslots";
 	}
 }
