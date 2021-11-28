package com.github.matschieu.ioc.core.beans;

import javax.annotation.PostConstruct;

/**
 *
 * @author Matschieu
 *
 */
public class ReverseServiceImpl implements ReverseService {

	private boolean activated = false;

	@PostConstruct
	public void postConstruct() {
		this.activated = true;
	}

	@Override
	public String reverse(final String str) {
		return this.activated && str != null && !str.isBlank() ? new StringBuilder(str).reverse().toString() : str;
	}

}
