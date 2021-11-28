package com.github.matschieu.ioc.core.beans;

import javax.annotation.PostConstruct;

/**
 *
 * @author Matschieu
 *
 */
public class BadPostConstruct1 {

	private boolean postConstructDone = false;

	@PostConstruct
	public void postConstruct(final String str) {
		this.postConstructDone = true;
	}

	public boolean isPostConstructDone() {
		return this.postConstructDone;
	}

}
