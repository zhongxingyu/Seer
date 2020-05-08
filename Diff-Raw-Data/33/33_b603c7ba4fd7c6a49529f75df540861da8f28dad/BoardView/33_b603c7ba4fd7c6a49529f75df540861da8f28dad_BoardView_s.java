 package edu.aueb.cs.uw;
 
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.Paint.Align;
 import android.graphics.Paint.Style;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.ImageButton;
 import edu.aueb.cs.uw.core.Board;
 import edu.aueb.cs.uw.core.GameEngine;
 import edu.aueb.cs.uw.core.Tile;
 import edu.aueb.cs.uw.core.TileStack;
 import edu.aueb.cs.uw.core.Tray;
 
 public class BoardView extends View{
 
 	private static int BOARD_SIZE=10;
 	private static final float MIN_FONT_DIPS = 14.0f;
 	private static final int TRAY_AREA=1;
 	private static final int BOARD_AREA=2;
 	private static final int SCORE_AREA=3;
 	
 
 	private Paint fillScorePaint;
     private Paint fillTrayPaint;
     private Paint fillBoardPaint;
     private Paint strokePaint;
     private Paint tileFillPaint;
     private Paint tileStrokePaint;
     private Paint tileTextPaint;
     private Paint centralSquarePaint;
     private Paint scorePaint;
     private Paint selectedTilePaint;
     private Paint underneathCellPaint;
 	private int defaultFontSize;
 	private Dimensions dimensions;
 	private Context mContext;
 	private boolean isInitialized;
 	private boolean tileIsMoved;
 	private boolean boardTileIsMoved;
 	private int selectedTileNum;
 	private int selectedBoardTileX;
 	private int selectedBoardTileY;
 	private int movingTileX,movingTileY;
 	private GameEngine ge;
 	private Rect [] tilesTray;
 	private Tile movingTile;
 	private ImageButton endTurn;
 	
 
 	public BoardView( Context context, AttributeSet attrs ) 
     {
         super(context, attrs);
         isInitialized=false;
         mContext=context;
     }
 
 	@Override
 	protected void onDraw(android.graphics.Canvas canvas) 
 	{
 		initialise(mContext);
 		drawTemplate(canvas);
 		invalidate();
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		int evt=event.getAction();
 		int x,y;
 		switch(evt){
 		case MotionEvent.ACTION_DOWN:
 			x=(int)event.getX();
 			y=(int)event.getY();
 			int area=getArea(x, y);
 			switch(area){
 			case TRAY_AREA:
 				handleTrayClick(x,y);
 				break;
 			case BOARD_AREA:
 				handleBoardClick(x,y);
 				break;
 			case SCORE_AREA:
 			default:
 				break;
 			}
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if(selectedTileNum!=-1){
 				x=(int)event.getX();
 				y=(int)event.getY();
 				int turn=ge.getPlayerTurn();
 				Tray t=ge.getPlayer(turn).getTray();
 				if(!tileIsMoved)
 					movingTile=t.temporaryRemoveTile(selectedTileNum);
 				tileIsMoved=true;
 				movingTileX=getMovingTileXPos(x);
 				movingTileY=getMovingTileYPos(y);
 			} else if(selectedBoardTileX!=-1&&selectedBoardTileY!=-1){
 				x=(int)event.getX();
 				y=(int)event.getY();
 				Board b=ge.getBoard();
 				if(!tileIsMoved)
 					movingTile=b.removeTile(selectedBoardTileX, selectedBoardTileY);
 				tileIsMoved=true;
 				boardTileIsMoved=true;
 				movingTileX=getMovingTileXPos(x);
 				movingTileY=getMovingTileYPos(y);
 			}
 			
 			break;
 		case MotionEvent.ACTION_UP:
 			if(tileIsMoved){
 				x=(int)event.getX();
 				y=(int)event.getY();
 				Board b=ge.getBoard();
 				int turn=ge.getPlayerTurn();
 				Tray t=ge.getPlayer(turn).getTray();
 				if(getArea(x, y)==BOARD_AREA){
 					int i=findCellRow(y);
 					int j=findCellCol(x);
 					if(b.canAddTile(i,j,movingTile)) {
 						b.addTile(movingTile, i, j);
 						if(!boardTileIsMoved) {
 							t.addTempRemovedTile(movingTile, selectedTileNum);
 							t.useTile(selectedTileNum);
						} else
							ge.getBoard().removeFromList(movingTile, selectedBoardTileX, selectedBoardTileY);
 					}
 					else {
 						if(boardTileIsMoved)
 							b.addTile(movingTile, selectedBoardTileX, selectedBoardTileY);
 						else
 							t.addTempRemovedTile(movingTile, selectedTileNum);
 					}
 				} else if(getArea(x, y)==TRAY_AREA) {
 					if(boardTileIsMoved) 
 						t.addTile(movingTile);
 					else if(selectedTileNum!=-1)
 						t.addTempRemovedTile(movingTile, selectedTileNum); 
 				} else{
 					if(selectedTileNum!=-1) 
 						t.addTempRemovedTile(movingTile, selectedTileNum); 
 					else if(selectedBoardTileX!=-1&&selectedBoardTileY!=-1) 
 						b.addTile(movingTile, selectedBoardTileX, selectedBoardTileY);
 				}
 				undoMovingChanges();
 			} else {
 				undoMovingChanges();
 			}
 			break;
 		default:
 			break;	
 		}
 		return true;
 	}
 	
 	private int getMovingTileXPos(int x){
 		if(x<dimensions.getCellSize()/2)
 			return dimensions.getCellSize()/2;
 		else if(x>dimensions.getTotalWidth()-dimensions.getCellSize()/2)
 			return dimensions.getTotalWidth()-dimensions.getCellSize()/2;
 		else 
 			return x;
 	}
 	
 	private int getMovingTileYPos(int y){
 		if(y<dimensions.getCellSize()/2)
 			return dimensions.getCellSize()/2;
 		else if(y>dimensions.getTotalHeight()-dimensions.getCellSize()/2)
 			return dimensions.getTotalHeight()-dimensions.getCellSize()/2;
 		else
 			return y;
 	}
 
 	private void handleBoardClick(int x, int y) {
 		Board b=ge.getBoard();
 		int turn=b.getTurn();
 		int i=findCellRow(y);
 		int j=findCellCol(x);
 		Tile t=b.getTile(i, j);
 		if(t!=null){
 			if(t.getAge()==turn) {
 				selectedBoardTileX=i;
 				selectedBoardTileY=j;
 			}			
 		}
 	}
 
 	private void handleTrayClick(int x, int y) {
 		for(int i=0;i<tilesTray.length;i++){
 			if(tilesTray[i].contains(x, y)){
 				selectedTileNum=i;
 			}
 		}
 	}
 	
 	private void undoMovingChanges(){
 		tileIsMoved=false;
 		boardTileIsMoved=false;
 		selectedTileNum=-1;
 		selectedBoardTileX=-1;
 		selectedBoardTileY=-1;
 	}
 
 	private void initialise(Context context) {
 		if(!isInitialized){			
 			final float scale = getResources().getDisplayMetrics().density;
 			int width=getWidth();
 			int height=getHeight();
 			defaultFontSize = (int)(MIN_FONT_DIPS * scale + 0.5f);
 			fillScorePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
 			fillScorePaint.setARGB(200, 75, 75, 75);
 			fillTrayPaint=new Paint();
 			fillTrayPaint.setARGB(200, 75, 75, 75);
 			fillBoardPaint=new Paint();
 			fillBoardPaint.setARGB(240, 150, 150, 150);
 			strokePaint=new Paint();
 			strokePaint.setStyle(Paint.Style.STROKE);
 			strokePaint.setColor(Color.WHITE);
 			tileStrokePaint=new Paint();
 			tileStrokePaint.setStyle(Paint.Style.STROKE);
 			tileStrokePaint.setColor(Color.BLACK);
 			tileFillPaint=new Paint();
 			tileFillPaint.setStyle(Paint.Style.FILL);
 			tileFillPaint.setColor(Color.WHITE);
 			tileTextPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
 			tileTextPaint.setColor(Color.BLACK);
 			tileTextPaint.setStyle(Paint.Style.STROKE);
 			tileTextPaint.setTextAlign(Align.CENTER);
 			selectedTilePaint=new Paint();
 			selectedTilePaint.setStyle(Paint.Style.FILL);
 			selectedTilePaint.setColor(Color.YELLOW);
 			underneathCellPaint=new Paint();
 			underneathCellPaint.setStyle(Style.STROKE);
 			underneathCellPaint.setColor(Color.RED);
 			centralSquarePaint=new Paint();
 			centralSquarePaint.setColor(Color.RED);
 			scorePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
 	   
 			float curWidth = tileStrokePaint.getStrokeWidth();
 			curWidth *= 2;
 			if ( curWidth < 2 ) {
 				curWidth = 2;
 			}
 			tileStrokePaint.setStrokeWidth(curWidth);
 			dimensions=calculateDimensions(width, height);
 			isInitialized=true;
 			tileIsMoved=false;
 			boardTileIsMoved=false;
 			selectedTileNum=-1;
 			selectedBoardTileX=-1;
 			selectedBoardTileY=-1;
 			setFocusable(true);
 		}
 		
 	}
 	
 	private void drawTemplate(Canvas canvas){
 		Rect bRect=new Rect(0,dimensions.getScoreHeight(),dimensions.getTotalWidth(),dimensions.getScoreHeight()+dimensions.getBoardheight());
 		canvas.drawRect(bRect, fillBoardPaint);
 		Rect scRect=new Rect(0,0, dimensions.getTotalWidth(),dimensions.getTotalHeight()-dimensions.getBoardheight()-dimensions.getTrayHeight());
 		canvas.drawRect(scRect, fillScorePaint);
 		Rect tRect=new Rect(0,dimensions.getScoreHeight()+dimensions.getBoardheight(), dimensions.getTotalWidth(),dimensions.getTotalHeight());
 		canvas.drawRect(tRect, fillTrayPaint);
 		canvas.drawRect(scRect, fillScorePaint);
 		drawBoard(canvas);
 		drawScore(canvas);
 		drawTray(canvas);
 		drawMovingTile(canvas);
 		if(ge.getBoard().isValidPlacement())
 			endTurn.setClickable(true);
 		else
 			endTurn.setClickable(false);
 	}
 	
 	private void drawMovingTile(Canvas canvas){
 		if(tileIsMoved){
 			tileTextPaint.setTextSize(defaultFontSize*2);
 			drawTile(canvas, movingTileX-dimensions.getCellSize()/2, movingTileY-dimensions.getCellSize()/2, 
 					movingTileX+dimensions.getCellSize()/2, movingTileY+dimensions.getCellSize()/2,
 					Character.toString(movingTile.getLetter()));
 			if(getArea(movingTileX, movingTileY)==BOARD_AREA){
 				int i=findCellRow(movingTileY);
 				int j=findCellCol(movingTileX);
 				Board b=ge.getBoard();
 				if(b.canAddTile(i,j,movingTile))
 					underneathCellPaint.setColor(Color.GREEN);
 				else
 					underneathCellPaint.setColor(Color.RED);
 				int left=dimensions.getPadding()+j*dimensions.getCellSize();
 				int right=left+dimensions.getCellSize();
 				int top=dimensions.getScoreHeight()+i*dimensions.getCellSize();
 				int bottom=top+dimensions.getCellSize();
 				Rect underRect=new Rect(left, top, right, bottom);
 				canvas.drawRect(underRect, underneathCellPaint);
 			}
 		}
 	}
 	
 	
 	private int findCellRow(int y){
 		/*Needs y coordinate to find row*/
 		for(int i=0;i<BOARD_SIZE;i++){
 			if(y>=dimensions.getScoreHeight()+i*dimensions.getCellSize()&&
 					y<=dimensions.getScoreHeight()+i*dimensions.getCellSize()+dimensions.getCellSize())
 				return i;
 		}
 		return 0;
 	}
 	
 	private int findCellCol(int x){
 		/*Needs x coordinate to find column*/
 		for(int i=0;i<BOARD_SIZE;i++){
 			if(x>=dimensions.getPadding()+i*dimensions.getCellSize()&&
 					x<=dimensions.getPadding()+i*dimensions.getCellSize()+dimensions.getCellSize())
 				return i;
 		}
 		return 0;
 	}
 	
 	private void drawScore(Canvas canvas)
 	{		
 		if(ge==null) return;
 		
 		int turn=ge.getPlayerTurn();
 		int NumOfPlayers=ge.getNumPlayers();
 		
 		int maxHeight=dimensions.getScoreHeight();
 		float scoreWidth=dimensions.getTotalWidth()/NumOfPlayers;
 		
 		scorePaint.setTextAlign(Align.CENTER);
 		
 		for(int i=0;i<NumOfPlayers;i++){
 			if(i==turn)
 				scorePaint.setTextSize(1.3f*defaultFontSize);
 			scorePaint.setColor(ge.getPlayer(i).getColor());
 			canvas.drawText(ge.getPlayer(i).getNickname()+":"+ge.getPlayer(i).getScore(),
 					i*scoreWidth+(scoreWidth/2) ,3*maxHeight/4, scorePaint);
 			scorePaint.setTextSize(defaultFontSize);
 		}
 
 	}
 	
 	private void drawTray(Canvas canvas){
 		
 		if(ge==null) return;
 		
 		int turn=ge.getPlayerTurn();
 		Tray t=ge.getPlayer(turn).getTray();
 		int tileSize=dimensions.getTotalWidth()/Tray.TRAY_SIZE;
 		if(tileSize>=dimensions.getTrayHeight())
 			tileSize=4*dimensions.getTrayHeight()/5;
 		int bot_border=(dimensions.getTrayHeight()-tileSize)/2;
 		int space=(dimensions.getTotalWidth()-(tileSize*Tray.TRAY_SIZE))/(Tray.TRAY_SIZE+1);
 		tilesTray=new Rect[t.getNumUnusedTiles()]; 
 		for(int i=0;i<t.getNumUnusedTiles();i++){
 			if(t.getTile(i)==null) continue;
 			if(selectedTileNum==i)
 				tileFillPaint.setColor(Color.YELLOW);
 			tileTextPaint.setTextSize(defaultFontSize*3);
 			tilesTray[i]=drawTile(canvas, i*tileSize+(i+1)*space, dimensions.getTotalHeight()-tileSize-bot_border, 
 					i*tileSize+(i+1)*space+tileSize, dimensions.getTotalHeight()-bot_border, t.getTileLetter(i));
 			tileFillPaint.setColor(Color.WHITE);
 		}
 	}
 	
 	private Rect drawTile(Canvas canvas, int left, int top, int right, int bottom, String text){
 		Rect tileRect;
 		tileRect=new Rect(left, top, right, bottom);
 		canvas.drawRect(tileRect, tileFillPaint);
 		canvas.drawRect(tileRect, tileStrokePaint);
 		canvas.drawText(text,left+(right-left)/2,bottom-(bottom-top)/5, tileTextPaint);
 		return tileRect;
 	}
 	
 	private void drawBoard(Canvas canvas)
 	{
 		int bHeight=dimensions.getBoardheight();
 		
 		int bWidth=bHeight;
 		int sHeight=dimensions.getScoreHeight();
 		int padding=dimensions.getPadding();
 		
 		int left=dimensions.getPadding()+(BOARD_SIZE/2-1)*dimensions.getCellSize();
 		int top=dimensions.getScoreHeight()+(BOARD_SIZE/2-1)*dimensions.getCellSize();
 		int bottom= 2*dimensions.getCellSize()+top;
 		int right= 2*dimensions.getCellSize()+left;
 		
 		Rect centralRect=new Rect(left, top, right, bottom);
 		canvas.drawRect(centralRect, centralSquarePaint);
 		
 		for(int i=0;i<11;i++)
 		{
 			int x=i*dimensions.getCellSize();
 			canvas.drawLine(padding+x,sHeight,padding+x,sHeight+bHeight,strokePaint);
 		}
 		
 		for(int i=0;i<11;i++)
 		{
 			int y=i*dimensions.getCellSize();
 			canvas.drawLine(padding,sHeight+y,padding+bWidth,sHeight+y,strokePaint);
 		}
 		
 		if(ge==null) return;
 		Board b=ge.getBoard();
 		TileStack [][] ts=b.getTilePlacement();
 		for(int i=0;i<ts.length;i++){
 			for(int j=0;j<ts[i].length;j++){
 				if(b.hasTile(i, j)){
 					if(selectedBoardTileX==i&&selectedBoardTileY==j)
 						tileFillPaint.setColor(Color.YELLOW);
 					left=dimensions.getPadding()+j*dimensions.getCellSize();
 					right=left+dimensions.getCellSize();
 					top=dimensions.getScoreHeight()+i*dimensions.getCellSize();
 					bottom=top+dimensions.getCellSize();
 					tileTextPaint.setTextSize(2*defaultFontSize);
 					drawTile(canvas,left , top, right, bottom, Character.toString(ts[i][j].getTop().getLetter()));
 					tileFillPaint.setColor(Color.WHITE);
 				}
 			}
 		}
 		
 	}
 	
 	private int getArea(int x,int y){
 		if(y<=dimensions.getScoreHeight())
 			return SCORE_AREA;
 		else if(y>dimensions.getScoreHeight()&&
 				y<=dimensions.getScoreHeight()+dimensions.getBoardheight())
 			return BOARD_AREA;
 		else return TRAY_AREA;
 	}
 
 	private Dimensions calculateDimensions(int width,int height){
 		int cellSize,padding,top,bWidth,bHeight,scHeight,tHeight;
 		int maxCellSize;
 
 		Dimensions dims=new Dimensions();
 		dims.setTotalWidth(width);
 		dims.setTotalHeight(height);		
 		cellSize=width/BOARD_SIZE;
 		maxCellSize=3*defaultFontSize;
 		if(maxCellSize<cellSize)
 			cellSize=maxCellSize;
 		bWidth=BOARD_SIZE*cellSize;
 		bHeight=bWidth;
 		padding=(width-bWidth)/2;
 		scHeight=defaultFontSize*2;
 		tHeight=cellSize*3;
 
 		if(height>=bWidth+scHeight+tHeight) {
 			top=(height-bWidth-scHeight-tHeight)/2;
 		}
 		else {
 			top=0;
 			tHeight=4*defaultFontSize;
 			if(height-bHeight-tHeight>tHeight){
 				tHeight=height-bHeight-tHeight;
 				scHeight=height-bHeight-tHeight;
 			} else{
 				scHeight=height-bHeight-tHeight;				
 			}
 		}
 		dims.setCellSize(cellSize);
 		dims.setBoardheight(bHeight);
 		dims.setPadding(padding);
 		dims.setScoreHeight(scHeight);
 		dims.setTop(top);
 		dims.setTrayHeight(tHeight);
 		return dims;
 	}
 
 	public void setGameEngine(GameEngine ge) {
 		this.ge=ge;
 	}
 
 	public void setEndTurn(ImageButton endTurn) {
 		this.endTurn = endTurn;
 	}
 
 	public ImageButton getEndTurn() {
 		return endTurn;
 	}	
 	
 }
