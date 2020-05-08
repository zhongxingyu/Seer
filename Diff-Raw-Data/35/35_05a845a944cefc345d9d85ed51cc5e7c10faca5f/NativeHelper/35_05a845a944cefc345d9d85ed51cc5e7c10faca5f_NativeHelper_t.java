 package com.starlon.starvisuals;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 
 
 
 public class NativeHelper
 {
 
     // VisInput native calls
     public static native int inputCount();
     public static native int inputGetCurrent();
     public static native boolean inputSetCurrent(int index, boolean now);
     public static native boolean inputSetCurrentByName(String name, boolean now);
     public static native String inputGetName(int index);
     public static native String inputGetLongName(int index);
     public static native String inputGetAuthor(int index);
     public static native String inputGetVersion(int index);
     public static native String inputGetAbout(int index);
     public static native String inputGetHelp(int index);
     public static native String inputGetLicense(int index);
     public static native int inputParamGetCount();
     public static native String inputParamGetType(int index);
     public static native String inputParamGetName(int index);
     public static native String inputParamGetString(String name);
     public static native boolean inputParamSetString(String name, String newstring);
     public static native int inputParamGetInteger(String name);
     public static native boolean inputParamSetInteger(String name, int newint);
     public static native float inputParamGetFloat(String name);
     public static native boolean inputParamSetFloat(String name, float newfloat);
     public static native double inputParamGetDouble(String name);
     public static native boolean inputParamSetDouble(String name, double newdouble);
 
     // VisMorph native calls
     public static native int morphCount();
     public static native int morphGetCurrent();
     public static native boolean morphSetCurrent(int index, boolean now);
     public static native boolean morphSetCurrentByName(String name, boolean now);
     public static native String morphGetName(int index);
     public static native String morphGetLongName(int index);
     public static native String morphGetAuthor(int index);
     public static native String morphGetVersion(int index);
     public static native String morphGetAbout(int index);
     public static native String morphGetHelp(int index);
     public static native String morphGetLicense(int index);
     public static native int morphParamGetCount();
     public static native String morphParamGetType(int index);
     public static native String morphParamGetName(int index);
     public static native String morphParamGetString(String name);
     public static native boolean morphParamSetString(String name, String newstring);
     public static native int morphParamGetInteger(String name);
     public static native boolean morphParamSetInteger(String name, int newint);
     public static native float morphParamGetFloat(String name);
     public static native boolean morphParamSetFloat(String name, float newfloat);
     public static native double morphParamGetDouble(String name);
     public static native boolean morphParamSetDouble(String name, double newdouble);
 
     // VisActor native calls
     public static native int actorCount();
     public static native int actorGetCurrent();
     public static native boolean actorSetCurrent(int index, boolean now);
     public static native boolean actorSetCurrentByName(String name, boolean now);
     public static native String actorGetName(int index);
     public static native String actorGetLongName(int index);
     public static native String actorGetAuthor(int index);
     public static native String actorGetVersion(int index);
     public static native String actorGetAbout(int index);
     public static native String actorGetHelp(int index);
     public static native String actorGetLicense(int index);
     public static native int actorParamGetCount();
     public static native String actorParamGetType(int index);
     public static native String actorParamGetName(int index);
     public static native String actorParamGetString(String name);
     public static native boolean actorParamSetString(String name, String newstring);
     public static native int actorParamGetInteger(String name);
     public static native boolean actorParamSetInteger(String name, int newint);
     public static native float actorParamGetFloat(String name);
     public static native boolean actorParamSetFloat(String name, float newfloat);
     public static native double actorParamGetDouble(String name);
     public static native boolean actorParamSetDouble(String name, double newdouble);
 
     /* Switch plugins */
     public static native int cycleInput(int prev);
     public static native int cycleMorph(int prev);
     public static native int cycleActor(int prev);
    public static native void finalizeSwitch(int prev);
     public static native void setMorphStyle(boolean style);
     public static native void updatePlugins();
 
     /* Audio input */
     public static native void resizePCM(int size, int rate, int channels, int encoding);
     public static native void uploadAudio(short[] data);
     public static native void newSong();
     public static native int getBPM();
     public static native int getBPMConfidence();
     public static native boolean isBeat();
 
     /* Mouse and key events */
     public static native void mouseMotion(float x, float y);
     public static native void mouseButton(int button, float x, float y);
 
     /* Resize vis */
     public static native void screenResize(int w, int h);
 
     /* Init and quit libvisual */
     public static native void visualsQuit();
     public static native void initApp(int w, int h);
 
     /* Draw and render vis */
     public static native boolean render(Bitmap  bitmap, boolean do_swap);
 
     /** constructor */
     public NativeHelper(Context c)
     {
     }
 }
