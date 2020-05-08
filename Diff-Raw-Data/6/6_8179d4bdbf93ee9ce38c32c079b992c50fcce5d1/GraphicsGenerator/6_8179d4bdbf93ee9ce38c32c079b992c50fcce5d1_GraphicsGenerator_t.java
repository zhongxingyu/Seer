 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle;
 
 import com.google.code.geobeagle.database.Tag;
 import com.google.code.geobeagle.database.TagWriterImpl;
 import com.google.inject.Inject;
 
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 
 public class GraphicsGenerator {
     public static class AttributePainter {
         private final Paint mTempPaint;
         private final Rect mTempRect;
 
         @Inject
        public AttributePainter() {
            mTempPaint = new Paint();
            mTempRect = new Rect();
         }
 
         void paintAttribute(int position, int bottom, int imageHeight, int imageWidth,
                 Canvas canvas, double attribute, int color) {
             final int diffWidth = (int)(imageWidth * (attribute / 10.0));
             final int MARGIN = 1;
             final int THICKNESS = 3;
             final int base = imageHeight - bottom - MARGIN;
             final int attributeBottom = base - position * (THICKNESS + 1);
             final int attributeTop = attributeBottom - THICKNESS;
             mTempPaint.setColor(color);
             mTempRect.set(0, attributeTop, diffWidth, attributeBottom);
             canvas.drawRect(mTempRect, mTempPaint);
         }
     }
 
     public static class RatingsGenerator {
         Drawable createRating(Drawable unselected, Drawable halfSelected, Drawable selected,
                 int rating) {
             int width = unselected.getIntrinsicWidth();
             int height = unselected.getIntrinsicHeight();
             Bitmap bitmap = Bitmap.createBitmap(5 * width, 16, Bitmap.Config.ARGB_8888);
 
             Canvas c = new Canvas(bitmap);
             int i = 0;
             while (i < rating / 2) {
                 draw(width, height, c, i++, selected);
             }
             if (rating % 2 == 1) {
                 draw(width, height, c, i++, halfSelected);
             }
             while (i < 5) {
                 draw(width, height, c, i++, unselected);
             }
             return new BitmapDrawable(bitmap);
         }
 
         void draw(int width, int height, Canvas c, int i, Drawable drawable) {
             drawable.setBounds(width * i, 0, width * (i + 1) - 1, height - 1);
             drawable.draw(c);
         }
 
     }
 
     public static class RatingsArray {
         private final RatingsGenerator mRatingsGenerator;
 
         @Inject
         RatingsArray(RatingsGenerator ratingsGenerator) {
             mRatingsGenerator = ratingsGenerator;
         }
 
         public Drawable[] getRatings(Drawable[] drawables, int maxRatings) {
             Drawable[] ratings = new Drawable[maxRatings];
             for (int i = 1; i <= maxRatings; i++) {
                 ratings[i - 1] = mRatingsGenerator.createRating(drawables[0], drawables[1],
                         drawables[2], i);
             }
             return ratings;
         }
     }
 
     public interface BitmapCopier {
         Bitmap copy(Bitmap source);
 
         int getBottom();
 
         Drawable getDrawable(Bitmap icon);
     }
 
     public static class ListViewBitmapCopier implements BitmapCopier {
         public Bitmap copy(Bitmap source) {
             int imageHeight = source.getHeight();
             int imageWidth = source.getWidth();
 
             Bitmap copy = Bitmap.createBitmap(imageWidth, imageHeight + 5, Bitmap.Config.ARGB_8888);
             int[] pixels = new int[imageWidth * imageHeight];
             source.getPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);
             copy.setPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight);
             return copy;
         }
 
         public int getBottom() {
             return 0;
         }
 
         public Drawable getDrawable(Bitmap icon) {
             return new BitmapDrawable(icon);
         }
 
     }
 
     public static class MapViewBitmapCopier implements BitmapCopier {
         public Bitmap copy(Bitmap source) {
             return source.copy(Bitmap.Config.ARGB_8888, true);
         }
 
         public int getBottom() {
             return 3;
         }
 
         public Drawable getDrawable(Bitmap icon) {
             Drawable iconMap = new BitmapDrawable(icon);
             int width = iconMap.getIntrinsicWidth();
             int height = iconMap.getIntrinsicHeight();
             iconMap.setBounds(-width / 2, -height, width / 2, 0);
             return iconMap;
         }
     }
 
     public static interface IconOverlay {
         void draw(Canvas canvas);
     }
 
     public static class IconOverlayImpl implements IconOverlay {
         private final Drawable mOverlayIcon;
 
         public IconOverlayImpl(Drawable overlayIcon) {
             mOverlayIcon = overlayIcon;
         }
 
         @Override
         public void draw(Canvas canvas) {
             if (mOverlayIcon != null) {
                 mOverlayIcon.setBounds(0, 0, mOverlayIcon.getIntrinsicHeight() - 1, mOverlayIcon
                         .getIntrinsicHeight() - 1);
                 mOverlayIcon.draw(canvas);
             }
         }
     }
 
     public static class NullIconOverlay implements IconOverlay {
         @Override
         public void draw(Canvas canvas) {
         }
     }
 
     public static interface AttributesPainter {
         void paintAttributes(int difficulty, int terrain, Bitmap copy, Canvas canvas) ;
     }
     
     public static class DifficultyAndTerrainPainter implements AttributesPainter {
         private final AttributePainter mAttributePainter;
 
         @Inject
         public DifficultyAndTerrainPainter(AttributePainter attributePainter) {
             mAttributePainter = attributePainter;
         }
 
         @Override
         public void paintAttributes(int difficulty, int terrain, Bitmap copy, Canvas canvas) {
             int imageHeight = copy.getHeight();
             int imageWidth = copy.getWidth();
             mAttributePainter.paintAttribute(1, 0, imageHeight, imageWidth, canvas, difficulty,
                     Color.rgb(0x20, 0x20, 0xFF));
             mAttributePainter.paintAttribute(0, 0, imageHeight, imageWidth, canvas, terrain, Color
                     .rgb(0xDB, 0xA1, 0x09));
         }
     }
 
     public static class IconRenderer {
         private final Resources mResources;
 
         @Inject
         public IconRenderer(Resources resources) {
             mResources = resources;
         }
 
         public Drawable renderIcon(int difficulty, int terrain, int backdropId,
                 IconOverlay iconOverlay, BitmapCopier bitmapCopier,
                 AttributesPainter attributesPainter) {
             Bitmap bitmap = BitmapFactory.decodeResource(mResources, backdropId);
             Bitmap copy = bitmapCopier.copy(bitmap);
             Canvas canvas = new Canvas(copy);
             attributesPainter.paintAttributes(difficulty, terrain, copy, canvas);
             iconOverlay.draw(canvas);
 
             return bitmapCopier.getDrawable(copy);
         }
     }
 
     public static class IconOverlayFactory {
         private final TagWriterImpl mTagWriter;
         private final Resources mResources;
 
         @Inject
         public IconOverlayFactory(TagWriterImpl tagWriterImpl, Resources resources) {
             mTagWriter = tagWriterImpl;
             mResources = resources;
         }
 
         public IconOverlay create(Geocache geocache, boolean fBig) {
             if (mTagWriter.hasTag(geocache.getId(), Tag.FOUND))
                 return new IconOverlayImpl(
                         mResources.getDrawable(fBig ? R.drawable.overlay_found_big
                                 : R.drawable.overlay_found));
             else if (mTagWriter.hasTag(geocache.getId(), Tag.DNF))
                 return new IconOverlayImpl(mResources.getDrawable(fBig ? R.drawable.overlay_dnf_big
                         : R.drawable.overlay_dnf));
             return new NullIconOverlay();
         }
     }
 
 }
