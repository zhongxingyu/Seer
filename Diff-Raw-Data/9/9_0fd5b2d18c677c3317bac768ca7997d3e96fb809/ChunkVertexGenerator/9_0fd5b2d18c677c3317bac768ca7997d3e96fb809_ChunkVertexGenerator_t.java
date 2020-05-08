 package ca.blarg.gdx.tilemap3d;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.VertexAttributes;
 import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
 import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector3;
 import ca.blarg.gdx.graphics.Vertices;
 import ca.blarg.gdx.tilemap3d.tilemesh.CubeTileMesh;
 import ca.blarg.gdx.tilemap3d.tilemesh.TileMesh;
 import ca.blarg.gdx.tilemap3d.tilemesh.TileMeshCollection;
 
 public class ChunkVertexGenerator {
 	public class GeneratedChunkMeshes {
 		public Mesh opaqueMesh;
 		public Mesh alphaMesh;
 	}
 
 	protected final MeshBuilder.VertexInfo vertex = new MeshPartBuilder.VertexInfo();
 
 	final MeshBuilder builder = new MeshBuilder();
 	final MeshBuilder alphaBuilder = new MeshBuilder();
 
 	final TileCoord tmpPosition = new TileCoord();
 	final Color tmpColor = new Color();
 	final Vector3 tmpOffset = new Vector3();
 
 	public GeneratedChunkMeshes generate(TileChunk chunk) {
 		TileMap tileMap = chunk.tileMap;
 
 		builder.begin(
 				VertexAttributes.Usage.Position |
 				VertexAttributes.Usage.Color |
 				VertexAttributes.Usage.Normal |
 				VertexAttributes.Usage.TextureCoordinates
 		);
 		alphaBuilder.begin(
 				VertexAttributes.Usage.Position |
 				VertexAttributes.Usage.Color |
 				VertexAttributes.Usage.Normal |
 				VertexAttributes.Usage.TextureCoordinates
 		);
 
 		for (int y = 0; y < chunk.getHeight(); ++y) {
 			for (int z = 0; z < chunk.getDepth(); ++z) {
 				for (int x = 0; x < chunk.getWidth(); ++x) {
 					Tile tile = chunk.get(x, y, z);
 					if (tile.isEmptySpace())
 						continue;
 
 					TileMesh mesh = chunk.tileMap.tileMeshes.get(tile);
 
 					// "world/tilemap space" position that this tile is at
 					tmpPosition.x = x + (int)chunk.getPosition().x;
 					tmpPosition.y = y + (int)chunk.getPosition().y;
 					tmpPosition.z = z + (int)chunk.getPosition().z;
 
 					Matrix4 transform = Tile.getTransformationFor(tile);
 
 					// tile color
 					if (tile.hasCustomColor())
 						tmpColor.set(tile.color);
 					else
 						tmpColor.set(mesh.color);
 
 					if (mesh instanceof CubeTileMesh)
 						handleCubeMesh(x, y, z, tile, chunk, (CubeTileMesh)mesh, tmpPosition, transform, tmpColor);
 					else
 						handleGenericMesh(x, y, z, tile, chunk, mesh, tmpPosition, transform, tmpColor);
 				}
 			}
 		}
 
 		GeneratedChunkMeshes output = new GeneratedChunkMeshes();
 		output.opaqueMesh = builder.end();
 		output.alphaMesh = alphaBuilder.end();
 		return output;
 	}
 
 	private void handleCubeMesh(int x, int y, int z, Tile tile, TileChunk chunk, CubeTileMesh mesh, TileCoord tileMapPosition, Matrix4 transform, Color color) {
 		// determine what's next to each cube face
 		Tile left = chunk.getWithinSelfOrNeighbourSafe(x - 1, y, z);
 		Tile right = chunk.getWithinSelfOrNeighbourSafe(x + 1, y, z);
 		Tile forward = chunk.getWithinSelfOrNeighbourSafe(x, y, z - 1);
 		Tile backward = chunk.getWithinSelfOrNeighbourSafe(x, y, z + 1);
 		Tile down = chunk.getWithinSelfOrNeighbourSafe(x, y - 1, z);
 		Tile up = chunk.getWithinSelfOrNeighbourSafe(x, y + 1, z);
 
 		// evaluate each face's visibility and add it's vertices if needed one at a time
 		if (canRenderCubeFace(TileMesh.SIDE_LEFT, tile, mesh, left, TileMesh.SIDE_RIGHT, color, chunk.tileMap.tileMeshes))
 			renderCubeFace(builder, alphaBuilder, tile, mesh, chunk, tileMapPosition, transform, color, mesh.leftFaceVertexOffset);
 		if (canRenderCubeFace(TileMesh.SIDE_RIGHT, tile, mesh, right, TileMesh.SIDE_LEFT, color, chunk.tileMap.tileMeshes))
 			renderCubeFace(builder, alphaBuilder, tile, mesh, chunk, tileMapPosition, transform, color, mesh.rightFaceVertexOffset);
 		if (canRenderCubeFace(TileMesh.SIDE_FRONT, tile, mesh, forward, TileMesh.SIDE_BACK, color, chunk.tileMap.tileMeshes))
 			renderCubeFace(builder, alphaBuilder, tile, mesh, chunk, tileMapPosition, transform, color, mesh.frontFaceVertexOffset);
 		if (canRenderCubeFace(TileMesh.SIDE_BACK, tile, mesh, backward, TileMesh.SIDE_FRONT, color, chunk.tileMap.tileMeshes))
 			renderCubeFace(builder, alphaBuilder, tile, mesh, chunk, tileMapPosition, transform, color, mesh.backFaceVertexOffset);
 		if (canRenderCubeFace(TileMesh.SIDE_BOTTOM, tile, mesh, down, TileMesh.SIDE_TOP, color, chunk.tileMap.tileMeshes))
 			renderCubeFace(builder, alphaBuilder, tile, mesh, chunk, tileMapPosition, transform, color, mesh.bottomFaceVertexOffset);
 		if (canRenderCubeFace(TileMesh.SIDE_TOP, tile, mesh, up, TileMesh.SIDE_BOTTOM, color, chunk.tileMap.tileMeshes))
 			renderCubeFace(builder, alphaBuilder, tile, mesh, chunk, tileMapPosition, transform, color, mesh.topFaceVertexOffset);
 	}
 
 	private boolean canRenderCubeFace(byte faceToCheck, Tile currentTile, CubeTileMesh currentTileMesh, Tile adjacentTile, byte adjacentFace, Color color, TileMeshCollection tileMeshes) {
 		if (!currentTileMesh.hasFace(faceToCheck))
 			return false;  // this tile doesn't even have the face, so nothing needs to be rendered
 
 		if (adjacentTile == null || adjacentTile.isEmptySpace())
 			return true;  // tile next to this face is empty, we can render away!
 
 		TileMesh adjacentTileMesh = tileMeshes.get(adjacentTile);
 
 		// both cube faces that are facing each other are part of the same tile and that tile is alpha + blended
 		// we should therefore not render this face as it tends to look a little weird in the rendered result (at best)
 		if (currentTileMesh.alpha && adjacentTileMesh.alpha && currentTile.tile == adjacentTile.tile && color.a < 1.0f)
 			return false;
 
 		if (adjacentTileMesh.isOpaque(adjacentFace) == false)
 			return true;
 
 		return false;
 	}
 
 	private void renderCubeFace(MeshBuilder builder, MeshBuilder alphaBuilder, Tile tile, TileMesh sourceMesh, TileChunk chunk, TileCoord position, Matrix4 transform, Color color, int firstVertex) {
 		if (sourceMesh.alpha)
 			addMesh(alphaBuilder, tile, sourceMesh, chunk, position, transform, color, firstVertex, TileMesh.CUBE_VERTICES_PER_FACE);
 		else
 			addMesh(builder, tile, sourceMesh, chunk, position, transform, color, firstVertex, TileMesh.CUBE_VERTICES_PER_FACE);
 	}
 
 	private void handleGenericMesh(int x, int y, int z, Tile tile, TileChunk chunk, TileMesh mesh, TileCoord tileMapPosition, Matrix4 transform, Color color) {
 		boolean visible = false;
 
 		// visibility determination. we check for at least one
 		// adjacent empty space / non-opaque tile
 		Tile left = chunk.getWithinSelfOrNeighbourSafe(x - 1, y, z);
 		Tile right = chunk.getWithinSelfOrNeighbourSafe(x + 1, y, z);
 		Tile forward = chunk.getWithinSelfOrNeighbourSafe(x, y, z - 1);
 		Tile backward = chunk.getWithinSelfOrNeighbourSafe(x, y, z + 1);
 		Tile down = chunk.getWithinSelfOrNeighbourSafe(x, y - 1, z);
 		Tile up = chunk.getWithinSelfOrNeighbourSafe(x, y + 1, z);
 
 		// null == empty space (off the edge of the entire map)
 		if (
 				(left == null || left.isEmptySpace() || !chunk.tileMap.tileMeshes.get(left).isOpaque(TileMesh.SIDE_RIGHT)) ||
 						(right == null || right.isEmptySpace() || !chunk.tileMap.tileMeshes.get(right).isOpaque(TileMesh.SIDE_LEFT)) ||
 						(forward == null || forward.isEmptySpace() || !chunk.tileMap.tileMeshes.get(forward).isOpaque(TileMesh.SIDE_BACK)) ||
 						(backward == null || backward.isEmptySpace() || !chunk.tileMap.tileMeshes.get(backward).isOpaque(TileMesh.SIDE_FRONT)) ||
 						(up == null || up.isEmptySpace() || !chunk.tileMap.tileMeshes.get(up).isOpaque(TileMesh.SIDE_BOTTOM)) ||
 						(down == null || down.isEmptySpace() || !chunk.tileMap.tileMeshes.get(down).isOpaque(TileMesh.SIDE_TOP))
 				)
 			visible = true;
 
 		if (visible) {
 			if (mesh.alpha)
 				addMesh(alphaBuilder, tile, mesh, chunk, tileMapPosition, transform, color, 0, mesh.getVertices().count());
 			else
 				addMesh(builder, tile, mesh, chunk, tileMapPosition, transform, color, 0, mesh.getVertices().count());
 		}
 	}
 
 	protected void addMesh(MeshBuilder builder, Tile tile, TileMesh sourceMesh, TileChunk chunk, TileCoord position, Matrix4 transform, Color color, int firstVertex, int numVertices) {
 		// adjust position by the tilemesh offset. TileMesh's are modeled using the
 		// origin (0,0,0) as the center and are 1 unit wide/deep/tall. So, their
 		// max extents are from -0.5,-0.5,-0.5 to 0.5,0.5,0.5. For rendering
 		// purposes in a chunk, we want the extents to be 0,0,0 to 1,1,1. This
 		// adjustment fixes that
 		tmpOffset.set(TileMesh.OFFSET);
 		tmpOffset.x += (float)position.x;
 		tmpOffset.y += (float)position.y;
 		tmpOffset.z += (float)position.z;
 
 		Vertices sourceVertices = sourceMesh.getVertices();
 		sourceVertices.moveTo(firstVertex);
 
 		// copy vertices
 		for (int i = 0; i < numVertices; ++i) {
 			sourceVertices.getVertex(vertex);

			// TODO: need to play with vertex/mesh color combinations a bit more to see if this is really correct
			vertex.color.set(
					vertex.color.r * color.r,
					vertex.color.g * color.g,
					vertex.color.b * color.b,
					vertex.color.a * color.a
			);
 
 			// transform if applicable... (this will probably just be per-tile rotation)
 			if (transform != null) {
 				vertex.position.mul(transform);
 				vertex.normal.rot(transform);
 			}
 
 			// translate vertex into "world/tilemap space"
 			vertex.position.add(tmpOffset);
 
 			builder.vertex(vertex);
 			sourceVertices.moveNext();
 		}
 	}
 }
