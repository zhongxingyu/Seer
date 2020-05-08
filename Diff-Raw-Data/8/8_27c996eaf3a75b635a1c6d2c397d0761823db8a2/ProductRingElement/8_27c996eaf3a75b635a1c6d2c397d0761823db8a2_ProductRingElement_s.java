 package arithmetic.objects.ring;
 
 import arithmetic.objects.ByteTree;
 import arithmetic.objects.ElementsExtractor;
 import arithmetic.objects.LargeInteger;
 import arithmetic.objects.arrays.ArrayGenerators;
 import arithmetic.objects.arrays.ArrayOfElements;
 
 /**
  * This class represents a product ring element. This product ring element has
  * coordinates which are integer ring elements that all belong to the same ring.
  * In our project this is the only kind of product ring element we need to
  * implement. That means we do not need recursive product ring elements where
  * some of the coordinates can also be product elements, or ring elements which
  * belong to different rings.
  * 
  * @author Itay
  * 
  */
 public class ProductRingElement implements ByteTree {
 
 	private ArrayOfElements<IntegerRingElement> arr;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param bt
 	 *            - a byte array representation (as a byte tree) of the product
 	 *            ring element.
 	 * @param ring
 	 *            - the ring which the coordinates of the product element belong
 	 *            to.
 	 * @param w
 	 *            - the width of the product element = the number of coordinates
 	 *            it has. this parameter is needed for the end case that it
 	 *            equals 1. in this case, the byte array representation of a
 	 *            product ring element that contains only one element is
 	 *            different and therefore this case needs to be handled
 	 *            differently.
 	 * 
 	 */
 	public ProductRingElement(byte[] bt, IRing<IntegerRingElement> ring, int w) {
 		if (w == 1) {
 			ArrayOfElements<IntegerRingElement> a = new ArrayOfElements<IntegerRingElement>();
 			a.add(new IntegerRingElement(ElementsExtractor.leafToInt(bt), ring));
 			arr = a;
 		} else {
 			arr = ArrayGenerators.createRingElementArray(bt, ring);
 		}
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param arr
 	 *            an array of all the coordinates of the product element.
 	 */
 	public ProductRingElement(ArrayOfElements<IntegerRingElement> arr) {
 		ArrayOfElements<IntegerRingElement> a = new ArrayOfElements<IntegerRingElement>();
 		for (int i = 0; i < arr.getSize(); i++) {
 			a.add(arr.getAt(i));
 		}
 		this.arr = a;
 	}
 
 	/**
 	 * 
 	 * @return an array of the product element coordinates.
 	 */
 	public ArrayOfElements<IntegerRingElement> getElements() {
 		return arr;
 	}
 
 	/**
 	 * 
 	 * @return the number of coordinates in the product element.
 	 */
 	public int getSize() {
 		return arr.getSize();
 	}
 
 	/**
 	 * add element to product
 	 * 
 	 * @param element
 	 *            the element to add
 	 */
 	public void addElement(IntegerRingElement element) {
 		arr.add(element);
 	}
 
 	/**
 	 * @param b
 	 *            another product ring element
 	 * @return the result of the addition of the 2 product elements. That is, a
 	 *         new product ring element composed of the addition of the
 	 *         coordinates of our two addition parameters.
 	 */
 	public ProductRingElement add(ProductRingElement b) {
 		ArrayOfElements<IntegerRingElement> a = new ArrayOfElements<IntegerRingElement>();
 		for (int i = 0; i < arr.getSize(); i++) {
 			a.add(arr.getAt(i).add(b.getElements().getAt(i)));
 		}
 		return new ProductRingElement(a);
 	}
 
 	/**
 	 * @param b
 	 *            another product ring element
 	 * @return the result of the multiplication of the 2 product elements.That
 	 *         is, a new product ring element composed of the multiplication of
 	 *         the coordinates of our two multiplication parameters.
 	 */
 	public ProductRingElement mult(ProductRingElement b) {
 		ArrayOfElements<IntegerRingElement> a = new ArrayOfElements<IntegerRingElement>();
 		for (int i = 0; i < arr.getSize(); i++) {
 			a.add(arr.getAt(i).mult(b.getElements().getAt(i)));
 		}
 		return new ProductRingElement(a);
 	}
 
 	/**
 	 * @param b
 	 *            a large integer representing the exponent.
 	 * @return the result of product element in the b'th power. That is, a new
 	 *         product ring element composed of the coordinates of the original
 	 *         element, each one to the b'th power.
 	 */
 	public ProductRingElement power(LargeInteger b) {
 		ArrayOfElements<IntegerRingElement> a = new ArrayOfElements<IntegerRingElement>();
 
 		for (int i = 0; i < arr.getSize(); i++) {
 			a.add(arr.getAt(i).power(b));
 		}
 		return new ProductRingElement(a);
 	}
 
 	/**
 	 * 
 	 * @return the negative of this product ring element. That is, a new product
 	 *         ring element composed of the negative coordinates of the original
 	 *         element.
 	 */
 	public ProductRingElement neg() {
 		ArrayOfElements<IntegerRingElement> a = new ArrayOfElements<IntegerRingElement>();
 
 		for (int i = 0; i < arr.getSize(); i++) {
 			a.add(arr.getAt(i).neg());
 		}
 		return new ProductRingElement(a);
 	}
 
 	/**
 	 * 
 	 * @param b
 	 *            another product ring element
 	 * @return true if and only if our element and b are equal. That means, all
 	 *         their coordinates are equal.
 	 */
	public boolean equals(ProductRingElement b) {
 		ArrayOfElements<IntegerRingElement> a = arr;
 		for (int i = 0; i < arr.getSize(); i++) {
 			if (!(a.getAt(i).equals(b.getElements().getAt(i)))) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * returns the byte array representation (as a byte tree) of the product
 	 * ring element.
 	 */
 	@Override
 	public byte[] toByteArray() {
 		return arr.toByteArray();
 	}
 
 	@Override
 	public String toString() {
 		String temp = "";
 		for (int i = 0; i < arr.getSize(); i++) {
 			temp = temp + "(" + arr.getAt(i).toString() + ")";
 		}
 		return temp;
 
 	}
 }
