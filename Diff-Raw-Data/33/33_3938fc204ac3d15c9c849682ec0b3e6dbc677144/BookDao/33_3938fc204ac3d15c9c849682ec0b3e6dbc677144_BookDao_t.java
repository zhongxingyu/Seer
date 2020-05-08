 package com.shining.ibookclubserver.dao;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import com.shining.ibookclubserver.BookBean;
 
 public class BookDao {
 
 	private static BookDao dao =new BookDao();
 	
 	private Connection con;
 	
 	private BookBean bookBean;
 	
 	private String mysql_url="jdbc:mysql://localhost:3306/"+"iBookClubDB"+"?user="+"root"+"&password="+"123456";
 
 	private BookDao() {
 		 
 		try{
 			
 			Class.forName("com.mysql.jdbc.Driver").newInstance();
             con = DriverManager.getConnection(mysql_url);
         }
         catch(Exception e){
             e.printStackTrace();
         }
 	}
 
 	public static BookDao getInstance() {
 		
 		return dao;
 	}
 	
 	public void setBookBean(BookBean bookBean){
         
 		this.bookBean=bookBean;
     }
 	
 	public boolean isBookExist(){
     	 
     	 String sql="select isbn from bookinfo where isbn='"+bookBean.getIsbn()+"';";
     	 
     	 try{
     		 Statement stmt=con.createStatement();
     		 ResultSet rs=stmt.executeQuery(sql);
     		 if(rs.next())
     			 return true;
     	 }catch(Exception e){
     		 e.printStackTrace();
     	 }
     	 return false;
     }
 	
 	public void addBook() throws Exception{
          
     	 String sql="insert into bookinfo(isbn,name,publisher,author,bookcover,summary,price)  values(?,?,?,?,?,?,?)";
          try{
         	 PreparedStatement pstmt=con.prepareStatement(sql);
              pstmt.setString(1,bookBean.getIsbn());
              pstmt.setString(2,bookBean.getBookname());
              pstmt.setString(3, bookBean.getPublisher());
              pstmt.setString(4, bookBean.getAuthor());
              pstmt.setString(5, bookBean.getBookcover_url());
              //TODO summary字段过长，待处理
              pstmt.setString(6, null);
              pstmt.setString(7, bookBean.getPrice());
              pstmt.executeUpdate();
          }
          catch(Exception e){
               
         	 e.printStackTrace();
          }      
 
      }
 	
 	public int findID(String email){
     	 
     	 String sql="select id from userinfo where email='"+email+"';";
     	 try{
     		 Statement stmt=con.createStatement();
     		 ResultSet rs=stmt.executeQuery(sql);
     		 if(rs.next())
     			 return rs.getInt("id");
     	 }catch(Exception e){
     		 e.printStackTrace();
     	 }
     	 return -1;
      }
 	
 	public void setOwner(String email){
    	 
 		String sql="insert into bookowner(isbn,id)  values(?,?)";
    	 	int id=findID(email);
    	 	try{
    	 		PreparedStatement pstmt=con.prepareStatement(sql);
    	 		pstmt.setString(1, bookBean.getIsbn());
    	 		pstmt.setInt(2, id);
    	 		pstmt.executeUpdate();
    	 	}catch(Exception e){
    	 		e.printStackTrace();
    	 	}
     }
 	
 	public Boolean checkBook(String email,String isbn){
 		
 		 String sql="select * from bookowner where isbn ='"+isbn+"' and id in (select id from userinfo where email ='"+email+"');" ;
 		 
 		 try{
 			 Statement stmt=con.createStatement();
 			 ResultSet rs=stmt.executeQuery(sql);
 			 if(rs.next())
 				 return true;
 	
 		 }catch(Exception e){
 			 e.printStackTrace();
 		 }
 		 return false;
 		
 	}
 	
 	public void deleteBook(String email,String isbn){
 		 
 		 String sql="delete from bookowner where isbn ='"+isbn+"' and id in (select id from userinfo where email ='"+email+"');" ;
 		 
 		 try{
 			 Statement stmt=con.createStatement();
 			 stmt.execute(sql);
 		 }catch(Exception e){
 			 e.printStackTrace();
 		 }
 	 }
 	
 	public ArrayList<BookBean> getMyBook(String email){
    	 
 	   	 String sql="select * from bookinfo where isbn in(select isbn from bookowner where id in(select id from userinfo where email='"+email+"'));";
 	   	 ArrayList<BookBean> bookList=new ArrayList<BookBean>();
 	   	 try{
 	   		 Statement stmt=con.createStatement();
 	   		 ResultSet rs=stmt.executeQuery(sql);
 	   		 for(int i=0;rs.next();i++){
 	   			BookBean bean=new BookBean();
 	   			bean.setIsbn(rs.getString("isbn"));
 	   			bean.setAuthor(rs.getString("author"));
 	   			bean.setBookcover_url(rs.getString("bookcover"));
 	   			bean.setPublisher(rs.getString("publisher"));
 	   			bean.setPrice(rs.getString("price"));
 	   			bean.setBookname(rs.getString("name"));
 	   			bean.setSummary(rs.getString("summary"));
 	   			bookList.add(bean);
 	   		 }
 	   	 }catch(Exception e){
 	   		e.printStackTrace();
 	   	 }
 	   	 return bookList;
    	 
     }
 	
 	public ArrayList<BookBean> getPublicBook(){
    	 
	   	 String sql="select * from bookinfo where isbn in(select isbn from bookowner);";
 	   	 ArrayList<BookBean> bookList=new ArrayList<BookBean>();
 	   	 try{
 	   		 Statement stmt=con.createStatement();
 	   		 ResultSet rs=stmt.executeQuery(sql);
 	   		 for(int i=0;rs.next();i++){
 	   			 
 	   			BookBean bean=new BookBean();
 	   			bean.setIsbn(rs.getString("isbn"));
 	   			bean.setAuthor(rs.getString("author"));
 	   			bean.setBookcover_url(rs.getString("bookcover"));
 	   			bean.setPublisher(rs.getString("publisher"));
 	   			bean.setPrice(rs.getString("price"));
 	   			bean.setBookname(rs.getString("name"));
 	   			bean.setSummary(rs.getString("summary"));
 	   			bookList.add(bean);
 	   		 }
 	   	 }catch(Exception e){
 	   		e.printStackTrace();
 	   	 }
 	   	 return bookList;
    	 
     }
 	
 	public ArrayList<BookBean> searchPublicBook(String keyword){
 		
 		String sql="select * from bookinfo where name like '%"+keyword+"%';";
 			
 		ArrayList<BookBean> bookList=new ArrayList<BookBean>();
 	   	 try{
 	   		 Statement stmt=con.createStatement();
 	   		 ResultSet rs=stmt.executeQuery(sql);
 	   		 for(int i=0;rs.next();i++){
 	   			 
 	   			BookBean bean=new BookBean();
 	   			bean.setIsbn(rs.getString("isbn"));
 	   			bean.setAuthor(rs.getString("author"));
 	   			bean.setBookcover_url(rs.getString("bookcover"));
 	   			bean.setPublisher(rs.getString("publisher"));
 	   			bean.setPrice(rs.getString("price"));
 	   			bean.setBookname(rs.getString("name"));
 	   			bean.setSummary(rs.getString("summary"));
 	   			bookList.add(bean);
 	   		 }
 	   	 }catch(Exception e){
 	   		e.printStackTrace();
 	   	 }
 	   	 return bookList;
 		
 	}
 }
