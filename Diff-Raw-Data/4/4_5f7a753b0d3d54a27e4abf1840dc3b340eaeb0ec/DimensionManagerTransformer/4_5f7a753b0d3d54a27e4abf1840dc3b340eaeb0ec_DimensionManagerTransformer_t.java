 package jk_5.nailed.coremod.transformers;
 
 import jk_5.nailed.coremod.NailedFMLPlugin;
 import jk_5.nailed.coremod.asm.ASMHelper;
 import jk_5.nailed.coremod.asm.Mapping;
 import net.minecraft.launchwrapper.IClassTransformer;
 import org.objectweb.asm.ClassWriter;
 import org.objectweb.asm.Opcodes;
 import org.objectweb.asm.tree.*;
 
 import java.util.Map;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class DimensionManagerTransformer implements IClassTransformer {
 
     @Override
     public byte[] transform(String name, String transformedName, byte[] bytes) {
         if(transformedName.equals(TransformerData.dimensionManagerDeobfuscated.get("className"))){
             if(NailedFMLPlugin.obfuscated){
                 return transformDimensionManager(bytes, TransformerData.dimensionManagerObfuscated);
             }else{
                 return transformDimensionManager(bytes, TransformerData.dimensionManagerDeobfuscated);
             }
         }else return bytes;
     }
 
     private byte[] transformDimensionManager(byte[] bytes, Map<String, String> data){
         ClassNode cnode = ASMHelper.createClassNode(bytes, 0);
         MethodNode mnode = ASMHelper.findMethod(new Mapping(data.get("className").replace('.', '/'), data.get("targetMethodName"), data.get("targetMethodSig")), cnode);
 
         int offset = 0;
         int numOfNews = 0;
         while(numOfNews != 2){
             while(mnode.instructions.get(offset).getOpcode() != Opcodes.NEW) offset ++;
             offset ++;
             numOfNews ++;
         }
 
         while(mnode.instructions.get(offset).getOpcode() != Opcodes.RETURN) offset ++;
         while(mnode.instructions.get(offset).getOpcode() != Opcodes.ASTORE) offset ++;
 
         InsnList list = new InsnList();
         list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "jk_5/nailed/map/NailedMapLoader", "instance", "()Ljk_5/nailed/map/NailedMapLoader;"));
         list.add(new VarInsnNode(Opcodes.ILOAD, 0));
         list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "jk_5/nailed/map/NailedMapLoader", "getMap", "(I)Ljk_5/nailed/api/map/Map;"));
         list.add(new VarInsnNode(Opcodes.ASTORE, 7));
 
         mnode.instructions.insert(mnode.instructions.get(offset + 2), list);
         list.clear();
 
         while(mnode.instructions.get(offset).getOpcode() != Opcodes.ALOAD) offset ++;
         ((VarInsnNode) mnode.instructions.get(offset)).var = 2;
         ((MethodInsnNode) mnode.instructions.get(offset + 1)).owner = data.get("minecraftServerName");
         ((MethodInsnNode) mnode.instructions.get(offset + 1)).name = data.get("getSaveFormatName");
         ((MethodInsnNode) mnode.instructions.get(offset + 1)).desc = data.get("getSaveFormatSig");
         list.add(new VarInsnNode(Opcodes.ALOAD, 7));
        list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "jk_5/nailed/api/map/Map", "getSaveFileName", "()Ljava/lang/String;"));
         list.add(new InsnNode(Opcodes.ICONST_1));
         list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, data.get("iSaveFormatName"), data.get("getSaveLoaderName"), data.get("getSaveLoaderSig")));
         mnode.instructions.insert(mnode.instructions.get(offset + 1), list);
         list.clear();
 
         while(mnode.instructions.get(offset).getOpcode() != Opcodes.DUP) offset ++;
         while(mnode.instructions.get(offset).getOpcode() != Opcodes.IFNE) offset ++;
         while(mnode.instructions.get(offset).getOpcode() != Opcodes.NEW) offset ++;
 
         TypeInsnNode typeNode = (TypeInsnNode) mnode.instructions.get(offset);
         typeNode.desc = data.get("worldServerClass");
 
         while(mnode.instructions.get(offset).getOpcode() != Opcodes.ALOAD) offset ++;
         offset += 2;
         ((VarInsnNode) mnode.instructions.get(offset)).var = 7;
        ((MethodInsnNode) mnode.instructions.get(offset + 1)).setOpcode(Opcodes.INVOKEINTERFACE);
         ((MethodInsnNode) mnode.instructions.get(offset + 1)).owner = "jk_5/nailed/api/map/Map";
         ((MethodInsnNode) mnode.instructions.get(offset + 1)).name = "getSaveFileName";
         ((MethodInsnNode) mnode.instructions.get(offset + 1)).desc = "()Ljava/lang/String;";
         mnode.instructions.remove(mnode.instructions.get(offset + 2));
 
         while(mnode.instructions.get(offset).getOpcode() != Opcodes.ILOAD) offset ++;
         offset += 2;
         mnode.instructions.remove(mnode.instructions.get(offset));
 
         while(mnode.instructions.get(offset).getOpcode() != Opcodes.INVOKESPECIAL) offset ++;
         MethodInsnNode node = (MethodInsnNode) mnode.instructions.get(offset);
         node.owner = data.get("worldServerClass");
         node.desc = data.get("worldServerConstructorSig");
 
         return ASMHelper.createBytes(cnode, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
     }
 }
