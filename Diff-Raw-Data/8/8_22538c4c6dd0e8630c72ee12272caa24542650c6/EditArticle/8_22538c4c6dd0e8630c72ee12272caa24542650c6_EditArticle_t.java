 /**
  * Copyright (C) 2012-2013  Olexandr Tyshkovets
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.github.aint.jblog.web.controller;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.ConstraintViolation;
 import javax.validation.Validator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.aint.jblog.model.dao.hibernate.ArticleHibernateDao;
 import com.github.aint.jblog.model.entity.Article;
 import com.github.aint.jblog.model.entity.Language;
 import com.github.aint.jblog.service.data.impl.ArticleServiceImpl;
 import com.github.aint.jblog.service.exception.data.EntityNotFoundException;
 import com.github.aint.jblog.service.util.HibernateUtil;
 import com.github.aint.jblog.service.util.HtmlTag;
 import com.github.aint.jblog.service.util.StringUtil;
 import com.github.aint.jblog.service.validation.Validation;
 import com.github.aint.jblog.web.constant.ConstantHolder;
 import com.github.aint.jblog.web.constant.SessionConstant;
 import com.github.aint.jblog.web.dto.ArticleDto;
 
 /**
  * This servlet edits an article.
  * 
  * @author Olexandr Tyshkovets
  */
 @WebServlet("/edit-article")
 public class EditArticle extends HttpServlet {
     private static final long serialVersionUID = 4442741525293399967L;
     private static Logger logger = LoggerFactory.getLogger(EditArticle.class);
     private static final String EDIT_ARTICLE_JSP_PATH = ConstantHolder.PATH.getProperty("path.jsp.editArticle");
     private static final String UPDATE_ARTICLE_BUTTON = "updateArticleButton";
     private static final String ARTICLE_TITLE_FIELD = "articleTitleField";
     private static final String ARTICLE_PREVIEW_FIELD = "articlePreviewField";
     private static final String ARTICLE_BODY_FIELD = "articleBodyField";
     private static final String ARTICLE_KEYWORDS_FIELD = "articleKeywords";
     private static final long ONE_HOUR = 1000 * 60 * 60 * 24;
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         Article article = null;
         try {
             article = new ArticleServiceImpl(new ArticleHibernateDao(HibernateUtil.getSessionFactory()))
                     .get(Long.parseLong(request.getParameter("articleId")));
         } catch (NumberFormatException e) {
             logger.warn("The article's id parameter is uncorrect {} {}" + e.getClass() + " " + e.getMessage());
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return;
         } catch (EntityNotFoundException e) {
             logger.info("The article not found", e);
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
             return;
         }
 
         if (!article.getAuthor().getUserName().equals(
                 request.getSession(true).getAttribute(SessionConstant.USER_NAME_SESSION_ATTRIBUTE)) ||
                 (System.currentTimeMillis() - article.getCreationDate().getTime() > ONE_HOUR)) {
             response.sendError(HttpServletResponse.SC_FORBIDDEN, "edit_article.label.disable_edit_article");
             return;
         }
 
         String articleTitle = request.getParameter(ARTICLE_TITLE_FIELD);
         String articlePreview = request.getParameter(ARTICLE_PREVIEW_FIELD);
         String articleBody = request.getParameter(ARTICLE_BODY_FIELD);
         String articleKeywords = request.getParameter(ARTICLE_KEYWORDS_FIELD);
 
         if (request.getParameter(UPDATE_ARTICLE_BUTTON) != null) {
            ArticleDto articleDto = new ArticleDto(
                    articleTitle,
                    articlePreview,
                    articleBody,
                    articleKeywords,
                    article.getHub().getName()
                    );
             Language language = (Language)
                     request.getSession().getAttribute(SessionConstant.USER_LANGUAGE_SESSION_ATTRIBUTE);
             Validator validator = Validation.getValidator(language.getLocale());
             Set<ConstraintViolation<ArticleDto>> validationErrors = validator.validate(articleDto);
             if (!validationErrors.isEmpty()) {
                 logger.debug("The article's validation error messages: {}", validationErrors);
                 request.setAttribute("validationErrors", validationErrors);
                 request.setAttribute("articleBody", StringUtil.convertHtmlBRLineDelimitersToLF(article.getBody()));
                 request.setAttribute("article", article);
                 request.getRequestDispatcher(EDIT_ARTICLE_JSP_PATH).forward(request, response);
                 return;
             }
 
             article.setTitle(articleTitle);
             article.setPreview(articlePreview);
             Set<HtmlTag> ignoreTag = new HashSet<HtmlTag>();
             ignoreTag.add(HtmlTag.A);
             ignoreTag.add(HtmlTag.B);
             ignoreTag.add(HtmlTag.BLOCKQUOTE);
             ignoreTag.add(HtmlTag.DEL);
             ignoreTag.add(HtmlTag.I);
             article.setBody(StringUtil.convertLineDelimitersToHtmlBR(
                     StringUtil.escapeHtmlInText(ignoreTag, articleBody)));
             article.setKeywords(StringUtil.processKeywords(articleKeywords));
 
             request.setAttribute("message", "edit_article.label.article_successfully_updated");
         }
 
         request.setAttribute("articleBody", StringUtil.convertHtmlBRLineDelimitersToLF(article.getBody()));
         request.setAttribute("article", article);
         request.getRequestDispatcher(EDIT_ARTICLE_JSP_PATH).forward(request, response);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         doGet(request, response);
     }
 
 }
