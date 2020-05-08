 package com.alexrnl.commons.arguments;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 import java.io.PrintStream;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 
 /**
  * Test suite for the {@link Arguments} class.
  * @author Alex
  */
 public class ArgumentsTest {
 	/** The arguments to test */
 	private Arguments	arguments;
 	/** The target for the parameters */
 	private Target		target;
 	/** The mocked output stream */
 	@Mock
 	private PrintStream	out;
 	
 	/**
 	 * A class representing the target for the argument parsing.
 	 * @author Alex
 	 */
 	private static class Target {
 		/** If the feature is used */
 		@Param(names = { "-u", "--used" }, description = "if the feature should be used")
 		private boolean	used;
 		/** The name to use */
 		@Param(names = { "-n" }, description = "the name of the object", required = true)
 		private String	name;
 		/** A boolean wrapper */
 		@Param(names = { "-b" }, description = "boolean wrapping")
 		private Boolean	b;
 		/** An object */
 		@Param(names = { "-o" }, description = "object")
 		private Object	o;
 		/** An integer */
 		@Param(names = { "-x" }, description = "the value for x")
 		private int		x;
 
 		/**
 		 * Constructor #1.<br />
 		 * Default constructor.
 		 */
 		private Target () {
 			super();
 		}
 		
 		/**
 		 * Return the attribute used.
 		 * @return the attribute used.
 		 */
 		public boolean isUsed () {
 			return used;
 		}
 		
 		/**
 		 * Return the attribute name.
 		 * @return the attribute name.
 		 */
 		public String getName () {
 			return name;
 		}
 		
 		/**
 		 * Return the attribute b.
 		 * @return the attribute b.
 		 */
 		public Boolean isB () {
 			return b;
 		}

		/**
		 * Return the attribute o.
		 * @return the attribute o.
		 */
		public Object getO () {
			return o;
		}

 		/**
 		 * Return the attribute x.
 		 * @return the attribute x.
 		 */
 		public int getX () {
 			return x;
 		}
 	}
 	
 	/**
 	 * Set up test attributes.
 	 */
 	@Before
 	public void setUp () {
 		initMocks(this);
 		target = new Target();
 		arguments = new Arguments("manLau", target, out);
 	}
 	
 	/**
 	 * Test method for {@link Arguments#parse(String[])}.
 	 */
 	@Test
 	public void testParseStringArray () {
 		arguments.parse("-u", "-n", "alex");
 		assertTrue(target.isUsed());
 		assertEquals("alex", target.getName());
 		assertFalse(target.isB());
 	}
 	
 	/**
 	 * Test method for {@link Arguments#parse(Iterable)}.
 	 */
 	@Test
 	public void testParseIterableOfString () {
		arguments.parse("--used", "-n", "manLau", "-b");
 		assertTrue(target.isUsed());
 		assertEquals("manLau", target.getName());
 		assertTrue(target.isB());
 	}
 	
 	/**
 	 * Test method for checking help request.
 	 */
 	@Test
 	public void testHelpRequest () {
 		try {
 			arguments.parse("-h");
 		} catch (final IllegalArgumentException e) {
 			verify(out, times(1)).println(arguments);
 			verify(out, never()).println(anyString());
 		}
 		try {
 			arguments.parse("--help");
 		} catch (final IllegalArgumentException e) {
 			verify(out, times(1)).println(arguments);
 			verify(out, never()).println(anyString());
 		}
 	}
 	
 	/**
 	 * Test for an unknown parameter.
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testUnknownParameter () {
 		arguments.parse("--unknown");
 	}
 	
 	/**
 	 * Test for missing a required parameter.
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testMissingArgument () {
 		arguments.parse("-u");
 	}
 	
 	/**
 	 * Test for missing a value for a parameter.
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testMissingValue () {
 		arguments.parse("-n");
 	}
 	
 	/**
 	 * Test for no parser matching type found.
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testNoParser () {
 		arguments.parse("-o", "-n", "test");
 	}
 	
 	/**
 	 * Test with unparsable argument.
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void testBadValue () {
 		arguments.parse("-n", "test", "-x", "0.28");
 	}
 	
 	/**
 	 * Test method for {@link Arguments#usage()}.
 	 */
 	@Test
 	public void testUsagePrintStream () {
 		arguments.usage();
 		verify(out).println(arguments);
 		// Check output on stdout
 		new Arguments("stdout", target).usage();
 	}
 	
 	/**
 	 * Test method for {@link Arguments#toString()}.
 	 */
 	@Test
 	public void testToString () {
 		assertEquals("manLau usage as follow:\n" +
 				"\t   -n\t\t\t\tthe name of the object\n" +
 				"\t[  -b\t\t\t\tboolean wrapping  ]\n" +
 				"\t[  -o\t\t\t\tobject  ]\n" +
 				"\t[  -u, --used\t\t\t\tif the feature should be used  ]\n" +
 				"\t[  -x\t\t\t\tthe value for x  ]",
 				arguments.toString());
 	}
 }
