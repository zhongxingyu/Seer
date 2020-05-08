 package net.ian.dcpu;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import javax.swing.JFrame;
 
 public class DCPU implements Runnable {
 	public enum Register { A, B, C, X, Y, Z, I, J }
 
 	public Cell[] register;
 	public MemoryCell[] memory;
 	public Cell SP, PC, EX, IA;
 	// If true, interrupts are queued. If false, they are triggered.
 	boolean iaq = false;
 	char interrupts[] = new char[256];
 	char intCurPtr, intEndPtr;
 	
 	public List<Hardware> devices = new ArrayList<>();
 	public List<MemoryListener> listeners = new ArrayList<>();
 	
 	public boolean running = false;
 	private boolean skipping = false;
 	
 	public int instructionCount, cycles;
 	
 	public static final boolean debug = false;
 	
 	public Map<Integer, String> labels;
 	
 	// Special stuff for the emulator being run w/o GUI, from the command line.
 	public boolean commandLine, setPanel;
 	public MonitorPanel panel;
 	
 	public DCPU() {
 		this(new char[0]);
 	}
 
 	public DCPU(char[] mem) {
 		register = new Cell[Register.values().length];
 		for (int i = 0; i < Register.values().length; i++)
 			register[i] = new Cell(0);
 		memory = new MemoryCell[0x10000]; // 0x10000 words in size
 		for (int i = 0; i < 0x10000; i++)
 			memory[i] = new MemoryCell(this, (char)i, i < mem.length ? mem[i] : 0);
 
 		SP = new Cell(0);
 		PC = new Cell(0);
 		EX = new Cell(0);
 		IA = new Cell(0);
 	}
 	
 	public DCPU(List<Character> mem) {
 		this(unboxArray(mem));
 	}
 	
 	// Code duplication... :/
 	public void clear(char[] mem) {
 		for (int i = 0; i < Register.values().length; i++)
 			register[i].value = 0;
 		
 		for (int i = 0; i < 0x10000; i++)
 			memory[i].value = i < mem.length ? mem[i] : 0;
 			
 		intEndPtr = intCurPtr = 0;
 		for (int i = 0; i < 256; i++)
 			interrupts[i] = 0;
 		iaq = false;
 			
 		SP.value = 0;
 		PC.value = 0;
 		EX.value = 0;
 		IA.value = 0;
 		
 		instructionCount = 0;
 	}
 	
 	public void clear(List<Character> mem) {
 		clear(unboxArray(mem));
 	}
 	
 	public void setMemory(List<Character> listMem) {
 		char[] mem = unboxArray(listMem);
 		for (int i = 0; i < mem.length; i++)
 			memory[i].value = mem[i];
 	}
 	
 	// Bleh.
 	private static char[] unboxArray(List<Character> mem) {
 		Character[] chars = mem.toArray(new Character[0]);
 		char[] ints = new char[chars.length];
 		for (int i = 0; i < ints.length; i++)
 			ints[i] = (char)chars[i];
 		return ints;
 	}
 	
 	private void debug(Object o) {
 		if (debug)
 			System.err.print(o);
 	}
 	
 	private void debugln(Object o) {
 		if (debug)
 			System.err.println(o);
 	}
 	
 	private void debugf(String s, Object... o) {
 		if (debug)
 			System.err.printf(s, o);
 	}
 	
 	public void attachDevice(Hardware h) {
 		devices.add(h);
 	}
 	public void addListener(MemoryListener l) {
 		listeners.add(l);
 	}
 	
 	public Cell getRegister(Register r) {
 		return register[r.ordinal()];
 	}
 	
 	public void interrupt(char interruptMsg) {
 		interrupts[intEndPtr++] = interruptMsg;		
 		intEndPtr &= 255;
 		// TODO: Make it catch fire if intEndPtr == intProcessPtr, which
 		// means it came around and the queue is over 256 interrupts.
 	}
 	
 	private Cell handleArgument(int code, boolean isA) {
 		debugf("0x%s: ", Integer.toHexString(code));
 		if (code >= 0x0 && code <= 0x7) {
 			debug(Register.values()[code]);
 			return register[code];
 		} else if (code >= 0x8 && code <= 0xf) {
 			debugf("[%s]", Register.values()[code - 0x8]);
 			return memory[register[code - 0x8].value];
 		} else if (code >= 0x10 && code <= 0x17) {
 			debugf("[next word + %s]", Register.values()[code - 0x10]);
 			cycles++;
 			return memory[memory[PC.value++].value + register[code - 0x10].value];
 		} else if (code == 0x18) {
 			debug(isA ? "POP" : "PUSH");
 			return isA ? memory[SP.value++] : memory[--SP.value];
 		} else if (code == 0x19) {
 			debug("PEEK");
 			return memory[SP.value];
 		} else if (code == 0x1a) {
 			debug("PICK " + memory[PC.value].value);
 			cycles++;
 			return memory[SP.value + memory[PC.value++].value];
 		} else if (code == 0x1b) {
 			debug("SP");
 			return SP;
 		} else if (code == 0x1c) {
 			debug("PC");
 			return PC;
 		} else if (code == 0x1d) {
 			debug("EX");
 			return EX;
 		} else if (code == 0x1e) {
 			debug("[next word]");
 			cycles++;
 			return memory[memory[PC.value++].value];
 		} else if (code == 0x1f) {
 			debug("next word (literal)");
 			cycles++;
 			return new Cell(memory[PC.value++].value);
 		}
 		// Only should happen if argument is A.
		debug("literal: " + (code - 0x21));
 		return new Cell(code - 0x21);
 	}
 	
 	public void skipIf(boolean test) {
 		// All IF instructions take at least 2 cycles.
 		cycles += 2;
 		// And an extra cycle if the test fails.
 		if (!test)
 			cycles++;
 		skipping = test;
 	}
 	
 	private void processBasic(int opcode, Cell cellA, Cell cellB) {
 		int a = cellA.value;
 		int b = cellB.value;
 		int ex = 0;
 		
 		if ((opcode - 1) < Assembler.basicOps.length)
 			debugln(Assembler.basicOps[opcode - 1]);
 		
 		switch (opcode) {
 		case 0x1: // SET - sets b to a
 			cycles++;
 			b = a;
 			break;
 		case 0x2: // ADD - add a to b
 			cycles += 2;
 			ex = (b += a) > 0xffff ? 1 : 0;
 			break;
 		case 0x3: // SUB - subtract from b
 			cycles += 2;
 			ex = (b -= a) < 0 ? 0xffff : 0;
 			break;
 		case 0x4: // MUL - multiplies b by a
 			cycles += 2;
 			ex = (b *= a) >> 16 & 0xffff;
 			break;
 		case 0x5: // MLI - multiplies signed values
 			cycles += 2;
 			b = (short)a * (short)b;
 			ex = b >> 16 & 0xffff;
 			break;
 		case 0x6: // DIV divides b by a
 			cycles += 3;
 			if (a == 0) {
 				b = 0;
 				ex = 0;
 			} else {
 				ex = (b << 16) / a;
 				b /= a;
 			}
 			break;
 		case 0x7: // DVI - divides signed values
 			cycles += 3;
 			if (a == 0) {
 				b = 0;
 				ex = 0;
 			} else {
 				ex = ((short)b << 16) / (short)a;
 				b = (short)b / (short)a;
 			}
 			break;
 		case 0x8: // MOD - (sets b to b % a)
 			cycles += 3;
 			b = (a == 0) ? 0 : b % a;
 			break;
 		case 0x9: // MDI - MOD with signed values
 			cycles += 3;
 			b = (a == 0) ? 0 : (short)b % (short)a;
 		case 0xa: // AND - sets b to b & a
 			cycles++;
 			b &= a;
 			break;
 		case 0xb: // BOR - sets b to b | a
 			cycles++;
 			b |= a;
 			break;
 		case 0xc: // XOR - sets b to b ^ a
 			cycles++;
 			b ^= a;
 			break;
 		case 0xd: // SHR - shifts b right by a (logical shift)
 			cycles++;
 			ex = b << 16 >> a;
 			b >>>= a;
 			break;
 		case 0xe: // ASR - shift b right by a (arithmetic shift)
 			cycles++;
 			ex = b << 16 >>> a;
 			b >>= a;
 			break;
 		case 0xf: // SHL - shifts b left by a
 			cycles++;
 			ex = b << a >> 16;
 			b = b << a;
 			break;
 		case 0x10: // IFB - performs next instruction if (b & a) != 0
 			skipIf((b & a) == 0);
 			break;
 		case 0x11: // IFC - performs next instruction if (b & a) == 0
 			skipIf((b & a) != 0);
 			break;
 		case 0x12: // IFE - performs next instruction if b == a
 			skipIf(b != a);
 			break;
 		case 0x13: // IFN - performs next instruction if b != a
 			skipIf(b == a);
 			break;
 		case 0x14: // IFG - performs next instruction if b > a
 			skipIf(b <= a);
 			break;
 		case 0x15: // IFA - IFG with signed values
 			skipIf((short)b <= (short)a);
 			break;
 		case 0x16: // IFL - performs next instruction if b < a
 			skipIf(b >= a);
 			break;
 		case 0x17: // IFU - IFL with signed values
 			skipIf((short)b >= (short)a); 
 			break;
 		case 0x1a: // ADX - sets b to b+a+EX
 			cycles += 3;
 			ex = (b += a + ex) > 0xffff ? 1 : 0;
 			break;
 		case 0x1b: // SBX - sets b to b-a+EX
 			cycles += 3;
 			ex = (b = b - a + ex) < 0 ? 0xffff : 0;
 			break;
 		case 0x1e: // STI - sets b to a, then increments I and J
 			cycles += 2;
 			b = a;
 			getRegister(Register.I).value++;
 			getRegister(Register.J).value++;
 			break;
 		case 0x1f: // STD - sets b to a, then decrements I and J
 			cycles += 2;
 			b = a;
 			getRegister(Register.I).value--;
 			getRegister(Register.J).value--;
 			break;	
 		default:
 			debugln("Error: Unimplemented basic instruction: 0x" + Integer.toHexString(opcode));
 		}
 		cellA.set(a);
 		cellB.set(b);
 		EX.set(ex);
 	}
 
 	private void processSpecial(int opcode, Cell cellA) {
 		int a = cellA.value;
 		
 		if (opcode > 0 && (opcode - 1) < Assembler.specialOps.length)
 			debugln(Assembler.specialOps[opcode - 1]);
 		
 		switch (opcode) {
 		case 0x0: // EXIT - custom code, makes the processor stop.
 			// This is nice because what to do at an empty instruction is undefined, and
 			// this provides a clean end for simple programs that don't loop forever.
 			debugln("EXIT");
 			running = false;
 			break;
 		case 0x1: // JSR - pushes the address of the next instruction to the stack, sets PC to a
 			cycles += 3;
 			memory[--SP.value].value = PC.value;
 			PC.value = (char)a;
 			break;
 		case 0x8: // INT - triggers software interrupt with message a
 			cycles += 4;
 			interrupt((char)a);
 			break;
 		case 0x9: // IAG - sets a to IA
 			cycles++;
 			a = IA.value;
 			break;
 		case 0xa: // IAS - sets IA to a
 			cycles++;
 			IA.set(a);
 			break;
 		case 0xb: // RFI - disables interrupt queueing, pops A and then PC from stack.
 			cycles += 3;
 			iaq = false;
 			getRegister(Register.A).set(memory[SP.value++].get());
 			PC.set(memory[SP.value++].get());
 			break;
 		case 0xc: // IAQ - if a is nonzero, interrupts are queued instead of triggered. otherwise they are triggered.
 			cycles += 2;
 			iaq = a == 0 ? false : true;
 			break;
 		case 0x10: // HWN - sets a to number of connected devices
 			cycles += 2;
 			a = devices.size();
 			break;
 		case 0x11: // HWQ - sets A, B, C, X, and Y to info about hardware a
 			cycles += 4;
 			if (a < 0 || a >= devices.size()) {
 				System.err.println("Error: Code attempted to query invalid hardware number: " + a);
 				break;
 			}
 			Hardware h = devices.get(a);
 			getRegister(Register.A).value = (char)h.id;
 			getRegister(Register.B).value = (char)(h.id >> 16);
 			getRegister(Register.C).value = (char)h.version;
 			getRegister(Register.X).value = (char)h.manufacturer;
 			getRegister(Register.Y).value = (char)(h.manufacturer >> 16);
 			break;
 		case 0x12: // HWI - send an interrupt to hardware a
 			cycles += 4;
 			if (a >= devices.size()) return;
 			Hardware device = devices.get(a);
 			// If running w/o GUI, a window is not created until a hardware interrupt is actually
 			// sent to the monitor or keyboard. Maybe add a public bool Hardware.requiresWindow?
 			if (commandLine && (device instanceof Monitor || device instanceof Keyboard))
 				setupPanel();
 			devices.get(a).interrupt();
 			break;
 		default:
 			debugln("Error: Unimplemented special instruction: 0x" + Integer.toHexString(opcode));
 		}
 		
 		cellA.set(a);
 	}
 	
 	@SuppressWarnings("unused")
 	public void cycle() {
 		if (debug && labels != null && labels.containsKey(PC.value)) {
 			System.err.println(labels.get(PC.value));
 		}
 		
 		int instruction = memory[PC.value].value;
 		int opcode = 0;
 		int rawA = 0, rawB = -1;
 		if ((instruction & 0b11111) == 0) {
 			// Non-basic opcode. aaaaaaooooo00000
 			instruction >>= 5;
 			opcode = skipping ? 0 : instruction & 0b11111;
 			rawA = instruction >> 5 & 0b111111;
 		} else {
 			// Basic opcode. aaaaaabbbbbooooo
 			opcode = instruction & 0b11111;
 			rawA = instruction >> 10 & 0b111111;
 			rawB = instruction >>  5 & 0b11111;
 		}
 				
 		if (skipping) {
 			PC.value++;
 	        if ((rawA >= 0x10 && rawA <= 0x17) || rawA == 0x1a || rawA == 0x1e || rawA == 0x1f)
 	            PC.value++;
 	        if ((rawB >= 0x10 && rawB <= 0x17) || rawB == 0x1a || rawB == 0x1e || rawB == 0x1f)
 	            PC.value++;
 			
 			skipping = false;
 			if (opcode >= 0x10 && opcode <= 0x17)
 				skipping = true;
 			return;
 		}
 		
 		if (IA.value > 0 && !iaq && intCurPtr != intEndPtr) {
 			iaq = true;
 			memory[--SP.value].set(PC.value);
 			memory[--SP.value].set(getRegister(Register.A).value);
 			PC.value = IA.value;
 			getRegister(Register.A).value = interrupts[intCurPtr++];
 			intCurPtr &= 255;
 			
 			// Maybe this should be moved to a parseInstruction() method.
 			instruction = memory[PC.value].value;
 			opcode = 0;
 			rawA = 0;
 			rawB = -1;
 			if ((instruction & 0b11111) == 0) {
 				// Non-basic opcode. aaaaaaooooo00000
 				instruction >>= 5;
 				opcode = skipping ? 0 : instruction & 0b11111;
 				rawA = instruction >> 5 & 0b111111;
 			} else {
 				// Basic opcode. aaaaaabbbbbooooo
 				opcode = instruction & 0b11111;
 				rawA = instruction >> 10 & 0b111111;
 				rawB = instruction >>  5 & 0b11111;
 			}
 		}
 		
 		PC.value++;
 
 		debug("A: ");
 		Cell a = handleArgument(rawA, true), b = null;
 		debugln(" = " + (int)a.value);
 		if (rawB != -1) {
 			debug("B: ");
 			b = handleArgument(rawB, false);
 			debugln(" = " + (int)b.value);
 		}
 		
 		if (b != null)
 			processBasic(opcode, a, b);
 		else
 			processSpecial(opcode, a);
 		
 		instructionCount++;
 	}
 	
 	public void setupPanel() {
 		if (setPanel) return;
 		setPanel = true;
 		
 		new Thread() {
 			public void run() {
 		        JFrame frame = new JFrame("DCPU-16");
 		        
 		        frame.setContentPane(panel);
 		        
 		        frame.pack();
 		        frame.setResizable(false);
 		        frame.setLocationRelativeTo(null);
 		        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		        frame.setVisible(true);
 		        
 		        while (running) {
 		        	panel.tick();
 		        }
 			}
 		}.start();
 	}
 	
 	public void run() {
 		running = true;
 		int fps = 60;
 		int hz = 100_000;
 		int cyclesPerFrame = hz / fps;
 		int nsPerFrame = 1000_000_000 / fps;
 		long time, diff = 0;
 		while (running) {
 			time = System.nanoTime();
 			
 			while (cycles < cyclesPerFrame)
 				cycle();
 			cycles -= cyclesPerFrame;
 			
 			for (Hardware device : devices)
 				device.tick();
 			try {
 				diff += nsPerFrame - (System.nanoTime() - time);
 				if (diff > 1000_000)
 					Thread.sleep(diff / 1000_000);
 				diff %= 1000_000;
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public String dump() {
 		String s = "";
 		for (Register r : Register.values())
 			s += r.toString() + ": " + Integer.toHexString(getRegister(r).value) + "\n";
 		return s;
 	}
 	
 	public static void main(String args[]) {
 		Scanner s = new Scanner(System.in);
 		List<Character> code = new ArrayList<>();
 		while (s.hasNextInt(16))
 			code.add((char)s.nextInt(16));
 		
 		DCPU cpu = new DCPU(code);
 		Monitor monitor = new Monitor(cpu);
 		Keyboard keyboard = new Keyboard(cpu);
 		Clock clock = new Clock(cpu);
 		
 		cpu.panel = new MonitorPanel(monitor);
 		cpu.panel.addKeyListener(keyboard);
 		cpu.commandLine = true;
 		cpu.run();
 		System.out.print(cpu.dump());
 	}
 }
