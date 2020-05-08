 package cisc_sim;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Controls the flow of the programs in the machine
  * 
  * @author cooniur
  * 
  */
 public class Controller implements Runnable {
 
 	private enum RunMode {
 		STEP_MODE, EXE_MODE
 	}
 
 	private RunMode							_runmode;
 	private List<ControllerEventListener>	_listeners		= new ArrayList<ControllerEventListener>();
 
 	/**
 	 * Register
 	 */
 	private Register						_reg;
 	/**
 	 * Arithmetic/Logical Unit
 	 */
 	private ALU								_alu;
 	/**
 	 * Machine Memory
 	 */
 	private Memory							_mem;
 	/**
 	 * Device
 	 */
 	private Device							_dev;
 
 	private boolean							_stopExecution;
 
 	private byte[][]						_inputBuffer	= null;
 	private int[]							_inputBufferPos	= null;
 	private boolean[]						_inputBufferred	= null;
 
 	public Controller() {
 		this._reg = new Register();
 		this._alu = new ALU(this._reg);
 		this._mem = new Memory();
 		this._dev = new Device();
 		this._runmode = RunMode.EXE_MODE;
 
 		this._inputBuffer = new byte[Device.DEVID_MAX][0];
 		this._inputBufferPos = new int[Device.DEVID_MAX];
 		this._inputBufferred = new boolean[Device.DEVID_MAX];
 		for (int i = 0; i < Device.DEVID_MAX; i++) {
 			this._inputBufferred[i] = false;
 			this._inputBufferPos[i] = 0;
 		}
 	}
 
 	public boolean isStepMode() {
 		return this._runmode == RunMode.STEP_MODE;
 	}
 
 	public void setStepMode() {
 		this._runmode = RunMode.STEP_MODE;
 	}
 
 	public void setExeMode() {
 		this._runmode = RunMode.EXE_MODE;
 	}
 
 	public synchronized void addControllerEventListener(
 			ControllerEventListener listener) {
 		if (this._listeners.indexOf(listener) == -1)
 			this._listeners.add(listener);
 	}
 
 	public synchronized void removeControllerEventListener(
 			ControllerEventListener listener) {
 		this._listeners.remove(listener);
 	}
 
 	public synchronized void addRegisterUpdatedEventListener(
 			RegisterEventListener listener) {
 		this._reg.addEventListener(listener);
 	}
 
 	public synchronized void removeRegisterUpdatedEventListener(
 			RegisterEventListener listener) {
 		this._reg.removeEventListener(listener);
 	}
 
 	public synchronized void addMemoryUpdatedEventListener(
 			MemoryEventListener listener) {
 		this._mem.addEventListener(listener);
 	}
 
 	public synchronized void removeMemoryUpdatedEventListener(
 			MemoryEventListener listener) {
 		this._mem.removeEventListener(listener);
 	}
 
 	public synchronized void addDeviceEventListener(DeviceEventListener listener) {
 		this._dev.addEventListener(listener);
 	}
 
 	public synchronized void removeDeviceEventListener(
 			DeviceEventListener listener) {
 		this._dev.removeEventListener(listener);
 	}
 
 	private void invokeExceptionEvent(Exception e) {
 		int len = this._listeners.size();
 		for (int i = 0; i < len; i++) {
 			this._listeners.get(i).invokeControllerExceptionEvent(e);
 		}
 	}
 
 	private void invokeCurrentInstructionEvent(Instruction instr) {
 		int len = this._listeners.size();
 		for (int i = 0; i < len; i++) {
 			this._listeners.get(i).invokeControllerCurrentInstructionEvent(
 					instr);
 		}
 	}
 
 	private void invokeExecutionFinishedEvent() {
 		int len = this._listeners.size();
 		for (int i = 0; i < len; i++) {
 			this._listeners.get(i).invokeControllerExecutionFinished();
 		}
 	}
 
 	public void loadRom(String filename) {
 		try {
 			byte[] data = BinaryFileReader.toByteArray(filename);
 			this._mem.loadRom(data);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void loadBoot(String filename) {
 		try {
 			byte[] data = BinaryFileReader.toByteArray(filename);
 			this._mem.loadBoot(data);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void step() throws InterruptedException {
 		if (this._runmode == RunMode.STEP_MODE) {
 			synchronized (this) {
 				this.wait();
 			}
 		}
 	}
 
 	public void init() {
 		this._mem.init();
 		this._reg.init();
 	}
 
 	/**
 	 * Executes the program from the address that the current program counter
 	 * points to.
 	 */
 	public void execute() {
 		this._stopExecution = false;
 		try {
 			this._reg.setPC(Memory.ROM_BASE_ADDR);
 
 			while (!this._stopExecution) {
 				int pc = this._reg.getPC();
 				this._reg.incPC();
 				this._mem.setMAR(pc);
 				this._mem.getData();
 
 				int ir = this._mem.getMBR();
 				this._reg.setIR(ir);
 				Instruction instr = Decoder
 						.decodeInstruction(this._reg.getIR());
 				this.invokeCurrentInstructionEvent(instr);
 
 				this.step();
 
 				InstrExeResult ret = this.executeOneInstruction(instr);
 				if (ret == InstrExeResult.IER_HLT) {
 					// HLT instruction executed, halt the machine
 					this.stop();
 					break;
 				} else if (ret == InstrExeResult.IER_TRAP) {
 					// TRAP instruction executed, do something
 				} else if (ret == InstrExeResult.IER_SUCCESS) {
 					// Instruction successfully executed
 				}
 			}
 		} catch (IllegalOpcodeException e) {
 			System.err.printf("Illegal opcode: %s\n", e.getMessage());
 			this.invokeExceptionEvent(e);
 		} catch (IllegalMemoryAddressException e) {
 			System.err.printf("Illegal memory address: %s\n", e.getMessage());
 			this.invokeExceptionEvent(e);
 		} catch (IllegalTrapCodeException e) {
 			System.err.printf("Illegal trap code: %s\n", e.getMessage());
 			this.invokeExceptionEvent(e);
 		} catch (Exception e) {
 			System.err.printf("Unhandled exception: %s\n%s\n", e.getMessage(),
 					e.getStackTrace());
 			this.invokeExceptionEvent(e);
 		}
 
 		this.invokeExecutionFinishedEvent();
 	}
 
 	/**
 	 * Execute one instruction
 	 * 
 	 * @param instr
 	 *            The instruction to be executed
 	 * @return Result of the instruction execution, refer to InstrExeResult enum
 	 * @throws IllegalOpcodeException
 	 *             Illegal opcode detected when decoding the instruction
 	 * @throws IllegalMemoryAddressException
 	 *             Illegal memory address access detected
 	 * @throws IllegalTrapCodeException
 	 *             Illegal trap code detected
 	 */
 	private InstrExeResult executeOneInstruction(Instruction instr)
 			throws IllegalOpcodeException, IllegalMemoryAddressException,
 			IllegalTrapCodeException {
 
 		Addressing.calculateEA(instr, this._reg, this._mem);
 
 		int data, r, rx, ry, immed;
 		ALUResult aluret;
 		switch (instr.getInstruction()) {
 			case I_LDR:
 				this._mem.setMAR(instr.getAddress());
 				this._mem.getData();
 				data = this._mem.getMBR();
 				this._reg.setReg(data, instr.getR());
 				break;
 
 			case I_STR:
 				data = this._reg.getReg(instr.getR());
 				this._mem.setMBR(data);
 				this._mem.setMAR(instr.getAddress());
 				this._mem.setData();
 				break;
 
 			case I_LDA:
 				this._reg.setReg(instr.getAddress(), instr.getR());
 				break;
 
 			case I_LDX:
 				this._mem.setMAR(instr.getAddress());
 				this._mem.getData();
 				data = this._mem.getMBR();
 				this._reg.setX0(data);
 				break;
 
 			case I_STX:
 				data = this._reg.getX0();
 				this._mem.setMBR(data);
 				this._mem.setMAR(instr.getAddress());
 				this._mem.setData();
 				break;
 
 			case I_JZ:
 				data = this._reg.getReg(instr.getR());
 				if (data == 0)
 					this._reg.setPC(instr.getAddress());
 				break;
 
 			case I_JNE:
 				data = this._reg.getReg(instr.getR());
 				if (data != 0)
 					this._reg.setPC(instr.getAddress());
 				break;
 
 			case I_JCC:
 				data = this._reg.getCC(instr.getCC());
 				if (data == 1)
 					this._reg.setPC(instr.getAddress());
 				break;
 
 			case I_SOB:
 				r = this._reg.getReg(instr.getR()) - 1;
 				this._reg.setReg(r, instr.getR());
 				if (r > 0)
 					this._reg.setPC(instr.getAddress());
 				break;
 
 			case I_JMP:
 				this._reg.setPC(instr.getAddress());
 				break;
 
 			case I_JSR:
 				this._reg.setReg(this._reg.getPC(), 3);
 				this._reg.setPC(instr.getAddress());
 				break;
 
 			case I_RFS:
 				this._reg.setPC(this._reg.getReg(3));
 				this._reg.setReg(instr.getImmed(), 0);
 				break;
 
 			case I_ADD:
 				this._mem.setMAR(instr.getAddress());
 				this._mem.getData();
 				data = this._mem.getMBR();
 				r = this._reg.getReg(instr.getR());
 				aluret = this._alu.add(r, data);
 				this._reg.setReg(aluret.getResult(), instr.getR());
 				break;
 
 			case I_SUB:
 				this._mem.setMAR(instr.getAddress());
 				this._mem.getData();
 				data = this._mem.getMBR();
 				r = this._reg.getReg(instr.getR());
 				aluret = this._alu.sub(r, data);
 				this._reg.setReg(aluret.getResult(), instr.getR());
 				break;
 
 			case I_AIR:
 				r = this._reg.getReg(instr.getR());
 				immed = instr.getImmed();
 				aluret = this._alu.add(r, immed);
 				this._reg.setReg(aluret.getResult(), instr.getR());
 				break;
 
 			case I_SIR:
 				r = this._reg.getReg(instr.getR());
 				immed = instr.getImmed();
 				aluret = this._alu.sub(r, immed);
 				this._reg.setReg(aluret.getResult(), instr.getR());
 				break;
 
 			case I_MUL:
 				rx = this._reg.getReg(instr.getRx());
 				ry = this._reg.getReg(instr.getRy());
 				aluret = this._alu.mul(rx, ry);
 				this._reg.setReg(aluret.getHi(), instr.getRx());
 				this._reg.setReg(aluret.getLo(), instr.getRx() + 1);
 				break;
 
 			case I_DIV:
 				rx = this._reg.getReg(instr.getRx());
 				ry = this._reg.getReg(instr.getRy());
 				aluret = this._alu.div(rx, ry);
 				this._reg.setReg(aluret.getQuotient(), instr.getRx());
 				this._reg.setReg(aluret.getRemainder(), instr.getRx() + 1);
 				break;
 
 			case I_TST:
 				rx = this._reg.getReg(instr.getRx());
 				ry = this._reg.getReg(instr.getRy());
 				this._alu.tst(rx, ry);
 				break;
 
 			case I_AND:
 				rx = this._reg.getReg(instr.getRx());
 				ry = this._reg.getReg(instr.getRy());
 				aluret = this._alu.and(rx, ry);
 				this._reg.setReg(aluret.getResult(), instr.getRx());
 				break;
 
 			case I_OR:
 				rx = this._reg.getReg(instr.getRx());
 				ry = this._reg.getReg(instr.getRy());
 				aluret = this._alu.or(rx, ry);
 				this._reg.setReg(aluret.getResult(), instr.getRx());
 				break;
 
 			case I_XOR:
 				rx = this._reg.getReg(instr.getRx());
 				ry = this._reg.getReg(instr.getRy());
 				aluret = this._alu.xor(rx, ry);
 				this._reg.setReg(aluret.getResult(), instr.getRx());
 				break;
 
 			case I_NOT:
 				rx = this._reg.getReg(instr.getRx());
 				aluret = this._alu.not(rx);
 				this._reg.setReg(aluret.getResult(), instr.getRx());
 				break;
 
 			case I_SRC:
 				r = this._reg.getReg(instr.getR());
 				aluret = this._alu.src(r, instr.getShiftCount(),
 						instr.getShiftDirection(), instr.getShiftType());
 				this._reg.setReg(aluret.getResult(), instr.getR());
 			case I_RRC:
 				r = this._reg.getReg(instr.getR());
 				aluret = this._alu.rrc(r, instr.getShiftCount(),
 						instr.getShiftDirection(), instr.getShiftType());
 				this._reg.setReg(aluret.getResult(), instr.getR());
 				break;
 
 			case I_IN:
 				// data = this._dev.getData(instr.getDevId());
 				data = 0;
 				int devid = instr.getDevId();
 				if (!this._inputBufferred[devid]) {
 					this._dev.getData(devid);
 					synchronized (this) {
 						try {
 							this.wait();
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 					switch (devid) {
 						case 0: // keyboard
 							data = this._inputBuffer[devid][0];
 							System.err.println(this._inputBuffer[devid].length);
 							System.err.flush();
 							this._reg.setReg(data, instr.getR());
 							this._inputBufferPos[devid] = 1;
 							break;
 						case 2: // card reader
 							if (this._inputBuffer[devid].length >= 2) {
 								data = (this._inputBuffer[devid][0]) & 0xFF;
 								data += ((this._inputBuffer[devid][1] << 8) & 0xFFFF);
 								this._inputBufferPos[devid] = 2;
 								this._reg.setReg(data, instr.getR());
 							}
 							break;
 					}
 				} else {
 					switch (devid) {
 						case 0: // keyboard
 							if (this._inputBufferPos[devid] < this._inputBuffer[devid].length) {
 								data = this._inputBuffer[devid][this._inputBufferPos[devid]];
 								this._reg.setReg(data, instr.getR());
 								this._inputBufferPos[devid] += 1;
 								if (this._inputBufferPos[devid] >= this._inputBuffer[devid].length) {
 									this._inputBufferred[devid] = false;
 								}
 							} else
 								this._inputBufferred[devid] = false;
 							break;
 						case 2: // card reader
 							if (this._inputBufferPos[devid] + 1 < this._inputBuffer[devid].length) {
 								data = (this._inputBuffer[devid][this._inputBufferPos[devid]]) & 0xFF;
 								this._inputBufferPos[devid] += 1;
 								data += ((this._inputBuffer[devid][this._inputBufferPos[devid]] << 8) & 0xFFFF);
 								this._inputBufferPos[devid] += 1;
 								this._reg.setReg(data, instr.getR());
 								if (this._inputBufferPos[devid] >= this._inputBuffer[devid].length) {
 									this._inputBufferred[devid] = false;
 								}
 							} else
 								this._inputBufferred[devid] = false;
 							break;
 					}
 				}
 				break;
 
 			case I_OUT:
 				r = this._reg.getReg(instr.getR());
 				this._dev.setData(r, instr.getDevId());
 				break;
 
 			case I_CHK:
 				data = this._dev.check(instr.getDevId());
 				this._reg.setReg(data, instr.getR());
 				break;
 
 			case I_HLT:
 				return InstrExeResult.IER_HLT;
 
 			case I_TRAP:
 				return InstrExeResult.IER_TRAP;
 
 			default:
 				return InstrExeResult.IER_FAILED;
 		}
 
 		return InstrExeResult.IER_SUCCESS;
 	}
 
 	public byte[][] getMemoryDump() {
 		return this._mem.getDump();
 	}
 
 	/**
 	 * Implements the Runnable interface
 	 */
 	@Override
 	public void run() {
 		this.execute();
 	}
 
 	public void stop() {
 		this._stopExecution = true;
 	}
 
 	public void setInputBuffer(byte[] data, int devid) {
 		if (data != null) {
 			this._inputBuffer[devid] = data.clone();
 			this._inputBufferred[devid] = true;
 		}
 	}
 
 	public int checkBuffer(int devid) {
 		return this._inputBufferred[devid] ? 1 : 0;
 	}
 }
