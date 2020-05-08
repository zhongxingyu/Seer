 package titocc.compiler.types;
 
 import java.util.ArrayList;
 import java.util.List;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 public class FunctionTypeTest
 {
 	private CType t;
 
 	private List<CType> params;
 
 	@Before
 	public void setUp()
 	{
		params = new ArrayList<CType>();
 		params.add(CType.INT);
 		t = new FunctionType(CType.INT, params);
 	}
 
 	@Test
 	public void hasRightProperties()
 	{
 		assertFalse(t.isObject());
 		assertFalse(t.isScalar());
 		assertFalse(t.isPointer());
 		assertFalse(t.isArithmetic());
 		assertFalse(t.isInteger());
 		assertTrue(t.isValid());
 	}
 
 	@Test
 	public void sizeIsCorrect()
 	{
 		assertEquals(0, t.getSize());
 	}
 
 	@Test
 	public void incrementSizeIsCorrect()
 	{
 		assertEquals(0, t.getIncrementSize());
 	}
 
 	@Test
 	public void dereferenceReturnsCorrectType()
 	{
 		assertTrue(t.dereference() instanceof InvalidType);
 	}
 
 	@Test
 	public void decayReturnsCorrectType()
 	{
 		assertEquals(new PointerType(t), t.decay());
 	}
 
 	@Test
 	public void equalsWorksCorrectly()
 	{
 		assertFalse(t.equals(new ArrayType(new ArrayType(CType.INT, 6), 7)));
 		assertFalse(t.equals(CType.INT));
 		assertFalse(t.equals(CType.VOID));
 		assertFalse(t.equals(new PointerType(new ArrayType(CType.INT, 6))));
 		assertTrue(t.equals(new FunctionType(CType.INT, params)));
		List<CType> params2 = new ArrayList<CType>();
 		params2.add(new PointerType(CType.INT));
 		assertFalse(t.equals(new FunctionType(CType.INT, params2)));
 		assertFalse(t.equals(new FunctionType(CType.VOID, params)));
 		assertFalse(t.equals(new InvalidType()));
 	}
 }
