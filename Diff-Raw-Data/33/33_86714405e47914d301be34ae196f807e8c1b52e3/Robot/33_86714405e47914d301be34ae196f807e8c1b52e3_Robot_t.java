 package ca.hilikus.jrobocom.robot;
 
import java.lang.reflect.InvocationTargetException;
 import java.util.Random;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ca.hilikus.jrobocom.GameSettings;
 import ca.hilikus.jrobocom.World;
 import ca.hilikus.jrobocom.WorldInfo;
 import ca.hilikus.jrobocom.player.Bank;
 import ca.hilikus.jrobocom.player.InstructionSet;
 import ca.hilikus.jrobocom.player.ScanResult;
 import ca.hilikus.jrobocom.robot.api.RobotAction;
 import ca.hilikus.jrobocom.robot.api.RobotStatus;
 import ca.hilikus.jrobocom.robot.api.RobotStatusLocal;
 import ca.hilikus.jrobocom.timing.MasterClock;
 
 /**
  * Keeps the state of each robot in the board
  * 
  * @author hilikus
  * 
  */
 public class Robot implements RobotAction, Runnable {
 
     private static int lastSerial = 0;
 
     private final TurnManager turnsControl;
 
     private final World world;
 
     private Logger log = LoggerFactory.getLogger(Robot.class);
 
     private RobotStatusLocal data;
 
     private boolean alive;
 
     private boolean pendingBankChange;
 
     private final Bank[] banks;
 
     private int runningBank;
 
     private int serialNumber;
 
     private final String name;
 
     private final RobotAction control;
 
     private final RobotStatus status;
 
     private final WorldInfo worldProxy;
 
     /**
      * Common constructor for all robots
      * 
      * @param theWorld the world the robots lives in
      * @param clock the clock that controls the robot turns
      * @param banksCount number of banks
      */
     private Robot(World theWorld, MasterClock clock, int banksCount, String pName) {
 	if (theWorld == null || clock == null) {
 	    throw new IllegalArgumentException("Arguments cannot be null");
 	}
 	serialNumber = Robot.getNextSerialNumber();
 
 	world = theWorld;
 	turnsControl = new TurnManager(clock);
 	worldProxy = new WorldPlayerProxy(turnsControl, world);
 	control = new RobotControlProxy(this);
 	status = new RobotStatusProxy(this, world);
 	banks = new Bank[banksCount];
 	name = pName;
     }
 
     /**
      * Creates first robot in the world. It is the responsibility of the caller to start the robot's
      * thread
      * 
      * @param theWorld the environment of the robot
      * @param clock the ticker to control turns
      * @param allBanks the code to execute
      * @param name this robot's name
      */
     public Robot(World theWorld, MasterClock clock, Bank[] allBanks, String name) {
 	this(theWorld, clock, allBanks.length, name);
 
 	Random generator = new Random();
 	int potentialTeamId;
 	do {
 	    potentialTeamId = generator.nextInt(1000);
 	} while (!world.validateTeamId(potentialTeamId));
 
 	data = new RobotData(turnsControl, InstructionSet.SUPER, false, potentialTeamId, 0, allBanks.length);
 
 	for (int pos = 0; pos < allBanks.length; pos++) {
 	    setBank(allBanks[pos], pos);
 	}
 
 	postInit();
     }
 
     /**
      * Creates a child robot. It is the responsibility of the caller to start the robot's thread
      * 
      * @param pSet maximum instruction set supported
      * @param banksCount number of banks
      * @param pMobile true if robot is mobile
      * @param parent creator of this robot
      * @param name a name of this single robot
      */
     public Robot(InstructionSet pSet, int banksCount, boolean pMobile, Robot parent, String name) {
 	this(parent.world, parent.getTurnsControl().clock, banksCount, name);
 
 	if (banksCount > GameSettings.MAX_BANKS) {
 	    throw new IllegalArgumentException("Too many banks");
 	}
 	if (parent.data.getInstructionSet().isLessThan(InstructionSet.SUPER)) {
 	    throw new IllegalArgumentException("Parent could not have created child");
 	}
 
 	data = new RobotData(turnsControl, pSet, pMobile, parent.data.getTeamId(),
 		parent.data.getGeneration() + 1, banksCount);
 
 	postInit();
     }
 
     private void postInit() {
 	alive = data.getGeneration() < GameSettings.MAX_GENERATION;
     }
 
     @Override
     public int reverseTransfer(int localBankIndex, int remoteBankIndex) {
 	if (data.getInstructionSet().isLessThan(InstructionSet.ADVANCED)) {
 	    die("Invalid action in Instruction Set");
 	}
 	Robot neighbour = world.getNeighbour(this);
 	if (neighbour != null) {
	    Bank remoteBank = neighbour.getBankCopy(remoteBankIndex);
 	    setBank(remoteBank, localBankIndex);
 	    return remoteBank.getCost();
 	} else {
 
 	    return 0;
 	}
     }
 
     @Override
     public int transfer(int localBankIndex, int remoteBankIndex) {
 	if (data.getInstructionSet().isLessThan(InstructionSet.ADVANCED)) {
 	    die("Invalid action in Instruction Set");
 	}
 	Robot neighbour = world.getNeighbour(Robot.this);
 	if (neighbour != null) {
	    Bank localBankCopy;
	    try {
		localBankCopy = banks[localBankIndex].getClass().getConstructor().newInstance();

		neighbour.setBank(localBankCopy, remoteBankIndex);
		return localBankCopy.getCost();
	    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
		    | InvocationTargetException | NoSuchMethodException | SecurityException exc) {

		log.error("[transfer] Error instantiating Bank for transfer", exc);
		return 0;
	    }
 	} else {
 	    return 0;
 	}
     }
 
     private static int getNextSerialNumber() {
 	return lastSerial++;
     }
 
     /**
      * The main loop of the robot
      */
     @Override
     public void run() {
 	try {
 	    turnsControl.waitTurns(1); // block at the beginning so that all robots start at the
 				       // same time
 	    while (alive) {
 		if (runningBank == 0) {
 		    if (banks[0] == null || banks[0].isEmpty()) {
 			die("Data Hunger");
 		    }
 		} else {
 		    if (runningBank > banks.length) {
 			if (runningBank >= GameSettings.MAX_BANKS) {
 			    die("Impossible Bank number");
 			} else {
 			    reboot("Bank not found");
 			}
 		    } else if (banks[runningBank] == null || banks[runningBank].isEmpty()) {
 			// tried to execute empty bank, reboot
 			reboot("Tried to execute empty bank");
 		    }
 		}
 
 		if (alive) {
 		    banks[runningBank].run();
 		    if (!pendingBankChange) {
 			reboot("End of bank");
 		    } else {
 			pendingBankChange = false;
 		    }
 		}
 
 	    }
 	} catch (Exception all) {
 	    log.error("[run] Problem running robot " + this, all);
 	    die("Execution Error: " + all.getClass().getName());
 	}
     }
 
     private void reboot(String reason) {
 	log.trace("[run] Rebooting robot: {}", reason);
 	changeBank(0);
     }
 
     @Override
     public void changeBank(int newBank) {
 	log.debug("[changeBank] Changing Bank. Old bank = {}, new bank = {}", runningBank, newBank);
 	runningBank = newBank;
 	pendingBankChange = true; // so the robot doesn't reboot at the end of this bank
 
     }
 
     /**
      * Kills the robot and removes it from the board
      * 
      * @param reason user-friendly explanation for the death
      */
     @Override
     public void die(String reason) {
 	log.debug("[die] Robot {} died with reason: {}", serialNumber, reason);
 	alive = false;
 	world.remove(Robot.this);
     }
 
     @Override
     public void die() {
 	die("No reason given");
 
     }
 
     /**
      * @return the total number of banks (including empty) in the robot
      */
     public int getBanksCount() {
 	if (banks == null) {
 	    return 0;
 	}
 	return banks.length;
     }
 
     private void setBank(Bank bank, int localBankIndex) {
 	if (banks == null || localBankIndex > banks.length) {
 	    throw new IllegalArgumentException("Bank doesn't exist");
 	}
 
 	if (bank != null) {
 	    bank.plugInterfaces(control, status, worldProxy);
 	    banks[localBankIndex] = bank;
 	} else {
 	    banks[localBankIndex] = null;
 	}
     }
 
    private Bank getBankCopy(int localBankIndex) {
 	if (banks == null || localBankIndex > banks.length) {
 	    throw new IllegalArgumentException("Bank doesn't exist");
 	}
 
	try {
	    return banks[localBankIndex].getClass().getConstructor().newInstance();
	} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
		| InvocationTargetException | NoSuchMethodException | SecurityException exc) {

	    log.error("[getBankCopy] Error instantiating copy of Bank to transfer", exc);
	    return null;
	}
     }
 
     /*    private void setTeamId(int teamId) {
     	this.teamId = teamId;
         }
 
         private void setTurns(TurnManager turns) {
     	this.turns = turns;
         }
 
         private void setWorld(World world) {
     	this.world = world;
         }*/
 
     /**
      * Controls waiting of I/O
      * 
      * @author hilikus
      * 
      */
     public class TurnManager {
 
 	private int turnsCounter = 0;
 
 	private MasterClock clock;
 
 	/**
 	 * @param pClock master clock used to schedule tasks
 	 */
 	public TurnManager(MasterClock pClock) {
 	    clock = pClock;
 	}
 
 	/**
 	 * Blocks the robot
 	 * 
 	 * @param turns the number of turns to block
 	 */
 	public void waitTurns(int turns) {
 	    if (turnsCounter > GameSettings.MAX_AGE) {
 		die("Old Age");
 	    } else {
 		clock.waitFor(serialNumber, turns);
 		turnsCounter += turns;
 	    }
 	}
 
 	/**
 	 * @return the number of turns so far
 	 */
 	public int getTurnsCount() {
 	    return turnsCounter;
 	}
     }
 
     /**
      * @return true if the robot is still alive; false otherwise
      */
     public boolean isAlive() {
 	return alive;
     }
 
     TurnManager getTurnsControl() {
 	return turnsControl;
     }
 
     @Override
     public void turn(boolean right) {
 	if (right) {
 	    ((RobotData) data).setFacing(data.getFacing().right());
 	} else {
 	    ((RobotData) data).setFacing(data.getFacing().left());
 	}
 
     }
 
     void setActiveState(int pActiveState) {
 	data.setActiveState(pActiveState);
 
     }
 
     @Override
     public void createRobot(String pName, InstructionSet pSet, int banksCount, boolean pMobile) {
 	if (data.getInstructionSet() != InstructionSet.SUPER) {
 	    die("Robot cannot create other robots");
 	}
 
 	int robotsCount = world.getBotsCount(data.getTeamId(), false);
 	if (data.getGeneration() < GameSettings.MAX_GENERATION && robotsCount < GameSettings.MAX_BOTS) {
 	    Robot child = new Robot(pSet, banksCount, pMobile, this, pName);
 	    world.add(this, child); // world does further verification so add is not guaranteed yet
 	    Thread newThread = new Thread(Thread.currentThread().getThreadGroup(), child, "Robot "
 		    + child.getSerialNumber());
 	    newThread.start(); // jumpstarts the robot
 	}
 
     }
 
     @Override
     public void move() {
 	if (!data.isMobile()) {
 	    die("Robot is not mobile but tried to move");
 	}
 	world.move(this);
 
     }
 
     @Override
     public ScanResult scan() {
 	return scan(1);
     }
 
     @Override
     public ScanResult scan(int maxDist) {
 	if (maxDist < 1) {
 	    throw new IllegalArgumentException("Scan argument has to be positive");
 	}
 
 	if (data.getInstructionSet().isLessThan(InstructionSet.ADVANCED)) {
 	    die("Robot doesn't have Scan operation in its Instruction Set");
 	}
 
 	ScanResult res = null;
 	for (int dist = 1; dist < maxDist; dist++) {
 	    res = world.scan(this, dist);
 	    if (!res.isEmpty()) {
 		return res;
 	    }
 	}
 
 	return res;
     }
 
     /**
      * @return the unique ID of the robot
      */
     public int getSerialNumber() {
 	return serialNumber;
     }
 
     /**
      * @return an object with the state information
      */
     public RobotStatusLocal getData() {
 	return data;
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode() {
 	final int prime = 31;
 	int result = 1;
 	result = prime * result + serialNumber;
 	return result;
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object obj) {
 	if (this == obj) {
 	    return true;
 	}
 	if (obj == null || getClass() != obj.getClass()) {
 	    return false;
 	}
 
 	Robot other = (Robot) obj;
 	return serialNumber == other.serialNumber;
     }
 
     /**
      * @return the name of this robot
      */
     public String getName() {
 	return name;
     }
 
 }
