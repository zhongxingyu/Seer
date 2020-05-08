 package net.cyklotron.cms.category.internal;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import net.cyklotron.cms.category.CategoryException;
 import net.cyklotron.cms.category.CategoryMapResource;
 import net.cyklotron.cms.category.CategoryResource;
 import net.cyklotron.cms.category.CategoryResourceImpl;
 import net.cyklotron.cms.category.CategoryService;
 import net.cyklotron.cms.integration.IntegrationService;
 import net.cyklotron.cms.integration.ResourceClassResource;
 import net.cyklotron.cms.site.SiteResource;
 
 import org.jcontainer.dna.Logger;
 import org.objectledge.ComponentInitializationError;
 import org.objectledge.coral.entity.AmbigousEntityNameException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.entity.EntityExistsException;
 import org.objectledge.coral.entity.EntityInUseException;
 import org.objectledge.coral.event.ResourceDeletionListener;
 import org.objectledge.coral.relation.Relation;
 import org.objectledge.coral.relation.RelationModification;
 import org.objectledge.coral.schema.CircularDependencyException;
 import org.objectledge.coral.schema.ResourceClass;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.session.CoralSessionFactory;
 import org.objectledge.coral.store.Resource;
 
 /**
  * Implementation of Category Service.
  *
  * @author <a href="mailto:pablo@ngo.pl">Pawel Potempski</a>.
  * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: CategoryServiceImpl.java,v 1.3 2005-01-18 16:33:34 pablo Exp $
  */
 public class CategoryServiceImpl 
     implements CategoryService, ResourceDeletionListener
 {
    /** workgroups relation name */
     public static final String RESOURCES_RELATION_NAME = "category.References";
     
    /** subjects relation name */
     public static final String RESOURCE_TYPE_RELATION_NAME = "category.ResourceTypeReferences";
 
     /** integration service */
     private IntegrationService integrationService;
 
     /** System category root */
     private CategoryMapResource categoryMap;
 
     /** coral session factory */
     private CoralSessionFactory sessionFactory;
     
     /** logger. */
     private Logger log;
     
     /** rc - category relation */
     private Relation resourceClassRelation;
     
     /** resources relation */
     private Relation resourcesRelation;
 
     
     // initialization ///////////////////////////////////////////////////////
 
     /**
      * Initializes the service.
      */
     public CategoryServiceImpl(CoralSessionFactory sessionFactory, Logger logger,
         IntegrationService integrationService)
     {
         this.log = logger;
         this.sessionFactory = sessionFactory;
         this.integrationService = integrationService;
         CoralSession coralSession = sessionFactory.getRootSession();
         try
         {
             Resource[] res = coralSession.getStore().getResourceByPath(SYSTEM_CATEGORIES);
             if (res.length == 0)
             {
                 throw new ComponentInitializationError("failed to lookup system-wide category root " + SYSTEM_CATEGORIES);
             }
             if (res.length > 1)
             {
                 throw new ComponentInitializationError("ambigous pathname " + SYSTEM_CATEGORIES);
             }
             categoryMap = (CategoryMapResource)res[0];
             coralSession.getEvent().addResourceDeletionListener(this, null);
         }
         finally
         {
             coralSession.close();
         }
     }
 
 	/**
 	 * Returns the resource which contains category cross references.
 	 */
 	public CategoryMapResource getCategoryMap()
 	{
 		return categoryMap;
 	}
 
 
     /**
      * Returns the root of category tree.
      *
      * @param site the site to return category tree for, or <code>null</code>
      *        for system-wide categories.
      */
     public Resource getCategoryRoot(CoralSession coralSession, SiteResource site) throws CategoryException
     {
         if (site == null)
         {
             return categoryMap;
         }
         Resource[] res = coralSession.getStore().getResource(site, SITE_CATEGORIES);
         if (res.length == 0)
         {
             throw new CategoryException("failed to lookup category root for site " + site.getName());
         }
         if (res.length > 1)
         {
             throw new CategoryException("multiple category roots for site " + site.getName());
         }
         return res[0];
     }
 
     /**
      * Returns a list of super categories of a specified category.
      *
      * @param category the category resource.
      * @param includeSelf <code>true</code> to include <code>category</code>
      *        in the list.
      * @return the list of categories.
      */
     public CategoryResource[] getImpliedCategories(CategoryResource category, boolean includeSelf)
     {
         ArrayList target = new ArrayList();
         if (includeSelf)
         {
             target.add(category);
         }
         Resource parent = category.getParent();
         while (parent instanceof CategoryResource)
         {
             target.add(parent);
             parent = parent.getParent();
         }
         CategoryResource[] result = new CategoryResource[target.size()];
         target.toArray(result);
         return result;
     }
 
     /**
      * Returns a list of sub categories of a specified category.
      *
      * @ deprecated WARN: this is used only in groups
      * @param category the category resource.
      * @param includeSelf <code>true</code> to include <code>category</code>
      *        in the list.
      * @return the list of categories.
      */
     public CategoryResource[] getSubCategories(CoralSession coralSession, CategoryResource category, boolean includeSelf)
     {
     	if(category == null)
     	{
     		return new CategoryResource[0];
     	}
 
         List target = new ArrayList();
         if (includeSelf)
         {
             target.add(category);
         }
         collectSubCategories(coralSession, category, target);
         CategoryResource[] result = new CategoryResource[target.size()];
         target.toArray(result);
         return result;
     }
 
     private void collectSubCategories(CoralSession coralSession, Resource resource, List target)
     {
         Resource[] categories = coralSession.getStore().getResource(resource);
         for (int i = 0; i < categories.length; i++)
         {
             target.add(categories[i]);
             collectSubCategories(coralSession, categories[i], target);
         }
     }
 
     /**
      * Returns all categories the resource belongs to.
      *
      * @param resource the resource.
      * @param includeImplied <code>false</code> to list categories assigned
      *        directly, <code>true</code> to include super-categories also.
      */
     public CategoryResource[] getCategories(CoralSession coralSession, Resource resource, boolean includeImplied)
     {
         Relation reference = getResourcesRelation(coralSession);
         Resource[] refCategories = reference.getInverted().get(resource);
         Set target = new HashSet();
         target.addAll(Arrays.asList(refCategories));
         if (includeImplied)
         {
             for (int i = 0; i < refCategories.length; i++)
             {
                 CategoryResource category = (CategoryResource)refCategories[i];
                 CategoryResource[] categories = getImpliedCategories(category, false);
                 for (int j = 0; j < categories.length; j++)
                 {
                     target.add(categories[j]);
                 }
             }
         }
         CategoryResource[] result = new CategoryResource[target.size()];
         target.toArray(result);
         return result;
     }
 
     /**
      * Returns all resources belonging to a category.
      *
      * <p>Note that the meaning of <code>includeImplied</code> parameter of
      * this method is quite different than in the {@link
      * #getImpliedCategories(CategoryResource,boolean)}, and in general case
      * setting this one to <code>true</code> makes less senese.</p>
      *
      * @param category the category.
      * @param includeImplied <code>false</code> to return only resources
      *        assigned directly to the given category, <code>true</code> to
      *        inclue resources assigned directly to sub-categories of the
      *        given category.
      */
     public Resource[] getResources(CoralSession coralSession, CategoryResource category, boolean includeImplied) throws CategoryException
     {
         Relation refs = getResourcesRelation(coralSession);
         if (!includeImplied)
         {
             return refs.get(category);
         }
         else
         {
             Set target = new HashSet();
             List categories = new ArrayList();
             getSubCategories(coralSession, category, categories);
             for (int i = 0; i < categories.size(); i++)
             {
                 CategoryResource cat = (CategoryResource)categories.get(i);
                 Resource[] resources = refs.get(cat);
                 for (int j = 0; j < resources.length; j++)
                 {
                     target.add(resources[j]);
                 }
             }
             Resource[] result = new Resource[target.size()];
             target.toArray(result);
             return result;
         }
     }
 
     /**
      * Collects subcategories of a category in a list.
      *
      * @param resource the category.
      * @param list the list to store results to.
      */
     private void getSubCategories(CoralSession coralSession, Resource resource, List list)
     {
         list.add(resource);
         Resource[] children = coralSession.getStore().getResource(resource);
         for (int i = 0; i < children.length; i++)
         {
             getSubCategories(coralSession, children[i], list);
         }
     }
 
     /**
      * Adds a new category to the system.
      *
      * @param name the category name.
      * @param description the category description.
      * @param parent the parent category or category tree root.
      * @param subject the creator.
      * @return category resource.
      */
     public CategoryResource addCategory(CoralSession coralSession, String name, String description, Resource parent, ResourceClassResource[] resourceClasses)
         throws CategoryException
     {
         CategoryResource category = CategoryResourceImpl.createCategoryResource(coralSession, name, parent);
         category.setDescription(description);
         category.update();
         setCategoryResourceClasses(coralSession, category, resourceClasses);
         return category;
     }
 
     /**
      * Deletes a category from the system.
      *
      * @param category the category to delete.
      * @param subject the subject performing delete action.
      */
     public void deleteCategory(CoralSession coralSession, CategoryResource category) throws CategoryException
     {
         CategoryResource[] cats = getSubCategories(coralSession, category, false);
         if (cats.length > 0)
         {
             throw new CategoryException("Cannot remove categories with children");
         }
         // remove resource categorization references
         Relation refs = getResourcesRelation(coralSession);
         RelationModification diff = new RelationModification();
         diff.remove(category);
         coralSession.getRelationManager().updateRelation(refs, diff);
         // remove resource class references
         setCategoryResourceClasses(coralSession, category, new ResourceClassResource[0]);
         // remove resource
         try
         {
             coralSession.getStore().deleteResource(category);
         }
         catch (EntityInUseException e)
         {
             throw new CategoryException("Entity in use exception", e);
         }
     }
 
     /**
      * Update a category.
      *
      * @param category the category being updated.
      * @param name new category name.
      * @param description new category description.
      * @param parent new category parent (can be another category, or category
      *        root)
      * @param subject the subject performing update action.
      */
     public void updateCategory(
         CoralSession coralSession, 
         CategoryResource category,
         String name,
         String description,
         Resource parent,
         ResourceClassResource[] resourceClasses)
         throws CategoryException
     {
         if (!category.getName().equals(name))
         {
             coralSession.getStore().setName(category, name);
         }
         if (!description.equals(category.getDescription()))
         {
             category.setDescription(description);
             category.update();
         }
         setCategoryResourceClasses(coralSession, category, resourceClasses);
         if (!parent.equals(category.getParent()))
         {
             try
             {
                 coralSession.getStore().setParent(category, parent);
             }
             catch (CircularDependencyException e)
             {
                 throw new CategoryException("circular dependedncy", e);
             }
         }
     }
 
     private void setCategoryResourceClasses(CoralSession coralSession, CategoryResource category, ResourceClassResource[] resourceClasses) throws CategoryException
     {
         Relation refs = getResourceClassRelation(coralSession);
         RelationModification diff = new RelationModification();
         diff.remove(category);
         for (int i = 0; i < resourceClasses.length; i++)
         {
             if (resourceClasses[i].getCategorizable())
             {
                 diff.add(category, resourceClasses[i]);
             }
         }
         coralSession.getRelationManager().updateRelation(refs, diff);
     }
 
     private void unsetCategoryResourceClass(CoralSession coralSession, ResourceClassResource resourceClass)
         throws CategoryException
     {
         Relation refs = getResourceClassRelation(coralSession);
         RelationModification diff = new RelationModification();
         diff.removeInv(resourceClass);
         coralSession.getRelationManager().updateRelation(refs, diff);
     }
 
     /**
      * Returns all resource classes bound to a category.
      *
      * @param category the category.
      * @return an array of resource class resources bound to a category
      */
     public ResourceClassResource[] getResourceClasses(CoralSession coralSession, CategoryResource category, boolean includeImplied)
     {
         Relation refs = getResourceClassRelation(coralSession);
         CategoryResource[] categories;
         if (includeImplied)
         {
             categories = getImpliedCategories(category, true);
         }
         else
         {
             categories = new CategoryResource[1];
             categories[0] = category;
         }
         HashSet resClasses = new HashSet();
         for (int i = 0; i < categories.length; i++)
         {
             resClasses.addAll(Arrays.asList(refs.get(categories[i])));
         }
         ResourceClassResource[] resClasses2 = new ResourceClassResource[resClasses.size()];
         return (ResourceClassResource[]) (resClasses.toArray(resClasses2));
     }
 
     /**
      * Returns <code>true</code> if given category is bound to a given resource class.
      *
      * @param category the category.
      * @param resClass the resource class.
      * @return <code>true</code> if given category is bound to given resource class.
      */
     public boolean hasResourceClass(CoralSession coralSession, CategoryResource category, ResourceClassResource resClass)
     {
         Relation refs = getResourceClassRelation(coralSession);
         return refs.hasRef(category, resClass);
     }
 
     /**
      * Returns <code>true</code> if given category supports given resource class,
      * ie. if the category or one of the parent categories is bound to a given resource class.
      *
      * @param category the category.
      * @param resClass the resource class.
      * @return <code>true</code> if given category is bound to given resource class.
      */
     public boolean supportsResourceClass(CoralSession coralSession, CategoryResource category, ResourceClassResource resClass)
     {
         Relation refs = getResourceClassRelation(coralSession);
         CategoryResource[] parentCats = getImpliedCategories(category, true);
         for (int i = 0; i < parentCats.length; i++)
         {
             if (refs.hasRef(parentCats[i], resClass))
             {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Remove a resource collection from a category.
      *
      * @param resources the resource collection.
      * @param category the category.
      * @param subject the subject that performs the operation.
      */
     public void removeFromCategory(CoralSession coralSession, Resource[] resources, CategoryResource category) throws CategoryException
     {
         Relation refs = getResourcesRelation(coralSession);
         RelationModification diff = new RelationModification();
         for (int i = 0; i < resources.length; i++)
         {
             diff.remove(category, resources[i]);
         }
         coralSession.getRelationManager().updateRelation(refs, diff);
     }
 
     /**
      * Remove a resource from all categories.
      *
      * @param resource the resource.
      */
     public void removeFromAllCategories(CoralSession coralSession, Resource resource) throws CategoryException
     {
         Relation refs = getResourcesRelation(coralSession);
         RelationModification diff = new RelationModification();
         diff.removeInv(resource);
         coralSession.getRelationManager().updateRelation(refs, diff);
     }
 
     // optimisation /////////////////////////////////////////////////////////////////////////
 
     /**
      *  Optimises Resource Class assignments in category tree.
      */
     public Set optimiseResourceClassesAssignments(CoralSession coralSession, CategoryResource category, boolean recursive) throws CategoryException
     {
         Relation refs = getResourceClassRelation(coralSession);
         RelationModification diff = new RelationModification();
         // get category - resource class references
 
         // prepare parent categories resource classes
         HashSet impliedResClasses = new HashSet();
         Resource parent = category.getParent();
         if (parent instanceof CategoryResource)
         {
             impliedResClasses.addAll(Arrays.asList(getResourceClasses(coralSession, (CategoryResource)parent, true)));
         }
 
         HashSet removedResClasses = new HashSet();
         // traverse down the tree and remove repeated resource class assignments
         optimiseRCA(coralSession, refs, diff, impliedResClasses, category, removedResClasses, recursive);
         coralSession.getRelationManager().updateRelation(refs, diff);
         // returns removed resource classes
         return removedResClasses;
     }
 
     private void optimiseRCA(CoralSession coralSession, Relation refs, RelationModification diff, HashSet impliedResClasses, CategoryResource category, HashSet removedResClasses, boolean recursive)
     {
         // TODO check this - if the because the diff is applied after all RCA optimisation was done!!!
         
         // get resource classes directly assigned to category
         ResourceClassResource[] resClasses = getResourceClasses(coralSession, category, false);
 
         // remove ones which are also bound to parent categories
         for (int i = 0; i < resClasses.length; i++)
         {
             ResourceClassResource resClass = resClasses[i];
             if (impliedResClasses.contains(resClass))
             {
                 // remove from references
                 diff.remove(category, resClass);
                 removedResClasses.add(resClass);
             }
             else
             {
                 // add for child categories
                 impliedResClasses.add(resClass);
             }
         }
 
         if (recursive)
         {
             // loop through children and descend
             Resource[] children = coralSession.getStore().getResource(category);
             for (int i = 0; i < children.length; i++)
             {
                 optimiseRCA(coralSession, refs, diff, impliedResClasses, (CategoryResource)children[i], removedResClasses, true);
             }
         }
     }
 
     /**
      *  Optimises category set (assigned to a resource), leaves only subcategories removing
      *  categories which are implied.
      */
     public Set optimiseCategorizationSet(Set categories)
     {
         HashSet removedCategories = new HashSet();
 
         // get implied categories for all categories excluding themselfs
         HashSet impliedCategories = new HashSet();
         for (Iterator i = categories.iterator(); i.hasNext();)
         {
             CategoryResource category = (CategoryResource)i.next();
             CategoryResource[] implied = getImpliedCategories(category, false);
             impliedCategories.addAll(Arrays.asList(implied));
         }
 
         // remove categories which have subcategories
         HashSet categs = new HashSet(categories);
         for (Iterator i = categs.iterator(); i.hasNext();)
         {
             CategoryResource category = (CategoryResource)i.next();
             if (impliedCategories.contains(category))
             {
                 // remove
                 categories.remove(category);
                 removedCategories.add(category);
             }
         }
 
         // return removed categories
         return removedCategories;
     }
 
     /**
      *  Gathers wrong resource assignments for a category taking into account supported resource
      *  classes. Returns resources which do not fit supported resource classes.
      */
     public Set fixCategoryAssignments(CoralSession coralSession, CategoryResource category) throws CategoryException
     {
         HashSet removedResources = new HashSet();
 
         // get resources directly assigned to category
         Resource[] assignedResources = getResources(coralSession, category, false);
 
         // get category's resource classes (including implied)
         HashSet categoryResClasses = new HashSet();
         categoryResClasses.addAll(Arrays.asList(getResourceClasses(coralSession, category, true)));
 
         HashMap resourceClassCache = new HashMap();
 
         // remove resources which do not fit category's resource classes
         for (int i = 0; i < assignedResources.length; i++)
         {
             Resource resource = assignedResources[i];
 
             ResourceClass resClass1 = resource.getResourceClass();
             if (!resourceClassCache.containsKey(resClass1))
             {
                 resourceClassCache.put(resClass1, integrationService.getResourceClass(coralSession, resClass1));
             }
             ResourceClassResource resClass = (ResourceClassResource) (resourceClassCache.get(resClass1));
 
             if (!categoryResClasses.contains(resClass))
             {
                 removedResources.add(resource);
             }
         }
 
         // return removed resources
         return removedResources;
     }
 
 	// resource deletion listener //////////////////////////////////////////////////////////////////
 	
     public void resourceDeleted(Resource resource)
     {
         CoralSession coralSession = sessionFactory.getRootSession();
         try
         {
             if (resource instanceof ResourceClassResource)
             {
                 unsetCategoryResourceClass(coralSession, (ResourceClassResource)resource);
             }
             if (!(resource instanceof CategoryResource))
             {
                 removeFromAllCategories(coralSession, resource);
             }
         }
         catch (CategoryException e)
         {
             throw new RuntimeException(e);
         }
         finally
         {
             coralSession.close();
         }
     }
 
     
     /**
      * Return the resource-resource relation.
      * 
      * @param coralSession the coral session.
      * @return the relation.
      */
     public Relation getResourcesRelation(CoralSession coralSession)
     {     
         if(resourcesRelation != null)
         {
             return resourcesRelation;
         }
         try
         {
             resourcesRelation = coralSession.getRelationManager().
                                    getRelation(RESOURCES_RELATION_NAME);
         }
         catch(AmbigousEntityNameException e)
         {
             throw new IllegalStateException("ambiguous roles relation");
         }
         catch(EntityDoesNotExistException e)
         {
             //ignore it.
         }
         if(resourcesRelation != null)
         {
             return resourcesRelation;
         }
         try
         {
             createSecurityRelation(coralSession, RESOURCES_RELATION_NAME);
         }
         catch(EntityExistsException e)
         {
             throw new IllegalStateException("the security relation already exists");
         }
         return resourcesRelation;
     }
 
     /**
      * 
      * 
      * @param coralSession the coral session.
      * @return the rc relation.
      */
     public Relation getResourceClassRelation(CoralSession coralSession)
     {     
         if(resourceClassRelation != null)
         {
             return resourceClassRelation;
         }
         try
         {
             resourceClassRelation = coralSession.getRelationManager().
                                    getRelation(RESOURCE_TYPE_RELATION_NAME);
         }
         catch(AmbigousEntityNameException e)
         {
             throw new IllegalStateException("ambiguous roles relation");
         }
         catch(EntityDoesNotExistException e)
         {
             //ignore it.
         }
         if(resourceClassRelation != null)
         {
             return resourceClassRelation;
         }
         try
         {
             createSecurityRelation(coralSession, RESOURCE_TYPE_RELATION_NAME);
         }
         catch(EntityExistsException e)
         {
             throw new IllegalStateException("the security relation already exists");
         }
         return resourceClassRelation;
     }
     
     /**
      * Create the security relation.
      * 
      * @param coralSession the coralSession. 
      */
     private synchronized void createSecurityRelation(CoralSession coralSession, String name)
         throws EntityExistsException
     {
         if(name.equals(RESOURCES_RELATION_NAME))
         {
             if(resourcesRelation == null)
             {
                 resourcesRelation = coralSession.getRelationManager().
                     createRelation(RESOURCES_RELATION_NAME);
             }
         }
         if(name.equals(RESOURCE_TYPE_RELATION_NAME))
         {
             if(resourceClassRelation == null)
             {
                 resourceClassRelation = coralSession.getRelationManager().
                     createRelation(RESOURCE_TYPE_RELATION_NAME);
             }
         }
     }    
 }
