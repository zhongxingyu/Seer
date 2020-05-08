 package us.exultant.ahs.codec;
 
 import us.exultant.ahs.core.*;
 import us.exultant.ahs.util.*;
 import us.exultant.ahs.test.junit.*;
 import us.exultant.ahs.codec.eon.*;
 import us.exultant.ahs.codec.json.*;
 
 
 public class AnnotationTest extends JUnitTestCase {
 	@Encodable(styles={Enc.DEFAULT, Enc.SELECTED})
 	private static class Encable {
 		public  @Enc					String $public;
 		private @Enc(selected={Enc.DEFAULT,Enc.SELECTED})	String $private;
 		
 		/** just for reflective instantiability */
 		private Encable(Encodable $x) {}
 		
 		public Encable(String $public, String $private) {
 			this.$public = $public;
 			this.$private = $private;
 		}
 		public String getPublic()	{ return this.$public;	}
 		public String getPrivate()	{ return this.$private;	}
 	}
 	
 	@Encodable(value="classname")
 	private static class Encable2 {
 		public  @Enc("o")					String $public;
 		private @Enc(value="x", selected={Enc.DEFAULT,Enc.SELECTED})	String $private;
 		private @Enc("b")					byte[] $bees;
 		
 		public Encable2(String $public, String $private, String $bees) {
 			this.$public = $public;
 			this.$private = $private;
 			this.$bees = Base64.decode($bees);
 		}
 		public String getPublic()	{ return this.$public;	}
 		public String getPrivate()	{ return this.$private;	}
 		public byte[] getBees()		{ return this.$bees;	}
 	}
 
 	@Encodable(all_fields=true)
 	private static class Big {
 		public Little	$lil;
 		
 		/** just for reflective instantiability */
 		private Big(Encodable $x) {}
 		
 		public Big(Little $lil) {
 			this.$lil = $lil;
 		}
 		public Little getLil()	{ return this.$lil;	}
 	}
 	
 	@Encodable
 	private static class Little {
 		public @Enc	String	$str;
 		
 		/** just for reflective instantiability */
 		private Little(Encodable $x) {}
 		
 		public Little(String $str) {
 			this.$str = $str;
 		}
 		public String getStr()	{ return this.$str;	}
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	public void testEncodeDefault() throws TranslationException {
 		Encable $e = new Encable("pub","priv");
 		
 		EonCodec $codec = new JsonCodec();
 		$codec.putHook(Encable.class, new EonRAE<Encable>(Encable.class, Enc.DEFAULT));
 		
 		EonObject $v = $codec.encode($e);
 		X.saye($v.toString());
 		assertEquals(3, $v.size());
 		assertEquals("Encable", $v.getKlass());
 		assertEquals("pub",  $v.getString("$public"));
 		assertEquals("priv", $v.getString("$private"));
 	}
 	
 	public void testEncodeSelected() throws TranslationException {
 		Encable $e = new Encable("pub","priv");
 		
 		EonCodec $codec = new JsonCodec();
 		$codec.putHook(Encable.class, new EonRAE<Encable>(Encable.class, Enc.SELECTED));
 		
 		EonObject $v = $codec.encode($e);
 		X.saye($v.toString());
 		assertEquals(2, $v.size());
 		assertEquals("Encable", $v.getKlass());
 		assertEquals("priv", $v.getString("$private"));
 	}
 	
 	public void testEncodeToMagicKey() throws TranslationException {
 		Encable2 $e = new Encable2("pub","priv","ABBA");
 		
 		EonCodec $codec = new JsonCodec();
 		$codec.putHook(Encable2.class, new EonRAE<Encable2>(Encable2.class, Enc.DEFAULT));
 		
 		EonObject $v = $codec.encode($e);
 		X.saye($v.toString());
 		assertEquals(4, $v.size());
 		assertEquals("classname", $v.getKlass());
 		assertEquals("pub",  $v.getString("o"));
 		assertEquals("priv", $v.getString("x"));
 		assertEquals(Base64.decode("ABBA"), $v.getBytes("b"));
 	}
 	
 	public void testEncodeUnacceptable() throws TranslationException {
 		Encable2 $e = new Encable2("pub","priv","ABBA");
 		
 		EonCodec $codec = new JsonCodec();
		$codec.putHook(Encable2.class, new EonRAE<Encable2>(Encable2.class, Enc.SELECTED));
 		
 		try {
			EonObject $v = $codec.encode($e);
 			fail("this should have exploded.");
 		} catch (UnencodableException $e1) {
 			/* good */
 		}
 	}
 	
 	public void testEncodeNull() throws TranslationException {
 		Encable2 $e = new Encable2("pub",null,"ABBA");
 		
 		EonCodec $codec = new JsonCodec();
 		$codec.putHook(Encable2.class, new EonRAE<Encable2>(Encable2.class, Enc.DEFAULT));
 		
 		EonObject $v = $codec.encode($e);
 		X.saye($v.toString());
 		assertEquals(3, $v.size());
 		assertEquals("classname", $v.getKlass());
 		assertEquals("pub",  $v.getString("o"));
 		assertEquals(null, $v.optString("x"));
 		assertEquals(Base64.decode("ABBA"), $v.getBytes("b"));
 	}
 	
 	public void testEncodeNull2() throws TranslationException {
 		Encable2 $e = new Encable2("pub","priv",null);
 		
 		EonCodec $codec = new JsonCodec();
 		$codec.putHook(Encable2.class, new EonRAE<Encable2>(Encable2.class, Enc.DEFAULT));
 		
 		EonObject $v = $codec.encode($e);
 		X.saye($v.toString());
 		assertEquals(3, $v.size());
 		assertEquals("classname", $v.getKlass());
 		assertEquals("pub",  $v.getString("o"));
 		assertEquals("priv", $v.getString("x"));
 		assertEquals(null, $v.optBytes("b"));
 	}
 	
 	public void testDecode() throws TranslationException {
 		Encable $e = new Encable("pub","priv");
 		
 		EonCodec $codec = new JsonCodec();
 		$codec.putHook(Encable.class, new EonRAE<Encable>(Encable.class, Enc.DEFAULT));
 		$codec.putHook(Encable.class, new EonRAD<Encable>(Encable.class, Enc.DEFAULT));
 		
 		EonObject $v = $codec.encode($e);
 		X.saye($v.toString());
 		Encable $z = $codec.decode($v, Encable.class);
 		assertEquals("pub",  $z.getPublic());
 		assertEquals("priv", $z.getPrivate());
 	}
 
 	public void testNestedEncode() throws TranslationException {
 		Big $b = new Big(new Little("asdf"));
 		
 		EonCodec $codec = new JsonCodec();
 		$codec.putHook(Big.class, new EonRAE<Big>(Big.class));
 		$codec.putHook(Little.class, new EonRAE<Little>(Little.class));
 		
 		EonObject $v = $codec.encode($b);
 		X.saye($v.toString());
 		assertEquals(2, $v.size());
 		assertEquals("Big", $v.getKlass());
 		EonObject $v2 = $v.getObj("$lil");
 		assertEquals(2, $v.size());
 		assertEquals("Little", $v2.getKlass());
 		assertEquals("asdf", $v2.getString("$str"));
 	}
 	
 	public void testNestedDecode() throws TranslationException {
 		Big $b = new Big(new Little("asdf"));
 		
 		EonCodec $codec = new JsonCodec();
 		$codec.putHook(Big.class, new EonRAE<Big>(Big.class));
 		$codec.putHook(Little.class, new EonRAE<Little>(Little.class));
 		$codec.putHook(Big.class, new EonRAD<Big>(Big.class));
 		$codec.putHook(Little.class, new EonRAD<Little>(Little.class));
 		
 		EonObject $v = $codec.encode($b);
 		X.saye($v.toString());
 		Big $z = $codec.decode($v, Big.class);
 		assertEquals("asdf", $z.getLil().getStr());
 	}
 }
