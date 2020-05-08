 /* Copyright 2012 Richard Sahlin
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 package com.super2k.openglen.utils;
 
 import java.nio.Buffer;
 import java.util.LinkedList;
 
 import com.super2k.openglen.ConstantValues;
 import com.super2k.openglen.OpenGLENException;
 import com.super2k.openglen.RenderSetting;
import com.super2k.openglen.Renderer;
 import com.super2k.openglen.objects.GLBlitObject;
 import com.super2k.openglen.objects.GLObject;
 import com.super2k.openglen.objects.GLParticleArray;
 import com.super2k.openglen.texture.TextureHandler;
 
 
 
 /**
  * This class provides an abstraction for graphics library utility functions
  * Currently supported on JOGL (J2SE) and Android.
  * @author Richard Sahlin
  *
  */
 public abstract class GraphicsLibraryHandler {
 
     public final String TAG = getClass().getSimpleName();
 
     protected final static String GLES_NULL_STR = "GLES is null";
 
     protected final static String VBO_BUFFER_NAME_ERROR = "Could not create buffer object names.";
     protected final static String VBO_CONVERT_ERROR = "Could not convert object to VBO.";
     protected final static String ILLEGAL_GET_NAME = "Could not get String, illegal name";
     protected final static String INVALID_PARAMETER = "Invalid paramter.";
 
 
     /**
      * Setup GL to the default values, clear buffers before rendering takes place.
      * Make sure RenderSettings are set to GL.
      * @param setting The RenderSetting to setup GL to.
      */
     public void setupGL(RenderSetting setting) {
         setting.setChangeFlag(RenderSetting.CHANGE_FLAG_ALL);
         setRenderSetting(setting);
         setting.setChangeFlag(RenderSetting.CHANGE_FLAG_NONE);
         clear(ConstantValues.COLOR_BUFFER_BIT | ConstantValues.DEPTH_BUFFER_BIT);
 
     }
 
 
     /**
      * Sets the RenderSetting variables to the underlying render backend
      * Call this method when context is current.
      * The isDirty flag of the RenderSetting is checked, if false nothing is done.
      * @param setting
      */
     public abstract void setRenderSetting(RenderSetting setting);
 
 
     /**
      * Checks the underlying rendering platform (usually GL) for error,
      * if an error a message is logged and the error is returned.
      * This only fetches the first possible error from the underlying framework.
      * To check all possible errors call this method until it returns NO_ERROR
      * @return The error result.
      */
     public abstract int checkError();
 
     /**
      * Internal method for getting a GL String, check that name is correct before calling
      * this method.
      * Returns a string describing the current GL connection
      * @param name a symbolic constant, one of VENDOR, RENDERER, VERSION, or EXTENSIONS.
      * @return string describing some aspect of the current GL connection.
      */
     protected abstract String internalGetString(int name);
 
     /**
      * Internal method to get a GL value. Make a call to glGetInteger
      * Clear error before this method and check error after.
      * @param pname Specifies the parameter value to be returned.
      * @param value value stored here,  the value or values of the specified parameter.
      * @param offset offset into value.
      */
     protected abstract void internalGetInteger(int pname, int[] value, int offset);
     /**
      * Internal method to get a GL value. Make a call to glGetFloat
      * Clear error before this method and check error after.
      * @param pname Specifies the parameter value to be returned.
      * @param value value stored here,  the value or values of the specified parameter.
      * @param offset offset into value.
      */
     protected abstract void internalGetFloat(int pname, float[] value, int offset);
     /**
      * Internal method to get a GL value. Make a call to glGetBoolean
      * Clear error before this method and check error after.
      * @param pname Specifies the parameter value to be returned.
      * @param value value stored here,  the value or values of the specified parameter.
      * @param offset offset into value.
      */
     protected abstract void internalGetBoolean(int pname, boolean[] value, int offset);
 
     /**
      * Internal wrapper method for bufferData.
      * Clear error before this method and check error after.
      * @param target The target of the buffer, ARRAY_BUFFER or ELEMENT_ARRAY_BUFFER
      * @param size Size of the buffer in bytes.
      * @param data Data array to copy into buffer or null for no copy.
      * @param usage Usage flag, STATIC_DRAW or DYNAMIC_DRAW
      * @return Result, NO_ERROR or errorcode
      */
     protected abstract int internalBufferData(int target, int size, Buffer data, int usage);
 
     /**
      * Internal method to clear buffer, what buffers to clear is specified by parameter.
      * @param flags What buffers to clear, bitwise or of:
      * ConstantValues.COLOR_BUFFER_BIT | ConstantValues.DEPTH_BUFFER_BIT |
      * ConstantValues.STENCIL_BUFFER_BIT
      */
     protected abstract void internalClearBuffer(int flags);
 
     /**
      * Internal method to set clear depth.
      * @param depth Clear depth value.
      */
     protected abstract void internalClearDepth(float depth);
 
     /**
      * Internal method to set clear color
      * @param colors RGBA color array, Red at lowest index
      * @param offset Offset into array where values are read.
      */
     protected abstract void internalClearColor(float[] colors, int offset);
 
     /**
      * Internal method to clear stencil buffer.
      * @param stencil Stencil value to clear with.
      */
     protected abstract void internalClearStencil(int stencil);
 
     /**
      * Clears any pending errors, this means we dont care of any possible errors.
      */
     public abstract void clearError();
 
     /**
      * Converts the GLObject to use VBO.
      * If object is succesfully converted to use VBOs the IDs are stored in the blit object.
      * These IDs must be deleted when the object is not used.
      * @param blit The blit object to use VBOs, for vertices and indices
      * @throws OpenGLENException If the object could not be converted to VBO.
      */
     public void convertToVBO(GLObject glObject) throws OpenGLENException {
 
         if (glObject instanceof GLBlitObject) {
             convertBlitTOVBO((GLBlitObject)glObject);
         }
         else if (glObject instanceof GLParticleArray) {
             convertParticleArrayTOVBO((GLParticleArray)glObject);
         }
     }
 
     /**
      * Converts a GLBLitObject to use VBOs.
      * @param blit The blit object to convert to using VBOs.
      * @throws OpenGLENException If the object could not be converted to using VBOs.
      * TODO Use one method to convert buffer array that is shared with GLObject
      */
     protected void convertBlitTOVBO(GLBlitObject blit) throws OpenGLENException {
 
         int tries = 0;
         //Create the buffer
         boolean failed = false;
         int[] bufferNames = new int[2];
         int[] bufferTarget = new int[2];
         boolean retry = true;
         while (retry) {
 
             retry = false; //set retry to true if out of memory error after calling gc.
             try {
 
                 genBuffers(2, bufferNames, 0); //only reports error if count is negative
                 if (bufferNames[0] != 0 && bufferNames[1] != 0 )    {
                     int vertexCount = blit.getVertexCount();     //Need to store 3 buffers,
                     //vertices, normals tex coords.
                     bindBuffer(ConstantValues.ARRAY_BUFFER, bufferNames[0]);
                     bufferData(ConstantValues.ARRAY_BUFFER,
                             (vertexCount * 3 + vertexCount * 3 + vertexCount * 2) * 4,
                             blit.getArrayBuffer().rewind(), ConstantValues.STATIC_DRAW);
                     int result = checkError();
                     if (result == ConstantValues.NO_ERROR) {
                         bufferTarget[0] = ConstantValues.ARRAY_BUFFER;
                         blit.setArrayVBOName(bufferNames[0]);
                         bindBuffer(ConstantValues.ELEMENT_ARRAY_BUFFER, bufferNames[1]);
                         bufferData(ConstantValues.ELEMENT_ARRAY_BUFFER,
                                 blit.getIndexCount() * 2,
                                 blit.getElementBuffer().rewind(),
                                 ConstantValues.STATIC_DRAW);
                         result = checkError();
                         if (result != ConstantValues.NO_ERROR) {
                             failed = true;
                             throw new OpenGLENException(VBO_CONVERT_ERROR, result);
                         }
                         bufferTarget[1] = ConstantValues.ELEMENT_ARRAY_BUFFER;
                         blit.setElementVBOName(bufferNames[1]);
                     }
                     else {
                         failed = true;
                         throw new OpenGLENException(VBO_CONVERT_ERROR, result);
                     }
                 }
                 else {
                     failed = true;
                     throw new OpenGLENException(VBO_BUFFER_NAME_ERROR);
                 }
             }
             catch (OutOfMemoryError oome) {
                 failed = true;
                 tries++;
                 if (tries < 2) {
                     Log.d(TAG, "Errror! OutofMemory allocating VBO, calling GC.");
                     JavaUtils.stabilizeFreeMemory();
                     retry = true;
                 } else {
                     Log.d(TAG, oome.toString() + ", already called GC. Aborting.");
                 }
             }
             finally {
                 //Release buffers if not succesful
                 if (failed) {
                     for (int i = 0; i < bufferTarget.length; i++) {
                         if (bufferTarget[i] != 0) {
                             clearError();
                             bindBuffer(bufferTarget[i], bufferNames[i]);
                             int[] name = new int[] {bufferNames[i]};
                             deleteBuffers(1, name, 0);
                         }
                     }
                 }
             }
         } //end while
     }
 
     /**
      * Converts a GLParticleArray to use VBOs.
      * @param blit The particles object to convert to using VBOs.
      * @throws OpenGLENException If the object could not be converted to using VBOs.
      * TODO Use one method to convert buffer array that is shared with GLObject
      */
     protected void convertParticleArrayTOVBO(GLParticleArray particles) throws OpenGLENException {
 
         //Create the buffer
         boolean failed = false;
         int[] bufferNames = new int[1];
         try {
             genBuffers(1, bufferNames, 0); //only reports error if count is negative
             if (bufferNames[0] != 0 )    {
                 int vertexCount = particles.getMaxParticleCount();     //Need to store 3 buffers,
                 //vertices, speed and extra data.
                 bindBuffer(ConstantValues.ARRAY_BUFFER, bufferNames[0]);
                 bufferData(ConstantValues.ARRAY_BUFFER,
                         (vertexCount * GLParticleArray.PARTICLE_FLOAT_COUNT) * 4,
                         particles.arrayBuffer.rewind(), ConstantValues.DYNAMIC_DRAW);
                 int result = checkError();
                 if (result == ConstantValues.NO_ERROR) {
 
                     result = checkError();
                     if (result != ConstantValues.NO_ERROR) {
                         failed = true;
                         throw new OpenGLENException(VBO_CONVERT_ERROR, result);
                     }
                     particles.setArrayVBOName(bufferNames[0]);
                 }
                 else {
                     failed = true;
                     throw new OpenGLENException(VBO_CONVERT_ERROR, result);
                 }
             }
             else {
                 failed = true;
                 throw new OpenGLENException(VBO_BUFFER_NAME_ERROR);
             }
         }
         finally {
             //Release buffers if not succesful
             if (failed) {
                 deleteBuffers(1, bufferNames, 0);
             }
         }
     }
 
 
 
     /**
      * Returns the uniform location for the specifid program and name.
      * @param program
      * @param name
      * @return The uniform location of -1 if not found.
      */
     public abstract int getUniformLocation(int program, String name);
 
     /**
      * Returns a String describing the current GL connection
      * @param name a symbolic constant, one of ConstantValues.VENDOR,
      * ConstantValues.RENDERER, ConstantValues.VERSION, or
      * ConstantValues.EXTENSIONS.
      * @return string describing some aspect of the current GL connection.
      * @throws IllegalArgumentException If name is not one of:
      * ConstantValues.VENDOR, ConstantValues.RENDERER,
      * ConstantValues.VERSION, or ConstantValues.EXTENSIONS.
      */
     public String getString(int name) {
         if (name != ConstantValues.VENDOR && name != ConstantValues.RENDERER &&
                 name != ConstantValues.VERSION && name != ConstantValues.EXTENSIONS) {
             throw new IllegalArgumentException(ILLEGAL_GET_NAME);
         }
 
         return internalGetString(name);
     }
 
 
     /**
      * return the value or values of a selected parameter
      * @see glGetInteger()
      * @param pname Specifies the parameter value to be returned. @see glGet for list of
      * accepted names.
      * @return The value or values of the specified parameter.
      * @throws IllegalArgumentException If pname is not valid.
      */
     public int[] getInteger(int pname) {
         int size = 1;
         //Check for all names that need more than one returnvalue.
         switch (pname) {
             case ConstantValues.ALIASED_LINE_WIDTH_RANGE:
             case ConstantValues.ALIASED_POINT_SIZE_RANGE:
             case ConstantValues.DEPTH_RANGE:
             case ConstantValues.MAX_VIEWPORT_DIMS:
                 size = 2;
                 break;
 
             case ConstantValues.BLEND_COLOR:
             case ConstantValues.COLOR_CLEAR_VALUE:
             case ConstantValues.COLOR_WRITEMASK:
             case ConstantValues.SCISSOR_BOX:
                 size = 4;
                 break;
 
 
             case ConstantValues.COMPRESSED_TEXTURE_FORMATS:
             case ConstantValues.SHADER_BINARY_FORMATS:
                 throw new IllegalArgumentException("Not implemented");
         }
         int[] result = new int[size];
         clearError();
         internalGetInteger(pname, result, 0);
         int error = checkError();
         if (error != ConstantValues.NO_ERROR) {
             throw new IllegalArgumentException(ILLEGAL_GET_NAME + ", " + error);
         }
         return result;
     }
 
     /**
      * Generates buffer object names.
      * @param count Number of buffer object names to generate.
      * @param names Array where object names are returned.
      * @param offset Offset into array.
      */
     public abstract void genBuffers(int count, int[] names, int offset);
 
     /**
      * Binds a named buffer to the specified target.
      * @param target The target to which the buffer is bound.
      * Must be ConstantValues.ARRAY_BUFFER or
      * ConstantValues.ELEMENT_ARRAY_BUFFER.
      * @param buffer The name of a buffer object.
      */
     public abstract void bindBuffer(int target, int buffer);
 
     /**
      * Clears the currently bound framebuffer.
      * If no buffers are attached to framebuffer then this
      * method does nothing.
      * Use this method for instance if you are rendering to offscreen buffers that needs
      * to be cleared.
      * Note that the clear values are affected so you may want to update RenderSettings,otherwise
      * the depth, color or stencil clear value may be wrong. Obviously not needed if
      * clear is set to NONE.
      * @param flags What parts of the buffer to clear.
      * ConstantValues.COLOR_BUFFER_BIT | ConstantValues.DEPTH_BUFFER_BIT |
      * ConstantValues.STENCIL_BUFFER_BIT
      * @param depth Depth value to clear depth buffer to if DEPTH_BUFFER_BIT is set in flags.
      * @param colors RGBA (Red at lowest index) to clear colorbuffer with if
      * COLOR_BUFFER_BIT is set.
      * @param offset Offset into colors array where values are fetched.
      * @param stencil Value to clear stencil buffer with if STENCIL_BUFFER_BIT is set.
      * @throws IllegalArgumentException If COLOR_BUFFER_BIT is set and colors is null
      * or does not contain 4 values at offset, OR if flags is not bitwise one of:
      * ConstantValues.COLOR_BUFFER_BIT | ConstantValues.DEPTH_BUFFER_BIT |
      * ConstantValues.STENCIL_BUFFER_BIT
      */
     public void clearBuffer(int flags, float depth, float[] colors, int offset, int stencil) {
         if ((flags & ConstantValues.COLOR_BUFFER_BIT) != 0) {
             if (colors == null || colors.length < offset + 4) {
                 throw new IllegalArgumentException(INVALID_PARAMETER);
             }
             internalClearColor(colors,  offset);
         }
         if ((flags & ConstantValues.DEPTH_BUFFER_BIT) != 0) {
             internalClearDepth(depth);
         }
         if ((flags & ConstantValues.STENCIL_BUFFER_BIT) != 0) {
             internalClearStencil(stencil);
         }
         clearError();
         internalClearBuffer(flags);
         int error = checkError();
         if (error != ConstantValues.NO_ERROR) {
            Log.d(Renderer.OPENGLEN_TAG,"Error clearBuffer: " + error);
         }
     }
 
     /**
      * Clear buffers to preset values
      * @param flags COLOR_BUFFER_BIT | DEPTH_BUFFER_BIT | STENCIL_BUFFER_BIT
      */
     public abstract void clear(int flags);
 
     /**
      * Creates and initializes a buffer object's data store.
      * @param target Specifies the target buffer object.
      * Must be ConstantValues.ARRAY_BUFFER or
      * ConstantValues.ELEMENT_ARRAY_BUFFER.
      * @param size Size in byte of the buffers new data store.
      * @param data The data to store at initialization,
      * or NULL to not copy any data.
      * @param usage Expected usage pattern
      * ConstantValues.StaticDraw or ConstantValues.DynamicDraw
      * @throws IllegalArgumentException If any of the parameters are invalid.
      * @throws OutOfMemoryException if the bufferData call fails due to out of memory.
      */
     public void bufferData(int target,int size, Buffer data, int usage) {
         //Check for illegal arguments.
         if (target < 0 || target == 0 || size < 0 ||
                 (target != ConstantValues.ELEMENT_ARRAY_BUFFER &&
                  target != ConstantValues.ARRAY_BUFFER) ||
                 (usage != ConstantValues.STATIC_DRAW && usage != ConstantValues.DYNAMIC_DRAW)){
             throw new IllegalArgumentException("Invalid parameter for bufferData.");
         }
         //Call should be OK unless out of memory.
         clearError();
         int result = internalBufferData(target, size, data, usage);
         if (result == ConstantValues.OUT_OF_MEMORY)   {
             throw new OutOfMemoryError("Out of memory calling bufferData" +
                     target + "," + size + "," + data + ", " + usage);
         }
 
     }
 
     /**
      * Deletes the named buffers.
      * @param count Number of buffer object names to delete.
      * @param names Array containing buffer object names.
      * @param offset Offset into the array.
      */
     public abstract void deleteBuffers(int count, int[] names, int offset);
 
     /**
      * Enable a capability in the underlying graphics library.
      * one of:
      * TEXTURE_2D
      * CULL_FACE
      * BLEND
      * DITHER
      * STENCIL_TEST
      * DEPTH_TEST
      * SCISSOR_TEST
      * POLYGON_OFFSET_FILL
      * SAMPLE_ALPHA_TO_COVERAGE
      * SAMPLE_COVERAGE
      * @param caps
      */
     public abstract void enable(int cap);
 
     /**
      * Disable a capability in the underlying graphics library.
      * One of:
      * TEXTURE_2D
      * CULL_FACE
      * BLEND
      * DITHER
      * STENCIL_TEST
      * DEPTH_TEST
      * SCISSOR_TEST
      * POLYGON_OFFSET_FILL
      * SAMPLE_ALPHA_TO_COVERAGE
      * SAMPLE_COVERAGE
      * @param caps
      */
     public abstract void disable(int cap);
 
     /**
      * @param sourceFactor Specifies how the red, green, blue,
      * and alpha source blending factors are computed.
      * The following symbolic constants are accepted:
      * ZERO, ONE, DST_COLOR, ONE_MINUS_DST_COLOR, SRC_ALPHA,
      * ONE_MINUS_SRC_ALPHA, DST_ALPHA, ONE_MINUS_DST_ALPHA, and SRC_ALPHA_SATURATE.
      * @param destFactor Specifies how the red, green, blue,
      * and alpha destination blending factors are computed.
      * ZERO, ONE, SRC_COLOR, ONE_MINUS_SRC_COLOR, SRC_ALPHA, ONE_MINUS_SRC_ALPHA,
      * DST_ALPHA, and ONE_MINUS_DST_ALPHA.
      */
     public abstract void blendFunc(int sourceFactor, int destFactor);
 
 
     /**
      * Fetches the EGL framebuffer configuration.
      * @param egl
      * @param eglDisplay
      * @param config The EGL framebuffer config to query.
      * @param attribute What EGL attribute to return.
      * @return The value for the specified EGL attribute.
      * @throws IllegalArgumentException If egl, eglDisplay or config is NULL.
      * @throws ClassCastException if the Objects are not correct EGL objects for the platform.
      */
     public abstract int getEGLConfigAttrib(Object egl, Object eglDisplay, Object config,
                                            int attribute);
 
     /**
      * Delete any VBO buffers allocated for this blit object and then delete the buffer
      * object names.
      * @param blit
      * @throws IllegalArgumentException If blit is null.
      */
     public void deleteVBOBuffers(GLBlitObject blit) {
         if (blit == null) {
             throw new IllegalArgumentException(INVALID_PARAMETER + blit);
         }
         int[] names = new int[] {blit.arrayVBOName};
         if (names[0] > 0) {
             deleteVBOBuffer(names, 1, 0, ConstantValues.ARRAY_BUFFER);
         }
         if (blit.elementVBOName > 0) {
             names[0] = blit.elementVBOName;
             deleteVBOBuffer(names, 1, 0, ConstantValues.ELEMENT_ARRAY_BUFFER);
         }
 
     }
 
     /**
      * Delete any VBO buffers allocated for this blit object and delete the buffer object names.
      * @param particles
      * @throws IllegalArgumentException If blit is null.
      */
     public void deleteVBOBuffers(GLParticleArray particles) {
         if (particles == null) {
             throw new IllegalArgumentException(INVALID_PARAMETER + particles);
         }
         int[] names = new int[] {particles.arrayVBOName};
         if (names[0] != 0) {
             deleteVBOBuffer(names, 1, 0, ConstantValues.ARRAY_BUFFER);
         }
 
 
     }
 
     /**
      * Delete VBO buffer and buffer object names.
      * @param names Name of buffer objects to delete, buffer and name will be deleted.
      * @param count Number of buffers to delete
      * @param index Index into names
      * @param target target of buffer ARRAY_BUFFER or ELEMENT_ARRAY_BUFFER
      * @throws IllegalArgumentException If target is invalid or count < 0 or names does
      * not contain at least index + count elements.
      */
     public void deleteVBOBuffer(int[] names, int count, int index, int target) {
         if (count < 0 || (target != ConstantValues.ARRAY_BUFFER &&
                 target != ConstantValues.ELEMENT_ARRAY_BUFFER) ||
                 names == null || names.length < index + count) {
             throw new IllegalArgumentException(INVALID_PARAMETER);
         }
         for (int i = index; i < index + count; i++) {
             if (names[i] != 0) {
                 bindBuffer(target, names[i]);
                 deleteBuffers(1, names, i);
             }
         }
 
     }
 
     /**
      * Logs the specifics of the EGLConfig.
      * This will log information regarding bitdepth, alpha size etc.
      * Currently only works on Android.
      * @param egl The EGL
      * @param eglDisplay EGL display connection.
      * @param config The EGL config to log.
      * @param loglevel Loglevel
      */
     public abstract void logConfig(Object eglObject,
                                    Object eglDisplayObject,
                                    Object configObject,
                                    int loglevel);
 
 
     /**
      * Delete all object buffers in the specified list and removes the
      * objects from the list.
      * Note! This will not destroy the texture name or texture buffer!
      * Use this method when you want to discard all the objects, in a list,
      * for instance when an application is exiting.
      * @param list List with GLBLitObject to delete the buffers for.
      * @param textureHandler
      * @throws IllegalArgumentException If list is null.
      */
     public void releaseGLBuffers(LinkedList<GLBlitObject> list, TextureHandler textureHandler) {
         if (list==null) {
             throw new IllegalArgumentException("GLBLitObject list is null");
         }
         int count = list.size();
         GLBlitObject blit;
         for (int i = 0; i < count; i++) {
             blit = list.removeLast();
             releaseGLObject(blit, textureHandler);
             blit.destroy();
         }
     }
 
     /**
      * Releases all the buffers for the object, this will release any VBO or similar buffers that are allocated.
      * The nio buffers holding verteces and indices will not be released, ie the blit object can be
      * reused after this call.
      * Note! Texture name and buffer will not be destroyed.
      * @param The object to release buffers for.
      * @param textureHandler
      */
     public void releaseGLObject(GLObject blit, TextureHandler textureHandler) {
         switch (blit.objectType) {
             case GLObject.BLIT_OBJECT:
                 deleteVBOBuffers((GLBlitObject) blit);
             break;
             case GLObject.PARTICLE_ARRAY:
                 deleteVBOBuffers((GLParticleArray) blit);
             break;
             default:
                 throw new IllegalArgumentException("Illegal object type: " + blit.objectType);
         }
 
     }
 
 }
