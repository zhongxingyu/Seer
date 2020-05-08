 package com.banished.core.entities.nonliving;
 
 import com.banished.SoundPlayer;
 import com.banished.core.Direction;
 import com.banished.core.Location;
 import com.banished.core.Tile;
 import com.banished.core.World;
 import com.banished.core.entities.Entity;
 import com.banished.core.entities.EntityEventListener;
 import com.banished.core.entities.Player;
 import com.banished.core.entities.RectangleCollisionShape;
 import com.banished.core.items.Inventories;
 import com.banished.core.items.Inventory;
 import com.banished.graphics.Image;
 
 public class ChestEntity extends Entity
 {
 	private boolean closed;
 	
 	private Image image;
 	private Inventory inv;
 	
 	static int CHEST_INV_LEFT = 0, CHEST_INV_TOP = 40;
 	
 	public ChestEntity(World world, Location location, Inventory inv)
 	{
 		super(world, location,
 				new RectangleCollisionShape(location.getX(), location.getY(), 30f/Tile.SIZE, 30f/Tile.SIZE),
 				Direction.North, new Location());
 		this.setEventListener(new ChestEventListener(this));
 		
 		this.closed = true;
 		this.inv = inv;
 		this.inv.setLocation(new Location(CHEST_INV_LEFT, CHEST_INV_TOP));
 		Inventories.add(this.inv);
 	}
 	
 	public void update(double frameTime) 
 	{
 		if(!closed) 
 		{
 			//Displays the chest and player inventory
 			this.image = Image.fromFile("entities/object_entities/chest/Chest2.png");
 		}
 		else this.image = Image.fromFile("entities/object_entities/chest/Chest0.png");
		
		if (Player.get().getLocation().distanceTo(getLocation()) > Player.INTERACTION_RANGE)
		{
			closed = true;
			inv.setShown(false);
		}
 	}
 	
 	public Image getImage()
 	{
 		
 		return this.image;
 	}
 	
 	private static class ChestEventListener implements EntityEventListener
 	{
 		private ChestEntity chest;
 		
 		public ChestEventListener(ChestEntity chest)
 		{
 			this.chest = chest;
 		}
 		
 		public void onTouched(Entity sender) { }
 		public void onLeft(Entity sender) 
 		{
 			System.out.println("left chest");
 			System.out.println("  player? " + (sender == Player.get()));
 			if (sender == Player.get())
 			{	
 				//chest.closed = true;
 				chest.inv.setShown(false);
 			}
 		}
 		public void onInteractedWith()
 		{
 			if(chest.closed)
 			{
 				SoundPlayer.getPlayer(SoundPlayer.Sound.ChestOpen).rewind();
 				SoundPlayer.getPlayer(SoundPlayer.Sound.ChestOpen).play();
 			}
 			else
 			{
 				SoundPlayer.getPlayer(SoundPlayer.Sound.ChestClose).rewind();
 				SoundPlayer.getPlayer(SoundPlayer.Sound.ChestClose).play();
 			}
 			chest.closed = !chest.closed;
 			chest.inv.setShown(!chest.closed);
 		}
 	}
 	
 	public String toText()
 	{
 		return super.toText() + " closed=" + closed;
 	}
 }
