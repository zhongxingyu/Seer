 package com.github.sophiedankel.permeate.rules;
 
 import static com.android.SdkConstants.ANDROID_URI;
 import static com.android.SdkConstants.ATTR_NAME;
 
 import com.android.annotations.NonNull;
 import com.android.tools.lint.detector.api.Category;
 import com.android.tools.lint.detector.api.Detector;
 import com.android.tools.lint.detector.api.Implementation;
 import com.android.tools.lint.detector.api.Issue;
 import com.android.tools.lint.detector.api.Scope;
 import com.android.tools.lint.detector.api.Severity;
 import com.android.tools.lint.detector.api.Speed;
 import com.android.tools.lint.detector.api.XmlContext;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 
 import java.util.EnumSet;
 
 
 public class XmlPermissionsDetector extends Detector implements Detector.XmlScanner {
 	/** The issue detected */
 	public static final Issue ISSUE = Issue.create(
             "FindsAllPerms", //$NON-NLS-1$
             "Finds all declared permissions",
             "Looks for all user-defined and system-defined permission declarations in " +
             "AndroidManifest.xml file.",
             
            "Looks for all user-defined and system-defined permission declarations in " +
            "AndroidManifest.xml file. Longer explanation",
             
             Category.SECURITY,
             2,
             Severity.WARNING,
             new Implementation(
                     XmlPermissionsDetector.class,
                     EnumSet.of(Scope.MANIFEST)));
 	
 	@NonNull
     @Override
     public Speed getSpeed() {
         return Speed.FAST;
     }
 	
 	@Override
     public void visitElement(@NonNull XmlContext context, @NonNull Element element) {
         Attr nameNode = element.getAttributeNodeNS(ANDROID_URI, ATTR_NAME);
         if (nameNode != null) {
             String permissionName = nameNode.getValue();
             context.report(ISSUE, element, context.getLocation(nameNode),
             		"Permission detected in XML file, name: " + permissionName, null);
         }
     }
 
 }
