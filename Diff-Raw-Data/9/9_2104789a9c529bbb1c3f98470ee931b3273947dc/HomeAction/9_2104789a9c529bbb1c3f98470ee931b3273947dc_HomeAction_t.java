 package com.tp.action;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.DecimalFormat;
 import java.util.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.tp.dao.log.LogJdbcDao;
 import com.tp.entity.*;
 import com.tp.orm.PageRequest;
 import com.tp.orm.PropertyFilter;
 import com.tp.service.*;
import com.tp.utils.FileUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.struts2.convention.annotation.Result;
 import org.apache.struts2.convention.annotation.Results;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.tp.dao.log.LogCountContentDao;
 import com.tp.orm.Page;
 import com.tp.utils.Constants;
 import com.tp.utils.ServletUtils;
 import com.tp.utils.Struts2Utils;
 
 @Results({ @Result(name = "reload", location = "home.action", type = "redirect") })
 public class HomeAction extends ActionSupport {
 
 	private static final long serialVersionUID = 1L;
 	private Logger logger = LoggerFactory.getLogger(getClass());
 
 	private CategoryManager categoryManager;
 //	private CategoryInfoManager categoryInfoManager;
 	private FileManager fileManager;
 	private MarketManager marketManager;
     private AdvertisementService advertisementService;
     private TopicService topicService;
 
 	private LogCountContentDao countContentDao;
     private LogJdbcDao logJdbcDao;
 
 	private Page<FileStoreInfo> hottestPage = new Page<FileStoreInfo>(10);
     private Page<Advertisement> advertisementPage=new Page<Advertisement>();
 
 	private Page<FileStoreInfo> newestPage = new Page<FileStoreInfo>(10);
 	private Page<FileStoreInfo> catePage = new Page<FileStoreInfo>(10);
 
 	private Long id;
     private Long pageNo=1L;
 	private String totalDown;
 	private FileStoreInfo info;
     private List<FileStoreInfo> gameInfo;
     private List<FileStoreInfo> appInfo;
 
 //	private List<CategoryInfo> cateInfos;
 	private Long categoryId;
 
 	private String categoryName;
 	private String language;
 	private List<Category> categories;
 
     private String bars;
 
     private List<Map<String,Object>> sorts;
     private List<Topic> topics;
 
     private String title;
     private String topicDescription;
     private Long offset;
 
     @Override
 	public String execute() throws Exception {
 
 		return list();
 	}
 
 	/**
 	 * 商店首页显示列表
 	 * @return
 	 * @throws Exception
 	 */
 	public String list() throws Exception {
 
 		HttpSession session = Struts2Utils.getSession();
 
 		language = (String) session.getAttribute(Constants.PARA_LANGUAGE);
 		Long storeId = chooseStoreId(session);
 
         String type=Struts2Utils.getParameter("g");
         if(StringUtils.isBlank(type)){
             type="female";
         }
 
 		newestPage = fileManager.searchStoreInfoInShelf(newestPage, type, storeId, language);
 
         setEachDownloadURl(newestPage,session);
         bars=json(getADs("store"));
 		return SUCCESS;
 	}
 
     private void setEachDownloadURl(Page<FileStoreInfo> page,HttpSession session)throws Exception{
         Iterator<FileStoreInfo> it=page.getResult().iterator();
         while (it.hasNext()){
             FileStoreInfo info=it.next();
             if(info.getTheme().getTagNames().contains("隐藏")){
                 it.remove();
             }else{
                 String category=info.getTheme().getCategories().get(0).getDescription();
                 setDownloadType(session,category,info);
             }
         }
     }
 
     public String topicList() throws Exception{
         HttpSession session = Struts2Utils.getSession();
 
         language = (String) session.getAttribute(Constants.PARA_LANGUAGE);
         String topicId=Struts2Utils.getParameter("topicId");
         if(StringUtils.isBlank(topicId)){
             topicId="";
         }
        topicId= FileUtils.getIconLevel(topicId);//兼容莫名的参数格式:1#
         Long id=Long.valueOf(topicId);
         Topic topic=topicService.getTopic(id);
         title=topic.getName();
         topicDescription=topic.getDescription();
         newestPage=fileManager.searchTopicFile(newestPage,id,language);
         setEachDownloadURl(newestPage,session);
         return "topic-list";
     }
 
     public String topicListJson() throws Exception{
         HttpSession session = Struts2Utils.getSession();
         language = (String) session.getAttribute(Constants.PARA_LANGUAGE);
         String topicId=Struts2Utils.getParameter("topicId");
         String pageNo=Struts2Utils.getParameter("pageNo");
         if(StringUtils.isBlank(topicId)){
             topicId="";
         }
         Long id=Long.valueOf(topicId);
         newestPage.setPageNo(Integer.valueOf(pageNo));
         newestPage=fileManager.searchTopicFile(newestPage,id,language);
         String json=category2Json(getNewestPage().getResult());
         Struts2Utils.renderJson(json);
         return null;
     }
     public String cate() throws Exception{
         HttpSession session = Struts2Utils.getSession();
 
         language = (String) session.getAttribute(Constants.PARA_LANGUAGE);
         Long storeId = chooseStoreId(session);
 
         String type=Struts2Utils.getParameter("g");
 
 
         if(StringUtils.isBlank(type)){
             type="female";
             title="女生专区";
         }else{
             if(type.equals("male")){
                 title="男生专区";
             }else if(type.equals("female")){
                 title="女生专区";
             }
         }
 
         newestPage = fileManager.searchStoreInfoInShelf(newestPage, type, storeId, language);
         setEachDownloadURl(newestPage,session);
         return "cate";
     }
     public String shelfJson() throws Exception{
         HttpSession session = Struts2Utils.getSession();
 
         language = (String) session.getAttribute(Constants.PARA_LANGUAGE);
         Long storeId = chooseStoreId(session);
 
         String type=Struts2Utils.getParameter("g");
         if(StringUtils.isBlank(type)){
             type="female";
         }
         newestPage.setPageNo(pageNo.intValue());
         newestPage = fileManager.searchStoreInfoInShelf(newestPage, type, storeId, language);
         String json= category2Json(newestPage.getResult());
         Struts2Utils.renderJson(json);
         return null;
     }
 
     private List<Advertisement> getADs(String type){
         List<PropertyFilter> filters = PropertyFilter.buildFromHttpRequest(Struts2Utils.getRequest());
         filters.add(new PropertyFilter("EQS_dtype", type));
         filters.add(new PropertyFilter("EQS_store", Constants.ST_LOCK));
         filters.add(new PropertyFilter("EQL_status","1"));
         if(!advertisementPage.isOrderBySetted()){
             advertisementPage.setOrderBy("sort");
             advertisementPage.setOrderDir(PageRequest.Sort.ASC);
         }
         advertisementPage=advertisementService.searchAdvertisement(advertisementPage,filters);
         return advertisementPage.getResult();
     }
 
     private String json(List<Advertisement> ads){
         StringBuilder buffer=new StringBuilder();
         buffer.append("[");
         for(Advertisement ad:ads){
             buffer.append("{");
             buffer.append("\"pic\":\"/UMS/image.action?path="+ad.getImgLink()+"\"");
             buffer.append(",");
             buffer.append("\"href\":\""+ad.getLink()+"\"");
             buffer.append(",");
             buffer.append("\"ext\":"+true);
             buffer.append("}");
             buffer.append(",");
         }
         String json=StringUtils.substringBeforeLast(buffer.toString(),",");
         json+="]";
         return json;
     }
 
     @Deprecated
 	public String game() throws Exception {
 
         return dispatcher("game");
 	}
 
     public String shelf() throws Exception{
         HttpSession session = Struts2Utils.getSession();
 
         language = (String) session.getAttribute(Constants.PARA_LANGUAGE);
         Long storeId = chooseStoreId(session);
         String sf=Struts2Utils.getParameter("sf");
 
        Store store= categoryManager.getStore(storeId);
         for(Shelf s:store.getShelfs()){
             if(s.getValue().equals(sf)){
                 categoryName=s.getName();
                 title=s.getName();
                 break;
             }
         }
 
         newestPage = fileManager.searchStoreInfoInShelf(newestPage, sf, storeId, language);
         setEachDownloadURl(newestPage,session);
         return "shelf";
     }
 
     public String diy() throws Exception{
         HttpSession session = Struts2Utils.getSession();
 
         language = (String) session.getAttribute(Constants.PARA_LANGUAGE);
         Long storeId = chooseStoreId(session);
         newestPage = fileManager.searchStoreInfoInShelf(newestPage, "diy", storeId, language);
         bars=json(getADs("diy"));
         setEachDownloadURl(newestPage,session);
         return "diy";
     }
 
     @Deprecated
     public String star() throws Exception{
 
         HttpSession session = Struts2Utils.getSession();
 
         language = (String) session.getAttribute(Constants.PARA_LANGUAGE);
         Long storeId = chooseStoreId(session);
         newestPage = fileManager.searchStoreInfoInShelf(newestPage, Shelf.Type.STAR.getValue(), storeId, language);
         categoryName=Shelf.Type.STAR.getDisplayName();
         return "star";
     }
 
     private String dispatcher(String type) throws Exception{
         HttpServletRequest request=Struts2Utils.getRequest();
         HttpServletResponse response=Struts2Utils.getResponse();
         request.getRequestDispatcher("/home!shelf.action?sf="+type).forward(request, response);
         return null;
     }
 
     public String gostar() throws Exception{
         return "gostar";
     }
 
 	public String love() throws Exception {
 
 		return "love";
 	}
 
     public String man() throws Exception{
         return "man";
     }
 
     public String hottest() throws Exception{
         HttpSession session = Struts2Utils.getSession();
 
         language = (String) session.getAttribute(Constants.PARA_LANGUAGE);
         Long storeId = chooseStoreId(session);
         sorts = logJdbcDao.countThemeFileDownload(language,storeId,pageNo);
 
         return "hottest";
     }
 
     public String jsonMore() throws Exception{
         HttpSession session = Struts2Utils.getSession();
 
         language = (String) session.getAttribute(Constants.PARA_LANGUAGE);
         Long storeId = chooseStoreId(session);
         sorts = logJdbcDao.countThemeFileDownload(language,storeId,pageNo);
         String json=toJson(sorts);
         json=json.replaceAll("/","\\\\/");
 //        json=json.replaceAll("\"","\\\"");
 //        logger.info(json);
         Struts2Utils.renderJson(json);
         return null;
     }
 
     private String toJson(List<Map<String,Object>> contents){
         String queryString=(String)Struts2Utils.getSession().getAttribute("queryString");
         Locale locale= getLocale();
         ResourceBundle resourceBundle = ResourceBundle.getBundle("localStrings", locale) ;
         StringBuilder buffer=new StringBuilder();
         buffer.append("{");
         if(contents.size()<10){
             buffer.append("\"code\":900");
         }else{
             buffer.append("\"code\":200");
         }
         buffer.append(",\"data\":\"");
         for(Map<String,Object> theme:contents){
             SpanHtml html=new SpanHtml((Integer) theme.get("f_id"),(Integer)theme.get("isnew")
                     ,(Integer)theme.get("ishot"), (String) theme.get("iconPath"), (String) theme.get("title"),
                     (String) theme.get("shortDescription"), queryString, resourceBundle);
             html.setUrl("file-download.action?inputPath="+theme.get("apk_path")+"&title="+theme.get("title")+"|"+theme.get("title"));
             loopHtml(buffer,html);
         }
         buffer.append("\"}");
         return buffer.toString();
     }
 
     static  class  SpanHtml{
         private int id;
         private String iconPath;
         private String title;
         private String shortDescription;
         private ResourceBundle resourceBundle;
         private String queryString;
         private int isnew;
         private int ishot;
         private String url;
 
         SpanHtml(int id,int isnew,int ishot,String iconPath,String title,String shortDescription,String queryString,ResourceBundle resourceBundle){
             this.id=id;
             this.ishot=ishot;
             this.isnew=isnew;
             this.queryString=queryString;
             this.resourceBundle=resourceBundle;
             this.title=title;
             this.iconPath=iconPath;
             this.shortDescription=shortDescription;
         }
 
         String getUrl() {
             return url;
         }
 
         void setUrl(String url) {
             this.url = url;
         }
 
         int getIsnew() {
             return isnew;
         }
 
         void setIsnew(int isnew) {
             this.isnew = isnew;
         }
 
         int getIshot() {
             return ishot;
         }
 
         void setIshot(int ishot) {
             this.ishot = ishot;
         }
 
         int getId() {
             return id;
         }
 
         void setId(int id) {
             this.id = id;
         }
 
         String getIconPath() {
             return iconPath;
         }
 
         void setIconPath(String iconPath) {
             this.iconPath = iconPath;
         }
 
         String getTitle() {
             return title;
         }
 
         void setTitle(String title) {
             this.title = title;
         }
 
         String getShortDescription() {
             return shortDescription;
         }
 
         void setShortDescription(String shortDescription) {
             this.shortDescription = shortDescription;
         }
 
         ResourceBundle getResourceBundle() {
             return resourceBundle;
         }
 
         void setResourceBundle(ResourceBundle resourceBundle) {
             this.resourceBundle = resourceBundle;
         }
 
         String getQueryString() {
             return queryString;
         }
 
         void setQueryString(String queryString) {
             this.queryString = queryString;
         }
     }
 
     private void loopHtml(StringBuilder buffer,SpanHtml html){
 
         buffer.append("<li>");
         buffer.append("<div class=\\\"icon\\\"><img src=\\\"/UMS/image.action?path="+html.getIconPath()+"\\\"></div>");
         buffer.append(" <div class=\\\"y-split\\\"></div>");
         buffer.append(" <div class=\\\"info\\\">" +
                 "        <p class=\\\"title\\\">"+html.getTitle()+"</p>" +
                 "        <p class=\\\"txt\\\">"+html.getShortDescription()+"</p>" +
                 "        <div class=\\\"y-split right\\\"></div>" +
                 "        <div class=\\\"down-btn\\\">" +
                 "        <a href=\\\"#\\\" onclick=\\\"goDownload('"+html.getId()+"','"+html.getUrl()+"')\\\">"+
                 "            <img src=\\\"static/images/2.0/down.png\\\">" +
                 "            <span>"+html.getResourceBundle().getString("home.down")+"</span>" +
                 "        </a>"+
                 "        </div>" +
                 "    </div>");
         if(html.getIshot()==1){
             buffer.append("<span class=\\\"icon_n\\\"></span>");
         }else if(html.getIsnew()==1){
             buffer.append("<span class=\\\"icon_n icon_n_new\\\"></span>");
         }
 
         buffer.append("<a href=\\\"home!details.action?id="+html.getId()+"&"+html.getQueryString()+"\\\" class=\\\"down-area\\\"></a>");
         buffer.append("</li>");
     }
 
     public String category() throws Exception {
 		List<Category> lists = categoryManager.getCategories();
         categories= Lists.newArrayList();
         for(Category category:lists){
             if(StringUtils.contains(category.getDescription(),"hidden")){
                 continue;
             }
             categories.add(category);
         }
 		return "category";
 	}
 
 	private Long chooseStoreId(HttpSession session) throws Exception {
 		String storeType = (String) session.getAttribute(Constants.PARA_STORE_TYPE);
 		Long storeId = (Long) session.getAttribute(Constants.ID_LOCK);
 		if (StringUtils.isBlank(storeType)) {
 			storeType = Constants.ST_LOCK;
 		}
 		if (storeId != null) {
 			return storeId;
 		} else {
 			try {
 				storeId = categoryManager.getStoreByValue(storeType).getId();
 			} catch (Exception e) {
 				logger.warn("商店{}不存在，请求错误～", storeType);
 				Struts2Utils.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND, "parametter is incorrect.");
 			}
 			session.setAttribute(Constants.ID_LOCK, storeId);
 			return storeId;
 		}
 
 	}
 
 	/**
 	 * 输出5个广告xml
 	 * @return
 	 * @throws Exception
 	 */
 	public String adXml() throws Exception {
 
         HttpServletRequest request=Struts2Utils.getRequest();
         HttpServletResponse response=Struts2Utils.getResponse();
         request.getRequestDispatcher("/poll/advertisement!generateXml.action?st=lock").forward(request,response);
 		return null;
 	}
 
 	public String details() throws Exception {
 		try {
 
 			HttpSession session = Struts2Utils.getSession();
 
 			language = (String) session.getAttribute(Constants.PARA_LANGUAGE);
 			Long storeId = chooseStoreId(session);
 			info = fileManager.getStoreInfoBy(storeId, id, language);
 			if (info == null)
 				return "reload";
 
 			Category cate = info.getTheme().getCategories().get(0);
 			List<CategoryInfo> cateInfos = cate.getInfos();
 			for (CategoryInfo ci : cateInfos) {
 				if (ci.getDescription().equals(language)) {
 					categoryName = ci.getName();
 					break;
 				}
 			}
             catePage.setPageSize(100);
             if(cate.getDescription().contains("other")){
                 if(cate.getName().contains("游戏")){
                     catePage=fileManager.searchStoreInfoInShelf(catePage,Shelf.Type.GAME.getValue(),storeId,language);
                 }else if(cate.getName().contains("推荐")){
                     catePage=fileManager.searchStoreInfoInShelf(catePage,"app",storeId,language);
                 }
             }else {
                 catePage = fileManager.searchInfoByCategoryAndStore(catePage, cate.getId(), storeId, language);
             }
 
 			List<FileStoreInfo> fileinfos = catePage.getResult();
 
             if(offset!=null){
                 Map<Long,FileStoreInfo> offsetInfo=getOffsetInfo(fileinfos);
                 long size=offsetInfo.size();
                 if(offset<=0L){
                    offset=size;
                 }else if(offset>size){
                     offset=1L;
                 }
                 info=offsetInfo.get(offset);
                 offset=info.getOffset();
             }
             totalDown = countContentDao.queryTotalDownload(info.getTheme().getTitle(),language);
 			fileinfos.remove(info);
 			Collections.shuffle(fileinfos);
 			if (fileinfos.size() > 2) {
 				fileinfos = fileinfos.subList(0, 2);
 			}
 			catePage.setResult(fileinfos);
             shuffleGame(info,storeId,session);
             shuffleApp(info,storeId,session);
             setDownloadType(session, cate.getDescription(),info);
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			return "reload";
 		}
 		return "details";
 	}
 
     private Map<Long,FileStoreInfo> getOffsetInfo(List<FileStoreInfo> infos){
         long i=0;
         Map<Long,FileStoreInfo> offsetMap= Maps.newHashMap();
         for(FileStoreInfo info:infos){
             ++i;
             info.setOffset(i);
             offsetMap.put(i,info);
         }
         return offsetMap;
     }
 
     private void shuffleGame(FileStoreInfo current,Long storeId,HttpSession session) throws Exception{
         gameInfo=shuffle(current,"game",storeId,session);
     }
 
     private void shuffleApp(FileStoreInfo current,Long storeId,HttpSession session) throws Exception{
         appInfo=shuffle(current,"app",storeId,session);
     }
 
     private List<FileStoreInfo> shuffle(FileStoreInfo currentInfo,String type,Long storeId,HttpSession session) throws Exception{
         String g=Struts2Utils.getParameter("g");
         if(StringUtils.isNotBlank(g)){
             if(g.equals("female")){
                 g="女";
             }else{
                 g="男";
             }
         }
         newestPage.setPageSize(100);
         newestPage=fileManager.searchStoreInfoInShelf(newestPage, type, storeId, language);
         List<FileStoreInfo> fileInfos=newestPage.getResult();
         List<FileStoreInfo> subInfo=Lists.newArrayList();
         fileInfos.remove(currentInfo);
         for(FileStoreInfo info :fileInfos){
             String tags=info.getTheme().getTagNames();
 
             if(tags!=null&&g!=null&&tags.contains(g)){
                 subInfo.add(info);
             }
         }
 
         if(subInfo.size()==0){
             subInfo.addAll(fileInfos);
         }
 //        Collections.shuffle(subInfo);
         subInfo=fileManager.shuffInfos(subInfo);
         if(subInfo.size()>1){
 //            subInfo= subInfo.subList(0,2);
             for(FileStoreInfo info:subInfo){
                 setDownloadType(session,info.getTheme().getCategories().get(0).getDescription(),info);
             }
         }
         return subInfo;
     }
 
 	private void setDownloadType(HttpSession session, String category,FileStoreInfo fsi) throws UnsupportedEncodingException {
 
 		String fromMarket = (String) session.getAttribute(Constants.PARA_FROM_MARKET);
 		String downType = (String) session.getAttribute(Constants.PARA_DOWNLOAD_METHOD);
 		StringBuilder httpBuffer = new StringBuilder();
 		if (category.contains("other")) {
 			httpBuffer.append("browerhttp://" + StringUtils.remove(Constants.getDomain(), "http://") + "/");
 		}
 		httpBuffer.append("file-download.action?id=");
 		httpBuffer.append(fsi.getTheme().getId());
 		httpBuffer.append("&inputPath=");
 		if (fsi.getTheme().getDtype().equals("1")) {
 			httpBuffer.append(URLEncoder.encode(fsi.getTheme().getUxPath(), "utf-8"));
 		} else {
 			httpBuffer.append(URLEncoder.encode(fsi.getTheme().getApkPath(), "utf-8"));
 		}
 
 		httpBuffer.append("&title=" + URLEncoder.encode(fsi.getTitle(), "utf-8"));
 		httpBuffer.append(URLEncoder.encode("|", "utf-8")).append(
 				URLEncoder.encode(fsi.getTheme().getTitle(), "utf-8"));
 		httpBuffer.append("&");
 		if (downType.equals(DownloadType.MARKET.getValue())) {
 			marketDownload(fromMarket, httpBuffer.toString(),fsi);
 		} else {
             fsi.getTheme().setDownloadURL(httpBuffer.toString());
 		}
 	}
 
 	private void marketDownload(String fromMarket, String http,FileStoreInfo fsi) {
 		Market market = marketManager.findByPkName(fromMarket);
 		if (market == null || market.getMarketKey().isEmpty()) {
 			fsi.getTheme().setDownloadURL(http);
 		} else {
 			fileInMarket(market, http,fsi);
 		}
 	}
 
 	private void fileInMarket(Market market, String http,FileStoreInfo fsi) {
 		List<ThemeFile> files = market.getThemes();
 		if (files.contains(fsi.getTheme())) {
 			String uri = market.getMarketKey() + fsi.getTheme().getMarketURL();
 			if (market.getPkName().equals(Constants.LENVOL_STORE)) {
 				uri += ("&versioncode=" + fsi.getTheme().getVersion());
 			}
 			if (market.getPkName().equals(Constants.OPPO_NEARME)) {
 				List<FileMarketValue> fvs = fsi.getTheme().getMarketValues();
 				for (FileMarketValue fm : fvs) {
 					if (market.getId().equals(fm.getMarket().getId())) {
 						uri += "&" + (fm.getKeyName() + "=" + fm.getKeyValue());
 					}
 				}
 			}
             fsi.getTheme().setDownloadURL(uri);
 		} else {
             fsi.getTheme().setDownloadURL(http);
 		}
 	}
 
 	public String more() throws Exception {
 
 		HttpSession session = Struts2Utils.getSession();
 		HttpServletRequest request = Struts2Utils.getRequest();
 		language = (String) session.getAttribute(Constants.PARA_LANGUAGE);
 
 		Long storeId = chooseStoreId(session);
         try {
 		categoryId = Long.valueOf(Struts2Utils.getParameter("cid"));
 
 		//		cateInfos = categoryInfoManager.getInfosBylanguage(language);
 
 			categoryName = categoryManager.getCategory(categoryId).getName();
 		} catch (Exception e) {
 			String userAgent = request.getHeader("User-Agent");
 			String ip = ServletUtils.getIpAddr(request);
 			logger.warn(
 					"com.tp.entity.Category:#{}不存在,ip:" + ip + ",User-Agent:" + userAgent + " param:"
 							+ session.getAttribute(Constants.QUERY_STRING), categoryId);
 			Struts2Utils.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND, "parametter is incorrect.");
 			return null;
 		}
 
 		catePage = fileManager.searchInfoByCategoryAndStore(catePage, categoryId, storeId, language);
         setEachDownloadURl(catePage,session);
 		return "more";
 	}
 
     public String categoryMore() throws Exception{
 
         HttpSession session = Struts2Utils.getSession();
         HttpServletRequest request = Struts2Utils.getRequest();
         language = (String) session.getAttribute(Constants.PARA_LANGUAGE);
         categoryId = Long.valueOf(Struts2Utils.getParameter("cid"));
         Long storeId = chooseStoreId(session);
 
         catePage.setPageNo(pageNo.intValue());
         catePage = fileManager.searchInfoByCategoryAndStore(catePage, categoryId, storeId, language);
         Struts2Utils.renderJson(category2Json(catePage.getResult()));
         return null;
     }
     private String category2Json(List<FileStoreInfo> infos) throws Exception{
         HttpSession session=Struts2Utils.getSession();
         String queryString=(String)session.getAttribute("queryString");
         Locale locale= getLocale();
         ResourceBundle resourceBundle = ResourceBundle.getBundle("localStrings", locale) ;
         StringBuilder buffer=new StringBuilder();
         buffer.append("{");
         if(infos.size()<10){
             buffer.append("\"code\":900");
         }else{
             buffer.append("\"code\":200");
         }
         buffer.append(",\"data\":\"");
         for(FileStoreInfo info:infos){
             SpanHtml html=new SpanHtml(info.getTheme().getId().intValue(),info.getTheme().getIsnew().intValue(),info.getTheme().getIshot().intValue(),
                     info.getTheme().getIconPath(),info.getTitle(),
                   info.getShortDescription(),queryString,resourceBundle);
             setDownloadType(session,info.getTheme().getCategories().get(0).getDescription(),info);
             html.setUrl(info.getTheme().getDownloadURL());
             loopHtml(buffer,html);
         }
         buffer.append("\"}");
         return buffer.toString();
     }
 
     public String topic() throws Exception{
         topics=topicService.getAllTopics();
         return "topic";
     }
 
 	@Autowired
 	public void setFileManager(FileManager fileManager) {
 		this.fileManager = fileManager;
 	}
 
 	@Autowired
 	public void setCategoryManager(CategoryManager categoryManager) {
 		this.categoryManager = categoryManager;
 	}
 
 	@Autowired
 	public void setMarketManager(MarketManager marketManager) {
 		this.marketManager = marketManager;
 	}
 //
 //	@Autowired
 //	public void setCategoryInfoManager(CategoryInfoManager categoryInfoManager) {
 //		this.categoryInfoManager = categoryInfoManager;
 //	}
 
 	public Page<FileStoreInfo> getHottestPage() {
 		return hottestPage;
 	}
 
 	public Page<FileStoreInfo> getNewestPage() {
 		return newestPage;
 	}
 
     public List<FileStoreInfo> getGameInfo() {
         return gameInfo;
     }
 
     public List<FileStoreInfo> getAppInfo() {
         return appInfo;
     }
 
     public Page<FileStoreInfo> getCatePage() {
 		return catePage;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public FileStoreInfo getInfo() {
 		return info;
 	}
 
 	public void setInfo(FileStoreInfo info) {
 		this.info = info;
 	}
 
     public Page<Advertisement> getAdvertisementPage(){
         return  advertisementPage;
     }
 
     public String getTopicDescription() {
         return topicDescription;
     }
 
     //	public List<CategoryInfo> getCateInfos() {
 //		return cateInfos;
 //	}
 
     public String getTitle() {
         return title;
     }
 
     public Long getCategoryId() {
 		return categoryId;
 	}
 
     public String getCategoryName() {
 		return categoryName;
 	}
 
     public List<Topic> getTopics() {
         return topics;
     }
 
     public String getLanguage() {
 		return language;
 	}
 
 	public List<Category> getCategories() {
 		return categories;
 	}
 
     public String getTotalDown() {
 		return totalDown;
 	}
 
     public String getBars(){
         return bars;
     }
 
     public List<Map<String,Object>> getSorts(){
         return sorts;
     }
 
     public Long getPageNo(){
         return pageNo;
     }
 
     public void setPageNo(Long pageNo){
         this.pageNo=pageNo;
     }
 
     public Long getNextPage(){
         return pageNo+1;
     }
 
     public Long getOffset() {
         return offset;
     }
 
     public void setOffset(Long offset) {
         this.offset = offset;
     }
 
     @Autowired
 	public void setCountContentDao(LogCountContentDao countContentDao) {
 		this.countContentDao = countContentDao;
 	}
 
     @Autowired
     public void setAdvertisementService(AdvertisementService advertisementService){
         this.advertisementService=advertisementService;
     }
 
     @Autowired
     public void setLogJdbcDao(LogJdbcDao logJdbcDao){
         this.logJdbcDao=logJdbcDao;
     }
 
     @Autowired
     public void setTopicService(TopicService topicService) {
         this.topicService = topicService;
     }
 }
