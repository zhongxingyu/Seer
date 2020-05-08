 package coulombCraft.Signs;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.scheduler.BukkitScheduler;
 
 public class SignUpdateTask implements Runnable
 {
 	BukkitScheduler scheduler;
 	int taskId;
 	Block block;
 	Runnable updateTask;
 	
 	public SignUpdateTask(BukkitScheduler scheduler, Block block, Runnable updateTask)
 	{
 		this.scheduler = scheduler;
 		this.updateTask = updateTask;
 		this.block = block;
 	}
 	
 	public void AssociateTaskId(int id)
 	{
 		taskId = id;
 	}
 	
 	@Override
 	public void run()
 	{
 		Material mat = block.getType();
 		if (mat != Material.SIGN && mat != Material.SIGN_POST && mat != Material.WALL_SIGN)
 			scheduler.cancelTask(taskId);
		
		updateTask.run();
 	}
 	
 }
