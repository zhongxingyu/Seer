 package org.eclipse.b3.aggregator.provider;
 
 import org.eclipse.b3.aggregator.AggregatorPackage;
 import org.eclipse.b3.aggregator.EnabledStatusProvider;
 import org.eclipse.b3.aggregator.Status;
 import org.eclipse.b3.aggregator.StatusCode;
 import org.eclipse.b3.aggregator.StatusProvider;
 import org.eclipse.b3.aggregator.util.OverlaidImage;
 import org.eclipse.b3.util.StringUtils;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.IItemColorProvider;
 import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 
 public class AggregatorItemProviderAdapter extends ItemProviderAdapter implements TooltipTextProvider {
 
 	static class AggregatorItemPropertyDescriptor extends ItemPropertyDescriptor {
 
 		public AggregatorItemPropertyDescriptor(AdapterFactory adapterFactory, ResourceLocator resourceLocator,
 				String displayName, String description, EStructuralFeature feature, boolean isSettable,
 				boolean multiLine, boolean sortChoices, Object staticImage, String category, String[] filterFlags) {
 			super(
 				adapterFactory, resourceLocator, displayName, description, feature, isSettable, multiLine, sortChoices,
 				staticImage, category, filterFlags);
 		}
 
 		@Override
 		public boolean canSetProperty(Object object) {
 			boolean result = super.canSetProperty(object);
 
 			if(result)
 				if(parentsEnabled(object)) {
 					if(object instanceof EnabledStatusProvider)
 						result = ((EnabledStatusProvider) object).isEnabled() ||
 								AggregatorPackage.Literals.ENABLED_STATUS_PROVIDER__ENABLED.getName().equals(
 									getId(object));
 				}
 				else
 					result = false;
 
 			return result;
 		}
 
 		private boolean parentsEnabled(Object object) {
 			if(object instanceof EObject) {
 				EObject eObject = (EObject) object;
 
 				EObject eContainer = eObject.eContainer();
 
 				if(eContainer == null)
 					return true;
 
 				if(eContainer instanceof EnabledStatusProvider)
 					if(!((EnabledStatusProvider) eContainer).isEnabled())
 						return false;
 
 				return parentsEnabled(eContainer);
 			}
 
 			return true;
 		}
 
 		@Override
 		public void setPropertyValue(Object object, Object value) {
 			// Replaces empty string with null
 			if(value instanceof String)
 				value = StringUtils.trimmedOrNull((String) value);
 
 			super.setPropertyValue(object, value);
 		}
 
 	}
 
 	public static String getTooltipText(Object object, ItemProviderAdapter itemProvider) {
 		if(!(object instanceof StatusProvider))
 			return null;
 
 		Status status = ((StatusProvider) object).getStatus();
 
 		if(status.getMessage() == null)
 			return null;
 
 		StringBuilder sb = new StringBuilder();
 
 		sb.append(AggregatorEditPlugin.INSTANCE.getString("_UI_Structured_Tooltip_Label"));
 		sb.append(" ");
 		sb.append(itemProvider.getText(object));
 		sb.append("\n");
 		sb.append(AggregatorEditPlugin.INSTANCE.getString("_UI_Structured_Tooltip_ErrorMessage"));
 		sb.append(" ");
 		sb.append(status.getMessage());
 
 		return sb.toString();
 	}
 
 	public AggregatorItemProviderAdapter(AdapterFactory adapterFactory) {
 		super(adapterFactory);
 	}
 
 	@Override
 	protected ItemPropertyDescriptor createItemPropertyDescriptor(AdapterFactory adapterFactory,
 			ResourceLocator resourceLocator, String displayName, String description, EStructuralFeature feature,
 			boolean isSettable, boolean multiLine, boolean sortChoices, Object staticImage, String category,
 			String[] filterFlags) {
 		return new AggregatorItemPropertyDescriptor(
 			adapterFactory, resourceLocator, displayName, description, feature, isSettable, multiLine, sortChoices,
 			staticImage, category, filterFlags);
 	}
 
 	/**
 	 * Grey out the label if this item is (directly or indirectly) disabled
 	 */
 	@Override
 	public Object getForeground(Object object) {
 		if(object instanceof EnabledStatusProvider)
			return ((EnabledStatusProvider) object).isEnabled()
 					? null
 					: IItemColorProvider.GRAYED_OUT_COLOR;
 
 		return null;
 	}
 
 	// default implementation
 	public String getTooltipText(Object object) {
 		return getTooltipText(object, this);
 	}
 
 	@Override
 	protected Object overlayImage(Object object, Object image) {
 		image = super.overlayImage(object, image);
 
 		StatusProvider sp = (StatusProvider) getRootAdapterFactory().adapt(object, StatusProvider.class);
 
 		if(sp != null) {
 			StatusCode sc = sp.getStatus().getCode();
 
 			if(sc == StatusCode.WAITING || sc == StatusCode.BROKEN)
 				image = new OverlaidImage(new Object[] { image, getResourceLocator().getImage(sc == StatusCode.WAITING
 						? "full/ovr16/Loading"
 						: "full/ovr16/Error") }, OverlaidImage.BASIC_BOTTOM_RIGHT);
 		}
 
 		return image;
 	}
 
 }
