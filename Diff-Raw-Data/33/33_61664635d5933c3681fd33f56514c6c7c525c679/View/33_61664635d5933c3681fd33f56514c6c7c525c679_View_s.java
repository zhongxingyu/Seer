 package blackjack;
 
 import model.BlackJackHand;
 import model.Card;
 import model.Deck;
import model.Hand;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.part.ViewPart;
 
 public class View extends ViewPart {
 	public static final String ID = "BlackJack.view";
 	private static final String STAY_BUTTON_TEXT = "Stay!";
 	private static final String CARD_BUTTON_TEXT = "Hit me!";
 
 	private Composite parent;
 
 	private Deck deck;
 	private BlackJackHand hand = new BlackJackHand();
 	private BlackJackHand dealersHand = new BlackJackHand();
 	private Text handLabel;
 	private Text dealersHandLabel;
 	private Text scoreLabel;
 	private Text dealersScoreLabel;
 	private Button newCard;
 	private Button stay;
 	private Composite cardImageComposite;
 	private Composite dealersCardImageComposite;
 
 	public View() {
 		super();
 		deck = new Deck();
 		hand.add(deck.drawCard());
 		hand.add(deck.drawCard());
 		dealersHand.add(deck.drawCard());
 	}
 
 	public void createPartControl(Composite parent) {
 		this.parent = parent;
 		GridLayout layout = new GridLayout();
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		parent.setLayout(layout);
 
 		addHandToView();
 		addScoreToView();
 		addDealersHandToView();
 		addDealersScoreToView();
 		Composite c = new Composite(parent, SWT.WRAP);
 		RowLayout rl = new RowLayout();
 		c.setLayout(rl);
 		addNewCardButtonToView(c);
 		addStayButtonToView(c);
 	}
 
 	private void addHandToView() {
 		cardImageComposite = new Composite(parent, SWT.WRAP);
 		addImages(cardImageComposite, hand);
 		handLabel = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
 		setupLabelLayout(handLabel);
 		updateHandLabel(handLabel, hand);
 	}
 
 	private void addDealersHandToView() {
 		dealersCardImageComposite = new Composite(parent, SWT.WRAP);
 		addImages(dealersCardImageComposite, dealersHand);
 		dealersHandLabel = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.WRAP);
 		setupLabelLayout(dealersHandLabel);
 		updateHandLabel(dealersHandLabel, dealersHand);
 	}
 	
 	private void addImages(Composite composite, BlackJackHand hand) {
 		RowLayout rl = new RowLayout();
 		composite.setLayout(rl);
 		Canvas canvas = new Canvas(composite, SWT.NONE);
 		int width = ApplicationWorkbenchWindowAdvisor.getWidth();
 		canvas.setLayoutData(new RowData(width, ImageUtil.getCardHeight()));
 		canvas.addPaintListener(new CardPaintListner(hand));
 	}
 	
 	private void setupLabelLayout(Text label) {
 		label.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
 				true, true));
 		label.setEditable(false);
 		label.setBackground(parent.getDisplay().getSystemColor(
 				SWT.COLOR_WHITE));
 	}
 
 	private void updateHandLabel(Text label, BlackJackHand hand) {
 		String text = "";
 		for (Card c : hand.getHand()) {
 			text += c + " ";
 		}
 		label.setText(text);
 	}
 
 	private void addScoreToView() {
 		scoreLabel = new Text(parent, SWT.MULTI | SWT.WRAP);
 		scoreLabel.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
 				true, true));
 		scoreLabel.setEditable(false);
 		int score = hand.getScore();
 		scoreLabel.setText("Your score : " + Integer.toString(score));
 	}
 
 	private void addDealersScoreToView() {
 		dealersScoreLabel = new Text(parent, SWT.MULTI | SWT.WRAP);
 		dealersScoreLabel.setLayoutData(new GridData(GridData.FILL,
 				GridData.FILL, true, true));
 		dealersScoreLabel.setEditable(false);
 		int score = dealersHand.getScore();
 		dealersScoreLabel.setText("Dealers score : " + Integer.toString(score));
 	}
 
 	private void addNewCardButtonToView(Composite c) {
 		newCard = new Button(c, SWT.PUSH);
 		newCard.setText(CARD_BUTTON_TEXT);
 		newCard.addMouseListener(new NewCardAdapter());
 	}
 
 	private void addStayButtonToView(Composite c) {
 		stay = new Button(c, SWT.PUSH);
 		stay.setText(STAY_BUTTON_TEXT);
 		stay.addMouseListener(new StayAdapter());
 	}
 
 	public void setFocus() {
 	}
 
 	public void dispose() {
 		super.dispose();
 	}
 
 	public void update() {
 	}
 
 	private void disableButtons() {
 		newCard.setEnabled(false);
 		stay.setEnabled(false);
 	}
 
 	class StayAdapter extends MouseAdapter {
 		public void mouseDown(MouseEvent e) {
 			disableButtons();
 			int dealersScore = dealersHand.getScore();
 			while (dealersScore < 17) {
 				dealersHand.add(deck.drawCard());
 				dealersScore = dealersHand.getScore();
 				dealersScoreLabel.setText("Dealers score : " + dealersScore);
 				updateHandLabel(dealersHandLabel, dealersHand);
 				dealersCardImageComposite.redraw(0, 0, ImageUtil.getCardWidth()
 						* dealersHand.getHand().size()+10, ImageUtil.getCardHeight()+10, true);
 			}
 			int score = hand.getScore();
 			MessageDialog dialog;
 			if (dealersScore < 22 && dealersScore >= score) {
 				dialog = new MessageDialog(parent.getShell(), "Dealer : "
 						+ dealersScore + Text.DELIMITER + "You : " + score
 						+ Text.DELIMITER + Text.DELIMITER + "Epic fail!");
 			} else {
 				dialog = new MessageDialog(parent.getShell(), "Dealer : "
 						+ dealersScore + Text.DELIMITER + "You : " + score
 						+ Text.DELIMITER + Text.DELIMITER + "You win!");
 			}
 			dialog.open();
 		}
 	}
 
 	class NewCardAdapter extends MouseAdapter {
 
 		public void mouseDown(MouseEvent e) {
 			Card c = deck.drawCard();
 			hand.add(c);
 			updateHandLabel(handLabel, hand);
 			int score = hand.getScore();
 			scoreLabel.setText("Your score : " + score);
 			cardImageComposite.redraw(0, 0, ImageUtil.getCardWidth()
 					* hand.getHand().size()+10, ImageUtil.getCardHeight()+10, true);
 			if (score > 21) {
 				disableButtons();
 				MessageDialog dialog = new MessageDialog(parent.getShell(),
 						"Score : " + score + " Epic fail!");
 				dialog.open();
 			}
 		}
 	}
 }
