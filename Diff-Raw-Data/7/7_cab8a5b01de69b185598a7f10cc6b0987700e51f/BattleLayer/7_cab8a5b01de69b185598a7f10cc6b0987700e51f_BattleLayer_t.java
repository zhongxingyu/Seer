 package contest.lab.gala;
 
 import org.cocos2d.actions.base.CCAction;
 import org.cocos2d.actions.base.CCFiniteTimeAction;
 import org.cocos2d.actions.base.CCRepeatForever;
 import org.cocos2d.actions.instant.CCCallFuncN;
 import org.cocos2d.actions.interval.CCAnimate;
 import org.cocos2d.actions.interval.CCMoveBy;
 import org.cocos2d.actions.interval.CCSequence;
 import org.cocos2d.layers.CCLayer;
 import org.cocos2d.nodes.CCAnimation;
 import org.cocos2d.nodes.CCDirector;
 import org.cocos2d.nodes.CCSprite;
 import org.cocos2d.types.CGPoint;
 
 import android.view.MotionEvent;
 import contest.lab.gala.callback.GetDamagedCallback;
 import contest.lab.gala.data.CurrentUserInformation;
 import contest.lab.gala.data.SkillType;
 
 public class BattleLayer extends CCLayer implements GetDamagedCallback{
 	public static CCSprite btn_skill_bark_activated = CCSprite.sprite("minigame/btn_skill_bark_activated.png");
 	public static CCSprite btn_skill_bark_normal = CCSprite.sprite("minigame/btn_skill_bark_normal.png");
 	public static CCSprite btn_skill_bone_activated = CCSprite.sprite("minigame/btn_skill_bone_activated.png");
 	public static CCSprite btn_skill_bone_normal = CCSprite.sprite("minigame/btn_skill_bone_normal.png");
 	public static CCSprite btn_skill_punch_activated = CCSprite.sprite("minigame/btn_skill_punch_activated.png");
 	public static CCSprite btn_skill_punch_normal = CCSprite.sprite("minigame/btn_skill_punch_normal.png");
 	
 	public static CCSprite gage_bar = CCSprite.sprite("minigame/gage_bar.png");
 	public static CCSprite gage_bar_black = CCSprite.sprite("minigame/bg_gage_bar.png");
 	
 	
 	public static float gage;
 	
 	////////////////////////////////////////////////////////////////////////////
 	CCSprite bg_battlelayer = CCSprite.sprite("battle/bg_battlelayer.png");
 	CCSprite bg_hp_bar = CCSprite.sprite("battle/bg_hp_bar.png");
 	CCSprite hp_bar_mine = CCSprite.sprite("battle/hp_bar.png");
 	CCSprite hp_bar_opponent = CCSprite.sprite("battle/hp_bar.png");
 	CCSprite title = CCSprite.sprite("battle/title.png");
 
 	static float hp;
 	
 	int ACTION_FLYING = 100;
 	int ACTION_DAMAGED = 200;
 	int ACTION_ATTACK = 300;
 	static int numOfCurrentActions = 0;
 	
 	CCCallFuncN returnToNormalMode;
 	CCSequence damagedAndReturnToNormal_mine;
 	CCSequence damagedAndReturnToNormal_opponent;
 	
 	CCSprite coming_bark;
 	CCSprite going_bark;
 	CCSprite coming_bone;
 	CCSprite going_bone;
 	CCSprite coming_punch;
 	CCSprite going_punch;
 
 	//  ĳ ̹
 	CCSprite attack_bark_opponent;
 	CCSprite attack_bark_mine;
 
 	CCSprite attack_bone_opponent;
 	CCSprite attack_bone_mine;
 
 	CCSprite attack_punch_opponent;
 	CCSprite attack_punch_mine;
 
 	CCFiniteTimeAction damagedAction;	// ƿ
 
 	CCFiniteTimeAction attackAction;	// ư
 
 	CCSequence damagedSequence_bark;	// ƿ   ´ ִϸ̼
 	//	CCSequence damagedSequence_bone;
 	//	CCSequence damagedSequence_punch;
 
 	CCSequence attackSequence_bark;		// ư   ´ ִϸ̼
 	//	CCSequence attackSequence_bone;
 	//	CCSequence attackSequence_punch;
 
 //	CCAnimation attackAnimation_mine;
 //	CCAnimation attackAnimation_opponent;
 //	CCAnimate attackAnimate_mine;
 //	CCAnimate attackAnimate_opponent;
 
 	CCAnimation damagedAnimation_mine;
 	CCAnimation damagedAnimation_opponent;
 	CCAnimate damagedAnimate_mine;
 	CCAnimate damagedAnimate_opponent;
 
 	CCAnimation normalAnimation_mine;
 	CCAnimation normalAnimation_opponent;
 	CCAnimate normalAnimate_mine;
 	CCAnimate normalAnimate_opponent;
 
 	CCSprite myCharacter_normal;
 	CCSprite opponentCharacter_normal;
 	CCSprite myCharacter_hurted;
 	CCSprite opponentCharacter_hurted;
 
 	
 	public void makeNormalAnimation()
 	{
 		normalAnimation_mine = CCAnimation.animation("animation1", 0.15f);
 //		myCharacter_normal = CCSprite.sprite(String.format("character/char%d_normal1.png", CurrentUserInformation.userChar));
 //		myCharacter_normal.setPosition(139 * Manager.ratio_width, 1019 * Manager.ratio_height);
 		for(int i = 2; i <= 5; i++ )
 		{
 			CCSprite frame = CCSprite.sprite(String.format("character/char%d_normal%d.png", CurrentUserInformation.userChar, i));
 			frame.setScaleX(-1 * Manager.ratio_width);
 			frame.setScaleY(Manager.ratio_height);
 			frame.setPosition(212 * Manager.ratio_width, 1019 * Manager.ratio_height);
 			normalAnimation_mine.addFrame(frame.getTexture());
 		}
 //		normalAnimate_mine = CCRepeatForever.action(CCAnimate.action(normalAnimation_mine, true));
 		normalAnimate_mine = CCAnimate.action(normalAnimation_mine, false);
 		
 		normalAnimation_opponent = CCAnimation.animation("animation2",0.15f);
 		opponentCharacter_normal = CCSprite.sprite(String.format("character/char%d_normal1.png", CurrentUserInformation.opponentchar));
 //		opponentCharacter_normal.setPosition(581 * Manager.ratio_width, 1019 * Manager.ratio_height);
 		for(int i = 5; i >= 2; i-- )
 		{
 			CCSprite frame = CCSprite.sprite(String.format("character/char%d_normal%d.png", CurrentUserInformation.opponentchar, i));
 			frame.setScaleX(Manager.ratio_width);
 			frame.setScaleY(Manager.ratio_height);
 			frame.setPosition(508 * Manager.ratio_width, 1019 * Manager.ratio_height);
 			normalAnimation_opponent.addFrame(frame.getTexture());
 		}
 		normalAnimate_opponent = CCAnimate.action(normalAnimation_opponent, true);
 	}
 
 //	public void makeAttackAnimation()
 //	{
 //		attackAnimation_mine = CCAnimation.animation("animation3");
 //		myCharacter_normal = CCSprite.sprite(String.format("character/char%d_attack0.png", CurrentUserInformation.userID));
 //		for(int i = 1; i <= 5; i++ )
 //		{
 //			CCSprite frame = CCSprite.sprite(String.format("character/char%d_attack%d", CurrentUserInformation.userID, i));
 //			frame.setScaleX(-1 * Manager.ratio_width);
 //			frame.setScaleY(Manager.ratio_height);
 //			attackAnimation_mine.addFrame(frame.getTexture());
 //		}
 //		attackAnimate_mine = CCAnimate.action(attackAnimation_mine, true);
 //
 //		attackAnimation_opponent = CCAnimation.animation("animation4");
 //		myCharacter_normal = CCSprite.sprite(String.format("character/char%d_attack0.png", CurrentUserInformation.opponentID));
 //		for(int i = 1; i <= 5; i++ )
 //		{
 //			CCSprite frame = CCSprite.sprite(String.format("character/char%d_attack%d", CurrentUserInformation.opponentID, i));
 //			frame.setScaleX(Manager.ratio_width);
 //			frame.setScaleY(Manager.ratio_height);
 //			attackAnimation_opponent.addFrame(frame.getTexture());
 //		}
 //		attackAnimate_opponent = CCAnimate.action(attackAnimation_opponent, true);
 //
 //	}
 
 	
 	public void makeDemagedAnimation()
 	{
 		damagedAnimation_mine = CCAnimation.animation("animation5", 0.15f);
 		myCharacter_hurted = CCSprite.sprite(String.format("character/char%d_hurted1.png", CurrentUserInformation.userChar));
 		myCharacter_hurted.setPosition(139 * Manager.ratio_width, 1019 * Manager.ratio_height);
 		for(int i = 1; i <= 5; i++ )
 		{
 			CCSprite frame = CCSprite.sprite(String.format("character/char%d_hurted%d.png", CurrentUserInformation.userChar, i));
 			frame.setScaleX(-1 * Manager.ratio_width);
 			frame.setScaleY(Manager.ratio_height);
 			damagedAnimation_mine.addFrame(frame.getTexture());
 		}
 		damagedAnimate_mine = CCAnimate.action(damagedAnimation_mine, false);
 
 		damagedAnimation_opponent = CCAnimation.animation("animation6", 0.15f);
 		opponentCharacter_hurted = CCSprite.sprite(String.format("character/char%d_hurted1.png", CurrentUserInformation.opponentchar));
 		opponentCharacter_hurted.setPosition(581 * Manager.ratio_width, 1019 * Manager.ratio_height);
 		for(int i = 1; i <= 5; i++ )
 		{
 			CCSprite frame = CCSprite.sprite(String.format("character/char%d_hurted%d.png", CurrentUserInformation.opponentchar, i));
 			frame.setScaleX(Manager.ratio_width);
 			frame.setScaleY(Manager.ratio_height);
 			damagedAnimation_opponent.addFrame(frame.getTexture());
 		}
 		damagedAnimate_opponent = CCAnimate.action(damagedAnimation_opponent, false);
 	}
 	public CCFiniteTimeAction makeDamagedAction()
 	{
 		return CCMoveBy.action(1, CGPoint.make(-200, 0));
 	}
 	public CCFiniteTimeAction makeAttackAction()
 	{
 		return CCMoveBy.action(1, CGPoint.make(200, 0));
 	}
 	public CCSequence makeMyAttackSequence(CCFiniteTimeAction attackAction)
 	{
 //		CCCallFuncN afterDemaged_mine = CCCallFuncN.action(this, "runAfterDamagedAnimation_mine");
 //		damagedSequence_bark = CCSequence.actions(damagedAction, afterDemaged_mine);
 		//		damagedSequence_bone;
 		//		damagedSequence_punch;
 
 		CCCallFuncN afterDamaged_opponent = CCCallFuncN.action(this, "runAfterDamagedAnimation_opponent");
 		CCSequence attackSequence = CCSequence.actions(attackAction, afterDamaged_opponent);
 		//		attackSequence_bone;
 		//		attackSequence_punch;
 		
 		return attackSequence;
 //		returnToNormalMode = CCCallFuncN.action(this, "returnToNormalMode");
 //		damagedAndReturnToNormal_mine = CCSequence.actions(damagedAnimate_mine, returnToNormalMode);
 //		damagedAndReturnToNormal_opponent = CCSequence.actions(damagedAnimate_opponent, returnToNormalMode);
 	}
 	public CCSequence makeMyDamagedSequence(CCFiniteTimeAction damagedAction)
 	{
//		CCCallFuncN afterDemaged_mine = CCCallFuncN.action(this, "runAfterDamagedAnimation_mine");
//		damagedSequence_bark = CCSequence.actions(damagedAction, afterDemaged_mine);
 		//		damagedSequence_bone;
 		//		damagedSequence_punch;
 
 		CCCallFuncN afterDamaged_opponent = CCCallFuncN.action(this, "runAfterDamagedAnimation_opponent");
		CCSequence DamagedSequence = CCSequence.actions(damagedAction, afterDamaged_opponent);
 		//		attackSequence_bone;
 		//		attackSequence_punch;
 		
 		return DamagedSequence;
 //		CCCallFuncN returnToNormalMode = CCCallFuncN.action(this, "returnToNormalMode");
 //		damagedAndReturnToNormal_mine = CCSequence.actions(damagedAnimate_mine, returnToNormalMode);
 //		damagedAndReturnToNormal_opponent = CCSequence.actions(damagedAnimate_opponent, returnToNormalMode);
 	}
 	public void runAfterDamagedAnimation_opponent(Object o)
 	{
 		opponentCharacter_normal.setVisible(false);
 		this.addChild(opponentCharacter_hurted, 1, ACTION_DAMAGED);
 		CCCallFuncN returnToNormalMode = CCCallFuncN.action(this, "returnToNormalMode");
 		CCSequence damagedAndReturnToNormal_opponent = CCSequence.actions(damagedAnimate_opponent, returnToNormalMode);
 		
 		opponentCharacter_hurted.runAction(damagedAndReturnToNormal_opponent);
 		/////////////////////////////
 	}
 	public void runAfterDamagedAnimation_mine(Object o)
 	{
 		CCCallFuncN returnToNormalMode = CCCallFuncN.action(this, "returnToNormalMode");
 		CCSequence damagedAndReturnToNormal_mine = CCSequence.actions(damagedAnimate_mine, returnToNormalMode);
 		
 		myCharacter_normal.setVisible(false);
 		this.addChild(myCharacter_hurted, 1, ACTION_DAMAGED);
 		myCharacter_hurted.runAction(damagedAndReturnToNormal_mine);
 		
 	}
 	public void returnToNormalMode(Object o)
 	{
 		int i;
 		for(i = 0; i < numOfCurrentActions; i++)
 		{
 			this.removeChildByTag(ACTION_FLYING, true);
 			this.removeChildByTag(ACTION_DAMAGED, true);
 			this.removeChildByTag(ACTION_ATTACK, true);
 		}
 		for(int j = 0; j < i; j++)
 			numOfCurrentActions--;
 		
 //		myCharacter_normal.resumeSchedulerAndActions();
 //		myCharacter_normal.resumeSchedulerAndActions();
 		myCharacter_normal.setVisible(true);
 //		myCharacter_normal.runAction(CCRepeatForever.action(normalAnimate_mine));
 		opponentCharacter_normal.setVisible(true);
 //		opponentCharacter_normal.runAction(CCRepeatForever.action(normalAnimate_opponent));
 		
 		
 	}
 //	public void runAttackAnimation_mine(int kindOfAttack)
 //	{
 //		myCharacter_normal.stopAction(normalAnimate_mine);
 //		myCharacter_normal.setVisible(false);
 //		switch(kindOfAttack)
 //		{
 //		case 1 : 
 //			myCharacter_normal.setVisible(false);
 ////			opponentCharacter_normal.setVisible(false);
 //			attack_bark_mine.setVisible(true);
 //
 //			going_bark.setPosition(225 * Manager.ratio_width,1030 * Manager.ratio_height);
 //			this.addChild(going_bark, ACTION_FLYING);
 //			going_bark.runAction(attackSequence_bark);
 //			break;
 //		case 2 : 
 //			myCharacter_normal.setVisible(false);
 ////			opponentCharacter_normal.setVisible(false);
 //			attack_bone_mine.setVisible(true);
 //
 //			going_bone.setPosition(225 * Manager.ratio_width,1030 * Manager.ratio_height);
 //			this.addChild(going_bone, ACTION_FLYING);
 //			going_bone.runAction(attackSequence_bark);
 //			break;
 //		case 3 : 
 //			myCharacter_normal.setVisible(false);
 ////			opponentCharacter_normal.setVisible(false);
 //			attack_punch_mine.setVisible(true);
 //
 //			going_punch.setPosition(225 * Manager.ratio_width,1030 * Manager.ratio_height);
 //			this.addChild(going_punch, ACTION_FLYING);
 //			going_punch.runAction(attackSequence_bark);
 //			break;
 //		}
 //	}
 
 
 
 	public BattleLayer()
 	{
 		this.setIsTouchEnabled(true);
 		
 		gage = 0;
 		gage_bar.setPosition(86 * Manager.ratio_width, 570 * Manager.ratio_height);
 		gage_bar.setScaleX(Manager.ratio_width);
 		gage_bar.setScaleY(Manager.ratio_height);
 		this.addChild(gage_bar);
 		
 		gage_bar_black.setPosition(85 * Manager.ratio_width, 765 * Manager.ratio_height);
 		gage_bar_black.setAnchorPoint(0.5f, 1f);
 		gage_bar_black.setScaleX(Manager.ratio_width);
 		gage_bar_black.setScaleY(1 - gage / Manager.full_gage);
 		gage_bar_black.setScaleY(Manager.ratio_height);
 		this.addChild(gage_bar_black);
 		
 		// ų ư ġ , ̾ ߰
 		btn_skill_bark_activated.setPosition(631 * Manager.ratio_width, 733 * Manager.ratio_height);
 		btn_skill_bark_activated.setScaleX(Manager.ratio_width);	//* Ȯ ʿ
 		btn_skill_bark_activated.setScaleY(Manager.ratio_height);
 		this.addChild(btn_skill_bark_activated);
 		btn_skill_bark_normal.setPosition(631 * Manager.ratio_width, 733 * Manager.ratio_height);
 		btn_skill_bark_normal.setScaleX(Manager.ratio_width);	//* Ȯ ʿ
 		btn_skill_bark_normal.setScaleY(Manager.ratio_height);
 		this.addChild(btn_skill_bark_normal);
 		
 		btn_skill_bone_activated.setPosition(631 * Manager.ratio_width, 589 * Manager.ratio_height);
 		btn_skill_bone_activated.setScaleX(Manager.ratio_width);
 		btn_skill_bone_activated.setScaleY(Manager.ratio_height);
 		this.addChild(btn_skill_bone_activated);
 		btn_skill_bone_normal.setPosition(631 * Manager.ratio_width, 589 * Manager.ratio_height);
 		btn_skill_bone_normal.setScaleX(Manager.ratio_width);
 		btn_skill_bone_normal.setScaleY(Manager.ratio_height);
 		this.addChild(btn_skill_bone_normal);
 		
 		btn_skill_punch_activated.setPosition(631 * Manager.ratio_width, 439 * Manager.ratio_height);
 		btn_skill_punch_activated.setScaleX(Manager.ratio_width);
 		btn_skill_punch_activated.setScaleY(Manager.ratio_height);
 		this.addChild(btn_skill_punch_activated);
 		btn_skill_punch_normal.setPosition(631 * Manager.ratio_width, 439 * Manager.ratio_height);
 		btn_skill_punch_normal.setScaleX(Manager.ratio_width);
 		btn_skill_punch_normal.setScaleY(Manager.ratio_height);
 		this.addChild(btn_skill_punch_normal);
 
 		////////////////////////////////////////////////////////////////////////////
 		bg_battlelayer.setPosition(360 * Manager.ratio_width, 1057 * Manager.ratio_height);
 		bg_battlelayer.setScaleX(Manager.ratio_width);
 		bg_battlelayer.setScaleY(Manager.ratio_height);
 		this.addChild(bg_battlelayer);
 
 		makeNormalAnimation();
 //		makeAttackAnimation();
 		makeDemagedAnimation();
 
 		myCharacter_normal = CCSprite.sprite(String.format("character/char%d_normal1.png", CurrentUserInformation.userChar));
 		myCharacter_normal.setScaleX(-1 * Manager.ratio_width);
 		myCharacter_normal.setScaleY(Manager.ratio_height);
 		myCharacter_normal.setPosition(212 * Manager.ratio_width, 1019 * Manager.ratio_height);
 		myCharacter_normal.runAction(CCRepeatForever.action(normalAnimate_mine));
 		this.addChild(myCharacter_normal);
 
 		opponentCharacter_normal = CCSprite.sprite(String.format("character/char%d_normal1.png", CurrentUserInformation.opponentchar));
 		opponentCharacter_normal.setScaleX(Manager.ratio_width);
 		opponentCharacter_normal.setScaleY(Manager.ratio_height);
 		opponentCharacter_normal.setPosition(508 * Manager.ratio_width, 1019 * Manager.ratio_height);
 		opponentCharacter_normal.runAction(CCRepeatForever.action(normalAnimate_opponent));
 		this.addChild(opponentCharacter_normal);
 
 		myCharacter_hurted = CCSprite.sprite(String.format("character/char%d_hurted1.png", CurrentUserInformation.userChar));
 		myCharacter_hurted.setScaleX(-1 * Manager.ratio_width);
 		myCharacter_hurted.setScaleY(Manager.ratio_height);
 		myCharacter_hurted.setPosition(212 * Manager.ratio_width, 1019 * Manager.ratio_height);
 
 		opponentCharacter_hurted = CCSprite.sprite(String.format("character/char%d_hurted1.png", CurrentUserInformation.opponentchar));
 		opponentCharacter_hurted.setScaleX(Manager.ratio_width);
 		opponentCharacter_hurted.setScaleY(Manager.ratio_height);
 		opponentCharacter_hurted.setPosition(508 * Manager.ratio_width, 1019 * Manager.ratio_height);
 		
 
 		bg_hp_bar.setPosition(360 * Manager.ratio_width, 1240 * Manager.ratio_height);
 		bg_hp_bar.setScaleX(-1 * Manager.ratio_width);
 		bg_hp_bar.setScaleY(Manager.ratio_height);
 		this.addChild(bg_hp_bar);
 
 		hp_bar_mine.setPosition(174 * Manager.ratio_width, 1249 * Manager.ratio_height);
 		hp_bar_mine.setScaleX(Manager.ratio_width);
 		hp_bar_mine.setScaleY(Manager.ratio_height);
 		this.addChild(hp_bar_mine);
 
 		hp_bar_opponent.setPosition(546 * Manager.ratio_width, 1249 * Manager.ratio_height);
 		hp_bar_opponent.setScaleX(Manager.ratio_width);
 		hp_bar_opponent.setScaleY(Manager.ratio_height);
 		this.addChild(hp_bar_opponent);
 
 		title.setPosition(360 * Manager.ratio_width, 1238 * Manager.ratio_height);
 		title.setScaleX(Manager.ratio_width);
 		title.setScaleY(Manager.ratio_height);
 		this.addChild(title);
 
 		attack_bark_opponent = CCSprite.sprite(String.format("character/attack_bark%d.png", CurrentUserInformation.opponentchar));
 		attack_bark_opponent.setScaleX(Manager.ratio_width);
 		attack_bark_opponent.setScaleY(Manager.ratio_height);
 		attack_bark_opponent.setVisible(false);
 		attack_bark_opponent.setPosition(508 * Manager.ratio_width, 1019 * Manager.ratio_height);
 		this.addChild(attack_bark_opponent);
 		attack_bark_mine = CCSprite.sprite(String.format("character/attack_bark%d.png", CurrentUserInformation.userChar));
 		attack_bark_mine.setPosition(212 * Manager.ratio_width, 1019 * Manager.ratio_height);
 		attack_bark_mine.setScaleX(-1 * Manager.ratio_width);
 		attack_bark_mine.setScaleY(Manager.ratio_height);
 //		attack_bark_mine.setVisible(false);
 //		this.addChild(attack_bark_mine);
 
 		attack_bone_opponent = CCSprite.sprite(String.format("character/attack_bone%d.png", CurrentUserInformation.opponentchar));
 		attack_bone_opponent.setScaleX(Manager.ratio_width);
 		attack_bone_opponent.setScaleY(Manager.ratio_height);
 //		attack_bone_opponent.setVisible(false);
 		attack_bone_opponent.setPosition(508 * Manager.ratio_width, 1019 * Manager.ratio_height);
 //		this.addChild(attack_bone_opponent);
 		attack_bone_mine = CCSprite.sprite(String.format("character/attack_bone%d.png", CurrentUserInformation.userChar));
 		attack_bone_mine.setScaleX(-1 * Manager.ratio_width);
 		attack_bone_mine.setScaleY(Manager.ratio_height);
 		attack_bone_mine.setPosition(212 * Manager.ratio_width, 1019 * Manager.ratio_height);
 //		attack_bone_mine.setVisible(false);
 //		this.addChild(attack_bone_mine);
 
 		attack_punch_opponent = CCSprite.sprite(String.format("character/attack_punch%d.png", CurrentUserInformation.opponentchar));
 		attack_punch_opponent.setScaleX(Manager.ratio_width);
 		attack_punch_opponent.setScaleY(Manager.ratio_height);
 //		attack_punch_opponent.setVisible(false);
 		attack_punch_opponent.setPosition(508 * Manager.ratio_width, 1019 * Manager.ratio_height);
 //		this.addChild(attack_punch_opponent);
 		attack_punch_mine = CCSprite.sprite(String.format("character/attack_punch%d.png", CurrentUserInformation.userChar));
 		attack_punch_mine.setScaleX(-1 * Manager.ratio_width);
 		attack_punch_mine.setScaleY(Manager.ratio_height);
 //		attack_punch_mine.setVisible(false);
 		attack_punch_mine.setPosition(212 * Manager.ratio_width, 1019 * Manager.ratio_height);
 //		this.addChild(attack_punch_mine);
 
 		coming_bark = CCSprite.sprite("battle/coming_bark.png");
 		coming_bark.setScaleX(Manager.ratio_width);
 		coming_bark.setScaleY(Manager.ratio_height);
 
 		going_bark = CCSprite.sprite("battle/coming_bark.png");
 		going_bark.setScaleX(-1 * Manager.ratio_width);
 		going_bark.setScaleY(Manager.ratio_height);
 
 		coming_bone = CCSprite.sprite("battle/coming_bone.png");
 		coming_bone.setScaleX(Manager.ratio_width);
 		coming_bone.setScaleY(Manager.ratio_height);
 
 		going_bone = CCSprite.sprite("battle/coming_bone.png");
 		going_bone.setScaleX(-1 * Manager.ratio_width);
 		going_bone.setScaleY(Manager.ratio_height);
 
 		coming_punch = CCSprite.sprite("battle/coming_punch.png");
 		coming_punch.setScaleX(Manager.ratio_width);
 		coming_punch.setScaleY(Manager.ratio_height);
 
 		going_punch = CCSprite.sprite("battle/coming_punch.png");
 		going_punch.setScaleX(-1 * Manager.ratio_width);
 		going_punch.setScaleY(-1 * Manager.ratio_height);
 
 		NetworkManager.getInstance().setGetDamagedCallback(this);
 //		SkillGageLayer.getInstance().setClickAttackBtnCallback(this);
 //		myCharacter_normal.runAction(normalAnimate_mine);
 //		opponentCharacter_normal.runAction(normalAnimate_opponent);
 	}
 
 
 	@Override
 	///* Լ   ޾ , Activity ȣϴ Լ
 	public void didGetDamaged(SkillType kindOfAttack) {
 		switch(kindOfAttack.toInteger())
 		{
 		case 1 : 
 			hp -= Manager.damaged_gage_per_attack_bark;
 			updateGageBar();
 			//*  ȿ ִϸ̼
 			runAttackAnimation_opponent(1);
 //			BattlerDogActivity.makeToast(1);
 			break;
 		case 2 :
 			hp -= Manager.damaged_gage_per_attack_bone;
 			updateGageBar();
 			runAttackAnimation_opponent(2);
 			//*  ȿ ִϸ̼
 //			BattlerDogActivity.makeToast(2);
 			break;
 		case 3 :
 			hp -= Manager.damaged_gage_per_attack_punch;
 			updateGageBar();
 			//*  ȿ ִϸ̼
 			runAttackAnimation_opponent(3);
 //			BattlerDogActivity.makeToast(3);
 			break;
 		}
 	}
 
 	public void runAttackAnimation_mine(int kindOfAttack) {
 		numOfCurrentActions++;
 		
 //		CCFiniteTimeAction damagedAction = makeDamagedAction();
 		CCFiniteTimeAction attackAction = makeAttackAction();
 		
 		
 		CCSequence my_attack_sequence = makeMyAttackSequence(attackAction);
 		
 		myCharacter_normal.stopAction(normalAnimate_mine);
 		myCharacter_normal.setVisible(false);
 		switch(kindOfAttack)
 		{
 		case 1 : 
 			myCharacter_normal.setVisible(false);
 //			opponentCharacter_normal.setVisible(false);
 			this.addChild(attack_bark_mine, 1, ACTION_ATTACK);
 			going_bark.setPosition(225 * Manager.ratio_width,1030 * Manager.ratio_height);
 			this.addChild(going_bark, 500, ACTION_FLYING);
 			going_bark.runAction(my_attack_sequence);
 			break;
 		case 2 : 
 			myCharacter_normal.setVisible(false);
 //			opponentCharacter_normal.setVisible(false);
 			this.addChild(attack_bone_mine, 1, ACTION_ATTACK);
 
 			going_bone.setPosition(225 * Manager.ratio_width,1030 * Manager.ratio_height);
 			this.addChild(going_bone, 500, ACTION_FLYING);
 			going_bone.runAction(my_attack_sequence);
 			break;
 		case 3 : 
 			myCharacter_normal.setVisible(false);
 //			opponentCharacter_normal.setVisible(false);
 
 			this.addChild(attack_punch_mine, 1, ACTION_ATTACK);
 			
 			going_punch.setPosition(225 * Manager.ratio_width,1030 * Manager.ratio_height);
 			this.addChild(going_punch, 500, ACTION_FLYING);
 			going_punch.runAction(my_attack_sequence);
 			break;
 		}
 	}
 	
 	public void runAttackAnimation_opponent(int kindOfAttack)
 	{
 		numOfCurrentActions++;
 		
 		CCFiniteTimeAction damagedAction = makeDamagedAction();
 //		CCFiniteTimeAction attackAction = makeAttackAction();
 		
 		CCSequence opponent_attack_sequence = makeMyDamagedSequence(damagedAction);
 		
 		opponentCharacter_normal.stopAction(normalAnimate_mine);
 		opponentCharacter_normal.setVisible(false);
 		switch(kindOfAttack)
 		{
 		case 1 : 
 			opponentCharacter_normal.setVisible(false);
 //			opponentCharacter_normal.setVisible(false);
 
 			this.addChild(attack_bark_opponent, 1, ACTION_ATTACK);
 			
 			coming_bark.setPosition(225 * Manager.ratio_width,1030 * Manager.ratio_height);
 			this.addChild(coming_bark, 1, ACTION_FLYING);
 			coming_bark.runAction(opponent_attack_sequence);
 			break;
 		case 2 : 
 			opponentCharacter_normal.setVisible(false);
 //			opponentCharacter_normal.setVisible(false);
 			this.addChild(attack_bone_opponent, 1, ACTION_ATTACK);
 			
 			going_bone.setPosition(225 * Manager.ratio_width,1030 * Manager.ratio_height);
 			this.addChild(going_bone, ACTION_FLYING);
 			going_bone.runAction(opponent_attack_sequence);
 			break;
 		case 3 : 
 			opponentCharacter_normal.setVisible(false);
 //			opponentCharacter_normal.setVisible(false);
 			this.addChild(attack_punch_opponent, 1, ACTION_ATTACK);
 			
 			going_punch.setPosition(225 * Manager.ratio_width,1030 * Manager.ratio_height);
 			this.addChild(going_punch, ACTION_FLYING);
 			going_punch.runAction(opponent_attack_sequence);
 			break;
 		}
 	}
 	/*
 	 * ų ư Ȱ, Ȱ  Ʈ
 	 */
 	public static void updateSkillBtns()
 	{
 		// ų ư Ȱȭ ==> normal image visibility  false
 		if(gage >= Manager.required_gage_for_skill_punch)
 		{
 			btn_skill_bark_normal.setVisible(false);
 			btn_skill_bone_normal.setVisible(false);
 			btn_skill_punch_normal.setVisible(false);
 		}
 		else if(gage >= Manager.required_gage_for_skill_bone)
 		{
 			btn_skill_bark_normal.setVisible(false);
 			btn_skill_bone_normal.setVisible(false);
 			btn_skill_punch_normal.setVisible(true);
 		}
 		else if(gage >= Manager.required_gage_for_skill_bark)
 		{
 			btn_skill_bark_normal.setVisible(false);
 			btn_skill_bone_normal.setVisible(true);
 			btn_skill_punch_normal.setVisible(true);
 		}
 		else
 		{
 			btn_skill_bark_normal.setVisible(true);
 			btn_skill_bone_normal.setVisible(true);
 			btn_skill_punch_normal.setVisible(true);
 		}
 	}
 	/*
 	 *      Ʈ
 	 */
 	public static void updateGageBar()
 	{
 		gage_bar_black.setScaleY((1 - (gage / Manager.full_gage)) * Manager.ratio_height);
 	}
 	
 	@Override
 	public boolean ccTouchesBegan(MotionEvent event) {
 		CGPoint touchPoint = CCDirector.sharedDirector().convertToGL(CGPoint.ccp(event.getX(),event.getY()));
 		if(btn_skill_bark_normal.getBoundingBox().contains(touchPoint.x, touchPoint.y) && gage >= Manager.required_gage_for_skill_bark)
 		{
 			gage -= Manager.required_gage_for_skill_bark;
 			updateSkillBtns();
 			updateGageBar();
 			runAttackAnimation_mine(1);
 			NetworkManager.getInstance().sendAttack(SkillType.BARK);
 		}
 		else if(btn_skill_bone_normal.getBoundingBox().contains(touchPoint.x, touchPoint.y) && gage >= Manager.required_gage_for_skill_bone)
 		{
 			gage -= Manager.required_gage_for_skill_bone;
 			updateSkillBtns();
 			updateGageBar();
 			runAttackAnimation_mine(2);
 			NetworkManager.getInstance().sendAttack(SkillType.BONE);
 			///*    
 		}
 		else if(btn_skill_punch_normal.getBoundingBox().contains(touchPoint.x, touchPoint.y) && gage >= Manager.required_gage_for_skill_punch)
 		{
 			gage -= Manager.required_gage_for_skill_punch;
 			updateSkillBtns();
 			updateGageBar();
 			runAttackAnimation_mine(3);
 			NetworkManager.getInstance().sendAttack(SkillType.PUNCH);
 			///*    
 		}
 		return super.ccTouchesBegan(event);
 	}
 
 }
