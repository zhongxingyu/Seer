 package cm.h3c.college.pay.demo.web.action;
 
 import javax.annotation.Resource;
 
 import org.apache.log4j.Logger;
 import org.apache.struts2.convention.annotation.Action;
 import org.apache.struts2.convention.annotation.Namespace;
 import org.apache.struts2.convention.annotation.Result;
 
 import cm.h3c.college.pay.demo.service.HelloService;
 
 import com.opensymphony.xwork2.ActionSupport;
 
 @Namespace("/hello")
 public class HelloVelocityAction extends ActionSupport {
 	
 	private Logger log = Logger.getLogger(HelloVelocityAction.class);
 	
 	private static final long serialVersionUID = 3921807869626262750L;
 
 	@Resource(name = "helloService")
 	private HelloService helloService;
 	
 	private String data;
 	
 	@Action(value = "helloVelocity", results = { @Result(name = "success", type = "velocity", location = "/vm/hello.vm") })
 	public String helloVelocity() {
		
 		String msg = helloService.sayHello("zhanghao!");
 		this.data = msg;
 		return SUCCESS;
 	}
 	
 	public String getData() {
 		return data;
 	}
 	
 
 }
