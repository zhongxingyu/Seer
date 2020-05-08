 package ua.dp.primat.schedule.services;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.annotation.Resource;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import ua.dp.primat.domain.Lecturer;
 import ua.dp.primat.domain.Room;
 import ua.dp.primat.domain.StudentGroup;
 import ua.dp.primat.domain.lesson.DayOfWeek;
 import ua.dp.primat.domain.lesson.Lesson;
 import ua.dp.primat.domain.workload.Discipline;
 import ua.dp.primat.repositories.DisciplineRepository;
 import ua.dp.primat.repositories.LecturerRepository;
 import ua.dp.primat.repositories.LessonRepository;
 import ua.dp.primat.repositories.RoomRepository;
 
 /**
  * Service which helps get and edit schedule.
  * @author EniSh
  */
 @Service
 @Transactional
 public class EditScheduleService {
 
     /**
      * gets special collecton of lessons which help edit schedule.
      * @param group
      * @param semester semester number of editable schedule
      * @return collection of lessons
      */
     public WeekLessonColection getSchedule(StudentGroup group, Long semester) {
         final List<Lesson> lessons = lessonRepository.getLessonsByGroupAndSemester(group, semester);
         return new WeekLessonColection(lessons);
     }
 
     /**
      *
      * @param group
      * @param semester
      * @param lessonColection
      */
     public void setSchedule(StudentGroup group, Long semester, WeekLessonColection lessonColection) {
        final LessonItem[][] lessonItems = lessonColection.getLessonItems();
         for (DayOfWeek day : DayOfWeek.values()) {
            final LessonItem[] liDay = lessonItems[day.getNumber()];
             for (int j = 0; j < liDay.length; j++) {
                 saveLessonItem(liDay[j], group, semester, day, j);
             }
         }
     }
 
     private void saveLessonItem(LessonItem lessonItem, StudentGroup group, Long semester,
                                 DayOfWeek day, int lessonNumber) {
         saveEditableLesson(lessonItem.getNumerator(), group, semester, day, lessonNumber);
         if (!lessonItem.isOneLesson()) {
             saveEditableLesson(lessonItem.getDenominator(), group, semester, day, lessonNumber);
         }
     }
 
     private void saveEditableLesson(EditableLesson editableLesson, StudentGroup group, Long semester,
                                     DayOfWeek day, int lessonNumber) {
         if (editableLesson.isEmpty()) {
             if (editableLesson.getId() != null) {
                 lessonService.deleteLesson(editableLesson.getId());
             }
         } else {
             final Lesson lesson = editableLesson.toLesson(day, Long.valueOf(lessonNumber));
             lesson.getLessonDescription().setSemester(semester);
             lesson.getLessonDescription().setStudentGroup(group);
             lessonService.saveLesson(lesson);
         }
     }
 
     public void updateLists() {
         disciplines = disciplineRepository.getDisciplines();
         disciplines.add(null);
         lecturers = lecturerRepository.getAllLecturers();
         rooms = roomRepository.getRooms();
     }
 
     public List<Discipline> getDisciplines() {
         return disciplines;
     }
 
     public List<Lecturer> getLecturers() {
         return lecturers;
     }
 
     public List<Room> getRooms() {
         return rooms;
     }
 
     private List<Discipline> disciplines = new ArrayList<Discipline>();
     private List<Lecturer> lecturers = new ArrayList<Lecturer>();
     private List<Room> rooms = new ArrayList<Room>();
 
     @Resource
     private LessonService lessonService;
 
     @Resource
     private LessonRepository lessonRepository;
 
     @Resource
     private DisciplineRepository disciplineRepository;
 
     @Resource
     private RoomRepository roomRepository;
 
     @Resource
     private LecturerRepository lecturerRepository;
 }
