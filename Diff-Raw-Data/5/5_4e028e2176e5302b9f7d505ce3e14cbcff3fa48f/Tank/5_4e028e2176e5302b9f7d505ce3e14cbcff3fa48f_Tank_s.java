 package jig.ironLegends;
 
 import java.awt.Point;
 
 import jig.engine.Mouse;
 import jig.engine.RenderingContext;
 import jig.engine.physics.Body;
 import jig.engine.util.Vector2D;
 import jig.ironLegends.core.ATSprite;
 import jig.ironLegends.core.KeyCommands;
 import jig.ironLegends.core.MultiSpriteBody;
 
 public class Tank extends MultiSpriteBody {
 	public static enum Type {
 		BASIC, SPEEDY, ARMORED
 	};
 
 	public static enum Team {
 		WHITE, BLUE, RED
 	};
 
 	public static enum Weapon {
 		CANNON, DOUBLECANNON
 	};
 
 	private static final double TURN_RATE = 2.0;
 	private static final int MAX_HEALTH = 100;
 	private static final int RESPAWN_DELAY = 1000;
 	
 	public static final int PU_SHIELD = 0;
 	public static final int PU_SPEED = 1;
 	public static final int PU_ARMOR = 2;
 	public static final int PU_MINE = 3;
 	public static final int PU_DAMAGE = 4;
 	
 	private int MAX_SHIELD_TIME = 10000;
 	private int SPEED = 300;
 	private int FIRE_DELAY = 300;
 
 	private Vector2D initialPosition;
 	private ATSprite sTurret;
 	private ATSprite sTeam;
 	private ATSprite sSpeed;
 	private ATSprite sArmor;
 	private ATSprite sShield;
 	private Animator m_animator;
 	private Type type = Type.BASIC;
 	private Team team = Team.WHITE;
 	private int m_team = 0;	// TODO: set during construction
 	private int m_entityNumber = 0;	// TODO: set during construction
 	private SteeringBehavior m_steering;
 	
 	private Weapon weapon = Weapon.CANNON;
 	private Body target = null;
 	private boolean playerControlled = true;
 
 	private boolean respawn = true;
 	private boolean shield = false;
 	private boolean fixturret = false;
 	private int curSpeed;
 	private double angularVelocity;
 	private int health = MAX_HEALTH;
 	private int damageAmount = 20;
 	private int bulletRange = 300;
 	private long timeSinceFired = 0;
 	private long timeSinceDied = 0;
 	private long timeSinceShield = 0;
 	private int score = 0;
 	private IronLegends game = null;
 	private HealthBar m_healthBar = null;
 	private double initialRotDeg = 0;
 	private boolean fireSecondBullet = false;
 	private double m_turretRotationRad;
 
 	public Tank(IronLegends game, int iTeam, Team team, Type type, int entityNumber) {
 		super(game.m_polygonFactory.createRectangle(Vector2D.ZERO, 85, 101),
 				IronLegends.SPRITE_SHEET + "#tank");
 		
 		setGame(game);
 		m_entityNumber = entityNumber;
 		m_team = iTeam;
 
 		// Tank Powers
 		sSpeed = getSprite(addSprite(IronLegends.SPRITE_SHEET + "#speed"));
 		sSpeed.setActivation(false);
 		sArmor = getSprite(addSprite(IronLegends.SPRITE_SHEET + "#armor"));
 		sArmor.setActivation(false);
 		sShield = getSprite(addSprite(IronLegends.SPRITE_SHEET
 				+ "#shield-effect"));
 		sShield.setActivation(false);
 
 		// Team star
 		sTeam = getSprite(addSprite(IronLegends.SPRITE_SHEET + "#star"));
 		sTeam.setOffset(new Vector2D(0, 35));
 
 		// Turret
 		sTurret = getSprite(addSprite(IronLegends.SPRITE_SHEET + "#cannon"));
 		sTurret.setOffset(new Vector2D(0, -5));
 		sTurret.setRotationOffset(new Vector2D(0, 22));
 		sTurret.setAbsRotation(true);
 
 		m_steering = null;
 		m_healthBar = new HealthBar();
 		m_animator = new Animator(2, 75, 0);
 		initialPosition = Vector2D.ZERO;
 		setTeam(team);
 		setType(type);
 		respawn();
 	}
 
 	public Tank(IronLegends game, int iTeam, Team team, int entityNumber) {
 		this(game, iTeam, team, Type.BASIC, entityNumber);
 	}
 
 	// AI Tank
 	public Tank(IronLegends game, int iTeam, Team team, Type type, int entityNumber, boolean AI) {
		this(game, iTeam, team, entityNumber);
 		setPlayerControlled(false);
 		allowRespawn(false);
 		setSteering();
 		FIRE_DELAY = 500;
 		damageAmount = 10;		
 	}
 	
 	// update client's "entity state" from es
 	public void clientUpdate(final EntityState es)
 	{
 		m_entityNumber = es.m_entityNumber;
 		setPosition(es.m_pos);
 		setRotation(es.m_tankRotationRad);
 		m_turretRotationRad = es.m_turretRotationRad;
 		m_team = es.m_team;
 		curSpeed = es.m_speed;
 		health = es.m_health;
 		
 		// maxHealth
 		if ((es.m_flags & EntityState.ESF_TT_BASIC) == EntityState.ESF_TT_BASIC)
 			setType(Type.BASIC);
 		if ((es.m_flags & EntityState.ESF_TT_ARMORED) == EntityState.ESF_TT_ARMORED)
 			setType(Type.ARMORED);
 		if ((es.m_flags & EntityState.ESF_TT_SPEED) == EntityState.ESF_TT_SPEED)
 			setType(Type.SPEEDY);
 		
 		if ((es.m_flags & EntityState.ESF_ACTIVE) == EntityState.ESF_ACTIVE)
 			setActivation(true);
 		else
 			setActivation(false);
 		
 		if ((es.m_flags & EntityState.ESF_PU_DBL_CANNON) == EntityState.ESF_PU_DBL_CANNON)
 			setWeapon(Weapon.DOUBLECANNON);
 		else
 			setWeapon(Weapon.CANNON);
 		
 		if ((es.m_flags & EntityState.ESF_PU_SHIELD) == EntityState.ESF_PU_SHIELD)
 			sShield.setActivation(true);
 		else
 			sShield.setActivation(false);
 
 		/*
 		if ((es.m_flags & EntityState.ESF_PU_MINE) == EntityState.ESF_PU_MINE)
 			sMine.setActivation(true);
 		else
 			sMine.setActivation(false);
 		*/
 	}
 	
 	// fill in an entity state that can later be transmitted
 	public void serverPopulate(EntityState es)
 	{
 		es.m_entityNumber = m_entityNumber ;
 		
 		es.m_pos = getPosition();
 		es.m_tankRotationRad = getRotation();
 		es.m_turretRotationRad = m_turretRotationRad;
 		es.m_team = m_team;
 		es.m_speed = curSpeed;
 		es.m_health = getHealth();
 		es.m_maxHealth = getMaxHealth();
 			
 		es.m_flags = 0;
 		es.m_flags |= (isActive()?EntityState.ESF_ACTIVE:0);
 		es.m_flags |= (curSpeed != 0?EntityState.ESF_MOVING:0);
 		
 		switch(getType())
 		{
 			case ARMORED:
 				es.m_flags |= EntityState.ESF_TT_ARMORED;
 			break;
 			case SPEEDY:
 				es.m_flags |= EntityState.ESF_TT_SPEED;
 			break;
 			case BASIC:
 			default:
 				es.m_flags |= EntityState.ESF_TT_BASIC;
 			break;
 		}
 
 		if (isPowerUpActive(PU_SHIELD))
 			es.m_flags |= EntityState.ESF_PU_SHIELD;
 		if (isPowerUpActive(PU_DAMAGE))
 			es.m_flags |= EntityState.ESF_PU_DBL_CANNON;
 		if (isPowerUpActive(PU_MINE))
 			es.m_flags |= EntityState.ESF_PU_MINE;	
 	}
 	
 	@Override
 	public void update(long deltaMs) {
 		if (!isActive()) {
 			if (respawn) {
 				timeSinceDied += deltaMs;
 				if (timeSinceDied > RESPAWN_DELAY) {
 					respawn();
 				}
 			}
 			return;
 		}
 		
 		timeSinceFired += deltaMs;
 		if (fireSecondBullet && timeSinceFired > 75) {
 			fireBullet();
 		}		
 		
 		if (shield) {
 			timeSinceShield += deltaMs;
 			if (timeSinceShield > MAX_SHIELD_TIME) {
 				setShield(false);
 			}
 		}
 		
 		if (playerControlled) {
 			double rotation = getRotation() + (angularVelocity * deltaMs / 1000.0);
 			Vector2D translateVec = Vector2D.getUnitLengthVector(
 					rotation + Math.toRadians(270)).scale(
 					curSpeed * deltaMs / 1000.0);
 			Vector2D p = position.translate(translateVec);
 			p = p.clampX(0, IronLegends.WORLD_WIDTH - getWidth());
 			p = p.clampY(0, IronLegends.WORLD_HEIGHT - getHeight());
 	
 			setPosition(p);
 			setRotation(rotation);
 			if ((curSpeed != 0 || angularVelocity != 0)
 					&& m_animator.update(deltaMs, translateVec)) {
 				getSprite(0).setFrame(m_animator.getFrame());
 			}
 		} else {
 			AIMovement(deltaMs);
 		}
 	}
 	public void serverControlMovement(KeyCommands m_keyCmds, Mouse mouse, CommandState cs) {
 		// server
 		{			
 			if (cs.isActive(CommandState.CMD_UP))
 				curSpeed = SPEED;
 	
 			if (cs.isActive(CommandState.CMD_DOWN))
 				curSpeed = -SPEED;
 	
 			if (!cs.isActive(CommandState.CMD_UP) && 
 				!cs.isActive(CommandState.CMD_DOWN) )
 				stopMoving();
 	
 			if (cs.isActive(CommandState.CMD_LEFT))
 				angularVelocity = -TURN_RATE;
 	
 			if (cs.isActive(CommandState.CMD_RIGHT))
 				angularVelocity = TURN_RATE;
 	
 			if (!cs.isActive(CommandState.CMD_LEFT) &&
 				!cs.isActive(CommandState.CMD_RIGHT) )
 				stopTurning();
 	
 			if (cs.isActive(CommandState.CMD_FIRE))
 				fire();
 		}		
 	}
 
 	public void clientControlMovement(KeyCommands m_keyCmds, Mouse mouse) {
 
 		// can turret fixed stay at client if send turret rotation to server?
 		// why not use wasPressed?
 		// turret rotation can get set here for client, but needs to send command to server
 		if (m_keyCmds.wasReleased("fixturret")) {
 			fixturret = !fixturret;
 		}
 
 		if (fixturret) {
 			m_turretRotationRad = getRotation();
 			setTurretRotation(m_turretRotationRad);
 		} else {
 			Point loc = mouse.getLocation();
 			Vector2D tankCenterPos = getShapeCenter();
 			Vector2D mousePt = game.m_mapCalc.screenToWorld(new Vector2D(loc.x,
 					loc.y));
 			m_turretRotationRad = tankCenterPos.angleTo(mousePt) + Math.toRadians(90);
 			
 			setTurretRotation(m_turretRotationRad);
 		}
 	}
 
 	private void AIMovement(long deltaMs) {
 		double dist = 0.0;
 		double target_angle = 0.0;
 		
 		if (target == null || !target.isActive()) {
 			m_steering.setBehavior(SteeringBehavior.Behavior.WANDER);
 		} else {
 			Vector2D tp = target.getCenterPosition();
 			Vector2D sp = getCenterPosition();
 			dist = Math.sqrt(tp.distance2(sp));
 			target_angle = sp.angleTo(tp);
 			if (dist <= 3 * bulletRange) { // go towards the target
 				m_steering.setTarget(target);
 				if (dist <= 2 * bulletRange) { // chase
 					m_steering.setBehavior(SteeringBehavior.Behavior.SEEK);
 					m_steering.setTargetBound(bulletRange);					
 				} else {
 					m_steering.setBehavior(SteeringBehavior.Behavior.ARIVE);
 					m_steering.setTargetBound(1.75 * bulletRange);
 				}
 				
 				if (dist <= 1.25 * bulletRange) { // close enough start firing
 					fire();
 				}
 			} else {
 				m_steering.setBehavior(SteeringBehavior.Behavior.WANDER);
 			}
 		}
 
 		m_steering.apply(deltaMs);
 		Vector2D translateVec = getVelocity();
 		if (!translateVec.epsilonEquals(Vector2D.ZERO, 0.01)) { // moving
 			if (m_animator.update(deltaMs, translateVec)) {
 				getSprite(0).setFrame(m_animator.getFrame());
 			}
 			
 			Vector2D p = getCenterPosition().translate(translateVec);
 			p = p.clampX(0, IronLegends.WORLD_WIDTH - getWidth());
 			p = p.clampY(0, IronLegends.WORLD_HEIGHT - getHeight());
 	
 			setCenterPosition(p);
 			setRotation(m_steering.getVectorAngle(translateVec) + Math.toRadians(90));
 			if (m_steering.getBehavior() != SteeringBehavior.Behavior.WANDER) {
 				setTurretRotation(target_angle + Math.toRadians(90));				
 			} else {
 				setTurretRotation(getRotation()); // fix sTurret
 			}
 		} else {
 			setTurretRotation(target_angle + Math.toRadians(90));
 		}
 	}	
 
 	public void respawn() {
 		stopMoving();
 		stopTurning();
 		setCenterPosition(initialPosition);
 		setRotation(initialRotDeg);
 		setTurretRotation(initialRotDeg);
 		setHealth(MAX_HEALTH);
 		setWeapon(Weapon.CANNON);
 		setActivation(true);
 		setShield(true);
 		MAX_SHIELD_TIME = 2000; // initial temp shield
 		
 		m_animator.setFrameBase(0);
 		getSprite(0).setFrame(m_animator.getFrame());
 	}
 
 	private void fireBullet() {
 		Bullet b = game.getBullet();
 		b.reload(damageAmount, bulletRange);
 		b.fire(this, getShapeCenter().translate(
 				new Vector2D(0, 20 - 86).rotate(getTurretRotation())),
 				getTurretRotation());	
 		fireSecondBullet = false;
 		game.m_soundFx.play("bullet");		
 	}
 	
 	public void fire() {
 		if (timeSinceFired > FIRE_DELAY) {
 			fireBullet();
 			timeSinceFired = 0;
 			if (weapon == Weapon.DOUBLECANNON) {
 				fireSecondBullet = true;
 			}
 		}
 	}
 
 	public void causeDamage(int damage) {
 		if (shield) {
 			return;
 		}
 		
		health -= damage;
 		if (health <= 0) {
 			explode();
 		}
 	}
 
 	public void explode() {
 		stopMoving();
 		active = false;
 		timeSinceDied = 0;
 		if (playerControlled) { // if died lose all power
 			setType(Type.BASIC);
 		}
 		// TODO: should server send a special effect update?
 		game.m_sfx.play("tankExplosion", getCenterPosition());
 	}
 
 	public void setShield(boolean s) {
 		sShield.setActivation(s);
 		shield = s;
 		MAX_SHIELD_TIME = 10000;
 		timeSinceShield = 0;
 	}
 	
 	public void upgrade() {
 		if (type == Type.BASIC) {
 			setType(Type.SPEEDY);
 		} else if (type == Type.SPEEDY) {
 			setType(Type.ARMORED);
 		}
 	}
 	
 	public void repair() {
 		setHealth(MAX_HEALTH);		
 	}
 	
 	public void doubleCannon() {
 		setWeapon(Weapon.DOUBLECANNON);
 	}
 	
 	@Override
 	public int getWidth() {
 		return getSprite(0).getWidth();
 	}
 
 	@Override
 	public int getHeight() {
 		return getSprite(0).getHeight();
 	}
 
 	@Override
 	public void render(RenderingContext rc) {
 		if (!isActive()) {
 			return;
 		}
 
 		super.render(rc);
 		m_healthBar.render(rc, getPosition(), getHealth(), getMaxHealth(), 10, true);
 	}
 
 	public double getTurretRotation() {
 		return sTurret.getRotation();
 	}
 
 	public void setTurretRotation(double rot) {
 		sTurret.setRotation(rot);
 	}
 
 	public void stopMoving() {
 		curSpeed = 0;
 	}
 
 	public void stopTurning() {
 		angularVelocity = 0.0;
 	}
 
 	public void setHealth(int health) {
 		this.health = health;
 	}
 
 	public int getHealth() {
 		return health;
 	}
 
 	public int getMaxHealth() {
 		return MAX_HEALTH;
 	}
 
 	public void setTeam(Team t) {
 		team = t;
 		sTeam.setFrame(team.ordinal());
 	}
 
 	public Team getTeam() {
 		return team;
 	}
 
 	public void setDamageAmount(int damageAmount) {
 		this.damageAmount = damageAmount;
 	}
 
 	public int getDamageAmount() {
 		return damageAmount;
 	}
 
 	public void setBulletRange(int bulletRange) {
 		this.bulletRange = bulletRange;
 	}
 
 	public int getBulletRange() {
 		return bulletRange;
 	}
 
 	public void setWeapon(Weapon weapon) {
 		this.weapon = weapon;
 		sTurret.setFrame(weapon.ordinal());
 	}
 
 	public Weapon getWeapon() {
 		return weapon;
 	}
 
 	public double getSpeed() {
 		return SPEED;
 	}
 
 	public void setSpeed(int speed) {
 		this.SPEED = speed;
 	}
 
 	public void setType(Type type) {
 		this.type = type;
 		setSpeed((type == Type.SPEEDY ? 500 : 300));
 		sSpeed.setActivation(false);
 		sArmor.setActivation(false);
 		switch (type) {
 		case BASIC:
 			break;
 		case SPEEDY:
 			sSpeed.setActivation(true);
 			break;
 		case ARMORED:
 			sArmor.setActivation(true);
 			break;
 		}
 	}
 
 	public Type getType() {
 		return type;
 	}
 
 	public static Type getType(int t) {
 		Type[] types = Type.values();
 		return types[t];
 	}
 	
 	public void setScore(int score) {
 		this.score = score;
 	}
 
 	public int getScore() {
 		return score;
 	}
 
 	public void addPoints(int p) {
 		this.score += p;
 	}
 
 	public void setPlayerControlled(boolean playerControlled) {
 		this.playerControlled = playerControlled;
 	}
 
 	public boolean isPlayerControlled() {
 		return playerControlled;
 	}
 
 	public void allowRespawn(boolean respawn) {
 		this.respawn = respawn;
 	}
 
 	public void setTarget(Body target) {
 		this.target = target;
 	}
 
 	public Body getTarget() {
 		return target;
 	}
 
 	public void setGame(IronLegends game) {
 		this.game = game;
 	}
 
 	public IronLegends getGame() {
 		return game;
 	}
 
 	public void setSpawn(SpawnInfo s) {
 		initialPosition = s.centerPosition();
 		initialRotDeg  = s.rotDeg();
 		setCenterPosition(initialPosition);
 		setRotation(initialRotDeg);
 		setTurretRotation(initialRotDeg);		
 	}
 
 	public boolean isPowerUpActive(int name) {
 
 		boolean bActive = false;
 		
 		switch (name)
 		{
 			case PU_SHIELD:
 				if (sShield.isActive())
 					bActive = true;
 			break;
 			case PU_SPEED:
 				if (sSpeed.isActive())
 					bActive = true;
 			break;
 			case PU_ARMOR:
 				if (sArmor.isActive())
 					bActive = true;
 			break;
 				/*
 			case PU_MINE:
 				if (sMine.isActive())
 					return true;
 					break;
 					*/
 			case PU_DAMAGE:
 				if (weapon == Weapon.DOUBLECANNON)
 					bActive = true;
 			break;
 		}
 		return bActive;
 	}
 
 	public int getEntityNumber() {
 		return m_entityNumber;
 	}
 
 	public void setSteering() {
 		m_steering = new SteeringBehavior(this);
 		m_steering.setWorldbounds(game.m_mapCalc.getWorldBounds());
 		m_steering.setMaxSpeed(SPEED);
 	}
 	
 	public void setSteering(SteeringBehavior m_steering) {
 		this.m_steering = m_steering;
 	}
 
 	public SteeringBehavior getSteering() {
 		return m_steering;
 	}
 }
