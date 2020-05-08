 package bots;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import forth2.Interpreter;
 import forth2.InterpreterState;
 import forth2.words.IntegerWord;
 import forth2.words.Word;
 
 public abstract class Bot extends Actor {
 	private static final int TICKCOUNT = 100;
 	private static final double VRANGE_FACTOR = 80.0 * POSITION_SCALE;
 	private static final double SRANGE_FACTOR = 120.0 * POSITION_SCALE;
 	private static final double SPEED_FACTOR = 10.0;
 
 	private final Interpreter interpreter;
 
 
 	protected boolean moving;
 	protected final Random rnd;
 	private final int maxX;
 	private final int maxY;
 	private List<Actor> sightings;
 	private Bot target;
 	private int energy = 100;
 	private int hp = 100;
 	private Bot display_shot;
 
 	public Bot(String program, Faction color, Random rnd, int maxX, int maxY) {
 		super(ActorType.Bot, color);
 		this.rnd = rnd;
 		this.maxX = maxX * POSITION_SCALE;
 		this.maxY = maxY * POSITION_SCALE;
 
 		interpreter = new Interpreter(program);
 		injectWords();
 	}
 
 	@Override
 	public void setPosition(int x, int y) {
		this.x = Math.min(maxX, (x * POSITION_SCALE));
		this.y = Math.min(maxY, (y * POSITION_SCALE));
 	}
 
 	public int getHP() {
 		return hp;
 	}
 
 	public void turn(Map map, List<Actor> bots) {
 		interpreter.turn(TICKCOUNT);
 
 		final Ground ground = map.get(x / POSITION_SCALE, y / POSITION_SCALE);
 
 		updateSightings(bots, ground);
 
 		if (target != null && energy >= 100) {
 			shoot();
 		}
 
 		if (moving) {
 			double speed = SPEED_FACTOR * getSpeed(ground);
 			final int dx = (int) (Math.round(speed * Math.cos(Math.toRadians(direction))));
 			final int dy = (int) (Math.round(speed * Math.sin(Math.toRadians(direction))));
			x = Math.min(maxX, x + dx);
			y = Math.min(maxY, y - dy); /* minus, because (0,0) is top left */
 
 			if (x < 0) x = 0;
 			if (y < 0) y = 0;
 			if (x >= maxX) x = maxX-1;
 			if (y >= maxY) y = maxY-1;
 		}
 
 		hp = Math.min(100, hp + 5);
 		energy = Math.min(100, energy + getEnergyRefill());
 	}
 
 	private void shoot() {
 		energy = 0;
 		target.damage(getDamage());
 		display_shot = target;
 	}
 
 	private void damage(int damage) {
 		hp -= damage * getArmorModificator();
 	}
 
 	private void updateSightings(List<Actor> bots, final Ground ground) {
 		sightings = new LinkedList<Actor>();
 		target = null;
 		double vrange = (VRANGE_FACTOR * getVisualRange(ground));
 		double srange = (SRANGE_FACTOR * getShootingRange(ground));
 		double min_range = srange;
 		for (Actor b : bots) {
 			if (b.type != ActorType.Bot) continue;
 			double distance = distance(b, this);
 			if (distance < vrange) {
 				sightings.add(b);
 			}
 			if (distance < min_range && !b.color.equals(color)) {
 				target = (Bot) b;
 				min_range = distance;
 			}
 		}
 	}
 
 	private static double distance(Actor a, Actor b) {
 		int dx = Math.abs(a.x - b.x);
 		int dy = Math.abs(a.y - b.y);
 		return Math.sqrt(dx*dx + dy*dy);
 	}
 
 	protected abstract double getSpeed(Ground ground);
 	protected abstract double getVisualRange(Ground ground);
 	protected abstract double getShootingRange(Ground ground);
 	protected abstract int getEnergyRefill();
 	protected abstract int getDamage();
 	protected abstract float getArmorModificator();
 
 	private void injectWords() {
 		interpreter.injectWord(new Word("move!") {
 			@Override
 			public void interpret(InterpreterState state) {
 				moving = true;
 			}
 		});
 		interpreter.injectWord(new Word("stop!") {
 			@Override
 			public void interpret(InterpreterState state) {
 				moving = false;
 			}
 		});
 		interpreter.injectWord(new Word("turn!") {
 			@Override
 			public void interpret(InterpreterState state) {
 				IntegerWord a = (IntegerWord) state.stack.pop();
 				direction = a.value;
 			}
 		});
 		interpreter.injectWord(new Word("direction") {
 			@Override
 			public void interpret(InterpreterState state) {
 				state.stack.push(new IntegerWord(direction));
 			}
 		});
 		interpreter.injectWord(new Word("color") {
 			@Override
 			public void interpret(InterpreterState state) {
 				state.stack.push(new IntegerWord(color.ordinal()));
 			}
 		});
 		interpreter.injectWord(new Word("rand") {
 			@Override
 			public void interpret(InterpreterState state) {
 				state.stack.push(new IntegerWord(rnd.nextInt()));
 			}
 		});
 		interpreter.injectWord(new Word("randBounded") {
 			@Override
 			public void interpret(InterpreterState state) {
 				IntegerWord a = (IntegerWord) state.stack.pop();
 				state.stack.push(new IntegerWord(rnd.nextInt(a.value)));
 			}
 		});
 	}
 
 	@Override
 	public void paint(Graphics g, Component observer) {
 		super.paint(g, observer);
 
 		/* display shooting? */
 		if (display_shot != null) {
 			g.setColor(Color.WHITE);
 			g.drawLine(x/POSITION_SCALE, y/POSITION_SCALE, display_shot.x/POSITION_SCALE, display_shot.y/POSITION_SCALE);
 			display_shot = null;
 		}
 	}
 }
