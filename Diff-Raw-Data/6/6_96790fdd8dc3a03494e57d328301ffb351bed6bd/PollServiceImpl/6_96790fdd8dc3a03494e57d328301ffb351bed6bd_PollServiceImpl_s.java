 package de.uniluebeck.itm.ep0.poll.service;
 
 import de.uniluebeck.itm.ep0.poll.dao.PollDao;
 import de.uniluebeck.itm.ep0.poll.dao.VoteDao;
 import de.uniluebeck.itm.ep0.poll.domain.BoAbstractOption;
 import de.uniluebeck.itm.ep0.poll.domain.BoOptionList;
 import de.uniluebeck.itm.ep0.poll.domain.BoPoll;
 import de.uniluebeck.itm.ep0.poll.domain.BoTextOption;
 import de.uniluebeck.itm.ep0.poll.domain.BoVote;
 import de.uniluebeck.itm.ep0.poll.domain.XoOption;
 import de.uniluebeck.itm.ep0.poll.domain.XoPoll;
 import de.uniluebeck.itm.ep0.poll.domain.XoPollInfo;
 import de.uniluebeck.itm.ep0.poll.domain.XoVote;
 import de.uniluebeck.itm.ep0.poll.exception.PollException;
 
 import static de.uniluebeck.itm.ep0.poll.util.Checks.ifNullArgument;
 import static de.uniluebeck.itm.ep0.poll.util.Checks.ifNull;
 
 import de.uniluebeck.itm.ep0.poll.util.PollUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 @Service("pollService")
 public class PollServiceImpl implements PollService {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(PollServiceImpl.class);
 
     @Autowired
     private PollDao pollDao;
 
     @Autowired
     private VoteDao voteDao;
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public XoPoll addPoll(final XoPoll xoPoll) throws PollException {
         ifNullArgument(xoPoll, "xoPoll == null");
         LOGGER.info("addPoll( " + xoPoll.getName() + " ) called");
        BoPoll boPoll = new BoPoll(xoPoll);
         if (xoPoll.getId() == null) {
             // Create a new persistent poll object
             pollDao.add(boPoll);
         } else {
             // Update an existing persistent object
             boPoll = pollDao.findById(Integer.parseInt(xoPoll.getId()));
             ifNull(boPoll, "Poll with id #" + xoPoll.getId() + " does not exist!");
             pollDao.update(boPoll);
         }
         XoPoll result = boPoll.toXo();
         ifNull(result, "result == null");
         result.setAdminUuid(boPoll.getAdminUuid());
 
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = true)
     public List<XoPoll> getPolls() throws PollException {
         LOGGER.info("getPolls() called");
         final List<XoPoll> xos = new ArrayList<XoPoll>();
         for (BoPoll bo : pollDao.findAll()) {
             xos.add(bo.toXo());
         }
         return xos;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = true)
     public XoPoll getPoll(final Integer pollId) throws PollException {
         ifNullArgument(pollId, "pollId == null");
         LOGGER.info("getPoll( " + pollId + " ) called");
         BoPoll bo = pollDao.findById(pollId);
         ifNull(bo, "pollDao.findById(pollId) is null!");
 
         return bo.toXo();
     }
 
     @Override
     @Transactional(readOnly = true)
     public XoPoll getPoll(final String uuid) throws PollException {
         ifNullArgument(uuid, "uuid == null");
         LOGGER.info("getPoll( " + uuid + " ) called");
         BoPoll boPoll = pollDao.findByUuid(uuid);
         ifNull(boPoll, "Poll with UUID=" + uuid + " not found!");
 
         return boPoll.toXo();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = true)
     public Map<String, List<XoVote>> getVotesForOptionList(final Integer optionListId) throws PollException {
         ifNullArgument(optionListId, "optionListId == null");
         LOGGER.info("getVotesForOptionList( " + optionListId + " ) called");
         Map<String, List<XoVote>> result = new HashMap<String, List<XoVote>>();
         final BoOptionList optionList = pollDao
                 .findOptionList(optionListId);
         for (BoAbstractOption option : optionList.getOptions()) {
             List<BoVote> votes = voteDao.findByOptionId(option.getId());
             for (BoVote vote : votes) {
                 final String voter = vote.getVoter();
                 if (result.containsKey(voter)) {
                     final List<XoVote> xoVotes = result.get(voter);
                     xoVotes.add(vote.toXo());
                 } else {
                     final List<XoVote> xoVotes = new ArrayList<XoVote>();
                     xoVotes.add(vote.toXo());
                     result.put(voter, xoVotes);
                 }
             }
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional
     public void removePoll(final Integer pollId, final String adminUuid) throws PollException {
         ifNullArgument(pollId, "pollId == null");
         ifNullArgument(adminUuid, "adminUuid == null");
         LOGGER.info("removePoll( " + pollId + " ) called");
         final BoPoll boPoll = pollDao.findById(pollId);
         if (boPoll != null && boPoll.getAdminUuid().equals(adminUuid)) {
             pollDao.remove(boPoll);
         }
     }
 
     @Override
     @Transactional
     public void removeVote(final Integer voteId, final String adminUuid) throws PollException {
         ifNullArgument(voteId, "voteId == null");
         ifNullArgument(adminUuid, "adminUuid == null");
         LOGGER.info("removeVote( " + voteId + " ) called");
         final BoVote boVote = voteDao.findById(voteId);
         ifNull(boVote, "boVote == null");
         final BoPoll boPoll = pollDao.findByOptionId(boVote.getOptionId());
         if (boPoll.getAdminUuid().equals(adminUuid)) {
             voteDao.remove(boVote);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = true)
     public List<XoVote> getVotes(final Integer optionId) throws PollException {
         ifNullArgument(optionId, "optionId == null");
         LOGGER.info("getVotes( " + optionId + " ) called");
         final List<XoVote> xos = new ArrayList<XoVote>();
         for (BoVote bo : voteDao.findByOptionId(optionId)) {
             xos.add(bo.toXo());
         }
         return xos;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional
     public XoVote addVote(final XoVote xoVote) throws PollException {
         ifNullArgument(xoVote, "xoVote == null");
         LOGGER.info("addVote( " + xoVote.toString() + " ) called");
 
         Integer optionId;
         try {
             optionId = Integer.parseInt(xoVote.getOptionId());
         } catch (NumberFormatException ex) {
             throw new PollException("optionId == null", ex);
         }
 
         final boolean isValid = PollUtil.isValid(pollDao.findByOptionId(
                 optionId).toXo());
         if (!isValid) {
             throw new PollException("poll is not valid");
         }
 
         final boolean isUnique = voteDao.findByVoterAndOption(
                 xoVote.getVoter(), Integer.parseInt(xoVote.getOptionId()))
                                         .isEmpty();
         if (!isUnique) {
             LOGGER.info("The voter " + xoVote.getVoter()
                     + " already voted for the option " + xoVote.getOptionId()
                     + "!");
             return null;
         }
 
         final BoVote boVote = new BoVote(xoVote);
         ifNull(boVote, "boVote == null");
         voteDao.add(boVote);
 
         return boVote.toXo();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = true)
     public XoOption getMostPopularOption(final Integer pollId,
                                          final Integer optionListId) throws PollException {
         ifNullArgument(pollId, "pollId == null");
         ifNullArgument(optionListId, "openListId == null");
         LOGGER.info("getMostPopularOption( " + pollId + " ) called");
         final BoOptionList boOptionList = pollDao.findOptionList(pollId,
                 optionListId);
         BoAbstractOption result = new BoTextOption();
         int votes = 0;
         for (BoAbstractOption boOption : boOptionList.getOptions()) {
             final List<BoVote> boVotesList = voteDao
                     .findByOptionId(boOption.getId());
             if (boVotesList != null) {
                 if (votes < boVotesList.size()) {
                     votes = boVotesList.size();
                     result = boOption;
                 }
             }
         }
         return (XoOption) result.toXo();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = true)
     public List<XoPollInfo> getPollInfos(final boolean onlyValidPublic) throws PollException {
         LOGGER.info("getPollInfos (" + onlyValidPublic + ") called");
         final List<XoPollInfo> result = new ArrayList<XoPollInfo>();
         for (BoPoll bo : pollDao.findAll()) {
             if (!onlyValidPublic
                     || (PollUtil.isValid(bo.toXo()) && Boolean.TRUE
                     .equals(bo.getIsPublic()))) {
                 result.add(new XoPollInfo(bo.getId().toString(), bo
                         .getUuid(), bo.getName().toXo()));
             }
         }
         return result;
     }
 
     @Override
     @Transactional(readOnly = true)
     public List<XoPollInfo> findPoll(final String name) throws PollException {
         ifNullArgument(name, "name == null");
         List<BoPoll> polls = pollDao.findAll();
 
         polls = getMatches(polls, name);
 
         List<XoPollInfo> result = new ArrayList<XoPollInfo>(polls.size());
         for (BoPoll poll : polls) {
             result.add(new XoPollInfo(poll.getId().toString(), poll
                     .getUuid(), poll.getName().toXo()));
         }
         return result;
     }
 
     private List<BoPoll> getMatches(final List<BoPoll> polls, final String name) {
         List<BoPoll> result = new ArrayList<BoPoll>();
         Pattern p = Pattern.compile(name);
 
         for (BoPoll poll : polls) {
             for (String localizedName : poll.getName().getLocalizedValues()
                                             .values()) {
                 if (p.matcher(localizedName).matches()) {
                     if (!result.contains(poll))
                         result.add(poll);
                 }
             }
         }
         return result;
     }
 }
