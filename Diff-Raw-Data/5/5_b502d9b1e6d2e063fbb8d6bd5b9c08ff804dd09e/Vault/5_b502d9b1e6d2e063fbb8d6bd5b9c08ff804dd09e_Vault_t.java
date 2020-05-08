 package model.common;
 
 import java.io.Serializable;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 
 import model.common.operator.Operator;
 import model.common.operator.OperatorFactory;
 import model.item.Item;
 import common.Result;
 import common.util.QueryParser;
 
 
 public abstract class Vault extends Observable implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	protected SortedMap<Integer, IModel> dataVault = new TreeMap<Integer, IModel>();
 	
 	/**
 	 * Constructor.
 	 * 
 	 * 
 	 */
 	public Vault(){
 		return;
 	}
 	
 	/**
 	 * Returns the size of the vault.
 	 */
 	public int size(){
 		ArrayList<IModel> models = this.findAll("Deleted = %o", false);
 		return models.size();
     }
 
     /**
      * Returns the size of the vault, disregarding the _removed tag. Removed items
      * will be included in the count unless item.obliterate() is used.
      */
     public int trueSize(){
         return dataVault.size();
     }
 
     public void clear(){
         dataVault.clear();
     }
 	
 	
 	/**
 	 * adds the item to the map and notifies all observers
 	 * 
 	 * @param newItem
 	 */
 	protected Result addModel(IModel newItem){
 		int id = newItem.getId();
 		this.dataVault.put(id, newItem);
         this.setChanged();
 		this.notifyObservers();
 		return null;
 	}
 	
 	/**
 	 * Configures this vault to use the data from another vault instance.
 	 * @param v
 	 */
 	public void useDataFromOtherVault(Vault v){
 		this.dataVault = v.dataVault;
 	}
 	
 	
 	/**
 	 * Returns a list of Models which match the criteria
 	 *
 	 * 
 	 */
 	public abstract IModel find(String query, Object... params);
 	public IModel findPrivateCall(String query, Object... params)  {
 		QueryParser MyQuery = new QueryParser(query);
         boolean deletedResults = false;
         if(MyQuery.getValue().equals("%o") && params != null){
             MyQuery.setValue(params[0]);
             if(params.length > 1)
                 deletedResults = (Boolean) params[1];
        } else if(params.length > 0){
             deletedResults = (Boolean) params[0];
         }
 
 		
 		//Do a linear Search first
 		//TODO: Add ability to search by index
 		try {
             ArrayList<Item> results = (ArrayList)linearSearch(MyQuery,1,deletedResults);
             if(results.size() == 0)
                 return null;
             return results.get(0);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		
 		return null;
 	}
 	public abstract  ArrayList findAll(String query, Object... params);
 	protected ArrayList<IModel> findAllPrivateCall(String query, Object... params){
 		QueryParser MyQuery = new QueryParser(query);
         boolean deletedResults = false;
         if(MyQuery.getValue().equals("%o") && params != null){
             MyQuery.setValue(params[0]);
             if(params.length > 1)
                 deletedResults = (Boolean) params[1];
        } else if(params.length > 0){
             deletedResults = (Boolean) params[0];
         }
 		//Do a linear Search first
 		//TODO: Add ability to search by index
 		try {
 			ArrayList<IModel> results = linearSearch(MyQuery,0,deletedResults);
 			return results;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	protected abstract IModel getNewObject();
 	protected abstract IModel getCopiedObject(IModel model);
     protected ArrayList<IModel> linearSearch(QueryParser MyQuery, int count)
             throws IllegalAccessException, IllegalArgumentException,
             InvocationTargetException, NoSuchMethodException, SecurityException{
         return this.linearSearch(MyQuery,count,false);
     }
 	protected ArrayList<IModel> linearSearch(QueryParser MyQuery, int count, boolean deletedResults)
             throws IllegalAccessException, IllegalArgumentException,
             InvocationTargetException, NoSuchMethodException, SecurityException{
 		ArrayList<IModel> results = new ArrayList<IModel>();
 		String attrName = MyQuery.getAttrName();
 		Object value = MyQuery.getValue();
 		String op = MyQuery.getOperator();
 		//PushDown
 		IModel myModel = getNewObject();
 		//Class associated with the item model
 		Class<? extends IModel> cls =  myModel.getClass();
 		
 		//Method we will call to get the value
 		Method method;
 		method = cls.getMethod("get"+attrName);
 
 		
 		//Loop through entire hashmap and check values one at a time
 		for (Entry<Integer, IModel> entry : dataVault.entrySet()) {
 			myModel = entry.getValue();
 			Object myItemValue;
 			myItemValue = method.invoke(myModel);
             if(myItemValue == null)
                 continue;
             Operator operator = OperatorFactory.getOperator(op, myItemValue.getClass());
 
             boolean compare = operator.execute(myItemValue,value);
             if(deletedResults && compare)
                 results.add(getCopiedObject(myModel));
             else if(compare && !myModel.isDeleted()){
 		    	//PushDown
 		    	results.add(getCopiedObject(myModel));
 		    }
 		    if(count != 0 && results.size() == count )
 		    	return results;
 		}
 		return results;
 	}
 	
 	/**
 	 * Adds the Model to the map if it already exists.  Should check before doing so.
 	 * 
 	 * @param model Item to add
 	 * @return Result of request
 	 */
 	public  Result saveModified(IModel model){
         if(!model.isValid())
             return new Result(false, "Model must be valid prior to saving,");
         model.setSaved(true);
         this.addModel(getCopiedObject(model));
         return new Result(true);
 	}
 	
 	public abstract IModel get(int id);
 	protected  IModel getPrivateCall(int id){
         IModel m = dataVault.get(id);
     	if(m == null)
     		return null;
 
         return this.getCopiedObject(m);
     }
 	
 	/**
 	 * Checks if the model already exists in the map
 	 * - Retrieve current model by index
 	 * - If barcode is the same do nothing, if it's changed check
 	 * 
 	 * @param model
 	 * @return Result of the check
 	 */
 	public  Result validateModified(IModel model){
 		assert(model!=null);
         assert(!dataVault.isEmpty());
 		
 		//Delete current model
         IModel currentModel = this.get(model.getId());
 		currentModel.delete();
 		//Validate passed in model
 		Result result = this.validateNew(model);
 		//Add current model back
 		currentModel.unDelete();
 		if(result.getStatus() == true)
 			model.setValid(true);
         return result;
 	}
 	
 	protected abstract Result validateNew(IModel model);
 
 	public void obliterate(IModel model) {
 		if (this.dataVault.containsKey(model.getId()))
 			this.dataVault.remove(model.getId());
         this.setChanged();
 		this.notifyObservers();
 	}
 }
