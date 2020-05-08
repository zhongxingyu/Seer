 package com.checkers.server.services.referee;
 
 import com.checkers.server.exceptions.CheckersException;
 import com.checkers.server.services.referee.graph.*;
 import com.checkers.server.services.referee.graph.coords.Coords;
 import com.checkers.server.services.referee.graph.coords.RussianCoords;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  *
  *
  * @author Pavel Kuchin
  */
 public class RussianGraphRefereeImpl implements Referee {
 
     CheckersGraph<RussianCoords> graph;
     Map<RussianCoords, Figure> white;
     Map<RussianCoords, Figure> black;
 
     /// steps without fight
     private int counter1;
 
     /// if one player has three kings another one king - steps without fight
     private int counter2;
 
     /// if one player has three kings another one king on big way - steps without fight
     private int counter3;
 
     /// One player has two king or one king and one check another player has one king - steps without fight
     private int counter4;
 
     public RussianGraphRefereeImpl() throws CheckersException {
         Cell<RussianCoords> cell;
         Figure  figure;
 
         graph = new CheckersGraph<RussianCoords>();
         white = new HashMap<RussianCoords, Figure>();
         black = new HashMap<RussianCoords, Figure>();
 
         for(int i = 0; i < 8; i++){
             for(int j = 0; j < 4; j++){
                 if ((i + 1) % 2 == 0) {
                     cell = graph.newCell(new RussianCoords(i + 1, 2 + (j * 2)));
 
                     if(i > 0){
                         Cell c1 = graph.getCell(new RussianCoords(i, 1 + (j * 2)));
                         cell.setLeftDown(c1);
                         c1.setRightUp(cell);
                         if(j < 3){
                             Cell c2 = graph.getCell(new RussianCoords(i, 3 + (j * 2)));
                             cell.setRightDown(c2);
                             c2.setLeftUp(cell);
                         }
                     }
                 } else {
                     cell = graph.newCell(new RussianCoords(i + 1, 1 + (j * 2)));
 
                     if(i > 0){
                         if(j > 0){
                             Cell c3 = graph.getCell(new RussianCoords(i, 0 + (j * 2)));
                             cell.setLeftDown(c3);
                             c3.setRightUp(cell);
                         }
                         Cell c4 = graph.getCell(new RussianCoords(i, 2 + (j * 2)));
                         cell.setRightDown(c4);
                         c4.setLeftUp(cell);
                     }
                 }
 
                 if(i < 3){
                     figure = new Figure();
                     figure.setType(FigureType.CHECKER);
                     figure.setColor(FigureColor.WHITE);
 
                     graph.newFigure(cell.getCoords(), figure);
                     white.put(cell.getCoords(), figure);
                 }else if(i > 4){
                     figure = new Figure();
                     figure.setType(FigureType.CHECKER);
                     figure.setColor(FigureColor.BLACK);
 
                     graph.newFigure(cell.getCoords(), figure);
                     black.put(cell.getCoords(), figure);
                 }
             }
         }
     }
 
     private Integer getDistance(Coords from, Coords to){
         if(from == null || to == null){
             return null;
         }
 
         return Math.abs(from.getX() - to.getX());
     }
 
     private Boolean canCheckerFight(Figure<RussianCoords> figure){
 
         Cell<RussianCoords> cell;
 
         cell = figure.getCell().getLeftUp();
 
         // Check opponent figure near me
         if(cell != null &&                                                  //Cell exist
                 cell.getFigure() != null &&                                 //Cell has figure
                 !cell.getFigure().getColor().equals(figure.getColor()) &&   //It is opponent figure
                 !cell.getFigure().getThreatened()){                         //This figure hasn't threatened yet
             //Check is it empty cell over opponent figure
             if(cell.getLeftUp() != null && cell.getLeftUp().getFigure() == null){
                 return true;
             }
         }
 
         cell = figure.getCell().getLeftDown();
 
         // Check opponent figure near me
         if(cell != null &&                                                  //Cell exist
                 cell.getFigure() != null &&                                 //Cell has figure
                 !cell.getFigure().getColor().equals(figure.getColor()) &&   //It is opponent figure
                 !cell.getFigure().getThreatened()){                         //This figure hasn't threatened yet
             //Check is it empty cell over opponent figure
             if(cell.getLeftDown() != null && cell.getLeftDown().getFigure() == null){
                 return true;
             }
         }
 
         cell = figure.getCell().getRightDown();
 
         // Check opponent figure near me
         if(cell != null &&                                                  //Cell exist
                 cell.getFigure() != null &&                                 //Cell has figure
                 !cell.getFigure().getColor().equals(figure.getColor()) &&   //It is opponent figure
                 !cell.getFigure().getThreatened()){                         //This figure hasn't threatened yet
             //Check is it empty cell over opponent figure
             if(cell.getRightDown() != null && cell.getRightDown().getFigure() == null){
                 return true;
             }
         }
 
         cell = figure.getCell().getRightUp();
 
         // Check opponent figure near me
         if(cell != null &&                                                  //Cell exist
                 cell.getFigure() != null &&                                 //Cell has figure
                 !cell.getFigure().getColor().equals(figure.getColor()) &&   //It is opponent figure
                 !cell.getFigure().getThreatened()){                         //This figure hasn't threatened yet
             //Check is it empty cell over opponent figure
             if(cell.getRightUp() != null && cell.getRightUp().getFigure() == null){
                 return true;
             }
         }
 
         return false;
     }
 
     private Boolean canKingFightAfterFight(Figure figure){
         Boolean result = canKingFight(figure);
 
         Cell<RussianCoords> l;
         Cell<RussianCoords> sl1;
         Cell<RussianCoords> sl2;
 
         if(result == false){
             if(figure.getLeftDownFigure() != null && figure.getLeftDownFigure().getThreatened()){
                 // if from left down to right up
                 //  then get all right upper cell in the loop
                 //      and from every check all right down and left up
                 //        if found our figure then out of the loop
                 //        if found their figure then return true
 
                 // current position has been checked in the canKingFight function,
                 // so we just skip it.
                 l = figure.getCell();
 
                 while((l = l.getRightUp()) != null){
                    if(l.getFigure() != null){
                        if(!l.getFigure().getColor().equals(figure.getColor())){
                            return true;
                        } else{
                            return false;
                        }
                    } else{
                        sl1 = l;
                        while((sl1 = sl1.getRightDown()) != null){
                            if(!sl1.getFigure().getColor().equals(figure.getColor())){
                                if(sl1.getRightDown() != null
                                        && sl1.getRightDown().getFigure() == null){
                                    return true;
                                }
                            } else{
                                break;
                            }
                        }
 
                        sl2 = l;
                        while((sl2 = sl2.getLeftUp()) != null){
                            if(!sl2.getFigure().getColor().equals(figure.getColor())){
                                if(sl2.getLeftUp() != null
                                        && sl2.getLeftUp().getFigure() == null){
                                    return true;
                                }
                            } else{
                                break;
                            }
                        }
                    }
                 }
             }
             if(figure.getLeftUpFigure() != null && figure.getLeftUpFigure().getThreatened()){
                 // if from left up to right down
                 //  then get all right nether cell in the loop
                 //      and from every check all right up and left down
                 //        if found our figure then out of the loop
                 //        if found their figure then return true
 
                 // current position has been checked in the canKingFight function,
                 // so we just skip it.
                 l = figure.getCell();
 
                 while((l = l.getRightDown()) != null){
                     if(l.getFigure() != null){
                         if(!l.getFigure().getColor().equals(figure.getColor())){
                             return true;
                         } else{
                             return false;
                         }
                     } else{
                         sl1 = l;
                         while((sl1 = sl1.getRightUp()) != null){
                             if(!sl1.getFigure().getColor().equals(figure.getColor())){
                                 if(sl1.getRightDown() != null
                                         && sl1.getRightDown().getFigure() == null){
                                     return true;
                                 }
                             } else{
                                 break;
                             }
                         }
 
                         sl2 = l;
                         while((sl2 = sl2.getLeftDown()) != null){
                             if(!sl2.getFigure().getColor().equals(figure.getColor())){
                                 if(sl2.getLeftUp() != null
                                         && sl2.getLeftUp().getFigure() == null){
                                     return true;
                                 }
                             } else{
                                 break;
                             }
                         }
                     }
                 }
             }
             if(figure.getRightDownFigure() != null && figure.getRightDownFigure().getThreatened()){
                 // if from right down to left up
                 //  then get all left upper cell in the loop
                 //      and from every check all right up and left down
                 //        if found our figure then out of the loop
                 //        if found their figure then return true
 
                 // current position has been checked in the canKingFight function,
                 // so we just skip it.
                 l = figure.getCell();
 
                 while((l = l.getLeftUp()) != null){
                     if(l.getFigure() != null){
                         if(!l.getFigure().getColor().equals(figure.getColor())){
                             return true;
                         } else{
                             return false;
                         }
                     } else{
                         sl1 = l;
                         while((sl1 = sl1.getRightUp()) != null){
                             if(!sl1.getFigure().getColor().equals(figure.getColor())){
                                 if(sl1.getRightDown() != null
                                         && sl1.getRightDown().getFigure() == null){
                                     return true;
                                 }
                             } else{
                                 break;
                             }
                         }
 
                         sl2 = l;
                         while((sl2 = sl2.getLeftDown()) != null){
                             if(!sl2.getFigure().getColor().equals(figure.getColor())){
                                 if(sl2.getLeftUp() != null
                                         && sl2.getLeftUp().getFigure() == null){
                                     return true;
                                 }
                             } else{
                                 break;
                             }
                         }
                     }
                 }
             }
             if(figure.getRightUpFigure() != null && figure.getRightUpFigure().getThreatened()){
                 // if from right up to left down
                 //  then get all left nether cell in the loop
                 //      and from every check all right up and left down
                 //        if found our figure then out of the loop
                 //        if found their figure then return true
 
                 // current position has been checked in the canKingFight function,
                 // so we just skip it.
                 l = figure.getCell();
 
                 while((l = l.getLeftDown()) != null){
                     if(l.getFigure() != null){
                         if(!l.getFigure().getColor().equals(figure.getColor())){
                             return true;
                         } else{
                             return false;
                         }
                     } else{
                         sl1 = l;
                         while((sl1 = sl1.getRightDown()) != null){
                             if(!sl1.getFigure().getColor().equals(figure.getColor())){
                                 if(sl1.getRightDown() != null
                                         && sl1.getRightDown().getFigure() == null){
                                     return true;
                                 }
                             } else{
                                 break;
                             }
                         }
 
                         sl2 = l;
                         while((sl2 = sl2.getLeftUp()) != null){
                             if(!sl2.getFigure().getColor().equals(figure.getColor())){
                                 if(sl2.getLeftUp() != null
                                         && sl2.getLeftUp().getFigure() == null){
                                     return true;
                                 }
                             } else{
                                 break;
                             }
                         }
                     }
                 }
             }
         }
 
         return result;
     }
 
     private Boolean canKingFight(Figure figure){
 
         Figure<RussianCoords> f = null;
 
         Integer vX = null;
         Integer svX = null;
 
         f = figure.getLeftDownFigure();
         if(f != null &&                                         //id figure exist
                 !f.getThreatened() &&                           //it hasn't threatened yet
                 !f.getColor().equals(figure.getColor()) &&      //it is opponent figure
                 f.getCell().getLeftDown() != null &&            //cell over it is exist
                 f.getCell().getLeftDown().getFigure() == null   //cell over it is free
                 ){
             return true;
         }
 
         f = figure.getLeftUpFigure();
         if(f != null &&                                         //id figure exist
                 !f.getThreatened() &&                           //it hasn't threatened yet
                 !f.getColor().equals(figure.getColor()) &&      //it is opponent figure
                 f.getCell().getLeftUp() != null &&              //cell over it is exist
                 f.getCell().getLeftUp().getFigure() == null     //cell over it is free
                 ){
             return true;
         }
 
         f = figure.getRightUpFigure();
         if(f != null &&                                         //id figure exist
                 !f.getThreatened() &&                           //it hasn't threatened yet
                 !f.getColor().equals(figure.getColor()) &&      //it is opponent figure
                 f.getCell().getRightUp() != null &&             //cell over it is exist
                 f.getCell().getRightUp().getFigure() == null    //cell over it is free
                 ){
             return true;
         }
 
         f = figure.getRightDownFigure();
         if(f != null &&                                         //id figure exist
                 !f.getThreatened() &&                           //it hasn't threatened yet
                 !f.getColor().equals(figure.getColor()) &&      //it is opponent figure
                 f.getCell().getRightDown() != null &&           //cell over it is exist
                 f.getCell().getRightDown().getFigure() == null  //cell over it is free
                 ){
             return true;
         }
 
         return false;
     }
 
     private Boolean canFigureFight(Figure figure, Boolean afterFight){
         if(figure.getType() == FigureType.CHECKER){
             return this.canCheckerFight(figure);
         } else if(figure.getType() == FigureType.KING){
             if(afterFight){
                 return this.canKingFightAfterFight(figure);
             } else {
                 return this.canKingFight(figure);
             }
         }
 
             return false;
     }
 
     private RussianCoords checkFightAnywhere(FigureColor color){
 
         if(color == FigureColor.WHITE){
             for(Figure<RussianCoords> f : white.values()){
                 if(canFigureFight(f, false)){
                     return f.getCell().getCoords();
                 }
             }
         } else if(color == FigureColor.BLACK) {
             for(Figure<RussianCoords> f : black.values()){
                 if(canFigureFight(f, false)){
                     return f.getCell().getCoords();
                 }
             }
         }
 
         return null;
     }
 
     private void isValidCheckerStep(RussianCoords fromCoord, RussianCoords toCoord, FigureColor color) throws CheckersException {
         Integer vX = toCoord.getX() - fromCoord.getX();
         Integer vY = toCoord.getY() - fromCoord.getY();
 
         //Check step direction (only forward allowed)
         if((vX < 0 && color.equals(FigureColor.WHITE)) || (vX > 0 && color.equals(FigureColor.BLACK))){
             throw new CheckersException(6L, "Invalid step direction (only forward allowed for checker)");
         }
 
         //Only one step
         if(Math.abs(vX) > 1){
             throw new CheckersException(7L, "Invalid step lenght (only one length step is allowed for checker)");
         }
 
         //Only diagonally
         if(Math.abs(vX) != Math.abs(vY)){
             throw new CheckersException(8L, "Only diagonally step allowed");
         }
 
     }
 
     private void isValidKingStep(RussianCoords fromCoord, RussianCoords toCoord) throws CheckersException {
         Integer vX = toCoord.getX() - fromCoord.getX();
         Integer vY = toCoord.getY() - fromCoord.getY();
 
         //Only diagonally
         if(Math.abs(vX) != Math.abs(vY)){
             throw new CheckersException(8L, "Only diagonally step allowed");
         }
 
     }
 
     @Override
     public Boolean checkStep(String step, FigureColor color) throws CheckersException {
 
         String[] steps = step.split("-|:");
 
         //Check is it your figure?
         Figure bf = graph.getFigure(new RussianCoords(steps[0]));
 
         if(bf == null){
             throw new CheckersException(9L, "Cell is clear");
         }
 
         if(!bf.getColor().equals(color)){
             throw new CheckersException(13L, "This figure isn't yours");
         }
 
         // Just a step
         if(step.contains("-")){
             RussianCoords fromCoord = new RussianCoords(steps[0]);
             RussianCoords toCoord = new RussianCoords(steps[1]);
 
             Figure f = null;
 
             if(graph.getFigure(toCoord) != null){
                 throw new CheckersException(12L, "This cell is occupied");
             }
 
             RussianCoords fight = checkFightAnywhere(color);
 
             if(fight != null){
                 throw new CheckersException(11L, "Your figure can fight on " + fight.getCheckersNotation());
             }
 
             if(color.equals(FigureColor.WHITE)){
                 f = white.get(fromCoord);
             } else if(color.equals(FigureColor.BLACK)){
                 f = black.get(fromCoord);
             }
 
             if(f == null){
                 throw new CheckersException(9L, "Cell is clear");
             }
 
             if(f.getType().equals(FigureType.CHECKER)){
                 isValidCheckerStep(fromCoord, toCoord, color);
             } else if(f.getType().equals(FigureType.KING)){
                 isValidKingStep(fromCoord, toCoord);
             }
 
             graph.moveFigure(fromCoord, toCoord);
 
             if(color.equals(FigureColor.BLACK)){
                 black.put(toCoord, black.get(fromCoord));
                 black.remove(fromCoord);
             } else if(color.equals(FigureColor.WHITE)){
                 white.put(toCoord, white.get(fromCoord));
                 white.remove(fromCoord);
             }
 
             if(color.equals(FigureColor.BLACK) && toCoord.getX().equals(1)){
                 black.get(toCoord).setType(FigureType.KING);
             } else if(color.equals(FigureColor.WHITE) && toCoord.getX().equals(8)){
                 white.get(toCoord).setType(FigureType.KING);
             }
 
             counter1++;
 
             // Step with fight
         } else if(step.contains(":")){
 
             graph.startTransaction();
             graph.getFigure(new RussianCoords(steps[0])).setFighter(true);
 
             try{
 
                 for(int i = 1; i < steps.length; i++){
                     RussianCoords fromCoord = new RussianCoords(steps[i-1]);
                     RussianCoords toCoord = new RussianCoords(steps[i]);
                     Figure<RussianCoords> f = graph.getFigure(fromCoord).getFigureFromDirection(toCoord);
 
                     if(f == null){
                         throw new CheckersException(18L, "There is no figure for fight");
                     }
 
                     //Only diagonally
                     if(Math.abs(fromCoord.getX() - toCoord.getX()) != Math.abs(fromCoord.getY() - toCoord.getY())){
                         throw new CheckersException(8L, "Only diagonally step allowed");
                     }
 
                     // all checks should be placed here
                     if(graph.getFigure(toCoord) != null){
                         throw new CheckersException(12L, "This cell is occupied");
                     }
 
                     if(!graph.getFigure(fromCoord).getFighter()){
                         throw new CheckersException(14L, "You lost step sequence");
                     }
 
                     if(graph.getFigure(fromCoord).getType().equals(FigureType.CHECKER)){
 
                         // one step length
                         if(getDistance(fromCoord, toCoord) != 2){
                             throw new CheckersException(15L, "Invalid step length (Fight)");
                         }
 
                         // fight opponent figure
                         if(f.getColor().equals(color)){
                             throw new CheckersException(16L, "You can't step over your figure");
                         }
 
                         // figure has been threatened already
                         if(f.getThreatened()){
                             throw new CheckersException(17L, "Figure has been threatened already");
                         }
 
                         // If all steps before performed successfully
                         //then mark figure under fight as threatened
                         f.setThreatened(true);
 
                     } else if(graph.getFigure(fromCoord).getType().equals(FigureType.KING)){
                         //Check that on the way just one figure
                         if(f.getFigureFromDirection(toCoord) != null){
                             throw new CheckersException(18L, "There is more then one figure on the way");
                         }
 
                         // fight opponent figure
                         if(f.getColor().equals(color)){
                             throw new CheckersException(16L, "You can't step over your figure");
                         }
 
                         // figure has been threatened already
                         if(f.getThreatened()){
                             throw new CheckersException(17L, "Figure has been threatened already");
                         }
 
                         // If all steps before performed successfully
                         //then mark figure under fight as threatened
                         f.setThreatened(true);
                     }
 
                     graph.moveFigure(fromCoord, toCoord);
 
                     if(color.equals(FigureColor.BLACK)){
                         black.put(toCoord, black.get(fromCoord));
                         black.remove(fromCoord);
                     } else if(color.equals(FigureColor.WHITE)){
                         white.put(toCoord, white.get(fromCoord));
                         white.remove(fromCoord);
                     }
 
                     if(f.getColor().equals(FigureColor.WHITE)){
                         white.remove(f.getCell().getCoords());
                     } else if (f.getColor().equals(FigureColor.BLACK)){
                         black.remove(f.getCell().getCoords());
                     }
                     graph.delFigure(f.getCell().getCoords());
 
                     //if all pre conditions correct in figure step up to last cell in the board then this become king
                     if(color.equals(FigureColor.BLACK) && toCoord.getX().equals(1)){
                         black.get(toCoord).setType(FigureType.KING);
                     } else if(color.equals(FigureColor.WHITE) && toCoord.getX().equals(8)){
                         white.get(toCoord).setType(FigureType.KING);
                     }
                 }
 
             } catch(CheckersException ce){
                 graph.rollbackTransaction();
                 throw ce;
             }
 
            if(canFigureFight(graph.getFigure(new RussianCoords(steps[steps.length - 1])), true)){
                throw new CheckersException(19L, "You have another step to fight");
            }

             graph.getFigure(new RussianCoords(steps[steps.length - 1])).setFighter(false);
             graph.commitTransaction();
 
             counter1 = 0;
         }
 
         if(checkCondition2(black, white) || checkCondition2(white, black)){
             counter2++;
         } else {
             counter2 = 0;
         }
 
         if(checkCondition3(black, white) || checkCondition3(white, black)){
             counter3++;
         } else {
             counter3 = 0;
         }
 
         if(checkCondition4(black, white) || checkCondition4(white, black)){
             counter4++;
         } else {
             counter4 = 0;
         }
 
         return true;
     }
 
     /// if one player has three kings another one king
     boolean checkCondition2(Map<RussianCoords, Figure> figures1, Map<RussianCoords, Figure> figures2){
         if(figures1.size() == 3 && figures2.size() == 1){
             for(Figure f : figures1.values()){
                 if(f.getType() != FigureType.KING){
                     return false;
                 }
             }
             if(figures2.values().iterator().next().getType() != FigureType.KING){
                 return false;
             }
                 return true;
         }
 
         return false;
     }
 
     /// if one player has three kings another one king on big way
     boolean checkCondition3(Map<RussianCoords, Figure> figures1, Map<RussianCoords, Figure> figures2){
         if(figures1.size() == 3 && figures2.size() == 1){
             for(Figure f : figures1.values()){
                 if(f.getType() != FigureType.KING){
                     return false;
                 }
             }
 
             Figure f = figures2.values().iterator().next();
             Coords c = f.getCell().getCoords();
             if(f.getType() != FigureType.KING || !c.getX().equals(c.getY())){
                 return false;
             }
             return true;
         }
 
         return false;
     }
 
     /// One player has two king or one king and one check another player has one king
     boolean checkCondition4(Map<RussianCoords, Figure> figures1, Map<RussianCoords, Figure> figures2){
         if(figures1.size() == 2 && figures2.size() == 1){
             boolean flag = false;
 
             for(Figure f : figures1.values()){
                 if(f.getType() == FigureType.KING){
                     flag = true;
                 }
             }
 
             if(!flag){
                 return false;
             }
 
             Figure f = figures2.values().iterator().next();
             if(f.getType() != FigureType.KING){
                 return false;
             }
             return true;
         }
 
         return false;
     }
 
     boolean isAllLocked(Map<RussianCoords, Figure> figures){
         for(Figure f : figures.values()){
             if(f.getCell().getLeftUp() != null){
                 if(f.getCell().getLeftUp().getFigure() != null){
                     if(f.getCell().getLeftUp().getLeftUp() != null &&
                             f.getCell().getLeftUp().getLeftUp().getFigure() == null){
                         return false;
                     }
                 } else {
                     return false;
                 }
             }
 
             if(f.getCell().getLeftDown() != null){
                 if(f.getCell().getLeftDown().getFigure() != null){
                     if(f.getCell().getLeftDown().getLeftDown() != null &&
                             f.getCell().getLeftDown().getLeftDown().getFigure() == null){
                         return false;
                     }
                 } else {
                     return false;
                 }
             }
 
             if(f.getCell().getRightUp() != null){
                 if(f.getCell().getRightUp().getFigure() != null){
                     if(f.getCell().getRightUp().getRightUp() != null &&
                             f.getCell().getRightUp().getRightUp().getFigure() == null){
                         return false;
                     }
                 } else {
                     return false;
                 }
             }
 
             if(f.getCell().getRightDown() != null){
                 if(f.getCell().getRightDown().getFigure() != null){
                     if(f.getCell().getRightDown().getRightDown() != null &&
                             f.getCell().getRightDown().getRightDown().getFigure() == null){
                         return false;
                     }
                 } else {
                     return false;
                 }
             }
         }
 
         return true;
     }
 
     @Override
     public GameResult checkGameStatus() {
         //Losing section
         if(black.isEmpty()){
             return GameResult.LOSING_BLACK_DESTROYED;
         }
         if(white.isEmpty()){
             return GameResult.LOSING_WHITE_DESTROYED;
         }
 
         if(isAllLocked(black)){
             return GameResult.LOSING_BLACK_LOCKED;
         }
         if(isAllLocked(white)){
             return GameResult.LOSING_WHITE_LOCKED;
         }
 
         //Dead Heat section
         if(counter1 >= 32){
             return GameResult.DEADHEAT_WITHOUT_FIGHT_32;
         }
 
         if(counter2 >= 15){
             return GameResult.DEADHEAT_KING_3_AND_KING_1_15;
         }
 
         if(counter3 >= 5){
             return GameResult.DEADHEAT_KING_3_AND_KING_1_ON_BIG_WAY_5;
         }
 
         if(counter4 >= 10){
             return GameResult.DEADHEAT_FIGURE_2_AND_KING_1_10;
         }
 
         //TODO DEADHEAT_THE_SAME_COMBINATION_3_TIMES
 
         return GameResult.CONTINUE;
     }
 
     private String f(Integer x, Integer y){
         try {
             Cell<RussianCoords> c = graph.getCell(new RussianCoords(x, y));
             Figure<RussianCoords> f;
             if((f = c.getFigure()) != null){
                 if(f.getColor().equals(FigureColor.WHITE)){
                     if(f.getType().equals(FigureType.CHECKER)){
                         return "w";
                     } else if(f.getType().equals(FigureType.KING)){
                         return "W";
                     }
                 } else if (f.getColor().equals(FigureColor.BLACK)) {
                     if(f.getType().equals(FigureType.CHECKER)){
                         return "b";
                     } else if(f.getType().equals(FigureType.KING)){
                         return "B";
                     }
                 }
             }
         } catch (CheckersException e) {
             e.printStackTrace();
         }
 
         return " ";
     }
 
     @Override
     public String toString(){
         StringBuilder sb = new StringBuilder();
 
         sb.append(" |a|b|c|d|e|f|g|h|\n");
 
         sb.append("8|x|" + f(8,2) + "|x|" + f(8,4) +  "|x|" + f(8,6) + "|x|" + f(8,8) + "|\n");
         sb.append("7|" + f(7,1) + "|x|" + f(7,3) + "|x|" + f(7,5) + "|x|" + f(7,7) + "|x|\n");
         sb.append("6|x|" + f(6,2) + "|x|" + f(6,4) + "|x|" + f(6,6) + "|x|" + f(6,8) + "|\n");
         sb.append("5|" + f(5,1) + "|x|" + f(5,3) + "|x|" + f(5,5) + "|x|" + f(5,7) + "|x|\n");
         sb.append("4|x|" + f(4,2) + "|x|" + f(4,4) + "|x|" + f(4,6) + "|x|" + f(4,8) + "|\n");
         sb.append("3|" + f(3,1) + "|x|" + f(3,3) + "|x|" + f(3,5) + "|x|" + f(3,7) + "|x|\n");
         sb.append("2|x|" + f(2,2) + "|x|" + f(2,4) + "|x|" + f(2,6) + "|x|" + f(2,8) + "|\n");
         sb.append("1|" + f(1,1) + "|x|" + f(1,3) + "|x|" + f(1,5) + "|x|" + f(1,7) + "|x|\n");
 
         return sb.toString();
     }
 }
