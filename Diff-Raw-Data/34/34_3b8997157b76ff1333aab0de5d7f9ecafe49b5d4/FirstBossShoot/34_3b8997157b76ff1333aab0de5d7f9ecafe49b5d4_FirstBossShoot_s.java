 package fr.umlv.escape.weapon;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 
 import android.graphics.Bitmap;
 import android.graphics.Point;
 
 import fr.umlv.escape.Objects;
 import fr.umlv.escape.front.FrontApplication;
 import fr.umlv.escape.ship.FirstBoss;
 import fr.umlv.escape.ship.Ship;
 import fr.umlv.escape.ship.ShipFactory;
 import fr.umlv.escape.world.EscapeWorld;
 
 /**
  * Class that represent the shoot behavior of the first boss
  * @implements {@link Shootable}.
  */
 public class FirstBossShoot implements Shootable{
 	private long lastShoot=0;
 	private FirstBoss shipBoss;
 	private int lastState;
 	private ArrayList<Ship> shipCreated;
 	
 	/**
 	 * Constructor
 	 * @param shipBoss The {@link Ship} of the boss.
 	 */
 	public FirstBossShoot(FirstBoss shipBoss) {
 		Objects.requireNonNull(shipBoss);
 		
 		this.shipBoss=shipBoss;
 		this.lastState=1;
 		this.shipCreated=new ArrayList<Ship>();
 	}
 	
 	/**
 	 * Manage how the boss have to shoot depending to his state.
 	 */
 	@Override
 	public boolean shoot(Weapon weapon,int x,int y) {
 		Objects.requireNonNull(weapon);
 		long currentTime=System.currentTimeMillis();
 		
 		Iterator<Ship> iterShip=shipCreated.iterator();
 		while(iterShip.hasNext()){
 			Ship ship=iterShip.next();
 			ship.move();
 			if(ship.shoot(ship.getPosXCenter(),ship.getPosYCenter())){
 				ship.fire();	
 			}
 		}
 
 		if(shipBoss.getState()==lastState){
 			if(shipBoss.getPosXCenter()<0){
 				weapon.setGhostShoot(1);
 			}
 			if(currentTime-lastShoot>500){
 				int ghostShoot=weapon.getGhostShoot();
 				if(ghostShoot>0){
 					weapon.setGhostShoot(ghostShoot-1);
 					lastShoot=currentTime;	
 					return false;
 				}
 			}
 			if(weapon.getLoadingBullet()!=null){
 				weapon.getLoadingBullet().loadPower();
 				if(weapon.getLoadingBullet().getCurrentLoad() == weapon.getLoadingBullet().getMaxLoad()){
 					return true;
 				}
 				return false;
 			}
 			if(currentTime-lastShoot>2000){
 				Point positionShip = new Point(x,y);
 				weapon.setLoadingBullet(weapon.fire(positionShip));
 				lastShoot=currentTime;
 				return false;
 			}
 		}
 		else if(lastState<7){
			Bitmap newImage=FrontApplication.frontImage.getImage("FirstBoss"+(shipBoss.getState()));
 			Ship ship;
 			Bitmap newShipImage;
 			PolygonShape dynamicBox=new PolygonShape();
 			switch (lastState){
 			case 1:
				newShipImage=FrontApplication.frontImage.getImage("DefaultShip");
				ship=ShipFactory.getTheShipFactory().createShip("DefaultShip", shipBoss.getPosXCenter()+newShipImage.getWidth(), shipBoss.getPosYCenter(), 50, "SquareRight");
 				break;
 			case 2:
				newShipImage=FrontApplication.frontImage.getImage("DefaultShip");
				ship=ShipFactory.getTheShipFactory().createShip("DefaultShip", shipBoss.getPosXCenter()-newShipImage.getWidth(), shipBoss.getPosYCenter(), 50, "SquareLeft");
 				break;
 			case 3:
				newShipImage=FrontApplication.frontImage.getImage("BatShip");
				ship=ShipFactory.getTheShipFactory().createShip("BatShip", shipBoss.getPosXCenter()+newShipImage.getWidth(), shipBoss.getPosYCenter(), 50, "SquareRight");
 				break;
 			case 4:
				newShipImage=FrontApplication.frontImage.getImage("BatShip");
				ship=ShipFactory.getTheShipFactory().createShip("BatShip", shipBoss.getPosXCenter()-newShipImage.getWidth(), shipBoss.getPosYCenter(), 50, "SquareLeft");
 				break;
 			case 5:
 			case 6:
				newShipImage=FrontApplication.frontImage.getImage("KamikazeShip");
				ship=ShipFactory.getTheShipFactory().createShip("KamikazeShip", shipBoss.getPosXCenter()+newShipImage.getWidth(), shipBoss.getPosYCenter(), 50, "KamikazeMove");
 				break;
 			default:
 				throw new AssertionError();
 			}
 			dynamicBox.setAsBox(((newImage.getWidth())/EscapeWorld.SCALE)/2, ((newImage.getHeight())/EscapeWorld.SCALE)/2);
 			shipBoss.getBody().getFixtureList().m_shape=dynamicBox;
 			EscapeWorld.getTheWorld().setActive(ship.getBody(),true);
 			shipCreated.add(ship);
 			lastState++;			
 		}
 		else{
 			shipBoss.setAlive(false);
 			EscapeWorld.getTheWorld().setActive(shipBoss.getBody(),false);
 			iterShip=shipCreated.iterator();
 			while(iterShip.hasNext()){
 				Ship ship=iterShip.next();
 				EscapeWorld.getTheWorld().setActive(ship.getBody(),false);
 				ship.setAlive(false);
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public void fire(Weapon weapon) {
 		Objects.requireNonNull(weapon);
 		
 		if(weapon == null) throw new IllegalArgumentException("Weapon can't be Null");
 		Bullet b = weapon.getLoadingBullet();
 		if(b == null) throw new IllegalStateException("Bullet can't be Null");
 		b.fire(new Vec2(0f,2f));
 		weapon.setLoadingBullet(null);
 	}
 }
