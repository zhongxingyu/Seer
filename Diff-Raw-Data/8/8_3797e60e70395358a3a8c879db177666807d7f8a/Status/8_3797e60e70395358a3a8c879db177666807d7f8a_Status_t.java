 package br.com.developer.redu.models;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA. User: igor Date: 8/31/12 Time: 11:56 AM To change
  * this template use File | Settings | File Templates.
  */
 public class Status implements Serializable {
 
 	private static final long serialVersionUID = -7806882147919355625L;
 
 	public final static String LOGEABLE_TYPE_LECTURE = "Lecture";
 	public final static String LOGEABLE_TYPE_SUBJECT = "Subject";
 	public final static String LOGEABLE_TYPE_COURSE = "Course";
 	public final static String LOGEABLE_TYPE_ENROLLMENT = "Enrollment";
 	public final static String LOGEABLE_TYPE_FRIENDSHIP = "Friendship";
 
 	public final static String TYPE_ACTIVITY = "Activity";
 	public final static String TYPE_ANSWER = "Answer";
 	public final static String TYPE_HELP = "Help";
 	public final static String TYPE_LOG = "Log";
 
 	public String id;
 	public String type;
 	public String logeable_type;
 	public String created_at;
 	public String text;
 	public int answers_count;
 	public User user;
 	public List<Link> links;
 
 	public long createdAtInMillis;
 	public long lastSeenAtInMillis;
 	public boolean lectureAreadySeen;
 	public boolean lastSeen;
 
 	private Link getLink(String rel) {
 		Link link = null;
 		for (Link l : links) {
 			if (l.rel.equals(rel)) {
 				link = l;
 				break;
 			}
 		}
 		return link;
 	}
 
 	private String getLinkName(String rel) {
 		Link link = getLink(rel);
 		if (link == null) {
 			return null;
 		}
 		return link.name;
 	}
 
 	private String getLinkHref(String rel) {
 		Link link = getLink(rel);
 		if (link == null) {
 			return null;
 		}
 		return link.href;
 	}
 
 	public String getEnvironmentName() {
 		return getLinkName(Link.REL_ENVIRONMENT);
 	}
 
 	public String getCourseName() {
 		return getLinkName(Link.REL_COURSE);
 	}
 
 	public String getSpaceName() {
 		return getLinkName(Link.REL_SPACE);
 	}
 
 	public String getSubjectName() {
 		return getLinkName(Link.REL_SUBJECT);
 	}
 
 	public String getLectureName() {
 		return getLinkName(Link.REL_LECTURE);
 	}
 
 	public String getEnvironmentPath() {
 		String href = getLinkHref(Link.REL_ENVIRONMENT);
 		if (href == null) {
 			return null;
 		}
 
 		String[] hrefSplitted = href.split("/");
 		return hrefSplitted[hrefSplitted.length - 1];
 	}
 
 	public String getCourseId() {
 		String href = getLinkHref(Link.REL_COURSE);
 		if (href == null) {
 			return null;
 		}
 
 		String[] hrefSplitted = href.split("/");
 		return hrefSplitted[hrefSplitted.length - 1];
 	}
 
 	public String getSpaceId() {
 		String href = getLinkHref(Link.REL_SPACE);
 		if (href == null) {
 			return null;
 		}
 
 		String[] hrefSplitted = href.split("/");
 		return hrefSplitted[hrefSplitted.length - 1];
 	}
 
 	public String getSubjectId() {
 		String href = getLinkHref(Link.REL_SUBJECT);
 		if (href == null) {
 			return null;
 		}
 
 		String[] hrefSplitted = href.split("/");
 		return hrefSplitted[hrefSplitted.length - 1];
 	}
 
 	public String getLectureId() {
 		String href = getLinkHref(Link.REL_LECTURE);
 		if (href == null) {
 			return null;
 		}
 
 		String[] hrefSplitted = href.split("/");
 		return hrefSplitted[hrefSplitted.length - 1].split("-")[0];
 	}
 
 	/**
 	 * A link example: http://www.redu.com.br/api/statuses/107070/answers
 	 * 
 	 * @return the id of the in response to the status
 	 */
 	public String getInResponseToStatusId() {
 		String id = null;
 
 		if (isAnswerType()) {
 			String href = getLinkHref(Link.REL_IN_RESPONSE_TO);
 			String[] splitted = href.split("/");
 			id = splitted[splitted.length - 2];
 		}
 
 		return id;
 	}
 
 	public Statusable getStatusable() {
 		Statusable statusable = new Statusable();
 
 		String href = getLinkHref(Link.REL_STATUSABLE);
 
 		if (href == null) {
 			return null;
 		}
 
 		String[] linkSplitted = href.split("/");
 
 		statusable.type = linkSplitted[linkSplitted.length - 2];
 
 		if (statusable.isTypeUser()) {
 			statusable.name = linkSplitted[linkSplitted.length - 1];
 
 		} else if (statusable.isTypeLecture()) {
 			String idAndName = linkSplitted[linkSplitted.length - 1];
 			statusable.id = idAndName.substring(0, idAndName.indexOf('-'));
 			statusable.name = idAndName.substring(idAndName.indexOf('-') + 1)
 					.replaceAll("-", " ");
 
 		} else if (statusable.isTypeSpace()) {
 			statusable.name = linkSplitted[linkSplitted.length - 1];
 		}
 
 		return statusable;
 	}
 
 	// --- type
 	public boolean isActivityType() {
 		return TYPE_ACTIVITY.equals(type);
 	}
 
 	public boolean isAnswerType() {
 		return TYPE_ANSWER.equals(type);
 	}
 
 	public boolean isHelpType() {
 		return TYPE_HELP.equals(type);
 	}
 
 	public boolean isLogType() {
 		return TYPE_LOG.equals(type);
 	}
 
 	// --- logeable_type
 	public boolean isLectureLogeableType() {
 		return LOGEABLE_TYPE_LECTURE.equals(logeable_type);
 	}
 
 	public boolean isSubjectLogeableType() {
 		return LOGEABLE_TYPE_SUBJECT.equals(logeable_type);
 	}
 
 	public boolean isCourseLogeableType() {
 		return LOGEABLE_TYPE_COURSE.equals(logeable_type);
 	}
 
 	public boolean isEnrollmentLogeableType() {
 		return LOGEABLE_TYPE_ENROLLMENT.equals(logeable_type);
 	}
 
 	public boolean isFriendshipLogeableType() {
 		return LOGEABLE_TYPE_FRIENDSHIP.equals(logeable_type);
 	}
 
 	// --- origin wall
 	public boolean isPostedOnLectureWall() {
 		return (getLectureName() != null);
 	}
 
 	public boolean isPostedOnSpaceWall() {
 		return (getLectureName() == null && getSpaceName() != null);
 	}
 
 	public boolean isPostedOnUserWall() {
 		return (getLectureName() == null && getSpaceName() == null);
 	}
 
 	// --- breadcrumbs
 	public List<String> getBreadcrumbs() {
		List<String> crumbs = new ArrayList<String>();
 
 		String environment = getEnvironmentName();
 		if (environment != null) {
 			crumbs.add(environment);
 
 			String course = getCourseName();
 			if (course != null) {
 				crumbs.add(course);
 
 				String space = getSpaceName();
 				if (space != null) {
 					crumbs.add(space);
 
 					String subject = getSubjectName();
 					if (subject != null) {
 						crumbs.add(subject);
 
 						String lecture = getLectureName();
 						if (lecture != null) {
 							crumbs.add(lecture);
 						}
 					}
 				}
 			}
 		}
 
 		return crumbs;
 	}
 
 	public String getLastBreadcrumb() {
 		List<String> crumbs = getBreadcrumbs();
		if(crumbs.size() == 0) {
			return null;
		}
 		return crumbs.get(crumbs.size() - 1);
 	}
 
 	@Override
 	public String toString() {
 		return String
 				.format("id: %s\ntype: %s\nlogeable_type: %s\ncreated_at: %s\ntext: %s\nuser: %s\nlectureAreadySeen: %s\nlastSeen: %s\nlinks: %s",
 						id, type, logeable_type, created_at, text, user,
 						lectureAreadySeen, lastSeen, links);
 	}
 
 }
