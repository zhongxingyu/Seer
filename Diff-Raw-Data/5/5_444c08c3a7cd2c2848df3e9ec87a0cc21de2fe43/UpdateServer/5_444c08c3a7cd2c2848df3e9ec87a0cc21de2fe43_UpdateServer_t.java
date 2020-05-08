 package factory.server.managers.kitAssemblyManager;
 
 import factory.global.data.*;
 
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.*;
 import factory.server.managers.GuiManager;
 import java.io.*;
 import factory.server.managers.laneManager.*;
 
 public class UpdateServer implements GuiManager
 {
 	ArrayList<FactoryObject> CurrentObjects = new ArrayList<FactoryObject>();
 	TreeMap<Integer, Boolean> ChangeMap = new TreeMap<Integer, Boolean>();
 	TreeMap<Integer, FactoryObject> ChangeData = new TreeMap<Integer, FactoryObject>();
     InspectionCamera cam;
 	LaneManager lm;
 	Conveyor conv;
 	KitRobot robot;
 	PartRobot probot;
 	ArrayList<FactoryObject> LineObjects = new ArrayList<FactoryObject>();
     ArrayList<FactoryObject> lastObjects = null;
 	ArrayList<KitStand> stands;
 	ArrayList<Kit> kits;
 	ArrayList<Part> parts;
     ArrayList<Nest> nests;
     ArrayList<Integer> nestsIndex;
     FactoryObject flash = new FactoryObject(100, 430, 14);
 	int count = 0;
 	int countconv = 0;
 	int partscount = 0;
 	int k;
 	int a[] = new int[4];
 	int b[] = new int[4];
 	boolean isBringKit = false;
 	boolean isMoveToStand = false;
 	boolean isMovePartstoStand = false;
 	boolean isMoveToInspection = false;
 	boolean isTakePic = false;
 	boolean isTakeToConveyor = false;
 	boolean isTakeKit = false;
     boolean isFinished = true;
     boolean isFlashed = false;
     boolean flag = false;
     boolean isBadKit = false;
     ArrayList<Boolean> isFull;
 	@SuppressWarnings("unchecked")
 	public UpdateServer()
 	{
 		cam = new InspectionCamera(100,500,13, this);
 		conv = new Conveyor(0,20,-1, this);
 		robot = new KitRobot(23,280,-1, this);
 		probot = new PartRobot(253,280,-1, this);
 		stands = new ArrayList<KitStand>();
 		kits = new ArrayList<Kit>();
 		parts = new ArrayList<Part>();
         nests = new ArrayList<Nest>();
         isFull = new ArrayList<Boolean>();
         for (int i = 0; i < 8; i++)
             isFull.add(false);
         nestsIndex = new ArrayList<Integer>();
         for (int i = 0; i < 8; i++)
 			for (int j = 0; j < 9; j++)
 				parts.add(new Part(0, 0, -1));
 		for (int i = 0; i < 3; i++)
 		{
 			KitStand ks = new KitStand(100,130 + i * 150,-1);
 			stands.add(ks);
 		}
         for (int i = 0; i < 4; i++){
 			Nest n1 = new Nest(355,72+i*124,-1);
             n1.setIndex(i * 2);
 			Nest n2 = new Nest(355,72+i*124+35,-1);
             n2.setIndex(i * 2 + 1);
 			nests.add(n1);
 			nests.add(n2);
 		}
         
         flash.setImage(14);
 		LineObjects.add(new FactoryObject((int)robot.getX1(),(int)robot.getY1(),(int)robot.getX2(),(int)robot.getY2()));
 		LineObjects.add(new FactoryObject((int)probot.getX1(),(int)probot.getY1(),(int)probot.getX2(),(int)probot.getY2()));
 		setCurrentObjects();
 	}
 	public void setFlash(boolean f)
     {
         isFlashed = f;
     }
     public void bindManager(GuiManager bindManager)
     {
         lm = (LaneManager) bindManager;
         for (int i = 0; i < 8; i++)
 		{
 			for (int j = 0; j < lm.getNest(i).size(); j++)
 			{
 				factory.global.data.Part p = lm.getNest(i).get(j);
 				parts.add(new Part(p.x, p.y, p.imageIndex));
 			}
 			for (int j = lm.getNest(i).size(); j < 9; j++)
 				parts.add(new Part(0, 0, -1));
 		}
     }
 	public ArrayList<FactoryObject> getCurrentObjects()
 	{
 		return CurrentObjects;
 	}
 	public void sync(TreeMap<Integer, FactoryObject> changeData)																			// frame sync
 	{
         for (int i = 0; i < CurrentObjects.size(); i++)
         {
 			FactoryObject t = null;
             if (CurrentObjects.get(i).isLine)
                 t = new FactoryObject(CurrentObjects.get(i).x, CurrentObjects.get(i).y, CurrentObjects.get(i).xf, CurrentObjects.get(i).yf);
             else
                 t = new FactoryObject(CurrentObjects.get(i).x, CurrentObjects.get(i).y, CurrentObjects.get(i).imageIndex);
             changeData.put(i, t);
 		}
 	}
 	
 	public void setCurrentObjects()
 	{
 		//add all the stuff to the arraylist
         CurrentObjects.clear();
         try
         {
             for (int i = 0; i < 8; i++)
             {
                 if (!isFull.get(i))
                 {
                     for (int j = 0; j < lm.getNest(i).size(); j++)
                     {
                         {
                             factory.global.data.Part p = lm.getNest(i).get(j);
                             parts.set(j + 9 * i, new Part(355 + p.x, p.y, p.imageIndex));
                         }
                     }
                     for (int j = lm.getNest(i).size(); j < 9; j++)
                             parts.set(j + 9 * i, new Part(0, 0, -1));
                 }
                 if (lm.getNest(i).size() == 9)
                 {
                     factory.global.data.Part p = lm.getNest(i).get(8);
                     //p.print();
                     //36, 98
                     if (p.x == 36 && p.y == 98 + i * 36)
                         isFull.set(i, true);
                     else
                         isFull.set(i, false);
                 }
                 else
                     isFull.set(i, false);
 		}
         }catch(Exception e)
         {}
         for (int i = 0; i < kits.size(); i++)
 			CurrentObjects.add(kits.get(i));
 		for (int i = 0; i < LineObjects.size(); i++)
 			CurrentObjects.add(LineObjects.get(i));
 		CurrentObjects.add(probot.getGripper());
 		for (int i = 0; i < parts.size(); i++)
 		{
 			//if (parts.get(i).imageIndex != -1)
 			//	parts.get(i).print();
 			CurrentObjects.add(parts.get(i));
 		}
 		CurrentObjects.add(cam);
         if (isFlashed)
         {
             CurrentObjects.add(flash);
         }
 	}
 	
 	public boolean emptyStand()
 	{
 		//return whether stands are all empty
         for (int i = 0; i < 2; i++)
 		{
 			if (stands.get(i).getKit() == null)
 				return true;
 		}
 		return false;
 	}
 	
 	public ArrayList<Kit> getKits()
 	{
 		return kits;
 	}
     
     public ArrayList<KitStand> getStands()
     {
         return stands;
     }
     
     public boolean isFinished()
     {
         return isFinished;
     }
 	
 	public int getCount()
 	{
 		return count;
 	}
 	
 	public TreeMap<Integer, Boolean> getChangeMap()
 	{
 		return ChangeMap;
 	}
 	
 	public TreeMap<Integer, FactoryObject> getChangeData()
 	{
 		return ChangeData;
 	}
 	
 	public void bringKit()
 	{
 		if (isBringKit) //the control signal
 		{	
 			flag = true;
             boolean f = true;
             if (conv.getInKit() == null && count < 26)
             {
 				conv.bringKit();
                 countconv++;
                 isFinished = false;
                 flag = false;
                 f = false;
                 
             }
 			if (!conv.kitArrived() && count < 26)
 			{
 				conv.moveKit();
                 f = false;
                 if (flag)
                     countconv++;
                     isFinished = false;
 
 			}
             if (f)
                 isBringKit = false;
             			//kits.get(0).print();
         }
         else
             isBringKit = true;
 		if (countconv == 26)
 		{
 			countconv = 0;
 			isBringKit = false;
             isFinished = true;
 			//isMoveToStand = true;
 		}
 	}
 	
 	public void moveToStand(int k)
 	{
 		if (isMoveToStand)
 		{
 			flag = true;
             boolean f = true;
             if (!robot.getIsMoving() && emptyStand() && conv.kitArrived())
 			{
 				robot.moveFromConveyorToStand(conv, stands.get(k), conv.getInKit(), 0); //call the robot to do the animation
                 count++;
                 isFinished = false;
                 flag = false;
                 f = false;
                 
 			}
 			if (robot.getIsMoving())
             {
 				robot.move();
                 f = false;
                 if (flag)
                 {
                     count++;
                     isFinished = false;
                 }
             }
             if (f)
                 isMoveToStand = false;
             
 			
 		}
         else
         {
             isMoveToStand = true;
             this.k  = k;
         }
 		if (count == 100)
 		{
 			count = 0;
 			isMoveToStand = false;
             isFinished = true;
 			//isBringKit = true;
 			//isMovePartstoStand = true;
 		}
 	}
 	
 	public void movePartstoStand(int nest, int stand, int[] pos, int[] indexes)
 	{
 		if (isMovePartstoStand)
 		{
 			flag = true;
             boolean f = true;
             if (!probot.isMoving())
 			{
                 if (stands.get(stand).getKit() != null){
 					
                     Part[] p = new Part[4];
                     Nest[] n = new Nest[4];
                     nestsIndex.removeAll(nestsIndex);
                     if (isBadKit)
                         for (int i = pos.length - 1;i >= 0; i--)
                             if (pos[i] != - 1)
                             {
                                 parts.set(8 + 9 * pos[i], new Part(0, 0, -1));
                                 lm.removePart(pos[i]);
                                 pos[i] = -1;
                                 indexes[i] = -1;
                                 break;
                             }
                     for (int j = 0; j < p.length; j++){
                         if (pos[j] != -1 && indexes[j] != -1)
                         {
                            Part p1 = null;
                            for (int i = 8; i >= 0; i--)
                                if (parts.get(i + 9 * pos[j]).imageIndex != -1)
                                    p1 = parts.get(i + 9 * pos[j]);
 							//System.out.println(p1.imageIndex);
 							//Part p1 = new Part(nests.get(j).getPosition()X,
 							//nests.get(j).getPositionY(), 1);
                             //lm.removePart(pos[j]);
 							parts.add(p1);
                             p[j] = parts.get(parts.size() - 1);
                             n[j] = nests.get(pos[j]);
                             nestsIndex.add(pos[j]);
                         }
                         else
                             p[j] = null;
                     }
                     probot.moveFromNest(stands.get(stand),p,n,indexes,0); //call the robot to do the animation
                     isFinished = false;
                     flag = false;
                     f = false;
                     count++;
 				}
 			}
 			if (probot.isMoving())
             {
 				probot.move();
                 f = false;
                 if (flag)
                 {
                     count++;
                     isFinished = false;
                 }
             }
             if (f)
                 isMovePartstoStand = false;
 		}
         else
         {
             isMovePartstoStand = true;
             this.k = stand;
             this.a = pos;
             this.b = indexes;
         }
 		if (count == 141)
 		{
 			count = 0;
             isFinished = true;
 			isMovePartstoStand = false;
 			//if (stands.get(stand).getKit().getIsComplete())
 				//isMoveToInspection = true;
 		}
 	}
 	
 	public void moveToInspection(int stand)
 	{
 		if(isMoveToInspection)
         {
             flag = true;
             boolean f = true;
             if (!robot.getIsMoving() && stands.get(2).getKit() == null && stands.get(stand).getKit() != null)
             {
                 robot.moveFromStandToStand(stands.get(stand),stands.get(2), stands.get(stand).getKit(), 0); //call the robot to do the animation
                 count++;
                 isFinished = false;
                 flag = false;
                 f = false;
             }
             if (robot.getIsMoving())
             {
                 robot.move();
                 if (flag)
                 {
                     count++;
                     isFinished = false;
                     f = false;
                 }
             }
             if (f)
                 isMoveToInspection = false;
         }
         else /*if (stands.get(stand).getKit().getIsComplete())*/
 		{
 			isMoveToInspection = true;
             this.k = stand;
 		}
 		if (count == 100)
 		{
 			count = 0;
 			isMoveToInspection = false;
             isFinished = true;
 			//isTakePic = true;
 			
 		}
 	}
 	
 	public void takePic()
 	{
 		if (isTakePic)
         {
             flag = true;
             boolean f = true;
             if (!robot.getIsMoving() && stands.get(2).getKit() != null && !stands.get(2).getKit().getPicTaken() && !cam.isMoving())
             {
                 cam.takePicture(stands.get(2), 0); //call the robot to do the animation
                 count++;
                 isFinished = false;
                 flag = false;
                 f = false;
             }
                 
             if (cam.isMoving)
             {
                 cam.move();
                 if (flag)
                 {
                     count++;
                     isFinished = false;
                 }
                 f = false;
             }
             if (f)
                 isTakePic = false;
         }
         else
             isTakePic = true;
 		if (count == 57)
 		{
 			count = 0;
 			isTakePic = false;
             isFinished = true;
 			//isTakeToConveyor = true;
 		}
 	}
 	
 	public void takeToConveyor()
 	{
 		if (isTakeToConveyor)
         {
             flag = true;
             boolean f = true;
             if (!robot.getIsMoving() && stands.get(2).getKit() != null && stands.get(2).getKit().getPicTaken())
             {
                 if(!isBadKit)
                     robot.moveFromStandToConveyor(stands.get(2), conv, stands.get(2).getKit(), 0); //call the robot to do the animation
                 else
                     robot.trashKit(stands.get(2), stands.get(2).getKit(), 0);
                 count++;
                 isFinished = false;
                 flag = false;
                 f = false;
             }
             if (robot.getIsMoving())
             {
                 robot.move();
                 f = false;
                 if (flag)
                 {
                     count++;
                     isFinished = false;
                 }
             }
             if (f)
                 isTakeToConveyor = false;
         }
         else
             isTakeToConveyor = true;
 		if (count == 100)
 		{
 			count = 0;
 			isTakeToConveyor = false;
             isFinished = true;
 			//isTakeKit = true;
 		}
 	}
 	public void takeKit()
 	{
 		if (isTakeKit)
         {
             if (conv.getOutKit() != null)
             {
                 conv.takeKit(); //call the robot to do the animation
                 count++;
                 isFinished = false;
             }
             else
                 isTakeKit = false;
         }
         else
             isTakeKit = true;
 		if (count == 26)
 		{
 			count = 0;
 			isTakeKit = false;
             isFinished = true;
             conv.getOutKit().setIsMoving(false);
             conv.setOutKit(null);
 		}
         
 	}
     
     public void removeExtraKits() //remove the unused kits
     {
 		for (int i = 0; i < kits.size(); i++){
 			if (kits.get(i).getPositionX() < 0 && !kits.get(i).getIsMoving()){
 				removeExtraParts();
 				kits.remove(i);
 				i--;
 			}
 		}
 	}
 	
 	public void removeExtraParts()//remove the unused parts
     {
 		for (int i = 0; i < parts.size(); i++){
 			if (parts.get(i).getPositionX() < 0){
 				parts.remove(i);
 				i--;
 			}
 		}
 	}
 	
 	public void update(TreeMap<Integer, Boolean> inputChangeMap, TreeMap<Integer, FactoryObject> inputChangeData)      													// ------------> update complete
 	{
 		//ChangeMap = inputChangeMap;
 		//ChangeData = inputChangeData;
 		
 		move(); //update the coornidates
 		 
         /*System.out.print("1st: ");
         lastObjects.get(0).print();
         System.out.print("2nd: ");
         CurrentObjects.get(0).print();*/
         inputChangeData.clear();
         inputChangeMap.clear();
 		if (lastObjects.size() <= CurrentObjects.size())
 		{
 			for (int i = 0; i < lastObjects.size(); i++)
 			{
 				if (!lastObjects.get(i).isEquals(CurrentObjects.get(i))) //if the objects are the same then update the network stuff
 				{
 					//System.out.print(i + " :");
                     //CurrentObjects.get(i).print();
                     inputChangeMap.put(i, true);
                     FactoryObject t = null;
                     if (CurrentObjects.get(i).isLine)
                         t = new FactoryObject(CurrentObjects.get(i).x, CurrentObjects.get(i).y, CurrentObjects.get(i).xf, CurrentObjects.get(i).yf);
                     else
                         t = new FactoryObject(CurrentObjects.get(i).x, CurrentObjects.get(i).y, CurrentObjects.get(i).imageIndex);
 					inputChangeData.put(i, t);
 				}
 			}
 			for (int i = lastObjects.size(); i < CurrentObjects.size(); i++)
 			{
 				inputChangeMap.put(i, true);
                 FactoryObject t = null;
                 if (CurrentObjects.get(i).isLine)
                     t = new FactoryObject(CurrentObjects.get(i).x, CurrentObjects.get(i).y, CurrentObjects.get(i).xf, CurrentObjects.get(i).yf);
                 else
                     t = new FactoryObject(CurrentObjects.get(i).x, CurrentObjects.get(i).y, CurrentObjects.get(i).imageIndex);
 				inputChangeData.put(i, t);
 			}
 		}
 		else
 		{
 			for (int i = 0; i < CurrentObjects.size(); i++)
 			{
 				if (!lastObjects.get(i).isEquals(CurrentObjects.get(i)))
 				{
 					//System.out.print(i + " :");
                     inputChangeMap.put(i, true);
                     FactoryObject t = null;
                     if (CurrentObjects.get(i).isLine)
                         t = new FactoryObject(CurrentObjects.get(i).x, CurrentObjects.get(i).y, CurrentObjects.get(i).xf, CurrentObjects.get(i).yf);
                     else
                         t = new FactoryObject(CurrentObjects.get(i).x, CurrentObjects.get(i).y, CurrentObjects.get(i).imageIndex);
 					inputChangeData.put(i, t);
 				}
 			}
 			for (int i = CurrentObjects.size(); i < lastObjects.size(); i++) //if the original list is shorter, delete all the extra objects
 			{
 				inputChangeMap.put(i, false);
 			}
 		}
         		//lastObjects = (ArrayList<FactoryObject>) CurrentObjects.clone();
 	}
     
 	
 	public void move()
 	{
 		//call the interfaces to do the animation
         lastObjects = new ArrayList<FactoryObject>();
         for (int i = 0; i < CurrentObjects.size(); i++)
         {
             lastObjects.add((FactoryObject)CurrentObjects.get(i).clone());
         }
         if (isBringKit)
 			bringKit();
 		if (isMoveToStand)
 			moveToStand(k);
 		if (isMovePartstoStand)
 		{
 			//int a[] = {0, 1, 2, 5};
 			//int b[] = {0,4,6,8};
 			movePartstoStand(200, k, a, b);
 		}
 			
 		if (isMoveToInspection)
 		{
 			moveToInspection(k);
 		}
 		if (isTakePic)
 			takePic();
 		if (isTakeToConveyor)
 			takeToConveyor();
 		if (isTakeKit)
 		{
 			
 			takeKit();
 		}
         removeExtraKits();
 		LineObjects.set(0, new FactoryObject((int)robot.getX1(),(int)robot.getY1(),(int)robot.getX2(),(int)robot.getY2()));
 		LineObjects.set(1, new FactoryObject((int)probot.getX1(),(int)probot.getY1(),(int)probot.getX2(),(int)probot.getY2()));
 		setCurrentObjects();
 	}
 
 	public boolean isBadKit()
     {
         return isBadKit;
     }
 
 	public void breakPart(String b, int x)
     {
         if (b.equals("BRKSM"))
             isBadKit = !isBadKit;
     }
     public void setSync(){}
     public boolean getSync()
     {
         return false;
     }
 
 }
