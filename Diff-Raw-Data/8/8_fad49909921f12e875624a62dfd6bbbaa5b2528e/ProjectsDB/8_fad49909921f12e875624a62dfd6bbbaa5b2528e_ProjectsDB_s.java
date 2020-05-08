 package data;
 
 import java.sql.*; 
 import java.util.ArrayList;
 import java.util.Date;
 import beans.Project;
 
 public class ProjectsDB {
 	/***************************************************************************************
 	 * Class....................................................................ProjectsDB *
 	 * Author..........................................................................BDS *
 	 * ----------------------------------------------------------------------------------- *
 	 ***************************************************************************************/
 	
   /**********************************************************************
   * Method...............................................getAllProjects *
   * Author..........................................................DJC *
   * --------------------------------------------------------------------*
   * Retrieves all projects currently in the project database            *
   *                                                                     *
   *     Return Value                                                    *
   *     ArrayList<Project> projectList - Returns an ArrayList of        *
   *                                      project beans                  *
   **********************************************************************/
   public static synchronized ArrayList<Project> getAllProjects()
   {
     //array list to hold all projects
     ArrayList<Project> projects = new ArrayList<Project>();
     
     //build the query
     StringBuilder query = new StringBuilder();
 
     query.append("SELECT p.projid, p.name, p.description, p.userID, ");
     query.append("(SELECT CONCAT_WS(' ', first_name, last_name) FROM user WHERE user_id = p.userID), ");
     query.append("p.createddate, p.lastupdated, p.updateduserid, ");
     query.append("(SELECT CONCAT_WS(' ', first_name, last_name) FROM user WHERE user_id = p.updateduserid), ");
     query.append("p.bannerpicture, p.bannerpictureextension ");
     query.append("FROM projects p ");
     query.append("ORDER BY p.createddate, p.lastupdated ");
     
     try
     {
       Connection connection       = DBConnector.getConnection();
       PreparedStatement statement = connection.prepareStatement(query.toString());      
       ResultSet resultSet         = statement.executeQuery();
 
       //loop through the result set and add each project to the array list
       while(resultSet.next())
       {
         Project project = new Project();
         
         project.setProjectID(resultSet.getInt(1));
         project.setName(resultSet.getString(2)); 
         project.setDesc(resultSet.getString(3));
         project.setUserID(resultSet.getInt(4));
         project.setSubmittedBy(resultSet.getString(5));
         project.setCreatedDate(resultSet.getString(6));
         project.setLastUpdatedDate(resultSet.getString(7));
         project.setLastUpdatedID(resultSet.getInt(8));
         project.setLastUpdatedBy(resultSet.getString(9));
         project.setBannerPicture(resultSet.getBytes(10));
         project.setBannerPicExt(resultSet.getString(11));
 
         projects.add(project);  
       }
       
       resultSet.close();   
       statement.close();
       connection.close();
     }
     catch (SQLException ex)
     {
       System.out.println("Error: " + ex);
       System.out.println("Query: " + query.toString());
     }
     
     //return the projects
     return projects;
   }
 	
   /**********************************************************************
   * Method............................................getProjectByAlpha *
   * Author..........................................................JLH *
   * --------------------------------------------------------------------*
   * This method retrieves a list of project beans based on projectName  *
   * If the projectName is an empty string the contents of the entire    *
   * table will be returned, otherwise the method will return all records*
   * that begin with the projectName string.(Use LIKE)                   *
   *                                                                     *
   *     Required parameters                                             *
   *     (String) projectName - the string to find                       *
   *                                                                     *
   *     Return Value                                                    *
   *     ArrayList<Project> projects - Returns an projectList of         *
   *                                      project beans                  *
   **********************************************************************/
   public static synchronized ArrayList<Project> getProjectsByAlpha(String projectName)
   {
     //array list to hold projects that match the search string
     ArrayList<Project> projects = new ArrayList<Project>();
     
     //build the query
     StringBuilder query = new StringBuilder();
     
     query.append("SELECT p.projid, p.name, p.description, p.userID, ");
     query.append("(SELECT CONCAT_WS(' ', first_name, last_name) FROM user WHERE user_id = p.userID), ");
     query.append("p.createddate, p.lastupdated, p.updateduserid, ");
     query.append("(SELECT CONCAT_WS(' ', first_name, last_name) FROM user WHERE user_id = p.updateduserid), ");
     query.append("p.bannerpicture, p.bannerpictureextension ");
     query.append("FROM projects p ");
     query.append("WHERE p.name LIKE ? ");
     query.append("ORDER BY p.createddate, p.lastupdated ");
     
     try
     {
       Connection connection       = DBConnector.getConnection();
       PreparedStatement statement = connection.prepareStatement(query.toString());
       
       //set the parameters for the query
       statement.setString(1, projectName+"%");
       
       ResultSet resultSet = statement.executeQuery();
 
       //loop through the result set and add each project retrieved
       while(resultSet.next())
       {
         Project project = new Project();
         
         project.setProjectID(resultSet.getInt(1));
         project.setName(resultSet.getString(2)); 
         project.setDesc(resultSet.getString(3));
         project.setUserID(resultSet.getInt(4));
         project.setSubmittedBy(resultSet.getString(5));
         project.setCreatedDate(resultSet.getString(6));
         project.setLastUpdatedDate(resultSet.getString(7));
         project.setLastUpdatedID(resultSet.getInt(8));
         project.setLastUpdatedBy(resultSet.getString(9));
         project.setBannerPicture(resultSet.getBytes(10));
         project.setBannerPicExt(resultSet.getString(11));
 
         projects.add(project);      
       }
       
       resultSet.close();    
       statement.close();
       connection.close();
     }
     catch (SQLException ex)
     {
       System.out.println("Error: " + ex);
       System.out.println("Query: " + query.toString());
     }
     
     //return the projects
     return projects;
   }
   
   /**********************************************************************
   * Method.......................................getProjectByAlphaExact *
   * Author..........................................................BDS *
   * --------------------------------------------------------------------*
   * This method retrieves a list of project beans based on projectName. *
   * If the projectName is an empty string the contents of the entire    *
   * table will be returned, otherwise the method will return all records*
   * that match the project name.                                        *
   *                                                                     *
   *     Required parameters                                             *
   *     String projectName - the name of the project to search for    *
   *                                                                     *
   *     Return Value                                                    *
   *     ArrayList<Project> projects - Returns an arrayList of project   *
   *                                   beans                             *
   **********************************************************************/
   public static synchronized ArrayList<Project> getProjectByAlphaExact(String projectName)
   {
     ArrayList<Project> projects = new ArrayList<Project>();
     
     StringBuilder query = new StringBuilder();
     
     query.append("SELECT p.projid, p.name, p.description, p.userID, ");
     query.append("(SELECT CONCAT_WS(' ', first_name, last_name) FROM user WHERE user_id = p.userID), ");
     query.append("p.createddate, p.lastupdated, p.updateduserid, ");
     query.append("(SELECT CONCAT_WS(' ', first_name, last_name) FROM user WHERE user_id = p.updateduserid), ");
     query.append("p.bannerpicture, p.bannerpictureextension ");
     query.append("FROM projects p ");
     query.append("WHERE p.name = ? ");
     query.append("ORDER BY p.createddate, p.lastupdated ");
     
     try
     {
       Connection connection = DBConnector.getConnection();
       PreparedStatement statement = connection.prepareStatement(query.toString());
       
       statement.setString(1, projectName+"%");
       
       ResultSet resultSet = statement.executeQuery();
 
       while(resultSet.next())
       {
         Project project = new Project();
 
         project.setProjectID(resultSet.getInt(1));
         project.setName(resultSet.getString(2)); 
         project.setDesc(resultSet.getString(3));
         project.setUserID(resultSet.getInt(4));
         project.setSubmittedBy(resultSet.getString(5));
         project.setCreatedDate(resultSet.getString(6));
         project.setLastUpdatedDate(resultSet.getString(7));
         project.setLastUpdatedID(resultSet.getInt(8));
         project.setLastUpdatedBy(resultSet.getString(9));
         project.setBannerPicture(resultSet.getBytes(10));
         project.setBannerPicExt(resultSet.getString(11));
         
         //kill of any nulls in Name column
         if(project.getName() == null) 
         {
           project.setName("New Project");
         }
 
         //add info columns
         projects.add(project);      
       }
       
       resultSet.close();    
       statement.close();
       connection.close();
     }
     catch (SQLException ex)
     {
       System.out.println("Error: " + ex);
       System.out.println("Query: " + query.toString());
     }
     
     return projects;
   }
 
   /**********************************************************************
   * Method..............................................getProjectByPID *
   * Author..........................................................BDS *
   * --------------------------------------------------------------------*
   * This method calls a Project Bean object based on the projectID      *
   * variable                                                            *
   *                                                                     *
   *     Required parameters                                             *
   *     (String) projectID - the id of the project to search for        *
   *                                                                     *
   *     Return Value                                                    *
   *     (Project) project  - Returns a project bean object.             *
   **********************************************************************/
   public static synchronized Project getProjByPID(String projectID)
   {
     Project project = new Project();
     
     StringBuilder query = new StringBuilder();
     
     query.append("SELECT p.projid, p.name, p.description, p.userID, ");
     query.append("(SELECT CONCAT_WS(' ', first_name, last_name) FROM user WHERE user_id = p.userID), ");
     query.append("p.createddate, p.lastupdated, p.updateduserid, ");
     query.append("(SELECT CONCAT_WS(' ', first_name, last_name) FROM user WHERE user_id = p.updateduserid), ");
     query.append("p.bannerpicture, p.bannerpictureextension ");
     query.append("FROM projects p ");
     query.append("WHERE p.projid = ? ");
     query.append("ORDER BY p.createddate, p.lastupdated ");
     
     try
     {
       Connection connection       = DBConnector.getConnection();
       PreparedStatement statement = connection.prepareStatement(query.toString());
       
       statement.setString(1, projectID);
       
       ResultSet resultSet = statement.executeQuery();
 
       while(resultSet.next())
       {
           project.setProjectID(resultSet.getInt(1));
           project.setName(resultSet.getString(2)); 
           project.setDesc(resultSet.getString(3));
           project.setUserID(resultSet.getInt(4));
           project.setSubmittedBy(resultSet.getString(5));
           project.setCreatedDate(resultSet.getString(6));
           project.setLastUpdatedDate(resultSet.getString(7));
           project.setLastUpdatedID(resultSet.getInt(8));
           project.setLastUpdatedBy(resultSet.getString(9));
           project.setBannerPicture(resultSet.getBytes(10));
           project.setBannerPicExt(resultSet.getString(11));
         
         //kill of any nulls in Name column
         if(project.getName() == null) 
         {
           project.setName("New Project");
         }
       }
       
       resultSet.close();    
       statement.close();
       connection.close();
     }
     catch (SQLException ex)
     {
       System.out.println("Error: " + ex);
       System.out.println("Query: " + query.toString());
     }
     
     return project;
   }
   
   /**********************************************************************
   * Method.............................................getProjectByBean *
   * Author..........................................................BDS *
   * --------------------------------------------------------------------*
   * This method a calls a Project Bean object based on the project      *
   *                                                                     *
   *     Required parameters                                             *
   *     Project proj - The project to search for                        *
   *     Return Value                                                    *
   *     (Project) project - Returns a project bean object.              *
   **********************************************************************/
   public static synchronized Project getProjectByBean(Project proj)
   {
     Project project = new Project();
       
     StringBuilder query = new StringBuilder();
     
     query.append("SELECT p.projid, p.name, p.description, p.userID, ");
     query.append("(SELECT CONCAT_WS(' ', first_name, last_name) FROM user WHERE user_id = p.userID), ");
     query.append("p.createddate, p.lastupdated, p.updateduserid, ");
     query.append("(SELECT CONCAT_WS(' ', first_name, last_name) FROM user WHERE user_id = p.updateduserid), ");
     query.append("p.bannerpicture, p.bannerpictureextension ");
     query.append("FROM projects p ");
     query.append("WHERE p.name = ? ");
     query.append("AND p.description = ? ");
     query.append("AND p.userID = ? ");
 
     try
     {
       Connection connection       = DBConnector.getConnection();
       PreparedStatement statement = connection.prepareStatement(query.toString());
      ResultSet resultSet         = statement.executeQuery();
      
       statement.setString(1, proj.getName());
       statement.setString(2, proj.getDesc());
       statement.setInt(3, proj.getUserID());

       while(resultSet.next())
       {
           project.setProjectID(resultSet.getInt(1));
           project.setName(resultSet.getString(2)); 
           project.setDesc(resultSet.getString(3));
           project.setUserID(resultSet.getInt(4));
           project.setSubmittedBy(resultSet.getString(5));
           project.setCreatedDate(resultSet.getString(6));
           project.setLastUpdatedDate(resultSet.getString(7));
           project.setLastUpdatedID(resultSet.getInt(8));
           project.setLastUpdatedBy(resultSet.getString(9));
           project.setBannerPicture(resultSet.getBytes(10));
           project.setBannerPicExt(resultSet.getString(11));
       }
       
       resultSet.close();    
       statement.close();
       connection.close();
     }
     catch (SQLException ex)
     {
       System.out.println("Error: " + ex);
       System.out.println("Query: " + query.toString());
     }
       
     return project;
   }
 	
   /**********************************************************************
   * Method......................................................addProj *
   * Author..........................................................BDS *
   *---------------------------------------------------------------------*
   * This method adds a project to the Project table.                    *
   *     Required parameters                                             *
   *     (Project) project - Project bean containing the info to be      *
   *                         added                                       *
   *     Return Value                                                    *
   *     (int) status - The number of records modified by the query      *
   **********************************************************************/
   public static synchronized int addProject(Project project)
   {
     StringBuilder query = new StringBuilder();
     
     query.append("INSERT INTO projects ");
     query.append("(name, createddate, description, userid, bannerpicture, bannerpictureextension) ");
     query.append("VALUES ");
     query.append("(?, ?, ?, ?, ?, ?) ");
     
     int status = 0;
     
     try
     {
       Connection connection       = DBConnector.getConnection();
       PreparedStatement statement = connection.prepareStatement(query.toString());
       
       statement.setString(1, project.getName());
       statement.setString(2, new Date().toString());
       statement.setString(3, project.getDesc());
       statement.setInt(4, project.getUserID());
       statement.setBytes(5, project.getBannerPicture());
       statement.setString(6, project.getBannerPicExt());
 
       status = statement.executeUpdate();
       
       statement.close();
       connection.close();
     }
     catch (SQLException ex)
     {
       System.out.println("Error: " + ex);
       System.out.println("Query: " + query.toString());
     }
     
     return status;
   }
   
   /**********************************************************************
   * Method................................................updateProject *
   * Author..........................................................BDS *
   *---------------------------------------------------------------------*
   * This method modifies an existing project record in the Project    . *
   * Table.                                                              *
   *                                                                     *
   *     Required parameters                                             *
   *     (Project) project - Project bean with the info to be modified   *
   *     Return Value                                                    *
   *     (int) status - The number of records modified by the query      *
   **********************************************************************/
   public static synchronized int updateProject(Project project)
   {
     StringBuilder query = new StringBuilder();
     
     query.append("UPDATE projects ");
     query.append("SET name = ?, description = ?, bannerpicture = ?, ");
     query.append("bannerpictureextension = ?, lastupdated = ?, updateduserid = ? ");
     query.append("WHERE projid = ? ");
     
     int status = 0;
     
     try
     {
       Connection connection       = DBConnector.getConnection();
       PreparedStatement statement = connection.prepareStatement(query.toString());
       
       statement.setString(1, project.getName());
       statement.setString(2, project.getDesc());
       statement.setBytes(3, project.getBannerPicture());  
       statement.setString(4, project.getBannerPicExt());
       statement.setString(5, new Date().toString());
       statement.setInt(6, project.getLastUpdatedID());
 
       status = statement.executeUpdate();
       
       statement.close();
       connection.close();
     }
     catch (SQLException ex)
     {
       System.out.println("Error: " + ex);
       System.out.println("Query: " + query.toString());
     }
     
     return status;
   }
   
   /**********************************************************************
   * Method................................................deleteProject *
   * Author..........................................................BDS *
   *---------------------------------------------------------------------*
   * This method deletes all records for a projectID in the Project      *
   *     table                                                           *
   *                                                                     *
   *     Required parameters                                             *
   *     String projectID - Primary Key for Project record               *
   *     Return Value                                                    *
   *     int status - Results of deletions                               *
   **********************************************************************/
   public static synchronized int deleteProject(String projectID)
   {
     String query = "DELETE FROM projects WHERE projid = ?";
 
     int status = 0;
     
     try
     {
       Connection connection       = DBConnector.getConnection();
       PreparedStatement statement = connection.prepareStatement(query);
       
       statement.setString(1, projectID);
 
       status = statement.executeUpdate();
       
       statement.close();
       connection.close();
     }
     catch (SQLException ex)
     {
       System.out.println("Error: " + ex);
       System.out.println("Query: " + query.toString());
     }
     
     return status;
   }
 }
