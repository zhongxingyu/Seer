 /**
  * 
  */
 package org.fubme.persistency.mappings;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.fubme.models.User;
 import org.fubme.models.UserList;
 import org.fubme.persistency.DBConnection;
 
 /**
  * @author riccardo
  * 
  */
 public abstract class UserMapper {
 
 	public static final void createUser(User user) {
 		Connection connection = null;
 		PreparedStatement stmt = null;
 		PreparedStatement stmt1 = null;
 		String sql = "INSERT INTO fuser (id,pswd,email) VALUES ( ?, ?, ? );";
 
 		String sql1 = "INSERT INTO luser (id,bio,birthdate,firstname,lastname) VALUES ( ?, ?, ?, ?, ? )";
 		try {
 			connection = DBConnection.getConnection();
 			stmt = connection.prepareStatement(sql);
 			stmt.setString(1, user.getId());
 			stmt.setString(2, user.getPswd());
 			stmt.setString(3, user.getEmail());
 			stmt.executeUpdate();
 
 			stmt1 = connection.prepareStatement(sql1);
 			stmt.setString(1, user.getId());
 			stmt.setString(2, (user.getBio() != null) ? user.getBio() : null);
 			stmt.setTimestamp(3,
 					(user.getBirthdate() != null) ? user.getBirthdate() : null);
 			stmt.setString(4,
 					(user.getFirstname() != null) ? user.getFirstname() : null);
 			stmt.setString(5, (user.getLastname() != null) ? user.getLastname()
 					: null);
 
 			stmt1.executeUpdate();
 		} catch (SQLException ex) {
 			Logger.getLogger(UserMapper.class.getName()).log(Level.SEVERE,
 					null, ex);
 		} finally {
 			if (stmt != null)
 				try {
 					stmt.close();
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 				}
 
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 	}
 
 	public static final boolean checkUserData(String attribute, String value) {
 		boolean exist = true;
 		Connection connection = null;
 		PreparedStatement stmt = null;
 		ResultSet resultset = null;
 		String sql = "SELECT " + attribute + " FROM fuser where " + attribute
 				+ "= ?";
 		try {
 			connection = DBConnection.getConnection();
 			stmt = connection.prepareStatement(sql);
 			stmt.setString(1, value);
 			resultset = stmt.executeQuery();
 			System.out.println(attribute + "\t" + value);
 			if (resultset.next())
 				return true;
 			return false;
 		} catch (SQLException ex) {
 			Logger.getLogger(UserMapper.class.getName()).log(Level.SEVERE,
 					null, ex);
 		} finally {
 			if (stmt != null)
 				try {
 					stmt.close();
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 				}
 			if (resultset != null) {
 				try {
 					resultset.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return exist;
 	}
 
 	public static final void follows(User follower, User toBeFollowed) {
 		Connection connection = null;
 		PreparedStatement stmt = null;
 		String sql = "INSERT INTO luser_follows_luser (luser_id_follower,luser_id_followed) VALUES ( ?, ? )";
 		try {
 			connection = DBConnection.getConnection();
 			stmt = connection.prepareStatement(sql);
 			stmt.setString(1, follower.getId());
 			stmt.setString(2, toBeFollowed.getId());
 			stmt.executeUpdate();
 		} catch (SQLException ex) {
 			Logger.getLogger(UserMapper.class.getName()).log(Level.SEVERE,
 					null, ex);
 		} finally {
 			if (stmt != null)
 				try {
 					stmt.close();
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 				}
 
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public static final void unfollows(User follower, User toBeUnfollowed) {
 		Connection connection = null;
 		PreparedStatement stmt = null;
 		String sql = "DELETE FROM luser_follows_luser where luser_id_follower = ? and luser_id_followed = ?)";
 		try {
 			connection = DBConnection.getConnection();
 			stmt = connection.prepareStatement(sql);
 			stmt.setString(1, follower.getId());
 			stmt.setString(2, toBeUnfollowed.getId());
 			stmt.executeUpdate();
 		} catch (SQLException ex) {
 			Logger.getLogger(UserMapper.class.getName()).log(Level.SEVERE,
 					null, ex);
 		} finally {
 			if (stmt != null)
 				try {
 					stmt.close();
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 				}
 
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public static final void addAUserToUserList(User listOwner, UserList list,
 			User toBeAdded) {
 		Connection connection = null;
 		PreparedStatement stmt = null;
 		String sql = "SELECT * from luser_lists_luser where id = ? and luser_id_listed = ?";
 
 		try {
 			connection = DBConnection.getConnection();
 			stmt = connection.prepareStatement(sql);
 			stmt.setString(1, list.getId());
 			stmt.setString(2, toBeAdded.getId());
 			ResultSet rList = stmt.executeQuery();
 			if (!rList.next()) {
 				sql = "INSERT INTO luser_lists_luser (id, luser_id_list_owner,luser_id_listed) VALUES ( ?, ?, ? )";
 				stmt.close();
 				stmt = connection.prepareStatement(sql);
 				stmt.setString(1, list.getId());
 				stmt.setString(2, listOwner.getId());
 				stmt.setString(3, toBeAdded.getId());
 				stmt.executeUpdate();
 			}
 		} catch (SQLException ex) {
 			Logger.getLogger(UserMapper.class.getName()).log(Level.SEVERE,
 					null, ex);
 		} finally {
 			if (stmt != null)
 				try {
 					stmt.close();
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 				}
 
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public static final List<User> getFollowers(User user) {
 		List<User> followers = new ArrayList<User>();
 		Connection connection = null;
 		PreparedStatement stmt = null;
 		ResultSet resultset = null;
		String sql = "select * from luser where id in (select luser_id_follower from luser_follows_luser where luser_id_followed = ?";
 		try {
 			connection = DBConnection.getConnection();
 			stmt = connection.prepareStatement(sql);
 			stmt.setString(1, user.getId());
 			resultset = stmt.executeQuery();
 			while (resultset.next()) {
 				followers.add(new User(resultset.getString("id"), null));
 			}
 			return followers;
 		} catch (SQLException ex) {
 			Logger.getLogger(UserMapper.class.getName()).log(Level.SEVERE,
 					ex.getMessage(), ex);
 		} finally {
 			if (stmt != null)
 				try {
 					stmt.close();
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 				}
 			if (resultset != null) {
 				try {
 					resultset.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return null;
 
 	}
 
 	public static final List<User> getFollowing(User user) {
 		List<User> following = new ArrayList<User>();
 		Connection connection = null;
 		PreparedStatement stmt = null;
 		ResultSet resultset = null;
		String sql = "select * from luser where id in (select luser_id_followed from luser_follows_luser where luser_id_follower = ?";
 		try {
 			connection = DBConnection.getConnection();
 			stmt = connection.prepareStatement(sql);
 			stmt.setString(1, user.getId());
 			resultset = stmt.executeQuery();
 			while (resultset.next()) {
 				following.add(new User(resultset.getString("id"), null));
 			}
 			return following;
 		} catch (SQLException ex) {
 			Logger.getLogger(UserMapper.class.getName()).log(Level.SEVERE,
 					null, ex);
 		} finally {
 			if (stmt != null)
 				try {
 					stmt.close();
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 				}
 			if (resultset != null) {
 				try {
 					resultset.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return null;
 
 	}
 
 	public static final User getUserInfo(User user) {
 		Connection connection = null;
 		PreparedStatement stmt = null;
 		ResultSet resultset = null;
 		String sql = "select * from luser where id = ?";
 
 		try {
 			connection = DBConnection.getConnection();
 			stmt = connection.prepareStatement(sql);
 			stmt.setString(1, user.getId());
 			resultset = stmt.executeQuery();
 			while (resultset.next()) {
 				return new User(resultset.getString("id"), null, null,
 						resultset.getString("bio"),
 						resultset.getString("firstname"),
 						resultset.getString("lastname"),
 						resultset.getTimestamp("birthdate"),
 						resultset.getString("location"));
 			}
 		} catch (SQLException ex) {
 			Logger.getLogger(UserMapper.class.getName()).log(Level.SEVERE,
 					null, ex);
 		} finally {
 			if (stmt != null)
 				try {
 					stmt.close();
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 				}
 			if (resultset != null) {
 				try {
 					resultset.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return null;
 	}
 }
