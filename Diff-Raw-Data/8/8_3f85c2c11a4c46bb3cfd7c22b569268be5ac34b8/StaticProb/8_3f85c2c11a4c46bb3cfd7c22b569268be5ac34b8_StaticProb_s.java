 package entity.probs;
 
 import java.io.File;
 
 import entity.Entity;
 
 public class StaticProb extends Entity
 {
 	@Override
 	public void render()
 	{
 		this.model = ((File) customValues.get("displayModel")).getPath();
 		super.render();
 	}

 }
