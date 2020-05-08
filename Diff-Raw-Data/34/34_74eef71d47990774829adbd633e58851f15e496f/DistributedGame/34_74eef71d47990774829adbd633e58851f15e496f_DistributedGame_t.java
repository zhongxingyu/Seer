 package fr.xebia.usiquizz.core.game.gemfire;
 
 import static fr.xebia.usiquizz.core.persistence.GemfireRepository.*;
 
 import com.gemstone.gemfire.cache.CacheListener;
 import com.usi.Question;
 import com.usi.Sessiontype;
 import fr.xebia.usiquizz.core.game.Game;
 import fr.xebia.usiquizz.core.game.QuestionLongpollingCallback;
 import fr.xebia.usiquizz.core.game.Scoring;
 import fr.xebia.usiquizz.core.game.exception.LoginPhaseEndedException;
 import fr.xebia.usiquizz.core.persistence.GemfireRepository;
 import fr.xebia.usiquizz.core.persistence.Joueur;
 import fr.xebia.usiquizz.core.sort.LocalBTree;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.concurrent.*;
 
 public class DistributedGame implements Game {
 
     private static final Logger logger = LoggerFactory.getLogger(DistributedGame.class);
 
     // Etat du login
     private static final byte LOGIN_PHASE_NON_COMMENCER = 21;
     private static final byte LOGIN_PHASE_EN_COURS = 22;
     private static final byte LOGIN_PHASE_TERMINER = 23;
 
     private ScheduledExecutorService scheduleExecutor = Executors.newScheduledThreadPool(4, new ThreadFactory() {
 
         private int i = 1;
 
         @Override
         public Thread newThread(Runnable r) {
             Thread thread = new Thread(r);
             thread.setName("Game schedule ThreadFactory " + i++);
             return thread;
         }
     });
 
     private ExecutorService eventTaskExector = Executors.newCachedThreadPool(new ThreadFactory() {
 
         private int i = 1;
 
         @Override
         public Thread newThread(Runnable r) {
             Thread thread = new Thread(r);
             thread.setName("Event task executor " + i++);
             return thread;
         }
     });
 
 
     private GemfireRepository gemfireRepository;
     private QuestionLongpollingCallback longpollingCallback;
     private Scoring scoring;
     private boolean firstForQuestion = true;
     private boolean firstLogin = true;
     private JsonQuestionWriter jsonQuestionWriter = new JsonQuestionWriter();
     private LocalBTree<Joueur> bTree;
 
     public DistributedGame(GemfireRepository gemfireRepository, Scoring scoring) {
         this.gemfireRepository = gemfireRepository;
         this.scoring = scoring;
         gemfireRepository.initQuestionStatusRegion(new QuestionStatusCacheListener(this, eventTaskExector));
         gemfireRepository.initCurrentQuestionRegion();
         gemfireRepository.initLoginRegion(new LoginCacheListener(this, eventTaskExector));
         this.bTree = new LocalBTree<Joueur>();
         gemfireRepository.initFinalScoreRegion(new ScoreCacheListener(this.bTree));
             // TODO : Instantiate it here for now but thats a shame
         scoring.setTree(this.bTree);
 
     }
 
     @Override
     public void init(Sessiontype st) {
         // Parametre du jeu
         gemfireRepository.getGameRegion().put(LOGIN_TIMEOUT, st.getParameters().getLogintimeout());
         gemfireRepository.getGameRegion().put(SYNCHROTIME, st.getParameters().getSynchrotime());
         gemfireRepository.getGameRegion().put(NB_USERS_THRESOLD, st.getParameters().getNbusersthreshold());
         gemfireRepository.getGameRegion().put(QUESTION_TIME_FRAME, st.getParameters().getQuestiontimeframe());
         gemfireRepository.getGameRegion().put(NB_QUESTIONS, st.getParameters().getNbquestions());
         gemfireRepository.getGameRegion().put(FLUSH_USER_TABLE, st.getParameters().isFlushusertable());
         gemfireRepository.getGameRegion().put(TRACKED_USER_IDMAIL, st.getParameters().getTrackeduseridmail());
         gemfireRepository.getGameRegion().put(CURRENT_QUESTION_INDEX, "1");
         gemfireRepository.getGameRegion().put(CURRENT_ANSWER_INDEX, "1");
 
         // Status du jeux
         gemfireRepository.getGameRegion().put(LOGIN_PHASE_STATUS, LOGIN_PHASE_NON_COMMENCER);
 
         // les questions
         gemfireRepository.getQuestionRegion().put(QUESTION_LIST, st.getQuestions());
 
         // Les status de chaque question (non jouée, en cours, jouée)
         for (byte currentIndex = 1; currentIndex <= st.getParameters().getNbquestions(); currentIndex++) {
             gemfireRepository.getQuestionStatusRegion().put(Byte.toString(currentIndex), QuestionStatus.QUESTION_NON_JOUEE);
         }
 
        longpollingCallback.reset();
         // Clear all caches used in the party 
         bTree.clear();
         gemfireRepository.clearGameCaches();
             // Flush user Table when requested
         if (st.getParameters().isFlushusertable()){
             gemfireRepository.getUserRegion().clear();
         }
 
     }
 
     @Override
     public int getLoginTimeout() {
         return ((Integer) gemfireRepository.getGameRegion().get(LOGIN_TIMEOUT)).intValue();
     }
 
     @Override
     public int getNbusersthresold() {
         return ((Integer) gemfireRepository.getGameRegion().get(NB_USERS_THRESOLD)).intValue();
     }
 
     @Override
     public int getQuestiontimeframe() {
         return ((Integer) gemfireRepository.getGameRegion().get(QUESTION_TIME_FRAME)).intValue();
     }
 
     @Override
     public int getNbquestions() {
         return ((Integer) gemfireRepository.getGameRegion().get(NB_QUESTIONS)).intValue();
     }
 
     @Override
     public boolean getFlushusertable() {
         return ((Boolean) gemfireRepository.getGameRegion().get(FLUSH_USER_TABLE)).booleanValue();
     }
 
     @Override
     public String getTrackeduseridmail() {
         return ((String) gemfireRepository.getGameRegion().get(TRACKED_USER_IDMAIL));
     }
 
     @Override
     public int getSynchrotime() {
         return ((Integer) gemfireRepository.getGameRegion().get(SYNCHROTIME));
     }
 
     @Override
     public void logPlayerToApplication(String sessionId, String email) throws LoginPhaseEndedException {
         // Si c'est le premier joueur, alors on démarre un timer
         // FIXME Le status doit bien être synchrone entre tous les serveurs.. Bien verifier la conf de cette région
         if ((Byte) gemfireRepository.getGameRegion().get(LOGIN_PHASE_STATUS) == LOGIN_PHASE_TERMINER) {
             throw new LoginPhaseEndedException();
         } else {
             if ((Byte) gemfireRepository.getGameRegion().get(LOGIN_PHASE_STATUS) == LOGIN_PHASE_NON_COMMENCER) {
                 synchronized (this) {
                     if ((Byte) gemfireRepository.getGameRegion().get(LOGIN_PHASE_STATUS) == LOGIN_PHASE_NON_COMMENCER) {
                         logger.info("Start timers login timeout");
                         gemfireRepository.getGameRegion().put(LOGIN_PHASE_STATUS, LOGIN_PHASE_EN_COURS);
                         scheduleExecutor.schedule((new Runnable() {
                             @Override
                             public void run() {
                                 logger.info("Login timer ended");
                                 gemfireRepository.getGameRegion().put(LOGIN_PHASE_STATUS, LOGIN_PHASE_TERMINER);
                                 logger.info("{} player logged for game", gemfireRepository.getPlayerRegion().size());
 
                                 startGame();
                             }
                         }), getLoginTimeout(), TimeUnit.MILLISECONDS);
                     }
                 }
             }
         }
         gemfireRepository.getPlayerRegion().put(sessionId, email);
     }
 
     @Override
     public void startGame() {
         // On change le status de la première question.
         // Un listener permet, si nécessaire (pas tous les joueurs de loggué) de démarrer l'envoie de la premiere question
         // Si la question n'est pas encore en cours on la passe en cours. (cas de tous les joueurs loggué avant la fin du timer)
         logger.warn("Start game");
         if (gemfireRepository.getQuestionStatusRegion().get(gemfireRepository.getGameRegion().get(CURRENT_QUESTION_INDEX)).intValue() == QuestionStatus.QUESTION_NON_JOUEE) {
             gemfireRepository.getQuestionStatusRegion().put((String) gemfireRepository.getGameRegion().get(CURRENT_QUESTION_INDEX), QuestionStatus.QUESTION_EN_COURS);
         }
     }
 
     @Override
     public boolean isAllPlayerLogged() {
         return gemfireRepository.getPlayerRegion().size() == ((Integer) gemfireRepository.getGameRegion().get(NB_USERS_THRESOLD)).intValue();
     }
 
     @Override
     public boolean isAlreadyLogged(String sessionKey) {
         return gemfireRepository.getPlayerRegion().containsKey(sessionKey);
     }
 
     @Override
     public String getEmailFromSession(String sessionKey) {
         return gemfireRepository.getPlayerRegion().get(sessionKey);
     }
 
     @Override
     public Collection<String> listPlayer() {
         return gemfireRepository.getPlayerRegion().values();
     }
 
     @Override
     public int countUserConnected() {
         return gemfireRepository.getPlayerRegion().size();
     }
 
     @Override
     public void addPlayerForQuestion(final String sessionId, final String questionIndex) {
         gemfireRepository.writeAsyncPlayerForQuestion(sessionId);
     }
 
     @Override
     public int countUserForQuestion(String questionIndex) {
         return gemfireRepository.getCurrentQuestionRegion().size();
     }
 
     @Override
     public Question getQuestion(int index) {
         // -1 difference between spec and list implementation
         return gemfireRepository.getQuestionRegion().get(QUESTION_LIST).getQuestion().get(index - 1);
     }
 
     @Override
     public String getCurrentQuestionIndex() {
         return (String) gemfireRepository.getGameRegion().get(CURRENT_QUESTION_INDEX);
     }
 
     @Override
     public String getCurrentAnswerIndex() {
         return (String) gemfireRepository.getGameRegion().get(CURRENT_ANSWER_INDEX);
     }
 
     @Override
     public void setCurrentQuestionIndex(String newIndex) {
         gemfireRepository.getGameRegion().put(CURRENT_QUESTION_INDEX, newIndex);
     }
 
     @Override
     public void setCurrentAnswerIndex(String newIndex) {
         gemfireRepository.getGameRegion().put(CURRENT_ANSWER_INDEX, newIndex);
     }
 
     @Override
     public void registerLongpollingCallback(QuestionLongpollingCallback callback) {
         this.longpollingCallback = callback;
     }
 
     @Override
     public boolean isPlayerCanAnswer(String sessionKey, String currentQuestion) {
         // L'index de la question doit correspondre au reponse que l'on attend
         if (!currentQuestion.equals(getCurrentAnswerIndex())) {
             logger.info("Player {} outside windows answer of question {} current answer {}", new Object[]{sessionKey, currentQuestion, getCurrentAnswerIndex()});
             return false;
         }
 
         // On doit être encore dans la bonne timeframe
         // Pour cela on regarde le statut de la question courante.
         // Elle doit être de en QUESTION_EN_COURS
         if (gemfireRepository.getQuestionStatusRegion().get(currentQuestion) != QuestionStatus.QUESTION_EN_COURS) {
             logger.info("Player {} outside windows answer of question {} current statut {} (should be 11)", new Object[]{sessionKey, currentQuestion, gemfireRepository.getQuestionStatusRegion().get(currentQuestion)});
             return false;
         }
 
         // Le joueur ne doit pas déja avoir répondu
         // FIXME A voir si reèlement obligatoire
         return true;
     }
 
     @Override
     public boolean isPlayerCanAskQuestion(String sessionKey, String questionNbr) {
         // L'index de la question doit être la question courante.
         if (!questionNbr.equals(getCurrentQuestionIndex())) {
             return false;
         }
 
         // On doit être encore dans la bonne timeframe
         // FIXME implements...
 
         // Le joueur ne doit pas déja avoir demandé la question
         return !gemfireRepository.getCurrentQuestionRegion().containsKey(sessionKey);
 
 
     }
 
     @Override
     public void startQuestionTimeframe(final byte currentQuestionIndex) {
         logger.info("Start question timeframe for question {}", currentQuestionIndex);
         longpollingCallback.initNewQuestion((byte) (currentQuestionIndex + 1));
 
         scheduleExecutor.schedule((new Runnable() {
             @Override
             public void run() {
                 logger.info("end question timeframe for question {}", currentQuestionIndex);
 
                 // Si c'est la dernière question fin du jeu
                 if (currentQuestionIndex >= getNbquestions()) {
                     // End of game
                     logger.info("END OF GAME");
                     // Create final ranking only when we own the score lock
                     scoring.calculRanking();
                 } else {
                     // Sinon On déclenche le synchrotime...
                     startSynchroTime();
                 }
             }
         }), getQuestiontimeframe(), TimeUnit.MILLISECONDS);
     }
 
     @Override
     public void resetPlayerAskedQuestion() {
         gemfireRepository.getCurrentQuestionRegion().clear();
     }
 
     @Override
     public ChannelBuffer createQuestionInJson(byte currentQuestionIndex, String session_key) {
         return jsonQuestionWriter.createQuestionInJson(currentQuestionIndex, gemfireRepository.getScoreRegion().get(session_key).getCurrentScore());
 
     }
 
 
     private void startSynchroTime() {
         final String currentAnswerIndex = getCurrentAnswerIndex();
         final String currentQuestionIndex = getCurrentQuestionIndex();
         logger.info("fin REPONSE {}", currentAnswerIndex);
         gemfireRepository.getQuestionStatusRegion().put(currentAnswerIndex, QuestionStatus.QUESTION_JOUEE);
         logger.info("Start synchro time");
 
         scheduleExecutor.schedule((new Runnable() {
             @Override
             public void run() {
                 logger.info("end synchro time");
                 // Fin de la fenetre de reponse de la question prec
                 // On met le status de la question en QUESTION_JOUEE
 
                 // On change le statut de la question
                 gemfireRepository.getQuestionStatusRegion().put(currentQuestionIndex, QuestionStatus.QUESTION_EN_COURS);
             }
         }), getSynchrotime(), TimeUnit.MILLISECONDS);
     }
 
     public void startCurrentLongPolling() {
         longpollingCallback.startSendAll();
     }
 
     public byte[] getGoodAnswers() {
         return (byte[]) gemfireRepository.getGameRegion().get(GOOD_RESPONSE);
     }
 
 
     private class JsonQuestionWriter {
 
         private final byte[] END_BA = "}".getBytes();
 
         private Map<Byte, byte[]> questionCache = new ConcurrentHashMap<Byte, byte[]>();
 
         public ChannelBuffer createQuestionInJson(byte currentQuestionIndex, byte currentScore) {
             byte[] questionBa = questionCache.get(currentQuestionIndex);
             if (questionBa == null) {
                 synchronized (this) {
                     questionBa = questionCache.get(currentQuestionIndex);
                     if (questionBa == null) {
                         // construct question
                         Question question = getQuestion(currentQuestionIndex);
                         StringBuffer sb = new StringBuffer();
                         sb.append("{\"question\":\"");
                         sb.append(question.getLabel());
                         sb.append("\",\"answer_1\":\"");
                         sb.append(question.getChoice().get(0));
                         sb.append("\",\"answer_2\":\"");
                         sb.append(question.getChoice().get(1));
                         sb.append("\",\"answer_3\":\"");
                         sb.append(question.getChoice().get(2));
                         sb.append("\",\"answer_4\":\"");
                         sb.append(question.getChoice().get(3));
                         sb.append("\",\"score\":");
                         questionBa = sb.toString().getBytes();
                         questionCache.put(currentQuestionIndex, questionBa);
                     }
                 }
             }
             return ChannelBuffers.wrappedBuffer(questionBa, Byte.toString(currentScore).getBytes(), END_BA);
         }
 
     }
 }
