 package no.runsafe.mailbox.repositories;
 
 import no.runsafe.framework.database.IDatabase;
 import no.runsafe.framework.database.Repository;
 import no.runsafe.framework.server.RunsafeServer;
 import no.runsafe.framework.server.inventory.RunsafeInventory;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class MailPackageRepository extends Repository
 {
 	public MailPackageRepository(IDatabase database)
 	{
 		this.database = database;
 	}
 
 	@Override
 	public String getTableName()
 	{
 		return "mail_packages";
 	}
 
 	public RunsafeInventory getMailPackage(int packageID)
 	{
 		Map<String, Object> data = this.database.QueryRow("SELECT contents FROM mail_packages WHERE ID = ?", packageID);
 		RunsafeInventory inventory = RunsafeServer.Instance.createInventory(null, 54, "");
 
 		if (data != null)
 			inventory.unserialize((String) data.get("contents"));
 
 		return inventory;
 	}
 
 	public int newPackage(RunsafeInventory contents)
 	{
 		this.database.Execute("INSERT INTO mail_packages (contents) VALUES(?)", contents.serialize());
 
 		Map<String, Object> data = this.database.QueryRow("SELECT LAST_INSERT_ID() AS ID FROM mail_packages");
 		if (data != null) return (Integer) data.get("ID");
 
 		return 0;
 	}
 
 	@Override
 	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
 	{
 		HashMap<Integer, List<String>> versions = new HashMap<Integer, List<String>>();
 		ArrayList<String> sql = new ArrayList<String>();
 		sql.add(
 				"CREATE TABLE `mail_packages` (" +
					"`ID` int(10) unsigned NOT NULL," +
 					"`contents` longtext," +
 					"PRIMARY KEY (`ID`)" +
 				")"
 		);
 		versions.put(1, sql);
 		return versions;
 	}
 
 	private IDatabase database;
 }
