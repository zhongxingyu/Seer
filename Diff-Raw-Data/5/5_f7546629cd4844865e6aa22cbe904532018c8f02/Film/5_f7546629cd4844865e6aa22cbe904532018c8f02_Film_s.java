 package mediateka.db;
 
 
 import biz.source_code.base64Coder.Base64Coder;
 import org.dom4j.Element;
 import org.dom4j.dom.DOMElement;
 
 /**
  * Класс, представляющий фильм
  * @author Il'ya
  */
 public final class Film implements Record {
 
 	private int filmID;
 	private String russianTitle;
 	private String englishTitle = "";
 	private int year = 0;
 	private String description = "";
 	private String[] genres = null;
 	private String[] countries = null;
 	private String comment = "";
 	private int length = 0;
 	private int rating = 0;
 	private String[] subtitles = null;
 	private byte[] cover = null;
 	private String[] soundLanguages = null;
 	private boolean isSeen = false;
 
         /**
          * Получить русское название фильма
          * @return Русское название фильма
          */
         public String getRussianTitle() {
 		return this.russianTitle;
 	}
 
         /**
          * Получить ID фильма
          * @return ID фильма
          */
         public int getID() {
             return this.filmID;
     }
         /**
          * Установить русское название фильма
          * @param russianTitle Устанавливаемое название фильма на русском языке
          */
         public void setRussianTitle(String russianTitle) {
 		this.russianTitle = russianTitle;
 	}
 
         /**
          * Получить английское название фильма
          * @return английское название фильма
          */
         public String getEnglishTitle() {
 		return this.englishTitle;
 	}
 
         /**
          * Установить английское название фильма
          * @param englishTitle Устанавливаемое английское название фильма
          */
         public void setEnglishTitle(String englishTitle) {
 		this.englishTitle = englishTitle;
 	}
 
         /**
          * Получить год выхода фидьма
          * @return год выхода фильма
          */
         public int getYear() {
 		return this.year;
 	}
 
         /**
          * Установить год выхода фильма
          * @param year Устанавливаемый год выхода фильма
          */
         public void setYear(int year) {
 		this.year = year;
 	}
 
         /**
          * Получение описания
          * @return описание фильма
          */
         public String getDescription() {
 		return this.description;
 	}
 
         /**
          * Установление описания фильма
          * @param description Устанавливаемое описание фильма
          */
         public void setDescription(String description) {
 		this.description = description;
 	}
 
         /**
          * Получить список жанров
          * @return список жанров
          */
         public String[] getGenres() {
 		return this.genres;
 	}
 
         /**
          * Установить список жанров
          * @param genres Устанавливаемый список жанров
          */
         public void setGenres(String[] genres) {
 		this.genres = genres;
 	}
 
         /**
          * Получить список стран выбранного фильма
          * @return список стран выбранного фильма
          */
         public String[] getCountries() {
 		return this.countries;
 	}
 
         /**
          * Установить список стран выбранного фильма
          * @param countries список стран выбранного фильма
          */
         public void setCountries(String[] countries) {
 		this.countries = countries;
 	}
 
         /**
          * Получить комментарий
          * @return комментарий
          */
         public String getComment() {
 		return this.comment;
 	}
 
         /**
          * Установить комментарий
          * @param comment устанавливаемый комментарий
          */
         public void setComment(String comment) {
 		this.comment = comment;
 	}
 
         /**
          * Получить длительность фильма
          * @return длительность фильма
          */
         public int getLength() {
 		return this.length;
 	}
 
         /**
          * Установить длину фильма
          * @param length длина фильма
          */
         public void setLength(int length) {
 		this.length = length;
 	}
 
         /**
          * Получить оценку фильма
          * @return оценка фильма
          */
         public int getRating() {
 		return this.rating;
 	}
 
         /**
          * Установить оценку фильма
          * @param rating Устанавливаемая оценка фильма
          */
         public void setRating(int rating) {
 		this.rating = rating;
 	}
 
         /**
          * Получить список доступных субтитров
          * @return список доступных субтитров
          */
         public String[] getSubtitles() {
 		return this.subtitles;
 	}
 
         /**
          * Установить список доступных субтитров
          * @param subtitles список доступных субтитров
          */
         public void setSubtitles(String[] subtitles) {
 		this.subtitles = subtitles;
 	}
 
         /**
          * Получить обложку
          * @return обложка
          */
         public byte[] getCover() {
 		return this.cover;
 	}
 
         /**
          * Установить обложку
          * @param cover Устанавливаемая обложка
          */
         public void setCover(byte[] cover) {
 		this.cover = cover;
 	}
 
         /**
          * Получить список языков озвучки
          * @return список языков озвучки
          */
         public String[] getSoundLanguages() {
 		return this.soundLanguages;
 	}
 
         /**
          * Установить список языков озвучки
          * @param soundLanguages список языков озвучки
          */
         public void setSoundLanguages(String[] soundLanguages) {
 		this.soundLanguages = soundLanguages;
 	}
 
         /**
          * Просмотрен ли фильм
          * @return true если фильм просмотрен
          */
         public boolean isIsSeen() {
 		return this.isSeen;
 	}
 
         /**
          * Установить факт просмотра
          * @param isSeen Если фильм просмотрен true, иначе - false
          */
         public void setIsSeen(boolean isSeen) {
 		this.isSeen = isSeen;
 	}
 
 	/**
 	 * Полный конструктор
         * @param russianName Русское название фильма
          * @param englishTitle Английское название фильма
          * @param year Год выхода
          * @param description Описание
          * @param genres Жанры
          * @param countries Страны
          * @param comment Комментарий
          * @param length Длительность
          * @param rating Оценка
          * @param subtitles Субтитры
          * @param cover Обложка
          * @param soundLanguages Языки озвучки
          * @param isSeen Факт просмотра
 	 */
 	public Film(String russianTitle, String englishTitle, int year,String description, String[] genres ,String[] countries,String comment,int length, int rating, String[] subtitles, byte[] cover, String[] soundLanguages, boolean isSeen) {
           
            this.russianTitle = russianTitle;
            this.englishTitle = englishTitle;
            this.year = year;
            this.description = description;
            this.genres = genres;
            this.countries = countries;
            this.comment = comment;
            this.length = length;
            this.rating = rating;
            this.subtitles = subtitles;
            this.cover = cover;
            this.soundLanguages = soundLanguages;
            this.isSeen = isSeen;
 	}
 
         /**
 	 * Конструктор с обязательными параметрами
         * @param russianName Русское название фильма
          */
         public  Film(String russianTitle) {
             this(russianTitle,"",0,"",null,null,"",0,0,null,null,null,false);
         }
         
         /**
          * Сериализует фильм в XML
          * @return Строка с фильмом, сериализованным в XML element
          */
         public String ToXmlElement() {
             Element elem = new DOMElement("film");
             Element tempElem;
             elem.addAttribute("filmID", Integer.toString(this.filmID));
             elem.addElement("russianTitle").addText(this.russianTitle);
             elem.addElement("englishTitle").addText(this.englishTitle);
             elem.addElement("year").addText(Integer.toString(this.year));
             elem.addElement("description").addText(this.description);
             elem.addElement("comment").addText(this.comment);
             elem.addElement("length").addText(Integer.toString(this.length));
             elem.addElement("rating").addText(Integer.toString(this.rating));
             elem.addElement("isSeen").addText(Boolean.toString(isSeen));
             elem.addElement("cover").addText(Base64Coder.encodeLines(cover));
             tempElem = new DOMElement("genres");
             for(int i=0;i<genres.length;i++){
                 elem.add(tempElem);
             }
             tempElem = new DOMElement("countries");
             for(int i=0;i<countries.length;i++){
                 elem.add(tempElem);
             }
             tempElem = new DOMElement("soundLanguages");
             for(int i=0;i<soundLanguages.length;i++){
                 elem.add(tempElem);
             }
             tempElem = new DOMElement("subtitles");
             for(int i=0;i<subtitles.length;i++){
                 elem.add(tempElem);
             }
             return elem.asXML();
         
     }
 }
