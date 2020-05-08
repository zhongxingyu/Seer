 package sonique.bango.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import sonique.bango.domain.Agent;
 import sonique.bango.util.SpringSecurityAuthorisedActorProvider;
 
 @Controller
 public class AgentApiController {
 
     private final SpringSecurityAuthorisedActorProvider authorisedActorProvider;
 
     public AgentApiController(SpringSecurityAuthorisedActorProvider authorisedActorProvider) {
         this.authorisedActorProvider = authorisedActorProvider;
     }
 
     @RequestMapping(method = {RequestMethod.GET}, value = "/authenticatedAgent")
     @ResponseBody
     public Agent authenticatedAgent() {
         return authorisedActorProvider.authenticatedAgent();
     }

    @RequestMapping(method = {RequestMethod.POST}, value = "/toggleAvailability")
    @ResponseBody
    public Agent toggleAvailability() {
        Agent agent = this.authenticatedAgent();
        agent.toggleAvailability();

        return agent;
    }
 }
