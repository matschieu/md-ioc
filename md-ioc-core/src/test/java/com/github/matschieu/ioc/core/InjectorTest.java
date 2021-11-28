package com.github.matschieu.ioc.core;

import java.lang.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.matschieu.ioc.core.beans.BadPostConstruct1;
import com.github.matschieu.ioc.core.beans.BadPostConstruct2;
import com.github.matschieu.ioc.core.beans.ChangeCaseAndReverseService;
import com.github.matschieu.ioc.core.beans.ChangeCaseLowerAndReverseService;
import com.github.matschieu.ioc.core.beans.ChangeCaseUpperAndReverseService;
import com.github.matschieu.ioc.core.beans.LonelyInterface;
import com.github.matschieu.ioc.core.beans.MultiDefault;
import com.github.matschieu.ioc.core.beans.MultiNamedBean;
import com.github.matschieu.ioc.core.beans.MultiPostConstruct;
import com.github.matschieu.ioc.core.beans.MultiQualifiedBean;
import com.github.matschieu.ioc.core.beans.NamedAndQualifiedBean;
import com.github.matschieu.ioc.core.beans.NamedAndQualifiedBeanImpl1;
import com.github.matschieu.ioc.core.beans.QualifiedBean;
import com.github.matschieu.ioc.core.beans.QualifiedBeanImpl;
import com.github.matschieu.ioc.core.beans.SingletonBean;
import com.github.matschieu.ioc.core.exceptions.IllegalArgumentException;
import com.github.matschieu.ioc.core.qualifiers.BeanQualifier;
import com.github.matschieu.ioc.core.qualifiers.MultiQualifier;

/**
 *
 * @author Matschieu
 *
 */
public class InjectorTest {

	private Injector injector;

	@Inject
	private ChangeCaseAndReverseService bean1;

	@Inject
	@Named("ChangeCaseLowerAndReverseService")
	private ChangeCaseAndReverseService bean2;

	@Inject
	@BeanQualifier
	private QualifiedBean bean3;

	@Inject
	private SingletonBean singleton1;

	@Inject
	private SingletonBean singleton2;

	@Before
	public void init() {
		Container.get().initComponent(this);
		this.injector = Container.get().getInjector();
	}

	@Test
	public void testNullInjection() throws Exception {
		Assert.assertNull(this.injector.inject(null));
	}

	@Test
	public void testInterfaceInjection() throws Exception {
		Assert.assertNull(this.injector.inject(LonelyInterface.class));
	}

	@Test
	public void testClassInjection() throws Exception {
		final InjectorTest test = this.injector.inject(InjectorTest.class);
		Assert.assertNotNull(test);
		Assert.assertTrue(test instanceof InjectorTest);
	}

	@Test
	public void testInjection() throws Exception {
		final ChangeCaseAndReverseService bean = this.injector.inject(ChangeCaseAndReverseService.class);

		Assert.assertNotNull(bean);
		Assert.assertTrue(bean instanceof ChangeCaseUpperAndReverseService);
		Assert.assertEquals("TSET", bean.changeCaseAndReverse("test"));
	}

	@Test
	public void testSingletonInjection() throws Exception {
		final ChangeCaseAndReverseService bean1 = this.injector.inject(ChangeCaseAndReverseService.class);
		final ChangeCaseAndReverseService bean2 = this.injector.inject(ChangeCaseAndReverseService.class);

		Assert.assertFalse(bean1 == bean2);

		final SingletonBean singleton1 = this.injector.inject(SingletonBean.class);
		final SingletonBean singleton2 = this.injector.inject(SingletonBean.class);

		Assert.assertTrue(singleton1 == singleton2);
	}

	@Test
	public void testInjectionUsingAnnotation() {
		Assert.assertNotNull(this.bean1);
		Assert.assertTrue(this.bean1 instanceof ChangeCaseUpperAndReverseService);
		Assert.assertEquals("TSET", this.bean1.changeCaseAndReverse("test"));
	}

	@Test
	public void testInjectionUsingNamedAnnotation() {
		Assert.assertNotNull(this.bean2);
		Assert.assertTrue(this.bean2 instanceof ChangeCaseLowerAndReverseService);
		Assert.assertEquals("tset", this.bean2.changeCaseAndReverse("TEST"));
	}

	@Test
	public void testInjectionUsingQualifierAnnotation() {
		Assert.assertNotNull(this.bean3);
		Assert.assertTrue(this.bean3 instanceof QualifiedBeanImpl);
	}

	@Test
	public void testSingletonInjectionUsingAnnotation() {
		Assert.assertTrue(this.singleton1 == this.singleton2);
	}

	@Test
	public void testInjectionUsingNamed() throws Exception {
		final ChangeCaseAndReverseService bean = this.injector.inject(ChangeCaseAndReverseService.class, new Named() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return Named.class;
			}
			@Override
			public String value() {
				return "ChangeCaseLowerAndReverseService";
			}
		});

		Assert.assertNotNull(bean);
		Assert.assertTrue(bean instanceof ChangeCaseLowerAndReverseService);
		Assert.assertEquals("tset", bean.changeCaseAndReverse("TEST"));
	}

	@Test
	public void testInjectionUsingQualifier() throws Exception {
		final QualifiedBean bean = this.injector.inject(QualifiedBean.class, () -> BeanQualifier.class);

		Assert.assertNotNull(bean);
		Assert.assertTrue(bean instanceof QualifiedBeanImpl);
	}

	@Test
	public void testInjectionUsingNamedAndQualifier() throws Exception {
		final NamedAndQualifiedBean bean = this.injector.inject(NamedAndQualifiedBean.class, new Named() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return Named.class;
			}
			@Override
			public String value() {
				return "NamedAndQualifiedBean1";
			}
		}, () -> BeanQualifier.class);

		Assert.assertNotNull(bean);
		Assert.assertTrue(bean instanceof NamedAndQualifiedBeanImpl1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInjectionUsingMultiNamed() throws Exception {
		this.injector.inject(MultiNamedBean.class, new Named() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return Named.class;
			}
			@Override
			public String value() {
				return "MultiNamedBean";
			}
		});
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInjectionUsingMultiQualifier() throws Exception {
		this.injector.inject(MultiQualifiedBean.class, () -> MultiQualifier.class);
	}

	@Test
	public void testInjectionUsingBadQualifier() throws Exception {
		Assert.assertNull(this.injector.inject(QualifiedBean.class, () -> MultiQualifier.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMultiDefault() throws Exception {
		this.injector.inject(MultiDefault.class);
	}

	@Test
	public void testBadPostConstruct() throws Exception {
		final BadPostConstruct1 instance1 = this.injector.inject(BadPostConstruct1.class);
		Assert.assertFalse(instance1.isPostConstructDone());

		final BadPostConstruct2 instance2 = this.injector.inject(BadPostConstruct2.class);
		Assert.assertFalse(instance2.isPostConstructDone());
	}

	@Test
	public void testMultiPostConstruct() throws Exception {
		final MultiPostConstruct instance = this.injector.inject(MultiPostConstruct.class);
		Assert.assertFalse(instance.isPostConstruct1Done());
		Assert.assertFalse(instance.isPostConstruct2Done());
	}

}
