 package uk.org.downesward.dirtside;
 
 import uk.org.downesward.dirtside.R;
 import uk.org.downesward.dirtside.domain.CombatResolutionConfig;
 import uk.org.downesward.dirtside.domain.CombatResolutionResult;
 import uk.org.downesward.dirtside.domain.Utilities;
 import uk.org.downesward.dirtside.domain.Weapon;
 import uk.org.downesward.utiliites.Dice;
 
 import android.app.Activity;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.os.Bundle;
 
 public class CombatResolutionActivity extends Activity implements
 		CombatResolutionConfigFragment.ResolveCombat {
 
 	/**
 	 * Returns the results of parsing the chits.
 	 * 
 	 * @author chrisdw
 	 * 
 	 */
 	private class ParseResult {
 		public Integer damageDone;
 		public String damageText;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.combatresolution);
 
 	}
 
 	@Override
 	public CombatResolutionResult resolveNormalCombat(
 			CombatResolutionConfig config) {
 		Weapon weapon = new Weapon(config.getWeaponType(), "");
 		weapon.setSize(config.getWeaponSize());
 		Integer weaponType = 0;
 		StringBuilder resultOut = new StringBuilder();
 		StringBuilder hitResult = new StringBuilder();
 		StringBuilder chitsOut = new StringBuilder();
 		StringBuilder dieRolls = new StringBuilder();
 		Resources res = getResources();
 		int atkRoll = 0;
 		int defRoll = 0;
 		CombatResolutionResult result = new CombatResolutionResult();
 		
 		result.setState(0);
 
 		if (weapon.getType().equals(Weapon.IAVR)) {
 			weaponType = 1;
 		} else if (weapon.getType().equals(Weapon.APSW)) {
 			weaponType = 2;
 		}
 
 		Integer apparentRange;
 		Integer whichRange = weapon.range(config.getRange(), this);
 		apparentRange = whichRange;
 		if (apparentRange > 0) {
 			// High signature targets are treated as being closer
 			if (config.getTargetSize() == 6) {
 				apparentRange--;
 			} else if (config.getTargetSize() == 7) {
 				apparentRange -= 2;
 			}
 			if (apparentRange < 1) {
 				apparentRange = 1;
 			}
 		}
 		if (apparentRange > 0) {
 			if (weaponType == 0) {
 				// convert the firecon and target levels to dice
 				int fireconDice = fireControlToDice(weapon.getType(),
 						config.getFireControl(), apparentRange);
 				int targetDice = signatureToDice(config.getTargetSize());
 				if (config.isMoving()) {
 					// The firer moved, drop the dice one level
 					fireconDice -= 2;
 				}
 				if (fireconDice < 4) {
 					// Missed, firecon not capable
 					resultOut.append(res.getString(R.string.msg_not_capable));
 					atkRoll = 0;
 					defRoll = 1;
 				} else {
 					int secondary = 0;
 					// For normal systems roll the firecon and the secondary die
 					// For GMS roll ADS, PDS and ECM
 					if (weapon.getType().equals(Weapon.GUIDED_MISSILE)
 							|| weapon.getType().equals(
 									Weapon.HIGH_VELOCITY_MISSILE)) {
 						if (config.isInfantry()) {
 							resultOut
 									.append(res
 											.getString(R.string.msg_missiles_cant_shoot_inf));
 							atkRoll = 0;
 							defRoll = 1;
 						} else {
 							defRoll = 0;
 							Dice attackDice = new Dice(fireconDice);
 							atkRoll = attackDice.roll();
 
 							Dice defenceDie;
 							Integer secondaryRoll;
 
 							resultOut.append(String.format(
 									res.getString(R.string.msg_attack_result),
 									fireconDice, atkRoll));
 							// HVMS ignore ADS and PDS
 							if (weapon.getType().equals(Weapon.GUIDED_MISSILE)) {
 								secondary = DSToDice(config.getAds());
 								if (secondary > 0) {
 									defenceDie = new Dice(secondary);
 									secondaryRoll = defenceDie.roll();
 									resultOut
 											.append(String.format(
 													res.getString(R.string.msg_ads_result),
 													defenceDie, secondaryRoll));
 									if (secondaryRoll > defRoll) {
 										defRoll = secondaryRoll;
 									}
 								}
 								secondary = DSToDice(config.getPds());
 								if (secondary > 0) {
 									defenceDie = new Dice(secondary);
 									secondaryRoll = defenceDie.roll();
 									resultOut
 											.append(String.format(
 													res.getString(R.string.msg_pds_result),
 													defenceDie, secondaryRoll));
 									if (secondaryRoll > defRoll) {
 										defRoll = secondaryRoll;
 									}
 								}
 							}
 							// ECM always effects missiles
 							secondary = ECMToDice(config.getEcm());
 							if (secondary > 0) {
 								defenceDie = new Dice(secondary);
 								secondaryRoll = defenceDie.roll();
 								resultOut.append(String.format(
 										res.getString(R.string.msg_ecm_result),
 										defenceDie, secondaryRoll));
 								if (secondaryRoll > defRoll) {
 									defRoll = secondaryRoll;
 								}
 							}
 						}
 					} else {
 						secondary = secondaryToDice(config.getTargetState());
 						dieRolls.append(String.format(
 								res.getString(R.string.msg_result_die),
 								fireconDice, targetDice, secondary));
 
 						// Now do the to hit rolls
 						if (targetDice > 0) {
 							Dice defenceDice = new Dice(targetDice);
 							defRoll = defenceDice.roll();
 						}
 
 						Integer secondaryRoll = 0;
 						if (secondary > 0) {
 							Dice secondaryDice = new Dice(secondary);
 							secondaryRoll = secondaryDice.roll();
 						}
 
 						Dice attackDice = new Dice(fireconDice);
 						atkRoll = attackDice.roll();
 
 						dieRolls.append(String.format(
 								res.getString(R.string.msg_result), atkRoll,
 								defRoll, secondaryRoll));
 						if (secondaryRoll > defRoll) {
 							defRoll = secondaryRoll;
 						}
 					}
 				}
 			} else {
 				// this is for weapons that don't roll to hit (IVAR, APSW)
 				if (config.isInfantry() && weaponType == 1) {
 					resultOut.append(res.getString(R.string.msg_iavr_inf));
 					atkRoll = 0;
 					defRoll = 1;
 				} else {
 					resultOut.append(res.getString(R.string.msg_autohit));
 					atkRoll = 1;
 					defRoll = 0;
 				}
 			}
 			if (atkRoll > defRoll) {
 				// Hit. resolve damage
 				String[] chitList = drawChits(weapon.effectiveSize());
 
 				// If a SLAM has fired and hit spillover fire is allowed
 				if (weapon.getType().equals(Weapon.SLAM)) {
 					// Get the true range, rather than adjusted for size
 					if (whichRange == 2) {
 						result.setState(1);
 						resultOut.append(String.format(
 								res.getString(R.string.msg_spillover), 1));
 					} else if (whichRange == 3) {
 						result.setState(2);
 						resultOut.append(String.format(
 								res.getString(R.string.msg_spillover), 2));
 					}
 				}
 				if (chitList.length > 0) {
 					Integer armour;
 					Weapon.ChitConfig chitConfig;
 
 					if (config.isInfantry()) {
 						armour = config.getHtk();
 						chitConfig = weapon.validChits(whichRange, -1, this);
 					} else {
 						armour = config.getArmourRating();
 						chitConfig = weapon.validChits(whichRange,
 								config.getArmourTypeId(), this);
 					}
 
 					chitsOut.append(String.format(
 							res.getString(R.string.msg_armour),
 							config.getArmourTypeId(), chitConfig.chits));
 
 					ParseResult chitResult = parseChits(chitList,
 							chitConfig.chits);
 
 					Float totalDamage = chitConfig.factor
 							* chitResult.damageDone;
 
 					if (chitResult.damageText.contains(res
 							.getString(R.string.chit_b))
 							&& !config.isInfantry()) {
 						hitResult.append(res.getString(R.string.msg_destroyed));
 					} else if (totalDamage.intValue() > armour) {
 						hitResult.append(res.getString(R.string.msg_destroyed));
 					} else if (totalDamage.intValue() == armour) {
 						hitResult.append(res.getString(R.string.msg_damaged));
 					} else {
 						hitResult.append(res.getString(R.string.msg_no_effect));
 					}
 
 					hitResult.append(String.format(
 							res.getString(R.string.msg_total_damage),
							totalDamage.intValue(), hitResult.toString()));
 					
 					resultOut.append(hitResult.toString());
 				}
 			} else {
 				// Missed
 				resultOut.append(res.getString(R.string.msg_missed));
 			}
 		} else {
 			resultOut.append(res.getString(R.string.msg_out_of_range));
 		}
 		result.setChits(chitsOut.toString());
		result.setOutcome(hitResult.toString());
 		result.setDieRolls(dieRolls.toString());
 		
 		// If there is a result fragment about, update it. If
 		CombatResolutionResultFragment frag = (CombatResolutionResultFragment) getFragmentManager()
 				.findFragmentById(R.id.combatresolutionresultfragment);
 		if (frag != null) {
 			frag.setResult(result);
 		} else {
 			
 		}
 		return result;
 	}
 
 	@Override
 	public void resolveSpillover(CombatResolutionConfig config, Integer range,
 			Integer probability) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Given a weapon type, fire control and range band work out the dice to
 	 * roll
 	 * 
 	 * @param weaponType
 	 * @param fireContol
 	 * @param range
 	 * @return
 	 */
 	private int fireControlToDice(String weaponType, String fireContol,
 			Integer range) {
 		int dice = 0;
 		String diceString = Utilities.rangeDie(weaponType, fireContol,
 				fireContol, range, this);
 
 		if (!diceString.equals("--")) {
 			dice = Integer.parseInt(diceString.substring(1));
 		}
 		return dice;
 	}
 
 	/**
 	 * Convert a signature code into the dice to throw
 	 * 
 	 * @param size
 	 *            the effective size
 	 * @return number of sides on the dice
 	 */
 	private int signatureToDice(Integer size) {
 		int dice = 0;
 		String diceString = "--";
 		DatabaseHelper dbh = new DatabaseHelper(this);
 		Cursor sig = dbh.getSignatureForSize(size.toString());
 		int colIndex = sig.getColumnIndex("Signature");
 		if (sig.moveToNext()) {
 			diceString = sig.getString(colIndex);
 		}
 		if (!diceString.equals("--")) {
 			dice = Integer.parseInt(diceString.substring(1));
 		}
 		return dice;
 	}
 
 	/**
 	 * Converts a Defence system reference to a die roll
 	 * 
 	 * @param which
 	 * @return
 	 */
 	private int DSToDice(Integer which) {
 		int die = 0;
 		switch (which) {
 		case 0: // None
 			die = 0;
 			break;
 		case 1: // Basic
 			die = 4;
 			break;
 		case 2: // Enhanced
 			die = 8;
 			break;
 		case 3: // Superior
 			die = 10;
 			break;
 		case 4: // Brilliant
 			die = 12;
 			break;
 		default:
 			die = 0;
 		}
 		return die;
 	}
 
 	/**
 	 * Given an ECM rating, work out the dice to roll
 	 * 
 	 * @param ecmRating
 	 * @return
 	 */
 	private int ECMToDice(String ecmRating) {
 		int die = 0;
 		String diceString = "--";
 		DatabaseHelper dbh = new DatabaseHelper(this);
 		Cursor res = dbh.getDiceForECM(ecmRating);
 		int colIndex = res.getColumnIndex("Die");
 		if (res.moveToNext()) {
 			diceString = res.getString(colIndex);
 		}
 		if (!diceString.equals("--")) {
 			die = Integer.parseInt(diceString.substring(1));
 		}
 		return die;
 	}
 
 	/**
 	 * Convert the state of the target into a secondary dice
 	 * 
 	 * @param targetState
 	 * @return
 	 */
 	private int secondaryToDice(Integer targetState) {
 		int die = 0;
 		switch (targetState) {
 		case 0: // None
 			die = 0;
 			break;
 		case 1: // Hull down
 			die = 10;
 			break;
 		case 2: // Turret down
 			die = 12;
 			break;
 		case 3: // Dug in
 			die = 10;
 			break;
 		case 4: // Evading
 			die = 8;
 			break;
 		case 5: // Popping up
 			die = 6;
 			break;
 		case 6: // Soft cover
 			die = 6;
 			break;
 		default:
 			die = 0;
 		}
 		return die;
 	}
 
 	/**
 	 * Draw a number of random chits from the chit pool.
 	 * 
 	 * @param weaponSize
 	 * @return
 	 */
 	private String[] drawChits(Integer weaponSize) {
 		String[] chitsDrawn = new String[weaponSize.intValue()];
 		Dice dice = new Dice(120);
 
 		for (int chit = 0; chit < weaponSize; chit++) {
 			Integer roll = dice.roll();
 			if (roll > 0 && roll <= 10) {
 				chitsDrawn[chit] = "R3";
 			} else if (roll > 10 && roll <= 25) {
 				chitsDrawn[chit] = "R2";
 			} else if (roll > 25 && roll <= 45) {
 				chitsDrawn[chit] = "R1";
 			} else if (roll > 45 && roll <= 50) {
 				chitsDrawn[chit] = "R0";
 			} else if (roll > 50 && roll <= 55) {
 				chitsDrawn[chit] = "G3";
 			} else if (roll > 55 && roll <= 62) {
 				chitsDrawn[chit] = "G2";
 			} else if (roll > 62 && roll <= 72) {
 				chitsDrawn[chit] = "G1";
 			} else if (roll > 72 && roll <= 75) {
 				chitsDrawn[chit] = "G0";
 			} else if (roll > 75 && roll <= 80) {
 				chitsDrawn[chit] = "Y3";
 			} else if (roll > 80 && roll <= 87) {
 				chitsDrawn[chit] = "Y2";
 			} else if (roll > 87 && roll <= 97) {
 				chitsDrawn[chit] = "Y1";
 			} else if (roll > 97 && roll <= 100) {
 				chitsDrawn[chit] = "Y0";
 			} else if (roll > 100 && roll <= 107) {
 				chitsDrawn[chit] = "MB"; // Mobility
 			} else if (roll > 107 && roll <= 112) {
 				chitsDrawn[chit] = "TD"; // Target systems down
 			} else if (roll > 112 && roll <= 114) {
 				chitsDrawn[chit] = "FD"; // Firer systems down
 			} else if (roll > 114 && roll <= 119) {
 				chitsDrawn[chit] = "B!"; // BOOM
 			} else {
 				chitsDrawn[chit] = "E:" + roll.toString();
 			}
 		}
 		return chitsDrawn;
 	}
 
 	/**
 	 * Given a set of drawn chits, compute the effect.
 	 * @param chitsDrawn The list of chits drawn
 	 * @param validChits The valid chits
 	 * @return Structure representing damage done and any textual results
 	 */
 	private ParseResult parseChits(String[] chitsDrawn, String validChits) {
 		ParseResult result = new ParseResult();
 		Integer damage = 0;
 		String chitMask = validChits;
 		StringBuilder text = new StringBuilder();
 		;
 		Resources res = getResources();
 
 		if (validChits.equals("ALL")) {
 			chitMask = "RYG";
 		}
 		result.damageDone = 0;
 		result.damageText = "";
 
 		for (int chit = 0; chit < chitsDrawn.length; chit++) {
 			if (chitMask.contains(chitsDrawn[chit].substring(0, 1))) {
 				damage += Integer.parseInt(chitsDrawn[chit].substring(1, 2));
 			}
 			if (chitsDrawn[chit].equals("MB")) {
 				text.append(res.getString(R.string.chit_mb));
 			}
 			if (chitsDrawn[chit].equals("TD")) {
 				text.append(res.getString(R.string.chit_td));
 			}
 			if (chitsDrawn[chit].equals("FD")) {
 				text.append(res.getString(R.string.chit_fd));
 			}
 			if (chitsDrawn[chit].equals("B!")) {
 				text.append(res.getString(R.string.chit_b));
 			}
 		}
 
 		result.damageDone = damage;
 		result.damageText = text.toString();
 		return result;
 	}
 }
