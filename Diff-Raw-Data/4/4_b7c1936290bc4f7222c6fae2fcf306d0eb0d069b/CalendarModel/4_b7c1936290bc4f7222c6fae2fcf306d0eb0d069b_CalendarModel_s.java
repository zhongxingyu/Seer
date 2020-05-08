 package cours.ulaval.glo4003.controller.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import cours.ulaval.glo4003.controller.model.utils.TimeSlotComparator;
 import cours.ulaval.glo4003.domain.Section;
 import cours.ulaval.glo4003.domain.TimeSlot;
 
 public class CalendarModel {
 
 	private List<CourseSlotModel> courseSlots = new ArrayList<CourseSlotModel>();
 
 	public CalendarModel(List<Section> sections) {
 		for (Section section : sections) {
 			for (TimeSlot timeSlot : section.getCourseTimeSlots()) {
 				courseSlots.add(new CourseSlotModel(section, timeSlot, false));
 			}
 		}
 		for (Section section : sections) {
 			TimeSlot timeSlot = section.getLabTimeSlot();
 			if (timeSlot != null) {
 				courseSlots.add(new CourseSlotModel(section, timeSlot, true));
 			}
 		}
 		Collections.sort(courseSlots, new TimeSlotComparator());

		for (CourseSlotModel cs : courseSlots) {
			System.out.println(cs.getAcronym());
		}
 	}
 
 	public List<CourseSlotModel> getCourseSlots() {
 		return courseSlots;
 	}
 
 	public void setCourseSlots(List<CourseSlotModel> courseSlots) {
 		this.courseSlots = courseSlots;
 	}
 
 }
