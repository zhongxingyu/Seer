 package de.fkoeberle.autocommit.message;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.ref.SoftReference;
 import java.lang.reflect.Field;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IRegistryEventListener;
 import org.eclipse.core.runtime.Platform;
 import org.osgi.framework.Bundle;
 
 public class ProfileManager {
 	private static final String FACTORY_EXTENSION_POINT_ID = "de.fkoeberle.autocommit.message.factory";
 	private final SoftReference<Profile> defaultProfile;
 	private final IRegistryEventListener factoryExtensionPointListener;
 	private final IRegistryEventListener profileExtensionPointListener;
 
 	public ProfileManager() throws CoreException {
 		defaultProfile = new SoftReference<Profile>(null);
 
 		factoryExtensionPointListener = new RegistryEventListener();
 		profileExtensionPointListener = new RegistryEventListener();
 		Platform.getExtensionRegistry().addListener(
 				factoryExtensionPointListener, FACTORY_EXTENSION_POINT_ID);
 	}
 
 	public void dispose() {
 		Platform.getExtensionRegistry().removeListener(
 				factoryExtensionPointListener);
 		Platform.getExtensionRegistry().removeListener(
 				profileExtensionPointListener);
 	}
 
 	private final class RegistryEventListener implements IRegistryEventListener {
 		@Override
 		public void added(IExtension[] extensions) {
 			defaultProfile.clear();
 		}
 
 		@Override
 		public void removed(IExtension[] extensions) {
 			defaultProfile.clear();
 		}
 
 		@Override
 		public void added(IExtensionPoint[] extensionPoints) {
 			// ignore
 		}
 
 		@Override
 		public void removed(IExtensionPoint[] extensionPoints) {
 			// ignore
 		}
 	}
 
 	public ProfileDescription createProfileDescriptionFor(URL resource)
 			throws IOException {
 		ProfileXml profileXml = ProfileXml.createFrom(resource);
 		CMFDescriptionFactory cmfFactory = new CMFDescriptionFactory();
 		return profileXml.createProfileDescription(cmfFactory);
 	}
 
 	public Profile getProfileFor(URL resource) throws IOException {
 		return createProfileDescriptionFor(resource).createProfile();
 	}
 
 	public Profile getProfileFor(File file) throws IOException {
 		return getProfileDescriptionFor(file).createProfile();
 	}
 
 	public ProfileDescription getProfileDescriptionFor(File commitMessagesFile)
 			throws IOException {
 		URL resource;
 		try {
 			resource = commitMessagesFile.toURI().toURL();
 		} catch (MalformedURLException e) {
 			throw new IOException(e);
 		}
 		return createProfileDescriptionFor(resource);
 	}
 
 	private List<CommitMessageFactoryDescription> createAvailableFactoryDescriptionList() {
 		IExtensionPoint factoryExtensionPoint = Platform.getExtensionRegistry()
 				.getExtensionPoint(FACTORY_EXTENSION_POINT_ID);
 		IConfigurationElement[] factoryConfigurations = factoryExtensionPoint
 				.getConfigurationElements();
 
 		List<CommitMessageFactoryDescription> factories = new ArrayList<CommitMessageFactoryDescription>(
 				factoryConfigurations.length);
 		for (IConfigurationElement element : factoryConfigurations) {
 			Class<?> classObject = getClassAttribute(element);
 			Class<? extends ICommitMessageFactory> factoryClass = classObject
 					.asSubclass(ICommitMessageFactory.class);
 			String description = element.getAttribute("description");
 			final List<String> argumentDescriptions;
 			argumentDescriptions = createArgumentListFrom(element);
 			List<CommitMessageDescription> messages = createMessagesListFrom(
 					element, factoryClass);
 
 			CommitMessageFactoryDescription factoryDescription = new CommitMessageFactoryDescription(
 					factoryClass, description, argumentDescriptions, messages);
 			factories.add(factoryDescription);
 		}
 		return factories;
 	}
 
 	private List<CommitMessageDescription> createMessagesListFrom(
 			IConfigurationElement element, Class<?> factoryClass) {
 		Map<String, Field> fieldMap = new HashMap<String, Field>();
 		for (Field field : factoryClass.getFields()) {
 			if (field.getType().equals(CommitMessageTemplate.class)) {
 				fieldMap.put(field.getName(), field);
 			}
 		}
 		Set<String> fieldsMissingInDoc = new HashSet<String>(fieldMap.keySet());
 		final List<CommitMessageDescription> messages;
 		IConfigurationElement[] messagesElements = element
 				.getChildren("messages");
 		if (messagesElements.length != 0) {
 			IConfigurationElement[] messageElements = messagesElements[0]
 					.getChildren();
 			messages = new ArrayList<CommitMessageDescription>(
 					messageElements.length);
 			for (IConfigurationElement messageElement : messageElements) {
 				String fieldName = messageElement.getAttribute("fieldName");
 				String defaultValue = messageElement
 						.getAttribute("defaultValue");
 				Field field = fieldMap.get(fieldName);
				fieldsMissingInDoc.remove(fieldName);
 				if (field == null) {
 					throw new RuntimeException(
 							String.format(
 									"Class %s has no field %s as declared in it's extension",
 									factoryClass.getName(), fieldName));
 				}
 				messages.add(new CommitMessageDescription(field, defaultValue,
 						defaultValue));
 			}
 		} else {
 			messages = new ArrayList<CommitMessageDescription>(0);
 		}
 		if (fieldsMissingInDoc.size() != 0) {
 			System.err.printf(
 					"Factory extension %s does not declare fields %s%n",
 					factoryClass.getName(), fieldsMissingInDoc);
 		}
 		return messages;
 	}
 
 	private List<String> createArgumentListFrom(IConfigurationElement element) {
 		final List<String> argumentDescriptions;
 		IConfigurationElement[] argumentsElements = element
 				.getChildren("arguments");
 		if (argumentsElements.length != 0) {
 			IConfigurationElement[] argumentElements = argumentsElements[0]
 					.getChildren();
 			argumentDescriptions = new ArrayList<String>(
 					argumentElements.length);
 			for (IConfigurationElement argumentElement : argumentElements) {
 				argumentDescriptions.add(argumentElement
 						.getAttribute("description"));
 			}
 		} else {
 			argumentDescriptions = new ArrayList<String>(0);
 		}
 		return argumentDescriptions;
 	}
 
 	private Class<?> getClassAttribute(IConfigurationElement element) {
 		String className = element.getAttribute("class");
 
 		String contributorName = element.getContributor().getName();
 		Bundle contributorBundle = Platform.getBundle(contributorName);
 		Class<?> classObject;
 		try {
 			classObject = contributorBundle.loadClass(className);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 		return classObject;
 	}
 
 	private Map<String, CommitMessageFactoryDescription> createFactoryIdToDescriptionMap() {
 		List<CommitMessageFactoryDescription> list = createAvailableFactoryDescriptionList();
 		Map<String, CommitMessageFactoryDescription> factoryMap = new HashMap<String, CommitMessageFactoryDescription>(
 				list.size());
 		for (CommitMessageFactoryDescription factoryDescription : list) {
 			factoryMap.put(factoryDescription.getFactoryClass()
 					.getCanonicalName(), factoryDescription);
 		}
 		return factoryMap;
 	}
 
 	private class CMFDescriptionFactory implements ICMFDescriptionFactory {
 		Map<String, CommitMessageFactoryDescription> map = createFactoryIdToDescriptionMap();
 
 		@Override
 		public CommitMessageFactoryDescription createFactoryDescription(
 				String id) {
 			if (id == null) {
 				throw new NullPointerException("id must not be null");
 			}
 			CommitMessageFactoryDescription factoryDescription = map.get(id);
 			if (factoryDescription == null) {
 				throw new RuntimeException("There is no factory with the id "
 						+ id);
 			}
 			return factoryDescription;
 		}
 	}
 
 }
