 package com.taobao.zeus.web;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.taobao.zeus.schedule.mvc.DebugInfoLog;
 import com.taobao.zeus.schedule.mvc.ScheduleInfoLog;
 import com.taobao.zeus.store.mysql.persistence.ZeusUser;
 
 public class UcSSOLogin implements LoginFilter.SSOLogin {
 	
 	static private List<ZeusUser> zuList = new ArrayList<ZeusUser>();
 	//QJW 模拟一堆用户
 	static {
 		String[][] temp = new String[][]{
 				{"qiujw@ucweb.com","仇家伟","13612345670","qiujw"},
 				{"qiujw1@ucweb.com","仇家伟1","13612345671","qiujw1"},
 				{"qiujw2@ucweb.com","仇家伟2","13612345672","qiujw2"},
 		};
 		for(int i=0;i<temp.length;i++){
 			ZeusUser zu = new ZeusUser();
 			zu.setEmail(temp[i][0]);
 			zu.setName(temp[i][1]);
 			zu.setPhone(temp[i][2]);
 			zu.setUid(temp[i][3]);
 			zuList.add(zu);
 		}
 	}
 	private ZeusUser getByUid(String uid){
 		for(ZeusUser zu : zuList){
 			if( zu.getUid().equalsIgnoreCase(uid) ) return zu;
 		}
 		return null;
 	}
 	@Override
 	public String getUid(HttpServletRequest req) {
 		String uid = req.getParameter("uid");
 		DebugInfoLog.info(uid);
 		if( uid!=null ){
 			ZeusUser zu = getByUid(uid);
 			if( zu==null ) return null;
 			else return zu.getUid();
 		}
 		return null;
 	}
 
 	@Override
 	public String getEmail(HttpServletRequest req) {
 		String uid = req.getParameter("uid");
 		if( uid!=null ){
 			ZeusUser zu = getByUid(uid);
 			if( zu==null ) return null;
 			else return zu.getEmail();
 		}
 		return null;
 	}
 
 	@Override
 	public String getName(HttpServletRequest req) {
 		String uid = req.getParameter("uid");
 		if( uid!=null ){
 			ZeusUser zu = getByUid(uid);
 			if( zu==null ) return null;
 			else return zu.getName();
 		}
 		return null;
 	}
 
 	@Override
 	public String getPhone(HttpServletRequest req) {
 		String uid = req.getParameter("uid");
 		if( uid!=null ){
 			ZeusUser zu = getByUid(uid);
 			if( zu==null ) return null;
 			else return zu.getPhone();
 		}
 		return null;
 	}
 
 }
