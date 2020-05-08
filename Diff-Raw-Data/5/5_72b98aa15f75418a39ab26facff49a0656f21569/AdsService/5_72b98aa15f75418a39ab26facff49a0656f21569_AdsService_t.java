 /**
  * Copyright 2011 ASTO.
  * All right reserved.
  * Created on 2011-6-14
  */
 package com.zz91.ads.count.service;
 
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.spy.memcached.MemcachedClient;
 
 import com.zz91.ads.count.domain.Ad;
 import com.zz91.ads.count.domain.AdLog;
 import com.zz91.ads.count.domain.AdPosition;
 import com.zz91.ads.count.dto.AdPositionDto;
 import com.zz91.ads.count.thread.AdLogThread;
 import com.zz91.ads.count.thread.ControlThread;
 import com.zz91.util.cache.MemcachedUtils;
 import com.zz91.util.db.DBUtils;
 import com.zz91.util.db.IReadDataHandler;
 import com.zz91.util.encrypt.MD5;
 import com.zz91.util.lang.StringUtils;
 
 /**
  * @author mays (mays@zz91.com)
  * 
  * created on 2011-6-14
  */
 public class AdsService {
 	
 	public static String CACHE_VERSION="1";
 	public static boolean DEBUG=false;
 	public static final String VERSION="ad";
 	public static final int CACHE_EXPIRE=600;
 	
 	public static final String AD_COLUMN = "id, ad_title, ad_description, ad_content, position_id, gmt_content_modified,expired_rent,sequence,gmt_start,gmt_plan_end";
 	public static final String WHERE_NORMAL = "online_status='Y' and review_status='Y' and gmt_start<now()";
 	
 	private static AdsService _instance = null;
 
 	private AdsService() {
 
 	}
 
 	synchronized public static AdsService getInstance() {
 		if (_instance == null) {
 			_instance = new AdsService();
 		}
 		return _instance;
 	}
 	
 	final static String HIT_URL="http://gg.zz91.com/hit";
 	
 	public AdPositionDto queryPosition(Integer id){
 		
 		StringBuffer sql= new StringBuffer();
 		sql.append("select p.id, p.payment_type, p.width, p.height, p.max_ad, p.has_exact_ad,d.js_function");
 		sql.append(" from ad_position p");
 		sql.append(" inner join delivery_style d on p.delivery_style_id=d.id");
 		sql.append(" where p.id=").append(id);
 		sql.append(" limit 1");
 		
 		final AdPositionDto positionDto=new AdPositionDto();
 		DBUtils.select("zzads",sql.toString() , new IReadDataHandler() {
 			@Override
 			public void handleRead(ResultSet rs) throws SQLException {
 				while (rs.next()) {
 					positionDto.setPosition(new AdPosition(rs.getInt(1), null, null, null, null, null, rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), null));
 					positionDto.setStyle(rs.getString(7));
 				}
 			}
 		});
 		
 		return positionDto;
 	}
 	
 	public Map<Integer, String> queryExactByPosition(Integer id){
 		StringBuffer sql= new StringBuffer();
 		sql.append("select et.id, et.exact_name from position_exact_type pet");
 		sql.append(" inner join exact_type et on pet.exact_type_id=et.id");
 		sql.append(" where pet.ad_position_id=").append(id);
 		sql.append(" order by et.id asc");
 		
 		final Map<Integer, String> m=new LinkedHashMap<Integer, String>();
 		DBUtils.select("zzads", sql.toString(), new IReadDataHandler() {
 			
 			@Override
 			public void handleRead(ResultSet rs) throws SQLException {
 				while(rs.next()){
 					m.put(rs.getInt(1), rs.getString(2));
 				}
 			}
 		});
 		
 		return m;
 	}
 	
 	public List<Ad> queryExactAd(Integer positionId, Integer maxAd, Map<Integer, String> exact){
 		if(positionId==null){
 			return new ArrayList<Ad>();
 		}
 		if(exact==null){
 			return new ArrayList<Ad>();
 		}
 		if(maxAd==null || maxAd.intValue()<1){
 			maxAd=1;
 		}
 		
 		StringBuffer sb=new StringBuffer();
 		sb.append("|");
 		for(Integer k:exact.keySet()){
 			if(StringUtils.isNotEmpty(exact.get(k))){
 				// TODO 关键字替换
 				
 				sb.append(k).append(":").append(saveEncode(exact.get(k))).append("|");
 			}
 		}
 		
 		StringBuffer sql=new StringBuffer();
 		sql.append("(select ").append(AD_COLUMN).append(" from ad");
 		sql.append(" where position_id=").append(positionId);
 		sql.append(" and ").append(WHERE_NORMAL).append(" and now()<gmt_plan_end");
 		sql.append(" and search_exact='").append(sb.toString()).append("' )");
 		sql.append(" union all ");
 		sql.append("(select ").append(AD_COLUMN).append(" from ad");
 		sql.append(" where position_id=").append(positionId);
 		sql.append(" and ").append(WHERE_NORMAL).append("  and gmt_plan_end is null");
 		sql.append(" and search_exact='").append(sb.toString()).append("')");
 		sql.append(" union all ");
 		sql.append("(select ").append(AD_COLUMN).append(" from ad");
 		sql.append(" where position_id=").append(positionId);
 		sql.append(" and ").append(WHERE_NORMAL).append("  and expired_rent!='' and now()>gmt_plan_end ");
 		sql.append(" and search_exact='").append(sb.toString()).append("')");
 		
 		sql.append(" order by sequence, gmt_start limit ").append(maxAd);
 		
 		return jdbcAD(sql.toString());
 		
 	}
 	
 	public List<Ad> queryAd(Integer positionId, Integer maxAd){
 		if(positionId==null){
 			return new ArrayList<Ad>();
 		}
 		if(maxAd==null || maxAd.intValue()<1){
 			maxAd=1;
 		}
 		
 		StringBuffer sql=new StringBuffer();
 		sql.append("(select ").append(AD_COLUMN).append(" from ad");
 		sql.append(" where position_id=").append(positionId);
 		sql.append(" and ").append(WHERE_NORMAL).append(" and now()<gmt_plan_end )");
 		sql.append(" union all ");
 		sql.append("(select ").append(AD_COLUMN).append(" from ad");
 		sql.append(" where position_id=").append(positionId);
 		sql.append(" and ").append(WHERE_NORMAL).append("  and gmt_plan_end is null )");
 		sql.append(" union all ");
 		sql.append("(select ").append(AD_COLUMN).append(" from ad");
 		sql.append(" where position_id=").append(positionId);
 		sql.append(" and ").append(WHERE_NORMAL).append("  and expired_rent!='' and now()>gmt_plan_end )");
 		
 		sql.append(" order by sequence, gmt_start limit ").append(maxAd);
 		
 		return jdbcAD(sql.toString());
 	}
 	
 	
 	public String buildAd(List<Ad> ad, String style, Integer width, Integer height, String ip){
 		if(ad==null || ad.size()<=0){
 			return "";
 		}
 		String[] s=style.split("\\|");
 		if(style.contains("$$$")){
 			s=style.split("\\$\\$\\$");
 		}
 		if(style.length()<2){
 			return "";
 		}
 //		String[] s=style.split("\\|");
 		StringBuilder result=new StringBuilder();
 //		if((width!=null && width.intValue()>0)
 //				&& (height!=null && height.intValue()>0)){
 //			result.append("document.write('<div ");
 //			result.append("style=\"overflow:hidden;zoom:1;width:").append(width).append("px;height:").append(height).append("px\"");
 //			result.append(" >');");
 //		}
 		result.append(s[0]);
 //		long time=System.currentTimeMillis();
 		for(Ad a:ad){
 			result.append("document.write('");
 			result.append(parse(a, width, height, s[1]));
 			result.append("');");
 			
 //			AdLog log=new AdLog();
 //			log.setAdId(a.getId());
 //			log.setPositionId(a.getPositionId());
 //			log.setHitType(0);
 //			log.setIp(ip);
 //			log.setTime(time);
 //			log.setClickType(0);
 //			ControlThread.excute(new AdLogThread(log));
 		}
 		if(s.length>2){
 			result.append(s[2]);
 		}
 //		if((width!=null && width.intValue()>0)
 //				&& (height!=null && height.intValue()>0)){
 //			result.append("document.write('</div>');");
 //		}
 		return result.toString();
 	}
 	
 	public String parse(Ad ad, Integer width, Integer height, String template){
 		MessageFormat format=new MessageFormat(template);
 //		if(ad.getAdContent()!=null 
 //				&& ad.getGmtContentModified()!=null 
 //				&& (new Date().getTime()-ad.getGmtContentModified().getTime())<86400000){
 //			ad.setAdContent(ad.getAdContent().replace("img1.zz91.com", "img180.zz91.com"));
 //		}
 		String w="";
 		if(width!=null && width.intValue()>0){
 			w="width=\""+width+"\"";
 		}
 		String h="";
 		if(height!=null && height.intValue()>0){
 			h="height=\""+height+"\"";
 		}
 		if(ad.getAdDescription()==null){
 			ad.setAdDescription("");
 		}
 		String description=ad.getAdDescription().replace("\n", "").replace("\t", "").trim();
 		return format.format(new String[]{
 				ad.getAdTitle(), 
 				ad.getAdContent(), 
 				description, 
 				HIT_URL+"?a="+ad.getId(), 
 				w, 
 				h, 
 				String.valueOf(ad.getId())});
 	}
 	
 //	public String parseAdAfter(Ad ad, String template){
 //		MessageFormat format=new MessageFormat(template);
 //		return format.format(new String[]{String.valueOf(ad.getId())});
 //	}
 	
 	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
 //		MessageFormat format=new MessageFormat("here {0}is{1} a{2}b test ");
 //		System.out.println(format.format(new String[]{"中国", "浙江", "", "test over"}));
 //		System.out.println("abcdefg***sdfds".contains("***"));
 //		System.out.println("$$$$$$1".split("\\$\\$\\$").length);
 		
 		System.out.println(MD5.encode("keywords=杭州", 16));
 		System.out.println(MD5.encode("keywords=杭州", 16));
 		System.out.println(MD5.encode("keywords=杭州", 16));
 		System.out.println(MD5.encode("keywords=杭州", 16));
 		System.out.println(MD5.encode("keywords=北京上海", 16));
 		System.out.println(MD5.encode("keywords=北京上海", 16));
 		System.out.println(MD5.encode("keywords=北京上海keywords=杭州", 16));
 		System.out.println(MD5.encode("keywords=北京上海keywords=杭州", 16));
 		System.out.println("-----------");
 		System.out.println(MD5.encode("keywords=北京上海", 32));
 		System.out.println(MD5.encode("keywords=北京上海", 32));
 		System.out.println(MD5.encode("keywords=杭州", 32));
 		System.out.println(MD5.encode("keywords=杭州", 32));
 		System.out.println(MD5.encode("keywords=杭州", 32));
 	}
 	
 	public Ad queryTargetUrl(Integer id){
 		StringBuffer sql= new StringBuffer();
 		sql.append("select a.ad_target_url,a.position_id from ad a where id="+id);
 		final List<Ad> list=new ArrayList<Ad>();
 		DBUtils.select("zzads", sql.toString(), new IReadDataHandler() {
 			
 			@Override
 			public void handleRead(ResultSet rs) throws SQLException {
 				while(rs.next()){
 					Ad ad=new Ad();
 					ad.setAdTargetUrl(rs.getString(1));
 					ad.setPositionId(rs.getInt(2));
 					list.add(ad);
 				}
 			}
 			
 		});
 		if(list.size()>0){
 			return list.get(0);
 		}
 		return null;
 	}
 	
 	/**
 	 * 新缓存规则：pid.cache_version
 	 * 旧缓存规则：pid.version
 	 * @param pid
 	 * @param ip
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public String showAd(Integer pid, String ip, Map<String, String> parameterMap){
 		MemcachedClient client=MemcachedUtils.getInstance().getClient();
 		List<String> cacheList=(List<String>) client.get(getCacheKey(pid, parameterMap));
 		if(cacheList==null){
 			cacheList = buildAdCache(pid, ip, parameterMap);
 		}
 		
 		//提交日志请求
 		long time=System.currentTimeMillis();
 		int cachesize=cacheList.size();
 		if(cachesize>1){
 			for(int i=1;i<cachesize;i++){
 				if(StringUtils.isNumber(cacheList.get(i))){
 					AdLog log=new AdLog();
 					log.setAdId(Integer.valueOf(cacheList.get(i)));
 					log.setPositionId(pid);
 					log.setHitType(0);
 					log.setIp(ip);
 					log.setTime(time);
 					log.setClickType(0);
 					ControlThread.excute(new AdLogThread(log));
 				}
 			}
 		}
 		
 		//返回广告信息
 		if(cachesize>0){
 			return cacheList.get(0);
 		}
 		return "";
 	}
 	
 	public String getCacheKey(Integer pid, Map<String, String> parameterMap){
 		
 		return pid+getKeywordsKey(parameterMap)+"."+CACHE_VERSION;
 	}
 	
 	public String getKeywordsKey(Map<String, String> parameterMap){
 		String keywordsKey="";
 		if(parameterMap.size()>0){
 			StringBuffer sb=new StringBuffer();
 			for(String k:parameterMap.keySet()){
 				sb.append(k).append("=").append(parameterMap.get(k));
 			}
 			try {
 				keywordsKey=MD5.encode(sb.toString(), MD5.LENGTH_16);
 			} catch (NoSuchAlgorithmException e) {
 			} catch (UnsupportedEncodingException e) {
 			}
 		}
 		return keywordsKey;
 	}
 	
 	//必需存0,1必需有值
 	public List<String> buildAdCache(Integer pid, String ip, Map<String, String> parameterMap){
 		List<String> adCache=new ArrayList<String>();
 		
 		AdPositionDto position = AdsService.getInstance().queryPosition(pid);
 		do {
 			if (position.getPosition() == null) {
 				break;
 			}
 			
 			List<Ad> adlist = null;
 			if (position.getPosition().getHasExactAd() != null
 					&& position.getPosition().getHasExactAd() == 1) {
 				// 精确广告
 				Map<Integer, String> map = AdsService.getInstance()
 						.queryExactByPosition(position.getPosition().getId());
 				Map<Integer, String> anchorMap = new HashMap<Integer, String>();
 				for (Integer p : map.keySet()) {
 //					anchorMap.put(p, StringUtils.decryptUrlParameter(request.getParameter(map
 //							.get(p))));
 //					anchorMap.put(p, URLDecoder.decode(request.getParameter(map
 //							.get(p)), "utf-8"));
 					anchorMap.put(p, parameterMap.get(map.get(p)));
 				}
 				adlist = queryExactAd(
 						position.getPosition().getId(),
 						position.getPosition().getMaxAd(), anchorMap);
 			} else {
 				adlist = queryAd(
 						position.getPosition().getId(),
 						position.getPosition().getMaxAd());
 			}
 			
 			adCache.add(0, buildAd(adlist,
 					position.getStyle(), position.getPosition().getWidth(),
 					position.getPosition().getHeight(),
 					ip));
 			
 			for(Ad ad:adlist){
 				adCache.add(String.valueOf(ad.getId()));
 			}
 		} while (false);
 		
 		//将adCache存入缓存
 		MemcachedUtils.getInstance().getClient().set(getCacheKey(pid, parameterMap), CACHE_EXPIRE, adCache);
 //		MemcachedUtils.getInstance().getClient().set(getKey(pid, parameterMap), CACHE_EXPIRE, adCache);
 		
 		return adCache;
 	}
 	
 	private String saveEncode(String keywords){
 		if(StringUtils.isEmpty(keywords)){
 			return "";
 		}
 		keywords=keywords.replaceAll("astoxg", "/");
 		keywords=keywords.replaceAll("asto5c", "\\\\");
 		keywords=keywords.replaceAll("astohg", "-");
 		keywords=keywords.replaceAll("astokhl", "(");
 		keywords=keywords.replaceAll("astokhr", ")");
 		return keywords;
 	}
 	
 	private List<Ad> jdbcAD(String sql){
 		final List<Ad> list = new ArrayList<Ad>(); 
 		
 		DBUtils.select("zzads", sql.toString(), new IReadDataHandler() {
 			
 			@Override
 			public void handleRead(ResultSet rs) throws SQLException {
				long now=System.currentTimeMillis() - 86400*1000l;
 				while (rs.next()) {
 					Ad ad=new Ad();
 					ad.setId(rs.getInt(1));
 					ad.setAdTitle(rs.getString(2));
 					ad.setAdDescription(rs.getString(3));
 					
 					Date planEndDate=rs.getDate(10);
					if(planEndDate!=null && planEndDate.getTime()<now && StringUtils.isNotEmpty(rs.getString(7)) ){
 						ad.setAdContent(rs.getString(7));
 					}else{
 						ad.setAdContent(rs.getString(4));
 					}
 					
 					ad.setPositionId(rs.getInt(5));
 					ad.setGmtContentModified(rs.getDate(6));
 					list.add(ad);
 				}
 			}
 		});
 		
 		return list;
 	}
 
 }
