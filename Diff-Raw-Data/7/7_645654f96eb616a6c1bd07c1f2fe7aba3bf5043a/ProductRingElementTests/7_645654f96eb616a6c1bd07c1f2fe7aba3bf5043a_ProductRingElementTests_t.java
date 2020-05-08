 package arithmetic.objects.ring;
 
 import java.io.UnsupportedEncodingException;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import arithmetic.objects.LargeInteger;
 import arithmetic.objects.arrays.ArrayOfElements;
 import cryptographic.primitives.CryptoUtils;
 
 public class ProductRingElementTests {
 
 	private Ring ring263 = new Ring(new LargeInteger("263"));
 	private IntegerRingElement ire5_ring263 = new IntegerRingElement(
 			new LargeInteger("5"), ring263);
 	private IntegerRingElement ire6_ring263 = new IntegerRingElement(
 			new LargeInteger("6"), ring263);
 	private IntegerRingElement ire258_ring263 = new IntegerRingElement(
 			new LargeInteger("258"), ring263);
 
 	private Ring ring3 = new Ring(new LargeInteger("3"));
 	private IntegerRingElement ire1_ring3 = new IntegerRingElement(
 			new LargeInteger("1"), ring3);
 	private IntegerRingElement ire2_ring3 = new IntegerRingElement(
 			new LargeInteger("2"), ring3);
 
 	private ArrayOfElements<IntegerRingElement> aoe = new ArrayOfElements<IntegerRingElement>();
 
 	@Test
 	public void toByteArrayTest() throws UnsupportedEncodingException {
 		aoe.add(ire258_ring263);
 		aoe.add(ire5_ring263);
 		ProductRingElement pre = new ProductRingElement(aoe);
 		Assert.assertEquals("00000000020100000002010201000000020005",
 				CryptoUtils.bytesToHexString(pre.toByteArray()));
 
 		// TODO - consider fixing the ProductRingElement constructor to copy the
 		// array and not use the same object
 		aoe.add(ire6_ring263);
		Assert.assertEquals("00000000020100000002010201000000020005",
				CryptoUtils.bytesToHexString(pre.toByteArray()));

		pre = new ProductRingElement(aoe);
 		Assert.assertEquals("00000000030100000002010201000000020005"
 				+ "01000000020006",
 				CryptoUtils.bytesToHexString(pre.toByteArray()));
 	}
 
 	@Test
 	public void getArrTest() {
 		aoe.add(ire258_ring263);
 		aoe.add(ire5_ring263);
 		ProductRingElement pre = new ProductRingElement(aoe);
		Assert.assertTrue(aoe.equals(pre.getArr()));
 	}
 
 	@Test
 	public void addTest() {
 		ArrayOfElements<IntegerRingElement> aoe1 = new ArrayOfElements<IntegerRingElement>();
 		aoe1.add(ire1_ring3);
 		ArrayOfElements<IntegerRingElement> aoe2 = new ArrayOfElements<IntegerRingElement>();
 		aoe2.add(ire2_ring3);
 		ProductRingElement pre1 = new ProductRingElement(aoe1);
 		ProductRingElement pre2 = new ProductRingElement(aoe2);
 		Assert.assertEquals(new LargeInteger("0"), pre1.add(pre2).getArr()
 				.getAt(0).getElement());
 	}
 
 	@Test
 	public void multTest() {
 		ArrayOfElements<IntegerRingElement> aoe1 = new ArrayOfElements<IntegerRingElement>();
 		aoe1.add(ire1_ring3);
 		ArrayOfElements<IntegerRingElement> aoe2 = new ArrayOfElements<IntegerRingElement>();
 		aoe2.add(ire2_ring3);
 		ProductRingElement pre1 = new ProductRingElement(aoe1);
 		ProductRingElement pre2 = new ProductRingElement(aoe2);
 		Assert.assertEquals(new LargeInteger("2"), pre1.mult(pre2).getArr()
 				.getAt(0).getElement());
 		Assert.assertEquals(new LargeInteger("2"), pre1.mult(pre2).getArr()
 				.getAt(0).getElement());
 	}
 
 	@Test
 	public void powerTest() {
 		aoe.add(ire1_ring3);
 		aoe.add(ire2_ring3);
 		ProductRingElement pre = new ProductRingElement(aoe);
 		Assert.assertEquals(new LargeInteger("1"),
 				pre.power(new LargeInteger("3")).getArr().getAt(0).getElement());
 		Assert.assertEquals(new LargeInteger("1"),
 				pre.power(new LargeInteger("3")).getArr().getAt(1).getElement());
 	}
 
 	@Test
 	public void neg() {
 		aoe.add(ire1_ring3);
 		aoe.add(ire2_ring3);
 		ProductRingElement pre = new ProductRingElement(aoe);
 		Assert.assertEquals(new LargeInteger("2"), pre.neg().getArr().getAt(0)
 				.getElement());
 		Assert.assertEquals(new LargeInteger("1"), pre.neg().getArr().getAt(1)
 				.getElement());
 	}
 
 	@Test
 	public void equalTest() {
 		// TODO
 	}
 
 }
