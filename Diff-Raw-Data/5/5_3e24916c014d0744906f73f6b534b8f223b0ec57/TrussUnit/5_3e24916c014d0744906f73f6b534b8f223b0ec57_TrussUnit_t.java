 
 package fermanext.trussunit;
 
 import java.util.Vector;
 
 class TrussNode {};
 class TrussPivot {};
 class DoubleSize {};
 class DoublePoint {};
 class LoadCases {};
 class TrussMaterial {};
 class NodeIndexOutOfBoundException extends Exception {};
 
 public class TrussUnit
 {
     public native TrussNode findNodeByCoord ( DoublePoint p );
     public native TrussNode findNodeByNumber ( int num );
     public native TrussPivot findPivotByNumber ( int num );
     public native TrussPivot findPivotByNodes ( TrussNode n1, TrussNode n2 );
     public native Vector findAdjoiningPivots ( TrussNode node );
     public native TrussNode createNode ( DoublePoint p );
     public native TrussNode createNode ( double x, double y );
     public native TrussPivot createPivot ( int firstNodeIndex, 
                                            int lastNodeIndex ) 
         throws NodeIndexOutOfBoundException;
 
     public native TrussPivot createPivot ( TrussNode first, TrussNode last );
     public native int countNodes ();
     public native int countPivots ();
     public native Vector getNodeList ();
     public native Vector getPivotList ();
     public native DoubleSize getTrussAreaSize ();
     public native LoadCases getLoadCases ();
     public native TrussMaterial getMaterial ();
     public native void setMaterial ( TrussMaterial mat );

    static { 
        System.loadLibrary("JavaPluginLoader.ldr");
    }

 };
 
