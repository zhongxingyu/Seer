 import java.awt.image.BufferedImage;
import java.util.Random;
 
 /**
  * The Cluster class provides the next structural tier after the Galaxy class for galaxy generation and manipulation.
  * @author Bryant
  */
 public class Cluster extends Location
 {
 	private static final int MULTI = 200;
 	private static final int HALF_LENGTH = 0;
 	private static final int MIN_ORBIT = 100;
 	
 	private Sector[] Sectors;	// Holds all sector information for this cluster.
 	private long seed;			// Seed value provided by Galaxy.
 	private short Name;			// Number corresponding to entry in Ops.ClusterNames.
 	private short Name2;		// Number corresponding to entry in Ops.ClusterNames2.
 	private int x;				// X coordinate data.
 	private int y;				// Y coordinate data.
 	private Singularity anchor; // Singularity forms the center of every Cluster; its coordinates are always 0,0
 	
 
 	/**
 	 * Constructor.
 	 * Creates the "lite" version of a cluster with no sector data.
 	 * @param s = seed value provided by Galaxy.
 	 */
 	public Cluster(long s)
 	{
 		super();
 		seed = s;
 		Name = (short) rand.nextInt(Ops.getCluster1NameCount());
 		Name2 = (short) rand.nextInt(Ops.getCluster2NameCount());
 		Picture = Pics.getClusterPic(rand.nextInt(10));
 	}
 	
 	/**
 	 * Constructor.
 	 * @param s = seed value provided by Galaxy.
 	 * @param name = number corresponding to entry in Ops.ClusterNames.
 	 * @param name2 = number corresponding to entry in Ops.ClusterNames2.
 	 * @param pic = assigned BufferedImage.
 	 */
 	public Cluster(long s, short name, short name2, BufferedImage pic, Location maker)
 	{
 		super();
 		seed = s;
 		Name = name;
 		Name2 = name2;
 		Picture = pic;
 		parent = maker;
 		anchor = new Singularity();
 		Generate();
 	}
 	
 	/**
 	 * Handles generation of all sectors within this cluster.
 	 */
 	public void Generate()
 	{
		rand = new Random(seed);
 		Sectors = new Sector[1 + rand.nextInt(MAX_SECTORS)];
 		double spacing = 360.0 / Sectors.length;
 		for (int z = 0; z < Sectors.length; z++)
 		{
 			Sectors[z] = new Sector(z,this);
 			Sectors[z].SetX((int)(((MIN_ORBIT + rand.nextInt(MULTI)) * Math.cos(Math.toRadians(spacing*z))))-HALF_LENGTH);
 			Sectors[z].SetY((int)(((MIN_ORBIT + rand.nextInt(MULTI)) * Math.sin(Math.toRadians(spacing*z))))-HALF_LENGTH);
 		}
 	}
 	
 	// Returns cluster name in string format
 	@Override
 	public String toString()
 	{
 		return Ops.getCluster(Name, Name2);
 	}
 	
 	/**
 	 * Returns a specific sector in this cluster.
 	 * @param index = index of a particular sector within this cluster's sector array.
 	 * @return Sector
 	 */
 	public Sector getChild(int index)
 	{
 		return getChild()[index];
 	}
 	
 	/**
 	 * Returns all of the sectors in this cluster.
 	 * @return Sector[]
 	 */
 	public Sector[] getChild()
 	{
 		return Sectors;
 	}
 	
 	/**
 	 * Returns seed value of this cluster.
 	 * @return long
 	 */
 	public long GetSeed()
 	{
 		return seed;
 	}
 	
 	/**
 	 * Returns number that is used to obtain the first part of this cluster's name
 	 * @return short
 	 */
 	public short GetName()
 	{
 		return Name;
 	}
 	
 	/**
 	 * Returns number that is used to obtain the second part of this cluster's name
 	 * @return short
 	 */
 	public short GetName2()
 	{
 		return Name2;
 	}
 	
 	/**
 	 * Gets this cluster's X coordinate within its galaxy.
 	 * @return int
 	 */
 	public int GetX()
 	{
 		return x;
 	}
 	
 	/**
 	 * Gets this cluster's Y coordinate within its galaxy.
 	 * @return int
 	 */
 	public int GetY()
 	{
 		return y;
 	}
 	
 	/**
 	 * Sets this cluster's X coordinate within its galaxy.
 	 * @param X
 	 */
 	public void SetX(int X)
 	{
 		x = X;
 	}
 	
 	/**
 	 * Sets this cluster's Y coordinate within its galaxy.
 	 * @param Y
 	 */
 	public void SetY(int Y)
 	{
 		y = Y;
 	}
 	
 	/**
 	 * Returns the black hole that holds this cluster together.
 	 * @return Singularity
 	 */
 	public Singularity getCenter()
 	{
 		return anchor;
 	}
 	
 	@Override
 	public BufferedImage GetCenterImage()
 	  {
 		  return Pics.getBlackHole();
 	  }
 	
 	public int whatAmI()
 	{
 	  return 1;
 	}
 }
