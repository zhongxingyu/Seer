 package com.haikuwind.tabs;
 
 import java.util.List;
 
 import com.haikuwind.feed.FeedException;
 import com.haikuwind.feed.Haiku;
 import com.haikuwind.feed.HaikuListData;
 import com.haikuwind.feed.HaikuWindData;
 import com.haikuwind.feed.HttpRequest;
 import com.haikuwind.notification.Update;
 import com.haikuwind.notification.UpdateNotifier;
 import com.haikuwind.tabs.buttons.HasFavoriteBtn;
 
 public class HallOfFame extends HaikuListActivity implements HasFavoriteBtn {
 
     @Override
     protected void onStart() {
         super.onStart();
         UpdateNotifier.addUpdateListener(this, Update.VOTE);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         UpdateNotifier.removeUpdateListener(this);
     }
 
     @Override
     protected List<Haiku> fetchElements() throws FeedException {
         return HttpRequest.getHallOfFame();
     }
     
     @Override
     public void processUpdate(Update update, Haiku haiku) {
         //if haiku was voted and got in hall of fame, mark the hall of fame to update.
        if(update==Update.REFRESH || haiku.getPoints()>=Haiku.HALL_OF_FAME_POINTS) {
             super.processUpdate(update, haiku);
         }
     }
 
     @Override
     protected HaikuListData getHaikuListData() {
         return HaikuWindData.getInstance().getHallOfFameData();
     }
 }
