 package entity;
 
 import java.util.ArrayList;
 
 import networking.*;
 import baseGame.Rendering.RGBImage;
 import baseGame.Rendering.Renderer;
 import blueMaggot.GameState;
 
 import entity.weapon.Airstrike;
 import entity.weapon.GrenadeGun;
 import entity.weapon.Gun;
 import entity.weapon.MineLauncher;
 import entity.weapon.Minigun;
 import entity.weapon.Rocketlauncher;
 import entity.weapon.ShellGun;
 import entity.weapon.Weapon;
 import gfx.ResourceManager;
 
 import level.BasicLevel;
 import inputhandler.InputHandler;
 
 public class Tank extends Entity {
 
 	// player variables
 	private int score = 0;
 	private int oldScore = 0;
 	private int life = 5;
 	private String nick;
 
 	private InputHandler input;
 	private double muzzleAngle;
 	private int muzzleLength = 16;
 
 	protected int playerNumber;
 
 	public void setPlayerNumber(int playerNumber) {
 		this.playerNumber = playerNumber;
 	}
 
 	protected double jetPackFuel = 100;
 	protected double cannonCharge = 0;
 	private boolean canGoUp = true;
 	private boolean canGoLeft = true;
 	private boolean canGoRight = true;
 	private boolean canGoDown = true;
 	protected boolean chargingCannon = false;
 
 	private Gun currentWeapon = Gun.SHELLGUN;
 
 	private double torque = 0.15;
 
 	private ArrayList<Weapon> weaponList;
 	private PixelHitbox boxUnderCenter;
 	private PixelHitbox boxLeft;
 	private PixelHitbox boxRight;
 	private PixelHitbox boxUp;
 
 	public Tank(double x, double y, int playerNumber, InputHandler input, BasicLevel level) {
 		super(x, y, 11, 6, level);
 
 		GameState.getInstance().getPlayers().add(this);
 
 		muzzleAngle = 0;
 		muzzleLength = 21;
 		this.playerNumber = playerNumber;
 		this.input = input;
 		frictionConstant = 0.12;
 		initInventory();
 
 		boxUnderCenter = new PixelHitbox();
 		boxUnderCenter.addPoint(new FloatingPoint(-2, yr));
 		boxUnderCenter.addPoint(new FloatingPoint(-1, yr));
 		boxUnderCenter.addPoint(new FloatingPoint(0, yr));
 		boxUnderCenter.addPoint(new FloatingPoint(1, yr));
 		boxUnderCenter.addPoint(new FloatingPoint(2, yr));
 		boxUnderCenter.addPoint(new FloatingPoint(-2, yr - 1));
 		boxUnderCenter.addPoint(new FloatingPoint(-1, yr - 1));
 		boxUnderCenter.addPoint(new FloatingPoint(0, yr - 1));
 		boxUnderCenter.addPoint(new FloatingPoint(1, yr - 1));
 		boxUnderCenter.addPoint(new FloatingPoint(2, yr - 1));
 
 		boxLeft = new PixelHitbox();
 		boxRight = new PixelHitbox();
 		boxUp = new PixelHitbox();
 		boxUp.addPoint(new FloatingPoint(-2, -yr + 2));
 		boxUp.addPoint(new FloatingPoint(+2, -yr + 2));
 		for (int i = (int) -yr + 2; i < -2; i++) {
 			boxLeft.addPoint(new FloatingPoint(-xr, i));
 		}
 		for (int ii = (int) -yr + 2; ii < -2; ii++) {
 			boxRight.addPoint(new FloatingPoint(xr, ii));
 		}
 
 		/*
 		 * for(int ii = 0;ii<yr-2;ii++){ boxLeft.addPoint(new FloatingPoint(0,
 		 * ii)); }
 		 */
 
 	}
 
 	public void setCurrentWeapon(Gun currentWeapon) {
 		this.currentWeapon = currentWeapon;
 	}
 
 	public void setLife(int life) {
 		this.life = life;
 	}
 
 	public Tank(FloatingPoint point, int playerNumber, InputHandler input, BasicLevel level) {
 		super(point.getX(), point.getY(), 11, 6, level);
 		GameState.getInstance().getPlayers().add(this);
 		muzzleAngle = 90;
 		muzzleLength = 20;
 		this.playerNumber = playerNumber;
 		this.input = input;
 		frictionConstant = 0.12;
 		initInventory();
 
 		boxUnderCenter = new PixelHitbox();
 		boxUnderCenter.addPoint(new FloatingPoint(-2, yr));
 		boxUnderCenter.addPoint(new FloatingPoint(-1, yr));
 		boxUnderCenter.addPoint(new FloatingPoint(0, yr));
 		boxUnderCenter.addPoint(new FloatingPoint(1, yr));
 		boxUnderCenter.addPoint(new FloatingPoint(2, yr));
 
 		boxLeft = new PixelHitbox();
 		boxRight = new PixelHitbox();
 		boxUp = new PixelHitbox();
 		boxUp.addPoint(new FloatingPoint(-2, -yr + 2));
 		boxUp.addPoint(new FloatingPoint(+2, -yr + 2));
 		for (int i = (int) -yr + 2; i < -2; i++) {
 			boxLeft.addPoint(new FloatingPoint(-xr, i));
 		}
 		for (int ii = (int) -yr + 2; ii < -2; ii++) {
 			boxRight.addPoint(new FloatingPoint(xr, ii));
 		}
 
 		/*
 		 * for(int ii = 0;ii<yr-2;ii++){ boxLeft.addPoint(new FloatingPoint(0,
 		 * ii)); }
 		 */
 
 	}
 
 	public int getPlayerNumber() {
 		return playerNumber;
 	}
 
 	public double getX() {
 
 		return x;
 	}
 
 	public double getY() {
 		return y;
 	}
 
 	public Gun getCurrentWeapon() {
 		return currentWeapon;
 	}
 
 	public void initInventory() {
 		weaponList = new ArrayList<Weapon>();
 		weaponList.add(new ShellGun());
 		weaponList.add(new GrenadeGun());
 		weaponList.add(new Rocketlauncher());
 		weaponList.add(new MineLauncher());
 		weaponList.add(new Airstrike());
 		weaponList.add(new Minigun(this));
 	}
 
 	public void setMuzzleAngle(double degrees) {
 		this.muzzleAngle = degrees;
 	}
 
 	public void incrementMuzzleAngle(double degrees) {
 		setMuzzleAngle(this.muzzleAngle + degrees);
 		muzzleAngle = ((360 + muzzleAngle) % 360);
 
 	}
 
 	public void addScore(int amount) {
 		this.score += amount;
 	}
 
 	public int getScore() {
 		return score;
 	}
 
 	public FloatingPoint getCrosshairLocation() {
 		return (new FloatingPoint(x + muzzleLength * Math.cos(Math.toRadians(muzzleAngle)), y - muzzleLength
 				* Math.sin(Math.toRadians(muzzleAngle))));
 	}
 
 	public void fire(double speedPercent) {
 		if (speedPercent < 0.2)
 			speedPercent = 0.2;
 		weaponList.get(currentWeapon.ordinal()).fire(this.x + muzzleLength * Math.cos(Math.toRadians(muzzleAngle)),
 				this.y - muzzleLength * Math.sin(Math.toRadians(muzzleAngle)), this.level, speedPercent,
 				this.muzzleAngle - 4);
 	}
 
 	public void handleTerrainIntersection() {
 
 		do {
 			canGoDown = true;
 			canGoUp = true;
 			for (FloatingPoint point : boxUnderCenter) {
 				if (level.getTerrain().hitTestpoint((int) (point.getX() + x), (int) (point.getY() + y + 1))) {
 					canGoDown = false;
 					break;
 				}
 			}
 
 			for (FloatingPoint point : boxUp) {
 				if (level.getTerrain().hitTestpoint((int) (point.getX() + x), (int) (point.getY() + y))) {
 					canGoUp = false;
 					break;
 				}
 			}
			if (!canGoUp && !canGoDown){
 				setLocation(x - Math.signum(dx) * 1, y - Math.signum(dy) * 1);
 			}
 		} while (!canGoUp && !canGoDown);
 
 		canGoLeft = true;
 		canGoRight = true;
 		canGoDown = true;
 		canGoUp = true;
 
 		for (FloatingPoint point : boxLeft) {
 			if (level.getTerrain().hitTestpoint((int) (point.getX() + x), (int) (point.getY() + y))) {
 
 				canGoLeft = false;
 				if (dx < 0)
 					dx = -0.2 * dx;
 			}
 		}
 		for (FloatingPoint point : boxRight) {
 			if (level.getTerrain().hitTestpoint((int) (point.getX() + x), (int) (point.getY() + y))) {
 
 				canGoRight = false;
 				if (dx > 0)
 					dx = -0.2 * dx;
 			}
 		}
 		for (FloatingPoint point : boxUnderCenter) {
 			if (level.getTerrain().hitTestpoint((int) (point.getX() + x), (int) (point.getY() + y))) {
 				while (level.getTerrain().hitTestpoint((int) (point.getX() + x), (int) (point.getY() + y))) {
 					setLocation(x, y - 1);
 					setSpeed(dx, 0);
 				}
 				canGoDown = false;
 				return;
 			}
 		}
 		for (FloatingPoint point : boxUp) {
 			if (level.getTerrain().hitTestpoint((int) (point.getX() + x), (int) (point.getY() + y))) {
 				while (level.getTerrain().hitTestpoint((int) (point.getX() + x), (int) (point.getY() + y))) {
 					setLocation(x, y + 1);
 					setSpeed(dx, 0.3);
 				}
 				break;
 			}
 		}
 
 	}
 
 	public boolean intersectsTerrain() {
 		if (level.getTerrain().hitTestpoint((int) x, (int) (y + yr * 2))) {
 			return true;
 		}
 		return false;
 	}
 
 	protected void tickWeapons(double dt) {
 		for (Weapon wep : weaponList) {
 			wep.tick(dt);
 		}
 	}
 
 	public void jetPack() {
 		double fuelTick = 13 * dt;
 		if (jetPackFuel >= fuelTick) {
 			accelerate(0, -0.45);
 			jetPackFuel -= fuelTick;
 		}
 	}
 
 	public void toggleWeapon() {
 		currentWeapon = Gun.values()[(currentWeapon.ordinal() + 1) % Gun.values().length];
 		if (currentWeapon.ordinal() >= weaponList.size())
 			currentWeapon = Gun.SHELLGUN;
 		if (weaponList.get(currentWeapon.ordinal()).getAmmo() == 0)
 			toggleWeapon();
 	}
 
 	public ArrayList<Weapon> getWeaponList() {
 		return weaponList;
 	}
 
 	protected void player2Input() {
 		if (input.up1.down)
 			jetPack();
 		if (input.down1.clicked)
 			toggleWeapon();
 		if (input.right1.down && canGoRight) {
 
 			if (dx < 2)
 				accelerate(torque, 0);
 
 		}
 		if (input.left1.down && canGoLeft) {
 			if (dx > -2)
 				accelerate(-torque, 0);
 		}
 		if (input.fire1.clicked) {
 			chargingCannon = true;
 		}
 		if (chargingCannon && !input.fire1.down || cannonCharge >= 1) {
 			fire(cannonCharge);
 			chargingCannon = false;
 			cannonCharge = 0;
 		}
 		if (input.rotateL1.down)
 			incrementMuzzleAngle(3);
 		if (input.rotateR1.down)
 			incrementMuzzleAngle(-3);
 	}
 
 	protected void player1Input() {
 
 		if (input.up2.down)
 			jetPack();
 		if (input.down2.clicked)
 			toggleWeapon();
 		if (input.right2.down && canGoRight)
 			if (dx < 2)
 				accelerate(torque, 0);
 		if (input.left2.down && canGoLeft) {
 			if (dx > -2)
 				accelerate(-torque, 0);
 		}
 		if (input.fire2.clicked) {
 			chargingCannon = true;
 		}
 		if (chargingCannon && !input.fire2.down || cannonCharge >= 1) {
 			fire(cannonCharge);
 			chargingCannon = false;
 			cannonCharge = 0;
 		}
 		if (input.rotateL2.down)
 			incrementMuzzleAngle(3 * dt);
 		if (input.rotateR2.down)
 			incrementMuzzleAngle(-3 * dt);
 	}
 
 	public double getMuzzleAngle() {
 		return muzzleAngle;
 	}
 
 	@Override
 	public void takeDamage(double amount) {
 		this.damageTaken += amount;
 	}
 
 	public void applyFriction() {
 		for (FloatingPoint point : boxUnderCenter) {
			if (level.getTerrain().hitTestpoint((int) (point.getX() + x), (int) (point.getY() + y + 1)))
 				accelerate(-dx * frictionConstant, 0);
			return;
 		}
 	}
 
 	@Override
 	public void remove() {
 
 		if (!removed) {
 			score -= 100;
 			--life;
 		}
 		if (life == 0) {
 			super.remove();
 			return;
 		}
 		setLocation(level.getPlayerSpawns().getPoint());
 		setSpeed(0, -1);
 		muzzleAngle = 90;
 		damageTaken = 1;
 		initInventory();
 		currentWeapon = Gun.SHELLGUN;
 	}
 
 	@Override
 	public void tick(double dt) {
 		super.tick(dt);
 		if (playerNumber == 1)
 			player1Input();
 		if (playerNumber == 2)
 			player2Input();
 
 		if (chargingCannon)
 			cannonCharge += 0.015 * dt;
 		if (jetPackFuel + 5 > 100)
 			jetPackFuel = 100;
 		else
 			jetPackFuel += 2 * dt;
 
 		handleTerrainIntersection();
 		applyFriction();
 		tickWeapons(dt);
 
 		if (this.y < -500)
 			remove();
 	}
 
 	@Override
 	public void render(Renderer renderer) {
 		// TODO Auto-generated method stub
 		RGBImage img;
 		RGBImage crossHair;
 		if (playerNumber == 1) {
 			img = ResourceManager.TANK1;
 			crossHair = ResourceManager.CROSSHAIR1;
 		} else {
 			img = ResourceManager.TANK2;
 			crossHair = ResourceManager.CROSSHAIR2;
 		}
 
 		renderer.DrawImage(img, -1, (int) (x - getXr()) - 1, (int) (y - getYr() + 1), img.getWidth(), img.getHeight());
 		renderer.DrawImage(crossHair, -1, (int) (getCrosshairLocation().getX() - crossHair.getWidth() / 2 + 1),
 				(int) (getCrosshairLocation().getY() - crossHair.getHeight() / 2), crossHair.getWidth(),
 				crossHair.getHeight());
 	}
 
 	@Override
 	public String getObject() {
 
 		return super.getObject() + "'" + encodeToDouble(this.muzzleAngle) + "'" + getPlayerNumber() + "'" + damageTaken;
 	}
 
 	@Override
 	public void handleMessage(String[] msg) {
 		super.handleMessage(msg);
 
 		double muzzleAngle = decodeToDouble(Integer.parseInt(msg[6]));
 		setMuzzleAngle(muzzleAngle);
 		double damageTaken = decodeToDouble(Integer.parseInt(msg[7]));
 		this.damageTaken = damageTaken;
 
 	}
 
 	@Override
 	public void initNetworkValues() {
 		// TODO Auto-generated method stub
 		setNetworkObjectType(NetworkObjectType.TANK);
 	}
 
 	public int getLife() {
 		return life;
 	}
 
 	public String getNick() {
 		return nick;
 	}
 
 	@Override
 	public String toString() {
 		return "Nick: " + nick + " - Score: " + score + " - Life: " + " - Weapon: " + currentWeapon;
 	}
 
 	public int getOldScore() {
 		return oldScore;
 	}
 
 	public void setOldScore(int oldScore) {
 		this.oldScore = oldScore;
 	}
 
 	public void setNick(String nick) {
 		this.nick = nick;
 	}
 
 	public void setScore(int score) {
 		this.score = score;
 	}
 
 	public String getCurrentWeaponName() {
 		return this.currentWeapon.name();
 	}
 }
