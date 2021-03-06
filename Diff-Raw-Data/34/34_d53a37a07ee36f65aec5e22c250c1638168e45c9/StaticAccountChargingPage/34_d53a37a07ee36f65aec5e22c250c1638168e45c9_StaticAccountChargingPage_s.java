 /*
  * Copyright (C) 2010 BloatIt. This file is part of BloatIt. BloatIt is free
  * software: you can redistribute it and/or modify it under the terms of the GNU
  * Affero General Public License as published by the Free Software Foundation,
  * either version 3 of the License, or (at your option) any later version.
  * BloatIt is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
  * details. You should have received a copy of the GNU Affero General Public
  * License along with BloatIt. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.bloatit.web.linkable.money;
 
 import static com.bloatit.framework.webprocessor.context.Context.tr;
 
 import com.bloatit.framework.exceptions.highlevel.ShallNotPassException;
 import com.bloatit.framework.exceptions.lowlevel.RedirectException;
 import com.bloatit.framework.exceptions.lowlevel.UnauthorizedOperationException;
 import com.bloatit.framework.webprocessor.annotations.ParamConstraint;
 import com.bloatit.framework.webprocessor.annotations.ParamContainer;
 import com.bloatit.framework.webprocessor.annotations.RequestParam;
 import com.bloatit.framework.webprocessor.annotations.tr;
 import com.bloatit.framework.webprocessor.components.HtmlDiv;
 import com.bloatit.framework.webprocessor.components.HtmlTitle;
 import com.bloatit.framework.webprocessor.components.HtmlTitleBlock;
 import com.bloatit.framework.webprocessor.components.meta.HtmlElement;
 import com.bloatit.framework.webprocessor.context.Context;
 import com.bloatit.model.Actor;
 import com.bloatit.model.Feature;
 import com.bloatit.model.Member;
 import com.bloatit.model.Team;
 import com.bloatit.web.linkable.contribution.HtmlChargeAccountLine;
 import com.bloatit.web.linkable.contribution.HtmlPayBlock;
 import com.bloatit.web.linkable.contribution.HtmlTotalSummary;
 import com.bloatit.web.linkable.contribution.MoneyVariationBlock;
 import com.bloatit.web.linkable.contribution.QuotationPage;
 import com.bloatit.web.linkable.contribution.StandardQuotation;
 import com.bloatit.web.linkable.features.FeaturesTools;
 import com.bloatit.web.linkable.softwares.SoftwaresTools;
 import com.bloatit.web.pages.master.Breadcrumb;
 import com.bloatit.web.pages.master.HtmlDefineParagraph;
 import com.bloatit.web.pages.master.sidebar.TwoColumnLayout;
 import com.bloatit.web.url.AccountChargingPageUrl;
 import com.bloatit.web.url.PaylineProcessUrl;
 import com.bloatit.web.url.StaticAccountChargingPageUrl;
 import com.bloatit.web.url.UnlockAccountChargingProcessActionUrl;
 
 /**
  * A page used to put money onto the internal bloatit account
  */
 @ParamContainer("account/charging/check")
 public final class StaticAccountChargingPage extends QuotationPage {
 
     @RequestParam(conversionErrorMsg = @tr("The process is closed, expired, missing or invalid."))
     @ParamConstraint(optionalErrorMsg = @tr("The process is closed, expired, missing or invalid."))
     private final AccountChargingProcess process;
     private final StaticAccountChargingPageUrl url;
 
     public StaticAccountChargingPage(final StaticAccountChargingPageUrl url) {
        super(url, null);
         this.url = url;
         process = url.getProcess();
     }
 
     @Override
     public HtmlElement createRestrictedContent(final Member loggedUser) throws RedirectException {
         final TwoColumnLayout layout = new TwoColumnLayout(true, url);
         layout.addLeft(generateCheckContributeForm(loggedUser));
         // TODO layout.addRight();
         return layout;
     }
 
     public HtmlElement generateCheckContributeForm(final Member member) {
         final HtmlTitleBlock group;
         if (process.getTeam() != null) {
             try {
                 group = new HtmlTitleBlock(tr("Validate the {0} account charging", process.getTeam().getDisplayName()), 1);
             } catch (final UnauthorizedOperationException e) {
                 throw new ShallNotPassException(e);
             }
         } else {
             group = new HtmlTitleBlock(tr("Validate your account charging"), 1);
         }
         try {
             getActor(member).getInternalAccount().getAmount();
             generateNoMoneyContent(group, getActor(member));
         } catch (final UnauthorizedOperationException e) {
             session.notifyError(Context.tr("An error prevented us from displaying getting your account balance. Please notify us."));
             throw new ShallNotPassException("User cannot access user's account balance", e);
         }
 
         return group;
     }
 
     private Actor<?> getActor(final Member member) {
         if (process.getTeam() != null) {
             return process.getTeam();
         }
         return member;
     }
 
     private void generateNoMoneyContent(final HtmlTitleBlock group, final Actor<?> actor) {
         // Total
         final StandardQuotation quotation = new StandardQuotation(process.getAmountToCharge());
 
         final HtmlDiv lines = new HtmlDiv("quotation_details_lines");
         lines.add(new HtmlChargeAccountLine(process.getAmountToCharge(), actor, null));
         group.add(lines);
 
         final HtmlDiv summary = new HtmlDiv("quotation_totals_lines_block");
         summary.add(new HtmlTotalSummary(quotation, hasToShowFeeDetails(), url));
         summary.add(new HtmlPayBlock(quotation,
                                      process.getTeam(),
                                      new PaylineProcessUrl(actor, process),
                                      new UnlockAccountChargingProcessActionUrl(process)));
         group.add(summary);
 
     }
 
     public HtmlDiv generateFeatureSummary(final Feature feature) {
         final HtmlDiv featureContributionSummary = new HtmlDiv("feature_contribution_summary");
         {
             featureContributionSummary.add(new HtmlTitle(tr("The feature"), 2));
 
             try {
                 final HtmlDiv changeLine = new HtmlDiv("change_line");
                 {
                     changeLine.add(SoftwaresTools.getSoftwareLogo(feature.getSoftware()));
                     changeLine.add(new MoneyVariationBlock(feature.getContribution(), feature.getContribution().add(process.getAmountToCharge())));
                 }
                 featureContributionSummary.add(changeLine);
                 featureContributionSummary.add(new HtmlDefineParagraph(tr("Target feature: "), FeaturesTools.getTitle(feature)));
             } catch (final UnauthorizedOperationException e) {
                 session.notifyError(Context.tr("An error prevented us from accessing user's info. Please notify us."));
                 throw new ShallNotPassException("User cannot access user information", e);
             }
         }
         return featureContributionSummary;
     }
 
     @Override
     protected String createPageTitle() {
         return tr("Contribute to a feature - check");
     }
 
     @Override
     public boolean isStable() {
         return false;
     }
 
     @Override
     public String getRefusalReason() {
         return tr("You must be logged to contribute");
     }
 
     @Override
     protected Breadcrumb createBreadcrumb() {
         return generateBreadcrumb(session.getAuthToken().getMember(), process.getTeam(), process);
     }
 
     public static Breadcrumb generateBreadcrumb(final Member member, final Team asTeam, final AccountChargingProcess process) {
         final Breadcrumb breadcrumb;
         if (asTeam != null) {
             breadcrumb = AccountChargingPage.generateBreadcrumb(member, asTeam, process);
         } else {
             breadcrumb = AccountPage.generateBreadcrumb(member);
         }
         final AccountChargingPageUrl url = new AccountChargingPageUrl(process);
 
         breadcrumb.pushLink(new UnlockAccountChargingProcessActionUrl(process).getHtmlLink(tr("Charging")));
         breadcrumb.pushLink(url.getHtmlLink(tr("Validation")));
         return breadcrumb;
     }
 }
