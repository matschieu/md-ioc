package com.github.matschieu.ioc.core.exceptions;

/**
 *
 * @author Matschieu
 *
 */
public class InjectionException extends Exception {

	private static final long serialVersionUID = 3209569823516788213L;

	/**
	 *
	 */
	public InjectionException() {}

	/**
	 *
	 * @param message
	 */
	public InjectionException(final String message) {
		super(message);
	}

	/**
	 *
	 * @param cause
	 */
	public InjectionException(final Throwable cause) {
		super(cause);
	}

	/**
	 *
	 * @param message
	 * @param cause
	 */
	public InjectionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 *
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public InjectionException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
