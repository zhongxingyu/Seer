 package com.lavans.lacoder2.generator.writer;
 
 import com.lavans.lacoder2.generator.model.Entity;
 
 public class EntityManagerWriter {
 	//private static Log logger = LogFactory.getLog(EntityManagerWriter.class);
 
 	private Entity entity;
 
 	public EntityManagerWriter(Entity entity){
 		this.entity = entity;
 	}
 
 	public Entity getEntity() {
 		return entity;
 	}
 
 	public void setEntity(Entity entity) {
 		this.entity = entity;
 	}
 
 	/**
 	 * このEntityに必要なimportを列挙する。
 	 * @return
 	 */
 	public String writeImports(){
 		StringBuffer buf = new StringBuffer();
 		buf.append("import java.util.List;\n");
 		if(entity.hasDate()){
 			buf.append("import java.util.Date;\n");
 		}
 		if(entity.hasInsertDatetime() || entity.hasUpdateDatetime()){
 			buf.append("import lombok.val;\n");
 		}
 		buf.append("\n");
 		buf.append("import com.lavans.lacoder2.di.BeanManager;\n");
 		buf.append("import com.lavans.lacoder2.lang.LogUtils;\n");
 		buf.append("import com.lavans.lacoder2.sql.dao.BaseDao;\n");
 		buf.append("import com.lavans.lacoder2.sql.dao.Condition;\n");
 		if(entity.isCached()){
 			buf.append("import com.lavans.lacoder2.cache.CacheHandler;\n");
 			buf.append("import com.lavans.lacoder2.cache.CacheManager;\n");
 			buf.append("import com.lavans.lacoder2.cache.ValidTerm;\n");
 			buf.append("import java.util.concurrent.TimeUnit;\n");
 			buf.append("import com.google.common.base.Optional;\n");
 			buf.append("import net.arnx.jsonic.JSON;\n");
 			buf.append("\n");
 
 		}
 		buf.append("import com.lavans.lacoder2.util.PageInfo;\n");
 		buf.append("import com.lavans.lacoder2.util.Pager;\n");
 		buf.append("import "+ entity.getParentPackage().getModelSubPackagePath()+".entity."+entity.getClassName()+";\n");
 		buf.append("\n");
 		buf.append("import org.slf4j.Logger;\n");
 		buf.append("\n");
 
 		return buf.toString();
 	}
 
 	/**
 	 * 定数定義書き出し。
 	 * @return
 	 */
 	public String writeFielsds() {
 		StringBuffer buf = new StringBuffer();
 		buf.append("	/** logger */\n");
 		buf.append("	private static final Logger logger = LogUtils.getLogger();\n");
 		buf.append("	/** dao */\n");
 		buf.append("	private BaseDao baseDao = BeanManager.getBean(BaseDao.class);\n");
 		if(entity.isCached()){
 			buf.append("	/** キャッシュマネージャー。 */\n");
 			buf.append("	private CacheManager&lt;"+ entity.getClassName() +".PK, Optional&lt;"+ entity.getClassName() +"&gt;&gt; cacheManager = BeanManager.getBean(CacheManager.class.getName());\n");
 		}
 
 		return buf.toString();
 	}
 
 	public String writeConstructor() {
 		StringBuffer buf = new StringBuffer();
 		buf.append("		logger.debug(\"\");\n");
 		if(entity.isCached()){
 			buf.append("		cacheManager.setCacheHandler(new "+ entity.getClassName() +"CacheHandler());\n");
 		}
 		return buf.toString();
 	}
 
 	public String writeInsertDate() {
 		if(!entity.hasInsertDatetime()){
 			return "";
 		}
 		StringBuffer buf = new StringBuffer();
		buf.append("		val now = new Date();\n");
 		buf.append("		entity.setInsertDatetime(now);\n");
 		buf.append("		entity.setUpdateDatetime(now);\n");
 		return buf.toString();
 	}
 
 	public String writeUpdateDate() {
 		if(!entity.hasUpdateDatetime()){
 			return "";
 		}
 		StringBuffer buf = new StringBuffer();
		buf.append("		val now = new Date();\n");
 		buf.append("		entity.setUpdateDatetime(now);\n");
 		return buf.toString();
 	}
 
 }
