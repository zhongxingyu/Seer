 package edu.ycp.cs320.fokemon_webApp.shared.Battle;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import edu.ycp.cs320.fokemon_webApp.client.TempBattle;
 import edu.ycp.cs320.fokemon_webApp.shared.MoveClasses.EffectDataBase;
 import edu.ycp.cs320.fokemon_webApp.shared.MoveClasses.EffectType;
 import edu.ycp.cs320.fokemon_webApp.shared.MoveClasses.Move;
 import edu.ycp.cs320.fokemon_webApp.shared.MoveClasses.MoveDataBase;
 import edu.ycp.cs320.fokemon_webApp.shared.MoveClasses.MoveName;
 import edu.ycp.cs320.fokemon_webApp.shared.Player.Player;
 import edu.ycp.cs320.fokemon_webApp.shared.Player.PlayerType;
 import edu.ycp.cs320.fokemon_webApp.shared.PokemonClasses.PokeID;
 import edu.ycp.cs320.fokemon_webApp.shared.PokemonClasses.PokeType;
 import edu.ycp.cs320.fokemon_webApp.shared.PokemonClasses.Pokemon;
 import edu.ycp.cs320.fokemon_webApp.shared.PokemonClasses.Status;
 import edu.ycp.cs320.fokemon_webApp.shared.PokemonClasses.TempBattleStats;
 
 public class Battle {
 	private Player user;
 	private Player opponent;
 	private Weather weather;
 	private Random rand;
 	private ArrayList<String> battleMessage;
 	private Move confused;
 	private Boolean battleOver;
 	
 	
 	
 	public Battle(Player user, Player opponent){
 		battleOver=false;
 		confused=MoveDataBase.generateMove(MoveName.Confused);
 		this.setUser(user);
 		this.setOpponent(opponent);
 		setInitialCurrentPokemon(user,0);
 		setInitialCurrentPokemon(opponent,0);
 		user.getTeam(user.getCurrentPokemonIndex()).setTempBattleStats(new TempBattleStats());
 		opponent.getTeam(user.getCurrentPokemonIndex()).setTempBattleStats(new TempBattleStats());
 		user.getTeam(user.getCurrentPokemonIndex()).getInfo().setUsedInBattle(true);
 		opponent.getTeam(user.getCurrentPokemonIndex()).getInfo().setUsedInBattle(true);
 		battleMessage=new ArrayList<String>();
 	}
 	public static Battle wildPokemonBattle(PokeID id, int lvl){
 		Pokemon wildPoke=Pokemon.GeneratePokemon(id,lvl);
 		wildPoke.getInfo().setIsWild(true);
 		Player wildPlayer=new Player(wildPoke.getInfo().getPlayerID(), wildPoke.getInfo().getNickname(), wildPoke.getInfo().getGender(), TempBattle.getUser().getPlayerLocation());
 		wildPlayer.getTeam().add(wildPoke);
 		return new Battle(TempBattle.getUser(),wildPlayer);
 	}
 	public static Battle wildPokemonBattle(){
 		Random rand=new Random();
 		int lvl=rand.nextInt(99)+1;
 		return wildPokemonBattle(PokeID.randomPokeID(),lvl);
 				
 	}
 	public void Turn(){
 		Pokemon userPoke=user.getTeam(user.getCurrentPokemonIndex());
 		Pokemon oppPoke=opponent.getTeam(opponent.getCurrentPokemonIndex());
 		boolean canAttack=false;
 		switch(user.getChoice()){
 		 case MOVE:
 			 if (opponent.getChoice()==TurnChoice.MOVE){
 				 double userSpeed=userPoke.getStats().getSpd()*
 						 getStatMod(userPoke.getTempBattleStats().getSPDBoost() + userPoke.getMove(user.getMoveIndex()).getMovePriority());
 				 //*SpeedAbilityMod*userPoke.getItem.getSpeedMod();
 				 if(userPoke.getStats().getStatus()==Status.PRL)userSpeed*=.25;
 				 
 				 double oppSpeed=oppPoke.getStats().getSpd()*
 						 getStatMod(oppPoke.getTempBattleStats().getSPDBoost() + oppPoke.getMove(opponent.getMoveIndex()).getMovePriority());
 				 //*SpeedAbilityMod*oppPoke.getItem.getSpeedMod();
 				 if(oppPoke.getStats().getStatus()==Status.PRL)oppSpeed*=.25;
 				 
 				 if(userSpeed>=oppSpeed){
 					
 					 canAttack=CheckAttackStatus(oppPoke);
 					 user.setTurnOrder(2);
 					 opponent.setTurnOrder(1);
 				 }
 					 canAttack=CheckAttackStatus(userPoke);
 					 if(canAttack==true){
 						 attack(userPoke,oppPoke,userPoke.getMove(user.getMoveIndex()));
 						 userPoke.getMove(user.getMoveIndex()).setCurPP(userPoke.getMove(user.getMoveIndex()).getCurPP()-1);
 					 }
 					 canAttack=CheckAttackStatus(oppPoke);
 					 if(canAttack==true){
 						 attack(oppPoke,userPoke,oppPoke.getMove(opponent.getMoveIndex()));
 						 oppPoke.getMove(opponent.getMoveIndex()).setCurPP(oppPoke.getMove(opponent.getMoveIndex()).getCurPP()-1);
 					 }
 				 }else{
 					 canAttack=CheckAttackStatus(oppPoke);
 					 user.setTurnOrder(2);
 					 opponent.setTurnOrder(1);
 					 if(canAttack==true){
 						 attack(oppPoke,userPoke,oppPoke.getMove(opponent.getMoveIndex()));
 						 oppPoke.getMove(opponent.getMoveIndex()).setCurPP(oppPoke.getMove(opponent.getMoveIndex()).getCurPP()-1);
 					 }
 					 canAttack=CheckAttackStatus(userPoke);
 					 if(canAttack==true){
 						 attack(userPoke,oppPoke,userPoke.getMove(user.getMoveIndex()));
 						 userPoke.getMove(user.getMoveIndex()).setCurPP(userPoke.getMove(user.getMoveIndex()).getCurPP()-1);
 					 }
 				 }
 				 
 		
 			break;
 	     default:
 	    	 	break; 
 		}
 	    applyStatusDamage(oppPoke);
 		applyStatusDamage(userPoke);
 	}
 
 	public void findTurnOrder(){
 	Pokemon userPoke=user.getTeam(user.getCurrentPokemonIndex());
 	Pokemon oppPoke=opponent.getTeam(opponent.getCurrentPokemonIndex());
 	switch(user.getChoice()){
 	 case MOVE:
 		 if (opponent.getChoice()==TurnChoice.MOVE){
 			 double userSpeed=userPoke.getStats().getSpd()*
 					 getStatMod(userPoke.getTempBattleStats().getSPDBoost() + userPoke.getMove(user.getMoveIndex()).getMovePriority());
 			 //*SpeedAbilityMod*userPoke.getItem.getSpeedMod();
 			 if(userPoke.getStats().getStatus()==Status.PRL)userSpeed*=.25;
 			 
 			 double oppSpeed=oppPoke.getStats().getSpd()*
 					 getStatMod(oppPoke.getTempBattleStats().getSPDBoost() + oppPoke.getMove(opponent.getMoveIndex()).getMovePriority());
 			 //*SpeedAbilityMod*oppPoke.getItem.getSpeedMod();
 			 if(oppPoke.getStats().getStatus()==Status.PRL)oppSpeed*=.25;
 			 
 			 if(userSpeed>=oppSpeed){
 	
 				 user.setTurnOrder(1);
 				 opponent.setTurnOrder(2);
 			 }else{
 				 user.setTurnOrder(2);
 				 opponent.setTurnOrder(1);
 			 }
 		 }else{
 			 user.setTurnOrder(2);
 			 opponent.setTurnOrder(1);
 		 }
 		 break;
 	 case ITEM:
 		 user.setTurnOrder(1);
 		 opponent.setTurnOrder(2);
 		 break;
 	 case RUN:
 		 user.setTurnOrder(1);
 		 opponent.setTurnOrder(2);
 		 break;
 	 case SWITCH:
 		 user.setTurnOrder(1);
 		 opponent.setTurnOrder(2);
 		 break;
 	 default:
 		 user.setTurnOrder(1);
 		 opponent.setTurnOrder(2);
 		 break;
 	}
 }
 	public boolean CheckTurnValidity(){
 	if(isTurnOk(user)==false)return false;
 	if(isTurnOk(opponent)==false)return false;
 	return true;
 }
 	public boolean isTurnOk(Player player){
 	Pokemon curPoke=player.getTeam(player.getCurrentPokemonIndex());
 	switch(player.getChoice()){
 	case MOVE:
 		if(curPoke.getMove(player.getMoveIndex()).getCurPP()==0) return false;
 		break;
 	case ITEM:
 		//not a use able item, not good target
 		break;
 	case SWITCH:
 		if(player.getCurrentPokemonIndex()==player.getMoveIndex()) return false;
 		if(player.getTeam(player.getMoveIndex()).getStats().getStatus()==Status.FNT)return false;
 		break;
 	case RUN:
 		//not a wild pokemon
 		break;
 	default:
 		return false;
 	}
 
 	return true;
 }
 	public void Turn(int turnNumber){
 	battleMessage=new ArrayList<String>();
 	boolean canAttack=false;
 	Player turnPlayer, otherPlayer;
 	
 	if(turnNumber==1) findTurnOrder();
 	if (turnNumber!=3){
 		if(user.getTurnOrder()==turnNumber){
 			turnPlayer=user;
 			otherPlayer=opponent;
 		}else{
 			turnPlayer=opponent;
 			otherPlayer=user;
 		}
 		
 		Pokemon userPoke=turnPlayer.getTeam(turnPlayer.getCurrentPokemonIndex());
 		Pokemon oppPoke=otherPlayer.getTeam(otherPlayer.getCurrentPokemonIndex());
 		
 		switch(turnPlayer.getChoice()){
 		case MOVE:
 			 canAttack=CheckAttackStatus(userPoke);
 			 if(canAttack==true){
 				 battleMessage.add(userPoke.getInfo().getNickname()+" used "+userPoke.getMove(turnPlayer.getMoveIndex()).getMoveName().toString()+".  ");
 				 attack(userPoke,oppPoke,userPoke.getMove(turnPlayer.getMoveIndex()));
 				 battleMessage.addAll(EffectDataBase.moveEffect(userPoke, oppPoke, userPoke.getMove(turnPlayer.getMoveIndex()).getEffect()));
 				 userPoke.getMove(turnPlayer.getMoveIndex()).setCurPP(userPoke.getMove(turnPlayer.getMoveIndex()).getCurPP()-1);
 			 }
 			break;
 		case ITEM:
 			break;
 		case SWITCH:
 			turnPlayer.setCurrentPokemonIndex(turnPlayer.getMoveIndex());
 			turnPlayer.getTeam(turnPlayer.getCurrentPokemonIndex()).setTempBattleStats(new TempBattleStats());
 			turnPlayer.getTeam(turnPlayer.getCurrentPokemonIndex()).getInfo().setUsedInBattle(true);
 			break;
 		case RUN:
 			break;
 		default:
 			break;
 		}
 	
 	
 	}else{
 		applyStatusDamage(user.getTeam(user.getCurrentPokemonIndex()));
 		applyStatusDamage(opponent.getTeam(opponent.getCurrentPokemonIndex()));
 		CheckFaintedPokemon();
 		
 	}
 	
 }
 	private void CheckFaintedPokemon() {
 	// TODO Auto-generated method stub
 	if(user.getTeam(user.getCurrentPokemonIndex()).getStats().getStatus()==Status.FNT){
 		user.getTeam(user.getCurrentPokemonIndex()).getInfo().setUsedInBattle(false);
 		battleMessage.add(user.getTeam(user.getCurrentPokemonIndex()).getInfo().getNickname()+" has lost the battle.  ");
 		battleOver=true;
 	}
 	if(opponent.getTeam(opponent.getCurrentPokemonIndex()).getStats().getStatus()==Status.FNT){
 		opponent.getTeam(opponent.getCurrentPokemonIndex()).getInfo().setUsedInBattle(false);
 		CalculateXP(user.getTeam(),opponent.getTeam(opponent.getCurrentPokemonIndex()));
 		battleMessage.add(opponent.getTeam(opponent.getCurrentPokemonIndex()).getInfo().getNickname()+" has lost the battle.  ");
 		battleOver=true;
 	}
 	
 }
 	public void CalculateXP(ArrayList<Pokemon> team, Pokemon loser){
 	//a is wild or trainer, t=traded, b=base, e=luckyegg,L=lvl of opp, Lp=lvl of poke
 	//s is 2 times the number of non fainted used pokemon, or 2 if holding xpshare
 	double a, b, L,Lp,s,t,e;
 	int xp;
 	a=1;
 	if(loser.getInfo().getIsWild()==true)a=1.5;
 	b=loser.getStats().getBaseXP();
 	L=loser.getInfo().getLvl();
 	s=0;
 	for(int i=0;i<team.size();i++){
 		if(team.get(i).getInfo().getUsedInBattle()==true)s+=2;
 	}
 	for(int i=0;i<team.size();i++){
 		Lp=team.get(i).getInfo().getLvl();
 		if (user.getPlayerID()==team.get(i).getInfo().getPlayerID()) t=1;
 		else t=1.5;
 		s=2;
 		e=1;
 		if(team.get(i).getInfo().getUsedInBattle()==true){
 			xp=(int)((a*t*b*e*L)/(5*s)*(L+2)/(Lp+2)+1);
 			team.get(i).getInfo().setXp(xp+team.get(i).getInfo().getXp());
 			battleMessage.add(team.get(i).getInfo().getNickname()+" has gained "+xp+" experience points.  ");
 			team.get(i).CheckLevelUp();
 		}
 		team.get(i).getInfo().setUsedInBattle(false);
 	}
 }
 	private void applyStatusDamage(Pokemon userPoke) {
 		int damage=0;
 		damage=userPoke.getStats().getMaxHp()/8;
 		if(damage==0)damage++;
 		if(userPoke.getStats().getStatus()==Status.BRN){
 			userPoke.getStats().setCurHp(userPoke.getStats().getCurHp()-damage);
 			battleMessage.add(userPoke.getInfo().getNickname()+" was hurt by burn. ");
 		}
 		if(userPoke.getStats().getStatus()==Status.PSN){
 			userPoke.getStats().setCurHp(userPoke.getStats().getCurHp()-damage);
 			battleMessage.add(userPoke.getInfo().getNickname()+" was hurt by poison.  ");
 		}
 		if(userPoke.getStats().getCurHp()<=0){
 			userPoke.getStats().setCurHp(0);
 			userPoke.getStats().setStatus(Status.FNT);
 		}
 		
 	}
 	private boolean CheckAttackStatus(Pokemon poke) {
 		Random rand=new Random();
 		int Chance=rand.nextInt(100);
 		//fainted
 		if(poke.getStats().getStatus()==Status.FNT)return false;
 		//Paralyzed
 		if(poke.getStats().getStatus()==Status.PRL && Chance<=25){
 			battleMessage.add(poke.getInfo().getNickname()+" is paralyzed and can't move! ");
 			return false;
 		}
 		//frozen
 		if(poke.getStats().getStatus()==Status.FRZ && Chance<=80){
 			battleMessage.add(poke.getInfo().getNickname()+" is frozen solid.  ");
 			return false;
 		}else if(poke.getStats().getStatus()==Status.FRZ){
 			battleMessage.add(poke.getInfo().getNickname()+" was unfrozen.  ");
 			poke.getStats().setStatus(Status.NRM);
 		}
 		//sleep
 		if(poke.getStats().getStatus()==Status.SLP){
 			poke.getTempBattleStats().setSLPCount(poke.getTempBattleStats().getSLPCount()+1);
 			if(poke.getTempBattleStats().getSLPCount()>poke.getStats().getSLPCount()){
 				poke.getStats().setSLPCount(0);
 				poke.getStats().setStatus(Status.NRM);
 				poke.getTempBattleStats().setSLPCount(0);
 				battleMessage.add(poke.getInfo().getNickname()+" woke up.  ");
 			}else{
 				battleMessage.add(poke.getInfo().getNickname()+" is fast asleep.  ");
 				return false;
 			}
 		}
 		//Counfused
 		if(poke.getTempBattleStats().isConfused()){
 			poke.getTempBattleStats().setConfusedCount(poke.getTempBattleStats().getConfusedCount()+1);
 			if(poke.getTempBattleStats().getConfusedCount()>poke.getTempBattleStats().getConfusedTurns()){
 				poke.getTempBattleStats().setConfused(false);
 				poke.getTempBattleStats().setConfusedCount(0);
 				poke.getTempBattleStats().setConfusedTurns(0);
 				battleMessage.add(poke.getInfo().getNickname()+" was cured of confusion.  ");
 			}
 			if(Chance<=50){
 				battleMessage.add(poke.getInfo().getNickname()+" hurt itself in confused.  ");
 				attack(poke, poke, confused);
 			}
 		}
 		
 		return true;
 	}
 	public int CalcDamage(Pokemon attacker, Pokemon defender, Move move){
 		Random rand=new Random();
 		int atk=0, def=0, R=100, CH=1, basePower=0;
 		double mod1=1, mod2=1, mod3=1, stab=1, type=1;
 		int Num=rand.nextInt(100);
 		if(attacker.getInfo().getType().contains(move.getPokeType()))stab=1.5;
 		if(Num>=100-Math.pow(6.25, attacker.getTempBattleStats().getCRITBoost())) CH=2;
 		if(CH==2)battleMessage.add("It's a critical hit ");
 		R-=rand.nextInt(15);
 		type=CalcSuperEffective(defender,move);
 		basePower=move.getDamage();//times itemmultiplier,user and foe ability
 		if(attacker.getStats().getStatus()==Status.BRN)mod1*=.5;
 		if((move.getPokeType()==PokeType.FIRE && weather==Weather.SUNNY)|| (move.getPokeType()==PokeType.WATER && weather==Weather.RAINY))mod1*=1.5;
 		if((move.getPokeType()==PokeType.WATER && weather==Weather.SUNNY)|| (move.getPokeType()==PokeType.FIRE && weather==Weather.RAINY))mod1*=.5;
 		//mod2=1.3 if holding life orb
 		//mod3 is special abilities we might not be using
 		if (move.getPhysical()==true){
 			atk=(int) (attacker.getStats().getAtk()*getStatMod(attacker.getTempBattleStats().getATKBoost())); //abilityMod,ItemMod
 			def=(int) (defender.getStats().getDef()*getStatMod(defender.getTempBattleStats().getDEFBoost())); //abilityMod,ItemMod
 		}else{
 			atk=(int) (attacker.getStats().getSpAtk()*getStatMod(attacker.getTempBattleStats().getSPATKBoost())); //abilityMod,ItemMod
 			def=(int) (defender.getStats().getSpDef()*getStatMod(defender.getTempBattleStats().getSPDEFBoost())); //abilityMod,ItemMod
 		}
 		
 		int damage=(int) ((attacker.getInfo().getLvl()*2/5 +2)*basePower*atk/50/def*mod1+2);
 		damage=(int) (damage*CH*mod2*R/100*stab*type*mod3);
 		return damage;
 	}
 	public void attack(Pokemon attacker, Pokemon defender, Move move){
 		double accuracy, evasion;
 		int damage=0;
 		Random rand=new Random();
 		
 		if(attacker.getTempBattleStats().getACCBoost()>=0){
 			if(attacker.getTempBattleStats().getACCBoost()>6)attacker.getTempBattleStats().setACCBoost(6);
 			accuracy=(attacker.getTempBattleStats().getACCBoost()+3)/3;
 		}else{
 			if(attacker.getTempBattleStats().getACCBoost()<-6)attacker.getTempBattleStats().setACCBoost(-6);
 			accuracy=3/(-1*attacker.getTempBattleStats().getACCBoost()+3);
 		}
 		if(defender.getTempBattleStats().getEVABoost()>=0){
 			if(defender.getTempBattleStats().getEVABoost()>6)defender.getTempBattleStats().setEVABoost(6);
 			evasion=(defender.getTempBattleStats().getEVABoost()+3)/3;
 		}
 		else{
 			if(defender.getTempBattleStats().getEVABoost()<-6)defender.getTempBattleStats().setEVABoost(-6);
 			evasion=3/(-1*defender.getTempBattleStats().getEVABoost()+3);
 		}
 		
 		if(move.getAccuracy()*accuracy/evasion>=rand.nextInt(100) || move.getAccuracy()<0){//move hits
 			damage=CalcDamage(attacker, defender, move);
 			if(move.getDamage()!=0)defender.getStats().setCurHp(defender.getStats().getCurHp()-damage);
 			//effect
 			if(defender.getStats().getCurHp()<=0){
 				battleMessage.add(defender.getInfo().getNickname()+" has fainted. ");
 				defender.getStats().setCurHp(0);
 				defender.getStats().setStatus(Status.FNT);
 			}
 				
 		}else{
 			battleMessage.add("The attack missed.  ");
 		}
 	}
 	public double CalcSuperEffective(Pokemon defender, Move move){
 		double damage=1;
 		switch(move.getPokeType())	{
 		case BUG:
 			if(defender.getInfo().getType().contains(PokeType.GRASS))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.PSYCHIC))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.DARK))damage*=2;
 			
 			
 			if(defender.getInfo().getType().contains(PokeType.FIRE))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.FIGHTING))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.POISON))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.FLYING))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.GHOST))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.STEEL))damage*=.5;
 			break;
 		case DARK:
 			if(defender.getInfo().getType().contains(PokeType.PSYCHIC))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.GHOST))damage*=2;
 			
 			
 			if(defender.getInfo().getType().contains(PokeType.FIGHTING))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.DARK))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.STEEL))damage*=.5;
 			break;
 		case DRAGON:
 			if(defender.getInfo().getType().contains(PokeType.DRAGON))damage*=2;
 			
 			
 			if(defender.getInfo().getType().contains(PokeType.STEEL))damage*=.5;
 			break;
 		case ELECTRIC:
 			if(defender.getInfo().getType().contains(PokeType.WATER))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.FLYING))damage*=2;
 			
 			
 			if(defender.getInfo().getType().contains(PokeType.ELECTRIC))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.GRASS))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.DRAGON))damage*=.5;
 			
 			if(defender.getInfo().getType().contains(PokeType.GROUND))damage*=0;
 			break;
 		case FIGHTING:
 			if(defender.getInfo().getType().contains(PokeType.NORMAL))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.ICE))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.ROCK))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.DARK))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.STEEL))damage*=2;
 			
 			
 			if(defender.getInfo().getType().contains(PokeType.POISON))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.FLYING))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.PSYCHIC))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.BUG))damage*=.5;
 			
 			if(defender.getInfo().getType().contains(PokeType.GHOST))damage*=0;
 			break;
 		case FIRE:
 			if(defender.getInfo().getType().contains(PokeType.GRASS))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.ICE))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.BUG))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.STEEL))damage*=2;
 			
 			if(defender.getInfo().getType().contains(PokeType.FIRE))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.WATER))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.ROCK))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.DRAGON))damage*=.5;
 			break;
 		case FLYING:
 			if(defender.getInfo().getType().contains(PokeType.GRASS))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.FIGHTING))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.BUG))damage*=2;
 			
 			if(defender.getInfo().getType().contains(PokeType.ELECTRIC))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.ROCK))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.STEEL))damage*=.5;
 			break;
 		case GHOST:
 			if(defender.getInfo().getType().contains(PokeType.PSYCHIC))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.GHOST))damage*=2;
 						
 			if(defender.getInfo().getType().contains(PokeType.DARK))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.STEEL))damage*=.5;
 			
 			if(defender.getInfo().getType().contains(PokeType.NORMAL))damage*=0;
 			break;
 		case GRASS:
 			if(defender.getInfo().getType().contains(PokeType.WATER))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.GROUND))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.ROCK))damage*=2;
 			
 			
 			if(defender.getInfo().getType().contains(PokeType.FIRE))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.GRASS))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.POISON))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.FLYING))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.BUG))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.DRAGON))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.STEEL))damage*=.5;
 			break;
 		case GROUND:
 			if(defender.getInfo().getType().contains(PokeType.FIRE))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.ELECTRIC))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.POISON))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.ROCK))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.STEEL))damage*=2;
 						
 			if(defender.getInfo().getType().contains(PokeType.GRASS))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.BUG))damage*=.5;
 			
 			if(defender.getInfo().getType().contains(PokeType.FLYING))damage*=0;
 			break;
 		case ICE:
 			if(defender.getInfo().getType().contains(PokeType.GRASS))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.GROUND))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.FLYING))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.DRAGON))damage*=2;
 						
 			if(defender.getInfo().getType().contains(PokeType.FIRE))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.WATER))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.ICE))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.STEEL))damage*=.5;
 			break;
 		case NORMAL:
 			
 			if(defender.getInfo().getType().contains(PokeType.ROCK))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.STEEL))damage*=.5;
 			
 			if(defender.getInfo().getType().contains(PokeType.GHOST))damage*=0;
 			break;
 		case POISON:
 			if(defender.getInfo().getType().contains(PokeType.GRASS))damage*=2;
 						
 			if(defender.getInfo().getType().contains(PokeType.POISON))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.GROUND))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.ROCK))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.GHOST))damage*=.5;
 			
 			if(defender.getInfo().getType().contains(PokeType.STEEL))damage*=0;
 			break;
 		case PSYCHIC:
 			if(defender.getInfo().getType().contains(PokeType.FIGHTING))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.POISON))damage*=2;
 			
 			if(defender.getInfo().getType().contains(PokeType.PSYCHIC))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.STEEL))damage*=.5;
 			
 			if(defender.getInfo().getType().contains(PokeType.DARK))damage*=0;
 			break;
 		case ROCK:
 			if(defender.getInfo().getType().contains(PokeType.FIRE))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.ICE))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.FLYING))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.BUG))damage*=2;
 						
 			if(defender.getInfo().getType().contains(PokeType.FIGHTING))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.GROUND))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.STEEL))damage*=.5;
 			break;
 		case STEEL:
 			if(defender.getInfo().getType().contains(PokeType.ICE))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.ROCK))damage*=2;
 			
 			if(defender.getInfo().getType().contains(PokeType.FIRE))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.WATER))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.ELECTRIC))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.STEEL))damage*=.5;
 			break;
 		case WATER:
 			if(defender.getInfo().getType().contains(PokeType.FIRE))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.GROUND))damage*=2;
 			if(defender.getInfo().getType().contains(PokeType.ROCK))damage*=2;
 						
 			if(defender.getInfo().getType().contains(PokeType.WATER))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.GRASS))damage*=.5;
 			if(defender.getInfo().getType().contains(PokeType.DRAGON))damage*=.5;
 			break;
 		default:
 			break;
 		}
		if(damage==0)battleMessage.add("It had no effect");
		else if(damage<1)battleMessage.add("It's not very effective");
		else if(damage>1)battleMessage.add("It's super effective");
 		return damage;
 	}
 	public double getStatMod(int modLevel){
 		if (modLevel>=0){
 			if(modLevel>6)modLevel=6;
 			return (modLevel+2)/2;
 		}else
 			if(modLevel<-6)modLevel=-6;
 			return 2/(-1*modLevel+2);
 	}
 	public void setInitialCurrentPokemon(Player player, int index){
 		if(player.getTeam(index).getStats().getStatus()==Status.FNT){
 			setInitialCurrentPokemon(player, index++);
 		}else{
 			player.setCurrentPokemonIndex(index);
 		}
 	}
 	public Player getUser() {
 		return user;
 	}
 
 
 	public void setUser(Player user) {
 		this.user = user;
 	}
 
 
 	public Player getOpponent() {
 		return opponent;
 	}
 
 
 	public void setOpponent(Player opponent) {
 		this.opponent = opponent;
 	}
 	public ArrayList<String> getBattleMessage() {
 		return battleMessage;
 	}
 	public void setBattleMessage(ArrayList<String> battleMessage) {
 		this.battleMessage = battleMessage;
 	}
 	public Boolean getBattleOver() {
 		return battleOver;
 	}
 	public void setBattleOver(Boolean battleOver) {
 		this.battleOver = battleOver;
 	}
 
 }
