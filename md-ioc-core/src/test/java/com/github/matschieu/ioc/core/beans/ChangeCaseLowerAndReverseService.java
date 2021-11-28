package com.github.matschieu.ioc.core.beans;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;

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
