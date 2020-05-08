 package fearlesscode;
 
 import fearlesscode.util.*;
 import java.util.*;
 
 /**
  * A sima blokkot reprezentálja, kezeli a benne található objektumokat
  * (objektumok interakciója a játékossal).
  */
 public class FilledBlock extends Block
 {
 	public FilledBlock(PlayField pf)
 	{
 		super(pf);
 	}
 	
 	/**
 	 * A blokkok kközötti mozgást kezeli le.
 	 */
 	public void checkBorders()
 	{
 		ArrayList<Player> leaveList=new ArrayList<Player>();
 		for(PlayerContainer player:players)
 		{
 			EntityPosition currentPosition=player.getPosition();
 			EntityPosition nextPosition=player.getPlayer().getNextPosition(player.getPosition());
 			EntityPosition entryPosition=null;
 			int dir=-1;
 			if(nextPosition.getX()<=0 && currentPosition.getX()>0)
 			{
 				dir=3;
 				entryPosition=new EntityPosition(Block.WIDTH, currentPosition.getY());
 			}
 			else if(nextPosition.getX()+Player.WIDTH >= Block.WIDTH && currentPosition.getX()+Player.WIDTH < Block.WIDTH)
 			{
 				dir=1;
				entryPosition=new EntityPosition(0, currentPosition.getY());
 			}
 			else if(nextPosition.getY()<=0 && currentPosition.getY()>0)
 			{
 				dir=0;
 				entryPosition=new EntityPosition(currentPosition.getX(), Block.HEIGHT);
 			}
 			else if(nextPosition.getY()+Player.HEIGHT >= Block.HEIGHT && currentPosition.getX()+Player.HEIGHT < Block.HEIGHT)
 			{
 				dir=2;
				entryPosition=new EntityPosition(currentPosition.getX(), 0);
 			}
 			if(dir != -1)
 			{
 				Block neighbour=getNeighbour(dir);
 				if(neighbour != null && neighbour.matches(neighbour, dir, true))
 				{
 					Logger.debug("enterBlock "+neighbour.getName());
 					player.getPlayer().enterBlock(neighbour, entryPosition);
 				}
 				else if(dir == 2)
 				{
 					playField.resetPlayer(player);
 				}
 				else
 				{
 					player.getPlayer().move(
 						new Speed(
 							-player.getPlayer().getSpeed().getX()*(dir%2),
 							-player.getPlayer().getSpeed().getY()*((dir+1)%2)));
 				}
 			}
 			if( nextPosition.getY() > Block.HEIGHT ||
 				nextPosition.getY()+Player.HEIGHT < 0 ||
 				nextPosition.getX() > Block.WIDTH ||
 				nextPosition.getX()+Player.WIDTH < 0)
 			{
 				leaveList.add(player.getPlayer());
 			}
 			player.setPosition(player.getPlayer().getNextPosition(currentPosition));
 			Logger.log(
 				player.getPlayer().getName()+
 				" is now at ("+
 				player.getPosition().getX()+
 				","+
 				player.getPosition().getY()+
 				") in "+
 				getName());
 		}
 		for(Player p:leaveList)
 		{
 			p.leaveBlock(this);
 		}
 	}
 	
 	/**
 	 * Végignézi az összes objektumot, hogy ütközik-e a jétékosokkal,
 	 * ha pedig igen, meghívja a meetPlayer metódusukat.
 	 */
 	public void processCollisions()
 	{
 		for(PlayerContainer player:players)
 		{
 			Rectangle playerBox=player.getPlayer().getBoundingBox();
 			EntityPosition nextPosition=player.getPlayer().getNextPosition(player.getPosition());
 			for(EntityContainer container:entities)
 			{
 				Rectangle entityBox=container.getEntity().getBoundingBox();
 				if(CollisionProcesser.isCollied(nextPosition, playerBox, container.getPosition(), entityBox))
 				{
 					Logger.log(player.getPlayer(),"collided with "+container.getEntity().getName());
 					container.getEntity().meetPlayer(player);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * A FilledBlock információk lekérésére.
 	 * @param pos A FilledBlock poziciója.
 	 * @return A Block koordinátáit a PlayField-en belül, melyik Player-ek, és milyen Entity-k tartózkodnak benne, és hogy milyen szomszédai vannak.
 	 */
 	public String getInfo(Position pos)
 	{
 		String entityList="";
 		if(entities.size()>0)
 		{
 			for(EntityContainer container:entities)
 			{
 				entityList+=container.getEntity().getName();
 			}
 		}
 		else
 		{
 			entityList="none";
 		}
 
 		String playerList="";
 		if(players.size()>0)
 		{
 			for(PlayerContainer container:players)
 			{
 				playerList+=container.getPlayer().getName();
 			}
 		}
 		else
 		{
 			playerList="none";
 		}
 
 		String neighbourList="";
 		for(Block neighbour:neighbours)
 		{
 			if(neighbour != null)
 			{
 				neighbourList+=neighbour.getName();
 			}
 			else
 			{
 				neighbourList+="[none]";
 			}
 		}
 		String posString ="("+pos.getX()+","+pos.getY()+")";
 
 		return getName()+"\r\n"+
 			"  Coordinates:"+posString+"\r\n"+
 			"  Players:"+playerList+"\r\n"+
 			"  Entities:"+entityList+"\r\n"+
 			"  Neighbours:"+neighbourList;
 	}
 	
 	
 	/**
 	 * A név és az ID lekérésére szolgáló metódus.
 	 * @return Szögletes zárójelek között visszaadja az ID-t és a nevet. ([ID:név])
 	 */
 	public String getName()
 	{
 		return "["+ID+":FilledBlock]";
 	}
 
 	public boolean matches(Block other, int dir, boolean callback)
 	{
 		BlockMatcher matcher=new BlockMatcher(this, other, dir);
 		if(matcher.matches())
 		{
 			if(callback)
 			{
 				return other.matches(this, (dir+2)%4, false);
 			}
 			return true;
 		}
 		return false;
 	}
 }
