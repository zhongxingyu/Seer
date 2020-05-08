 package org.geogit.web.api.commands;
 
 import org.geogit.api.GeoGIT;
 import org.geogit.api.ObjectId;
 import org.geogit.api.Ref;
 import org.geogit.api.SymRef;
 import org.geogit.api.plumbing.RefParse;
 import org.geogit.api.plumbing.RevParse;
 import org.geogit.api.plumbing.UpdateRef;
 import org.geogit.api.plumbing.UpdateSymRef;
 import org.geogit.web.api.CommandContext;
 import org.geogit.web.api.CommandResponse;
 import org.geogit.web.api.CommandSpecException;
 import org.geogit.web.api.ResponseWriter;
 import org.geogit.web.api.WebAPICommand;
 
 import com.google.common.base.Optional;
 
 public class UpdateRefWeb implements WebAPICommand {
 
     private String name;
 
     private String newValue;
 
     private boolean delete;
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setNewValue(String newValue) {
         this.newValue = newValue;
     }
 
     public void setDelete(boolean delete) {
         this.delete = delete;
     }
 
     @Override
     public void run(CommandContext context) {
         if (name == null) {
             throw new CommandSpecException("No name was given.");
         } else if (delete == false && newValue.equals(ObjectId.NULL)) {
             throw new CommandSpecException(
                     "Nothing specified to update with, must specify either deletion or new value to update to.");
         }
 
         final GeoGIT geogit = context.getGeoGIT();
         Optional<Ref> ref;
         final String oldValue;
         final String newValueName;
         try {
             ref = geogit.command(RefParse.class).setName(name).call();
 
             if (!ref.isPresent()) {
                 throw new CommandSpecException("Invalid name: " + name);
             }
 
             if (ref.get() instanceof SymRef) {
                 oldValue = ((SymRef) ref.get()).getTarget();
                 Optional<Ref> target = geogit.command(RefParse.class).setName(newValue).call();
                 if (target.isPresent() && target.get() instanceof Ref) {
                     newValueName = target.get().getName();
                     ref = geogit.command(UpdateSymRef.class).setDelete(delete).setName(name)
                             .setNewValue(target.get().getName()).call();
                 } else {
                     throw new CommandSpecException("Invalid new target: " + newValue);
                 }
 
             } else {
                 oldValue = ref.get().getObjectId().toString();
                Optional<ObjectId> target = geogit.command(RevParse.class).setRefSpec(newValue)
                        .call();
                 if (target.isPresent()) {
                     newValueName = target.get().toString();
                     ref = geogit.command(UpdateRef.class).setDelete(delete)
                             .setName(ref.get().getName()).setNewValue(target.get()).call();
                 } else {
                     throw new CommandSpecException("Invalid new value: " + newValue);
                 }
             }
         } catch (Exception e) {
             context.setResponseContent(CommandResponse.error("Aborting UpdateRef: "
                     + e.getMessage()));
             return;
         }
 
         if (ref.isPresent()) {
             context.setResponseContent(new CommandResponse() {
 
                 @Override
                 public void write(ResponseWriter out) throws Exception {
                     out.start();
                     out.writeUpdateRefResponse(name, newValueName, oldValue);
                     out.finish();
                 }
             });
         }
 
     }
 
 }
