 package de.db12.game.chessit.client;
 
 import com.google.inject.Inject;
 import com.gwtplatform.mvp.client.EventBus;
 import com.gwtplatform.mvp.client.proxy.PlaceManagerImpl;
 import com.gwtplatform.mvp.client.proxy.PlaceRequest;
 import com.gwtplatform.mvp.client.proxy.TokenFormatter;
 
 import de.db12.game.chessit.client.main.MainPresenter;
 
 public class MyPlaceManager extends PlaceManagerImpl {
 
     @Inject
     public MyPlaceManager(EventBus eventBus, TokenFormatter tokenFormatter) {
         super(eventBus, tokenFormatter);
     }
 
     @Override
     public void revealDefaultPlace() {
        //revealPlace(new PlaceRequest(MainPresenter.nameToken));
     }
 }
