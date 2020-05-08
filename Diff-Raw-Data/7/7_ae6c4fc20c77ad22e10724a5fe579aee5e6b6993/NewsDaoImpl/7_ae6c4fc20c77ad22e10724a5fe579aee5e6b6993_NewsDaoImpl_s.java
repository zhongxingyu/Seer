 package cn.edu.sicau.rs.daoimpl;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 
 import cn.edu.sicau.rs.bean.News;
 import cn.edu.sicau.rs.bean.NewsPager;
 import cn.edu.sicau.rs.bean.User;
 import cn.edu.sicau.rs.common.DbUtil;
 import cn.edu.sicau.rs.common.HibernateUtil;
 import cn.edu.sicau.rs.dao.NewsDao;
 
 public class NewsDaoImpl implements NewsDao{
 
 	@Override
 	public boolean addNews(News news) {
 		boolean flag = false;
 		Session s = null;
 		Transaction tx = null;
 		try {
 			s = HibernateUtil.getSession();
 			tx = s.beginTransaction();
 			s.save(news);
 			tx.commit();
 			flag = true;
 		} catch (HibernateException e) {
 			e.printStackTrace();
 		}finally {
 			s.close();
 		}
 		return flag;
 	}
 
 	@Override
 	public boolean delNews(int ids[]) {
 		DbUtil dbutil = null;
 		PreparedStatement ps = null;
 		Connection conn = null;
 		String sql = "delete from tb_news where id = ?";
 		try {
 			dbutil = new DbUtil();
 			conn = dbutil.getCon();
 			conn.setAutoCommit(false);
 			ps = conn.prepareStatement(sql);
 			for(int j = 0;j<ids.length;j++) {
 				ps.setInt(1, ids[j]);
 				ps.addBatch();
 			}
 			int[] k = ps.executeBatch();
 			conn.commit();
 			if(k.length == ids.length){
 				return true;
 			}
 		} catch (Exception e) {
 			try {
 				conn.rollback();
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}finally {
 			try {
 				ps.close();
 				dbutil.close();
 			} catch(SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean updateNews() {
 		
 		
 		return false;
 	}
 
 	@Override
 	public Map getAllNews(int type) {
		// TODO Auto-generated method stub
 		Map newsMap = new HashMap();
 		DbUtil dbutil = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		String sql = "select * from tb_news where type = ? order by top desc , createtime desc";
 		try {
 			dbutil = new DbUtil();
 			ps = dbutil.getCon().prepareStatement(sql);
 			ps.setInt(1, type);
 			rs = ps.executeQuery();
 			while (rs.next()) {
 				News news = new News();
 				news.setId(rs.getInt("id"));
 				news.setSubject(rs.getString("subject"));
 				news.setCreateTime(rs.getString("createtime"));
 				news.setContent(rs.getString("content"));
 				news.setAuthor(rs.getString("author"));
 				news.setType(rs.getString("type"));
 				news.setTop(rs.getString("top"));
 				newsMap.put(new Integer(news.getId()),news);     
 				
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				rs.close();
 				ps.close();
 				dbutil.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return newsMap;
 		
 	}
 	
 	public List getAllNewses(int type) {
 		// TODO Auto-generated method stub
 		List newsList = new ArrayList();
 		DbUtil dbutil = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		String sql = null;
 		if(type == 0) {
 			sql = "select * from tb_news where type = ? and top = 1 order by top desc , createtime desc";
 		}else{
 			sql = "select * from tb_news where type = ? order by top desc , createtime desc";
 		}
 		
 		try {
 			dbutil = new DbUtil();
 			ps = dbutil.getCon().prepareStatement(sql);
 			ps.setInt(1, type);
 			rs = ps.executeQuery();
 			while (rs.next()) {
 				News news = new News();
 				news.setId(rs.getInt("id"));
 				news.setSubject(rs.getString("subject"));
 				news.setCreateTime(rs.getString("createtime"));
 				news.setContent(rs.getString("content"));
 				news.setAuthor(rs.getString("author"));
 				news.setType(rs.getString("type"));
 				news.setTop(rs.getString("top"));
 				newsList.add(news);     
 				
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				rs.close();
 				ps.close();
 				dbutil.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return newsList;
 		
 	}
 
 	@Override
 	public News getByID(int id) {
 		// TODO Auto-generated method stub
 		DbUtil dbutil = null;
 		PreparedStatement ps = null;
 		String sql =  "select * from tb_news where id = ?";
 		ResultSet rs = null;
 		try {
 			dbutil = new DbUtil();
 			ps = dbutil.getCon().prepareStatement(sql);
 			ps.setInt(1, id);
 			rs = ps.executeQuery();
 			if(rs.next()) {
 				News news = new News();
 				news.setId(rs.getInt("id"));
 				news.setSubject(rs.getString("subject"));
 				String sub = rs.getString("subject");
 				System.out.println(sub);
 				news.setContent(rs.getString("content"));
 				news.setCreateTime(rs.getString("createtime"));
 				news.setType(rs.getString("type"));
 				news.setAuthor(rs.getString("author"));
 				news.setTop(rs.getString("top"));
 				return news;
 			} else {
 				System.out.println("Ųڣ");
 			}
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 
 	@Override
 	public boolean delNews(int id) {
 		// TODO Auto-generated method stub
 		DbUtil dbutil = null;
 		PreparedStatement ps = null;
 		Connection conn = null;
 		String sql = "delete from tb_news where id = ?";
 		try {
 			dbutil = new DbUtil();
 			conn = dbutil.getCon();
 			ps = conn.prepareStatement(sql);
 			ps.setInt(1, id);
 			int i = ps.executeUpdate();
 			if(i != 0) {
 				return true;
 			}
 		} catch (Exception e) {
 			try {
 				conn.rollback();
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}finally {
 			try {
 				ps.close();
 				dbutil.close();
 			} catch(SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean topSign(int id,String top) {
 		// TODO Auto-generated method stub
 		DbUtil dbutil = null;
 		PreparedStatement ps = null;
 		String sql;
 		if(top.equals("1")){
 			sql = "update tb_news set top = 0 where id = ?";
 		} else {
 			sql = "update tb_news set top = 1 where id = ?";
 		}
 		
 		try {
 			dbutil = new DbUtil();
 			ps = dbutil.getCon().prepareStatement(sql);
 			ps.setInt(1, id);
 			int i = ps.executeUpdate();
 			if(i != 0) {
 				return true;
 			}
 		} catch(SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				ps.close();
 				dbutil.close();
 			}catch(SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public NewsPager getNewsPager(int index, int pageSize, int type) {
 		// TODO Auto-generated method stub
 		Map newsMap = new HashMap();
 		DbUtil dbutil = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		String sql = "select * from tb_news where type = ? limit ?,?";
 		try {
 			dbutil = new DbUtil();
 			ps = dbutil.getCon().prepareStatement(sql);
 			ps.setInt(1, type);
 			ps.setInt(2, index);
 			ps.setInt(3, pageSize);
 			rs = ps.executeQuery();
 			while (rs.next()) {
 				News news = new News();
 				news.setId(rs.getInt("id"));
 				news.setSubject(rs.getString("subject"));
 				news.setCreateTime(rs.getString("createtime"));
 				news.setContent(rs.getString("content"));
 				news.setAuthor(rs.getString("author"));
 				news.setType(rs.getString("type"));
 				news.setTop(rs.getString("top"));
 				newsMap.put(news.getId(),news);     
 				
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 //				rs.close();
 				ps.close();
 				dbutil.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		NewsPager np = new NewsPager();
 		np.setNewsMap(newsMap);
 		np.setPageSize(pageSize);
 		np.setTotalNum(getAllNews(type).size());
 		return np;
 	}
 	
 	public boolean clearSign(){
 		boolean flag = false;
 		DbUtil dbutil = null;
 		PreparedStatement ps = null;
 		String sql = "update tb_news set top = 0 where type = 0";
 		try{
 			dbutil = new DbUtil();
 			ps = dbutil.getCon().prepareStatement(sql);
 			int i = ps.executeUpdate();
 			if(i != 0){
 				flag = true;
 			}
 		}catch(SQLException e){
 			e.printStackTrace();
 		}finally{
 			try{
 				ps.close();
 				dbutil.close();
 			}catch(SQLException e){
 				e.printStackTrace();
 			}
 		}
 		return flag;
 	}
 
 }
