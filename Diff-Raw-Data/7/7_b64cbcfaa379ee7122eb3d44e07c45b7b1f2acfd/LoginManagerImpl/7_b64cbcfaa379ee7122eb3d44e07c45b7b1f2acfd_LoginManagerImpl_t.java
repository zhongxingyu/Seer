 /**
  * 
  */
 package com.chinarewards.qqgbvpn.main.logic.login.impl;
 
 import java.util.Arrays;
 
 import javax.persistence.NoResultException;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.domain.Pos;
 import com.chinarewards.qqgbvpn.domain.event.DomainEntity;
 import com.chinarewards.qqgbvpn.domain.event.DomainEvent;
 import com.chinarewards.qqgbvpn.domain.status.PosDeliveryStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosInitializationStatus;
 import com.chinarewards.qqgbvpn.domain.status.PosOperationStatus;
 import com.chinarewards.qqgbvpn.logic.journal.JournalLogic;
 import com.chinarewards.qqgbvpn.main.dao.qqapi.PosDao;
 import com.chinarewards.qqgbvpn.main.logic.challenge.ChallengeUtil;
 import com.chinarewards.qqgbvpn.main.logic.login.LoginManager;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.InitRequestMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.InitResponseMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.LoginRequestMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.LoginResponseMessage;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.init.InitResult;
 import com.chinarewards.qqgbvpn.main.protocol.cmd.login.LoginResult;
 import com.chinarewards.utils.StringUtil;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 /**
  * Deal with init, login, bind operation.
  * 
  * @author cream
  * @since 1.0.0 2011-08-26
  */
 public class LoginManagerImpl implements LoginManager {
 
 	Logger logger = LoggerFactory.getLogger(getClass());
 
 	@Inject
 	Provider<PosDao> posDao;
 
 	@Inject
 	JournalLogic journalLogic;
 
 	@Override
 	public InitResponseMessage init(InitRequestMessage req) {
 		logger.debug("InitResponse() invoke");
 
 		InitResponseMessage resp = new InitResponseMessage();
 
 		if (StringUtil.isEmptyString(req.getPosId())) {
 			throw new IllegalArgumentException("POS ID is missing!");
 		}
 
 		Pos pos = null;
 		InitResult result = null;
 
 		byte[] challenge = ChallengeUtil.generateChallenge();
 
 		try {
 			
 			// TODO reports better error code to distinguish between 
 			// POS ID not found or not assigned.

			pos = posDao.get().fetchPos(req.getPosId(), null, null,
 					PosOperationStatus.ALLOWED);
 
 			// check pos.secret. When not existed, generate one.
 			if (StringUtil.isEmptyString(pos.getSecret())) {
 				pos.setSecret(ChallengeUtil.generatePosSecret());
 			}
 
 			pos.setChallenge(challenge);
 			posDao.get().merge(pos);
 			logger.debug("POS ID:{}, init challenge saved - {}", new Object[] {
 					req.getPosId(), Arrays.toString(challenge) });
 
 			switch (pos.getIstatus()) {
 			case INITED:
 				result = InitResult.INIT;
 				break;
 			case UNINITED:
 				result = InitResult.UNINIT;
 				break;
 			default:
 				result = InitResult.OTHERS;
 				break;
 			}
 		} catch (NoResultException e) {
 			logger.warn("No usable POS machine found. POS ID not exists or not assigned.");
 			result = InitResult.OTHERS;
 		}
 		resp.setChallenge(challenge);
 		resp.setResult(result.getPosCode());
 
 		// Add journal.
 		ObjectMapper mapper = new ObjectMapper();
 		String eventDetail = null;
 		try {
 			eventDetail = mapper.writeValueAsString(resp);
 		} catch (Exception e) {
 			logger.error("mapping InitResponse error.", e);
 			eventDetail = e.toString();
 		}
 
 		journalLogic.logEvent(DomainEvent.POS_INIT_REQ.toString(),
 				DomainEntity.POS.toString(), req.getPosId(), eventDetail);
 
 		return resp;
 	}
 
 	@Override
 	public LoginResponseMessage login(LoginRequestMessage req) {
 		LoginResponseMessage resp = new LoginResponseMessage();
 
 		LoginResult result = null;
 		byte[] challenge = ChallengeUtil.generateChallenge();
 		String domainEvent = null;
 		try {
 			Pos pos = posDao.get().fetchPos(req.getPosId(), null, null, null);
 			logger.trace(
 					"Loaded from db: pos.posId:{}, pos.secret:{}, pos.challenge:{}",
 					new Object[] { pos.getPosId(), pos.getSecret(),
 							pos.getChallenge() });
 			boolean check = ChallengeUtil.checkChallenge(
 					req.getChallengeResponse(), pos.getSecret(),
 					pos.getChallenge());
 
 			logger.debug("new challenge for POS (POS ID):{}", challenge, pos.getPosId());
 			pos.setChallenge(challenge);
 			posDao.get().merge(pos);
 
 			if (check) {
 				if (pos.getIstatus() == PosInitializationStatus.INITED) {
 					result = LoginResult.SUCCESS;
 					domainEvent = DomainEvent.POS_LOGGED_IN.toString();
 				} else {
 					// Check POS initialization status. Must be INITED.
 					result = LoginResult.OTHERS;
 					domainEvent = DomainEvent.POS_LOGGED_FAILED.toString();
 				}
 			} else {
 				result = LoginResult.VALIDATE_FAILED;
 				domainEvent = DomainEvent.POS_LOGGED_FAILED.toString();
 			}
 		} catch (NoResultException e) {
 			logger.warn("POS ID '{}' not found in DB", req.getPosId());
 			domainEvent = DomainEvent.POS_LOGGED_FAILED.toString();
 			result = LoginResult.POSID_NOT_EXIST;
 		}
 		resp.setChallenge(challenge);
 		resp.setResult(result.getPosCode());
 
 		// Add journal.
 		ObjectMapper mapper = new ObjectMapper();
 		String eventDetail = null;
 		try {
 			eventDetail = mapper.writeValueAsString(resp);
 		} catch (Exception e) {
 			logger.error("mapping InitResponse error.", e);
 			eventDetail = e.toString();
 		}
 
 		journalLogic.logEvent(domainEvent, DomainEntity.POS.toString(),
 				req.getPosId(), eventDetail);
 
 		return resp;
 	}
 
 	@Override
 	public LoginResponseMessage bind(LoginRequestMessage req) {
 		LoginResponseMessage resp = new LoginResponseMessage();
 
 		LoginResult result = null;
 		byte[] challenge = ChallengeUtil.generateChallenge();
 		String domainEvent = null;
 		try {
 			Pos pos = posDao.get().fetchPos(req.getPosId(), null, null, null);
 			logger.trace(
 					"pos.posId:{}, pos.secret:{}, pos.challenge:{}",
 					new Object[] { pos.getPosId(), pos.getSecret(),
 							pos.getChallenge() });
 			boolean check = ChallengeUtil.checkChallenge(
 					req.getChallengeResponse(), pos.getSecret(),
 					pos.getChallenge());
 
 			logger.debug("new challenge:{}", challenge);
 			pos.setChallenge(challenge);
 			posDao.get().merge(pos);
 
 			if (check) {
 				if (pos.getIstatus() == PosInitializationStatus.UNINITED) {
 					result = LoginResult.SUCCESS;
 					pos.setIstatus(PosInitializationStatus.INITED);
 					domainEvent = DomainEvent.POS_INIT_OK.toString();
 				} else {
 					// Check POS initialization status. Must be INITED.
 					result = LoginResult.OTHERS;
 					domainEvent = DomainEvent.POS_INIT_FAILED.toString();
 				}
 			} else {
 				result = LoginResult.VALIDATE_FAILED;
 				domainEvent = DomainEvent.POS_INIT_FAILED.toString();
 			}
 		} catch (NoResultException e) {
 			logger.warn("Pos ID not found in DB. PosId={}", req.getPosId());
 			domainEvent = DomainEvent.POS_INIT_FAILED.toString();
 			result = LoginResult.POSID_NOT_EXIST;
 		}
 		resp.setChallenge(challenge);
 		resp.setResult(result.getPosCode());
 
 		// Add journal.
 		ObjectMapper mapper = new ObjectMapper();
 		String eventDetail = null;
 		try {
 			eventDetail = mapper.writeValueAsString(resp);
 		} catch (Exception e) {
 			logger.error("mapping InitResponse error.", e);
 			eventDetail = e.toString();
 		}
 
 		journalLogic.logEvent(domainEvent, DomainEntity.POS.toString(),
 				req.getPosId(), eventDetail);
 
 		return resp;
 	}
 }
