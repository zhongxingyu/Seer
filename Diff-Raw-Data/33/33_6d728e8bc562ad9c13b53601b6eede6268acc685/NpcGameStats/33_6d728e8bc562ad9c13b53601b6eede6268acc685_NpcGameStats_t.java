 /*
  * This file is part of aion-emu <aion-emu.com>.
  *
  *  aion-emu is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  aion-emu is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with aion-emu.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.aionemu.gameserver.model.gameobjects.stats;
 
 import com.aionemu.gameserver.model.gameobjects.Npc;
 import com.aionemu.gameserver.model.templates.stats.NpcStatsTemplate;
 
 /**
  * @author xavier
  *
  */
 public class NpcGameStats extends CreatureGameStats<Npc>
 {
 	public NpcGameStats(Npc owner, NpcStatsTemplate nst) {
 		super(owner);
 		// TODO set other stats
 		initStat(StatEnum.MAXHP, nst.getMaxHp() + Math.round((owner.getObjectTemplate().getHpGauge() * 1.5f) * (int)owner.getLevel()));
 		initStat(StatEnum.MAXMP, nst.getMaxMp());
 		initStat(StatEnum.ATTACK_SPEED, Math.round(nst.getAttackSpeed() * 1000));
		initStat(StatEnum.PHYSICAL_DEFENSE, Math.round(((nst.getPdef() / (int)owner.getLevel()) - 1) * nst.getPdef() + 10 * (int)owner.getLevel()));
 		initStat(StatEnum.EVASION, Math.round(nst.getEvasion() * 2.3f + (int)owner.getLevel() * 10));
 		initStat(StatEnum.MAGICAL_RESIST, Math.round(nst.getMdef()));
 		initStat(StatEnum.MAIN_HAND_POWER, nst.getPower());
 		initStat(StatEnum.MAIN_HAND_ACCURACY, Math.round(nst.getAccuracy() * 2.3f + (int)owner.getLevel() * 10));
 		initStat(StatEnum.MAIN_HAND_CRITICAL, Math.round(nst.getCrit()));
 		initStat(StatEnum.SPEED, Math.round(nst.getRunSpeedFight() * 1000));
 	}
 }
