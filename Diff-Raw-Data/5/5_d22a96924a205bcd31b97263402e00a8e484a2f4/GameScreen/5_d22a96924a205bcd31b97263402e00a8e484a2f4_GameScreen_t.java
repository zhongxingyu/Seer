 package se.chalmers.dat255.risk.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.dat255.risk.GDXGame;
 import se.chalmers.dat255.risk.model.Card;
 import se.chalmers.dat255.risk.model.Card.CardType;
 import se.chalmers.dat255.risk.model.IGame;
 import se.chalmers.dat255.risk.model.IProvince;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 
 /**
  * shows the gameboard, including provinces, cards and buttons.
  * 
  */
 public class GameScreen extends AbstractScreen {
 	private boolean isWorld;
 	private Stage worldStage;
 	private Stage cardStage;
 
 	public GameScreen(GDXGame game, IGame model) {
 		super(game, model);
 		// Create four provinceViews, 4 CardViews and one ChangePhaseButton.
 
 		camera.setToOrtho(false);
 		isWorld = true;
 		worldStage = new WorldStage(
 				/* TODO model.getProvinces() */new ArrayList<IProvince>());
		cardStage = new CardStage(model.getActivePlayer().getCards());
 
 	}
 
 	@Override
 	public void show() {
 
 	}
 
 	@Override
 	public void render(float render) {
 		Gdx.gl.glClearColor(0f, 0f, 1f, 1f);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 		camera.update();
 		getStage().act(Gdx.graphics.getDeltaTime());
 		getStage().draw();
 
 	}
 
 	public void changeStage() {
 		isWorld = !isWorld;
 
 	}
 
 	private Stage getStage() {
 		return isWorld ? worldStage : cardStage;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		worldStage.dispose();
 	}
 }
