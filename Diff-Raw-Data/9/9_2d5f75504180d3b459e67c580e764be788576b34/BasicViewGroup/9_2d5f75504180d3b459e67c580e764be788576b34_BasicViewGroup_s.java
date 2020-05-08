 package com.time.master.view;
 
 import com.time.master.TimeMasterApplication;
 import com.time.master.interfacer.LayoutStyleableInterface;
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.ViewGroup;
 
 
 /**
  * Զ岼࣬
  * 趨view֮
  * ĻСviewĳ
  * @author Desmond
  *
  */
 public class BasicViewGroup extends ViewGroup{
 
 	protected int screen_width,
 	screen_height,
 	unit_width,//view λ
 	gap,//viewļ
 	current_margin_top=0,//ǰy
 	current_margin_left=0,//ǰx
 	screen_mode; //1  2
 	
 	public BasicViewGroup(Context context) {
 		super(context);
 		init();
 	}
 	
 	public BasicViewGroup(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 	
 	public BasicViewGroup(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 	
 	
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		init();
 		int num=this.getChildCount();
 		int line=0;
 		int width=0;
 		int max_width=0;//ȵ
 		for(int i=0;i<num;i++){
 			View view=this.getChildAt(i);
 			LayoutStyleableInterface styleable=(LayoutStyleableInterface)view;
 			if(styleable.isNewLine()){
 				if(width>max_width)
 					max_width=width;
 				width=0;
 				line++;
 			}
			width+=unit_width+gap;
 		}
		
 		int height=line==0?0:(int)(line*unit_width*0.75+gap*(line+1));
 		
 		setMeasuredDimension(max_width,height);
 		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 	}
 	
 	@Override
 	protected void onLayout(boolean changed, int l, int t, int r, int b) {
 		init();
 		int num=this.getChildCount();
 		for(int i=0;i<num;i++){
 			View view=this.getChildAt(i);
 			LayoutStyleableInterface styleable=(LayoutStyleableInterface)view;
 			if(styleable.isNewLine()){
 				current_margin_top+=0.75*unit_width+gap;
 				current_margin_left=gap;
 			}
 			ViewGroup.LayoutParams layoutParams=view.getLayoutParams();
 			layoutParams.width=styleable.getMultiWidth()*unit_width+(styleable.getMultiWidth()-1)*gap;
 			layoutParams.height=(int)(unit_width*0.75);
 			
 			view.setLayoutParams(layoutParams);
 			/***view СviewonMeasureص **/
 			view.measure(
 					MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY), 
 					MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.EXACTLY));
 			view.layout(current_margin_left, 
 					current_margin_top, 
 					current_margin_left+layoutParams.width, 
 					current_margin_top+layoutParams.height);
 			current_margin_left+=layoutParams.width+gap;
 
 		}
 
 		
 	}
 
 	/***
 	 * ʼв
 	 */
 	protected void init(){
 		screen_mode=TimeMasterApplication.getInstance().getScreenMode();
 		screen_width=TimeMasterApplication.getInstance().getScreen_W();
 		screen_height=TimeMasterApplication.getInstance().getScreen_H();
 		unit_width=screen_width/6;
 		gap=screen_width/36;
 		current_margin_top=(int)(-0.75*unit_width);
 	}
 	
 	
 
 }
