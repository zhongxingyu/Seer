 package cd.semantic;
 
 import java.util.Set;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import cd.ir.symbols.ArrayTypeSymbol;
 import cd.ir.symbols.ClassSymbol;
 import cd.ir.symbols.TypeSymbol;
 
 /**
  * Tests {@link TypeSymbolTable}.
  */
 public class TypeSymbolTableTest {
 
 	private TypeSymbolTable typeSymbols;
 	private ClassSymbol A, B, C, D, E;
 
 	@Before
 	public void setUp() {
 		typeSymbols = new TypeSymbolTable();
 
 		A = new ClassSymbol("A", typeSymbols.getObjectType());
 		B = new ClassSymbol("B", A);
 		C = new ClassSymbol("C", B);
 		D = new ClassSymbol("D", B);
 		E = new ClassSymbol("E", typeSymbols.getObjectType());
 
 		typeSymbols.add(A);
 		typeSymbols.add(B);
 		typeSymbols.add(C);
 		typeSymbols.add(D);
 		typeSymbols.add(E);
 
 		// Object
 		// |-- A
 		// |...|-- B
 		// |.......|-- C
 		// |.......|-- D
 		// |-- E
 	}
 
 	@Test
 	public void testArrayTypes() {
		// Test that for each non-array type, there is a corresponding array
 		// type in the type symbol table
 		for (TypeSymbol elementType : typeSymbols.localSymbols()) {
			if (!(elementType instanceof ArrayTypeSymbol)) {
 				ArrayTypeSymbol arrayType = typeSymbols
 						.getArrayTypeSymbol(elementType);
 				Assert.assertEquals(elementType, arrayType.elementType);
 			}
 		}
 	}
 
 	@Test
 	public void testLCA() {
 		TypeSymbol topType = typeSymbols.getTopType();
 		TypeSymbol bottomType = typeSymbols.getBottomType();
 		TypeSymbol objectType = typeSymbols.getObjectType();
 		TypeSymbol nullType = typeSymbols.getNullType();
 
 		for (TypeSymbol type : typeSymbols.allSymbols()) {
 			assertLCA(type, type, type);
 			assertLCA(topType, topType, type);
 			assertLCA(type, bottomType, type);
 
 			if (type.isReferenceType()) {
 				assertLCA(type, nullType, type);
 			}
 		}
 
 		for (TypeSymbol primitiveType : typeSymbols.getPrimitiveTypeSymbols()) {
 			assertLCA(topType, nullType, primitiveType);
 
 			for (TypeSymbol otherType : typeSymbols.allSymbols()) {
 				// The LCA of a primitive type and any other type one that is
 				// not the bottom type, is the top type
 				if (otherType != primitiveType && otherType != bottomType) {
 					assertLCA(topType, primitiveType, otherType);
 				}
 			}
 		}
 
 		// Assert that arrays are invariant
 		Set<ArrayTypeSymbol> arrayTypes = typeSymbols.getArrayTypeSymbols();
 		for (ArrayTypeSymbol arrayType : arrayTypes) {
 			for (ArrayTypeSymbol otherArrayType : arrayTypes) {
 				if (arrayType != otherArrayType) {
 					assertLCA(objectType, arrayType, otherArrayType);
 				}
 			}
 		}
 
 		assertLCA(objectType, A, objectType);
 		assertLCA(objectType, A, E);
 		assertLCA(A, A, B);
 		assertLCA(B, C, D);
 	}
 
 	private void assertLCA(TypeSymbol expectedLCA, TypeSymbol type,
 			TypeSymbol other) {
 		Assert.assertEquals(expectedLCA, typeSymbols.getLCA(type, other));
 		Assert.assertEquals(expectedLCA, typeSymbols.getLCA(other, type));
 	}
 
 }
