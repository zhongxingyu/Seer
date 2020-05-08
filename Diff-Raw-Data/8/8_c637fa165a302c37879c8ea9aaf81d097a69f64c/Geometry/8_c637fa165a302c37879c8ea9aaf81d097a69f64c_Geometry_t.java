 package ja3d.resources;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import utils.ByteArray;
 import utils.Vector3D;
 
 import ja3d.core.BoundBox;
 import ja3d.core.Resource;
 import ja3d.core.Transform3D;
 import ja3d.core.VertexAttributes;
 import ja3d.core.VertexStream;
 
 public class Geometry extends Resource {
 
 	// the name of the mesh which contains this geometry.
 	// this value is used to identify the target mesh during the collision detection.
 	private String _name;
 
 	private List<VertexStream> _vertexStreams = new ArrayList<VertexStream>();
 	
 	private int _numVertices = 0;
 	
 	// This is the vertex indices with stride 3, each forms a triangle.
 	public List<Integer> _indices = new ArrayList<Integer>();
 	
 	private VertexStream[] _attributesStreams = new VertexStream[VertexAttributes.NEXT_INDEX];
 	private int[] _attributesOffsets = new int[VertexAttributes.NEXT_INDEX];
 	private int[] _attributesStrides = new int[VertexAttributes.NEXT_INDEX];
 	
 	
 	public int getNumVertices() {
 		return _numVertices;
 	}
 	
 	public void setNumVertices(int value) {
 		if (_numVertices != value) {
 			// Change buffers
 			// Since the buffer data is set by setVertexStreamData(), no need to update the buffer here.
 			_numVertices = value;
 		}
 	}
 	
 	public int addVertexStream(List<Integer> attributes) {
 		int numMappings = attributes.size();
 		if (numMappings < 1) {
 			throw new IllegalArgumentException("empty attributes");
 		}
 		for (int attribute : attributes) {
 			if (attribute <= 0 || attribute >= VertexAttributes.NEXT_INDEX) {
 				throw new IllegalArgumentException("Invalid attribute " + attribute);
 			}
 		}
 		
 		VertexStream vBuffer = new VertexStream();
 		int attribute = attributes.get(0);
 		int stride = 1;
 		// this is to process situations like [POSITION, POSITION, POSITION] together.
 		for (int i = 1; i <= numMappings; i++) {
 			int next = i < numMappings ? attributes.get(i) : 0;
 			if (next != attribute) {
 				if (attribute != 0) {
 					if (_attributesStreams[attribute] != null) {
 						throw new IllegalStateException("Attribute " + attribute + " already used in this geometry.");
 					}
 					int numStandardFloats = VertexAttributes.getAttributeStride(attribute);
 					if (numStandardFloats != 0 && numStandardFloats != stride) {
 						throw new IllegalStateException("Standard attributes must be predefined size.");
 					}
 					
 					int startIndex = i - stride;
 					
 					_attributesStreams[attribute] = vBuffer;
 					_attributesOffsets[attribute] = startIndex;
 					_attributesStrides[attribute] = stride;
 				}
  				stride = 1;
 			} else {
 				stride ++;
 			}
 			attribute = next;
 		}
 		
 		vBuffer.attributes = new int[numMappings];
 		for (int i = 0; i < numMappings; i++) {
 			vBuffer.attributes[i] = attributes.get(i);
 		}
 		// we don't initialize the data here, since it will be updated by setVertexStreamData().
 //		vBuffer.data = new ByteArray();
 //		vBuffer.data.setEndian(ByteOrder.LITTLE_ENDIAN);
 //		vBuffer.data.setLength(4 * numMappings * _numVertices);
 		
 		_vertexStreams.add(vBuffer);
 		// return the index of this vertex stream.
 		return _vertexStreams.size() - 1;
 	}
 	
 	public void setVertexStreamData(int vertexStreamIndex, ByteArray data) {
 		if (vertexStreamIndex < this._vertexStreams.size()) {
 			VertexStream vs = _vertexStreams.get(vertexStreamIndex);
 			vs.data = data;
 		}
 	}
 	
 	public void updateBoundBox(BoundBox boundBox, Transform3D transform) {
 		VertexStream vBuffer = getVertexStream(VertexAttributes.POSITION);
 		if (vBuffer == null) {
 			throw new IllegalStateException("Cannot calculate BoundBox without data.");
 		}
 		
 		int offset = getAttributeOffset(VertexAttributes.POSITION);
 		int numMappings = vBuffer.attributes.length;
 		ByteArray data = vBuffer.data;
 		
 		for (int i = 0; i < _numVertices; i++) {
 			data.setPosition(4 * (numMappings * i + offset));
 			Vector3D v = new Vector3D();
 			v.x = data.readFloat();
 			v.y = data.readFloat();
 			v.z = data.readFloat();
 			Vector3D tv = v;
 			if (transform != null) {
 				tv = transform.transform(v, new Vector3D());
 			}
 			if (tv.x < boundBox.minX) boundBox.minX = tv.x;
 			if (tv.x > boundBox.maxX) boundBox.maxX = tv.x;
 			if (tv.y < boundBox.minY) boundBox.minY = tv.y;
 			if (tv.y > boundBox.maxY) boundBox.maxY = tv.y;
 			if (tv.z < boundBox.minZ) boundBox.minZ = tv.z;
 			if (tv.z > boundBox.maxZ) boundBox.maxZ = tv.z;
 		}
 	}
 	
 	public VertexStream getVertexStream(int attribute) {
 		assert(attribute > 0 && attribute < VertexAttributes.NEXT_INDEX);
 		return _attributesStreams[attribute];
 	}
 	
 	public int getAttributeOffset(int attribute) {
 		assert(attribute > 0 && attribute < VertexAttributes.NEXT_INDEX);
 		return _attributesOffsets[attribute];
 	}
 	
 	public String getName() {
		return _name;
 	}
 	
 	public void setName(String value) {
 		_name = value;
 	}
 }
