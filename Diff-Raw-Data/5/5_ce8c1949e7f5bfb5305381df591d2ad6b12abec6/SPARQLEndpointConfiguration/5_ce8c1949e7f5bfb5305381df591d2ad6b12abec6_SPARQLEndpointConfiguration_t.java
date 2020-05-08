 package org.sonatype.nexus.plugin.rdf.internal.capabilities;
 
 import java.util.Map;
 
 public class SPARQLEndpointConfiguration
 {
 
     private final String repositoryId;
 
     SPARQLEndpointConfiguration( final Map<String, String> properties )
     {
         repositoryId = repository( properties );
     }
 
     public String repositoryId()
     {
         return repositoryId;
     }
 
     @Override
     public int hashCode()
     {
         final int prime = 31;
         int result = 1;
         result = prime * result + ( ( repositoryId == null ) ? 0 : repositoryId.hashCode() );
         return result;
     }
 
     @Override
     public boolean equals( Object obj )
     {
         if ( this == obj )
             return true;
         if ( obj == null )
             return false;
         if ( getClass() != obj.getClass() )
             return false;
         SPARQLEndpointConfiguration other = (SPARQLEndpointConfiguration) obj;
         if ( repositoryId == null )
         {
             if ( other.repositoryId != null )
                 return false;
         }
         else if ( !repositoryId.equals( other.repositoryId ) )
             return false;
         return true;
     }

     @Override
     public String toString()
     {
         return "SPARQLEndpointConfiguration [repositoryId=" + repositoryId + "]";
     }
 
     private static String repository( final Map<String, String> properties )
     {
        String repositoryId = properties.get( SPARQLEndpointCapabilityDescriptor.REPO_OR_GROUP_ID );
         repositoryId = repositoryId.replaceFirst( "repo_", "" );
         repositoryId = repositoryId.replaceFirst( "group_", "" );
         return repositoryId;
     }
 
 }
