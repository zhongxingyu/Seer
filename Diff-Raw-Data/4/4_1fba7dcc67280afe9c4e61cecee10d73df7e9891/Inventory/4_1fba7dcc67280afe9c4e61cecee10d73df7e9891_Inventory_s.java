 package com.github.desmaster.Devio.realm.storage;
 
 import static org.lwjgl.opengl.GL11.GL_LINES;
 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glColor4f;
 import static org.lwjgl.opengl.GL11.glDisable;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glLoadIdentity;
 import static org.lwjgl.opengl.GL11.glTranslatef;
 import static org.lwjgl.opengl.GL11.glVertex2f;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.Rectangle;
 
 import com.github.desmaster.Devio.cons.Console;
 import com.github.desmaster.Devio.gfx.Screen;
 import com.github.desmaster.Devio.gfx.Text;
 import com.github.desmaster.Devio.gfx.userinterface.UserInterface;
 import com.github.desmaster.Devio.realm.item.Item;
 
 public class Inventory extends UserInterface {
 
 	public List<Item> items = new ArrayList<Item>();
 
 	public static boolean active;
 
 	public Inventory() {
 		super("Inventory");
 		container = new Rectangle(50, 50, Display.getWidth() - 125, Display.getHeight() - 125);
 
 	}
 
 	public void add(int slot, Item item) {
 		items.add(slot, item);
 	}
 
 	public boolean contains(Item item) {
 		return items.contains(items);
 	}
 
 	public void tick() {
 		if (Screen.getInput().inventory.clicked) {
 			if (Screen.canOpenScreen(this))
 				active = !active;
 		}
 
 		if (active) {
 			Screen.getPlayer().disableInput();
 		} else {
 			if (!Console.isActive())
 				if (!Screen.getPlayer().isTicking())
 					Screen.getPlayer().enableInput();
 		}
 	}
 
 	public void render() {
 		if (active) {
 			shadeBackground();
 			renderInventoryContainer();
 			renderInventory();
 		}
 	}
 
 	public void shadeBackground() {
 		glLoadIdentity();
 		glDisable(GL_TEXTURE_2D);
 		glColor4f(0.025f, 0.025f, 0.025f, 0.8f);
 		glTranslatef(0, 0, 0);
 		glBegin(GL_QUADS);
 		glVertex2f(0, 0);
 		glVertex2f(0 + Display.getWidth(), 0);
 		glVertex2f(0 + Display.getWidth(), 0 + Display.getHeight());
 		glVertex2f(0, 0 + Display.getHeight());
 		glEnd();
 	}
 
 	public void renderInventoryContainer() {
 		int x = container.getX();
 		int y = container.getY();
 		glLoadIdentity();
 		glDisable(GL_TEXTURE_2D);
 		glColor4f(.4f, .2f, .08f, 1);
 		glTranslatef(x, y, 0);
 		glBegin(GL_QUADS);
 		glVertex2f(x, y);
 		glVertex2f(x + container.getWidth() - 50, y);
 		glVertex2f(x + container.getWidth() - 50, y + container.getHeight() - 50);
 		glVertex2f(x, y + container.getHeight() - 50);
 		glEnd();
 		glLoadIdentity();
 	}
 
 	public void renderInventory() {
 		Text.drawString(name, container.getX() + 55, container.getY() + 65, 1, 1, 1, 1);
 		glColor4f(1, 1, 1, 1);
 		glBegin(GL_LINES);
 		glVertex2f(container.getX() + 50, container.getY() + 70);
 		glVertex2f(container.getX() + container.getWidth(), container.getY() + 70);
 		glEnd();
 		renderSlotBackground();
 		renderSlots();
 	}
 
 	public void renderSlotBackground() {
 		glLoadIdentity();
 		int x = container.getX() + 25;
 		int y = container.getY() + 20;
 		glColor4f(0.67f, 0.35f, 0, 1);
 		glTranslatef(x, y, 0);
 		glBegin(GL_QUADS);
 		glVertex2f(x, y);
 		glVertex2f(x + container.getWidth() - 150, y);
 		glVertex2f(x + container.getWidth() - 150, y + container.getHeight() - 115);
 		glVertex2f(x, y + container.getHeight() - 115);
 		glEnd();
 	}
 
 	public void renderSlots() {
 		glLoadIdentity();
 		int x = container.getX();
 		int y = container.getY() + 20;
 		glColor4f(1, 1, 1, 1);
 		glTranslatef(x, y, 0);
 		glBegin(GL_LINES);
 		glVertex2f(x, y);
 		glVertex2f(x + container.getWidth() - 50, y);
 		glEnd();
 		x += 50;
 		glBegin(GL_LINES);
 		glVertex2f(x, y);
 		glVertex2f(x, y + container.getHeight() - 115);
 		glEnd();
 		y += container.getHeight() - 115;
 		glBegin(GL_LINES);
 		glVertex2f(x, y);
 		glVertex2f((x) + container.getWidth() - 150, y);
 		glEnd();
 		x += container.getWidth() - 150;
 		y -= container.getHeight() - 115;
 		glBegin(GL_LINES);
 		glVertex2f(x, y);
 		glVertex2f(x, y + container.getHeight() - 115);
 		glEnd();
 		int width = (container.getWidth()) / 32;
 		int height = (container.getHeight()) / 32;
 		glColor4f(1, 1, 1, 1);
 		
		for (int xx = 0; x < width; x++) {
			for (int yy = 0; y < height; y++) {
 				glBegin(GL_LINES);
 				xx *= 32 / 2;
 				yy *= 32 / 2;
 				glVertex2f(xx, yy);
 				glVertex2f(xx + 32, yy + 32);
 				glEnd();
 			}
 		}
 		
 	}
 
 	public boolean contains(Item item, int count) {
 		List<Item> items = new ArrayList<Item>();
 		for (int i = 0; i < count; i++) {
 			items.add(item);
 		}
 		return items.containsAll(items);
 	}
 
 	public static boolean isActive() {
 		return active;
 	}
 
 	public static void setActive(boolean bool) {
 		active = bool;
 	}
 
 }
