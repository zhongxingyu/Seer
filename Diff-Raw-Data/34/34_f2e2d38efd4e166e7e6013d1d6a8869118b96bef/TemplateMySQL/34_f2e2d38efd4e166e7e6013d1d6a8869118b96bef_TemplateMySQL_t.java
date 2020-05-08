 /*******************************************************************************
  *Copyright (c) 2013 StorySense
  *All rights reserved. This program and the accompanying materials
  *are made available under the terms of the Eclipse Public License v1.0
  *which accompanies this distribution, and is available at
  *http://www.eclipse.org/legal/epl-v10.html
  *
  *Contributors:
  *    Nickleus Jimenez
  *******************************************************************************/
 package mysqlDao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import dao.*;
 import dbConnection.DBConnectionFactory;
 import entity.Template;
 
 /**
  *   `TemplateID` int(11) NOT NULL AUTO_INCREMENT,
   `StoryURL` text,
   `RelationURL` text,
   `LevelReq` int(11) DEFAULT '1',
   `plusScore` int(11) DEFAULT '10',
   `Name` varchar(50) DEFAULT NULL,
   PRIMARY KEY (`TemplateID`)
 
  * @author nickleus
  *
  */
 
 public class TemplateMySQL extends TemplateDAO {
 
 	/**
 	 * Add a template so that it can be use to generate stories
 	 * 
 	 * @param
 	 * giventemplate : The template to be included in generating stories
 	 */
 	@Override
 	public void addTemplate(Template giventemplate) {
 		PreparedStatement ps;
         try{
         	DBConnectionFactory myFactory = DBConnectionFactory.getInstance(DAOFactory.MYSQL);
         	Connection con = myFactory.getConnection();
         	
         	ps = con.prepareStatement("INSERT INTO template(StoryURL,RelationURL,LevelReq,plusScore,Name,authorID) " +
         			"VALUES (?,?,?,?,?,?)");
             ps.setString(1, giventemplate.getStoryURL());
             ps.setString(2, giventemplate.getRelationURL());
             ps.setInt(3, giventemplate.getLevelRequirement());
             ps.setInt(4, giventemplate.getPlusScore());
             ps.setString(5, giventemplate.getName());
             ps.setInt(6, giventemplate.getAuthorID());
             ps.execute();
             ps.close();
 
             con.close();
         }catch(Exception ex){
         	Logger.getLogger(ProfileMySQL.class.getName()).log(Level.SEVERE, null, ex);
         }
 		
 	}
 
 	/**
 	 * Any changes in the template being referred to by the record
 	 */
 	@Override
 	public void updateTemplate(Template giventemplate) {
 		PreparedStatement ps;
 		try{
 			DBConnectionFactory myFactory = DBConnectionFactory.getInstance(DAOFactory.MYSQL);
         	Connection con = myFactory.getConnection();
         	
        	ps=con.prepareStatement("update  template  SET Name= ?, StoryURL = ?, RelationURL = ?,LevelReq = ?," +
         			" plusScore = ? where TemplateID = ?");
         	
         	ps.setString(1, giventemplate.getName());
         	ps.setString(2, giventemplate.getStoryURL());
         	ps.setString(3, giventemplate.getRelationURL());
         	ps.setInt(4, giventemplate.getLevelRequirement());
         	ps.setInt(5, giventemplate.getPlusScore());
         	ps.setInt(6, giventemplate.getTemplateID());
        	ps.executeUpdate();
         	
 			ps.close();
 			con.close();
 		}catch(Exception ex){
 			Logger.getLogger(TemplateMySQL.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		
 	}
 
 	/**
 	 * change the URL of the story template half
 	 * 
 	 * @param
 	 * ID : the ID of the template  refereed 
 	 * newURL : the new URL of the templateS
 	 */
 	@Override
 	public void changeStoryURL(int ID,String newURL) {
 		PreparedStatement ps;
 		try{
 			DBConnectionFactory myFactory = DBConnectionFactory.getInstance(DAOFactory.MYSQL);
         	Connection con = myFactory.getConnection();
         	
         	ps=con.prepareStatement("update * from template  SET StoryURL = ? where TemplateID = ?");
         	
         	ps.setString(1, newURL);
         	ps.setInt(2, ID);
         	
 			ps.close();
 			con.close();
 		}catch(Exception ex){
 			Logger.getLogger(TemplateMySQL.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		
 	}
 
 	/**
 	 * change the URL of the relational template half
 	 * 
 	 * @param
 	 * ID : the ID of the template  refereed 
 	 * newURL : the new URL of the templateS
 	 */
 	@Override
 	public void changeRelationalURL(int ID,String newURL) {
 		PreparedStatement ps;
 		try{
 			DBConnectionFactory myFactory = DBConnectionFactory.getInstance(DAOFactory.MYSQL);
         	Connection con = myFactory.getConnection();
         	
         	ps=con.prepareStatement("update * from template  SET RelationURL = ? where TemplateID = ?");
         	
         	ps.setString(1, newURL);
         	ps.setInt(2, ID);
         	
 			ps.close();
 			con.close();
 		}catch(Exception ex){
 			Logger.getLogger(TemplateMySQL.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 
 	/**
 	 * Change the name of the template
 	 */
 	@Override
 	public void changeTemplateName(int ID,String newName) {
 		PreparedStatement ps;
 		try{
 			DBConnectionFactory myFactory = DBConnectionFactory.getInstance(DAOFactory.MYSQL);
         	Connection con = myFactory.getConnection();
         	
         	ps=con.prepareStatement("update * from template  SET Name = ? where TemplateID = ?");
         	
         	ps.setString(1, newName);
         	ps.setInt(2, ID);
         	
 			ps.close();
 			con.close();
 		}catch(Exception ex){
 			Logger.getLogger(TemplateMySQL.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		
 	}
 
 	
 	/**
 	 * gets the templates with a minimum level
 	 */
 	@Override
 	public List<Template> getTemplatebyLevel(int minimumLevel) {
 		try {
             PreparedStatement ps;
             ResultSet rs;
 
             DBConnectionFactory myFactory = DBConnectionFactory.getInstance(DAOFactory.MYSQL);
             Connection con = myFactory.getConnection();
 
             ps = con.prepareStatement("SELECT * FROM template WHERE LevelReq <= ?");
             ps.setInt(1, minimumLevel);
             rs = ps.executeQuery();
            
             
             Template t;
             ArrayList<Template> templates=new ArrayList<Template>();
             while(rs.next()){
             	t=new Template();
             	t.setTemplateID(rs.getInt("TemplateID"));
             	t.setName(rs.getString("Name"));
             	t.setLevelRequirement(rs.getInt("LevelReq"));
             	t.setPlusScore(rs.getInt("plusScore"));
             	t.setStoryURL(rs.getString("StoryURL"));
             	t.setRelationURL(rs.getString("RelationURL"));
             	templates.add(t);
             }
 
             rs.close();
             ps.close();
             con.close();
             
             return templates;
 		}
         catch (Exception ex)
         {
             Logger.getLogger(TemplateMySQL.class.getName()).log(Level.SEVERE, null, ex);
         }
 		return null;
 	}
 
 	/**
 	 * get templates that give at least a given amount of points
 	 */
 	@Override
 	public List<Template> getTemplatebyScore(int minimumPoints) {
 		try {
             PreparedStatement ps;
             ResultSet rs;
 
             DBConnectionFactory myFactory = DBConnectionFactory.getInstance(DAOFactory.MYSQL);
             Connection con = myFactory.getConnection();
 
             ps = con.prepareStatement("SELECT * FROM template WHERE plusScore >= ?");
             ps.setInt(1, minimumPoints);
             rs = ps.executeQuery();
            
             
             Template t;
             ArrayList<Template> templates=new ArrayList<Template>();
             while(rs.next()){
             	t=new Template();
             	t.setTemplateID(rs.getInt("TemplateID"));
             	t.setName(rs.getString("Name"));
             	t.setLevelRequirement(rs.getInt("LevelReq"));
             	t.setPlusScore(rs.getInt("plusScore"));
             	t.setStoryURL(rs.getString("StoryURL"));
             	t.setRelationURL(rs.getString("RelationURL"));
             	templates.add(t);
             }
 
             rs.close();
             ps.close();
             con.close();
             
             return templates;
 		}
         catch (Exception ex)
         {
             Logger.getLogger(TemplateMySQL.class.getName()).log(Level.SEVERE, null, ex);
         }
 		return null;
 	}
 
 	/**
 	 * Get templates with a name related to a given query from the user
 	 */
 	@Override
 	public List<Template> getTemplatebyName(String Name) {
 		try {
             PreparedStatement ps;
             ResultSet rs;
 
             DBConnectionFactory myFactory = DBConnectionFactory.getInstance(DAOFactory.MYSQL);
             Connection con = myFactory.getConnection();
 
             ps = con.prepareStatement("SELECT * FROM template WHERE Name LIKE ?");
             ps.setString(1, Name);
             rs = ps.executeQuery();
            
             
             Template t;
             ArrayList<Template> templates=new ArrayList<Template>();
             while(rs.next()){
             	t=new Template();
             	t.setTemplateID(rs.getInt("TemplateID"));
             	t.setName(rs.getString("Name"));
             	t.setLevelRequirement(rs.getInt("LevelReq"));
             	t.setPlusScore(rs.getInt("plusScore"));
             	t.setStoryURL(rs.getString("StoryURL"));
             	t.setRelationURL(rs.getString("RelationURL"));
             	templates.add(t);
             }
 
             rs.close();
             ps.close();
             con.close();
             
             return templates;
 		}
         catch (Exception ex)
         {
             Logger.getLogger(TemplateMySQL.class.getName()).log(Level.SEVERE, null, ex);
         }
 		return null;
 	}
 
 	/**
 	 * Return all templates from the datbase
 	 */
 	@Override
 	public List<Template> getAlltemplates() {
 		try {
             PreparedStatement ps;
             ResultSet rs;
 
             DBConnectionFactory myFactory = DBConnectionFactory.getInstance(DAOFactory.MYSQL);
             Connection con = myFactory.getConnection();
 
             ps = con.prepareStatement("SELECT * FROM template");
             rs = ps.executeQuery();
            
             
             Template t;
             ArrayList<Template> templates=new ArrayList<Template>();
             while(rs.next()){
             	t=new Template();
             	t.setTemplateID(rs.getInt("TemplateID"));
             	t.setName(rs.getString("Name"));
             	t.setLevelRequirement(rs.getInt("LevelReq"));
             	t.setPlusScore(rs.getInt("plusScore"));
             	t.setStoryURL(rs.getString("StoryURL"));
             	t.setRelationURL(rs.getString("RelationURL"));
             	templates.add(t);
             }
 
             rs.close();
             ps.close();
             con.close();
             
             return templates;
 		}
         catch (Exception ex)
         {
             Logger.getLogger(TemplateMySQL.class.getName()).log(Level.SEVERE, null, ex);
         }
 		return null;
 	}
 
 	@Override
 	public Template getTemplate(int id) {
 		try {
             PreparedStatement ps;
             ResultSet rs;
 
             DBConnectionFactory myFactory = DBConnectionFactory.getInstance(DAOFactory.MYSQL);
             Connection con = myFactory.getConnection();
 
             ps = con.prepareStatement("SELECT * FROM template WHERE TemplateID=?");
             ps.setInt(1, id);
             rs = ps.executeQuery();
            
             
             Template t=null;
             
             if(rs.first()){
             	t=new Template();
             	t.setTemplateID(rs.getInt("TemplateID"));
             	t.setName(rs.getString("Name"));
             	t.setLevelRequirement(rs.getInt("LevelReq"));
             	t.setPlusScore(rs.getInt("plusScore"));
             	t.setStoryURL(rs.getString("StoryURL"));
             	t.setRelationURL(rs.getString("RelationURL"));
             	//templates.add(t);
             }
 
             rs.close();
             ps.close();
             con.close();
             
             return t;
 		}
         catch (Exception ex)
         {
             Logger.getLogger(TemplateMySQL.class.getName()).log(Level.SEVERE, null, ex);
         }
 		return null;
 	}
 
 	@Override
 	public List<Template> getGroupedtemplates() {
 		try {
             PreparedStatement ps;
             ResultSet rs;
 
             DBConnectionFactory myFactory = DBConnectionFactory.getInstance(DAOFactory.MYSQL);
             Connection con = myFactory.getConnection();
 
             ps = con.prepareStatement("SELECT * from template " +
             		"WHERE TemplateID IN (SELECT templateID from storyaccomplishment GROUP BY templateID)" +
             		"GROUP BY LevelReq");
             rs = ps.executeQuery();
            
             
             Template t;
             ArrayList<Template> templates=new ArrayList<Template>();
             while(rs.next()){
             	t=new Template();
             	t.setTemplateID(rs.getInt("TemplateID"));
             	t.setName(rs.getString("Name"));
             	t.setLevelRequirement(rs.getInt("LevelReq"));
             	t.setPlusScore(rs.getInt("plusScore"));
             	t.setStoryURL(rs.getString("StoryURL"));
             	t.setRelationURL(rs.getString("RelationURL"));
             	templates.add(t);
             }
 
             rs.close();
             ps.close();
             con.close();
             
             return templates;
 		}
         catch (Exception ex)
         {
             Logger.getLogger(TemplateMySQL.class.getName()).log(Level.SEVERE, null, ex);
         }
 		return null;
 	}
 
 	@Override
 	public List<Template> getAlltemplatesOfAuthor(int authorId) {
 		try {
             PreparedStatement ps;
             ResultSet rs;
 
             DBConnectionFactory myFactory = DBConnectionFactory.getInstance(DAOFactory.MYSQL);
             Connection con = myFactory.getConnection();
 
             ps = con.prepareStatement("SELECT * from template WHERE authorID= ?");
             ps.setInt(1, authorId);
             rs = ps.executeQuery();
            
             
             Template t;
             ArrayList<Template> templates=new ArrayList<Template>();
             while(rs.next()){
             	t=new Template();
             	t.setTemplateID(rs.getInt("TemplateID"));
             	t.setName(rs.getString("Name"));
             	t.setLevelRequirement(rs.getInt("LevelReq"));
             	t.setPlusScore(rs.getInt("plusScore"));
             	t.setStoryURL(rs.getString("StoryURL"));
             	t.setRelationURL(rs.getString("RelationURL"));
             	templates.add(t);
             }
 
             rs.close();
             ps.close();
             con.close();
            
             return templates;
 		}
         catch (Exception ex)
         {
             Logger.getLogger(TemplateMySQL.class.getName()).log(Level.SEVERE, null, ex);
         }
 		return null;
 	}
 
 }
