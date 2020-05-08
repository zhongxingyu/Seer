 package pl.pronux.sokker.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import pl.pronux.sokker.bean.TrainingSummary;
 
 public class Training {
 
 	final public static int FORMATION_GK = 0;
 	final public static int FORMATION_DEF = 1;
 	final public static int FORMATION_MID = 2;
 	final public static int FORMATION_ATT = 3;
 	final public static int FORMATION_ALL = 4;
 
 	final public static int TYPE_UNKNOWN = 0;
 	final public static int TYPE_STAMINA = 1;
 	final public static int TYPE_KEEPER = 2;
 	final public static int TYPE_PLAYMAKING = 3;
 	final public static int TYPE_PASSING = 4;
 	final public static int TYPE_TECHNIQUE = 5;
 	final public static int TYPE_DEFENDING = 6;
 	final public static int TYPE_STRIKER = 7;
 	final public static int TYPE_PACE = 8;
 
 	final public static int NO_TRAINING = 1 << 1;
 	final public static int NEW_TRAINING = 1 << 2;
 	final public static int UPDATE_TRAINING = 1 << 3;
 	final public static int UPDATE_PLAYERS = 1 << 4;
 
 	private Date date;
 
 	private int formation;
 
 	private int id;
 
 	private String note;
 
 	private int type;
 
 	private Coach headCoach;
 
 	private Coach juniorCoach;
 
 	private List<Coach> assistants = new ArrayList<Coach>();
 
 	private List<Player> players;
 
 	private List<Junior> juniors;
 
 	private boolean reported;
 
 	private int status;
 
 	public List<Coach> getAssistants() {
 		return assistants;
 	}
 
 	public void setAssistants(List<Coach> assistants) {
 		this.assistants = assistants;
 	}
 
 	public Coach getHeadCoach() {
 		return headCoach;
 	}
 
 	public void setHeadCoach(Coach headCoach) {
 		this.headCoach = headCoach;
 	}
 
 	public Coach getJuniorCoach() {
 		return juniorCoach;
 	}
 
 	public void setJuniorCoach(Coach juniorCoach) {
 		this.juniorCoach = juniorCoach;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public int getFormation() {
 		return formation;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public String getNote() {
 		return note;
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public void setFormation(int formation) {
 		this.formation = formation;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public void setNote(String note) {
 		this.note = note;
 	}
 
 	public void setType(int type) {
 		this.type = type;
 	}
 
 	public boolean isReported() {
 		return reported;
 	}
 
 	public void setReported(boolean reported) {
 		this.reported = reported;
 	}
 
 	public List<Junior> getJuniors() {
 		return juniors;
 	}
 
 	public void setJuniors(List<Junior> juniors) {
 		this.juniors = juniors;
 	}
 
 	public List<Player> getPlayers() {
 		return players;
 	}
 
 	public void setPlayers(List<Player> players) {
 		this.players = players;
 	}
 
 	// public Object clone() {
 	// Object o = null;
 	// try {
 	// o = super.clone();
 	// } catch (CloneNotSupportedException e) {
 	// System.err.println("There is no posibillity to clone training object");
 	// }
 	// return o;
 	// }
 
 	public void copy(Training training) {
 		this.setAssistants(training.getAssistants());
 		this.setJuniors(training.getJuniors());
 		this.setPlayers(training.getPlayers());
 		this.setDate(training.getDate());
 		this.setFormation(training.getFormation());
 		this.setHeadCoach(training.getHeadCoach());
 		this.setId(training.getId());
 		this.setJuniorCoach(training.getJuniorCoach());
 		this.setNote(training.getNote());
 		this.setReported(training.isReported());
 		this.setType(training.getType());
 	}
 
 	public Training clone() {
 		Training training = new Training();
 		training.setAssistants(this.getAssistants());
 		training.setAssistants(new ArrayList<Coach>());
 		for (Coach assistant : this.assistants) {
 			training.getAssistants().add(assistant);
 		}
 		training.setJuniors(this.getJuniors());
 		training.setPlayers(this.getPlayers());
 		training.setDate(this.getDate());
 		training.setFormation(this.getFormation());
 		training.setHeadCoach(this.getHeadCoach());
 		training.setId(this.getId());
 		training.setJuniorCoach(this.getJuniorCoach());
 		training.setNote(this.getNote());
 		training.setReported(this.isReported());
 		training.setType(this.getType());
 		return training;
 	}
 
 	public int getStatus() {
 		return status;
 	}
 
 	public void setStatus(int status) {
 		this.status = status;
 	}
 
 	public TrainingSummary getTrainingSummary() {
 		TrainingSummary trainingSummary = new TrainingSummary();
 		trainingSummary = getJuniorsTrainingSummary(trainingSummary);
 		trainingSummary = getPlayersTrainingSummary(trainingSummary);
 		return trainingSummary;
 	}
 
 	private TrainingSummary getJuniorsTrainingSummary(TrainingSummary trainingSummary) {
 		int max = 0;
 		if (this.getJuniors().size() > 0) {
 			for (Junior junior : this.getJuniors()) {
 				JuniorSkills[] skills = junior.getSkills();
 				max = skills.length;
 				if (max > 1) {
 					for (int i = max - 1; i > 0; i--) {
 						if (this.equals(skills[i].getTraining())) {
 							if (skills[i].getSkill() - skills[i - 1].getSkill() > 0) {
 								trainingSummary.setJuniorsPops(trainingSummary.getJuniorsPops() + 1);
							} else if (skills[i].getSkill() - skills[i - 1].getSkill() < 0) {
 								trainingSummary.setJuniorsFalls(trainingSummary.getJuniorsFalls() + 1);
 							}
							break;
 						}
 					}
 				}
 			}
 		}
 		return trainingSummary;
 	}
 
 	private TrainingSummary getPlayersTrainingSummary(TrainingSummary trainingSummary) {
 		int max = 0;
 		if (this.getPlayers().size() > 0) {
 			for (Player player : this.getPlayers()) {
 				PlayerSkills[] skills = player.getSkills();
 				max = skills.length;
 				if (max > 1) {
 					for (int i = max - 1; i > 0; i--) {
 						if (this.equals(skills[i].getTraining())) {
 							if (skills[i].getStamina() - skills[i - 1].getStamina() > 0) {
 								trainingSummary.setStaminaPops(trainingSummary.getStaminaPops() + 1);
 								if (this.getType() == Training.TYPE_STAMINA) {
 									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
 								}
 							} else if (skills[i].getStamina() - skills[i - 1].getStamina() < 0) {
 								trainingSummary.setStaminaFalls(trainingSummary.getStaminaFalls() + 1);
 								if (this.getType() == Training.TYPE_STAMINA) {
 									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
 								}
 							}
 							if (skills[i].getPace() - skills[i - 1].getPace() > 0) {
 								trainingSummary.setAllSkillsPops(trainingSummary.getAllSkillsPops() + 1);
 								if (this.getType() == Training.TYPE_PACE) {
 									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
 								}
 							} else if (skills[i].getPace() - skills[i - 1].getPace() < 0) {
 								trainingSummary.setAllSkillsFalls(trainingSummary.getAllSkillsFalls() + 1);
 								if (this.getType() == Training.TYPE_PACE) {
 									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
 								}
 							}
 							if (skills[i].getTechnique() - skills[i - 1].getTechnique() > 0) {
 								trainingSummary.setAllSkillsPops(trainingSummary.getAllSkillsPops() + 1);
 								if (this.getType() == Training.TYPE_TECHNIQUE) {
 									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
 								}
 							} else if (skills[i].getTechnique() - skills[i - 1].getTechnique() < 0) {
 								trainingSummary.setAllSkillsFalls(trainingSummary.getAllSkillsFalls() + 1);
 								if (this.getType() == Training.TYPE_TECHNIQUE) {
 									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
 								}
 							}
 							if (skills[i].getPassing() - skills[i - 1].getPassing() > 0) {
 								trainingSummary.setAllSkillsPops(trainingSummary.getAllSkillsPops() + 1);
 								if (this.getType() == Training.TYPE_PASSING) {
 									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
 								}
 							} else if (skills[i].getPassing() - skills[i - 1].getPassing() < 0) {
 								trainingSummary.setAllSkillsFalls(trainingSummary.getAllSkillsFalls() + 1);
 								if (this.getType() == Training.TYPE_PASSING) {
 									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
 								}
 							}
 							if (skills[i].getKeeper() - skills[i - 1].getKeeper() > 0) {
 								trainingSummary.setAllSkillsPops(trainingSummary.getAllSkillsPops() + 1);
 								if (this.getType() == Training.TYPE_KEEPER) {
 									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
 								}
 							} else if (skills[i].getKeeper() - skills[i - 1].getKeeper() < 0) {
 								trainingSummary.setAllSkillsFalls(trainingSummary.getAllSkillsFalls() + 1);
 								if (this.getType() == Training.TYPE_KEEPER) {
 									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
 								}
 							}
 							if (skills[i].getDefender() - skills[i - 1].getDefender() > 0) {
 								trainingSummary.setAllSkillsPops(trainingSummary.getAllSkillsPops() + 1);
 								if (this.getType() == Training.TYPE_DEFENDING) {
 									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
 								}
 							} else if (skills[i].getDefender() - skills[i - 1].getDefender() < 0) {
 								trainingSummary.setAllSkillsFalls(trainingSummary.getAllSkillsFalls() + 1);
 								if (this.getType() == Training.TYPE_DEFENDING) {
 									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
 								}
 							}
 							if (skills[i].getPlaymaker() - skills[i - 1].getPlaymaker() > 0) {
 								trainingSummary.setAllSkillsPops(trainingSummary.getAllSkillsPops() + 1);
 								if (this.getType() == Training.TYPE_PLAYMAKING) {
 									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
 								}
 							} else if (skills[i].getPlaymaker() - skills[i - 1].getPlaymaker() < 0) {
 								trainingSummary.setAllSkillsFalls(trainingSummary.getAllSkillsFalls() + 1);
 								if (this.getType() == Training.TYPE_PLAYMAKING) {
 									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
 								}
 							}
 							if (skills[i].getScorer() - skills[i - 1].getScorer() > 0) {
 								trainingSummary.setAllSkillsPops(trainingSummary.getAllSkillsPops() + 1);
 								if (this.getType() == Training.TYPE_STRIKER) {
 									trainingSummary.setTrainedSkillsPops(trainingSummary.getTrainedSkillsPops() + 1);
 								}
 							} else if (skills[i].getScorer() - skills[i - 1].getScorer() < 0) {
 								trainingSummary.setAllSkillsFalls(trainingSummary.getAllSkillsFalls() + 1);
 								if (this.getType() == Training.TYPE_STRIKER) {
 									trainingSummary.setTrainedSkillsFalls(trainingSummary.getTrainedSkillsFalls() + 1);
 								}
 							}
 							break;
 						}
 					}
 				}
 			}
 		}
 		return trainingSummary;
 	}
 	
 	public int getHeadCoachTrainedSkill() {
 		if (this.getHeadCoach() != null) {
 			switch (this.getType()) {
 			case Training.TYPE_DEFENDING:
 				return getHeadCoach().getDefenders();
 			case Training.TYPE_KEEPER:
 				return getHeadCoach().getKeepers();
 			case Training.TYPE_PACE:
 				return getHeadCoach().getPace();
 			case Training.TYPE_PASSING:
 				return getHeadCoach().getPassing();
 			case Training.TYPE_PLAYMAKING:
 				return getHeadCoach().getPlaymakers();
 			case Training.TYPE_STAMINA:
 				return getHeadCoach().getStamina();
 			case Training.TYPE_STRIKER:
 				return getHeadCoach().getScorers();
 			case Training.TYPE_TECHNIQUE:
 				return getHeadCoach().getTechnique();
 			}
 		}
 		return 0;
 	}
 }
