package com.github.matschieu.ioc.core.exceptions;

/**
 *
 * @author Matschieu
 *
 */
public class InvocationException extends Exception {

	private static final long serialVersionUID = 457631707643536311L;

	/**
	 *
	 */
	public InvocationException() {}

	/**
	 *
	 * @param message
	 */
	public InvocationException(final String message) {
		super(message);
	}

	/**
	 *
	 * @param cause
	 */
	public InvocationException(final Throwable cause) {
		super(cause);
	}

	/**
	 *
	 * @param message
	 * @param cause
	 */
	public InvocationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 *
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public InvocationException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
