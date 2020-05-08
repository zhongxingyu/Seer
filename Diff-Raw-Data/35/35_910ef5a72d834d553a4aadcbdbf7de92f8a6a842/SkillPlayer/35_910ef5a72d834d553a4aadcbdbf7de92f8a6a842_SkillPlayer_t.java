 package core.skills;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "SkillPlayers")
 public class SkillPlayer {
 
 	private static final int	MAX_TOTAL_LEVEL	= 150;
 
 	@Id
 	@Column(name = "ID")
 	private int					id;
 
 	@Column(name = "ACCOUNT_NAME", nullable = false)
 	private String				accountName;
 
 	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "player")
 	private List<Skill>			skills			= new ArrayList<Skill>();
 
 	public SkillPlayer() {
 
 	}
 
 	public void setSkills(List<Skill> skills) {
 		this.skills = skills;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getAccountName() {
 		return this.accountName;
 	}
 
 	public void setAccountName(String name) {
 		this.accountName = name;
 	}
 
 	// @Transient
 	private int getExperienceNeeded(int totalLevel) {
 		return (int) Math.round(10 + (totalLevel + 1 + Math.log(totalLevel + 1) * Math.log(150) * 700));
 	}
 	
 	public int getExpNextLevel(){
 		int exp = 0;
 		exp= getExperienceNeeded(getTotalLevel());
 		return exp;
 	}
 
 	public int getTotalLevel() {
 
 		int totalLevel = 0;
 
 		for (Skill skill : skills) {
 			totalLevel += skill.getLevel();
 		}
 
 		return totalLevel;
 	}
 
 	public void addExperience(SkillType type, int newExperience) {
 		Skill skill = getSkill(type);
 
 		if (moreLevelsAvailable(getTotalLevel(), skill)) {
 
 			int experienceNeed = getExperienceNeeded(getTotalLevel());
 
 			int currentExperience = skill.getExperience() + newExperience;
 
 			if (currentExperience >= experienceNeed) {
 				int newLevelExperience = currentExperience - experienceNeed;
 				skill.setExperience(newLevelExperience);
 				skill.addLevel(1);
 				CheckPermission check = new CheckPermission(accountName, type, skill.getLevel());
 				check.check();
 			} else {
 				skill.addExperience(newExperience);
 			}
 		}
 	}
 
 	// @Transient
 	public int getExperience(SkillType type) {
 		return getSkill(type).getExperience();
 	}
 
 	// @Transient
 	public int getLevel(SkillType type) {
 		return getSkill(type).getLevel();
 	}
 
 	public void setLevel(SkillType type, int level) {
 		getSkill(type).setLevel(level);
 	}
 
 	// @Transient
 	private Skill getSkill(SkillType type) {
 		Skill skill = null;
 		for (Skill skill2 : skills) {
 			if (skill2.getType() == type) {
 				skill = skill2;
 				break;
 			}
 		}
 		return skill;
 	}
 
	public void addSkill(Skill skill) {
 
		skills.add(skill);
 
 	}
 
 	// @Transient
 	public List<Skill> getSkills() {
 		return this.skills;
 	}
 
 	private boolean moreLevelsAvailable(int totalLevel, Skill skill) {
 		return (skill.getLevel() < skill.getMaxLevel()) && (getTotalLevel() < MAX_TOTAL_LEVEL);
 	}
 }
