 package sos;
 
 import java.util.*;
 
 /**
  * This class contains the simulated operating system (SOS).  Realistically it
  * would run on the same processor (CPU) that it is managing but instead it uses
  * the real-world processor in order to allow a focus on the essentials of
  * operating system design using a high level programming language.
  *
  * @author Vincent Clasgens, Aaron Dobbe, Fernando Freire, Et Begert
  */
    
 public class SOS implements CPU.TrapHandler 
 {
 	
     //======================================================================
     //Constants
     //----------------------------------------------------------------------
 
     //These constants define the system calls this OS can currently handle
     public static final int SYSCALL_EXIT     = 0;    /* exit the current program */
     public static final int SYSCALL_OUTPUT   = 1;    /* outputs a number */
     public static final int SYSCALL_GETPID   = 2;    /* get current process id */
     public static final int SYSCALL_OPEN     = 3;    /* access a device */
     public static final int SYSCALL_CLOSE    = 4;    /* release a device */
     public static final int SYSCALL_READ     = 5;    /* get input from device */
     public static final int SYSCALL_WRITE    = 6;    /* send output to device */
     public static final int SYSCALL_COREDUMP = 9;    /* print process state and exit */
     
     //These constants define op codes for device access
     public static final int OP_SUCCESS		 	  =  0;	 /* Device operation successful */
     public static final int OP_DEVICE_DNE	 	  = -1;	 /* Device does not exist */
     public static final int OP_DEV_NOT_SHAREABLE  = -2;  /* Device is not shareable */
     public static final int OP_DEV_ALREADY_OPEN   = -3;  /* Device has already been opened by the process*/
     public static final int OP_DEV_NOT_OPENED     = -4;  /* Illegal device access */
     public static final int OP_DEV_READONLY       = -5;  /* Device is ready-only */
     public static final int OP_DEV_WRITEONLY      = -6;  /* Device is write-only */
     
     //======================================================================
     //Member variables
     //----------------------------------------------------------------------
 
     /**
      * This flag causes the SOS to print lots of potentially helpful
      * status messages
      **/
     public static final boolean m_verbose = false;
     
     /**
      * The CPU the operating system is managing.
      **/
     private CPU m_CPU = null;
     
     /**
      * The RAM attached to the CPU.
      **/
     private RAM m_RAM = null;
     
     /**
      * The current process running on the CPU.
      */
     private ProcessControlBlock m_currProcess = null;
     
     /**
      * The vector of currently attached I/O devices.
      */
     private Vector<DeviceInfo> m_devices = null;
 
     /*======================================================================
      * Constructors & Debugging
      *----------------------------------------------------------------------
      */
     
     /**
      * The constructor does nothing special
      */
     public SOS(CPU c, RAM r)
     {
         //Init member list
         m_CPU = c;
         m_RAM = r;
         m_CPU.registerTrapHandler(this);
         m_currProcess = new ProcessControlBlock(42);
         m_devices = new Vector<DeviceInfo>();
     }//SOS ctor
     
     /**
      * Does a System.out.print as long as m_verbose is true
      **/
     public static void debugPrint(String s)
     {
         if (m_verbose)
         {
             System.out.print(s);
         }
     }
     
     /**
      * Does a System.out.println as long as m_verbose is true
      **/
     public static void debugPrintln(String s)
     {
         if (m_verbose)
         {
             System.out.println(s);
         }
     }
     
     /*======================================================================
      * Memory Block Management Methods
      *----------------------------------------------------------------------
      */
 
     //None yet!
     
     /*======================================================================
      * Device Management Methods
      *----------------------------------------------------------------------
      */
 
     /**
      * registerDevice
      *
      * adds a new device to the list of devices managed by the OS
      *
      * @param dev     the device driver
      * @param id      the id to assign to this device
      * 
      */
     public void registerDevice(Device dev, int id)
     {
         m_devices.add(new DeviceInfo(dev, id));
     }//registerDevice
     
     
     /*======================================================================
      * Process Management Methods
      *----------------------------------------------------------------------
      */
 
     //None yet!
     
     /*======================================================================
      * Program Management Methods
      *----------------------------------------------------------------------
      */
 
     /**
      * createProcess
      *
      * @param the program with the instructions to run
      * @param the size of the memory allocated for the program
      */
     public void createProcess(Program prog, int allocSize)
     {
         int[] prog_instructions = prog.export();
         
         // allocate memory
         int base = 500;
         int limit = allocSize;
         
         for(int i = 0; i < prog_instructions.length; i++)
         {
         	m_RAM.write(base + i, prog_instructions[i]);
         }
         
         m_CPU.setBASE(base);
         m_CPU.setLIM(limit);
         m_CPU.setPC(0);
         m_CPU.setSP(limit);
         
     }//createProcess
         
     /*======================================================================
      * Interrupt Handlers
      *----------------------------------------------------------------------
      */
 
     /**
      * interuptIllegalMemoryAccess
      *
      * helper method that prints out an error that notifies the user of illegal memory access
      * and exits
      *
      * @param the address of the illegal memory access
      *
      */
     public void interruptIllegalMemoryAccess(int addr) {
     	System.err.print("Illegal memory access attempted on PC " + m_CPU.getPC() + " at memory address " + addr);
     	System.exit(0);
     }//interruptIllegalMemoryAccess
     
     /**
      * interuptDivideByZero
      *
      * helper method that prints out an error that notifies the user of a divide by
      * zero interrupt and exits
      *
      *
      */
     public void interruptDivideByZero() {
     	System.err.print("Divide by zero error at PC " + m_CPU.getPC());
     	System.exit(0);
     }//interruptDivideByZero
     
     /**
      * interuptIllegalInstruction
      *
      * helper method prints out an error that notifies the user of an illegal instruction
      * and exits
      *
      * @param an array containing
      *
      */
     public void interruptIllegalInstruction(int[] instr) {
     	System.err.print("Illegal instruction attempted at PC " + m_CPU.getPC());
     	System.exit(0);
     }//interruptIllegalInstruction
     
     /*======================================================================
      * System Calls
      *----------------------------------------------------------------------
      */
     
     /**
      * systemCall
      *
      * Handles when a system call occurs, by determining
      * from the number stored at the top of the stack, which
      * system call occurred, and executing the appropriate one
      *
      */
     public void systemCall()
     {
     	int syscall = popHelper();
         switch(syscall){
         	case SYSCALL_EXIT: 		syscall_exit();
         		break;
         	case SYSCALL_OUTPUT: 	syscall_output();
         		break;
         	case SYSCALL_GETPID: 	syscall_getpid();
         		break;
         	case SYSCALL_COREDUMP: 	syscall_coredump();
         		break;
         	case SYSCALL_OPEN: 		syscall_open();
         		break;
         	case SYSCALL_CLOSE:		syscall_close();
         		break;
         	case SYSCALL_READ: 		syscall_read();
         		break;
         	case SYSCALL_WRITE:		syscall_write();
         		break;
         	default:
         		break;
         }
     }
 
     private void syscall_exit () {
     	System.exit(0);
     }//syscall_exit
     
 
     private void syscall_output () {
     	int val = popHelper();
     	System.out.println("OUTPUT: " + val);
     }//syscall_output
     
     /**
      * syscall_getpid
      * 
      * The pid of the current process.
      */
     private void syscall_getpid () {
     	m_currProcess.getProcessId();
     }//syscall_getpid
     
     private void syscall_coredump () {
     	m_CPU.regDump();
     	System.out.println("Top three items on the process stack (descending order): " + popHelper() + " " + popHelper() + " " + popHelper());
     	syscall_exit();
     }//syscall_coredump
     
     /**
      * syscall_open
      * 
      * This functions attempts to open a given device.
      */
     private void syscall_open() {
     	int deviceID = popHelper();
 
     	Object[] deviceCheck = deviceIdentifier(deviceID);
 
     	if((Integer) deviceCheck[1] != OP_SUCCESS) {
     		// Device does not exist.
     		pushHelper((Integer) deviceCheck[1]);
     		return;
     	}
     	DeviceInfo deviceInfo = (DeviceInfo) deviceCheck[0];
     	
     	if ((!deviceInfo.device.isSharable()) && (!deviceInfo.device.isAvailable())){
     		pushHelper(OP_DEV_NOT_SHAREABLE);
     		return;
     	}
     	if(!(deviceInfo.device.isAvailable())){
     		pushHelper(OP_DEV_ALREADY_OPEN);
     		return;
     	}
     	deviceInfo.addProcess(m_currProcess);
     	pushHelper(OP_SUCCESS);
     }//syscall_open
     
     /**
      * syscall_close
      * 
      * This function attempts to close a given device.
      */
     private void syscall_close() {
     	int deviceID = popHelper();
     	
     	Object[] deviceCheck = deviceIdentifier(deviceID);
     	
     	if((Integer) deviceCheck[1] != OP_SUCCESS) {
     		// Device does not exist.
     		pushHelper((Integer) deviceCheck[1]);
     		return;
     	}
     	
     	DeviceInfo deviceInfo = (DeviceInfo) deviceCheck[0];
     	
     	if(!(deviceInfo.procs.firstElement() == m_currProcess)) {
     		pushHelper(OP_DEV_NOT_OPENED);
     		return;
     	}
     	
     	deviceInfo.removeProcess(m_currProcess);
     	pushHelper(OP_SUCCESS);
     }//syscall_close
     
     /**
      * syscall_read
      * 
      * The function attempts to read from a given device.
      */
     private void syscall_read () {
     	int address = popHelper();
     	int deviceID = popHelper();
     	
     	Object[] deviceCheck = deviceIdentifier(deviceID);
     	
     	if((Integer) deviceCheck[1] != OP_SUCCESS) {
     		// Device does not exist.
     		pushHelper((Integer) deviceCheck[1]);
     		return;
     	}
     	
     	DeviceInfo deviceInfo = (DeviceInfo) deviceCheck[0];
     	
     	if(!(deviceInfo.procs.firstElement() == m_currProcess)) {
     		pushHelper(OP_DEV_NOT_OPENED);
     		return;
     	}
     	
     	if(!deviceInfo.device.isReadable()) {
     		pushHelper(OP_DEV_WRITEONLY);
     		return;
     	}
     	
     	int value = deviceInfo.device.read(address);
     	pushHelper(value);
     	pushHelper(OP_SUCCESS);
     }//syscall_read
     
     /**
      * syscall_write
      * 
      * This function attempts to write to a given device.
      */
     private void syscall_write () {
     	int value = popHelper();
     	int address = popHelper();
     	int deviceID = popHelper();
     	
     	Object[] deviceCheck = deviceIdentifier(deviceID);
     	
     	if((Integer) deviceCheck[1] != OP_SUCCESS) {
     		// Device does not exist.
     		pushHelper((Integer) deviceCheck[1]);
     		return;
     	}
     	
     	DeviceInfo deviceInfo = (DeviceInfo) deviceCheck[0];
     	
     	if(!(deviceInfo.procs.firstElement() == m_currProcess)) {
     		pushHelper(OP_DEV_NOT_OPENED);
     		return;
     	}
     	
     	if (!deviceInfo.device.isWriteable()){
     		pushHelper(OP_DEV_READONLY);
     	}
 
     	deviceInfo.device.write(address, value);
     	pushHelper(OP_SUCCESS);
     }//syscall_write
     
     /**
      * deviceIdentifier
      * 
      * This function will iterate over the current vector of devices
      * and attempt to retrieve the device by deviceID.
      * 
      * @param deviceID the ID of the device we want.
      * 
      * @return returnItems Since Java does not support tuple data types
      * we have decided to create a simple two object array to pass back
      * a DeviceInfo object as well as an appropriate OP code if an error
      * is found.
      * 
      */
     private Object[] deviceIdentifier (int deviceID) {
     	Object[] returnItems = new Object[2];
     	
     	Iterator<DeviceInfo> itr = m_devices.iterator();
     	DeviceInfo deviceInfo = null;
     	
     	while(itr.hasNext()) {
     		deviceInfo = itr.next();
     		if (deviceInfo.getId() == deviceID) {
     			
     			returnItems[0] = deviceInfo;
     			returnItems[1] = OP_SUCCESS;
     			return returnItems;
     		}
     	}
    	returnItems[0] = deviceInfo; //should return null
     	returnItems[1] = OP_DEVICE_DNE;
     	return returnItems;
     }//deviceIdentifier
     
     /**
      * pushHelper
      * 
      * Pushes an int to the stack
      *
      * @param the value to be pushed to the stack
      */
     private void pushHelper (int val) {
     	m_CPU.setSP(m_CPU.getSP()-1);
     	m_RAM.write(m_CPU.getBASE() + m_CPU.getSP(), val);
     }//pushHelper
     
     /**
      * popHelper
      * 
      * Pops the top most element from the stack
      */
     private int popHelper () {
     	int syscallOption = m_RAM.read(m_CPU.getBASE()+m_CPU.getSP());
     	m_CPU.setSP(m_CPU.getSP()+1);
     	return syscallOption;
     }//pushHelper
     
     //======================================================================
     // Inner Classes
     //----------------------------------------------------------------------
 
     /**
      * class ProcessControlBlock
      *
      * This class contains information about a currently active process.
      */
     private class ProcessControlBlock
     {
         /**
          * a unique id for this process
          */
         private int processId = 0;
 
         /**
          * constructor
          *
          * @param pid        a process id for the process.  The caller is
          *                   responsible for making sure it is unique.
          */
         public ProcessControlBlock(int pid)
         {
             this.processId = pid;
         }
 
         /**
          * @return the current process' id
          */
         public int getProcessId()
         {
             return this.processId;
         }
 
         
     }//class ProcessControlBlock
     
     /**
      * class DeviceInfo
      *
      * This class contains information about a device that is currently
      * registered with the system.
      */
     private class DeviceInfo
     {
         /** every device has a unique id */
         private int id;
         /** a reference to the device driver for this device */
         private Device device;
         /** a list of processes that have opened this device */
         private Vector<ProcessControlBlock> procs;
 
         /**
          * constructor
          *
          * @param d          a reference to the device driver for this device
          * @param initID     the id for this device.  The caller is responsible
          *                   for guaranteeing that this is a unique id.
          */
         public DeviceInfo(Device d, int initID)
         {
             this.id = initID;
             this.device = d;
             this.procs = new Vector<ProcessControlBlock>();
         }
 
         /** @return the device's id */
         public int getId()
         {
             return this.id;
         }
 
         /** @return this device's driver */
         public Device getDevice()
         {
             return this.device;
         }
 
         /** Register a new process as having opened this device */
         public void addProcess(ProcessControlBlock pi)
         {
             procs.add(pi);
         }
         
         /** Register a process as having closed this device */
         public void removeProcess(ProcessControlBlock pi)
         {
             procs.remove(pi);
         }
 
         /** Does the given process currently have this device opened? */
         public boolean containsProcess(ProcessControlBlock pi)
         {
             return procs.contains(pi);
         }
         
         /** Is this device currently not opened by any process? */
         public boolean unused()
         {
             return procs.size() == 0;
         }
         
     }//class DeviceInfo
 
 };//class SOS
