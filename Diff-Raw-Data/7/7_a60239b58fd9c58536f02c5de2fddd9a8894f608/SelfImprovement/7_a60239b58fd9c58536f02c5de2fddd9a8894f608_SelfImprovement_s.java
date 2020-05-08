 import java.util.ArrayList;
 
 	public class SelfImprovement extends ReactionCard {
 		
 		public SelfImprovement() {
 			this.cardColor = new ArrayList<CardColor>();
 			cardColor.add(CardColor.BLUE);
 			this.cardType = cardType.PUZZLE;
 			this.defense = true;
 			this.opposing = false;
 			this.cost = 4;
 			this.name = "SelfImprovement";
 			this.imagePath = name + ".png";
 		}
 		
 		public void use(ArrayList<Choice> choices, Game game) {
 			super.use(choices, game);
 			Choice cardChoice = choices.get(0);
 			Card card = (Card) cardChoice.getChoice().get(0);
 			game.getCurrentPlayer().hand.remove(card);
 		}
 		
 		public ChoiceGroup getChoice(Game g) {
 			ArrayList<String> hand = g.getHand(this);
 			Choice c1 = new Choice(g.choices.getString("cardTrash"), hand, g.getHandObj(this), 1);
 			ChoiceGroup choices = new ChoiceGroup();
 			choices.addChoiceToGroup(c1);
 			return choices;
 		}
 		
 		public Card newCard() {
 			ParamCard newCard = new ParamCard();
 			newCard.setEqual(this);
 			return newCard;
 		}
 		
 		public void setEqual(ParamCard card) {
 			super.setEqual(card);
 		}
 		
		public void react(Card card, Player reacting, ArrayList<Choice> choices, Game game) {
 			reacting.drawFromBag(3);
 		}
 
 		public boolean canReactTo(Card card) {
 			return card.cardColor.contains(CardColor.RED);
 		}
 		
 		public ChoiceGroup getReactChoices(Game g) {
 			return new ChoiceGroup();
 		}
 
 		@Override
 		public Choice getTrickyChoice(int i, Choice choice, Game g) {
 			// ignore
 			return null;
 		}
 	}
