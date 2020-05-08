 package sct.View;
 
 import java.lang.reflect.Array;
 import java.util.Random;
 
 import sct.Lib.DigHoles;
 import sct.Lib.Sudoku;
 import sct.Lib.Unit;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 public class PlayView extends View implements OnTouchListener{
 	float xo=15; //xoffset
 	float  yo=15; //yoffset
 	float  w=450; //width
 	Paint paint = new Paint();
 	int lightblue=  Color.rgb(164, 209, 255);
 	int lightgray=  Color.rgb(200, 200, 200);
 	int lightyellow=Color.rgb(255, 255, 128);
 	int[][] ans=null;//full sudoku as answer [1~9]!
 
 	float wp=w/9*4;//width of input pad
 	int oi,oj,pi,pj;//o:pad for (i,j) p:left and top location of pad
 	int shadowNum;
 	boolean padOnShow=false,padOnMark=false,shadowOnShow=false,automark=false;
 	Context context;
 	
 	Sudoku sudoku;//num, mark, background color
 
 	public PlayView(Context context) { super(context);this.context=context; init(); }
 
 	public PlayView(Context context,AttributeSet attr) 
 	{ super(context,attr);this.context=context; init(); }     
 
 	private void init(){
 		setFocusable(true);
 		setFocusableInTouchMode(true);
 		this.setOnTouchListener(this);
 		sudoku=new Sudoku();
 		paint.setStyle(Style.FILL);
 		paint.setAntiAlias(true);
 	}
 	
 	/*
 	 * function: generate the first sudoku
 	 * input: number of holes
 	 */
 	public void generateSudoku(int holes){
 		//		long start=System.currentTimeMillis(); 
 		int [][]m = null;
 		Object arr = DigHoles.tryDigLoop(holes);
 		ans=(int[][])Array.get(arr, 0);//answer
 		m=(int[][])Array.get(arr, 1);// sudoku with holes
 		//		int times=(Integer) Array.get(arr, 2);
 		//		long end=System.currentTimeMillis(); 
 		//		System.out.printf("Try:%dtimes using:%dms\n",times,end-start);
 
 		//m[][] to sudoku.unit[][]
 		for(int i=1;i<=9;i++)
 			for(int j=1;j<=9;j++)
 			{
 				if(m[i][j]!=0)
 					sudoku.unit[i-1][j-1].setFixType();
 				sudoku.unit[i-1][j-1].setNum(m[i][j]);
 			}
 	}
 
 	@Override
 	public void onDraw(Canvas canvas) {
 		super.onDraw(canvas);     
 
 		drawSudoku(canvas);
 		drawEdge(canvas);
 		drawPad(canvas);
 	}
 
 
 	/* function: draw sudoku
 	 * input: (Sudoku)
 	 */
 	void drawSudoku(Canvas canvas){
 		for(int i=0;i<9;i++)
 			for(int j=0;j<9;j++) {
 				Unit u = sudoku.unit[i][j];
 
 				// set unit bg color
 				if(u.getbg()==Unit.Bg.white)
 					paint.setColor(Color.WHITE);
 				else if(u.getbg()==Unit.Bg.blue)
 					paint.setColor(lightblue);
 				else
 					paint.setColor(lightgray);
 
 				//rect to show bg color
 				canvas.drawRect(w/9*i+xo, w/9*j+yo, w/9*(i+1)+xo, w/9*(j+1)+yo, paint);
 
 				// draw number in unit
 				if(u.isMarkType()) {// mark
 					paint.setColor(Color.BLACK);
 					paint.setTextSize(16);
 					float xoo=w/9*i + xo, yoo=w/9*j +yo;
 					for(int k=1;k<=9;k++)
 						if(u.isMark(k))
 							canvas.drawText(""+k,
 									4 + (k-1)%3*w/27 + xoo,
 									15 + (k-1)/3*w/27 + yoo,
 									paint);
 				}
 				else //fix or guess
 				{
 					if(u.isFixType())
 						paint.setColor(Color.BLACK);
 					else
 					{
 						if(u.getNum()==0) continue;
 						if(sudoku.unit[i][j].getNum()==ans[i+1][j+1])
 							paint.setColor(Color.BLUE);
 						else
 							paint.setColor(Color.RED);// wrong guess
 					}
 
 					paint.setTextSize(32);
 					canvas.drawText(""+sudoku.unit[i][j].getNum()
 							, 15 + w/9*i + xo,35 + w/9*j +yo, paint);
 				}
 			}
 	}
 
 
 	/* function: draw the edge of sudoku
 	 */
 	void drawEdge(Canvas canvas){
 		paint.setColor(Color.BLACK);
 
 		//light line
 		paint.setStrokeWidth(3);
 		for(int i=0;i<=3;i++)
 			canvas.drawLine(w/3*i+xo, yo, w/3*i+xo, w+yo, paint);
 		for(int i=0;i<=3;i++)
 			canvas.drawLine(xo, w/3*i+yo, w+xo, w/3*i+yo, paint);
 
 		//weight line
 		paint.setStrokeWidth(1);
 		for(int i=0;i<=9;i++)
 			canvas.drawLine(w/9*i+xo, yo, w/9*i+xo, w+yo, paint);
 		for(int i=0;i<=9;i++)
 			canvas.drawLine(xo, w/9*i+yo, w+xo, w/9*i+yo, paint);
 	}
 
 	/*
 	 * function: draw pad to imput number
 	 */
 	void drawPad(Canvas canvas){
 		float xop=pi*w/9+xo,yop=pj*w/9+yo;//todo
 
 		if(!padOnShow) return;
 
 		//draw yellow bg
 		paint.setColor(lightyellow);
 		canvas.drawRect(xop, yop, xop+wp, yop+wp+w/9, paint);
 
 		//edge
 		paint.setStrokeWidth(3);
 		paint.setColor(Color.BLACK);
 		for(int i=0;i<=3;i++)//row
 			canvas.drawLine(wp/3*i+xop, yop, wp/3*i+xop, wp+w/9+yop, paint);
 		for(int i=0;i<=3;i++)//column
 			canvas.drawLine(xop, wp/3*i+yop, wp+xop, wp/3*i+yop, paint);
 		canvas.drawLine(xop, wp+w/9+yop, wp+xop, wp+w/9+yop, paint);//last column line
 
 		//number
 		if(!padOnMark){// pad of guess
 			paint.setTextSize(48);
 			for(int i=0;i<3;i++) for(int j=0;j<3;j++) {
 				int num=i*3+j+1,originNum=sudoku.unit[oi][oj].getNum();
 				// show guess num in blue in pad
 				if(num==originNum) paint.setColor(Color.BLUE);
 				canvas.drawText(""+num, 20+wp/3*j+xop, 50+wp/3*i+yop, paint);
 				if(num==originNum) paint.setColor(Color.BLACK);
 			}
 		}
 		else{// pad of mark
 			paint.setTextSize(24);
 			for(int i=0;i<3;i++) for(int j=0;j<3;j++) {
 				int num=i*3+j+1;
 				if(sudoku.unit[oi][oj].isMark(num)) paint.setColor(Color.BLUE);
 				canvas.drawText(""+num,
 						6+wp/3*j+wp/9*j +xop, 19+wp/3*i+wp/9*i +yop,
 						paint);
 				if(sudoku.unit[oi][oj].isMark(num)) paint.setColor(Color.BLACK);
 			}
 		}
 
 		// draw mark switch and exit
 		paint.setTextSize(16);
 		canvas.drawText("mark", 15+xop, 30+wp+yop, paint);
 		paint.setTextSize(48);
 		canvas.drawText("X", 20+wp/3*2+xop, 40+wp+yop, paint);
 	}
 
 	/*
 	 * function: get the pad location (pi,pj) for unit (i,j)
 	 * intput: i,j
 	 */
 	void adjustPiPj(int i,int j){
 		if(j<=5){
 			pi=i/3*3+3; pj=j+1;
 			if(pi>5||pi<0||pj>6||pj<0) {
 				pi=i+1; pj=j/3*3+3;
 			}
 			if(pi>5||pi<0||pj>6||pj<0) {
 				pi=i/3*3-4; pj=j+1;
 			}
 			if(pi>5||pi<0||pj>6||pj<0) {
 				pi=i-4; pj=j/3*3+3;
 			}
 		}
 		else{
 			pi=i/3*3+3; pj=j-5;
 			if(pi>5||pi<0||pj>6||pj<0) {
 				pi=i+1; pj=j/3*3-5;
 			}
 			if(pi>5||pi<0||pj>6||pj<0) {
 				pi=i/3*3-4; pj=j-5;
 			}
 			if(pi>5||pi<0||pj>6||pj<0) {
 				pi=i-4; pj=j/3*3-5;
 			}
 		}
 	}
 
 	/*
 	 * function: if this touch is on pad, 
 	 *           before use this, check padOnShow first!
 	 */
 	boolean touchInPad(int i,int j){
 		return (pi<=i && i<pi+4 && pj<=j && j<pj+5);
 	}
 
 	/*
 	 * function: set the bg of all unit and show the pad (set padOnShow)
 	 * 			 set oi,oj(the pad for unit) first!
 	 * side effect: if show pad, set shadowOnShow=false
 	 * input: showOrNot(boolean)
 	 */
 	void setShowPad(boolean show){
 		for(int i=0;i<9;i++)
 			for(int j=0;j<9;j++)
 				if(show&&(((i/3*3==oi/3*3)&&(j/3*3==oj/3*3))||//same house
 						i==oi||j==oj)){
 					sudoku.unit[i][j].setbg(Unit.Bg.blue);
 				} else
 					sudoku.unit[i][j].setbg(Unit.Bg.white);
 		sudoku.unit[oi][oj].setbg(Unit.Bg.white);
 		padOnShow=show;
 		if(show) shadowOnShow=false;
 	}
 
 	/*
 	 * fuction: show shadow of impossible unit after touch a fix num
 	 * input: the i,j of the fix unit
 	 */
 	void toggleShowShadow(int ti,int tj){
 		if(shadowOnShow && shadowNum==sudoku.unit[ti][tj].getNum()){
 			for(int i=0;i<9;i++)
 				for(int j=0;j<9;j++)
 					sudoku.unit[i][j].setbg(Unit.Bg.white);
 			shadowOnShow=false;
 			return;
 		}
 
 		shadowNum=sudoku.unit[ti][tj].getNum();
 		shadowOnShow=true;
 
 		//find
 		int[] a=new int[81];int top=0;
 		for(int i=0;i<9;i++)
 			for(int j=0;j<9;j++)
 				if(sudoku.unit[i][j].isOnNum()){
 					if(sudoku.unit[i][j].getNum()==shadowNum)
 						a[top++]=i*10+j;
 				}
 
 		//set
 		for(int k=0;k<top;k++){
 			int di=a[k]/10,dj=a[k]%10;
 			for(int i=0;i<9;i++)
 				for(int j=0;j<9;j++)
 					if(sudoku.unit[i][j].isOnNum()|| // on num
 					(sudoku.unit[i][j].isMarkType() && !sudoku.unit[i][j].isMark(shadowNum))|| //unmarked this num
 					((i/3*3==di/3*3)&&(j/3*3==dj/3*3))|| //same house
 					i==di||j==dj) //same row or column
 						sudoku.unit[i][j].setbg(Unit.Bg.gray);
 		}
 		for(int k=0;k<top;k++){
 			int di=a[k]/10,dj=a[k]%10;
 			sudoku.unit[di][dj].setbg(Unit.Bg.blue);
 		}
 	}
 	
 	/*
 	 * function: automark all unit ,called by push automark button
 	 */
 	public void automarkAll(){
 		for(int i=0;i<9;i++) for(int j=0;j<9;j++)
 			if(!sudoku.unit[i][j].isOnNum()){
 				sudoku.unit[i][j].setMarkType();
 				for(int k=1;k<=9;k++)
 					sudoku.unit[i][j].setMark(k);
 			}
 		for(int i=0;i<9;i++) for(int j=0;j<9;j++)
 			if(sudoku.unit[i][j].isOnNum()){
 				int num=sudoku.unit[i][j].getNum();
 				
 				for(int t=0;t<9;t++){//row and column
 					if(t!=j) sudoku.unit[i][t].clearMark(num);
 					if(t!=i) sudoku.unit[t][j].clearMark(num);
 				}
 				//house
 				int bi=i/3*3,bj=j/3*3;
 				for(int ti=0;ti<3;ti++) for(int tj=0;tj<3;tj++)
 					if(ti!=i || tj!=j)
 						sudoku.unit[bi+ti][bj+tj].clearMark(num);
 			}
 	}
 	/*
 	 * function:(After set guess,) auto unmark the around
 	 */
 	public void automarkSet(int oi,int oj,int num){
 		int bi=oi/3*3,bj=oj/3*3;
 		for(int i=0;i<3;i++) for(int j=0;j<3;j++)
 			sudoku.unit[bi+i][bj+j].clearMark(num);
 		for(int t=0;t<9;t++){ 
 			sudoku.unit[oi][t].clearMark(num);
 			sudoku.unit[t][oj].clearMark(num);
 		}
 	}
 	/*
 	 * function:(After clear the origin guess,) auto ununmark the around
 	 */
 	public void automarkClear(int oi,int oj,int num){
 		int bi=oi/3*3,bj=oj/3*3;
 		for(int i=0;i<9;i++) for(int j=0;j<9;j++)
 			if(oi==i || oj==j || (bi<=i && i<bi+3 && bj<=j && j<bj+3)){ //row or column or house
 				if(sudoku.unit[i][j].isMarkType()){ //is mark and check num can be marked
 					int tbi=i/3*3,tbj=j/3*3;
 					boolean foundNum=false;
 					for(int ti=0;ti<9;ti++) for(int tj=0;tj<9;tj++)
 						if(i==ti || j==tj || (tbi<=ti && ti<tbi+3 && tbj<=tj && tj<tbj+3)) //row or column or house
 						if( sudoku.unit[ti][tj].isOnNum() && sudoku.unit[ti][tj].getNum()==num )
 							foundNum=true;
 					if(!foundNum) sudoku.unit[i][j].setMark(num);
 				}
 			}
 	}
 	/*
 	 * function: clear all mark
 	 */
 	public void clearAllMark(){
 		for(int i=0;i<9;i++) for(int j=0;j<9;j++)
 			for(int k=1;k<=9;k++)
 			sudoku.unit[i][j].clearMark(k);
 	}
 	/*
 	 * function: give a hint
 	 */
 	public void giveHint(){
 		Random r=new Random();
 		//check there are place to give hint
 		int[] arr=new int[81];
 		int top=0;
 		for(int i=0;i<9;i++) for(int j=0;j<9;j++)
			if(!sudoku.unit[i][j].isMarkType())
 				arr[top++]=i*10+j;
 		if(top==0) return;
 		
 		//clear
 		setShowPad(false);
 		shadowOnShow=false;
 		for(int i=0;i<9;i++) for(int j=0;j<9;j++)
 			sudoku.unit[i][j].setbg(Unit.Bg.white);
 		
 		int luck=r.nextInt(top);
		int li=luck/10,lj=luck%10;
 //		System.out.printf("%d %d\n",li,lj);
 		sudoku.unit[li][lj].setNum(ans[li+1][lj+1]);
 		if(automark) automarkSet(li,lj,ans[li+1][lj+1]);
 		sudoku.unit[li][lj].setFixType();
 		sudoku.unit[li][lj].setbg(Unit.Bg.blue);
 		invalidate();
 	}
 	/*
 	 * function: main touch method, do many things
 	 * 
 	 */
 	public boolean onTouch(View view, MotionEvent event) {
 		if(event.getAction()!=MotionEvent.ACTION_DOWN) return false;
 
 		int i=(int)((event.getX()-xo)/(w/9));
 		int j=(int)((event.getY()-yo)/(w/9));
 
 		//		System.out.printf("i:%d j:%d\n",i,j);
 		if(padOnShow && touchInPad(i,j)){// touch on pad
 
 			int ti=(int) ((event.getX()-(pi*w/9+xo))/(wp/3));
 			int tj=(int) ((event.getY()-(pj*w/9+yo))/(wp/3));
 			int inputNum=tj*3+ti+1;
 
 			if(inputNum<=9){ // touch guess pad todo:mark
 				if(!padOnMark){// mark a guess
 					int originNum=sudoku.unit[oi][oj].getNum();
 					if(originNum==inputNum){//cancel guess
 						sudoku.unit[oi][oj].setNum(0);
 						if(automark){
 							sudoku.unit[oi][oj].setMarkType();
 							automarkClear(oi,oj,originNum);
 						}
 					}else{//really make a guess
 						sudoku.unit[oi][oj].setGuessType();
 						sudoku.unit[oi][oj].setNum(inputNum);
 						if(automark) automarkSet(oi,oj,inputNum);
 						setShowPad(false);
 					}
 				}else{ // make a mark
 					sudoku.unit[oi][oj].setMarkType();
 					if(sudoku.unit[oi][oj].isMark(inputNum))
 						sudoku.unit[oi][oj].clearMark(inputNum);
 					else
 						sudoku.unit[oi][oj].setMark(inputNum);
 				}
 			}
 			else if(inputNum==10){ // touch mark btn
 				padOnMark=!padOnMark;
 			}
 			else setShowPad(false);
 		} else if(0<=i && i<9 && 0<=j && j<9){ // touch on unit
 			if(sudoku.unit[i][j].isFixType()){
 				setShowPad(false);
 				toggleShowShadow(i,j);
 			}else{ //guess
 				oi=i;oj=j;
 				adjustPiPj(i,j);
 				setShowPad(true);
 			}
 		}
 		else setShowPad(false);
 		invalidate();
 		return true;
 	}
 
 }
