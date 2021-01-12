package LSD;

import LSD.udf.SelfService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;


public class CoreTest {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext applicationContext=
				new AnnotationConfigApplicationContext(AppConfig.class);
		DemoDao bean = applicationContext.getBean(DemoDao.class);
		bean.query();
		System.out.println(bean.getClass());

		SelfService service = (SelfService) applicationContext.getBean("selfService");
		System.out.println(service.getName());

//		ClassPathXmlApplicationContext xmlApplicationContext =
//				new ClassPathXmlApplicationContext("config.xml");
//		SimpleBean simpleBean = xmlApplicationContext.getBean(SimpleBean.class);
//		simpleBean.send();
//		xmlApplicationContext.close();


//		System.setProperty("spring","classpath");
//		ClassPathXmlApplicationContext context =
//				new ClassPathXmlApplicationContext("${spring}:config.xml");
//		context.getBean(SimpleBean.class);
	}
}
