 package com.github.donkirkby.plank;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.github.donkirkby.plank.model.Piece;
 import com.github.donkirkby.plank.model.PieceColour;
 import com.github.donkirkby.plank.model.Plank;
 import com.github.donkirkby.plank.view.GameComponentView;
 import com.github.donkirkby.plank.view.PieceView;
 import com.github.donkirkby.plank.view.PlankView;
 
 public class PlankGame implements ApplicationListener {
 	private OrthographicCamera camera;
 	private List<PieceView> pieceViews;
 	private List<PlankView> placedPlankViews;
 	private List<PlankView> unplacedPlankViews;
 	private List<GameComponentView> draggableViews;
 	private List<GameComponentView> allViews;
 	private SpriteBatch batch;
 	private TextureAtlas atlas;
 	private GameComponentView draggingView;
 
 	@Override
 	public void create() {
 		atlas = new TextureAtlas(Gdx.files.internal("plank.pack"));
		List<String> shapeNames = 
				Arrays.asList("circle", "square", "triangle", "octagon");
 		pieceViews = new ArrayList<PieceView>();
 		
 		PieceColour[] colours = PieceColour.values();
 		for (int i = 0; i < colours.length; i++) {
 			PieceColour colour = colours[i];
 			for (int player = 0; player < shapeNames.size(); player++) {
 				String shapeName = shapeNames.get(player);
				String colourName = colour.name().toLowerCase();
				String imageName = "images/" + shapeName + "-" + colourName;
				AtlasRegion image = findRegion(imageName);
				int playerSide = player % 2;
				int playerLevel = player / 2 % 2;
				int pieceX = 50 + i*50 + playerSide * 600;
 				PieceView piece1 = new PieceView(
 						new Piece(player, colour), 
						new Vector2(pieceX, 50 + playerLevel*350), 20);
 				PieceView piece2 = new PieceView(
 						new Piece(player, colour), 
						new Vector2(pieceX, 100 + playerLevel*350), 20);
 				piece1.setImage(image);
 				piece2.setImage(image);
 				pieceViews.add(piece1);
 				pieceViews.add(piece2);
 			}
 		}
 
 		unplacedPlankViews = new ArrayList<PlankView>();
 		placedPlankViews = new ArrayList<PlankView>();
 		PieceColour[][] plankColourSets = {
 				{PieceColour.RED, PieceColour.GREEN, PieceColour.BLUE},
 				{PieceColour.RED, PieceColour.BLUE, PieceColour.GREEN},
 				{PieceColour.BLUE, PieceColour.RED, PieceColour.GREEN}
 		};
 		for (int i = 0; i < plankColourSets.length; i++) {
 			PieceColour[] pieceColours = plankColourSets[i];
			StringBuilder builder = new StringBuilder("images/plank-");
 			for (PieceColour pieceColour : pieceColours) {
 				builder.append(pieceColour.name().toLowerCase().charAt(0));
 			}
			AtlasRegion image = findRegion(builder.toString());
 			for (int player = 0; player < shapeNames.size(); player++) {
 				for (int j = 0; j < 2; j++) {
 					Plank plank = new Plank(
 							pieceColours[0], 
 							pieceColours[1], 
 							pieceColours[2]);
 					PlankView plankView = new PlankView(
 							plank, 
 							new Vector2(
 									250 + i * 120 + j * 60, 
 									80 + player*320), 
 							50);
 					plankView.setImage(image);
 					plankView.setLineHeight(240);
 					unplacedPlankViews.add(plankView);
 				}
 			}
 		}
 		allViews = new ArrayList<GameComponentView>();
 		allViews.addAll(unplacedPlankViews);
 		allViews.addAll(pieceViews);
 		for (GameComponentView view : allViews) {
 			view.setDestinations(placedPlankViews);
 		}
 		draggableViews = new ArrayList<GameComponentView>();
 		draggableViews.addAll(allViews);
 		
 		camera = new OrthographicCamera();
 		camera.setToOrtho(false, 800, 480);
 		batch = new SpriteBatch();
 	}
 
	private AtlasRegion findRegion(String regionName) {
		AtlasRegion region = atlas.findRegion(regionName);
		if (region == null) {
			throw new IllegalArgumentException(
					"Region not found, '" + regionName + "'.");
		}
		return region;
	}

 	@Override
 	public void dispose() {
 		batch.dispose();
 		atlas.dispose();
 	}
 
 	@Override
 	public void render() {
 
 		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 		batch.setProjectionMatrix(camera.combined);
 		batch.begin();
 		for (GameComponentView view : allViews) {
 			batch.draw(view.getImage(), view.getLeft(), view.getBottom());
 		}
 		batch.end();
 		if ( ! Gdx.input.isTouched()) {
 			draggingView = null;
 		}
 		else {
 			Vector3 touchPos3 = new Vector3();
 			touchPos3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
 			camera.unproject(touchPos3);
 			Vector2 touchPos = new Vector2(touchPos3.x, touchPos3.y);
 			if (draggingView == null && Gdx.input.justTouched()) {
 				draggingView =
 						GameComponentView.findClosest(touchPos, draggableViews);
 			}
 			if (draggingView != null) {
 				if (Gdx.input.isTouched(1) && Gdx.input.justTouched()) {
 					if (draggingView instanceof PlankView) {
 						PlankView plankView = (PlankView) draggingView;
 						plankView.flip();
 					}
 				}
 				boolean isSnapped = draggingView.dragTo(touchPos);
 				if (isSnapped && unplacedPlankViews.contains(draggingView)) {
 					unplacedPlankViews.remove(draggingView);
 					draggableViews.remove(draggingView);
 					draggingView = null;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 }
