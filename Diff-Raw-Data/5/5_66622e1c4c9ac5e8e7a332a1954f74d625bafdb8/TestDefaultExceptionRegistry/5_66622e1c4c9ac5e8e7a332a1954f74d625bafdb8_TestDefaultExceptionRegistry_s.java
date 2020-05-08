 package net.meisen.general.genmisc.exceptions.registry;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 
 import java.text.ParseException;
 import java.util.Date;
 import java.util.Locale;
 
 import net.meisen.general.genmisc.exceptions.catalog.DefaultLocalizedExceptionCatalog;
 import net.meisen.general.genmisc.exceptions.catalog.IExceptionCatalog;
 import net.meisen.general.genmisc.exceptions.catalog.InvalidCatalogEntryException;
 import net.meisen.general.genmisc.exceptions.registry.stubs.FullException;
 import net.meisen.general.genmisc.exceptions.registry.stubs.OnlyMessageAndThrowableException;
 import net.meisen.general.genmisc.exceptions.registry.stubs.OnlyMessageException;
 import net.meisen.general.genmisc.exceptions.registry.stubs.OnlyThrowableException;
 import net.meisen.general.genmisc.types.Dates;
 
 import org.junit.Test;
 
 /**
  * Tests the implementation of the <code>DefaultExceptionRegistry</code>.
  * 
  * @author pmeisen
  * 
  */
 public class TestDefaultExceptionRegistry {
 
 	/**
 	 * Tests the creation of constructors with different constructors.
 	 * 
 	 * @throws InvalidCatalogEntryException
 	 *           if the bundle cannot be loaded
 	 */
 	@Test
 	public void testDifferentConstructors() throws InvalidCatalogEntryException {
 		final DefaultExceptionRegistry registry = new DefaultExceptionRegistry();
 		final DefaultLocalizedExceptionCatalog catalog = new DefaultLocalizedExceptionCatalog(
 				"net/meisen/general/genmisc/exceptions/catalog/localizedCatalog/testExceptions");
 
 		// create the registry
 		registry.addExceptionCatalog(FullException.class, catalog);
 		registry.addExceptionCatalog(OnlyMessageException.class, catalog);
 		registry.addExceptionCatalog(OnlyThrowableException.class, catalog);
 		registry.addExceptionCatalog(OnlyMessageAndThrowableException.class,
 				catalog);
 
 		// throw some exceptions and check the result
 		try {
			registry.throwException(FullException.class, 1000);
 			fail("Exception was not thrown.");
 		} catch (final FullException e) {
 			assertNull(e.getCause());
 			assertEquals("This is the first sample value", e.getMessage());
 		}
 
 		try {
 			registry.throwException(FullException.class, 1000, new Locale("de"));
 			fail("Exception was not thrown.");
 		} catch (final FullException e) {
 			assertNull(e.getCause());
 			assertEquals("Dies ist der erste Testwert", e.getMessage());
 		}
 
 		try {
 			registry.throwException(FullException.class, 1000, new Locale("de"),
 					new IllegalStateException("Dummy"));
 			fail("Exception was not thrown.");
 		} catch (final FullException e) {
 			assertNotNull(e.getCause());
 			assertEquals(IllegalStateException.class, e.getCause().getClass());
 			assertEquals("Dies ist der erste Testwert", e.getMessage());
 		}
 
 		try {
			registry.throwException(OnlyMessageException.class, 1001);
 			fail("Exception was not thrown.");
 		} catch (final OnlyMessageException e) {
 			assertNull(e.getCause());
 			assertEquals("This is the second value", e.getMessage());
 		}
 
 		try {
 			registry.throwException(OnlyMessageAndThrowableException.class, 1002,
 					new Locale("fr"));
 			fail("Exception was not thrown.");
 		} catch (final OnlyMessageAndThrowableException e) {
 			assertNull(e.getCause());
 			assertEquals("This is the last value", e.getMessage());
 		}
 
 		try {
 			registry.throwException(OnlyThrowableException.class, 1002);
 			fail("Exception was not thrown.");
 		} catch (final IllegalArgumentException e) {
 			assertNull(e.getCause());
 			assertEquals(
 					"Unable to generate an exception of class '"
 							+ OnlyThrowableException.class.getName()
 							+ "', no valid constructor was found. Make sure at least one constructor supports a string parameter.",
 					e.getMessage());
 		} catch (OnlyThrowableException e) {
 			fail("Unexpected exception was thrown.");
 		}
 	}
 
 	/**
 	 * Tests the replacement of parameters
 	 * 
 	 * @throws InvalidCatalogEntryException
 	 *           if a catalog entry is invalid
 	 * @throws ParseException
 	 *           if the date cannot be parsed
 	 */
 	@Test
 	public void testParameterReplacement() throws InvalidCatalogEntryException,
 			ParseException {
 		final DefaultExceptionRegistry registry = new DefaultExceptionRegistry();
 		final DefaultLocalizedExceptionCatalog catalog = new DefaultLocalizedExceptionCatalog(
 				"net/meisen/general/genmisc/exceptions/catalog/localizedCatalog/testParametrizedExceptions");
 
 		registry.addExceptionCatalog(FullException.class, catalog);
 
 		try {
 			registry
 					.throwException(FullException.class, 1001, new Locale("en"), 1001);
 			fail("Exception was not thrown.");
 		} catch (final FullException e) {
 			assertNull(e.getCause());
 			assertEquals("This error has number 1001", e.getMessage());
 		}
 
 		try {
 			registry.throwException(FullException.class, 1000, new Locale("de"),
 					Dates.createDateFromString("20.01.1981 08:07:00",
 							"dd.MM.yyyy HH:mm:ss"));
 			fail("Exception was not thrown.");
 		} catch (final FullException e) {
 			assertNull(e.getCause());
 			assertEquals("Dieser Fehler wurde um 08:07 geschmissen", e.getMessage());
 		}
 
 		try {
 			registry.throwException(FullException.class, 1000, new Locale("de"),
 					(Date) null);
 			fail("Exception was not thrown.");
 		} catch (final FullException e) {
 			assertNull(e.getCause());
 			assertEquals("Dieser Fehler wurde um null geschmissen", e.getMessage());
 		}
 	}
 
 	/**
 	 * Tests the implementation to add a <code>ExceptionCatalog</code> by
 	 * class-name.
 	 * 
 	 * @see IExceptionCatalog
 	 */
 	@Test
 	public void testAddByName() {
 		final DefaultExceptionRegistry registry = new DefaultExceptionRegistry();
 		registry.addExceptionCatalogByName(FullException.class,
 				DefaultLocalizedExceptionCatalog.class.getName());
 
 		// not existing class
 		try {
 			registry.addExceptionCatalogByName(FullException.class, "sample");
 			fail("Exception was not thrown.");
 		} catch (final IllegalArgumentException e) {
 			assertEquals("Could not find the catalog clazz 'sample'.", e.getMessage());
 		}
 
 		// no catalog class
 		try {
 			registry.addExceptionCatalogByName(FullException.class, getClass()
 					.getName());
 			fail("Exception was not thrown.");
 		} catch (final IllegalArgumentException e) {
 			assertEquals("The catalog clazz '" + getClass().getName()
 					+ "' is not an concrete implementation of '"
 					+ IExceptionCatalog.class.getName() + "'.", e.getMessage());
 		}
 	}
 }
