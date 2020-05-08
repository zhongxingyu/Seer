 package com.greenteam.huntjumper.shaders;
 
 import com.greenteam.huntjumper.match.Camera;
 import com.greenteam.huntjumper.utils.Point;
 import org.lwjgl.opengl.ContextCapabilities;
 import org.lwjgl.opengl.GLContext;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.opengl.shader.ShaderProgram;
 
 import java.util.EnumMap;
 import java.util.Map;
 
 /**
  * User: GreenTea Date: 11.08.12 Time: 16:18
  */
 public class ShadersSystem
 {
    private static ShadersSystem instance = new ShadersSystem();
 
    private Map<Shader, ShaderProgram> programs = new EnumMap<Shader, ShaderProgram>(Shader.class);
    private boolean supported;
 
    public static ShadersSystem getInstance()
    {
       return instance;
    }
 
    private ShadersSystem()
    {
    }
 
    public ShaderProgram getProgram(Shader shaderKey)
    {
       return programs.get(shaderKey);
    }
 
    public boolean isSupported()
    {
       return supported;
    }
 
    public void init()
    {
      supported = GLContext.getCapabilities().OpenGL15;
       if (!supported)
       {
         System.out.println("WARNING: OpenGL version 1.5 is not supported. Disable using shaders.");
       }
       else
       {
          for (Shader s : Shader.values())
          {
             try
             {
                ShaderProgram program = ShaderProgram.loadProgram(s.getPathToVertexShader(),
                        s.getPathToPixelShader());
                programs.put(s, program);
             }
             catch (SlickException e)
             {
                throw new RuntimeException(e);
             }
          }
       }
    }
 
    public void setResolution(ShaderProgram program, float width, float height)
    {
       program.setUniform2f("resolution", width, height);
    }
 
    public void setPosition(ShaderProgram program, float x, float y)
    {
       program.setUniform2f("position", x,
               Camera.getCamera().getViewHeight()- y);
    }
 
    public void setPosition(ShaderProgram program, Point p)
    {
       setPosition(program, p.getX(), p.getY());
    }
 
    public void setColor(ShaderProgram program, Color c)
    {
       program.setUniform3f("color", c.r, c.g, c.b);
    }
 }
