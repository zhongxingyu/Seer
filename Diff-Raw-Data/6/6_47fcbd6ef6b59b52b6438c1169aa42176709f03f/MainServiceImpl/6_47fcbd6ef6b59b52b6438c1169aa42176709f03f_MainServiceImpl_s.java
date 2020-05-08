 package org.cotrix.web.server;
 
 import static org.cotrix.action.MainAction.*;
 import static org.cotrix.web.share.shared.feature.ApplicationFeatures.*;
 import static org.cotrix.web.shared.AuthenticationFeature.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 
 import org.cotrix.action.Action;
 import org.cotrix.action.Actions;
 import org.cotrix.action.CodelistAction;
 import org.cotrix.application.ApplicationEvents;
 import org.cotrix.application.ApplicationEvents.Startup;
 import org.cotrix.application.NewsService;
 import org.cotrix.application.NewsService.NewsItem;
 import org.cotrix.application.StatisticsService;
 import org.cotrix.application.StatisticsService.Statistics;
 import org.cotrix.common.cdi.Current;
 import org.cotrix.engine.Engine;
 import org.cotrix.engine.TaskOutcome;
 import org.cotrix.security.LoginService;
 import org.cotrix.security.impl.DefaultNameAndPasswordCollector;
 import org.cotrix.user.PredefinedUsers;
 import org.cotrix.user.User;
 import org.cotrix.web.client.MainService;
 import org.cotrix.web.share.server.task.ActionMapper;
 import org.cotrix.web.share.shared.feature.FeatureCarrier;
 import org.cotrix.web.share.shared.feature.ResponseWrapper;
 import org.cotrix.web.shared.MainServiceException;
 import org.cotrix.web.shared.UINews;
 import org.cotrix.web.shared.UIStatistics;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @SuppressWarnings("serial")
 public class MainServiceImpl extends RemoteServiceServlet implements MainService {
 
 	protected Logger logger = LoggerFactory.getLogger(MainServiceImpl.class);
 
 	protected static final Callable<Void> NOP = new Callable<Void>() {
 
 		@Override
 		public Void call() throws Exception {
 			return null;
 		}
 	};
 
 	@Inject
 	protected LoginService loginService;
 
 	@Inject
 	protected ActionMapper actionMapper;
 
 	@Inject
 	protected HttpServletRequest httpServletRequest;
 
 	@Inject
 	protected StatisticsService statisticsService;
 
 	@Inject
 	protected NewsService newsService;
 
 	@Inject
 	ActionMapper mapper;
 
 	@Inject
 	Engine engine;
 
 	@Current
 	@Inject
 	User currentUser;
 
 	@Inject
 	Event<ApplicationEvents.Startup> startup;
 	
 	/** 
 	 * {@inheritDoc}
 	 */
 	public void init() {
 
 		mapper.map(LOGIN).to(CAN_LOGIN);
 		mapper.map(LOGOUT).to(CAN_LOGOUT);
 		mapper.map(IMPORT).to(IMPORT_CODELIST);
 		mapper.map(PUBLISH).to(PUBLISH_CODELIST);
 		
 		startup.fire(Startup.INSTANCE);
 		
 	}
 
 	@Override
 	public ResponseWrapper<String> login(final String username, final String password, List<String> openCodelists) {
 		logger.trace("login username: {}",username);
 		return doLogin(LOGIN, username, password, openCodelists);
 	}
 
 	@Override
 	public ResponseWrapper<String> logout(List<String> openCodelists) {
 		logger.trace("logout");
 
 		return doLogin(LOGOUT, null, null, openCodelists);
 	}
 
 	protected ResponseWrapper<String> doLogin(Action action, final String username, final String password, List<String> openCodelists)
 	{
 		logger.trace("doLogin action: {} username: {} openCodelists: {}", action, username, openCodelists);
 
 		User user = null;
 
 		//FIXME workaround to returning user with active session
 		logger.trace("currentUser: "+currentUser);
 		if (username == null 
 				&& password == null 
 				&& currentUser != null 
 				&& currentUser.id() != null 
 				&& !currentUser.id().equals(PredefinedUsers.guest.id()) 
 				&& action==LOGIN) {
 			user = currentUser;
 		} else {
 
 			TaskOutcome<User> outcome = engine.perform(action).with(new Callable<User>() {
 
 				@Override
 				public User call() throws Exception {
 					httpServletRequest.setAttribute(DefaultNameAndPasswordCollector.nameParam, username);
 					httpServletRequest.setAttribute(DefaultNameAndPasswordCollector.pwdParam, password);
 					User user = loginService.login(httpServletRequest);	
 					logger.trace("returned user: {}",user);
 
 					return user;
 				}
 			});
 
 			user = outcome.output();
 		}
 
 		ResponseWrapper<String> wrapper = new ResponseWrapper<String>(user.name());
 
 		Collection<Action> actions = Actions.filterForAction(action, user.permissions());
 
 		actionMapper.fillFeatures(wrapper, actions);
 
 		fillOpenCodelistsActions(openCodelists, user, wrapper);
 
 		return wrapper;
 	}
 
 	protected void fillOpenCodelistsActions(List<String> openCodelists, User user, FeatureCarrier featureCarrier)
 	{
 		for (String openCodelist:openCodelists) fillCodelistActions(openCodelist, user, featureCarrier);
 	}
 
 	protected void fillCodelistActions(String codelistId, User user, FeatureCarrier featureCarrier)
 	{
 		engine.perform(CodelistAction.VIEW.on(codelistId)).with(NOP);
 		Collection<Action> actions = Actions.filterForAction(CodelistAction.VIEW, user.permissions());
 		actionMapper.fillFeatures(featureCarrier, codelistId, actions);
 	}
 
 	@Override
 	public UIStatistics getStatistics() throws MainServiceException {
 		try {
 			Statistics statistics = statisticsService.statistics();
 			UIStatistics uiStatistics = new UIStatistics();
 			uiStatistics.setCodelists(statistics.totalCodelists());
 			uiStatistics.setCodes(statistics.totalCodes());
 			uiStatistics.setUsers(statistics.totalUsers());
 			uiStatistics.setRepositories(statistics.totalRepositories());
 			return uiStatistics;
 		} catch(Exception e) {
 			logger.error("Error getting statistics", e);
 			throw new MainServiceException("Error getting statistics: "+e.getMessage());
 		}
 	}
 
 	@Override
 	public List<UINews> getNews() {
 		List<UINews> news = new ArrayList<UINews>();

 		for (NewsItem newsItem:newsService.news()) {
 			logger.trace("news: {}",newsItem);
 			UINews uiNews = new UINews();
 			uiNews.setTimestamp(newsItem.timestamp());
 			uiNews.setText(newsItem.text());
 			news.add(uiNews);
 		}
 		return news;
 	}
 
 }
