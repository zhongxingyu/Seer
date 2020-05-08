 package no.niths.application.rest;
 
 import static org.junit.Assert.*;
 import no.niths.application.rest.interfaces.CourseController;
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.Course;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.annotation.Rollback;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { TestAppConfig.class, HibernateConfig.class })
 public class CourseControllerTest {
 
     @Autowired
     private CourseController controller;
 
     @Test
     @Rollback(true)
     public void testGetCourse() {
         final Course firstCourse = new Course("foo", "bar");
 
         controller.create(firstCourse);
        final Course secondCourse = controller.getAll(firstCourse).get(1);
 
         assertEquals(firstCourse.getName(), secondCourse.getName());
     }
 }
