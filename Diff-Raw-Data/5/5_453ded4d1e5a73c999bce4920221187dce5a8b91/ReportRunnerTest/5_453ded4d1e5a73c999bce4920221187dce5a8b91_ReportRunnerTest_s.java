 package org.pitest.runner;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InOrder;
 import org.mockito.Mock;
 import org.mockito.Spy;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.pitest.functional.Option;
 import org.pitest.internal.ClassPath;
 import org.pitest.mutationtest.MutationClassPaths;
 import org.pitest.mutationtest.MutationCoverageReport;
 import org.pitest.mutationtest.ReportOptions;
 import org.pitest.mutationtest.instrument.JarCreatingJarFinder;
 
 import static org.mockito.BDDMockito.given;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.inOrder;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ReportRunnerTest {
 
     @Spy
     private ReportRunner reportRunner;
     @Mock
     private ReportOptions reportOptions;
     @Mock
     private ClassLoader originalClassLoader;
     @Mock
     private ClassLoader pitClassLoader;
     @Mock
     private Runnable report;
     @Mock
     private ClassPath classpath;
     @Mock
     private JarCreatingJarFinder jarCreatingJarFinder;
     @Mock
     private MutationClassPaths mutationClasspaths;
 
     @Test
     public void shouldRunReportWithClassLoaderChanging() {
         // given
         given(reportOptions.getClassPath()).willReturn(classpath);
        given(reportOptions.getReportDir()).willReturn("anyDir");
         given(reportOptions.getMutationClassPaths()).willReturn(mutationClasspaths);
         given(jarCreatingJarFinder.getJarLocation()).willReturn(mock(Option.class));
         given(reportRunner.getJarCreatingJarFinder(classpath)).willReturn(jarCreatingJarFinder);
         given(reportRunner.getOriginalClassLoader()).willReturn(originalClassLoader);
         given(reportRunner.getPitClassLoader(classpath)).willReturn(pitClassLoader);
         given(reportRunner.cloneReportObjectForClassLoader(any(MutationCoverageReport.class), eq(pitClassLoader))).willReturn(report);
 
         // when
         reportRunner.runReport(reportOptions);
 
         // then
         InOrder inOrder = inOrder(reportRunner);
         inOrder.verify(reportRunner).setCurrentClassLoader(pitClassLoader);
         inOrder.verify(reportRunner).setCurrentClassLoader(originalClassLoader);
         verify(report).run();
 
     }
 
 }
