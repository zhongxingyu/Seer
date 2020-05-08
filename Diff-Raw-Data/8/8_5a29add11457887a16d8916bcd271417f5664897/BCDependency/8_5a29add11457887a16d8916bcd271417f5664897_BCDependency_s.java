 package org.htfv.maven.plugins.buildconfigurator.model;
 
 public class BCDependency
 {
     private String artifactId;
     private String classifier;
     private BCExclusion[] exclusions;
     private String groupId;
     private Boolean optional;
     private String scope;
     private String skip;
     private String systemPath;
     private String type;
     private String version;
 
     public String getArtifactId()
     {
         return artifactId;
     }
 
     public String getClassifier()
     {
         return classifier;
     }
 
     public BCExclusion[] getExclusions()
     {
        return exclusions;
     }
 
     public String getGroupId()
     {
         return groupId;
     }
 
     public Boolean getOptional()
     {
         return optional;
     }
 
     public String getScope()
     {
         return scope;
     }
 
     public String getSkip()
     {
         return skip;
     }
 
     public String getSystemPath()
     {
         return systemPath;
     }
 
     public String getType()
     {
         return type;
     }
 
     public String getVersion()
     {
         return version;
     }
 
     public void setArtifactId(final String artifactId)
     {
         this.artifactId = artifactId;
     }
 
     public void setClassifier(final String classifier)
     {
         this.classifier = classifier;
     }
 
     public void setExclusions(final BCExclusion[] exclusions)
     {
        this.exclusions = exclusions;
     }
 
     public void setGroupId(final String groupId)
     {
         this.groupId = groupId;
     }
 
     public void setOptional(final Boolean optional)
     {
         this.optional = optional;
     }
 
     public void setScope(final String scope)
     {
         this.scope = scope;
     }
 
     public void setSkip(final String skip)
     {
         this.skip = skip;
     }
 
     public void setSystemPath(final String systemPath)
     {
         this.systemPath = systemPath;
     }
 
     public void setType(final String type)
     {
         this.type = type;
     }
 
     public void setVersion(final String version)
     {
         this.version = version;
     }
 }
