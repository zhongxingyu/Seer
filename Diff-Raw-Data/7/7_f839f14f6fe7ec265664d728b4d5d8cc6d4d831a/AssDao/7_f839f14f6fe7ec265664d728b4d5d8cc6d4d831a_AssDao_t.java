 package elw.dao;
 
 import elw.vo.Assignment;
 
 /**
  * Path: course.id/assType.id/ass.id
  */
 public class AssDao extends Dao<Assignment> {
 	@Override
 	public Path pathFromMeta(Assignment assignment) {
 		return new Path(assignment.getPath());
 	}
 
 	protected String[] findIdsForAssType(final String courseId, final String assTypeId) {
 		final Path pathAllForAssType = new Path(new String[]{courseId, assTypeId, null});
 		final String[][] pathElems = listCriteria(pathAllForAssType, null, false, true, false, null);
 
 		return pathElems[2];
 	}
 
 	public Assignment[] findAllForAssType(final String courseId, final String assTypeId) {
 		final String[] assIds = findIdsForAssType(courseId, assTypeId);

 		return load(courseId, assTypeId, assIds);
 	}
 
 	protected Assignment[] load(final String courseId, final String assTypeId, String[] assIds) {
 		final Assignment[] assignments = new Assignment[assIds.length];
 
 		for (int i = 0; i < assignments.length; i++) {
 			assignments[i] = findAssignment(courseId, assTypeId, assIds[i]);
 		}
 
 		return assignments;
 	}
 
 	public Assignment findAssignment(final String courseId, final String assTypeId, final String assId) {
 		if (assId == null) {
 			return null;
 		}
 
 		final Entry<Assignment> entry = findLast(new Path(new String[] {courseId, assTypeId, assId}), null, null);
 
		return entry != null ? entry.getMeta() : null;
 	}
 }
