 package ch.unibe.scg.cc;
 
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.stub;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 
 import org.apache.hadoop.hbase.client.HTable;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mockito;
 
 import ch.unibe.jexample.Given;
 import ch.unibe.jexample.JExample;
 import ch.unibe.scg.cc.activerecord.Function;
 import ch.unibe.scg.cc.activerecord.RealProject;
 import ch.unibe.scg.cc.lines.StringOfLines;
 import ch.unibe.scg.cc.lines.StringOfLinesFactory;
 import ch.unibe.scg.cc.modules.CCModule;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 @RunWith(JExample.class)
 public class RegisterClonesBackendTest {
 
 	RealProject project;
 	final HTable htable = mock(HTable.class);
 	Function function;
 	final StringOfLinesFactory stringOfLinesFactory = new StringOfLinesFactory();
	final StringOfLines sampleLines = stringOfLinesFactory.make("a\nb\nc\nd\ne\nf\ng\nh\ni\nj\nk\n");
 	final StringOfLines aThruK = stringOfLinesFactory.make("a\nb\nc\nd\ne\nf\ng\nh\ni\nj\nk\n");
	final StringOfLines aThruP = stringOfLinesFactory.make("a\nb\nc\nd\ne\nf\ng\nh\ni\nj\nk\nl\nm\nn\no\np\n");
 
 	@Test
 	public RegisterClonesBackend testOneRegister() {
 		CloneRegistry cr = mock(CloneRegistry.class);
 		Injector i = Guice.createInjector(new CCModule());
 		StandardHasher sth = i.getInstance(StandardHasher.class);
 		ShingleHasher shh = i.getInstance(ShingleHasher.class);
 
 		RegisterClonesBackend rcb = new RegisterClonesBackend(cr, sth, shh, null);
 
 		project = mock(RealProject.class);
 		function = mock(Function.class);
 		stub(function.getBaseLine()).toReturn(3);
 
 		rcb.registerConsecutiveLinesOfCode(sampleLines, function, Main.TYPE_3_CLONE);
 
 		verify(cr).register(
				eq(new byte[] { -22, -6, -54, -20, 74, -100, 75, 44, -1, -114, 117, 100, 14, 91, -69, -66, 115, -57,
						54, 80 }), (String) anyObject(), eq(function), eq(0), eq(10), eq(Main.TYPE_3_CLONE));
 
 		Mockito.reset(cr); // JExample doesn't support @After
 		return rcb;
 	}
 
 	@Given("testOneRegister")
 	public RegisterClonesBackend testMoreRegisters(RegisterClonesBackend rcb) {
 		CloneRegistry cr = mock(CloneRegistry.class);
 		rcb.registry = cr;
		rcb.registerConsecutiveLinesOfCode(aThruK, function, Main.TYPE_3_CLONE);
 		verify(cr).register(
				eq(new byte[] { -22, -6, -54, -20, 74, -100, 75, 44, -1, -114, 117, 100, 14, 91, -69, -66, 115, -57,
						54, 80 }), (String) anyObject(), eq(function), eq(0), eq(10), eq(Main.TYPE_3_CLONE));
		verify(cr)
				.register(
						eq(new byte[] { -70, 95, 22, -52, 75, -75, 56, 22, 37, -100, 43, 120, 70, 2, 77, 69, -58, -23,
								20, 18 }), (String) anyObject(), eq(function), eq(1), eq(10), eq(Main.TYPE_3_CLONE));
 		Mockito.reset(cr); // JExample doesn't support @After
 		return rcb;
 	}
 
 	@Given("testMoreRegisters")
 	public RegisterClonesBackend testLotsOfRegisters(RegisterClonesBackend rcb) {
 		CloneRegistry cr = mock(CloneRegistry.class);
 		rcb.registry = cr;
		rcb.registerConsecutiveLinesOfCode(aThruP, function, Main.TYPE_2_CLONE);
 		verify(cr, times(7)).register(((byte[]) anyObject()), (String) anyObject(), eq(function), anyInt(), anyInt(),
 				eq(Main.TYPE_2_CLONE));
 		Mockito.reset(cr); // JExample doesn't support @After
 
 		return rcb;
 	}
 
 }
