 package ch.bfh.monopoly.common;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.border.Border;
 
 import ch.bfh.monopoly.tests.EventJPanelTest;
 import ch.bfh.monopoly.tile.EventPanelFactory;
 import ch.bfh.monopoly.tile.EventPanelInfo;
 import ch.bfh.monopoly.tile.EventPanelSource;
 import ch.bfh.monopoly.tile.Step;
 
 public class Dice implements EventPanelSource {
 
 	int maxValDiceOne;
 	int maxValDiceTwo;
 	int throwValueOne = 0;
 	int throwValueTwo = 0;
 	GameClient gameClient;
 	ActionListener al;
 	boolean testOff;
 	boolean doublesLastRoll = false;
 	ResourceBundle rb;
 	EventPanelFactory epf;
 
 	/**
 	 * Construct a dice object
 	 * 
 	 * @param maxValDiceOne
 	 *            the maximal value for the die one
 	 * @param maxValDiceTwo
 	 *            the maximal value for the die two
 	 */
 	public Dice(int maxValDiceOne, int maxValDiceTwo) {
 		this.maxValDiceOne = maxValDiceOne;
 		this.maxValDiceTwo = maxValDiceTwo;
 	}
 
 	/**
 	 * Construct a dice object
 	 * 
 	 * @param maxValDiceOne
 	 *            the maximal value for the die one
 	 * @param maxValDiceTwo
 	 *            the maximal value for the die two
 	 */
 	public Dice(int maxValDiceOne, int maxValDiceTwo, GameClient gameClient,
 			boolean testOff) {
 		this.testOff = testOff;
 		this.gameClient = gameClient;
 		this.maxValDiceOne = maxValDiceOne;
 		this.maxValDiceTwo = maxValDiceTwo;
 		rb = ResourceBundle.getBundle("ch.bfh.monopoly.resources.tile",
 				gameClient.getLoc());
 	}
 
 	/**
 	 * Throw the dice
 	 * 
 	 * @return int an integer representing the throw of the two dice
 	 */
 	public int throwDice() {
 		// generates number between 1 and 12
 		// the one is added because Math.random() generates
 		// number between 0.0 and 1.0. But 1.0 is not included.
 		// so in the case where:
 		// maxValDice = 6 ; Math.random = 0.99
 		// 6*0.99 = 5.94 rounded => 5 + 1 = 6
 		throwValueOne = (int) (maxValDiceOne * Math.random()) + 1;
 		throwValueTwo = (int) (maxValDiceTwo * Math.random()) + 1;
 
 		// throwValueOne = 4;
 		// throwValueTwo = 4;
 		return throwValueOne + throwValueTwo;
 	}
 
 	/**
 	 * Get the single value of both dice
 	 * 
 	 * @return String a string representing the two values, for example 3, 5
 	 */
 	public String getDiceValues() {
 		return throwValueOne + ", " + throwValueTwo;
 	}
 
 	public boolean isDoubles() {
 		return throwValueOne == throwValueTwo;
 	}
 
 	public JPanel getNewStartRoll() {
 		epf = new EventPanelFactory(this, gameClient.getSubjectForPlayer());
 		gameClient.attemptedRollsReset();
 		epf.changePanel(Step.ROLL_NORMAL);
 		return epf.getJPanel();
 	}
 
 	public JPanel getNewJailStart() {
 		epf = new EventPanelFactory(this, gameClient.getSubjectForPlayer());
 		gameClient.attemptedRollsReset();
 		epf.changePanel(Step.JAIL_START);
 		return epf.getJPanel();
 	}
 
 	public EventPanelInfo getEventPanelInfoForStep(Step step) {
 		String labelText;
 		String buttonText;
 		ActionListener al;
 
 		EventPanelInfo epi;
 
 		switch (step) {
 		// case ROLL_NORMAL:
 		// epi = getRollStartEPI(false);
 		// break;
 		case ROLL_NORMAL:
 			epi = getRollStartEPI(false);
 			break;
 		case DOUBLES_TRANSITION:
 			System.out.println("IN DOUBLES TRANSITION");
 			epi = new EventPanelInfo(gameClient);
 			if (gameClient.attempedRollsGetCount() > 2)
 				epi = getEventPanelInfoForStep(Step.DOUBLES_TO_JAIL);
 			else {
 				labelText = rb.getString("rollAgainDoubles");
 				buttonText = rb.getString("roll");
 
 				al = new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						epf.changePanel(Step.ROLL_NORMAL);
 					}
 				};
 
 				epi.setText(labelText);
 				epi.addButton(buttonText, 0, al);
 			}
 			break;
 		case DOUBLES_TO_JAIL:
 			epi = new EventPanelInfo(gameClient);
 
 			labelText = rb.getString("tooManyDoubles");
 			buttonText = rb.getString("continueButton");
 			al = new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					gameClient.goToJail(testOff);
 					epf.disableAfterClick();
 				}
 			};
 			epi.setText(labelText);
 			epi.addButton(buttonText, 0, al);
 
 			break;
 		case JAIL_START:
 			epi = getJailStartEPI();
 			break;
 		case JAIL_PAY:
 			gameClient.getOutOfJailByPayment(testOff);
 			epi = getFreedFromJailEPI();
 			break;
 		case JAIL_CARD:
 			gameClient.getOutOfJailByCard(testOff);
 			epi = getFreedFromJailEPI();
 			break;
 		case JAIL_ROLL:
 			epi = new EventPanelInfo(gameClient);
 
 			final int rollValue = throwDice();
 			if (isDoubles()) {
 				labelText = rb.getString("youRolled") + " " + getDiceValues()
 						+ " " + rb.getString("outOfJail");
 				buttonText = rb.getString("roll");
 				al = new ActionListener() {
 
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						System.out.println("JailStatus:"
 								+ gameClient.getCurrentPlayer().isInJail());
 						gameClient.getOutOfJailByRoll(testOff);
 						System.out.println("JailStatus:"
 								+ gameClient.getCurrentPlayer().isInJail());
 						epf.changePanel(Step.ROLL_NORMAL);
 					}
 				};
 
 				epi.setText(labelText);
 				epi.addButton(buttonText, 0, al);
 			} else {
 				if (gameClient.attempedRollsGetCount() > 2)
 					epi = rollFailureEPI();
 				else {
 					epi = getJailStartEPI();
 				}
 			}
 			break;
 		case JAIL_FREED:
 			epi = getFreedFromJailEPI();
 			break;
 		default:
 			epi = new EventPanelInfo(gameClient);
 			labelText = "No case defined";
 			buttonText = "ok";
 			al = new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 				}
 			};
 			epi.setText(labelText);
 			epi.addButton(buttonText, 0, al);
 			break;
 		}
 		return epi;
 	}
 
 	public EventPanelInfo rollFailureEPI() {
 		String labelText;
 		String buttonText;
 		ActionListener al;
 		EventPanelInfo epi = new EventPanelInfo(gameClient);
 
 		labelText = rb.getString("rollFailure");
 
 		buttonText = "ok";
 		al = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gameClient.sendTransactionSuccesToGUI(testOff);
 				epf.disableAfterClick();
 			}
 		};
 		epi.setText(labelText);
 		epi.addButton(buttonText, 0, al);
 		return epi;
 	}
 
 	public EventPanelInfo getRollStartEPI(boolean freed) {
 		String labelText;
 		String buttonText;
 		ActionListener al;
 		EventPanelInfo epi = new EventPanelInfo(gameClient);
 
 		final int roll = throwDice();
 
 		System.out.println("DICE CLASS rolled: " + roll);
 
 		labelText = rb.getString("youRolled") + getDiceValues() + "\n\n "
 				+ rb.getString("advance") + " " + roll + " "
 				+ rb.getString("spaces");
 		buttonText = rb.getString("continueButton");
 
 		gameClient.attemptedRollIncrement();
 		if (isDoubles())
 			labelText = "DOUBLES! \n" + labelText;
 		al = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
				epf.disableAfterClick();
 				int prevPos = gameClient.getCurrentPlayer().getPosition();
 				gameClient.advancePlayerNSpaces(roll, testOff);
 				// gameClient.sendTransactionSuccesToGUI(true);
 				int curPos = gameClient.getCurrentPlayer().getPosition();
 				System.out.println("PrevPos" + prevPos + "  CurPos" + curPos);
				
 			}
 		};
 
 		epi.setText(labelText);
 		epi.addButton(buttonText, 0, al);
 		return epi;
 	}
 
 	public EventPanelInfo getFreedFromJailEPI() {
 		String labelText;
 		String buttonText;
 		ActionListener al;
 		EventPanelInfo epi = new EventPanelInfo(gameClient);
 
 		buttonText = rb.getString("roll");
 		al = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				epf.changePanel(Step.ROLL_NORMAL);
 			}
 		};
 		epi.setText(rb.getString("freed"));
 		epi.addButton(buttonText, 0, al);
 		return epi;
 	}
 
 	public EventPanelInfo getJailStartEPI() {
 		String labelText;
 
 		EventPanelInfo epi = new EventPanelInfo(gameClient);
 		if (gameClient.attempedRollsGetCount() > 0) {
 			labelText = rb.getString("youRolled") + getDiceValues() + " "
 					+ rb.getString("rollAgain") + " "
 					+ (3 - gameClient.attempedRollsGetCount()) + " "
 					+ rb.getString("triesRemaining");
 		} else
 			labelText = rb.getString("inJail");
 
 		int bail = gameClient.getBail();
 		ActionListener pay = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gameClient.getOutOfJailByPayment(testOff);
 
 				epf.changePanel(Step.JAIL_PAY);
 			}
 		};
 
 		ActionListener card = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gameClient.getOutOfJailByCard(testOff);
 				epf.changePanel(Step.JAIL_CARD);
 			}
 		};
 
 		ActionListener rollJail = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				gameClient.attemptedRollIncrement();
 				epf.changePanel(Step.JAIL_ROLL);
 				System.out.println("attempted Rolls: "
 						+ gameClient.attempedRollsGetCount());
 			}
 		};
 
 		String buttonTextPay = rb.getString("pay") + " " + bail;
 		String buttonTextCard = rb.getString("card");
 		String buttonTextRoll = rb.getString("roll");
 
 		epi.setText(labelText);
 		epi.addButton(buttonTextPay, bail, pay);
 		epi.addButton(buttonTextCard, -100, card);
 		epi.addButton(buttonTextRoll, 0, rollJail);
 		return epi;
 	}
 
 }
