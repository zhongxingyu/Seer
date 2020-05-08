 package org.commonjava.web.fd.fixture;
 
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
 import javax.inject.Singleton;
 
 import org.junit.rules.TemporaryFolder;
 
 @Singleton
 public class InjectableTemporaryFolder
     extends TemporaryFolder
 {
 
    @PostConstruct
    public void postConstruct()
        throws Throwable
    {
        before();
        System.out.println( "Temporary folder is: " + getRoot().getCanonicalPath() );
    }

    @PreDestroy
    public void destroy()
        throws IOException
    {
        System.out.println( "Destroying temporary folder in: " + getRoot().getCanonicalPath() );
        after();
    }

 }
