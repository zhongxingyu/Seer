 package game;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import forth.Interpreter;
 import forth.InterpreterState;
 import forth.words.IntegerWord;
 import forth.words.Word;
 
 public abstract class Bot extends ColoredActor {
 	private static final int MAXIMUM_RADIO_MESSAGES = 10;
 	private static final int TICKCOUNT = 100;
 	private static final double VRANGE_FACTOR = 80.0 * POSITION_SCALE;
 	private static final double SRANGE_FACTOR = 120.0 * POSITION_SCALE;
 	private static final double SPEED_FACTOR = 10.0;
 
 	private final Interpreter interpreter;
 	List<ColoredActor> sightings;
 	private ColoredActor target;
 	private int energy = 100;
 	private ColoredActor display_shot;
 
 	List<ColoredActor> current_sightings;
 	ColoredActor current_sight;
 
 	protected boolean moving;
 	protected final Random rnd;
 	protected final int maxX;
 	protected final int maxY;
 	protected final int startX;
 	protected final int startY;
 	protected final GameBoard board;
 	protected LinkedList<RadioMessage> incomingRadio;
 
 	public Bot(String program, Faction color, Random rnd, int maxX, int maxY, int x, int y, GameBoard board) {
 		super(ActorType.Bot, color);
 		this.rnd = rnd;
 		this.board = board;
 		this.maxX = maxX * POSITION_SCALE;
 		this.maxY = maxY * POSITION_SCALE;
 		assert x <= maxX;
 		assert y <= maxY;
 		this.startX = this.x = Math.min(this.maxX, (x * POSITION_SCALE));
 		this.startY = this.y = Math.min(this.maxY, (y * POSITION_SCALE));
 
 		resetRadio();
 
 		interpreter = new Interpreter(program);
 		injectWords();
 	}
 
 	public void turn(Map map, List<ColoredActor> things) {
 		interpreter.turn(TICKCOUNT);
 
 		final Ground ground = map.getGround(x / POSITION_SCALE, y / POSITION_SCALE);
 
 		updateSightings(things, ground);
 
 		if (target != null && energy >= 100) {
 			shoot();
 		}
 
 		move(ground);
 
 		hp = Math.min(100, hp + 5);
 		energy = Math.min(100, energy + getEnergyRefill());
 	}
 
 	private void move(final Ground ground) {
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
 	}
 
 	private void shoot() {
 		energy = 0;
 		target.damage(getDamage());
 		display_shot = target;
 	}
 
 	private void updateSightings(List<ColoredActor> things, final Ground ground) {
 		sightings = new LinkedList<ColoredActor>();
 		target = null;
 		double vrange = (VRANGE_FACTOR * getVisualRange(ground));
 		double srange = (SRANGE_FACTOR * getShootingRange(ground));
 		double min_range = srange;
 		for (ColoredActor b : things) {
 			double distance = distance(b, this);
 			if (distance < vrange) {
 				sightings.add(b);
 			}
 			if (distance < min_range && !b.color.equals(color)) {
 				target = b;
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
 		interpreter.injectWord(new Word("aim!") {
 			@Override
 			public void interpret(InterpreterState state) {
 				final int ty = startY + ((IntegerWord) state.stack.pop()).value * POSITION_SCALE;
 				final int tx = startX + ((IntegerWord) state.stack.pop()).value * POSITION_SCALE;
 				final double theta = Math.atan2(y - ty, tx - x);
 				direction = (int) Math.round(Math.toDegrees(theta));
 				//System.out.println("From "+x+","+y+" to "+tx+","+ty+" => direction "+direction);
 			}
 		});
 		interpreter.injectWord(new Word("direction") {
 			@Override
 			public void interpret(InterpreterState state) {
 				state.stack.push(new IntegerWord(direction));
 			}
 		});
 		interpreter.injectWord(new Word("position") {
 			@Override
 			public void interpret(InterpreterState state) {
 				int xpos = (x - startX) / POSITION_SCALE;
 				int ypos = (y - startY) / POSITION_SCALE;
 				state.stack.push(new IntegerWord(xpos));
 				state.stack.push(new IntegerWord(ypos));
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
 		interpreter.injectWord(new Word("scan!") {
 			@Override
 			public void interpret(InterpreterState state) {
 				current_sightings = sightings;
 				current_sight = null; /* precaution */
 			}
 		});
 		interpreter.injectWord(new Word("scanNext!") {
 			@Override
 			public void interpret(InterpreterState state) {
 				if (current_sightings.size() == 0) {
 					/* no next available */
 					state.stack.push(new IntegerWord(0));
 					return;
 				}
 				current_sight = current_sightings.remove(current_sightings.size()-1);
 				state.stack.push(new IntegerWord(1)); /* next available now */
 			}
 		});
 		interpreter.injectWord(new Word("scannedColor") {
 			@Override
 			public void interpret(InterpreterState state) {
 				int is_enemy = -1;
 				if (current_sight.color == Faction.Neutral) {
 					is_enemy = 2; /* neutral */
 				} else if (current_sight.color != color) {
 					is_enemy = 0; /* enemy color (also false) */
 				} else {
 					assert (current_sight.color == color);
 					is_enemy = 1; /* own color */
 				}
 				assert is_enemy >= 0;
 				state.stack.push(new IntegerWord(is_enemy));
 			}
 		});
 		interpreter.injectWord(new Word("scannedPosition") {
 			@Override
 			public void interpret(InterpreterState state) {
 				int xpos = (current_sight.x - startX) / POSITION_SCALE;
 				int ypos = (current_sight.y - startY) / POSITION_SCALE;
 				state.stack.push(new IntegerWord(xpos));
 				state.stack.push(new IntegerWord(ypos));
 			}
 		});
 
 		interpreter.injectWord(new Word("look") {
 			@Override
 			public void interpret(InterpreterState state) {
 				int range = POSITION_SCALE * ((IntegerWord) state.stack.pop()).value;
 				if (range > VRANGE_FACTOR * getVisualRange(getGround(x,y))) {
 					state.stack.push(new IntegerWord(Ground.Unknown.ordinal()));
 					return;
 				}
 				final int dx = (int) (Math.round(range * Math.cos(Math.toRadians(direction))));
 				final int dy = (int) (Math.round(range * Math.sin(Math.toRadians(direction))));
 				Ground g = getGround(x+dx, y-dy);
 				//System.out.println("sees "+g+" ("+g.ordinal()+") at "+(x+dx)+","+(y+dy));
 				state.stack.push(new IntegerWord(g.ordinal()));
 			}
 		});
 		interpreter.injectWord(new Word("resetRadio!") {
 			@Override
 			public void interpret(InterpreterState state) {
 				resetRadio();
 			}
 		});
 		interpreter.injectWord(new Word("emptyRadio?") {
 			@Override
 			public void interpret(InterpreterState state) {
 				state.stack.push(new IntegerWord(incomingRadio.isEmpty() ? 1 : 0));
 			}
 		});
 		interpreter.injectWord(new Word("popRadio!") {
 			@Override
 			public void interpret(InterpreterState state) {
 				RadioMessage msg = incomingRadio.pop();
 				state.stack.push(new IntegerWord(msg.d));
 				state.stack.push(new IntegerWord(msg.c));
 				state.stack.push(new IntegerWord(msg.b));
 				state.stack.push(new IntegerWord(msg.a));
 			}
 		});
 		interpreter.injectWord(new Word("sendRadio!") {
 			@Override
 			public void interpret(InterpreterState state) {
				IntegerWord d = (IntegerWord) state.stack.pop();
				IntegerWord c = (IntegerWord) state.stack.pop();
				IntegerWord b = (IntegerWord) state.stack.pop();
 				IntegerWord a = (IntegerWord) state.stack.pop();
 				RadioMessage msg = new RadioMessage(a.value, b.value, c.value, d.value);
 				board.sendRadio(Bot.this, msg);
 			}
 		});
 	}
 
 	protected Ground getGround(int bx, int by) {
 		return board.getGround(bx/POSITION_SCALE, by/POSITION_SCALE);
 	}
 
 	@Override
 	public void paint(Graphics g, Component observer) throws IOException {
 		super.paint(g, observer);
 
 		/* display shooting? */
 		if (display_shot != null) {
 			g.setColor(Color.WHITE);
 			g.drawLine(x/POSITION_SCALE, y/POSITION_SCALE, display_shot.x/POSITION_SCALE, display_shot.y/POSITION_SCALE);
 			display_shot = null;
 		}
 	}
 
 	public void recvRadio(RadioMessage msg) {
 		incomingRadio.add(msg);
 		if (incomingRadio.size() > MAXIMUM_RADIO_MESSAGES) {
 			incomingRadio.pop();
 		}
 	}
 
 	protected void resetRadio() {
 		incomingRadio = new LinkedList<RadioMessage>();
 	}
 }
