 package net.dynamic_tools.model;
 
 import net.dynamic_tools.exception.CircularDependencyException;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Peter
  * Date: 4/11/11
  * Time: 4:32 PM
  * To change this template use File | Settings | File Templates.
  */
 public abstract class NamedResource<K extends NamedResource> implements Comparable<K> {
     private String name;
 	protected final Set<K> dependencies = new HashSet<K>();
 
     public NamedResource(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
 	public boolean isDependentOn(K resource) {
         for (NamedResource dependency : dependencies) {
             if (dependency.equals(resource) || dependency.isDependentOn(resource)) {
                 return true;
             }
         }
         return false;
     }
 
 	public void addDependency(K dependency) throws CircularDependencyException {
         if (dependency.isDependentOn(this) || dependency.equals(this)) {
            throw new CircularDependencyException("Circular dependencies are not supported. Failed trying to add " + dependency.name + " as a dependency on " + name);
         }
         dependencies.add(dependency);
     }
 
 	public Set<K> getDependencies() {
         return new HashSet<K>(dependencies);
     }
 
     public void removeDependency(K dependency) {
         dependencies.remove(dependency);
     }
 
     @Override
     public boolean equals(Object obj) {
         return obj.getClass().equals(this.getClass()) && name.equals(((NamedResource) obj).name);
     }
 
     @Override
     public int hashCode() {
         return name.hashCode();
     }
 
     @Override
     public int compareTo(K namedResource) {
         if (namedResource.isDependentOn(this)) {
             return 1;
         }
         if (this.isDependentOn(namedResource)) {
             return -1;
         }
        return this.name.compareTo(namedResource.name);
     }
 }
