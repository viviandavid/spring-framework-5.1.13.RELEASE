package LSD.factorybeantest;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

/**
 * @author LSD
 * @date 2021/3/9 19:27
 */
@Component
public class FactoryBeanLean implements FactoryBean<FactoryBeanService> {

	@Override
	public FactoryBeanService getObject() throws Exception {
		return new FactoryBeanServiceImpl();
	}

	@Override
	public Class<?> getObjectType() {
		return FactoryBeanService.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}
