 package com.greenteam.huntjumper.match;
 
 import com.greenteam.huntjumper.HuntJumperGame;
 import com.greenteam.huntjumper.audio.AudioSystem;
 import com.greenteam.huntjumper.contoller.AbstractJumperController;
 import com.greenteam.huntjumper.contoller.BotController;
 import com.greenteam.huntjumper.contoller.MouseController;
 import com.greenteam.huntjumper.effects.Effect;
 import com.greenteam.huntjumper.effects.particles.ParticleEntity;
 import com.greenteam.huntjumper.effects.particles.ParticleType;
 import com.greenteam.huntjumper.effects.particles.TypedParticleGenerator;
 import com.greenteam.huntjumper.map.AvailabilityMap;
 import com.greenteam.huntjumper.map.Map;
 import com.greenteam.huntjumper.model.*;
 import com.greenteam.huntjumper.model.bonuses.*;
 import com.greenteam.huntjumper.model.bonuses.acceleration.AccelerationBonus;
 import com.greenteam.huntjumper.model.bonuses.coin.Coin;
 import com.greenteam.huntjumper.model.bonuses.gravity.GravityBonus;
 import com.greenteam.huntjumper.model.bonuses.inelastic.InelasticBonus;
 import com.greenteam.huntjumper.parameters.GameConstants;
 import com.greenteam.huntjumper.parameters.ViewConstants;
 import com.greenteam.huntjumper.shaders.Shader;
 import com.greenteam.huntjumper.shaders.ShadersSystem;
 import com.greenteam.huntjumper.utils.Point;
 import com.greenteam.huntjumper.utils.TextUtils;
 import com.greenteam.huntjumper.utils.Utils;
 import com.greenteam.huntjumper.utils.Vector2D;
 import net.phys2d.math.Vector2f;
 import net.phys2d.raw.Body;
 import net.phys2d.raw.CollisionEvent;
 import net.phys2d.raw.StaticBody;
 import net.phys2d.raw.World;
 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.Ellipse;
 import org.newdawn.slick.geom.RoundedRectangle;
 import org.newdawn.slick.opengl.shader.ShaderProgram;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.*;
 
 import static com.greenteam.huntjumper.parameters.GameConstants.COIN_RADIUS;
 import static com.greenteam.huntjumper.parameters.GameConstants.DEFAULT_GAME_TIME;
 import static com.greenteam.huntjumper.parameters.GameConstants.TIME_TO_BECOME_SUPER_HUNTER;
 import static com.greenteam.huntjumper.parameters.ViewConstants.*;
 
 /**
  * User: GreenTea Date: 03.06.12 Time: 16:33
  */
 public class SinglePlayerMatchState extends AbstractMatchState
 {
    private static List<Class<? extends AbstractPhysBonus>> allBonusClasses = new ArrayList<>();
    static
    {
       allBonusClasses.add(AccelerationBonus.class);
       allBonusClasses.add(GravityBonus.class);
       allBonusClasses.add(InelasticBonus.class);
    }
 
    private static ShaderProgram ligthProgram;
    private static void initLightShader()
    {
       if (ligthProgram != null)
       {
          return;
       }
 
       ligthProgram = ShadersSystem.getInstance().getProgram(Shader.LIGHT);
    }
 
    private World world;
    private File mapFile;
    private Map map;
    private List<Jumper> jumpers = new ArrayList<Jumper>();
 
    private Jumper myJumper;
 
    private TimeAccumulator updateTimeAccumulator = new TimeAccumulator();
    private InitializationScreen initializationScreen;
    private ArrowsVisualizer arrowsVisualizer;
    private GameContainer gameContainer;
    private LinkedList<Integer> beforeEndNotifications = new LinkedList<Integer>();
    private boolean gameFinished = false;
 
    private TimeAccumulator createCoinsAccumulator = new TimeAccumulator(
            GameConstants.COIN_APPEAR_INTERVAL);
 
    private TimeAccumulator createPositiveBonusesAccumulator = new TimeAccumulator(
            GameConstants.BONUS_APPEAR_INTERVAL);
    private TimeAccumulator createNeutralBonusesAccumulator = new TimeAccumulator(
            GameConstants.BONUS_APPEAR_INTERVAL);
    private TimeAccumulator createNegativeBonusesAccumulator = new TimeAccumulator(
            GameConstants.BONUS_APPEAR_INTERVAL);
 
    private Set<Coin> coins = new HashSet<>();
    private Set<AbstractPositiveBonus> positiveBonuses = new HashSet<>();
    private Set<AbstractNeutralBonus> neutralBonuses = new HashSet<>();
    private Set<AbstractNegativeBonus> negativeBonuses = new HashSet<>();
    private Set<AbstractPhysBonus> allPhysBonuses = new HashSet<>();
 
    public SinglePlayerMatchState(File mapFile)
    {
       this.mapFile = mapFile;
    }
 
    private void initWorld()
    {
       world = new World(new Vector2f(0f, 0f), 5);
    }
 
    private void initMap()
    {
       try
       {
          initializationScreen.setStatus("Loading map: " + mapFile.getName(), null);
          AvailabilityMap availabilityMap = new AvailabilityMap(mapFile);
 
          map = new Map(availabilityMap);
       }
       catch (IOException e)
       {
          throw new RuntimeException(e);
       }
 
       for (StaticBody body : map.getAllPolygons())
       {
          world.add(body);
       }
    }
 
    private Jumper addJumper(Point startPos, String name, Color color,
                             AbstractJumperController jumperController, JumperRole role)
    {
       Jumper jumper = new Jumper(name, color, startPos.toVector2f(), jumperController, role);
 
       jumpers.add(jumper);
       world.add(jumper.getBody());
       return jumper;
    }
 
    private boolean isStartPointFree(Point p, List<Point> resultJumperPositions,
                                     int currentJumperIndex)
    {
       return map.isCircleFree(p, GameConstants.JUMPER_RADIUS) &&
               p.inRange(resultJumperPositions.subList(0, currentJumperIndex),
                       GameConstants.JUMPER_RADIUS * 2).size() == 0;
    }
 
    private List<Point> getJumperPositionsOnFreePoints(List<Point> initialJumperPositions)
    {
       List<Point> res = new ArrayList<Point>();
 
       float randomStep = GameConstants.JUMPER_RADIUS*5;
 
       Random rand = Utils.rand;
       for (int i = 0; i < initialJumperPositions.size(); ++i)
       {
          Point p = initialJumperPositions.get(i);
          while(!isStartPointFree(p, res, i))
          {
             Vector2D tv = new Vector2D(rand.nextFloat()*randomStep, rand.nextFloat()* randomStep);
             p = p.plus(tv);
 
          }
 
          res.add(p);
       }
 
       return res;
    }
 
    private void initJumpers()
    {
       initializationScreen.setStatus("Init jumpers", null);
 
       gameContainer.setForceExit(false);
       float maxRandomRadius = GameConstants.JUMPERS_START_RADIUS - GameConstants.JUMPER_RADIUS;
 
       List<Point> jumperPositions = Utils.getRotationPoints(
               new Point(0, 0), Utils.rand.nextFloat()*maxRandomRadius, Utils.rand.nextInt(360), 5);
       jumperPositions = getJumperPositionsOnFreePoints(jumperPositions);
 
       myJumper = addJumper(jumperPositions.get(0), "GreenTea", Utils.randomColor(),
               new MouseController(), JumperRole.Escaping);
       addRoleChangeEffect();
       myJumper.setJumperRole(JumperRole.Escaping);
 
       for (int i = 1; i < jumperPositions.size(); ++i)
       {
          addJumper(jumperPositions.get(i), "bot" + i, Utils.randomColor(),
                  new BotController(new BotController.WorldInformationSource() {
                     @Override
                     public List<JumperInfo> getOpponents(Jumper jumper)
                     {
                        List<JumperInfo> jumperInfos = new ArrayList<JumperInfo>();
                        for (Jumper j : jumpers)
                        {
                           if (!j.equals(jumper))
                           {
                              jumperInfos.add(new JumperInfo(j));
                           }
                        }
                        return jumperInfos;
                     }
 
                     @Override
                     public Map getMap()
                     {
                        return map;
                     }
 
                     @Override
                     public java.util.Map<Class<? extends IBonus>, List<Point>> getBonuses()
                     {
                        java.util.Map<Class<? extends IBonus>, List<Point>> res = new HashMap<>();
                        List<Point> positions = new ArrayList<>();
                        res.put(Coin.class, positions);
                        for (Coin c : coins)
                        {
                           positions.add(c.getPosition());
                        }
 
                        for (IBonus bonus : allPhysBonuses)
                        {
                           positions = res.get(bonus.getClass());
                           if (positions == null)
                           {
                              positions = new ArrayList<>();
                              res.put(bonus.getClass(), positions);
                           }
                           positions.add(bonus.getPosition());
                        }
 
                        return res;
                     }
                  }), JumperRole.Hunting);
       }
 
       initOtherJumpers();
 
       arrowsVisualizer = new ArrowsVisualizer(myJumper, jumpers);
       scoresManager = new ScoresManager(jumpers);
    }
 
    private Set<AbstractPhysBonus> collectAllPhysBonuses()
    {
       Set<AbstractPhysBonus> allBonuses = new HashSet<>();
       allBonuses.addAll(positiveBonuses);
       allBonuses.addAll(neutralBonuses);
       allBonuses.addAll(negativeBonuses);
       return allBonuses;
    }
 
    private void initOtherJumpers()
    {
       for (Jumper j : jumpers)
       {
          List<Jumper> other = new ArrayList<>(jumpers);
          other.remove(j);
          j.setOtherJumpers(other);
       }
    }
 
    private void addRoleChangeEffect()
    {
       final HashMap<JumperRole, List<String>> messages = new HashMap<JumperRole, List<String>>();
       messages.put(JumperRole.Escaping,
               Arrays.asList("Run!", "Escape!"));
       messages.put(JumperRole.Hunting,
               Arrays.asList("Catch him!", "Run down him!"));
       messages.put(JumperRole.HuntingForEveryone,
               Arrays.asList("Time for hunting!", "Go to papa..."));
       messages.put(JumperRole.EscapingFromHunter,
               Arrays.asList("Hunter appeared!", "Danger!"));
 
       myJumper.getRoleChangedListeners().add(new IRoleChangedListener()
       {
 
          @Override
          public void signalRoleIsChanged(JumperRole oldRole, final JumperRole newRole)
          {
             List<String> possibleMessages = messages.get(newRole);
             final String text = possibleMessages.get(Utils.rand.nextInt(possibleMessages.size()));
 
             addEffect(new FlyUpTextEffect(myJumper, text, ViewConstants.ROLE_CHANGE_EFFECT_DURATION,
                     newRole.getRoleColor().brighter(), ViewConstants.ROLE_CHANGE_EFFECT_FONT,
                     ViewConstants.ROLE_CHANGE_EFFECT_HEIGHT));
          }
       });
    }
 
    private void initCamera()
    {
       initializationScreen.setStatus("Init camera", null);
       Camera.instance = new Camera(this, new Point(myJumper.getBody().getPosition()),
               ViewConstants.VIEW_WIDTH, ViewConstants.VIEW_HEIGHT);
    }
 
    public void addEffect(Effect effect)
    {
       EffectsContainer.getInstance().addEffect(effect);
    }
 
    @Override
    public void init()
    {
       EffectsContainer.getInstance().clearEffects();
       gameContainer = HuntJumperGame.getInstance().getGameContainer();
       initializationScreen = InitializationScreen.getInstance();
       beforeEndNotifications.addAll(GameConstants.NOTIFY_TIMES_BEFORE_END);
 
       new Thread(new Runnable()
       {
          @Override
          public void run()
          {
             initWorld();
             initMap();
             initJumpers();
             initCamera();
             initialized = true;
          }
       }).start();
    }
 
    public void update(int delta) throws SlickException
    {
       if (!initialized)
       {
          initializationScreen.update(delta);
          return;
       }
 
       int cycles = updateTimeAccumulator.update(delta);
       for (int i = 0; i < cycles; i++)
       {
          int dt = updateTimeAccumulator.getCycleLength();
          EffectsContainer.getInstance().updateEffects(dt);
          if (gameFinished)
          {
             continue;
          }
 
          world.step(0.001f * dt);
          Camera.getCamera().update(dt);
          for (Jumper j : jumpers)
          {
             j.update(dt);
          }
 
          updateCoins(dt);
          updateBonuses(dt);
          processTakingBonuses();
          updateJumperToJumperCollisions();
          updateRolesByTimer();
          scoresManager.update(dt);
          checkGameIsFinished();
       }
       AudioSystem.getInstance().update(delta);
    }
 
    private void updateCoins(int dt)
    {
       for (Coin c : coins)
       {
          c.update(dt);
       }
 
       processTakingCoins();
       createNewCoin(dt);
    }
 
    private void updateBonuses(int dt)
    {
       for (AbstractPhysBonus b : allPhysBonuses)
       {
          b.update(dt);
       }
 
       processTakingBonuses();
       createNewBonus(createPositiveBonusesAccumulator,
               dt, AbstractPositiveBonus.class, positiveBonuses);
       createNewBonus(createNeutralBonusesAccumulator,
               dt, AbstractNeutralBonus.class, neutralBonuses);
       createNewBonus(createNegativeBonusesAccumulator,
               dt, AbstractNegativeBonus.class, negativeBonuses);
       allPhysBonuses = collectAllPhysBonuses();
    }
 
    private void processTakingCoins()
    {
       Iterator<Coin> i = coins.iterator();
       A: while (i.hasNext())
       {
          final Coin c = i.next();
 
          for (Jumper j : jumpers)
          {
             if (c.getPosition().distanceTo(new Point(j.getBody().getPosition())) <
                     GameConstants.JUMPER_RADIUS + GameConstants.COIN_RADIUS)
             {
                i.remove();
 
                c.onBonusTaken(this, j);
 
                continue A;
             }
          }
       }
    }
 
    private void processTakingBonuses()
    {
       for (Jumper j : jumpers)
       {
          CollisionEvent[] collisions = world.getContacts(j.getBody());
          if (collisions != null && collisions.length > 0)
          {
             for (CollisionEvent e : collisions)
             {
                Body bodyA = e.getBodyA();
                Body bodyB = e.getBodyB();
 
                AbstractPhysBonus bonus = getUserDataOfClass(bodyA, AbstractPhysBonus.class);
                if (bonus == null)
                {
                   bonus = getUserDataOfClass(bodyB, AbstractPhysBonus.class);
                }
                if (bonus != null)
                {
                   bonus.onBonusTaken(this, j);
                   world.remove(bonus.getBody());
                   if (bonus instanceof AbstractPositiveBonus)
                   {
                      positiveBonuses.remove(bonus);
                   }
                   else if (bonus instanceof AbstractNeutralBonus)
                   {
                      neutralBonuses.remove(bonus);
                   }
                   else
                   {
                      negativeBonuses.remove(bonus);
                   }
                   allPhysBonuses.remove(bonus);
                }
             }
          }
       }
    }
 
    private void createNewCoin(int dt)
    {
       if (createCoinsAccumulator.update(dt) == 0 || coins.size() >= GameConstants.MAX_COINS_ON_MAP)
       {
          return;
       }
 
       Point pos = getRandomBonusPos(COIN_RADIUS);
       Coin c = new Coin(pos);
       coins.add(c);
    }
 
    private AbstractPhysBonus.WorldInformationSource createWorldInfo()
    {
       return new AbstractPhysBonus.WorldInformationSource()
       {
          @Override
          public List<JumperInfo> getJumpers()
          {
             List<JumperInfo> res = new ArrayList<>();
             for (Jumper j : jumpers)
             {
                res.add(new JumperInfo(j));
             }
 
             return res;
          }
       };
    }
 
    private <T extends AbstractPhysBonus> void createNewBonus(TimeAccumulator timeAccumulator,
                                                              int dt, Class<T> bonusClazz,
                                                              Set<T> bonuses)
    {
       if (timeAccumulator.update(dt) == 0 ||
               bonuses.size() >= GameConstants.MAX_BONUSES_OF_1_TYPE_ON_MAP)
       {
          return;
       }
 
       Point pos = getRandomBonusPos(GameConstants.MAX_BONUS_RADIUS);
       T bonus = createRandomBonus(pos, bonusClazz);
       world.add(bonus.getBody());
       bonuses.add(bonus);
    }
 
    private <T extends AbstractPhysBonus> T createRandomBonus(Point pos, Class<T> bonusClazz)
    {
       List<Class<T>> bonusClasses = new ArrayList<>();
       for (Class c : allBonusClasses)
       {
          if (bonusClazz.isAssignableFrom(c))
          {
             bonusClasses.add(c);
          }
       }
 
       Class<? extends AbstractPhysBonus> bonusClass = bonusClasses.get(
               Utils.rand.nextInt(bonusClasses.size()));
 
       T res;
       try
       {
          Constructor c = bonusClass.getConstructor(
                  AbstractPhysBonus.WorldInformationSource.class, Point.class);
          res = (T)c.newInstance(createWorldInfo(), pos);
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);
       }
 
       return res;
    }
 
    private Point getRandomBonusPos(float bonusRadius)
    {
       Random rand = Utils.rand;
       int appearRadius = (int)(0.9 * map.getWidth() / 2);
 
       Point pos;
       do
       {
          Vector2D createVector = Vector2D.fromAngleAndLength(rand.nextFloat() * 360,
                  rand.nextFloat()*appearRadius);
          pos = new Point(createVector.getX(), createVector.getY());
       }
       while (!map.isCircleFree(pos, bonusRadius));
       return pos;
    }
 
    private String makeWinnersString(List<Jumper> winners)
    {
       StringBuilder sb = new StringBuilder(winners.size() == 1 ? "Winner is " : "Winners are ");
       for (int i = 0; i < winners.size(); ++i)
       {
          sb.append(winners.get(i).getPlayerName()).append(", ");
       }
       if (winners.size() > 0)
       {
          sb.delete(sb.length() - 2, sb.length());
       }
 
       return sb.toString();
    }
 
    public void checkGameIsFinished()
    {
       int totalTime = updateTimeAccumulator.getTotalTimeInMilliseconds();
       if (!gameFinished && totalTime > GameConstants.DEFAULT_GAME_TIME)
       {
          gameFinished = true;
 
          final String text1 = "GAME OVER";
          final String text2 = makeWinnersString(scoresManager.calcWinners());
 
          final Point pos = new Point(gameContainer.getWidth() / 2, gameContainer.getHeight() / 2);
          addEffect(new Effect()
          {
             @Override
             public int getDuration()
             {
                return Integer.MAX_VALUE;
             }
 
             @Override
             public void draw(Graphics g)
             {
                Font font = ViewConstants.WINNER_BOX_FONT;
                final float indentFactor = ViewConstants.WINNER_BOX_INDENT_FACTOR;
 
                Point text1Pos1 = new Point(pos);
                Point text1Pos2 = pos.plus(new Vector2D(1, 1));
                Point text2Pos1 = pos.plus(new Vector2D(0, indentFactor*font.getHeight(text1)));
                Point text2Pos2 = text2Pos1.plus(new Vector2D(1, 1));
 
                int maxWidth = Math.max(font.getWidth(text1), font.getWidth(text2));
                int maxHeight = Math.max(font.getHeight(text1), font.getHeight(text2));
                Point boxPos = new Point(text1Pos1.getX() - maxWidth/2 - maxHeight*indentFactor/2,
                        text1Pos1.getY() - maxHeight*indentFactor);
                float boxWidth = maxWidth + maxHeight*indentFactor;
                float boxHeight = maxHeight*3*indentFactor;
                g.setColor(Utils.toColorWithAlpha(ViewConstants.WINNER_BOX_COLOR,
                        ViewConstants.WINNER_BOX_RECTANGLE_ALPHA));
                g.fill(new RoundedRectangle(boxPos.getX(), boxPos.getY(), boxWidth, boxHeight,
                        ViewConstants.WINNER_BOX_RECTANGLE_CORNER_RADIUS));
 
                Color c = ViewConstants.WINNER_BOX_FONT_BACK_COLOR;
                TextUtils.drawTextInCenter(text1Pos1, text1, c, font, g);
 
                c = ViewConstants.WINNER_BOX_FONT_FRONT_COLOR;
                TextUtils.drawTextInCenter(text1Pos2, text1, c, font, g);
 
                c = ViewConstants.WINNER_BOX_FONT_BACK_COLOR;
                TextUtils.drawTextInCenter(text2Pos1, text2, c, font, g);
 
                c = ViewConstants.WINNER_BOX_FONT_FRONT_COLOR;
                TextUtils.drawTextInCenter(text2Pos2, text2, c, font, g);
             }
          });
       }
 
       int x = gameContainer.getWidth() / 2;
       int y = ViewConstants.TIMER_INDENT_FROM_TOP + (int)(TIMER_ELLIPSE_VERTICAL_RADIUS *2.5f);
       final Point pos = new Point(x, y);
 
       Iterator<Integer> i = beforeEndNotifications.iterator();
       final int timeToEnd = GameConstants.DEFAULT_GAME_TIME - totalTime;
       while (i.hasNext())
       {
          int notificationTime = i.next();
          if (timeToEnd < notificationTime)
          {
             i.remove();
             addEffect(new Effect()
             {
                @Override
                public int getDuration()
                {
                   return BEFORE_END_NOTIFICATION_DURATION;
                }
 
                @Override
                public void draw(Graphics g)
                {
                   float ep = getExecutionPercent();
                   float angle = ep * 2 * (float) Math.PI *
                           BEFORE_END_NOTIFICATION_BLINKS_PER_SEC * BEFORE_END_NOTIFICATION_DURATION /1000;
                   float alpha = (1 + (float) Math.cos(angle)) / 2;
 
                   int endAfterTime = GameConstants.DEFAULT_GAME_TIME -
                           updateTimeAccumulator.getTotalTimeInMilliseconds();
                   String text = "End after " + Utils.getTimeString(endAfterTime);
 
                   float green = (float)timeToEnd / DEFAULT_GAME_TIME;
                   Color c = new Color(0, 0, 0, alpha);
                   TextUtils.drawTextInCenter(pos, text, c, ViewConstants.BEFORE_END_NOTIFICATION_FONT,
                           g);
 
                   c = new Color(1, green, 0, alpha);
                   Point pos2 = pos.plus(new Vector2D(1, 1));
                   TextUtils.drawTextInCenter(pos2, text, c, ViewConstants.BEFORE_END_NOTIFICATION_FONT,
                           g);
                }
             });
          }
       }
    }
 
    public void updateRolesByTimer()
    {
       boolean makeHunterForEveryone = false;
       for (Jumper j : jumpers)
       {
          if (j.getJumperRole().equals(JumperRole.Escaping) &&
                  j.getTimeInCurrentRole() > TIME_TO_BECOME_SUPER_HUNTER)
          {
             makeHunterForEveryone = true;
             break;
          }
       }
 
       if (makeHunterForEveryone)
       {
          for (Jumper j : jumpers)
          {
             if (j.getJumperRole().equals(JumperRole.Escaping))
             {
                j.setJumperRole(JumperRole.HuntingForEveryone);
             }
             else
             {
                j.setJumperRole(JumperRole.EscapingFromHunter);
             }
          }
       }
    }
 
    private <T> T getUserDataOfClass(Body body, Class<T> clazz)
    {
       Object userData = body.getUserData();
       if (userData != null)
       {
          if (!clazz.isAssignableFrom(userData.getClass()))
          {
             userData = null;
          }
       }
 
       return (T)userData;
    }
 
 
    public void updateJumperToJumperCollisions()
    {
       Set<Jumper> executedJumpers = new HashSet<Jumper>();
       boolean myJumperEscaping = false;
 
       for (Jumper j : jumpers)
       {
          if (executedJumpers.contains(j))
          {
             continue;
          }
 
          CollisionEvent[] collisions = world.getContacts(j.getBody());
          if (collisions != null && collisions.length > 0)
          {
             for (CollisionEvent e : collisions)
             {
                Body bodyA = e.getBodyA();
                Body bodyB = e.getBodyB();
                Vector2D collisionVelocity = new Vector2D(bodyA.getVelocity()).minus(
                        new Vector2D(bodyB.getVelocity()));
 
                addCollisionEffect(e, collisionVelocity);
 
                Jumper jumperA = getUserDataOfClass(bodyA, Jumper.class);
                Jumper jumperB = getUserDataOfClass(bodyB, Jumper.class);
 
                float collisionDist = myJumper.getBody().getPosition().distance(e.getPoint());
                boolean hasChangeRole = false;
 
                if (jumperA != null && jumperB != null)
                {
                   executedJumpers.add(jumperA);
                   executedJumpers.add(jumperB);
 
                   JumperRole roleA = jumperA.getJumperRole();
                   JumperRole roleB = jumperB.getJumperRole();
                   if (roleA.ordinal() > roleB.ordinal())
                   {
                      JumperRole tmpRole = roleB;
                      roleB = roleA;
                      roleA = tmpRole;
 
                      Jumper tmpJumper = jumperB;
                      jumperB = jumperA;
                      jumperA = tmpJumper;
                   }
 
                   if (roleA.equals(JumperRole.Escaping) &&
                       roleB.equals(JumperRole.Hunting))
                   {
                      jumperB.setJumperRole(JumperRole.Escaping);
                      jumperA.setJumperRole(JumperRole.Hunting);
                      hasChangeRole = true;
                      myJumperEscaping = myJumper.equals(jumperB);
                   }
                   else if (roleA.equals(JumperRole.EscapingFromHunter) &&
                            roleB.equals(JumperRole.HuntingForEveryone))
                   {
                      jumperB.setJumperRole(JumperRole.Escaping);
                      for (Jumper otherJumper : jumpers)
                      {
                         if (!otherJumper.equals(jumperB))
                         {
                            otherJumper.setJumperRole(JumperRole.Hunting);
                         }
                      }
                      hasChangeRole = true;
                      myJumperEscaping = myJumper.equals(jumperB);
                   }
                }
 
                float volumePercent = Math.max(
                        1 - collisionDist / GameConstants.MAX_SOUNDS_DIST, 0f);
 
                String sound = AudioSystem.COLLISION_SOUND;
                if (hasChangeRole)
                {
                   sound = myJumperEscaping ? AudioSystem.ESCAPING_SOUND :
                           AudioSystem.HUNTING_SOUND;
                }
                else
                {
                   float powerModifier = Math.min(
                           collisionVelocity.length() / ViewConstants.collisionVelocityOfMaxVolume, 1);
                   volumePercent *= powerModifier;
                }
 
                AudioSystem.getInstance().playSound(sound, volumePercent);
             }
          }
       }
    }
 
    private void addCollisionEffect(CollisionEvent e, Vector2D collisionVelocity)
    {
       int particlesCount = (int) (ViewConstants.COLLISIONS_PARTICLES_MAX_COUNT *
               (collisionVelocity.length() / GameConstants.MAX_VELOCITY));
       int particlesDeviation = (int) (ViewConstants.COLLISIONS_PARTICLES_MAX_DEVIATION *
               (collisionVelocity.length() / GameConstants.MAX_VELOCITY));
 
       if (particlesCount > 0)
       {
          Collection<ParticleEntity> particles =
                  new TypedParticleGenerator(ParticleType.SPARK, 0, particlesCount).update(0);
          Random rand = Utils.rand;
          float currAngle = rand.nextFloat()*360f;
          float dAngle = 360f / particlesCount;
 
          EffectsContainer effectsContainer = EffectsContainer.getInstance();
          for (ParticleEntity p : particles)
          {
             p.setPosition(new Point(e.getPoint()));
             p.setDeviation(particlesDeviation);
 
             p.setVelocity(Vector2D.fromAngleAndLength(currAngle,
                     ViewConstants.COLLISIONS_PARTICLES_VELOCITY_FACTOR *
                             GameConstants.JUMPER_RADIUS * 1000 / p.getDuration()));
             currAngle += dAngle;
             effectsContainer.addEffect(p);
          }
       }
    }
 
    public void render(Graphics g) throws SlickException
    {
       if (!initialized)
       {
          initializationScreen.draw(g);
          return;
       }
 
       map.draw(g);
       drawJumpers(g);
       drawLight(g);
 
       drawCoins(g);
       drawBonuses(g);
 
       arrowsVisualizer.draw(g);
       drawInterface(g);
       EffectsContainer.getInstance().drawEffects(g);
    }
 
    Image lightPassability;
    private void drawLight(Graphics g) throws SlickException
    {
       g.setAntiAlias(false);
       if (lightPassability == null)
       {
          lightPassability = new Image(VIEW_WIDTH, VIEW_HEIGHT);
       }
       Graphics passGraphics = lightPassability.getGraphics();
       passGraphics.setAntiAlias(false);
 
       passGraphics.setColor(ILightproof.LIGHT_FREE_COLOR);
       passGraphics.fillRect(0, 0, VIEW_WIDTH, VIEW_HEIGHT);
 
       map.drawBorder(passGraphics);
       for (Jumper j : jumpers)
       {
          j.drawBorder(passGraphics);
       }
       passGraphics.flush();
 
       initLightShader();
       ShadersSystem shadersSystem = ShadersSystem.getInstance();
       final float lightRadius = 300;
       for (Jumper j : jumpers)
       {
          Point viewPos = Camera.getCamera().toView(j.getPosition());
          ligthProgram.bind();
          shadersSystem.setPosition(ligthProgram, viewPos.getX(), viewPos.getY());
          shadersSystem.setResolution(ligthProgram, VIEW_WIDTH, VIEW_WIDTH);
          ligthProgram.setUniform1f("lightCircle", j.getBodyCircle().getRadius() + 2);
          ligthProgram.setUniform3f("color", 1f, 1f, 1f);
 
          lightPassability.bind();
          ligthProgram.setUniform1i("passability", 0);
 
          g.fillRect(viewPos.getX() - lightRadius, viewPos.getY() - lightRadius,
                  2*lightRadius, 2*lightRadius);
 //         g.drawImage(passLocalImage, viewPos.getX() - lightRadius, viewPos.getY() - lightRadius);
       }
 
      ShaderProgram.unbind();
 //      g.drawImage(lightPassability, 0, 0);
       g.setAntiAlias(true);
    }
 
    private void drawJumpers(Graphics g)
    {
       for (Jumper j : jumpers)
       {
          j.draw(g);
       }
    }
 
    public void drawCoins(Graphics g)
    {
       for (Coin c : coins)
       {
          c.draw(g);
       }
    }
 
    public void drawBonuses(Graphics g)
    {
       for (AbstractPhysBonus b : allPhysBonuses)
       {
          b.draw(g);
       }
    }
 
    private void drawInterface(Graphics g)
    {
       scoresManager.draw(g);
       drawTimer(g);
    }
 
    private void drawTimer(Graphics g)
    {
       Font font = TextUtils.Arial30Font;
       String timeStr = Utils.getTimeString(
               Math.min(updateTimeAccumulator.getTotalTimeInMilliseconds(),
                       GameConstants.DEFAULT_GAME_TIME));
       int timerIndentFromTop = ViewConstants.TIMER_INDENT_FROM_TOP;
 
       int textHeight = font.getHeight(timeStr);
       int width = gameContainer.getWidth();
       Point timerPos = new Point(width / 2, timerIndentFromTop + textHeight / 2);
 
       float ellipseVRadius = TIMER_ELLIPSE_VERTICAL_RADIUS;
       float ellipseHRadius = TIMER_ELLIPSE_HORIZONTAL_RADIUS;
       Color ellipseColor = new Color(1f, 1f, 1f, TIMER_ELLIPSE_ALPHA);
       g.setColor(ellipseColor);
       g.fill(new Ellipse(timerPos.getX(), timerPos.getY() + TIMER_ELLIPSE_INDENT_FROM_TEXT,
               ellipseHRadius, ellipseVRadius));
 
       TextUtils.drawTextInCenter(timerPos, timeStr, Color.black, font, g);
    }
 
    public boolean closeRequested()
    {
       return true;
    }
 
    public String getTitle()
    {
       return ViewConstants.GAME_NAME;
    }
 
    public GameContainer getGameContainer()
    {
       return gameContainer;
    }
 
    public Jumper getMyJumper()
    {
       return myJumper;
    }
 
    public List<Jumper> getJumpers()
    {
       return jumpers;
    }
 }
