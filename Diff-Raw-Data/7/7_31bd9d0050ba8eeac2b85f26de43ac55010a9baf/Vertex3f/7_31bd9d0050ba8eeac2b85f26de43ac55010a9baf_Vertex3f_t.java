 package org.jagatoo.datatypes;
 
 import org.openmali.vecmath.Color3f;
 import org.openmali.vecmath.Point3f;
 import org.openmali.vecmath.TexCoord2f;
 import org.openmali.vecmath.TexCoord3f;
 import org.openmali.vecmath.TexCoord4f;
 import org.openmali.vecmath.Tuple3f;
 import org.openmali.vecmath.Vector3f;
 
 /**
  * This class represents vertices.
  * A Vertex is composed of coordinates, normals, colors and texture-coordinates.
  * 
  * @author Marvin Froehlich (aka Qudus)
  */
 public class Vertex3f
 {
     public static final int COORDINATES            = 1;
     public static final int NORMALS                = 2;
     public static final int COLORS_3               = 4;
     public static final int COLORS_4               = 8;
    public static final int TEXTURE_COORDINATES_1  = 16;
    public static final int TEXTURE_COORDINATES_2  = 32;
    public static final int TEXTURE_COORDINATES_3  = 64;
    public static final int TEXTURE_COORDINATES_4  = 128;
     
     private int features;
     
     private Point3f coord = null;
     private Vector3f normal = null;
     private Color3f color3 = null;
     private TexCoord2f texCoord2 = null;
     private TexCoord3f texCoord3 = null;
     private TexCoord4f texCoord4 = null;
     
     
     public void setFeatures( int features )
     {
         this.features = features;
     }
     
     public int getFeatures()
     {
         return( features );
     }
     
     public void addFeature(int feature)
     {
         this.features |= feature;
     }
     
     public boolean hasFeature( int feature )
     {
         return( ( this.features & feature ) > 0 );
     }
     
     
     public void setCoord( Tuple3f coord )
     {
         if ( this.coord == null )
             this.coord = new Point3f( coord );
         else
             this.coord.set( coord );
         
         addFeature( COORDINATES );
     }
     
     public void getCoord( Tuple3f coord )
     {
         coord.set( this.coord );
     }
     
     public Tuple3f getCoord()
     {
         return( this.coord );
     }
     
     
     public void setNormal( Vector3f normal )
     {
         if ( this.normal == null )
             this.normal = new Vector3f( normal );
         else
             this.normal.set( normal );
         
         addFeature( NORMALS );
     }
     
     public void getNormal( Vector3f normal )
     {
         normal.set( this.normal );
     }
     
     public Vector3f getNormal()
     {
         return( this.normal );
     }
     
     
     public void setColor3( Color3f color )
     {
         if ( this.color3 == null )
             this.color3 = new Color3f( color );
         else
             this.color3.set( color );
         
         addFeature( COLORS_3 );
     }
     
     public void getColor3( Color3f color )
     {
         color.set( this.color3 );
     }
     
     public Color3f getColor3()
     {
         return( this.color3 );
     }
     
     
     public void setTexCoord2( TexCoord2f texCoord )
     {
         if ( this.texCoord2 == null )
             this.texCoord2 = new TexCoord2f( texCoord );
         else
             this.texCoord2.set( texCoord );
         
         addFeature( TEXTURE_COORDINATES_2 );
     }
     
     public void getTexCoord2( TexCoord2f texCoord )
     {
         texCoord.set( this.texCoord2 );
     }
     
     public TexCoord2f getTexCoord2()
     {
         return( this.texCoord2 );
     }
     
     
     public void setTexCoord3( TexCoord3f texCoord )
     {
         if ( this.texCoord3 == null )
             this.texCoord3 = new TexCoord3f( texCoord );
         else
             this.texCoord3.set( texCoord );
         
         addFeature( TEXTURE_COORDINATES_3 );
     }
     
     public void getTexCoord3( TexCoord3f texCoord )
     {
         texCoord.set( this.texCoord3 );
     }
     
     public TexCoord3f getTexCoord3()
     {
         return( this.texCoord3 );
     }
     
     
     public void setTexCoord4( TexCoord4f texCoord )
     {
         if ( this.texCoord4 == null )
             this.texCoord4 = new TexCoord4f( texCoord );
         else
             this.texCoord4.set( texCoord );
         
         addFeature( TEXTURE_COORDINATES_4 );
     }
     
     public void getTexCoord4( TexCoord4f texCoord )
     {
         texCoord.set( this.texCoord4 );
     }
     
     public TexCoord4f getTexCoord4()
     {
         return( this.texCoord4 );
     }
     
     
     public void set( Tuple3f coord, Vector3f normal )
     {
         if ( coord != null )
         {
             addFeature( COORDINATES );
             setCoord( coord );
         }
         
         if ( normal != null )
         {
             addFeature( NORMALS );
             setNormal( normal );
         }
     }
     
     public void get( Tuple3f coord, Vector3f normal )
     {
         if ( ( coord != null ) && ( hasFeature( COORDINATES ) ) )
             getCoord( coord );
         if ( ( normal != null ) && ( hasFeature( NORMALS ) ) )
             getNormal( normal );
     }
     
     
     public void set( Tuple3f coord, Vector3f normal, Color3f color )
     {
         set( coord, normal );
         
         if ( color != null )
         {
             addFeature( COLORS_3 );
             setColor3( color );
         }
     }
     
     public void get( Tuple3f coord, Vector3f normal, Color3f color )
     {
         get( coord, normal );
         
         if ( ( color != null ) && ( hasFeature( COLORS_3 ) ) )
             getColor3( color );
     }
     
     
     public void set( Tuple3f coord, Vector3f normal, Color3f color, TexCoord2f texCoord )
     {
         set( coord, normal, color );
         
         if ( texCoord != null )
         {
             addFeature( TEXTURE_COORDINATES_2 );
             setTexCoord2( texCoord );
         }
     }
     
     public void get( Tuple3f coord, Vector3f normal, Color3f color, TexCoord2f texCoord )
     {
         get( coord, normal, color );
         
         if ( ( texCoord != null ) && ( hasFeature( TEXTURE_COORDINATES_2 ) ) )
             getTexCoord2( texCoord );
     }
     
     
     public void set( Tuple3f coord, Vector3f normal, Color3f color, TexCoord3f texCoord )
     {
         set( coord, normal, color );
         
         if ( texCoord != null )
         {
             addFeature( TEXTURE_COORDINATES_2 );
             setTexCoord3( texCoord );
         }
     }
     
     public void get( Tuple3f coord, Vector3f normal, Color3f color, TexCoord3f texCoord )
     {
         get( coord, normal, color );
         
         if ( ( texCoord != null ) && ( hasFeature( TEXTURE_COORDINATES_2 ) ) )
             getTexCoord3( texCoord );
     }
     
     
     public void set( Tuple3f coord, Vector3f normal, Color3f color, TexCoord4f texCoord )
     {
         set( coord, normal, color );
         
         if ( texCoord != null )
         {
             addFeature( TEXTURE_COORDINATES_2 );
             setTexCoord4( texCoord );
         }
     }
     
     public void get( Tuple3f coord, Vector3f normal, Color3f color, TexCoord4f texCoord )
     {
         get( coord, normal, color );
         
         if ( ( texCoord != null ) && ( hasFeature( TEXTURE_COORDINATES_2 ) ) )
             getTexCoord4( texCoord );
     }
     
     
     public Vertex3f( int features )
     {
         this.features = features | COORDINATES;
     }
     
     public Vertex3f()
     {
         this( COORDINATES | NORMALS | COLORS_3 | TEXTURE_COORDINATES_2 );
     }
 }
