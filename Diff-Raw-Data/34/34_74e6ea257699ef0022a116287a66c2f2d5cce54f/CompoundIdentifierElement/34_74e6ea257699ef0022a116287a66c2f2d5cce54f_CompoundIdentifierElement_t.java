 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.BuildDataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedEntityUsage;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedFieldUsage;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeInfo;
 
 
 public class CompoundIdentifierElement implements IdentifierElement {
 
     public CompoundIdentifierElement(final IdentifierElement owner, final String name) {
        if (null == owner) {
            throw new NullPointerException("owner is null.");
        }
        if (null == name) {
            throw new NullPointerException("name is null.");
        }

         this.name = name;
         this.owner = owner;
 
         final String[] ownerName = owner.getQualifiedName();
         final String[] thisName = new String[ownerName.length + 1];
         System.arraycopy(ownerName, 0, thisName, 0, ownerName.length);
         thisName[thisName.length - 1] = name;
         this.qualifiedName = thisName;
     }
 
     public String[] getQualifiedName() {
         return this.qualifiedName;
     }
 
     public String getName() {
         return this.name;
     }
 
     public ExpressionElement getOwner() {
         return this.owner;
     }
 
     public UnresolvedTypeInfo getType() {
         return null;
     }
 
     public UnresolvedTypeInfo getOwnerType() {
         return this.ownerType;
     }
 
     public UnresolvedTypeInfo resolveAsAssignmetedVariable(final BuildDataManager buildDataManager) {
         this.ownerType = this.resolveOwner(buildDataManager);
         final UnresolvedFieldUsage fieldUsage = new UnresolvedFieldUsage(buildDataManager
                 .getAllAvaliableNames(), this.ownerType, this.name);
         buildDataManager.addFieldAssignment(fieldUsage);
         return fieldUsage;
     }
 
     public IdentifierElement resolveAsCalledMethod(final BuildDataManager buildDataManager) {
         this.ownerType = this.resolveOwner(buildDataManager);
         return this;
     }
 
     public UnresolvedTypeInfo resolveAsReferencedVariable(final BuildDataManager buildDataManager) {
         this.ownerType = this.resolveOwner(buildDataManager);
         final UnresolvedFieldUsage fieldUsage = new UnresolvedFieldUsage(buildDataManager
                 .getAllAvaliableNames(), this.ownerType, this.name);
         buildDataManager.addFieldReference(fieldUsage);
         return fieldUsage;
     }

    public UnresolvedTypeInfo resolveReferencedEntityIfPossible(final BuildDataManager buildDataManager) {
        this.ownerType = this.owner.resolveReferencedEntityIfPossible(buildDataManager);

        if (this.ownerType != null) {
            final UnresolvedFieldUsage fieldUsage = new UnresolvedFieldUsage(buildDataManager
                    .getAllAvaliableNames(), this.ownerType, this.name);
             buildDataManager.addFieldReference(fieldUsage);
             return fieldUsage;
         } else {
             return null;
         }
     }
 
     protected UnresolvedTypeInfo resolveOwner(final BuildDataManager buildDataManager) {
        this.ownerType = this.owner.resolveReferencedEntityIfPossible(buildDataManager);
        if (null != this.ownerType) {
            return this.ownerType;
         } else {
             return new UnresolvedEntityUsage(buildDataManager.getAllAvaliableNames(), this.owner
                    .getQualifiedName());
         }
     }
 
     private final String name;
 
     private final String[] qualifiedName;
 
     private final IdentifierElement owner;
 
     private UnresolvedTypeInfo ownerType;
 
 }
