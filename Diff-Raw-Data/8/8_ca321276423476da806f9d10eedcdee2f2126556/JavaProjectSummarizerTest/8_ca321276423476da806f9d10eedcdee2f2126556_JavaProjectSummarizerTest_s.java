 package org.chaoticbits.collabcloud.codeprocessor.java;
 
 import static org.easymock.EasyMock.expect;
 import static org.powermock.api.easymock.PowerMock.*;
 import japa.parser.JavaParser;
 import japa.parser.ast.CompilationUnit;
 
 import java.io.File;
 
 import org.chaoticbits.collabcloud.codeprocessor.CloudWeights;
 import org.easymock.EasyMock;
 import org.easymock.IMocksControl;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({ JavaParser.class, CompilationUnit.class })
 public class JavaProjectSummarizerTest {
 
 	@Test
 	public void testProject() throws Exception {
		IMocksControl ctrl = EasyMock.createStrictControl();
 		JavaProjectSummarizer summarizer = new JavaProjectSummarizer();
 		PowerMock.mockStatic(JavaParser.class);
 		CompilationUnit unit = PowerMock.createMock(CompilationUnit.class);
 		File mockDir = ctrl.createMock(File.class);
 		File mockFile = ctrl.createMock(File.class);
 		CloudWeights weights = new CloudWeights();
 
 		expect(mockDir.isDirectory()).andReturn(true).anyTimes();
 		expect(mockDir.listFiles()).andReturn(new File[] { mockFile }).once();
 		expect(mockFile.isDirectory()).andReturn(false).anyTimes();
 		expect(mockFile.getName()).andReturn("a.java").anyTimes();
 		expect(JavaParser.parse(mockFile)).andReturn(unit).once();
//		expect(unit.accept((JavaSummarizeVisitor) EasyMock.anyObject(), (CloudWeights) EasyMock.anyObject())).andReturn(weights).once();
 		ctrl.replay();
		PowerMock.replay();
 
 		summarizer.summarize(mockDir);
 
 		PowerMock.verify(JavaParser.class);
 		ctrl.verify();
 	}
 }
