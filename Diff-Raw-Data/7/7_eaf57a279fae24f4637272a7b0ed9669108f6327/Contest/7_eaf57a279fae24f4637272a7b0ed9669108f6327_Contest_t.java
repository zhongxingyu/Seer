 package me.asofold.bpl.archer.core;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import me.asofold.bpl.archer.Archer;
 import me.asofold.bpl.archer.config.compatlayer.CompatConfig;
 import me.asofold.bpl.archer.config.properties.ConfigPropertyHolder;
 import me.asofold.bpl.archer.config.properties.Property;
 import me.asofold.bpl.archer.core.contest.HitResult;
 import me.asofold.bpl.archer.utils.Utils;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 
 public class Contest extends ConfigPropertyHolder implements Comparable<Contest>{
 	
 	public String name;
 	public String owner;
 	
 	public String world = null;
 	
 	/** Players who take part in this contest. */
 	public final Map<String, PlayerData> activePlayers = new LinkedHashMap<String, PlayerData>();
 	
 	/** Last time started (2+ players in). */
 	public long lastTimeValid = 0;
 	public boolean started = false;
 	
 	/** Time that has to pass until the contest is really active. */
 	public Property startDelay = setProperty("start-delay", 0, Long.MAX_VALUE, 60000L); // 1 minutes.
 	public Property maxTime = setProperty("max-time", 0, Long.MAX_VALUE, 600000L); // 10 minutes.
 	
 	public Property minDistance = setProperty("min-distance", 0, Integer.MAX_VALUE, 5.0);
 	
 //	public Property maxPlayers = setProperty("max-players", 0, Integer.MAX_VALUE, 0);
 	public Property minPlayers = setProperty("min-players", 0, Integer.MAX_VALUE, 2);
 	public Property winMinPlayers = setProperty("min-players", 0, 1, 1);
 	
 	public Property allowLateJoin = setProperty("late-join", 0, 1, 0);
 	/** Allow re-join after disconnect. */
 	public Property allowDisconnect = setProperty("allow-disconect", 0, 1, 0);
 	
 	public Property allowWorldChange = setProperty("allow-world-change", 0, 1, 0);
 	
 	/** Only count shots that hurt (needs monitor)*/
 	// TODO: Check this on what priority level (should be like normal, depending on worldguard).
 	// TODO: Define a delay for hits to count.
 //	public Property ignoreCancelled = setProperty("ignore-cancelled", 0, 1, 0);
 	
 	// TODO: This needs redesign with listener.
 //	public Property cancelDamage = setProperty("cancel-damage", 0, 1, 0);
 	
 	/** Maximum number of shots allowed. */
 	public Property maxShots = setProperty("max-shots", 0, Integer.MAX_VALUE, 0);
 	public Property bonusShotsHit = setProperty("bonus-shots-hit", 0, Integer.MAX_VALUE, 0);
 	public Property bonusShotsKill = setProperty("bonus-shots-kill", 0, Integer.MAX_VALUE, 0);
 	
 	// TODO: Win hits can too easily be abuse with teaming up.
 //	public Property winHits = setProperty("win-hits", 0, Integer.MAX_VALUE, 0);
 	public Property lossHits = setProperty("loss-hits", 0, Integer.MAX_VALUE, 5.0);
 //	public Property winHitBalance = setProperty("win-hit-balance", 0, Integer.MAX_VALUE, 0);
 	// lossHitBalance
 	
 //	public Property winScore = setProperty("win-score", 0, Double.MAX_VALUE, 0);
 	public Property lossScore = setProperty("loss-score", 0, Integer.MAX_VALUE, 100.0);
 //	public Property winScoreBalance = setProperty("win-score-balance", Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
 	// TODO: lossScoreBalance
 	
 //	/** Number of hits that made others leave the contest. */
 //	public Property winKills = setProperty("win-kills", 0, Integer.MAX_VALUE, 0);
 	/** Loss on real player death. */
 	public Property lossDeath = setProperty("loss-death", 0, 1, 1);
 	// TODO: lossKills, winKillsBalance, lossKillsBalance.
 	
 	
 	public Contest(String name, String owner){
 		this.name = name;
 		this.owner = owner;
 		setAliases(); // Done here to be sure never to mix up order.
 	}
 	
 	public void fromConfig(CompatConfig cfg, String prefix){
 		name = cfg.getString(prefix + "name");
 		owner = cfg.getString(prefix + "owner");
 		super.fromConfig(cfg, prefix);
 	}
 	
 	@Override
 	public int hashCode(){
 		return name.toLowerCase().hashCode();
 	}
 	
 	@Override
 	public boolean equals(Object other){
 		if (other instanceof Contest){
 			return name.equalsIgnoreCase(((Contest) other).name);
 		}
 		else return false; // Might allow strings ?
 	}
 	
 	/**
 	 * 
 	 * @param data
 	 * @return If to remove cd from data.
 	 */
 	public boolean onPlayerJoinServer(final PlayerData data) {
 		// Re check validity.
 		if (!world.equals("*") && !data.player.getWorld().getName().equalsIgnoreCase(world)){
 			// World changed.
 			removePlayer(data);
 			return true;
 		}
 		else{
 			notifyActive(Archer.msgStart + data.playerName + " still in contest: " + name);
 			return false;
 		}
 	}
 
 	/**
 	 * 
 	 * @param data
 	 * @return true if the player was removed from the contest (does not remove ContestData from PlayerData).
 	 */
 	public boolean onPlayerLeaveServer(final PlayerData data) {
 		// TODO: Broadcast to the remaining active players.
 		if (allowDisconnect.nonzero()){
 			return false;
 		}
 		else {
 			removePlayer(data);
 			return true;
 		}
 	}
 
 	public void onPlayerDataExpire(PlayerData data) {
 		// TODO: Broadcast to the remaining active players.
 		removePlayer(data);
 	}
 
 	public boolean onPlayerChangedWorld(final PlayerData data, final Location to) {
 		// TODO: Broadcast to the remaining active players.
 		if (allowWorldChange.nonzero()){
 			return false;
 		}
 		else {
 			removePlayer(data);
 			return true;
 		}
 	}
 
 	/**
 	 * Return false if the player already is in the contest.
 	 * @param data
 	 * @param loc
 	 * @return
 	 */
 	public boolean isAvailable(final PlayerData data, final Location loc) {
 		// Assuming the world has been checked before...
 		if (started && !allowLateJoin.nonzero()) return false;
 		// TODO: Might change to 
 		return !data.activeContests.containsKey(name.toLowerCase());
 	}
 
 	public void clear() {
 		activePlayers.clear();
 	}
 
 	/**
 	 * This adds a new ContestData to the PlayerData and registers the player here.
 	 * @param data
 	 */
 	public void addPlayer(PlayerData data) {
 		notifyActive(Archer.msgStart + data.playerName + " joined contest: " + name);
 		// TODO: Set active.
 		activePlayers.put(data.playerName.toLowerCase(), data);
 		final ContestData cd = new ContestData(this);
 		if (maxShots.nonzero()) cd.shotsLeft = maxShots.getInt();
 		data.activeContests.put(name.toLowerCase(), cd);
 		checkState();
 	}
 	
 	/**
 	 * Start if ready, message players.
 	 * @return true if started (already or just).
 	 */
 	public boolean checkState() {
 		if (activePlayers.isEmpty()){
 			if (started) endContest(null);
 			return false;
 		}
 		if (started){
 			// Check max duration.
 			if (maxTime.value > 0.0){
 				final long time = System.currentTimeMillis();
 				if (time < lastTimeValid){
 					endContest("System time inconsistency.");
 					return false;
 				}
 				if (time - lastTimeValid > maxTime.value){
 					endContest("Time ran up!");
 					return false;
 				}
 			}
 			return true;
 		}
 		if (minPlayers.nonzero() && minPlayers.value > activePlayers.size()){
 			lastTimeValid = 0;
 			return false;
 		}
 		final long time = System.currentTimeMillis();
 		if (lastTimeValid == 0) lastTimeValid = time;
 		if (time >= lastTimeValid && startDelay.nonzero() && time - lastTimeValid < startDelay.value){
 			return false;
 		}
 		started = true;
		Bukkit.getServer().broadcastMessage(Archer.msgStart + ChatColor.YELLOW + "Contest " + ChatColor.GREEN + name + ChatColor.YELLOW + " starts with players: " + Utils.joinObjects(getOnlineNameList(), ChatColor.DARK_GRAY + ", "));
 		notifyActive(Archer.msgStart + ChatColor.YELLOW + "Contest started: " + ChatColor.GREEN + name);
 		return true;
 	}
 
 	/**
 	 * This will not alter data, but might alter all other players data if the contest ends by this (!).
 	 * @param data
 	 * @return If previously contained.
 	 */
 	public boolean removePlayer(final PlayerData data) {
 		return removePlayer(data, true);
 	}
 
 	/**
 	 * This will not alter data, but might alter all other players data if the contest ends by this (!).
 	 * @param data
 	 * @param notify If to notify remaining active players.
 	 * @return If previously contained.
 	 */
 	public boolean removePlayer(final PlayerData data, boolean notify) {
 		// TODO: Message other active players.
 		// TODO: Set finished.
 		boolean was = activePlayers.remove(data.playerName.toLowerCase()) != null;
 		if (notify){
 			notifyActive(Archer.msgStart + data.playerName + " left contest: " + ChatColor.RED + name + ChatColor.GRAY + ", still in: " + Utils.joinObjects(getOnlineNameList(), ChatColor.DARK_GRAY + ", "));
 		}
 		if (started && minPlayers.nonzero() && minPlayers.value > activePlayers.size()){
 			endContest(null);
 		}
 		sendSummary(data, data.activeContests.get(name.toLowerCase()));
 		return was;
 	}
 
 	private void sendSummary(final PlayerData data, final ContestData cd) {
 		if (cd == null || !cd.interesting() || data.player == null || !data.player.isOnline()){
 			return;
 		}
 		final StringBuilder b = new StringBuilder(200);
 		b.append(Archer.msgStart);
 		b.append("Contest " + ChatColor.YELLOW + name + ChatColor.GRAY + " summary: ");
 		b.append(cd.hitsDealt + " hits (" + cd.hitsTaken +" taken) | ");
 		if (lossScore.nonzero()){
 			b.append(((int) Math.round(cd.score)) + " score (" + ((int) Math.round(cd.scoreSuffered)) + " taken) | ");
 		}
 		if (cd.kills > 0){
 			b.append(cd.kills + " kills | ");
 		}
 		b.append(cd.shotsFired + " shots fired");
 		data.player.sendMessage(b.toString());
 	}
 
 	/**
 	 * 
 	 * @param message If set to null, some standard checks will be done if someone remaining has won.
 	 */
 	public void endContest(String message) {
 		boolean broadCast = message != null;
 		if (message == null){
 			// Might check other stuff (max score or so).
 			if (!activePlayers.isEmpty() && minPlayers.nonzero() && minPlayers.value > activePlayers.size()){
 				if (winMinPlayers.nonzero()){
 					final List<String> names = getOnlineNameList();
 					if (names.isEmpty()) message = "No winners.";
 					else{
 						message = "Winners: " + Utils.joinObjects(names, ", ");
 						broadCast = true;
 					}
 				}
 				else{
 					message = "Not enough players left.";
 				}
 			}
 		}
 		message = Archer.msgStart + ChatColor.YELLOW + "Contest " + name + " ended" + (message == null ? "." : "! " + message);
 		if (broadCast){
 			Bukkit.getServer().broadcastMessage(message);
 		}
 		else{
 			notifyActive(message);
 		}
 		// Remove players without side effects
 		for (final PlayerData data : activePlayers.values()){
 			sendSummary(data, data.activeContests.remove(name.toLowerCase()));
 		}
 		activePlayers.clear();
 		this.lastTimeValid = 0;
 		this.started = false;
 	}
 
	/**
	 * Does add color.
	 * @return
	 */
 	public List<String> getOnlineNameList() {
 		final List<String> names = new ArrayList<String>(activePlayers.size());
 		for (final PlayerData data : activePlayers.values()){
 			names.add(((data.player != null && data.player.isOnline()) ? ChatColor.WHITE : ChatColor.GRAY) + data.playerName);
 		}
 		Collections.sort(names, String.CASE_INSENSITIVE_ORDER); // TODO: Might re-think, size dependent?
 		return names;
 	}
 
 	/**
 	 * Send notification message to all active players of this contest.
 	 * @param string
 	 */
 	public void notifyActive(final String message) {
 		for (final PlayerData data : activePlayers.values()){
 			if (data.player != null && data.player.isOnline()){
 				data.player.sendMessage(message);
 			}
 		}
 	}
 
 	@Override
 	public int compareTo(final Contest other) {
 		return name.toLowerCase().compareTo(other.name.toLowerCase());
 	}
 
 	@Override
 	public String toString() {
 		// TODO: temp ?
 		return name;
 	}
 
 	/**
 	 * Process a projectile launch.
 	 * @param data
 	 * @param cd
 	 * @param launchLoc
 	 * @return If the player was removed from the contest.
 	 */
 	public boolean addLaunch(final PlayerData data, final ContestData cd, final LaunchSpec launchSpec) {
 		if (!started) return false;
 		cd.shotsFired ++;
 		if (maxShots.value > 0.0){
 			cd.shotsLeft --;
 		}
 		return removeIfInvalid(data, cd, launchSpec);
 	}
 
 	public boolean removeIfInvalid(final PlayerData data, final ContestData cd, final LaunchSpec launchSpec)
 	{
 		boolean remove = false;
 		if (maxShots.value > 0.0){
 			if (cd.shotsLeft <= 0){
 				remove = true;
 			}
 		}
 		if (remove){
 			removePlayer(data);
 			if (data.player != null) Archer.send(data.player, ChatColor.YELLOW + "Contest finished: " + name);
 		}
 		return remove;
 	}
 
 	/**
 	 * Does not message the player if removed, but others. Might end the contest.
 	 * @param data
 	 * @param cd
 	 * @param killer May be null.
 	 * @return If cs has to be removed from data.
 	 */
 	public boolean onPlayerDeath(final PlayerData data, final ContestData cd) {
 		if (lossDeath.nonzero()){
 			removePlayer(data);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param data
 	 * @param cd
 	 * @param distance
 	 * @param damagedData
 	 * @param damagedCd
 	 * @return
 	 */
 	public HitResult onHit(PlayerData data, ContestData cd, double distance, PlayerData damagedData, ContestData damagedCd)
 	{
 		if (!started) return HitResult.NOT_HIT;
 		if (minDistance.nonzero() && distance < minDistance.value){
 			// TODO: Might check loss by number of shots.
 			return HitResult.NOT_HIT;
 		}
 		// TODO: Message ...
 		double score = distance;
 		cd.hitsDealt ++;
 		cd.score += score;
 		damagedCd.hitsTaken ++;
 		damagedCd.scoreSuffered += score;
 		// TODO: Implement more ...
 		boolean kill = false;
 		if (lossHits.nonzero() && damagedCd.hitsTaken >= lossHits.value){
 			kill = true;
 		}
 		if (lossScore.nonzero() && damagedCd.scoreSuffered >= lossScore.value){
 			kill = true;
 		}
 		if (kill){
 			cd.kills ++;
 			if (bonusShotsKill.nonzero()){
 				cd.shotsLeft += bonusShotsKill.getInt();
 			}
 		}
 		if (bonusShotsHit.nonzero()){
 			cd.shotsLeft += bonusShotsHit.getInt();
 		}
 		
 		boolean win = false;
 //		if (winKills.nonzero() && cd.kills >= winKills.getInt()){
 //			win = true;
 //		}
 		boolean loss = false;
 		if (win){
 			// TODO: process win.
 		}
 		if (started && kill){
 			// Remove hit player.
 			if (damagedData.player != null && damagedData.player.isOnline()){
 				Archer.send(damagedData.player, "Shot by " + data.playerName + ", ends contest: " + name);
 			}
 			removePlayer(damagedData);
 			damagedData.activeContests.remove(name.toLowerCase());
 		}
 		if (maxShots.nonzero() && cd.shotsFired >= maxShots.value){
 			loss = true;
 		}
 		if (started && loss){
 			// Remove shooter.
 			removePlayer(data);
 		}
 		else{
 			loss = false;
 		}
 		return loss || !started ? HitResult.HIT_FINISHED : HitResult.HIT;
 	}
 	
 	
 	
 	// Player hit Player (dataShoot, dataHit)
 	
 }
