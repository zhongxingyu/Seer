 package repository;
 
 /**
  * @author Xuesong Meng&Yidu Liang
  * @author Aniket Hajirnis
  * @author Joanne Zhuo
  * @author Ying Gan
  * @author Siddhesh Jaiswal
  */
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import domain.Project;
 import domain.User;
 import domain.DiagramContext;
 
 public class ProjectDAO {
 	/**
      * Get Project by project name
      * @param projectName
      * @return project
      * @throws SQLException
      */
     public static Project getProject(String projectName) throws SQLException {
     	Project project = null;
     	Connection conn = null;
     	PreparedStatement pstmt = null;
     	ResultSet rs = null;
     	try {
     		conn = DbManager.getConnection();
     	    pstmt = conn.prepareStatement(
     		    "SELECT * FROM project where projectName = ? ;");
     	    pstmt.setString(1, projectName);
     	    rs = pstmt.executeQuery();
     	    if (rs.next()) {
     		project = new Project(rs.getInt("projectId"), rs.getString("projectName"),
     				rs.getString("description"), rs.getString("startDate"),
     				rs.getBoolean("enabled"),rs.getString("disabledDate"));
     	    }
     	    return project;
     	} catch (SQLException e) {
     	    System.out.println("Using default model.");
     	} finally {
     		if( rs != null) {rs.close();}
     		if( pstmt != null) {pstmt.close();}
     		if( conn != null) {conn.close();}
     	}
     	return project;
     }
     
     /**
      * Add a new project
      * @param project
      * @return true - successfully added. false - failed
      * @throws SQLException
      */
     public static boolean addProject(Project project) throws SQLException {
    	int defaultPolicyId = 2; // This needs to dynamic when multiple policy handling is implemented.
     	Connection conn = null;
     	PreparedStatement pstmt = null;
     	try {
     		conn = DbManager.getConnection();
     	    pstmt = conn.prepareStatement(
     	    		"INSERT into project(projectName, startDate, description, enabled) VALUES(?,NOW(),?,?);");
     	    pstmt.setString(1, project.getProjectName());
     	    pstmt.setString(2, project.getDescription());
     	    pstmt.setBoolean(3, project.getEnabled());
     	    if(pstmt.executeUpdate() != 0) {
 				project = ProjectDAO.getProject(project.getProjectName());
				DiagramContext dc = new DiagramContext(project.getProjectName()+"_DefaultContext","Please enter description here",defaultPolicyId,project.getProjectId());
    	    	ContextDAO.addContext(new DiagramContext(project.getProjectName()+"_DefaultContext","Please enter description here",defaultPolicyId,project.getProjectId()));  // Add 1 in global policy   	    	
     	    	return true;
     	    } else {
     	    	return false;
     	    }
     	} catch (SQLException e) {
     	    e.printStackTrace();
     	} finally {
     		if( pstmt != null) {pstmt.close();}
     		if( conn != null) {conn.close();}
     	}
     	return false;
     }
     
     public static boolean updateProject(Project project) throws SQLException {
     	Connection conn = null;
     	PreparedStatement pstmt = null;
     	try {
     		conn = DbManager.getConnection();
     		if (!project.getEnabled()) {
     			pstmt = conn.prepareStatement(
         	    		"UPDATE project SET projectName = ?, description = ?, enabled = ?, "
         	    		+ "disabledDate = NOW() WHERE projectId = ?;");
     		}
     		else {
     			pstmt = conn.prepareStatement(
         	    		"UPDATE project SET projectName = ?, description = ?, enabled = ?, "
         	    		+ "WHERE projectId = ?;");
     		} 	    
     	    pstmt.setString(1, project.getProjectName());
     	    pstmt.setString(2, project.getDescription());
     	    pstmt.setBoolean(3, project.getEnabled());
     	    pstmt.setInt(4, project.getProjectId());
     	    if(pstmt.executeUpdate() != 0) {
     	    	return true;
     	    } else {
     	    	return false;
     	    }
     	} catch (SQLException e) {
     	    e.printStackTrace();
     	} finally {
     		if( pstmt != null) {pstmt.close();}
     		if( conn != null) {conn.close();}
     	}
     	return false;
     }
     
     /**
      * Disable a project
      * @param projectName
      * @return true - successful. false - failed.
      * @throws SQLException
      * @author Indrajit Kulkarni
      */
     public static boolean disableProject(int projectId) throws SQLException {
     	Connection conn = null;
     	PreparedStatement pstmt = null;
     	try {
     		conn = DbManager.getConnection();
     	    pstmt = conn.prepareStatement(
     	    		"UPDATE project SET enabled = false ,disabledDate = NOW() WHERE projectId = ?;");
     	    pstmt.setInt(1, projectId);
     	    if(pstmt.executeUpdate() != 0) {
     	    	return true;
     	    } else {
     	    	return false;
     	    }
     	} catch (SQLException e) {
     	    e.printStackTrace();
     	} finally {
     		if( pstmt != null) {pstmt.close();}
     		if( conn != null) {conn.close();}
     	}
     	return false;
     }
     
     /**
      * Enable a project
      * @param projectName
      * @return true - successful. false - failed.
      * @throws SQLException
      * @author Indrajit Kulkarni
      */
     public static boolean enableProject(int projectId) throws SQLException {
     	Connection conn = null;
     	PreparedStatement pstmt = null;
     	try {
     		conn = DbManager.getConnection();
     	    pstmt = conn.prepareStatement(
     	    		"UPDATE project SET enabled = true WHERE projectId = ?;");
     	    pstmt.setInt(1, projectId);
     	    if(pstmt.executeUpdate() != 0) {
     	    	return true;
     	    } else {
     	    	return false;
     	    }
     	} catch (SQLException e) {
     	    e.printStackTrace();
     	} finally {
     		if( pstmt != null) {pstmt.close();}
     		if( conn != null) {conn.close();}
     	}
     	return false;
     }
     
     /**
      * Check if the project which has the same project name is existed.
      * @param projectName
      * @return true - existed. false - not existed.
      * @throws SQLException
      */
     public static boolean exists(String projectName) throws SQLException {
     	if(getProject(projectName) == null) {
     		return false;
     	}
     	else {
     		return true;
     	}
     }
     
     /**
      * Get all projects (enabled and disabled)
      * @return Projects list
      * @throws SQLException
      */
     public static ArrayList<Project> getAllProjects() throws SQLException {
     	ArrayList<Project> projects = new ArrayList<Project>();
     	Connection conn = null;
     	PreparedStatement pstmt = null;
     	ResultSet rs = null;
     	try {
     		conn = DbManager.getConnection();
     	    pstmt = conn.prepareStatement(
     		    "SELECT * FROM project;");
     	    rs = pstmt.executeQuery();
     	    while (rs.next()) {
     		Project project = new Project(rs.getInt("projectId"), rs.getString("projectName"),
     				rs.getString("description"), rs.getString("startDate"),
     				rs.getBoolean("enabled"),rs.getString("disabledDate"));
     		projects.add(project);
     	    }
     	    return projects;
     	} catch (SQLException e) {
     		e.printStackTrace();
     	} finally {
     		if( rs != null) {rs.close();}
     		if( pstmt != null) {pstmt.close();}
     		if( conn != null) {conn.close();}
     	}
     	return projects;
     }
     
     /**
      * Get all context of specific project
      * @param projectName
      * @return
      * @throws SQLException
      */
     public static ArrayList getDiagramContexts(String projectName) throws SQLException {
     	//To be implemented
     	return null;
     }
     public static ArrayList<User> getUsers(int projectId) throws SQLException {
     	ArrayList<User> users = new ArrayList<User>();
     	
     	Connection conn = null;
 		PreparedStatement pstmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DbManager.getConnection();
 			pstmt = conn.prepareStatement("SELECT user.userId, projectId, username, email, securityQ, securityA, userType" + 
 							" FROM userproject join user on userproject.userId = user.userId" +
 							" where projectId = ?;");
 			pstmt.setInt(1, projectId);
 			// Execute the SQL statement and store result into the ResultSet
 			rs = pstmt.executeQuery();
 			
 			while(rs.next()){
 				User u = new User(rs.getInt("userId"),rs.getString("userName"),
 						"",rs.getString("email"), rs.getString("securityQ"),rs.getString("securityA"), rs.getString("userType"));
 				users.add(u);
 			}
 			rs.close();
 			pstmt.close();
 			conn.close();
 			return users;						
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 		} finally {
 			if(rs != null) {rs.close();}
 			if(pstmt != null) {pstmt.close();}
 			if(conn != null) {conn.close();}
 		}
 		return null;
     }
 }
