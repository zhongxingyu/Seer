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
 
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 import ultraextreme.model.item.AbstractWeapon;
 import ultraextreme.model.item.ItemBar;
 import ultraextreme.model.item.ItemBarUpdatedListener;
 import ultraextreme.model.util.Position;
 
 /**
  * An item bar that is shown in the view part of the game.
  * 
  * @author Daniel Jonsson
  * 
  */
 public class ItemBarPanel extends Sprite implements ItemBarUpdatedListener {
 
 	private SpriteFactory spriteFactory;
 
 	private VertexBufferObjectManager vertexBufferObjectManager;
 
 	private float alpha = 0.01f;
 
 	/**
 	 * Create an ItemBarPanel.
 	 * 
 	 * @param itemBar
 	 *            Reference to an ItemBar that this panel should show items
 	 *            from.
 	 * @param spriteFactory
 	 *            Reference to a SpriteFactory.
 	 * @param vertexBufferObjectManager
 	 * @param position
 	 *            Where the panel should be placed on the screen.
 	 * @param scaling
 	 *            Scaling so the position and size become correct.
 	 */
 	public ItemBarPanel(ItemBar itemBar, SpriteFactory spriteFactory,
 			VertexBufferObjectManager vertexBufferObjectManager,
 			Position position, float scaling) {
 		super((float) position.getX() * scaling, (float) position.getY()
 				* scaling, 750 * scaling, 75 * scaling, spriteFactory
 				.getItemBarTexture(), vertexBufferObjectManager);
 		this.spriteFactory = spriteFactory;
 		this.vertexBufferObjectManager = vertexBufferObjectManager;
 		updateItemBar(itemBar);
 		itemBar.addListener(this);
 		setAlpha(alpha);
 	}
 
 	/**
 	 * Make the item bar update itself.
 	 */
 	public void updateItemBar(ItemBar itemBar) {
 		// Clean the item bar from sprites
 		this.detachChildren();
 		// Repopulate it with sprites
 		for (int i = 0; i < itemBar.getItems().size(); i++) {
 			AbstractWeapon item = itemBar.getItems().get(i);
 			int x = i * 60 + 10;
 			int y = 10;
 			Sprite sprite = new Sprite(x, y, spriteFactory.getItemTexture(item
 					.getName()), vertexBufferObjectManager);
 			// TODO Need a way for setting alpha for the whole panel instead of
 			// for the background panel and the sprites individually. This
 			// causes the sprites to be hard to see because you can see the
 			// panel through the sprites.
 			// sprite.setAlpha(alpha);
 			this.attachChild(sprite);
 		}
 	}
 
 	@Override
 	public void updatedItemBar(ItemBar itemBar) {
 		updateItemBar(itemBar);
 	}
 }
