 package autoeq.modules.melee;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 import autoeq.ExpressionEvaluator;
 import autoeq.TargetPattern;
 import autoeq.ThreadScoped;
 import autoeq.eq.ChatListener;
 import autoeq.eq.Command;
 import autoeq.eq.EverquestSession;
 import autoeq.eq.ExpressionRoot;
 import autoeq.eq.Listener;
 import autoeq.eq.Me;
 import autoeq.eq.Module;
 import autoeq.eq.Spawn;
 import autoeq.eq.SpawnType;
 import autoeq.eq.UserCommand;
 import autoeq.ini.Section;
 import autoeq.modules.target.TargetModule;
 
 import com.google.inject.Inject;
 
 @ThreadScoped
 public class MeleeModule implements Module {
   private static final Pattern MELEE_PROBLEM = Pattern.compile("(Your target is too far away, get closer!|You cannot see your target\\.)");
 
   private final EverquestSession session;
   private final TargetModule targetModule;
 
   private final String validTargets;
   private final List<String> conditions;
 
   private String targetType;
   private Spawn mainTarget;
   private int resetCount;
   private int range = 50;
   private boolean attacking;
   private boolean active;
 
   @Inject
   public MeleeModule(final EverquestSession session, TargetModule targetModule) {
     this.session = session;
     this.targetModule = targetModule;
 
     Section section = session.getIni().getSection("Melee");
 
     active = section.getDefault("Active", "true").toLowerCase().equals("true");
     targetType = section.getDefault("TargetType", "assist");
     validTargets = section.getDefault("ValidTargets", "war pal shd mnk rog ber rng bst brd clr shm dru enc mag nec wiz pet");
     conditions = section.getAll("Condition");
 
     if(targetType == null || validTargets == null) {
       throw new RuntimeException("Melee section must contain 'TargetType' and 'ValidTargets'");
     }
 
     session.addChatListener(new ChatListener() {
       @Override
       public Pattern getFilter() {
         return MELEE_PROBLEM;
       }
 
       @Override
       public void match(Matcher matcher) {
         resetCount++;
       }
     });
 
     session.onZoned().call(new Listener() {
       @Override
       public void execute() {
         mainTarget = null;
         resetCount = 0;
         attacking = false;
       }
     });
 
     session.addUserCommand("melee", Pattern.compile("(off|on|status|range ([0-9]+))"), "(off|on|status|range <range>)", new UserCommand() {
       @Override
       public void onCommand(Matcher matcher) {
         if(matcher.group(1).equals("off")) {
           active = false;
         }
         else if(matcher.group(1).equals("on")) {
           active = true;
         }
         else if(matcher.group(1).startsWith("range ")) {
           range = Integer.parseInt(matcher.group(2));
         }
 
         session.echo("==> Melee is " + (active ? "on" : "off") + ".  Range is " + range + ".  Mode is: " + targetType + ".");
       }
     });
   }
 
   @Override
   public List<Command> pulse() {
     Me me = session.getMe();
 
     if((!me.isMoving() || me.isBard()) && me.getType() == SpawnType.PC && active && session.tryLockMovement()) {
       try {
         String meleeStatus = me.getMeleeStatus();
 
         if(mainTarget != null) {
           mainTarget = session.getSpawn(mainTarget.getId());   // Regets the target by ID from the actual spawns in the zone.  If spawn depopped, then this returns null.
         }
 
        if((mainTarget == null && attacking) || (mainTarget != null && (mainTarget.getType() != SpawnType.NPC || mainTarget.getDistance() > range))) {
          session.echo("MELEE: Holding (attacking = " + attacking + "; mainTarget = " + mainTarget + ")");
           mainTarget = null;
          session.doCommand("/attack off");
 
           if(attacking) {
             session.unlockMovement();
             attacking = false;
           }
         }
 
         if(mainTarget == null) {
           Spawn target = targetModule.getMainAssistTarget();
 
           if(target != null && isValidTarget(target) && target.getDistance() < range) {
             mainTarget = target;
 //            session.echo("MELEE: Assisting on [ " + mainTarget.getName() + " ]");
           }
         }
         else {
           if(!meleeStatus.contains("ENGAGED") || (!meleeStatus.contains("MELEE") && "WAR SHD PAL RNG BRD BST MNK BER".contains(me.getClassShortName()))) {
             session.doCommand("/stand");
             session.doCommand("/target id " + mainTarget.getId());
 
             if(session.delay(1000, "${Target.ID} == " + mainTarget.getId())) {
               session.echo("MELEE: Attacking [ " + mainTarget.getName() + " ] -- status = " + me.getMeleeStatus());
               session.doCommand("/killthis");
 //              session.delay(1000);
 //              session.doCommand("/killthis");
 
               if(!attacking) {
                 attacking = true;
                 resetCount = 0;
 
                 session.lockMovement();
               }
             }
           }
           else if(attacking && resetCount > 7) {
             session.doCommand("/melee reset");
             session.delay(1000);
             resetCount = 0;
           }
 //          else if(attacking) {
 //            session.echo("Attack State = " + me.getMeleeStatus());
 //          }
         }
       }
       finally {
         session.unlockMovement();
       }
     }
 
     return null;
   }
 
   private boolean isValidTarget(Spawn spawn) {
     if(spawn.isEnemy()) {
       if(TargetPattern.isValidTarget(validTargets, spawn)) {
         if(ExpressionEvaluator.evaluate(conditions, new ExpressionRoot(session, spawn, null, null), this)) {
           return true;
         }
       }
     }
 
     return false;
   }
 
   @Override
   public boolean isLowLatency() {
     return false;
   }
 }
