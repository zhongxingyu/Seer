 /*
    Copyright 2011 kanata3249
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package com.github.kanata3249.ffxieq;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import com.github.kanata3249.ffxi.status.*;
 
 public class EquipmentSet extends StatusModifier implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	public static final int MAINWEAPON = 0;
 	public static final int SUBWEAPON = 1;
 	public static final int HEAD = 2;
 	public static final int BODY = 3;
 	public static final int HANDS = 4;
 	public static final int FEET = 5;
 	public static final int LEGS = 6;
 	public static final int BACK = 7;
 	public static final int NECK = 8;
 	public static final int WAIST = 9;
 	public static final int RING1 = 10;
 	public static final int RING2 = 11;
 	public static final int EAR1 = 12;
 	public static final int EAR2 = 13;
 	public static final int RANGE = 14;
 	public static final int ANMO = 15;
 	public static final int EQUIPMENT_NUM = 16;
 	
 	private Equipment[] mEquipments;
 	private transient ArrayList<Combination> mCombinations;
 	
 	public EquipmentSet () {
 		super();
 		
 		mEquipments = new Equipment[EQUIPMENT_NUM];
 	}
 
 	// IStatus
 	public StatusValue getStatus(JobLevelAndRace level, StatusType type) {
 		StatusValue total = new StatusValue(0, 0);
 
 		parseDescriptions();
 		if (mCombinations != null) {
 			for (int i = 0; i < mCombinations.size(); i++) {
 				Combination combi;
 				
 				combi = mCombinations.get(i);
 				combi.parseDescription();
 				total.add(combi.getStatus(level, type));
 			}
 		}
 		if (mEquipments[HEAD] != null)
 			total.add(mEquipments[HEAD].getStatus(level, type));
 		if (mEquipments[BODY] != null)
 			total.add(mEquipments[BODY].getStatus(level, type));
 		if (mEquipments[HANDS] != null)
 			total.add(mEquipments[HANDS].getStatus(level, type));
 		if (mEquipments[FEET] != null)
 			total.add(mEquipments[FEET].getStatus(level, type));
 		if (mEquipments[LEGS] != null)
 			total.add(mEquipments[LEGS].getStatus(level, type));
 		if (mEquipments[BACK] != null)
 			total.add(mEquipments[BACK].getStatus(level, type));
 		if (mEquipments[NECK] != null)
 			total.add(mEquipments[NECK].getStatus(level, type));
 		if (mEquipments[WAIST] != null)
 			total.add(mEquipments[WAIST].getStatus(level, type));
 		if (mEquipments[RING1] != null)
 			total.add(mEquipments[RING1].getStatus(level, type));
 		if (mEquipments[RING2] != null)
 			total.add(mEquipments[RING2].getStatus(level, type));
 		if (mEquipments[EAR1] != null)
 			total.add(mEquipments[EAR1].getStatus(level, type));
 		if (mEquipments[EAR2] != null)
 			total.add(mEquipments[EAR2].getStatus(level, type));
 		switch(type) {
 		case D:
 		case Delay:
 			if (mEquipments[MAINWEAPON] != null) {
 				if (mEquipments[MAINWEAPON].getWeaponType() == StatusType.SKILL_HANDTOHAND) {
 					total.setAdditional(mEquipments[MAINWEAPON].getStatus(level, type).getAdditional());
 				} else {
 					total.setValue(mEquipments[MAINWEAPON].getStatus(level, type).getAdditional());
 				}
 			}
 			break;
 
 		case DSub:
 			if (mEquipments[SUBWEAPON] != null) {
 				total.setValue(mEquipments[SUBWEAPON].getStatus(level, StatusType.D).getAdditional());
 			}
 			break;
 		case DelaySub:
 			if (mEquipments[SUBWEAPON] != null) {
 				total.setValue(mEquipments[SUBWEAPON].getStatus(level, StatusType.Delay).getAdditional());
 			}
 			break;
 
 		case DRange:
 			if (mEquipments[RANGE] != null) {
 				total.setValue(mEquipments[RANGE].getStatus(level, StatusType.D).getAdditional());
 				if (mEquipments[ANMO] != null)
 					total.setAdditional(mEquipments[ANMO].getStatus(level, StatusType.D).getAdditional());
 			} else if (mEquipments[ANMO] != null)
 				total.setValue(mEquipments[ANMO].getStatus(level, StatusType.D).getAdditional());
 			break;
 		case DelayRange:
 			if (mEquipments[RANGE] != null) {
 				total.setValue(mEquipments[RANGE].getStatus(level, StatusType.Delay).getAdditional());
 				if (mEquipments[ANMO] != null)
 					total.setAdditional(mEquipments[ANMO].getStatus(level, StatusType.Delay).getAdditional());
 			} else if (mEquipments[ANMO] != null)
 				total.setValue(mEquipments[ANMO].getStatus(level, StatusType.Delay).getAdditional());
 			break;
 		default:
 			if (mEquipments[MAINWEAPON] != null)
 				total.add(mEquipments[MAINWEAPON].getStatus(level, type));
 			if (mEquipments[SUBWEAPON] != null)
 				total.add(mEquipments[SUBWEAPON].getStatus(level, type));
 			if (mEquipments[RANGE] != null)
 				total.add(mEquipments[RANGE].getStatus(level, type));
 			if (mEquipments[ANMO] != null)
 				total.add(mEquipments[ANMO].getStatus(level, type));
 		}
 		return total;
 	}
 
 	public Equipment getEquipment(int part) {
 		return mEquipments[part];
 	}
 	
 	public void setEquipment(int part, long id, long augId) {
 		mEquipments[part] = Dao.instantiateEquipment(id, augId);
 	}
 	
 	public boolean reloadEquipments() {
 		boolean updated = false;
 		for (int i = 0; i < mEquipments.length; i++) {
 			if (mEquipments[i] != null) {
 				mEquipments[i] = Dao.instantiateEquipment(mEquipments[i].getId(), mEquipments[i].getAugId());
 				updated = true;
 			}
 		}
 		if (updated) {
 			parseDescriptions();			
 			if (mCombinations != null) {
 				for (int i = 0; i < mCombinations.size(); i++) {
 					Combination combi;
 					
 					combi = mCombinations.get(i);
 					combi.parseDescription();
 				}
 			}
 		}
 		return updated;
 	}
 	
 	public long[] reloadEquipmentsForUpdatingDatabase() {
 		boolean updated = false;
 		long result[] = new long[mEquipments.length + 1];
 		for (int i = 0; i < mEquipments.length; i++) {
 			if (mEquipments[i] != null) {
 				Equipment eq = Dao.instantiateEquipment(mEquipments[i].getId(), mEquipments[i].getAugId());
 				if (!eq.getName().equals(mEquipments[i].getName())) {
 					// find
 					eq = Dao.findEquipment(mEquipments[i].getName(), mEquipments[i].getLevel(), mEquipments[i].getPart(), mEquipments[i].getWeapon());
 					if (eq != null) {
 						mEquipments[i] = eq;
 						updated = true;
 						result[i] = -1;
 					} else {
 						result[i] = mEquipments[i].getId();
 					}
 				} else {
 					result[i] = -1;
 				}
 			} else {
 				result[i] = -1;
 			}
 		}
 		if (updated) {
 			parseDescriptions();			
 			if (mCombinations != null) {
 				for (int i = 0; i < mCombinations.size(); i++) {
 					Combination combi;
 					
 					combi = mCombinations.get(i);
 					combi.parseDescription();
 				}
 			}
 		}
 		result[EQUIPMENT_NUM] = updated ? 1 : 0;
 		return result;
 	}
 
 	public boolean reloadAugmentsIfChangesThere() {
 		boolean updated = false;
 
 		for (int i = 0; i < mEquipments.length; i++) {
 			if (mEquipments[i] != null) {
 				if (mEquipments[i].getAugId() >= 0) {
 					Equipment eq = Dao.instantiateEquipment(mEquipments[i].getId(), mEquipments[i].getAugId());
 					if (mEquipments[i].getId() != eq.getId()) {
 						updated = true;
 						mEquipments[i] = eq;
 					}
 				}
 			}
 		}
 		if (updated) {
 			parseDescriptions();			
 			if (mCombinations != null) {
 				for (int i = 0; i < mCombinations.size(); i++) {
 					Combination combi;
 					
 					combi = mCombinations.get(i);
 					combi.parseDescription();
 				}
 			}
 		}
 		return updated;
 	}
 
 	public SortedStringList getUnknownTokens() {
 		SortedStringList unknownTokens = new SortedStringList();
 		for (int i = 0; i < mEquipments.length; i++) {
 			if (mEquipments[i] != null) {
 				unknownTokens.mergeList(mEquipments[i].getUnknownTokens());
 			}
 		}
 		if (mCombinations != null) {
 			for (int i = 0; i < mCombinations.size(); i++) {
 				unknownTokens.mergeList(mCombinations.get(i).getUnknownTokens());
 			}
 		}
 		return unknownTokens;
 	}
 	
 	private void parseDescriptions() {
 		boolean updated;
 
 		updated = false;
 		for (int i = 0; i < mEquipments.length; i++) {
 			if (mEquipments[i] != null) {
 				updated = mEquipments[i].parseDescription() || updated;
 				mEquipments[i].removeCombinationToken();
 				mEquipments[i].removeAugmentCommentFromUnknownToken();
 			}
 		}
 		if (updated) {
 			// check combination
 			boolean used[] = new boolean[EQUIPMENT_NUM];
 			
 			mCombinations = new ArrayList<Combination>();
 			for (int i = 0; i < mEquipments.length; i++) {
 				if (mEquipments[i] != null && used[i] == false) {
 					long combiID;
 					int numMatches;
 					Combination combi;
 					
 					combiID = mEquipments[i].getCombinationID();
 					if (combiID > 0) {
 						numMatches = 1;
 						for (int ii = i + 1; ii < mEquipments.length; ii++) {
 							if (mEquipments[ii] != null && used[ii] == false && mEquipments[ii].getCombinationID() == combiID) {
 								numMatches++;
 							}
 						}
 						if (numMatches > 1) {
 							// instantiate
 							combi = Dao.instantiateCombination(combiID, numMatches);
 							if (combi != null) {
 								mCombinations.add(combi);
 								
 								for (int ii = i; ii < mEquipments.length; ii++) {
 									if (mEquipments[ii] != null && used[ii] == false && mEquipments[ii].getCombinationID() == combiID) {
 										used[ii] = true;
 									}
 								}
 							}
 						}
 					} else {
 						used[i] = true;
 					}
 				} else {
 					used[i] = true;
 				}
 			}
 			
 			// Check all combination they are not used...
 			int maxCombi = 0;
 			int parts[];
 			
 			// check free parts.
 			for (int i = 0; i < mEquipments.length; i++) {
 				if (used[i] == false) {
 					maxCombi++;
 				}
 			}
 			parts = new int[maxCombi];
 			for (int n = 0, i = 0; i < mEquipments.length; i++) {
 				if (used[i] == false) {
 					parts[n++] = i;
 				}
 			}
 			
 			if (maxCombi > 1) {
 				for (int match = maxCombi; match > 1; match--) {
 					for (int n = 3; n < (1 << maxCombi); n++) { /* Number 3 is first value that has two bits. */
 						int bits = 0;
 						
 						// count bits
 						for (int i = 0; i < maxCombi; i++) {
 							if ((n & (1 << i)) != 0) {
 								if (used[parts[i]] == false) {
 									bits++;
 								}
 							}
 						}
 						if (bits == match) {
 							// check this combination
 							String names[] = new String[match];
 							int ii = 0;
 							for (int i = 0; i < maxCombi; i++) {
 								if ((n & (1 << i)) != 0) {
									if (used[parts[i]] == false) {
										names[ii++] = mEquipments[parts[i]].getName();
									}
 								}
 							}
 							// instantiate
 							Combination combi = Dao.searchCombination(names);
 							if (combi != null) {
 								mCombinations.add(combi);
 								
 								for (int i = 0; i < maxCombi; i++) {
 									if ((n & (1 << i)) != 0) {
 										used[parts[i]] = true;
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 }
