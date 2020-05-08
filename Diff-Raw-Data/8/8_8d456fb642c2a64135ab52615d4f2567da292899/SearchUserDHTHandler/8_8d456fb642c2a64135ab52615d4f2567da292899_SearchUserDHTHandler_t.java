 package ceid.netcins.exo.frontend.handlers;
 
 import java.util.Hashtable;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import rice.Continuation;
 import ceid.netcins.exo.CatalogService;
import ceid.netcins.exo.catalog.ScoreBoard;
 import ceid.netcins.exo.messages.ResponsePDU;
 
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
 public class SearchUserDHTHandler extends AbstractHandler {
 	private static final long serialVersionUID = 2138885947637986858L;
 
 	public SearchUserDHTHandler(CatalogService catalogService,
 			Hashtable<String, Hashtable<String, Object>> queue) {
 		super(catalogService, queue);
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException {
 		if (prepare(request, response) == RequestState.FINISHED)
 			return;
 
 		if (rawQuery == null || queryTopK == null) {
 			sendStatus(response, RequestStatus.FAILURE, null);
 			return;
 		}
 
 		final String reqID = getNewReqID(response);
 		catalogService.searchUser(rawQuery, queryTopK, new Continuation<Object, Exception>() {
 			@Override
 			public void receiveResult(Object arg0) {
				ScoreBoard sb = null;
				if (arg0 == null || !(arg0 instanceof ResponsePDU) || (sb = ((ResponsePDU)arg0).getScoreBoard()) == null) {
 					queueStatus(reqID, RequestStatus.FAILURE, null);
 					return;
 				}
				queueStatus(reqID, RequestStatus.SUCCESS, sb);
 			}
 
 			@Override
 			public void receiveException(Exception arg0) {
 				queueStatus(reqID, RequestStatus.FAILURE, null);
 			}
 		});
 	}
 }
