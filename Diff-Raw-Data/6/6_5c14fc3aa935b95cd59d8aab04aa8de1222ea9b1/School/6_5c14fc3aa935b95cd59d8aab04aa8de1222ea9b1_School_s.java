 package models;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import database.DBFactory;
 import database.InvalidModelException;
 import database.Model;
 
 public class School extends Model
 {
 
 	private String id;
 
 	private Location location;
 	private String locationId;
 	private String name;
 	private String rmpId;
 
 	private ArrayList<Teacher> teachers;
 
 	public School(ResultSet resultSet) throws SQLException
 	{
		this(resultSet.getString("id"), resultSet.getString("name"), resultSet
				.getString("rmp_id"), resultSet.getString("location_id"),
				false, false);
 	}
 
 	public School(String rmpId, String name, Location location)
 	{
 		this(null, rmpId, name, location.getId(), true, true);
 	}
 
 	public School(String id, String rmpId, String name, String locationId,
 			Boolean dirty, Boolean fresh)
 	{
 		super("schools", new ArrayList<String>()
 		{
 
 			{
 				this.add("id");
 				this.add("location_id");
 				this.add("name");
 				this.add("rmp_id");
 			}
 		}, new ArrayList<String>()
 		{
 
 			{
 			}
 		}, dirty, fresh);
 		this.setId(id);
 		this.setRmpId(rmpId);
 		this.setName(name);
 		this.setLocationId(this.locationId);
 	}
 
 	public static ArrayList<School> findAll()
 	{
 		System.out.println("find all schools");
 		ArrayList<School> schools = new ArrayList<>();
 
 		Connection connection = DBFactory.getConnection();
 		Statement statement = null;
 		ResultSet resultSet = null;
 
 		try
 		{
 
 			String query = "SELECT id, location_id, name, rmp_id FROM schools";
 			statement = connection.createStatement();
 			resultSet = statement.executeQuery(query);
 
 			while (resultSet.next())
 			{
 				School school = new School(resultSet);
 				schools.add(school);
 			}
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			System.exit(1);
 		}
 		finally
 		{
 			try
 			{
 				resultSet.close();
 				statement.close();
 				DBFactory.closeConnection(connection);
 			}
 			catch (Exception exception)
 			{
 				exception.printStackTrace();
 			}
 		}
 		return schools;
 	}
 
 	public static School findById(String id)
 	{
 		School school = null;
 
 		Connection connection = DBFactory.getConnection();
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 
 		try
 		{
 
 			String query = "SELECT id, location_id, name, rmp_id FROM schools WHERE id = ? LIMIT 1";
 			statement = connection.prepareStatement(query);
 			statement.setString(1, id);
 			resultSet = statement.executeQuery();
 
 			while (resultSet.next())
 			{
 				school = new School(resultSet);
 			}
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			System.exit(1);
 		}
 		finally
 		{
 			try
 			{
 				resultSet.close();
 				statement.close();
 				DBFactory.closeConnection(connection);
 			}
 			catch (Exception exception)
 			{
 				exception.printStackTrace();
 			}
 		}
 		return school;
 	}
 
 	public static School findByName(String name2)
 	{
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException();
 		// return null;
 	}
 
 	public static ArrayList<School> findByState(String state_id)
 	{
 		System.out.println("find schools by state");
 		ArrayList<School> schools = new ArrayList<>();
 
 		Connection connection = DBFactory.getConnection();
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 
 		try
 		{
 
 			String query = "SELECT schools.id, schools.location_id, schools.name, schools.rmp_id FROM schools JOIN locations ON locations.id = schools.location_id WHERE locations.state_id=?";
 			statement = connection.prepareStatement(query);
 			statement.setString(1, state_id);
 			resultSet = statement.executeQuery();
 
 			while (resultSet.next())
 			{
 				School school = new School(resultSet);
 				schools.add(school);
 			}
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			System.exit(1);
 		}
 		finally
 		{
 			try
 			{
 				resultSet.close();
 				statement.close();
 				DBFactory.closeConnection(connection);
 			}
 			catch (Exception exception)
 			{
 				exception.printStackTrace();
 			}
 		}
 		return schools;
 	}
 
 	public static School findOrCreate(String rmpId, String name,
 			Location location) throws InvalidModelException
 	{
 		String findQuery = "SELECT id, rmp_id, name, location_id FROM schools WHERE rmp_id = ? AND name like ? AND location_id = ? LIMIT 1";
 		Connection connection = DBFactory.getConnection();
 		PreparedStatement statement = null;
 		ResultSet resultSet = null;
 		try
 		{
 
 			statement = connection.prepareStatement(findQuery);
 			statement.setString(1, rmpId);
 			statement.setString(2, name);
 			statement.setString(3, location.getId());
 			resultSet = statement.executeQuery();
 			if (resultSet.next())
 			{
 				System.out.println("School already exists");
 				return new School(resultSet);
 			}
 			else
 			{
 				School school = new School(rmpId, name, location);
 				school.save();
 				return school;
 			}
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			System.exit(1);
 		}
 		finally
 		{
 			try
 			{
 				statement.close();
 				resultSet.close();
 				DBFactory.closeConnection(connection);
 			}
 			catch (SQLException e)
 			{
 				e.printStackTrace();
 				System.exit(1);
 			}
 		}
 		return null;
 	}
 
 	public String getId()
 	{
 		return this.id;
 	}
 
 	public Location getLocation()
 	{
 		return this.location;
 	}
 
 	public String getLocationId()
 	{
 		return this.locationId;
 	}
 
 	public String getName()
 	{
 		return this.name;
 	}
 
 	public String getRmpId()
 	{
 		return this.rmpId;
 	}
 
 	public ArrayList<Teacher> getTeachers()
 	{
 		return this.teachers;
 	}
 
 	@Override
 	public String isValid()
 	{
 		String valid = null;
 		if (!this.isFresh())
 		{
 			valid = (this.getId() != null) ? null
 					: "An existing school requires an ID";
 		}
 		if (valid == null)
 		{
 			valid = (this.getName() != null) ? null
 					: "A school requires a name";
 		}
 		if (valid == null)
 		{
 			valid = (this.getLocationId() != null) ? null
 					: "A school requires a location id";
 		}
 		if (valid == null)
 		{
 			valid = (this.getRmpId() != null) ? null
 					: "A school requires an rmp_id";
 		}
 		return valid;
 	}
 
 	@Override
 	public boolean save() throws InvalidModelException
 	{
 		int result = 0;
 
 		String error = this.isValid();
 		if (error == null)
 		{
 			Connection connection = DBFactory.getConnection();
 			PreparedStatement insertNewSchool = null;
 			PreparedStatement selectId = null;
 			ResultSet resultSet = null;
 			if (this.isFresh())
 			{
 				String insertQuery = "INSERT INTO schools (rmp_id, location_id, name) VALUES (?, ?, ?)";
 				try
 				{
 					insertNewSchool = connection.prepareStatement(insertQuery);
 					insertNewSchool.setString(1, this.getRmpId());
 					insertNewSchool.setString(2, this.getLocation().getId());
 					insertNewSchool.setString(3, this.getName());
 					result = insertNewSchool.executeUpdate();
 					if (result > 0)
 					{
 						String idQuery = "SELECT id FROM schools WHERE rmp_id = ? AND location_id = ? AND name like ?";
 						selectId = connection.prepareStatement(idQuery);
 						selectId.setString(1, this.getRmpId());
 						selectId.setString(2, this.getLocation().getId());
 						selectId.setString(3, this.getName());
 						resultSet = selectId.executeQuery();
 						if (resultSet.next())
 						{
 							String id = resultSet.getString(1);
 							System.out.println("save get id: " + id);
 							this.setId(id);
 							this.fresh = false;
 							this.dirty = false;
 							return true;
 						}
 						else
 						{
 							return false;
 						}
 					}
 				}
 				catch (SQLException e)
 				{
 					e.printStackTrace();
 					System.exit(1);
 				}
 				finally
 				{
 					try
 					{
 						insertNewSchool.close();
 						selectId.close();
 						resultSet.close();
 						DBFactory.closeConnection(connection);
 					}
 					catch (SQLException e)
 					{
 						e.printStackTrace();
 						System.exit(1);
 					}
 				}
 
 			}
 			else
 			{
 				String updateQuery = "UPDATE schools SET rmp_id=?, name=?, location_id=? WHERE id=?";
 				PreparedStatement updateSchool = null;
 				try
 				{
 					updateSchool = connection.prepareStatement(updateQuery);
 					updateSchool.setString(1, this.getRmpId());
 					updateSchool.setString(2, this.getName());
 					updateSchool.setString(3, this.getLocation().getId());
 					updateSchool.setString(4, this.getId());
 					result = updateSchool.executeUpdate();
 					if (result > 0)
 					{
 						this.dirty = false;
 						return true;
 					}
 				}
 				catch (SQLException e)
 				{
 					e.printStackTrace();
 					System.exit(1);
 				}
 				finally
 				{
 					try
 					{
 						updateSchool.close();
 						DBFactory.closeConnection(connection);
 					}
 					catch (SQLException e)
 					{
 						e.printStackTrace();
 						System.exit(1);
 					}
 				}
 			}
 		}
 		else
 		{
 			throw new InvalidModelException(error);
 		}
 		return false;
 	}
 
 	public void setId(String id)
 	{
 		this.id = id;
 	}
 
 	public void setLocation(Location location)
 	{
 		this.location = location;
 		this.setLocationId(location.getId());
 	}
 
 	public void setLocationId(String locationId)
 	{
 		this.locationId = locationId;
 		this.location = null;
 	}
 
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	public void setRmpId(String rmpId)
 	{
 		this.rmpId = rmpId;
 	}
 
 	public void setTeachers(ArrayList<Teacher> teachers)
 	{
 		this.teachers = teachers;
 	}
 
 }
