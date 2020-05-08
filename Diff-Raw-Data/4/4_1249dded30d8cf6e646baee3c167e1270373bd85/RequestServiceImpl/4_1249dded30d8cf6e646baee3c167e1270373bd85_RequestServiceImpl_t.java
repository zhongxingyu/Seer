 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.server.services;
 
 import net.sf.gilead.core.PersistentBeanManager;
 import net.sf.gilead.gwt.PersistentRemoteService;
 
 import org.accesointeligente.client.services.RequestService;
 import org.accesointeligente.model.*;
 import org.accesointeligente.server.*;
 import org.accesointeligente.server.solr.SolrStatus;
 import org.accesointeligente.shared.*;
 
 import org.hibernate.Criteria;
 import org.hibernate.FetchMode;
 import org.hibernate.Session;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class RequestServiceImpl extends PersistentRemoteService implements RequestService {
 	private PersistentBeanManager persistentBeanManager;
 
 	public RequestServiceImpl() {
 		persistentBeanManager = HibernateUtil.getPersistentBeanManager();
 		setBeanManager(persistentBeanManager);
 	}
 
 	@Override
 	public Request saveRequest(Request request) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			if (request.getCreationDate() == null) {
 				request.setCreationDate(new Date());
 			}
 
 			hibernate.saveOrUpdate(request);
 			hibernate.getTransaction().commit();
 			return request;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public void deleteRequest(Request request) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			hibernate.delete(request);
 			hibernate.getTransaction().commit();
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public List<RequestCategory> getCategories() throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(RequestCategory.class);
 			criteria.addOrder(Order.asc("id"));
 			List<RequestCategory> categories = (List<RequestCategory>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return categories;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Request getRequest(Integer requestId) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFetchMode("user", FetchMode.JOIN);
 			criteria.setFetchMode("institution", FetchMode.JOIN);
 			criteria.setFetchMode("categories", FetchMode.JOIN);
 			criteria.setFetchMode("favorites", FetchMode.JOIN);
 			criteria.setFetchMode("responses", FetchMode.JOIN);
 			criteria.add(Restrictions.eq("id", requestId));
 			Request request = (Request) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 			return (Request) persistentBeanManager.clone(request);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Request getRequest(String remoteIdentifier) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.add(Restrictions.eq("remoteIdentifier", remoteIdentifier));
 			Request request = (Request) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 			return (Request) persistentBeanManager.clone(request);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Page<Request> getUserRequestList(Integer offset, Integer limit) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			User user = SessionUtil.getUser(getThreadLocalRequest().getSession());
 
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.add(Restrictions.eq("user", user));
 			criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 			criteria.setProjection(Projections.rowCount());
 			Long totalResults = (Long) criteria.uniqueResult();
 			List<Request> requests = new ArrayList<Request>(0);
 
 			if (totalResults > 0) {
 				criteria = hibernate.createCriteria(Request.class);
 				criteria.add(Restrictions.eq("user", user));
 				criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 				criteria.addOrder(Order.asc("confirmationDate"));
 				criteria.addOrder(Order.asc("institution"));
 				criteria.setProjection(Projections.distinct(Projections.id()));
 				criteria.setFirstResult(offset);
 				criteria.setMaxResults(limit);
 				List<Long> ids = criteria.list();
 
 				if (!ids.isEmpty()) {
 					criteria = hibernate.createCriteria(Request.class);
 					criteria.add(Restrictions.in("id", ids));
 					criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 					criteria.addOrder(Order.asc("confirmationDate"));
 					criteria.addOrder(Order.asc("institution"));
 					criteria.setFetchMode("institution", FetchMode.JOIN);
 					criteria.setFetchMode("favorites", FetchMode.JOIN);
 					criteria.setFetchMode("responses", FetchMode.JOIN);
 					requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 				}
 			}
 
 			hibernate.getTransaction().commit();
 			Page<Request> page = new Page<Request>();
 			page.setStart(offset.longValue());
 			page.setDataCount(totalResults);
 			page.setData(requests);
 			return page;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Page<Request> getUserRequestList(Integer offset, Integer limit, RequestSearchParams params) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			params = (RequestSearchParams) persistentBeanManager.merge(params);
 			User user = SessionUtil.getUser(getThreadLocalRequest().getSession());
 
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.add(Restrictions.eq("user", user));
 			criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 
 			if (params != null) {
 				SearchParamParseUtil.criteriaAddSearchParams(criteria, params);
 			}
 
 			criteria.setProjection(Projections.rowCount());
 			Long totalResults = (Long) criteria.uniqueResult();
 			List<Request> requests = new ArrayList<Request>(0);
 
 			if (totalResults > 0) {
 				criteria = hibernate.createCriteria(Request.class);
 				criteria.add(Restrictions.eq("user", user));
 				criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 
 				if (params != null) {
 					SearchParamParseUtil.criteriaAddSearchParams(criteria, params);
 				}
 
 				criteria.addOrder(Order.asc("confirmationDate"));
 				criteria.addOrder(Order.asc("institution"));
 				criteria.setProjection(Projections.distinct(Projections.id()));
 				criteria.setFirstResult(offset);
 				criteria.setMaxResults(limit);
 				List<Long> ids = criteria.list();
 
 				if (!ids.isEmpty()) {
 					criteria = hibernate.createCriteria(Request.class);
 					criteria.add(Restrictions.in("id", ids));
 					criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 					criteria.addOrder(Order.asc("confirmationDate"));
 					criteria.addOrder(Order.asc("institution"));
 					criteria.setFetchMode("institution", FetchMode.JOIN);
 					criteria.setFetchMode("favorites", FetchMode.JOIN);
 					criteria.setFetchMode("responses", FetchMode.JOIN);
 					requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 				}
 			}
 
 			hibernate.getTransaction().commit();
 			Page<Request> page = new Page<Request>();
 			page.setStart(offset.longValue());
 			page.setDataCount(totalResults);
 			page.setData(requests);
 			return page;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Page<Request> getUserFavoriteRequestList(Integer offset, Integer limit) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			User user = SessionUtil.getUser(getThreadLocalRequest().getSession());
 
 			Criteria criteria = hibernate.createCriteria(UserFavoriteRequest.class);
 			criteria.add(Restrictions.eq("user", user));
 			criteria.setProjection(Projections.countDistinct("request"));
 			Long totalResults = (Long) criteria.uniqueResult();
 			List<Request> requests = new ArrayList<Request>(0);
 
 			if (totalResults > 0) {
 				criteria = hibernate.createCriteria(UserFavoriteRequest.class);
 				criteria.add(Restrictions.eq("user", user));
 				criteria.setProjection(Projections.distinct(Projections.property("request.id")));
 				criteria.setFirstResult(offset);
 				criteria.setMaxResults(limit);
 				List<Long> ids = criteria.list();
 
 				if (!ids.isEmpty()) {
 					criteria = hibernate.createCriteria(Request.class);
 					criteria.add(Restrictions.in("id", ids));
 					criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 					criteria.setFetchMode("institution", FetchMode.JOIN);
 					criteria.setFetchMode("favorites", FetchMode.JOIN);
 					criteria.setFetchMode("responses", FetchMode.JOIN);
 					requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 				}
 			}
 
 			hibernate.getTransaction().commit();
 
 			Page<Request> page = new Page<Request>();
 			page.setStart(offset.longValue());
 			page.setDataCount(totalResults);
 			page.setData(requests);
 			return page;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Page<Request> getUserFavoriteRequestList(Integer offset, Integer limit, RequestSearchParams params) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			User user = SessionUtil.getUser(getThreadLocalRequest().getSession());
 
 			Criteria criteria = hibernate.createCriteria(UserFavoriteRequest.class);
 			criteria.add(Restrictions.eq("user", user));
 
 			if (params != null) {
 				SearchParamParseUtil.criteriaAddSearchParams(criteria, params);
 			}
 
 			criteria.setProjection(Projections.countDistinct("request"));
 			Long totalResults = (Long) criteria.uniqueResult();
 			List<Request> requests = new ArrayList<Request>(0);
 
 			if (totalResults > 0) {
 				criteria = hibernate.createCriteria(UserFavoriteRequest.class);
 				criteria.add(Restrictions.eq("user", user));
 
 				if (params != null) {
 					SearchParamParseUtil.criteriaAddSearchParams(criteria, params);
 				}
 
 				criteria.setProjection(Projections.distinct(Projections.property("request.id")));
 				criteria.setFirstResult(offset);
 				criteria.setMaxResults(limit);
 				List<Long> ids = criteria.list();
 
 				if (!ids.isEmpty()) {
 					criteria = hibernate.createCriteria(Request.class);
 					criteria.add(Restrictions.in("id", ids));
 					criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 					criteria.setFetchMode("institution", FetchMode.JOIN);
 					criteria.setFetchMode("favorites", FetchMode.JOIN);
 					criteria.setFetchMode("responses", FetchMode.JOIN);
 					requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 				}
 			}
 
 			hibernate.getTransaction().commit();
 
 			Page<Request> page = new Page<Request>();
 			page.setStart(offset.longValue());
 			page.setDataCount(totalResults);
 			page.setData(requests);
 			return page;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Page<Request> getUserDraftList(Integer offset, Integer limit) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			User user = SessionUtil.getUser(getThreadLocalRequest().getSession());
 
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.add(Restrictions.eq("user", user));
 			criteria.add(Restrictions.eq("status", RequestStatus.DRAFT));
 			criteria.setProjection(Projections.rowCount());
 			Long totalResults = (Long) criteria.uniqueResult();
 			List<Request> requests = new ArrayList<Request>(0);
 
 			if (totalResults > 0) {
 				criteria = hibernate.createCriteria(Request.class);
 				criteria.add(Restrictions.eq("user", user));
 				criteria.add(Restrictions.eq("status", RequestStatus.DRAFT));
 				criteria.addOrder(Order.asc("confirmationDate"));
 				criteria.addOrder(Order.asc("institution"));
 				criteria.setProjection(Projections.distinct(Projections.id()));
 				criteria.setFirstResult(offset);
 				criteria.setMaxResults(limit);
 				List<Long> ids = criteria.list();
 
 				if (!ids.isEmpty()) {
 					criteria = hibernate.createCriteria(Request.class);
 					criteria.add(Restrictions.in("id", ids));
 					criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 					criteria.addOrder(Order.asc("confirmationDate"));
 					criteria.addOrder(Order.asc("institution"));
 					criteria.setFetchMode("institution", FetchMode.JOIN);
 					criteria.setFetchMode("favorites", FetchMode.JOIN);
 					criteria.setFetchMode("responses", FetchMode.JOIN);
 					requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 				}
 			}
 
 			hibernate.getTransaction().commit();
 			Page<Request> page = new Page<Request>();
 			page.setStart(offset.longValue());
 			page.setDataCount(totalResults);
 			page.setData(requests);
 			return page;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Page<Request> getRequestList(Integer offset, Integer limit) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 			criteria.setProjection(Projections.rowCount());
 			Long totalResults = (Long) criteria.uniqueResult();
 			List<Request> requests = new ArrayList<Request>(0);
 
 			if (totalResults > 0) {
 				criteria = hibernate.createCriteria(Request.class);
 				criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 				criteria.setProjection(Projections.distinct(Projections.id()));
 				criteria.setFirstResult(offset);
 				criteria.setMaxResults(limit);
 				List<Long> ids = criteria.list();
 
 				if (!ids.isEmpty()) {
 					criteria = hibernate.createCriteria(Request.class);
 					criteria.add(Restrictions.in("id", ids));
 					criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 					criteria.addOrder(Order.asc("confirmationDate"));
 					criteria.addOrder(Order.asc("institution"));
 					criteria.setFetchMode("institution", FetchMode.JOIN);
 					criteria.setFetchMode("favorites", FetchMode.JOIN);
 					criteria.setFetchMode("responses", FetchMode.JOIN);
 					requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 				}
 			}
 
 			hibernate.getTransaction().commit();
 			Page<Request> page = new Page<Request>();
 			page.setStart(offset.longValue());
 			page.setDataCount(totalResults);
 			page.setData(requests);
 			return page;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Page<Request> getRequestList(Integer offset, Integer limit, RequestSearchParams params) throws ServiceException {
 
 		if (ApplicationProperties.getProperty("solr.server.status") != null && ApplicationProperties.getProperty("solr.server.status").equals(SolrStatus.AVAILABLE.toString())) {
 			return getSolrRequestList(offset, limit, params);
 		}
 
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 
 			if (params != null) {
 				SearchParamParseUtil.criteriaAddSearchParams(criteria, params);
 			}
 
 			criteria.setProjection(Projections.rowCount());
 			Long totalResults = (Long) criteria.uniqueResult();
 			List<Request> requests = new ArrayList<Request>(0);
 
 			if (totalResults > 0) {
 				criteria = hibernate.createCriteria(Request.class);
 				criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 
 				if (params != null) {
 					SearchParamParseUtil.criteriaAddSearchParams(criteria, params);
 				}
 
 				criteria.addOrder(Order.asc("confirmationDate"));
 				criteria.addOrder(Order.asc("institution"));
 				criteria.setProjection(Projections.distinct(Projections.id()));
 				criteria.setFirstResult(offset);
 				criteria.setMaxResults(limit);
 				List<Long> ids = criteria.list();
 
 				if (!ids.isEmpty()) {
 					criteria = hibernate.createCriteria(Request.class);
 					criteria.add(Restrictions.in("id", ids));
 					criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 					criteria.addOrder(Order.asc("confirmationDate"));
 					criteria.addOrder(Order.asc("institution"));
 					criteria.setFetchMode("institution", FetchMode.JOIN);
 					criteria.setFetchMode("favorites", FetchMode.JOIN);
 					criteria.setFetchMode("responses", FetchMode.JOIN);
 					requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 				}
 			}
 
 			hibernate.getTransaction().commit();
 			Page<Request> page = new Page<Request>();
 			page.setStart(offset.longValue());
 			page.setDataCount(totalResults);
 			page.setData(requests);
 			return page;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Page<Request> getSolrRequestList(Integer offset, Integer limit, RequestSearchParams params) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 
 			Long totalResults = 0L;
 			try {
 				totalResults = SearchParamParseUtil.solrCriteriaAddSearchParams(criteria, params, offset, limit);
 			} catch (Exception ex) {
 				hibernate.getTransaction().rollback();
 				throw new ServiceException();
 			}
 
 			List<Request> requests = new ArrayList<Request>(0);
 
 			if (totalResults > 0) {
 				criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 				criteria.addOrder(Order.asc("confirmationDate"));
 				criteria.addOrder(Order.asc("institution"));
 				criteria.setFetchMode("institution", FetchMode.JOIN);
 				criteria.setFetchMode("favorites", FetchMode.JOIN);
 				criteria.setFetchMode("responses", FetchMode.JOIN);
 				requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 			}
 
 			hibernate.getTransaction().commit();
 			Page<Request> page = new Page<Request>();
 			page.setStart(offset.longValue());
 			page.setDataCount(totalResults);
 			page.setData(requests);
 			return page;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public List<Attachment> getResponseAttachmentList(Response response) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Attachment.class);
 			criteria.add(Restrictions.eq("response", response));
 			criteria.addOrder(Order.asc("name"));
 			List<Attachment> attachments = (List<Attachment>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return attachments;
 		} catch (Exception ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public UserFavoriteRequest getFavoriteRequest(Request request, User user) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(UserFavoriteRequest.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFetchMode("user", FetchMode.JOIN);
 			criteria.setFetchMode("request", FetchMode.JOIN);
 			criteria.add(Restrictions.eq("user", user));
 			criteria.add(Restrictions.eq("request", request));
 			UserFavoriteRequest favorite = (UserFavoriteRequest) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 			if (favorite != null) {
 				return (UserFavoriteRequest) persistentBeanManager.clone(favorite);
 			} else {
 				return null;
 			}
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public UserFavoriteRequest createFavoriteRequest(Request request, User user) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			UserFavoriteRequest favorite = new UserFavoriteRequest();
 			favorite.setRequest(request);
 			favorite.setUser(user);
 			hibernate.save(favorite);
 			hibernate.getTransaction().commit();
 			return favorite;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public void deleteFavoriteRequest(UserFavoriteRequest favorite) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			favorite = (UserFavoriteRequest) persistentBeanManager.merge(favorite);
 			hibernate.delete(favorite);
 			hibernate.getTransaction().commit();
 			return;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public List<RequestComment> getRequestComments(Request request) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(RequestComment.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFetchMode("user", FetchMode.JOIN);
 			criteria.setFetchMode("request", FetchMode.JOIN);
 			criteria.add(Restrictions.eq("request", request));
 			criteria.addOrder(Order.desc("date"));
 			List<RequestComment> comments = (List<RequestComment>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return comments;
 		} catch (Exception ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public RequestComment createRequestComment(RequestComment comment) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			comment = (RequestComment) persistentBeanManager.merge(comment);
 			hibernate.save(comment);
 			hibernate.getTransaction().commit();
 			return comment;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public void deleteRequestComment(RequestComment comment) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			comment = (RequestComment) persistentBeanManager.merge(comment);
 			hibernate.delete(comment);
 			hibernate.getTransaction().commit();
 			return;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Response saveResponse(Response response) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			response = (Response) persistentBeanManager.merge(response);
 			hibernate.saveOrUpdate(response);
 			hibernate.getTransaction().commit();
 			return response;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public void deleteResponse(Response response) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			hibernate.delete(response);
 			hibernate.getTransaction().commit();
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Attachment saveAttachment(Attachment attachment) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			attachment = (Attachment) persistentBeanManager.merge(attachment);
 			hibernate.saveOrUpdate(attachment);
 			hibernate.getTransaction().commit();
 			return attachment;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 
 	@Override
 	public void deleteAttachment(Attachment attachment) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			hibernate.delete(attachment);
 			hibernate.getTransaction().commit();
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public UserRequestQualification saveUserRequestQualification(UserRequestQualification qualification) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			qualification = (UserRequestQualification) persistentBeanManager.merge(qualification);
 			hibernate.saveOrUpdate(qualification);
 			hibernate.getTransaction().commit();
 			qualification.setRequest(updateRequestQualification(qualification.getRequest()));
 			return (UserRequestQualification) persistentBeanManager.clone(qualification);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Request updateRequestQualification(Request request) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			request = (Request) persistentBeanManager.merge(request);
 			Double averageQualification = (Double) hibernate.createQuery("select avg(qualification) from UserRequestQualification where Request = :request").setParameter("request", request).uniqueResult();
 			request.setQualification(averageQualification);
 			hibernate.update(request);
 			hibernate.getTransaction().commit();
 			return (Request) persistentBeanManager.clone(request);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public UserResponse saveUserResponse(UserResponse userResponse) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			userResponse = (UserResponse) persistentBeanManager.merge(userResponse);
 			hibernate.saveOrUpdate(userResponse);
 
 			Response response = userResponse.getResponse();
 			Request request = response.getRequest();
 			User user = request.getUser();
 			hibernate.getTransaction().commit();
 
 			Emailer emailer = new Emailer();
 			emailer.setRecipient(response.getSender());
 			emailer.setSubject(String.format(ApplicationProperties.getProperty("email.user.response.subject"), request.getRemoteIdentifier()));
 			emailer.setBody(String.format(ApplicationProperties.getProperty("email.user.response.body"),  userResponse.getInformation()) + ApplicationProperties.getProperty("email.signature"));
 			emailer.connectAndSend();
 
 			emailer = new Emailer();
 			emailer.setRecipient(user.getEmail());
 			emailer.setSubject(String.format(ApplicationProperties.getProperty("email.user.response.subject"), request.getRemoteIdentifier()));
 			emailer.setBody(String.format(ApplicationProperties.getProperty("email.user.response.body"),  userResponse.getInformation()) + ApplicationProperties.getProperty("email.signature"));
 			emailer.connectAndSend();
 
 			return (UserResponse) persistentBeanManager.clone(userResponse);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public UserResponse getUserResponse(Response response) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 		try {
 			response = (Response) persistentBeanManager.merge(response);
 			Criteria criteria = hibernate.createCriteria(UserResponse.class);
 			criteria.add(Restrictions.eq("response", response));
 			UserResponse userResponse = (UserResponse) criteria.uniqueResult();
 			hibernate.getTransaction().commit();
 
 			return (UserResponse) persistentBeanManager.clone(userResponse);
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public List<Request> getBestVotedRequests() throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 			criteria.addOrder(Order.desc("qualification"));
 			criteria.setFirstResult(0);
 			criteria.setMaxResults(5);
 			List<Request> requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return requests;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public List<Request> getLastResponseRequests() throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.add(Restrictions.ne("status", RequestStatus.DRAFT));
 			criteria.addOrder(Order.desc("responseDate"));
 			criteria.setFirstResult(0);
 			criteria.setMaxResults(3);
 			List<Request> requests = (List<Request>) persistentBeanManager.clone(criteria.list());
 			hibernate.getTransaction().commit();
 			return requests;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Request setRequestUserSatisfaction(Request request) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			request = (Request) persistentBeanManager.merge(request);
 
 			Criteria criteria = hibernate.createCriteria(Response.class);
 			criteria.add(Restrictions.eq("request", request));
 			List<Response> relatedResponses = (List<Response>)criteria.list();
 
 			Boolean satisfiedRequest = false;
 			Integer unsatisfiedResponses = 0;
 
 			for (Response relatedResponse : relatedResponses) {
 				if (relatedResponse.getUserSatisfaction() != null && relatedResponse.getUserSatisfaction().equals(UserSatisfaction.SATISFIED)) {
 					satisfiedRequest = true;
 					break;
 				}
 				unsatisfiedResponses++;
 			}
 
 			if (satisfiedRequest) {
 				request.setUserSatisfaction(UserSatisfaction.SATISFIED);
 				hibernate.saveOrUpdate(request);
 			} else if (unsatisfiedResponses.equals(relatedResponses.size())) {
 				request.setUserSatisfaction(UserSatisfaction.UNSATISFIED);
 				hibernate.saveOrUpdate(request);
 			}
 
 			request = (Request) persistentBeanManager.clone(request);
 			hibernate.getTransaction().commit();
 			return request;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Response getResponse(Integer responseId, String responseKey) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Response.class);
			criteria.setFetchMode("request", FetchMode.JOIN);
 			criteria.add(Restrictions.eq("id", responseId));
 			criteria.add(Restrictions.eq("responseKey", responseKey));
 			Response response = (Response) persistentBeanManager.clone(criteria.uniqueResult());
 			hibernate.getTransaction().commit();
 			return response;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 
 	@Override
 	public Request getRequestByResponseId(Integer responseId) throws ServiceException {
 		Session hibernate = HibernateUtil.getSession();
 		hibernate.beginTransaction();
 
 		try {
 			Criteria criteria = hibernate.createCriteria(Response.class);
 			criteria.add(Restrictions.eq("id", responseId));
 			Response response = (Response) criteria.uniqueResult();
 
 			criteria = hibernate.createCriteria(Request.class);
			criteria.setFetchMode("user", FetchMode.JOIN);
			criteria.setFetchMode("institution", FetchMode.JOIN);
 			criteria.add(Restrictions.eq("id", response.getRequest().getId()));
 			Request request = (Request) persistentBeanManager.clone(criteria.uniqueResult());
 			hibernate.getTransaction().commit();
 			return request;
 		} catch (Throwable ex) {
 			hibernate.getTransaction().rollback();
 			throw new ServiceException();
 		}
 	}
 }
