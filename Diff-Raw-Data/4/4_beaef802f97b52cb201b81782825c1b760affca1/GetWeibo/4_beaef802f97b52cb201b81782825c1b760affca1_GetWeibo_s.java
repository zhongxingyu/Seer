 /**
  * 
  */
 package q.web.weibo;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import q.dao.DaoHelper;
 import q.dao.FavoriteDao;
 import q.dao.GroupDao;
 import q.dao.PeopleDao;
 import q.dao.WeiboDao;
 import q.dao.page.WeiboReplyPage;
 import q.domain.Weibo;
 import q.domain.WeiboReply;
 import q.util.CollectionKit;
 import q.util.IdCreator;
 import q.web.Resource;
 import q.web.ResourceContext;
 import q.web.exception.RequestParameterInvalidException;
 
 /**
  * @author seanlinwang
  * @email xalinx at gmail dot com
  * @date Feb 22, 2011
  * 
  */
 public class GetWeibo extends Resource {
 	private WeiboDao weiboDao;
 
 	public void setWeiboDao(WeiboDao weiboDao) {
 		this.weiboDao = weiboDao;
 	}
 
 	private PeopleDao peopleDao;
 
 	public void setPeopleDao(PeopleDao peopleDao) {
 		this.peopleDao = peopleDao;
 	}
 
 	private GroupDao groupDao;
 
 	public void setGroupDao(GroupDao groupDao) {
 		this.groupDao = groupDao;
 	}
 
 	private FavoriteDao favoriteDao;
 
 	public void setFavoriteDao(FavoriteDao favoriteDao) {
 		this.favoriteDao = favoriteDao;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see q.web.Resource#execute(q.web.ResourceContext)
 	 */
 	@Override
 	public void execute(ResourceContext context) throws Exception {
 		long weiboId = context.getResourceIdLong();
 		long loginPeopleId = context.getCookiePeopleId();
 
 		Weibo weibo = weiboDao.getWeiboById(weiboId);
		DaoHelper.injectWeiboModelWithQuote(weiboDao, weibo);
 		DaoHelper.injectWeiboModelWithPeople(peopleDao, weibo);
 		DaoHelper.injectWeiboModelWithFrom(groupDao, weibo);
 		if (loginPeopleId > 0) {
 			DaoHelper.injectWeiboWithFavorite(favoriteDao, weibo, loginPeopleId);
 		}
 		context.setModel("weibo", weibo);
 
 		WeiboReplyPage page = new WeiboReplyPage();
 		page.setQuoteWeiboId(weiboId);
 		int size = context.getInt("size", 10);
 		long startId = context.getIdLong("startId");
 		page.setSize(size);
 		page.setStartIndex(0);
 		if (startId > 0) {
 			page.setStartId(startId);
 		}
 		List<WeiboReply> replies = weiboDao.getWeiboRepliesByPage(page);
 		if (CollectionKit.isNotEmpty(replies)) {
 			DaoHelper.injectWeiboModelsWithPeople(peopleDao, replies);
 			DaoHelper.injectWeiboModelsWithFrom(groupDao, replies);
 			if (loginPeopleId > 0) {
 				DaoHelper.injectWeiboModelsWithFavorite(favoriteDao, replies, loginPeopleId);
 			}
 			context.setModel("replies", replies);
 		}
 
 		if (context.isApiRequest()) {
 			Map<String, Object> api = new HashMap<String, Object>();
 			api.put("weibo", weibo);
 			if (CollectionKit.isNotEmpty(replies)) {
 				api.put("replies", replies);
 			}
 			context.setModel("api", api);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see q.web.Resource#validate(q.web.ResourceContext)
 	 */
 	@Override
 	public void validate(ResourceContext context) throws Exception {
 		long weiboId = context.getResourceIdLong();
 		if (IdCreator.isNotValidId(weiboId)) {
 			throw new RequestParameterInvalidException("weibo:invalid");
 		}
 	}
 
 }
