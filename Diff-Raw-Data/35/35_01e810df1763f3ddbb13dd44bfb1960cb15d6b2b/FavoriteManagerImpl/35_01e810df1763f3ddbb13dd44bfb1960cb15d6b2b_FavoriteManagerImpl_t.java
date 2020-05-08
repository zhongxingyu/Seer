 package edu.thu.keg.mdap.management.impl.favorite;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.thu.keg.mdap.management.favorite.Favorite;
 import edu.thu.keg.mdap.management.favorite.IFavoriteManager;
 import edu.thu.keg.mdap.management.impl.provider.SqlServerProviderImpl;
 import edu.thu.keg.mdap.management.provider.AbsSqlServerProvider;
 import edu.thu.keg.mdap.management.provider.IllegalFavManageException;
 
 public class FavoriteManagerImpl implements IFavoriteManager {
 	public static FavoriteManagerImpl instance;
 
 	public static FavoriteManagerImpl getInstance() {
 		System.out.print("instance ");
 		// TODO multi-thread
 		if (instance == null)
 			instance = new FavoriteManagerImpl();
 
 		return instance;
 	}
 
 	@Override
 	public int addFav(String userid, String favstr) throws SQLException,
 			IllegalFavManageException {
 		System.out.println("add fav ");
 		String sql = "insert into [Favorite] ( userid,  favstring) "
 				+ " values (?,?)";
 		AbsSqlServerProvider ssp = null;
 		ssp = SqlServerProviderImpl.getInstance();
 		PreparedStatement pstmt = ssp.getConnection().prepareStatement(sql,
 				Statement.RETURN_GENERATED_KEYS);
 		pstmt.setString(1, userid);
 		pstmt.setString(2, favstr);
 		pstmt.executeUpdate();
 		List<Integer> favid_list = getFavid(userid, favstr);
 		pstmt.getConnection().close();
 		return favid_list.get(favid_list.size() - 1);
 	}
 
 	private List<Integer> getFavid(String userid, String favstr) {
 		String sql = "select favid" + " from [Favorite]"
 				+ " where (userid=? and favstring= ?) order by favid";
 		AbsSqlServerProvider ssp = null;
 		ssp = SqlServerProviderImpl.getInstance();
 		List<Integer> favid_list = new ArrayList<>();
 		PreparedStatement pstmt = null;
 		try {
 			pstmt = ssp.getConnection().prepareStatement(sql);
 			pstmt.setString(1, userid);
 			pstmt.setString(2, favstr);
 			ResultSet rs = pstmt.executeQuery();
 			while (rs.next()) {
 				favid_list.add(rs.getInt("favid"));
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			try {
 				pstmt.getConnection().close();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		return favid_list;
 	}
 
 	@Override
 	public Favorite getFav(String userid, String favid) throws SQLException,
 			IllegalFavManageException {
 		String sql = "select favstring" + " from [Favorite]"
 				+ " where (userid = ? and favid = ?)";
 		AbsSqlServerProvider ssp = null;
 		ssp = SqlServerProviderImpl.getInstance();
 		PreparedStatement pstmt = ssp.getConnection().prepareStatement(sql);
 		pstmt.setString(1, userid);
		pstmt.setInt(2, Integer.parseInt(favid));
 		ResultSet rs = pstmt.executeQuery();
 		rs.next();
		Favorite fav= new Favorite(userid, favid, rs.getString("favstring"));
 		pstmt.getConnection().close();
		return fav;
 	}
 
 	@Override
 	public List<Favorite> getAllFavs(String userid) throws SQLException,
 			IllegalFavManageException {
 		String sql = "select favid, favstring" + " from [Favorite]"
 				+ " where (userid = ? )";
 		AbsSqlServerProvider ssp = null;
 		ssp = SqlServerProviderImpl.getInstance();
 		PreparedStatement pstmt = ssp.getConnection().prepareStatement(sql);
 		pstmt.setString(1, userid);
 		ResultSet rs = pstmt.executeQuery();
 		List<Favorite> favs = new ArrayList<>();
 		while (rs.next()) {
 			favs.add(new Favorite(userid, rs.getString("favid"), rs
 					.getString("favstring")));
 		}
 		pstmt.getConnection().close();
 		return favs;
 	}
 
 	@Override
 	public boolean setFavid(String userid, String oldfavid, String newfavid)
 			throws SQLException, IllegalFavManageException {
 		String sql = "update [Favorite] set favid = ? "
 				+ "where (userid=? and favid=?)";
 		AbsSqlServerProvider ssp = null;
 		ssp = SqlServerProviderImpl.getInstance();
 		PreparedStatement pstmt = ssp.getConnection().prepareStatement(sql,
 				Statement.RETURN_GENERATED_KEYS);
 		pstmt.setString(1, newfavid);
 		pstmt.setString(2, userid);
 		pstmt.setString(3, oldfavid);
 		pstmt.executeUpdate();
 		ResultSet rs = pstmt.getGeneratedKeys();
 		pstmt.getConnection().close();
 		return true;
 	}
 
 	@Override
 	public boolean setFavstring(String userid, String favid, String newfavstring)
 			throws SQLException, IllegalFavManageException {
 		String sql = "update [Favorite] set favstring = ? "
 				+ "where ( userid = ? and favid= ? )";
 		AbsSqlServerProvider ssp = null;
 		// if (!isFavExsist(userid, favid))
 		// throw new IllegalFavManageException("the favrite already exists!");
 		ssp = SqlServerProviderImpl.getInstance();
 		PreparedStatement pstmt = ssp.getConnection().prepareStatement(sql,
 				Statement.RETURN_GENERATED_KEYS);
 		pstmt.setString(1, newfavstring);
 		pstmt.setString(2, userid);
 		pstmt.setString(3, favid);
 		pstmt.executeUpdate();
 		ResultSet rs = pstmt.getGeneratedKeys();
 		pstmt.getConnection().close();
 		return true;
 	}
 
 	@Override
 	public boolean removeFav(String userid, String favid) throws SQLException,
 			IllegalFavManageException {
 		String sql = "delete from [Favorite] "
 				+ "where (userid = ? and favid = ? )";
 		AbsSqlServerProvider ssp = null;
 		if (!isFavExsist(userid, favid))
 			throw new IllegalFavManageException("the favrite don't exists!");
 		ssp = SqlServerProviderImpl.getInstance();
 		PreparedStatement pstmt = ssp.getConnection().prepareStatement(sql,
 				Statement.RETURN_GENERATED_KEYS);
 		pstmt.setString(1, userid);
 		pstmt.setString(2, favid);
 		pstmt.executeUpdate();
 		pstmt.getConnection().close();
 		return true;
 	}
 
 	@Override
 	public boolean removeAllFav(String userid) throws SQLException,
 			IllegalFavManageException {
 		String sql = "delete from [Favorite] " + "where (userid=?)";
 		AbsSqlServerProvider ssp = null;
 
 		ssp = SqlServerProviderImpl.getInstance();
 		PreparedStatement pstmt = ssp.getConnection().prepareStatement(sql,
 				Statement.RETURN_GENERATED_KEYS);
 		pstmt.setString(1, userid);
 		pstmt.executeUpdate();
 		ResultSet rs = pstmt.getGeneratedKeys();
 		pstmt.getConnection().close();
 		return true;
 	}
 
 	@Override
 	public boolean isFavExsist(String userid, String favid)
 			throws SQLException, IllegalFavManageException {
 		String sql = "select userid" + " from [Favorite]"
 				+ " where ( userid = ? and favid = ? )";
 		AbsSqlServerProvider ssp = null;
 
 		ssp = SqlServerProviderImpl.getInstance();
 		PreparedStatement pstmt = ssp.getConnection().prepareStatement(sql);
 		pstmt.setString(1, userid);
 		pstmt.setString(2, favid);
 		ResultSet rs = pstmt.executeQuery();
 		if (rs.next()) {
 			pstmt.getConnection().close();
 			return true;
 		}
 		pstmt.getConnection().close();
 		return false;
 	}
 
 }
