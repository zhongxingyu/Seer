 package ceid.netcins.exo.frontend.handlers;
 
 import java.util.Hashtable;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import rice.Continuation;
 import ceid.netcins.exo.CatalogService;
 
 /**
  * 
  * @author <a href="mailto:loupasak@ceid.upatras.gr">Andreas Loupasakis</a>
  * @author <a href="mailto:ntarmos@cs.uoi.gr">Nikos Ntarmos</a>
  * @author <a href="mailto:peter@ceid.upatras.gr">Peter Triantafillou</a>
  * 
  * "eXO: Decentralized Autonomous Scalable Social Networking"
  * Proc. 5th Biennial Conf. on Innovative Data Systems Research (CIDR),
  * January 9-12, 2011, Asilomar, California, USA.
  * 
  */
 public abstract class FriendRequestBaseHandler extends AbstractHandler {
 	private static final long serialVersionUID = 6574754775170389536L;
 	protected String reqID = null;
 	Continuation<Object, Exception> command = null;
 
 	public FriendRequestBaseHandler(CatalogService catalogService,
 			Hashtable<String, Hashtable<String, Object>> queue) {
 		super(catalogService, queue);
 	}
 
 	@Override
 	public RequestState prepare(HttpServletRequest request,
 			HttpServletResponse response) {
 		if (super.prepare(request, response) == RequestState.FINISHED)
 			return RequestState.FINISHED;
 		if (uid == null) {
 			sendStatus(response, RequestStatus.FAILURE, null);
 			return RequestState.FINISHED;
 		}
 		reqID = getNewReqID(response);
 		command = new Continuation<Object, Exception>() {
 			@Override
 			public void receiveResult(Object result) {
				boolean didit = (result instanceof Boolean && (Boolean)result == true);
 				queueStatus(reqID, didit ? RequestStatus.SUCCESS : RequestStatus.FAILURE, null);
 			}
 
 			@Override
 			public void receiveException(Exception exception) {
 				queueStatus(reqID, RequestStatus.FAILURE, null);
 			}
 		};
 		return RequestState.REMOTE;
 	}
 }
