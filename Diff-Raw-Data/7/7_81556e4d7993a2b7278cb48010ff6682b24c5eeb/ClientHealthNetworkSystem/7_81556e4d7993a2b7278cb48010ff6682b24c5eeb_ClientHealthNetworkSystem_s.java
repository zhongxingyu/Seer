 package clientNetworkSystems;
 
 
 import common.Network;
 import common.messages.HealthChangedMessage;
 import components.HealthComp;
 import entityFramework.EntityNetworkIDManager;
 import entityFramework.IEntity;
 import entityFramework.IEntityWorld;
 
 /**
  * 
  * @author Joakim Johansson
  *
  */
 public class ClientHealthNetworkSystem extends ClientNetworkSystem<HealthChangedMessage>{
 
 	public ClientHealthNetworkSystem(IEntityWorld world, EntityNetworkIDManager idManager,
 			Network client) {
 		super(world, HealthChangedMessage.class, idManager, client);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void processMessage(HealthChangedMessage message) {
 		IEntity entity = IdManager.getEntityFromNetworkID(message.entityID);
		if(entity != null) //Have to do a nullcheck since you cannot be sure the messages are sent in order.
			entity.getComponent(HealthComp.class).setHealthPoints(message.health);
 	}
 
 }
