 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */ 
 package org.jboss.aop.deployment;
 
 import java.io.File;
 import java.lang.ref.WeakReference;
 import java.net.URL;
 import java.util.Iterator;
 
 import org.jboss.aop.AspectManager;
 import org.jboss.aop.classpool.AOPClassPool;
 import org.jboss.aop.classpool.AOPClassPoolRepository;
 import org.jboss.aop.deployment.LoaderRepositoryUrlUtil.UrlInfo;
 import org.jboss.mx.loading.HeirarchicalLoaderRepository3;
 import org.jboss.mx.loading.LoaderRepository;
 import org.jboss.mx.loading.RepositoryClassLoader;
 
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.NotFoundException;
 import javassist.scopedpool.ScopedClassPoolRepository;
 
 /**
  * A classpool in JBoss backed by a scoped (HierarchicalLoaderRepository) loader repository
  * 
 * @deprecated TODO JBAOP-107 Need a different version for the JBoss5 classloader 
  * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
  * @version $Revision: 1.1 $
  */
 @Deprecated
 public class ScopedJBossClassPool extends JBossClassPool 
 {
    final static LoaderRepositoryUrlUtil LOADER_REPOSITORY_UTIL = new LoaderRepositoryUrlUtil();
    
    WeakReference repository = null;
    UrlInfo urlInfo;
    ThreadLocal lastPool = new ThreadLocal();
 
    protected ScopedJBossClassPool(ClassLoader cl, ClassPool src, ScopedClassPoolRepository repository, File tmp, URL tmpURL)
    {
       super(cl, src, repository, tmp, tmpURL);
       
       boolean parentFirst = false;
       LoaderRepository loaderRepository = null;
       ClassLoader prnt = cl;
       while (prnt != null)
       {
          if (prnt instanceof RepositoryClassLoader)
          {
             loaderRepository = ((RepositoryClassLoader)prnt).getLoaderRepository();
             if (loaderRepository instanceof HeirarchicalLoaderRepository3)
             {
                parentFirst = ((HeirarchicalLoaderRepository3)loaderRepository).getUseParentFirst();
             }
             break;
          }
          prnt = cl.getParent();
       }
       
       super.childFirstLookup = !parentFirst;
    }
    
 
    private HeirarchicalLoaderRepository3 getRepository()
    {
       ClassLoader cl = getClassLoader0();
       if (cl != null)
       {
          return (HeirarchicalLoaderRepository3)((RepositoryClassLoader)cl).getLoaderRepository();
       }
       return null;
    }
 
    private URL getResourceUrlForClass(String resourcename)
    {
       HeirarchicalLoaderRepository3 repo = getRepository();
       return repo.getResource(resourcename, super.getClassLoader());
    }
    
    private boolean isMine(URL url)
    {
       HeirarchicalLoaderRepository3 repo = getRepository();
       if (repo != null)
       {
          //The URL of the class loaded with my scoped classloader
          if (url != null)
          {
             urlInfo = LOADER_REPOSITORY_UTIL.getURLInfo(getRepository(), urlInfo);
             
             URL[] myUrls = urlInfo.getLocalUrls();
             String resource = url.toString();
             for (int i = 0 ; i < myUrls.length ; i++)
             {
                if (resource.indexOf(myUrls[i].toString()) >= 0)
                {
                   return true;
                }
             }
             return false;
          }
       }
       return true;
    }
 
    public CtClass getCached(String classname)
    {
       if (classname == null)
       {
          return null;
       }
 
       if (generatedClasses.get(classname) != null)
       {
          //It is a new class, and this callback is probably coming from the frozen check when creating a new nested class
          return super.getCached(classname);
       }
       
       //Is this from the scoped classloader itself of from the parent?
       String resourcename = getResourceName(classname);
       URL url = getResourceUrlForClass(resourcename);
       boolean isMine = isMine(url);
       
       if (isMine)
       {
          if (super.childFirstLookup)
          {
             //Parent delegation is false, attempt to get this class out of ourselves
             CtClass clazz = super.getCachedLocally(classname);
             if (clazz == null)
             {
                clazz = createCtClass(classname, false);
                if (clazz != null)
                {
                   lockInCache(clazz);
                }
             }
             if (clazz != null)
             {
                return clazz;
             }
          }
          return super.getCached(classname);
       }
       
 
       try
       {
          ClassPool pool = getCorrectPoolForResource(url);
          if (pool != lastPool.get())
          {
             lastPool.set(pool);
             return pool.get(classname);
          }
       }
       catch (NotFoundException e)
       {
       }
       catch(StackOverflowError e)
       {
          throw e;
       }
       finally
       {
          lastPool.set(null);
       }
 
       return null;
    }
 
    private ClassPool getCorrectPoolForResource(URL url)
    {
       synchronized(AspectManager.getRegisteredCLs())
       {
          for(Iterator it = AspectManager.getRegisteredCLs().values().iterator() ; it.hasNext() ; )
          {
             AOPClassPool candidate = (AOPClassPool)it.next();
             if (candidate.isUnloadedClassLoader())
             {
                AspectManager.instance().unregisterClassLoader(candidate.getClassLoader());
                continue;
             }
             
             if (candidate.getClassLoader() instanceof RepositoryClassLoader)
             {
                //Sometimes the ClassLoader is a proxy for MBeanProxyExt?!
                RepositoryClassLoader rcl = (RepositoryClassLoader)candidate.getClassLoader();
                URL[] urls = rcl.getClasspath();
                String resource = url.toString();
                for (int i = 0 ; i < urls.length ; i++)
                {
                   if (resource.indexOf(urls[i].toString()) >= 0)
                   {
                      return candidate;
                   }
                }
             }
          }
       }
 
       return AOPClassPool.createAOPClassPool(ClassPool.getDefault(), AOPClassPoolRepository.getInstance());
    }
 }
