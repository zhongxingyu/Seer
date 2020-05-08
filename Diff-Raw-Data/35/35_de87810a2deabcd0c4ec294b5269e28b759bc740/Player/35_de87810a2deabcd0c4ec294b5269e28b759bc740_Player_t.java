 package fr.debnet.ircrpg.models;
 
 import fr.debnet.ircrpg.enums.Activity;
 import fr.debnet.ircrpg.enums.Model;
 import fr.debnet.ircrpg.enums.Status;
 import fr.debnet.ircrpg.Config;
 import fr.debnet.ircrpg.interfaces.IEntity;
 import fr.debnet.ircrpg.interfaces.MappedEntity;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Set;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.Temporal;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 import org.hibernate.annotations.Index;
 import org.hibernate.annotations.LazyCollection;
 import org.hibernate.annotations.LazyCollectionOption;
 
 /**
  * Player
  * @author Marc
  */
 @Entity
 public class Player extends MappedEntity implements IEntity {
     
     private Long id;
     private Integer version;
     private String username;
     private String password;
     private String nickname;
     private String hostname;
     private Boolean online;
     private Boolean admin;
     private Activity activity;
     private Status status;
     private Integer level;
     private Double experience;
     private Integer experienceRequired;
     private Double currentHealth;
     private Integer maxHealth;
     private Double currentMana;
     private Integer maxMana;
     private Integer attack;
     private Integer defense;
     private Double gold;
     private Integer skillPoints;
     private Integer healthPotions;
     private Integer manaPotions;
     private Integer remedyPotions;
     private Long activityDuration;
     private Long statusDuration;
     private Calendar lastUpdate;
     private Calendar lastEvent;
     private Set<Item> items;
     private Set<Spell> spells;
     
     // Statistics
     private Long timeIngame;
     private Long timeWorking;
     private Long timeResting;
     private Long timeTraining;
     private Integer moneySpent;
     private Double moneyStolen;
     private Double damageTaken;
     private Double damageGiven;
     private Integer deaths;
     private Integer kills;
 
     public Player() {
         this.initialize();
     }
     
     private void initialize() {
         this.setId(0l);
         this.setVersion(0);
         this.setUsername(null);
         this.setPassword(null);
         this.setNickname(null);
         this.setHostname(null);
         this.setOnline(false);
         this.setAdmin(false);
         this.setActivity(Activity.NONE);
         this.setStatus(Status.NORMAL);
         this.setLevel(1);
         this.setExperience(0d);
         this.setExperienceRequired(Config.RATE_LEVEL);
         this.setMaxHealth(Config.START_HEALTH);
         this.setCurrentHealth((double) this.getMaxHealth());
         this.setMaxMana(Config.START_MANA);
         this.setCurrentMana((double) this.getMaxMana());
         this.setAttack(Config.START_ATTACK);
         this.setDefense(Config.START_DEFENSE);
         this.setGold((double) Config.START_GOLD);
         this.setSkillPoints(0);
         this.setHealthPotions(0);
         this.setManaPotions(0);
         this.setRemedyPotions(0);
         this.setActivityDuration(0l);
         this.setStatusDuration(0l);
         this.setLastUpdate(Calendar.getInstance());
         this.setLastEvent(Calendar.getInstance());
         this.setItems(new HashSet<Item>());
         this.setSpells(new HashSet<Spell>());
         
         this.setTimeIngame(0l);
         this.setTimeWorking(0l);
         this.setTimeResting(0l);
         this.setTimeTraining(0l);
         this.setMoneySpent(0);
         this.setMoneyStolen(0d);
         this.setDamageTaken(0d);
         this.setDamageGiven(0d);
         this.setDeaths(0);
         this.setKills(0);
     }
     
     @Override
     @Transient
     public Model getModel() {
         return Model.PLAYER;
     }
     
     @Override
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     public Long getId() {
         return id;
     }
 
     @Override
     public void setId(Long id) {
         this.id = id;
         this.set("id", this.id);
     }
 
     @Override
     @Version
     public Integer getVersion() {
         return version;
     }
 
     @Override
     public void setVersion(Integer version) {
         this.version = version;
         this.set("version", this.version);
     }
 
     @Column(unique = true, nullable = false)
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
         this.set("username", this.username);
     }
 
     @Column(nullable = false)
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
         this.set("password", this.password);
     }
 
     @Column(nullable = false)
     public String getNickname() {
         return nickname;
     }
 
     public void setNickname(String nickname) {
         this.nickname = nickname;
         this.set("nickname", this.nickname);
     }
 
     @Column(nullable = false)
     public String getHostname() {
         return hostname;
     }
 
     public void setHostname(String hostname) {
         this.hostname = hostname;
         this.set("hostname", this.hostname);
     }
 
     @Column(nullable = false)
     public Boolean getAdmin() {
         return admin;
     }
 
     public void setAdmin(Boolean admin) {
         this.admin = admin;
         this.set("admin", this.admin);
     }
 
     @Column(nullable = false)
     public Boolean getOnline() {
         return online;
     }
 
     public void setOnline(Boolean online) {
         this.online = online;
         this.set("online", this.online);
     }
 
     @Column(nullable = false)
     @Enumerated(EnumType.STRING)
     public Activity getActivity() {
         return activity;
     }
 
     public void setActivity(Activity activity) {
         this.activity = activity;
         this.set("activity", this.activity);
     }
 
     @Column(nullable = false)
     @Enumerated(EnumType.STRING)
     public Status getStatus() {
         return status;
     }
 
     public void setStatus(Status status) {
         this.status = status;
         this.set("status", this.status);
     }
 
     @Column(nullable = false)
     public Integer getLevel() {
         return level;
     }
 
     public void setLevel(Integer level) {
         this.level = level;
         this.set("level", this.level);
     }
 
     public void addLevel(Integer level) {
         this.level += level;
         this.set("level", this.level);
     }
 
     @Column(nullable = false)
     public Double getExperience() {
         return experience;
     }
 
     public void setExperience(Double experience) {
         this.experience = experience;
         this.set("experience", this.experience);
     }
     
     public void addExperience(Double experience) {
         this.experience += experience;
         this.set("experience", this.experience);
     }
 
     @Column(nullable = false)
     public Integer getExperienceRequired() {
         return experienceRequired;
     }
 
     public void setExperienceRequired(Integer experienceRequired) {
         this.experienceRequired = experienceRequired;
         this.set("experienceRequired", this.experienceRequired);
     }
     
     @Column(nullable = false)
     public Double getCurrentHealth() {
         return currentHealth;
     }
 
     public void setCurrentHealth(Double currentHealth) {
         this.currentHealth = currentHealth;
         this.set("currentHealth", this.currentHealth);
     }
 
     public void addHealth(Double health) {
         this.currentHealth += health;
         this.set("currentHealth", this.currentHealth);
     }
 
     @Column(nullable = false)
     public Integer getMaxHealth() {
         return maxHealth;
     }
 
     public void setMaxHealth(Integer maxHealth) {
         this.maxHealth = maxHealth;
         this.set("maxHealth", this.maxHealth);
     }
 
     public void addMaxHealth(Integer maxHealth) {
         this.maxHealth += maxHealth;
         this.set("maxHealth", this.maxHealth);
     }
 
     @Column(nullable = false)
     public Double getCurrentMana() {
         return currentMana;
     }
 
     public void setCurrentMana(Double currentMana) {
         this.currentMana = currentMana;
         this.set("currentMana", this.currentMana);
     }
     
     public void addMana(Double mana) {
         this.currentMana += mana;
         this.set("currentMana", this.currentMana);
     }
 
     @Column(nullable = false)
     public Integer getMaxMana() {
         return maxMana;
     }
 
     public void setMaxMana(Integer maxMana) {
         this.maxMana = maxMana;
         this.set("maxMana", this.maxMana);
     }
 
     public void addMaxMana(Integer maxMana) {
         this.maxMana += maxMana;
         this.set("maxMana", this.maxMana);
     }
 
     @Column(nullable = false)
     public Integer getAttack() {
         return attack;
     }
 
     public void setAttack(Integer attack) {
         this.attack = attack;
         this.set("attack", this.attack);
     }
 
     public void addAttack(Integer attack) {
         this.attack += attack;
         this.set("attack", this.attack);
     }
 
     @Column(nullable = false)
     public Integer getDefense() {
         return defense;
     }
 
     public void setDefense(Integer defense) {
         this.defense = defense;
         this.set("defense", this.defense);
     }
 
     public void addDefense(Integer defense) {
         this.defense += defense;
         this.set("defense", this.defense);
     }
 
     @Column(nullable = false)
     public Double getGold() {
         return gold;
     }
 
     public void setGold(Double gold) {
         this.gold = gold;
         this.set("gold", this.gold);
     }
 
     public void addGold(Double gold) {
         this.gold += gold;
         this.set("gold", this.gold);
     }
 
     @Column(nullable = false)
     public Integer getSkillPoints() {
         return skillPoints;
     }
 
     public void setSkillPoints(Integer skillPoints) {
         this.skillPoints = skillPoints;
         this.set("skillPoints", this.skillPoints);
     }
 
     public void addSkillPoints(Integer skillPoints) {
         this.skillPoints += skillPoints;
         this.set("skillPoints", this.skillPoints);
     }
 
     @Column(nullable = false)
     @Index(name = "player_lastupdate")
     @Temporal(javax.persistence.TemporalType.TIMESTAMP)
     public Calendar getLastUpdate() {
         return lastUpdate;
     }
 
     public void setLastUpdate(Calendar lastUpdate) {
         this.lastUpdate = lastUpdate;
         this.set("lastUpdate", this.lastUpdate);
     }
 
     @Column(nullable = false)
     @Temporal(javax.persistence.TemporalType.TIMESTAMP)
     public Calendar getLastEvent() {
         return lastEvent;
     }
 
     public void setLastEvent(Calendar lastEvent) {
         this.lastEvent = lastEvent;
         this.set("lastEvent", this.lastEvent);
     }
 
     @Column(nullable = false)
     public Integer getHealthPotions() {
         return healthPotions;
     }
 
     public void setHealthPotions(Integer healthPotions) {
         this.healthPotions = healthPotions;
         this.set("healthPotions", this.healthPotions);
     }
 
     public void addHealthPotions(Integer healthPotions) {
         this.healthPotions += healthPotions;
         this.set("healthPotions", this.healthPotions);
     }
 
     @Column(nullable = false)
     public Integer getManaPotions() {
         return manaPotions;
     }
 
     public void setManaPotions(Integer manaPotions) {
         this.manaPotions = manaPotions;
         this.set("manaPotions", this.manaPotions);
     }
 
     public void addManaPotions(Integer manaPotions) {
         this.manaPotions += manaPotions;
         this.set("manaPotions", this.manaPotions);
     }
 
     @Column(nullable = false)
     public Integer getRemedyPotions() {
         return remedyPotions;
     }
 
     public void setRemedyPotions(Integer remedyPotions) {
         this.remedyPotions = remedyPotions;
         this.set("remedyPotions", this.remedyPotions);
     }
 
     public void addRemedyPotions(Integer remedyPotions) {
         this.remedyPotions += remedyPotions;
         this.set("remedyPotions", this.remedyPotions);
     }
 
    @ManyToMany(cascade = CascadeType.ALL)
     @LazyCollection(LazyCollectionOption.FALSE)
     public Set<Item> getItems() {
         return items;
     }
 
     public void setItems(Set<Item> items) {
         this.items = items;
         this.set("items", this.items);
     }
 
     public void addItem(Item item) {
         this.items.add(item);
         this.set("items", this.items);
     }
 
     public void removeItem(Item item) {
         this.items.remove(item);
         this.set("items", this.items);
     }
     
    @ManyToMany(cascade = CascadeType.ALL)
     @LazyCollection(LazyCollectionOption.FALSE)
     public Set<Spell> getSpells() {
         return spells;
     }
 
     public void setSpells(Set<Spell> spells) {
         this.spells = spells;
         this.set("spells", this.spells);
     }
 
     public void addSpell(Spell spell) {
         this.spells.add(spell);
         this.set("spells", this.spells);
     }
 
     public void removeSpell(Spell spell) {
         this.spells.remove(spell);
         this.set("spells", this.spells);
     }
 
     @Column(nullable = false)
     public Long getActivityDuration() {
         return activityDuration;
     }
 
     public void setActivityDuration(Long activityDuration) {
         this.activityDuration = activityDuration;
         this.set("activityDuration", this.activityDuration);
         this.set("activityTime", new Time(this.activityDuration));
     }
 
     public void addActivityDuration(Long activityDuration) {
         this.activityDuration += activityDuration;
         this.set("activityDuration", this.activityDuration);
         this.set("activityTime", new Time(this.activityDuration));
     }
 
     @Column(nullable = false)
     public Long getStatusDuration() {
         return statusDuration;
     }
 
     public void setStatusDuration(Long statusDuration) {
         this.statusDuration = statusDuration;
         this.set("statusDuration", this.statusDuration);
         this.set("statusTime", new Time(this.statusDuration));
     }
 
     public void addStatusDuration(Long statusDuration) {
         this.statusDuration += statusDuration;
         this.set("statusDuration", this.statusDuration);
         this.set("statusTime", new Time(this.statusDuration));
     }
 
     @Column(nullable = false)
     public Long getTimeIngame() {
         return timeIngame;
     }
 
     public void setTimeIngame(Long timeIngame) {
         this.timeIngame = timeIngame;
         this.set("timeIngame", new Time(this.timeIngame));
     }
 
     public void addTimeIngame(Long timeIngame) {
         this.timeIngame += timeIngame;
         this.set("timeIngame", new Time(this.timeIngame));
     }
 
     @Column(nullable = false)
     public Long getTimeWorking() {
         return timeWorking;
     }
 
     public void setTimeWorking(Long timeWorking) {
         this.timeWorking = timeWorking;
         this.set("timeWorking", new Time(this.timeWorking));
     }
 
     public void addTimeWorking(Long timeWorking) {
         this.timeWorking += timeWorking;
         this.set("timeWorking", new Time(this.timeWorking));
     }
 
     @Column(nullable = false)
     public Long getTimeResting() {
         return timeResting;
     }
 
     public void setTimeResting(Long timeResting) {
         this.timeResting = timeResting;
         this.set("timeResting", new Time(this.timeResting));
     }
 
     public void addTimeResting(Long timeResting) {
         this.timeResting += timeResting;
         this.set("timeResting", new Time(this.timeResting));
     }
 
     @Column(nullable = false)
     public Long getTimeTraining() {
         return timeTraining;
     }
 
     public void setTimeTraining(Long timeTraining) {
         this.timeTraining = timeTraining;
         this.set("timeTraining", new Time(this.timeTraining));
     }
 
     public void addTimeTraining(Long timeTraining) {
         this.timeTraining += timeTraining;
         this.set("timeTraining", new Time(this.timeTraining));
     }
 
     @Column(nullable = false)
     public Integer getMoneySpent() {
         return moneySpent;
     }
 
     public void setMoneySpent(Integer moneySpent) {
         this.moneySpent = moneySpent;
         this.set("moneySpent", this.moneySpent);
     }
 
     public void addMoneySpent(Integer moneySpent) {
         this.moneySpent += moneySpent;
         this.set("moneySpent", this.moneySpent);
     }
 
     @Column(nullable = false)
     public Double getMoneyStolen() {
         return moneyStolen;
     }
 
     public void setMoneyStolen(Double moneyStolen) {
         this.moneyStolen = moneyStolen;
         this.set("moneyStolen", this.moneyStolen);
     }
 
     public void addMoneyStolen(Double moneyStolen) {
         this.moneyStolen += moneyStolen;
         this.set("moneyStolen", this.moneyStolen);
     }
 
     @Column(nullable = false)
     public Double getDamageTaken() {
         return damageTaken;
     }
 
     public void setDamageTaken(Double damageTaken) {
         this.damageTaken = damageTaken;
         this.set("damageTaken", this.damageTaken);
     }
 
     public void addDamageTaken(Double damageTaken) {
         this.damageTaken += damageTaken;
         this.set("damageTaken", this.damageTaken);
     }
 
     @Column(nullable = false)
     public Double getDamageGiven() {
         return damageGiven;
     }
 
     public void setDamageGiven(Double damageGiven) {
         this.damageGiven = damageGiven;
         this.set("damageGiven", this.damageGiven);
     }
 
     public void addDamageGiven(Double damageGiven) {
         this.damageGiven += damageGiven;
         this.set("damageGiven", this.damageGiven);
     }
 
     @Column(nullable = false)
     public Integer getDeaths() {
         return deaths;
     }
 
     public void setDeaths(Integer deaths) {
         this.deaths = deaths;
         this.set("deaths", this.deaths);
     }
 
     public void addDeaths(Integer deaths) {
         this.deaths += deaths;
         this.set("deaths", this.deaths);
     }
 
     @Column(nullable = false)
     public Integer getKills() {
         return kills;
     }
 
     public void setKills(Integer kills) {
         this.kills = kills;
         this.set("kills", this.kills);
     }
 
     public void addKills(Integer kills) {
         this.kills += kills;
         this.set("kills", this.kills);
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 67 * hash + (this.username != null ? this.username.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Player other = (Player) obj;
         if ((this.username == null) ? (other.username != null) : !this.username.equals(other.username)) {
             return false;
         }
         return true;
     }
     
     @Override
     public String toString() {
         return String.format("%s (%s)", this.nickname, this.username);
     }
     
     @Override
     public void updateMapping() {
         Modifiers modifiers = new Modifiers(this);
         this.set("maxHealthCalculated", this.maxHealth + modifiers.getHealth());
         this.set("maxManaCalculated", this.maxMana + modifiers.getMana());
         this.set("attackCalculated", this.attack + modifiers.getAttack());
         this.set("defenseCalculated", this.defense + modifiers.getDefense());
     }
     
     /* Specific methods */
     
     public void addHealth(int health) {
         this.currentHealth += health;
         this.set("currentHealth", this.currentHealth);
     }
 
     public void addMana(int mana) {
         this.currentMana += mana;
         this.set("currentMana", this.currentMana);
     }
 
     public void addExperience(int experience) {
         this.experience += experience;
         this.set("experience", this.experience);
     }
     
     public void addGold(int gold) {
         this.gold += gold;
         this.set("gold", this.gold);
     }
 }
