 package com.idamobile.map.yandex;
 
 import java.util.Iterator;
 
 import ru.yandex.yandexmapkit.overlay.OverlayItem;
 import ru.yandex.yandexmapkit.overlay.balloon.BalloonItem;
 import ru.yandex.yandexmapkit.overlay.balloon.BalloonOverlay;
 import android.view.animation.Animation;
 
 import com.idamobile.map.AbstractBalloonController;
 import com.idamobile.map.BalloonOverlayExtension;
 import com.idamobile.map.ItemizedOverlayBase;
 import com.idamobile.map.OverlayItemBase;
 
 class BalloonOverlayAdapter<T extends OverlayItemBase> extends ItemizedOverlayAdapter<T> {
 
     private MapViewWrapper mapViewWrapper;
 
     @SuppressWarnings("unchecked")
     public BalloonOverlayAdapter(final MapViewWrapper mapViewWrapper, ItemizedOverlayBase<T> overlay) {
         super(mapViewWrapper, overlay,
                 new BalloonOverlayItemAdapter(mapViewWrapper.getContext(),
                         ((BalloonOverlayExtension<T>) overlay).getAdapter(), overlay.getMarker()));
         this.mapViewWrapper = mapViewWrapper;
         BalloonOverlayExtension<T> overlayExtension = (BalloonOverlayExtension<T>) overlay;
         overlayExtension.setBalloonController(new AbstractBalloonController(mapViewWrapper.getContext()) {
             @Override
             public boolean isBalloonShowing() {
                 return findItemWithBalloon() != null;
             }
 
             @Override
             protected void showBalloon(OverlayItemBase forItem, Animation animation) {
                OverlayItem item = getResultItem((T) forItem);
                if (item != null) {
                    mapViewWrapper.getView().getMapController().showBalloon(item.getBalloonItem());
                }
             }
 
             @Override
             protected void hideBalloon(Animation animation) {
                 mapViewWrapper.getView().getMapController().hideBalloon();
             }
 
             @Override
             public OverlayItemBase getItemWithOpenBalloon() {
                 OverlayItem itemWithBalloon = findItemWithBalloon();
                 return itemWithBalloon != null ? getOriginalItem(itemWithBalloon) : null;
             }
         });
     }
 
     @SuppressWarnings("unchecked")
     private OverlayItem findItemWithBalloon() {
         BalloonOverlay balloonOverlay = mapViewWrapper.getView()
                 .getMapController()
                 .getOverlayManager()
                 .getBalloon();
         BalloonItem balloonItem = balloonOverlay.isVisible() ? balloonOverlay.getBalloonItem() : null;
         if (balloonItem != null) {
             for (Iterator<OverlayItem> iter = getResultOverlay().getOverlayItems().iterator(); iter.hasNext();) {
                 OverlayItem item = iter.next();
                 if (balloonItem == item.getBalloonItem()) {
                     return item;
                 }
             }
         }
         return null;
     }
 
     @Override
     public void release() {
         ((BalloonOverlayExtension<?>) getBaseOverlay()).setBalloonController(null);
         super.release();
     }
 
 }
