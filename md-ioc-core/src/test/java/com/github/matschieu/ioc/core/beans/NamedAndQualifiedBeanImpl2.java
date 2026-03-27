package com.github.matschieu.ioc.core.beans;

import jakarta.inject.Named;

import com.github.matschieu.ioc.core.qualifiers.BeanQualifier;

/**
 *
 * @author Matschieu
 *
 */
@Named(value = "NamedAndQualifiedBean2")
@BeanQualifier
public class NamedAndQualifiedBeanImpl2 implements NamedAndQualifiedBean {

}
