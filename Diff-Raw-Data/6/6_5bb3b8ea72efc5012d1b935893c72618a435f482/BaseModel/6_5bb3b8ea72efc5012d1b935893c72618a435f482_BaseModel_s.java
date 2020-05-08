 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package badm;
 
 
 import cc.test.bridge.BridgeConstants;
 import cc.test.bridge.BridgeInterface;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.workplicity.entry.Entry;
 import org.workplicity.task.NetTask;
 import org.workplicity.util.MongoHelper;
 import org.workplicity.worklet.WorkletContext;
 
 /**
  * BaseModel
  *	
  * Gloabl model methods
  * 
  * @author Colby Rabideau
  */
 abstract class BaseModel extends Entry implements BridgeInterface{
 	
     
         String name;
         @Override
         public void setName(String name){
             this.name = name;
             dirty();
         }
         @Override
         public String getName(){
             return name;
         }
 	/**
 	 * Repository Name
 	 *
 	 * name of the model's Mongo collection
 	 */
 	
 	@JsonIgnore
 	protected String repositoryName = "";
 	
 	/**
 	 * Get Repository Name
 	 *	
 	 * returns repo name based on the class, if repositoryName is not set
 	 * 
 	 * @return String
 	 */
 	public String getRepositoryName() {
 		
 		String name;
 		
 		if(repositoryName == ""){
 			String full =this.getClass().getName();
 			int mid = full.lastIndexOf ('.') + 1;
 			name = full.substring(mid) + "s";
 		} else {
 			name = repositoryName;
 		}
 
 		return name;
 	}
         
         public BaseModel(){
             BridgeHelper.getHamper().put(this,BridgeConstants.State.CREATE);
         }
 	
 	/**
 	 * Get ID
 	 * 
 	 * returns the model ID
 	 * 
 	 * @return Integer
 	 */
 	@Override
 	public Integer getId() {
 		return this.id;
 	}
 	
 	/**
 	 * Commit
 	 * 
 	 * saves the model to the database
 	 * 
 	 * @return Boolean 
 	 */
         @Override
 	public Boolean commit() {
             Integer newId = -1;
             Map hamper =  BridgeHelper.getHamper();
             Iterator it = hamper.keySet().iterator();
             while(it.hasNext()){
                 BaseModel bm = (BaseModel) it.next();
                 if(hamper.get(bm) == BridgeConstants.State.CREATE){
                     try {
                         newId = MongoHelper.insert(bm,BaseModel.getStoreName(), bm.getRepositoryName());
                         
                     } catch (Exception ex) {
                         System.out.println("New " + bm.getClass() + bm.getId() + " failed to be commited "+ex);
                         continue;
                     }
                 }
                 else if(hamper.get(bm) == BridgeConstants.State.UPDATE){
                     try {
                         newId = MongoHelper.update(bm,BaseModel.getStoreName(), bm.getRepositoryName());
                     } catch (Exception ex) {
                         System.out.println("Updated " + bm.getClass() + bm.getId() +  " failed to be commited "+ex);
                         continue;
                     }
              
                 }
                BridgeHelper.doTheLaundry();
             }
            
             return (newId > -1) ? true : false;
 	}
         
         public Boolean delete(){
             Integer something;
 		try {
 			something = MongoHelper.delete(this,BaseModel.getStoreName(), getRepositoryName());
 		} catch(Exception e) {
 			System.out.println(this.getClass().getName()+" with id:" + id + " has not been deleted because of error" + e);
 			return false;
 		}
 		return (something > -1) ? true : false; 
         }
 	
 	/**
 	 * context
 	 * 
 	 * Convenience method for getting the current context
 	 * 
 	 * @return WorkletContext
 	 */
 	public static WorkletContext context(){
 		return WorkletContext.getInstance();
 	}
 	
 	/**
 	 * find
 	 * 
 	 * fetches model by id and returns the object
 	 * 
 	 * @param id
 	 * @return BaseModel
 	 */
 	public static BaseModel find(Integer id) {
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
         
         public static ArrayList all(){
             throw new UnsupportedOperationException("Not supported yet.");
         }
         
 	/**
 	 * Update
 	 * 
 	 * @param audit 
 	 */
         public void update(Audit audit){
             audit.getUpdated().add(0, id);
             dirty();
         }
         
         @JsonIgnore
         public static String getStoreName() {
             return NetTask.getStoreName();
         }
         
         
         public void dirty(){
             if(!BridgeHelper.getHamper().containsKey(this)){
             BridgeHelper.getHamper().put(this,BridgeConstants.State.UPDATE);
             }
         }
 	
 }
 // if(BridgeHelper.getHamper().get(bm) == BridgeConstants.State.CREATE){
 //                        something = MongoHelper.insert(bm,BaseModel.getStoreName(), bm.getRepositoryName());
 //                        BridgeHelper.getHamper().remove(bm);
 //                        setId(something);
 //                        System.out.println("Commiting "+bm.getId()+ " to repo, new");   
 //                    }
 //                    else if(BridgeHelper.getHamper().get(bm) == BridgeConstants.State.UPDATE){
 //                        something = MongoHelper.update(bm,BaseModel.getStoreName(), bm.getRepositoryName());
 //                        BridgeHelper.getHamper().remove(bm); 
 //                    setId(something);
 //                        System.out.println("Commiting "+bm.getId()+ " to repo, update");   
 //                    }
