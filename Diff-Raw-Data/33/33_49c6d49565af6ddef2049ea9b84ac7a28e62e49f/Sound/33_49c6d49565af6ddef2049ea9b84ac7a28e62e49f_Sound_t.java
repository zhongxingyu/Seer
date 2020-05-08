 package hunternif.mc.dota2items;
 
 public enum Sound {
 	BLINK_IN("blink_in"),
 	BLINK_OUT("blink_out"),
 	DENY_COOLDOWN("deny_cooldown"),
 	DENY_GENERAL("deny_general"),
 	DENY_SILENCE("deny_silence"),
 	DENY_MANA("deny_mana"),
 	MAGIC_IMMUNE("magic_immune"),
 	TREE_FALL("tree_fall"),
 	CYCLONE_START("cyclone_start"),
 	COINS("coins"),
 	BUY("buy"),
	ARROW("arrow", 4),
 	CLARITY("clarity");
 	
 	private String name;
	public int randomVariants = 0;
 	private Sound(String name) {
		this(name, 0);
	}
	private Sound(String name, int randomVariants) {
 		this.name = name;
		this.randomVariants = randomVariants;
	}
	
	public boolean isRandom() {
		return randomVariants > 0;
 	}
	
 	public String toString() {
 		return name;
 	}
 	
 	public String getName() {
 		return Dota2Items.ID+":"+name;
 	}
 }
