 package com.androidmontreal.rhok;
 
 import java.util.List;
 
 import com.androidmontreal.rhok.pieces.factory.PieceType;
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 public class WaterSupplyGame implements ApplicationListener {
 
 	private SpriteBatch batch;
 	private Sprite sprite;
 
 	private double steps;
 
 	List<PieceType> pipeTypes;
 
 	@Override
 	public void create() {
 
 		initializePipeTypes();
 
 		Art.load();
 		sprite = new Sprite(Art.texture);
 		batch = new SpriteBatch();
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
 		int waveX = (int) (Math.cos(steps) * 6d);
 		int waveY = (int) (Math.sin(steps) * 6d);
 		for (int i = 0; i < 10; i++) {
 			for (int j = 0; j < 10; j++) {
 				int x = (j + 1) * spacing;
 				int y = (i + 1) * spacing;
 				sprite.setPosition(x + waveX, y + waveY);
 				sprite.setRotation((float) (steps * 15));
 				sprite.draw(batch);
 				// batch.draw(texture, x+waveX, y+waveY );
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
 
 		PieceType pieceType;
		for (PieceType.PipeType pipeType : PieceType.PipeType.values()) {
 
 			switch (pipeType) {
 			case DOWN_LEFT:
 				pieceType = new PieceType(new Sprite(new Texture(
 						Gdx.files.internal("DownLeft.png"))));
 				break;
 			case DOWN_RIGHT:
 				pieceType = new PieceType(new Sprite(new Texture(
 						Gdx.files.internal("DownRight.png"))));
 				break;
 
 			case TOP_LEFT:
 				pieceType = new PieceType(new Sprite(new Texture(
 						Gdx.files.internal("TopLeft.png"))));
 				break;
 
 			case TOP_RIGHT:
 				pieceType = new PieceType(new Sprite(new Texture(
 						Gdx.files.internal("TopRight.png"))));
 				break;
 
 			case HORIZONTAL:
 				pieceType = new PieceType(new Sprite(new Texture(
 						Gdx.files.internal("HorizontalPipe.png"))));
 				break;
 
 			case VERTICAL:
 				pieceType = new PieceType(new Sprite(new Texture(
 						Gdx.files.internal("VerticalPipe.png"))));
 				break;
 
 			default:
 				pieceType = null;
 				break;
 			}
 			
 			if(pieceType!=null){
 				pipeTypes.add(pieceType);
 			}
 		}
 	}
 }
