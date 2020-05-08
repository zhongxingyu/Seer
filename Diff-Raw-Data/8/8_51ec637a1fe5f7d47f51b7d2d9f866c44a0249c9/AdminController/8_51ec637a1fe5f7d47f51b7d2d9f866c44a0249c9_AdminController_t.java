 package elw.web;
 
 import elw.dao.CourseDao;
 import elw.vo.*;
 import org.akraievoy.gear.G4Parse;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Arrays;
 import java.util.HashMap;
 
 public class AdminController extends MultiActionController {
 	private static final Logger log = LoggerFactory.getLogger(AdminController.class);
 
 	protected final CourseDao courseDao;
 	protected final ObjectMapper mapper = new ObjectMapper();
 
 	public AdminController(CourseDao courseDao) {
 		this.courseDao = courseDao;
 	}
 
 	public ModelAndView do_courses(final HttpServletRequest req, final HttpServletResponse resp) {
 		resp.setCharacterEncoding("UTF-8");
 		resp.setContentType("text/html; charset=UTF-8");
 
 		final HashMap<String, Object> model = new HashMap<String, Object>();
 
 		model.put("courses", courseDao.findAllCourses());
 
 		return new ModelAndView("a/courses", model);
 	}
 
 	public ModelAndView do_course(final HttpServletRequest req, final HttpServletResponse resp) {
 		final Course course = courseDao.findCourse(req.getParameter("id"));
 		if (course == null) {
 			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 
 		resp.setCharacterEncoding("UTF-8");
 		resp.setContentType("text/html; charset=UTF-8");
 
 		final HashMap<String, Object> model = new HashMap<String, Object>();
 
 		model.put("course", course);
 
 		return new ModelAndView("a/course", model);
 	}
 
 	public ModelAndView do_launch(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
 		final String path = req.getParameter("path");
 		final String[] ids = path.split("--");
 		if (ids.length != 4) {
 			log.warn("malformed path {}", Arrays.toString(ids));
 
 			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 
 		final String courseId = ids[0];
 		final Course course = courseDao.findCourse(courseId);
 		if (course == null) {
 			log.warn("course not found by id {}", courseId);
 			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 
 		final int assBundleIndex = G4Parse.parse(ids[1], -1);
 		if (assBundleIndex < 0 || course.getAssBundles().length <= assBundleIndex) {
 			log.warn("bundle not found by index {}", assBundleIndex);
 			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 
 		final String assId = ids[2];
 		final AssignmentBundle bundle = course.getAssBundles()[assBundleIndex];
 		final Assignment ass = findIdName(bundle.getAssignments(), assId);
 		if (ass == null) {
 			log.warn("assignment not found by id {}", assId);
 			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 
 		final String verId = ids[3];
 		final Version ver = findIdName(ass.getVersions(), verId);
 		if (ver == null) {
 			log.warn("assignment not found by id {}", verId);
 			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 
 		resp.setCharacterEncoding("UTF-8");
 		resp.setContentType("text/html; charset=UTF-8");
 
 		final HashMap<String, Object> model = new HashMap<String, Object>();
 
 		final StringWriter verSw = new StringWriter();
 		mapper.writeValue(verSw, ver);
 
		final Version verNoSolution = mapper.readValue(verSw.toString(), Version.class);
		verNoSolution.setSolution(new String[] {"#  your code","#    goes here", "#      :)"});

		final StringWriter verNsSw = new StringWriter();
		mapper.writeValue(verNsSw, verNoSolution);

		model.put("ver", verNsSw.toString().replaceAll("&", "&amp;").replaceAll("\"", "&quot;"));
 
 		return new ModelAndView("a/launch", model);
 	}
 
 	protected static <E extends IdName> E findIdName(final E[] elems, String id) {
 		E found = null;
 		for (E e : elems) {
 			if (id.equals(e.getId())) {
 				found = e;
 				break;
 			}
 		}
 
 		return found;
 	}
 }
