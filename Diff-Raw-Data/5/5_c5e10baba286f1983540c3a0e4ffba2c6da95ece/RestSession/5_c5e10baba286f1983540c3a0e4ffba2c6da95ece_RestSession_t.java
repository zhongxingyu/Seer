 /**
  *** This software is licensed under the GNU General Public License, version 3.
  *** See http://www.gnu.org/licenses/gpl.html for full details of the license terms.
  *** Copyright 2012 Andrew Heald.
  */
 
 package uk.org.sappho.applications.restful.transcript.jersey;
 
 import uk.org.sappho.applications.services.transcript.registry.ConfigurationException;
 
 public class RestSession {
 
     public interface Action<T> {
 
         void execute() throws ConfigurationException;
 
         T getResponse();
     }
 
    synchronized public void execute(Action action) throws ConfigurationException {
 
        action.execute();
     }
 }
