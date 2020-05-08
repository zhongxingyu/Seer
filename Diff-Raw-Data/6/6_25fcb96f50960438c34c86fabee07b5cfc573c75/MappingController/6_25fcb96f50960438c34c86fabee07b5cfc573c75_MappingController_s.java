 package xyz.sample.baremvc.mapping;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.RequestBody;
 
 @Controller
 @RequestMapping(value = "/mapping")
 public class MappingController {
 	
 	@RequestMapping(value = "/path")
 	public @ResponseBody String byPath() {
 		return "Mapping by Path";
 	}
 	
 	@RequestMapping(value = "/method", method = RequestMethod.GET)
 	public @ResponseBody String byParameterAll() {
 		return "Mapping by Method All";
 	}
 	
 	// mapping parameter name foo
 	@RequestMapping(value = "/parameter", method = RequestMethod.GET, params = "foo")
 	public @ResponseBody String byParameter() {
 		return "Mapping by Parameter foo";
 	}
 	
 	// mapping parameter not foo (!foo)
 	@RequestMapping(value = "/parameter", method = RequestMethod.GET, params = "!foo")
 	public @ResponseBody String byParameterNegation() {
 		return "Mapping by Parameter (all parameter not foo)";
 	}
 	
 	@RequestMapping(value = "/consumes", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody String byConsumes(@RequestBody JavaBean javaBean) {
        return "Mapped by path + method + consumable media type (javaBean '" + javaBean + "')";
    }
 	
 	// mapping by produce to json format
 	@RequestMapping(value = "/produces", method = RequestMethod.GET, produces = "application/json")
 	public @ResponseBody JavaBean byProduces() {
 		return new JavaBean();
 	}
 	
 }
