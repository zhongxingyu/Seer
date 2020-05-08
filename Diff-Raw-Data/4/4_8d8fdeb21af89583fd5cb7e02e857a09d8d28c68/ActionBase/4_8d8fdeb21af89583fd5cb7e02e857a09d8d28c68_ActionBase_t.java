 package Action.ActionBase;
 
import com.opensymphony.xwork2.ActionSupport;

 /**
  * 文件名：ActionBase.java All right Rserved liqx2012
  * 
  * @author 李青鑫 E-mail: lqx0830@hotmail.com
  * @version 1.0,创建时间：2012-3-10 下午05:19:59
  * @since jdk1.6 类说明
  */
 public interface ActionBase {
	ActionSupport as = new ActionSupport();
 	String SUCCESS = "success";
 	String ERROR = "error";
 	String INPUT = "input";
 
 	public String execute();
 }
