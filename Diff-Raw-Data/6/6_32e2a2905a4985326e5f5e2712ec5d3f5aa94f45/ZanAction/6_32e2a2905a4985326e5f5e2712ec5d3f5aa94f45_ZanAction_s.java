 package com.nbcedu.function.schoolmaster2.action;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.google.common.reflect.TypeToken;
 import com.nbcedu.function.functionsupport.core.FunctionSupportUtil;
 import com.nbcedu.function.functionsupport.core.SupportManager;
 import com.nbcedu.function.functionsupport.mapping.PortalMessage;
 import com.nbcedu.function.schoolmaster2.biz.SM2MasterSubBiz;
 import com.nbcedu.function.schoolmaster2.biz.SM2ZanBiz;
 import com.nbcedu.function.schoolmaster2.core.action.BaseAction;
 import com.nbcedu.function.schoolmaster2.core.util.Struts2Util;
 import com.nbcedu.function.schoolmaster2.core.util.struts2.Struts2Utils;
import com.nbcedu.function.schoolmaster2.data.model.SM2SubjectMaster;
 import com.nbcedu.function.schoolmaster2.data.model.Sm2Zan;
 import com.nbcedu.function.schoolmaster2.utils.Utils;
 import com.nbcedu.function.schoolmaster2.vo.SubjectZanVo;
 import com.nbcedu.function.schoolmaster2.vo.ZanVo;
 
 /**
  * 赞action
  * @author xuechong
  */
 @SuppressWarnings("serial")
 public class ZanAction extends BaseAction{
 	
 	private SM2ZanBiz zanBiz;
 	private SM2MasterSubBiz subBiz;
 	private Integer zanSize = null;
 	
 	/**
 	 * 新增赞
 	 * @author xuechong
 	 */
 	public void add(){
 		Sm2Zan zan = new Sm2Zan();
 		zan.setProgressId(this.id);
 		zan.setUserName(Utils.curUserName());
 		zan.setUserUid(getUserId());
 		zan.setCreateTime(new Date());
 		Sm2Zan upResult = (Sm2Zan) this.zanBiz.addOrUpdate(zan);
 		Boolean result = upResult!=null;
 		if(result){
 			sendMsg(upResult);
 		}
		Struts2Util.renderText(result.toString());
		
 	}
 	
 	/**
 	 * 查询有多少人赞过
 	 * @author xuechong
 	 */
 	public void showZans(){
 		List<ZanVo> zans = this.zanBiz.findByProg(this.id, zanSize);
 		String json = Utils.gson.toJson(zans, 
 				new TypeToken<List<ZanVo>>() {}.getType());
 		Struts2Utils.renderJson(json);
 	}
 	
 	/**
 	 * 取消zan
 	 * @author xuechong
 	 */
 	public void cancel(){
 		String result = "suc";
 		try {
 			this.zanBiz.removeByUserProg(this.id);
 		} catch (Exception e) {
 			logger.error("取消赞出现错误",e);
 			result = "err";
 		}
 		Struts2Utils.renderText(result);
 	}
 	
 	private void sendMsg(Sm2Zan zan){
 		
 		final SubjectZanVo sub = this.subBiz.findByProgId(zan.getProgressId());
 		String content = zan.getUserName() + "赞了你的主题:" + sub.getTitle();
 		
 		FunctionSupportUtil util = SupportManager.getFunctionSupportUtil();
 		PortalMessage message = new PortalMessage();
 		message.setModuleName("校长工作台");
 		message.setFunctionName("scMaster2");
 		message.setMessageId(zan.getId());
 		message.setTitle(content);
 		message.setUrl("scMaster2/detail_master.action?id=" + sub.getId());
 		message.setReceiverUids(new LinkedList<String>(){{add(sub.getCreatorId());}});
 		util.sendPortalMsg(message, null);
 		
 		util.sendOAMessage(zan.getUserUid(), sub.getCreatorId(), content);
 	}
 	
 	///////////////////////
 	////getters&setters////
 	///////////////////////
 	public void setZanBiz(SM2ZanBiz zanBiz) {
 		this.zanBiz = zanBiz;
 	}
 	public Integer getZanSize() {
 		return zanSize;
 	}
 	public void setZanSize(Integer zanSize) {
 		this.zanSize = zanSize;
 	}
 	public void setSubBiz(SM2MasterSubBiz subBiz) {
 		this.subBiz = subBiz;
 	}
 	
 	
 }
