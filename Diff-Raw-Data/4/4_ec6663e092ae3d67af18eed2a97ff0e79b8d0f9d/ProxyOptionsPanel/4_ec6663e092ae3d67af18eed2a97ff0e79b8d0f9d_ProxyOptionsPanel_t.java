 /*
  * ProxyOptionsPanel.java
  *
  * Created on 14.09.2009, 15:01:16
  */
 package de.cismet.lookupoptions.options;
 
 import de.cismet.lookupoptions.*;
 import de.cismet.security.Proxy;
 import de.cismet.security.WebAccessManager;
 import de.cismet.tools.configuration.NoWriteError;
 import org.jdom.Element;
 import org.openide.util.lookup.ServiceProvider;
 
 /**
  * OptionsPanel for the Proxy Options.
  *
  * This panel allows to configure the proxy. Proxy-configuration affects the
  * WebAccessManager and the proxy of the java http protocol handler
  * (System.setProperty("http.proxyHost") & System.setProperty("http.proxyPort")).
  *
  * @author jruiz
  */
 @ServiceProvider(service = OptionsPanelController.class)
 public class ProxyOptionsPanel extends AbstractOptionsPanel implements OptionsPanelController {
 
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     
     private static final String OPTION_NAME = org.openide.util.NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.OptionController.name");
     private static final String CONFIGURATION = "ProxyOptionsPanel";
     private static final String CONF_TYPE = "ProxyType";
     private static final String CONF_HOST = "ProxyHost";
     private static final String CONF_PORT = "ProxyPort";
 
     private static enum ProxyTypes { NO, SYSTEM, MANUAL }
     private boolean stillConfigured = false;
     private ProxyTypes proxyType;
     private String host;
     private int port;
 
     /** Creates new form ProxyOptionsPanel */
     public ProxyOptionsPanel() {
         super(OPTION_NAME, NetworkOptionsCategory.class);
         initComponents();
     }
 
     @Override
     public int getOrder() {
         return 1;
     }
 
     @Override
     public void update() {
         // hier werden die Werte aus dem Proxy ausgelesen
         final Proxy proxy = WebAccessManager.getInstance().getHttpProxy();
         if (proxy != null) {
             proxyType = ProxyTypes.MANUAL;
             host = proxy.getHost();
             port = proxy.getPort();
         } else if (System.getProperty("http.proxyHost") != null && System.getProperty("http.proxyPort") != null) {
             proxyType = ProxyTypes.SYSTEM;
         } else {
             proxyType = ProxyTypes.NO;
         }
 
         // Componenten füttern
         switch (proxyType) {
             case MANUAL:
                 rdoManualProxy.setSelected(true);
                 txtHost.setText(host);
                 if (port > 0) {
                     txtPort.setText(Integer.toString(port));
                 } else {
                     txtPort.setText("");
                 }
                 break;
             default:
                 rdoNoProxy.setSelected(true);
         }
     }
 
     @Override
     public void applyChanges() {
         boolean useProxy;
         if (rdoManualProxy.isSelected()) {
             useProxy = true;
             proxyType = ProxyTypes.MANUAL;
             host = txtHost.getText().trim();
             try {
                 port = Integer.valueOf(txtPort.getText().trim());
             } catch (NumberFormatException ex) {
                 port = 0;
             }
         } else {
             proxyType = ProxyTypes.NO;
             useProxy = false;
         }
 
         // hier werden die Werte in dem Proxy gesetzt
         setProxy(useProxy, host, port);
     }
 
     @Override
     public boolean isChanged() {
         int intPort;
         try {
             intPort = Integer.valueOf(txtPort.getText());
         } catch (NumberFormatException ex) {
             intPort = 0;
         }
         return ((rdoNoProxy.isSelected() && proxyType != ProxyTypes.NO) ||
                 (rdoManualProxy.isSelected() && proxyType != ProxyTypes.MANUAL) ||
                 !txtHost.getText().equals(host) ||
                 intPort != port);
     }
 
     @Override
     public String getTooltip() {
         return "change Proxy settings (tooltip test)";
     }
 
     /*@Override
     public String getHelp() {
         return
                 "";
     }*/
 
     /**
      * Applies the proxy settings for the WebAccessManager and for the proxy of
      * the java http protocol handler.
      * 
      * @param isActivated Should the proxy be used
      * @param host Proxy Host
      * @param port Proxy Port
      */
     private void setProxy(final boolean isActivated, final String host, final int port) {
         if (isActivated) {
             final Proxy newProxy = new Proxy(host, port);
             WebAccessManager.getInstance().setHttpProxy(newProxy);
             log.debug("set proxy in system-property: " + newProxy);
             System.setProperty("http.proxyHost", newProxy.getHost());
             System.setProperty("http.proxyPort", String.valueOf(newProxy.getPort()));
         } else {
             WebAccessManager.getInstance().setHttpProxy(null);
             log.debug("set proxy in system-property: null");
            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
         }
     }
 
     @Override
     public void configure(Element parent) {
         if (!stillConfigured) {
             log.debug("Configure ProxyOptionPanels");
             try {
                 String elementProxyType = "";
                 String elementProxyHost = "";
                 String elementProxyPort = "";
                 if (parent != null) {
                     final Element conf = parent.getChild(CONFIGURATION);
                     if (conf != null) {
                         elementProxyType = conf.getChildText(CONF_TYPE);
                         elementProxyHost = conf.getChildText(CONF_HOST);
                         elementProxyPort = conf.getChildText(CONF_PORT);
                     }
                 }
                 if (elementProxyType.equals(ProxyTypes.MANUAL.toString())) {
                     proxyType = ProxyTypes.MANUAL;
                 } else {
                     proxyType = ProxyTypes.NO;
                 }
 
                 host = elementProxyHost;
                 try {
                     port = Integer.valueOf(elementProxyPort);
                 } catch (NumberFormatException ex) {
                     port = 0;
                 }
             } catch (Exception ex) {
                 log.error("Fehler beim Konfigurieren des ProxyOptionPanels", ex);
             }
 
             // hier werden die Werte in der GUI gesetzt
             txtHost.setText(host);
             if (port > 0) {
                 txtPort.setText(Integer.toString(port));
             } else {
                 txtPort.setText("");
             }
             switch (proxyType) {
                 case MANUAL:
                     rdoManualProxy.setSelected(true);
                     break;
                 default:
                     rdoNoProxy.setSelected(true);
             }
 
             stillConfigured = true;
         } else {
             log.debug("skip Configure ProxyOptionPanels - still configured");
         }
 
         // Änderungen anwenden
         applyChanges();
     }
 
     @Override
     public Element getConfiguration() throws NoWriteError {
         log.debug("ProxyOptionPanels - getConfiguration");
         Element conf = new Element(CONFIGURATION);
 
         Element proxyTypeElement = new Element(CONF_TYPE);
         Element proxyHostElement = new Element(CONF_HOST);
         Element proxyPortElement = new Element(CONF_PORT);
 
         log.debug("    type: " + proxyType.toString());
         log.debug("    host: " + host);
         log.debug("    port: " + Integer.toString(port));
 
         proxyTypeElement.addContent(proxyType.toString());
         proxyHostElement.addContent(host);
         proxyPortElement.addContent(Integer.toString(port));
 
         conf.addContent(proxyTypeElement);
         conf.addContent(proxyHostElement);
         conf.addContent(proxyPortElement);
 
         return conf;
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         buttonGroup1 = new javax.swing.ButtonGroup();
         labHost = new javax.swing.JLabel();
         txtHost = new javax.swing.JTextField();
         txtPort = new javax.swing.JTextField();
         labPort = new javax.swing.JLabel();
         rdoNoProxy = new javax.swing.JRadioButton();
         rdoManualProxy = new javax.swing.JRadioButton();
 
         labHost.setText(org.openide.util.NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.labHost.text")); // NOI18N
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rdoManualProxy, org.jdesktop.beansbinding.ELProperty.create("${selected}"), labHost, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rdoManualProxy, org.jdesktop.beansbinding.ELProperty.create("${selected}"), txtHost, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rdoManualProxy, org.jdesktop.beansbinding.ELProperty.create("${selected}"), txtPort, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         labPort.setText(org.openide.util.NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.labPort.text")); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, rdoManualProxy, org.jdesktop.beansbinding.ELProperty.create("${selected}"), labPort, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         buttonGroup1.add(rdoNoProxy);
         rdoNoProxy.setText(org.openide.util.NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.rdoNoProxy.text")); // NOI18N
 
         buttonGroup1.add(rdoManualProxy);
         rdoManualProxy.setText(org.openide.util.NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.rdoManualProxy.text")); // NOI18N
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(rdoNoProxy)
                             .addComponent(rdoManualProxy))
                         .addGap(179, 179, 179))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(labHost)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(txtHost, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(labPort)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap())))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(rdoNoProxy)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(rdoManualProxy)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(labHost)
                     .addComponent(txtHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(labPort))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         labHost.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.labHost.text")); // NOI18N
         labPort.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.labPort.text")); // NOI18N
         rdoNoProxy.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.rdoNoProxy.text")); // NOI18N
         rdoManualProxy.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ProxyOptionsPanel.class, "ProxyOptionsPanel.rdoManualProxy.text")); // NOI18N
 
         bindingGroup.bind();
     }// </editor-fold>//GEN-END:initComponents
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.JLabel labHost;
     private javax.swing.JLabel labPort;
     private javax.swing.JRadioButton rdoManualProxy;
     private javax.swing.JRadioButton rdoNoProxy;
     private javax.swing.JTextField txtHost;
     private javax.swing.JTextField txtPort;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 }
