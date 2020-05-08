 package no.runsafe.UserControl.database;
 
 import no.runsafe.framework.api.IScheduler;
 import no.runsafe.framework.api.database.IDatabase;
 import no.runsafe.framework.api.database.IRow;
 import no.runsafe.framework.api.database.Repository;
 import no.runsafe.framework.api.hook.IPlayerDataProvider;
 import no.runsafe.framework.api.hook.IPlayerLookupService;
 import no.runsafe.framework.api.hook.IPlayerSessionDataProvider;
import no.runsafe.framework.api.log.IDebug;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.timer.TimedCache;
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.joda.time.PeriodType;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.PeriodFormat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.regex.Pattern;
 
 public class PlayerDatabase extends Repository
 	implements IPlayerLookupService, IPlayerDataProvider, IPlayerSessionDataProvider
 {
 	public PlayerDatabase(IDebug console, IDatabase database, IScheduler scheduler)
 	{
 		this.console = console;
 		this.database = database;
 		this.lookupCache = new TimedCache<String, List<String>>(scheduler);
 		this.dataCache = new TimedCache<String, PlayerData>(scheduler);
 	}
 
 	@Override
 	public String getTableName()
 	{
 		return "player_db";
 	}
 
 	@Override
 	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
 	{
 		HashMap<Integer, List<String>> queries = new HashMap<Integer, List<String>>();
 		ArrayList<String> sql = new ArrayList<String>();
 		sql.add(
 			"CREATE TABLE player_db (" +
 				"`name` varchar(255) NOT NULL," +
 				"`joined` datetime NOT NULL," +
 				"`login` datetime NOT NULL," +
 				"`logout` datetime NULL," +
 				"`banned` datetime NULL," +
 				"`ban_reason` varchar(255) NULL," +
 				"`ban_by` varchar(255) NULL," +
 				"`ip` int unsigned NULL," +
 				"PRIMARY KEY(`name`)" +
 				")"
 		);
 		queries.put(1, sql);
 		sql = new ArrayList<String>();
 		sql.add("ALTER TABLE player_db ADD COLUMN temp_ban datetime NULL");
 		queries.put(2, sql);
 		return queries;
 	}
 
 	public void logPlayerInfo(IPlayer player)
 	{
 		console.debugFine("Updating player_db with login time");
 		database.Update(
 			"INSERT INTO player_db (`name`,`joined`,`login`,`ip`) VALUES (?,NOW(),NOW(),INET_ATON(?))" +
 				"ON DUPLICATE KEY UPDATE `name`=VALUES(`name`), `login`=VALUES(`login`), `ip`=VALUES(`ip`)",
 			player.getName(), player.getIP()
 		);
 		dataCache.Invalidate(player.getName());
 		lookupCache.Purge();
 	}
 
 	public void logPlayerBan(IPlayer player, IPlayer banner, String reason)
 	{
 		database.Update(
 			"UPDATE player_db SET `banned`=NOW(), ban_reason=?, ban_by=? WHERE `name`=?",
 			reason, banner == null ? "console" : banner.getName(), player.getName()
 		);
 		dataCache.Invalidate(player.getName());
 	}
 
 	public void setPlayerTemporaryBan(IPlayer player, DateTime temporary)
 	{
 		database.Update("UPDATE player_db SET temp_ban=? WHERE `name`=?", temporary, player.getName());
 		dataCache.Invalidate(player.getName());
 	}
 
 	public void logPlayerUnban(IPlayer player)
 	{
 		database.Update(
 			"UPDATE player_db SET `banned`=NULL, ban_reason=NULL, ban_by=NULL, temp_ban=NULL WHERE `name`=?",
 			player.getName()
 		);
 		dataCache.Invalidate(player.getName());
 	}
 
 	public void logPlayerLogout(IPlayer player)
 	{
 		database.Update(
 			"UPDATE player_db SET `logout`=NOW() WHERE `name`=?",
 			player.getName()
 		);
 		dataCache.Invalidate(player.getName());
 	}
 
 	public PlayerData getData(IPlayer player)
 	{
 		PlayerData data = dataCache.Cache(player.getName());
 		if (data != null)
 			return data;
 
 		IRow raw = database.QueryRow("SELECT * FROM player_db WHERE `name`=?", player.getName());
 		data = new PlayerData();
 		data.setBanned(raw.DateTime("banned"));
 		data.setBanner(raw.String("ban_by"));
 		data.setBanReason(raw.String("ban_reason"));
 		data.setJoined(raw.DateTime("joined"));
 		data.setLogin(raw.DateTime("login"));
 		data.setLogout(raw.DateTime("logout"));
 		data.setUnban(raw.DateTime("temp_ban"));
 
 		return dataCache.Cache(player.getName(), data);
 	}
 
 	@Override
 	public List<String> findPlayer(String lookup)
 	{
 		if (lookup == null)
 			return null;
 
 		List<String> result = lookupCache.Cache(lookup);
 		if (result != null)
 			return result;
 		result = database.QueryStrings(
 			"SELECT name FROM player_db WHERE name LIKE ?",
 			String.format("%s%%", SQLWildcard.matcher(lookup).replaceAll("\\\\$1"))
 		);
 		return lookupCache.Cache(lookup, result);
 	}
 
 	@Override
 	public HashMap<String, String> GetPlayerData(IPlayer player)
 	{
 		PlayerData data = getData(player);
 		HashMap<String, String> result = new LinkedHashMap<String, String>();
 		if (data.getBanned() != null)
 		{
 			result.put("usercontrol.ban.status", "true");
 			result.put("usercontrol.ban.timestamp", DATE_FORMAT.print(data.getBanned()));
 			result.put("usercontrol.ban.reason", data.getBanReason());
 			if (data.getUnban() != null)
 				result.put("usercontrol.ban.temporary", DATE_FORMAT.print(data.getUnban()));
 			result.put("usercontrol.ban.by", data.getBanner());
 		}
 		else
 			result.put("usercontrol.ban.status", "false");
 		result.put("usercontrol.joined", DATE_FORMAT.print(data.getJoined()));
 		result.put("usercontrol.login", DATE_FORMAT.print(data.getLogin()));
 		result.put("usercontrol.logout", DATE_FORMAT.print(data.getLogout()));
 		if (data.getLogout() != null && data.getLogout().isAfter(data.getLogin()))
 		{
 			Period period = new Period(data.getLogout(), DateTime.now(), SEEN_FORMAT);
 			result.put("usercontrol.seen", PeriodFormat.getDefault().print(period));
 		}
 		return result;
 	}
 
 	@Override
 	public DateTime GetPlayerLogout(IPlayer player)
 	{
 		PlayerData data = getData(player);
 		if (data == null)
 			return null;
 		return data.getLogout();
 	}
 
 	@Override
 	public String GetPlayerBanReason(IPlayer player)
 	{
 		return getData(player).getBanReason();
 	}
 
 	@Override
 	public boolean IsFirstSession(IPlayer player)
 	{
 		return GetPlayerLogout(player) == null;
 	}
 
 	private final IDebug console;
 	private final IDatabase database;
 	private final PeriodType SEEN_FORMAT = PeriodType.standard().withMillisRemoved().withSecondsRemoved();
 	private final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
 	private final Pattern SQLWildcard = Pattern.compile("([%_])");
 	private final TimedCache<String, List<String>> lookupCache;
 	private final TimedCache<String, PlayerData> dataCache;
 }
