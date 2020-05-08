 /**
  * 
  */
 package com.github.podd.impl.file.test;
 
 import com.github.podd.api.file.FileReferenceManager;
 import com.github.podd.api.file.FileReferenceProcessorFactoryRegistry;
 import com.github.podd.api.file.test.AbstractFileReferenceManagerTest;
 import com.github.podd.impl.file.FileReferenceManagerImpl;
import com.github.podd.impl.file.SSHFileReferenceProcessorFactoryImpl;
 
 /**
  * Concrete test for FileReferenceManager
  * 
  * @author kutila
  */
 public class FileReferenceManagerImplTest extends AbstractFileReferenceManagerTest
 {
     
    /* (non-Javadoc)
     * @see com.github.podd.api.file.test.AbstractFileReferenceManagerTest#getNewFileReferenceManager()
      */
     @Override
     public FileReferenceManager getNewFileReferenceManager()
     {
         FileReferenceManagerImpl fileRefManager = new FileReferenceManagerImpl();
         return fileRefManager;
     }
     
    /* (non-Javadoc)
     * @see com.github.podd.api.file.test.AbstractFileReferenceManagerTest#getNewPoddFileReferenceProcessorFactoryRegistry()
      */
     @Override
     public FileReferenceProcessorFactoryRegistry getNewPoddFileReferenceProcessorFactoryRegistry()
     {
         FileReferenceProcessorFactoryRegistry registry = new FileReferenceProcessorFactoryRegistry();
        registry.add(new SSHFileReferenceProcessorFactoryImpl());
         
         return registry;
     }
     
 }
