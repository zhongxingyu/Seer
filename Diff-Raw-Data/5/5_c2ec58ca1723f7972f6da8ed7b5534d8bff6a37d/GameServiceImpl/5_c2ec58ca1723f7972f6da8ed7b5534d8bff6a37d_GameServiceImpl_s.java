 package cl.own.usi.service.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;
 
 import cl.own.usi.dao.GameDAO;
 import cl.own.usi.gateway.client.UserInfoAndScore;
 import cl.own.usi.gateway.client.WorkerClient;
 import cl.own.usi.gateway.netty.QuestionWorker;
 import cl.own.usi.gateway.utils.ExecutorUtil;
 import cl.own.usi.gateway.utils.ScoresHelper;
 import cl.own.usi.gateway.utils.Twitter;
 import cl.own.usi.model.Game;
 import cl.own.usi.model.Question;
 import cl.own.usi.service.GameService;
 
 /**
  * Game service implementation.
  *
  * @author bperroud
  *
  */
 @Service
 public class GameServiceImpl implements GameService {
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(GameServiceImpl.class);
 
 	@Autowired
 	private ExecutorUtil executorUtil;
 
 	@Autowired
 	private GameDAO gameDAO;
 
 	@Autowired
 	private Twitter twitter;
 
 	@Autowired
 	private WorkerClient workerClient;
 
 	private final ExecutorService executorService = Executors
 			.newSingleThreadExecutor();
 
 	private GameSynchronization gameSynchronization;
 
 	private static final int FIRST_QUESTION = 1;
 
 	private static final String TWITTER_MESSAGE = "Notre Appli supporte %d joueurs #challengeUSI2011";
 
 	private boolean twitt = false;
 
 	private AtomicReference<String> top100AsString = new AtomicReference<String>();
 
 	private final AtomicBoolean gameRunning = new AtomicBoolean(false);
 
 	@Value(value = "${frontend.twitt:false}")
 	public void setTwitt(boolean twitt) {
 		this.twitt = twitt;
 	}
 
 	public boolean insertGame(int usersLimit, int questionTimeLimit,
 			int pollingTimeLimit, int synchroTimeLimit, int numberOfQuestion,
 			List<Map<String, Map<String, Boolean>>> questions) {
 
 		if (!gameRunning.compareAndSet(false, true)) {
 			return false;
 		}
 
 		resetPreviousGame();
 
 		Game game = gameDAO.insertGame(usersLimit, questionTimeLimit,
 				pollingTimeLimit, synchroTimeLimit, numberOfQuestion,
 				mapToQuestion(questions));
 
 		gameSynchronization = new GameSynchronization(game);
 
 		executorService.execute(new GameFlowWorker(gameSynchronization));
 
 		return true;
 	}
 
 	@Override
 	public Game getGame() {
 		return gameDAO.getGame();
 	}
 
 	private void resetPreviousGame() {
 		GameSynchronization oldGameSynchronization = gameSynchronization;
 		if (oldGameSynchronization != null) {
 			oldGameSynchronization.currentQuestionToAnswer = 0;
 			for (Map.Entry<Question, QuestionSynchronization> entry : oldGameSynchronization.questionSynchronizations
 					.entrySet()) {
 				QuestionSynchronization questionSynchronization = entry
 						.getValue();
 				questionSynchronization.questionReadyLatch.countDown();
 			}
 		}
 	}
 
 	private List<Question> mapToQuestion(
 			List<Map<String, Map<String, Boolean>>> questions) {
 		List<Question> list = new ArrayList<Question>();
 		int number = FIRST_QUESTION;
 		for (Map<String, Map<String, Boolean>> element : questions) {
 			for (Map.Entry<String, Map<String, Boolean>> entry : element
 					.entrySet()) {
 				Question question = new Question();
 				question.setNumber(number);
 				question.setLabel(entry.getKey());
 				question.setChoices(new ArrayList<String>(entry.getValue()
 						.size()));
 				int value = 1;
 				if (number > 5 && number <= 10) {
 					value = 5;
 				} else if (number > 10 && number <= 15) {
 					value = 10;
 				} else if (number > 15) {
 					value = 15;
 				}
 				question.setValue(value);
 				int i = 1;
 				for (Map.Entry<String, Boolean> answer : entry.getValue()
 						.entrySet()) {
 					question.getChoices().add(answer.getKey());
 					if (answer.getValue()) {
 						question.setCorrectChoice(i);
 					}
 					i++;
 				}
 				list.add(question);
 				number++;
 			}
 		}
 		return list;
 	}
 
 	@Override
 	public Question getQuestion(int questionNumber) {
 		return gameDAO.getQuestion(questionNumber);
 	}
 
 	private QuestionSynchronization getQuestionSync(int questionNumber) {
 		return gameSynchronization.getQuestionSynchronization(questionNumber);
 	}
 
 	@Override
 	public boolean waitOtherUsers(int questionNumber)
 			throws InterruptedException {
 		QuestionSynchronization questionSync = getQuestionSync(questionNumber);
 		if (questionSync == null) {
 			return false;
 		} else {
 			int timeToWait = gameDAO.getGame().getQuestionTimeLimit()
 					+ gameDAO.getGame().getSynchroTimeLimit();
 			if (questionNumber == 1) {
 				timeToWait = gameDAO.getGame().getPollingTimeLimit();
 			}
 			boolean enter = questionSync.questionReadyLatch.await(
 					timeToWait + 5, TimeUnit.SECONDS);
 			return enter && gameSynchronization.currentQuestionRunning;
 		}
 	}
 
 	@Override
 	public boolean enterGame(String userId) {
 		if (gameSynchronization != null) {
 			gameSynchronization.waitForFirstLogin.countDown();
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private class GameFlowWorker implements Runnable {
 
 		private final GameSynchronization gameSynchronization;
 
 		public GameFlowWorker(GameSynchronization gameSynchronization) {
 			this.gameSynchronization = gameSynchronization;
 		}
 
 		@Override
 		public void run() {
 
 			try {
 				LOGGER.debug("Start game");
 
 				try {
 					LOGGER.debug("Wait for first login");
 					gameSynchronization.waitForFirstLogin.await();
 					LOGGER.debug("First user have joined the game");
 				} catch (InterruptedException e) {
 					LOGGER.warn("Interrupted", e);
 					return;
 				}
 
 				try {
 					LOGGER.debug("Wait on all users login and requesting the first question.");
 					boolean awaited = gameSynchronization.enoughUsersLatch
 							.await(gameSynchronization.game
 									.getPollingTimeLimit(), TimeUnit.SECONDS);
 					if (awaited) {
 						LOGGER.debug("Enough users have joined the game and requested the first question.");
 					} else {
 						LOGGER.debug("Waiting time is ellapsed, starting anyway.");
 					}
 				} catch (InterruptedException e) {
 					LOGGER.warn("Interrupted", e);
 					return;
 				}
 
 				for (int i = FIRST_QUESTION; i <= gameSynchronization.game
 						.getNumberOfQuestion(); i++) {
 
 					gameSynchronization.currentQuestionToRequest = i + 1;
 
 					LOGGER.info(
 							"Starting question {}. Response question number {}",
 							i, gameSynchronization.currentQuestionToAnswer);
 					QuestionSynchronization questionSynchronization = gameSynchronization
 							.getQuestionSynchronization(i);
 
 					gameSynchronization.currentQuestionRunning = true;
 
 					// Send questions to the users
 					questionSynchronization.questionReadyLatch.countDown();
 
 					try {
 						LOGGER.debug(
 								"Wait to all users answer, or till the timeout {}",
 								gameSynchronization.game.getQuestionTimeLimit());
 
 						// Wait the quest time limit
 						LOGGER.debug("Question wait time ...");
 						Thread.sleep(TimeUnit.SECONDS
 								.toMillis(gameSynchronization.game
 										.getQuestionTimeLimit()));
 						LOGGER.debug("Question wait time ... done");
 
 						if (i < gameSynchronization.game.getNumberOfQuestion()) {
 							LOGGER.debug("Synchrotime ...");
 
 							long starttime = System.currentTimeMillis();
 
 							questionSynchronization.lock.lock();
 							gameSynchronization.currentQuestionToAnswer++;
 							for (Runnable r : questionSynchronization.waitingQueue) {
 								LOGGER.debug("Inserting a early requester to the working queue");
 								executorUtil.getExecutorService().execute(r);
 							}
 							questionSynchronization.lock.unlock();
 
 							long stoptime = System.currentTimeMillis();
 
 							long synchrotime = TimeUnit.SECONDS
 									.toMillis(gameSynchronization.game
 											.getSynchroTimeLimit())
 									+ starttime - stoptime;
 							if (synchrotime > 0) {
 								// mmmh, weird specs...
 								Thread.sleep(synchrotime);
 							}
 
 							LOGGER.debug("Synchrotime done");
 						}
 
 					} catch (InterruptedException e) {
 						LOGGER.warn("Interrupted", e);
 						return;
 					}
 
 					LOGGER.info(
 							"Question {} finished, going to the next question",
 							i);
 
 				}
 
 				LOGGER.info("All questions finished. Doing some processing, latest synchrotime, and request for ranking will be allowed");
 
 				LOGGER.debug("Latest synchrotime ...");
 
 				long starttime = System.currentTimeMillis();
 
 				gameSynchronization.currentQuestionToAnswer++;
 
 				workerClient.startRankingsComputation();
 
 				List<UserInfoAndScore> top100 = workerClient.getTop100();
 				StringBuilder sb = new StringBuilder();
 				ScoresHelper.appendUsersScores(top100, sb);
 				top100AsString.set(sb.toString());
 
 				long stoptime = System.currentTimeMillis();
 
 				LOGGER.info(
 						"Ranking computation and top100 query done in {} ms. Returns {} UserInfoAndScores.",
 						(stoptime - starttime), top100.size());
 
 				long synchrotime = TimeUnit.SECONDS
 						.toMillis(gameSynchronization.game
 								.getSynchroTimeLimit())
 						+ starttime - stoptime;
 				if (synchrotime > 0) {
 					try {
 						// mmmh, weird specs again...
 						Thread.sleep(synchrotime);
 					} catch (InterruptedException e) {
 						LOGGER.warn("Interrupted", e);
 						return;
 					}
 				}
 
 				LOGGER.debug("Latest synchrotime done");
 
 				gameSynchronization.rankingRequestAllowed = true;
 
 				LOGGER.info("Ranking requests are now allowed, tweet and clean everything");
 
 				if (twitt) {
 					twitter.twitt(String.format(TWITTER_MESSAGE,
 							gameSynchronization.game.getUsersLimit()));
 				}
 			} finally {
 				gameRunning.set(false);
 			}
 		}
 	}
 
 	@Override
 	public boolean validateQuestionToAnswer(int questionNumber) {
 		if (gameSynchronization == null) {
 			return false;
 		} else {
 			return questionNumber == gameSynchronization.currentQuestionToAnswer
 					&& gameSynchronization.currentQuestionRunning;
 		}
 	}
 
 	@Override
 	public boolean validateQuestionToRequest(int questionNumber) {
 		if (gameSynchronization == null) {
 			return false;
 		} else {
 			boolean questionValid = questionNumber == gameSynchronization.currentQuestionToRequest;
 			if (questionValid
 					&& gameSynchronization.currentQuestionToRequest == FIRST_QUESTION) {
 				gameSynchronization.enoughUsersLatch.countDown();
 			}
 			return questionValid;
 		}
 	}
 
 	private static class GameSynchronization {
 
 		private final CountDownLatch waitForFirstLogin;
 
 		private final CountDownLatch enoughUsersLatch;
 
 		private final Map<Question, QuestionSynchronization> questionSynchronizations;
 
 		private volatile int currentQuestionToRequest = 1;
 		private volatile int currentQuestionToAnswer = 1;
 		private volatile boolean currentQuestionRunning = false;
 		private volatile boolean rankingRequestAllowed = false;
 
 		private final Game game;
 
 		public GameSynchronization(Game game) {
 
 			this.game = game;
 
 			questionSynchronizations = new HashMap<Question, QuestionSynchronization>(
 					game.getQuestions().size());
 
 			for (Question question : game.getQuestions()) {
 				questionSynchronizations.put(question,
						new QuestionSynchronization(game.getUsersLimit()));
 			}
 
 			enoughUsersLatch = new CountDownLatch(game.getUsersLimit());
 			waitForFirstLogin = new CountDownLatch(1);
 		}
 
 		QuestionSynchronization getQuestionSynchronization(int questionNumber) {
 			return questionSynchronizations.get(game.getQuestions().get(
 					questionNumber - 1));
 		}
 
 	}
 
 	private static class QuestionSynchronization {
 
 		private final CountDownLatch questionReadyLatch;
 
 		private final Queue<Runnable> waitingQueue = new LinkedBlockingQueue<Runnable>();
 		private final Lock lock = new ReentrantLock();
 
		public QuestionSynchronization(int userLimit) {
 			questionReadyLatch = new CountDownLatch(1);
 		}
 
 	}
 
 	@Override
 	public boolean userAnswer(int questionNumber) {
 		// void.
 		return true;
 	}
 
 	public void scheduleQuestionReply(QuestionWorker questionWorker) {
 
 		if (questionWorker.getQuestionNumber() <= gameSynchronization.currentQuestionToRequest) {
 			executorUtil.getExecutorService().execute(questionWorker);
 		} else {
 			QuestionSynchronization questionSynchronization = getQuestionSync(questionWorker
 					.getQuestionNumber());
 
 			questionSynchronization.lock.lock();
 
 			// double check
 			if (questionWorker.getQuestionNumber() <= gameSynchronization.currentQuestionToRequest) {
 				executorUtil.getExecutorService().execute(questionWorker);
 			} else {
 				LOGGER.debug(
 						"Too early question request for question {}, putting in a temporaray queue",
 						questionWorker.getQuestionNumber());
 				questionSynchronization.waitingQueue.offer(questionWorker);
 			}
 
 			questionSynchronization.lock.unlock();
 
 		}
 	}
 
 	private Integer validateAnswer(int questionNumber, Integer answer) {
 		Question question = getQuestion(questionNumber);
 
 		if (question == null || answer == null) {
 			return null;
 		} else {
 
 			if (answer < 1 || answer > question.getChoices().size()) {
 				return null;
 			} else {
 				return answer;
 			}
 		}
 	}
 
 	@Override
 	public boolean isAnswerCorrect(final int questionNumber,
 			final Integer answer) {
 
 		final Integer validatedAnswer = validateAnswer(questionNumber, answer);
 
 		if (validatedAnswer == null) {
 			return false;
 		} else {
 			Question question = getQuestion(questionNumber);
 			if (question == null) {
 				return false;
 			} else {
 				return question.getCorrectChoice() == validatedAnswer;
 			}
 		}
 	}
 
 	@Override
 	public boolean isRankingRequestAllowed() {
 		return gameSynchronization != null
 				&& gameSynchronization.rankingRequestAllowed;
 	}
 
 	@Override
 	public String getTop100AsString() {
 		return top100AsString.get();
 	}
 
 }
