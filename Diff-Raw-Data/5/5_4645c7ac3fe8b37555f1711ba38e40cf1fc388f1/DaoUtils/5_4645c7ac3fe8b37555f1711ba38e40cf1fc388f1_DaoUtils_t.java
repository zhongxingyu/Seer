 package com.lavans.lacoder2.sql.dao;
 
 import java.math.BigDecimal;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.lavans.lacoder2.lang.DateUtils;
 import com.lavans.lacoder2.lang.StringUtils;
 import com.lavans.lacoder2.sql.dbutils.dbms.DbmsFactory;
 import com.lavans.lacoder2.sql.dbutils.enums.DbmsType;
 import com.lavans.lacoder2.util.Config;
 
 public class DaoUtils {
 	/** ロガー */
 	private static Logger logger = LoggerFactory.getLogger(DaoUtils.class);
 	//private static final String CLASSNAME=DaoUtils.class.getName();
 
 	/**
 	 * IN句の文字列作成
 	 * 任意の型。
 	 *
 	 * @param <T>
 	 * @param objs
 	 * @param prefix
 	 * @param params パラメータ用Map。ここに格納される。
 	 * @return
 	 */
 	public static String makeInPhrase(String[] objs, String prefix, Map<String, String[]> params){
 		if(objs.length==0) throw new IllegalArgumentException("target's length==0.");
 		StringBuilder str = new StringBuilder();
 		for(int i=0; i<objs.length; i++){
 			str.append(",:"+prefix+i);
 			params.put(prefix+i, new String[]{objs[i]});
 		}
 		return " IN ("+str.substring(1) +")";
 	}
 	public static <T> String makeInPhrase(T[] objs, String prefix, Map<String, Object> params){
 		if(objs.length==0) throw new IllegalArgumentException("target's length==0.");
 		StringBuilder str = new StringBuilder();
 		for(int i=0; i<objs.length; i++){
 			str.append(",:"+prefix+i);
 			params.put(prefix+i, objs[i]);
 		}
 		return " IN ("+str.substring(1) +")";
 	}
 
 	public static String getSql(Class<?> clazz, String key){
 		return getSql(clazz.getName(), key);
 	}
 
 	/** CacheMap for SQL */
 	private static Map<String,String> sqlCacheMap = new ConcurrentHashMap<>();
 	/**
 	 * SQL取得。
 	 * ファイル名と名前をキーにキャッシュする
 	 * @param className
 	 * @param key
 	 * @return
 	 * @throws SQLException
 	 */
 	public static String getSql(String className, String key) {
 		// Cache key
 		String cacheKey = className+"#"+key;
 		// Find from cache.
 		if(sqlCacheMap.containsKey(cacheKey)){
 			return sqlCacheMap.get(cacheKey);
 		}
 		Config config = Config.getInstance(className.replace(".","/")+".xml");
 //			sql = config.getNodeValue("/sql/"+key).trim();
 		String sql = config.getNodeValue("sql[@name='"+key +"']").trim();
 		if(StringUtils.isEmpty(sql)){
 			throw new RuntimeException("sql is not found["+ cacheKey +"]");
 		}
 
 		// Save to cache
 		sqlCacheMap.put(cacheKey, sql);
 
 		return sql;
 	}
 
 	/**
 	 * 検索条件の設定。ここでは特定のprefixがついたkey=valueを取得するだけ
 	 */
 	public static Map<String, String[]> getConditionMap(Map<String, String[]> requestParameters, String prefix){
 		Map<String, String[]> map = new HashMap<String, String[]>();
 		// just only get "search condition paraemters" here. it use later.
 		for(Map.Entry<String, String[]> entry: requestParameters.entrySet()){
 			if(entry.getKey().startsWith(prefix)){
 				map.put(entry.getKey().substring(prefix.length()), entry.getValue());
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * make SQL condition from parameter map<String, String[]>
 	 *
 	 * @param request
 	 * @param prefix
 	 * @return
 	 */
 	public static Condition getCondition(Map<String, String[]> requestParameters, String prefix){
 		return new Condition(getConditionMap(requestParameters, prefix));
 	}
 
 	/**
 	 * make SQL where phrase from search condtions.
 	 * @return
 	 */
 	public static String makeWherePhrase(Condition cond){
 		// null check.
 		if(cond==null){
 			return "";
 		}
 
 		Map<String, String[]> map = cond.getMap();
 
 		// SQL construction start.
 		StringBuilder builder = new StringBuilder();
 		Map<String, String[]> copy = new LinkedHashMap<>(map);
 		Iterator<Map.Entry<String, String[]>> ite = copy.entrySet().iterator();
 		while(ite.hasNext()){
 			proccessCondition(ite.next(), map, builder);
 		}
 
 		String phrase = addWhere(builder);
 
 		return phrase;
 	}
 
 	/**
 	 * Conditionの各項目に対してWHERE句の条件を作成します。
 	 * @param entry
 	 * @param map
 	 * @param builder
 	 */
 	private static void proccessCondition(Map.Entry<String, String[]> entry,Map<String, String[]> map, StringBuilder builder){
 		String key = entry.getKey();
 		// empty check
 		if((map.get(key)==null) || map.get(key).length==0 || StringUtils.isEmpty(map.get(key)[0])){
 			return;
 		}
 		String keys[] = key.split("\\.");
 		// memeberId -> MEMBER_ID
 		String field = StringUtils.toUnderscore(keys[0]).toUpperCase();
 		String typeStr = StringUtils.toUnderscore(keys[1]).toUpperCase();
 		ConditionTypeEnum type = ConditionTypeEnum.valueOf(ConditionTypeEnum.class, typeStr);
 		type.processCondition(key, field, builder, map);
 	}
 
 	/**
 	 * WHERE文字列を足します。
 	 * @param builder
 	 * @return
 	 */
 	private static String addWhere(StringBuilder builder){
 		String phrase = builder.toString();
 
 		// add "WHERE"
 		if(builder.length()>4){
 			// remove first " AND"
 			if(!phrase.startsWith("(")){
 				phrase = phrase.substring(4);
 			}
 			// remove " AND" after "(";
 			phrase = phrase.replace("( AND ", "(");
 			phrase = " WHERE "+ phrase;
 		}
 
 		return phrase;
 	}
 
 	/**
 	 * make SQL ORDER BY phrase from search condtions.
 	 * @return
 	 */
 	public static String makeOrderByPhrase(Condition cond){
 		// null check.
 		if(cond==null){
 			return "";
 		}
 		String result="";
 		if(!StringUtils.isEmpty(cond.getOrderBy())){
 			result = " ORDER BY "+ StringUtils.toUnderscore(cond.getOrderBy());
 		}
 
 		return result;
 	}
 
 	/** TODO DbmsType */
 	private static DbmsType dbmsType=DbmsType.POSTGRES;
 
 	/**
 	 * Limit/Offsetの設定
 	 * @param cond
 	 * @param sql
 	 * @return
 	 */
 	public static String makeLimitOffset(Condition cond, String sql){
 		if(cond.getLimit()==0 && cond.getOffset()==0){
 			return sql;
 		}
 		return DbmsFactory.getDbmsUtils(dbmsType).makeLimitOffset(sql, cond.getLimit(),  cond.getOffset());
 	}
 
 	/**
 	 * 検索条件をMap<String,String[]>からMap<String,Object>に変換。
 	 * InteterとかLongとか型渡して変換した方がよさそう。
 	 *
 	 */
 	public static Map<String, Object> convertSearchCond(Condition cond, Map<String,Class<?>> attributeInfo){
 		Map<String, Object> result = new HashMap<String, Object>();
 		if(cond==null || cond.getMap()==null){
 			return result;
 		}
 		// for editing keys, copy param map.
 		Map<String, String[]> copy = new HashMap<>(cond.getMap());
 		for(Map.Entry<String, String[]> entry: copy.entrySet()){
 			// 値が指定されていなければ評価しない
 			if(entry.getValue()==null || entry.getValue().length==0 || StringUtils.isEmpty(entry.getValue()[0])){
 				continue;
 			}
 
 			String attributeName = getAttributeName(entry.getKey());
 			// この属性の型情報を取得
 			Class<?> clazz = attributeInfo.get(attributeName);
 			// 念のためnullチェック
 			if(clazz==null){
 				logger.debug("No attribute Info,["+ attributeName +"]");
 				continue;
 			}
 
 			setValue(clazz, entry.getKey(), entry.getValue()[0], result);
 		}
 		return result;
 	}
 
 	/**
 	 * キーからフィールド名のみを取得
 	 * @param key
 	 * @return
 	 */
 	private static String getAttributeName(String key){
 		// "."がある場合は属性名は"."より前の部分(ex memberId.equal
 		if(key.contains(".")){
 			String names[]=key.split("\\.");
 			return names[0];
 		}
 		return key;
 	}
 
 	/**
 	 * 型に応じてパラメータをセット
 	 * @param clazz
 	 * @param key
 	 * @param value
 	 * @param result
 	 * @return
 	 */
 	private static Map<String, Object> setValue(Class<?> clazz, String key, String value, Map<String, Object> result){
		if(clazz.equals(Integer.class) || clazz.equals(Integer.TYPE)){
 			result.put(key, Integer.valueOf(value));
		}else if(clazz.equals(Long.class) || clazz.equals(Long.TYPE)){
 			result.put(key, Long.valueOf(value));
 		}else if(clazz.equals(Double.class)){
 			result.put(key, Double.valueOf(value));
 		}else if(clazz.equals(BigDecimal.class)){
 			result.put(key, new BigDecimal(value));
 //		}else if(clazz.equals(byte[].class)){
 //			// バイナリは検索不可
 //			result.put(key, byte.valueOf(value));
 		}else if(clazz.equals(Date.class) || clazz.equals(java.sql.Date.class)){
 			result.put(key, DateUtils.getDate(value));
 		}else{
 			// String
 			result.put(key, value);
 		}
 
 		return result;
 	}
 }
