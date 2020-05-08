 package myservs;
 
 import java.sql.*;
 
 public class DatabaseMethods {
 
 	//Method for connecting to db server
 	public Connection connect(){
 		String url="jdbc:mysql://127.0.0.1/elawyer?useUnicode=true&characterEncoding=UTF8";
 		Connection con=null;
 		try{
 			String driver="com.mysql.jdbc.Driver";
 			Class.forName(driver).newInstance();
 		}catch (Exception e){
 			System.out.println("Failed to load the JDBC driver");
 		}
 		try{
 			con=DriverManager.getConnection(url,"elawyer","elawyer");
 		}catch (Exception e){
 			e.printStackTrace();
 		}
 		return con;
 	}
 	
 	//Method for registering new users
 	public int registerUser(String name, String surname, String email, String password, String id, int telephone, String address, int postcode, int isadmin){
 		Connection con=null;
 		try{
 			con=connect();
 			Statement stmt=con.createStatement();
			String insQuery="INSERT INTO users (name,surname,email,password,id,initId,telephone,address,postcode,isadmin) VALUES ('"+name+"','"+surname+"','"+email+"','"+password+"','"+id+"','"+id+"','"+telephone+"','"+address+"','"+postcode+"','"+isadmin+"')";
 			int result=stmt.executeUpdate(insQuery);
 			return result;
 		}catch(SQLException e){
 			System.out.println("SQL statement did not executed");
 			int result=0;
 			return result;
 		}finally{
 			closedb(con);
 		}
 	}
 	
 	public int updateUser(String name, String surname, String email, String password, String id, int telephone, String address, int postcode, String oldId){
 		Connection con=null;
 		try{
 			con=connect();
 			Statement stmt=con.createStatement();
 			String upQuery="UPDATE users SET name=\'"+name+"\', surname=\'"+surname+"\', email=\'"+email+"\', password=\'"+password+"\', id=\'"+id+"\', telephone=\'"+telephone+"\', address=\'"+address+"\', postcode=\'"+postcode+"\' WHERE id=\'"+oldId+"\'";
 			int result=stmt.executeUpdate(upQuery);
 			return result;
 		}catch(SQLException e){
 			e.printStackTrace();
 			int result=0;
 			return result;
 		}finally{
 			closedb(con);
 		}
 	}
 	
 	public int uploadTable(String title, String description, String location, String userid){
 		Connection con=null;
 		try{
 			con=connect();
 			Statement stmt=con.createStatement();
 			String upQuery="INSERT INTO uploads (title,description,location,userid) VALUES ('"+title+"','"+description+"','"+location+"','"+userid+"')";
 			int result=stmt.executeUpdate(upQuery);
 			return result;
 		}catch(SQLException e){
 			e.printStackTrace();
 			int result=0;
 			return result;
 		}finally{
 			closedb(con);
 		}
 	}
 	
	public int populatePfileTable(String title, String comment, int revision, String path, String owner, String commonFileName, String realName){
 		Connection con=null;
 		int result=0;
 		try{
 			con=connect();
 			Statement stmt=con.createStatement();
			String insQuery="INSERT INTO pfiles (title,comment,version,path,owner,fileName,realName) VALUES ('"+title+"', '"+comment+"', '"+revision+"', '"+path+"', '"+owner+"', '"+commonFileName+"', '"+realName+"')";
 			result=stmt.executeUpdate(insQuery);
 			return result;
 		}catch(SQLException e){
 			e.printStackTrace();
 			return result;
 		}finally{
 			closedb(con);
 		}
 	}
 	
 	//Method for identifying unique users
 	public int uniqueUser(String id, String email){
 		Connection con=null;
 		try{
 			con=connect();
 			Statement stmt=con.createStatement();
 			String uniqueQuery="SELECT id,email FROM users";
 			ResultSet result=stmt.executeQuery(uniqueQuery);
 			int exists=0;
 			
 			while (result.next()){
 				String usersId=result.getString(1);
 				String usersEmail=result.getString(2);
 				if ((id.equals(usersId)) || (email.equals(usersEmail))){
 					//User with same login details already exists
 					exists=1;
 				}
 			}
 			return exists;
 		}catch (SQLException e){
 			System.out.println("SQL statement did not executed");
 			int sqlError=1;
 			return sqlError;
 		}finally{
 			closedb(con);
 		}		
 	}
 	
 	//Method for identifying unique users only for the update
 	public int updateUniqueUser(String id, String email){
 		Connection con=null;
 		try{
 			con=connect();
 			Statement stmt=con.createStatement();
 			String uniqueQuery="SELECT id,email FROM users";
 			ResultSet result=stmt.executeQuery(uniqueQuery);
 			int exists=0;
 			
 			while (result.next()){
 				String usersId=result.getString(1);
 				String usersEmail=result.getString(2);
 				if ((id.equals(usersId)) && (email.equals(usersEmail))){
 					//User with same login details already exists
 					exists=1;
 				}
 			}
 			return exists;
 		}catch (SQLException e){
 			System.out.println("SQL statement did not executed");
 			int sqlError=1;
 			return sqlError;
 		}finally{
 			closedb(con);
 		}		
 	}
 	
 	//Login method
 	public String[] identifyUser(String email, String password){
 		Connection con=null;
		String userCredentials[]=new String[11];
 		
 		try{
 			con=connect();
 			Statement stmt=con.createStatement();
 			String identifyQuery="SELECT * FROM users";
 			ResultSet result=stmt.executeQuery(identifyQuery);
 			
 			while (result.next()){
 				String storedEmail=result.getString(4);
 				String storedPass=result.getString(5);
 				if (email.equals(storedEmail) && password.equals(storedPass)){
 					for (int i=1;i<11;i++){
 						userCredentials[i-1]=result.getString(i);
 					}
 					break;
 				}
 			}
 			return userCredentials;
 		}catch(Exception e){
 			e.printStackTrace();
 			return userCredentials;
 		}finally{
 			closedb(con);
 		}
 	}
 	
 	public int findFileRevision(String commonFileName){
 		Connection con=null;
 		int revision=0;
 		try{
 			con=connect();
 			Statement stmt=con.createStatement();
 			String queryString="SELECT version FROM pfiles WHERE fileName=\'"+commonFileName+"\'";
 			ResultSet result=stmt.executeQuery(queryString);
 			while(result.next()){
 				revision=result.getInt(1);
 			}
 			return revision;
 		}catch(Exception e){
 			e.printStackTrace();
 			return revision;
 		}finally{
 			closedb(con);
 		}
 	}
 	
 	//Method for closing the connection to db server
 	public void closedb(Connection con){
 		if (con != null){
 			try{
 				con.close();
 			}catch (Exception e){
 				e.printStackTrace();
 			}
 			
 		}
 	}	
 }
