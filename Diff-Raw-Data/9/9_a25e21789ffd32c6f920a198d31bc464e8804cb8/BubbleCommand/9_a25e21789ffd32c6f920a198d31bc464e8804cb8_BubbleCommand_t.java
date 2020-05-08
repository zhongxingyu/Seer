 package com.mel.wallpaper.starWars.entity.commands;
 
 
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.AlphaModifier;
 import org.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
 import org.andengine.util.debug.Debug;
 import org.andengine.util.modifier.IModifier;
 import org.andengine.util.modifier.ease.EaseLinear;
 import org.andengine.util.modifier.ease.EaseQuartOut;
 
 import com.mel.entityframework.Game;
 import com.mel.util.MathUtil;
 import com.mel.util.Point;
 import com.mel.wallpaper.starWars.entity.Bubble;
 import com.mel.wallpaper.starWars.entity.Bubble.BubbleType;
 import com.mel.wallpaper.starWars.entity.LaserBeam;
 import com.mel.wallpaper.starWars.entity.Map;
 import com.mel.wallpaper.starWars.entity.Walker;
 import com.mel.wallpaper.starWars.entity.Walker.Rol;
 import com.mel.wallpaper.starWars.sound.SoundLibrary;
 import com.mel.wallpaper.starWars.sound.SoundLibrary.Sample;
 import com.mel.wallpaper.starWars.view.Position;
 import com.mel.wallpaper.starWars.view.SpriteFactory;
 
 public class BubbleCommand extends MoveCommand
 {
 	public Bubble bubble;
 	private Game game;
 	
 	public BubbleCommand(Walker walker, Game game) {
 		super(walker, 1.4f, EaseLinear.getInstance());
 		
 		this.walker = walker;
 		this.game = game;
 		
 		Position bubblePosition = walker.position.clone();
 		
		bubblePosition.setY(bubblePosition.getY());
 		
 		switch(walker.rol)
 		{
 		case CHUWAKA:
 			bubble = new Bubble(BubbleType.BUBBLE_OLA_K_ASE, bubblePosition);
 			break;
 		case JEDI:
 			bubble = new Bubble(BubbleType.BUBBLE_TU_PADRE, bubblePosition);
 			break;
 		case STORM_TROOPER:
 			bubble = new Bubble(BubbleType.BUBBLE_OLA_K_ASE, bubblePosition);
 			break;
 		default:
 			bubble = new Bubble(BubbleType.BUBBLE_OLA_K_ASE, bubblePosition);
 		}
 		
 		movable = bubble;
 		game.addEntity(bubble);
 	}
 	
 	@Override
 	public void execute(Map p) {
 		//increase destination
 //		this.destination = MathUtil.getPuntDesti(this.bubble.position.toPoint(), this.destination, 2000f);
 		destination = new Point(bubble.position.getX(), bubble.position.getY() + 50);
 		super.execute(p);
 
 		bubble.playSound();
 		
 		walker.forceStopMovement();
 		//walker.animateTalking();
 		
 		bubble.getSprite().registerEntityModifier(new AlphaModifier(2f, 1f, 0f, new IEntityModifierListener() {
 			public void onModifierStarted(IModifier<IEntity> arg0, IEntity sprite) {	
 			}
 			public void onModifierFinished(IModifier<IEntity> arg0, IEntity sprite) {
 				bubble.isFinished = true;
 			}
 		}));
 	}
 }
