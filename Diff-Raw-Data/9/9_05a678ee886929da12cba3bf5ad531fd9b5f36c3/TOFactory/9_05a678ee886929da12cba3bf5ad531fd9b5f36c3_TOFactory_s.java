 package kornell.core.shared.to;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.autobean.shared.AutoBeanFactory.Category;
 
 public interface TOFactory  extends AutoBeanFactory {
 	AutoBean<CourseTO> newCourseTO();
 	AutoBean<CoursesTO> newCoursesTO();
 	AutoBean<UserInfoTO> newUserInfoTO();
 	AutoBean<RegistrationsTO> newRegistrationsTO();
 }
