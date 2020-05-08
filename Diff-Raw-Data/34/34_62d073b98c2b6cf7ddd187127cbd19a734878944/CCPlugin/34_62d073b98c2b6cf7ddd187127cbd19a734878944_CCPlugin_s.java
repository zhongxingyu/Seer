 /* ====================================================================
  *   Copyright 2003-2005 Fabrizio Giustina.
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  * ====================================================================
  */
 package net.sf.commonclipse;
 
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 
 /**
  * Main plugin class.
  * @author fgiust
  * @version $Revision$ ($Author$)
  */
 public class CCPlugin extends AbstractUIPlugin
 {
 
     /**
      * "commonclipse".
      */
     public static final String PLUGIN_NAME = "commonclipse"; //$NON-NLS-1$
 
     /**
      * key for ToStringStyle name.
      */
     public static final String P_TOSTRING_STYLE = "tostring_style"; //$NON-NLS-1$
 
     /**
      * key for "javabean-style toString()".
      */
     public static final String P_TOSTRING_BEAN = "tostring_bean"; //$NON-NLS-1$
 
     /**
      * add appendSuper() in toString.
      */
     public static final String P_TOSTRING_SUPER = "tostring_super"; //$NON-NLS-1$
 
     /**
      * add an instance equality check to the generated equals() method.
      */
     public static final String P_EQUALS_INSTANCECHECK = "equals_instancecheck"; //$NON-NLS-1$
 
     /**
      * add appendSuper() in toString.
      */
     public static final String P_HASHCODE_SUPER = "hashcode_super"; //$NON-NLS-1$
 
     /**
      * add appendSuper() in toString.
      */
     public static final String P_EQUALS_SUPER = "equals_super"; //$NON-NLS-1$
 
     /**
      * add appendSuper() in toString.
      */
     public static final String P_COMPARETO_SUPER = "compareto_super"; //$NON-NLS-1$
 
     /**
     * esclusion list.
      */
     public static final String P_EXCLUDE = "exclude"; //$NON-NLS-1$
 
     /**
      * use final parameters in generated methods.
      */
     public static final String P_FINALPARAMETERS = "final_param"; //$NON-NLS-1$
 
     /**
      * don't ask for overwriting existing methods.
      */
     public static final String P_DONTASKONOVERWRITE = "dontask"; //$NON-NLS-1$
 
     /**
      * value for the P_TOSTRING_BEAN properties: use javabean properties in toString().
      */
     public static final String TOSTRINGSTYLE_BEAN = "bean"; //$NON-NLS-1$
 
     /**
      * value for the P_TOSTRING_BEAN properties: use fields in toString().
      */
     public static final String TOSTRINGSTYLE_FIELDS = "fields"; //$NON-NLS-1$
 
     /**
      * Shared instance.
      */
     private static CCPlugin plugin;
 
     /**
      * Constructor.
      */
     public CCPlugin()
     {
         plugin = this;
     }
 
     /**
      * Returns the shared instance.
      * @return shared instance
      */
     public static CCPlugin getDefault()
     {
         return plugin;
     }
 
     /**
      * Returns the workspace instance.
      * @return IWorkspace workspace instance
      */
     public static IWorkspace getWorkspace()
     {
         return ResourcesPlugin.getWorkspace();
     }
 
 }
