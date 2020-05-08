 package com.laivi.sic;
 
 
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
 
 import org.nutz.dao.Cnd;
 import org.nutz.dao.Dao;
 import org.nutz.dao.entity.annotation.Table;
 import org.nutz.log.Log;
 import org.nutz.log.Logs;
 import org.nutz.mvc.NutConfig;
 import org.nutz.mvc.Setup;
 import org.nutz.resource.Scans;
 
 import com.laivi.sic.model.po.blog.Article;
 import com.laivi.sic.model.po.blog.Tag;
 import com.laivi.sic.model.po.user.LoginUser;
 import com.laivi.sic.model.po.user.Role;
 import com.laivi.sic.model.po.user.User;
 import com.laivi.sic.model.type.CategoryType;
import com.laivi.sic.service.blog.ArticleService;
import com.laivi.sic.util.basic.DBUtil;
 
 public class NutzSetUp implements Setup{
 	
 	private static final Log log=Logs.get();
 
 	@Override
 	public void init(NutConfig config) {
 		log.debug("config Ioc=="+config.getIoc());
 		Dao dao=config.getIoc().get(Dao.class);
 		for (Class<?> klass : Scans.me().scanPackage("com.laivi.sic.model.po")) {
 			if(null!=klass.getAnnotation(Table.class)){
 				dao.create(klass, false);
 			}
 		}
 		
 		if(dao.count(User.class)==0){
 			User user=new User();
 			user.setName("laivi");
 			user.setDescription("laivi");
 			user.setRoleIds("1");
 			dao.insert(user);
 		}
 		
 		if(dao.count(LoginUser.class)==0){
 			LoginUser user=new LoginUser();
 			user.setEmail("laivi.zhu@gmail.com");
 			user.setPassword("laivi");
 			user.setUidendity("laivi.zhu@gmail.com");
 			user.setUserId(1);
 			dao.insert(user);
 		}
 		
 		if(dao.count(Role.class)==0){
 			Role[] roles=new Role[2];
 			roles[0]=new Role();
 			roles[0].setName("ROLE_ADMIN");
 			roles[0].setDescription("管理员");
 			
 			roles[1]=new Role();
 			roles[1].setName("ROLE_USER");
 			roles[1].setDescription("普通用户");
 			
 			for(Role role:roles){
 				dao.insert(role);
 			}
 		}
 		
 		if(dao.count(Tag.class)==0){
 			Tag[] tags=new Tag[11];
 			tags[0]=new Tag("言情",CategoryType.BOOK);
 			tags[1]=new Tag("都市",CategoryType.BOOK);
 			tags[2]=new Tag("情感",CategoryType.ARTICLE);
 			tags[3]=new Tag("哲理",CategoryType.BOOK);
 			tags[4]=new Tag("散文",CategoryType.BOOK);
 			tags[5]=new Tag("生活",CategoryType.ARTICLE);
 			tags[6]=new Tag("感悟",CategoryType.ARTICLE);
 			tags[7]=new Tag("侦探",CategoryType.BOOK);
 			tags[8]=new Tag("武侠",CategoryType.BOOK);
 			tags[9]=new Tag("java",CategoryType.KNOWLEDGE);
 			tags[10]=new Tag("技术",CategoryType.ARTICLE);
 			for(Tag tag:tags){
 				dao.insert(tag);
 			}
 		}
		ArticleService articleService=config.getIoc().get(ArticleService.class);
		Connection conn=DBUtil.getConnection();
 		try {
 			Statement st=conn.createStatement();
 			ResultSet rs = st.executeQuery("select * from t_article");
 			while(rs.next()){
 				System.out.println("-------------------------------------------------------------------id:"+rs.getLong("id"));
				if(dao.count(Article.class, Cnd.where("title", "=", rs.getString("title")))==0&&rs.getLong("id")!=419&&rs.getLong("id")!=775){
 					Article article=new Article();
 					article.setContent(rs.getString("content"));
 					article.setTitle(rs.getString("title"));
 					article.setTagId(Long.parseLong(rs.getString("tagIds")));
 					article.setUserId(1);
					articleService.add(article);
 				}
 			}
 		} catch (SQLException e) {
 			System.out.println("******************************************error**********************************");
 			e.printStackTrace();
		}
 		
 		/*List<Article> articleList=dao.query(Article.class, null);
 		for(int i=0;i<articleList.size();i++){
 			if(dao.count(SimpleDegree.class, Cnd.where("objId", "=", articleList.get(i).getId()).and("type", "=", CategoryType.ARTICLE))==0){
 				SimpleDegree simple=new SimpleDegree();
 				simple.setObjId(articleList.get(i).getId());
 				simple.setType(CategoryType.ARTICLE);
 				for(int j=0;j<articleList.size();j++){
 					if(i==j){
 						continue;
 					}
 					String degree=DataUtil.getSimilarDegree(articleList.get(i).getTitle(), articleList.get(j).getTitle());
 					if(Double.parseDouble(degree)==0){
 						continue;
 					}
 					DataUtil.setSampleDegree(simple,degree,articleList.get(j).getId());
 				}
 				dao.insert(simple);
 			}
 		}*/
 	}
 
 	@Override
 	public void destroy(NutConfig config) {
 		
 	}
 
 }
