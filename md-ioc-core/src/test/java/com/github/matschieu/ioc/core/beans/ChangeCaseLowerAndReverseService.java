package com.github.matschieu.ioc.core.beans;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 *
 * @author Matschieu
 *
 */
@Named("ChangeCaseLowerAndReverseService")
@Alternative
public class ChangeCaseLowerAndReverseService implements ChangeCaseAndReverseService {

	@Inject
	private ReverseService reverseService;

	private boolean activated = false;

	@PostConstruct
	public void postConstruct() {
		this.activated = this.reverseService != null;
	}

	@Override
	public String changeCaseAndReverse(final String str) {
		return this.activated ? this.reverseService.reverse(str != null ? str.toLowerCase() : str) : str;
	}

}
