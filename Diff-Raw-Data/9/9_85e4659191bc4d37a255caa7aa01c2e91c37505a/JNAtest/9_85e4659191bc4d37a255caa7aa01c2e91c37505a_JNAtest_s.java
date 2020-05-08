 package experimental;
 
 import com.sun.jna.*;
 import com.sun.jna.platform.win32.*;
 import com.sun.jna.ptr.IntByReference;
 import com.sun.jna.ptr.PointerByReference;
 import com.sun.jna.win32.StdCallLibrary;
 import com.sun.jna.win32.W32APIFunctionMapper;
 import com.sun.jna.win32.W32APITypeMapper;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author gmatoga
  */
public class JNATest {
     /**
      * Common error codes
      * http://msdn.microsoft.com/en-us/library/windows/desktop/aa378137(v=vs.85).aspx
      * <p/>
      * One or more arguments are not valid
      * <p/>
      * ? 8007007a
      */
     public static final int E_INVALIDARG = 0x80070057;
     private static Map<String, Object> OPTIONS = new HashMap<String, Object>();
 
     static {
         OPTIONS.put(Library.OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
         OPTIONS.put(Library.OPTION_FUNCTION_MAPPER,
                 W32APIFunctionMapper.UNICODE);
     }
 
     public static void main(String[] args) {
         final Kernel32b kernel32 = Kernel32b.INSTANCE;
         int pid = kernel32.GetCurrentProcessId();
 
 //        System.out.println("Current process command line: " + commandLine);
         System.out.println("PID == " + pid);
         final JFrame frame = new JFrame("hi");
         frame.add(new JButton(new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 doRun(frame);
             }
         }));
         frame.setVisible(true);
         tryReallyHardToSetWindowsTaskbarProperties(frame);
 
 
 //        final IntByReference cProps = new IntByReference();
 //        propertyStore.GetCount(cProps);
 //        System.out.println("PropCount = " + cProps);
 
 
         //enumWindows(user32);
     }
 
     public static void tryReallyHardToSetWindowsTaskbarProperties(JFrame frame) {
         final long windowID = Native.getWindowID(frame);
         final Pointer windowPointer = Native.getWindowPointer(frame);
         byte[] windowText2 = new byte[512];
         final User32 user32 = User32.INSTANCE;
         user32.GetWindowTextA(windowPointer, windowText2, 512);
 
         final Shell32 shell32 = Shell32.INSTANCE;
         final String IID_IPropertyStoreString = "{886d8eeb-8cf2-4446-8d02-cdba1dbdcf99}";
         final Guid.GUID IID_IPropertyStore = Ole32Util.getGUIDFromString(IID_IPropertyStoreString);
         PointerByReference pp = new PointerByReference();
         shell32.SHGetPropertyStoreForWindow(windowPointer, IID_IPropertyStore, pp);
 
         System.out.println(pp);
 
         final Pointer interfacePointer = pp.getValue();
         final Pointer vTablePointer = interfacePointer.getPointer(0);
         final Pointer[] vTable = new Pointer[3];
         vTablePointer.read(0, vTable, 0, 3);
         final IUnknown iUnknown = new IUnknown() {
             public int QueryInterface(Guid.GUID riid, PointerByReference ppvObject) {
                 Function f = Function.getFunction(vTable[0], Function.ALT_CONVENTION);
                 return f.invokeInt(new Object[]{interfacePointer, riid, ppvObject});
             }
 
             public int AddRef() {
                 Function f = Function.getFunction(vTable[1], Function.ALT_CONVENTION);
                 return f.invokeInt(new Object[]{interfacePointer});
             }
 
             public int Release() {
                 Function f = Function.getFunction(vTable[2], Function.ALT_CONVENTION);
                 return f.invokeInt(new Object[]{interfacePointer});
             }
 
         };
         final PointerByReference ppObj = new PointerByReference();
         final int i = iUnknown.QueryInterface(IID_IPropertyStore, ppObj);
 
         final Pointer interfacePointer2 = ppObj.getValue();
         final Pointer vTablePointer2 = interfacePointer2.getPointer(0);
         final Pointer[] vTable2 = new Pointer[8];
         vTablePointer2.read(0, vTable2, 0, 8);
 
         final IPropertyStore propertyStore = new IPropertyStore() {
 
             @Override
             public int GetCount(IntByReference cProps) {
                 Function f = Function.getFunction(vTable2[3], Function.ALT_CONVENTION);
                 return f.invokeInt(new Object[]{interfacePointer, cProps});
             }
 
             @Override
             public int GetAt() {
                 Function f = Function.getFunction(vTable2[4], Function.ALT_CONVENTION);
                 return f.invokeInt(new Object[]{interfacePointer});
             }
 
             @Override
             public int GetValue(PROPERTYKEY.ByReference propkey, PROPVARIANT.ByReference propvar) {
                 Function f = Function.getFunction(vTable2[5], Function.ALT_CONVENTION);
                 return f.invokeInt(new Object[]{interfacePointer, propkey, propvar});
             }
 
             @Override
             public int SetValue(PROPERTYKEY.ByReference propkey, PROPVARIANT.ByReference propvar) {
                 Function f = Function.getFunction(vTable2[6], Function.ALT_CONVENTION);
                 return f.invokeInt(new Object[]{interfacePointer, propkey, propvar});
             }
 
             @Override
             public int Commit() {
                 Function f = Function.getFunction(vTable2[7], Function.ALT_CONVENTION);
                 return f.invokeInt(new Object[]{interfacePointer});
             }
 
 
         };
 
         System.out.println("after first call. QueryInterFace returned " + i);
 
         iUnknown.AddRef();
         IntByReference pCount = new IntByReference();
         propertyStore.Commit();
 
         IntByReference pbr2 = new IntByReference();
         propertyStore.GetCount(pbr2);
 
 
         final PROPERTYKEY.ByReference propKeyByReference = new PROPERTYKEY.ByReference();
         final String AppUserModelID = "{9F4C2855-9F79-4B39-A8D0-E1D42DE1D5F3}";
         propKeyByReference.fmtid = Ole32Util.getGUIDFromString(AppUserModelID);
         propKeyByReference.pid = 5;
         final PROPVARIANT.ByReference propVarByReference = new PROPVARIANT.ByReference();
         //propVarByReference.val = Pointer.NULL;
         propVarByReference.vt = Variant.VT_LPWSTR;
         int result = propertyStore.GetValue(propKeyByReference, propVarByReference);
         System.out.println("ValueOfAppUserModelId: " + propVarByReference + " result of get value: " + result);
 
         System.out.println("Property Count = " + pbr2.getValue() + " variant: " + propVarByReference.vt);
         System.out.println("Window id = " + windowID + " Text: " + Native.toString(windowText2));
 
         final String propertyValueToBeSet = "Test.Experimental";
         setStringPropertyOnGUID(propertyStore, propertyValueToBeSet, 5, AppUserModelID);
 
         // relaunch command
         final WString commandLine = Kernel32b.INSTANCE.GetCommandLineW();
         // String commandLineJNLP = "C:\\Windows\\System32\\javaws.exe
         // -localfile -J-Djnlp.application.href=http://pacelibom-eurekin.rhcloud.com/app.jnlp
         // \"C:\\Users\\gmatoga\\AppData\\LocalLow\\Sun\\Java\\Deployment\\cache\\6.0\\36\\d38cde4-69ef18af";
         // String commandLineWithURL = "javaws http://pacelibom-eurekin.rhcloud.com/app.jnlp";
 
         // Manual shortcut creation.
         //
         // Need to automatically set 3 parts. Two of them present little or no problem.
         // The biggest issue is with the cached JNLP file, which lives in a WebStart cache
         // and has it's name mangled.
 
         // 1. Part. quite OK
         //
         // Reference to a JavaWS executable. Not so easy on 64 bit OS's with 32 Java installed.
         // Needs a good heuristic. The system properties doesn't hold exact pointer, but the
         // following might suffice (if javaws is in the same directory as javaw).
         //
         // need some version of it: jnlpx.jvm=C:\\Program Files\\Java\\jre7\\bin\\javaw.exe
         //
         // Update, this:
         //
         //   String binaryExecutablePath = System.getProperty("jnlpx.jvm").replace("javaw", "javaws");
         //
         // failed for 2 reasons:
         //
         //   1. Double slashes
         //   2. Program Files (x86) on 64 bit.
         //
         // Proposed solution: SHGetSpecialFolderPath with CSIDL_SYSTEMX86
         //        int nFolder;
         //
         // Update: On Windows 2008 R2 64 bit with Java 64 bit the correct folder wasn't found. Fix:
         //
         //   Platform.is64Bit() - returns the JVM's bitness
         int nFolder;
         if (Platform.is64Bit())
             nFolder = Shell32.CSIDL_SYSTEM;
         else
             nFolder = Shell32.CSIDL_SYSTEMX86;
         final String JAVA_WS_EXEC_PATH = getKnownFolderLocation(nFolder);
         String binaryExecutablePath = JAVA_WS_EXEC_PATH + "\\javaws.exe";
 
         // 2. Part. OK
         //
         // This can be set automatically - providing we know the codebase and final descriptor
         // file name. All that is or can be derived from build and deploy settings.
         String options = "-localfile -J-Djnlp.application.href=http://pacelibom-eurekin.rhcloud.com/app.jnlp";
 
         // 3. Part. Not ok.
         //
         // One board post suggested using private WebStart API to derive the mangled name.
         // Might work, though upon the first try it seemed the implementation was changed.
         // JRE 6 -> 7 might have changed the "getCachedURL" method.
         //
         // As for now - hardcoded the remote URL.
         String descriptorURL = "\"http://pacelibom-eurekin.rhcloud.com/app.jnlp\"";
         final JNLPHackToGetShortcutPath jnlpHackToGetShortcutPath = new JNLPHackToGetShortcutPath();
         final String hackedJNLPPath = jnlpHackToGetShortcutPath.getPath(ShowOffApplication.class);
         if (hackedJNLPPath != null) descriptorURL = "\"" + hackedJNLPPath + "\"";
 
         String finalCommandLine = binaryExecutablePath + " " + options + " " + descriptorURL;
 
         System.out.println(commandLine.toString().replaceAll("\\s", "\n"));
         setStringPropertyOnGUID(propertyStore, finalCommandLine, 2, "{9F4C2855-9F79-4B39-A8D0-E1D42DE1D5F3}");
 
         // 9F4C2855-9F79-4B39-A8D0-E1D42DE1D5F3
         setStringPropertyOnGUID(propertyStore, "Experimental", 4, "{9F4C2855-9F79-4B39-A8D0-E1D42DE1D5F3}");
 
 
     }
 
     private static void setStringPropertyOnGUID(IPropertyStore propertyStore, String propertyValueToBeSet, int pid, String appUserModelID) {
         int result;
         final PROPERTYKEY.ByReference propKeyByReference2 = new PROPERTYKEY.ByReference();
         propKeyByReference2.fmtid = Ole32Util.getGUIDFromString(appUserModelID);
         propKeyByReference2.pid = pid;
         final PROPVARIANT.ByReference propVarByReference2 = new PROPVARIANT.ByReference();
         propVarByReference2.val = new WString(propertyValueToBeSet);
         propVarByReference2.vt = Variant.VT_LPWSTR;
         System.out.println("argument: " + propVarByReference2);
         result = propertyStore.SetValue(propKeyByReference2, propVarByReference2);
         System.out.println("set value result: " + Integer.toHexString(result));
     }
 
     private static void doRun(JFrame frame) {
         tryReallyHardToSetWindowsTaskbarProperties(frame);
     }
 
     public static String getKnownFolderLocation(int nFolder) {
         if (com.sun.jna.Platform.isWindows()) {
             HWND hwndOwner = null;
             HANDLE hToken = null;
             int dwFlags = Shell32.SHGFP_TYPE_CURRENT;
             char[] pszPath = new char[Shell32.MAX_PATH];
             int hResult = Shell32.INSTANCE.SHGetFolderPath(hwndOwner, nFolder,
                     hToken, dwFlags, pszPath);
             if (Shell32.S_OK == hResult) {
                 String path = new String(pszPath);
                 int len = path.indexOf('\0');
                 path = path.substring(0, len);
                 return path;
             } else {
                 System.err.println("Error: " + hResult);
             }
         }
         return "";
     }
 
 
     public interface IUnknown {
         int QueryInterface(
                 Guid.GUID riid,
                 PointerByReference ppvObject);
 
         int AddRef();
 
         int Release();
 
     }
 
 
     public interface IPropertyStore {
         int Commit();
 
         int GetAt();
 
         int GetCount(IntByReference cProps);
 
         int GetValue(PROPERTYKEY.ByReference propkey, PROPVARIANT.ByReference propvar);
 
         int SetValue(PROPERTYKEY.ByReference propkey, PROPVARIANT.ByReference propvar);
     }
 
     public interface Shell32 extends StdCallLibrary {
         Shell32 INSTANCE = (Shell32) Native.loadLibrary("shell32", Shell32.class, OPTIONS);
         public static final int MAX_PATH = 260;
         // public static final int CSIDL_LOCAL_APPDATA = 0x001c;
         public static final int CSIDL_SYSTEMX86 = 0x0029;
         public static final int CSIDL_SYSTEM =  0x0025;
         public static final int SHGFP_TYPE_CURRENT = 0;
         public static final int SHGFP_TYPE_DEFAULT = 1;
         public static final int S_OK = 0;
 
         WinNT.HRESULT SHGetPropertyStoreForWindow(Pointer hWnd, Guid.GUID guid, PointerByReference ppv);
 
         /**
          * see http://msdn.microsoft.com/en-us/library/bb762181(VS.85).aspx
          * <p/>
          * HRESULT SHGetFolderPath( HWND hwndOwner, int nFolder, HANDLE hToken,
          * DWORD dwFlags, LPTSTR pszPath);
          */
         public int SHGetFolderPath(HWND hwndOwner, int nFolder, HANDLE hToken,
                                    int dwFlags, char[] pszPath);
     }
 
 
 //        typedef struct {
 //            GUID  fmtid;
 //            DWORD pid;
 //        } PROPERTYKEY;
 
     public interface Kernel32b extends Kernel32 {
         Kernel32b INSTANCE = (Kernel32b) Native.loadLibrary("kernel32", Kernel32b.class);
 
         WString GetCommandLineW();
     }
 
     //        typedef struct PROPVARIANT {
     //            VARTYPE vt;
     //            WORD    wReserved1;
     //            WORD    wReserved2;
     //            WORD    wReserved3;
     //            union {
     //                LPSTR             pszVal;
     //                LPWSTR            pwszVal;
     //            };
     //        } PROPVARIANT
 
     // VT TYPES http://msdn.microsoft.com/en-us/library/aa380072(v=vs.85).aspx
 
     // Equivalent JNA mappings
     public interface User32 extends StdCallLibrary {
         User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);
 
         // boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer arg);
 
         int GetWindowTextA(Pointer hWnd, byte[] lpString, int nMaxCount);
 
         // int GetWindowThreadProcessId(Pointer hWnd, IntByReference lpDword);
 
 //        interface WNDENUMPROC extends StdCallCallback {
 //            boolean callback(Pointer hWnd, Pointer arg);
 //        }
     }
 
 
     // Snippet from the great answer: http://stackoverflow.com/a/586917/309259
 
     public interface Ole32 extends StdCallLibrary {
         Ole32 INSTANCE = (Ole32) Native.loadLibrary("ole32", Ole32.class);
     }
 
     public static class PROPERTYKEY extends Structure {
         public Guid.GUID fmtid;
         public int pid;
 
         @Override
         protected List getFieldOrder() {
             return Arrays.asList("fmtid", "pid");
         }
 
         public static class ByReference extends PROPERTYKEY implements Structure.ByReference {
         }
     }
 
     @SuppressWarnings("UnusedDeclaration") // the r1, r2 and r3 are necessary to properly align the struct
     public static class PROPVARIANT extends Structure {
         public int vt;
         public byte r1;
         public byte r2;
         public byte r3;
         public WString val;
 
         @Override
         protected List getFieldOrder() {
             return Arrays.asList("vt", "r1", "r2", "r3", "val");
         }
 
         public static class ByReference extends PROPVARIANT implements Structure.ByReference {
         }
     }
 
     static class HANDLE extends PointerType implements NativeMapped {
     }
 
     static class HWND extends HANDLE {
     }
 
 
 }
 
