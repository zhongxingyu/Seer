 package fr.kissy.q3logparser.dto;
 
 import com.esotericsoftware.kryo.Kryo;
 import com.esotericsoftware.kryo.KryoSerializable;
 import com.esotericsoftware.kryo.io.Input;
 import com.esotericsoftware.kryo.io.Output;
 import com.google.common.base.Objects;
 import com.google.common.collect.*;
 import fr.kissy.q3logparser.dto.enums.MeanOfDeath;
 import fr.kissy.q3logparser.dto.enums.Team;
 import fr.kissy.q3logparser.dto.kill.PlayerKill;
 import fr.kissy.q3logparser.dto.kill.WeaponKill;
 import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
 import org.apache.commons.lang3.builder.ToStringStyle;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 /**
  * @author Guillaume <lebiller@fullsix.com>
  */
 public class Player implements Comparable<Player>, KryoSerializable {
     private Integer id;
     private Team team;
     private String name;
     private Integer score = 0;
     private Streak streak = new Streak();
     private Flag flag = new Flag();
     private Integer startPlaying;
 
     transient private Integer duration;
     transient private Boolean hasFlag = false;
     transient private List<Kill> frags = Lists.newArrayList();
     transient private List<Kill> deaths = Lists.newArrayList();
     transient private List<Kill> suicides = Lists.newArrayList();
     transient private Map<MeanOfDeath, WeaponKill> weaponKills = Maps.newHashMap();
     transient private Map<Player, PlayerKill> playerKills = Maps.newHashMap();
 
     public void update(Integer playerNumber, String name, Team team) {
         this.id = playerNumber;
         this.name = name;
         this.team = team;
     }
 
     public void addKill(Kill kill) {
         if (Objects.equal(kill.getTarget(), this)) {
             streak.addDeath();
             deaths.add(kill);
             if (kill.isSuicide()) {
                 suicides.add(kill);
             }
         } else if (Objects.equal(kill.getPlayer(), this)) {
             streak.addFrag();
             frags.add(kill);
 
             // Stats
             if (!weaponKills.containsKey(kill.getMeanOfDeath())) {
                 weaponKills.put(kill.getMeanOfDeath(), new WeaponKill(kill.getMeanOfDeath()));
             }
             weaponKills.get(kill.getMeanOfDeath()).addFrag();
             if (!playerKills.containsKey(kill.getTarget())) {
                  playerKills.put(kill.getTarget(), new PlayerKill(kill.getTarget()));
             }
             playerKills.get(kill.getTarget()).addFrag();
         }
 
         // Flag update
         if (Objects.equal(kill.getTarget(), this) && hasFlag) {
             //System.out.println("Lost: " + name + " " + team);
             this.hasFlag = false;
         }
     }
 
     public void captureFlag() {
         this.hasFlag = false;
         this.flag.addCaptured();
     }
 
     public void pickupFlag() {
         this.hasFlag = true;
         this.flag.addPickedUp();
     }
 
     public void returnFlag() {
         this.flag.addReturned();
     }
 
     public void processShutdownGame(Integer time) {
         this.duration = startPlaying != null ? time - startPlaying : 0;
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public Team getTeam() {
         return team;
     }
 
     public String getTeamCssClass() {
         return team.getCssClass();
     }
 
     public String getTeamName() {
         return team.getName();
     }
 
     public void setTeam(Team team) {
         this.team = team;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Integer getScore() {
         return score;
     }
 
     public void setScore(Integer score) {
         this.score = score;
     }
 
     public Streak getStreak() {
         return streak;
     }
 
     public void setStreak(Streak streak) {
         this.streak = streak;
     }
 
     public Flag getFlag() {
         return flag;
     }
 
     public void setFlag(Flag flag) {
         this.flag = flag;
     }
 
     public Boolean getHasFlag() {
         return hasFlag;
     }
 
     public void setHasFlag(Boolean hasFlag) {
         this.hasFlag = hasFlag;
     }
 
     public Integer getStartPlaying() {
         return startPlaying;
     }
 
     public void setStartPlaying(Integer startPlaying) {
         this.startPlaying = startPlaying;
     }
 
     public Integer getDuration() {
         return duration;
     }
 
     public String getFormattedDuration() {
         long minutes = TimeUnit.SECONDS.toMinutes(duration);
         return String.format("%dm %ds",minutes, TimeUnit.SECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes));
     }
 
     public void setDuration(Integer duration) {
         this.duration = duration;
     }
 
     public List<Kill> getFrags() {
         return frags;
     }
 
     public void setFrags(List<Kill> frags) {
         this.frags = frags;
     }
 
     public List<Kill> getDeaths() {
         return deaths;
     }
 
     public void setDeaths(List<Kill> deaths) {
         this.deaths = deaths;
     }
 
     public List<Kill> getSuicides() {
         return suicides;
     }
 
     public void setSuicides(List<Kill> suicides) {
         this.suicides = suicides;
     }
 
     public Map<MeanOfDeath, WeaponKill> getWeaponKills() {
         return weaponKills;
     }
 
     public List<WeaponKill> getSortedWeaponKills() {
         // Special, sort it before
         return Ordering.natural().immutableSortedCopy(weaponKills.values());
     }
 
     public void setWeaponKills(Map<MeanOfDeath, WeaponKill> weaponKills) {
         this.weaponKills = weaponKills;
     }
 
     public Map<Player, PlayerKill> getPlayerKills() {
         return playerKills;
     }
 
     public List<PlayerKill> getSortedPlayerKills() {
         // Special, sort it before
         return Ordering.natural().immutableSortedCopy(playerKills.values());
     }
 
     public void setPlayerKills(Map<Player, PlayerKill> playerKills) {
         this.playerKills = playerKills;
     }
 
     @Override
     @SuppressWarnings("ConstantConditions")
     public void write(Kryo kryo, Output output) {
         output.writeInt(id);
         kryo.writeObject(output, team);
         output.writeString(name);
         output.writeInt(score);
         kryo.writeObject(output, streak);
         kryo.writeObject(output, flag);
        output.writeInt(startPlaying == null ? -1 : startPlaying);
         kryo.writeObject(output, frags);
         kryo.writeObject(output, deaths);
         kryo.writeObject(output, suicides);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void read(Kryo kryo, Input input) {
         id = input.readInt();
         team = kryo.readObject(input, Team.class);
         name = input.readString();
         score = input.readInt();
         streak = kryo.readObject(input, Streak.class);
         flag = kryo.readObject(input, Flag.class);
        int startPlayingRead = input.readInt();
        startPlaying = startPlayingRead == -1 ? null : startPlayingRead;
         frags = kryo.readObject(input, ArrayList.class);
         deaths = kryo.readObject(input, ArrayList.class);
         suicides = kryo.readObject(input, ArrayList.class);
     }
 
     @Override
     public int compareTo(Player player) {
         return player.getName().compareTo(name);
     }
 
     @Override
     public String toString() {
         return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE, null, null, false, false).toString();
     }
 
     @Override
     public boolean equals(Object o) {
         if(o instanceof Player){
             final Player player = (Player) o;
             return Objects.equal(id, player.id);
         } else{
             return false;
         }
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(id);
     }
 }
