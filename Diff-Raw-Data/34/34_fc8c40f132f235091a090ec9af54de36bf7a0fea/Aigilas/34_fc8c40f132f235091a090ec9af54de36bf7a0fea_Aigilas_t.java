 package aigilas;
 
 import spx.core.Settings;
 import spx.core.SpxManager;
 import spx.devtools.DevConsole;
 import spx.graphics.SpriteSheetManager;
 import spx.io.Input;
 import spx.io.PlayerIndex;
 import spx.net.Client;
 import spx.particles.ParticleEngine;
 import spx.states.StateManager;
 import spx.text.TextManager;
 import aigilas.management.Commands;
 import aigilas.management.InputInitializer;
 import aigilas.management.SpriteInitializer;
 import aigilas.states.MainMenuState;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 
 public class Aigilas implements ApplicationListener {
 	private boolean IsRunning = true;
 
 	private void SetIsRunning(boolean isRunning) {
 		IsRunning = isRunning;
 	}
 
 	@Override
 	public void create() {
 		SpxManager.Setup();
 		Input.Setup(new InputInitializer());
 		SpriteSheetManager.Setup(new SpriteInitializer());
 		StateManager.LoadState(new MainMenuState());
 		ParticleEngine.Reset();
 		StateManager.LoadContent();
 		// //$$$MediaPlayer.Play(Content.Load<Song>("MainTheme"));
 		// //$$$MediaPlayer.IsRepeating = true;
 	}
 
 	@Override
 	public void resize(int width, int height) {
 
 	}
 
 	@Override
 	public void render() {
 		if (Settings.Get().consoleLogging) {
 			DevConsole.Get().Add("" + Gdx.graphics.getFramesPerSecond() + ": " + Gdx.graphics.getDeltaTime());
 		}
 
 		// Update
 		Input.Update();
		if (Input.IsActive(Commands.ToggleDevConsole,Client.Get().GetFirstPlayerIndex())){
 			DevConsole.Get().Toggle();
 		}
 		if (Client.Get().NextTurn()) {
 			for (int ii = 0; ii < 4; ii++) {
 				PlayerIndex player = PlayerIndex.values()[ii];
 				/*
 				 * //$$$ if (GamePad.GetState(player).IsPressed(Buttons.Back) &&
 				 * GamePad.GetState(player).IsPressed(Buttons.Start)) {
 				 * SetIsRunning(false); }
 				 */
 			}
 			ParticleEngine.Update();
 			StateManager.Update();
 			TextManager.Update();
 			Client.Get().PrepareForNextTurn();
 		}
 		else {
 			Client.Get().HeartBeat();
 		}
 		if (!IsRunning) {
 			System.exit(0);
 		}
 
 		// Render
 		SpxManager.Renderer.Begin();
 		StateManager.Draw();
 		ParticleEngine.Draw();
 		TextManager.Draw();
 		DevConsole.Get().Draw();
 		SpxManager.Renderer.End();
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	@Override
 	public void dispose() {
 	}
 }
