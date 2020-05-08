 package com.undeadscythes.udsplugin;
 
 import org.bukkit.*;
 
 /**
  * Permissions to use within UDSPlugin.
  * @author UndeadScythes
  */
 public enum Perm {
     A(PlayerRank.MOD),
     ACCEPTRULES(PlayerRank.NEWBIE, false),
     ADMIN(PlayerRank.ADMIN),
     ADMINCHAT(PlayerRank.MOD),
     ADMINMSG(PlayerRank.MOD),
     AFK(PlayerRank.NEWBIE),
     ANYMODE(PlayerRank.MOD),
     BACK(PlayerRank.MEMBER),
     BACK_ON_DEATH(PlayerRank.VIP),
     BAN(PlayerRank.MOD),
     BOUNTY(PlayerRank.MEMBER),
     BROADCAST(PlayerRank.MOD),
     BUTCHER(PlayerRank.MOD),
     BYPASS(PlayerRank.MOD),
     C(PlayerRank.MEMBER),
     CALL(PlayerRank.NEWBIE),
     CHALLENGE(PlayerRank.MEMBER),
     CHAT_HELP(PlayerRank.NEWBIE),
     CHECK(PlayerRank.NEWBIE),
     CHUNK(PlayerRank.ADMIN),
     CI(PlayerRank.MEMBER),
     CITY(PlayerRank.MEMBER, GameMode.SURVIVAL),
     CLAN(PlayerRank.MEMBER),
     CLAN_BASE_BUILD(PlayerRank.MEMBER, GameMode.SURVIVAL),
     COMPASS(PlayerRank.WARDEN),
     DAY(PlayerRank.WARDEN),
     DEBUG(PlayerRank.ADMIN),
     DELWARP(PlayerRank.WARDEN),
     DEMOTE(PlayerRank.MOD),
     ENCHANT(PlayerRank.MOD),
     FACE(PlayerRank.NEWBIE),
     GIFT(PlayerRank.MEMBER, GameMode.SURVIVAL),
     GOD(PlayerRank.MOD),
     HEADDROP(PlayerRank.MOD),
     HEAL(PlayerRank.WARDEN),
     HELP(PlayerRank.NEWBIE),
     HOME(PlayerRank.MEMBER),
     HOME_MAKE(PlayerRank.MEMBER, GameMode.SURVIVAL),
     HOME_OTHER(PlayerRank.MOD),
     I(PlayerRank.VIP),
     IGNORE(PlayerRank.NEWBIE),
     INVSEE(PlayerRank.WARDEN),
     ITEM(PlayerRank.NEWBIE),
     I_ADMIN(PlayerRank.MOD),
     JAIL(PlayerRank.WARDEN),
     KICK(PlayerRank.MOD),
     KIT(PlayerRank.NEWBIE, GameMode.SURVIVAL),
     LOCKDOWN(PlayerRank.ADMIN),
     MAP(PlayerRank.NEWBIE, GameMode.SURVIVAL),
     ME(PlayerRank.NEWBIE),
     MINECART(PlayerRank.NEWBIE),
     MOD(PlayerRank.MOD),
     MOD_HELP(PlayerRank.MOD, false),
     MONEY(PlayerRank.NEWBIE),
     MONEY_ADMIN(PlayerRank.MOD),
     MONEY_OTHER(PlayerRank.WARDEN),
     MIDAS(PlayerRank.MOD),
     N(PlayerRank.NEWBIE),
     NEWBIEMSG(PlayerRank.NEWBIE, false),
     NICK(PlayerRank.MEMBER),
     NICK_OTHER(PlayerRank.MOD),
     NIGHT(PlayerRank.WARDEN),
     P(PlayerRank.NEWBIE),
     PAPER_COMPLEX(PlayerRank.MOD),
     PAPER_SIMPLE(PlayerRank.NEWBIE),
     PAYBAIL(PlayerRank.NEWBIE),
     PET(PlayerRank.MEMBER),
     PLOT(PlayerRank.MEMBER, GameMode.CREATIVE),
     PLOT_MANY(PlayerRank.ADMIN, GameMode.CREATIVE),
     PLOT_REMOVE(PlayerRank.ADMIN, GameMode.CREATIVE),
     PORTAL(PlayerRank.ADMIN),
     PORTAL_USE(PlayerRank.NEWBIE),
     POWERTOOL(PlayerRank.MOD),
     PRIVATE(PlayerRank.NEWBIE),
     PRIZE(PlayerRank.NEWBIE),
     PROMOTE(PlayerRank.MOD),
     R(PlayerRank.NEWBIE),
     RAIN(PlayerRank.WARDEN),
     REGION(PlayerRank.MOD),
     RULES(PlayerRank.NEWBIE),
     SCUBA(PlayerRank.NEWBIE, GameMode.SURVIVAL),
     SERVER(PlayerRank.ADMIN),
     SETSPAWN(PlayerRank.ADMIN),
     SETWARP(PlayerRank.WARDEN),
     SHAREDINV(PlayerRank.MOD),
     SHOP(PlayerRank.MEMBER, GameMode.SURVIVAL),
     SHOP_ADMIN(PlayerRank.ADMIN, GameMode.SURVIVAL),
     SHOP_SERVER(PlayerRank.MOD, GameMode.SURVIVAL),
     SHOP_SIGN(PlayerRank.MEMBER, GameMode.SURVIVAL),
     SIGNS(PlayerRank.MOD),
     SIGN_CHECKPOINT(PlayerRank.MOD),
     SIGN_ITEM(PlayerRank.ADMIN),
     SIGN_MINECART(PlayerRank.MOD),
     SIGN_PRIZE(PlayerRank.ADMIN),
     SIGN_SPLEEF(PlayerRank.MOD),
     SIGN_WARP(PlayerRank.MOD),
     SIT(PlayerRank.NEWBIE),
     SPAWN(PlayerRank.NEWBIE),
     SPAWNER(PlayerRank.MOD),
     SPLEEF(PlayerRank.NEWBIE),
     STACK(PlayerRank.NEWBIE),
     STATS(PlayerRank.NEWBIE),
     STORM(PlayerRank.WARDEN),
     SUN(PlayerRank.WARDEN),
     TELL(PlayerRank.NEWBIE),
     TGM(PlayerRank.MOD),
     TICKET(PlayerRank.NEWBIE),
     TP(PlayerRank.MOD),
     UNAVOIDABLE(PlayerRank.WARDEN),
     UNBAN(PlayerRank.MOD),
     UNJAIL(PlayerRank.WARDEN),
     UNKICKABLE(PlayerRank.MOD),
     VANISH(PlayerRank.MOD),
    VIP(PlayerRank.NEWBIE),
     VIP_BUY(PlayerRank.MEMBER, false),
     VIP_HELP(PlayerRank.VIP, false),
     WAND(PlayerRank.MEMBER),
     WARDEN(PlayerRank.WARDEN),
     WARDEN_HELP(PlayerRank.WARDEN, false),
     WARP(PlayerRank.NEWBIE),
     WE(PlayerRank.WARDEN),
     WE_COPY(PlayerRank.MOD),
     WE_DRAIN(PlayerRank.MOD),
     WE_EXT(PlayerRank.WARDEN),
     WE_LOAD(PlayerRank.OWNER),
     WE_MOVE(PlayerRank.MOD),
     WE_PASTE(PlayerRank.MOD),
     WE_REGEN(PlayerRank.MOD),
     WE_REPLACE(PlayerRank.MOD),
     WE_SAVE(PlayerRank.OWNER),
     WE_SET(PlayerRank.MOD),
     WE_UNDO(PlayerRank.MOD),
     WE_VIP(PlayerRank.VIP, false),
     WHERE(PlayerRank.NEWBIE),
     WHO(PlayerRank.NEWBIE),
     WHOIS(PlayerRank.NEWBIE),
     WORLD(PlayerRank.ADMIN),
     XP(PlayerRank.MOD),
     Y(PlayerRank.NEWBIE);
 
     public PlayerRank rank;
     private boolean hereditary;
     private GameMode mode;
 
     private Perm(final PlayerRank rank, final boolean isHereditary, final GameMode mode) {
         this.rank = rank;
         this.hereditary = isHereditary;
         this.mode = mode;
     }
 
     private Perm(final PlayerRank rank) {
         this(rank, true, null);
     }
     
     private Perm(final PlayerRank rank, final boolean hereditary) {
         this(rank, hereditary, null);
     }
     
     private Perm(final PlayerRank rank, final GameMode mode) {
         this(rank, true, mode);
     }
 
     /**
      * Get the rank assigned to the permission.
      * @return
      */
     public PlayerRank getRank() {
         return rank;
     }
 
     /**
      * Check if a permission is hereditary.
      * @return
      */
     public boolean isHereditary() {
         return hereditary;
     }
     
     public GameMode getMode() {
         return mode;
     }
 }
