 package org.eclipse.jst.jsf.facelet.core.internal.tagmodel;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.ITagElement;
 import org.eclipse.jst.jsf.designtime.internal.view.model.jsp.ITagResolvingStrategy;
 import org.eclipse.jst.jsf.facelet.core.internal.registry.IFaceletTagResolvingStrategy.TLDWrapper;
 import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.IFaceletTagRecord;
 import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.faceletTaglib.FaceletTaglibTag;
 
 /**
  * A description about a facelet tag library descriptor (facelet-taglib_1_0.dtd)
  * 
  * @author cbateman
  * 
  */
 public class FaceletNamespace extends
         org.eclipse.jst.jsf.common.runtime.internal.view.model.common.Namespace
 {
     /**
      * 
      */
     private static final long          serialVersionUID = 2133853120220947741L;
     /**
      * The namespace that this tag library is associated with
      */
     private final FaceletNamespaceData _data;
 
     /**
      * @param record
      * @param resolver
      */
     public FaceletNamespace(final IFaceletTagRecord record,
             final ITagResolvingStrategy<TLDWrapper, String> resolver)
     {
         _data = new TaglibFaceletNamespaceData(record, resolver);
     }
 
     @Override
     public String getDisplayName()
     {
         return _data.getDisplayName();
     }
 
     @Override
     public String getNSUri()
     {
         return _data.getUri();
     }
 
     @Override
     public Collection<? extends ITagElement> getViewElements()
     {
        return _data.getAllViewElements().values();
     }
 
     @Override
     public String toString()
     {
         return "Namespace: " + getNSUri() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
     }
 
     @Override
     public ITagElement getViewElement(final String name)
     {
         return _data.getViewElement(name);
     }
 
     @Override
     public boolean hasViewElements()
     {
         return _data.getNumTags() > 0;
     }
 
     @Override
     public boolean isInitialized()
     {
        return _data.isInitialized();
     }
 
     /**
      * Namespace data driven off a record.
      * 
      */
     private static class TaglibFaceletNamespaceData extends
             FaceletNamespaceData
     {
         /**
          * 
          */
         private static final long                                         serialVersionUID = -562720162853425804L;
         private transient final IFaceletTagRecord                         _record;
         private transient final ITagResolvingStrategy<TLDWrapper, String> _resolver;
         private final Map<String, ITagElement>                            _tags;
 
         public TaglibFaceletNamespaceData(final IFaceletTagRecord record,
                 final ITagResolvingStrategy<TLDWrapper, String> resolver)
         {
             _record = record;
             _tags = new HashMap<String, ITagElement>();
             _resolver = resolver;
         }
 
         @Override
         public synchronized Map<String, ITagElement> getAllViewElements()
         {
             if (!isInitialized())
             {
                 for (final FaceletTaglibTag tagDefn : _record.getTags())
                 {
                     getViewElement(tagDefn.getTagName());
                 }
             }
             return _tags;
         }
 
         @Override
         public synchronized ITagElement getViewElement(final String name)
         {
             final FaceletTaglibTag tagDefn = _record.getTag(name);
             if (tagDefn != null)
             {
                 return getAndInitIfMissing(tagDefn);
             }
             return null;
         }
 
         private ITagElement getAndInitIfMissing(final FaceletTaglibTag tagDefn)
         {
             ITagElement tagElement = _tags.get(tagDefn.getTagName());
             if (tagElement == null)
             {
                 tagElement = _resolver
                         .resolve(new TLDWrapper(tagDefn, getUri()));
                 _tags.put(tagDefn.getTagName(), tagElement);
             }
             return tagElement;
         }
 
         @Override
         public synchronized boolean isInitialized()
         {
             return _tags.size() == _record.getNumTags();
         }
 
         @Override
         public String getDisplayName()
         {
             return _record.getURI();
         }
 
         @Override
         public int getNumTags()
         {
             return _record.getNumTags();
         }
 
         @Override
         public String getUri()
         {
             return _record.getURI();
         }
     }
 
     /**
      * Encapsulates all the data for a TLDNamespace. Allows the model to be
      * separated from the Namespace interface for ease of serialization and
      * controlled subclassing.
      * 
      */
     public abstract static class FaceletNamespaceData implements Serializable
     {
         /**
          * 
          */
         private static final long serialVersionUID = 1697605990460247389L;
 
         /**
          * @return the displayb
          */
         public abstract String getDisplayName();
 
         /**
          * @return the number of tags
          */
         public abstract int getNumTags();
 
         /**
          * @return the namespace uri
          */
         public abstract String getUri();
 
         /**
          * @param name
          * @return the view element for name or null if not found.
          */
         public abstract ITagElement getViewElement(final String name);
 
         /**
          * May be long running since it will lazily calculate all unloaded tags.
          * 
          * @return all view elements for this namespace
          */
         public abstract Map<String, ITagElement> getAllViewElements();
 
         /**
          * @return true if all elements have been lazily loaded
          */
         public abstract boolean isInitialized();
     }
 }
