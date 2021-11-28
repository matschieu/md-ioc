package com.github.matschieu.ioc.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.matschieu.ioc.core.exceptions.InvocationException;

/**
 *
 * @author Matschieu
 *
 */
public class Container {

	private static final Logger LOGGER = LoggerFactory.getLogger(Container.class);

	private static Container INSTANCE;

	private final Injector injector;

	private final Map<Class<?>, Class<?>> applicationScope = new HashMap<>();

	private final Map<Class<?>, Object> singletonInstance = new HashMap<>();

	/**
	 *
	 */
	private Container() {
		LOGGER.info("Starting bean management container");
		this.discover();
		this.injector = new Injector(this);
	}

	/**
	 *
	 * @return BeanDiscovery
	 */
	public static final Container get() {
		synchronized (Injector.class) {
			if (INSTANCE == null) {
				INSTANCE = new Container();
			}
		}
		return INSTANCE;
	}

	/**
	 *
	 * @return Injector
	 */
	public Injector getInjector() {
		return this.injector;
	}

	/**
	 *
	 * @param packageName
	 * @return boolean
	 */
	private boolean isExcludedPackage(final Package packageName) {
		return packageName == null
				|| packageName.getName().startsWith("sun.")
				|| packageName.getName().startsWith("com.sun.")
				|| packageName.getName().startsWith("jdk.")
				|| packageName.getName().startsWith("java.")
				|| packageName.getName().startsWith("javax.");
	}

	/**
	 *
	 * @param packageName
	 * @return Set<Class<?>>
	 */
	private Set<Class<?>> findAllClasses(final String packageName) {
		final Reflections reflections = new Reflections(packageName, Scanners.SubTypes.filterResultsBy(s -> true));
		return reflections.getSubTypesOf(Object.class).stream().collect(Collectors.toSet());
	}

	/**
	 *
	 */
	private void discover() {
		for (final Package package1 : Package.getPackages()) {
			this.discover(package1);
		}
	}

	/**
	 *
	 * @param package1
	 */
	private void discover(final Package package1) {
		if (this.isExcludedPackage(package1)) {
			return;
		}

		LOGGER.debug("Scanning {}", package1.getName());

		this.findAllClasses(package1.getName()).stream().forEach(c -> this.discover(c));
	}

	/**
	 *
	 * @param clazz
	 */
	private <T> void discover(final Class<T> clazz) {
		if (clazz.isInterface()) {
			final Reflections reflections = new Reflections(clazz.getPackageName(), Scanners.SubTypes.filterResultsBy(s -> true));
			reflections.getSubTypesOf(clazz).stream().forEach(impl -> this.applicationScope.put(impl, clazz));
		}
	}

	/**
	 * @return the applicationScope
	 */
	public Map<Class<?>, Class<?>> getApplicationScope() {
		return new HashMap<>(this.applicationScope);
	}

	/**
	 *
	 * @param clazz
	 * @return T
	 * @throws InvocationException
	 */
	@SuppressWarnings("unchecked")
	public <T> T getObjectInstance(final Class<T> clazz) throws InvocationException {
		final T instance;

		if (clazz.getDeclaredAnnotation(Singleton.class) != null) {
			if (this.singletonInstance.containsKey(clazz)) {
				instance = (T)this.singletonInstance.get(clazz);
			} else {
				try {
					instance = clazz.getDeclaredConstructor().newInstance();
					this.singletonInstance.put(clazz, instance);
				} catch (final Exception e) {
					throw new InvocationException(e);
				}
			}
		} else {
			try {
				instance = clazz.getDeclaredConstructor().newInstance();
			} catch (final Exception e) {
				throw new InvocationException(e);
			}
		}

		return instance;
	}

	/**
	 *
	 * @param instance
	 * @return Object
	 */
	public Object initComponent(final Object instance) {
		try {
			final Method initMethod = this.getInjector().getClass().getDeclaredMethod("initBean", Object.class);
			if (initMethod.trySetAccessible()) {
				return initMethod.invoke(this.injector, instance);
			}
		} catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return instance;
	}

}
