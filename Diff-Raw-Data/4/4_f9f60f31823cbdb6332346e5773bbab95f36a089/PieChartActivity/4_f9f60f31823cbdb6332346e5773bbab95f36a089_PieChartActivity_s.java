 package no.kantega.android.afp;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.*;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import no.kantega.android.afp.controllers.Transactions;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class PieChartActivity extends Activity {
 
     private List<PieItem> PieData = new ArrayList<PieItem>();
     private Transactions db;
     private Cursor cursor;
     private int maxCount = 0;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.pie);
        String month = getIntent().getExtras().getString("Month");
        String year = getIntent().getExtras().getString("Year");
         this.db = new Transactions(getApplicationContext());
         this.cursor = db.getCursorTags(month, year);
         createPieDataFromCursor();
         int overlayId = R.drawable.cam_overlay_big;
         int size = 480;
         int bgColor = 0xffa1a1a1;
         Bitmap backgroundImage = Bitmap.createBitmap(size, size + 250, Bitmap.Config.RGB_565);
         PieChart pieChart = new PieChart(this);
         pieChart.setLayoutParams(new ViewGroup.LayoutParams(size, size));
         pieChart.setGeometry(size, size, 5, 5, 5, 5, overlayId);
         pieChart.setSkinParams(bgColor);
         pieChart.setData(PieData, maxCount);
         pieChart.invalidate();
         pieChart.draw(new Canvas(backgroundImage));
         pieChart = null;
         ImageView imageView = new ImageView(this);
         imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
         imageView.setBackgroundColor(bgColor);
         imageView.setImageBitmap(backgroundImage);
         LinearLayout targetPieView = (LinearLayout) findViewById(R.id.pie_container);
         targetPieView.addView(imageView);
     }
 
     private void createPieDataFromCursor() {
         PieItem item;
         Random numGen = new Random();
         cursor.moveToFirst();
         while (cursor.isAfterLast() == false) {
             item = new PieItem();
             item.setCount((int) cursor.getDouble(cursor.getColumnIndex("sum")));
             String tag = cursor.getString(cursor.getColumnIndex("tag"));
             item.setLabel(tag);
             item.setColor(0xff000000 + 256 * 256 * numGen.nextInt(256) + 256 * numGen.nextInt(256) + numGen.nextInt(256));
             PieData.add(item);
             maxCount += item.getCount();
             cursor.moveToNext();
         }
         cursor.close();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         if (cursor != null && !cursor.isClosed()) {
             cursor.close();
         }
         db.close();
     }
 
     private class PieChart extends View {
 
         private static final int WAIT = 0;
         private static final int IS_READY_TO_DRAW = 1;
         private static final int IS_DRAW = 2;
         private static final float START_INC = 30;
         private Paint bgPaints = new Paint();
         private Paint linePaints = new Paint();
         private Paint textPaints = new Paint();
         private String tag = null;
         private int overlayId;
         private int width;
         private int height;
         private int gapLeft;
         private int gapRight;
         private int gapTop;
         private int gapBottom;
         private int bgColor;
         private int DESCRIPTION_MARGIN_LEFT = 75;
         private int DESCRIPTION_MARGIN_TOP = 50;
         private int COLUMN_MARGIN_WIDTH = 150;
         private int DESCRIPTION_CIRCLE_RADIUS = 10;
         private int CIRCLE_MARGIN_LEFT = 15;
         private int state = WAIT;
         private float start;
         private float sweep;
         private int maxConnection;
         private List<PieItem> dataArray;
 
         public PieChart(Context context) {
             super(context);
         }
 
         public PieChart(Context context, AttributeSet attrs) {
             super(context, attrs);
         }
 
         @Override
         protected void onDraw(Canvas canvas) {
             super.onDraw(canvas);
             if (state != IS_READY_TO_DRAW) return;
             canvas.drawColor(bgColor);
             bgPaints.setAntiAlias(true);
             bgPaints.setStyle(Paint.Style.FILL);
             bgPaints.setColor(0x88FF0000);
             bgPaints.setStrokeWidth(0.5f);
             linePaints.setAntiAlias(true);
             linePaints.setStyle(Paint.Style.STROKE);
             linePaints.setColor(0xff000000);
             linePaints.setStrokeWidth(0.5f);
             linePaints.setTextSize(15);
             textPaints.setAntiAlias(true);
             textPaints.setStyle(Paint.Style.FILL_AND_STROKE);
             textPaints.setStrokeWidth(0.5f);
             textPaints.setColor(Color.BLACK);
             textPaints.setTextSize(20);
             RectF ovals = new RectF(gapLeft, gapTop, width - gapRight, height - gapBottom);
             start = START_INC;
             float lblX;
             float lblY;
             String LblPercent;
             float Percent;
             DecimalFormat FloatFormatter = new DecimalFormat("0.## %");
             float CenterOffset = (width / 2); // Pie Center from Top-Left origin
             float Conv = (float) (2 * Math.PI / 360);     // Constant for convert Degree to rad.
             float Radius = 2 * (width / 2) / 3;     // Radius of the circle will be drawn the legend.
             Rect bounds = new Rect();
             PieItem item;
             for (int i = 0; i < dataArray.size(); i++) {
                 item = (PieItem) dataArray.get(i);
                 bgPaints.setColor(item.getColor());
                 sweep = (float) 360 * ((float) item.getCount() / (float) maxConnection);
                 canvas.drawArc(ovals, start, sweep, true, bgPaints);
                 canvas.drawArc(ovals, start, sweep, true, linePaints);
                 Percent = (float) item.getCount() / (float) maxConnection;
                 sweep = (float) 360 * Percent;
                 // Format Label
                 LblPercent = FloatFormatter.format(Percent);
                 // Get Label width and height in pixels
                 linePaints.getTextBounds(LblPercent, 0, LblPercent.length(), bounds);
                 // Claculate final coords for Label
                 lblX = (float) ((float) CenterOffset + Radius * Math.cos(Conv * (start + sweep / 2))) - bounds.width() / 2;
                 lblY = (float) ((float) CenterOffset + Radius * Math.sin(Conv * (start + sweep / 2))) + bounds.height() / 2;
                 // Dwraw Label on Canvas
                 canvas.drawText(LblPercent, lblX, lblY, textPaints);
                 canvas.drawCircle(DESCRIPTION_MARGIN_LEFT, height + DESCRIPTION_MARGIN_TOP, DESCRIPTION_CIRCLE_RADIUS, bgPaints);
                 tag = item.getLabel();
                 if (tag == null) {
                     tag = "Ukategorisert";
                 }
                 if (i % 2 == 0) {
                     canvas.drawText(tag, DESCRIPTION_MARGIN_LEFT + CIRCLE_MARGIN_LEFT, height + DESCRIPTION_MARGIN_TOP, textPaints);
                     DESCRIPTION_MARGIN_LEFT += COLUMN_MARGIN_WIDTH;
                 } else {
                     canvas.drawText(tag, DESCRIPTION_MARGIN_LEFT + CIRCLE_MARGIN_LEFT, height + DESCRIPTION_MARGIN_TOP, textPaints);
                     DESCRIPTION_MARGIN_TOP += 40;
                     DESCRIPTION_MARGIN_LEFT -= COLUMN_MARGIN_WIDTH;
                 }
                 start += sweep;
             }
             BitmapFactory.Options options = new BitmapFactory.Options();
             options.inScaled = false;
             Bitmap OverlayBitmap = BitmapFactory.decodeResource(getResources(), overlayId, options);
             int overlay_width = OverlayBitmap.getWidth();
             int overlay_height = OverlayBitmap.getHeight();
             float scaleWidth = (((float) width) / overlay_width) * 0.678899083f;
             float scaleHeight = (((float) height) / overlay_height) * 0.678899083f;
             Matrix matrix = new Matrix();
             matrix.postScale(scaleWidth, scaleHeight);
             Bitmap resizedBitmap = Bitmap.createBitmap(OverlayBitmap, 0, 0, overlay_width, overlay_height, matrix, true);
             //canvas.drawBitmap(resizedBitmap, 0.0f, 0.0f, null);
             state = IS_DRAW;
         }
 
         public void setGeometry(int width, int height, int gapLeft, int gapRight, int gapTop, int gapBottom, int overlayId) {
             this.width = width;
             this.height = height;
             this.gapLeft = gapLeft;
             this.gapRight = gapRight;
             this.gapTop = gapTop;
             this.gapBottom = gapBottom;
             this.overlayId = overlayId;
         }
 
         public void setSkinParams(int bgColor) {
             this.bgColor = bgColor;
         }
 
         public void setData(List<PieItem> data, int maxConnection) {
             dataArray = data;
             this.maxConnection = maxConnection;
             state = IS_READY_TO_DRAW;
         }
 
         public void setState(int state) {
             this.state = state;
         }
 
         public int getColorValue(int index) {
             if (dataArray == null) return 0;
             if (index < 0) {
                 return ((PieItem) dataArray.get(0)).getColor();
             } else if (index >= dataArray.size()) {
                 return ((PieItem) dataArray.get(dataArray.size() - 1)).getColor();
             } else {
                 return ((PieItem) dataArray.get(dataArray.size() - 1)).getColor();
             }
         }
     }
 
     private class PieItem {
 
         private int count;
         private String label;
         private int color;
 
         public String getLabel() {
             return label;
         }
 
         public void setLabel(String label) {
             this.label = label;
         }
 
         public int getColor() {
             return color;
         }
 
         public void setColor(int color) {
             this.color = color;
         }
 
         public int getCount() {
             return count;
         }
 
         public void setCount(int count) {
             this.count = count;
         }
     }
 }
