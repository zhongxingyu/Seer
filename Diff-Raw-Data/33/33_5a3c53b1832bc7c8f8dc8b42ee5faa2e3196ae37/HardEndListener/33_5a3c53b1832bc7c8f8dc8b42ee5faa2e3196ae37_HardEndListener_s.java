 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.listener
  * Created: 2013/02/10 4:06:31
  */
 package net.syamn.sakuracmd.listener;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import net.syamn.sakuracmd.SakuraCmd;
 import net.syamn.sakuracmd.enums.PartyStatus;
 import net.syamn.sakuracmd.events.EndResetEvent;
 import net.syamn.sakuracmd.events.EndResettingEvent;
 import net.syamn.sakuracmd.manager.HardEndManager;
 import net.syamn.sakuracmd.manager.Worlds;
 import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.sakuracmd.player.PlayerManager;
 import net.syamn.sakuracmd.player.Power;
 import net.syamn.utils.LogUtil;
 import net.syamn.utils.StrUtil;
 import net.syamn.utils.Util;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.ExplosionPrimeEvent;
 import org.bukkit.event.entity.ItemSpawnEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * HardEndListener (HardEndListener.java)
  * @author syam(syamn)
  */
 public class HardEndListener implements Listener{
     private Random rnd;
     private SakuraCmd plugin;
     private HardEndManager mgr;
     public HardEndListener (final SakuraCmd plugin){
         this.plugin = plugin;
         rnd = new Random();
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEntityDeath(final EntityDeathEvent event) {
         if (event.getEntity().getType() == EntityType.ENDER_DRAGON && event.getEntity().getKiller() != null &&  event.getEntity().getWorld().getName().equals(Worlds.hard_end)) {
             final Entity ent = event.getEntity();
             final int hard_end_DragonExp = 40000;
 
             event.setDroppedExp(hard_end_DragonExp);
             Util.broadcastMessage("&6" + event.getEntity().getKiller().getName() + " &bさんがハードエンドでドラゴンを倒しました！");
 
             mgr = HardEndManager.getInstance();
             List<ItemStack> dropItems = new ArrayList<>();
             if (mgr != null){
                 mgr.dragonKilled();
                 dropItems = mgr.getDropItems();
             }
             event.getDrops().addAll(dropItems);
 
             Util.worldcastMessage(ent.getWorld(), "&aメインワールドに戻るには&f /spawn &aコマンドを使ってください！", false);
             
             // clear living entities
             clearEntity(ent.getWorld());
             plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
                 @Override public void run(){
                     clearEntity(ent.getWorld());
                 }
             }, 10L);
         }
     }
     private void clearEntity(final World world){
         for (final Entity ent : world.getEntities()){
             if (ent == null || (ent instanceof Player) || (ent instanceof EnderDragon)){
                 continue;
             }
             if ((ent instanceof TNTPrimed)|| (ent instanceof LivingEntity)){
                 ent.remove();
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onPlayerTeleport(final PlayerTeleportEvent event) {
         if (!event.getTo().getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
 
         mgr = HardEndManager.getInstance();
         final Player player = event.getPlayer();
         if ((mgr.getStatus() != PartyStatus.OPENING && mgr.isMember(player)) || Perms.TRUST.has(player)){
             // nothing to do
         }else{
             Util.message(player, "&cハードエンドに行くためにはパーティ登録を行う必要があります");
             event.setCancelled(true);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onEntityDamage(final EntityDamageEvent event) {
         if (!event.getEntity().getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
         final Entity ent = event.getEntity();
 
         // ドラゴンがダメージを受けた
         if (ent.getType() == EntityType.ENDER_DRAGON || ent.getType() == EntityType.COMPLEX_PART) {
             final Location dragonLocation = ent.getLocation();
 
             final List<Player> inWorldPlayers= new ArrayList<Player>();
             for (final Player p : ent.getWorld().getPlayers()){
                 if (!PlayerManager.getPlayer(p).hasPower(Power.INVISIBLE)){
                     inWorldPlayers.add(p);
                 }
             }
 
             event.setDamage(event.getDamage() / 3); // ダメージ1/3
 
             //LivingEntity e;
             // 毒グモ3匹ランダムターゲットで召還
             for (short i = 0; i < 6; i++) {
                 ent.getWorld().spawnEntity(dragonLocation, EntityType.CAVE_SPIDER);
             }
             // ガスト3匹ランダムターゲットで召還
             for (short i = 0; i < 4; i++) {
                 ent.getWorld().spawnEntity(dragonLocation, EntityType.GHAST);
             }
             // ゾンビ5匹ランダムターゲットで召還
             for (short i = 0; i < 6; i++) {
                 ent.getWorld().spawnEntity(dragonLocation, EntityType.ZOMBIE);
             }
 
             // 帯電クリーパー3匹召還
             for (short i = 0; i < 6; i++) {
                 ((Creeper) ent.getWorld().spawnEntity(dragonLocation, EntityType.CREEPER)).setPowered(true);
             }
 
             // ランダムプレイヤーの真上にTNTをスポーン
 
             for (short i = 0; i < 20; i++) {
                 if (inWorldPlayers.size() < 1) return;
                 Location targetLoc = inWorldPlayers.get(rnd.nextInt(inWorldPlayers.size())).getLocation(); // ターゲットプレイヤー確定と座標取得
                 Location tntloc = new Location(targetLoc.getWorld(), targetLoc.getX(), dragonLocation.getY(), targetLoc.getZ());
                 ent.getWorld().spawn(tntloc, TNTPrimed.class);
             }
         }
     }
 
     @SuppressWarnings("incomplete-switch")
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void disableEnvironmentDamageToMobs(final EntityDamageEvent event) {
         if (!event.getEntity().getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
         final Entity ent = event.getEntity();
 
         if (ent != null && (ent instanceof LivingEntity) && !(ent instanceof Player)){
             switch (event.getCause()){
                 // TNT, fire, drown, lightning, fall, lava, poison
                 case BLOCK_EXPLOSION:
                 case ENTITY_EXPLOSION:
                 case FIRE:
                 case FIRE_TICK:
                 case DROWNING:
                 case LIGHTNING:
                 case FALL:
                 case LAVA:
                 case POISON:
                     event.setDamage(0);
                     event.setCancelled(true);
                     break;
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
         if (!event.getEntity().getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
 
         final Entity ent = event.getEntity();
         final Entity attacker = event.getDamager();
 
         // ドラゴン
         if (ent.getType() == EntityType.ENDER_DRAGON || ent.getType() == EntityType.COMPLEX_PART) {
             //飛翔物によるダメージ
             if(attacker instanceof Projectile){
                 Projectile projectile = (Projectile)attacker;
                 LivingEntity shooter = projectile.getShooter();
                 //プレイヤーが発射したものならそのプレイヤーに雷を落とす
                 if(shooter instanceof Player){
                     shooter.getWorld().strikeLightning(shooter.getLocation());
                 }
             }
 
             //プレイヤーによる攻撃ならそのプレイヤーに雷を落とす
             if(attacker instanceof Player){
                 attacker.getWorld().strikeLightning(attacker.getLocation());
             }
         }
         // エンダークリスタル
         else if (ent.getType() == EntityType.ENDER_CRYSTAL) {
             switch(attacker.getType()){
                 case ARROW:
                 case PRIMED_TNT:
                     event.setDamage(0);
                     event.setCancelled(true);
                     break;
             }
 
             if (attacker.getType() == EntityType.ARROW){
                 final Projectile arrow = (Arrow) attacker;
                 if (arrow.getShooter() instanceof Player) {
                     Util.message((Player) arrow.getShooter(), "&c矢ではクリスタルを破壊できません！");
                 }
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void addPlayerDamage(final EntityDamageByEntityEvent event) {
         if (!event.getEntity().getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
         if (event.getEntity() instanceof Player){
             event.setDamage(event.getDamage() + 8);
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void cancelBedClick(final PlayerInteractEvent event) {
         if (event.getPlayer().getWorld().getName().equals(Worlds.hard_end)){
             if (event.getClickedBlock().getType() == Material.BED_BLOCK && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                 event.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void cancelSpawnEnderStone(final ItemSpawnEvent event) {
         final Item item = event.getEntity();
        if (item.getWorld().getName().equals(Worlds.hard_end) && item.getItemStack().getType() == Material.ENDER_STONE) {
            event.setCancelled(true); // 負荷対策
         }
     }
 
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
         if (event.getPlayer().getWorld().getName().equals(Worlds.hard_end)) {
             event.getPlayer().setNoDamageTicks(200); // ハードエンドに移動したら10秒間無敵
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onExplosionPrime(final ExplosionPrimeEvent event) { // 爆発時の威力を高める
         if (!event.getEntity().getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
 
         // デフォルト: CREEPER:3.0 / CHARGED_CREEPER:6.0 / PRIMED_TNT:4.0 / FIREBALL:1.0(Fire:true)
         switch (event.getEntityType()) {
             case CREEPER: // クリーパー
                 event.setRadius((float) 9.0);
                 break;
                 /*
                  * TODO:Breaking 1.4.2 case FIREBALL: // ガストの火の玉
                  * event.setRadius((float) 3.0); event.setFire(true); break;
                  */
             case PRIMED_TNT: // TNT
                 event.setRadius((float) 7.0);
                 event.setFire(true);
                 break;
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEntityExplode(final EntityExplodeEvent event) {
         if (event.getEntity() == null || !event.getEntity().getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
 
         if (EntityType.PRIMED_TNT.equals(event.getEntityType())) {
             final Location baseLoc = event.getLocation().getBlock().getRelative(BlockFace.DOWN, 1).getLocation();
 
             // 基準座標を元に 3x3 まで走査する
             Block block;
             for (int x = baseLoc.getBlockX() - 1; x <= baseLoc.getBlockX() + 1; x++) {
                 for (int z = baseLoc.getBlockZ() - 1; z <= baseLoc.getBlockZ() + 1; z++) {
                     for (int y = baseLoc.getBlockY() - 1; y <= baseLoc.getBlockY() + 1; y++) {
                         block = baseLoc.getWorld().getBlockAt(x, y, z);
                         if (block.getType() != Material.AIR && block.getType() != Material.BEDROCK) {
                             block.setType(Material.AIR);
                         }
                     }
                 }
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onJoinEvent(final PlayerJoinEvent event){
         if (!event.getPlayer().getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
 
         final Player player = event.getPlayer();
         if ((mgr.getStatus() != PartyStatus.OPENING && mgr.isMember(player)) || Perms.TRUST.has(player)){
             // TODO do stuff..?
         }else{
             plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
                 @Override public void run(){
                     player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation(), TeleportCause.PLUGIN);
                 }
             }, 1L);
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onCreatureSpawn(final CreatureSpawnEvent event){
         if (!event.getLocation().getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
 
         mgr = HardEndManager.getInstance();
         if (mgr != null && mgr.getStatus() == PartyStatus.WAITING){
             switch (event.getSpawnReason()){
                 case NATURAL:
                 case DEFAULT:
                     event.setCancelled(true);
                     break;
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEndResetting(final EndResettingEvent event){
         if (!event.getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
 
         mgr = HardEndManager.getInstance();
 
         if (mgr.getStatus() == PartyStatus.STARTING){
             event.setCancelled(true);
             LogUtil.warning("Cancelled world " + Worlds.hard_end + " resetting event due to starting status");
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEndReset(final EndResetEvent event){
         if (!event.getWorld().getName().equals(Worlds.hard_end)){
             return;
         }
 
         plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){
             @Override public void run(){
                 final World world = event.getWorld();
                 final Location spawnLoc = world.getSpawnLocation();
                 if (!world.loadChunk(spawnLoc.getBlockX(), spawnLoc.getBlockZ(), false)){
                     return;
                 }
 
                 Block baseBlock = world.getHighestBlockAt(spawnLoc);
                 if (baseBlock == null) {
                     return;
                 }
                 baseBlock = baseBlock.getRelative(BlockFace.DOWN, 1);
                 world.setSpawnLocation(baseBlock.getX(), baseBlock.getY() + 1, baseBlock.getZ());
 
                 // 3x3で足場を作る
                 Block block;
                 for (int x = baseBlock.getX() - 1; x <= baseBlock.getX() + 1; x++) {
                     for (int z = baseBlock.getZ() - 1; z <= baseBlock.getZ() + 1; z++) {
                         block = baseBlock.getWorld().getBlockAt(x, baseBlock.getY(), z);
                         if (block.getType() != Material.OBSIDIAN) {
                             block.setType(Material.OBSIDIAN);
                         }
                     }
                 }
 
                 LogUtil.info("Update spawn location and create grounds on " + StrUtil.getLocationString(baseBlock));
             }
         }, 20L);
     }
 }
