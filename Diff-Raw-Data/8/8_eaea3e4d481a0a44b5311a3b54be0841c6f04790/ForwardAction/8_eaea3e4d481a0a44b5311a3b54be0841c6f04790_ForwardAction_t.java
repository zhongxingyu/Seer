 package utn.frba.tadp.firewall.impl.actions;
 
 import utn.frba.tadp.firewall.api.Action;
 import utn.frba.tadp.firewall.api.Filter;
 import utn.frba.tadp.firewall.impl.Firewall;
 import utn.frba.tadp.firewall.impl.model.Request;
 import utn.frba.tadp.firewall.utils.ApplicationContext;
 
 public class ForwardAction implements Action {
 
 	private Filter filter;
 	private String ipForward;
 	private int puertoForward;
	private Firewall firewall;
 
	public ForwardAction(Filter filter, String ipForward, int puertoForward, Firewall firewall) {
 		this.filter = filter;
 		this.ipForward = ipForward;
 		this.puertoForward = puertoForward;
		this.firewall = firewall;
 	}
 
 	public void makeAction(Request request) {
 		if (filter.accepts(request)) {
 			Request forwardRequest = new Request(puertoForward, ipForward, request.getIpDestino());
 			request.requestForwarded(forwardRequest);
 			firewall.evaluate(forwardRequest);
 		}
 	}
 
 }
