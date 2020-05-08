 package org.zend.php.zendserver.deployment.core.internal.descriptor;
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.zend.php.zendserver.deployment.core.descriptor.IMapping;
 import org.zend.php.zendserver.deployment.core.descriptor.IResourceMapping;
 
 
 public class ResourceMapping implements IResourceMapping {
 
	private Map<IPath, IMapping[]> mappingRules;
	private List<IPath> exclusions;
 
 	public Map<IPath, IMapping[]> getMappingRules() {
 		return mappingRules;
 	}
 
 	public List<IPath> getExclusions() {
 		return exclusions;
 	}
 
 	void setMappingRules(Map<IPath, IMapping[]> mappingRules) {
 		this.mappingRules = mappingRules;
 	}
 
 	void setExclusions(List<IPath> exclusions) {
 		this.exclusions = exclusions;
 	}
 
 }
