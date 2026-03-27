package com.github.matschieu.ioc.core.beans;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;

/**
 *
 * @author Matschieu
 *
 */
@Default
public class ChangeCaseUpperAndReverseService implements ChangeCaseAndReverseService {

	@Inject
	private ReverseService reverseService;

	private boolean activated = false;

	@PostConstruct
	public void postConstruct() {
		this.activated = this.reverseService != null;
	}

	@Override
	public String changeCaseAndReverse(final String str) {
		return this.activated ? this.reverseService.reverse(str != null ? str.toUpperCase() : str) : str;
	}

}
