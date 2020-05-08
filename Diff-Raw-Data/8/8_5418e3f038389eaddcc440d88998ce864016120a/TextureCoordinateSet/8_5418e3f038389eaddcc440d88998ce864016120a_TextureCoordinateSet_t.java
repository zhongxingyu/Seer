 package yang.graphics.textures;
 
 import yang.graphics.translator.GraphicsTranslator;
 import yang.graphics.translator.Texture;
 import yang.util.NonConcurrentList;
 
 public abstract class TextureCoordinateSet {
 
 	public GraphicsTranslator mGraphics;
 	public NonConcurrentList<TextureCoordinatesQuad> mTexCoords;
 	protected float mTexWidth,mTexHeight;
 	protected float mDefaultBiasX=0,mDefaultBiasY=0;
 	protected float mPatchSizeX,mPatchSizeY;
 	
 	public static TextureCoordinatesQuad[] createTexCoordSequence(float startX,float startY,float width,float height,float texWidth,float texHeight,int count,float biasX,float biasY) {
 		TextureCoordinatesQuad[] result = new TextureCoordinatesQuad[count];
 		for(int i=0;i<count;i++) {
 			result[i] = new TextureCoordinatesQuad().initBiased(startX+i*width,startY,startX+i*width+width,startY+height,texWidth,texHeight,biasX,biasY);
 		}
 		return result; 
 	}
 	
 	public static TextureCoordinatesQuad[] createTexCoordSequence(float startX,float startY,float width,float height,float texWidth,float texHeight,int count) {
 		return createTexCoordSequence(startX,startY,width,height,texWidth,texHeight,count,0,0);
 	}
 	
 	public static TextureCoordinatesQuad[] createTexCoordSequencePixelsBias(Texture texture, float startX, float startY, float width, float height, int count, float biasX,float biasY) {
 		return createTexCoordSequence(startX,startY,width,height,texture.mWidth,texture.mHeight,count,biasX,biasY);
 	}
 	
 	public static TextureCoordinatesQuad[] createTexCoordSequencePixels(Texture texture, float startX, float startY, float width, float height, int count) {
 		return createTexCoordSequencePixelsBias(texture,startX,startY,width,height,count,0,0);
 	}
 	
 	public static TextureCoordinatesQuad[] createTexCoordSequencePixels(Texture texture, float startX, float startY, float size, int count) {
 		return createTexCoordSequencePixelsBias(texture,startX,startY,size,size,count,0,0);
 	}
 	
 	public TextureCoordinateSet(GraphicsTranslator graphics,float texWidth,float texHeight,float patchSize,float defaultBias) {
 		mGraphics = graphics;
 		mTexWidth = texWidth;
 		mTexHeight = texHeight;
 		mPatchSizeX = patchSize;
 		mPatchSizeY = patchSize;
 		mDefaultBiasX = defaultBias;
 		mDefaultBiasY = defaultBias;
 		mTexCoords = new NonConcurrentList<TextureCoordinatesQuad>();
 	}
 	
 	public TextureCoordinateSet(GraphicsTranslator graphics) {
 		this(graphics,1,1,1/8f,0);
 	}
 	
 	public TextureCoordinateSet(GraphicsTranslator graphics,float texWidth,float texHeight) {
 		this(graphics,texWidth,texHeight,1,0.5f);
 	}
 	
 	public TextureCoordinateSet(GraphicsTranslator graphics,float texSize) {
 		this(graphics,texSize,texSize);
 	}
 	
 	public TextureCoordinatesQuad addTexCoords(TextureCoordinatesQuad texCoords) {
 		mTexCoords.add(texCoords);
 		return texCoords;
 	}
 	
 	public TextureCoordinatesQuad createTexCoordsAbsBiased(float left,float top,float right,float bottom, float biasX, float biasY) {
 		TextureCoordinatesQuad texCoords = new TextureCoordinatesQuad().initBiased(left,top,right,bottom,mTexWidth,mTexHeight,biasX,biasY);
 		mTexCoords.add(texCoords);
 		return texCoords;
 	}
 	
 	public TextureCoordinatesQuad createTexCoordsAbs(float left,float top,float right,float bottom) {
 		return createTexCoordsAbsBiased(left,top,right,bottom,mDefaultBiasX,mDefaultBiasY);
 	}
 	
 	public TextureCoordinatesQuad createTexCoordsBiased(float left,float top,float width,float height, float biasX, float biasY) {
 		return createTexCoordsAbsBiased(left,top,left+width,top+height,biasX,biasY);
 	}
 	
 	public TextureCoordinatesQuad createTexCoords(float left,float top,float width,float height) {
 		return createTexCoordsBiased(left,top,width,height,mDefaultBiasX,mDefaultBiasY);
 	}
 	
 	public TextureCoordinatesQuad createTexCoordsPatched(float patchX,float patchY,float patchWidth,float patchHeight) {
 		return createTexCoords(patchX*mPatchSizeX,patchY*mPatchSizeY,(patchWidth)*mPatchSizeX,(patchHeight)*mPatchSizeY);
 	}
 	
 	public TextureCoordinatesQuad createTexCoordsPatched(float patchX,float patchY,float patchSize) {
 		return createTexCoordsPatched(patchX,patchY,patchSize,patchSize);
 	}
 	
 	public TextureCoordinatesQuad setBias(TextureCoordinatesQuad texCoords,float biasX,float biasY) {
 		return texCoords.setBias(biasX/mTexWidth,biasY/mTexHeight);
 	}
 	
 	public TextureCoordinatesQuad setBias(TextureCoordinatesQuad texCoords,float bias) {
 		return setBias(texCoords,bias,bias);
 	}
 	
 	public TextureCoordinatesQuad[] createTexCoordSequenceBias(float startX,float startY,float width,float height,int countX,int countY,float biasX,float biasY) {
 		//TextureCoordinatesQuad[] result = new TextureCoordinatesQuad[count];
 //		for(int i=0;i<count;i++) {
 //			result[i] = createTexCoordsAbsBiased(startX+i*width,startY,startX+i*width+width,startY+height, biasX,biasY);
 //		}
 		return createSequence(createTexCoordsBiased(startX,startY,width,height,biasX,biasY), countX,countY);
 	}
 	
 	public TextureCoordinatesQuad[] createTexCoordSequenceBias(float startX,float startY,float width,float height,int count,float biasX,float biasY) {
 		return createTexCoordSequenceBias(startX,startY,width,height,count,1,biasX,biasY);
 	}
 	
 	public TextureCoordinatesQuad[] createTexCoordSequence(float startX,float startY,float width,float height,int count) {
 		return createTexCoordSequenceBias(startX,startY,width,height,count,mDefaultBiasX,mDefaultBiasY);
 	}
 	
 	public TextureCoordinatesQuad[] createTexCoordPatchSequence(float patchX,float patchY,float patchWidth,float patchHeight,int countX,int countY) {
 		return createTexCoordSequenceBias(patchX*mPatchSizeX,patchY*mPatchSizeY,(patchWidth)*mPatchSizeX,(patchHeight)*mPatchSizeY,countX,countY,mDefaultBiasX,mDefaultBiasY);
 	}
 	
 	public TextureCoordinatesQuad[] createTexCoordPatchSequence(float patchX,float patchY,float patchWidthAndHeight,int countX) {
 		return createTexCoordPatchSequence(patchX,patchY,patchWidthAndHeight,patchWidthAndHeight,countX,1);
 	}
 	
 	public TextureCoordinatesQuad[] createTexCoordSequence(float startX,float startY,float size,int count) {
 		return createTexCoordSequence(startX,startY,size,size,count);
 	}
 
 	public TextureCoordinatesQuad createTexCoordsNormalized(float left, float top, float width,float height) {
 		TextureCoordinatesQuad texCoords = new TextureCoordinatesQuad().initBiased(left,top,left+width,top+height, mDefaultBiasX/mTexWidth,mDefaultBiasY/mTexHeight);
 		mTexCoords.add(texCoords);
 		return texCoords;
 	}
 	
 	public TextureCoordinatesQuad createTexCoordsNormalized(float left, float top, float size) {
 		return createTexCoordsNormalized(left,top,size,size);
 	}
 	
 	public TextureCoordinatesQuad register(TextureCoordinatesQuad texCoords) {
 		if(texCoords!=null && !mTexCoords.contains(texCoords))
 			mTexCoords.add(texCoords);
 		return texCoords;
 	}
 	
 	public TextureCoordinatesQuad[] register(TextureCoordinatesQuad[] texCoords) {
 		for(TextureCoordinatesQuad coords:texCoords) 
 			register(coords);
 		return texCoords;
 	}
 	
 	public TextureCoordinatesQuad[] createSequence(TextureCoordinatesQuad texCoords,int countX,int countY) {
 		TextureCoordinatesQuad[] result = TextureCoordinatesQuad.createSequence(texCoords, countX,countY);
 		register(result);
 		return result;
 	}
 	
 	public TextureCoordinatesQuad[] createSequence(TextureCoordinatesQuad texCoords,int count) {
 		return createSequence(texCoords,count,1);
 	}
 	
 }
