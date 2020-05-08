 /**
  * 
  */
 package com.k99k.testcenter;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 
 import com.k99k.khunter.Action;
 import com.k99k.khunter.ActionManager;
 import com.k99k.khunter.ActionMsg;
 import com.k99k.khunter.DaoInterface;
 import com.k99k.khunter.DaoManager;
 import com.k99k.khunter.HttpActionMsg;
 import com.k99k.khunter.JOut;
 import com.k99k.khunter.KFilter;
 import com.k99k.khunter.KObjManager;
 import com.k99k.khunter.KObjSchema;
 import com.k99k.khunter.KObject;
 import com.k99k.khunter.dao.StaticDao;
 import com.k99k.tools.StringUtil;
 
 /**
  * Topic 相当于论坛主贴
  * @author keel
  *
  */
 public class Topic extends Action {
 
 	/**
 	 * @param name
 	 */
 	public Topic(String name) {
 		super(name);
 	}
 	
 	private int pageSize = 30;
 	static final Logger log = Logger.getLogger(Topic.class);
 	static DaoInterface dao;
 	static KObjSchema schema;
 	static Comm comm;
 	static final HashMap<String,Integer> cateMap = new HashMap<String,Integer>();
 	static{
 		cateMap.put("pub",0);
 		cateMap.put("company",1);
 		cateMap.put("doc",2);
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
 	 */
 	@Override
 	public ActionMsg act(ActionMsg msg) {
 		HttpActionMsg httpmsg = (HttpActionMsg)msg;
 		HttpServletRequest req = httpmsg.getHttpReq();
 		
 		String[] pathArr = (String[]) msg.getData("[pathArr]");
 		int r = KFilter.getRootNum()+2;
 		String subact = (pathArr.length <= r) ? "" : pathArr[r];
 		String subsub = (pathArr.length <= r+1) ? "" : pathArr[r+1];
 		KObject u = Auth.checkCookieLogin(httpmsg);
 		if (u == null) {
 			msg.addData("[redirect]", "/login");
 			return super.act(msg);
 		}
 		if (subact.equals("pub")) {
 			this.pub(subact,req, u, httpmsg);
 		}else if(subact.equals("company")){
 			this.myCompany(subact, req, u, httpmsg);
 		}else if(subact.equals("doc")){
 			this.doc(subact,subsub, req, u, httpmsg);
 		}else if (StringUtil.isDigits(subact)) {
 			this.one(subact, req, u, httpmsg);
 		}else if(subact.equals("add")){
 			String subsub2 = (pathArr.length <= r+2) ? "" : pathArr[r+2];
 			this.toAdd(subsub,subsub2, u, req, httpmsg);
 		}else if(subact.equals("a_a")){
 			this.add(req, u, httpmsg);
 		}else if(subact.equals("a_u")){
 			this.update(req, u, httpmsg);
 		}else if(subact.equals("a_d")){
 			this.del(req, u, httpmsg);
 		}else if(subact.equals("a_s")){
 			this.search(subact,req, u, httpmsg);
 		}else{
 			JOut.err(404, httpmsg);
 		}
 		return super.act(msg);
 	}
 	
 	private void search(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
 		if (StringUtil.isStringWithLen(req.getParameter("k"), 1)) {
 			String key = null;
 			try {
 				//TODO 针对tomcatURL编码转换
 				key = new String(req.getParameter("k").getBytes("ISO-8859-1"),"utf-8").trim();
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 			HashMap<String,Object> query = new HashMap<String, Object>(2);
 			Pattern p = Pattern.compile(key);
 			query.put("name", p);
			if (req.getParameter("sub") != null) {
 				query.put("cate", cateMap.get(req.getParameter("sub").trim()));
 			}
			if (req.getParameter("tag") != null) {
 				query.put("tags", req.getParameter("tag").trim());
 			}
 			query.putAll(StaticDao.prop_state_normal);
 			this.queryPage(query,subact, req, u, msg);
 			return;
 		}else{
 			JOut.err(401, msg);
 		}
 	}
 	
 	
 	/**
 	 * 删除话题
 	 * @param req
 	 * @param u
 	 * @param msg
 	 */
 	private void del(HttpServletRequest req,KObject u,HttpActionMsg msg){
 		String topic_id = req.getParameter("id");
 		if (!StringUtil.isDigits(topic_id) ) {
 			JOut.err(403,"E403"+Err.ERR_TOPIC_DEL, msg);
 			return;
 		}
 		long tid = Long.parseLong(topic_id);
 		KObject kobj = dao.findOne(tid);
 		if (kobj==null) {
 			JOut.err(404,"E404"+Err.ERR_TOPIC_DEL, msg);
 			return;
 		}
 		if (u.getType()<=10 && u.getId()!=kobj.getCreatorId()) {
 			JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
 			return;
 		}
 		if (dao.deleteOne(tid)!=null) {
 			msg.addData("[print]", "ok");
 			return;
 		}
 		JOut.err(500, "E500"+Err.ERR_TOPIC_DEL, msg);
 	}
 	
 	
 	/**
 	 * 查看单个话题
 	 * @param subact
 	 * @param req
 	 * @param u
 	 * @param msg HttpActionMsg
 	 */
 	@SuppressWarnings("unchecked")
 	private void one(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
 		long id = Long.parseLong(subact);
 		KObject one = dao.findOne(id);
 		if (one== null || one.getState() == -1) {
 			JOut.err(404, msg);
 			return;
 		}
 		//company验证权限 
 		ArrayList<Object> tags = (ArrayList<Object>)one.getProp("tags");
 		if (tags.contains("company")) {
 			if (!u.getProp("company").equals(one.getProp("company")) && u.getType()<4) {
 				JOut.err(401,"E401"+Err.ERR_AUTH_FAIL, msg);
 				return;
 			}
 		}
 		msg.addData("u", u);
 		msg.addData("one", one);
 		Object e = req.getParameter("edit");
 		if (e !=null && e.equals("true")) {
 			if (StringUtil.isDigits(one.getProp("PID"))) {
 				long pid = (Long)one.getProp("PID");
 				if (pid>0) {
 					KObject product = Product.dao.findOne(pid);
 					msg.addData("pName", product.getName());
 				}
 			}
 			msg.addData("[jsp]", "/WEB-INF/tc/topic_edit.jsp");
 			return;
 		}
 		// 评论列表
 		HashMap<String,Object> query = new HashMap<String, Object>();
 		query.put("TPID", id);
 		comm.queryPage(query, req,msg);
 		
 		msg.addData("[jsp]", "/WEB-INF/tc/topic_one.jsp");
 	}
 	
 	
 	/**
 	 * 编辑主贴
 	 * @param req
 	 * @param u
 	 * @param msg
 	 */
 	private void update(HttpServletRequest req,KObject u,HttpActionMsg msg){
 		String t_name = req.getParameter("t_name");
 		String t_text = req.getParameter("t_text");
 		String t_level = req.getParameter("t_level");
 		String t_lock = req.getParameter("t_lock");
 		String files = req.getParameter("news_files");
 		String topic_id = req.getParameter("topic_id");
 		if (!StringUtil.isDigits(topic_id) || 
 			!StringUtil.isStringWithLen(t_name, 1) || 
 			!StringUtil.isStringWithLen(t_text, 1) 
 			) {
 			JOut.err(403,"E403"+Err.ERR_TOPIC_UPDATE, msg);
 			return;
 		}
 		String name = t_name.trim();
 		long tid = Long.parseLong(topic_id);
 		int level = (StringUtil.isDigits(t_level) && u.getType()>10)?Integer.parseInt(t_level):0;
 		int lock = (StringUtil.isDigits(t_lock))? Integer.parseInt(t_lock):0;
 		String text = StringUtil.repstr1(t_text.trim());
 		if (u.getType()<10) {
 			level = 0;
 		}
 		KObject kobj = dao.findOne(tid);
 		if (kobj==null) {
 			JOut.err(404,"E404"+Err.ERR_TOPIC_UPDATE, msg);
 			return;
 		}
 		kobj.setLevel(level);
 		kobj.setName(name);
 		kobj.setProp("text", text);
 		kobj.setProp("lock", lock);
 		if (StringUtil.isStringWithLen(files, 1)) {
 			boolean enc = true;
 			try {
 				files = URLDecoder.decode(files, "utf-8");
 			} catch (UnsupportedEncodingException e) {
 				enc = false;
 			}
 			if (enc) {
 				kobj.setProp("files", files.trim().split(","));
 			}
 		}
 		if (dao.save(kobj)) {
 			String re = String.valueOf(kobj.getId());
 			msg.addData("[print]", re);
 //			ActionMsg task = new ActionMsg("topicTask");
 //			task.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
 //			task.addData("topicId", kobj.getId());
 //			TaskManager.makeNewTask("TCTopicTask:"+kobj.getId(), task);
 			return;
 		}
 		JOut.err(500, "E500"+Err.ERR_TOPIC_UPDATE, msg);
 	}
 	
 	/**
 	 * 添加主贴
 	 * @param req
 	 * @param u
 	 * @param msg
 	 */
 	private void add(HttpServletRequest req,KObject u,HttpActionMsg msg){
 		String t_cate = req.getParameter("t_cate");
 		String t_tags = req.getParameter("t_tags");
 		String t_name = req.getParameter("t_name");
 		String t_text = req.getParameter("t_text");
 		String t_level = req.getParameter("t_level");
 		String t_pid = req.getParameter("t_pid");
 		String t_tid = req.getParameter("t_tid");
 		String t_lock = req.getParameter("t_lock");
 		String files = req.getParameter("news_files");
 		String company = u.getProp("company").toString();
 		
 		if (!StringUtil.isStringWithLen(t_name, 1) || 
 			!StringUtil.isStringWithLen(t_text, 1) 
 			) {
 			JOut.err(403,"E403"+Err.ERR_AUTH_FAIL, msg);
 			return;
 		}
 		String name = t_name.trim();
 		int cate = (StringUtil.isDigits(t_cate))? Integer.parseInt(t_cate):0;
 		int level = (StringUtil.isDigits(t_level) && u.getType()>10)?Integer.parseInt(t_level):0;
 		long pid = (StringUtil.isDigits(t_pid))?Long.parseLong(t_pid):0;
 		long tid = (StringUtil.isDigits(t_tid))?Long.parseLong(t_tid):0;
 		int lock = (StringUtil.isDigits(t_lock))? Integer.parseInt(t_lock):0;
 		String text = StringUtil.repstr1(t_text.trim());
 		if (cate > 1 && u.getType()<10) {
 			JOut.err(403,"E403"+Err.ERR_AUTH_FAIL, msg);
 			return;
 		}
 		if (u.getType()<10) {
 			level = 0;
 		}
 		KObject kobj = schema.createEmptyKObj(dao);
 		kobj.setLevel(level);
 		kobj.setName(name);
 		kobj.setProp("text", text);
 		kobj.setProp("PID", pid);
 		kobj.setProp("TID", tid);
 		kobj.setProp("cate", cate);
 		kobj.setProp("company", company);
 		kobj.setProp("lock", lock);
 		kobj.setCreatorName(u.getName());
 		kobj.setCreatorId(u.getId());
 		if (StringUtil.isStringWithLen(t_tags, 1)) {
 			boolean enc = true;
 			try {
 				t_tags = URLDecoder.decode(t_tags, "utf-8");
 			} catch (UnsupportedEncodingException e) {
 				enc = false;
 			}
 			if (enc) {
 				kobj.setProp("tags", t_tags.trim().split(","));
 			}
 		}
 		if (StringUtil.isStringWithLen(files, 1)) {
 			boolean enc = true;
 			try {
 				files = URLDecoder.decode(files, "utf-8");
 			} catch (UnsupportedEncodingException e) {
 				enc = false;
 			}
 			if (enc) {
 				kobj.setProp("files", files.trim().split(","));
 			}
 		}
 		if (dao.save(kobj)) {
 			String re = String.valueOf(kobj.getId());
 			msg.addData("[print]", re);
 //			ActionMsg task = new ActionMsg("topicTask");
 //			task.addData(TaskManager.TASK_TYPE, TaskManager.TASK_TYPE_EXE_POOL);
 //			task.addData("topicId", kobj.getId());
 //			TaskManager.makeNewTask("TCTopicTask:"+kobj.getId(), task);
 			return;
 		}
 		JOut.err(500, "E500"+Err.ERR_TOPIC_ADD, msg);
 	}
 	
 	/**
 	 * 转到增加页
 	 * @param subsub
 	 * @param u
 	 * @param req
 	 * @param msg
 	 */
 	private void toAdd(String sub,String tag,KObject u,HttpServletRequest req,HttpActionMsg msg){
 		msg.addData("tag", tag);
 		msg.addData("sub", sub);
 		String t_pid = req.getParameter("pid");
 		if (StringUtil.isDigits(t_pid)) {
 			KObject product = Product.dao.findOne(Long.parseLong(t_pid));
 			msg.addData("pName", product.getName());
 		}
 		msg.addData("u", u);
 		msg.addData("[jsp]", "/WEB-INF/tc/topic_add.jsp");
 	}
 	
 	/**
 	 * 文档Topic列表,可显示到某一个tag
 	 * @param subact
 	 * @param tag
 	 * @param req
 	 * @param u
 	 * @param msg
 	 */
 	private void doc(String subact,String tag,HttpServletRequest req,KObject u,HttpActionMsg msg){
 		HashMap<String,Object> q = StaticDao.prop_topic_doc;
 		if (!tag.equals("")) {
 			q.put("tags", tag);
 		}
 		this.queryPage(q,subact, req, u, msg);
 		msg.addData("tag", tag);
 		msg.addData("title", "文档-"+tag);
 	}
 	
 	/**
 	 * Topic公共列表
 	 * @param subact
 	 * @param req
 	 * @param u
 	 * @param msg
 	 */
 	private void pub(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
 		msg.addData("title", "公共讨论");
 		this.queryPage(StaticDao.prop_topic_pub,subact, req, u, msg);
 	}
 	
 	
 	/**
 	 * 用户所在公司的Topic列表
 	 * @param subact
 	 * @param req
 	 * @param u
 	 * @param msg
 	 */
 	private void myCompany(String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
 		msg.addData("title", "公司产品讨论");
 		HashMap<String,Object> q = StaticDao.prop_topic_company;
 		if (u.getType()<2) {
 			String company = u.getProp("company").toString();
 			q.put("company", company);
 		}
 		this.queryPage(q,subact, req, u, msg);
 	}
 	/**
 	 * 按条件查看某页列表
 	 * @param query
 	 * @param subact
 	 * @param req
 	 * @param u
 	 * @param msg
 	 */
 	private void queryPage(HashMap<String,Object> query,String subact,HttpServletRequest req,KObject u,HttpActionMsg msg){
 		String p_str = req.getParameter("p");
 		String pz_str = req.getParameter("pz");
 		int page = StringUtil.isDigits(p_str)?Integer.parseInt(p_str):1;
 		int pz = StringUtil.isDigits(pz_str)?Integer.parseInt(pz_str):this.pageSize;
 		ArrayList<KObject> list = dao.queryByPage(page,pageSize,query, null, StaticDao.prop_level_id_desc, null);
 		msg.addData("u", u);
 		msg.addData("list", list);
 		msg.addData("pz", pz);
 		msg.addData("p", page);
 		msg.addData("sub", subact);
 		msg.addData("[jsp]", "/WEB-INF/tc/topics.jsp");
 	}
 
 	/* (non-Javadoc)
 	 * @see com.k99k.khunter.Action#init()
 	 */
 	@Override
 	public void init() {
 		dao = DaoManager.findDao("TCTopicDao");
 		schema = KObjManager.findSchema("TCTopic");
 		comm = (Comm)ActionManager.findAction("comm");
 		super.init();
 	}
 
 
 
 	/**
 	 * @return the pageSize
 	 */
 	public final int getPageSize() {
 		return pageSize;
 	}
 
 	/**
 	 * @param pageSize the pageSize to set
 	 */
 	public final void setPageSize(int pageSize) {
 		this.pageSize = pageSize;
 	}
 	
 }
