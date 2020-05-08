 package org.sukrupa.student;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.Matchers.hasItems;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class StudentControllerTest {
 
     @Mock
     private StudentRepository repository;
 
     private StudentController controller;
 
    private Map<String, List<Student>> model = new HashMap<String, List<Student>>();
     private Student sahil = new Student("Sahil");
     private Student pat = new Student("Pat");
 
     @Before
     public void setUp() throws Exception {
         initMocks(this);
         controller = new StudentController(repository);
     }
 
     @Test
     public void shouldPopulateModelWithAllStudents() {
         when(repository.findAll()).thenReturn(asList(sahil, pat));
         controller.all(model);
         assertThat(model.get("students"), hasItems(sahil, pat));
     }
 
     @Test
     public void shouldPickStudentViewForDisplayingAllStudents() {
         assertThat(controller.all(model), is("students"));
     }
 
 }
