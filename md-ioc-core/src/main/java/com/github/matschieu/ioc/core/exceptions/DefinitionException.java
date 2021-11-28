package com.github.matschieu.ioc.core.exceptions;

/**
 *
 * @author Matschieu
 *
 */
public class DefinitionException extends Exception {

	private static final long serialVersionUID = 4447221023219999062L;

	/**
	 *
	 */
	public DefinitionException() {}

	/**
	 *
	 * @param message
	 */
	public DefinitionException(final String message) {
		super(message);
	}

	/**
	 *
	 * @param cause
	 */
	public DefinitionException(final Throwable cause) {
		super(cause);
	}

	/**
	 *
	 * @param message
	 * @param cause
	 */
	public DefinitionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 *
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public DefinitionException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
