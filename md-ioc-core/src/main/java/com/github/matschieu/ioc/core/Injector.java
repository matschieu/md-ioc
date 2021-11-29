package com.github.matschieu.ioc.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
	 * @param qualifiers
	 * @return T
	 * @throws InvocationException
	 * @throws IllegalArgumentException
	 */
	public <T> T inject(final Class<T> clazz, final Annotation... qualifiers) throws InvocationException, IllegalArgumentException {
		T instance = null;

		try {
			instance = this.instanceOf(clazz, qualifiers);
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
	 * @param beanClass
	 * @param qualifiers
	 * @return boolean
	 */
	private boolean hasQualifiers(final Class<?> beanClass, final List<Annotation> qualifiers) {
		final List<Annotation> beanQualifiers = Arrays.asList(beanClass.getDeclaredAnnotations()).stream().filter(a -> this.isQualifier(a)).collect(Collectors.toList());

		if (beanQualifiers.size() != qualifiers.size()) {
			return false;
		}

		final var sortedBeanQualifiers = beanQualifiers.stream().map(a -> a.annotationType()).collect(Collectors.toList());
		final var sortedQualifiers = qualifiers.stream().map(a -> a.annotationType()).collect(Collectors.toList());

		for(final var annotation : sortedBeanQualifiers) {
			if (!sortedQualifiers.contains(annotation)) {
				return false;
			}
		}

		final var namedBeanQualifiers = beanQualifiers.stream().filter(a -> a.annotationType() == Named.class).map(a -> (Named)a).collect(Collectors.toList());
		final var namedQualifiers = qualifiers.stream().filter(a -> a.annotationType() == Named.class).map(a -> (Named)a).collect(Collectors.toList());

		if (namedBeanQualifiers.size() > 1 || namedQualifiers.size() > 1 || namedBeanQualifiers.size() != namedQualifiers.size()) {
			return false;
		}

		if (!namedBeanQualifiers.isEmpty() && !namedQualifiers.isEmpty() && !namedBeanQualifiers.get(0).value().equals(namedQualifiers.get(0).value())) {
			return false;
		}

		return true;
	}

	/**
	 *
	 * @param <T>
	 * @param interfaceClass
	 * @param implementations
	 * @param qualifiers
	 * @return T
	 * @throws IllegalArgumentException
	 */
	private <T> Class<? extends T> getByQualifiers(final Class<T> interfaceClass, final Set<Class<? extends T>> implementations, final List<Annotation> qualifiers) throws IllegalArgumentException {
		Class<? extends T> foundImplementation = null;

		for(final var impl : implementations) {
			if (this.hasQualifiers(impl, qualifiers)) {
				if (foundImplementation != null) {
					final StringBuilder qualifiersStr = new StringBuilder();

					qualifiers.forEach(a -> {
						qualifiersStr.append(" @").append(a.annotationType().getSimpleName());
					});

					throw new IllegalArgumentException(String.format("Ambiguous dependencies for type %s with qualifiers%s", interfaceClass.getName(), qualifiersStr.toString()));
					//throw new IllegalArgumentException(String.format("Unsatisfied dependencies for type %s with qualifiers%s", interfaceClass.getName(), qualifiersStr.toString()));
				}
				foundImplementation = impl;
			}
		}

		return foundImplementation;
	}

	/**
	 *
	 * @param <T>
	 * @param clazz
	 * @param qualifiers
	 * @return T
	 * @throws InvocationException
	 * @throws IllegalArgumentException
	 */
	private <T> T instanceOf(final Class<T> clazz, final Annotation... qualifiers) throws InvocationException, IllegalArgumentException {
		T instance = null;

		for(final Annotation annotation : qualifiers) {
			if (!this.isQualifier(annotation)) {
				throw new IllegalArgumentException(String.format("Bad qualifier @%s for class %s", annotation.annotationType().getSimpleName(), clazz.getName()));
			}
		}

		if (clazz != null) {
			@SuppressWarnings("unchecked")
			final Set<Class<? extends T>> implementations = this.container.getApplicationScope().entrySet().stream().filter(e -> e.getValue().equals(clazz)).map(e -> (Class<? extends T>)e.getKey()).collect(Collectors.toSet());
			Class<? extends T> foundImplementation = null;
			final List<Annotation> qualifierList = qualifiers.length > 0 ? Arrays.asList(qualifiers) : new ArrayList<>();

			if (implementations.size() == 0 && !clazz.isInterface()) {
				foundImplementation = clazz;
			}

			if (implementations.size() == 1) {
				foundImplementation = implementations.stream().findFirst().get();
			}

			if (foundImplementation != null && !qualifierList.isEmpty() && !this.hasQualifiers(foundImplementation, qualifierList)) {
				foundImplementation = null;
			}

			if (implementations.size() > 1) {
				if (qualifierList.isEmpty()) {
					qualifierList.add(() -> Default.class);
				}

				foundImplementation = this.getByQualifiers(clazz, implementations, qualifierList);
			}

			if (foundImplementation != null) {
				instance = this.container.getObjectInstance(foundImplementation);
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
	 * @return List<Annotation>
	 */
	private List<Annotation> getQualifiers(final Field field) {
		final List<Annotation> qualifiers = new ArrayList<>();
		for(final Annotation annotation : field.getDeclaredAnnotations()) {
			if (this.isQualifier(annotation)) {
				qualifiers.add(annotation);
			}
		}
		return qualifiers;
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

				final Object value = this.instanceOf(field.getType(), this.getQualifiers(field).toArray(new Annotation[0]));

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
