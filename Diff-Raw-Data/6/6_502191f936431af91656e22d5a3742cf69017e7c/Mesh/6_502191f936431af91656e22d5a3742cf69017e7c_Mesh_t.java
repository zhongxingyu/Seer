 package chum.gl;
 
 import chum.fp.*;
 import chum.gl.RenderContext;
 import chum.util.Log;
 
 import chum.gl.VertexAttributes.Usage;
 
 import java.nio.Buffer;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.nio.ShortBuffer;
 
 import javax.microedition.khronos.opengles.GL10;
 import javax.microedition.khronos.opengles.GL11;
 
 
 /**
    A Mesh holds vertices composed of attributes specified by a 
    {@link VertexAttributes} instance. The vertices are held in either
    a FloatBuffer or IntBuffer
    
    Adapted from Mesh class from libgdx, originally written by mzechner
    
    FIXME managed VBOs will leak two vbo handles per instance!
 */
 public class Mesh {
 
     /** the vertex attributes */
     public VertexAttributes attributes;
 
     /** the maximum number of vertices */
     public int maxVertices;
     
     /** the maximum number of indices */
     public int maxIndices;
     
     /** the direct byte buffer that holds the vertices */
     public Buffer vertices;
     
     /** a view of the vertices buffer for manipulating floats */
     public FloatBuffer verticesFloat;
     
     /** a view of the vertices buffer for manipulating fixed point values */
     public IntBuffer verticesFixed;
     
     /** the direct short buffer that holds the indices */
     public ShortBuffer indices;
     
     /** the VBO handle */
     public int vertexBufferObjectHandle;
     
     /** the IBO handle */
     public int indexBufferObjectHandle;
     
     /** dirty flag */
     public boolean dirty = false;
     
     /** the rendering context object */
     private RenderContext renderContext;
     
     /** managed? */
     public final boolean managed;
     
     /** static? */
     public final boolean isStatic;
     
     /** fixed point? */
     public final boolean useFixedPoint;
 
     /** The primitive type used to render this mesh */
     public int type;
 
 
     /**
        Creates a new Mesh with the given attributes
        
        @param graphics the graphics instance
        @param managed whether this mesh should be managed or not.
        @param useFixedPoint whether to use fixed point or floats
        @param maxVertices the maximum number of vertices this mesh can hold
        @param maxIndices the maximum number of indices this mesh can hold
        @param attributes the {@link VertexAttribute}s.
     */
     public Mesh( boolean managed, boolean isStatic,
                  boolean useFixedPoint, int maxVertices, int maxIndices,
                  VertexAttribute ... attributes ) {
         this(managed,isStatic,useFixedPoint,
              maxVertices,maxIndices,
              new VertexAttributes( attributes ));
     }
 
 
     /**
        Creates a new Mesh with the given attributes
        
        @param graphics the graphics instance
        @param managed whether this mesh should be managed or not.
        @param useFixedPoint whether to use fixed point or floats
        @param maxVertices the maximum number of vertices this mesh can hold
        @param maxIndices the maximum number of indices this mesh can hold
        @param attributes the {@link VertexAttributes}.
     */
     public Mesh( boolean managed, boolean isStatic,
                  boolean useFixedPoint, int maxVertices, int maxIndices,
                  VertexAttributes attributes ) {
         this.managed = managed;
         this.isStatic = isStatic;
         this.useFixedPoint = useFixedPoint;
         this.maxVertices = maxVertices;
         this.maxIndices = maxIndices;
         this.attributes = attributes;
         
         createCPUBuffers();
     }
     
 
     /** Create buffers to hold the Mesh data on the CPU side */
     private void createCPUBuffers() {
         ByteBuffer buffer = ByteBuffer.allocateDirect( maxVertices *
                                                        this.attributes.vertexSize );
         buffer.order(ByteOrder.nativeOrder());
         vertices = buffer;
         verticesFixed = buffer.asIntBuffer();
         verticesFloat = buffer.asFloatBuffer();
 
         buffer = ByteBuffer.allocateDirect( maxIndices * 2 );
         buffer.order( ByteOrder.nativeOrder() );
         indices = buffer.asShortBuffer();
     }
 
 
     /**
        Prepare for rendering.  This will create VBO's, etc as appropriate, before the
        first time the mesh is rendered.
     */
     public void onSurfaceCreated(RenderContext renderContext) {
         this.renderContext = renderContext;
         checkManagedAndDirty();
     }
 
 
     public void checkManagedAndDirty() {
         if( managed ) {
             if( renderContext.isGL11 &&
                 (vertexBufferObjectHandle == 0 ||
                  renderContext.gl11.glIsBuffer( vertexBufferObjectHandle ) == false) ) {
                 createGPUBuffers();
                 fillGPUBuffers();
             }
 //             if( renderContext.isGL20 &&
 //                 (vertexBufferObjectHandle == 0 ||
 //                  renderContext.gl20.glIsBuffer( vertexBufferObjectHandle ) == false) ) {
 //                 createGPUBuffers();
 //                 fillGPUBuffers();
 //             }
         }
         
         if( dirty )
             fillGPUBuffers();
     }
     
 
     private void createGPUBuffers() {
         if ( renderContext.isGL20 )
             ;//createGPUBuffers(renderContext.gl20);
         else if ( renderContext.isGL11 )
             createGPUBuffers(renderContext.gl11);
     }
 
 
     /** Allocate a VBO and IBO for the mesh */
     private void createGPUBuffers(GL11 gl) {
         if ( !renderContext.canUseVBO )
             return;
 
         int[] handle = new int[1];
         gl.glGenBuffers( 1, handle, 0 );
         vertexBufferObjectHandle = handle[0];
         
         if( maxIndices > 0 ) {
             gl.glGenBuffers( 1, handle, 0 );
             indexBufferObjectHandle = handle[0];
         }
     }
     
 
 //     /** Allocate a VBO and IBO for the mesh
 //         todo: why does it need a tmp buffer to allocate? */
 //     private void createGPUBuffers( GL20 gl ) {
 //         ByteBuffer tmp = ByteBuffer.allocateDirect( 4 );
 //         tmp.order( ByteOrder.nativeOrder() );
 //         IntBuffer handle = tmp.asIntBuffer();
         
 //         gl.glGenBuffers( 1, handle );
 //         vertexBufferObjectHandle = handle.get(0);
         
 //         if( maxIndices > 0 ) {
 //             gl.glGenBuffers( 1, handle );
 //             indexBufferObjectHandle = handle.get(0);
 //         }
 //     }
     
 
     /** Fill the VBO / IBO for the mesh */
     private void fillGPUBuffers() {
         if( renderContext.isGL20 )
             ;//fillGPUBuffers(renderContext.gl20);
         else if ( renderContext.isGL11 )
             fillGPUBuffers(renderContext.gl11);
 
         dirty = false;
     }
     
 
     /** Send the VBO / IBO data to the GPU */
     private void fillGPUBuffers( GL11 gl ) {
         gl.glBindBuffer( GL11.GL_ARRAY_BUFFER, vertexBufferObjectHandle );
         gl.glBufferData( GL11.GL_ARRAY_BUFFER, getNumVertices() * attributes.vertexSize,
                          vertices, isStatic ? GL11.GL_STATIC_DRAW : GL11.GL_DYNAMIC_DRAW );
         gl.glBindBuffer( GL11.GL_ARRAY_BUFFER, 0 );
         
         if( maxIndices > 0 ) {
             gl.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, indexBufferObjectHandle );
             gl.glBufferData( GL11.GL_ELEMENT_ARRAY_BUFFER, indices.limit() * 2,
                              indices, isStatic ? GL11.GL_STATIC_DRAW : GL11.GL_DYNAMIC_DRAW );
             gl.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, 0 );
         }
     }
     
 //     private void fillGPUBuffers( GL20 gl ) {
 //         gl.glBindBuffer( GL20.GL_ARRAY_BUFFER, vertexBufferObjectHandle );
 //         gl.glBufferData( GL20.GL_ARRAY_BUFFER, getNumVertices() * attributes.vertexSize,
 //                          vertices, isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW );
 //         gl.glBindBuffer( GL20.GL_ARRAY_BUFFER, 0 );
         
 //         if( maxIndices > 0 ) {
 //             gl.glBindBuffer( GL20.GL_ELEMENT_ARRAY_BUFFER, indexBufferObjectHandle );
 //             gl.glBufferData( GL20.GL_ELEMENT_ARRAY_BUFFER, getNumIndices() * 2,
 //                              indices, isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW );
 //             gl.glBindBuffer( GL20.GL_ELEMENT_ARRAY_BUFFER, 0 );
 //         }
 //     }
     
     /**
        Sets the vertices of this Mesh. The attributes are assumed to be given
        in float format. If this mesh is configured to use fixed point an
        IllegalArgumentException will be thrown.
         
        @param vertices the vertices.
     */
     public void setVertices( float[] vertices ) {
         if( useFixedPoint )
             throw new IllegalArgumentException( "can't set float vertices for fixed point mesh" );
         
         verticesFloat.clear();			
         verticesFloat.put( vertices );			
         verticesFloat.limit(vertices.length);			
         verticesFloat.position(0);		
         
         this.vertices.limit(verticesFloat.limit()*4); // 4 = sizeof(float)
         this.vertices.position(0);
 
         dirty = true;
     }
     
     /**
        Sets the vertices of this Mesh. The attributes are assumed to be given
        in float format. If this mesh is configured to use fixed point an
        IllegalArgumentException will be thrown.
        
        @param vertices the vertices.
        @param offset the offset into the vertices array
        @param count the number of floats to use
     */
     public void setVertices(float[] vertices, int offset, int count) {
         if( useFixedPoint )
             throw new IllegalArgumentException( "can't set float vertices for fixed point mesh" );
         
         verticesFloat.clear();
         verticesFloat.put( vertices, offset, count );
         verticesFloat.limit( count );
         verticesFloat.position(0);
 
         this.vertices.limit(verticesFloat.limit()*4);
         this.vertices.position(0);
 
         dirty = true;
     }
     
     /**
        Sets the vertices of this Mesh. The attributes are assumed to be given
        in fixed point format. If this mesh is configured to use floats an
        IllegalArgumentException will be thrown.
        
        @param vertices the vertices.
      */
     public void setVertices( int[] vertices ) {
         if( !useFixedPoint )
             throw new IllegalArgumentException( "can't set fixed point vertices for float mesh" );
         
         verticesFixed.clear();
         verticesFixed.put( vertices );
         verticesFixed.limit( vertices.length );
         verticesFixed.position(0);
 
         this.vertices.limit(verticesFixed.limit()*4);
         this.vertices.position(0);
 
         dirty = true;
     }
     
     /**
        Sets the vertices of this Mesh. The attributes are assumed to be given
        in fixed point format. If this mesh is configured to use floats an
        IllegalArgumentException will be thrown.
        
        @param vertices the vertices.
        @param offset the offset into the vertices array
        @param count the number of floats to use
      */
     public void setVertices( int[] vertices, int offset, int count ) {
         if( !useFixedPoint )
             throw new IllegalArgumentException( "can't set fixed point vertices for float mesh" );
         
         verticesFixed.clear();
         verticesFixed.put( vertices, offset, count );
         verticesFixed.limit(count);
         verticesFixed.position(0);
 
         this.vertices.limit(verticesFixed.limit()*4);
         this.vertices.position(0);
 
         dirty = true;
     }
     
     /**
        Sets the indices of this Mesh
        
        @param indices the indices
     */
     public void setIndices( short[] indices )
     {	
         this.indices.put( indices );
         this.indices.position(0);
         dirty = true;
     }
     
     /**
        Sets the indices of this Mesh
        
        @param indices the indices
        @param offset the offset into the indices array
        @param count the number of indices to use
     */
     public void setIndices( short[] indices, int offset, int count )
     {	
         this.indices.put( indices, offset, count );
         this.indices.position(0);
         dirty = true;
     }
     
     /**
      * @return the number of defined indices
      */
     public int getNumIndices() {
         return indices.limit();
     }
     
     /**
      * @return the number of defined vertices
      */
     public int getNumVertices() {
         return vertices.limit() / attributes.vertexSize;
     }
     
     /**
      * @return the size of a single vertex in bytes
      */
     public int getVertexSize() {
         return attributes.vertexSize;
     }
     
     /** Frees all resources associated with this Mesh */
     public void dispose() {
         if( renderContext.isGL20 )
             ;//dispose( renderContext.gl20 );
         else if ( renderContext.isGL11)
             dispose( renderContext.gl11 );
     }
     
     private void dispose( GL11 gl ) {
         int handle[] = new int[1];
         handle[0] = vertexBufferObjectHandle;
         gl.glDeleteBuffers( 1, handle, 0 );
         
         if( maxIndices > 0 ) {
             handle[0] = indexBufferObjectHandle;
             gl.glDeleteBuffers( 1, handle, 0 );
         }
     }
     
 //     private void dispose( GL20 gl ) {
 //         ByteBuffer tmp = ByteBuffer.allocateDirect( 4 );
 //         tmp.order( ByteOrder.nativeOrder() );
 //         IntBuffer handle = tmp.asIntBuffer();
 //         handle.put( vertexBufferObjectHandle );
 //         handle.position(0);
 //         gl.glDeleteBuffers( 1, handle );
         
 //         if( maxIndices > 0 ) {
 //             handle.clear();
 //             handle.put( indexBufferObjectHandle );
 //             handle.position(0);
 //             gl.glDeleteBuffers( 1, handle );
 //         }
 //     }
     
     /**
        @return whether 16.16 fixed point is used
     */
     public boolean usesFixedPoint() {
         return useFixedPoint;
     }
     
     /**
        @return the maximum number of vertices this mesh can hold
     */
     public int getMaxVertices() {
         return maxVertices;
     }
     
     /**
        @return the maximum number of indices this mesh can hold
     */
     public int getMaxIndices() {
         return maxIndices;		
     }
     
     /**
        Returns the first {@link VertexAttribute} having the given
        {@link Usage}.
        
        @param usage the Usage.
        @return the VertexAttribute or null if no attribute with that usage was found.
     */
     public VertexAttribute getVertexAttribute(int usage) {
         return attributes.getByUsage(usage);
     }
     
     /**
        @return the vertex attributes of this Mesh
     */
     public VertexAttributes getVertexAttributes() {
         return attributes;
     }
     
     /**
        @return the backing ByteBuffer holding the vertices
     */
     public Buffer getVerticesBuffer() {
         return vertices;
     }
     
     /**
        @return the backing shortbuffer holding the indices
     */
     public ShortBuffer getIndicesBuffer() {
         return indices;
     }
     
     /**
        Returns getNumVertices() vertices in the float array
        @param vertices the destination array
     */
     public void getVertices(float[] vertices) {
         if( useFixedPoint )
             throw new IllegalArgumentException( "can't get float vertices from fixed point mesh" );
 
         verticesFloat.get(vertices);
         verticesFloat.position(0);
     }
     
     /**
        Returns getNumVertices() vertices in the fixed point array
        @param vertices the destination array
     */
     public void getVertices( int[] vertices ) {
         if( !useFixedPoint )
             throw new IllegalArgumentException( "can't get fixed point vertices from float mesh" );
         
         verticesFixed.get(vertices);
         verticesFixed.position(0);
     }
     
 
     /**
      * Returns getNumIndices() indices in the short array
      * @param indices the destination array
      */
     public void getIndices( short[] indices ) {
         this.indices.get(indices);
         this.indices.position(0);
     }
 
 
     /**
        Populate the VertexFixedPoint structure with the vertex info from
        the specified vertex.
 
        Note that extracting vertex info is fairly expensive.
 
        @param vert the vertex number
        @param data the structure to hold the vertex data
     */
     public void getVertex(int vert,VertexFixedPoint data) {
         int per_vert = attributes.vertexSize/4; // 4 = sizeof int or sizeof float
 
         if ( data.positionAttr != null ) {
             int base = (vert * per_vert) + data.positionAttr.offset/4;
             if ( useFixedPoint ) {
                 data.position.x = verticesFixed.get(base++);
                 data.position.y = verticesFixed.get(base++);
                 data.position.z = verticesFixed.get(base++);
             } else {
                 data.position.x = FP.floatToFP(verticesFloat.get(base++));
                 data.position.y = FP.floatToFP(verticesFloat.get(base++));
                 data.position.z = FP.floatToFP(verticesFloat.get(base++));
             }
         }
 
         if ( data.normalAttr != null ) {
             int base = (vert * per_vert) + data.normalAttr.offset/4;
             if ( useFixedPoint ) {
                 data.normal.x = verticesFixed.get(base++);
                 data.normal.y = verticesFixed.get(base++);
                 data.normal.z = verticesFixed.get(base++);
             } else {
                 data.normal.x = FP.floatToFP(verticesFloat.get(base++));
                 data.normal.y = FP.floatToFP(verticesFloat.get(base++));
                 data.normal.z = FP.floatToFP(verticesFloat.get(base++));
             }
         }
 
         if ( data.colorAttr != null ) {
             int base = (vert * per_vert) + data.colorAttr.offset/4;
             if ( useFixedPoint ) {
                 data.color.red = verticesFixed.get(base++);
                 data.color.green = verticesFixed.get(base++);
                 data.color.blue = verticesFixed.get(base++);
                 data.color.alpha = verticesFixed.get(base++);
             } else {
                 data.color.red = FP.floatToFP(verticesFloat.get(base++));
                 data.color.green = FP.floatToFP(verticesFloat.get(base++));
                 data.color.blue = FP.floatToFP(verticesFloat.get(base++));
                 data.color.alpha = FP.floatToFP(verticesFloat.get(base++));
             }
         }
 
         if ( data.textureAttr != null ) {
             for( int t=0; t < data.textureAttr.length; ++t) {
                 int base = (vert * per_vert) + data.textureAttr[t].offset/4;
                 if ( useFixedPoint ) {
                     data.texture[t].u = verticesFixed.get(base++);
                     data.texture[t].v = verticesFixed.get(base++);
                 } else {
                     data.texture[t].u = FP.floatToFP(verticesFloat.get(base++));
                     data.texture[t].v = FP.floatToFP(verticesFloat.get(base++));
                 }
             }
         }
     }
 
 
     /**
        Store the values from  the VertexFixedPoint structure into the specified
        vertex of the mesh.
 
        Note that update vertex info is fairly expensive.
 
        @param vert the vertex number
        @param data the structure to hold the vertex data
     */
     public void putVertex(int vert,VertexFixedPoint data) {
         if ( data.positionAttr != null ) {
             int base = (vert * attributes.vertexSize/4) + data.positionAttr.offset;
             if ( useFixedPoint ) {
                 verticesFixed.put(base,data.position.x);
                 verticesFixed.put(base+1,data.position.y);
                 verticesFixed.put(base+2,data.position.z);
             } else {
                 verticesFloat.put(base,FP.toFloat(data.position.x));
                 verticesFloat.put(base+1,FP.toFloat(data.position.y));
                 verticesFloat.put(base+2,FP.toFloat(data.position.z));
             }
         }
 
         if ( data.normalAttr != null ) {
             int base = (vert * attributes.vertexSize/4) + data.normalAttr.offset;
             if ( useFixedPoint ) {
                 verticesFixed.put(base,data.normal.x);
                 verticesFixed.put(base+1,data.normal.y);
                 verticesFixed.put(base+2,data.normal.z);
             } else {
                 verticesFloat.put(base,FP.toFloat(data.normal.x));
                 verticesFloat.put(base+1,FP.toFloat(data.normal.y));
                 verticesFloat.put(base+2,FP.toFloat(data.normal.z));
             }
         }
 
         if ( data.colorAttr != null ) {
             int base = (vert * attributes.vertexSize/4) + data.colorAttr.offset;
             if ( useFixedPoint ) {
                 verticesFixed.put(base,data.color.red);
                 verticesFixed.put(base+1,data.color.green);
                 verticesFixed.put(base+2,data.color.blue);
                 verticesFixed.put(base+3,data.color.alpha);
             } else {
                 verticesFloat.put(base,FP.toFloat(data.color.red));
                 verticesFloat.put(base+1,FP.toFloat(data.color.green));
                 verticesFloat.put(base+2,FP.toFloat(data.color.blue));
                 verticesFloat.put(base+3,FP.toFloat(data.color.alpha));
             }
         }
 
         if ( data.textureAttr != null ) {
             for( int t=0; t < data.textureAttr.length; ++t) {
                 int base = (vert * attributes.vertexSize/4) + data.textureAttr[t].offset;
                 if ( useFixedPoint ) {
                     verticesFixed.put(base,data.texture[t].u);
                     verticesFixed.put(base+1,data.texture[t].v);
                 } else {
                     verticesFloat.put(base,FP.toFloat(data.texture[t].u));
                     verticesFloat.put(base+1,FP.toFloat(data.texture[t].v));
                 }
             }
         }
     }
 
 
     /**
        Get the Texture (if any) associated with this Mesh.  Used by the MeshNode
        when rendering the mesh.
     */
     public Texture getTexture() { return null; }
 
 
 
     /**
        Helper class for working with individual vertices in the mesh
     */
     public static class VertexFixedPoint {
 
         /** The Position attribute (if any) */
         public VertexAttribute positionAttr;
 
         /** The position */
         public Vec3 position;
 
 
         /** The Normal attribute (if any) */
         public VertexAttribute normalAttr;
 
         /** The normal */
         public Vec3 normal;
 
 
         /** The Color attribute (if any) */
         public VertexAttribute colorAttr;
 
         /** The color */
         public Color color;
 
 
         /** The texture attributes (if any) */
         public VertexAttribute[] textureAttr;
 
         /** The texture coords */
         public Vec2[] texture;
 
 
         /** Create an empty instance */
         public VertexFixedPoint() {
             
         }
 
         public VertexFixedPoint(VertexAttributes attributes) {
             for ( int i=0,n=attributes.size(); i<n; ++i ) {
                 VertexAttribute attr = attributes.get(i);
                 prep(attr);
             }
         }
 
 
         /**
            Set up the structure to extract info for the given atribute.
         */
         public void prep(VertexAttribute attr) {
             if ( attr == null )
                 throw new IllegalArgumentException("attr can't be null");
 
             switch(attr.usage) {
             case Usage.Position:
                 positionAttr = attr;
                 position = new Vec3();
                 break;
             case Usage.Normal:
                 normalAttr = attr;
                 normal = new Vec3();
                 break;
             case Usage.Color:
                 colorAttr = attr;
                 color = new Color();
                 break;
             case Usage.Texture:
                 if ( textureAttr == null ) {
                     textureAttr = new VertexAttribute[1];
                     textureAttr[0] = attr;
                     
                     texture = new Vec2[1];
                     texture[0] = new Vec2();
                 } else {
                     VertexAttribute[] old_attrs = textureAttr;
                     textureAttr = new VertexAttribute[old_attrs.length+1];
                     for(int a=0; a<old_attrs.length; ++a) textureAttr[a] = old_attrs[a];
                     textureAttr[textureAttr.length-1] = attr;
                     
                     Vec2[] old_tex = texture;
                     texture = new Vec2[old_tex.length+1];
                     for(int t=0; t<old_tex.length; ++t) texture[t] = old_tex[t];
                     texture[texture.length-1] = new Vec2();
                 }
                 break;
             }
         }
     }
 
 
 
     /**
        Helper class for computing bounding box and other info for a mesh.
 
        All bounding info is currently only availabled via FP values.
     */
     public static class Bounds {
         
         /** The mesh */
         public Mesh mesh;
 
         /** The size of the mesh in all three dimensions */
         public Vec3 size = new Vec3();
 
         /** The min bounds (min x,y,z) of the mesh */
         public Vec3 minimum = new Vec3();
 
         /** The max bounds (max x,y,z) of the mesh */
         public Vec3 maximum = new Vec3();
 
         /** The center point of the mesh (midpoint between minimum and maximum) */
         public Vec3 center = new Vec3();
 
         /** The 'center of mass' point of the mesh (geometric average of all vertices) */
         public Vec3 centerMass = new Vec3();
 
         /** The radius of the mesh, for bounding-sphere tests */
         public int radius;
 
         
            
         /** Create an empty instance */
         protected Bounds() {
         }
 
 
         private Bounds next_avail;
         private static Bounds first_avail;
         private static Object sync = new Object();
 
 
         /** Obtain an instance from a pool */
         public static Bounds obtain() {
             synchronized(sync) {
                 if ( first_avail == null )
                     first_avail = new Bounds();
                 Bounds b = first_avail;
                 first_avail = b.next_avail;
                 return b;
             }
         }
 
         /** Return an instance to the pool */
         public void recycle() {
             synchronized(sync) {
                 next_avail = first_avail;
                 first_avail = this;
             }
         }
 
 
         public Bounds update(Mesh mesh) {
             this.mesh = mesh;
             update();
             return this;
         }
 
 
         /** Update the bounding info from the mesh's current geometry */
         public void update() {
             if ( mesh == null ) 
                 throw new IllegalStateException("No mesh to update bounds");
             
             // Only need the position info for each vertex
             VertexFixedPoint vert = new VertexFixedPoint();
             vert.prep(mesh.getVertexAttribute(Usage.Position));
 
             // Extract each vertex, update the min/max,
             // and add all together for the centerMass
             centerMass.set(0,0,0);
             minimum.set(FP.MAX,FP.MAX,FP.MAX);
             maximum.set(FP.MIN,FP.MIN,FP.MIN);
             
             int numVerts = mesh.getNumVertices();
             for( int v=0; v < numVerts; ++v ) {
                 mesh.getVertex(v,vert);
                 centerMass.add(vert.position,centerMass);
                 minimum.minimum(vert.position,minimum);
                 maximum.maximum(vert.position,maximum);
             }
 
             // Divide centerMass total by num verts, for geometric average
             centerMass.scale( FP.floatToFP(1f/numVerts), centerMass );
 
             // The size is just the delta between min and max
             maximum.delta(minimum, size);
 
             // The center is the midpoint between min and max
             minimum.add(maximum,center);
             center.scale(FP.ONE >> 1, center);
             
             Log.d("Mesh.Bounds: "+mesh+
                   " min="+minimum+
                   " max="+maximum+
                   " ctr="+center+
                   " size="+size);
         }
 
 
         /**
            Test whether the given point is within the bounding box
            of the mesh -- does not check for inclusion within the actual
            volume of the mesh itself, that's a much harder problem.
         */
         public boolean contains(Vec3 pt) {
             final int x = pt.x;
             if ( x < minimum.x || x > maximum.x ) return false;
             
             final int y = pt.y;
             if ( y < minimum.y || y > maximum.y ) return false;
             
             final int z = pt.z;
             if ( z < minimum.z || z > maximum.z ) return false;
             
             return true;
         }
         
         
         /**
            Test whether the given points is within the bounds of the mesh,
            or whether the line joining the points passes through the mesh.
         */
         public boolean contains(Vec3 pt1, Vec3 pt2) {
             if ( contains(pt1) ) return true;
             if ( contains(pt2) ) return true;
             
             // Doesn't contain either point, but perhaps the line passes through?
             // Need essentially a ray-box intersection test
             
             if ( pt1.x <= pt2.x ) {
                 if ( pt2.x < minimum.x ) return false;
             } else {
                 if ( pt1.x > maximum.x ) return false;
             }
             
             if ( pt1.y <= pt2.y ) {
                 if ( pt2.y < minimum.y ) return false;
             } else {
                 if ( pt1.y > maximum.y ) return false;
             }
             
             if ( pt1.z <= pt2.z ) {
                 if ( pt2.z < minimum.z ) return false;
             } else {
                 if ( pt1.z > maximum.z ) return false;
             }
             
             return true;
         }
         
     }
 
 
     /**
        Helper class for modifying the points in a mesh
     */
     public static class Transform {
 
         /** The matrix to use for the transformation */
         public M4 matrix;
 
 
         /** Construcct a new Transform */
         public Transform() {
             this(new M4());
            this.matrix.setIdentity();
         }
 
         
         /** Construcct a new Transform using the given matrix */
         public Transform(M4 matrix) {
             this.matrix = matrix;
         }
 
 
         /** Modify the matrix to translate the vertices of the mesh */
         public Transform translate(Vec3 delta) {
             matrix.translate(delta);
             return this;
         }
 
 
         /** Modify the matrix to translate the vertices of the mesh around the origin */
         public Transform rotate(Vec3 dir, int theta) {
             matrix.rotate(dir,theta);
             return this;
         }
 
 
         /** Modify the matrix to translate the vertices of the mesh around the origin */
         public Transform rotate(Vec4 q) {
             matrix.rotate(q);
             return this;
         }
 
 
         /** Modify the matrix to scale all the vertices of the mesh uniformly */
         public Transform scale(int scale) {
             matrix.scale(scale);
             return this;
         }
 
 
         /** Modify the matrix to scale the vertices in different do*/
         public Transform scale(int x, int y, int z) {
             matrix.scale(x,y,z);
             return this;
         }
 
 
         /**
            Apply the transform to a specific mesh
         */
         public void apply(Mesh mesh) {
            //Log.d("Mesh.Transform: apply to "+mesh);
            //Log.d("%s", matrix);
 
             // Need the position info for each vertex
             VertexFixedPoint vert = new VertexFixedPoint();
             vert.prep(mesh.getVertexAttribute(Usage.Position));
 
             // Also need to update normals if present
             VertexAttribute normalAttr = mesh.attributes.getByUsage(Usage.Normal);
             if ( normalAttr != null )
                 vert.prep(normalAttr);
             
             for( int v=0, num=mesh.getNumVertices(); v<num; ++v ) {
                 mesh.getVertex(v,vert);
                 matrix.multiply(vert.position,vert.position);
                 if ( vert.normal != null )
                     matrix.multiply(vert.normal,vert.normal);
                 mesh.putVertex(v,vert);
             }
         }
 
 
         /**
            Helper method that translates the mesh so that it is centered
            on the origin
         */
         public void center(Mesh mesh) {
             center(mesh,Vec3.ORIGIN);
         }
 
         /**
            Helper method that translates the mesh so that it is centered
            on the given point
         */
         public void center(Mesh mesh, Vec3 pos) {
             Bounds bounds = Bounds.obtain().update(mesh);
             Vec3 delta = new Vec3();
             pos.delta(bounds.center,delta);
 
             matrix.setIdentity();
             matrix.translate(delta);
 
             apply(mesh);
         }
 
     }
 
 
 
     /**
        Class to dump mesh vertex data out, for debugging
     */
     public static class Dumper {
 
         public static void dump(Mesh mesh) {
             String typeStr = "unknown";
             switch(mesh.type) {
             case GL10.GL_POINTS: typeStr="GL_POINTS"; break;
             case GL10.GL_LINES: typeStr="GL_LINES"; break;
             case GL10.GL_LINE_STRIP: typeStr="GL_LINE_STRIP"; break;
             case GL10.GL_LINE_LOOP: typeStr="GL_LINE_LOOP"; break;
             case GL10.GL_TRIANGLES: typeStr="GL_TRIANGLES"; break;
             case GL10.GL_TRIANGLE_FAN: typeStr="GL_TRIANGLE_FAN"; break;
             case GL10.GL_TRIANGLE_STRIP: typeStr="GL_TRIANGLE_STRIP"; break;
             }
 
             Log.d("Mesh dump %s: %d vertices, %d indices, type=%d(%s)",
                   mesh, mesh.getNumVertices(), mesh.getNumIndices(),
                   mesh.type, typeStr);
 
             VertexFixedPoint vert = new VertexFixedPoint();
             for(int u=0; u<Usage.Generic; ++u) {
                 VertexAttribute attr = mesh.getVertexAttribute(u);
                 if ( attr != null ) vert.prep(attr);
             }
             Log.d("  vertex layout (%d bytes): p=%d n=%d c=%d t=%d",
                   mesh.getVertexSize(),
                   vert.positionAttr == null ? -1 : vert.positionAttr.offset,
                   vert.normalAttr == null ? -1 : vert.normalAttr.offset,
                   vert.colorAttr == null ? -1 : vert.colorAttr.offset,
                   vert.textureAttr == null ? -1 : vert.textureAttr[0].offset);
 
 
             // Extract each vertex, dump it out
             int numVerts = mesh.getNumVertices();
             for( int v=0; v < numVerts; ++v ) {
                 mesh.getVertex(v,vert);
                 Log.d("  vert[%d] p=%s n=%s c=%s t=%s",
                       v, vert.position, vert.normal, vert.color,
                       vert.texture == null ? null : vert.texture[0]);
             }
         }
     }
 
 }
