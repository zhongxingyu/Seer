 package com.nbcedu.function.schoolmaster2.action;
 
 import java.util.Date;
 import java.util.List;
 
 import com.nbcedu.function.schoolmaster2.biz.Sm2ProgressBiz;
 import com.nbcedu.function.schoolmaster2.core.action.BaseAction;
 import com.nbcedu.function.schoolmaster2.core.util.struts2.Struts2Utils;
 import com.nbcedu.function.schoolmaster2.data.model.TSm2Progress;
 
 /**
  * 工作进展action
  * @author wl
  */
 @SuppressWarnings("serial")
 public class ProgressAction extends BaseAction{
 
 	private Sm2ProgressBiz progBiz;
 	
 	private TSm2Progress progress = new TSm2Progress();
 	private String stepId;
 	private String name;
 	/**
 	 * 增加工作进展
 	 */
 	public void add(){
 		progress.setCreaterId(this.getUserId());
 		progress.setCreateTime(new Date());
		this.progBiz.add(progress);
		Struts2Utils.renderText("0","encoding:UTF-8");
 	}
 	public void isExist(){
 		List<TSm2Progress> p = this.progBiz.findByNameStepId(stepId, name);
 		if(p==null||p.size()<1){
 			Struts2Utils.renderText("0","encoding:UTF-8");
 		}
 	}
 	
 	public String changeStep(){
 		this.progBiz.modifyStep(this.stepId, this.id);
 		this.stepId = this.getRequest().getParameter("originStepId").toString();
 		return "refreshStep";
 	}
 	////////////////////////
 	////getters&setters////
 	//////////////////////
 	public TSm2Progress getProgress() {
 		return progress;
 	}
 	public void setProgress(TSm2Progress progress) {
 		this.progress = progress;
 	}
 	public String getStepId() {
 		return stepId;
 	}
 	public void setStepId(String stepId) {
 		this.stepId = stepId;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public void setProgBiz(Sm2ProgressBiz progBiz) {
 		this.progBiz = progBiz;
 	}
 	
 }
