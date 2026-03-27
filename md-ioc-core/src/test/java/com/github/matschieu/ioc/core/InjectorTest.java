package com.github.matschieu.ioc.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 *
 * @author Matschieu
 *
 */
class InjectorTest {

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

	@BeforeEach
	void init() {
		Container.get().initComponent(this);
		this.injector = Container.get().getInjector();
	}

	@Test
	void testNullInjection() throws Exception {
		assertNull(this.injector.inject(null));
	}

	@Test
	void testInterfaceInjection() throws Exception {
		assertNull(this.injector.inject(LonelyInterface.class));
	}

	@Test
	void testClassInjection() throws Exception {
		final InjectorTest test = this.injector.inject(InjectorTest.class);
		assertNotNull(test);
		assertTrue(test instanceof InjectorTest);
	}

	@Test
	void testInjection() throws Exception {
		final ChangeCaseAndReverseService bean = this.injector.inject(ChangeCaseAndReverseService.class);

		assertNotNull(bean);
		assertTrue(bean instanceof ChangeCaseUpperAndReverseService);
		assertEquals("TSET", bean.changeCaseAndReverse("test"));
	}

	@Test
	void testSingletonInjection() throws Exception {
		final ChangeCaseAndReverseService bean1 = this.injector.inject(ChangeCaseAndReverseService.class);
		final ChangeCaseAndReverseService bean2 = this.injector.inject(ChangeCaseAndReverseService.class);

		assertFalse(bean1 == bean2);

		final SingletonBean singleton1 = this.injector.inject(SingletonBean.class);
		final SingletonBean singleton2 = this.injector.inject(SingletonBean.class);

		assertTrue(singleton1 == singleton2);
	}

	@Test
	void testInjectionUsingAnnotation() {
		assertNotNull(this.bean1);
		assertTrue(this.bean1 instanceof ChangeCaseUpperAndReverseService);
		assertEquals("TSET", this.bean1.changeCaseAndReverse("test"));
	}

	@Test
	void testInjectionUsingNamedAnnotation() {
		assertNotNull(this.bean2);
		assertTrue(this.bean2 instanceof ChangeCaseLowerAndReverseService);
		assertEquals("tset", this.bean2.changeCaseAndReverse("TEST"));
	}

	@Test
	void testInjectionUsingQualifierAnnotation() {
		assertNotNull(this.bean3);
		assertTrue(this.bean3 instanceof QualifiedBeanImpl);
	}

	@Test
	void testSingletonInjectionUsingAnnotation() {
		assertTrue(this.singleton1 == this.singleton2);
	}

	@Test
	void testInjectionUsingNamed() throws Exception {
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

		assertNotNull(bean);
		assertTrue(bean instanceof ChangeCaseLowerAndReverseService);
		assertEquals("tset", bean.changeCaseAndReverse("TEST"));
	}

	@Test
	void testInjectionUsingQualifier() throws Exception {
		final QualifiedBean bean = this.injector.inject(QualifiedBean.class, () -> BeanQualifier.class);

		assertNotNull(bean);
		assertTrue(bean instanceof QualifiedBeanImpl);
	}

	@Test
	void testInjectionUsingNamedAndQualifier() throws Exception {
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

		assertNotNull(bean);
		assertTrue(bean instanceof NamedAndQualifiedBeanImpl1);
	}

	@Test
	void testInjectionUsingMultiNamed() throws Exception {
		assertThrows(IllegalArgumentException.class, () ->
			this.injector.inject(MultiNamedBean.class, new Named() {
				@Override
				public Class<? extends Annotation> annotationType() {
					return Named.class;
				}
				@Override
				public String value() {
					return "MultiNamedBean";
				}
			})
		);
	}

	@Test
	void testInjectionUsingMultiQualifier() throws Exception {
		assertThrows(IllegalArgumentException.class, () ->
			this.injector.inject(MultiQualifiedBean.class, () -> MultiQualifier.class)
		);
	}

	@Test
	void testInjectionUsingBadQualifier() throws Exception {
		assertNull(this.injector.inject(QualifiedBean.class, () -> MultiQualifier.class));
	}

	@Test
	void testMultiDefault() throws Exception {
		assertThrows(IllegalArgumentException.class, () ->
			this.injector.inject(MultiDefault.class)
		);
	}

	@Test
	void testBadPostConstruct() throws Exception {
		final BadPostConstruct1 instance1 = this.injector.inject(BadPostConstruct1.class);
		assertFalse(instance1.isPostConstructDone());

		final BadPostConstruct2 instance2 = this.injector.inject(BadPostConstruct2.class);
		assertFalse(instance2.isPostConstructDone());
	}

	@Test
	void testMultiPostConstruct() throws Exception {
		final MultiPostConstruct instance = this.injector.inject(MultiPostConstruct.class);
		assertFalse(instance.isPostConstruct1Done());
		assertFalse(instance.isPostConstruct2Done());
	}

}
