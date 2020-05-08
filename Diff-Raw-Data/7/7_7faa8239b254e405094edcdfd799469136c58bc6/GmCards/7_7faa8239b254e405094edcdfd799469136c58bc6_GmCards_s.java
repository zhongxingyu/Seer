 package oorlog.client;
 
 import java.util.ArrayList;
 
 import oorlog.shared.Card;
 import oorlog.shared.Country;
 import oorlog.shared.Message;
 import oorlog.shared.TurnInCards;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public class GmCards extends GameModule {
 	private boolean locked;
 	GmCards(GameScreen gs) {
 		super(gs);
 	}
 
 	@Override
 	public void receive(Message m) {
  		/** CARD **/
 		if (m.getType().equals("CARD")) {
  			Card card = m.card;
  			card.populate(gs.getGame());
  			gs.getGame().add(GUI.getUser(),card);
  			gs.update();
  		/** TURN IN **/
  		} else if (m.getType().equals("TurnInCards")) {
  			TurnInCards turnIn = m.turnInCards;
  			turnIn.populate(gs.getGame());
  			turnIn.turnIn();
  			gs.update();
  		}
 	}
 
 	@Override
 	public void click(Country country, ClickEvent e) {
 		//Nothing
 	}
 	public void turnIn(final ArrayList<Card> arrayList) {
 		locked=true;
 		gs.update();
 		gs.getGameService().turnIn(gs.getGame().getID(), arrayList, new AsyncCallback<Boolean>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Window.alert("Error while turning in cards");
 				locked=false;
 				gs.update();
 			}
 
 			@Override
 			public void onSuccess(Boolean result) {
 				if (!result) {
 					Window.alert("Invalid turn in");
				}
				for (Card c : arrayList) {
					gs.getGame().returnCard(GUI.getUser(), c);
 				}
 				locked=false;
 				gs.update();
 			}
 			
 		});
 	}
 
 	@Override
 	public void clear() {
 		locked=false;
 	}
 
 	public boolean isLocked() {
 		return locked;
 	}
 }
