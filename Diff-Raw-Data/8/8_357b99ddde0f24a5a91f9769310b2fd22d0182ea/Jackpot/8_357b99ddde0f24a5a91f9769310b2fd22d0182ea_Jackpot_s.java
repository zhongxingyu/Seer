 package com.tesserate.jackpot;
 
 import com.tesserate.game.api.ControllerDevice;
 import com.tesserate.game.api.GameCore;
 import com.tesserate.game.api.fs.ImageResource;
 import com.tesserate.game.api.fs.Resource;
 import com.tesserate.game.api.fs.ResourceManager;
 import com.tesserate.game.api.sound.SoundManager;
 import com.tesserate.game.api.ui.FullScreenDevice;
 import com.tesserate.game.api.ui.SceneGraph;
 
 public class Jackpot extends GameCore{
 
 	private Arena arena;
 
 	@Override
 	public void init() {
 		GameCore.pause();
 		arena = Arena.getInstance();
 		ControllerDevice.getInstance().setKeyListener(arena.keyMapper());
 		SceneGraph.getInstance().add( arena );
 		super.init();
 	}
 
 	@Override
 	public void update(final long elapsedTime) {
 		arena.update(elapsedTime);
 	}
 
 	@Override
 	public void loadResources() {
 		loadImageResources();
 		loadAudioResources();
 	}
 
 	private void loadAudioResources() {
 		final SoundManager soundManager = SoundManager.getInstance(); 
 		soundManager.getSound("go", Resource.loadFile("go.wav"));
 		soundManager.getSound("time", Resource.loadFile("time.wav"));
 		soundManager.getSound("hit", Resource.loadFile("hit.wav"));
 		soundManager.getSound("kill", Resource.loadFile("kill.wav"));
 	}
 
 	private void loadImageResources() {
 		addBackgroundResource();
 		addMainBallsResource();
 		addColoredBallsResource();
 	}
 
 	private void addBackgroundResource() {
		final ImageResource image = new ImageResource("lobby", String.format("images/jackpot_%dx%d.png", FullScreenDevice.getWidth(), FullScreenDevice.getHeight()));
 		ResourceManager.addResource(image);
 	}
 
 	private void addMainBallsResource() {
 		addResource("killball-a", "b_branca.png");
 		addResource("killball-i", "b_preta.png");
 	}
 
 	private void addColoredBallsResource() {
 		addResource("ball_0", "b_amarela_c.png");
 		addResource("ball_1", "b_amarela_e.png");
 		addResource("ball_2", "b_azul_c.png");
 		addResource("ball_3", "b_azul_e.png");
 		addResource("ball_4", "b_azul.png");
 		addResource("ball_5", "b_ciano_c.png");
 		addResource("ball_6", "b_ciano_e.png");
 		addResource("ball_7", "b_laranja.png");		
 		addResource("ball_8", "b_marrom.png");
 		addResource("ball_9", "b_rosa_c.png");		
 		addResource("ball_10", "b_rosa_e.png");
 		addResource("ball_11", "b_roxa.png");		
 		addResource("ball_12", "b_verde_c.png");
 		addResource("ball_13", "b_verde_e.png");		
 		addResource("ball_14", "b_vermelho_c.png");
 		addResource("ball_15", "b_vermelho_e.png");
 	}
 
 	private void addResource(final String resourceId, final String resourceFile) {
 		ImageResource image;
 		image = new ImageResource(resourceId, "images/"+resourceFile);
 		ResourceManager.addResource(image);
 	}
 
 	public static void main(final String[] args) {
 		try{
 			new Jackpot();
 		}catch (final Exception e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 }
