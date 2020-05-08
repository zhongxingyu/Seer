 package touchvg.demo;
 
 import touchvg.skiaview.GiContext;
 import touchvg.skiaview.GiSkiaView;
 import touchvg.view.PaintView;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class Testy extends Activity {
 	private PaintView mView;        //
 	private GiSkiaView mCoreView;   //ں
 	private Button buttonSelect;    //ѡͼΰť
 	private Button buttonClear;     //ͼΰť
 	private Button buttonRed;       //ɫʰť
 	private Button buttonBlue;      //ɫʰť
 	private Button buttonThickPen;  //ʱϸť
 	private Button buttonBoldPen;   //ʱְť
 	private Button buttonYellow;    //ɫʰť
 	private Button buttonEraser;	//Ƥť
 	private final Testy mHandler = this;  //ͼʵ
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         this.initComponent();  	//ʼťʵ
         this.bindEvent();    	//Ϊť󶨷
     }
     
     //¼
     private void bindEvent()
     {
     	buttonSelect.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
         		mHandler.onSelectShapes();
         	}
     	});
     	
     	buttonClear.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
         		mHandler.onClearShapes();
         	}
     	});
     	
     	buttonRed.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				mHandler.onPenRed();
 			}
 		});
     	
     	buttonBlue.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				mHandler.onPenBlue();
 			}
 		});
     	
     	buttonYellow.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				mHandler.onPenYellow();
 			}
 		});
     	
     	buttonBoldPen.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				mHandler.onPenBold();
 			}
 		});
     	
     	buttonThickPen.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				mHandler.onPenThick();
 			}
 		});
     	
     	buttonEraser.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				mHandler.onEraser();
 			}
 		});
     }
 
     private void onPenYellow() {
     	mCoreView.setCommandName("splines");
    	GiContext ctx = mCoreView.getCurrentContext(false);
     	ctx.setLineColor(255, 255, 0);
     	mCoreView.applyContext(ctx, 1, 1);
     	ctx.delete();
     }
     
     private void onPenBlue() {
     	mCoreView.setCommandName("splines");
    	GiContext ctx = mCoreView.getCurrentContext(false);
     	ctx.setLineColor(0, 0, 255);
     	mCoreView.applyContext(ctx, 1, 1);
     	ctx.delete();
     }
     
     private void onPenRed() {
     	mCoreView.setCommandName("splines");
    	GiContext ctx = mCoreView.getCurrentContext(false);
     	ctx.setLineColor(255, 0, 0);
     	mCoreView.applyContext(ctx, 1, 1);
     	ctx.delete();
     }
     
     private void onSelectShapes() {
     	mCoreView.setCommandName("select");
     }
     
     private void onClearShapes() {
     	mCoreView.loadShapes(null);
     	mCoreView.setCommandName("splines");
     }
     
     private void onPenBold() {
     	GiContext ctx = mCoreView.getCurrentContext(true);
     	ctx.setLineWidth(ctx.getLineWidth() < 0 ? ctx.getLineWidth() - 1 : -1);
     	mCoreView.applyContext(ctx, 4, 1);
     	ctx.delete();
     }
     
     private void onPenThick() {
     	GiContext ctx = mCoreView.getCurrentContext(true);
     	ctx.setLineWidth(ctx.getLineWidth() < 0 ? ctx.getLineWidth() + 1 : -1);
     	mCoreView.applyContext(ctx, 4, 1);
     	ctx.delete();
     }
     
     private void onEraser() {
     	mCoreView.setCommandName("erase");
     }
 
     private void initComponent()
     {
     	mView = (PaintView) this.findViewById(R.id.paintView);
     	mCoreView = mView.getCoreView();
     	buttonSelect  = (Button) this.findViewById(R.id.selectshapes_button);
     	buttonClear = (Button) this.findViewById(R.id.clearshapes_button);
     	buttonRed = (Button) this.findViewById(R.id.redPen_button);
     	buttonBlue = (Button) this.findViewById(R.id.bluePen_button);
     	buttonYellow = (Button) this.findViewById(R.id.yellowPen_button);
     	buttonBoldPen = (Button) this.findViewById(R.id.boldPen_button);
     	buttonThickPen = (Button) this.findViewById(R.id.thickPen_button);
     	buttonEraser = (Button) this.findViewById(R.id.eraser_button);
     }
 }
