 package com.mitsugaru.Karmiconomy.permissions;
 
 public enum PermissionNode {
     /**
      * Admin nodes
      */
     ADMIN(".admin"),
     /**
      * Bypass nodes
      */
     //Normal
     BYPASS_BED_ENTER(".bypass.bed.enter"),
     BYPASS_BED_LEAVE(".bypass.bed.leave"),
     BYPASS_BLOCK_DESTROY(".bypass.block.destroy"),
     BYPASS_BLOCK_PLACE(".bypass.block.place"),
     BYPASS_BOW_SHOOT(".bypass.bow.shoot"),
     BYPASS_BUCKET_EMPTY_LAVA(".bypass.bucket.empty.lava"),
     BYPASS_BUCKET_EMPTY_WATER(".bypass.bucket.empty.water"),
     BYPASS_BUCKET_FILL_LAVA(".bypass.bucket.fill.lava"),
     BYPASS_BUCKET_FILL_WATER(".bypass.bucket.fill.water"),
     BYPASS_ITEM_CRAFT(".bypass.item.craft"),
     BYPASS_ITEM_ENCHANT(".bypass.item.enchant"),
     BYPASS_ITEM_DROP(".bypass.item.drop"),
     BYPASS_ITEM_PICKUP(".bypass.item.pickup"),
     BYPASS_ITEM_EGG(".bypass.item.egg"),
     BYPASS_PAINTING(".bypass.painting"),
     BYPASS_PLAYER_CHAT(".bypass.player.chat"),
    BYPASS_PLAYER_COMMAND(".bypass.player.command"),
    BYPASS_PLAYER_DEATH(".bypass.player.death"),
     BYPASS_PLAYER_GAMEMODE_CREATIVE(".bypass.player.gamemode.creative"),
     BYPASS_PLAYER_GAMEMODE_SURVIVAL(".bypass.player.gamemode.survival"),
     BYPASS_PLAYER_JOIN(".bypass.player.join"),
     BYPASS_PLAYER_KICK(".bypass.player.kick"),
    BYPASS_PLAYER_QUIT(".bypass.player.quit"),
     BYPASS_PLAYER_RESPAWN(".bypass.player.respawn"),
     BYPASS_PLAYER_SNEAK(".bypass.player.sneak"),
     BYPASS_PLAYER_SPRINT(".bypass.player.sprint"),
     BYPASS_PORTAL_NETHER(".bypass.portal.nether"),
     BYPASS_PORTAL_END(".bypass.portal.end"),
     BYPASS_PORTAL_CUSTOM(".bypass.portal.custom"),
     BYPASS_PORTAL_ENTER(".bypass.portal.enter"),
     BYPASS_TAME_OCELOT(".bypass.tame.ocelot"),
     BYPASS_TAME_WOLF(".bypass.tame.wolf"),
     BYPASS_WORLD_CHANGE(".bypass.world.change"),
     //McMMO
     BYPASS_MCMMO_PARTY_JOIN(".bypass.mcmmo.party.join"),
     BYPASS_MCMMO_PARTY_LEAVE(".bypass.mcmmo.party.leave"),
     BYPASS_MCMMO_PARTY_KICK(".bypass.mcmmo.party.kick"),
     BYPASS_MCMMO_PARTY_CHANGE(".bypass.mcmmo.party.change"),
     BYPASS_MCMMO_PARTY_TELEPORT(".bypass.mcmmo.party.teleport"),
     BYPASS_MCMMO_LEVEL_ACROBATICS(".bypass.mcmmo.level.acrobatics"),
     BYPASS_MCMMO_LEVEL_ARCHERY(".bypass.mcmmo.level.archery"),
     BYPASS_MCMMO_LEVEL_AXES(".bypass.mcmmo.level.axes"),
     BYPASS_MCMMO_LEVEL_EXCAVATION(".bypass.mcmmo.level.excavation"),
     BYPASS_MCMMO_LEVEL_FISHING(".bypass.mcmmo.level.fishing"),
     BYPASS_MCMMO_LEVEL_HERBALISM(".bypass.mcmmo.level.herbalism"),
     BYPASS_MCMMO_LEVEL_MINING(".bypass.mcmmo.level.mining"),
     BYPASS_MCMMO_LEVEL_REPAIR(".bypass.mcmmo.level.repair"),
     BYPASS_MCMMO_LEVEL_SWORDS(".bypass.mcmmo.level.swords"),
     BYPASS_MCMMO_LEVEL_TAMING(".bypass.mcmmo.level.taming"),
     BYPASS_MCMMO_LEVEL_UNARMED(".bypass.mcmmo.level.unarmed"),
     BYPASS_MCMMO_LEVEL_WOODCUTTING(".bypass.mcmmo.level.woodcutting"),
     BYPASS_MCMMO_XP_ACROBATICS(".bypass.mcmmo.xp.acrobatics"),
     BYPASS_MCMMO_XP_ARCHERY(".bypass.mcmmo.xp.archery"),
     BYPASS_MCMMO_XP_AXES(".bypass.mcmmo.xp.axes"),
     BYPASS_MCMMO_XP_EXCAVATION(".bypass.mcmmo.xp.excavation"),
     BYPASS_MCMMO_XP_FISHING(".bypass.mcmmo.xp.fishing"),
     BYPASS_MCMMO_XP_HERBALISM(".bypass.mcmmo.xp.herbalism"),
     BYPASS_MCMMO_XP_MINING(".bypass.mcmmo.xp.mining"),
     BYPASS_MCMMO_XP_REPAIR(".bypass.mcmmo.xp.repair"),
     BYPASS_MCMMO_XP_SWORDS(".bypass.mcmmo.xp.swords"),
     BYPASS_MCMMO_XP_TAMING(".bypass.mcmmo.xp.taming"),
     BYPASS_MCMMO_XP_UNARMED(".bypass.mcmmo.xp.unarmed"),
     BYPASS_MCMMO_XP_WOODCUTTING(".bypass.mcmmo.xp.woodcutting"),
     //Heroes
     BYPASS_HEROES_CHANGE_CLASS(".bypass.heroes.change.class"),
     BYPASS_HEROES_CHANGE_EXP(".bypass.heroes.change.exp"),
     BYPASS_HEROES_CHANGE_LEVEL(".bypass.heroes.change.level"),
     BYPASS_HEROES_COMBAT_ENTER(".bypass.heroes.combat.enter"),
     BYPASS_HEROES_COMBAT_LEAVE(".bypass.heroes.combat.leave"),
     BYPASS_HEROES_KILL_ATTACK_PLAYER(".bypass.heroes.kill.attack.player"),
     BYPASS_HEROES_KILL_ATTACK_MOB(".bypass.heroes.kill.attack.mob"),
     BYPASS_HEROES_KILL_DEFEND_PLAYER(".bypass.heroes.kill.defend.player"),
     BYPASS_HEROES_KILL_DEFEND_MOB(".bypass.heroes.kill.defend.mob"),
     BYPASS_HEROES_PARTY_JOIN(".bypass.heroes.party.join"),
     BYPASS_HEROES_PARTY_LEAVE(".bypass.heroes.party.leave"),
     BYPASS_HEROES_REGAIN_HEALTH(".bypass.heroes.regain.health"),
     BYPASS_HEROES_REGAIN_MANA(".bypass.heroes.regain.mana"),
     BYPASS_HEROES_SKILL_COMPLETE(".bypass.heroes.skill.complete"),
     BYPASS_HEROES_SKILL_USE(".bypass.heroes.skill.use"),
     BYPASS_HEROES_SKILL_DAMAGE(".bypass.heroes.skill.damage"),
     BYPASS_HEROES_WEAPON_DAMAGE(".bypass.heroes.weapon.damage");
     
     private static final String prefix = "Karmiconomy";
     private String node;
 
     private PermissionNode(String node) {
 	this.node = prefix + node;
     }
 
     public String getNode() {
 	return node;
     }
 
 }
