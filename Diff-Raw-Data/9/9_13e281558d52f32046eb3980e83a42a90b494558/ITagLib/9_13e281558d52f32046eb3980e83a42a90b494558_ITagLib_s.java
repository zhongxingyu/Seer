 package gutenberg.workers;
 
 public interface ITagLib {
     
     String comment ="%",
             printanswers = "\\printanswers",
            pageNumber = "\\thealphnumpage", 
             docAuthor = "\\DocAuthor",
             newpage = "\\nextpg", 
             usepackage = "\\usepackage",
             fancyfoot = "\\fancyfoot", 
             beginDocument = "\\begin{document}",
             beginQuestions = "\\begin{questions}", 
             school = "\\School",
             docClass = "\\documentclass", 
             insertQR = "\\insertQR",
             endQuestions = "\\end{questions}", 
             endDocument = "\\end{document}",
             printRubric = "\\printrubric", 
             question = "\\question",
             solution = "\\begin{solution}", 
             part = "\\part",            
             tableEnd = "\\end{tabular}",
             setCounter = "\\setcounter{rolldice}",
             fancyfooter = "\\fancyfoot[C]{\\copyright\\, www.gradians.com}";    
 }
