 package org.glite.authz.pap.common.xacml.wizard;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.glite.authz.pap.common.utils.Utils;
 import org.glite.authz.pap.common.xacml.impl.PolicyTypeString;
 import org.glite.authz.pap.common.xacml.impl.TypeStringUtils;
 import org.glite.authz.pap.common.xacml.utils.DescriptionTypeHelper;
 import org.glite.authz.pap.common.xacml.utils.ObligationsHelper;
 import org.glite.authz.pap.common.xacml.utils.PolicyHelper;
 import org.glite.authz.pap.common.xacml.utils.XMLObjectHelper;
 import org.glite.authz.pap.common.xacml.wizard.exceptions.PolicyWizardException;
 import org.glite.authz.pap.common.xacml.wizard.exceptions.UnsupportedPolicyException;
 import org.glite.authz.pap.common.xacml.wizard.exceptions.UnsupportedPolicySetWizardException;
 import org.opensaml.xacml.policy.EffectType;
 import org.opensaml.xacml.policy.ObligationType;
 import org.opensaml.xacml.policy.ObligationsType;
 import org.opensaml.xacml.policy.PolicyType;
 import org.opensaml.xacml.policy.RuleType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class allows to render an action (of the simplified policy language) into an OpenSAML object
  * and vice versa. The XACML policy this class produces (or recognizes) has a <i>Target</i> with one
  * <i>ActionMatch</i> and a set of rules (see {@link RuleWizard}.
  * <p>
  * The {@link PolicyType} object (and the DOM) is build only if it is used the constructor
  * {@link PolicyWizard#PolicyWizard(PolicyType)} or the method {@link PolicyWizard#getXACML()}.<br>
  * In order to cut on memory usage release the PolicyType object when it's not needed by using the
  * helper method {@link TypeStringUtils#releaseUnneededMemory(Object)}.
  * <p>
  * The {@link PolicyWizard#getXACML()} method returns an instance of {@link PolicyTypeString}.
  * Therefore, if you don't have to do specific operations on it (e.g. just giving it to Axis), call
  * the {@link TypeStringUtils#releaseUnneededMemory(Object)} method on it to keep only the string
  * representation and save a lot of memory.
  */
 public class PolicyWizard extends XACMLWizard {
 
     private static final Logger log = LoggerFactory.getLogger(PolicyWizard.class);
 
     /** Prefix used in the policyId to set a policy as private */
     protected static final String VISIBILITY_PRIVATE_PREFIX = "private";
     
     /** Prefix used in the policyId to set a policy as public */
     protected static final String VISIBILITY_PUBLIC_PREFIX = "public";
     
     protected final String actionValue;
     
     /** The action attribute on which this policy is based */
     protected static final AttributeWizardType attributeWizardType = AttributeWizardTypeConfiguration.getInstance()
                                                                                               .getActionAttributeWizard();
     protected final List<ObligationWizard> obligationWizardList = new LinkedList<ObligationWizard>();
     protected final List<RuleWizard> ruleWizardList = new LinkedList<RuleWizard>();
     protected final TargetWizard targetWizard;
     
     protected String description = null;
     protected boolean isPrivate = false;
     protected PolicyTypeString policy = null;
     protected String policyId = null;
     protected String policyIdUniqueNumber;
     protected String version = null;
     
     /**
      * Constructor that renders an action of the simplified policy language to XACML.
      * 
      * @param attributeWizard the action attribute defining the <i>Target</i> of the policy.
      * 
      * @throws UnsupportedPolicyException if the given attribute is not an action attribute as the
      *             one given by {@link AttributeWizardTypeConfiguration#getActionAttributeWizard()}.
      */
     public PolicyWizard(AttributeWizard attributeWizard) {
 
         if (attributeWizardType != attributeWizard.getAttributeWizardType()) {
             throw new UnsupportedPolicyException("Attribute not supported: " + attributeWizard.getId());
         }
 
         actionValue = attributeWizard.getValue();
         targetWizard = new TargetWizard(attributeWizard);
 
         policyId = generatePolicyId();
         version = "1";
     }
 
     /**
      * Constructor the renders an XACML policy to an action of the simplified policy language.
      * 
      * @param policy the XACML policy.
      * 
      * @throws UnsupportedPolicyException if the given XACML policy cannot be rendered to an action
      *             of the simplified policy language.
      */
     public PolicyWizard(PolicyType policy) {
 
         if (!(policy instanceof PolicyTypeString)) {
             throw new PolicyWizardException("Argument is not PolicyTypeString");
         }
 
         targetWizard = new TargetWizard(policy.getTarget());
 
         List<AttributeWizard> targetAttributeWizardList = targetWizard.getAttributeWizardList();
         validateTargetAttributewizardList(targetAttributeWizardList);
 
         policyId = policy.getPolicyId();
         decomposePolicyId(policy.getPolicyId());
 
         actionValue = targetAttributeWizardList.get(0).getValue();
 
         if (policy.getDescription() != null) {
             description = policy.getDescription().getValue();
         }
 
         try {
             version = policy.getVersion();
             new Integer(version);
         } catch (NumberFormatException e) {
             throw new UnsupportedPolicyException("Wrong version format", e);
         }
 
         if (policy.getObligations() != null) {
             List<ObligationType> obligationList = policy.getObligations().getObligations();
             for (ObligationType obligation : obligationList) {
                 obligationWizardList.add(new ObligationWizard(obligation));
             }
         }
 
         for (RuleType rule : policy.getRules()) {
             ruleWizardList.add(new RuleWizard(rule));
         }
 
         this.policy = (PolicyTypeString) policy;
     }
     
     /**
      * Return the action value of the given policy.
      * 
      * @param policy
      * @return the action value of the given policy.
      * 
      * @throws UnsupportedPolicyException if the given policy is not an action policy.
      */
     public static String getActionValue(PolicyType policy) {
         TargetWizard targetWizard = new TargetWizard(policy.getTarget());
 
         List<AttributeWizard> targetAttributeWizardList = targetWizard.getAttributeWizardList();
         validateTargetAttributewizardList(targetAttributeWizardList);
 
         return targetAttributeWizardList.get(0).getValue();
     }
 
     /**
      * Increases by one the version of the given policy. If the existing version of thepolicy is
      * missing or not recognized then it is set to 1.
      * 
      * @param policy the policy to increase the verion number.
      */
     public static void increaseVersion(PolicyType policy) {
         int version;
 
         try {
             version = (new Integer(policy.getVersion())).intValue();
             version++;
         } catch (NumberFormatException e) {
             log.error("Unrecognized version format, setting version to 1. PolicySetId=" + policy.getPolicyId());
             version = 1;
         }
 
         policy.setVersion(Integer.toString(version));
     }
 
     /**
      * Checks if the prefix of the given policy id is equal to
      * {@link PolicyWizard#VISIBILITY_PUBLIC_PREFIX}.
      * 
      * @param policyId the policy id to check.
      * @return <code>true</code> if the prefix of the given policy id is equal to
      *         {@link PolicyWizard#VISIBILITY_PUBLIC_PREFIX}, <code>false</code> otherwise.
      */
     public static boolean isPrivate(String policyId) {
         return !isPublic(policyId);
     }
 
     /**
      * Checks if the prefix of the given policy id is not equal to
      * {@link PolicyWizard#VISIBILITY_PRIVATE_PREFIX} or missing.
      * 
      * @param policyId the policy id to check.
      * @return <code>true</code> if the prefix of the given policy id is not equal to
      *         {@link PolicyWizard#VISIBILITY_PRIVATE_PREFIX} or missing, <code>false</code>
      *         otherwise.
      */
     public static boolean isPublic(String policyId) {
         String[] idTokens = policyId.split("_");
 
         if (idTokens.length == 0) {
             throw new UnsupportedPolicyException("Unrecognized policyId: " + policyId);
         }
 
         if (idTokens.length == 1) {
             return true;
         }
 
         if (VISIBILITY_PRIVATE_PREFIX.equals(idTokens[0])) {
             return false;
         }
 
         return true;
     }
 
     /**
      * The policy id is composed by two tokens: a prefix and an UUID. This method returns the UUID
      * part.
      * 
      * @param policyId the policy id.
      * @return the UUID part of the given policy id.
      * 
      * @throws UnsupportedPolicyException if the given policy id is not recognized.
      */
     private static String getIdUniqueNumber(String policyId) {
 
         String[] idTokens = policyId.split("_");
 
         if (idTokens.length == 0) {
             throw new UnsupportedPolicyException("Unrecognized policyId: " + policyId);
         }
 
         if (idTokens.length == 1) {
             return idTokens[0];
         }
 
         if (idTokens.length == 2) {
             if ((VISIBILITY_PRIVATE_PREFIX.equals(idTokens[0])) || (VISIBILITY_PUBLIC_PREFIX.equals(idTokens[0]))) {
                 return idTokens[1];
             }
         }
         throw new UnsupportedPolicyException("Unrecognized policyId: " + policyId);
     }
 
     /**
      * Append an obligation to the list of obligations.
      * 
      * @param obligationWizard the obligation to add.
      */
     public void addObligation(ObligationWizard obligationWizard) {
         obligationWizardList.add(obligationWizard);
         invalidatePolicyType();
     }
 
     /**
      * Add a rule which <i>Target</i> is defined by the given attribute and with the given effect.
      * 
      * @param attribute the attribute used to build the Target of the rule.
      * @param effect the effect of the rule.
      */
     public void addRule(AttributeWizard attribute, EffectType effect) {
         addRule(new RuleWizard(attribute, effect));
     }
 
     public void addRule(int index, AttributeWizard attribute, EffectType effect) {
         addRule(index, new RuleWizard(attribute, effect));
     }
 
     public void addRule(int index, List<AttributeWizard> targetAttributeList, EffectType effect) {
         addRule(new RuleWizard(targetAttributeList, effect));
     }
 
     public void addRule(int index, RuleWizard ruleWizard) {
         ruleWizardList.add(index, ruleWizard);
         invalidatePolicyType();
     }
 
     public void addRule(List<AttributeWizard> targetAttributeList, EffectType effect) {
         addRule(new RuleWizard(targetAttributeList, effect));
     }
 
     public void addRule(RuleWizard ruleWizard) {
         ruleWizardList.add(ruleWizard);
         invalidatePolicyType();
     }
 
     public boolean denyRuleForAttributeExists(AttributeWizard attributeWizard) {
 
         for (RuleWizard ruleWizard : ruleWizardList) {
             if (ruleWizard.deniesAttribute(attributeWizard)) {
                 return true;
             }
         }
         return false;
     }
 
     public String getDescription() {
         return description;
     }
 
     public int getNumberOfRules() {
         return ruleWizardList.size();
     }
     
     public String getPolicyId() {
         return policyId;
     }
 
     public String getPolicyIdPrefix() {
         if (isPrivate) {
             return VISIBILITY_PRIVATE_PREFIX;
         } else {
             return VISIBILITY_PUBLIC_PREFIX;
         }
     }
 
     public String getTagAndValue() {
         return String.format("%s \"%s\"", attributeWizardType.getId(), actionValue);
     }
 
     public int getVersion() {
         return Integer.valueOf(version);
     }
 
     public String getVersionString() {
         return version;
     }
 
     public PolicyType getXACML() {
         initPolicyTypeIfNotSet();
         return policy;
     }
 
     public void increaseVersion() {
         setVersion(getVersion() + 1);
     }
 
     public boolean isDOMReleased() {
         return (policy == null);
     }
 
     public boolean isEquivalent(PolicyType policy) {
 
         if (!(targetWizard.isEquivalent(policy.getTarget()))) {
             return false;
         }
 
         List<RuleType> ruleList = policy.getRules();
 
         if (ruleList.size() != ruleWizardList.size()) {
             return false;
         }
 
         for (int i = 0; i < ruleWizardList.size(); i++) {
             if (!(ruleWizardList.get(i).isEquivalent(ruleList.get(i)))) {
                 return false;
             }
         }
 
         if (description != null) {
             if (!(description.equals(policy.getDescription()))) {
                 return false;
             }
         } else {
             if (policy.getDescription() != null) {
                 return false;
             }
         }
         return true;
     }
 
     public boolean isPrivate() {
         return isPrivate;
     }
 
     public boolean isPublic() {
         return !isPrivate;
     }
 
     public void releaseChildrenDOM() {
         targetWizard.releaseChildrenDOM();
         targetWizard.releaseDOM();
         for (ObligationWizard obligationWizard : obligationWizardList) {
             obligationWizard.releaseChildrenDOM();
             obligationWizard.releaseDOM();
         }
         for (RuleWizard ruleWizard : ruleWizardList) {
             ruleWizard.releaseChildrenDOM();
             ruleWizard.releaseDOM();
         }
     }
 
     public void releaseDOM() {
         if (policy != null) {
             policy.releaseDOM();
             policy = null;
         }
     }
 
     public boolean removeDenyRuleForAttribute(AttributeWizard attributeWizard) {
 
         for (int i = 0; i < ruleWizardList.size(); i++) {
 
             RuleWizard ruleWizard = ruleWizardList.get(i);
 
             if (ruleWizard.deniesAttribute(attributeWizard)) {
 
                 if (policy != null) {
                    PolicyHelper.removeRule(policy, ruleWizard.getRuleId());
                 }

                ruleWizardList.remove(i);
                
                 return true;
             }
         }
         return false;
     }
 
     public void setDescription(String value) {
         this.description = value;
         if (policy != null) {
             policy.setDescription(DescriptionTypeHelper.build(value));
         }
     }
 
     public void setPolicyId(String policyId) {
         this.policyId = policyId;
         if (policy != null) {
             policy.setPolicyId(policyId);
         }
     }
 
     public void setPrivate(boolean isPrivate) {
         this.isPrivate = isPrivate;
 
         policyId = composeId();
 
         if (policy != null) {
             policy.setPolicyId(policyId);
         }
     }
 
     public void setVersion(int version) {
         this.version = Integer.toString(version);
 
         if (policy != null) {
             policy.setVersion(this.version);
         }
     }
 
     public String toFormattedString() {
         return toFormattedString(false);
     }
 
     public String toFormattedString(boolean printPolicyId) {
         return toFormattedString(0, 4, printPolicyId, false);
     }
 
     public String toFormattedString(int baseIndentation, int internalIndentation) {
         return toFormattedString(baseIndentation, internalIndentation, false, false);
     }
 
     public String toFormattedString(int baseIndentation, int internalIndentation, boolean printPolicyId, boolean printRuleIds) {
 
         String baseIndentString = Utils.fillWithSpaces(baseIndentation);
         String indentString = Utils.fillWithSpaces(baseIndentation + internalIndentation);
         StringBuffer sb = new StringBuffer();
 
         if (printPolicyId) {
             sb.append(String.format("%sid=%s\n", baseIndentString, policyId));
         }
 
         if (isPrivate()) {
             sb.append(String.format("%sprivate\n", baseIndentString));
         }
 
         sb.append(String.format("%saction \"%s\" {\n", baseIndentString, actionValue));
 
         if (description != null) {
             sb.append(String.format("%sdescription=\"%s\"\n", indentString, description));
         }
 
         for (ObligationWizard obligationWizard : obligationWizardList) {
             sb.append(obligationWizard.toFormattedString(baseIndentation + internalIndentation, internalIndentation));
             sb.append('\n');
         }
 
         for (RuleWizard ruleWizard : ruleWizardList) {
             if (printRuleIds) {
                 sb.append('\n');
             }
             sb.append(ruleWizard.toFormattedString(baseIndentation + internalIndentation, internalIndentation, printRuleIds));
             sb.append('\n');
         }
 
         sb.append(baseIndentString + "}");
 
         return sb.toString();
     }
 
     public String toXACMLString() {
         initPolicyTypeIfNotSet();
         return XMLObjectHelper.toString(policy);
     }
 
     /**
      * Return the effective policy id by composing the prefix and the UUID.
      * 
      * @return the effective policy id.
      */
     private String composeId() {
         return getPolicyIdPrefix() + "_" + policyIdUniqueNumber;
     }
 
     /**
      * Set the members {@link PolicyWizard#isPrivate}, {@link PolicyWizard#policyIdUniqueNumber} and
      * {@link PolicyWizard#policyIdVisibilityPrefix} from the given policy id.
      * 
      * @param policyId the policy id to decompose.
      * 
      * @throws UnsupportedPolicyException if the given policy id format is not recognized.
      */
     private void decomposePolicyId(String policyId) {
         isPrivate = isPrivate(policyId);
         policyIdUniqueNumber = getIdUniqueNumber(policyId);
     }
 
     /**
      * Return a new generated policy id.
      * 
      * @return a new generated policy id.
      */
     private String generatePolicyId() {
         policyIdUniqueNumber = WizardUtils.generateId(null);
         return composeId();
     }
 
     /**
      * Build the PolicyType object if it doesn't already exits.
      */
     private void initPolicyTypeIfNotSet() {
         if (policy == null) {
             log.debug("Initializing policyType");
             setPolicyType();
         } else {
             log.debug("policyType already initialized");
         }
     }
 
     /**
      * Release the memory used for the PolicyType object.
      */
     private void invalidatePolicyType() {
         releaseChildrenDOM();
         releaseDOM();
     }
 
     /**
      * Build (or re-build if already exists) the PolicyType object.
      */
     private void setPolicyType() {
 
         releaseDOM();
 
         policy = new PolicyTypeString(PolicyHelper.build(policyId, PolicyHelper.RULE_COMBALG_FIRST_APPLICABLE));
 
         if (description != null) {
             policy.setDescription(DescriptionTypeHelper.build(description));
         }
 
         policy.setTarget(targetWizard.getXACML());
         policy.setVersion(version);
 
         if (obligationWizardList.size() > 0) {
             ObligationsType obligations = ObligationsHelper.build();
             List<ObligationType> obligationList = obligations.getObligations();
             for (ObligationWizard obligationWizard : obligationWizardList) {
                 obligationList.add(obligationWizard.getXACML());
             }
 
             policy.setObligations(obligations);
         }
 
         for (RuleWizard ruleWizard : ruleWizardList) {
             policy.getRules().add(ruleWizard.getXACML());
         }
     }
 
     /**
      * Checks if the given target attribute wizard list can be used to build an action of the
      * simplified policy language.
      * 
      * @param targetAttributeWizardList the list of wizard attributes to validate.
      * 
      * @throws UnsupportedPolicyException if the validation fails.
      */
     private static void validateTargetAttributewizardList(List<AttributeWizard> targetAttributeWizardList) {
 
         if (targetAttributeWizardList == null) {
             throw new UnsupportedPolicySetWizardException("targetAttributeWizardList is null");
         }
 
         if (targetAttributeWizardList.size() != 1) {
             throw new UnsupportedPolicySetWizardException("Only one action attribute is supported (found "
                     + targetAttributeWizardList.size() + " attributes)");
         }
 
         AttributeWizard aw = targetAttributeWizardList.get(0);
 
         if (aw.getAttributeWizardType() != attributeWizardType) {
             throw new UnsupportedPolicySetWizardException("Only one action attribute is supported");
         }
     }
 }
