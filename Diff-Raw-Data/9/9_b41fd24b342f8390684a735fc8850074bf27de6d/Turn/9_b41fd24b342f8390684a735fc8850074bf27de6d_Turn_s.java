 package napoleon.model.rule;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import napoleon.model.card.Card;
 import napoleon.model.card.Suit;
 import napoleon.model.player.Player;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 
 public class Turn {
 	private HashMap<Player, Card> cardHash = new LinkedHashMap<Player, Card>();
 	private int no;
 	private boolean ignoreSpecial;
 	public final Suit trump;
 	
 	private Turn(int no, Suit trump) {
 		super();
 		this.trump = trump;
 		if(no == 1 || no == 12) {
 			ignoreSpecial = true;
 		}
 		this.no = no;
 	}
 	
 	public int getNo() {
 		return no;
 	}
 
 	public static Turn New(int no, Suit trump) {
 		if(no < 1 || 12 < no) throw new IllegalArgumentException("^[ԍ1ȏ12ȉłKv܂");
 		
 		return new Turn(no, trump);
 	}
 
 	public boolean isLeadSuitDefined() {
 		return 0 < cardHash.size();
 	}
 
 	public void addCard(Player player, Card card) {
 		cardHash.put(player,  card);
 	}
 
 	public Suit getLeadSuit() {
 		return !isLeadSuitDefined() ? null
 				: getFirstCard() == Card.Jorker ? getLeadSuitWhenJorkerFirst()
 				: getFirstCard().getSuit();
 	}
 
 	private Suit getLeadSuitWhenJorkerFirst() {
 		return ignoreSpecial ? (
				2 <= cardHash.size() ? getCardAt(2).getSuit() : null)
 		: trump;
 	}
 
 	private Card getFirstCard() {
 		return getCardAt(0);
 	}
 
 	private Card getCardAt(int index) {
 		return (Card) CollectionUtils.get(cardHash.values(), index);
 	}
 
 	public Player getWinner() {
 		return whoOpened(getWinnerCard());
 	}
 
 	private Player whoOpened(Card winnerCard) {
 		for(Map.Entry<Player, Card> entry : cardHash.entrySet()) {
 			if (entry.getValue() == winnerCard)
 				return entry.getKey();
 		}
 		return null;
 	}
 
 	public Card getWinnerCard() {
 		if(Table._PLAYERS_COUNT != cardHash.size())
 			throw new IllegalStateException("SJ[hoĂ܂");
 		List<Card> list = new ArrayList<Card>(cardHash.values());
 		if(ignoreSpecial) {
 			Collections.sort(list, Card.GetCardIgnoreSpecialComparator(getLeadSuit()));
 		}else {
 			Collections.sort(list, Card.getCardNormalComparator(getTrump(), getLeadSuit(), cardHash.values()));
 		}
 //		System.out.println(list.get(getMaxCardIndex()));
 		return list.get(getMaxCardIndex());
 	}
 
 	public Suit getTrump() {
 		return trump;
 	}
 
 	private int getMaxCardIndex() {
 		return cardHash.size() - 1;
 	}
 
 	public TurnStatus getStatus() {
 		return cardHash.isEmpty() ? TurnStatus.HasNotYetBegan
 				: cardHash.size() < Table._PLAYERS_COUNT ? TurnStatus.Processing
 				: TurnStatus.Finished;
 	}
 
 	public void winnerGainCards() {
 		getWinner().takeCards(getPictureCards());
 	}
 
 	@SuppressWarnings("unchecked")
 	private Collection<? extends Card> getPictureCards() {
 		return CollectionUtils.select(cardHash.values(), new Predicate() {
 			
 			@Override
 			public boolean evaluate(Object arg0) {
 				return isPictureCard((Card) arg0);
 			}
 		});
 	}
 
 	protected boolean isPictureCard(Card card) {
 		return card == null ? false
 				: card.getNumber() == 1 ? true
 				: 10 <= card.getNumber() ? true
 				: false;
 	}
 
 	public boolean isJorkerOpenedFirst() {
 		return isOpenedFirst(Card.Jorker);
 	}
 
 	public boolean isRequireJorkerOpenedFirst() {
 		return isOpenedFirst(Card.RequireJorker);
 	}
 
 	private boolean isOpenedFirst(Card cardToAssert) {
 		return !ignoreSpecial && 0 < cardHash.size() && getFirstCard() == cardToAssert;
 	}
 	
 }
