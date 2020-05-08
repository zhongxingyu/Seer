 package pt.ist.bennu.vaadin.domain.contents;
 
 import myorg.domain.VirtualHost;
 import myorg.domain.contents.Node;
 import myorg.domain.groups.PersistentGroup;
 import pt.ist.fenixWebFramework.services.Service;
 
 public class VaadinNode extends VaadinNode_Base {
 
     public VaadinNode(final VirtualHost virtualHost, final Node parentNode, final String linkBundle, final String linkKey,
 	    final String argument, final PersistentGroup accessibilityGroup) {
 	super();
 	init(virtualHost, parentNode, linkBundle, linkKey, argument, accessibilityGroup, true);
     }
 
     public VaadinNode(final VirtualHost virtualHost, final Node parentNode, final String linkBundle, final String linkKey,
 	    final String argument, final PersistentGroup accessibilityGroup, final boolean useBennuLayout) {
 	super();
 	init(virtualHost, parentNode, linkBundle, linkKey, argument, accessibilityGroup, useBennuLayout);
     }
 
     protected void init(final VirtualHost virtualHost, final Node parentNode, final String linkBundle, final String linkKey,
 	    final String argument, final PersistentGroup accessibilityGroup, final boolean useBennuLayout) {
 	final String method = useBennuLayout ? "forwardToVaadin" : "forwardToFullVaadin";
 	init(virtualHost, parentNode, "/vaadinContext", method, linkBundle, linkKey, accessibilityGroup);
 	setArgument(argument);
     }
 
     @Service
     public static VaadinNode createVaadinNode(final VirtualHost virtualHost, final Node parentNode, final String linkBundle,
 	    final String linkKey, final String argument, final PersistentGroup accessibilityGroup) {
 	return new VaadinNode(virtualHost, parentNode, linkBundle, linkKey, argument, accessibilityGroup);
     }
 
     @Service
     public static VaadinNode createVaadinNode(final VirtualHost virtualHost, final Node parentNode, final String linkBundle,
 	    final String linkKey, final String argument, final PersistentGroup accessibilityGroup, boolean useBennuLayout) {
 	return new VaadinNode(virtualHost, parentNode, linkBundle, linkKey, argument, accessibilityGroup, useBennuLayout);
     }
 
     @Override
    public String getUrl(final String appContext) {
 	final StringBuilder stringBuilder = new StringBuilder();
	stringBuilder.append(super.getUrl(appContext));
 	stringBuilder.append("#");
 	stringBuilder.append(getArgument());
 	return stringBuilder.toString();
     }
 
     @Override
     public boolean isRedirect() {
         return true;
     }
 
 }
