 package net.yebaihe.puzzle;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 import net.yebaihe.puzzle.MultiTouchController.PointInfo;
 import net.yebaihe.puzzle.PhotoSorterView.Img;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Display;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 public class PuzzleActivity extends Activity implements PhotoSorterDelegate {
 	
 	class MyDragableView extends ImageView implements DraggableView{
 		
 		private Bitmap bitmap;
 
 		public MyDragableView(Bitmap bitmap) {
 			super(PuzzleActivity.this);
 			//this.setDrawingCacheEnabled(true);
 			this.setImageBitmap(bitmap);
 			this.setScaleType(ImageView.ScaleType.FIT_XY);
 			this.bitmap=bitmap;
 		}
 
 		@Override
 		public void beforeDrag() {
 		}
 
 		@Override
 		public DragView createDragView() {
 			return new DragView(PuzzleActivity.this,bitmap);
 		}
 
 		@Override
 		public Object getDraggedInfo() {
 			return null;
 		}
 
 		@Override
 		public void afterDrop(float x, float y) {
 			x=x-p.getLeft();
 			y=y-p.getTop();
 			
 			if (new Rect(0,0,p.getWidth(),p.getHeight()).contains((int)x+DeltaWidth/2, (int)y+DeltaHeight/2)){
 
 				if (x<0) x=0;
 				if (y<0) y=0;
 				if (x>p.getWidth()-DeltaWidth) x=p.getWidth()-DeltaWidth;
 				if (y>p.getHeight()-DeltaHeight) y=p.getHeight()-DeltaHeight;
 				
 				
 				float dstx=x+DeltaWidth/2;
 				float dsty=y+DeltaHeight/2;
 				
 				p.addImage(bitmap,PuzzleActivity.this.getResources(),dstx,dsty);
				bitmaps.remove(bitmap);
 				int sel=g.getSelectedItemPosition();
 				g.setAdapter(adapt);
 				if (sel<g.getAdapter().getCount()){
 					g.setSelection(sel);
 				}
 				else{
 					if (adapt.getCount()>0){
 						g.setSelection(g.getAdapter().getCount()/2);
 					}
 				}
 			}
 		}
 		
 	}
 	public  int DeltaWidth = 0;
 	public  int DeltaHeight = 0;
	public ArrayList<Bitmap> bitmaps=new ArrayList<Bitmap>();
	private HashMap<Bitmap,Integer>bmphash=new HashMap<Bitmap,Integer>();
 	
 	class ImageAdapter extends BaseAdapter{
 		private Context mContext;  
 		
 		public int getBitmapHash(Bitmap b){
 			return bmphash.get(b);
 		}
 		
 		public ImageAdapter(Context c) {
 			mContext = c;  
 			Bitmap b=BitmapFactory.decodeResource(getResources(), R.drawable.r01);
 			int w=b.getWidth()/3;
 			int h=b.getHeight()/3;
 			for (int i=0;i<9;i++){
 				Bitmap bmp=Bitmap.createBitmap(w, h, Config.ARGB_8888);
 				Canvas cv = new Canvas(bmp); 
 				cv.drawBitmap(b, new Rect((i%3)*w,(i/3)*h,(i%3+1)*w,(i/3+1)*h), new Rect(0,0,w,h), null);
 				Bitmap piece=Bitmap.createScaledBitmap(bmp, DeltaWidth, DeltaHeight, true);
 				bitmaps.add(piece);
 				bmphash.put(piece, i);
 			}
 			Collections.shuffle(bitmaps);
 		}
 
 		@Override
 		public int getCount() {
 			return bitmaps.size(); 
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return position;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public View getView(int position, View arg1, ViewGroup arg2) {
 			MyDragableView i = new MyDragableView(bitmaps.get(position)); 
 			i.setLayoutParams(new Gallery.LayoutParams(150, 150));
 			i.setScaleType(ImageView.ScaleType.FIT_XY);
 			return i;  
 		}
 
 	}
 	
 	@Override
 	protected
 	void onResume(){
 		super.onResume();
 		p.loadImages(this);
 	}
 	@Override
 	protected
 	void onStop(){
 		super.onStop();
 		p.unloadImages();
 	}
 	
 	PhotoSorterView p;
 	DraggableItemGallery g;
 	ImageAdapter adapt;
 	private boolean inited=false;
 	
 	class LinearLayoutChange extends LinearLayout{
 		public LinearLayoutChange(Context context) {
 			super(context);
 		}
 		
 		@Override
 	    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld)
 	    {
 			layoutSizeChange();
 	    }
 	}
 	
 	LinearLayoutChange ll;
 	private boolean initedmargin=false;
 	LinearLayout toplevel;
 	LinearLayout cnt;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         Display display = getWindowManager().getDefaultDisplay();
 		DeltaWidth=display.getWidth()/3;
 		DeltaHeight=DeltaWidth;//display.getHeight()/3;
         
         p=new PhotoSorterView(this);
         p.delegate=this;
         
         g = new DraggableItemGallery(this);
         g.setSpacing(3);
        adapt=new ImageAdapter(this);
        g.setAdapter(adapt);
		if (g.getAdapter().getCount()>0) g.setSelection(g.getAdapter().getCount()/2);
         
         cnt=(LinearLayout)this.findViewById(R.id.cnt);
         ll=new LinearLayoutChange(this);
         
         ll.setBackgroundResource(R.drawable.frame);
         
         
         cnt.addView(ll,new LinearLayoutChange.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
         toplevel=(LinearLayout)this.findViewById(R.id.toplevel);
         
         LinearLayout lad=(LinearLayout)this.findViewById(R.id.ad);
         int adheight=72;
         if (display.getWidth()>=800){
         	adheight=120;
         }
         else if (display.getWidth()>=600){
        	adheight=90;
         }
         toplevel.updateViewLayout(lad, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,adheight));
         
        toplevel.addView(g,1, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));  
         
     }
 	public void layoutSizeChange() {
 		if (initedmargin) return;
 		initedmargin=true;
 		
 		Bitmap frame=BitmapFactory.decodeResource(getResources(), R.drawable.frame);
 		int widthframe=ll.getWidth()*33/frame.getWidth();
 		int heightframe=ll.getHeight()*49/frame.getHeight();
 		
         LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT);
         lp.setMargins(widthframe, heightframe, widthframe, heightframe);
         ll.addView(p,lp);
 		
 	}
 	@Override
 	public void imagePositionChange(Img img, float x, float y,PointInfo touchPoint) {
 		int pos=adapt.getBitmapHash(img.bitmap);
 		int goodx=(pos % 3) * DeltaWidth;
 		int goody=(pos / 3) * DeltaHeight;
 		int curx=(int) (x-DeltaWidth/2);
 		int cury=(int) (y-DeltaHeight/2);
 		int delta=Math.max(Math.abs(goodx-curx),Math.abs(goody-cury));
 		
 		if (delta<15){			
 			int setx=goodx+DeltaWidth/2;
 			int sety=goody+DeltaHeight/2;
 			p.setImgPosition(img,setx,sety,true);
 		}
 		else{
 			if ((curx<0) || (curx>p.getWidth()-DeltaWidth) || (cury<0) || (cury>p.getHeight()-DeltaHeight)){
 				if (curx<0) curx=0;
 				if (cury<0) cury=0;
 				if (curx>p.getWidth()-DeltaWidth) curx=p.getWidth()-DeltaWidth;
 				if (cury>p.getHeight()-DeltaHeight) cury=p.getHeight()-DeltaHeight;
 				int setx=curx+DeltaWidth/2;
 				int sety=cury+DeltaHeight/2;
 				p.setImgPosition(img,setx,sety,false);
 			}
 		}
 		
 		if (p.allFixed()){
 			//TODO next level!
 		}
 	}
 	
 	@Override
 	public void sizeChanged() {
 		if (!initedmargin) return;
 		if (inited) return;
 		inited=true;
 		
 		DeltaWidth=p.getWidth()/3;
 		DeltaHeight=p.getHeight()/3;
 		
 		toplevel.updateViewLayout(cnt, new LinearLayout.LayoutParams(cnt.getWidth(),cnt.getHeight()));
 		
		for (int i=0;i<bitmaps.size();i++){
 			int hash=bmphash.get(bitmaps.get(i));
 			bitmaps.set(i, Bitmap.createScaledBitmap(bitmaps.get(i), DeltaWidth, DeltaHeight, true));
 			bmphash.put(bitmaps.get(i), hash);
		}
		g.setAdapter(new ImageAdapter(this));
         if (adapt.getCount()>0) g.setSelection(adapt.getCount()/2);
 	}
 }
