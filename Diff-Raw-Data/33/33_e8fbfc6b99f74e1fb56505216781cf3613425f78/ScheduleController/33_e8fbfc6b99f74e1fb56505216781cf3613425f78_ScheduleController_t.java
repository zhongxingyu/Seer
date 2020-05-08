 package cours.ulaval.glo4003.controller;
 
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import cours.ulaval.glo4003.controller.model.CalendarModel;
 import cours.ulaval.glo4003.controller.model.ScheduleModel;
 import cours.ulaval.glo4003.controller.model.SectionModel;
 import cours.ulaval.glo4003.controller.model.SortedSlotsModel;
 import cours.ulaval.glo4003.domain.Role;
 import cours.ulaval.glo4003.domain.Schedule;
 import cours.ulaval.glo4003.domain.Section;
 import cours.ulaval.glo4003.domain.Semester;
 import cours.ulaval.glo4003.domain.Time;
 import cours.ulaval.glo4003.domain.TimeSlot;
 import cours.ulaval.glo4003.domain.TimeSlot.DayOfWeek;
 import cours.ulaval.glo4003.domain.User;
 import cours.ulaval.glo4003.domain.repository.CourseRepository;
 import cours.ulaval.glo4003.domain.repository.OfferingRepository;
 import cours.ulaval.glo4003.domain.repository.ScheduleRepository;
 import cours.ulaval.glo4003.domain.repository.UserRepository;
 
 @Controller
 @RequestMapping(value = "/schedule")
 public class ScheduleController {
 
 	@Inject
 	private CourseRepository courseRepository;
 
 	@Inject
 	private OfferingRepository offeringRepository;
 
 	@Inject
 	private ScheduleRepository scheduleRepository;
 
 	@Inject
 	private UserRepository userRepository;
 
 	private ObjectMapper mapper = new ObjectMapper();
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView schedule() {
 		ModelAndView mv = new ModelAndView("schedule");
 
 		List<ScheduleModel> scheduleModels = new ArrayList<ScheduleModel>();
 
 		for (Schedule schedule : scheduleRepository.findAll()) {
 			scheduleModels.add(new ScheduleModel(schedule));
 		}
 
 		mv.addObject("schedules", scheduleModels);
 		return mv;
 	}
 
 	@RequestMapping(value = "/{id}/{view}", method = RequestMethod.GET)
 	public ModelAndView scheduleById(@PathVariable String id, @PathVariable String view) throws Exception {
 
 		ModelAndView mv;
 		if (view.contentEquals("calendar")) {
 			mv = new ModelAndView("calendar");
 			CalendarModel calendarModel = new CalendarModel(new ArrayList<Section>(scheduleRepository.findById(id).getSections().values()));
 
 			String JSON = "";
 			try {
 				JSON = mapper.writeValueAsString(calendarModel);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			mv.addObject("sections", JSON);
 			mv.addObject("scheduleId", id);
 		} else {
 			mv = new ModelAndView("schedulebyid");
 			mv.addObject("schedule", new ScheduleModel(scheduleRepository.findById(id)));
 			mv.addObject("sections", new SortedSlotsModel(new ArrayList<Section>(scheduleRepository.findById(id).getSections().values())));
 
 		}
 		return mv;
 	}
 
 	@RequestMapping(value = "/add", method = RequestMethod.GET)
 	public ModelAndView addSchedule() throws Exception {
 		ModelAndView mv = new ModelAndView("addschedule");
 		mv.addObject("years", offeringRepository.findYears());
 
 		return mv;
 	}
 
 	@RequestMapping(value = "/add/{year}/{semester}", method = RequestMethod.GET)
 	public ModelAndView addSchedule(@PathVariable String year, @PathVariable Semester semester) throws Exception {
 		Schedule schedule = new Schedule(scheduleRepository.getId(year, semester));
 		schedule.setYear(year);
 		schedule.setSemester(semester);
 
 		scheduleRepository.store(schedule);
 
 		ModelAndView mv = new ModelAndView("createschedule");
 		mv.addObject("year", year);
 		mv.addObject("semester", semester);
 		mv.addObject("id", schedule.getId());
 		mv.addObject("courses", courseRepository.findByOffering(offeringRepository.find(year)));
 
 		return mv;
 	}
 
 	@RequestMapping(value = "/addsection/{id}/{year}/{semester}", method = RequestMethod.POST)
 	public ModelAndView postSection(@PathVariable String id, @PathVariable String year, @PathVariable Semester semester,
 			@ModelAttribute("section") SectionModel section) throws Exception {
 		Schedule schedule = scheduleRepository.findById(id);
 		schedule.add(section.convertToSection());
 		scheduleRepository.store(schedule);
 
 		ModelAndView mv = new ModelAndView("createschedule");
 		mv.addObject("year", year);
 		mv.addObject("semester", semester);
 		mv.addObject("id", id);
 		mv.addObject("courses", courseRepository.findByOffering(offeringRepository.find(year)));
 
 		List<SectionModel> sections = getSections(schedule);
 		mv.addObject("sections", sections);
 
 		return mv;
 	}
 
 	@RequestMapping(value = "/addsection/{id}/{year}/{semester}", method = RequestMethod.GET)
 	public ModelAndView addSection(@PathVariable String id, @PathVariable String year, @PathVariable Semester semester,
 			@RequestParam(required = true, value = "acronym") String acronym) {
 		ModelAndView mv = new ModelAndView("addsection");
 		mv.addObject("acronym", acronym);
 		mv.addObject("course", courseRepository.findByAcronym(acronym));
 		mv.addObject("semester", semester);
 		mv.addObject("year", year);
 		mv.addObject("id", id);
 		List<String> teachers = new ArrayList<String>();
 		for (User teacher : userRepository.findByRole(Role.ROLE_Enseignant)) {
 			teachers.add(teacher.getName());
 		}
 		mv.addObject("teachers", teachers);
 
 		return mv;
 	}
 
 	@RequestMapping(value = "/editsection/{id}/{year}/{semester}/{sectionNrc}", method = RequestMethod.GET)
 	public ModelAndView editSection(@PathVariable String id, @PathVariable String year, @PathVariable Semester semester,
 			@PathVariable String sectionNrc) {
 
 		// ModelAndView mv = new
 		// ModelAndView("partialViews/editSectionPartial");
 		ModelAndView mv = new ModelAndView("editsection");
 		mv.addObject("semester", semester);
 		mv.addObject("year", year);
 		mv.addObject("id", id);
 
 		Schedule schedule = scheduleRepository.findById(id);
 		Section section = schedule.getSections().get(sectionNrc);
 		mv.addObject("section", new SectionModel(section));
 
 		return mv;
 	}
 
 	@RequestMapping(value = "/editsection/{id}/{year}/{semester}/{sectionNrc}", method = RequestMethod.POST)
 	public ModelAndView postEditSection(@PathVariable String id, @PathVariable String year, @PathVariable Semester semester,
 			@PathVariable String sectionNrc, @ModelAttribute("section") SectionModel section, Principal principal) throws Exception {
 
 		if (!userRepository.findByIdul(principal.getName()).getRoles().contains(Role.ROLE_Responsable)) {
 			ModelAndView mv = scheduleById(id, "list");
 			mv.addObject("user", userRepository.findByIdul(principal.getName()));
 			return mv;
 		}
 		ModelAndView mv = new ModelAndView("createschedule");
 		try {
 			Schedule schedule = scheduleRepository.findById(id);
 			schedule.delete(sectionNrc);
 			schedule.add(section.convertToSection());
 			scheduleRepository.store(schedule);
 			mv.addObject("error", ControllerMessages.SUCCESS);
 			mv.addObject("year", year);
 			mv.addObject("semester", semester);
 			mv.addObject("id", id);
 			mv.addObject("courses", courseRepository.findByOffering(offeringRepository.find(year)));
 			mv.addObject("sections", getSections(schedule));
 		} catch (Exception e) {
 			mv.addObject("error", e.getMessage());
 		}
 		return mv;
 	}
 
 	@RequestMapping(value = "/{id}/update", method = RequestMethod.POST)
 	@ResponseBody
 	public String updateSection(@PathVariable String id, String nrc, String oldDay, String oldTimeStart, String newDay, String newTimeStart,
 			String duration, Principal principal) throws Exception {
 
		// System.out.println("idhoraire:" + id);
		// System.out.println("nrc:" + nrc);
		// System.out.println("oldDay:" + oldDay);
		// System.out.println("oldTimeStart:" + oldTimeStart);
		// System.out.println("newDay:" + newDay);
		// System.out.println("newTimeStart:" + newTimeStart);
		// System.out.println("duration:" + duration);
 
 		DayOfWeek newDayOfWeek = null;
 		if (newDay == "mon") {
 			newDayOfWeek = DayOfWeek.MONDAY;
 		} else if (newDay == "tue") {
 			newDayOfWeek = DayOfWeek.TUESDAY;
 		} else if (newDay == "wed") {
 			newDayOfWeek = DayOfWeek.WEDNESDAY;
 		} else if (newDay == "thu") {
 			newDayOfWeek = DayOfWeek.THURSDAY;
 		} else if (newDay == "fri") {
 			newDayOfWeek = DayOfWeek.FRIDAY;
 		}
 
 		TimeSlot newTimeSlot = new TimeSlot(new Time(Integer.parseInt(newTimeStart.split(":")[0]), Integer.parseInt(newTimeStart.split(":")[1])),
 				Integer.parseInt(duration), newDayOfWeek);
 
 		Time oldTmStart = new Time(Integer.parseInt(oldTimeStart.split(":")[0]), Integer.parseInt(oldTimeStart.split(":")[1]));
 
 		List<TimeSlot> courseTimeSlots = scheduleRepository.findById(id).getSections().get(nrc).getCourseTimeSlots();
 
 		for (TimeSlot slot : courseTimeSlots) {
 			if (slot.getDayOfWeek().toString().toLowerCase().substring(0, 2) == oldDay || slot.getStartTime().equals(oldTmStart)) {
 				courseTimeSlots.remove(slot);
 				courseTimeSlots.add(newTimeSlot);
 				scheduleRepository.findById(id).getSections().get(nrc).setCourseTimeSlots(courseTimeSlots);
 			}
 		}
 
 		TimeSlot labTimeSlot = scheduleRepository.findById(id).getSections().get(nrc).getLabTimeSlot();
 		if (labTimeSlot != null) {
 			if (labTimeSlot.getDayOfWeek().toString().toLowerCase().substring(0, 2) == oldDay || labTimeSlot.getStartTime().equals(oldTmStart)) {
 				scheduleRepository.findById(id).getSections().get(nrc).setLabTimeSlot(newTimeSlot);
 			}
 		}
 
		List<TimeSlot> test = scheduleRepository.findById(id).getSections().get(nrc).getCoursesAndLabTimeSlots();
		for (TimeSlot ts : test) {
			System.out.println("nrc: " + nrc);
			System.out.println("startTime: " + ts.getStartTime().toString());
			System.out.println("endTime: " + ts.getEndTime().toString());
		}
		System.out.println(scheduleRepository.findById(id).getSections().get(nrc).getCoursesAndLabTimeSlots());

 		return "success";
 	}
 
 	@RequestMapping(value = "/delete/{scheduleId}", method = RequestMethod.GET)
 	public ModelAndView deleteSchedule(@PathVariable String scheduleId) {
 		Boolean error = false;
 		String errorMessage = "";
 		try {
 			scheduleRepository.delete(scheduleId);
 		} catch (Exception e) {
 			error = true;
 			errorMessage = e.getMessage();
 		}
 
 		ModelAndView mv = schedule();
 		if (error) {
 			mv.addObject("error", errorMessage);
 		} else {
 			mv.addObject("error", ControllerMessages.SUCCESS);
 		}
 
 		return mv;
 	}
 
 	@RequestMapping(value = "/deletesection/{scheduleId}/{year}/{semester}/{sectionNrc}", method = RequestMethod.GET)
 	public ModelAndView deleteSection(@PathVariable String scheduleId, @PathVariable String sectionNrc, @PathVariable String year,
 			@PathVariable Semester semester) {
 		ModelAndView mv = new ModelAndView("createschedule");
 
 		try {
 			Schedule schedule = scheduleRepository.findById(scheduleId);
 			schedule.delete(sectionNrc);
 			scheduleRepository.store(schedule);
 			mv.addObject("error", ControllerMessages.SUCCESS);
 			mv.addObject("year", year);
 			mv.addObject("semester", semester);
 			mv.addObject("id", scheduleId);
 			mv.addObject("courses", courseRepository.findByOffering(offeringRepository.find(year)));
 			mv.addObject("sections", getSections(schedule));
 
 		} catch (Exception e) {
 			mv.addObject("error", e.getMessage());
 		}
 
 		return mv;
 	}
 
 	private List<SectionModel> getSections(Schedule schedule) {
 		List<SectionModel> sections = new ArrayList<SectionModel>();
 		for (Section sectionInSchedule : schedule.getSections().values()) {
 			sections.add(new SectionModel(sectionInSchedule));
 		}
 		return sections;
 	}
 }
