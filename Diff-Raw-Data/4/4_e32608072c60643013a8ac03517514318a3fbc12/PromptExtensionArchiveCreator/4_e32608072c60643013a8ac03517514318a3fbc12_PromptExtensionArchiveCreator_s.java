 package ch.open.arquillian.lab06.client;
 
 import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
 import org.jboss.shrinkwrap.api.Archive;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 
 public class PromptExtensionArchiveCreator implements AuxiliaryArchiveAppender {
     @Override
     public Archive<?> createAuxiliaryArchive() {
        return ShrinkWrap.create(JavaArchive.class, "arquillian-ch-open-hello-extension.jar")
                          .addManifest();
     }
 
 }
