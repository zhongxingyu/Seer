 package classviewer.model;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import classviewer.Settings;
 import classviewer.filters.CategoryCourseFilter;
 import classviewer.filters.CourseFilter;
 import classviewer.filters.LanguageCourseFilter;
 import classviewer.filters.StatusCourseFilter;
 import classviewer.filters.UniversityCourseFilter;
 
 /**
  * Main model file. Contains a bunch of hashes for universities, languages,
  * courses, offerings, etc.
  * 
  * @author TK
  */
 public class CourseModel {
 	private HashMap<String, DescRec> categories = new HashMap<String, DescRec>();
 	private HashMap<String, DescRec> universities = new HashMap<String, DescRec>();
 	private HashMap<Integer, CourseRec> courses = new HashMap<Integer, CourseRec>();
 	private ArrayList<CourseFilter> filters = new ArrayList<CourseFilter>();
 	private ArrayList<CourseModelListener> listeners = new ArrayList<CourseModelListener>();
 	/** Cached list of courses after filters have been applied */
 	private ArrayList<CourseRec> filteredCourses = new ArrayList<CourseRec>();
 	private Settings settings;
 	/** Smallest (most negative) id ever assigned. */
 	private int smallestId = 0;
 
 	public CourseModel(Settings settings) {
 		this.settings = settings;
 		filters.add(new StatusCourseFilter(this));
 		filters.add(new CategoryCourseFilter(this));
 		filters.add(new UniversityCourseFilter(this));
 		filters.add(new LanguageCourseFilter(this));
 	}
 
 	public void addCategory(DescRec desc) {
 		if (desc == null)
 			return;
 		if (categories.containsKey(desc.getId()))
 			System.err.println("Ignoring duplicate category key "
 					+ desc.getId());
 		categories.put(desc.getId(), desc);
 	}
 
 	public void addUniversity(DescRec desc) {
 		if (desc == null)
 			return;
 		if (universities.containsKey(desc.getId()))
 			System.err.println("Ignoring duplicate university key "
 					+ desc.getId());
 		universities.put(desc.getId(), desc);
 	}
 
 	public DescRec removeUniversity(String id) {
 		return universities.remove(id);
 	}
 
 	public void addCourse(CourseRec course) {
 		if (courses.containsKey(course.getId()))
 			System.err
 					.println("Ignoring duplicate course id " + course.getId());
 		courses.put(course.getId(), course);
 	}
 
 	public CourseRec getCourse(Integer id) {
 		return courses.get(id);
 	}
 
 	public CourseRec removeCourse(Integer id) {
 		return courses.remove(id);
 	}
 
 	public DescRec getCategory(String id) {
 		return categories.get(id);
 	}
 
 	public DescRec removeCategory(String id) {
 		return categories.remove(id);
 	}
 
 	public DescRec getUniversity(String id) {
 		return universities.get(id);
 	}
 
 	public Collection<DescRec> getCategories() {
 		return categories.values();
 	}
 
 	public Collection<DescRec> getUniversities() {
 		return universities.values();
 	}
 
 	public Collection<CourseRec> getCourses() {
 		return courses.values();
 	}
 
 	public ArrayList<CourseFilter> getFilters() {
 		return filters;
 	}
 
 	public void fireModelReloaded() {
 		this.smallestId = getSmallestId();
 		applyCourseFilters();
 		for (CourseModelListener lnr : listeners)
 			lnr.modelUpdated();
 	}
 
 	public void fireFiltersChanged(CourseFilter filter) {
 		applyCourseFilters();
 		for (CourseModelListener lnr : listeners)
 			lnr.filtersUpdated();
 	}
 
 	/** Course or one of it's offerings changed status */
 	public void fireCourseStatusChanged(CourseRec course) {
 		applyCourseFilters();
 		for (CourseModelListener lnr : listeners)
 			lnr.courseStatusChanged(course);
 	}
 
 	public void addListener(CourseModelListener lnr) {
 		this.listeners.add(lnr);
 	}
 
 	public void removeListener(CourseModelListener lnr) {
 		this.listeners.remove(lnr);
 	}
 
 	private void applyCourseFilters() {
 		filteredCourses.clear();
 		for (CourseRec rec : courses.values()) {
 			boolean passed = true;
 			for (int i = 0; i < filters.size() && passed; i++)
 				passed = filters.get(i).accept(rec);
 			if (passed)
 				filteredCourses.add(rec);
 		}
 	}
 
 	public ArrayList<CourseRec> getFilteredCourses() {
 		return filteredCourses;
 	}
 
 	public CourseRec getClassById(int id) {
 		return courses.get(id);
 	}
 
 	/**
 	 * Locate the class by short name AKA string id. This is used, for example,
 	 * when EdX data is imported.
 	 */
 	public CourseRec getClassByShortName(String courseId) {
 		for (CourseRec cr : courses.values()) {
 			if (courseId.equals(cr.getShortName()))
 				return cr;
 		}
 		return null;
 	}
 
 	public CourseRec getClassByLongNameAndUni(String name, String uni) {
 		for (CourseRec cr : courses.values()) {
			if (name.equals(cr.getName())) {
				for (DescRec dr : cr.getUniversities())
					if (uni.equals(dr.getId()))
						return cr;
			}
 		}
 		return null;
 	}
 
 	public HashSet<String> getLanguages() {
 		HashSet<String> res = new HashSet<String>();
 		for (CourseRec r : courses.values())
 			res.add(r.getLanguage());
 		// Clean up possible NULL and ""?
 		return res;
 	}
 
 	public void saveStatusFile() throws IOException {
 		File file = settings.getStatusFile();
 		FileWriter writer = new FileWriter(file);
 		StatusFileModelAdapter adap = new StatusFileModelAdapter();
 		ArrayList<CourseRec> all = new ArrayList<CourseRec>(courses.values());
 		Collections.sort(all, new CourseById());
 		adap.saveStatuses(writer, all);
 		writer.close();
 	}
 
 	public void saveModelFile() throws IOException {
 		File file = settings.getStaticFile();
 		FileOutputStream output = new FileOutputStream(file);
 		XmlModelAdapter xml = new XmlModelAdapter();
 		xml.writeModel(output, this);
 		output.close();
 	}
 
 	/**
 	 * Get the smallest int value mentioned in class or offering ids. Assume
 	 * this method will not be called often.
 	 */
 	private int getSmallestId() {
 		int value = 0; // default
 		for (CourseRec cr : courses.values()) {
 			value = Math.min(value, cr.getId());
 			for (OffRec or : cr.getOfferings())
 				value = Math.min(value, or.getId());
 		}
 		return value;
 	}
 
 	/** Generate a brand new negative ID for use for EdX and such */
 	public int getNewNegativeId() {
 		this.smallestId--;
 		return this.smallestId;
 	}
 
 	private class CourseById implements Comparator<CourseRec> {
 		@Override
 		public int compare(CourseRec o1, CourseRec o2) {
 			return Integer.compare(o1.getId(), o2.getId());
 		}
 	}
 
 	public Settings getSettings() {
 		return this.settings;
 	}
 }
