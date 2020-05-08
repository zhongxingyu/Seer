 package com.lorent.dao.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.lorent.common.dto.LCMConferenceDto;
 import com.lorent.common.dto.LCMRoleDto;
 import com.lorent.dao.ConferenceNewDao;
 import com.lorent.dao.GenericDaoImpl;
 import com.lorent.model.AuthorityBean;
 import com.lorent.model.ConferenceNewBean;
 import com.lorent.model.UserBean;
 import com.lorent.util.StringUtil;
 
 public class ConferenceNewDaoImpl extends GenericDaoImpl<ConferenceNewBean,Integer> implements ConferenceNewDao{
 	public LCMRoleDto getMyRoleAndPermission(String confno, String lccno)throws Exception{
 		return getRoleAndPermission(confno, lccno, true);
 	}
 	@Override
 	public List getConferenceNewsByConfNo(String confNo) throws Exception {
 		String hql = "select c from ConferenceNewBean c where c.del=1 and c.confNo = ";
 		List list = queryByHql(hql + "'" + confNo + "'");
 		return list;
 	}
 	
 	public List getConferenceNoByConferenceIds(Integer[] ids) throws Exception {
 		List list = null;
 		String strIds = StringUtil.changeIntegerArrayToString(ids);
 		list = queryByHql("select cn.confNo from ConferenceNewBean cn where cn.id in ("+ strIds +")");
 		return list;
 	}
 
 	@Override
 	public List getUsersByConferenceId(Integer conferenceId) throws Exception {
 		List list = queryByHql("select ub from UserBean ub where ub.id in (select cu.userId from ConferenceUserBean cu where cu.conferenceId=" + conferenceId + ")" );
 		return list;
 	}
 
 	@Override
 	public void removeConferenceNewBeanByIds(Integer[] ids) throws Exception {
 		List list = null;
 		String strIds = StringUtil.changeIntegerArrayToString(ids);
 		executeUpdate("update ConferenceNewBean cn set cn.del=0 where cn.id in ("+strIds+")", list);
 	}
 	@Override
 	public Map<String, LCMRoleDto> getConfUserRole(String confno, String[] lccnos) throws Exception {
 //		Map<String, LCMRoleDto> map = new HashMap<String, LCMRoleDto>();
 //		for(String lccno : lccnos){
 //			map.put(lccno, getRoleAndPermission(confno, lccno, false));
 //		}
 //		return map;
 		HashMap<String, LCMRoleDto> hashMap = new HashMap<String, LCMRoleDto>();
 		String[] temp = StringUtil.parseObjectArrayToArray(lccnos, String.class);
 		for (String lccno : temp) {
 			LCMRoleDto myRoleAndPermission = getMyRoleAndPermission(confno, lccno);
 			hashMap.put(lccno, myRoleAndPermission);
 		}
 		return hashMap;
 	}
 	
 	@Override
 	public List getConferenceDto() throws Exception {
 //		LCMConferenceDto result = new LCMConferenceDto();
 		ArrayList<LCMConferenceDto> result = new ArrayList<LCMConferenceDto>();
 		
 		String hql = "select t.typeName,r.roleName,u.realName"+
 		",conf.conferenceName,conf.creator,conf.needApply,conf.defaultRoleId"+
 		",conf.conferenceTypeId,conf.del,conf.confNo,conf.topic,conf.description,conf.password,conf.id"+
 		" from ConferenceNewBean conf,ConferenceTypeBean t,ConferenceRoleBean r,UserBean u"+
 		" where conf.del = 1 and conf.conferenceTypeId=t.id "+
 		" and conf.defaultRoleId=r.id and conf.creator = u.id";
 		List list = this.queryByHql(hql);
 		for (Object object : list) {
 			Object[] row = (Object[]) object;
 			LCMConferenceDto lcmConferenceDto = new LCMConferenceDto();
 			lcmConferenceDto.setConferenceTypeName((String) row[0]);
 			lcmConferenceDto.setDefaultRoleName((String) row[1]);
 			lcmConferenceDto.setCreatorName((String) row[2]);
 			lcmConferenceDto.setConferenceName((String) row[3]);
 			lcmConferenceDto.setCreator((Integer) row[4]);
 			lcmConferenceDto.setNeedApply((Integer) row[5]);
 			lcmConferenceDto.setDefaultRoleId((Integer) row[6]);
 			lcmConferenceDto.setConferenceTypeId((Integer) row[7]);
 			lcmConferenceDto.setDel((Integer) row[8]);
 			lcmConferenceDto.setConfNo((String) row[9]);
 			lcmConferenceDto.setTopic((String) row[10]);
 			lcmConferenceDto.setDescription((String) row[11]);
 			lcmConferenceDto.setPassword((String) row[12]);
 			lcmConferenceDto.setId((Integer) row[13]);
 			result.add(lcmConferenceDto);
 		}
 		return result;
 	}
 	public LCMRoleDto getRoleAndPermission(String confno, String lccno, boolean getPermission){
 		LCMRoleDto data = new LCMRoleDto();
 		//get confuser id
 		String hql = "select cu.id, u.realName, c.conferenceName " +
 				"from ConferenceNewBean c ,ConferenceUserBean cu ,UserBean u " +
 				"where c.id = cu.conferenceId and cu.userId = u.id and " +
 				"u.lccAccount = '" + lccno + "' and c.confNo = '" + confno + "'";
 		List ret = this.queryByHql(hql);
 		if (!ret.isEmpty()) {
 			Object[] rets = (Object[])ret.get(0);
 			
 			int confUserId =(Integer)rets[0];
 			String nickname =(String)rets[1];
 			String conferenceName = (String)rets[2];
 			data.setNickname(nickname);
 			data.setConferenceTitle(conferenceName);
 			//get role name
 			hql = "select r.roleName " +
 					"from ConferenceRoleBean r ,ConfUserRoleBean ur " +
 					"where ur.roleId = r.id and " +
 					"ur.conferenceUserId = " + confUserId;
 			ret = this.queryByHql(hql);
 			for(Object o : ret){
 				data.addName((String)o);
 			}
 			
 			if(getPermission){
 				//get permission
 				hql = "select a " +
 				"from AuthorityBean a, ConfUserAuthorityBean ua " +
 				"where ua.authorityId = a.id and ua.conferenceUserId = " + confUserId;
 				ret = this.queryByHql(hql);
 				for(Object o : ret){
 					AuthorityBean temp = (AuthorityBean)o;
 					data.addPermission(temp.getMark(), temp.getAuthorityName());
 				}
 			}
 		}
 		return data;
 	}
 	
 	//查询某一会议某一角色的用户
 	public List<UserBean> getConfRoleUser(String confno, int roleId){
 		String hql = "select u " +
 		"from ConferenceNewBean c ,ConferenceUserBean cu ,UserBean u, ConfUserRoleBean cur " +
 		"where c.id = cu.conferenceId and cu.userId = u.id and cur.conferenceUserId = cu.id " +
		"cur.roleId = " + roleId + " and c.confNo = '" + confno + "'";
 		List<UserBean> list = this.queryByHql(hql);
 		if(list != null && list.size() > 0){
 			return list;
 		}else{
 			return null;
 		}
 	}
 
 	@Override
 	public void getSearchSubPageMapResult(Map<String, Object> map, Map<String,Object> params)
 			throws Exception {
 		StringBuffer hql = new StringBuffer();
 		List list = new ArrayList();
 		hql.append("select cn.id,cn.conferenceName,cr.roleName,ct.typeName,ub.username,cn.confNo from ConferenceNewBean cn,ConferenceRoleBean cr,ConferenceTypeBean ct,UserBean ub where cn.del=1 and cn.defaultRoleId=cr.id and cn.conferenceTypeId=ct.id and cn.creator=ub.id ");
 		if(!"".equals((String)params.get("isSearch"))){
 			//conferenceNewSearchModel = conferenceNew;
 			ConferenceNewBean conferenceNewSearchModel = (ConferenceNewBean)params.get("conferenceNewSearchModel");
 			if(conferenceNewSearchModel.getConferenceName()!=null && conferenceNewSearchModel.getConferenceName().length()>0){
 				hql.append(" and cn.conferenceName like ?");
 				list.add("%" + conferenceNewSearchModel.getConferenceName() + "%");
 			}
 			if(conferenceNewSearchModel.getConferenceTypeId()!=null && conferenceNewSearchModel.getConferenceTypeId().intValue()>0){
 				list.add(conferenceNewSearchModel.getConferenceTypeId());
 				hql.append(" and ct.id=?");
 			}
 			
 		}
 		hql.append("order by cn." + (String)params.get("orderString") + " " + (String)params.get("ascOrDesc"));
 		getSubPageResult(hql.toString(), map, list);
 	}
 
 	@Override
 	public void getInitSubPageMapResult(Map<String, Object> map,
 			Map<String, Object> params) throws Exception {
 		StringBuffer hql = new StringBuffer();
 		hql.append("select cn.id,cn.conferenceName,cr.roleName,ct.typeName,ub.username,cn.confNo from ConferenceNewBean cn,ConferenceRoleBean cr,ConferenceTypeBean ct,UserBean ub where cn.del=1 and cn.defaultRoleId=cr.id and cn.conferenceTypeId=ct.id and cn.creator=ub.id order by cn." + (String)params.get("orderString") + " " + (String)params.get("ascOrDesc"));
 		List list = null;
 		getSubPageResult(hql.toString(), map, list);
 	}
 	
 	@Override
 	public int getValidConfNum() {
 		String hql = "select count(c.id) from ConferenceNewBean c where c.del = 1";
 		List ret = this.queryByHql(hql);
 		Object temp = ret.get(0);
 		if(temp instanceof Long){
 			return ((Long)temp).intValue();
 		}else{
 			return (Integer)temp;
 		}
 	}
 	
 
 }
