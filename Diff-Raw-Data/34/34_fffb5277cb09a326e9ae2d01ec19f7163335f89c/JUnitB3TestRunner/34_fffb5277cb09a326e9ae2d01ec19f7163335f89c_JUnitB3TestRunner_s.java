 /*******************************************************************************
  * Copyright (c) 2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  ******************************************************************************/
 
 package org.eclipse.b3.beelang.junit;
 
 import org.junit.runners.Suite;
 
 /**
  * <p>
  * The custom runner <code>JUnitB3TestRunner</code> is a wrapper around the functionality implemented in
  * <code>{@link JUnitB3FileRunnerFactory}</code> which makes it possible to run B3 language functions as JUnit tests.
  * </p>
  * 
  * The runner is used as follows:
  * 
  * <pre>
  * &#064;RunWith (JUnitB3TestRunner.class)
  * &#064;B3Files ( { &quot;/b3/basic.b3&quot;, &quot;/b3/advanced.b3&quot; })
  * public class AllB3Tests {
  * }
  * </pre>
  * 
  * @author michal.ruzicka@cloudsmith.com
  * 
  * @see B3Files
  * @see JUnitB3FileRunnerFactory
  */
 public class JUnitB3TestRunner extends Suite {
 
 	/**
 	 * Only called reflectively. Do not use programmatically.
 	 */
	public JUnitB3TestRunner(Class<?> klass) throws Throwable {
 		super(klass, new JUnitB3FileRunnerFactory(klass).getB3FileRunners());
 	}
 
 }
