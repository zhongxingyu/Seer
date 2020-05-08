 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pojo;
 import java.sql.*;
 import java.util.ArrayList;
 
 /**
  *
  * @author Ajit Gupta 
  */
 public class NgoList 
 {
 	protected String ngoid;
 	protected String unid;
 	protected String name;
 	protected String emailid;
 	protected String ngologo;
    
 	public NgoList()
 	{
 		this.ngoid = new String();
 		this.unid  = new String();
 		this.name  = new String();
 		this.emailid = new String();
 	}
 	public NgoList (String ngoid,String unid ,String name, String emailid )
 	{
 		this.ngoid=ngoid;
 		this.unid=unid;
 		this.name=name;
 		this.emailid=emailid;
 	}
 	public String getNgologo()
 	{
 		return ngologo;
 	}
 	public void setNgologo(String ngologo)
 	{
 		this.ngologo = ngologo;
 	}
 	public String getEmailid()
 	{
 		return emailid;
 	}
 	public void setEmailid(String emailid)
 	{
 		this.emailid = emailid;
 	}
 	public String getName()
 	{
 		return name;
 	}
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 	public String getNgoid()
 	{
 		return ngoid;
 	}
 	public void setNgoid(String ngoid)
 	{
 		this.ngoid = ngoid;
 	}
 	public String getUnid()
 	{
 		return unid;
 	}
 	public void setUnid(String unid)
 	{
 		this.unid = unid;
 	}
 	
 	public ArrayList<NgoList> getNgoList()
 	{
 		ArrayList<NgoList> ngo_list = new ArrayList<NgoList>();
 		String query = null;
 		DbContainor.loadDbDriver();
         
 		try
 		{
 			query = "Select ngoname , EMail,ngologo  from ngoinfo where EMail in (Select ngoid from joins where unid=?)";
 			Connection con = DbContainor.createConnection();
 			PreparedStatement ps = con.prepareStatement(query);
 			ps.setString(1,unid);
 			ResultSet rs = ps.executeQuery();
 			while(rs.next())
 			{
				NgoList ngolst =n ew NgoList();
 				ngolst.setName(rs.getString("ngoname"));
 				ngolst.setEmailid(rs.getString("EMail"));
 				ngolst.setNgologo(rs.getString(3));
				ngo_list.add(nglost);
 			}
 			con.close();
 		}
 		catch(NullPointerException npe)
 		{
 			System.out.println("DbContainor.createConnection():can not create connection to database : "+npe.getMessage());
 		}
 		catch(SQLException sqle)
 		{
 			System.out.println("sql error in getNgoLsit() of NgoList.java : " + sqle.getMessage());
 		}
 		return ngo_list;
 	}
 	public boolean updateNgoList()
 	{
 		boolean ret_val = false;
 		String query = null;
 		DbContainor.loadDbDriver();
         
 		try
 		{
 			query = "insert into joins values(?,?)";
 			Connection con = DbContainor.createConnection();
 			PreparedStatement ps = con.prepareStatement(query);
 			ps.setString(1,ngoid);
 			ps.setString(2, unid);
 
 			if(ps.executeUpdate()>0)
 			{
 				System.out.println("Data Succesfully inserted into Joins table  ");
 				ret_val = true;
 			}
 			else
 			{
 				System.out.println("Could not insert data into Joins table.");
 			}
 			con.close();
 		}
 		catch(NullPointerException npe)
 		{
 			System.out.println("DbContainor.createConnection():can not create connection to database : "+npe.getMessage());
 		}
 		catch(SQLException sqle)
 		{
             
 			System.out.println("sql error in createNgoList() of NgoList.java : " + sqle.getMessage());
 		}
 		return ret_val;
 	}
 }
