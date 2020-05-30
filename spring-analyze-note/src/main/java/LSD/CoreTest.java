package LSD;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CoreTest {

	public static void main(String[] args) {
//		AnnotationConfigApplicationContext applicationContext=
//				new AnnotationConfigApplicationContext(AppConfig.class);
//		DemoDao bean = applicationContext.getBean(DemoDao.class);
//		bean.query();
//		System.out.println(bean.getClass());

		ClassPathXmlApplicationContext xmlApplicationContext =
				new ClassPathXmlApplicationContext("config.xml");
		SimpleBean simpleBean = xmlApplicationContext.getBean(SimpleBean.class);
		simpleBean.send();
		xmlApplicationContext.close();

		System.out.println("饿哦挖掘佛分解挖偶就仸");
		//test submiting

//		System.setProperty("spring","classpath");
//		ClassPathXmlApplicationContext context =
//				new ClassPathXmlApplicationContext("${spring}:config.xml");
//		context.getBean(SimpleBean.class);
	}
}
