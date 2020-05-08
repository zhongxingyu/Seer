 /*
 *This is a part of libsimplefft
 *
 * Copyright (C) 2012  Kevin Krüger (kkevin@gmx.net)
 * 
 * libsimplefft is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * libsimplefft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with libsimplefft; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
 
 package in.waslos.kneo.libsimplefft;
 
 public class FFTProcessor{
 	public static final byte FFT_MODE_NORMAL  = 0;
 	public static final byte FFT_MODE_INVERSE = 1;
 
 	public static final byte CPLX_TYPE_SP  = 0;
 	public static final byte CPLX_TYPE_DP  = 1;
 	public static final byte CPLX_TYPE_INT = 2;
 	
 	static{
 		System.loadLibrary("simplefft");
 		System.loadLibrary("simplefft4j");
 	}
 
 	private static native int createFFTContext(int samples, byte mode, byte type);
 	private static native void destroyFFTContext(int handle);
 
 	private static native void performFFTf(int handle, float[]  re, float[]  im);
 	private static native void performFFTd(int handle, double[] re, double[] im);
 	private static native void performFFTi(int handle, short[]  re, short[]  im);
 	
 	public static native int findFFTHandle(int type,int size,int mode);
 	
 	public static native int createFastConvolutionContext(int samples, float[] kernel_re,float[] kernel_im);
 	public static native int createFastConvolutionContext(int samples, double[] kernel_re,double[] kernel_im);
 	public static native int createFastConvolutionContext(int samples, short[] kernel_re,short[] kernel_im);
 	
 	public static native int getConvolutionSize(int handle);
 	
 	public static native int getTransformedConvolutionKernel(int handle, short[] kernel_re,short[] kernel_im);
 	public static native int getTransformedConvolutionKernel(int handle, float[] kernel_re,float[] kernel_im);
 	public static native int getTransformedConvolutionKernel(int handle, double[] kernel_re,double[] kernel_im);
 	
 	public static native void performFastConvolution(int handle, float[]  signal_re,float[]  signal_im);
 	public static native void performFastConvolution(int handle, double[] signal_re,double[] signal_im);
 	public static native void performFastConvolution(int handle, short[]  signal_re, short[] signal_im);
 	
 	public static native int destroyFastConvolution(int handle);
 
 	public static int initializeFFT(int samples,byte mode, byte type){
 		return FFTProcessor.createFFTContext(samples,mode,type);
 	}
 
 	public static void destroyFFT(int handle){
 		FFTProcessor.destroyFFTContext(handle);
 	}
 
 	public static void performFFT(int handle, float[] re, float[] im){
 		performFFTf(handle,re,im);
 	}
 
 	public static void performFFT(int handle, double[] re, double[] im){
 		performFFTd(handle,re,im);
 	}
 
 	public static void performFFT(int handle, short[] re, short[] im){
 		performFFTi(handle,re,im);
 	}
 	
 	public static void main(String[] argV){
 		System.err.println("Testing INT:");
 		test_int();
 		System.err.println("Testing FLOAT:");
 		test_float();
 		System.err.println("Testing DOUBLE:");
 		test_double();
 	}
 	
 	private static void test_int(){
 		//System.loadLibrary("simplefft");
 		int size = 16;
 		
 		short[] kernel_re = {-1,0,1};
 		short[] kernel_im = {0, 0,0};
 		
 		short[] re = new short[size];
 		short[] im = new short[size];
 	
 		int handle    = FFTProcessor.initializeFFT(size, FFTProcessor.FFT_MODE_NORMAL,  FFTProcessor.CPLX_TYPE_INT);
 		int ihandle   = FFTProcessor.initializeFFT(size, FFTProcessor.FFT_MODE_INVERSE, FFTProcessor.CPLX_TYPE_INT);
 		int conhandle = FFTProcessor.createFastConvolutionContext(size,kernel_re,kernel_im);
 		
 		System.out.println("handle retrieved : "+handle+" inverse : "+ihandle+" convolution :"+conhandle);
 
		//for(int i = 0;i<size;i++){
		//	re[i] = (short)(i);
		//}

        re[3] = 10;
        re[4] = 10;
 		
 		long time = System.currentTimeMillis();
 		performFFT(handle,re,im);
 		time = System.currentTimeMillis() - time;
 		System.out.println("runtime : "+time+"ms");
 		System.err.println("FFT:");
 		for(int i = 0;i<size;i++){
 			System.out.printf("%d + %d * i\n",re[i],im[i]);
 		}
 		
 		System.err.println("Inverse FFT:");
 		performFFT(ihandle,re,im);
 		
 		for(int i = 0;i<size;i++){
 			System.out.printf("%d + %d * i\n",re[i],im[i]);
 		}
 		
 		System.err.println("Convolution:");
 		performFastConvolution(conhandle,re,im);
 		
 		for(int i = 0;i<size;i++){
 			System.out.printf("%d + %d * i\n",re[i],im[i]);
 		}
 		
 		//int ihandle = FFTProcessor.initializeFFT(size,FFTProcessor.FFT_MODE_INVERSE,FFTProcessor.CPLX_TYPE_INT);
 		System.err.println("deleting Convolution...");
 		destroyFastConvolution(conhandle);
 		System.err.println("deleting FFT...");
 		destroyFFT(handle);
 		System.err.println("deleting IFFT...");
 		destroyFFT(ihandle);
 	}
 	
 	private static void test_float(){
 		//System.loadLibrary("simplefft");
 		int size = 16;
 		
 		float[] kernel_re = {-1,0,1};
 		float[] kernel_im = {0, 0,0};
 		
 		float[] re = new float[size];
 		float[] im = new float[size];
 	
 		int handle    = FFTProcessor.initializeFFT(size, FFTProcessor.FFT_MODE_NORMAL,  FFTProcessor.CPLX_TYPE_SP);
 		int ihandle   = FFTProcessor.initializeFFT(size, FFTProcessor.FFT_MODE_INVERSE, FFTProcessor.CPLX_TYPE_SP);
 		int conhandle = FFTProcessor.createFastConvolutionContext(size,kernel_re,kernel_im);
 		
 		System.out.println("handle retrieved : "+handle+" inverse : "+ihandle+" convolution :"+conhandle);
 
 		for(int i = 0;i<size;i++){
 			re[i] = (short)(i);
 		}
 		
 		long time = System.currentTimeMillis();
 		performFFT(handle,re,im);
 		time = System.currentTimeMillis() - time;
 		System.out.println("runtime : "+time+"ms");
 		System.err.println("FFT:");
 		for(int i = 0;i<size;i++){
 			System.out.printf("%f + %f * i\n",re[i],im[i]);
 		}
 		
 		System.err.println("Inverse FFT:");
 		performFFT(ihandle,re,im);
 		
 		for(int i = 0;i<size;i++){
 			System.out.printf("%f + %f * i\n",re[i],im[i]);
 		}
 		
 		System.err.println("Convolution:");
 		
 		performFastConvolution(conhandle,re,im);
 		
 		for(int i = 0;i<size;i++){
 			System.out.printf("%f + %f * i\n",re[i],im[i]);
 		}
 		
 		//int ihandle = FFTProcessor.initializeFFT(size,FFTProcessor.FFT_MODE_INVERSE,FFTProcessor.CPLX_TYPE_INT);
 		System.err.println("deleting Convolution...");
 		destroyFastConvolution(conhandle);
 		System.err.println("deleting FFT...");
 		destroyFFT(handle);
 		System.err.println("deleting IFFT...");
 		destroyFFT(ihandle);
 	}
 	
 	private static void test_double(){
 		//System.loadLibrary("simplefft");
 		int size = 16;
 		
 		double[] kernel_re = {-1,0,1};
 		double[] kernel_im = {0, 0,0};
 		
 		double[] re = new double[size];
 		double[] im = new double[size];
 	
 		int handle    = FFTProcessor.initializeFFT(size, FFTProcessor.FFT_MODE_NORMAL,  FFTProcessor.CPLX_TYPE_DP);
 		int ihandle   = FFTProcessor.initializeFFT(size, FFTProcessor.FFT_MODE_INVERSE, FFTProcessor.CPLX_TYPE_DP);
 		int conhandle = FFTProcessor.createFastConvolutionContext(size,kernel_re,kernel_im);
 		
 		System.out.println("handle retrieved : "+handle+" inverse : "+ihandle+" convolution :"+conhandle);
 
 		for(int i = 0;i<size;i++){
 			re[i] = (short)(i);
 		}
 		
 		long time = System.currentTimeMillis();
 		performFFT(handle,re,im);
 		time = System.currentTimeMillis() - time;
 		System.out.println("runtime : "+time+"ms");
 		System.err.println("FFT:");
 		for(int i = 0;i<size;i++){
 			System.out.printf("%f + %f * i\n",re[i],im[i]);
 		}
 		
 		System.err.println("Inverse FFT:");
 		performFFT(ihandle,re,im);
 		
 		for(int i = 0;i<size;i++){
 			System.out.printf("%f + %f * i\n",re[i],im[i]);
 		}
 		
 		System.err.println("Convolution:");
 		
 		performFastConvolution(conhandle,re,im);
 		
 		for(int i = 0;i<size;i++){
 			System.out.printf("%f + %f * i\n",re[i],im[i]);
 		}
 		
 		System.err.println("deleting Convolution...");
 		destroyFastConvolution(conhandle);
 		System.err.println("deleting FFT...");
 		destroyFFT(handle);
 		System.err.println("deleting IFFT...");
 		destroyFFT(ihandle);
 		
 		//int ihandle = FFTProcessor.initializeFFT(size,FFTProcessor.FFT_MODE_INVERSE,FFTProcessor.CPLX_TYPE_INT);
 	}
 }
 
 
