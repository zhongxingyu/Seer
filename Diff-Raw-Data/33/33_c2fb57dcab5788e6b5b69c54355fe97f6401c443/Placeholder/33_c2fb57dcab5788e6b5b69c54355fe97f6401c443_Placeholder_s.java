 package entities;
 
 import world.World;
 
 public class Placeholder extends Agent {
 
 	public Placeholder(int x, int y, int z)
 	{
 		pos[0] = x;
 		pos[1] = y;
 		pos[2] = z;
 	}
 	
 	@Override
 	public void decideNextAction(World world) 
 	{
 		this.setCurrentAction(idle);
 	}
 
 	@Override
 	public void loadTextures()
 	{
 		//load no texture, this is a placeholder to simply occupy a space while another agent is making
 		//a special case move to that space
 	}
 
 	@Override
 	public void renderAgent(int pixelSize, int terrainTextureSize)
 	{
 		//render nothing, this is a placeholder to simply occupy a space while another agent is making
 		//a special case move to that space
 	}
 	
 	@Override
 	public boolean equals(Object obj)
 	{
 		if (obj == null)
 			return false;
 		
 		if (obj.getClass() != this.getClass())
 			return false;
 		
 		int[] pos2 = ((Placeholder)(obj)).getPos();
 		return (pos2[0] == pos[0] && pos2[1] == pos[1] && pos2[2] == pos[2]);
 	}
 }
