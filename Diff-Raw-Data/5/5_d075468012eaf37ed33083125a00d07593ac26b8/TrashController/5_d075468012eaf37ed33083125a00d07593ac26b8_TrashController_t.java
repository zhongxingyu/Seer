 package org.jdominion.gui;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.jdominion.Card;
 import org.jdominion.Trash;
 import org.jdominion.event.CardGainedFromTrash;
 import org.jdominion.event.CardTrashed;
 import org.jdominion.event.Event;
 import org.jdominion.event.EventManager;
 import org.jdominion.event.EventManager.Duration;
 import org.jdominion.event.IEventHandler;
 
 public class TrashController implements IEventHandler, MouseListener {
 
 	/**
 	 * Dummy card as a placeholder before there are real cards in the trash
 	 * 
 	 */
 	private static class TrashCard extends Card {
 
 		public TrashCard() {
 			super("Trash", 0);
 		}
 
 	}
 
 	private TrashView view;
 	private boolean fullViewVisible = false;
 	private IGuiInformationSource guiInformationSource;
 
 	public TrashView getView() {
 		return view;
 	}
 
 	public TrashController(IGuiInformationSource guiInformationSource) {
 		this.guiInformationSource = guiInformationSource;
 		EventManager.getInstance().addEventHandler(this, CardTrashed.class, Duration.FOREVER);
 		EventManager.getInstance().addEventHandler(this, CardGainedFromTrash.class, Duration.FOREVER);
 		this.view = new TrashView(this);
 		view.addMouseListener(this);
 		view.setImageForSmallView(new CardImage(new TrashCard()));
 	}
 
 	@Override
 	public void handleEvent(Event event) {
 		update();
 	}
 
 	private void update() {
 
 		view.resetFullView();
 
		if (guiInformationSource.getTrash().isEmpty()) {
			view.setImageForSmallView(new CardImage(new TrashCard()));
			return;
		}

 		Set<Class<? extends Card>> cardTypesAlreadyProcessed = new HashSet<Class<? extends Card>>();
 		for (Card cardInTrash : guiInformationSource.getTrash()) {
 			if (!cardTypesAlreadyProcessed.contains(cardInTrash.getClass())) {
 				addCardClassToView(cardInTrash, guiInformationSource.getTrash());
 				cardTypesAlreadyProcessed.add(cardInTrash.getClass());
 			}
 		}
 		CardImage topCardImage = new CardImage(guiInformationSource.getTrash().getLast());
 		topCardImage.addMouseListener(this);
 		view.setImageForSmallView(topCardImage);
 	}
 
 	private void addCardClassToView(Card cardClassToAdd, Trash trash) {
 		CardImage image = new CardImage(cardClassToAdd);
 		image.setOverlayText(Integer.toString(trash.countCard(cardClassToAdd.getClass())) + "X");
 		view.addCardImageToFullView(image);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		if (fullViewVisible) {
 			hideFullView();
 		} else {
 			showFullView();
 		}
 	}
 
 	private void hideFullView() {
 		view.hideFullView();
 		fullViewVisible = false;
 	}
 
 	private void showFullView() {
 		if (!guiInformationSource.getTrash().isEmpty()) {
 			view.showFullView();
 			fullViewVisible = true;
 		}
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
