 package com.idamobile.map.yandex;
 
 import java.util.Iterator;
 
 import ru.yandex.yandexmapkit.overlay.OverlayItem;
 import android.view.animation.Animation;
 
 import com.idamobile.map.AbstractBalloonController;
 import com.idamobile.map.BalloonOverlayExtension;
 import com.idamobile.map.ItemizedOverlayBase;
 import com.idamobile.map.OverlayItemBase;
 
 class BalloonOverlayAdapter<T extends OverlayItemBase> extends ItemizedOverlayAdapter<T> {
 
     @SuppressWarnings("unchecked")
     public BalloonOverlayAdapter(final MapViewWrapper mapViewWrapper, ItemizedOverlayBase<T> overlay) {
         super(mapViewWrapper, overlay,
                 new BalloonOverlayItemAdapter(mapViewWrapper.getContext(),
                         ((BalloonOverlayExtension<T>) overlay).getAdapter(), overlay.getMarker()));
         BalloonOverlayExtension<T> overlayExtension = (BalloonOverlayExtension<T>) overlay;
         overlayExtension.setBalloonController(new AbstractBalloonController(mapViewWrapper.getContext()) {
             @Override
             public boolean isBalloonShowing() {
                 return findItemWithBalloon() != null;
             }
 
             @Override
             protected void showBalloon(OverlayItemBase forItem, Animation animation) {
                 mapViewWrapper.getView().getMapController().showBalloon(getResultItem((T) forItem).getBalloonItem());
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
        for (Iterator<OverlayItem> iter = getResultOverlay().getOverlayItems().iterator(); iter.hasNext();) {
            OverlayItem item = iter.next();
            if (item.getBalloonItem().isVisible()) {
                return item;
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
