 package org.thoughtworks.orc.internal.util;
 
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
 import org.thoughtworks.orc.test.model.Book;
 import org.thoughtworks.orc.test.model.Student;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Map;
 
 import static com.google.common.collect.Maps.newHashMap;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class FormPickerTest {
     Map<String, String[]> form;
     HttpServletRequest request;
 
    @BeforeMethod
     public void setUp() throws Exception {
         form = newHashMap();
         request = mock(HttpServletRequest.class);
         when(request.getParameterMap()).thenReturn(form);
     }
 
     private void put2Form(String key, String value) {
         form.put(key, new String[]{value});
     }
 
     @Test
     public void test_create_obj_from_map() throws Exception {
         form.put("age", new String[]{"1"});
         final Student student = FormPicker.pickupObject(request, Student.class);
         assertThat(student.getAge(), is(1));
         assertNull(student.getBooks());
         assertNull(student.getGrade());
     }
 
     @Test
     public void test_create_Grade() throws Exception {
         put2Form("grade.number", "3");
         final Student student = FormPicker.pickupObject(request, Student.class);
         assertNotNull(student.getGrade());
         assertThat(student.getGrade().getNumber(), is(3));
         assertNull(student.getBooks());
     }
 
     @Test
     public void test_get_generic_type() throws Exception {
         final Class<?> booksType = FormPicker.getGenericType(Student.class, "books");
         assertEquals(booksType, Book.class);
     }
 
     @Test
     public void test_create_List() throws Exception {
         put2Form("books[0].name", "book1");
         put2Form("books[3].name", "book2");
         put2Form("books[2].name", "book3");
         final Student student = FormPicker.pickupObject(request, Student.class);
         assertNotNull(student.getBooks());
         assertThat(student.getBooks().size(), is(3));
         assertNotNull(student.getBooks().stream().filter(b -> b.getName().equals("book1")));
         assertNotNull(student.getBooks().stream().filter(b -> b.getName().equals("book2")));
         assertNotNull(student.getBooks().stream().filter(b -> b.getName().equals("book3")));
     }
 }
