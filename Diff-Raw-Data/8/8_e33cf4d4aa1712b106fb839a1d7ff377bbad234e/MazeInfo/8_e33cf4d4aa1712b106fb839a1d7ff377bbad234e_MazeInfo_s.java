 package maze.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import maze.util.Listener;
 
 /**
  * Stores information about a Maze including its file name and model.
  * @author John Smith
  */
 public class MazeInfo implements Listener<MazeCell>
 {
    private MazeModel mModel;
    private String mPath;
    private String mName;
    private boolean isDirty = false;
    private boolean isMutable = true;
    private boolean isExtended = false;
 
    public static MazeInfo load(InputStream in, String name)
    {
       if (in == null)
          return null;
 
       MazeInfo returned = null;
       try
       {
          MazeInfo mi = new MazeInfo();
          MazeModel maze = new MazeModel();
          maze.loadMaze(in, false);
          mi.mPath = name;
          mi.isMutable = false;
          mi.mName = name;
          mi.mModel = maze;
          mi.mModel.addDelayedListener(mi);
          returned = mi;
       }
       catch (Exception e)
       {}
       return returned;
    }
 
    public static MazeInfo load(File file)
    {
       if (file == null || !file.isFile())
          return null;
       //DataInputStream dis;
       MazeInfo returned = null;
       try
       {
          MazeInfo mi = new MazeInfo();
          MazeModel maze = new MazeModel();
          mi.mPath = file.getCanonicalPath();
          String name = maze.loadMaze(mi.mPath);
          if (name != null)
          {
             mi.mName = name;
             mi.isExtended = true;
          }
          else
             mi.mName = file.getName();
          mi.mModel = maze;
 
          mi.mModel.addDelayedListener(mi);
          returned = mi;
       }
       catch (IOException ex){}
 
       return returned;
    }
 
    public static MazeInfo createEmptyMaze(String name, boolean extended)
    {
       MazeInfo mi = new MazeInfo();
       mi.mName = name;
       mi.mPath = "";
       mi.mModel = new MazeModel();
       mi.isDirty = true;
       mi.mModel.addDelayedListener(mi);
       mi.isExtended = extended;
       return mi;
    }
 
    public boolean store()
    {
       try
       {
          if (isExtended)
             mModel.saveMaze(mPath, mName);
          else
             mModel.saveMaze(mPath, null);
          isDirty = false;
       }
       catch (IOException e)
       {
          System.out.println("Saving Maze Error");
          return false;
       }
 
       return true;
    }
 
    public String getName()
    {
       return mName;
    }
 
    public String getPath()
    {
       return mPath;
    }
 
    public MazeModel getModel()
    {
       return mModel;
    }
 
    public boolean isDirty()
    {
       return isDirty;
    }
 
    public void clearDirty()
    {
       if (!isMutable)
          isDirty = false;
    }
 
    public boolean isMutable()
    {
       return isMutable;
    }
 
    public boolean isExtended()
    {
       return isExtended;
    }
 
    public void setName(String name)
    {
       mName = name;
       isExtended = true;
    }
 
    public void setPath(String path)
    {
       if (!mPath.equalsIgnoreCase(path))
       {
          mPath = path;
          isDirty = true;
       }
    }
 
    /**
     * Private constructor. Instances must be retrieved from factory methods.
     */
    private MazeInfo()
    {}
 
    @Override
    public String toString()
    {
       return this.mName;
    }
 
    public MazeInfo getMutableClone()
    {
       MazeInfo theClone = new MazeInfo();
       theClone.isDirty = isDirty;
       theClone.isExtended = isExtended;
       theClone.isMutable = true;
       theClone.mModel = (MazeModel) mModel.clone();
       theClone.mName = mName;
       theClone.mPath = mPath;
       return theClone;
    }
 
    @Override
    public void eventFired(MazeCell event)
    {
       this.isDirty = true;
    }
 }
