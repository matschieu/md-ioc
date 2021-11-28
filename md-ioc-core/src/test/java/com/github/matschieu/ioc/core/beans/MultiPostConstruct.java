package com.github.matschieu.ioc.core.beans;

import javax.annotation.PostConstruct;

/**
 *
 * @author Matschieu
 *
 */
public class MultiPostConstruct {

	private boolean postConstruct1Done = false;
	private boolean postConstruct2Done = false;

	@PostConstruct
	public void postConstruct1() {
		this.postConstruct1Done = true;
	}

	@PostConstruct
	public void postConstruct2() {
		this.postConstruct2Done = true;
	}

	public boolean isPostConstruct1Done() {
		return this.postConstruct1Done;
	}

	public boolean isPostConstruct2Done() {
		return this.postConstruct2Done;
	}

}
