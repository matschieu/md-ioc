package com.github.matschieu.ioc.core.beans;

import javax.annotation.PostConstruct;

/**
 *
 * @author Matschieu
 *
 */
public class BadPostConstruct2 {

	private boolean postConstructDone = false;

	@PostConstruct
	public boolean postConstruct() {
		this.postConstructDone = true;
		return this.postConstructDone;
	}

	public boolean isPostConstructDone() {
		return this.postConstructDone;
	}

}
