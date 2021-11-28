package com.github.matschieu.ioc.core;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.matschieu.ioc.core.exceptions.DefinitionException;
import com.github.matschieu.ioc.core.exceptions.InvocationException;

/**
 *
 * @author Matschieu
 *
 */
public class BeanHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanHandler.class);

	/**
	 *
	 */
	private BeanHandler() {}

	/**
	 *
	 * @param obj
	 * @throws InvocationException
	 * @throws DefinitionException
	 */
	public static void handlePostConstruct(final Object obj) throws InvocationException, DefinitionException {
		if (obj == null) {
			return;
		}

		final List<Method> methods = Arrays.asList(obj.getClass().getDeclaredMethods()).stream().filter(m -> m.getAnnotation(PostConstruct.class) != null).collect(Collectors.toList());

		if (methods.size() == 0) {
			return;
		}

		if (methods.size() > 1) {
			throw new DefinitionException(String.format("Cannot have more than one post construct method annotated with @%s for class %s", PostConstruct.class.getSimpleName(), obj.getClass().getName()));
		}

		final Method postConstructMethod = methods.get(0);

		if (postConstructMethod.getReturnType() != void.class) {
			throw new DefinitionException(String.format("Method %s defined on class %s is not defined according to the specification. It is annotated with %s but it does not have a void return type", postConstructMethod.getName(), obj.getClass().getName(), PostConstruct.class.getSimpleName()));
		}

		if (postConstructMethod.getParameterCount() > 0) {
			throw new DefinitionException(String.format("Method %s defined on class %s is not defined according to the specification. It is annotated with %s but it does not have zero parameters", postConstructMethod.getName(), obj.getClass().getName(), PostConstruct.class.getSimpleName()));
		}

		if (!postConstructMethod.trySetAccessible()) {
			throw new InvocationException(String.format("Method %s defined on class %s is not accessible", postConstructMethod.getName(), obj.getClass().getName()));
		}

		LOGGER.debug("Calling post construct method: {}.{}()", obj.getClass().getName(), postConstructMethod.getName());

		try {
			postConstructMethod.invoke(obj);
		} catch (final Exception e) {
			throw new InvocationException(e);
		}
	}

}