 package kornell.core.shared.to;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanFactory;
 
 public interface TOFactory  extends AutoBeanFactory {
 	AutoBean<CourseTO> newCourseTO();
 	AutoBean<CoursesTO> newCoursesTO();
 	AutoBean<UserInfoTO> newUserInfoTO();
 	AutoBean<RegistrationsTO> newRegistrationsTO();
 }
