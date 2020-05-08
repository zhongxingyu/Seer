 package gameBoy.tests.opcodeTests;
 
 import static org.junit.Assert.*;
 import gameBoy.cpu.Flag;
 import gameBoy.cpu.GameBoyProcessor;
 import gameBoy.cpu.Register;
 import gameBoy.cpu.opcodes.add.*;
 import gameBoy.interfaces.IOpcode;
 import gameBoy.interfaces.IProcessor;
 
 import org.junit.Test;
 
 public class AdditionTests {
 
 	@Test
 	public void testAddAB() {
 		IProcessor processor = new GameBoyProcessor();
 		IOpcode add = new AddAB( processor );
 		
 		this.do8BitTest( Register.B, add, processor );
 
 	}
 
 	@Test
 	public void testAddAC() {
 		IProcessor processor = new GameBoyProcessor();
 		IOpcode add = new AddAC( processor );
 		
 		this.do8BitTest( Register.C, add, processor );
 
 	}
 	
 	@Test
 	public void testAddAD() {
 		IProcessor processor = new GameBoyProcessor();
 		IOpcode add = new AddAD( processor );
 		
 		this.do8BitTest( Register.D, add, processor );
 
 	}
 	
 	@Test
 	public void testAddAE() {
 		IProcessor processor = new GameBoyProcessor();
 		IOpcode add = new AddAE( processor );
 		
 		this.do8BitTest( Register.E, add, processor );
 
 	}
 	
 	@Test
 	public void testAddAH() {
 		IProcessor processor = new GameBoyProcessor();
 		IOpcode add = new AddAH( processor );
 		
 		this.do8BitTest( Register.H, add, processor );
 
 	}
 	
 	@Test
 	public void testAddAL() {
 		IProcessor processor = new GameBoyProcessor();
 		IOpcode add = new AddAL( processor );
 		
 		this.do8BitTest( Register.L, add, processor );
 
 	}
 	
 	@Test
 	public void testAddAA() {
 		IProcessor processor = new GameBoyProcessor();
 		IOpcode add = new AddAA( processor );
 		
 		assertEquals( 4, add.getCycles() );
 		
 		add.execute();
 		
 		assertEquals( 0, processor.getRegisters().getRegister( Register.A ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.Z ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 		
 		processor.getRegisters().setRegister( Register.A, (byte) 1 );
 		
 		add.execute();
 		
 		assertEquals( 2, processor.getRegisters().getRegister( Register.A ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.Z ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 		
 		processor.getRegisters().setRegister( Register.A, (byte) 8 );
 		
 		add.execute();
 		
 		assertEquals( 16, processor.getRegisters().getRegister( Register.A ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.Z ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 		
 		processor.getRegisters().setRegister( Register.A, (byte) 127 );
 		
 		add.execute();
 		
 		assertEquals( -2, processor.getRegisters().getRegister( Register.A ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.Z ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 
 	}
 	
 	@Test
 	public void testAddAAddrHl() {
 		short address = 0x100;
 		IProcessor processor = new GameBoyProcessor();
 		IOpcode add = new AddAAddrHl( processor );
 		
 		processor.getRegisters().setRegister( Register.HL, address );
 		
 		assertEquals(  8, add.getCycles() );
 		
 		add.execute();
 		
 		assertEquals( 0, processor.getRegisters().getRegister( Register.A ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.Z ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 		
 		processor.getRegisters().setRegister( Register.A, (byte) 1 );
 		processor.getMemory().set8BitValue( address, (byte) 1 );
 		
 		add.execute();
 		
 		assertEquals( 2, processor.getRegisters().getRegister( Register.A ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.Z ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 		
 		processor.getRegisters().setRegister( Register.A, (byte) 2 );
 		processor.getMemory().set8BitValue( address, (byte) 14 );
 		
 		add.execute();
 		
 		assertEquals( 16, processor.getRegisters().getRegister( Register.A ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.Z ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 		
 		processor.getRegisters().setRegister( Register.A, (byte) 127 );
 		processor.getMemory().set8BitValue( address, (byte) 1 );
 		
 		add.execute();
 		
 		assertEquals( -128, processor.getRegisters().getRegister( Register.A ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.Z ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 
 	}
 	
 	@Test
 	public void testAddHlBc() {
 		IProcessor processor = new GameBoyProcessor();
 		IOpcode add = new AddHlBc( processor );
 		
 		this.do16BitTest( Register.BC, add, processor );
 
 	}
 	
 	@Test
 	public void testAddHlDe() {
 		IProcessor processor = new GameBoyProcessor();
 		IOpcode add = new AddHlDe( processor );
 		
 		this.do16BitTest( Register.DE, add, processor );
 
 	}
 	
 	@Test
 	public void testAddHlSp() {
 		IProcessor processor = new GameBoyProcessor();
 		IOpcode add = new AddHlSp( processor );
 		
 		this.do16BitTest( Register.SP, add, processor );
 	}
 	
 	@Test
 	public void testAddHlHl() {
 		IProcessor processor = new GameBoyProcessor();
 		IOpcode add = new AddHlHl( processor );
 		
 		assertEquals( 8, add.getCycles() );
 		
 		add.execute();
 		
 		assertEquals( 0, processor.getRegisters().getRegister( Register.HL ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 
 		processor.getRegisters().setRegister( Register.HL, (short) 0b100000000000 );
 		
 		add.execute();
 		
 		assertEquals( 0b1000000000000, processor.getRegisters().getRegister( Register.HL ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 		
 		processor.getRegisters().setRegister( Register.HL, Short.MAX_VALUE / 2 + 1 );
 
 		add.execute();
 		
		assertEquals( Short.MIN_VALUE, (short) processor.getRegisters().getRegister( Register.HL ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 		
 		processor.getRegisters().setRegister( Register.HL, (short) 100 );
 		
 		add.execute();
 		
 		assertEquals( 200, processor.getRegisters().getRegister( Register.HL ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 	}
 	
 	@Test
 	public void AddImmToSp() {
 		IProcessor processor = new GameBoyProcessor();
 		IOpcode add = new AddImmToSp( processor );
 		
 		processor.getRegisters().setRegister( Register.SP, (short) 0x101 );
 		processor.getMemory().set8BitValue( 0x1, (byte) 0xF );
 		
 		add.execute();
 		
 		assertEquals( 0x110, processor.getRegisters().getRegister( Register.SP ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.Z ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 		
 		processor.getRegisters().setRegister( Register.PC, (short) 0x2000 );
 		processor.getRegisters().setRegister( Register.SP, (short) 0x1 );
 		processor.getMemory().set8BitValue( 0x2001, (byte) 0xB );
 		
 		add.execute();
 		
 		assertEquals( 0xC, processor.getRegisters().getRegister( Register.SP ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.Z ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 		
 		processor.getRegisters().setRegister( Register.PC, (short) 0x2 );
 		processor.getRegisters().setRegister( Register.SP, (short) ( Byte.MAX_VALUE * 2 ) );
 		processor.getMemory().set8BitValue( 0x3, (byte) 0x2 );
 		
 		add.execute();
 		
 		assertEquals( 0x100, processor.getRegisters().getRegister( Register.SP ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.Z ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 	}
 	
 	private void do8BitTest( Register register, IOpcode add, IProcessor processor ) {
 		assertEquals( 4, add.getCycles() );
 		
 		add.execute();
 		
 		assertEquals( 0, processor.getRegisters().getRegister( Register.A ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.Z ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 		
 		processor.getRegisters().setRegister( Register.A, (byte) 1 );
 		processor.getRegisters().setRegister( register, (byte) 1 );
 		
 		add.execute();
 		
 		assertEquals( 2, processor.getRegisters().getRegister( Register.A ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.Z ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 		
 		processor.getRegisters().setRegister( register, (byte) 14 );
 		
 		add.execute();
 		
 		assertEquals( 16, processor.getRegisters().getRegister( Register.A ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.Z ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 		
 		processor.getRegisters().setRegister( Register.A, (byte) 127 );
 		processor.getRegisters().setRegister( register, (byte) 1 );
 		
 		add.execute();
 		
 		assertEquals( -128, processor.getRegisters().getRegister( Register.A ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.Z ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 	}
 	
 	private void do16BitTest( Register register, IOpcode add, IProcessor processor ) {
 		assertEquals( 8, add.getCycles() );
 		
 		add.execute();
 		
 		assertEquals( 0, processor.getRegisters().getRegister( Register.HL ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 
 		processor.getRegisters().setRegister( register, (short) 1 );
 		processor.getRegisters().setRegister( Register.HL, (short) 0b111111111111 );
 		
 		add.execute();
 		
 		assertEquals( 0b1000000000000, processor.getRegisters().getRegister( Register.HL ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 		
 		processor.getRegisters().setRegister( register, (short) 1 );
 		processor.getRegisters().setRegister( Register.HL, Short.MAX_VALUE );
 
 		add.execute();
 		
		assertEquals( Short.MIN_VALUE, (short) processor.getRegisters().getRegister( Register.HL ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 1, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 		
 		processor.getRegisters().setRegister( register, (short) 300 );
 		processor.getRegisters().setRegister( Register.HL, (short) 100 );
 		
 		add.execute();
 		
 		assertEquals( 400, processor.getRegisters().getRegister( Register.HL ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.C ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.H ) );
 		assertEquals( 0, processor.getRegisters().getFlag( Flag.N ) );
 	}
 }
