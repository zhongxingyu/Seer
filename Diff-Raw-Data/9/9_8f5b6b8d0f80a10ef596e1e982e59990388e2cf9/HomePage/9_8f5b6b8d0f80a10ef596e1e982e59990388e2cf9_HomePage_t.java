 package ua.dp.primat.curriculum.view;
 
 import java.util.List;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import ua.dp.primat.domain.StudentGroup;
 import ua.dp.primat.domain.workload.Workload;
 import ua.dp.primat.repositories.WorkloadRepository;
 import ua.dp.primat.utils.view.AbstractChoosePanel;
 
 public class HomePage extends WebPage {
 
     private static final long serialVersionUID = 1L;
     @SpringBean
     private WorkloadRepository workloadRepository;
     private ListView<Workload> workloadsView;
     private List<Workload> workloads;
 
     public HomePage() {
         super();
 
         final AbstractChoosePanel choosePanel = new AbstractChoosePanel("choosePanel") {
 
             @Override
             protected void executeAction(StudentGroup studentGroup, Long semester) {
                 workloads = workloadRepository.getWorkloadsByGroupAndSemester(studentGroup, semester);
                 if (workloadsView != null) {
                     workloadsView.setList(workloads);
                 }
             }
         };
         add(choosePanel);
 
         workloadsView = new WorkloadsListView("disciplineRow", workloads);
         add(workloadsView);
     }
 
     private static class CourseImage extends Image {
 
         public CourseImage(String id, boolean isCourse) {
             super(id);
             this.isCourse = isCourse;
         }
 
         @Override
         public boolean isVisible() {
             return isCourse;
         }
 
         private boolean isCourse;

        private static final long serialVersionUID = 3L;
        
     }
 
     private static class WorkloadsListView extends ListView<Workload> {
 
         public WorkloadsListView(String string, List<Workload> list) {
             super(string, list);
         }
 
         @Override
         protected void populateItem(ListItem<Workload> li) {
             final Workload workload = li.getModelObject();
             li.add(new Label("disciplineName", workload.getDiscipline().getName()));
             li.add(new Label("cathedra", workload.getDiscipline().getCathedra().getName()));
             li.add(new Label("lection", workload.getLectionHours().toString()));
             li.add(new Label("practice", workload.getPracticeHours().toString()));
             li.add(new Label("labs", workload.getLaboratoryHours().toString()));
             li.add(new Label("selfwork", workload.getSelfworkHours().toString()));
             li.add(new CourseImage("course", workload.getCourseWork()));
             li.add(new Label("finalcontrol", workload.getFinalControlType().toString()));
         }
 
         private static final long serialVersionUID = 2L;
     }
 }
