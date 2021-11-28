package com.github.matschieu.ioc.core.beans;

import javax.inject.Named;

import com.github.matschieu.ioc.core.qualifiers.BeanQualifier;

/**
 *
 * @author Matschieu
 *
 */
@Named(value = "NamedAndQualifiedBean1")
@BeanQualifier
public class NamedAndQualifiedBeanImpl1 implements NamedAndQualifiedBean {

}
