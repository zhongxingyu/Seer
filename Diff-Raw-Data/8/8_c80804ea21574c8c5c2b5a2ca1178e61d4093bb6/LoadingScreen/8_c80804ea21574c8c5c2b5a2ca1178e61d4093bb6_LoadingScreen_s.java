 package com.jeremyP.diseasedefense;
 
 import com.jeremyP.diseasedefense.framework.Game;
 import com.jeremyP.diseasedefense.framework.Graphics;
 import com.jeremyP.diseasedefense.framework.Pixmap;
 import com.jeremyP.diseasedefense.framework.Screen;
 import com.jeremyP.diseasedefense.framework.Graphics.PixmapFormat;
 
 public class LoadingScreen extends Screen
 {
 	public LoadingScreen(Game game)
 	{
 		super(game);
 	}
 	
 	public void update(float deltaTime)
 	{
 		Graphics g = game.getGraphics();
 		Assets.levelBackground[0] = g.newPixmap("bloodstream.png", PixmapFormat.RGB565);
 		Assets.levelBackground[1] = g.newPixmap("brain.png", PixmapFormat.RGB565);
		Assets.levelBackground[2] = g.newPixmap("blue_blood_cell.jpg", PixmapFormat.RGB565);
		Assets.levelBackground[3] = g.newPixmap("inside-human-body-2-28.jpg", PixmapFormat.RGB565);
		Assets.levelBackground[4] = g.newPixmap("purple-blood-cells.jpg", PixmapFormat.RGB565);
		Assets.levelBackground[5] = g.newPixmap("inside_human_heart.jpg", PixmapFormat.RGB565);
 		Assets.helpScreen1 = g.newPixmap("helpScreen1.png", PixmapFormat.RGB565);
 		Assets.helpScreen2 = g.newPixmap("helpScreen2.png", PixmapFormat.RGB565);
 		Assets.helpScreen3 = g.newPixmap("helpScreen3.png", PixmapFormat.RGB565);
 		Assets.helpScreen4 = g.newPixmap("helpScreen4.png", PixmapFormat.RGB565);
 		Assets.helpScreen5 = g.newPixmap("helpScreen5.png", PixmapFormat.RGB565);
 		Assets.helpScreen6 = g.newPixmap("helpScreen6.png", PixmapFormat.RGB565);
 		Assets.pc1 = g.newPixmap("luketheleukocyte1.png", PixmapFormat.ARGB4444);
 		Assets.pc2 = g.newPixmap("luketheleukocyte2.png", PixmapFormat.ARGB4444);
 		Assets.pc3 = g.newPixmap("luketheleukocyte3.png", PixmapFormat.ARGB4444);
 		Assets.pc4 = g.newPixmap("luketheleukocyte4.png", PixmapFormat.ARGB4444);
 		Assets.pc5 = g.newPixmap("luketheleukocyte5.png", PixmapFormat.ARGB4444);
 		Assets.pc6 = g.newPixmap("luketheleukocyte6.png", PixmapFormat.ARGB4444);
 		Assets.pc7 = g.newPixmap("luketheleukocyte7.png", PixmapFormat.ARGB4444);
 		Assets.pc8 = g.newPixmap("luketheleukocyte8.png", PixmapFormat.ARGB4444);
 		Assets.pc9 = g.newPixmap("luketheleukocyte9.png", PixmapFormat.ARGB4444);
 		Assets.pc10 = g.newPixmap("luketheleukocyte10.png", PixmapFormat.ARGB4444);
 		Assets.pc11 = g.newPixmap("luketheleukocyte11.png", PixmapFormat.ARGB4444);
 		Assets.pc12 = g.newPixmap("luketheleukocyte12.png", PixmapFormat.ARGB4444);
 		Assets.pc13 = g.newPixmap("luketheleukocyte13.png", PixmapFormat.ARGB4444);
 		Assets.title = g.newPixmap("diseasedefensetitle.png", PixmapFormat.ARGB4444);
 		Assets.gameover = g.newPixmap("gameover.png", PixmapFormat.ARGB4444);
 		Assets.youWin = g.newPixmap("You_win.png", PixmapFormat.ARGB4444);
 		Assets.contin = g.newPixmap("continue_button.png", PixmapFormat.ARGB4444);
 		Assets.mainmenu = g.newPixmap("menubuttons.png", PixmapFormat.ARGB4444);
 		Assets.pauseMenu = g.newPixmap("pauseMenu.png", PixmapFormat.ARGB4444);
 		Assets.pauseButton = g.newPixmap("pauseButton.png", PixmapFormat.ARGB4444);
 		Assets.numbers = g.newPixmap("numbers.png", PixmapFormat.ARGB4444);
 		
 		//First badguy
 		Assets.badGuys[0] = new Pixmap[7];
 		Assets.badGuys[0][0] = g.newPixmap("badGuy.png", PixmapFormat.ARGB4444);
 		Assets.badGuys[0][1] = g.newPixmap("badGuy2.png", PixmapFormat.ARGB4444);
 		Assets.badGuys[0][2] = g.newPixmap("badGuy3.png", PixmapFormat.ARGB4444);
 		Assets.badGuys[0][3] = g.newPixmap("badGuy4.png", PixmapFormat.ARGB4444);
 		Assets.badGuys[0][4] = g.newPixmap("badGuy5.png", PixmapFormat.ARGB4444);
 		Assets.badGuys[0][5] = g.newPixmap("badGuy6.png", PixmapFormat.ARGB4444);
 		Assets.badGuys[0][6] = g.newPixmap("badGuy7.png", PixmapFormat.ARGB4444);
 		
 		//Second badguy
 		Assets.badGuys[1] = new Pixmap[5];
 		Assets.badGuys[1][0] = g.newPixmap("badGuy2-1.png", PixmapFormat.ARGB4444);
 		Assets.badGuys[1][1] = g.newPixmap("badGuy2-2.png", PixmapFormat.ARGB4444);
 		Assets.badGuys[1][2] = g.newPixmap("badGuy2-3.png", PixmapFormat.ARGB4444);
 		Assets.badGuys[1][3] = g.newPixmap("badGuy2-4.png", PixmapFormat.ARGB4444);
 		Assets.badGuys[1][4] = g.newPixmap("badGuy2-5.png", PixmapFormat.ARGB4444);
 		Assets.click = game.getAudio().newSound("click.ogg");
 		game.setScreen(new MainMenuScreen(game));
 	}
 
 	@Override
 	public void present(float deltaTime) {
 		// TODO Auto-generated method stub
 		
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
 }
