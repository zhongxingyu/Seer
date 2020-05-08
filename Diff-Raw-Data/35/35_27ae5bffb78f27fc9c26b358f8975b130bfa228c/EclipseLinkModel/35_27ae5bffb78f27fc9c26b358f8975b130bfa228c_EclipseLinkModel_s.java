 package nl.topicus.onderwijs.models;
 
 import nl.topicus.cobra.entities.IdObject;
 import nl.topicus.onderwijs.providers.IdObjectProvider;
 
 import org.apache.wicket.model.IDetachable;
 import org.apache.wicket.model.IModel;
 
 public class EclipseLinkModel<T extends IdObject> implements IDetachable, IModel<T>
 {
 	private static final long serialVersionUID = 1L;
 
 	private Long id;
 
 	private T entiteit;
 
 	private Class<T> clazz;
 
 	public EclipseLinkModel(T entiteit)
 	{
 		setProperties(entiteit);
 	}
 
 	@Override
 	public void detach()
 	{
 		setProperties(entiteit);
 		entiteit = null;
 	}
 
	private void setProperties(T entiteit)
	{
		if (entiteit == null)
		{
			return;
		}
		this.entiteit = entiteit;
		this.id = (Long) entiteit.getIdAsSerializable();
		this.clazz = (Class<T>) entiteit.getClass();
	}

 	@Override
 	public T getObject()
 	{
 		if (entiteit != null)
 		{
 			return entiteit;
 		}
 		IdObjectProvider<T> provider = new IdObjectProvider<T>(clazz);
 		entiteit = provider.get(id);
 		return entiteit;
 	}
 
 	@Override
 	public void setObject(T entiteit)
 	{
 		setProperties(entiteit);
 	}
 }
