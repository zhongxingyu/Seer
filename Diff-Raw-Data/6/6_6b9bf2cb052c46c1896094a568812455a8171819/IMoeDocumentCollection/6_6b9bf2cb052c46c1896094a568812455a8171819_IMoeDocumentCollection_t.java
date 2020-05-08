 
 package org.oha7.mol.moe;
 
 import org.oha7.dispdriver.annotations.Dispid;
 import org.oha7.dispdriver.interfaces.IUnknown;
 
 import org.oha7.mol.moe.IMoeDocument;
 import org.oha7.dispdriver.interfaces.IEnumVARIANT;
 
 public interface IMoeDocumentCollection extends IUnknown {
 
     @Dispid(1)
 	int getCount();
 
     @Dispid(2)
 	IMoeDocument getActiveDoc();
 
     @Dispid(-4)
     IEnumVARIANT _NewEnum( );
 
     @Dispid(0)
     IMoeDocument Item( Object i);
 
     @Dispid(3)
     IMoeDocument New( );
 
     @Dispid(4)
     IMoeDocument Open( String fPath);
 
     @Dispid(5)
     IMoeDocument OpenUTF8( String fPath);
 
     @Dispid(6)
     IMoeDocument OpenDir( String dir);
 
     @Dispid(7)
     IMoeDocument OpenHexEditor( String f, boolean vbReadOnly);
 
     @Dispid(8)
     IMoeDocument OpenHtmlFrame( String f);
 
     @Dispid(9)
     IMoeDocument OpenUserForm( String pathname);
 
     @Dispid(10)
     IMoeDocument NewUserForm( );
 
     @Dispid(11)
     void Activate( Object i);
 
     @Dispid(12)
     void SaveAll( );
 
     @Dispid(13)
     void CloseAll( );
 
     @Dispid(14)
     void Remove( Object index);
 
     @Dispid(15)
     void Move( Object what, Object to);
 
    @Dispid(16)
    IMoeDocument NewRTFDocument( );

    @Dispid(17)
    IMoeDocument OpenTailDocument( String fPath);

 
 }
