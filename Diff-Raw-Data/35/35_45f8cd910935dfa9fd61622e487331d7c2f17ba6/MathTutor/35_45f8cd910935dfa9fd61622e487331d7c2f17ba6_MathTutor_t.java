 /*
  * Chris Card
  * Ben Gillman
  * 10/18/2012
  * This is the main class
  */
 package csci422.ccard.androidmathtutor;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Point;
import android.util.Log;
 import android.view.Display;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.Toast;
 
 @TargetApi(13)
 public class MathTutor extends Activity {
 
     private FrameLayout frame;
     private double height;
     private double width;
 
     private ArrayList<Targets> icons;
 
     private final int imag[] = {R.drawable.zero, R.drawable.one, R.drawable.two, R.drawable.three ,R.drawable.four ,R.drawable.five, R.drawable.six, R.drawable.seven, R.drawable.eight, R.drawable.nine};
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_math_tutor);
 
         initIcons();//inits icons
         frame = (FrameLayout)findViewById(R.id.graphics_holder);
         ImageView image = new ImageView(this);
         frame.addView(image);//addes the image view to the fram
     }
 
     /**
      * This initailizes the icons list so with 6 icons
      */
     private void initIcons()
     {
         Display dis = getWindowManager().getDefaultDisplay();
         Point size = new Point();
         dis.getSize(size);
         width = size.x/3.0;
         height = size.y/5.0;
         Point loc = new Point();
         loc.set(20, 20);
         icons = new ArrayList<Targets>();
 
         Targets answer = new Targets(R.drawable.answerbox,new Point((int)(size.x/2) + 200,size.y-300),this);
         answer.isAnswer();
         icons.add(answer);
 
         icons.add(new Targets(imag[0], loc, this));
         
         for (int i = 1; i < 10; i++) 
         {
             if (i%3 == 0) 
             {
                loc = new Point(20, (int)(loc.y + 80));
                icons.add(new Targets(imag[i], loc, this));
             }
             else
             {
                loc = new Point((int)(loc.x + width), loc.y);
                icons.add(new Targets(imag[i], loc, this));
             }
            
         }
     }
 
     /**
      * This class captures the users touches on the screen
      * @author Chris card
      *
      */
     private class GestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener
     {
         ImageView view;
 
         public GestureListener(ImageView view)
         {
             this.view = view;
         }
 
         public boolean onDoubleTap(MotionEvent arg0) {
             return true;
         }
 
         public boolean onDoubleTapEvent(MotionEvent e) {
             
             return false;
         }
 
         public boolean onSingleTapConfirmed(MotionEvent e) {
             //when the user touchs the screen it try to find the icon touched
             
             return false;
         }
 
         public boolean onDown(MotionEvent e) {
             
            // for(Targets t : icons)
            // {
            //     if (t.hasPoint(new Point((int)e.getX(),(int)e.getY()))) 
            //     {
            //         t.clicked(R.drawable.orangehighlight, MathTutor.this);
            //         view.onSelected();
            //         break;
            //     }
            // }
             return true;
         }
 
         public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX,
                 final float velocityY) {
             
             return false;
         }
 
         public void onLongPress(MotionEvent e) {
             // TODO Auto-generated method stub
             
         }
 
         public boolean onScroll(MotionEvent e1, MotionEvent e2,
                 float distanceX, float distanceY) {
             Targets temp = null;
             for(Targets t : icons)
             {
                 if (t.hasPoint(new Point((int)e1.getX(),(int)e1.getY()))) 
                 {
                     temp = t;
                     break;
                 }
             }
             if(temp != null)
             {
                 temp.move(-distanceX,-distanceY);
                 view.onSelected();
                 
                 Point fin = new Point((int)(e1.getX()+distanceX), (int)(e2.getY()+distanceY));
             }
             return true;
         }
 
         public void onShowPress(MotionEvent e) {
             // TODO Auto-generated method stub
             
         }
 
         public boolean onSingleTapUp(MotionEvent e) {
             //for(Targets t : icons)
             //{
             //    if (t.hasPoint(new Point((int)e.getX(),(int)e.getY()))) 
             //    {
             //    	if(t.inAbasket())
             //    	{
             //    		t.clicked(R.drawable.orangeplaced, MathTutor.this);
             //    	}
             //    	else
             //    	{
             //    		t.clicked(R.drawable.orange, MathTutor.this);
             //    	}
             //        view.onSelected();
             //        break;
             //    }
             //}
            return false;
         }
 
     }
 
     private class ImageView extends View
     {
         private GestureDetector gesture;
 
         public ImageView(Context ctxt)
         {
             super(ctxt);
             //sets gesture to a new gesturedectror
             gesture = new GestureDetector(MathTutor.this, new GestureListener(this));
         }
 
         /**
          * This method draws all icons onto the screen
          */
         protected void onDraw(Canvas canvas)
         {
         	//goes through and draws each icon
             for(Targets i : icons)
             {
                 i.draw(canvas);
             }
         }
 
         /**
          * detects when the user touches the screen and then informs gesture
          */
          @Override
         public boolean onTouchEvent(MotionEvent event)
         {
         	 if(gesture.onTouchEvent(event))
         	 {
         		 return true;
         	 }
         	 if(event.getAction() == MotionEvent.ACTION_UP)
         	 {
         		 for(Targets t : icons)
         		 {
         			 if(!t.isAnAnswer())
         			 {
         				 //this is where you will put the answer checking.
         				if (icons.get(0).isInAnswer(t))//check if it is answer box and if it correct answer 
                         {
                             t.clicked(R.drawable.correct_ans, MathTutor.this);
                             onSelected();
                         }
                         else if(icons.get(0).isInAnswer(t))//and wrong answer
                         {
                         	t.clicked(R.drawable.wrong_ans, MathTutor.this);
                         	onSelected();
                         }
        				t.setLoc();
         			 }
         		 }
         	 }
         	 return false;
         }
         
          /**
           * When an icon is selected it redraws the screen
           */
         public void onSelected()
         {
         	invalidate();
         }
     }
 }
