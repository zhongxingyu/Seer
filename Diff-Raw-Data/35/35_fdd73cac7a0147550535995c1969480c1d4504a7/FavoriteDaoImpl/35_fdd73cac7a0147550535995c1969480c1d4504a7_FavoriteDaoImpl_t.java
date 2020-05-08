 /**
  * 
  */
 package q.dao.ibatis;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import q.dao.FavoriteDao;
 import q.dao.page.FavoritePage;
 import q.domain.Favorite;
 import q.domain.FavoriteFromType;
 import q.domain.FavoriteStatus;
 import q.util.IdCreator;
 
 /**
  * @author seanlinwang
  * @email xalinx at gmail dot com
  * @date Mar 1, 2011
  * 
  */
 public class FavoriteDaoImpl extends AbstractDaoImpl implements FavoriteDao {
 
 	private void addFavorite(Favorite favorite) throws SQLException {
 		favorite.setId(IdCreator.getLongId());
 		this.sqlMapClient.insert("insertFavorite", favorite);
 	}
 
 	@Override
 	public void addReplyFavorite(long replyId, long creatorId, long senderId) throws SQLException {
 		Favorite favorite = new Favorite();
 		favorite.setCreatorId(creatorId);
 		favorite.setStatus(FavoriteStatus.FAV);
 		favorite.setFromType(FavoriteFromType.REPLY);
 		favorite.setFromId(replyId);
 		favorite.setSenderId(senderId);
 		this.addFavorite(favorite);
 	}
 
 	@Override
 	public void addWeiboFavorite(long weiboId, long creatorId, long senderId) throws SQLException {
 		Favorite favorite = new Favorite();
 		favorite.setCreatorId(creatorId);
 		favorite.setStatus(FavoriteStatus.FAV);
 		favorite.setFromType(FavoriteFromType.WEIBO);
 		favorite.setFromId(weiboId);
 		favorite.setSenderId(senderId);
 		this.addFavorite(favorite);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see q.dao.FavoriteDao#getPageFavorites(q.dao.page.FavoritePage)
 	 */
 	@Override
 	public List<Favorite> getPageFavorites(FavoritePage page) throws SQLException {
 		@SuppressWarnings("unchecked")
 		List<Favorite> favorites = this.sqlMapClient.queryForList("selectFavoritesByPage", page);
 		return favorites;
 	}
 
 	@Override
 	public List<Long> getFavReplyIds(List<Long> replyIds, long creatorId) throws SQLException {
 		FavoritePage page = new FavoritePage();
 		page.setCreatorId(creatorId);
 		page.setStatus(FavoriteStatus.FAV);
		//page.setFromType(FavoriteFromType.REPLY);
 		page.setFromIds(replyIds);
 		@SuppressWarnings("unchecked")
 		List<Long> ids = this.sqlMapClient.queryForList("selectFavoriteFromIdsByPage", page);
 		return ids;
 	}
 
 	@Override
 	public List<Long> getFavWeiboIds(List<Long> weiboIds, long creatorId) throws SQLException {
 		FavoritePage page = new FavoritePage();
 		page.setCreatorId(creatorId);
 		page.setStatus(FavoriteStatus.FAV);
 		page.setFromType(FavoriteFromType.WEIBO);
 		page.setFromIds(weiboIds);
 		@SuppressWarnings("unchecked")
 		List<Long> ids = this.sqlMapClient.queryForList("selectFavoriteFromIdsByPage", page);
 		return ids;
 	}
 
 	@Override
 	public Favorite getReplyFavoriteByReplyIdAndCreatorId(long replyId, long creatorId) throws SQLException {
 		FavoritePage page = new FavoritePage();
 		page.setCreatorId(creatorId);
 		page.setStatus(FavoriteStatus.FAV);
 		page.setFromType(FavoriteFromType.REPLY);
 		page.setFromId(replyId);
 		return (Favorite) this.sqlMapClient.queryForObject("selectFavoriteByPage", page);
 	}
 
 	@Override
 	public Favorite getWeiboFavoriteByWeiboIdAndCreatorId(long weiboId, long creatorId) throws SQLException {
 		FavoritePage page = new FavoritePage();
 		page.setCreatorId(creatorId);
 		page.setStatus(FavoriteStatus.FAV);
 		page.setFromType(FavoriteFromType.WEIBO);
 		page.setFromId(weiboId);
 		return (Favorite) this.sqlMapClient.queryForObject("selectFavoriteByPage", page);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see q.dao.FavoriteDao#unFavReplyFavorite(long, long)
 	 */
 	@Override
 	public void unFavReplyFavorite(long replyId, long creatorId) throws SQLException {
 		FavoritePage page = new FavoritePage();
 		page.setCreatorId(creatorId);
 		page.setStatus(FavoriteStatus.UNFAV);
 		page.setFromType(FavoriteFromType.REPLY);
 		page.setFromId(replyId);
 		this.sqlMapClient.update("updateFavoriteStatus", page);
 	}
 
 	@Override
 	public void unFavWeiboFavorite(long weiboId, long creatorId) throws SQLException {
 		FavoritePage page = new FavoritePage();
 		page.setCreatorId(creatorId);
 		page.setStatus(FavoriteStatus.UNFAV);
 		page.setFromType(FavoriteFromType.WEIBO);
 		page.setFromId(weiboId);
 		this.sqlMapClient.update("updateFavoriteStatus", page);
 	}
 
 	@Override
 	public void favReplyById(long id) throws SQLException {
 		FavoritePage page = new FavoritePage();
 		page.setId(id);
 		page.setStatus(FavoriteStatus.FAV);
 		page.setFromType(FavoriteFromType.REPLY);
 		this.sqlMapClient.update("updateFavoriteStatus", page);
 	}
 
 	@Override
 	public void favWeiboById(long id) throws SQLException {
 		FavoritePage page = new FavoritePage();
 		page.setId(id);
 		page.setStatus(FavoriteStatus.FAV);
 		page.setFromType(FavoriteFromType.WEIBO);
 		this.sqlMapClient.update("updateFavoriteStatus", page);
 	}
 
 }
