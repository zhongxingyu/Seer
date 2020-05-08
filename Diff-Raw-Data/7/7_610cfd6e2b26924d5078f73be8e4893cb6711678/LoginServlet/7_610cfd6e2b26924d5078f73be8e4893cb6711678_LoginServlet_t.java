 package com.tongji.j2ee.sp;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import model.Courses;
 import model.CoursesDAO;
 import model.StudentCourse;
 import model.StudentCourseDAO;
 import model.Studentinfo;
 import model.StudentinfoDAO;
 import model.Teacherinfo;
 import model.TeacherinfoDAO;
 
 public class LoginServlet extends HttpServlet {
 
 	// when to initialize the DAO is a problem
 	private StudentinfoDAO studentinfoDAO = new StudentinfoDAO();
 	private Studentinfo studentinfo;
 	private TeacherinfoDAO teacherinfoDAO = new TeacherinfoDAO();
 	private Teacherinfo teacherinfo;
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 
 		HttpSession hs = request.getSession();
 
 		String id = request.getParameter("userID");
 		String password = request.getParameter("password");
 
 		// for student_id
 		if (id.length() == 6) {
 			studentinfo = studentinfoDAO.findById(id);
 			if (studentinfo == null) {
 				request.setAttribute("errorInformation", "用户名不存在");
 				request.getRequestDispatcher("index.jsp").forward(request,
 						response);
 			} else {
 				if (password.equals(studentinfo.getPassword())) {
 					hs.setAttribute("user", studentinfo);
 					if (studentinfo.getEmail().equals("")) {
 						// hs.setAttribute("user", studentinfo);
 						request.getRequestDispatcher("register.jsp").forward(
 								request, response);
 					} else {
 						// find the course_id in the relation table
 						StudentCourseDAO studentCourseDAO = new StudentCourseDAO();
 						List<StudentCourse> myCourses = studentCourseDAO
 								.findBySId(studentinfo.getSId());
 
 						// find the courses in the courses table
 						CoursesDAO coursesDAO = new CoursesDAO();
 						List myCoursesInfo = (List) new java.util.ArrayList();
 						for (int i = 0; i < myCourses.size(); i++) {
 							Courses temp = coursesDAO.findById(myCourses.get(i)
 									.getCourseId());
 							myCoursesInfo.add(temp);
 						}
 
 						// to implement the course schedule
 						Courses emptyCourse = new Courses();
 						emptyCourse.setName("");
 						ArrayList<Courses> courseSchedule = new ArrayList();
 						for (int i = 0; i < 35; ++i) {
                             Courses temp = findCourseByTime(i + 1, myCourses, coursesDAO);
                             if(temp != null){
                             	courseSchedule.add(temp);
                             }else{
                             	courseSchedule.add(emptyCourse);
                             }
 						}
 						
 						//TODO:可以考虑放到session里
 						//hs.setAttribute("myCourses", myCoursesInfo);
 						//hs.setAttribute("schedule", courseSchedule);
 						request.setAttribute("myCourses", myCoursesInfo);
 						request.setAttribute("schedule", courseSchedule);
 						request.getRequestDispatcher("student.jsp").forward(
 								request, response);
 					}
 
 				} else {
 					request.setAttribute("errorInformation", "密码错误");
 					request.getRequestDispatcher("index.jsp").forward(request,
 							response);
 				}
 			}
 		}
 
 		// for teacher_id
 		if (id.length() == 10) {
 			teacherinfo = teacherinfoDAO.findById(id);
 			if (teacherinfo == null) {
 				request.setAttribute("errorInformation", "用户名不存在");
 				request.getRequestDispatcher("index.jsp").forward(request,
 						response);
 				return;
 			} else {
				
 				if (password.equals(teacherinfo.getPassword())) {
 					hs.setAttribute("user", teacherinfo);
					if(teacherinfo.getTId().equals("0000000007")){
						request.getRequestDispatcher("admin.jsp").forward(
								request, response);
						return;
					}
 					if (teacherinfo.getEmail().equals("")) {
 						request.getRequestDispatcher("register.jsp").forward(
 								request, response);
 					} else {
 						request.getRequestDispatcher("teacher.jsp").forward(
 								request, response);
 					}
 
 				} else {
 
 					request.setAttribute("errorInformation", "密码错误");
 					request.getRequestDispatcher("index.jsp").forward(request,
 							response);
 				}
 			}
 		}
 
 		// TODO:for admin_id
 	}
 
 	private Courses findCourseByTime(int iTime,
 			List<StudentCourse> mycourses, CoursesDAO coursesDAO) {
 		for (int i = 0; i < mycourses.size(); i++) {
 			Courses temp = coursesDAO.findById(mycourses.get(i).getCourseId());
 			if (((temp.getSchedule0() - 1)%35 + 1) == iTime || ((temp.getSchedule1() - 1)%35 + 1) == iTime
 					|| ((temp.getSchedule2() - 1)%35 + 1) == iTime
 					|| ((temp.getSchedule3() - 1)%35 + 1) == iTime) {
                 return temp;
 			}
 			// myCoursesInfo.add(temp);
 		}
 		return null;
 	}
 }
