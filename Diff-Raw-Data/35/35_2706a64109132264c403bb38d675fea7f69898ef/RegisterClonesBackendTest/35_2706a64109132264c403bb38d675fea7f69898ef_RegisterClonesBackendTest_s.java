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
import ch.unibe.scg.cc.modules.JavaModule;
 
import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
 
 @RunWith(JExample.class)
 public class RegisterClonesBackendTest {
 
 	RealProject project;
 	final HTable htable = mock(HTable.class);
 	Function function;
 	final StringOfLinesFactory stringOfLinesFactory = new StringOfLinesFactory();
	final StringOfLines sampleLines = stringOfLinesFactory.make("a\nb\nc\nd\ne\nf\n");
	final StringOfLines aThruF = stringOfLinesFactory.make("a\nb\nc\nd\ne\nf\n");
 	final StringOfLines aThruK = stringOfLinesFactory.make("a\nb\nc\nd\ne\nf\ng\nh\ni\nj\nk\n");
 
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
				eq(new byte[] { 69, 10, -93, -20, 53, -4, -66, -128, 103, -28, 44, 42, 38, -9, 20, -75, -38, 89, -71,
						70 }), (String) anyObject(), eq(function), eq(3 + 0), eq(5), eq(Main.TYPE_3_CLONE));
 
 		Mockito.reset(cr); // JExample doesn't support @After
 		return rcb;
 	}
 
 	@Given("testOneRegister")
 	public RegisterClonesBackend testMoreRegisters(RegisterClonesBackend rcb) {
 		CloneRegistry cr = mock(CloneRegistry.class);
 		rcb.registry = cr;
		rcb.registerConsecutiveLinesOfCode(aThruF, function, Main.TYPE_3_CLONE);
 		verify(cr).register(
				eq(new byte[] { 69, 10, -93, -20, 53, -4, -66, -128, 103, -28, 44, 42, 38, -9, 20, -75, -38, 89, -71,
						70 }), (String) anyObject(), eq(function), eq(3), eq(5), eq(Main.TYPE_3_CLONE));
		verify(cr).register(
				eq(new byte[] { 11, 94, -59, -112, 63, 8, -52, -68, 86, -65, 105, 103, -53, -106, -96, -11, 25, 50,
						-98, -25 }), (String) anyObject(), eq(function), eq(4), eq(5), eq(Main.TYPE_3_CLONE));
 		Mockito.reset(cr); // JExample doesn't support @After
 		return rcb;
 	}
 
 	@Given("testMoreRegisters")
 	public RegisterClonesBackend testLotsOfRegisters(RegisterClonesBackend rcb) {
 		CloneRegistry cr = mock(CloneRegistry.class);
 		rcb.registry = cr;
		rcb.registerConsecutiveLinesOfCode(aThruK, function, Main.TYPE_2_CLONE);
 		verify(cr, times(7)).register(((byte[]) anyObject()), (String) anyObject(), eq(function), anyInt(), anyInt(),
 				eq(Main.TYPE_2_CLONE));
 		Mockito.reset(cr); // JExample doesn't support @After
 
 		return rcb;
 	}
 
 }
