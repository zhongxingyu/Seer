 package fr.umlv.escape.bonus;
 
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.Filter;
 
 import android.graphics.Bitmap;
 
 import fr.umlv.escape.Objects;
 import fr.umlv.escape.front.BattleField;
 import fr.umlv.escape.front.FrontApplication;
 import fr.umlv.escape.front.FrontImages;
 import fr.umlv.escape.world.Bodys;
 import fr.umlv.escape.world.EscapeWorld;
 
 /**This class supplies methods to create properly a {@link Bonus}.
  */
 public class BonusFactory {
 	private final FrontImages frontImages;
 	private final BattleField battleField;
 	
 	public BonusFactory(BattleField battleField){
 		this.frontImages = FrontApplication.frontImage;
 		this.battleField = battleField;
 	}
 
 	 /** Create a {@link Bonus}.
 	  * 
 	  * @param bonusName The name of the bonus to create.
 	  * @param posX The x position of the bonus to create.
 	  * @param posY The y position of the bonus to create.
 	  * @param type The type of the bonus to create. Each {@link Bonus} can have their owns types.
 	  * @return the bonus created.
 	  */
 	public Bonus createBonus(String bonusName, int posX, int posY, int type) {
 		Objects.requireNonNull(bonusName);
 		
 		Bonus bonus;
 		String stringType;
 		Body body;
 		Bitmap img;
 		
 		if(bonusName.equals("weapon_reloader")){
 			switch(type){
 			case 1:
 				stringType="missile_launcher";
 				img=frontImages.getImage(bonusName+"_"+stringType);
 				body=Bodys.createBasicRectangle(posX, posY, img.getWidth(), img.getHeight(), 0);
 				bonus= new Bonus(50, body,stringType,img);
 				break;
 			case 2:
 				stringType="flame_thrower";
				System.out.println("=======>");
				System.out.println(bonusName);
				System.out.println(stringType);
				img=frontImages.getImage(bonusName+stringType);
 				body=Bodys.createBasicRectangle(posX, posY, img.getWidth(), img.getHeight(), 0);
 				bonus= new Bonus(25, body,stringType,img);
 				break;
 			case 3:
 				stringType="shiboleet_thrower";
 				img=frontImages.getImage(bonusName+"_"+stringType);
 				body=Bodys.createBasicRectangle(posX, posY, img.getWidth(), img.getHeight(), 0);
 				bonus= new Bonus(10, body,stringType,img);
 				break;
 			case 4:
 				stringType="laser_beam";
 				img=frontImages.getImage(bonusName+"_"+stringType);
 				body=Bodys.createBasicRectangle(posX, posY, img.getWidth(), img.getHeight(), 0);
 				bonus= new Bonus(10, body,stringType,img);
 				break;
 			default : throw new IllegalArgumentException(type+" isn't a legal type");
 			}
 		} else{
 			throw new IllegalArgumentException(bonusName+" isn't a legal bonus");
 		}
 		
 		Filter filter=new Filter();
 		filter.categoryBits=EscapeWorld.CATEGORY_BONUS;
 		filter.maskBits=EscapeWorld.CATEGORY_PLAYER;
 		body.getFixtureList().setFilterData(filter);
 		body.setActive(true);
 
 		battleField.addBonus(bonus);
 		return bonus;
 	}
 }
