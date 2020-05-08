 /*
  * This code is distributed under terms of GNU GPLv2.
  * *See LICENSE file.
  * ©UKRINFORM 2011-2012
  */
 
 package jtvprog;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 /**
  * Channel processing set class
  * @author Stanislav Nepochatov
  */
 public class chProcSet extends chSet{
     
     /**
      * System dependent line seporator
      */
     private static String lineSeparator = System.getProperty("line.separator");
     
     /**
      * Relative path for channel release
      */
     private String CH_PATH = "по каналам/";
     
     /**
      * Relative path for day release
      */
     private String DAY_PATH = "по дням/";
     
     /**
      * Relative path for ASOP release
      */
     //private String ASOP_PATH = "ASOP/";
     
     /**
      * List of channels processors units
      */
     private java.util.ArrayList<chProcUnit> chProcList = new java.util.ArrayList();
     
     /**
      * Current processor unit
      */
     protected chProcUnit currentUnit;
     
     /**
      * Current inputed channel in stack;
      */
     public Integer currentIndex = 1;
     
     /**
      * Current channel name
      */
     public String currentChName = "";
     
     /**
      * Enumeration of states for channel processor;
      */
     private enum states {
         /**
          * Processor doing nothing
          */
         EMPTY,
         
         /**
          * Processor now at input state
          */
         INPUT,
         
         /**
          * Processor now at output state
          */
         OUTPUT};
     
     /**
      * Default state of processor
      */
     private states currentState = states.EMPTY;
     
     /**
      * Files for day release
      */
     private java.io.File[] outDays = new java.io.File[7];
     
     /**
      * 
      */
     private String[] daysHeaders = new String[7]; 
     
     /**
      * Array of string constants for days of week
      */
     private String[] daysPatterns = new String[] {"Понедельник,", "Вторник,", "Среда,", "Четверг,", "Пятница,", "Суббота,", "Воскресенье,"};
     private String[] daysPatterns_NOMINATIVE = new String[] {"понедельник", "вторник", "среда", "четверг", "пятница", "суббота", "воскресенье"};
     private String[] daysPatterns_GENITIVE = new String[] {"понедельника", "вторника", "среды", "четверга", "пятницы", "субботы", "воскресенья"};
     private String[] daysPatterns_INSTRUMENTAL = new String[] {"понедельником", "вторником", "средой", "четвергом", "пятницой", "субботой", "воскресеньем"};
     
     /**
      * Temporary array for checkInputDP and perfromInput conversation;
      * @see #checkInputDP(java.lang.String)
      * @see #performInput(java.lang.String) 
      */
     private String[] dayTempStack = new String[7];
     
     /**
      * Array of string blocks for day release
      */
     private String[][] dayMatrix = new String[7][JTVProg.configer.Channels.getSetSize()];
     
     /**
      * Output stack for program relese<br>
      * <b>[0]</b> - headers of channels;<br>
      * <b>[1]</b> - channels content;<br>
      * <b>[2]</b> - headers of days;<br>
      * <b>[3]</b> - day's content;
      */
     private java.util.ArrayList<String>[] outputStack = new java.util.ArrayList[4];
     
     /**
      * Operatioanl output index flag
      */
     private Integer operFlag = 0;
     
     /**
      * Operational output stack for headers
      */
     public java.util.ArrayList<String> operOutHeaders;
     
     /**
      * Operational output stack for current release mode
      */
     public java.util.ArrayList<String> operOutStack;
     
     /**
      * Maximum length of message.
      * Changed to 13000 in order to fix issue with applying messages
      */
     private static int maxLength = 13000;
     
     /**
      * Empty constructor
      */
     chProcSet() {
         new java.io.File(CH_PATH).mkdir();
         new java.io.File(DAY_PATH).mkdir();
        // new java.io.File(ASOP_PATH).mkdir();
     }
     
     /**
      * Construct set with given list of channels
      * @param givenChList list of channels from chSet object
      */
     chProcSet(java.util.ArrayList<chEntry> givenChList) {
         this();
         java.util.ListIterator<chEntry> chIter = givenChList.listIterator();
         while (chIter.hasNext()) {
             chProcUnit tempProcUnit = new chProcUnit(chIter.next());
             this.chProcList.add(tempProcUnit);
         }
     }
     
     /**
      * Channel processing unit
      */
     protected class chProcUnit extends chEntry {
         
         /**
          * Empty constructor
          */
         chProcUnit() {
             super ("", 0, 0, "");
         }
         
         /**
          * Counstruct from arguments
          * @param GivenChName
          * @param GivenChFOrder
          * @param GivenChROrder
          * @param GivenChFilename 
          */
         chProcUnit(String GivenChName, Integer GivenChFOrder, Integer GivenChROrder, String GivenChFilename) {
             super(GivenChName, GivenChFOrder, GivenChROrder, GivenChFilename);
         }
         
         /**
          * Construct from chEntry
          * @param givenChannel 
          */
         chProcUnit(chEntry givenChannel) {
             super(givenChannel.chName, givenChannel.chFillOrder, givenChannel.chReleaseOrder, givenChannel.chFilename);
         }
         
         /**
          * File object for this unit
          */
         private java.io.File chFile = new java.io.File(CH_PATH + this.chFilename);
         
         /**
          * Temporary string content of channel
          */
         public String chStored = "";
         
         /**
          * Write string to file and close it.
          */
         public void writeChannel() {
             try {
                 java.io.FileWriter chWriter = new java.io.FileWriter(chFile);
                 chWriter.write(chStored);
                 chWriter.close();
                 JTVProg.logPrint(this, 3, "файл канала [" + this.chName + "] успешно сохранен");
             } catch (IOException ex) {
                 JTVProg.logPrint(this, 0, "ошибка записи файла канала [" + this.chName + "]");
             } finally {
                 JTVProg.configer.markWrited(this.chFillOrder - 1);
             }
         }
         
         /**
          * Read channel content from file
          */
         public void readChannel() {
             this.chStored = "";
             try {
                 java.io.FileReader chReader = new java.io.FileReader(chFile);
                 while (chReader.ready()) {
                     this.chStored = this.chStored + chReader.read();
                 }
                 chReader.close();
             } catch (FileNotFoundException ex) {
                 JTVProg.logPrint(this, 0, "файл канала [" + this.chName + "] не найден");
             } catch (IOException ex) {
                 JTVProg.logPrint(this, 0, "ошибка чтения файла канала [" + this.chName + "]");
             }
         }
         
         @Override
         public String toString() {
             return "[" + this.chName + " ," + this.chFillOrder + " ," + this.chReleaseOrder + " ," + this.chFilename + "]";
         }
     }
     
     /**
      * Get processor unit according to state of processors set
      * @param index index of unit
      * @return chProcUnit object
      */
     private chProcUnit getUnit(Integer index) {
         chProcUnit returnedUnit = new chProcUnit();
         if (currentState == states.INPUT) {
             java.util.ListIterator<chProcUnit> chIter = chProcList.listIterator();
             while (chIter.hasNext()) {
                 chProcUnit tempUnit = chIter.next();
                 if (tempUnit.chFillOrder == index) {
                     returnedUnit = tempUnit;
                     break;
                 }
             }
         } else if (currentState == states.OUTPUT) {
             java.util.ListIterator<chProcUnit> chIter = chProcList.listIterator();
             while (chIter.hasNext()) {
                 chProcUnit tempUnit = chIter.next();
                 if (tempUnit.chReleaseOrder == index) {
                     returnedUnit = tempUnit;
                     break;
                 }
             }
         } else {
             JTVProg.logPrint(this, 1, "ошибка вызова: процессор без состояния");
         }
         return returnedUnit;
     }
     
     /**
      * Get file content as string
      * @param fileObj file descriptor object
      * @return string content of given file object
      */
     private String getFileContent(java.io.File fileObj) {
         String returned = "";
         try {
             java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(fileObj));
             while (reader.ready()) {
                 returned = returned + reader.readLine() + lineSeparator;
             }
         } catch (FileNotFoundException ex) {
             JTVProg.logPrint(this, 0, "файл [" + fileObj.getName() + "] не найден");
         } catch (IOException ex) {
             JTVProg.logPrint(this, 0, "[" + fileObj.getName() + "]: ошибка ввода/вывода");
         }
         return returned;
     }
     
     /**
      * Prepeare set for channels input
      */
     public void beginInput() {
         this.currentState = states.INPUT;
         this.currentIndex = 1;
         this.currentUnit = getUnit(this.currentIndex);
         this.currentChName = this.currentUnit.chName;
     }
     
     /**
      * End input and unlock processing mode
      */
     public void endInput() {
         this.currentIndex = 1;
         this.currentUnit = null;
         this.currentChName = null;
         this.currentState = states.EMPTY;
         JTVProg.mainWindow.tvFillBut.setEnabled(false);
         JTVProg.mainWindow.tvProcBut.setEnabled(true);
     }
     
     /**
      * Move processor stack to input next channel
      */
     public void inputNext() {
         ++this.currentIndex;
         this.currentUnit = getUnit(this.currentIndex);
         this.currentChName = this.currentUnit.chName;
     }
     
     /**
      * Move processor stack to input previous channel
      */
     public void inputPrev() {
         --this.currentIndex;
         this.currentUnit = getUnit(this.currentIndex);
         this.currentChName = this.currentUnit.chName;
     }
     
     /**
      * Get current channel content
      * @return content current content of channel
      */
     public String getCurrentContent() {
         return this.currentUnit.chStored;
     }
     
     /**
      * Get size of set
      * @return size of channel processor set
      */
     @Override
     public Integer getSetSize() {
         return this.chProcList.size();
     }
     
     /**
      * Check channel input (deeper version)
      * This method checks given string with multifactor conditions<br>
      * to pass this checking channel content must satisfy such conditions as:<br>
      * 1) First line must contain channel's name;<br>
      * 2) String must contain all days in the week;<<br>
      * 3) All days should have correct date against other channels;<br>
      * <br>
      * This method also split current channel text by days and prepeare<br>
      * <code>daysTempStack</code> variable to accept by <code>performInput()</code><br>
      * method.
      * @param content string with channel content
      * @return string with error or null
      * @since JTVProg v0.2
      */
     public String checkInputDP(String content) {
         String returned = null;
         
         String gmtLabel = "GMT + 2";
         if (content.contains(gmtLabel)) {
             content = content.substring(content.indexOf(gmtLabel) + gmtLabel.length()).trim();
         }
         
         //First condition
         if (this.pickHead(content).contains(this.currentChName)) {
             String[] blocks = content.split(lineSeparator + lineSeparator);
             Integer acceptedIndex = -1;
             for (Integer currBlockIndex = 0; currBlockIndex < blocks.length; currBlockIndex++) {
                 String currentBlock = blocks[currBlockIndex];
                 String blockHeader = pickHead(currentBlock);
                 Integer currentDayIndex;
                 if ((currentDayIndex = recognizeDay(blockHeader)) != -1) {
                     if (daysHeaders[currentDayIndex] == null) {
                         //JTVProg.logPrint(this, 3, "добавление дня [" + blockHeader + "]");
                         daysHeaders[currentDayIndex] = blockHeader;
                         //this.dayTempStack[currentDayIndex] = currentBlock.trim();
                     } else {
                         
                         //Third condition
                         if (!blockHeader.equals(daysHeaders[currentDayIndex])) {
                             returned = this.currentUnit.chName + ": несовпадение дат (" + blockHeader + "->" + daysHeaders[currentDayIndex] + ");\n";
                             break;
                             //JTVProg.logPrint(this, 1, "несовпадение дат: [" + blockHeader + "->" + daysHeaders[currentDayIndex] + "]");
                         }
                     }
                     
                     //Additional condition: sequence checking
                     if (acceptedIndex != currentDayIndex - 1) {
                         returned = "Нарушение очередности!\nЗа " + this.daysPatterns_INSTRUMENTAL[acceptedIndex] + " следует " + this.daysPatterns_NOMINATIVE[currentDayIndex] + "?";
                         break;
                     } else {
                         this.dayTempStack[currentDayIndex] = currentBlock.replaceAll(blockHeader, this.currentChName).trim();
                         acceptedIndex = currentDayIndex;
                     }
                 } else {
                     //JTVProg.logPrint(this, 2, "блок [" + currBlockIndex + "] не является днем");
                 }
             }
             
             //Second condition
             if (acceptedIndex != 6) {
                 if (returned == null) {
                     returned = "Не хватает " + this.daysPatterns_GENITIVE[acceptedIndex + 1];
                 }
             }
         } else {
             returned = "Введен не тот канал!\nНеобходимый канал: " + this.currentChName;
         }
         
         if (returned == null) {
             JTVProg.configer.markProcessed(currentIndex - 1);
         }
         return returned;
     }
     
     /**
      * Check channel input 
      * @param content current content of channel
      * @return true if given content correspond to current channel / false of not
      * @deprecated This method didn't provide full channel text parsing
      */
     public String checkInput(String content) {
         String returned;
         if (content.contains(this.currentChName)) {
             returned = preProcess(content);
         } else {
             returned = "Введен не тот канал!\nНеобходимый канал: " + this.currentChName;
         }
         return returned;
     }
     
     /**
      * General check of channel format
      * @param content current content of channel
      * @return true if channel content contains all days of week
      * @deprecated This method is too simple to parse channel
      */
     private String preProcess(String content) {
         String returned = null;
         for (Integer dayIndex = 0; dayIndex < this.daysPatterns.length; dayIndex++) {
             if (content.contains(this.daysPatterns[dayIndex]) == false) {
                 JTVProg.logPrint(this, 1, "[" + this.currentChName + "]: текст не полон");
                 returned = "Текст канала [" + this.currentChName + "] не полон!\nПроверьте целостность текста!";
                 break;
             }
         }
         return returned;
     }
     
     /**
      * Perform channel input
      * @param content content current content of channel
      */
     public void performInput(String content) {
         this.currentUnit.chStored = content;
         this.currentUnit.writeChannel();
         for (Integer dayIndex = 0; dayIndex < 7; dayIndex++) {
             this.dayMatrix[dayIndex][this.currentUnit.chReleaseOrder - 1] = this.dayTempStack[dayIndex];
         }
     }
     
     /**
      * Thread launching object for processDays() method
      * @see #processDays() 
      */
     public class processDaysThread implements Runnable {
         
         @Override
         public void run() {
             processDays();
             JTVProg.mainWindow.procDaysTail();
         }
         
     }
     
     /**
      * Process channel for day release (init method)
      */
     public void processDays() {
         String procDirtyMessage = "\n\nОшибки обработки:\n";
         Boolean procIsDirty = false;
         JTVProg.logPrint(this, 3, "начата обработка каналов");
         //String[][] dayMatrix = new String[7][this.getSetSize()];
         /**java.util.ListIterator<chProcUnit> chUnits = this.chProcList.listIterator();
         Integer chProgCounter = 1;
         while (chUnits.hasNext()) {
             Boolean chIsDirty = false;
             chProcUnit currentProc = chUnits.next();
             JTVProg.logPrint(this, 3, "обработка канала [" + currentProc.chName + "]");
             JTVProg.procWindow.procLabel.setText(currentProc.chName);
             JTVProg.procWindow.procProgres.setValue((chProgCounter / chProcList.size()) * 100 / 2);
             chProgCounter++;
             String[] blocks = currentProc.chStored.split(lineSeparator + lineSeparator);
             for (Integer currBlockIndex = 0; currBlockIndex < blocks.length; currBlockIndex++) {
                 String currentBlock = blocks[currBlockIndex];
                 String blockHeader = pickHead(currentBlock);
                 Integer currentDayIndex;
                 if ((currentDayIndex = recognizeDay(blockHeader)) != -1) {
                     JTVProg.logPrint(this, 3, "добавление дня [" + blockHeader + "]");
                     if (daysHeaders[currentDayIndex] == null) {
                         daysHeaders[currentDayIndex] = blockHeader;
                     } else {
                         if (!blockHeader.equals(daysHeaders[currentDayIndex])) {
                             JTVProg.logPrint(this, 1, "несовпадение дат: [" + blockHeader + "->" + daysHeaders[currentDayIndex] + "]");
                             procDirtyMessage += currentProc.chName + ": несовпадение дат (" + blockHeader + "->" + daysHeaders[currentDayIndex] + ");\n";
                             chIsDirty = true;
                         }
                     }
                     dayMatrix[currentDayIndex][currentProc.chReleaseOrder - 1] = currentProc.chName + lineSeparator + currentBlock.substring(blockHeader.length()).trim();
                 } else {
                     JTVProg.logPrint(this, 2, "блок [" + currBlockIndex + "] не является днем");
                 }
             }
             if (chIsDirty == true) {
                 procIsDirty = true;
             } else {
                 JTVProg.configer.markProcessed(currentProc.chFillOrder - 1);
             }
         }**/
         for (Integer currFileIndex = 0; currFileIndex < this.outDays.length; currFileIndex++) {
             this.outDays[currFileIndex] = new java.io.File(this.DAY_PATH + this.daysHeaders[currFileIndex] + ".txt");
             String dayContent = this.daysHeaders[currFileIndex];
             JTVProg.procWindow.procLabel.setText(this.daysHeaders[currFileIndex]);
             JTVProg.procWindow.procProgres.setValue(((currFileIndex + 1) / 7) * 100);
             for (Integer currChannelIndex = 0; currChannelIndex < this.getSetSize(); currChannelIndex++) {
                 String channelBlock = dayMatrix[currFileIndex][currChannelIndex];
                 if (channelBlock == null) {
                     JTVProg.logPrint(this, 1, "блок канала пуст! [" + currFileIndex + "," + currChannelIndex + "]");
                     channelBlock = "ПУСТОЙ БЛОК!!! [" + currFileIndex + "," + currChannelIndex + "]";
                     procDirtyMessage += dayContent + ": пустой блок в канале (" + JTVProg.configer.Channels.getChannelByROrder(currChannelIndex + 1) + ");\n";
                     procIsDirty = true;
                 }
                 dayContent = dayContent + lineSeparator + lineSeparator + channelBlock;
             }
             try {
                 java.io.FileWriter dayWriter = new java.io.FileWriter(this.outDays[currFileIndex]);
                 dayWriter.write(dayContent);
                 dayWriter.close();
                 JTVProg.logPrint(this, 3, "файл дня [" + this.daysHeaders[currFileIndex] + "] успешно сохранен");
             } catch (IOException ex) {
                 JTVProg.logPrint(this, 0, "ошибка записи файла дня [" + this.daysHeaders[currFileIndex] + "]");
             }
         }
         if (procIsDirty == true) {
             JTVProg.logPrint(this, 0, "Обработка телепрограммы не удалась!");
             JTVProg.warningMessage("Обработка телепрограммы не удалась из-за повреждения данных!\n"+ procDirtyMessage + "Свяжитесь с разработчиком!");
         }
     }
     
     /**
      * Pick and return first line in string block (pick date header)
      * @param block string of block in channel stack
      * @return first line of given string block
      * @see #processDays() 
      */
     private String pickHead(String block) {
         //return block.split("\n")[0].trim();
         if (block.length() > 0) {
             Integer breakIndex = 1;
             for (Integer index = 0; index < block.length(); index++) {
                 if (block.charAt(index) == '\n') {
                     breakIndex = index;
                     break;
                 }
             }
             return block.substring(0, breakIndex);
         } else {
             return "";
         }
     }
     
     /**
      * Recognize block header as day of weak
      * @param header first line of string block
      * @return index of day in array
      * @see #processDays()
      */
     private Integer recognizeDay(String header) {
         Integer returned = -1;
         for (Integer day = 0; day < 7; day++) {
             if (header.contains(this.daysPatterns[day])) {
                 returned = day;
                 break;
             }
         }
         return returned;
     }
     
     /**
      * Preapeare set for output mode
      */
     public void beginOutput() {
         this.currentState = states.OUTPUT;
         this.buildOutStack();
         this.setOutputMode(0);
     }
     
     /**
      * Build output stack with text splitting
      */
     private void buildOutStack() {
         this.outputStack[0] = new java.util.ArrayList<String>();
         this.outputStack[1] = new java.util.ArrayList<String>();
         this.outputStack[2] = new java.util.ArrayList<String>();
         this.outputStack[3] = new java.util.ArrayList<String>();
         JTVProg.logPrint(this, 3, "подготовка выпуска по каналам");
         for (Integer currentRIndex = 1; currentRIndex < this.getSetSize() + 1; currentRIndex++) {
             chProcUnit currentProc = this.getUnit(currentRIndex);
             if (currentProc.chStored.length() > maxLength) {
                 java.util.ArrayList<String> splittedStored = this.textSplit(currentProc.chStored);
                 String chHeader = this.pickHead(currentProc.chStored);
                 java.util.ListIterator<String> splitIter = splittedStored.listIterator();
                 Integer currentSplitIndex = 1;
                 while (splitIter.hasNext()) {
                     String currentSplitText = splitIter.next();
                     this.outputStack[0].add("БЛ-" + currentSplitIndex + ":" + chHeader);
                     if (currentSplitIndex == 1) {
                         this.outputStack[1].add(currentSplitText.replace(chHeader, "БЛ-" + currentSplitIndex + ":" + chHeader).trim());
                     } else {
                         this.outputStack[1].add("БЛ-" + currentSplitIndex + ":" + chHeader + lineSeparator + lineSeparator + currentSplitText.trim());
                     }
                     ++currentSplitIndex;
                 }
             }
             else {
                 this.outputStack[0].add(currentProc.chName);
                 this.outputStack[1].add(currentProc.chStored.trim());
             }
         }
         JTVProg.logPrint(this, 3, "подготовка выпуска по дням");
         for (Integer dayFileIndex = 0; dayFileIndex < outDays.length; dayFileIndex++) {
             java.io.File currDayFile = this.outDays[dayFileIndex];
             String dayContent = this.getFileContent(currDayFile).trim();
             if (dayContent.length() > maxLength) {
                 java.util.ArrayList<String> splittedDay = this.textSplit(dayContent);
                 String dayHeader = this.pickHead(dayContent);
                 java.util.ListIterator<String> splitIter = splittedDay.listIterator();
                 Integer currentSplitIndex = 1;
                 while (splitIter.hasNext()) {
                     String currentSplitText = splitIter.next();
                     this.outputStack[2].add("БЛ-" + currentSplitIndex + ":" + dayHeader);
                     if (currentSplitIndex == 1) {
                         this.outputStack[3].add(currentSplitText.replace(dayHeader, "БЛ-" + currentSplitIndex + ":" + dayHeader));
                     } else {
                         this.outputStack[3].add("БЛ-" + currentSplitIndex + ":" + dayHeader + lineSeparator + lineSeparator + currentSplitText);
                     }
                     ++currentSplitIndex;
                 }
             }
             else {
                 this.outputStack[2].add(this.pickHead(dayContent));
                 this.outputStack[3].add(dayContent);
             }
         }
     }
     
     /**
      * Split text to proper release in ASOP system
      * @param splitted given text
      * @return ArrayList with splitted text
      */
     private java.util.ArrayList<String> textSplit(String splitted) {
         java.util.ArrayList<String> returned = new java.util.ArrayList<String>();
         String[] pieces = splitted.split(lineSeparator + lineSeparator);
         String rstr = "";
         for (int cpiece = 0; cpiece < pieces.length; cpiece++) {
             String cstr = pieces[cpiece];
             if (rstr.length() + cstr.length() > maxLength) {
                 if (!rstr.isEmpty()) {
                     returned.add(rstr);
                     rstr = cstr;
                 }
                 else {
                     returned.add(cstr);
                 }
             }
             else {
                 if (rstr.equals("")) {
                     rstr = cstr;
                 }
                 else {
                     rstr = rstr + lineSeparator + lineSeparator + cstr;
                 }
             }
             if (cpiece == pieces.length - 1) {
                 returned.add(rstr);
             }
         }
         return returned;
     }
     
     /**
      * Set mode for output (channel/day)
      * @param givenFlag number of flag state (0 - channel output/1 - day output);
      */
     public void setOutputMode(Integer givenFlag) {
         switch (givenFlag) {
             case 0:
                 this.operFlag = givenFlag;
                 this.operOutHeaders = this.outputStack[0];
                 this.operOutStack = this.outputStack[1];
                 break;
             case 1:
                 this.operFlag = givenFlag;
                 this.operOutHeaders = this.outputStack[2];
                 this.operOutStack = this.outputStack[3];
                 break;
         }
         this.currentIndex = 1;
         this.currentChName = this.operOutHeaders.get(this.currentIndex - 1);
     }
     
     /**
      * Move processor stack to output next item
      */
     public void outputNext() {
         ++this.currentIndex;
         this.currentChName = this.operOutHeaders.get(this.currentIndex - 1);
     }
     
     /**
      * Move processor stack to output previous item
      */
     public void outputPrev() {
         --this.currentIndex;
         this.currentChName = this.operOutHeaders.get(this.currentIndex);
     }
 }
