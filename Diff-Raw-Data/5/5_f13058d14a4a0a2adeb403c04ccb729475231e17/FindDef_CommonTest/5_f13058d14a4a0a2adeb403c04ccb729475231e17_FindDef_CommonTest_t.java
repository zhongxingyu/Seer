 package dtool.tests.ref;
 
 import java.io.IOException;
 
 import mmrnmhrm.core.model.CompilationUnit;
 import mmrnmhrm.tests.BasePluginTest;
 import mmrnmhrm.tests.SampleMainProject;
 
 import org.eclipse.core.runtime.CoreException;
 import org.junit.AfterClass;
 import org.junit.Test;
 
import dtool.dom.ast.ASTNodeFinder;
 import dtool.dom.ast.ASTNode;
 import dtool.dom.definitions.DefUnit;
 import dtool.dom.definitions.Module;
 import dtool.dom.references.Entity;
 import dtool.refmodel.NodeUtil;
 
 //@RunWith(Parameterized.class)
 public abstract class FindDef_CommonTest extends BasePluginTest {
 
 	public static int counter = -666;
 	
 	public static final String TEST_SRCFOLDER = SampleMainProject.TEST_SRC_REFS;
 	
 
 	protected static void staticTestInit(String testfile) {
 		counter = -1;
 		System.out.println("======== "+ testfile +" ========");
 	}
 	
 	CompilationUnit cunit;
 	Module module;
 
 	int offset; 
     int targetOffset;
 	
 	public FindDef_CommonTest(int offset, int targetOffset, String testfile) throws IOException, CoreException {
 		this.offset = offset;
 		this.targetOffset = targetOffset;
 		//cunit = CoreTestUtils.testParseCUnit(TestUtils.readTestDataFile(testfile));
 		cunit = SampleMainProject.getCompilationUnit(TEST_SRCFOLDER +"/"+ testfile);
 		//System.out.println("==== Source length: "+cunit.source.length()+" ====");
 		module = cunit.getNeoModule();	
 	}
 	
 	@Test
 	public void test() {
 		assertFindReF(cunit, offset, cunit, targetOffset);
 	}
 
 	
 	public static void assertFindReF(CompilationUnit cunit, int offset,
 			CompilationUnit targetCunit, int targetOffset) {
 		counter++;
 		System.out.print("Find ref case #"+counter+": "+offset+": ");
 		System.out.println(cunit.getSource().substring(offset).split("\\s")[0]);
 		
		ASTNode node = ASTNodeFinder.findElement(cunit.getNeoModule(), offset);
 		Entity ent = (Entity) node;
 		
 		// TODO: Assert only one found
 		// Perform the find def
 		DefUnit defunit = ent.findTargetDefUnit();
 		
 		if(targetOffset == -1) {
 			assertTrueP(defunit == null, 
 					" Find Ref got a DefUnit when it shouldn't.");
 			return;
 		}
 		
 		assertTrueP(defunit != null, " Find Ref got no DefUnit.");
 
 		Module obtainedModule = NodeUtil.getParentModule(defunit);
 		assertTrueP(obtainedModule.getCUnit().equals(targetCunit),
 				" Find Ref got wrong target module.");
 		
 		assertTrueP(defunit.defname.startPos == targetOffset, 
 				" Find Ref went to wrong offset: " + defunit.defname.startPos);
 	}
 }
