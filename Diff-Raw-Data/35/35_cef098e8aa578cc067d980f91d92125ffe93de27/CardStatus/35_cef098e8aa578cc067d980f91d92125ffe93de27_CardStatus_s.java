 package edu.uwm.cs552;
 
 import java.util.Observable;
 
 /**
  * Status cards of a certain player. Each player has a number of cards (normally
  * 3). One of these cards can be selected, and on the selected card, one goal
  * may be selected.
  */
 public class CardStatus extends Observable {
   private final GoalCard[] cards;
 
   private int selectedCard = -1;
   private int selectedGoal = -1;
 
   /**
    * Create a card status object initialized with the given array of cards. The
    * number of cards cannot change during execution. None of the cards passed in
    * may be null
    * 
    * @param cds array of cards to copy
    */
   public CardStatus(GoalCard[] cds) {
     cards = cds.clone();
     for (GoalCard c : cds) {
       if (c == null)
         throw new IllegalArgumentException("Initial cards may not be null");
     }
   }
 
   /**
    * Return the number of spaces for cards in this status object.
    * 
    * @return
    */
   public int size() {
     return cards.length;
   }
 
   /**
    * Return a particular card
    * 
    * @param i index (0-based)
    * @return card at that index
    * @throws IndexOutOfBoundsException if index not in range [0,size())
    */
   public GoalCard getCard(int i) {
     return cards[i];
   }
 
   /**
    * Indicate which card is selected and which goal on this card is selected.
    * Either index may be -1 to signify no selection at that level.
    * 
    * @param card index of selected card
    * @param goal index of selected goal on selected card
    */
   public void setSelected(int card, int goal) {
     System.out.println("setSelected(" + card + "," + goal + ")");
     if (card < -1 || card >= cards.length)
       throw new IllegalArgumentException("illegal card index");
     if (goal < -1 || goal >= GoalCard.NUM_GOALS)
       throw new IllegalArgumentException("illegal goal index");
     selectedCard = card;
     selectedGoal = goal;
   }
 
   /**
    * Return the currently selected card, or null if none selected.
    * 
    * @return currently selected card, if any
    */
   public GoalCard getSelectedCard() {
     if (selectedCard < 0)
       return null;
     return cards[selectedCard];
   }
 
   /**
    * Replace the selected card (which must not be null) with the given card.
    * Afterwards, there is no selected goal.
    * 
    * @param c new card to use in place of the selected on, must not be null.
    * @throws IllegalStateException if no selected card
    */
   public void replaceSelectedCard(GoalCard c) {
     if (selectedCard < 0)
       throw new IllegalArgumentException("no selected card to replace");
     cards[selectedCard] = c;
     selectedGoal = -1;
     setChanged();
     notifyObservers(selectedCard);
   }
 
   /**
    * Return selected goal, if any. If there is no selected card, or if on the
    * selected card there is no selected goal, the result is null.
    * 
    * @return selected goal, if any
    */
   public Goal getSelectedGoal() {
     if (selectedCard < 0 || selectedGoal < 0)
       return null;
     return getSelectedCard().getGoal(selectedGoal);
   }
 }
