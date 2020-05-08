 package napoleon.model.rule;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import mockit.Mocked;
 import mockit.Tested;
 import mockit.Verifications;
 import napoleon.model.card.Card;
 import napoleon.model.card.Suit;
 import napoleon.model.player.Player;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 public class TurnTest {
 	@Mocked Player player1;
 	@Mocked Player player2;
 	@Mocked Player player3;
 	@Mocked Player player4;
 	@Tested Turn turn = Turn.New(1, Suit.Spade);
 	Card[] parameterOfCardsToTake;
 
 	@Rule 
 	public ExpectedException exception = ExpectedException.none();
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void T01_CX^X^[1菬ꍇ̓G[() {
 		Turn.New(0, Suit.Spade);
 	}
 
 	@Test
 	public void T02_CX^X^[12傫ꍇ̓G[() {
 		exception.expect(IllegalArgumentException.class);
 		exception.expectMessage("^[ԍ1ȏ12ȉłKv܂");
 		Turn.New(13, Suit.Spade);
 	}
 	
 	@Test
 	public void T03_ŏɏoꂽX[gDɂȂ邱() {
 		Turn turn = Turn.New(1, Suit.Spade);
 		assertThat(turn.getLeadSuit(), equalTo(null));	
 		turn.addCard(player1, Card.New(Suit.Spade, 2));
 		assertThat(turn.getLeadSuit(), equalTo(Suit.Spade));	
 	}
 	
 	@Test
 	public void T03_ŏɃW[J[oꂽ؂DDɂȂ邱() {
 		Turn turn = Turn.New(2, Suit.Spade);
 		assertThat(turn.getLeadSuit(), equalTo(null));	
 		turn.addCard(player1, Card.Jorker);
 		assertThat(turn.getLeadSuit(), equalTo(Suit.Spade));	
 	}
 
 	@Test
 	public void T03_J[h̃^[ōŏɃW[J[oꂽD2lڂɂ܂邱() {
 		Turn turn = Turn.New(1, Suit.Spade);
 		assertThat(turn.getLeadSuit(), equalTo(null));	
 		turn.addCard(player1, Card.Jorker);
 		assertThat(turn.getLeadSuit(), equalTo(null));	
 	}
 	
 	@Test
 	public void T04_J[h4oĂꍇɓJ[hlŏ҂𔻒fł() {
 		
 		final Parameters param = new Parameters(
 				Turn.New(1, Suit.Spade),
 				player2, Card.New(Suit.Dia, 8), 
 				player3, Card.New(Suit.Dia, 4), 
 				player4, Card.New(Suit.Dia, 12), 
 				player1, Card.New(Suit.Heart, 13), 
 				player4, Card.New(Suit.Dia, 12),
 				new Card[]{Card.New(Suit.Dia, 12), Card.New(Suit.Heart, 13)});
 		
 		^[񂵂ď҂mF(param);
 	}
 
 	@Test
 	public void T05_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_ZC2̖() {
 		Parameters param = new Parameters(
 				Turn.New(1, Suit.Spade),
 				player2, Card.New(Suit.Dia, 8), 
 				player3, Card.New(Suit.Dia, 2), 
 				player4, Card.New(Suit.Dia, 12), 
 				player1, Card.New(Suit.Dia, 13), 
 				player1, Card.New(Suit.Dia, 13),
 				new Card[]{Card.New(Suit.Dia, 12), Card.New(Suit.Dia, 13)});
 		
 		^[񂵂ď҂mF(param);
 	}
 	
 	@Test
 	public void T05_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_ZC2() {
 		Parameters param = new Parameters(
 				Turn.New(2, Suit.Spade),
 				player2, Card.New(Suit.Dia, 8), 
 				player3, Card.New(Suit.Dia, 2), 
 				player4, Card.New(Suit.Dia, 12), 
 				player1, Card.New(Suit.Dia, 13), 
 				player3, Card.New(Suit.Dia, 2),
 				new Card[]{Card.New(Suit.Dia, 12), Card.New(Suit.Dia, 13)});
 		
 		^[񂵂ď҂mF(param);
 	}
 	
 	@Test
 	public void T05_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_ZC2_ʂ̃X[g() {
 		Parameters param = new Parameters(
 				Turn.New(2, Suit.Spade),
 				player2, Card.New(Suit.Dia, 8), 
 				player3, Card.New(Suit.Dia, 2), 
 				player4, Card.New(Suit.Dia, 12), 
 				player1, Card.New(Suit.Heart, 13), 
 				player4, Card.New(Suit.Dia, 12),
 				new Card[]{Card.New(Suit.Dia, 12), Card.New(Suit.Heart, 13)});
 		
 		^[񂵂ď҂mF(param);
 	}
 	
 	@Test
 	public void T06_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_߂̖() {
 		Parameters param = new Parameters(
 				Turn.New(1, Suit.Spade),
 				player2, Card.New(Suit.Spade, 1), 
 				player3, Card.New(Suit.Heart, 12), 
 				player4, Card.New(Suit.Dia, 12), 
 				player1, Card.New(Suit.Dia, 13), 
 				player2, Card.New(Suit.Spade, 1),
 				new Card[]{Card.New(Suit.Dia, 12), Card.New(Suit.Dia, 13), Card.New(Suit.Spade, 1), Card.New(Suit.Heart, 12)});
 		
 		^[񂵂ď҂mF(param);
 	}
 
 	@Test
 	public void T06_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_߂() {
 		Parameters param = new Parameters(
 				Turn.New(11, Suit.Spade),
 				player2, Card.New(Suit.Spade, 1), 
 				player3, Card.New(Suit.Heart, 12), 
 				player4, Card.New(Suit.Dia, 12), 
 				player1, Card.New(Suit.Dia, 13), 
 				player3, Card.New(Suit.Heart, 12),
 				new Card[]{Card.New(Suit.Dia, 12), Card.New(Suit.Dia, 13), Card.New(Suit.Spade, 1), Card.New(Suit.Heart, 12)});
 		
 		^[񂵂ď҂mF(param);
 	}
 
 	@Test
 	public void T07_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_}CeB̖() {
 		Parameters param = new Parameters(
 				Turn.New(1, Suit.Spade),
 				player2, Card.New(Suit.Heart, 1), 
 				player3, Card.New(Suit.Heart, 12), 
 				player4, Card.New(Suit.Spade, 1), 
 				player1, Card.New(Suit.Dia, 13), 
 				player2, Card.New(Suit.Heart, 1),
 				new Card[]{Card.New(Suit.Heart, 12), Card.New(Suit.Dia, 13), Card.New(Suit.Heart, 1), Card.New(Suit.Spade, 1),});
 		
 		^[񂵂ď҂mF(param);
 	}
 
 	@Test
 	public void T07_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_}CeB() {
 		Parameters param = new Parameters(
 				Turn.New(2, Suit.Spade),
 				player2, Card.New(Suit.Heart, 1), 
 				player3, Card.New(Suit.Heart, 13), 
 				player4, Card.New(Suit.Spade, 1), 
 				player1, Card.New(Suit.Dia, 13), 
 				player4, Card.New(Suit.Spade, 1),
 				new Card[]{Card.New(Suit.Heart, 13), Card.New(Suit.Dia, 13), Card.New(Suit.Heart, 1), Card.New(Suit.Spade, 1),});
 		
 		^[񂵂ď҂mF(param);
 	}
 
 	@Test
 	public void T08_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_؂D̖() {
 		Parameters param = new Parameters(
 				Turn.New(1, Suit.Club),
 				player2, Card.New(Suit.Heart, 1), 
 				player3, Card.New(Suit.Heart, 12), 
 				player4, Card.New(Suit.Club, 3), 
 				player1, Card.New(Suit.Dia, 13), 
 				player2, Card.New(Suit.Heart, 1),
 				new Card[]{Card.New(Suit.Heart, 12), Card.New(Suit.Dia, 13), Card.New(Suit.Heart, 1)});
 		
 		^[񂵂ď҂mF(param);
 	}
 	
 	@Test
 	public void T08_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_؂D() {
 		Parameters param = new Parameters(
 				Turn.New(2, Suit.Club),
 				player2, Card.New(Suit.Heart, 1), 
 				player3, Card.New(Suit.Heart, 12), 
 				player4, Card.New(Suit.Club, 3), 
 				player1, Card.New(Suit.Dia, 13), 
 				player4, Card.New(Suit.Club, 3),
 				new Card[]{Card.New(Suit.Heart, 12), Card.New(Suit.Dia, 13), Card.New(Suit.Heart, 1)});
 		
 		^[񂵂ď҂mF(param);
 	}
 	
 	@Test
 	public void T09_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_J̖() {
 		Parameters param = new Parameters(
 				Turn.New(1, Suit.Club),
 				player2, Card.New(Suit.Club, 1), 
 				player3, Card.New(Suit.Club, 12), 
 				player4, Card.New(Suit.Spade, 11), 
 				player1, Card.New(Suit.Dia, 13), 
 				player2, Card.New(Suit.Club, 1),
 				new Card[]{Card.New(Suit.Club, 12), Card.New(Suit.Dia, 13), Card.New(Suit.Club, 1), Card.New(Suit.Spade, 11),});
 		
 		^[񂵂ď҂mF(param);
 	}
 
 	@Test
 	public void T09_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_J() {
 		Parameters param = new Parameters(
 				Turn.New(2, Suit.Club),
 				player2, Card.New(Suit.Club, 1), 
 				player3, Card.New(Suit.Club, 12), 
 				player4, Card.New(Suit.Spade, 11), 
 				player1, Card.New(Suit.Dia, 13), 
 				player4, Card.New(Suit.Spade, 11),
 				new Card[]{Card.New(Suit.Club, 12), Card.New(Suit.Dia, 13), Card.New(Suit.Club, 1), Card.New(Suit.Spade, 11),});
 		
 		^[񂵂ď҂mF(param);
 	}
 
 	@Test
 	public void T10_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_J̖() {
 		Parameters param = new Parameters(
 				Turn.New(1, Suit.Club),
 				player2, Card.New(Suit.Spade, 12), 
 				player3, Card.New(Suit.Heart, 1), 
 				player4, Card.New(Suit.Club, 11), 
 				player1, Card.New(Suit.Dia, 13), 
 				player2, Card.New(Suit.Spade, 12),
 				new Card[]{Card.New(Suit.Spade, 12), Card.New(Suit.Dia, 13), Card.New(Suit.Heart, 1), Card.New(Suit.Club, 11),});
 		
 		^[񂵂ď҂mF(param);
 	}
 
 	@Test
 	public void T10_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_J() {
 		Parameters param = new Parameters(
 				Turn.New(2, Suit.Club),
 				player2, Card.New(Suit.Spade, 12), 
 				player3, Card.New(Suit.Heart, 1), 
 				player4, Card.New(Suit.Club, 11), 
 				player1, Card.New(Suit.Dia, 13), 
 				player4, Card.New(Suit.Club, 11),
 				new Card[]{Card.New(Suit.Spade, 12), Card.New(Suit.Dia, 13), Card.New(Suit.Heart, 1), Card.New(Suit.Club, 11),});
 		
 		^[񂵂ď҂mF(param);
 	}
 
 	@Test
 	public void T11_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_Jorker̖() {
 		Parameters param = new Parameters(
 				Turn.New(1, Suit.Club),
 				player2, Card.Jorker, 
 				player3, Card.New(Suit.Club, 1), 
 				player4, Card.New(Suit.Club, 11), 
 				player1, Card.New(Suit.Dia, 13), 
 				player3, Card.New(Suit.Club, 1),
 				new Card[]{Card.New(Suit.Dia, 13), Card.New(Suit.Club, 1), Card.New(Suit.Club, 11),});
 		
 		^[񂵂ď҂mF(param);
 	}
 
 	@Test
 	public void T11_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_ŏJorker() {
 		Parameters param = new Parameters(
 				Turn.New(2, Suit.Club),
 				player2, Card.Jorker, 
 				player3, Card.New(Suit.Club, 1), 
 				player4, Card.New(Suit.Spade, 11), 
 				player1, Card.New(Suit.Dia, 13), 
 				player2, Card.Jorker,
 				new Card[]{Card.New(Suit.Dia, 13), Card.New(Suit.Club, 1), Card.New(Suit.Spade, 11),});
 		
 		^[񂵂ď҂mF(param);
 	}
 
 	@Test
 	public void T11_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_JorkerVSJ() {
 		Parameters param = new Parameters(
 				Turn.New(2, Suit.Club),
 				player2, Card.Jorker, 
 				player3, Card.New(Suit.Club, 1), 
 				player4, Card.New(Suit.Club, 11), 
 				player1, Card.New(Suit.Dia, 13), 
 				player4, Card.New(Suit.Club, 11), 
 				new Card[]{Card.New(Suit.Dia, 13), Card.New(Suit.Club, 1), Card.New(Suit.Club, 11),});
 		
 		^[񂵂ď҂mF(param);
 	}
 
 	@Test
 	public void T11_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_JorkerVS}CeB() {
 		Parameters param = new Parameters(
 				Turn.New(2, Suit.Club),
 				player2, Card.Jorker, 
 				player3, Card.New(Suit.Club, 1), 
 				player4, Card.Mighty, 
 				player1, Card.New(Suit.Dia, 13), 
 				player4, Card.Mighty, 
 				new Card[]{Card.New(Suit.Dia, 13), Card.New(Suit.Club, 1), Card.Mighty,});
 		
 		^[񂵂ď҂mF(param);
 	}
 
 	@Test
 	public void T11_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_JorkerVS߂() {
 		Parameters param = new Parameters(
 				Turn.New(2, Suit.Club),
 				player2, Card.Jorker, 
 				player3, Card.New(Suit.Club, 1), 
 				player4, Card.Mighty, 
 				player1, Card.Yoromeki, 
 				player1, Card.Yoromeki, 
 				new Card[]{Card.Yoromeki, Card.New(Suit.Club, 1), Card.Mighty,});
 		
 		^[񂵂ď҂mF(param);
 	}
 
 	@Test
 	public void T11_J[h4oĂꍇɓJ[hlŏ҂𔻒fł_rJorker() {
 		Parameters param = new Parameters(
 				Turn.New(2, Suit.Club),
 				player2, Card.New(Suit.Club, 1), 
 				player3, Card.Jorker, 
 				player4, Card.New(Suit.Spade, 11), 
 				player1, Card.New(Suit.Dia, 13), 
 				player4, Card.New(Suit.Spade, 11),
 				new Card[]{Card.New(Suit.Dia, 13), Card.New(Suit.Club, 1), Card.New(Suit.Spade, 11),});
 		
 		^[񂵂ď҂mF(param);
 	}
 	
 	private void ^[񂵂ď҂mF(Parameters param) {
 		turn = param.turn;
 		turn.addCard(param.player1, param.card1);
 		turn.addCard(param.player2, param.card2);
 		turn.addCard(param.player3, param.card3);
 		turn.addCard(param.player4, param.card4);
 		turn.winnerGainCards();
 		
 		assertThat(turn.getWinnerCard(), equalTo(param.winnerCard));
 		assertThat(turn.getWinner(), equalTo(param.winner));
 		parameterOfCardsToTake = param.winnerWillGet;
 		new Verifications(){
 			{
 				Collection<Card> cardToTake;
 				turn.getWinner().takeCards(cardToTake = withCapture());
 				assertTrue(CollectionUtils.isEqualCollection(cardToTake, Arrays.asList(parameterOfCardsToTake)));
 			}
 		};
 	}
 
 	class Parameters {
 		public Parameters(Turn turn, Player player1, Card card1, Player player2,
 				Card card2, Player player3, Card card3, Player player4,
 				Card card4, Player winner, Card winnerCard, Card[] winnerWillGet) {
 			super();
 			this.turn = turn;
 			this.player1 = player1;
 			this.card1 = card1;
 			this.player2 = player2;
 			this.card2 = card2;
 			this.player3 = player3;
 			this.card3 = card3;
 			this.player4 = player4;
 			this.card4 = card4;
 			this.winner = winner;
 			this.winnerCard = winnerCard;
 			this.winnerWillGet = winnerWillGet;
 		}
 		Turn turn;
 		Player player1;
 		Card card1;
 		Player player2;
 		Card card2;
 		Player player3;
 		Card card3;
 		Player player4;
 		Card card4;
 		Player winner;
 		Card winnerCard;
 		Card[] winnerWillGet;
 	}
 
 	
 }
 
