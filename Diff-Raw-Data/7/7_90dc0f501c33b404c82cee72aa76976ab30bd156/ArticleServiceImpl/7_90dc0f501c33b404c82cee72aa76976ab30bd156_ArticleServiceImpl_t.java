 package tw.com.dsc.service.impl;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import tw.com.dsc.dao.AccountDao;
 import tw.com.dsc.dao.ArticleDao;
 import tw.com.dsc.dao.ArticleIdDao;
 import tw.com.dsc.dao.ArticleLogDao;
 import tw.com.dsc.dao.ExportPackageDao;
 import tw.com.dsc.dao.GroupDao;
 import tw.com.dsc.dao.NewsDao;
 import tw.com.dsc.dao.StatisticsDataDao;
 import tw.com.dsc.domain.Account;
 import tw.com.dsc.domain.ActionType;
 import tw.com.dsc.domain.AgentType;
 import tw.com.dsc.domain.Article;
 import tw.com.dsc.domain.ArticleId;
 import tw.com.dsc.domain.ArticleLog;
 import tw.com.dsc.domain.ArticleType;
 import tw.com.dsc.domain.ExpireType;
 import tw.com.dsc.domain.ExportPackage;
 import tw.com.dsc.domain.Group;
 import tw.com.dsc.domain.Language;
 import tw.com.dsc.domain.Level;
 import tw.com.dsc.domain.StatisticsData;
 import tw.com.dsc.domain.Status;
 import tw.com.dsc.domain.support.BetweenCondition;
 import tw.com.dsc.domain.support.Condition;
 import tw.com.dsc.domain.support.ContainsCondition;
 import tw.com.dsc.domain.support.InCondition;
 import tw.com.dsc.domain.support.LikeCondition;
 import tw.com.dsc.domain.support.LikeMode;
 import tw.com.dsc.domain.support.NullCondition;
 import tw.com.dsc.domain.support.OperationEnum;
 import tw.com.dsc.domain.support.OrCondition;
 import tw.com.dsc.domain.support.Page;
 import tw.com.dsc.domain.support.SimpleCondition;
 import tw.com.dsc.service.ArticleService;
 import tw.com.dsc.service.MailService;
 import tw.com.dsc.to.ExportInfo;
 import tw.com.dsc.to.PackagedArticle;
 import tw.com.dsc.to.User;
 import tw.com.dsc.util.DateUtils;
 import tw.com.dsc.util.ThreadLocalHolder;
 
 @Service("articleService")
 @Transactional(readOnly=true)
 public class ArticleServiceImpl extends AbstractDomainService<ArticleDao, Article, Long>
 		implements ArticleService {
 	
 	private static final Logger logger = LoggerFactory.getLogger(ArticleServiceImpl.class);
 	
 	@Autowired
 	private ArticleDao dao;
 	
 	@Autowired
 	private ArticleIdDao articleIdDao;
 	
 	@Autowired
 	private ArticleLogDao articleLogDao;
 	
 	@Autowired
 	private StatisticsDataDao statisticsDataDao;
 	
 	@Autowired
 	private ExportPackageDao exportPackageDao;
 	
 	@Autowired
 	private MailService mailService;
 	
 	@Autowired
 	private AccountDao accountDao;
 	
 	@Autowired
 	private GroupDao groupDao;
 	@Autowired
 	private NewsDao newsDao;
 	
 	@Autowired
 	@Qualifier("articleIdSeq")
 	private DataFieldMaxValueIncrementer incrementer;
 	@Override
 	public ArticleDao getDao() {
 		return dao;
 	}
 
 	@Override
 	public Logger getLogger() {
 		return logger;
 	}
 	
 	public ArticleIdDao getArticleIdDao() {
 		return articleIdDao;
 	}
 
 	public void setArticleIdDao(ArticleIdDao articleIdDao) {
 		this.articleIdDao = articleIdDao;
 	}
 	
 	public ArticleLogDao getArticleLogDao() {
 		return articleLogDao;
 	}
 
 	public void setArticleLogDao(ArticleLogDao articleLogDao) {
 		this.articleLogDao = articleLogDao;
 	}
 	
 	public void setDao(ArticleDao dao) {
 		this.dao = dao;
 	}
 	
 	public StatisticsDataDao getStatisticsDataDao() {
 		return statisticsDataDao;
 	}
 
 	public void setStatisticsDataDao(StatisticsDataDao statisticsDataDao) {
 		this.statisticsDataDao = statisticsDataDao;
 	}
 
 	public MailService getMailService() {
 		return mailService;
 	}
 
 	public void setMailService(MailService mailService) {
 		this.mailService = mailService;
 	}
 
 	@Transactional(readOnly=false)
 	public void draftNewArticle(Article article) {
 		User op = ThreadLocalHolder.getOperator();
 		if (!article.getAvailableStatus().contains(Status.Draft)) {
 			logger.warn("Article[{}] can't be drafted for User[{}]", article, op.getAccount());
 			return;
 		}
 		this.newArticle(article, Status.Draft);
 	}
 	
 	@Transactional(readOnly=false)
 	public void finalNewArticle(Article article) {
 		User op = ThreadLocalHolder.getOperator();
 		if (!article.getAvailableStatus().contains(Status.WaitForApproving)) {
 			logger.warn("Article[{}] can't be approved for User[{}]", article, op.getAccount());
 			return;
 		}
 		this.newArticle(article, Status.WaitForApproving);
 	}
 	
 	@Transactional(readOnly=false)
 	public void publishNewL2Article(Article article) {
 		User op = ThreadLocalHolder.getOperator();
 		if (!article.getAvailableStatus().contains(Status.Published)) {
 			logger.warn("Article[{}] can't be published for User[{}]", article, op.getAccount());
 			return;
 		}
 		
 		Calendar cal = Calendar.getInstance();
 		article.setPublishDate(new Date());
 		if (null == article.getExpireType()) {
 			logger.warn("Publish an article without expire type!");
 			article.setExpireType(ExpireType.M1);
 		}
 		cal.add(Calendar.MONTH, article.getExpireType().getMonth());
 		article.setExpireDate(cal.getTime());
 		this.newArticle(article, Status.Published);
 	}
 	
 	@Transactional(readOnly=false)
 	public void publishNewL3Article(Article article) {
 		/*
 		User op = ThreadLocalHolder.getOperator();
 		if (!article.getAvailableStatus().contains(Status.WaitForProofRead)) {
 			logger.warn("Article[{}] can't be proof read for User[{}]", article, op.getAccount());
 			return;
 		}
 		this.newArticle(article, Status.WaitForProofRead);
 		*/
 		User op = ThreadLocalHolder.getOperator();
 		if (!article.getAvailableStatus().contains(Status.Published)) {
 			logger.warn("Article[{}] can't be published for User[{}]", article, op.getAccount());
 			return;
 		}
 		
 		Calendar cal = Calendar.getInstance();
 		article.setPublishDate(new Date());
 		if (null == article.getExpireType()) {
 			logger.warn("Publish an article without expire type!");
 			article.setExpireType(ExpireType.M1);
 		}
 		cal.add(Calendar.MONTH, article.getExpireType().getMonth());
 		article.setExpireDate(cal.getTime());
 		this.newArticle(article, Status.Published);
 	}
 	
 	protected void newArticle(Article article, Status status) {
 		User op = ThreadLocalHolder.getOperator();
 		//1.Check ArticleId
 		ArticleId aid = article.getArticleId();
 		if (null == aid) {
 			logger.error("article.articleId can't be null! Please check your data.");
 			return;
 		}
 		this.articleIdDao.saveOrUpdate(aid);
 		
 		//2.Update Entry User and Date
 		article.setEntryUser(op.getAccount());
 		article.setUserGroup(op.getGroup());
 		article.setEntryDate(new Date());
 		article.setHitCount(0);
 		article.setAgentType(op.getAgentType());
 		article.setStatus(status);
 		
 		//Rate
 		article.setRate1(0);
 		article.setRate2(0);
 		article.setRate3(0);
 		article.setRate4(0);
 		article.setRate5(0);
 		
 		//3.Create Article
 		this.dao.create(article);
 		
 		//4.Log this action
 		if (Status.Draft == status) {
 			this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Create, op.getAccount(), "Create Draft", op.getIp()));
 		} else if (Status.WaitForApproving == status) {
 			this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Create, op.getAccount(), "Create Draft and Save as Final", op.getIp()));
 			this.mailService.approval(article.getOid());
 		} else if (Status.Published == status) {
 			this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Create, op.getAccount(), "Create Draft and Published", op.getIp()));
 		} else if (Status.WaitForProofRead == status) {
 			this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Create, op.getAccount(), "Create Draft and Waiting for Proof Read", op.getIp()));
 		}
 	}
 	
 	@Transactional(readOnly=false)
 	public void finalArticle(Article article) {
 		User op = ThreadLocalHolder.getOperator();
 		if (!article.getAvailableStatus().contains(Status.WaitForApproving)) {
 			logger.warn("Article[{}] can't be finalized for User[{}]", article, op.getAccount());
 			return;
 		}
 		article.setUpdateDate(new Date());
 		article.setStatus(Status.WaitForApproving);
 		
 		this.dao.saveOrUpdate(article);
 		
 		this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Final, op.getAccount(), "Update Status to Waiting for Approving", op.getIp()));
 		this.mailService.approval(article);
 	}
 	
 	@Transactional(readOnly=false)
 	public void approve(Article article) {
 		User op = ThreadLocalHolder.getOperator();
 		if (!article.getAvailableStatus().contains(Status.LeaderApproved)) {
 			logger.warn("Article[{}] can't be approved for User[{}]", article, op.getAccount());
 			return;
 		}
 		article.setUpdateDate(new Date());
 		article.setStatus(Status.LeaderApproved);
 		
 		this.dao.saveOrUpdate(article);
 		
 		this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Approve, op.getAccount(), "Approved", op.getIp()));
 	}
 	
 	@Transactional(readOnly=false)
 	public void reject(Article article, String reason) {
 		User op = ThreadLocalHolder.getOperator();
 		if (!article.getAvailableStatus().contains(Status.LeaderReject)) {
 			logger.warn("Article[{}] can't be rejected for User[{}]", article, op.getAccount());
 			return;
 		}
 		article.setUpdateDate(new Date());
 		article.setStatus(Status.LeaderReject);
 		
 		this.dao.saveOrUpdate(article);
 		
 		this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Reject, op.getAccount(), "Reason:" + reason, op.getIp()));
 		this.mailService.reject(article.getOid());
 	}
 	
 	@Transactional(readOnly=false)
 	public void disable(Article article) {
 		User op = ThreadLocalHolder.getOperator();
 		
 		article.setUpdateDate(new Date());
 		article.setStatus(Status.Deleted);
 		
 		this.dao.saveOrUpdate(article);
 		
 		this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Delete, op.getAccount(), "Deleted", op.getIp()));
 	}
 	
 	@Transactional(readOnly=false)
 	public void readyUpdate(Article article) {
 		User op = ThreadLocalHolder.getOperator();
 		/*
 		if (!article.getAvailableStatus().contains(Status.ReadyToUpdate)) {
 			logger.warn("Article[{}] can't be ReadyToUpdate for User[{}]", article, op.getAccount());
 			return;
 		}
 		*/
 		Status origin = article.getStatus();
 		article.setUpdateDate(new Date());
 		article.setStatus(Status.ReadyToUpdate);
 		
 		this.dao.saveOrUpdate(article);
 		
 		
 		if (origin == Status.WaitForProofRead) {
 			this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Approve, op.getAccount(), "Ready to Updated", op.getIp()));
 			this.mailService.readyUpdate(article.getOid());
 		} else {
 			this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Reject, op.getAccount(), "Reject to Ready to Updated", op.getIp()));
 			this.mailService.rejectToUpdate(article.getOid());
 		}
 	}
 	
 	@Transactional(readOnly=false)
 	public void readyUpdate(String epOid) {
 		User op = ThreadLocalHolder.getOperator();
 		ExportPackage ep = this.exportPackageDao.findByOid(epOid);
 		if (null == ep) {
 			logger.error("Can't find ExportPackage[{}] for readyToUpdate operation.", epOid);
 			return;
 		}
 		Article example = new Article();
 		List<Condition> conds = new ArrayList<Condition>();
 		conds.add(new SimpleCondition("exportPackage.oid", epOid, OperationEnum.EQ));
 
 		List<Article> articles = this.listByExample(example, conds, LikeMode.NONE, null, null);
 
 		//Map<Account, List<Article>> leaderMap = new HashMap<Account, List<Article>>();
 		Map<Account, List<Article>> engineerMap = new HashMap<Account, List<Article>>();
 
 		for (Article article : articles) {
 			List<Article> list = null;
 			// Find Leader Group
 			/*
 			Group lGroup = this.groupDao.findByOid(article.getLeaderGroupId());
 			List<Account> leaders = lGroup.getAccounts();
 			for (Account leader : leaders) {
 				if (leaderMap.containsKey(leader)) {
 					list = leaderMap.get(leader);
 				} else {
 					list = new ArrayList<Article>();
 					leaderMap.put(leader, list);
 				}
 				list.add(article);
 			}
 			*/
 			// Engineer
 			Account engineer = this.accountDao.findByOid(article.getEntryUser());
 			article.setEntryUserName(engineer.getName());
 			list = null;
 			if (engineerMap.containsKey(engineer)) {
 				list = engineerMap.get(engineer);
 			} else {
 				list = new ArrayList<Article>();
 				engineerMap.put(engineer, list);
 			}
 			list.add(article);
 
 			// Update Article to "Ready to Update"
 			article.setUpdateDate(new Date());
 			article.setStatus(Status.ReadyToUpdate);
 
 			this.dao.saveOrUpdate(article);
 			this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Approve, op.getAccount(), "Ready to Updated", op.getIp()));
 			// readyUpdate(article);
 		}
 		
 		for (Entry<Account, List<Article>> entry : engineerMap.entrySet()) {
 			this.mailService.packageUpdate(entry.getKey(), entry.getValue());
 		}
 		ep.setClosed(Boolean.TRUE);
 	}
 	@Transactional(readOnly=false)
 	public void readyPublish(Article article) {
 		User op = ThreadLocalHolder.getOperator();
 		if (!article.getAvailableStatus().contains(Status.ReadyToPublish)) {
 			logger.warn("Article[{}] can't be ReadyToPublish for User[{}]", article, op.getAccount());
 			return;
 		}
 		article.setUpdateDate(new Date());
 		article.setStatus(Status.ReadyToPublish);
 		
 		this.dao.saveOrUpdate(article);
 		
 		this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Final, op.getAccount(), "Ready to Published", op.getIp()));
 		this.mailService.readyPublish(article.getOid());
 	}
 	
 	@Transactional(readOnly=false)
 	public void publish(Article article) {
 		
 		User op = ThreadLocalHolder.getOperator();
 		AgentType at = article.getAgentType();
 		logger.info("User[{}] try to publish a Article[{}]", op.getAccount(), article);
 
 		if (AgentType.L2 == at) {
 			if (Status.WaitForApproving == article.getStatus() || Status.Draft == article.getStatus()) {
 				this.realPublish(article);
 			} else if (Status.WaitForRepublish == article.getStatus()) {
 				this.republish(article);
 			} else {
 				logger.error("Incorrect Status[{}] for Publish", article.getStatus());
 			}
 		} else if (AgentType.L3 == at) {
 			if (Status.ReadyToPublish == article.getStatus() || Status.WaitForApproving == article.getStatus()) {
 				this.realPublish(article);
 			} else if (Status.WaitForRepublish == article.getStatus()) {
 				this.republish(article);
 			} else {
 				logger.error("Incorrect Status[{}] for Publish", article.getStatus());
 			}
 		} else {
 			logger.error("Incorrect AgentType[{}] for Publish!", article.getAgentType());
 		}
 		
 		
 	}
 	
 	/**
 	 * @param article
 	 */
 	protected void realPublish(Article article) {
 		User op = ThreadLocalHolder.getOperator();
 		Calendar cal = Calendar.getInstance();
 		article.setPublishDate(new Date());
 		if (null == article.getExpireType()) {
 			logger.warn("Publish an article without expire type!");
 			article.setExpireType(ExpireType.M1);
 		}
 		cal.add(Calendar.MONTH, article.getExpireType().getMonth());
 		article.setUpdateDate(new Date());
 		article.setExpireDate(cal.getTime());
 		article.setStatus(Status.Published);
 		
 		this.dao.saveOrUpdate(article);
 		
 		this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Publish, op.getAccount(), "Publish", op.getIp()));
 
 	}
 	
 	protected void republish(Article article) {
 		User op = ThreadLocalHolder.getOperator();
 		Calendar cal = Calendar.getInstance();
 		article.setPublishDate(new Date());
 		if (null == article.getExpireType()) {
 			logger.warn("Publish an article without expire type!");
 			article.setExpireType(ExpireType.M1);
 		}
 		cal.add(Calendar.MONTH, article.getExpireType().getMonth());
 		article.setUpdateDate(new Date());
 		article.setExpireDate(cal.getTime());
 		article.setStatus(Status.Published);
 		
 		this.dao.saveOrUpdate(article);
 		
 		this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Republish, op.getAccount(), "Republish", op.getIp()));
 		
 		this.mailService.republish(article.getOid());
 	}
 	
 	@Transactional(readOnly=false)
 	public void rate(Article article, int point) {
 		User op = ThreadLocalHolder.getOperator();
 		logger.debug("User[{}] rate [{}] to Article[{}]", new Object[] {op, point, article.getOid()});
 		ActionType at = ActionType.Rating1;
 		switch(point) {
 		case 1:
 			at = ActionType.Rating1;
 			article.setRate1(article.getRate1()+1);
 			break;
 		case 2:
 			at = ActionType.Rating2;
 			article.setRate2(article.getRate2()+1);
 			break;
 		case 3:
 			at = ActionType.Rating3;
 			article.setRate3(article.getRate3()+1);
 			break;
 		case 4:
 			at = ActionType.Rating4;
 			article.setRate4(article.getRate4()+1);
 			break;
 		case 5:
 			at = ActionType.Rating5;
 			article.setRate5(article.getRate5()+1);
 			break;
 		default:
 			logger.error("Unknow rate number[{}] for Article[{}]", point, article.getOid());
 		}
 		
 		article.setAvgRate(article.countRating());
 		this.dao.saveOrUpdate(article);
 //		this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Rating, op.getAccount(), String.valueOf(point), op.getIp()));
 		this.statisticsDataDao.create(new StatisticsData(article.getOid(), at, op.getAccount(), op.getIp()));
 	}
 	@Transactional(readOnly=false)
 	public void comment(Article article, String comment) {
 		User op = ThreadLocalHolder.getOperator();
 		this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Comment, op.getAccount(), "Comment:"+comment, op.getIp()));
 		this.statisticsDataDao.create(new StatisticsData(article.getOid(), ActionType.Comment, op.getAccount(), op.getIp()));
 	}
 	
 	public Page<Article> searchFaqArticlesPage(Page<Article> page) {
 		Article example = page.getExample();
 		User op = ThreadLocalHolder.getOperator();
 		
 		if (null == example) {
 			logger.warn("Page.example could not be null!");
 			example = new Article();
 			example.setOid(-1l);
 			
 			page.setExample(example);
 		}
 		
 		this.initFaqLastestSearch(page);
 		page.setDescOrders(new String[] {"hitCount"});
 		
 		
 		return this.dao.listByPage(page);
 	}
 	
 	/**
 	 * Active = true
 	 * Status = LeaderPublished
 	 * Level
 	 * 	Public = public
 	 * 	L2/Sub in {public or Partner}
 	 * 	L3 in {public or L3 CSO}
 	 * @param page
 	 * @return
 	 */
 	public Page<Article> searchLatestArticlesPage(Page<Article> page) {
 		Article example = page.getExample();
 		User op = ThreadLocalHolder.getOperator();
 		
 		if (null == example) {
 			logger.warn("Page.example could not be null!");
 			example = new Article();
 			example.setOid(-1l);
 			
 			page.setExample(example);
 		}
 		
 		this.initFaqLastestSearch(page);
 		page.setDescOrders(new String[] {"publishDate"});
 		
 		return this.dao.listByPage(page);
 	}
 	
 	protected void initFaqLastestSearch(Page<Article> page) {
 		Article example = page.getExample();
 		User op = ThreadLocalHolder.getOperator();
 		String keyword = example.getKeywords();
 		
 		List<Condition> conds = page.getConditions();
 		conds.add(new InCondition("level", op.getAvailableLevels()));
 		conds.add(new InCondition("status", new Status[] {Status.Published, Status.WaitForRepublish}));
 		//example.setStatus(Status.Published);
 		/*
 		if (AgentType.L3 != op.getAgentType()) {
 			example.setAgentType(AgentType.L2);
 		}
 		*/
 		if (StringUtils.isNotEmpty(keyword)) {
 			int index = 1;
 			Condition qcond = new ContainsCondition("QUESTION", keyword, index++);
 			Condition acond = new ContainsCondition("ANSWER", keyword, index++);
 			Condition scond = new ContainsCondition("SCENARIO", keyword, index++);
 			Condition stcond = new ContainsCondition("STEP", keyword, index++);
 			Condition vcond = new ContainsCondition("VERIFICATION", keyword, index++);
 			Condition pmcond = new ContainsCondition("PROBLEM", keyword, index++);
 			Condition socond = new ContainsCondition("SOLUTION", keyword, index++);
 			Condition pecond = new ContainsCondition("PROCEDURE_DATA", keyword, index++);
			Condition smcond = new ContainsCondition("SUMMARY", keyword, index++);
 			/*
 			String likeValue = "%"+keyword+"%";
 			Condition qcond = new LikeCondition("question", likeValue, true);
 			Condition acond = new LikeCondition("answer", likeValue, true);
 			Condition scond = new LikeCondition("scenario", likeValue, true);
 			Condition stcond = new LikeCondition("step", likeValue, true);
 			Condition vcond = new LikeCondition("verification", likeValue, true);
 			Condition pmcond = new LikeCondition("problem", likeValue, true);
 			Condition socond = new LikeCondition("solution", likeValue, true);
 			Condition pecond = new LikeCondition("procedure", likeValue, true);
 			Condition smcond = new LikeCondition("summary", likeValue, true);
 			*/
			conds.add(new OrCondition(qcond, acond, scond, stcond, vcond, pmcond, socond, pecond, smcond));
//			conds.add(new OrCondition(qcond, acond, scond, stcond, vcond, pmcond, socond, pecond));
 			example.setKeywords(null);
 		}
 	}
 	
 	public Page<Article> searchUnpublishedPage(Page<Article> page) {
 		User op = ThreadLocalHolder.getOperator();
 		/* Remove for LeaderReject
 		if (!op.isLeader()) {
 			page.setTotalCount(0);
 			return page;
 		}
 		*/
 		Article example = page.getExample();
 		
 		if (null == example) {
 			logger.warn("Page.example could not be null!");
 			example = new Article();
 			example.setOid(-1l);
 			
 			page.setExample(example);
 		}
 		
 		List<Condition> conds = page.getConditions();
 		/*
 		if (null != page.getConditions()) {
 			for (Condition cond : page.getConditions()) {
 				conds.add(cond);
 			}
 		}
 		*/
 		if (op.isLeader()) {
 			String[] groups = op.getAvailableGroups();
 			if (groups.length > 0) {
 				conds.add(new InCondition("userGroup", groups));
 			}
 //			conds.add(new InCondition("status", new Object[] {Status.WaitForApproving, Status.LeaderReject, Status.ReadyToUpdate, Status.ReadyToPublish}));
 		} else {
 			example.setEntryUser(op.getAccount());
 //			conds.add(new InCondition("status", new Object[] {Status.WaitForApproving, Status.LeaderReject}));
 		}
 		if (null == example.getStatus()) {
 			conds.add(new InCondition("status", op.getUnpublishedStatus()));
 		}
 		conds.add(new InCondition("level", op.getAvailableLevels()));
 		conds.add(new SimpleCondition("agentType", op.getAgentType(), OperationEnum.EQ));
 		
 		return this.dao.listByPage(page);
 	}
 	
 	public Page<Article> searchDraftPage(Page<Article> page) {
 		Article example = page.getExample();
 		
 		if (null == example) {
 			logger.warn("Page.example could not be null!");
 			example = new Article();
 			example.setOid(-1l);
 			
 			page.setExample(example);
 		}
 		
 		User op = ThreadLocalHolder.getOperator();
 		example.setEntryUser(op.getAccount());
 		List<Condition> conds = page.getConditions();
 		
 		conds.add(new InCondition("status", new Object[] {Status.Draft}));
 		conds.add(new InCondition("level", op.getAvailableLevels()));
 		conds.add(new SimpleCondition("agentType", op.getAgentType(), OperationEnum.EQ));
 		/*
 		if (!op.isLeader()) {
 			conds.add(new SimpleCondition("entryUser", op.getAccount(), OperationEnum.EQ));
 		} else {
 			String[] groups = op.getAvailableGroups();
 			if (groups.length > 0) {
 				conds.add(new InCondition("userGroup", groups));
 			}
 		}
 		*/
 		return this.dao.listByPage(page);
 	}
 	
 	public Page<Article> searchExpiredPage(Page<Article> page) {
 		User op = ThreadLocalHolder.getOperator();
 		
 		Article example = page.getExample();
 		
 		if (null == example) {
 			logger.warn("Page.example could not be null!");
 			example = new Article();
 			example.setOid(-1l);
 			
 			page.setExample(example);
 		}
 		
 		List<Condition> conds = page.getConditions();
 		// 4/26 Glavine : 1. 當 KB expired 後 , 要出現在此 KB creater 的 Unpublished Article List 中
 		if (!op.isLeader()) {
 			conds.add(new SimpleCondition("entryUser", op.getAccount(), OperationEnum.EQ));
 		} else {
 			String[] groups = op.getAvailableGroups();
 			if (groups.length > 0) {
 				conds.add(new InCondition("userGroup", groups));
 			}
 		}
 		conds.add(new SimpleCondition("status", Status.WaitForRepublish, OperationEnum.EQ));
 		conds.add(new InCondition("level", op.getAvailableLevels()));
 		conds.add(new SimpleCondition("agentType", op.getAgentType(), OperationEnum.EQ));
 		
 		return this.dao.listByPage(page);
 	}
 	
 	public Page<Article> searchL3LatestPublishedPage(Page<Article> page) {
 		User op = ThreadLocalHolder.getOperator();
 		
 		Article example = page.getExample();
 		
 		if (null == example) {
 			logger.warn("Page.example could not be null!");
 			example = new Article();
 			example.setOid(-1l);
 			
 			page.setExample(example);
 		}
 		Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.MONTH, -1);
 		List<Condition> conds = page.getConditions();
 		
 		conds.add(new SimpleCondition("status", Status.Published, OperationEnum.EQ));
 		conds.add(new InCondition("level", new Object[] {Level.Public, Level.Partner} ));
 		conds.add(new SimpleCondition("agentType", AgentType.L3, OperationEnum.EQ));
 		conds.add(new SimpleCondition("publishDate", cal.getTime(), OperationEnum.GE));
 		page.setDescOrders(new String[] {"publishDate"});
 		return this.dao.listByPage(page);
 	}
 	@Transactional(readOnly=false)
 	public void addHitCount(Article article) {
 		User op = ThreadLocalHolder.getOperator();
 		article.setHitCount(article.getHitCount()+1);
 		this.dao.saveOrUpdate(article);
 //		this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.View,  op.getAccount(), "View Detail.", op.getIp()));
 		this.statisticsDataDao.create(new StatisticsData(article.getOid(), ActionType.View,  op.getAccount(), op.getIp()));
 	}
 	
 	@Transactional(readOnly=false)
 	public void expire(Article art) {
 		logger.info("Expire KB [{}] ", art.getOid());
 		User op = ThreadLocalHolder.getOperator();
 				
 			art.setStatus(Status.WaitForRepublish);
 			art.setExpireDate(null);
 			this.dao.saveOrUpdate(art);
 			this.articleLogDao.create(new ArticleLog(art.getOid(), ActionType.Expired, op.getAccount(), "Wait For Republish", op.getIp()));
 			this.mailService.expired(art.getOid());
 		
 	}
 
 	@Transactional(readOnly=false)
 	public void expire() {
 		logger.info("Execute KB expire task");
 		User op = ThreadLocalHolder.getOperator();
 		Article example = new Article();
 		example.setStatus(Status.Published);
 		List<Condition> conds = new ArrayList<Condition>();
 		conds.add(new SimpleCondition("expireDate", new Date(), OperationEnum.LE));
 		
 		List<Article> list = this.dao.listByExample(example, conds, LikeMode.NONE, new String[0], new String[0]);
 		
 		for (Article art : list) {
 			art.setStatus(Status.WaitForRepublish);
 			art.setExpireDate(null);
 			this.dao.saveOrUpdate(art);
 			this.articleLogDao.create(new ArticleLog(art.getOid(), ActionType.Expired, "System", "Wait For Republish", op.getIp()));
 			this.mailService.expired(art.getOid());
 		}
 		
 		logger.info("Execute KB expire task finished, and expire [{}] article(s)", list.size());
 	}
 	
 	@Transactional(readOnly=false)
 	public void archive(Article article) {
 		User op = ThreadLocalHolder.getOperator();
 		if (!article.getAvailableStatus().contains(Status.Archived)) {
 			logger.warn("Article[{}] can't be archived for User[{}]", article, op.getAccount());
 			return;
 		}
 		
 		List<Article> same = this.findByArticleId(article.getArticleId().getOid());
 		for (Article target : same) {
 			target.setUpdateDate(new Date());
 			target.setStatus(Status.Archived);
 			
 			this.dao.saveOrUpdate(target);
 			
 			this.articleLogDao.create(new ArticleLog(target.getOid(), ActionType.Archived, op.getAccount(), "Archived", op.getIp()));
 			this.mailService.archived(target.getOid());
 		}
 	}
 
 	public List<Article> findByArticleId(String articleId) {
 		Article example = new Article();
 		List<Condition> conds = new ArrayList<Condition>();
 		conds.add(new SimpleCondition("articleId.oid", articleId, OperationEnum.EQ));
 		return this.listByExample(example, conds, LikeMode.NONE, null, null);
 	}
 	@Transactional(readOnly=false)
 	public String getNextArticleId() {
 		return this.incrementer.nextStringValue();
 	}
 	
 	public DataFieldMaxValueIncrementer getIncrementer() {
 		return incrementer;
 	}
 
 	public void setIncrementer(DataFieldMaxValueIncrementer incrementer) {
 		this.incrementer = incrementer;
 	}
 	
 	
 	public Boolean checkRated(Long articleOid, String account) {
 		List<Condition> conds = new ArrayList<Condition>();
 		conds.add(new SimpleCondition("articleOid", articleOid, OperationEnum.EQ));
 		conds.add(new SimpleCondition("account", account, OperationEnum.EQ));
 		conds.add(new InCondition("action", new Object[] {ActionType.Rating1, ActionType.Rating2, ActionType.Rating3, ActionType.Rating4, ActionType.Rating5}));
 		List<StatisticsData> statis = this.statisticsDataDao.listByExample(new StatisticsData(), conds, LikeMode.NONE, null, null);
 		
 		return !statis.isEmpty();
 	}
 	
 	@Override
 	public List<ExportInfo> listProofReadArticles() {
 		List<ExportInfo> infos = new ArrayList<ExportInfo>();
 		
 		Article example = new Article();
 		example.setStatus(Status.WaitForProofRead);
 		ExportInfo info = null;
 		for (Article article : this.dao.listByExample(example, null, null, new String[] {"entryUser"}, null)) {
 			if (null == info || !article.getEntryUser().equals(info.getAccount())) {
 				info = new ExportInfo();
 				info.setAccount(article.getEntryUser());
 				infos.add(info);
 			}
 			info.getArticles().add(article);
 		}
 		return infos;
 	}
 	
 	@Override
 	public List<ExportInfo> listProofReadNews() {
 		List<ExportInfo> infos = new ArrayList<ExportInfo>();
 		
 		Article example = new Article();
 		example.setStatus(Status.WaitForProofRead);
 		example.setNews(Boolean.TRUE);
 		ExportInfo info = null;
 		for (Article article : this.dao.listByExample(example, null, null, new String[] {"entryUser"}, null)) {
 			if (null == info || !article.getEntryUser().equals(info.getAccount())) {
 				info = new ExportInfo();
 				info.setAccount(article.getEntryUser());
 				infos.add(info);
 			}
 			info.getArticles().add(article);
 		}
 		return infos;
 	}
 	@Override
 	public List<ExportInfo> listProofReadKB() {
 		List<ExportInfo> infos = new ArrayList<ExportInfo>();
 		
 		Article example = new Article();
 		example.setStatus(Status.WaitForProofRead);
 		example.setNews(Boolean.FALSE);
 		ExportInfo info = null;
 		for (Article article : this.dao.listByExample(example, null, null, new String[] {"entryUser"}, null)) {
 			if (null == info || !article.getEntryUser().equals(info.getAccount())) {
 				info = new ExportInfo();
 				info.setAccount(article.getEntryUser());
 				infos.add(info);
 			}
 			info.getArticles().add(article);
 		}
 		return infos;
 	}
 	
 	public List<Language> listUsedLanguage(Article article) {
 		List<Language> usedLanguage = new ArrayList<Language>();
 		
 		Article example = new Article();
 		example.setArticleId(article.getArticleId());
 		
 		List<Article> articles = this.dao.listByExample(example);
 		
 		for (Article art : articles) {
 			usedLanguage.add(art.getLanguage());
 		}
 		
 		return usedLanguage;
 	}
 	
 	protected void group(List<Article> articles, List<ExportInfo> infos) {
 		ExportInfo info = null;
 		for (Article article : articles) {
 			if (null == info || !article.getEntryUser().equals(info.getAccount())) {
 				info = new ExportInfo();
 				info.setAccount(article.getEntryUser());
 				infos.add(info);
 				Account account = this.accountDao.findByOid(article.getEntryUser());
 				if (null != account) {
 					info.setName(account.getName());
 				}
 			}
 			info.getArticles().add(article);
 		}
 	}
 	
 	
 	
 	public List<ExportInfo> searchForProofRead(Boolean news, Date beginDate, Date endDate, ArticleType[] types) {
 		Article example = new Article();
 		example.setStatus(Status.LeaderApproved);
 		example.setNews(news);
 		List<Condition> conds = new ArrayList<Condition>();
 		conds.add(new BetweenCondition("entryDate", DateUtils.begin(beginDate), DateUtils.end(endDate)));
 		conds.add(new InCondition("type", types));
 		conds.add(new NullCondition("exportPackage"));
 		List<Article> articles = this.dao.listByExample(example, conds, null, new String[] {"entryUser", "oid"}, null);
 		
 		List<ExportInfo> infos = new ArrayList<ExportInfo>();
 		
 		if (articles.isEmpty()) {
 			logger.warn("No article can be exported.");
 			return infos;
 		}
 		
 		this.group(articles, infos);
 		
 		return infos;
 	}
 	@Transactional(readOnly=false)
 	public List<ExportInfo> exportProofRead(String epId, ArticleType[] types) {
 		User op = ThreadLocalHolder.getOperator();
 		ExportPackage ep = this.exportPackageDao.findByOid(epId);
 		
 		Article example = new Article();
 		example.setStatus(Status.LeaderApproved);
 		example.setNews(ep.getNews());
 		List<Condition> conds = new ArrayList<Condition>();
 		conds.add(new BetweenCondition("entryDate", DateUtils.begin(ep.getBeginDate()), DateUtils.end(ep.getEndDate())));
 		conds.add(new InCondition("type", types));
 		conds.add(new NullCondition("exportPackage"));
 		List<Article> articles = this.dao.listByExample(example, conds, null, new String[] {"entryUser", "oid"}, null);
 		
 		
 		List<ExportInfo> infos = new ArrayList<ExportInfo>();
 		
 		if (articles.isEmpty()) {
 			logger.warn("No article can be exported.");
 			return infos;
 		}
 		
 		for (Article article : articles) {
 			article.setExportPackage(ep);
 			article.setStatus(Status.WaitForProofRead);
 			this.dao.update(article);
 			this.articleLogDao.create(new ArticleLog(article.getOid(), ActionType.Approve, op.getAccount(), "Waiting For Proofread", op.getIp()));
 		}
 		
 		Map<Account, List<Article>> leaderMap = new HashMap<Account, List<Article>>();
 		Map<Account, List<Article>> engineerMap = new HashMap<Account, List<Article>>();
 
 		for (Article article : articles) {
 			Account engineer = this.accountDao.findByOid(article.getEntryUser());
 			article.setEntryUserName(engineer.getName());
 			
 			List<Article> list = null;
 			// Find Leader Group
 			Group lGroup = this.groupDao.findByOid(article.getLeaderGroupId());
 			List<Account> leaders = lGroup.getAccounts();
 			String leaderId = "";
 			for (Account leader : leaders) {
 				if (StringUtils.isEmpty(leaderId)) {
 					leaderId += leader.getId();
 				} else {
 					leaderId += (","+leader.getId());
 				}
 				if (leaderMap.containsKey(leader)) {
 					list = leaderMap.get(leader);
 				} else {
 					list = new ArrayList<Article>();
 					leaderMap.put(leader, list);
 				}
 				list.add(article);
 			}
 			article.setLeaderAccount(leaderId);
 
 			// Engineer
 			list = null;
 			if (engineerMap.containsKey(engineer)) {
 				list = engineerMap.get(engineer);
 			} else {
 				list = new ArrayList<Article>();
 				engineerMap.put(engineer, list);
 			}
 			list.add(article);
 			
 		}
 		
 		//Send mail to leader
 		for (Entry<Account, List<Article>> entry : leaderMap.entrySet()) {
 			this.mailService.packageReadLeader(entry.getKey(), entry.getValue());
 		}
 		//Send mail to engineer(agent)
 		for (Entry<Account, List<Article>> entry : engineerMap.entrySet()) {
 			this.mailService.packageReadAgent(entry.getKey(), entry.getValue());
 		}
 		
 		this.group(articles, infos);
 		
 		//Update ArticleIdList
 		StringBuilder sb = new StringBuilder();
 		StringBuilder oids = new StringBuilder();
 		if (!articles.isEmpty()) {
 			sb.append(articles.get(0).getArticleId().getOid());
 			oids.append(articles.get(0).getOid());
 			for (int i = 1; i < articles.size(); i++) {
 				sb.append(","+articles.get(i).getArticleId().getOid());
 				oids.append(","+articles.get(i).getOid());
 			}
 		}
 
 		ep.setArticleIdList(sb.toString());
 		ep.setOidList(oids.toString());
 		ep.setNewsIdList(this.updateNewsForEP(epId, ep.getBeginDate(), ep.getEndDate()));
 		ep.setExportDate(new Date());
 		this.exportPackageDao.update(ep);
 		/*
 		for (Article article : articles) {
 			this.mailService.proofread(article.getOid());
 		}
 		*/
 		
 		
 		return infos;
 	}
 	
 	protected String updateNewsForEP(String epOid, Date beginDate, Date endDate) {
 		List<String> newsIds = this.newsDao.findUpdateableNewsId(beginDate, endDate);
 		for (String newsId : newsIds) {
 			this.newsDao.updateNewsPackageId(epOid, newsId);
 		}
 		
 		StringBuilder sb = new StringBuilder();
 		if (!newsIds.isEmpty()) {
 			sb.append(newsIds.get(0));
 			for (int i = 1; i < newsIds.size(); i++) {
 				sb.append(","+newsIds.get(i));
 			}
 		}
 		return sb.toString();
 	}
 	
 	public List<ExportInfo> viewExportPackage(String epOid) {
 		Article example = new Article();
 		List<Condition> conds = new ArrayList<Condition>();
 		conds.add(new SimpleCondition("exportPackage.oid", epOid, OperationEnum.EQ));
 		List<Article> articles = this.dao.listByExample(example, conds, null, new String[] {"entryUser", "oid"}, null);
 		List<ExportInfo> infos = new ArrayList<ExportInfo>();
 		
 		this.group(articles, infos);
 		return infos;
 	}
 
 	@Override
 	@Transactional(readOnly=false)
 	public void saveOrUpdate(Article entity) {
 		User op = ThreadLocalHolder.getOperator();
 		super.saveOrUpdate(entity);
 		this.articleLogDao.create(new ArticleLog(entity.getOid(), ActionType.Update, op.getAccount(), "Update Data", op.getIp()));
 	}
 	
 	public List<PackagedArticle> findPAByExportPackage(String epOid) {
 		List<PackagedArticle> results = new ArrayList<PackagedArticle>();
 		Article example = new Article();
 		List<Condition> conds = new ArrayList<Condition>();
 		conds.add(new SimpleCondition("exportPackage.oid", epOid, OperationEnum.EQ));
 		List<Article> articles = this.dao.listByExample(example, conds, null, new String[] {"entryUser", "oid"}, null);
 		Account agent = null;
 		Account leader = null;
 		for (Article article : articles) {
 			if (null == agent || !agent.getId().equals(article.getEntryUser())) {
 				agent = this.accountDao.findByOid(article.getEntryUser());
 				leader = findGroupLeaders(article.getUserGroup());
 			}
 			PackagedArticle pa = new PackagedArticle();
 			pa.setAgent(agent.getName());
 			pa.setArticleId(article.getArticleId().getOid());
 			if (null != leader) {
 				pa.setLeader(leader.getName());
 			}
 			pa.setStatus(article.getI18nStatus());
 			pa.setSummary(article.getSummary());
 			results.add(pa);
 		}
 		return results;
 	}
 	public List<PackagedArticle> findFWPAByExportPackage(String epOid) {
 		List<PackagedArticle> results = new ArrayList<PackagedArticle>();
 		ExportPackage ep = this.exportPackageDao.findByOid(epOid);
 		if (null == ep || StringUtils.isEmpty(ep.getNewsIdList())) {
 			return results;
 		}
 		String[] newsIds = ep.getNewsIdList().split(",");
 		for (String newsId : newsIds) {
 			results.add(this.newsDao.findPAByNewsId(newsId));
 		}
 		return results;
 	}
 	private Account findGroupLeaders(String groupId) {
 		List<Account> result = new ArrayList<Account>();
 		//Find Leader 
 		String leaderGroupId = "";
 		if (!"L3_Admin".equals(groupId)) {
 			leaderGroupId = groupId+"_Leader";
 		} else {
 			leaderGroupId = groupId;
 		}
 		
 		Group leader = this.groupDao.findByOid(leaderGroupId);
 		
 		if (null != leader) {
 			result.addAll(leader.getAccounts());
 		} else {
 			logger.error("Can't find Leader Group[{}] in system", leaderGroupId);
 		}
 		
 		return result.isEmpty()?null:result.get(0);
 	}
 	
 }
