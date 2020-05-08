 package com.lorent.service.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.lorent.common.dto.LCMConferenceDto;
 import com.lorent.common.dto.LCMRoleDto;
 import com.lorent.common.tree.BroadcastEvent;
 import com.lorent.dao.ConferenceNewDao;
 import com.lorent.dto.XmlrpcConf;
 import com.lorent.exception.ArgsException;
 import com.lorent.exception.ServerException;
 import com.lorent.model.AuthorityBean;
 import com.lorent.model.ConfRoleAuthorityBean;
 import com.lorent.model.ConfUserAuthorityBean;
 import com.lorent.model.ConfUserRoleBean;
 import com.lorent.model.ConferenceBean;
 import com.lorent.model.ConferenceNewBean;
 import com.lorent.model.ConferenceRoleBean;
 import com.lorent.model.ConferenceTypeBean;
 import com.lorent.model.ConferenceTypeRoleBean;
 import com.lorent.model.ConferenceUserBean;
 import com.lorent.model.CustomerBean;
 import com.lorent.model.SipConfBean;
 import com.lorent.model.UserBean;
 import com.lorent.service.ConferenceNewService;
 import com.lorent.util.Constant;
 import com.lorent.util.Counter;
 import com.lorent.util.McuUtil;
 import com.lorent.util.OpenfireUtil;
 import com.lorent.util.PropertiesUtil;
 import com.lorent.util.StringUtil;
 import com.lorent.util.ThreadLocaleUtil;
 import com.lorent.whiteboard.client.Client;
 import com.lorent.xmlrpc.McuXmlrpc;
 
 public class ConferenceNewServiceImpl extends GenericServiceImpl<ConferenceNewDao,ConferenceNewBean,Integer> implements
 ConferenceNewService{
 	private Logger log = Logger.getLogger(ConferenceNewServiceImpl.class);
 	
 	@Override
 	public void renewConferenceNew(UserBean user,
 			ConferenceNewBean conferenceNew, String[] urIds) throws Exception {
 		try{
 			validateConfData(urIds, conferenceNew.getConferenceTypeId());
 			daoFacade.getConferenceNewDao().update(conferenceNew);
 			ConferenceUserBean conferenceUser = new ConferenceUserBean();
 			conferenceUser.setConferenceId(conferenceNew.getId());
 //			List<ConferenceUserBean> list = daoFacade.getConferenceUserDao().getByExample(conferenceUser);
 //			Integer[] cuIds = new Integer[list.size()];
 //			List params = new ArrayList();
 //			params.add(conferenceNew.getId());
 			List<UserBean> oldUsers = daoFacade.getConferenceNewDao().getUsersByConferenceId(conferenceNew.getId());//getUserDao().queryByHql("select ub from UserBean ub where ub.id in (select cu.userId from ConferenceUserBean cu where cu.conferenceId="+conferenceNew.getId()+")" );
 			List<String> confMembers = new ArrayList<String>();
 			
 			
 			daoFacade.getConferenceUserDao().removeConferenceUserByConferenceId(conferenceNew.getId());//executeUpdate("delete from ConferenceUserBean cu where cu.conferenceId=?", params);
 //			if(cuIds.length>0){
 //				//params.clear(); 
 //				String delIds = StrUtil.changeIntegerArrayToString(cuIds);
 //				//删除用户权限
 //				daoFacade.getConfUserAuthorityDao().removeConfUserAuthoritysByConferenceId(conferenceNew.getId());//executeUpdate("delete from ConfUserAuthorityBean ua where ua.conferenceUserId in (select cu.userId from ConferenceUserBean cu where cu.conferenceId=?)", params);
 //				//删除用户角色
 //				daoFacade.getConfUserRoleDao().removeConfUserRolesByConferenceId(conferenceNew.getId());//executeUpdate("delete from ConfUserRoleBean ua where ua.conferenceUserId in (select cu.userId from ConferenceUserBean cu where cu.conferenceId=?)", params);
 //			}
 			//删除用户权限
 			daoFacade.getConfUserAuthorityDao().removeConfUserAuthoritysByConferenceId(conferenceNew.getId());//executeUpdate("delete from ConfUserAuthorityBean ua where ua.conferenceUserId in (select cu.userId from ConferenceUserBean cu where cu.conferenceId=?)", params);
 			//删除用户角色
 			daoFacade.getConfUserRoleDao().removeConfUserRolesByConferenceId(conferenceNew.getId());//executeUpdate("delete from ConfUserRoleBean ua where ua.conferenceUserId in (select cu.userId from ConferenceUserBean cu where cu.conferenceId=?)", params);
 			Map<String,Integer> confUserMap = new HashMap<String,Integer>();
 			for(int i=0;urIds!=null && i<urIds.length;i++){
 				String[] stra = urIds[i].split("_");
 				if(!confUserMap.containsKey(stra[1])){
 					//插入参加会议人员记录
 					ConferenceUserBean cubean = new ConferenceUserBean();
 					cubean.setConferenceId(conferenceNew.getId());
 					cubean.setUserId(Integer.parseInt(stra[1]));
 					daoFacade.getConferenceUserDao().save(cubean);
 					confUserMap.put(stra[1], cubean.getId());
 				}
 				
 				
 				//插入会议人员角色表
 				ConfUserRoleBean urbean = new ConfUserRoleBean();
 				urbean.setRoleId(Integer.parseInt(stra[0]));
 				urbean.setConferenceUserId(confUserMap.get(stra[1]));
 				daoFacade.getConfUserRoleDao().save(urbean);
 				
 				//插入会议人员权限表
 //				params.clear();
 //				params.add(Integer.parseInt(stra[0]));
 				daoFacade.getConfUserAuthorityDao().insertConfUserAuthoritysByRoleId(confUserMap.get(stra[1]), stra[0]);//.executeUpdate("insert into ConfUserAuthorityBean (conferenceUserId,authorityId) select " + cubean.getId() + ",ra.authorityId from ConfRoleAuthorityBean ra where ra.roleId=?", params);
 				confMembers.add(stra[2]);
 			}
 			
 			OpenfireUtil.getInstance().updateConferenceRoom(conferenceNew, confMembers, user, oldUsers);
 			
 		}catch(Exception ex){
 			ex.printStackTrace();
 			throw ex;
 		}
 	}
 
 	public void renewConferenceNew(ConferenceNewBean conferenceNew, String[] urIds) throws Exception{
 		UserBean user = ThreadLocaleUtil.getUser();
 		renewConferenceNew(user, conferenceNew, urIds);
 	}
 	
 	@Override
 	public void createConferenceNew(UserBean user, ConferenceNewBean conferenceNew,
 			String[] urIds, boolean valid)
 			throws Exception {
 		try{
 			if(valid){
 				validateConfData(urIds, conferenceNew.getConferenceTypeId());
 			}
 			String confNo = conferenceNew.getConfNo();
 //			String confNo = this.createConfNo(conferenceNew, customerCode, confType);
 //			conferenceNew.setConfNo(confNo);
 			daoFacade.getConferenceNewDao().save(conferenceNew);
 			Map<String,Integer> confUserMap = new HashMap<String,Integer>();
 			List<String> confMembers = new ArrayList<String>();
 			
 			for(int i=0;urIds!=null && i<urIds.length;i++){
 				String[] stra = urIds[i].split("_");
 				if(!confUserMap.containsKey(stra[1])){
 					//插入参加会议人员记录
 					ConferenceUserBean cubean = new ConferenceUserBean();
 					cubean.setConferenceId(conferenceNew.getId());
 					cubean.setUserId(Integer.parseInt(stra[1]));
 					daoFacade.getConferenceUserDao().save(cubean);
 					confUserMap.put(stra[1], cubean.getId());
 				}
 				
 				
 				//插入会议人员角色表
 				ConfUserRoleBean urbean = new ConfUserRoleBean();
 				urbean.setRoleId(Integer.parseInt(stra[0]));
 				urbean.setConferenceUserId(confUserMap.get(stra[1]));
 				daoFacade.getConfUserRoleDao().save(urbean);
 				
 				//插入会议人员权限表
 //				list.clear();
 //				list.add(Integer.parseInt(stra[0]));
 				daoFacade.getConfUserAuthorityDao().insertConfUserAuthoritysByRoleId(confUserMap.get(stra[1]), stra[0]);//.executeUpdate("insert into ConfUserAuthorityBean (conferenceUserId,authorityId) select " + cubean.getId() + ",ra.authorityId from ConfRoleAuthorityBean ra where ra.roleId=?", list);
 				confMembers.add(stra[2]);
 			}
 			
 			//MCU创建会议
 //			UserBean user = ThreadLocaleUtil.getUser();
 			try{
 				McuUtil.createForwardConference(confNo,user.getCustomer().getMcuServer().getServerUrl());
 			}catch(Exception e){
 				throw new ServerException("server.mcuCreateConfFail", e);
 			}
 			
 			//在OPENFIRE创建会议
 			//confMembers.add(user.getLccAccount());
 			try{
 				OpenfireUtil.getInstance().createConferenceRoom(conferenceNew, confMembers, false, user);
 			}catch(Exception e){
 				McuUtil.removeForwardConference(user.getCustomer().getMcuServer().getServerUrl(), confNo);
 				throw new ServerException("server.imsConnectFail", e);
 			}
 			
 			//在白板服务创建会议
 			try{
 				Client.create(PropertiesUtil.getConstant("lvmc.serverIP"), confNo);
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}catch(Exception ex){
 			ex.printStackTrace();
 			throw ex;
 		}
 	}
 
 	@Override
 	public void createConferenceNew(ConferenceNewBean conferenceNew, String[] urIds)
 			throws Exception {
 		UserBean user = ThreadLocaleUtil.getUser();
 		createConferenceNew(user, conferenceNew, urIds, true);
 	}
 	
 	public void removeSipConfByConfno(String confno) throws Exception {
 		if(confno == null){
 			return;
 		}
 		SipConfBean example = new SipConfBean();
 		example.setName(confno);
 		List<SipConfBean> sipConfs = daoFacade.getSipConfDao().getByExample(
 				example);
 		if(sipConfs!=null){
 			daoFacade.getSipConfDao().delete(sipConfs);
 		}
 	}
 	
 	public Map<String,String> getAllConferences() throws Exception {
 		ConferenceNewBean example = new ConferenceNewBean();
 		example.setDel(1);
 		List<ConferenceNewBean> confs = daoFacade.getConferenceNewDao().getByExample(example);
 		ConferenceNewBean conferenceNewBean=null;
 		Map<String,String> map=new HashMap<String,String>();
 		if(null!=confs)
 		for(int i=0;i<confs.size();i++){
 			conferenceNewBean=confs.get(i);
 			map.put(conferenceNewBean.getConfNo(), conferenceNewBean.getConferenceName());
 		}
 		return map;
 	}
 	
 	
 	
 	@Override
 	public Map<String, LCMConferenceDto> getAllConferencesDto()
 			throws Exception {
 		List<LCMConferenceDto> confs = daoFacade.getConferenceNewDao().getConferenceDto();
 		Map<String,LCMConferenceDto> map=new HashMap<String,LCMConferenceDto>();
 		if(null!=confs)
 		for(int i=0;i<confs.size();i++){
 			LCMConferenceDto lcmConferenceDto = confs.get(i);
 			map.put(lcmConferenceDto.getConfNo(), lcmConferenceDto);
 		}
 		return map;
 	}
 
 	public String createConfNo(ConferenceNewBean conf, String customerCode,
 			Integer conftype) throws Exception {
 		String no = "";
 		if (StringUtil.isEmpty(customerCode)) {
 			throw new ArgsException("args.customercodeisempty");
 		}
 		// 生成会议号码 会议号码=客户代码+会议类型+随机数，并判断是否重复
 		boolean isOK = true;
 		//String hql = "select c from ConferenceNewBean c where c.confNo = ";
 		while (isOK) {
 			no = customerCode
 					+ conftype
 					+ StringUtil.getRandom(Constant.getConferenceNoLength()
 							- Constant.getCustomerCodeLength() - 1);
 			List list = daoFacade.getConferenceNewDao().getConferenceNewsByConfNo(no);//.queryByHql(hql + "'" + no + "'");
 			if (list.size() == 0) {
 				isOK = false;
 			}
 		}
 		SipConfBean sipConf = new SipConfBean();
 		sipConf.setName(no);
 		sipConf.setNonExpired(true);
 		sipConf.setNonLocked(true);
 		sipConf.setCanreinvite("yes");
 		sipConf.setSecret("1qaz@WSX");
 		int sipId = daoFacade.getSipConfDao().save(sipConf);
 //		conf.setSipId(sipId);
 		conf.setConfNo(no);
 		return no;
 	}
 
 	@Override
 	public LCMRoleDto getMyRoleAndPermission(String confno, String lccno) throws Exception {
 		LCMRoleDto data = new LCMRoleDto();
 		//获取会议
 		List<ConferenceNewBean> confs = getDao().getConferenceNewsByConfNo(confno);
 		if(confs.size() == 0){
 			throw new ArgsException("args.confNoExist");
 		}
 		ConferenceNewBean conf = confs.get(0);
 		boolean isUserInConf = daoFacade.getConferenceUserDao().userHasConfRole(confno, lccno);
 		if(!isUserInConf){//创建会议时没有选择该用户角色
 			if(conf.getNeedApply()==1){//需要申请
 				return null;
 			}else{//
 				//获取默认角色
 				Integer defaultRoleId = conf.getDefaultRoleId();
 				
 				//添加相应数据
 				////插入ConferenceUserBean
 				ConferenceUserBean cubean = new ConferenceUserBean();
 				cubean.setConferenceId(conf.getId());
 				UserBean userBean = serviceFacade.getUserService().getByLccAccount(lccno);
 				if(userBean == null){
 					throw new ArgsException("sql.usernotexits");
 				}
 				cubean.setUserId(userBean.getId());
 				daoFacade.getConferenceUserDao().save(cubean);
 				
 				////插入ConfUserRoleBean
 				ConfUserRoleBean urbean = new ConfUserRoleBean();
 				urbean.setRoleId(defaultRoleId);
 				urbean.setConferenceUserId(cubean.getId());
 				daoFacade.getConfUserRoleDao().save(urbean);
 				
 				////插入ConfUserAuthorityBean
 				daoFacade.getConfUserAuthorityDao().insertConfUserAuthoritysByRoleId(cubean.getId(), defaultRoleId + "");
 				
 				//设置返回数据
 				ConferenceRoleBean role = daoFacade.getConferenceRoleDao().get(defaultRoleId);
 				data.addName(role.getRoleName());
 				data.setNickname(userBean.getRealName());
 				data.setConferenceTitle(conf.getConferenceName());
 				List<AuthorityBean> authoritys = daoFacade.getConferenceRoleDao().getAuthorityByRoleId(defaultRoleId);
 				for(AuthorityBean authority : authoritys){
 					data.addPermission(authority.getMark(), authority.getAuthorityName());
 				}
 				return data;
 			}
 		}else{
 			data = daoFacade.getConferenceNewDao().getMyRoleAndPermission(confno, lccno);
 			return data;
 		}
 	}
 
 	@Override
 	public void removeConferenceNew(Integer[] ids) throws Exception {
 		UserBean user = ThreadLocaleUtil.getUser();
 		removeConferenceNew(user, ids, true);
 	}
 
 	@Override
 	public void removeConferenceNew(UserBean user, Integer[] ids, boolean removeSipConf)
 			throws Exception {
 		if(ids.length>0){
 //			List list = null;
 //			String strIds = StrUtil.changeIntegerArrayToString(ids);
 			daoFacade.getConferenceNewDao().removeConferenceNewBeanByIds(ids);//executeUpdate("update ConferenceNewBean cn set cn.del=0 where cn.id in ("+strIds+")", list);
 			daoFacade.getConfUserAuthorityDao().removeConfUserAuthoritysByConferenceIds(ids);//executeUpdate("delete from ConfUserAuthorityBean ua where ua.conferenceUserId in (select id from ConferenceUserBean cu where cu.conferenceId in ("+strIds+"))", list);
 			daoFacade.getConfUserRoleDao().removeConfUserRolesByConferenceIds(ids);//executeUpdate("delete from ConfUserRoleBean ur where ur.conferenceUserId in (select id from ConferenceUserBean cu where cu.conferenceId in ("+strIds+"))", list);
 			daoFacade.getConferenceUserDao().removeConferenceUserByConferenceIds(ids);//executeUpdate("delete from ConferenceUserBean ur where ur.conferenceId in ("+strIds+")", list);
 			
 			List<String> confNos = daoFacade.getConferenceNewDao().getConferenceNoByConferenceIds(ids);//.queryByHql("select cn.confNo from ConferenceNewBean cn where cn.id in ("+ strIds +")");
 			
 //			UserBean user = ThreadLocaleUtil.getUser();
 			for(String confno:confNos){
 				try{
 					McuUtil.removeForwardConference(user.getCustomer().getMcuServer().getServerUrl(),confno);
 				}catch(Exception e){
 					log.error("removeConferenceNew", e);
 					throw new ServerException("server.mcuRemoveConfFail", e);
 				}
 				if(removeSipConf){
 					removeSipConfByConfno(confno);
 				}
 				try{
 					//在白板服务删除会议
 					Client.remove(PropertiesUtil.getConstant("lvmc.serverIP"), confno);
 				}catch(Exception e){
 					log.error("removeConferenceNew", e);
 					throw new ServerException("server.lvmcRemoveConfFail", e);
 				}
 			}
 			try{
 				OpenfireUtil.getInstance().removeConferenceRoom(confNos);
 			}catch(Exception e){
 				log.error("removeConferenceNew", e);
 				throw new ServerException("server.imsRemoveConfFail", e);
 			}
 		}
 	}
 
 	@Override
 	public Map<String, LCMRoleDto> getConfUserRole(String confno, String[] lccnos) throws Exception {
 		return daoFacade.getConferenceNewDao().getConfUserRole(confno, lccnos);
 	}
 
 	@Override
 	public void getSearchSubPageMapResult(Map<String, Object> map, Map<String,Object> params)
 			throws Exception {
 		daoFacade.getConferenceNewDao().getSearchSubPageMapResult(map,params);
 		
 	}
 
 	@Override
 	public void getInitSubPageMapResult(Map<String, Object> map,
 			Map<String, Object> params) throws Exception {
 		daoFacade.getConferenceNewDao().getInitSubPageMapResult(map,params);
 	}
 
 	@Override
 	public List<ConferenceNewBean> getMcuRestoreConfs() throws Exception {
 		ConferenceNewBean example = new ConferenceNewBean();
 		example.setDel(1);
 		List<ConferenceNewBean> confs = daoFacade.getConferenceNewDao().getByExample(example);
 		return confs;
 	}
 
 	@Override
 	public List getConferenceRoles() throws Exception {
 		List<ConferenceRoleBean> all = daoFacade.getConferenceRoleDao().getAll();
 		return all;
 	}
 
 	@Override
 	public List getConferenceTypes() throws Exception {
 		List<ConferenceTypeBean> all = daoFacade.getConferenceTypeDao().getAll();
 		return all;
 	}
 
 	@Override
 	public boolean canCreateConf(String lccno) throws Exception {
 		int confNum = daoFacade.getConferenceNewDao().getValidConfNum();
 		int maxNum = Integer.parseInt(PropertiesUtil.getConstant("max.newconf"));
 		if(confNum < maxNum){
 			return true;
 		}else{
 			throw new ArgsException("validate.ismaxconf");
 		}
 	}
 
 	@Override
 	public boolean validateConfData(String[] paras, int conftype)
 			throws Exception {
 		Map<String, Counter> map = new HashMap<String, Counter>();
 		for(String para : paras){
 			String roleId = para.split("_")[0];
 			if(map.containsKey(roleId)){
 				map.get(roleId).add();
 			}else{
 				map.put(roleId, new Counter(1));
 			}
 		}
 		List<ConferenceTypeRoleBean> roles = daoFacade.getConferenceTypeRoleDao().getConfRolesByType(conftype);
 //		Map<String, ConferenceTypeRoleBean> condition = new HashMap<String, ConferenceTypeRoleBean>();
 //		for(ConferenceTypeRoleBean role : roles){
 //			condition.put(role.getConferenceRoleId() + "", role);
 //		}
 		for(ConferenceTypeRoleBean role : roles){
 			String roleId = role.getConferenceRoleId() + "";
 			int num = -1;
 			Counter counter = map.get(roleId);
 			if(counter == null){
 				num = 0 ;
 			}else{
 				num = counter.getNum();
 			}
 			if(role.getMinnum() != -1 && num < role.getMinnum()){
 				String msg = PropertiesUtil.getProperty("exceptionResource", "args.confRoleMinLimit", true);
 				msg = StringUtil.getFormatString(msg, role.getConferenceTypeName(), role.getMinnum(), role.getConferenceRoleName());
 				throw new ArgsException(msg);
 			}
 			if(role.getMaxnum() != -1 && num > role.getMaxnum()){
 				String msg = PropertiesUtil.getProperty("exceptionResource", "args.confRoleMaxLimit", true);
 				msg = StringUtil.getFormatString(msg, role.getConferenceTypeName(), role.getMaxnum(), role.getConferenceRoleName());
 				throw new ArgsException(msg);
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean grantAuthority(String confNo, String lccno,String roleName)
 			throws Exception {
 		ConferenceRoleBean roleBean = new ConferenceRoleBean();
 		roleBean.setRoleName(roleName);
 		List<ConferenceRoleBean> roles = daoFacade.getConferenceRoleDao().getByExample(roleBean);
 		if(roles!=null && roles.size()==1){
 			ConfRoleAuthorityBean raBean = new ConfRoleAuthorityBean();
 			raBean.setRoleId(roles.get(0).getId());
 			List<ConfRoleAuthorityBean> roleAuthorities = daoFacade.getConfRoleAuthorityDao().getByExample(raBean);
 			UserBean user = new UserBean();
 			user.setLccAccount(lccno);
 			List<UserBean> users = daoFacade.getUserDao().getByExample(user);
 			ConferenceNewBean conference = new ConferenceNewBean();
 			conference.setConfNo(confNo);
 			List<ConferenceNewBean> conferences = daoFacade.getConferenceNewDao().getByExample(conference);
 			ConferenceUserBean cuBean = new ConferenceUserBean();
 			cuBean.setConferenceId(conferences.get(0).getId());
 			cuBean.setUserId(users.get(0).getId());
 			List<ConferenceUserBean> conferenceUsers = daoFacade.getConferenceUserDao().getByExample(cuBean);
 			if(roleName.equals("主持人")){
 				List paras = new ArrayList();
 				paras.add(roles.get(0).getId());
 				paras.add(conferences.get(0).getId());
 				daoFacade.getConfUserRoleDao().executeUpdate("delete from ConfUserAuthorityBean cua " +
 						"where cua.id in (select ua.id from ConfUserAuthorityBean ua,ConferenceUserBean cu," +
 						"ConferenceNewBean c,ConfRoleAuthorityBean ra " +
 						"where c.id=cu.conferenceId and ra.roleId=? and c.id=? " +
 						"and cu.id=ua.conferenceUserId and ra.authorityId=ua.authorityId and c.del=1)" , paras);
 				List paras1 = new ArrayList();
 				paras1.add(roles.get(0).getId());
 				paras1.add(conferences.get(0).getId());
 				paras1.add(roles.get(0).getId());
 				daoFacade.getConfUserRoleDao().executeUpdate("delete from ConfUserRoleBean cur " +
 						"where cur.conferenceUserId=(select cu.id from ConferenceUserBean cu,ConfUserRoleBean ur,ConferenceNewBean c where cu.id=ur.conferenceUserId and c.id=cu.conferenceId and ur.roleId=? and c.id=? and c.del=1)" +
 						" and cur.roleId=?", paras1);
 				
 			}
 			ConfUserRoleBean curBean = new ConfUserRoleBean();
 			curBean.setConferenceUserId(conferenceUsers.get(0).getId());
 			curBean.setRoleId(roles.get(0).getId());
 			List<ConfUserRoleBean> confUserRoles = daoFacade.getConfUserRoleDao().getByExample(curBean);
 			if(confUserRoles == null || confUserRoles.size()<1){
 				daoFacade.getConfUserRoleDao().save(curBean);
 			}
 			List<AuthorityBean> authorityList = new ArrayList<AuthorityBean>();
 			for(ConfRoleAuthorityBean confRoleAuthorityBean:roleAuthorities){
 				ConfUserAuthorityBean confUserAuthorityBean = new ConfUserAuthorityBean();
 				confUserAuthorityBean.setAuthorityId(confRoleAuthorityBean.getAuthorityId());
 				confUserAuthorityBean.setConferenceUserId(conferenceUsers.get(0).getId());
 				List<ConfUserAuthorityBean> confUserAuthorities = daoFacade.getConfUserAuthorityDao().getByExample(confUserAuthorityBean);
 				if(confUserAuthorities == null || confUserAuthorities.size()<1){
 					daoFacade.getConfUserAuthorityDao().save(confUserAuthorityBean);
 				}
 				confUserAuthorityBean = null;
 				AuthorityBean authorityBean = daoFacade.getAuthorityDao().get(confRoleAuthorityBean.getAuthorityId());
 				authorityList.add(authorityBean);
 			}
 			
 			OpenfireUtil.getInstance().sendConfAuthorityUpdateBroadcast(BroadcastEvent.GRANT_CONF_AUTHORITY, confNo, lccno,authorityList,roleName);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean revokeAuthority(String confNo, String lccno,String roleName)
 			throws Exception {
 		ConferenceRoleBean roleBean = new ConferenceRoleBean();
 		roleBean.setRoleName(roleName);
 		List<ConferenceRoleBean> roles = daoFacade.getConferenceRoleDao().getByExample(roleBean);
 		List params = new ArrayList();
 		if(roles!=null && roles.size()==1){
 			
 			List list = daoFacade.getConferenceUserDao().queryByHql("select cu.id,cu.conferenceId,cu.userId from ConferenceUserBean cu where cu.conferenceId=(select c.id from ConferenceNewBean c where c.confNo='"+confNo+"' and c.del=1) and cu.userId=(select u.id from UserBean u where u.lccAccount='"+lccno+"' and u.status=1)");
 			if(list!=null && list.size()==1){
 				ConfRoleAuthorityBean raBean = new ConfRoleAuthorityBean();
 				raBean.setRoleId(roles.get(0).getId());
 				List<ConfRoleAuthorityBean> roleAuthorities = daoFacade.getConfRoleAuthorityDao().getByExample(raBean);
 				Object[] objs = (Object[])list.get(0);
 				Integer confefenceUserId = (Integer)objs[0];
 				Integer conferenceId = (Integer)objs[1];
 				Integer userId = (Integer)objs[2];
 				ConfUserRoleBean confUserRoleBean = new ConfUserRoleBean();
 				confUserRoleBean.setConferenceUserId(confefenceUserId);
 				confUserRoleBean.setRoleId(roles.get(0).getId());
 				daoFacade.getConfUserRoleDao().executeUpdate("delete from ConfUserRoleBean cur where cur.roleId=" + roles.get(0).getId() + " and cur.conferenceUserId="+confefenceUserId, params);//.delete(confUserRoleBean);
 				List<AuthorityBean> authorityList = new ArrayList<AuthorityBean>();
 				ConfUserAuthorityBean confUserAuthorityBean = new ConfUserAuthorityBean();
 				
 				for(ConfRoleAuthorityBean confRoleAuthorityBean:roleAuthorities){
 					confUserAuthorityBean.setAuthorityId(confRoleAuthorityBean.getAuthorityId());
 					confUserAuthorityBean.setConferenceUserId(confefenceUserId);
 					daoFacade.getConfUserAuthorityDao().executeUpdate("delete from ConfUserAuthorityBean cua where cua.authorityId="+confRoleAuthorityBean.getAuthorityId()+" and cua.conferenceUserId="+confefenceUserId, params);
 					AuthorityBean authorityBean = daoFacade.getAuthorityDao().get(confRoleAuthorityBean.getAuthorityId());
 					authorityList.add(authorityBean);
 				}
 				OpenfireUtil.getInstance().sendConfAuthorityUpdateBroadcast(BroadcastEvent.REVOKE_CONF_AUTHORITY, confNo, lccno,authorityList,roleName);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public int inviteUserFromLcm(String inviter, String confno, String invitee)
 			throws Exception {
 		log.info("inviteUserFromeLcm : inviter = " + inviter + " , confno = " + confno + " , invitee = " + invitee);
 		LCMRoleDto roleDto = getMyRoleAndPermission(confno, inviter);
		if(roleDto.getNames().contains("主持人")){//不是主持人不能邀请
 			log.info("inviteUserFromeLcm : inviter = " + inviter + " is not master");
 			return -1; 
 		}
 		CustomerBean firstValidCustomer = serviceFacade.getCustomerService().getFirstValidCustomer();
 		String xmlrpcUrl = firstValidCustomer.getMcuServer().getServerUrl();
 		try{
 			McuUtil.inviteConfUser(xmlrpcUrl, confno, invitee);
 		}catch(Exception e){
 			log.error("mcu invite user not success", e);
 			return -2;
 		}
 		log.info("inviteUserFromeLcm success");
 		return 0;
 	}
 
 	
 	@Override
 	public Object[] getCurrentForwardConfInfo() throws Exception {
 		CustomerBean firstValidCustomer = serviceFacade.getCustomerService().getFirstValidCustomer();
 		String xmlrpcUrl = firstValidCustomer.getMcuServer().getServerUrl();
 		List<XmlrpcConf> confs = McuXmlrpc.getForwardConferenceList(xmlrpcUrl);
 		if(confs.size() == 0){
 			log.info("getCurrentForwardConfInfo : no conf");
 			return null;
 		}
 		Object[] objs = new Object[confs.size()];
 		for(int i = 0; i < objs.length; i++){
 			String[] strs = new String[3];
 			XmlrpcConf conf = confs.get(i);
 			strs[0] = conf.getConfno();
 			List<UserBean> users = daoFacade.getConferenceNewDao().getConfRoleUser(conf.getConfno(), 1);//主持人
 			if(users != null){
 				strs[1] = users.get(0).getLccAccount();//只有一个主持人
 			}
 			strs[2] = conf.getMemberCount() + "";
 			//返回object数组，数组里面的每一个元素是字符串数组，其中string[0]代表会议号码，其中string[1]主持人，string[2]当前人数
 			log.info("getCurrentForwardConfInfo : confno = " + strs[0] + " & master = " + strs[1] + " & memberCount = " + strs[2]);
 			objs[i] = strs;
 		}
 		return objs;
 	}
 
 }
