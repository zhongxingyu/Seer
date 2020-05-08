 package mef.core;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import mef.daos.ICompanyDAO;
 import mef.daos.IComputerDAO;
 import mef.entities.Company;
 import mef.entities.Computer;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.mef.framework.entities.Entity;
 import org.mef.framework.sfx.SfxBaseObj;
 import org.mef.framework.sfx.SfxContext;
 
 public class EntityLoader extends SfxBaseObj
 {
 	private ICompanyDAO companyDal;
 	private IComputerDAO computerDal;
 	
 	public EntityLoader(SfxContext ctx)
 	{
 		super(ctx);
 		companyDal = (ICompanyDAO) Initializer.getDAO(ICompanyDAO.class); 
 		computerDal = (IComputerDAO) Initializer.getDAO(IComputerDAO.class); 
 	}
 	
     public void loadCompany(String json) throws Exception
     {
     	ObjectMapper mapper = new ObjectMapper();
     	Company[] arCompany = mapper.readValue(json, Company[].class);
     	for(int i = 0; i < arCompany.length; i++)
     	{
     		Company entity = arCompany[i];
     		Company existing = companyDal.find_by_name(entity.name); //use seedWith field
     		if (existing != null)
     		{
     			entity.id = existing.id;
     		}
     		companyDal.save(entity); //inserts or updates 
     	}
     }
     
     
 
 	private void doCompany(Company entity) 
 	{
 		Company ph = companyDal.find_by_name(entity.name); //use seedWith field
 		
 		if (ph != null)
 		{
 			ph.name = entity.name; //!!copy all over
 			companyDal.update(ph);
 		}
 		else
 		{
 			companyDal.save(entity);
 		}
 		
 	}
 
 	public void loadAll(String json) throws Exception
 	{
     	DaoJsonLoader loader = new DaoJsonLoader();
     	ObjectMapper mapper = new ObjectMapper();
     	JsonNode rootNode = mapper.readTree(json);
 
     	//entities can have logical ids in the json, so that one entity can refer to another
     	//When entities stored in DB they will get a real id
     	//Keep a map to track logical-id to real-id.
     	//Logical id formatted as classname+id, eg "Company.20"
     	HashMap<String, Long> map = new HashMap<String, Long>();
     	
     	List<Company> phoneL = loader.loadCompanys(rootNode);
     	saveCompanies(phoneL, map);
     	
     	log("map:");
     	for(String key : map.keySet())
     	{
     		Long val = map.get(key);
     		log(String.format("%s -> %d", key, val));
     	}
     	
     	List<Computer> computerL = loader.loadComputers(rootNode);
     	saveComputers(computerL, map);
  	}
 
 
 	private void saveCompanies(List<Company> phoneL, HashMap<String, Long> map) 
 	{
     	for(Company ph : phoneL)
     	{
    		String key = makeKey(ph, ph.id);
    		
     		Company existing = companyDal.find_by_name(ph.name); //use seedWith field
     		if (existing != null)
     		{
    			existing.name = ph.name; //copy to existing 
    			companyDal.save(existing); //updates 
    			
    			map.put(key, existing.id);
     		}
     		else
     		{
     			ph.id = 0L;
    			companyDal.save(ph); //inserts
    			map.put(key, ph.id);
     		}
     	}
 	}
 	
 	private void saveComputers(List<Computer> computerL, HashMap<String, Long> map) 
 	{
     	for(Computer computer : computerL)
     	{
     		if (computer.company != null)
     		{
 				String phKey = makeKey(computer.company, computer.company.id);
 				Long companyId = map.get(phKey);
				if (companyId != null && companyId.longValue() != 0L)
 				{
 		    		Company existing = companyDal.findById(companyId);
 					computer.company = existing;
 				}
				else
				{
					log(String.format("ERR: can't find company id %d", computer.company.id));
				}
     		
     		}    		
     		
     		Computer existing = computerDal.find_by_name(computer.name); //use seedWith field
     		if (existing != null)
     		{
    			//copy all but id
    			existing.company = computer.company;
    			existing.discontinued = computer.discontinued;
    			existing.introduced = computer.introduced;
    			existing.name = computer.name;
    			
    			computerDal.save(existing); //updates 
     		}
     		else
     		{
     			String s = makeKey(computer, computer.id);
     			computer.id = 0L;
     			computerDal.save(computer); //inserts or updates 
     			map.put(s, computer.id);
     		}
     	}
     	
     	
     	
 	}
 	private String makeKey(Entity entity, Long id)
 	{
 		String s = entity.getClass().getSimpleName();
 		s = String.format("%s.%d", s, id);
 		return s;
 	}
 
 }
