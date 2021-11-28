package com.github.matschieu.ioc.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.matschieu.ioc.core.exceptions.DefinitionException;
import com.github.matschieu.ioc.core.exceptions.IllegalArgumentException;
import com.github.matschieu.ioc.core.exceptions.InvocationException;

/**
 *
 * @author Matschieu
 *
 */
public class Injector {

	private static final Logger LOGGER = LoggerFactory.getLogger(Injector.class);

	private final Container container;

	/**
	 *
	 * @param container
	 */
	public Injector(final Container container) {
		this.container = container;
	}

	/**
	 *
	 * @param <T>
	 * @param clazz
	 * @param namedAnnotation
	 * @param qualifier
	 * @return T
	 * @throws InvocationException, IllegalArgumentException
	 */
	public <T> T inject(final Class<T> clazz, final Named namedAnnotation, final Annotation qualifier) throws InvocationException, IllegalArgumentException {
		T instance = null;

		try {
			instance = this.instanceOf(clazz, namedAnnotation, qualifier);
		} catch (InvocationException | IllegalArgumentException e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}

		if (instance != null) {
			try {
				this.initBean(instance);
			} catch (InvocationException | DefinitionException | IllegalArgumentException e) {
				LOGGER.warn(e.getMessage(), e);
			}
		}

		return instance;
	}

	/**
	 *
	 * @param clazz
	 * @return T
	 * @throws IllegalArgumentException
	 * @throws InvocationException
	 */
	public <T> T inject(final Class<T> clazz) throws InvocationException, IllegalArgumentException {
		return this.inject(clazz, null, null);
	}

	/**
	 *
	 * @param <T>
	 * @param clazz
	 * @param namedAnnotation
	 * @return T
	 * @throws IllegalArgumentException
	 * @throws InvocationException
	 */
	public <T> T inject(final Class<T> clazz, final Named namedAnnotation) throws InvocationException, IllegalArgumentException {
		return this.inject(clazz, namedAnnotation, null);
	}

	/**
	 *
	 * @param <T>
	 * @param clazz
	 * @param qualifier
	 * @return T
	 * @throws IllegalArgumentException
	 * @throws InvocationException
	 */
	public <T> T inject(final Class<T> clazz, final Annotation qualifier) throws InvocationException, IllegalArgumentException {
		return this.inject(clazz, null, qualifier);
	}

	/**
	 *
	 * @param impl
	 * @param namedAnnotation
	 * @param qualifier
	 * @return boolean
	 */
	private boolean isClassHasQualifiers(final Class<?> impl, final Named namedAnnotation, final Annotation qualifier) {
		boolean goodQualifier = false;
		boolean goodName = false;

		if (qualifier != null) {
			goodQualifier = impl.getDeclaredAnnotation(qualifier.annotationType()) != null;
		}
		if (namedAnnotation != null) {
			goodName = impl.getDeclaredAnnotation(Named.class) != null && namedAnnotation.value().equals(impl.getDeclaredAnnotation(Named.class).value());
		}

		if (qualifier != null && namedAnnotation != null) {
			return goodQualifier && goodName;
		}
		if (qualifier != null) {
			return goodQualifier;
		}
		if (namedAnnotation != null) {
			return goodName;
		}

		return namedAnnotation == null && qualifier == null;
	}

	/**
	 *
	 * @param <T>
	 * @param interfaceClass
	 * @param implementations
	 * @return Class<? extends T>
	 * @throws IllegalArgumentException
	 */
	private <T> Class<? extends T> getDefault(final Class<T> interfaceClass, final Set<Class<? extends T>> implementations) throws IllegalArgumentException {
		Class<? extends T> foundImplementation = null;

		for(final var impl : implementations) {
			if (Arrays.asList(impl.getInterfaces()).contains(interfaceClass) && impl.getDeclaredAnnotation(Default.class) != null) {
				if (foundImplementation != null) {
					throw new IllegalArgumentException(String.format("Ambiguous dependencies for type %s with qualifiers @Default", interfaceClass.getName()));
				}
				foundImplementation = impl;
			}
		}

		return foundImplementation;
	}

