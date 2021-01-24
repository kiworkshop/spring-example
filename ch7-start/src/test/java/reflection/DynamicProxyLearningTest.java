package reflection;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.Test;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.NameMatchMethodPointcut;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;


class DynamicProxyLearningTest {

    @Test
    void simpleProxy() {
        Hello hello = new HelloUppercase(new HelloTarget());

        assertThat(hello.sayHi("ki")).isEqualTo("HI KI");
        assertThat(hello.sayHello("ki")).isEqualTo("HELLO KI");
        assertThat(hello.sayThankyou("ki")).isEqualTo("THANK YOU KI");
    }

    @Test
    void jDKSimpleProxy() {
        Hello hello = (Hello) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[]{Hello.class},
            new UppercaseHandler(new HelloTarget())
        );
        assertThat(hello.sayHi("ki")).isEqualTo("HI KI");
        assertThat(hello.sayHello("ki")).isEqualTo("HELLO KI");
        assertThat(hello.sayThankyou("ki")).isEqualTo("THANK YOU KI");
    }

    @Test
    void proxyFactoryBean() {
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(new HelloTarget());
        proxyFactoryBean.addAdvisor(new PointcutAdvisor() {
            @Override
            public Pointcut getPointcut() {
                NameMatchMethodPointcut nameMatchMethodPointcut = new NameMatchMethodPointcut();
                nameMatchMethodPointcut.setMappedName("sayH*");
                return nameMatchMethodPointcut;
            }

            @Override
            public Advice getAdvice() {
                return new UppercaseAdvice();
            }

            @Override
            public boolean isPerInstance() {
                return false;
            }
        });

        Hello proxied = (Hello) proxyFactoryBean.getObject();
        assertThat(proxied.sayHi("ki")).isEqualTo("HI KI");
        assertThat(proxied.sayHello("ki")).isEqualTo("HELLO KI");
        assertThat(proxied.sayThankyou("ki")).isEqualTo("Thank You ki");
    }

    private class UppercaseAdvice implements MethodInterceptor {
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            String ret = (String) invocation.proceed();
            return ret.toUpperCase();
        }
    }
}
