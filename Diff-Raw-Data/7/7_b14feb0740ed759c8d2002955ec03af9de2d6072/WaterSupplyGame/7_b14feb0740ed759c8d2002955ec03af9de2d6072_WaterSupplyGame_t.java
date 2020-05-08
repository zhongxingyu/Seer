 package com.androidmontreal.rhok;
 
 import java.util.Hashtable;
 import java.util.List;
 
 import com.androidmontreal.rhok.pieces.Piece;
 import com.androidmontreal.rhok.pieces.Pipe;
 import com.androidmontreal.rhok.pieces.Pipe.PipeType;
 import com.androidmontreal.rhok.pieces.Point;
 import com.androidmontreal.rhok.pieces.factory.PieceType;
 import com.androidmontreal.rhok.pieces.factory.PipeFactory;
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class WaterSupplyGame implements ApplicationListener {
 
 	private static final int TABLE_HEIGHT = 6;
 	private static final int TABLE_WIDTH = 5;
 	private Pipe[][] pipesTable;
 	
 	private SpriteBatch batch;
 	private Sprite sprite;
 
 	private double steps;
 
 	List<PieceType> pipeTypes;
 	List<Piece> pieces;
 	private Hashtable<PipeType, Sprite> pipeSprites;
 
 	
 	@Override
 	public void create() {
 		
 		pipesTable = new Pipe[TABLE_WIDTH][TABLE_HEIGHT];
 
 		initializePipeTypes();
 		
 		initializeSomePieces();
 		
 	
 		
 		batch = new SpriteBatch();
 	}
 	
 	private void initializeSomePieces() {
 					
 		for (int x = 0; x < TABLE_WIDTH; x++) {
 			
 			for (int y = 0; y < TABLE_HEIGHT; y++) {
 				pipesTable[x][y] = PipeFactory.getInstance().createPipe(PipeType.DOWN_LEFT, new Point(x, y));
 			} 		
 		}
 		
 	}
 
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void render() {
 		steps += 0.1;
 
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT); // This cryptic line clears
 													// the screen.					
 		batch.begin();
 				
 		int spacing = 22;
 		
 		for (int x = 0; x < TABLE_WIDTH; x++) {			
 			for (int y = 0; y < TABLE_HEIGHT; y++) {
 				
 				Pipe p = pipesTable[x][y];
 				
 				Sprite s = pipeSprites.get(p.getType());		
 				int displayX = (x + 1) * spacing;
 				int displayY = (y + 1) * spacing;
 				
 				s.setPosition(displayX, displayY);
 				s.draw(batch);
 								
 			} 		
 		}
 					
 		batch.end();
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 	
 
 	private void initializePipeTypes() {
 
 		pipeSprites = new Hashtable<Pipe.PipeType, Sprite>();
 		
 		Sprite sprite;
 		
 		for (Pipe.PipeType pipeType : Pipe.PipeType.values()) {
 
 			switch (pipeType) {
 			case DOWN_LEFT:
 				sprite = new Sprite(new Texture(Gdx.files.internal("DownLeft.png")));
 				break;
 			case DOWN_RIGHT:
 				sprite =new Sprite(new Texture(Gdx.files.internal("DownRight.png")));
 				break;
 
 			case TOP_LEFT:
 				sprite =new Sprite(new Texture(Gdx.files.internal("TopLeft.png")));
 				break;
 
 			case TOP_RIGHT:
 				sprite =new Sprite(new Texture(Gdx.files.internal("TopRight.png")));
 				break;
 
 			case HORIZONTAL:
 				sprite =new Sprite(new Texture(Gdx.files.internal("HorizontalPipe.png")));
 				break;
 
 			case VERTICAL:
 				sprite =new Sprite(new Texture(Gdx.files.internal("VerticalPipe.png")));
 				break;
 				
 			case BLANK:
 				sprite =new Sprite(new Texture(Gdx.files.internal("blank.png")));
 				break;
 
 			default:
 				sprite = null;
 				break;
 			}
 			
 			if(sprite!=null){
 				pipeSprites.put(pipeType, sprite);
 			}
 		}
 	}
 
 }
