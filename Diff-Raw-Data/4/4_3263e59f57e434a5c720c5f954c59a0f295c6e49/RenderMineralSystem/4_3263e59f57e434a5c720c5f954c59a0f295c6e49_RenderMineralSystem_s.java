 package com.kingx.dungeons.engine.system;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.kingx.artemis.Aspect;
 import com.kingx.artemis.ComponentMapper;
 import com.kingx.artemis.Entity;
 import com.kingx.artemis.annotations.Mapper;
 import com.kingx.artemis.systems.EntityProcessingSystem;
 import com.kingx.dungeons.App;
 import com.kingx.dungeons.engine.component.FollowCameraComponent;
 import com.kingx.dungeons.engine.component.ShadowComponent;
 import com.kingx.dungeons.engine.component.dynamic.MoveComponent;
 import com.kingx.dungeons.engine.component.dynamic.PositionComponent;
 import com.kingx.dungeons.graphics.Shader;
 import com.kingx.dungeons.graphics.cube.CubeRegion;
 import com.kingx.dungeons.graphics.cube.CubeRenderer;
 
 public class RenderMineralSystem extends EntityProcessingSystem {
     @Mapper
     ComponentMapper<PositionComponent> pm;
     @Mapper
     ComponentMapper<MoveComponent> mm;
     @Mapper
     ComponentMapper<ShadowComponent> sm;
 
     private final FollowCameraComponent camera;
 
     private final ShaderProgram shadowProjectShader;
     private CubeRenderer batchRender;
     private final ArrayList<CubeRegion> blocks;
 
     public RenderMineralSystem(FollowCameraComponent camera, ArrayList<CubeRegion> blocks) {
         super(Aspect.getAspectForAll(PositionComponent.class, ShadowComponent.class));
         this.camera = camera;
         this.blocks = blocks;
 
         shadowProjectShader = Shader.getShader("mineral");
 
     }
 
     @Override
     protected void begin() {
         if (batchRender == null) {
             batchRender = new CubeRenderer();
         }
     }
 
     @Override
     protected void end() {
     }
 
     @Override
     protected void process(Entity e) {
         batchRender.setShader(shadowProjectShader);
         batchRender.begin();
         shadowProjectShader.setUniformMatrix("u_projTrans", camera.getCamera().combined);
         batchRender.enableBlending();
        batchRender.draw(blocks.get(App.getPrevView()), false);
        batchRender.draw(blocks.get(App.getCurrentView()), true);
        batchRender.draw(blocks.get(App.getNextView()), false);
         batchRender.end();
 
     }
 
 }
