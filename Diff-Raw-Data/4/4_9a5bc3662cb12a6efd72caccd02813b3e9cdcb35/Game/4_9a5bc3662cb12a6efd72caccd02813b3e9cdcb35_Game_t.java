 package com.antsapps.triples.backend;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 import android.util.Log;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.primitives.Ints;
 import com.google.common.primitives.Longs;
 
 public class Game implements Comparable<Game> {
 
   public static class CardsRemainingGameComparator implements Comparator<Game> {
 
     @Override
     public int compare(Game lhs, Game rhs) {
       return Ints.compare(lhs.getCardsRemaining(), rhs.getCardsRemaining());
     }
 
   }
 
   public static class TimeElapsedGameComparator implements Comparator<Game> {
 
     @Override
     public int compare(Game lhs, Game rhs) {
       return Longs.compare(lhs.getTimeElapsed(), rhs.getTimeElapsed());
     }
 
   }
 
   public static class DateGameComparator implements Comparator<Game> {
 
     @Override
     public int compare(Game lhs, Game rhs) {
       return lhs.mDate.compareTo(rhs.mDate);
     }
 
   }
 
   public interface OnUpdateGameStateListener {
     void onUpdateGameState(GameState state);
   }
 
   public interface OnUpdateCardsInPlayListener {
     void onUpdateCardsInPlay(ImmutableList<Card> newCards,
         ImmutableList<Card> oldCards, int numRemaining);
   }
 
   /**
    * This reflects the game state as controlled by the user. This is orthogonal
    * to that controlled by Android's activity lifecycle.
    */
   public enum GameState {
     STARTING,
     ACTIVE,
     PAUSED,
     COMPLETED;
   }
 
   public static final int MIN_CARDS_IN_PLAY = 12;
 
   public static final String ID_TAG = "game_id";
 
   private GameState mGameState;
 
   private boolean mActivitiyLifecycleActive;
 
   private final Deck mDeck;
 
   private final List<Card> mCardsInPlay;
 
   private final Timer mTimer;
 
   private final long mRandomSeed;
 
   private long id;
 
   private final Date mDate;
 
   private final List<OnUpdateGameStateListener> mGameStateListeners = Lists
       .newArrayList();
 
   private final List<OnUpdateCardsInPlayListener> mCardsInPlayListeners = Lists
       .newArrayList();
 
   public static Game createFromSeed(long seed) {
     Game game = new Game(-1, seed, Collections.<Card> emptyList(), new Deck(
         new Random(seed)), 0, new Date(), GameState.STARTING);
     game.init();
     return game;
   }
 
   Game(long id,
       long seed,
       List<Card> cardsInPlay,
       Deck cardsInDeck,
       long timeElapsed,
       Date date,
       GameState gameState) {
     this.id = id;
     mRandomSeed = seed;
     mCardsInPlay = Lists.newArrayList(cardsInPlay);
     mDeck = cardsInDeck;
     mTimer = new Timer(timeElapsed);
     mDate = date;
     mGameState = gameState;
   }
 
   public void setOnTimerTickListener(OnTimerTickListener listener) {
     mTimer.setOnTimerTickListener(listener);
   }
 
   public void addOnUpdateGameStateListener(OnUpdateGameStateListener listener) {
     mGameStateListeners.add(listener);
   }
 
   public void
       removeOnUpdateGameStateListener(OnUpdateGameStateListener listener) {
     mGameStateListeners.remove(listener);
   }
 
   public void addOnUpdateCardsInPlayListener(
       OnUpdateCardsInPlayListener listener) {
     mCardsInPlayListeners.add(listener);
   }
 
   public void removeOnUpdateCardsInPlayListener(
       OnUpdateCardsInPlayListener listener) {
     mCardsInPlayListeners.remove(listener);
   }
 
   private void init() {
     Preconditions.checkState(mCardsInPlay.isEmpty());
     // Add cards so there is at least one valid triple.
     while (mCardsInPlay.size() < MIN_CARDS_IN_PLAY || !checkIfAnyValidTriples()) {
       for (int i = 0; i < 3; i++) {
         mCardsInPlay.add(mDeck.getNextCard());
       }
     }
   }
 
   public void begin() {
     Preconditions.checkState(
         isGameInValidState(),
         "Game is not in a valid state. Game state = " + mGameState);
     dispatchCardsInPlayUpdate(
         ImmutableList.copyOf(mCardsInPlay),
         ImmutableList.<Card> of(),
         getCardsRemaining());
     dispatchGameStateUpdate();
     updateTimer();
    if (mGameState == GameState.STARTING) {
       mGameState = GameState.ACTIVE;
     }
    dispatchGameStateUpdate();
   }
 
   public void resume() {
     if (mGameState == GameState.COMPLETED) {
       return;
     }
     mGameState = GameState.ACTIVE;
     updateTimer();
     dispatchGameStateUpdate();
   }
 
   public void resumeFromLifecycle() {
     mActivitiyLifecycleActive = true;
     updateTimer();
   }
 
   public void pauseFromLifecycle() {
     mActivitiyLifecycleActive = false;
     updateTimer();
   }
 
   public void pause() {
     if (mGameState == GameState.COMPLETED) {
       return;
     }
     mGameState = GameState.PAUSED;
     updateTimer();
     dispatchGameStateUpdate();
   }
 
   private void updateTimer() {
     if ((mGameState == GameState.ACTIVE || mGameState == GameState.STARTING)
         && mActivitiyLifecycleActive) {
       mTimer.resume();
     } else {
       mTimer.pause();
     }
   }
 
   public void commitTriple(List<Card> cards) {
     commitTriple(Iterables.toArray(cards, Card.class));
   }
 
   public void commitTriple(Card... cards) {
     Preconditions.checkState(
         mGameState != GameState.COMPLETED,
         "Game is already completed.");
     ImmutableList<Card> oldCards = ImmutableList.copyOf(mCardsInPlay);
     if (!mCardsInPlay.containsAll(Lists.newArrayList(cards))) {
       throw new IllegalArgumentException("Cards are not in the set. cards = "
           + cards + ", mCardsInPlay = " + mCardsInPlay);
     }
     if (!isValidTriple(cards)) {
       throw new IllegalArgumentException("Cards are not a valid triple");
     }
 
     for (int i = 0; i < 3; i++) {
       mCardsInPlay.set(mCardsInPlay.indexOf(cards[i]), null);
     }
 
     // Add more cards up to the minimum.
     while (numNotNull(mCardsInPlay) < MIN_CARDS_IN_PLAY && !mDeck.isEmpty()) {
       for (int i = 0; i < 3; i++) {
         mCardsInPlay.set(mCardsInPlay.indexOf(null), mDeck.getNextCard());
       }
     }
 
     // Remove any null cards by replacing them with the last cards.
     int numNotNull = numNotNull(mCardsInPlay);
     for (int i = 0; i < numNotNull; i++) {
       if (mCardsInPlay.get(i) == null) {
         removeTrailingNulls(mCardsInPlay);
         if (i == mCardsInPlay.size() - 1)
           break;
         mCardsInPlay.set(i, mCardsInPlay.remove(mCardsInPlay.size() - 1));
       }
     }
     removeTrailingNulls(mCardsInPlay);
 
     // Add more cards until there is a valid triple.
     while (!checkIfAnyValidTriples() && !mDeck.isEmpty()) {
       for (int i = 0; i < 3; i++) {
         mCardsInPlay.add(mDeck.getNextCard());
       }
     }
 
     dispatchCardsInPlayUpdate(
         ImmutableList.copyOf(mCardsInPlay),
         oldCards,
         getCardsRemaining());
 
     if (!checkIfAnyValidTriples()) {
       finish();
     }
   }
 
   private void finish() {
     mGameState = GameState.COMPLETED;
     updateTimer();
     dispatchGameStateUpdate();
   }
 
   private void dispatchGameStateUpdate() {
     for (OnUpdateGameStateListener listener : mGameStateListeners) {
       listener.onUpdateGameState(mGameState);
     }
   }
 
   public static boolean isValidTriple(List<Card> cards) {
     return isValidTriple(Iterables.toArray(cards, Card.class));
   }
 
   public static boolean isValidTriple(Card... cards) {
     if (cards.length != 3 || !isDistinct(cards)) {
       throw new IllegalArgumentException();
     }
     if ((cards[0].mNumber + cards[1].mNumber + cards[2].mNumber) % 3 == 0) {
       if ((cards[0].mShape + cards[1].mShape + cards[2].mShape) % 3 == 0) {
         if ((cards[0].mPattern + cards[1].mPattern + cards[2].mPattern) % 3 == 0) {
           if ((cards[0].mColor + cards[1].mColor + cards[2].mColor) % 3 == 0) {
             return true;
           }
         }
       }
     }
     return false;
   }
 
   private static boolean isDistinct(Card... cards) {
     return (cards[0] != cards[1]) && (cards[0] != cards[2])
         && (cards[1] != cards[2]);
   }
 
   private boolean checkIfAnyValidTriples() {
     for (int i = 0; i < mCardsInPlay.size(); i++) {
       Card c0 = mCardsInPlay.get(i);
       if (c0 == null)
         continue;
       for (int j = i + 1; j < mCardsInPlay.size(); j++) {
         Card c1 = mCardsInPlay.get(j);
         if (c1 == null)
           continue;
         for (int k = j + 1; k < mCardsInPlay.size(); k++) {
           Card c2 = mCardsInPlay.get(k);
           if (c2 == null)
             continue;
           if (isValidTriple(c0, c1, c2)) {
             Log.i(
                 "Game",
                 String.format("Valid triple for positions %d %d %d", i, j, k));
             return true;
           }
         }
       }
     }
     return false;
   }
 
   private static int numNotNull(Iterable<Card> cards) {
     int countNotNull = 0;
     for (Card card : cards) {
       if (card != null)
         countNotNull++;
     }
     return countNotNull;
   }
 
   private static void removeTrailingNulls(List<Card> cards) {
     Iterator<Card> reverseIt = Lists.reverse(cards).iterator();
     while (reverseIt.hasNext()) {
       if (reverseIt.next() == null) {
         reverseIt.remove();
       } else {
         return;
       }
     }
   }
 
   private void dispatchCardsInPlayUpdate(ImmutableList<Card> newCards,
       ImmutableList<Card> oldCards, int numRemaining) {
     for (OnUpdateCardsInPlayListener listener : mCardsInPlayListeners) {
       listener.onUpdateCardsInPlay(newCards, oldCards, numRemaining);
     }
   }
 
   /**
    * A game is in a valid state if any of the following are true:
    * <ul>
    * <li>It is completed and there are no cards in the deck and no valid triples
    * on the board.
    * <li>It is not completed and there are at least {@link MIN_CARDS_IN_PLAY}
    * cards in play and at least one valid triple.
    * </ul>
    */
   private boolean isGameInValidState() {
     switch (mGameState) {
       case COMPLETED:
         return !checkIfAnyValidTriples() && mDeck.isEmpty();
       case PAUSED:
       case ACTIVE:
       case STARTING:
         return checkIfAnyValidTriples()
             && (mCardsInPlay.size() >= MIN_CARDS_IN_PLAY || mDeck.isEmpty());
       default:
         return false;
     }
   }
 
   public int getCardsRemaining() {
     return mDeck.getCardsRemaining() + mCardsInPlay.size();
   }
 
   byte[] getCardsInPlayAsByteArray() {
     return Utils.cardListToByteArray(mCardsInPlay);
   }
 
   byte[] getCardsInDeckAsByteArray() {
     return mDeck.toByteArray();
   }
 
   public long getId() {
     return id;
   }
 
   void setId(long id) {
     this.id = id;
   }
 
   @Override
   public int compareTo(Game another) {
     return (int) Utils.compareTo(mDate, id, another.mDate, another.id);
   }
 
   public long getRandomSeed() {
     return mRandomSeed;
   }
 
   public long getTimeElapsed() {
     return mTimer.getElapsed();
   }
 
   public Date getDateStarted() {
     return mDate;
   }
 
   public GameState getGameState() {
     return mGameState;
   }
 
   public boolean getActivityLifecycleActive() {
     return mActivitiyLifecycleActive;
   }
 }