	/**
	 *
	 * @param <T>
	 * @param interfaceClass
	 * @param implementations
	 * @param namedAnnotation
	 * @param qualifier
	 * @return Class<? extends T>
	 * @throws IllegalArgumentException
	 */
	private <T> Class<? extends T> getByQualifier(final Class<T> interfaceClass, final Set<Class<? extends T>> implementations, final Named namedAnnotation, final Annotation qualifier) throws IllegalArgumentException {
		Class<? extends T> foundImplementation = null;

		for(final var impl : implementations) {
			if (Arrays.asList(impl.getInterfaces()).contains(interfaceClass) && this.isClassHasQualifiers(impl, namedAnnotation, qualifier)) {
				if (foundImplementation != null) {
					String qualifierStr = "";
					if (qualifier != null) {
						qualifierStr += " @" + qualifier.annotationType().getSimpleName();
					}
					if (namedAnnotation != null) {
						qualifierStr += " @" + namedAnnotation.getClass().getSimpleName();
					}
					throw new IllegalArgumentException(String.format("Unsatisfied dependencies for type %s with qualifiers%s", interfaceClass.getName(), qualifierStr));
				}
				foundImplementation = impl;
			}
		}

		return foundImplementation;
	}

	/**
	 *
	 * @param clazz
	 * @param namedAnnotation
	 * @param qualifier
	 * @return T
	 * @throws InvocationException
	 * @throws IllegalArgumentException
	 */
	private <T> T instanceOf(final Class<T> clazz, final Named namedAnnotation, final Annotation qualifier) throws InvocationException, IllegalArgumentException {
		T instance = null;
		final boolean useQualifiers = namedAnnotation != null || qualifier != null;

		if (qualifier != null && !this.isQualifier(qualifier)) {
			throw new IllegalArgumentException(String.format("Bad qualifier @%s for class %s", qualifier.annotationType().getSimpleName(), clazz.getName()));
		}

		if (clazz != null) {
			@SuppressWarnings("unchecked")
			final Set<Class<? extends T>> implementations = this.container.getApplicationScope().entrySet().stream().filter(e -> e.getValue().equals(clazz)).map(e -> (Class<? extends T>)e.getKey()).collect(Collectors.toSet());
			Class<? extends T> foundImplementation = null;

			if (implementations.size() == 0 && !clazz.isInterface()) {
				foundImplementation = clazz;
			}

			if (implementations.size() == 1) {
				foundImplementation = implementations.stream().findFirst().get();
			}

			if (foundImplementation != null && useQualifiers && !this.isClassHasQualifiers(foundImplementation, namedAnnotation, qualifier)) {
				foundImplementation = null;
			}

			if (implementations.size() > 1) {
				if (useQualifiers) {
					foundImplementation = this.getByQualifier(clazz, implementations, namedAnnotation, qualifier);
				} else {
					foundImplementation = this.getDefault(clazz, implementations);
				}
			}

			if (foundImplementation != null) {
				try {
					instance = foundImplementation.getDeclaredConstructor().newInstance();
				} catch (final Exception e) {
					throw new InvocationException(e);
				}
			}
		}

		LOGGER.info("Instance of {} -> {}", clazz, instance != null ? instance.getClass() : null);

		return instance;
	}

	/**
	 *
	 * @param annotation
	 * @return boolean
	 */
	private boolean isQualifier(final Annotation annotation) {
		return annotation != null && annotation.annotationType().getDeclaredAnnotation(Qualifier.class) != null;
	}

	/**
	 *
	 * @param field
	 * @return Annotation
	 */
	private Annotation getQualifier(final Field field) {
		for(final Annotation annotation : field.getDeclaredAnnotations()) {
			if (this.isQualifier(annotation) && annotation.annotationType() != Named.class) {
				return annotation;
			}
		}
		return null;
	}

	/**
	 *
	 * @param bean
	 * @return Object
	 * @throws InvocationException
	 * @throws DefinitionException
	 * @throws IllegalArgumentException
	 */
	private Object initBean(final Object bean) throws InvocationException, DefinitionException, IllegalArgumentException {
		if (bean == null) {
			return bean;
		}

		for(final Field field : bean.getClass().getDeclaredFields()) {
			if (field.getDeclaredAnnotation(Inject.class) != null) {
				LOGGER.debug("Injecting value in {}.{}", bean.getClass().getName(), field.getName());

				if (!field.trySetAccessible()) {
					LOGGER.error("Field {}.{} is not accessible", bean.getClass().getName(), field.getName());
				}

				final Object value = this.instanceOf(field.getType(), field.getDeclaredAnnotation(Named.class), this.getQualifier(field));

				try {
					LOGGER.debug("Setting {}.{} with value {}", bean.getClass().getName(), field.getName(), value != null ? value.getClass().getName() : null);
					field.set(bean, value);
				} catch (final Exception e) {
					LOGGER.error(e.getMessage(), e);
				}

				this.initBean(value);
			}
		}

		BeanHandler.handlePostConstruct(bean);

		return bean;
	}

}
