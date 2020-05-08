 package org.jdominion;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jdominion.effects.CardEffect;
 import org.jdominion.event.CalculatingCardCost;
 
 public abstract class Card implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public enum Type {
 		ACTION, ATTACK, CURSE, DURATION, PRIZE, REACTION, TREASURE, VICTORY
 	}
 
 	private final int cost;
 	private List<CardEffect> effects = new ArrayList<CardEffect>();
 	private final String name;
 	private Player owner;
 	private List<Type> types = new ArrayList<Type>();
 	private boolean isKingdomCard = true;
 	private boolean effectsDependOnEachOther = false;
 	private final int cardID;
 
 	private static int maxCardID = 0;
 
 	public int getCost() {
 		return CalculatingCardCost.calculateCost(this, cost);
 	}
 
 	public List<CardEffect> getEffects() {
 		return effects;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public Player getOwner() {
 		return owner;
 	}
 
 	public void setOwner(Player owner) {
 		this.owner = owner;
 	}
 
 	public List<Type> getTypes() {
 		return types;
 	}
 
 	public Card(String name, int cost) {
 		this.cost = cost;
 		this.name = name;
 		this.cardID = ++maxCardID;
 	}
 
 	public Card(String name, CardEffect effect, int cost) {
 		this(name, cost);
 		addCardEffect(effect);
 	}
 
 	public Card(String name, CardEffect effect, int cost, boolean isKingdomCard) {
 		this(name, cost);
 		this.isKingdomCard = isKingdomCard;
 		addCardEffect(effect);
 	}
 
 	public Card(String name, int cost, boolean isKingdomCard) {
 		this(name, cost);
 		this.isKingdomCard = isKingdomCard;
 	}
 
 	public Card(String name, List<CardEffect> effects, int cost) {
 		this(name, cost);
 		for (CardEffect effect : effects) {
 			addCardEffect(effect);
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + cardID;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Card other = (Card) obj;
 		if (cardID != other.cardID)
 			return false;
 		return true;
 	}
 
 	protected void addCardEffect(CardEffect effect) {
 		effects.add(effect);
 		effect.setCard(this);
 		if (effect.getType() != null && !types.contains(effect.getType())) {
 			types.add(effect.getType());
 			if (effect.getType() == Type.ATTACK && !types.contains(Type.ACTION)) {
 				types.add(Type.ACTION);
 			}
 		}
 	}
 
	public boolean hasEffect(Class<? extends CardEffect> effectToLookFor) {
		for (CardEffect effect : getEffects()) {
			if (effect.getClass().equals(effectToLookFor)) {
				return true;
			}
		}
		return false;
	}

	public CardEffect getEffect(Class<? extends CardEffect> effectToLookFor) {
		for (CardEffect effect : getEffects()) {
			if (effect.getClass().equals(effectToLookFor)) {
				return effect;
			}
		}
		return null;
	}

 	public boolean isOfType(Type type) {
 		return this.types.contains(type);
 	}
 
 	protected void setKingdomCard(boolean isKingdomCard) {
 		this.isKingdomCard = isKingdomCard;
 	}
 
 	public boolean isKingdomCard() {
 		return isKingdomCard;
 	}
 
 	protected void setEffectsDependOnEachOther(boolean effectsDependOnEachOther) {
 		this.effectsDependOnEachOther = effectsDependOnEachOther;
 	}
 
 	public boolean effectsDependOnEachOther() {
 		return effectsDependOnEachOther;
 	}
 
 	public void play(Player activePlayer, Turn currentTurn, Supply supply) {
 		for (CardEffect effect : this.effects) {
 			boolean effectWorked = effect.execute(activePlayer, currentTurn, supply);
 			if (effectsDependOnEachOther && !effectWorked) {
 				return;
 			}
 		}
 	}
 
 	public void isAddedToHand(Player owner) {
 		for (CardEffect effect : this.effects) {
 			effect.isAddedToHand(owner);
 		}
 
 	}
 
 	public void getsRemovedFromHand(Player owner) {
 		for (CardEffect effect : this.effects) {
 			effect.getsRemovedFromHand(owner);
 		}
 	}
 
 	public int getCoins() {
 		int coins = 0;
 
 		for (CardEffect effect : this.effects) {
 			coins += effect.getCoins();
 		}
 
 		return coins;
 	}
 
 	public int getVictoryPoints(Player owner, CardList allCardsOfPlayer) {
 		int points = 0;
 
 		for (CardEffect effect : this.effects) {
 			points += effect.getVictoryPoints(owner, allCardsOfPlayer);
 		}
 
 		return points;
 	}
 
 	public int getAddedActions() {
 		int actions = 0;
 
 		for (CardEffect effect : this.effects) {
 			actions += effect.getAddedActions();
 		}
 
 		return actions;
 	}
 
 	public boolean isTerminalAction() {
 		return isOfType(Type.ACTION) && getAddedActions() == 0;
 	}
 }
