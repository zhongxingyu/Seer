 /**
  * 
  */
 package q.dao;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import q.dao.page.PeopleRelationPage;
 import q.domain.Event;
 import q.domain.Favorite;
 import q.domain.Group;
 import q.domain.Message;
 import q.domain.People;
 import q.domain.PeopleJoinEvent;
 import q.domain.PeopleRelation;
 import q.domain.Weibo;
 import q.domain.WeiboModel;
 import q.domain.WeiboReply;
 import q.util.CollectionKit;
 
 /**
  * @author seanlinwang
  * @email xalinx at gmail dot com
  * @date Feb 22, 2011
  * 
  */
 public class DaoHelper {
 
	public static void injectWeiboModelsWithFavorite(FavoriteDao favoriteDao, List<? extends WeiboModel> replies, long peopleId) throws SQLException {
		if (CollectionKit.isNotEmpty(replies)) {
			List<Long> replyIds = new ArrayList<Long>(replies.size());
			for (WeiboModel reply : replies) {
				replyIds.add(reply.getId());
 			}
			List<Long> favIds = favoriteDao.getFavReplyIds(replyIds, peopleId);
 			if (CollectionKit.isNotEmpty(favIds)) {
 				Set<Long> favSet = new HashSet<Long>(favIds);
				for (WeiboModel reply : replies) {
					if (favSet.contains(reply.getId())) {
						reply.setFav(true);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param groupDao
 	 * @param weibos
 	 * @return
 	 * @throws SQLException
 	 */
 	public static void injectWeiboModelsWithFrom(GroupDao groupDao, List<? extends WeiboModel> weibos) throws SQLException {
 		if (CollectionKit.isNotEmpty(weibos)) {
 			Set<Long> groupIds = null;
 			for (WeiboModel weibo : weibos) {
 				if (weibo.isFromGroup()) {
 					if (groupIds == null) {
 						groupIds = new HashSet<Long>(weibos.size());// lazy init
 					}
 					groupIds.add(weibo.getFromId());
 				}
 			}
 			if (CollectionKit.isNotEmpty(groupIds)) {
 				Map<Long, String> groupIdNameMap = groupDao.getGroupIdNameMapByIds(groupIds);
 				for (WeiboModel weibo : weibos) {
 					if (weibo.isFromGroup()) {
 						weibo.setFromPostfix(groupIdNameMap.get(weibo.getFromId()));
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param groupDao
 	 * @param events
 	 * @throws SQLException
 	 */
 	public static void injectEventsWithGroupName(GroupDao groupDao, List<Event> events) throws SQLException {
 		if (CollectionKit.isNotEmpty(events)) {
 			Set<Long> groupIds = new HashSet<Long>(events.size());
 			for (Event event : events) {
 				groupIds.add(event.getGroupId());
 			}
 			if (CollectionKit.isNotEmpty(groupIds)) {
 				Map<Long, String> groupIdNameMap = groupDao.getGroupIdNameMapByIds(groupIds);
 				for (Event event : events) {
 					event.setGroupName(groupIdNameMap.get(event.getGroupId()));
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param groupDao
 	 * @param event
 	 * @throws SQLException
 	 */
 	public static void injectEventWithGroupName(GroupDao groupDao, Event event) throws SQLException {
 		if (event != null) {
 			Group group = groupDao.getGroupById(event.getGroupId());
 			event.setGroupName(group.getName());
 		}
 
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param event
 	 * @throws SQLException
 	 */
 	public static void injectEventWithRealName(PeopleDao peopleDao, Event event) throws SQLException {
 		if (event != null) {
 			People people = peopleDao.getPeopleById(event.getCreatorId());
 			event.setCreatorRealName(people.getRealName());
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param hotWeibos
 	 * @throws SQLException
 	 */
 	public static void injectWeiboModelsWithSenderRealName(PeopleDao peopleDao, List<? extends WeiboModel> weiboModels) throws SQLException {
 		if (CollectionKit.isNotEmpty(weiboModels)) {
 			Set<Long> senderIds = new HashSet<Long>(weiboModels.size());
 			for (WeiboModel weibo : weiboModels) {
 				senderIds.add(weibo.getSenderId());
 				if (weibo.getQuoteSenderId() > 0) {
 					senderIds.add(weibo.getQuoteSenderId());
 				}
 			}
 			if (CollectionKit.isNotEmpty(senderIds)) {
 				Map<Long, String> peopleIdRealNameMap = peopleDao.getIdRealNameMapByIds(senderIds);
 				for (WeiboModel weibo : weiboModels) {
 					weibo.setSenderRealName(peopleIdRealNameMap.get(weibo.getSenderId()));
 					if (weibo.getQuoteSenderId() > 0) {
 						weibo.setQuoteSenderRealName(peopleIdRealNameMap.get(weibo.getQuoteSenderId()));
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param weiboDao
 	 * @param favorites
 	 */
 	public static void injectFavoritesWithSource(WeiboDao weiboDao, List<Favorite> favorites) throws SQLException {
 		if (CollectionKit.isNotEmpty(favorites)) {
 			List<Long> sourceWeiboIds = null; // lazy init
 			List<Long> sourceReplyIds = null; // lazy inti
 			for (Favorite fav : favorites) { // extract weibo and reply sources
 				if (fav.isFromWeibo()) {
 					if (sourceWeiboIds == null) {
 						sourceWeiboIds = new ArrayList<Long>(favorites.size());
 					}
 					sourceWeiboIds.add(fav.getFromId());
 				}
 				if (fav.isFromReply()) {
 					if (sourceReplyIds == null) {
 						sourceReplyIds = new ArrayList<Long>(favorites.size());
 					}
 					sourceReplyIds.add(fav.getFromId());
 				}
 			}
 			Map<Long, Weibo> weiboMap = null;
 			if (CollectionKit.isNotEmpty(sourceWeiboIds)) {
 				weiboMap = new HashMap<Long, Weibo>(sourceWeiboIds.size());
 				List<Weibo> weibos = weiboDao.getWeibosByIds(sourceWeiboIds, false);
 				for (Weibo weibo : weibos) {
 					if (weibo.isCommon()) {
 						weiboMap.put(weibo.getId(), weibo);
 					}
 				}
 			}
 			Map<Long, WeiboReply> replyMap = null;
 			if (CollectionKit.isNotEmpty(sourceReplyIds)) {
 				replyMap = new HashMap<Long, WeiboReply>(sourceReplyIds.size());
 				List<WeiboReply> replies = weiboDao.getWeiboRepliesByIds(sourceReplyIds, false);
 				for (WeiboReply reply : replies) {
 					if (reply.isCommon()) {
 						replyMap.put(reply.getId(), reply);
 					}
 				}
 			}
 			for (Favorite fav : favorites) { // inject weibo and reply sources
 				if (weiboMap != null && fav.isFromWeibo()) {
 					fav.setSource(weiboMap.get(fav.getFromId()));
 					fav.getSource().setFav(true);
 				}
 				if (replyMap != null && fav.isFromReply()) {
 					fav.setSource(replyMap.get(fav.getFromId()));
 					fav.getSource().setFav(true);
 				}
 			}
 		}
 
 	}
 
 	public static void injectMessagesWithSenderReceiverRealName(PeopleDao peopleDao, List<Message> messages) throws SQLException {
 		if (CollectionKit.isNotEmpty(messages)) {
 			HashSet<Long> peopleIds = new HashSet<Long>(messages.size() + 1); // people number is less than messages count + 1
 			for (Message message : messages) {
 				peopleIds.add(message.getSenderId());
 				peopleIds.add(message.getReceiverId());
 			}
 			Map<Long, String> map = peopleDao.getIdRealNameMapByIds(peopleIds);
 			for (Message message : messages) {
 				message.setSenderRealName(map.get(message.getSenderId()));
 				message.setReceiverRealName(map.get(message.getReceiverId()));
 			}
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param events
 	 * @throws SQLException
 	 */
 	public static void injectEventsWithRealName(PeopleDao peopleDao, List<Event> events) throws SQLException {
 		if (CollectionKit.isNotEmpty(events)) {
 			HashSet<Long> peopleIds = new HashSet<Long>(events.size());
 			for (Event event : events) {
 				peopleIds.add(event.getCreatorId());
 			}
 			Map<Long, String> map = peopleDao.getIdRealNameMapByIds(peopleIds);
 			for (Event event : events) {
 				event.setCreatorRealName(map.get(event.getCreatorId()));
 			}
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param weiboDao
 	 * @param groupDao
 	 * @param weiboId
 	 * @return
 	 * @throws SQLException
 	 */
 	public static void injectWeiboWithSenderRealNameAndFrom(PeopleDao peopleDao, GroupDao groupDao, Weibo weibo) throws SQLException {
 		People people = peopleDao.getPeopleById(weibo.getSenderId());
 		weibo.setSenderRealName(people.getRealName());
 		if (weibo.isFromGroup()) {
 			Group group = groupDao.getGroupById(weibo.getFromId());
 			weibo.setFromPostfix(group.getName());
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param weiboDao
 	 * @param groupDao
 	 * @param replyId
 	 * @return
 	 * @throws SQLException
 	 */
 	public static void injectWeiboReplyWithSenderRealNameAndFrom(PeopleDao peopleDao, GroupDao groupDao, WeiboReply reply) throws SQLException {
 		People people = peopleDao.getPeopleById(reply.getSenderId());
 		reply.setSenderRealName(people.getRealName());
 		if (reply.isFromGroup()) {
 			Group group = groupDao.getGroupById(reply.getFromId());
 			reply.setFromPostfix(group.getName());
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param peoples
 	 * @param fromPeopleId
 	 * @throws SQLException
 	 */
 	public static void injectPeoplesWithRelation(PeopleDao peopleDao, List<People> peoples, Long fromPeopleId) throws SQLException {
 		if (CollectionKit.isEmpty(peoples)) {
 			return;
 		}
 		Set<Long> peopleIdSet = new HashSet<Long>(peoples.size());
 		for (People people : peoples) {
 			if (people.getId() != fromPeopleId) {
 				peopleIdSet.add(people.getId());
 			}
 		}
 		if (CollectionKit.isEmpty(peopleIdSet)) {
 			return;
 		}
 		PeopleRelationPage page = new PeopleRelationPage();
 		page.setToPeopleIds(new ArrayList<Long>(peopleIdSet));
 		page.setFromPeopleId(fromPeopleId);
 		List<PeopleRelation> relations = peopleDao.getPeopleRelationsByPage(page);
 		if (CollectionKit.isNotEmpty(relations)) {
 			Map<Long, PeopleRelation> relationMap = new HashMap<Long, PeopleRelation>();
 			for (PeopleRelation relation : relations) {
 				relationMap.put(relation.getToPeopleId(), relation);
 			}
 			for (People people : peoples) {
 				PeopleRelation relation = relationMap.get(people.getId());
 				if (relation == null) {
 					continue;
 				}
 				if (relation.isFollowing()) {
 					people.setFollowing();
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param relations
 	 * @throws SQLException
 	 */
 	public static void injectPeopleRelationsWithFromRealName(PeopleDao peopleDao, List<PeopleRelation> relations) throws SQLException {
 		if (CollectionKit.isNotEmpty(relations)) {
 			HashSet<Long> peopleIds = new HashSet<Long>(relations.size());
 			for (PeopleRelation relation : relations) {
 				peopleIds.add(relation.getFromPeopleId());
 			}
 			Map<Long, String> map = peopleDao.getIdRealNameMapByIds(peopleIds);
 			for (PeopleRelation relation : relations) {
 				relation.setFromPeopleRealName(map.get(relation.getFromPeopleId()));
 			}
 		}
 	}
 
 	public static void injectPeopleRelationsWithToRealName(PeopleDao peopleDao, List<PeopleRelation> relations) throws SQLException {
 		if (CollectionKit.isNotEmpty(relations)) {
 			HashSet<Long> peopleIds = new HashSet<Long>(relations.size());
 			for (PeopleRelation relation : relations) {
 				peopleIds.add(relation.getToPeopleId());
 			}
 			Map<Long, String> map = peopleDao.getIdRealNameMapByIds(peopleIds);
 			for (PeopleRelation relation : relations) {
 				relation.setToPeopleRealName(map.get(relation.getToPeopleId()));
 			}
 
 		}
 	}
 
 	/**
 	 * @param peopleDao
 	 * @param peopleJoinEvents
 	 * @throws SQLException
 	 */
 	public static void injectPeopleJoinEventsWithRealName(PeopleDao peopleDao, List<PeopleJoinEvent> joins) throws SQLException {
 		if (CollectionKit.isNotEmpty(joins)) {
 			HashSet<Long> peopleIds = new HashSet<Long>(joins.size());
 			for (PeopleJoinEvent join : joins) {
 				peopleIds.add(join.getPeopleId());
 			}
 			Map<Long, String> map = peopleDao.getIdRealNameMapByIds(peopleIds);
 			for (PeopleJoinEvent join : joins) {
 				join.setPeopleRealName(map.get(join.getPeopleId()));
 			}
 		}
 
 	}
 
 }
