 package com.github.vspiewak.mowitnow.mower.config;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.github.vspiewak.mowitnow.mower.api.Engine;
 import com.github.vspiewak.mowitnow.mower.base.Position;
 import com.github.vspiewak.mowitnow.mower.domain.Lawn;
 import com.github.vspiewak.mowitnow.mower.domain.Mower;
 import com.github.vspiewak.mowitnow.mower.domain.XEngine;
 
 /**
  * This class represent an application configuration.
  * 
  * @author Vincent Spiewak
  * @since 1.0
  */
 public class Config {
 
    /* CRLF evilness... */
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
 
    private Position lawnTopRightCorner;
    private List<MowerConfig> mowerConfigList;
 
    public Config() {
       mowerConfigList = new ArrayList<MowerConfig>();
    }
 
    public Position getLawnTopRightCorner() {
       return lawnTopRightCorner;
    }
 
    public void setLawnTopRightCorner(Position lawnTopRightCorner) {
       this.lawnTopRightCorner = lawnTopRightCorner;
    }
 
    public List<MowerConfig> getMowerConfig() {
       return mowerConfigList;
    }
 
    public void addMowerConfig(MowerConfig config) {
       this.mowerConfigList.add(config);
    }
 
    public String execute() {
 
       /* hold result */
       StringBuilder result = new StringBuilder();
 
       /* init engine */
       Engine engine = XEngine.get();
       engine.init(new Lawn(getLawnTopRightCorner()));
 
       List<Mower> mowers = new ArrayList<Mower>();
 
       /* for each mowers */
       for (MowerConfig mowerConfig : mowerConfigList) {
 
          /* init mower */
         Mower mower = new Mower(mowerConfig.getInitialPosition(),
               mowerConfig.getInitialOrientation());
 
          /* attach to the engine */
          engine.attach(mower);
 
          /* adding to the list */
          mowers.add(mower);
 
          /* run every commands */
          for (MowerCommand command : mowerConfig.getCommands()) {
             command.execute(mower, command);
          }
 
       }
 
       /* collect each mower.print() */
       if (!mowers.isEmpty()) {
          for (Mower mower : mowers) {
             result.append(mower.print()).append(LINE_SEPARATOR);
          }
          /* remove last breakline */
          result.setLength(result.length() - LINE_SEPARATOR.length());
       }
 
       return result.toString();
 
    }
 
 }
