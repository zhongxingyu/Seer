 package states;
 
 import interfaces.NetworkService;
 import interfaces.WebService;
 
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 import data.Message;
 import data.Service;
 
 public class EnabledState extends AbstractState {
 	private static final long	serialVersionUID	= 1L;
 	private static Logger		logger				= Logger.getLogger(EnabledState.class);
 
 	public EnabledState(Service service) {
		this.service = service;
 	}
 
 	public EnabledState(EnabledState state) {
		service = state.service;
 	}
 
 	public void executeNet(NetworkService net) {
 		logger.debug("Begin");
 		logger.debug("End");
 	}
 
 	public void executeWeb(WebService web) {
 		logger.debug("Begin");
 		logger.debug("End");
 	}
 
 	public String getName() {
 		return "EnabledState";
 	}
 
 	@Override
 	public ArrayList<Message> asMessages(NetworkService net) {
 		logger.debug("Begin");
 		// Nothing to do, for intern usage
 		return new ArrayList<Message>();
 	}
 
 	@Override
 	public EnabledState clone() {
 		return new EnabledState(this);
 	};
 }
