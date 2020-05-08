 package com.gemserk.libgdx.tests.customsprite;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.gemserk.libgdx.tests.ShowStageInputProcessor;
 import com.gemserk.libgdx.tests.TestBaseWindow;
 import com.gemserk.libgdx.tests.TestScreen;
 
 public class SpriteToMeshSpritePerformanceTestScreen extends TestScreen {
 
 	TextureAtlas skinAtlas;
 	TextureAtlas atlas;
 	Skin skin;
 	Stage stage;
 
 	boolean blending;
 
 	OrthographicCamera camera;
 	MeshSpriteBatch meshSpriteBatch;
 
 	ArrayList<MeshSprite> sprites;
 	
 	Sprite prototypeSprite;
 	PolygonDefinition polygonDefinition;
 
 	@Override
 	public void create() {
 		Gdx.gl.glClearColor(0.69f, 0.86f, 0.81f, 1f);
 
 		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 
 		int width = Gdx.graphics.getWidth();
 		int height = Gdx.graphics.getHeight();
 
 		skinAtlas = new TextureAtlas(Gdx.files.internal("data/ui/uiskin.atlas"));
 		skin = new Skin(Gdx.files.internal("data/ui/uiskin.json"), skinAtlas);
 
 		atlas = new TextureAtlas(Gdx.files.internal("data/images/polygon.atlas"));
 
 		prototypeSprite = atlas.createSprite("worm");
 		polygonDefinition = PolygonDefinition.fromSprite(prototypeSprite);
 		
 		sprites = new ArrayList<MeshSprite>();
 
 		generateElements(50);
 
 		meshSpriteBatch = new MeshSpriteBatch();
 
 		stage = new Stage(width, height, false);
 
 		blending = true;
 
 		Table optionsContainer = new Table();
 		optionsContainer.setTransform(false);
 
 		{
 			{
 				final CheckBox actor = new CheckBox("Blending: " + blending, skin);
 				actor.setName("Blending");
 				actor.addListener(new ClickListener() {
 					@Override
 					public void clicked(InputEvent event, float x, float y) {
 						super.clicked(event, x, y);
 						blending = !blending;
 						actor.setText("Blending: " + blending);
 					}
 				});
 				optionsContainer.add(actor).padLeft(10f).padRight(10f).expandX().fillX().padBottom(10f).colspan(5);
 			}
 
 			optionsContainer.row();
 
 			{
 				TextButton actor = new TextButton("Sprites: " + sprites.size(), skin);
 				actor.setName("SpritesCount");
 				actor.setTouchable(Touchable.disabled);
 				optionsContainer.add(actor).padLeft(10f).padRight(10f).expandX().fillX().padBottom(10f).colspan(5);
 			}
 
 			optionsContainer.row();
 
 			{
 				TextButton actor = new TextButton("-", skin);
 				actor.addListener(new ClickListener() {
 					@Override
 					public void clicked(InputEvent event, float x, float y) {
 						super.clicked(event, x, y);
 						removeElements(50);
 					}
 				});
 				optionsContainer.add(actor).padLeft(10f).padRight(10f).expandX().fillX().padBottom(10f);
 			}
 
 			{
 				TextButton actor = new TextButton("+", skin);
 				actor.addListener(new ClickListener() {
 					@Override
 					public void clicked(InputEvent event, float x, float y) {
 						super.clicked(event, x, y);
 						generateElements(50);
 					}
 				});
 				optionsContainer.add(actor).padLeft(10f).padRight(10f).expandX().fillX().padBottom(10f);
 			}
 
 		}
 
 		Table baseWindowContainer = new TestBaseWindow("Options", skin, parent);
 
 		baseWindowContainer.setTransform(false);
 		baseWindowContainer.setSize(width * 0.8f, height * 0.8f);
 		baseWindowContainer.setPosition(width * 0.1f, height * 0.1f);
 
 		stage.addActor(baseWindowContainer);
 
 		Table innerContainer = (Table) baseWindowContainer.findActor(TestBaseWindow.INNER_CONTAINER_NAME);
 		innerContainer.add(optionsContainer).fill().expand();
 
 		stage.getRoot().setVisible(false);
 
 		Gdx.input.setInputProcessor(new InputMultiplexer(new ShowStageInputProcessor(stage, parent), stage));
 		Gdx.input.setCatchBackKey(true);
 	}
 
 	private void generateElements(int count) {
 		for (int i = 0; i < count; i++) {
 			MeshSprite borderSprite = new MeshSprite(polygonDefinition.getVertices(), polygonDefinition.getTextureCoordinates(), 
 					polygonDefinition.getIndices(), prototypeSprite.getTexture());
 
 			float x = MathUtils.random(-Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getWidth() * 0.5f);
 			float y = MathUtils.random(-Gdx.graphics.getHeight() * 0.5f, Gdx.graphics.getHeight() * 0.5f);
 
 			borderSprite.setZ(0f);
 			borderSprite.setPosition(x, y);
 
 			sprites.add(borderSprite);
 		}
 
 		if (stage == null)
 			return;
 
 		TextButton button = (TextButton) stage.getRoot().findActor("SpritesCount");
 		if (button != null)
 			button.setText("Sprites: " + sprites.size());
 	}
 
 	private void removeElements(int count) {
 
 		while (!sprites.isEmpty() && count > 0) {
 			sprites.remove(0);
 			count--;
 		}
 
 		TextButton button = (TextButton) stage.getRoot().findActor("SpritesCount");
 		if (button != null)
 			button.setText("Sprites: " + sprites.size());
 	}
 
 	@Override
 	public void dispose() {
 		stage.dispose();
 		stage = null;
 		skin.dispose();
 		skin = null;
 		skinAtlas.dispose();
 		skinAtlas = null;
 		atlas.dispose();
 		atlas = null;
 		meshSpriteBatch.dispose();
 		meshSpriteBatch = null;
 	}
 
 	@Override
 	public void render() {
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 
 		meshSpriteBatch.setDepthTestEnabled(false);
 
 		if (blending)
 			meshSpriteBatch.enableBlending();
 		else
 			meshSpriteBatch.disableBlending();
 
 		meshSpriteBatch.setProjectionMatrix(camera.combined);
 		meshSpriteBatch.begin();
 		for (int i = 0; i < sprites.size(); i++) {
 			MeshSprite sprite = sprites.get(i);
 			meshSpriteBatch.draw(sprite.getTexture(), sprite.getVertices(), sprite.getIndices());
 		}
 		meshSpriteBatch.end();

 		stage.draw();
 	}
 
 	@Override
 	public void update() {
 		stage.act();
 	}
 
 }
