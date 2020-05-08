 package cl.own.usi.dao.impl.cassandra;
 
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.emailColumn;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.dbKeyspace;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.answersColumnFamily;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.firstnameColumn;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.lastnameColumn;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.passwordColumn;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.usersColumnFamily;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.bonusesColumnFamily;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.ranksColumnFamily;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.scoresColumnFamily;
 import static cl.own.usi.dao.impl.cassandra.CassandraConfiguration.loginsColumnFamily;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.ReentrantLock;
 
 import me.prettyprint.cassandra.model.AllOneConsistencyLevelPolicy;
 import me.prettyprint.cassandra.model.QuorumAllConsistencyLevelPolicy;
 import me.prettyprint.cassandra.serializers.BooleanSerializer;
 import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
 import me.prettyprint.cassandra.serializers.IntegerSerializer;
 import me.prettyprint.cassandra.serializers.LongSerializer;
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.hector.api.Cluster;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.beans.ColumnSlice;
 import me.prettyprint.hector.api.beans.HColumn;
 import me.prettyprint.hector.api.beans.OrderedRows;
 import me.prettyprint.hector.api.beans.Row;
 import me.prettyprint.hector.api.beans.Rows;
 import me.prettyprint.hector.api.exceptions.HectorException;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.mutation.MutationResult;
 import me.prettyprint.hector.api.mutation.Mutator;
 import me.prettyprint.hector.api.query.MultigetSliceQuery;
 import me.prettyprint.hector.api.query.QueryResult;
 import me.prettyprint.hector.api.query.RangeSlicesQuery;
 import me.prettyprint.hector.api.query.SliceQuery;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import cl.own.usi.dao.ScoreDAO;
 import cl.own.usi.dao.UserDAO;
 import cl.own.usi.exception.UserAlreadyLoggedException;
 import cl.own.usi.model.Answer;
 import cl.own.usi.model.User;
 
 @Repository
 public class AllDAOCassandraImpl implements ScoreDAO, UserDAO, InitializingBean {
 
 	private static final String DEFAULT_START_KEY = "";
 	private static final String DEFAULT_FIELDS_SEPARATOR = "%%%";
 	private static final int DEFAULT_MAX_QUESTIONS = 20;
 
 	private List<Integer> orderedScores = Collections.<Integer> emptyList();
 	private List<Integer> reverseOrderedScores = Collections
 			.<Integer> emptyList();
 
 	private final ReentrantLock scoresComputationLock = new ReentrantLock();
 
 	@Autowired
 	private Cluster cluster;
 
 	private Keyspace consistencyOneKeyspace;
 	private Keyspace consistencyQuorumKeyspace;
 
 	private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
 	private final StringSerializer ss = StringSerializer.get();
 	private final ByteBufferSerializer bbs = ByteBufferSerializer.get();
 	private final IntegerSerializer is = IntegerSerializer.get();
 	private final LongSerializer ls = LongSerializer.get();
 	private final BooleanSerializer bs = BooleanSerializer.get();
 	
 	@Override
 	public List<User> getTop(int limit) {
 	
 		return computeTop(limit);
 
 	}
 
 	@Override
 	public List<User> getBefore(User user, int limit) {
 
 		ensureOrderedScoresLoaded();
 
 		String userKey = generateRankedUserKey(user);
 
 		List<User> users = findRankedUsers(limit, user.getScore(),
 				orderedScores, true, userKey);
 
 		return users;
 	}
 
  	@Override
 	public List<User> getAfter(User user, int limit) {
 
 		ensureOrderedScoresLoaded();
 
 		String userKey = generateRankedUserKey(user);
 
 		List<User> users = findRankedUsers(limit, user.getScore(),
 				reverseOrderedScores, false, userKey);
 
 		return users;
 	}
 
 	@Override
 	public int setBadAnswer(final String userId, final int questionNumber) {
 
 		int score = getScore(userId);
 		
 		Mutator<String> mutator = HFactory.createMutator(
 				consistencyOneKeyspace, StringSerializer.get());
 		mutator.addInsertion(userId, bonusesColumnFamily, HFactory
 				.createColumn(questionNumber, Boolean.FALSE, is, bs));
 		mutator.execute();
 		return score;
 	}
 
 	@Override
 	public int setGoodAnswer(final String userId, final int questionNumber,
 			final int questionValue) {
 		
 		if (userId != null) {
 
 			int oldScore = getScore(userId, questionNumber - 1);
 
 			SliceQuery<String, Integer, Boolean> q = HFactory.createSliceQuery(
 					consistencyOneKeyspace, ss, is, bs);
 			q.setKey(userId);
 			q.setColumnFamily(bonusesColumnFamily);
 			q.setRange(questionNumber - 1, 0, true, DEFAULT_MAX_QUESTIONS);
 
 			QueryResult<ColumnSlice<Integer, Boolean>> result = q.execute();
 
 			ColumnSlice<Integer, Boolean> columnSlice = result.get();
 
 			int newBonus = 0;
 			for (int searchedQuestion = questionNumber - 1; searchedQuestion > 0; searchedQuestion--) {
 				HColumn<Integer, Boolean> previousAnswer = columnSlice.getColumnByName(searchedQuestion);
 				if (previousAnswer != null && previousAnswer.getValue()) {
 					newBonus++;
 				} else {
 					break;
 				}
 			}
 
 			int newScore = oldScore + questionValue + newBonus;
 
 			Mutator<String> mutator = HFactory.createMutator(
 					consistencyOneKeyspace, StringSerializer.get());
 			mutator.addInsertion(userId, bonusesColumnFamily,
 					HFactory.createColumn(questionNumber, Boolean.TRUE, is, bs));
 
 			mutator.addInsertion(userId, scoresColumnFamily,
 					HFactory.createColumn(questionNumber, newScore, is, is));
 
 			mutator.execute();
 
 			return newScore;
 
 		} else {
 			return 0;
 		}
 
 	}
 
 	private String generateRankedUserKey(User user) {
		return user.getLastname() + user.getFirstname() + user.getEmail();
 	}
 
 	@Override
 	public boolean insertUser(User user) {
 
 		String userId = CassandraHelper.generateUserId(user);
 
 		try {
 			if (isEmailInDatabase(userId)) {
 				logger.debug(
 						"user {} was already in the database, insertion aborted",
 						user.getEmail());
 				return false;
 			}
 
 			Mutator<String> mutator = HFactory.createMutator(
 					consistencyQuorumKeyspace, StringSerializer.get());
 
 			// Add the user in the CF Users
 			mutator.addInsertion(userId, usersColumnFamily,
 					HFactory.createColumn(emailColumn, user.getEmail(), ss, ss));
 			mutator.addInsertion(userId, usersColumnFamily, HFactory
 					.createColumn(firstnameColumn, user.getFirstname(), ss, ss));
 			mutator.addInsertion(userId, usersColumnFamily, HFactory
 					.createColumn(lastnameColumn, user.getLastname(), ss, ss));
 			mutator.addInsertion(userId, usersColumnFamily, HFactory
 					.createColumn(passwordColumn, user.getPassword(), ss, ss));
 
 			MutationResult result = mutator.execute();
 			logger.debug(
 					"user {} was successfully inserted in {} ms, userId = {}",
 					new Object[] { user.getEmail(),
 							result.getExecutionTimeMicro(), userId });
 		} catch (HectorException e) {
 			logger.error("An error occured while inserting user " + user.getEmail(), e);
 			return false;
 		}
 		return true;
 	}
 
 	private boolean isEmailInDatabase(String userId) {
 		SliceQuery<String, String, ByteBuffer> q = HFactory.createSliceQuery(
 				consistencyQuorumKeyspace, ss, ss, bbs);
 		q.setKey(userId);
 		q.setColumnFamily(usersColumnFamily);
 		q.setColumnNames(emailColumn);
 
 		QueryResult<ColumnSlice<String, ByteBuffer>> result = q.execute();
 		ColumnSlice<String, ByteBuffer> cs = result.get();
 
 		return !cs.getColumns().isEmpty();
 	}
 
 	@Override
 	public User getUserById(String userId) {
 
 		if (userId == null) {
 			return null;
 		}
 
 		long starttime = System.currentTimeMillis();
 
 		SliceQuery<String, String, ByteBuffer> q = HFactory.createSliceQuery(
 				consistencyOneKeyspace, ss, ss, bbs);
 		q.setKey(userId);
 		q.setColumnFamily(usersColumnFamily);
 		q.setColumnNames(emailColumn, firstnameColumn, lastnameColumn);
 
 		QueryResult<ColumnSlice<String, ByteBuffer>> result = q.execute();
 		ColumnSlice<String, ByteBuffer> cs = result.get();
 
 		User user;
 		if (!cs.getColumns().isEmpty()) {
 			user = toUser(userId, cs);
 		} else {
 			logger.debug("fetching userId={} is impossible, not in db", userId);
 			return null;
 		}
 
 		// load score
 		user.setScore(getScore(userId));
 
 		logger.debug("User {} loaded in {} ms", userId,
 				(System.currentTimeMillis() - starttime));
 
 		return user;
 
 	}
 
 	private int getScore(String userId) {
 		return getScore(userId, null, false);
 	}
 	
 	private int getScore(String userId, Integer tillQuestionNumber) {
 		return getScore(userId, tillQuestionNumber, false);
 	}
 	
 	private int getScore(String userId, Integer tillQuestionNumber, boolean quorum) {
 
 		// load score
 		SliceQuery<String, Integer, Integer> sliceQuery = HFactory
 				.createSliceQuery(quorum ? consistencyQuorumKeyspace : consistencyOneKeyspace, ss, is, is);
 		sliceQuery.setKey(userId);
 		sliceQuery.setColumnFamily(scoresColumnFamily);
 		if (tillQuestionNumber != null) {
 			sliceQuery.setRange(tillQuestionNumber, 0, true, 1);
 		} else {
 			sliceQuery.setRange(DEFAULT_MAX_QUESTIONS, 0, true, 1);			
 		}
 
 		QueryResult<ColumnSlice<Integer, Integer>> queryResult = sliceQuery
 				.execute();
 		ColumnSlice<Integer, Integer> columnSlice = queryResult.get();
 		if (!columnSlice.getColumns().isEmpty()) {
 			return columnSlice.getColumns().get(0).getValue();
 		} else {
 			return 0;
 		}
 
 	}
 
 	private boolean isLogged(String userId) {
 
 		// load score
 		SliceQuery<String, Long, Boolean> sliceQuery = HFactory
 				.createSliceQuery(consistencyOneKeyspace, ss, ls, bs);
 		sliceQuery.setKey(userId);
 		sliceQuery.setColumnFamily(loginsColumnFamily);
 		sliceQuery.setRange(System.currentTimeMillis(), 0L, true, 1);
 
 		QueryResult<ColumnSlice<Long, Boolean>> queryResult = sliceQuery
 				.execute();
 		ColumnSlice<Long, Boolean> columnSlice = queryResult.get();
 		if (!columnSlice.getColumns().isEmpty()) {
 			return columnSlice.getColumns().get(0).getValue();
 		} else {
 			return false;
 		}
 
 	}
 
 	private User toUser(String userId, ColumnSlice<String, ByteBuffer> cs) {
 		User user = new User();
 		user.setEmail(ss.fromByteBuffer(cs.getColumnByName(emailColumn)
 				.getValue()));
 		user.setFirstname(ss.fromByteBuffer(cs.getColumnByName(firstnameColumn)
 				.getValue()));
 		user.setLastname(ss.fromByteBuffer(cs.getColumnByName(lastnameColumn)
 				.getValue()));
 		HColumn<String, ByteBuffer> passwordColum = cs.getColumnByName(passwordColumn);
 		if (passwordColum != null) {
 			user.setPassword(ss.fromByteBuffer(passwordColum.getValue()));
 		}
 		user.setUserId(userId);
 		return user;
 	}
 
 	@Override
 	public void insertRequest(String userId, int questionNumber) {
 		// not needed anymore.
 	}
 
 	@Override
 	public void insertAnswer(Answer answer) {
 
 		logger.debug(
 				"insertAnswer ({}, {}, {})",
 				new Object[] { answer.getAnswerNumber(),
 						answer.getQuestionNumber(), answer.getUserId() });
 		try {
 			Mutator<String> mutator = HFactory.createMutator(
 					consistencyOneKeyspace, ss);
 
 			mutator.addInsertion(
 					answer.getUserId(),
 					answersColumnFamily,
 					HFactory.createColumn(answer.getQuestionNumber(),
 							answer.getAnswerNumber(), is, is));
 
 			mutator.execute();
 			logger.debug("answer inserted, {}", answer);
 
 		} catch (HectorException e) {
 			logger.error("An error occured while inserting answer", e);
 		}
 	}
 
 	@Override
 	public List<Answer> getAnswers(String userId) {
 
 		SliceQuery<String, Integer, Integer> query = HFactory.createSliceQuery(
 				consistencyOneKeyspace, ss, is, is);
 		query.setColumnFamily(answersColumnFamily);
 		query.setKey(userId);
 		query.setRange(0, DEFAULT_MAX_QUESTIONS, false, DEFAULT_MAX_QUESTIONS);
 
 		QueryResult<ColumnSlice<Integer, Integer>> result = query.execute();
 		List<HColumn<Integer, Integer>> columns = result.get().getColumns();
 
 		if (columns.size() == 0) {
 			return Collections.emptyList();
 		}
 
 		List<Answer> answers = new ArrayList<Answer>(columns.size());
 		for (HColumn<Integer, Integer> column : columns) {
 			Answer answer = new Answer();
 			answer.setAnswerNumber(column.getValue());
 			answer.setQuestionNumber(column.getName());
 			answer.setUserId(userId);
 			answers.add(answer);
 		}
 
 		return answers;
 	}
 
 	@Override
 	public String login(String email, String password)
 			throws UserAlreadyLoggedException {
 
 		String userId = CassandraHelper.generateUserId(email);
 
 		SliceQuery<String, String, ByteBuffer> q = HFactory.createSliceQuery(
 				consistencyQuorumKeyspace, ss, ss, bbs);
 		q.setKey(userId);
 		q.setColumnFamily(usersColumnFamily);
 		q.setColumnNames(passwordColumn);
 
 		QueryResult<ColumnSlice<String, ByteBuffer>> result = q.execute();
 		ColumnSlice<String, ByteBuffer> cs = result.get();
 
 		if (!cs.getColumns().isEmpty()) {
 
 			String passwordFromDB = ss.fromByteBuffer(cs.getColumnByName(
 					passwordColumn).getValue());
 			Boolean isLogged = isLogged(userId);
 
 			if (isLogged) {
 				throw new UserAlreadyLoggedException();
 			} else if (password.equals(passwordFromDB)) {
 
 				Mutator<String> mutator = HFactory.createMutator(
 						consistencyQuorumKeyspace, StringSerializer.get());
 				mutator.addInsertion(userId, loginsColumnFamily, HFactory
 						.createColumn(System.currentTimeMillis(), Boolean.TRUE,
 								ls, bs));
 				mutator.execute();
 				logger.debug("login sucessful for {}, userId={}", email, userId);
 				return userId;
 			}
 		}
 		logger.debug("login failed for {}, returned columns = {}, userId={}", new Object[] {email, cs
 				.getColumns().size(), userId});
 		return null;
 	}
 
 	@Override
 	public void logout(String userId) {
 
 		long starttime = System.currentTimeMillis();
 		Mutator<String> mutator = HFactory.createMutator(
 				consistencyQuorumKeyspace, StringSerializer.get());
 		mutator.addInsertion(userId, loginsColumnFamily, HFactory.createColumn(
 				System.currentTimeMillis(), Boolean.FALSE, ls, bs));
 		mutator.execute();
 
 		logger.debug("User {} successfully logout in {} ms", userId,
 				(System.currentTimeMillis() - starttime));
 	}
 
 	@Override
 	public void flushUsers() {
 
 		long starttime = System.currentTimeMillis();
 		
 		cluster.truncate(dbKeyspace, answersColumnFamily);
 		cluster.truncate(dbKeyspace, usersColumnFamily);
 		cluster.truncate(dbKeyspace, bonusesColumnFamily);
 		cluster.truncate(dbKeyspace, ranksColumnFamily);
 		cluster.truncate(dbKeyspace, scoresColumnFamily);
 		cluster.truncate(dbKeyspace, loginsColumnFamily);
 		
 		orderedScores = Collections.<Integer> emptyList();
 		reverseOrderedScores = Collections.<Integer> emptyList();
 		
 		logger.debug("Keyspace flushed in {} ms.", (System.currentTimeMillis() - starttime));
 		
 	}
 
 	@Override
 	public void computeRankings() {
 
 		int limit = 2000;
 		String start = DEFAULT_START_KEY;
 
 		boolean oneMoreIteration = true;
 		do {
 
 			RangeSlicesQuery<String, String, ByteBuffer> rangeSliceQuery = HFactory
 					.createRangeSlicesQuery(consistencyQuorumKeyspace, ss, ss,
 							bbs);
 
 			rangeSliceQuery.setColumnFamily(usersColumnFamily);
 			rangeSliceQuery.setKeys(start, DEFAULT_START_KEY);
 			rangeSliceQuery.setRowCount(limit);
 			rangeSliceQuery.setColumnNames(emailColumn, firstnameColumn, lastnameColumn);
 			
 			QueryResult<OrderedRows<String, String, ByteBuffer>> result = rangeSliceQuery
 					.execute();
 
 			OrderedRows<String, String, ByteBuffer> rows = result.get();
 
 			logger.debug("Query fetches {} rows in {} ms", rows.getCount(),
 					result.getExecutionTimeMicro());
 
 			if (rows.getCount() < limit) {
 				oneMoreIteration = false;
 			}
 			Iterator<Row<String, String, ByteBuffer>> iterator = rows
 					.iterator();
 
 			Mutator<Integer> mutator = HFactory.createMutator(
 					consistencyQuorumKeyspace, is);
 
 			List<User> users = new ArrayList<User>(rows.getCount());
 			List<String> userIds = new ArrayList<String>(rows.getCount());
 			
 			while (iterator.hasNext()) {
 				Row<String, String, ByteBuffer> row = iterator.next();
 				if (!row.getKey().equals(start)
 						&& row.getColumnSlice().getColumnByName(emailColumn) != null) {
 					User user = toUser(row.getKey(), row.getColumnSlice());
 					users.add(user);
 					userIds.add(row.getKey());
 					start = row.getKey();
 				}
 			}
 			
 			loadScores(userIds, users);
 			
 			for (User user : users) {
 				String userKey = generateRankedUserKey(user);
 				mutator.addInsertion(user.getScore(), ranksColumnFamily,
 						HFactory.createColumn(userKey, encodeUserToString(user),
 								ss, ss));
 			}
 
 			MutationResult mutationResult = mutator.execute();
 
 			logger.debug("Mutator executed in {} ms",
 					mutationResult.getExecutionTimeMicro());
 
 		} while (oneMoreIteration);
 
 	}
 
 	private void loadScores(List<String> userIds, List<User> users) {
 		
 		final long starttime = System.currentTimeMillis();
 		
 		// load score
 		MultigetSliceQuery<String, Integer, Integer> sliceQuery = HFactory
 				.createMultigetSliceQuery(consistencyOneKeyspace, ss, is, is);
 		sliceQuery.setKeys(userIds);
 		sliceQuery.setColumnFamily(scoresColumnFamily);
 		sliceQuery.setRange(DEFAULT_MAX_QUESTIONS, 0, true, 1);			
 
 		QueryResult<Rows<String, Integer, Integer>> queryResult = sliceQuery
 				.execute();
 		Rows<String, Integer, Integer> rows = queryResult.get();
 		for (User user : users) {
 			Row<String, Integer, Integer> row = rows.getByKey(user.getUserId());
 			
 			if (row != null) {
 				ColumnSlice<Integer, Integer> columnSlice = row.getColumnSlice();
 				if (!columnSlice.getColumns().isEmpty()) {
 					user.setScore(columnSlice.getColumns().get(0).getValue());
 				} else {
 					user.setScore(0);
 				}
 			} else {
 				user.setScore(0);
 			}
 		}
 		
 		logger.debug("Loaded {} scores in {} ms", users.size(), (System.currentTimeMillis() - starttime));
 		
 	}
 	
 	private void ensureOrderedScoresLoaded() {
 
 		if (orderedScores.isEmpty()) {
 			try {
 				if (scoresComputationLock.tryLock(60, TimeUnit.SECONDS)) {
 					if (orderedScores.isEmpty()) {
 						computeOrderedScores();
 					}
 					scoresComputationLock.unlock();
 				}
 			} catch (InterruptedException e) {}
 		}
 	}
 
 	private void computeOrderedScores() {
 
 		RangeSlicesQuery<Integer, String, String> rangeSliceQuery = HFactory
 				.createRangeSlicesQuery(consistencyOneKeyspace, is, ss, ss);
 
 		rangeSliceQuery.setColumnFamily(ranksColumnFamily);
 		rangeSliceQuery.setReturnKeysOnly();
 
 		QueryResult<OrderedRows<Integer, String, String>> result = rangeSliceQuery
 				.execute();
 
 		orderedScores = new ArrayList<Integer>(result.get().getCount());
 
 		Iterator<Row<Integer, String, String>> iterator = result.get()
 				.iterator();
 
 		while (iterator.hasNext()) {
 			Row<Integer, String, String> row = iterator.next();
 			int key = row.getKey();
 			orderedScores.add(key);
 		}
 
 		Collections.sort(orderedScores);
 		reverseOrderedScores = new ArrayList<Integer>(orderedScores);
 		Collections.sort(reverseOrderedScores, Collections.reverseOrder());
 
 	}
 
 	private List<User> computeTop(int limit) {
 
 		ensureOrderedScoresLoaded();
 
 		List<User> users = findRankedUsers(limit, null, reverseOrderedScores,
 				false, DEFAULT_START_KEY);
 
 		return users;
 	}
 
 	List<User> findRankedUsers(int limit, Integer startScore,
 			List<Integer> orderedScores, boolean reverseOrder, String startKey) {
 
 		List<User> users = new ArrayList<User>(limit);
 
 		boolean scoreFound = false;
 		boolean first = true;
 		String start = startKey;
 		EXTERNALLOOP: for (Integer score : orderedScores) {
 			if (!scoreFound) {
 				if (startScore == null || score.equals(startScore)) {
 					scoreFound = true;
 				} else {
 					continue;
 				}
 			}
 
 			if (first) {
 				first = false;
 			} else {
 				start = DEFAULT_START_KEY;
 			}
 
 			SliceQuery<Integer, String, String> sliceQuery = HFactory
 					.createSliceQuery(consistencyOneKeyspace, is, ss, ss);
 
 			sliceQuery.setColumnFamily(ranksColumnFamily);
 			sliceQuery.setKey(score);
			sliceQuery.setRange(start, DEFAULT_START_KEY, reverseOrder, limit);
 			
 			QueryResult<ColumnSlice<String, String>> sliceResult = sliceQuery
 					.execute();
 			ColumnSlice<String, String> columnSlice = sliceResult.get();
 			for (HColumn<String, String> column : columnSlice.getColumns()) {
 				if (start == null || !start.equals(column.getName())) {
 					users.add(decodeStringToUser(column.getValue()));
 					if (users.size() >= limit) {
 						break EXTERNALLOOP;
 					}
 				}
 			}
 		}
 		
 		if (!scoreFound) {
 			logger.error("Heum, dude, score {} not found for user {}", startScore, startKey);
 		}
 
 		return users;
 
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		
 		consistencyOneKeyspace = HFactory.createKeyspace(dbKeyspace, cluster,
 				new AllOneConsistencyLevelPolicy());
 		consistencyQuorumKeyspace = HFactory.createKeyspace(dbKeyspace,
 				cluster, new QuorumAllConsistencyLevelPolicy());
 
 	}
 	
 	private static String encodeUserToString(final User user) {
 		return user.getUserId() + DEFAULT_FIELDS_SEPARATOR + 
 		user.getFirstname() + DEFAULT_FIELDS_SEPARATOR + 
 		user.getLastname() + DEFAULT_FIELDS_SEPARATOR + 
 		user.getEmail() + DEFAULT_FIELDS_SEPARATOR + 
 		String.valueOf(user.getScore());
 	}
 	
 	private static User decodeStringToUser(final String userString) {
 		User user = new User();
 		
 		String[] parts = userString.split(DEFAULT_FIELDS_SEPARATOR);
 		
 		if (parts.length > 0) {
 			user.setUserId(parts[0]);
 		}
 		if (parts.length > 1) {
 			user.setFirstname(parts[1]);
 		}
 		if (parts.length > 2) {
 			user.setLastname(parts[2]);
 		}
 		if (parts.length > 3) {
 			user.setEmail(parts[3]);
 		}
 		if (parts.length > 4) {
 			user.setScore(Integer.valueOf(parts[4]));
 		}
 		
 		return user;
 	}
 
 	public List<Answer> getAnswersByEmail(final String userEmail) {
 		
 		final String userId = CassandraHelper.generateUserId(userEmail);
 		
 		List<Answer> answers = getAnswers(userId);
 		
 		return answers;
 	}
 }
