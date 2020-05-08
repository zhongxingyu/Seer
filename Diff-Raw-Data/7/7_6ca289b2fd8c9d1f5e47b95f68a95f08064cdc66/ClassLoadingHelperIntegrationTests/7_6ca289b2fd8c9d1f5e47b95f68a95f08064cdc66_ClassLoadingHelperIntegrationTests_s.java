 /*******************************************************************************
  * Copyright (c) 2010 SAP AG
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Hristo Iliev, SAP AG - initial contribution
  *******************************************************************************/
 package org.eclipse.virgo.kernel.osgicommand.helper.test;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.virgo.shell.osgicommand.helper.ClassLoadingHelper;
 import org.eclipse.virgo.kernel.test.AbstractKernelIntegrationTest;
 import org.eclipse.virgo.test.framework.dmkernel.DmKernelTestRunner;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 
 /**
 * Class for integration testing {@link org.eclipse.virgo.kernel.osgicommand.helper.ClassLoadingHelper}
  */
 @RunWith(DmKernelTestRunner.class)
 public class ClassLoadingHelperIntegrationTests extends AbstractKernelIntegrationTest {
    private static final String SHELL_COMMANDS_BUNDLE_NAME = "org.eclipse.virgo.kernel.osgicommand";
    private static final String CLASSLOADING_PACKAGE = "org.eclipse.virgo.kernel.osgicommand.helper";
 
     private static final String TEST_CLASS_NAME = ClassLoadingHelperIntegrationTests.class.getName();
     private static final String TEST_CLASS_PACKAGE = ClassLoadingHelperIntegrationTests.class.getPackage().getName();
 
     private static final String TEST_CLASS_NAME_PATH = TEST_CLASS_NAME.replace(".", "/");
 
     private final String FRAMEWORK_CLASS_PACKAGE = BundleContext.class.getPackage().getName();
     private final String FRAMEWORK_CLASS_NAME = BundleContext.class.getName();
 
     private Bundle currentBundle = null;
     private Bundle shellCommandsBundle = null;
     private Bundle systemBundle = null;
 
     private final String UNEXPORTED_ERROR_MESSAGE = "Package [%s] is reported as exported by [%s] with id [%s], but it is not";
     private final String EXPORTED_ERROR_MESSAGE = "Package [%s] is reported as not exported by [%s] with id [%s], but it is";
     private final String LOADED = "Class [%s] was loaded by by bundle [%s] with id [%s], but it should be available only in [%s]";
     private final String NOT_LOADED = "Class [%s] was not loaded from bundle [%s] with id [%s]";
 
     @Before
     public void setUp() throws Exception {
         // execute initialization code
         super.setup();
 
         for (Bundle bundle : context.getBundles()) {
             if (SHELL_COMMANDS_BUNDLE_NAME.equals(bundle.getSymbolicName())) {
                this.shellCommandsBundle = bundle; 
             }
         }
         
         assertNotNull("No bundles with symbolic name [" + SHELL_COMMANDS_BUNDLE_NAME + "] found in bundles " + Arrays.toString(context.getBundles()),
                       this.shellCommandsBundle);
 
         // get this bundle
         this.currentBundle = context.getBundle();
 
         // get the system bundle
         this.systemBundle = context.getBundle(0);
     }
 
     @Test
     public void testIsPackageExportedMethod() throws Exception {
         // Check which bundles export CLASSLOADING_PACKAGE
         assertTrue(String.format(EXPORTED_ERROR_MESSAGE, CLASSLOADING_PACKAGE, SHELL_COMMANDS_BUNDLE_NAME, shellCommandsBundle.getBundleId()),
                    ClassLoadingHelper.isPackageExported(context, CLASSLOADING_PACKAGE, shellCommandsBundle));
         assertFalse(String.format(UNEXPORTED_ERROR_MESSAGE, CLASSLOADING_PACKAGE, currentBundle.getSymbolicName(), currentBundle.getBundleId()),
                     ClassLoadingHelper.isPackageExported(context, CLASSLOADING_PACKAGE, currentBundle));
         assertFalse(String.format(UNEXPORTED_ERROR_MESSAGE, CLASSLOADING_PACKAGE, systemBundle.getSymbolicName(), systemBundle.getBundleId()),
                     ClassLoadingHelper.isPackageExported(context, CLASSLOADING_PACKAGE, systemBundle));
 
         // Check which bundles export CLASSLOADING_TEST_PACKAGE
         assertFalse(String.format(UNEXPORTED_ERROR_MESSAGE, TEST_CLASS_PACKAGE, SHELL_COMMANDS_BUNDLE_NAME, shellCommandsBundle.getBundleId()),
                     ClassLoadingHelper.isPackageExported(context, TEST_CLASS_PACKAGE, shellCommandsBundle));
         assertTrue(String.format(EXPORTED_ERROR_MESSAGE, TEST_CLASS_PACKAGE, currentBundle.getSymbolicName(), currentBundle.getBundleId()),
                    ClassLoadingHelper.isPackageExported(context, TEST_CLASS_PACKAGE, currentBundle));
         assertFalse(String.format(UNEXPORTED_ERROR_MESSAGE, TEST_CLASS_PACKAGE, systemBundle.getSymbolicName(), systemBundle.getBundleId()),
                     ClassLoadingHelper.isPackageExported(context, TEST_CLASS_PACKAGE, systemBundle));
     }
 
     @Test
     public void testGetBundlesContainingResource() throws Exception {
         final String CONTAINS_ERROR_MESSAGE = "Bundle [%s] is returned as bundle that contains the test class [%s]. The returned set of bundles is %s";
         final String DOES_NOT_CONTAIN_ERROR_MESSAGE = "Bundle [%s] is not returned as bundle that contains the test class [%s]. The returned set of bundles is %s";
         final String RESOURCE_NOT_FOUND = "Bundle [%s] is returned as bundle that contains the test class [%s], but the returned URLs [%s] doesn't seem to have it.";
 
         // Check which bundles contain this class
         Map<Bundle, List<String>> result = ClassLoadingHelper.getBundlesContainingResource(context, TEST_CLASS_NAME_PATH + ".class");
         assertFalse(String.format(CONTAINS_ERROR_MESSAGE, SHELL_COMMANDS_BUNDLE_NAME, TEST_CLASS_NAME, Arrays.toString(result.keySet().toArray())),
                     result.containsKey(shellCommandsBundle));
         assertTrue(String.format(DOES_NOT_CONTAIN_ERROR_MESSAGE, currentBundle.getSymbolicName(), TEST_CLASS_NAME, Arrays.toString(result.keySet().toArray())),
                    result.containsKey(currentBundle));
 
         // Check the resources contained in the bundles
         assertTrue(String.format(RESOURCE_NOT_FOUND, currentBundle.getSymbolicName(), TEST_CLASS_NAME, result.toString().contains(TEST_CLASS_NAME_PATH)),
                    result.toString().contains(TEST_CLASS_NAME_PATH));
     }
 
     @Test
     public void testGetBundlesLoadingClassMethod() throws Exception {
         final String CAN_LOAD_ERROR_MESSAGE = "Bundle [%s] is returned as bundle that can load the test class [%s]. The returned set of bundles is %s";
         final String CANNOT_LOAD_ERROR_MESSAGE = "Bundle [%s] is not returned as bundle that can load the test class [%s]. The returned set of bundles is %s";
         final String ORIGINATING_ERROR_MESSAGE = "Bundle [%s] is returned as originating bundle for class [%s]. The returned set of bundles is %s";
         final String NON_ORIGINATING_ERROR_MESSAGE = "Bundle [%s] is not returned as originating bundle for class [%s]. The returned set of bundles is %s";
 
         // Check which bundles can load this class
         Map<Bundle, Bundle> result = ClassLoadingHelper.getBundlesLoadingClass(context, TEST_CLASS_NAME);
         assertFalse(String.format(CAN_LOAD_ERROR_MESSAGE, SHELL_COMMANDS_BUNDLE_NAME, TEST_CLASS_NAME, Arrays.toString(result.keySet().toArray())),
                     result.containsKey(shellCommandsBundle));
         assertFalse(String.format(ORIGINATING_ERROR_MESSAGE, SHELL_COMMANDS_BUNDLE_NAME, TEST_CLASS_NAME, Arrays.toString(result.values().toArray())),
                     result.containsValue(shellCommandsBundle));
         assertTrue(String.format(CANNOT_LOAD_ERROR_MESSAGE, currentBundle.getSymbolicName(), TEST_CLASS_NAME, Arrays.toString(result.keySet().toArray())),
                    result.containsKey(currentBundle));
         assertTrue(String.format(NON_ORIGINATING_ERROR_MESSAGE, currentBundle.getSymbolicName(), TEST_CLASS_NAME, Arrays.toString(result.values().toArray())),
                    result.containsValue(currentBundle));
 
         // Check how osgicommand bundle can load BundleContext
         result = ClassLoadingHelper.getBundlesLoadingClass(context, FRAMEWORK_CLASS_NAME, SHELL_COMMANDS_BUNDLE_NAME);
         assertTrue(String.format(CANNOT_LOAD_ERROR_MESSAGE, SHELL_COMMANDS_BUNDLE_NAME, FRAMEWORK_CLASS_NAME, Arrays.toString(result.keySet().toArray())),
                    result.containsKey(shellCommandsBundle));
         assertTrue(String.format(NON_ORIGINATING_ERROR_MESSAGE, systemBundle.getSymbolicName(), FRAMEWORK_CLASS_NAME, Arrays.toString(result.values().toArray())),
                    result.containsValue(systemBundle));
     }
 
     @Test
     public void testTryToLoadClassMethod() throws Exception {
         assertNotNull(String.format(NOT_LOADED, TEST_CLASS_NAME, currentBundle.getSymbolicName(), currentBundle.getBundleId()),
                       ClassLoadingHelper.tryToLoadClass(TEST_CLASS_NAME, currentBundle));
         assertNull(String.format(LOADED, TEST_CLASS_NAME, SHELL_COMMANDS_BUNDLE_NAME, shellCommandsBundle.getBundleId(), currentBundle.getSymbolicName()),
                    ClassLoadingHelper.tryToLoadClass(TEST_CLASS_NAME, shellCommandsBundle));
     }
 
     @Test
     public void testExportAndLoad() throws Exception {
         // Check TEST_CLASS_* export and load
         assertFalse(String.format(UNEXPORTED_ERROR_MESSAGE, TEST_CLASS_PACKAGE, shellCommandsBundle.getSymbolicName(), shellCommandsBundle.getBundleId()),
                     ClassLoadingHelper.isPackageExported(context, TEST_CLASS_PACKAGE, shellCommandsBundle));
         assertNull(String.format(LOADED, TEST_CLASS_NAME, SHELL_COMMANDS_BUNDLE_NAME, shellCommandsBundle.getBundleId(), SHELL_COMMANDS_BUNDLE_NAME),
                    ClassLoadingHelper.tryToLoadClass(TEST_CLASS_NAME, shellCommandsBundle));
         assertTrue(String.format(EXPORTED_ERROR_MESSAGE, TEST_CLASS_PACKAGE, currentBundle.getSymbolicName(), currentBundle.getBundleId()),
                    ClassLoadingHelper.isPackageExported(context, TEST_CLASS_PACKAGE, currentBundle));
         assertNotNull(String.format(NOT_LOADED, TEST_CLASS_NAME, currentBundle.getSymbolicName(), currentBundle.getBundleId()),
                       ClassLoadingHelper.tryToLoadClass(TEST_CLASS_NAME, currentBundle));
 
         // Check FRAMEWORK_CLASS_* export and load
         assertFalse(String.format(UNEXPORTED_ERROR_MESSAGE, FRAMEWORK_CLASS_PACKAGE, currentBundle.getSymbolicName(), currentBundle.getBundleId()),
                     ClassLoadingHelper.isPackageExported(context, FRAMEWORK_CLASS_PACKAGE, currentBundle));
         assertNotNull(String.format(NOT_LOADED, FRAMEWORK_CLASS_NAME, currentBundle.getSymbolicName(), currentBundle.getBundleId()),
                       ClassLoadingHelper.tryToLoadClass(FRAMEWORK_CLASS_NAME, currentBundle));
         assertTrue(String.format(EXPORTED_ERROR_MESSAGE, FRAMEWORK_CLASS_PACKAGE, systemBundle.getSymbolicName(), systemBundle.getBundleId()),
                    ClassLoadingHelper.isPackageExported(context, FRAMEWORK_CLASS_PACKAGE, systemBundle));
         assertNotNull(String.format(NOT_LOADED, FRAMEWORK_CLASS_NAME, currentBundle.getSymbolicName(), currentBundle.getBundleId()),
                       ClassLoadingHelper.tryToLoadClass(FRAMEWORK_CLASS_NAME, systemBundle));
     }
 }
 
