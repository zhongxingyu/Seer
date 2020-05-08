 package cours.ulaval.glo4003.controller.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 import cours.ulaval.glo4003.domain.Section;
 import cours.ulaval.glo4003.domain.TeachMode;
 import cours.ulaval.glo4003.domain.Time;
 import cours.ulaval.glo4003.domain.TimeDedicated;
 import cours.ulaval.glo4003.domain.TimeSlot;
 import cours.ulaval.glo4003.domain.TimeSlot.DayOfWeek;
 
 public class SectionModel {
 
 	private static final String VENDREDI = "Vendredi";
 	private static final String JEUDI = "Jeudi";
 	private static final String MERCREDI = "Mercredi";
 	private static final String MARDI = "Mardi";
 	private static final String LUNDI = "Lundi";
 	private static final Map<String, DayOfWeek> daysAssociations;
 	private static final Map<DayOfWeek, String> inverseDaysAssociations;
 	static {
 		Map<String, DayOfWeek> days = new HashMap<String, DayOfWeek>();
 		days.put(LUNDI, DayOfWeek.MONDAY);
 		days.put(MARDI, DayOfWeek.TUESDAY);
 		days.put(MERCREDI, DayOfWeek.WEDNESDAY);
 		days.put(JEUDI, DayOfWeek.THURSDAY);
 		days.put(VENDREDI, DayOfWeek.FRIDAY);
 		daysAssociations = Collections.unmodifiableMap(days);
 		Map<DayOfWeek, String> inverseDays = new HashMap<DayOfWeek, String>();
 		inverseDays.put(DayOfWeek.MONDAY, LUNDI);
 		inverseDays.put(DayOfWeek.TUESDAY, MARDI);
 		inverseDays.put(DayOfWeek.WEDNESDAY, MERCREDI);
 		inverseDays.put(DayOfWeek.THURSDAY, JEUDI);
 		inverseDays.put(DayOfWeek.FRIDAY, VENDREDI);
 		inverseDaysAssociations = Collections.unmodifiableMap(inverseDays);
 	}
 
 	private String acronym;
 	private List<String> days;
 	private String labDay;
 	private String group;
 	private Integer hoursInClass = 0;
 	private Integer hoursAtHome = 0;
 	private Integer hoursInLab = 0;
 	private String laboTimeSlotStart;
 	private String laboTimeSlotEnd;
 	private String nrc;
 	private String personInCharge;
 	private List<String> teachers;
 	private String teachMode;
 	private List<String> timeSlotStarts;
 	private List<String> timeSlotEnds;
 
 	public SectionModel() {
 		initialize();
 		this.nrc = NRCGenerator.generate();
 	}
 
 	public SectionModel(Section section) {
 		initialize();
 		this.nrc = section.getNrc();
 		this.group = section.getGroup();
 		this.personInCharge = section.getPersonInCharge();
 		this.teachers = section.getTeachers();
 		this.teachMode = section.getTeachMode().toString();
 		this.acronym = section.getCourseAcronym();
 
 		TimeDedicated timeDedicated = section.getTimeDedicated();
 		this.hoursInClass = timeDedicated.getCourseHours();
 		this.hoursInLab = timeDedicated.getLabHours();
 		this.hoursAtHome = timeDedicated.getOthersHours();
 
 		TimeSlot timeSlot = section.getLabTimeSlot();
 		this.laboTimeSlotStart = timeSlot.getStartTime().toString();
 		this.laboTimeSlotEnd = timeSlot.getEndTime().toString();
 		this.labDay = inverseDaysAssociations.get(timeSlot.getDayOfWeek());
 
 		for (TimeSlot slot : section.getCourseTimeSlots()) {
 			this.days.add(inverseDaysAssociations.get(slot.getDayOfWeek()));
			this.timeSlotStarts.add(timeSlot.getStartTime().toString());
			this.timeSlotEnds.add(timeSlot.getEndTime().toString());
 		}
 
 	}
 
 	public Section convertToSection() {
 		Section section = new Section();
 		section.setCourseAcronym(acronym);
 		section.setGroup(group);
 		section.setNrc(nrc.toString());
 		section.setPersonInCharge(personInCharge);
 		section.setTeachers(teachers);
 		section.setTeachMode(TeachMode.valueOf(teachMode));
 		section.setLabTimeSlot(new TimeSlot());
 
 		TimeDedicated timeDedicated = new TimeDedicated(hoursInClass, hoursInLab, hoursAtHome);
 		section.setTimeDedicated(timeDedicated);
 
 		if (StringUtils.isNotEmpty(labDay) && StringUtils.isNotEmpty(laboTimeSlotStart) && StringUtils.isNotEmpty(laboTimeSlotEnd)) {
 			TimeSlot labTimeSlot = convertTimeSlot(labDay, laboTimeSlotStart, laboTimeSlotEnd);
 			section.setLabTimeSlot(labTimeSlot);
 		}
 
 		List<TimeSlot> timeSlots = new ArrayList<TimeSlot>();
 		for (int i = 0; i < timeSlotStarts.size(); i++) {
 			timeSlots.add(convertTimeSlot(days.get(i), timeSlotStarts.get(i), timeSlotEnds.get(i)));
 		}
 		section.setCourseTimeSlots(timeSlots);
 
 		return section;
 	}
 
 	private TimeSlot convertTimeSlot(String day, String start, String end) {
 		TimeSlot timeSlot = new TimeSlot();
 
 		String[] hoursMinStart = start.split(":");
 		Time startTime = new Time(new Integer(hoursMinStart[0]), new Integer(hoursMinStart[1]));
 
 		String[] hoursMinEnd = end.split(":");
 		Time endTime = new Time(new Integer(hoursMinEnd[0]), new Integer(hoursMinEnd[1]));
 
 		timeSlot.setStartTime(startTime);
 		timeSlot.setEndTime(endTime);
 		timeSlot.setDayOfWeek(daysAssociations.get(day));
 
 		return timeSlot;
 	}
 
 	public String getAcronym() {
 		return acronym;
 	}
 
 	public void setAcronym(String acronym) {
 		this.acronym = acronym;
 	}
 
 	public List<String> getDays() {
 		return days;
 	}
 
 	public void setDays(List<String> days) {
 		this.days = days;
 	}
 
 	public String getGroup() {
 		return group;
 	}
 
 	public void setGroup(String group) {
 		this.group = group;
 	}
 
 	public Integer getHoursInClass() {
 		return hoursInClass;
 	}
 
 	public void setHoursInClass(Integer hoursInClass) {
 		this.hoursInClass = hoursInClass;
 	}
 
 	public Integer getHoursAtHome() {
 		return hoursAtHome;
 	}
 
 	public void setHoursAtHome(Integer hoursAtHome) {
 		this.hoursAtHome = hoursAtHome;
 	}
 
 	public Integer getHoursInLab() {
 		return hoursInLab;
 	}
 
 	public void setHoursInLab(Integer hoursInLab) {
 		this.hoursInLab = hoursInLab;
 	}
 
 	public String getLaboTimeSlotStart() {
 		return laboTimeSlotStart;
 	}
 
 	public void setLaboTimeSlotStart(String laboTimeSlotStart) {
 		this.laboTimeSlotStart = laboTimeSlotStart;
 	}
 
 	public String getLaboTimeSlotEnd() {
 		return laboTimeSlotEnd;
 	}
 
 	public void setLaboTimeSlotEnd(String laboTimeSlotEnd) {
 		this.laboTimeSlotEnd = laboTimeSlotEnd;
 	}
 
 	public String getPersonInCharge() {
 		return personInCharge;
 	}
 
 	public void setPersonInCharge(String personInCharge) {
 		this.personInCharge = personInCharge;
 	}
 
 	public List<String> getTeachers() {
 		return teachers;
 	}
 
 	public void setTeachers(List<String> teachers) {
 		this.teachers = teachers;
 	}
 
 	public String getTeachMode() {
 		return teachMode;
 	}
 
 	public void setTeachMode(String teachMode) {
 		this.teachMode = teachMode;
 	}
 
 	public List<String> getTimeSlotStarts() {
 		return timeSlotStarts;
 	}
 
 	public void setTimeSlotStarts(List<String> timeSlotStarts) {
 		this.timeSlotStarts = timeSlotStarts;
 	}
 
 	public List<String> getTimeSlotEnds() {
 		return timeSlotEnds;
 	}
 
 	public void setTimeSlotEnds(List<String> timeSlotEnds) {
 		this.timeSlotEnds = timeSlotEnds;
 	}
 
 	public String getLabDay() {
 		return labDay;
 	}
 
 	public void setLabDay(String labDays) {
 		this.labDay = labDays;
 	}
 
 	public String getNrc() {
 		return nrc;
 	}
 
 	public void setNrc(String nrc) {
 		this.nrc = nrc;
 	}
 
 	private void initialize() {
 		this.days = new ArrayList<String>();
 		this.timeSlotStarts = new ArrayList<String>();
 		this.timeSlotEnds = new ArrayList<String>();
 	}
 }
