package LSD.udf;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LSD
 * @date 2021/1/11 19:53
 */
@Component
public class ModelInital {

	@Autowired
	private ConfigurableListableBeanFactory beanFactory;

	@Autowired
	private ApplicationContext applicationContext;

	private static final String RESOURCE_PATTERN = "LSD";

	@PostConstruct
	public void init(){
		AnnotationScanner scanner = AnnotationScanner.getScanner((BeanDefinitionRegistry) beanFactory, MyService.class);
		scanner.doScan(RESOURCE_PATTERN).forEach(beanDefinitionHolder -> {
//			Object o = applicationContext.getBean(beanDefinitionHolder.getBeanName());
//			Class<?> clazz = o.getClass();
//			MyService myService = clazz.getAnnotation(MyService.class);
			String className = beanDefinitionHolder.getBeanName();
//			((BeanDefinitionRegistry) beanFactory).removeBeanDefinition(beanDefinitionHolder.getBeanName());
			GenericBeanDefinition beanDefinition = new GenericBeanDefinition(beanDefinitionHolder.getBeanDefinition());
//			beanDefinition.setBeanClass(myService.getClass());
			((BeanDefinitionRegistry) beanFactory).registerBeanDefinition(className,beanDefinition);
			//https://www.liangzl.com/get-article-detail-121995.html
		});

	}
}
