 package pudgewars.components;
 
 import java.awt.Image;
 
 import pudgewars.Game;
 import pudgewars.entities.PudgeEntity;
 import pudgewars.entities.Team;
 import pudgewars.render.ArcImage;
 import pudgewars.render.GUI;
 import pudgewars.render.TextColor;
 import pudgewars.render.TextSize;
 import pudgewars.util.ImageHandler;
 import pudgewars.util.TimedRenderObject;
 
 public class Stats {
 	public PudgeEntity pudge;
 
 	// Inventory Data
 	private int experience = 25;
 	public boolean isOpen = false;
 	public Image[] statImages;
 	public Image[] expImages;
 	public ArcImage[] lifeImages;
 
 	public Image[] hookCooldownImages;
 	public Image[] grappleCooldownImages;
 	public Image[] burnerCooldownImages;
 	public Image activeHookSelection;
 
 	// Movement Data
 	public BaseStat moveSpeed = new BaseStat(this, "MoveSpeed", 0, 3.8, 0.4, 1);
 
 	// Hook Data
 	public BaseStat hookSize = new BaseStat(this, "Hook Size", 5, 0.75, 0.1, 1);
 	public BaseStat hookSpeed = new BaseStat(this, "Hook Speed", 3, 8, 1, 1);
 	public BaseStat hookRange = new BaseStat(this, "Hook Range", 4, 14, 2, 1);
 	public BaseStat hookDamage = new BaseStat(this, "Hook Damage", 2, 4, 1, 1);
 
 	// Player Data
 	public BaseStat life = new BaseStat(this, "Life", 1, 20, 2, 1);
 	public BaseStat[] ref;
 	private int _life;
 	private int kills = 0;
 
 	// public boolean shouldShowKillStreak = false;
 	public int killStreak = 0;
 
 	// private int deaths = 0;
 
 	public Stats(PudgeEntity p) {
 		pudge = p;
 
 		ref = new BaseStat[CharStat.length];
 		ref[0] = life;
 		ref[1] = moveSpeed;
 		ref[2] = hookSize;
 		ref[3] = hookSpeed;
 		ref[4] = hookRange;
 		ref[5] = hookDamage;
 
 		_life = (int) life.getValue();
 
 		// Load the Images
 		expImages = new Image[4];
 		for (int i = 0; i < 4; i++)
 			expImages[i] = ImageHandler.get().getImage("HUD_exp_" + i);
 
 		// Life Images
 		lifeImages = new ArcImage[3];
 		lifeImages[0] = new ArcImage(ImageHandler.get().getImage("hp_empty"), ImageHandler.get().getImage("hp_full"));
 		lifeImages[1] = new ArcImage(ImageHandler.get().getImage("hp_empty"), ImageHandler.get().getImage("hp_half"));
 		lifeImages[2] = new ArcImage(ImageHandler.get().getImage("hp_empty"), ImageHandler.get().getImage("hp_quarter"));
 
 		hookCooldownImages = new Image[2];
 		hookCooldownImages[0] = ImageHandler.get().getImage("hooks/lasso_icon_empty");
 		hookCooldownImages[1] = ImageHandler.get().getImage("hooks/lasso_icon_full");
 		grappleCooldownImages = new Image[2];
 		grappleCooldownImages[0] = ImageHandler.get().getImage("hooks/grapple_icon_empty");
 		grappleCooldownImages[1] = ImageHandler.get().getImage("hooks/grapple_icon_full");
 		burnerCooldownImages = new Image[2];
 		burnerCooldownImages[0] = ImageHandler.get().getImage("hooks/burner_empty");
 		burnerCooldownImages[1] = ImageHandler.get().getImage("hooks/burner_full");
 
 		activeHookSelection = ImageHandler.get().getImage("hooks/activehook");
 	}
 
 	public void restoreDefaults() {
 		pudge.rigidbody.speed = moveSpeed.getValue();
 	}
 
 	/*
 	 * Network
 	 */
 	public String getNetString() {
 		String s = experience + "]" + _life;
 		for (int i = 0; i < ref.length; i++)
 			s += "]" + ref[i].getNetString();
 		s += "]" + kills;
 		s += "]" + killStreak;
 		return s;
 	}
 
 	public void setNetString(String s) {
 		String[] t = s.split("]");
		// if ((Game.isServer && Integer.parseInt(t[0]) < experience) //
		// || (!Game.isServer) && Integer.parseInt(t[0]) > experience) experience = Integer.parseInt(t[0]);
 		if (!Game.isServer) _life = Integer.parseInt(t[1]);
 		for (int i = 2; i < ref.length + 2; i++)
 			ref[i - 2].setNetString(t[i]);
 		if (!Game.isServer) kills = Integer.parseInt(t[ref.length + 2]);
 		if (!Game.isServer) {
 			int tKillStreak = Integer.parseInt(t[ref.length + 3]);
 			if (killStreak != tKillStreak) {
 				killStreak = tKillStreak;
 
 				if (killStreak >= 2) {
 					TimedRenderObject tro = new TimedRenderObject(ImageHandler.get().getImage("messages/killingspree"), Game.s.cx, 10, true, false, 4);
 					Game.entities.screenOverlays.add(tro);
 				}
 			}
 		}
 		restoreDefaults();
 	}
 
 	/*
 	 * Life
 	 */
 	public void set_life(int life) {
 		this._life = life;
 	}
 
 	public int life() {
 		return _life;
 	}
 
 	public double lifePercentage() {
 		return (double) _life / life.getValue();
 	}
 
 	public void addLife(int a) {
 		_life += a;
 		if (_life > life.getValue()) _life = (int) life.getValue();
 	}
 
 	public boolean subLife(int a) {
 		_life -= a;
 		if (_life <= 0) {
 			pudge.kill();
 			killStreak = 0;
 			return true;
 		}
 		return false;
 	}
 
 	public void addExp(int amt) {
 		experience += amt;
 		if (experience > (expImages.length - 1) * 10) experience = ((expImages.length - 1) * 10);
 		if (experience < 0) experience = 0;
 	}
 
 	public void addKill() {
 		kills++;
 		killStreak++;
 	}
 
 	public int getKills() {
 		return kills;
 	}
 
 	/*
 	 * GUI
 	 */
 	public final static int LEFT_PADDING = Game.TILE_SIZE / 2;
 	public final static int RIGHT_PADDING = Game.TILE_SIZE / 2;
 	public final static int BOT_PADDING = Game.TILE_SIZE / 2;
 	public final static int BAR_PADDING = Game.TILE_SIZE / 2;
 
 	public void onGUI() {
 		// Draw Life
		// GUI.partialVerticalBar(lifeImages, LEFT_PADDING, Game.s.height -
		// (BOT_PADDING + lifeImages[0].getHeight(null)), -lifePercentage());
		// GUI.partialHorizontalBar(lifeImages, (Game.s.width -
		// lifeImages[0].getWidth(null)) / 2, Game.s.height - (BOT_PADDING +
		// barHeight), lifePercentage());
 		int lifeIndex = 0;
		if (lifePercentage() > 0.5)
		;
 		else if (lifePercentage() > 0.25) lifeIndex = 1;
 		else lifeIndex = 2;
 		lifeImages[lifeIndex].renderCenteredAt( //
 				LEFT_PADDING + lifeImages[0].getWidth() / 2, Game.s.height - (BOT_PADDING + lifeImages[0].getHeight() / 2), //
 				lifePercentage());
 
 		/*
 		 * Hooks
 		 */
 		int cW = grappleCooldownImages[0].getWidth(null); // = Cooldown Width
 		int cH = grappleCooldownImages[0].getHeight(null); // = Cooldown height
 
 		int hookTypesCount = 3;
 		int sx = Game.s.width - (RIGHT_PADDING + hookTypesCount * cW + (hookTypesCount - 1) * BAR_PADDING);
 		int sy = Game.s.height - (BOT_PADDING + cH);
 		int xInt = cW + BAR_PADDING;
 
 		// Hook Cooldown
 		GUI.partialHorizontalBar(hookCooldownImages, sx, sy, (1 - (pudge.hookCooldown / PudgeEntity.HOOK_COOLDOWN)));
 		sx += xInt;
 		// Hook Cooldown
 		GUI.partialHorizontalBar(burnerCooldownImages, sx, sy, (1 - (pudge.burnerCooldown / PudgeEntity.BURNERHOOK_COOLDOWN)));
 		sx += xInt;
 		// Grapple Cooldown
 		GUI.partialHorizontalBar(grappleCooldownImages, sx, sy, (1 - (pudge.grappleCooldown / PudgeEntity.GRAPPLEHOOK_COOLDOWN)));
 
 		// Draw Active Hook Selection
 		int nx = Game.s.width - (RIGHT_PADDING + hookTypesCount * cW + (hookTypesCount - 1) * BAR_PADDING) + pudge.activeHook * xInt;
 		Game.s.g.drawImage(activeHookSelection, nx, sy, null);
 
 		// Draw Experience
 		int ex = LEFT_PADDING + lifeImages[0].getWidth() + BAR_PADDING;
 		int ey = Game.s.height - (BOT_PADDING + expImages[0].getHeight(null));
 		if (experience < 30) {
 			Image tImg[] = { expImages[(int) (experience / 10)], expImages[(int) (experience / 10) + 1] };
 			GUI.partialHorizontalBar(tImg, ex, ey, ((((int) experience) % 10) / 10.0));
 		} else {
 			Image tImg[] = { expImages[(int) (experience / 10)], expImages[(int) (experience / 10)] };
 			GUI.partialHorizontalBar(tImg, ex, ey, ((((int) experience)) / 10.0));
 		}
 
 		int radius = 60;
 		int cx = Game.s.width / 2;
 		int cy = Game.s.height / 2;
 		double angle = 0;
 		if (isOpen) {
 			for (int i = 0; i < CharStat.length; i++) {
 				if (ref[i].drawButtons((int) (radius * Math.cos(angle)) + cx - ref[i].image[0].getWidth(null) / 2, //
 						(int) (radius * Math.sin(angle)) + cy - ref[i].image[0].getHeight(null) / 2)) {
 					if (ref[i].getCost() <= experience) {
 						experience -= ref[i].getCost();
 						ref[i].setLevel(ref[i].level + 1);
 						pudge.restoreDefaults();
 						pudge.shouldSendNetworkData = true;
 					}
 				}
 				angle += Math.PI / 3;
 			}
 		}
 	}
 
 	public void showScore() {
 		// Draw Kills
 		int x = (pudge.team == Team.leftTeam) ? 10 : Game.s.width - 82;
 		int y = ((pudge.ClientID / 2) + 2) * 10;
 		TextColor color = (pudge.remove) ? TextColor.grey : (pudge.controllable) ? TextColor.red : TextColor.black;
 		GUI.showText(pudge.name + ": " + kills, TextSize.normal, color, x, y);
 	}
 }
