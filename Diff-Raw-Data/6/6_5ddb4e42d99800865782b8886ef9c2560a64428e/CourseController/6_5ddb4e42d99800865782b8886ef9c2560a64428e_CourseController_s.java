 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.web;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.model.SelectItem;
 import javax.inject.Inject;
 import javax.inject.Named;
 import no.hials.muldvarpweb.domain.*;
 import no.hials.muldvarpweb.service.ArticleService;
 import no.hials.muldvarpweb.service.CourseService;
 import no.hials.muldvarpweb.service.LibraryService;
 import no.hials.muldvarpweb.service.QuizService;
 import no.hials.muldvarpweb.service.VideoService;
 import org.primefaces.model.DualListModel;
 
 /**
  *
  * @author kristoffer
  */
 @Named
 @SessionScoped
 public class CourseController implements Serializable {
     @Inject CourseService service;
     Course newCourse;
     Theme newTheme;
     Task newTask;
     ObligatoryTask newObligatoryTask;
     Exam newExam;
     Question newQuestion;
     Alternative newAlternative;
     List<Course> courses;
     Course selected;
     Course filter;
     Theme selectedTheme;
     Task selectedTask;
     ObligatoryTask selectedObligatoryTask;
     Exam selectedExam;
     Question selectedQuestion;
     Alternative selectedAlternative;
     Video selectedVideo;
     Video newVideo;
     LibraryItem selectedDocument;
 
     public List<Course> getCourses() {
         //if(courses == null) {
             courses = service.findCourses();
         //}
         return courses;
     }
 
     public void setCourses(List<Course> courses) {
         this.courses = courses;
     }
 
     public Course getFilter() {
         return filter;
     }
 
     public void setFilter(Course filter) {
         this.filter = filter;
     }
 
     public Course getCourse() {
        if(newCourse == null)
             newCourse = new Course();
         
         return newCourse;
     }
 
     public void setCourse(Course course) {
         this.newCourse = course;
     }
 
     public Course getSelected() {
         return selected;
     }
 
     public String setSelected(Course selected) {
         if(selected == null) {
             selected = getCourse();
         }
         this.selected = selected;
         videos = null;
         documents = null;
         quizzes = null;
         articles = null;
         return "editCourse?faces-redirect=true";
     }
     
     public String setSelectedTheme(Theme selectedTheme) {
         this.selectedTheme = selectedTheme;
         return "editTheme";
     }
 
     public Theme getSelectedTheme() {
         return selectedTheme;
     }
 
     public Task getSelectedTask() {
         return selectedTask;
     }
     
     public String setSelectedTask(Task selectedTask) {
         this.selectedTask = selectedTask;
         return "editTask";
     }
 
     public Exam getSelectedExam() {
         return selectedExam;
     }
 
     public String setSelectedExam(Exam selectedExam) {
         this.selectedExam = selectedExam;
         return "editExam";
     }
     
 
     public ObligatoryTask getSelectedObligatoryTask() {
         return selectedObligatoryTask;
     }
 
     public String setSelectedObligatoryTask(ObligatoryTask selectedObligatoryTask) {
         this.selectedObligatoryTask = selectedObligatoryTask;
         return "editObligTask";
     }
 
     public Question getSelectedQuestion() {
         return selectedQuestion;
     }
 
     public String setSelectedQuestion(Question selectedQuestion) {
         this.selectedQuestion = selectedQuestion;
         return "editQuestion";
     }
 
     public CourseService getService() {
         return service;
     }
 
     public void setService(CourseService service) {
         this.service = service;
     }
     
     public String addCourse() {
         if(newCourse != null) {
             
 //            Date ts =  new Date();
 //            SimpleDateFormat sDF = new SimpleDateFormat("H:mm:ss dd.MM.yyyy");
 //        
 //            String date = sDF.format(ts);
             
             //newCourse.setLastUpdate(date);
             
             service.addCourse(newCourse);
         }
         return "listCourses";
     }
     
     public String editCourse() {
         if(selected != null) {
             service.editCourse(selected);
         }
         return "listCourses";
     }
     
     public String addNewRevCourse() {
         if(selected != null ) {
             service.addNewRevCourse(selected);
         }
         return "listCourses";
     }
     
     public String removeCourse() {
         if(selected != null) {
             service.removeCourse(selected);
         }
         return "listCourses";
     }
     
     public void addTheme() {
         if(newTheme != null && selected != null) {
             service.addTheme(selected, newTheme);
             newTheme = null;
         }
     }
     
     public String editTheme() {
         if(selectedTheme != null) {
             service.editTheme(selected, selectedTheme);
         }
         return "editCourse?faces-redirect=true";
     }
     
     public String removeTheme(Theme theme) {
         if(selected != null) {
             service.removeTheme(selected, theme);
         }
         return "editCourse";
     }
     
     public void addExam() {
         if(newExam != null && selected != null) {
             service.addExam(selected, newExam);
             newExam = null;
         }
     }
     
     public String removeExam(Exam exam) {
         if(selectedExam != null) {
             service.removeExam(selected, exam);
         }
         return "editCourse?faces-redirect=true";
     }
     
     public void addTask() {
         if(newTask != null && selected != null) {
             service.addTask(selected, selectedTheme, newTask);
             newTask = null;
         }
     }
     
     public String editTask() {
         if(selectedTask != null) {
             service.editTask(selected, selectedTheme, selectedTask);
         }
         return "editTheme?faces-redirect=true";
     }
     
     public String removeTask(Task task) {
         if(selected != null) {
             service.removeTask(selected, selectedTheme, task);
         }
         return "editTheme?faces-redirect=true";
     }
     
     public void addObligatoryTask() {
         if(newObligatoryTask != null && selected != null) {
             service.addObligatoryTask(selected, newObligatoryTask);
             newObligatoryTask = null;
         }
     }
     
     public String editObligatoryTask() {
         if(selectedObligatoryTask != null) {
             service.editObligatoryTask(selected, selectedObligatoryTask);
         }
         return "editCourse?faces-redirect=true";
     }
     
     public String removeObligatoryTask(ObligatoryTask obligtask) {
         if(selected != null) {
             service.removeObligatoryTask(selected, obligtask);
         }
         return "editCourse?faces-redirect=true";
     }
     
     public String acceptObligatoryTask() {
         if(selectedObligatoryTask != null) {
             service.acceptObligatoryTask(selected, selectedObligatoryTask);
         }
         return "editObligTask?faces-redirect=true";
     }
 
     public Exam getExam() {
         if(newExam == null)
             newExam = new Exam();
         return newExam;
     }
     
     public String editExam() {
         if(selectedExam != null) {
             service.editExam(selected, selectedExam);
         }
         return "editCourse?faces-redirect=true";
     }
 
     public void setExam(Exam newExam) {
         this.newExam = newExam;
     }
     
     public void addVideo() {
         if(newVideo != null && selected != null) {
             service.addVideo(selected, newVideo);
             newVideo = null;
         }
     }
     
     public Video getVideo() {
         if(newVideo == null)
             newVideo = new Video();
         return newVideo;
     }
     
     public String editVideo() {
         if(selectedVideo != null) {
             service.editVideo(selected, selectedVideo);
         }
         return "editVideo?faces-redirect=true";
     }
     
     public String removeVideo(Video video) {
         if(selected != null) {
             service.removeVideo(selected, video);
         }
         return "editCourse?faces-redirect=true";
     }
 
     public void setVideo(Video newVideo) {
         this.newVideo = newVideo;
     }
 
     public Video getSelectedVideo() {
         return selectedVideo;
     }
 
     public String setSelectedVideo(Video selectedVideo) {
         this.selectedVideo = selectedVideo;
         return "editVideo";
     }
     
     public ObligatoryTask getObligatoryTask() {
         if(newObligatoryTask == null)
             newObligatoryTask = new ObligatoryTask();
         return newObligatoryTask;
     }
 
     public void setObligatoryTask(ObligatoryTask newObligatoryTask) {
         this.newObligatoryTask = newObligatoryTask;
     }
 
     public Task getTask() {
         if(newTask == null)
             newTask = new Task();
         return newTask;
     }
 
     public void setTask(Task newTask) {
         this.newTask = newTask;
     }
 
     public Theme getTheme() {
         if(newTheme == null)
             newTheme = new Theme();
         return newTheme;
     }
 
     public void setTheme(Theme newTheme) {
         this.newTheme = newTheme;
     }
     
     public void makeTestData() {
         service.makeTestData();
     }
     
      /**
      * This function retrieves a list of programs and creates a list of Select Items for use with JSF
      * 
      * 
      * @return List of SelectItem
      */
     public List<SelectItem> getProgrammeItems(List<Programme> programmeList) {
         
                  
         List<SelectItem> selectItems = new ArrayList<SelectItem>();
         
         
         //Loop once for each element in the supplied List of Programmes
         for (int i = 0; i < programmeList.size(); i++) {
             
             SelectItem currentSelectItem = new SelectItem(programmeList.get(i), programmeList.get(i).getName());
             
             //Loop once for every Programme in the selected course
             for(int n = 0; n < selected.getProgrammes().size() ; n++) {
                 
                 //Compare and check if there are matching ID's between programmes
                 if(programmeList.get(i).getId() == selected.getProgrammes().get(n).getId()){
                     
                     //Set checkbox to true
                     currentSelectItem.setValue(true);
 
                 }
                 
             }
             
             selectItems.add(currentSelectItem);
             
         }
         
         return selectItems;
     }
     
     /**
      * 
      * 
      */
     public void setProgrammeItems() {
         
         
     }
     
         
     public void addInfo(int i) {  
         switch(i) {
             case 1:
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "INFO: ", "Changes saved"));
                 break;
             case 2:
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "INFO: ", "Course deleted"));
                 break;
             case 3:
                 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "INFO: ", "New revision created"));
                 break;
          }
     }
 
     public List<Programme> getProgrammes() {
         return selected.getProgrammes();
     }
 
     public void setProgrammes(List<Programme> programmes) {
         selected.setProgrammes(programmes);
     }
     
     public void addProgramme(Programme p) {
         selected.addProgramme(p);
     }
 
     public Question getQuestion() {
         if(newQuestion == null)
             newQuestion = new Question();
         return newQuestion;
     }
     
     public void addQuestion() {
         if(newQuestion != null && selected != null) {
             service.addQuestion(selected, selectedTheme, selectedTask, newQuestion);
             newQuestion = null;
         }
     }
     
     public String editQuestion() {
        if(selectedQuestion != null) {
             service.editQuestion(selected, selectedTheme, selectedTask, selectedQuestion);
         }
         return "editTask?faces-redirect=true"; 
     }
     
     public String removeQuestion(Question q) {
         if(selected != null) {
             service.removeQuestion(selected, selectedTheme, selectedTask, q);
         }
         return "editTask?faces-redirect=true";
     }
     
     public void setAnswer(Alternative a) {
         if(selectedQuestion != null) {
             service.setAnswer(selected, selectedTheme, selectedTask, selectedQuestion, a);
         }
     }
     
     public void addAlternative() {
         if(newAlternative != null && selected != null) {
             service.addAlternative(selected, selectedTheme, selectedTask, selectedQuestion, newAlternative);
             newAlternative = null;
         }
     }
     
     public String removeAlternative(Alternative a) {
         if(selected != null) {
             service.removeAlternative(selected, selectedTheme, selectedTask, selectedQuestion, a);
         }
         return "editTask?faces-redirect=true";
     }
 
     public Alternative getAlternative() {
         if(newAlternative == null)
             newAlternative = new Alternative();
         return newAlternative;
     }
     public Alternative getSelectedAlternative() {
         return selectedAlternative;
     }
 
     public void setSelectedAlternative(Alternative selectedAlternative) {
         this.selectedAlternative = selectedAlternative;
     }
 
     public LibraryItem getSelectedDocument() {
         return selectedDocument;
     }
 
     public void setSelectedDocument(LibraryItem selectedDocument) {
         this.selectedDocument = selectedDocument;
     }
     
     public void addDocument() {
         if(selectedDocument != null && selected != null) {
             service.addDocument(selected, selectedDocument);
             //newVideo = null;
         }
     }
     
     public LibraryItem getDocument() {
 //        if(newVideo == null)
 //            newVideo = new Video();
         return selectedDocument;
     }
     
     public String editDocument() {
         if(selectedDocument != null) {
             service.editDocument(selected, selectedDocument);
         }
         return "editDocument?faces-redirect=true";
     }
     
     public String removeDocument(LibraryItem document) {
         if(selected != null) {
             service.removeDocument(selected, document);
         }
         return "editCourse?faces-redirect=true";
     }
     
     
     // Video stuff
     private DualListModel<Video> videos;
     @Inject VideoService videoService;
 
     public DualListModel<Video> getVideos() {
         if(videos == null) {
             List<Video> source = videoService.findVideos();
             List<Video> target = new ArrayList<Video>();
             if(selected.getVideos() != null) {
                 target = selected.getVideos();
                 
                 for(int i = 0; i < target.size(); i++) {
                     Video v = target.get(i);
                     System.out.println("Checking Video " + v.getVideoName());
                     for(int k = 0; k < source.size(); k++) {
                         Video vv = source.get(k);
                         System.out.println("Comparing " + v.getVideoName() + " to " + vv.getVideoName());
                         if(vv.getId().equals(v.getId())) {
                             System.out.println("It's the same!");
                             source.remove(vv);
                             break;
                         }
                     }
                 }
             }
             
             videos = new DualListModel<Video>(source, target);
         }
         
         return videos;
     }
     
 
 //    public DualListModel<Video> getVideos2() {
 //        DualListModel<Video> result = new DualListModel<Video>(videoService.findVideos(),new ArrayList<Video>());
 //        System.out.println("Result is size " + result.getSource().size());
 //        return result;
 //    }
     
     public void setVideos(DualListModel<Video> videos) {
         this.videos = videos;
     }
     
     public void addVideos(List<Video> v) {
         selected = service.setVideos(selected, videos.getTarget());
         /*for(Video vv : v) {
             service.addVideo(selected, vv);
             //selected.addVideo(vv);
             //vv.addCourse(selected);
         }*/
     }
     
     public void setVideos(List<Video> v) {
         selected.setVideos(v);
     }
     
     // Document stuff
     private DualListModel<LibraryItem> documents;
     @Inject LibraryService documentService;
     
     public DualListModel<LibraryItem> getDocuments() {
         if(documents == null) {
             List<LibraryItem> source = documentService.getLibrary();
             List<LibraryItem> target = new ArrayList<LibraryItem>();
             if(selected.getVideos() != null) {
                 target = selected.getDocuments();
                 
                 for(int i = 0; i < target.size(); i++) {
                     LibraryItem v = target.get(i);
                     for(int k = 0; k < source.size(); k++) {
                         LibraryItem vv = source.get(k);
                         if(vv.getId().equals(v.getId())) {
                             source.remove(vv);
                             break;
                         }
                     }
                 }
             }
             
             documents = new DualListModel<LibraryItem>(source, target);
         }
         
         return documents;
     }
     
     public void setDocuments(DualListModel<LibraryItem> document) {
         this.documents = document;
     }
     
     public void addDocuments(List<LibraryItem> v) {
         selected = service.setDocuments(selected, documents.getTarget());
     }
     
     // Quiz stuff
     private DualListModel<Quiz> quizzes;
     @Inject QuizService quizService;
     
     public DualListModel<Quiz> getQuizzes() {
         if(quizzes == null) {
             List<Quiz> source = quizService.findQuizzes();
             List<Quiz> target = new ArrayList<Quiz>();
             if(selected.getQuizzes() != null) {
                 target = selected.getQuizzes();
                 
                 for(int i = 0; i < target.size(); i++) {
                     Quiz v = target.get(i);
                     for(int k = 0; k < source.size(); k++) {
                         Quiz vv = source.get(k);
                         if(vv.getId() == v.getId()) {
                             source.remove(vv);
                             break;
                         }
                     }
                 }
             }
             
             quizzes = new DualListModel<Quiz>(source, target);
         }
         
         return quizzes;
     }
     
     public void setQuizzes(DualListModel<Quiz> quizzes) {
         this.quizzes = quizzes;
     }
     
     public void addQuizzes(List<Quiz> q) {
         selected = service.setQuizzes(selected, quizzes.getTarget());
     }
     
     // Article stuff
     @Inject ArticleService articleService;
     List<Article> articles;
     
     public List<Article> getArticles() {
         articles = articleService.findArticles();
         return articles;
     }
 
     public Article getInformation() {
         return selected.getInfo();
     }
 
     public void setInformation(Article information) {
         selected.setInfo(information);
     }
 
     public Article getDates() {
         return selected.getDates();
     }
 
     public void setDates(Article dates) {
         selected.setDates(dates);
     }
 
     public Article getHelp() {
         return selected.getHelp();
     }
 
     public void setHelp(Article help) {
         selected.setHelp(help);
     }
 }
