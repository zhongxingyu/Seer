 package org.pvv.larschri.gameday;
 
 import java.io.IOException;
 import java.net.URL;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBException;
 
import org.pvv.larschri.gameday.xml.Grid;
 import org.pvv.larschri.gameday.xml.Grid.Game;
 import org.pvv.larschri.gameday.xml.InningAll;
 import org.pvv.larschri.gameday.xml.InningAll.Inning;
 import org.pvv.larschri.gameday.xml.InningAll.Inning.HalfInning;
 import org.pvv.larschri.gameday.xml.InningAll.Inning.HalfInning.Atbat;
 import org.pvv.larschri.gameday.xml.InningAll.Inning.HalfInning.Atbat.Pitch;
 import org.pvv.larschri.gameday.xml.Players;
 import org.pvv.larschri.gameday.xml.Players.Team.Player;
 
 /**
  * Class for calculating stats per {@link Pitch}. It can be used to build a
  * matrix of {@link Pitch}es and {@link PitchColumn}s.
  */
 public class PitchMatrix {
 
 	/**
 	 * Interface used to retrieve a value for a {@link Pitch}.
 	 */
	interface PitchColumn<X> {
		X get(Pitch pitch);
 	}
 
 	public static final PitchColumn<Double> START_SPEED = new PitchColumn<Double> () {
 		@Override public Double get(Pitch pitch) {
 			return pitch.startSpeed;
 		}
 	};
 
 	/**
 	 * Found at http://pitchfx.texasleaguers.com/league-averages.php. Not sure
 	 * if all of these are in actual use or if any is missing.
 	 */
	enum PitchType {
 		FA, // Fastball
 		FF, // 4-seam Fastball
 		FT, // 2-seam Fastball
 		FC, // Cut Fastball
 		FS, // Split-finger Fastball
 		FO, // Forkball
 		SI, // Sinker
 		SL, // Slider
 		CU, // Curveball
 		KC, // Knuckle Curve
 		EP, // Ephuus
 		CH, // Change-up
 		SC, // Screwball
 		KN, // Knuckleball
 		UN, // Unknown
 	}
 
 	public static final PitchColumn<PitchType> PITCH_TYPE = new PitchColumn<PitchType> () {
 		@Override public PitchType get(Pitch pitch) {
			return PitchType.valueOf(pitch.pitchType);
 		}
 	};
 
 	enum Type {
 		B, // ball
 		S, // strike
 		X, // in play
 	}
 
 	public static final PitchColumn<Type> TYPE = new PitchColumn<Type> () {
 		@Override public Type get(Pitch pitch) {
 			return Type.valueOf(pitch.type);
 		}
 	};
 
 
 	private class PlayerStat {
 		final PitchColumn<Player> player;
 
 		PlayerStat(PitchColumn<Player> player) {
 			this.player = player;
 		}
 
 		PitchColumn<Integer> teamId = new PitchColumn<Integer> () {
 			@Override public Integer get(Pitch pitch) {
 				return player.get(pitch).teamId;
 			}
 		};
 
 		private Double parse(String s) {
 			try {
 				return Double.valueOf(s);
 			} catch (NumberFormatException e) {
 				return Double.NaN;
 			}
 		}
 
 		PitchColumn<Double> era = new PitchColumn<Double> () {
 			@Override public Double get(Pitch pitch) {
 				return parse(pitcherStats.get(pitch).era);
 			}
 		};
 
 		PitchColumn<Double> avg = new PitchColumn<Double> () {
 			@Override public Double get(Pitch pitch) {
 				return batterStats.get(pitch).avg;
 			}
 		};
 	}
 
 	private final List<Map<Pitch, ?>> allMaps = new ArrayList<>();
 
 	/**
 	 * {@link PitchColumn} for stuff that depends on more than a single
 	 * {@link Pitch}, so the stats are stored in a map. Other {@link PitchColumn}
 	 * can depend on a {@link MapStat}.
 	 */
 	class MapStat<X> implements PitchColumn<X> {
 		private final Map<Pitch, X> map = new HashMap<>();
 		{ allMaps.add(map); }
 
 		@Override public X get(Pitch pitch) {
 			return map.get(pitch);
 		}
 	}
 
 	private final MapStat<Atbat> atbatStats = new MapStat<>();
 	private final MapStat<HalfInning> halfInningStats = new MapStat<>();
 	private final MapStat<Integer> pitchCountStats = new MapStat<>();
 	private final MapStat<Integer> ballCountStats = new MapStat<>();
 	private final MapStat<Integer> batOrderStats = new MapStat<>();
 	private final MapStat<Integer> strikeCountStats = new MapStat<>();
 	private final MapStat<Player> batterStats = new MapStat<>();
 	private final MapStat<Player> pitcherStats = new MapStat<>();
 	private final PlayerStat batterStat = new PlayerStat(batterStats);
 	private final PlayerStat pitcherStat = new PlayerStat(pitcherStats);
 
 	private final List<Pitch> pitches = new ArrayList<>();
 
 	public void load(Game game, Downloader downloader) throws IOException, JAXBException {
 		List<List<HalfInning>> topsAndBottoms = toTopsAndBottoms(downloader.inningAll(game));
 		Map<Integer, Player> players = toPlayerMap(downloader.players(game));
 		load(topsAndBottoms.get(0), players);
 		load(topsAndBottoms.get(1), players);
 	}
 
 	/** @param halfInningList {@link List} containing all of either {@link Inning#top} or {@link Inning#bottom} from a full {@link Game}. */
 	private void load(List<HalfInning> halfInningList, Map<Integer, Player> players) {
 		int pitchCount = 0;
 		int pitcher = -1;
 		int batCount = 0;
 
 		for (HalfInning halfInning : halfInningList) {
 			for (Atbat atbat : halfInning.atbat) {
 				if (atbat.pitcher != pitcher) {
 					pitcher = atbat.pitcher;
 					pitchCount = 0;
 				}
 				pitches.addAll(atbat.pitch);
 				for (Pitch pitch : atbat.pitch) {
 					atbatStats.map.put(pitch, atbat);
 					halfInningStats.map.put(pitch, halfInning);
					pitchCountStats.map.put(pitch, pitchCount);
 					batOrderStats.map.put(pitch, batCount % 9);
 					batterStats.map.put(pitch, players.get(atbat.batter));
 					pitcherStats.map.put(pitch, players.get(atbat.pitcher));
 				}
 
 				int ballCount = 0, strikeCount = 0;
 				for (Pitch pitch : atbat.pitch) {
 					strikeCountStats.map.put(pitch, Math.min(strikeCount, 2));
 					ballCountStats.map.put(pitch, ballCount);
 
 					switch(TYPE.get(pitch)) {
 						case B: ballCount++; break;
 						case S: strikeCount++; break;
 						case X: break;
 					}
 				}
 
 				batCount++;
 			}
 		}
 	}
 
 	/** @return all values for the given {@link PitchColumn} */
 	<X> List<X> getAll(PitchColumn<X> stat) {
 		List<X> list = new ArrayList<>();
 		for (Pitch p : pitches)
 			list.add(stat.get(p));
 		return list;
 	}
 
 	public PitchColumn<Double> getPitcherERA() { return pitcherStat.era; }
 	public PitchColumn<Double> getBatterAVG() { return batterStat.avg; }
 	public PitchColumn<Integer> getPitcherTeam() { return pitcherStat.teamId; }
 	public PitchColumn<Integer> getBatterTeam() { return batterStat.teamId; }
 	public PitchColumn<Double> getPitchSpeed() { return START_SPEED; }
 	public PitchColumn<PitchType> getPitchType() { return PITCH_TYPE; }
 	public PitchColumn<Type> getType() { return TYPE; }
	public PitchColumn<Integer> getPitcherPitchCount() { return pitchCountStats; }
 	public PitchColumn<Integer> getBallScore() { return ballCountStats; }
 	public PitchColumn<Integer> getStrikeScore() { return strikeCountStats; }
 	public PitchColumn<Player> getBatter() { return batterStats; }
 	public PitchColumn<Player> getPitcher() { return pitcherStats; }
 
 	public List<Pitch> getPitches() {
 		return Collections.unmodifiableList(pitches);
 	}
 
 	/** Helper method to organize xml data */
 	private static Map<Integer, Player> toPlayerMap(Collection<Player> players) {
 		Map<Integer, Player> playerMap = new HashMap<>();
 		for (Player player : players)
 			playerMap.put(player.id, player);
 		return playerMap;
 	}
 
 	/** Helper method to organize xml data */
 	public static Map<Integer, Player> toPlayerMap(Players players) {
 		List<Player> playerList = new ArrayList<>(players.team.get(0).player);
 		playerList.addAll(players.team.get(1).player);
 		return toPlayerMap(playerList);
 	}
 
 	/** Helper method to organize xml data */
 	public static List<List<HalfInning>> toTopsAndBottoms(InningAll inningAll) {
 		List<HalfInning> topInnings = new ArrayList<>();
 		List<HalfInning> bottomInnings = new ArrayList<>();
 		for (Inning inning : inningAll.inning) {
 			topInnings.add(inning.top);
 			if (inning.bottom != null)
 				bottomInnings.add(inning.bottom);
 		}
 		return Arrays.asList(topInnings, bottomInnings);
 	}
 
 	/** Load data for the given interval */
 	public void load(Calendar from, Calendar to, Downloader downloader) throws IOException, JAXBException {
 		for (Calendar c = (Calendar) from.clone(); c.before(to); c.add(Calendar.DAY_OF_MONTH, 1)) {
 			for (Game game : downloader.grid(c).game) {
 				if (!game.mediaState.equals("media_dead"))
 					load(game, downloader);
 			}
 		}
 	}
 
 	/** Test stuff */
 	public static void main(String[] args) throws NumberFormatException, IOException, JAXBException {
		Downloader downloader = new Downloader(Paths.get(System.getProperty("user.home") + "/.gameday"), new URL("http://statenisland.yankees.milb.com/gdcross/"));
		PitchMatrix pitchStats = new PitchMatrix();
		pitchStats.load(
				new GregorianCalendar(2012, Calendar.AUGUST, 1),
				new GregorianCalendar(2012, Calendar.SEPTEMBER, 1), downloader);
 	}
 }
