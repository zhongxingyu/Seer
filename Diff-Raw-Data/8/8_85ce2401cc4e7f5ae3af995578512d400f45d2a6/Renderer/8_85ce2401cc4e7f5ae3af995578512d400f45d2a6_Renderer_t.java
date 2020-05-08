 package com.photobox.world;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 
 public class Renderer {
 
     private GraphicalDebugger debugger;
     private WorldMapping mapping;
     private PhotoCollection collection;
 
     public Renderer(GraphicalDebugger debugger, WorldMapping mapping, PhotoCollection collection) {
         this.debugger = debugger;
         this.mapping = mapping;
         this.collection = collection;
     }
 
     public void renderBackground(Canvas canvas) {
         Paint bgPaint = new Paint();
         bgPaint.setARGB(255, 255, 255, 0);
         canvas.drawPaint(bgPaint);
     }
 
     public void renderPhoto(Canvas canvas, Photo photo) {
         Bitmap image = photo.image;
         float w = photo.width;
         float h = photo.height;
 
         Paint borderPaint = new Paint();
         borderPaint.setARGB(255, 255, 255, 255);
        borderPaint.setAntiAlias(true);

        Paint photoPaint = new Paint();
        photoPaint.setAntiAlias(true);
        photoPaint.setFilterBitmap(true);
 
         canvas.drawRect(-w / 2, -h / 2, w / 2, h / 2, borderPaint);
        canvas.drawBitmap(image, -w / 2 + photo.BORDER, -h / 2 + photo.BORDER, photoPaint);
         debugger.drawCenterPoint(canvas);
         debugger.printAngle(canvas, photo);
     }
 
     public void onDraw(Canvas canvas) {
         mapping.setWorldToScreenTransformation(canvas);
         renderBackground(canvas);
         for (Photo photo : collection.getPhotos()) {
             canvas.save();
             mapping.setPhotoToViewportTransformation(canvas, photo);
             renderPhoto(canvas, photo);
             canvas.restore();
         }
         debugger.renderAxis(canvas);
         debugger.renderFingers(canvas);
     }
 }
