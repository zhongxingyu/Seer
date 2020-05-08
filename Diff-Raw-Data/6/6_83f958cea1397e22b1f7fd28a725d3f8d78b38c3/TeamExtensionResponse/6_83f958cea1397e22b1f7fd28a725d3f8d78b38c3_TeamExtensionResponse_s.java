 package com.cloudbees.openid4java.team;
 
 import org.openid4java.message.ParameterList;
 
 import java.util.Arrays;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * Response from the server indicating the actual group membership.
  *
  * @author Kohsuke Kawaguchi
  */
 public class TeamExtensionResponse extends TeamExtensionMessage {
     public TeamExtensionResponse(ParameterList parameterList) {
         super(parameterList);
     }
 
     public TeamExtensionResponse() {
     }
 
     /**
      * Returns all the group names that the user actually belongs to (among the list provided
      * in the {@link TeamExtensionRequest}.
      */
     public Set<String> getTeamMembership() {
        return new TreeSet<String>(Arrays.asList(params.getParameterValue("is_member").split(",")));
     }
 }
