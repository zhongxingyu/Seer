 /* ============================================================
  * Copyright 2012 Bjorn Persson Mattsson, Johan Gronvall, Daniel Jonsson,
  * Viktor Anderling
  *
  * This file is part of UltraExtreme.
  *
  * UltraExtreme is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * UltraExtreme is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with UltraExtreme. If not, see <http://www.gnu.org/licenses/>.
  * ============================================================ */
 
 package ultraextreme.view;
 
 import java.util.Collections;
 import java.util.List;
 
 import javax.vecmath.Vector2d;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.scene.background.SpriteBackground;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.FontFactory;
 import org.andengine.opengl.font.FontManager;
 import org.andengine.opengl.texture.TextureManager;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.util.color.Color;
 
 import ultraextreme.model.util.Dimension;
 import ultraextreme.model.util.Position;
 import ultraextreme.view.SpriteFactory.HighScoresTexture;
 import android.graphics.Typeface;
 
 /**
  * 
  * @author Bjorn Persson Mattsson
  * @author Daniel Jonsson
  * 
  */
 public class HighscoreScene extends MenuScene {
 
 	/**
 	 * Destination variable used by the return button and in the controller.
 	 */
 	public static final int GOTO_MENU = 0;
 
 	/**
 	 * Number of high scores what will shop up in the list.
 	 */
 	private static final int NR_OF_HIGHSCORES = 10;
 
 	/**
 	 * Distance between two high score entries in the list.
 	 */
	private static final int HIGHSCORE_DISPERSION = 40;
 
 	/**
 	 * References to the high score list entries.
 	 */
 	private HighscoreText[] highscores = new HighscoreText[NR_OF_HIGHSCORES];
 
 	/**
 	 * The width of the screen. This needs to be stored so the high score list
 	 * easily can be updated with new centered entries.
 	 */
 	private float screenWidth;
 
 	/**
 	 * Create a high score scene.
 	 * 
 	 * @param camera
 	 * @param fontManager
 	 *            Needed so the scene can create its own Font with a appropriate
 	 *            font size.
 	 * @param textureManager
 	 *            Needed so the scene can create its own Font with a appropriate
 	 *            font size.
 	 * @param vbo
 	 */
 	public HighscoreScene(Camera camera, FontManager fontManager,
 			TextureManager textureManager, VertexBufferObjectManager vbo) {
 		super(camera);
 
 		/*
 		 * Add the background.
 		 */
 		screenWidth = camera.getWidth();
 		final float screenHeight = camera.getHeight();
 		final SpriteBackground background = new SpriteBackground(new Sprite(0,
 				0, screenWidth, screenHeight,
 				SpriteFactory
 						.getHighScoresTexture(HighScoresTexture.BACKGROUND),
 				vbo));
 		setBackground(background);
 
 		/*
 		 * Scaling
 		 */
 		final Dimension screenDimension = new Dimension(screenWidth,
 				screenHeight);
 		// The following resolution is what the background and buttons were made
 		// for
 		Vector2d scaling = screenDimension
 				.getQuotient(new Dimension(800, 1280));
 
 		/*
 		 * Add a return-to-main-menu button.
 		 */
 		addMenuItem(createButton(GOTO_MENU, 1000,
 				HighScoresTexture.RETURN_BUTTON, scaling, screenWidth, vbo));
 
 		/*
 		 * Create a new scaled font.
 		 */
 		float fontSize = (float) scaling.y * 32f;
 		Font font = FontFactory.create(fontManager, textureManager, 256, 256,
 				Typeface.create(Typeface.DEFAULT, Typeface.BOLD), fontSize,
 				Color.WHITE_ARGB_PACKED_INT);
 		font.load();
 
 		/*
 		 * Add basic high score text entries to the scene. These don't have any
 		 * text yet and aren't placed correctly along the x axis.
 		 */
 		for (int i = 0; i < highscores.length; i++) {
			highscores[i] = new HighscoreText(new Position(0, scaling.y * 420
					+ i * HIGHSCORE_DISPERSION), font, vbo, i + 1);
 			attachChild(highscores[i]);
 		}
 	}
 
 	// FIXME: Copied method from OptionsScene and slightly modified. Maybe we
 	// could use a menu abstract class or something.
 	/**
 	 * Create a menu button.
 	 * 
 	 * @param destination
 	 * @param y
 	 * @param texture
 	 * @param scaling
 	 * @param screenWidth
 	 * @param vertexBufferObjectManager
 	 */
 	private IMenuItem createButton(final int destination, final int y,
 			final HighScoresTexture texture, final Vector2d scaling,
 			final float screenWidth,
 			final VertexBufferObjectManager vertexBufferObjectManager) {
 		final IMenuItem button = new SpriteMenuItem(destination,
 				SpriteFactory.getHighScoresTexture(texture),
 				vertexBufferObjectManager);
 		button.setWidth((float) scaling.x * button.getWidth());
 		button.setHeight((float) scaling.y * button.getHeight());
 		button.setX((screenWidth - button.getWidth()) / 2);
 		button.setY((float) scaling.y * y);
 		return button;
 	}
 
 	/**
 	 * Displays the high scores on the screen.
 	 * 
 	 * @param highscores
 	 *            The list of high scores.
 	 */
 	public void displayHighscore(List<Highscore> highscores) {
 		Collections.sort(highscores);
 
 		for (int i = 0; i < this.highscores.length; i++) {
 			if (i < highscores.size()) {
 				this.highscores[i].setHighscore(highscores.get(i));
 			} else {
 				this.highscores[i].setHighscore(Highscore.EMPTY_HIGHSCORE);
 			}
 			this.highscores[i].setX((screenWidth - this.highscores[i]
 					.getWidth()) / 2);
 		}
 	}
 }
