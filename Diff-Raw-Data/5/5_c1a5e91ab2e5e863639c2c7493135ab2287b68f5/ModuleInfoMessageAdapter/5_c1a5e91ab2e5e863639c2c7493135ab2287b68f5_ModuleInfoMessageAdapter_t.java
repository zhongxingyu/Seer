 package edu.teco.dnd.module.messages.infoReq;
 
 import java.lang.reflect.Type;
 import java.util.UUID;
 
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParseException;
 
 import edu.teco.dnd.module.Module;
 import edu.teco.dnd.module.config.BlockTypeHolder;
 
 public class ModuleInfoMessageAdapter implements JsonDeserializer<ModuleInfoMessage> {
 	@Override
 	public ModuleInfoMessage deserialize(final JsonElement json, final Type typeOfT,
 			final JsonDeserializationContext context) throws JsonParseException {
 		if (!json.isJsonObject()) {
 			throw new JsonParseException("is not an object");
 		}
 		final JsonObject jsonObj = (JsonObject) json;
 		final JsonElement jsonUUID = jsonObj.get("uuid");
 		UUID uuid = context.deserialize(jsonUUID, UUID.class);
 		final JsonElement jsonSourceUUID = jsonObj.get("sourceuuid");
 		UUID sourceUUID = context.deserialize(jsonSourceUUID, UUID.class);
 		final JsonElement jsonModule = jsonObj.get("module");
 		Module module = context.deserialize(jsonModule, Module.class);
		final BlockTypeHolder rootHolder = module.getHolder();
		if (rootHolder != null) {
			setParents(rootHolder);
		}
 		return new ModuleInfoMessage(sourceUUID, uuid, module);
 	}
 
 	private static void setParents(final BlockTypeHolder blockTypeHolder) {
 		if (!blockTypeHolder.isLeave()) {
 			for (final BlockTypeHolder child : blockTypeHolder.getChildren()) {
 				child.setParent(blockTypeHolder);
 				setParents(child);
 			}
 		}
 	}
 }
