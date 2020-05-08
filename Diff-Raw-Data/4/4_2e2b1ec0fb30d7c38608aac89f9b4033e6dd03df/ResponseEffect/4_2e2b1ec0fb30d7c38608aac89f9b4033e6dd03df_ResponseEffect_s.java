 package org.andfRa.mythr.responses;
 
 import java.util.Random;
 
 import org.andfRa.mythr.player.DerivedStats;
 import org.andfRa.mythr.player.MythrPlayer;
 import org.bukkit.entity.LivingEntity;
 
 public abstract class ResponseEffect {
 
 	/** Cast ticks key. */
 	final public static String CAST_TICKS_KEY = "CAST_TICKS";
 
 	/** Attribute used for checks key. */
 	final public static String ATTRIBUTE_KEY = "ATTRIBUTE";
 
 	/** Attack score modifier key. */
 	final public static String ATTACK_SCORE_MODIFIER_KEY = "MODIFIER";
 
 	
 	/**
 	 * Response key.
 	 * 
 	 * @return response key
 	 */
 	public abstract String key();
 	
 
 	/**
 	 * Triggers the passive modifications.
 	 * 
 	 * @param response response
 	 * @param mplayer Mythr player
 	 * @param dsstats derived stats
 	 * @return true if successful
 	 */
 	public boolean passiveTrigger(Response response, DerivedStats dsstats)
 	 { return false; }
 	
 	/**
 	 * Triggers the response effect.
 	 * 
 	 * @param response response
 	 * @param mplayer Mythr player
 	 * @param dsstats derived stats
 	 * @return true if successful
 	 */
 	public boolean castTrigger(Response response, MythrPlayer mplayer, DerivedStats dsstats)
 	 { return false; }
 	
 	/**
 	 * Called on interact.
 	 * 
 	 * @param response response
 	 * @param mplayer Mythr player
 	 * @param dsstats derived stats
 	 * @return true if successful
 	 */
 	public boolean interactTrigger(Response response, MythrPlayer mplayer, DerivedStats dsstats)
 	 { return false; }
 
 	/**
 	 * Called on attack.
 	 * 
 	 * @param response response
 	 * @param mattacker living attacker
 	 * @param mdefender living defender
 	 * @param dsattacker attackers derived stats
 	 * @param dsdefender defenders derived stats
 	 * @return true if successful
 	 */
 	public boolean attackTrigger(Response response, LivingEntity lattacker, LivingEntity ldefender, DerivedStats dsattacker, DerivedStats dsdefender)
 	 { return false; }
 
 	
 	// UTIL:
 	/**
 	 * Finds if the attack succeeded, based on attacker and defender attribute scores.
 	 * 
 	 * @param response response
 	 * @param dsattacker attacker derived stats
 	 * @param dsdefender defender derived stats
 	 * @param mod attack score modifier
 	 * @return true if succeeded
 	 */
 	public static boolean findAttribScoreSuccess(Response response, DerivedStats dsattacker, DerivedStats dsdefender, int mod)
 	 {
 		String attribName = response.getString(ATTRIBUTE_KEY);
 		
 		int attckScore = dsattacker.getAttribScore(attribName) + mod;
 		int defndScore = dsdefender.getAttribScore(attribName);
 		
 		double check = 0.5;
		if(attckScore + defndScore == 0) check = -1.0;
		if(attckScore != 0 || defndScore != 0) check = attckScore / (attckScore + defndScore);
 		
 		return check >= new Random().nextDouble();
 	 }
 
 	/**
 	 * Finds if the attack succeeded, based on attacker and defender attribute scores.
 	 * Uses the default attack modifier.
 	 * 
 	 * @param response response
 	 * @param dsattacker attacker derived stats
 	 * @param dsdefender defender derived stats
 	 * @return true if succeeded
 	 */
 	public static boolean findAttribScoreSuccess(Response response, DerivedStats dsattacker, DerivedStats dsdefender)
 	 {
 		int mod = response.getInt(ATTACK_SCORE_MODIFIER_KEY);
 		return findAttribScoreSuccess(response, dsattacker, dsdefender, mod);
 	 }
 	
 }
