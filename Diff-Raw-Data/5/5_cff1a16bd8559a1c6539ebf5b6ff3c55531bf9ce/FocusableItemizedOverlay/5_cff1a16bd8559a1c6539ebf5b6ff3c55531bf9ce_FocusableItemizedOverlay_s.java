 package fr.itinerennes.ui.views.overlays;
 
 import org.andnav.osm.views.OpenStreetMapView;
 import org.andnav.osm.views.overlay.OpenStreetMapViewItemizedOverlay;
 import org.slf4j.Logger;
 import org.slf4j.impl.ItinerennesLoggerFactory;
 
 import android.graphics.Canvas;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.view.View;
 
 import fr.itinerennes.ui.activity.ITRContext;
 import fr.itinerennes.ui.adapter.ItemizedOverlayAdapter;
 import fr.itinerennes.ui.adapter.MapBoxAdapter;
 import fr.itinerennes.ui.views.MapBoxView;
 
 /**
  * An enhanced {@link OpenStreetMapViewItemizedOverlay} to handle focus of one of its elements.
  * 
  * @param <T>
  *            the type of the items of this overlay
  * @param <D>
  *            the type of the bundled data with each items of this overlay
  * @author Jérémie Huchet
  * @author Olivier Boudet
  */
 public class FocusableItemizedOverlay<T extends FocusableOverlayItem<D>, D> extends
         ItemizedOverlay<T, D> implements FocusableOverlay<D> {
 
     /** The event logger. */
     private static final Logger LOGGER = ItinerennesLoggerFactory
             .getLogger(FocusableItemizedOverlay.class);
 
     /** The adapter to use to display the focused item. */
     private final MapBoxAdapter<OverlayItem<D>, D> boxDisplayAdaper;
 
     /**
      * Creates the focusable itemized overlay.
      * 
      * @param context
      *            the itinerennes application context
      */
     public FocusableItemizedOverlay(final ITRContext context,
             final ItemizedOverlayAdapter<T, D> itemProviderAdapter,
             final MapBoxAdapter<OverlayItem<D>, D> boxDisplayAdaper) {
 
         // items are set using setItems() / addItem() / removeItem()
         // OnItemGestureListener is implemented by this class and shouldn't be overridden as is
         // triggers more precise event helpers (onFocusHelper() and onBlurHelper())
         super(context, itemProviderAdapter);
         this.boxDisplayAdaper = boxDisplayAdaper;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.views.overlays.FocusableOverlay#onFocus(fr.itinerennes.ui.views.MapBoxView)
      */
     @Override
     public final boolean onFocus(final MapBoxView additionalInformationView) {
 
         final T item = mItemList.get(focusedItemIndex);
         if (null != item) {
             onFocusHelper(additionalInformationView, item);
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.views.overlays.FocusableOverlay#onBlur(fr.itinerennes.ui.views.MapBoxView)
      */
     @Override
     public final boolean onBlur(final MapBoxView additionalInformationView) {
 
         if (NOT_SET != prevFocusedItemIndex) {
             final T item = mItemList.get(prevFocusedItemIndex);
             onBlurHelper(additionalInformationView, item);
         }
         prevFocusedItemIndex = NOT_SET;
         return false;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.views.overlays.FocusableOverlay#onKeepFocus(fr.itinerennes.ui.views.MapBoxView)
      */
     @Override
     public final boolean onKeepFocus(final MapBoxView additionalInformationView) {
 
         final T item = mItemList.get(focusedItemIndex);
         if (null != item) {
             onFocusHelper(additionalInformationView, item);
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.views.overlays.FocusableOverlay#hasFocus()
      */
     @Override
     public final boolean hasFocus() {
 
         return focusedItemIndex != NOT_SET;
     }
 
     /**
      * Helper function to handle focus events.
      * 
      * @param additionalInformationView
      *            the view containing additional informations about the focused item
      * @param item
      *            the newly focused item
      */
     protected void onFocusHelper(final MapBoxView additionalInformationView, final T item) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onFocusHelper.start - itemId={}", item.getId());
         }
 
         additionalInformationView.updateInBackground(boxDisplayAdaper, item);
         item.onFocus();
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onFocusHelper.end - itemId={}", item.getId());
         }
     }
 
     /**
      * Helper function to handle blur events.
      * 
      * @param additionalInformationView
      *            the view containing additional informations about the focused item
      * @param item
      *            the newly focused item
      */
     protected void onBlurHelper(final MapBoxView additionalInformationView, final T item) {
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onBlurHelper.start - itemId={}", item.getId());
         }
 
         additionalInformationView.setVisibility(View.GONE);
         item.onBlur();
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("onBlurHelpe.end - itemId={}", item.getId());
         }
     }
 
     /**
      * Workaround <code>onDraw()</code> method.
      * 
      * @see fr.itinerennes.ui.views.overlays.WrappableOverlay#onDrawOverlayFinished(android.graphics.Canvas,
      *      org.andnav.osm.views.OpenStreetMapView)
      */
     @Override
     public final void onDrawOverlay(final Canvas canvas, final OpenStreetMapView mapView) {
 
         onDraw(canvas, mapView);
     }
 
     /**
      * Workaround <code>onDrawFinished()</code> method.
      * 
      * @see fr.itinerennes.ui.views.overlays.WrappableOverlay#onDrawOverlayFinished(android.graphics.Canvas,
      *      org.andnav.osm.views.OpenStreetMapView)
      */
     @Override
     public final void onDrawOverlayFinished(final Canvas canvas, final OpenStreetMapView mapView) {
 
         onDrawFinished(canvas, mapView);
 
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.andnav.osm.views.overlay.OpenStreetMapViewItemizedOverlay#onDrawItem(android.graphics.Canvas,
      *      int, android.graphics.Point)
      */
     @Override
     protected void onDrawItem(final Canvas canvas, final int index, final Point curScreenCoords) {
 
        if (NOT_SET != index && focusedItemIndex != index) {
             LOGGER.debug("drawing item id={}", index);
             super.onDrawItem(canvas, index, curScreenCoords);
         } else if (focusedItemIndex == index) {
             this.curScreenCoords.set(curScreenCoords.x, curScreenCoords.y);
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see fr.itinerennes.ui.views.overlays.FocusableOverlay#getMapBoxAdapter()
      */
     @Override
     public MapBoxAdapter<OverlayItem<D>, D> getMapBoxAdapter() {
 
         return boxDisplayAdaper;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.andnav.osm.views.overlay.OpenStreetMapViewItemizedOverlay#onDrawFinished(android.graphics.Canvas,
      *      org.andnav.osm.views.OpenStreetMapView)
      */
     @Override
     protected void onDrawFinished(final Canvas canvas, final OpenStreetMapView osmv) {
 
         // TJHU pourquoi la bordure orange n'apparait pas ?
         // TOBO pourquoi la bordure orange n'apparait pas ?
         super.onDrawFinished(canvas, osmv);
 
         if (NOT_SET == focusedItemIndex) {
             LOGGER.debug("NOT drawing focused item id=NOT_SET");
             return;
         }
 
         // get this item's preferred marker & hotspot
         final T item = this.mItemList.get(focusedItemIndex);
 
         final Drawable marker = item.getDrawable();
         final int[] originalState = marker.getState();
         marker.setState(new int[] { android.R.attr.state_pressed });
 
         final Rect rect = new Rect();
         getItemBoundingRetangle(item, rect, curScreenCoords);
         marker.draw(canvas);
         LOGGER.debug("drawing focused item id={}, marker={}, state=" + marker.getState()[0],
                 focusedItemIndex, marker.getCurrent());
 
         // restore original state
         marker.setState(originalState);
     }
 
     /**
      * <strong>from OSMDROID</strong> Finds the bounding rectangle for the object in current
      * projection.
      * 
      * @param item
      * @param rect
      * @return
      */
     private Rect getItemBoundingRetangle(final T item, final Rect rect, final Point ctr) {
 
         final Drawable marker = (item.getMarker(0) == null) ? this.mDefaultItem.getMarker(0) : item
                 .getMarker(0);
         final Point markerHotspot = (item.getMarkerHotspot(0) == null) ? this.mDefaultItem
                 .getMarkerHotspot(0) : item.getMarkerHotspot(0);
 
         // calculate bounding rectangle
         final int markerWidth = marker.getIntrinsicWidth();
         final int markerHeight = marker.getIntrinsicHeight();
         final int left = ctr.x - markerHotspot.x;
         final int right = left + markerWidth;
         final int top = ctr.y - markerHotspot.y;
         final int bottom = top + markerHeight;
 
         rect.set(left, top, right, bottom);
         return rect;
     }
 }
