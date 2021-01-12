package LSD.udf;

import jdk.nashorn.internal.objects.annotations.Setter;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author LSD
 * @date 2021/1/11 19:57
 */
public class AnnotationScanner extends ClassPathBeanDefinitionScanner {

	private Class<? extends Annotation> selfAnnotationClazz;

	private static Class<? extends Annotation> staticTempAnnotationClazz = null;

	public static synchronized AnnotationScanner getScanner(BeanDefinitionRegistry registry,Class<? extends Annotation> clazz){
		staticTempAnnotationClazz = clazz;
		AnnotationScanner scanner = new AnnotationScanner(registry);
		scanner.setSelfAnnotationClazz(clazz);
		return scanner;
	}

	public AnnotationScanner(BeanDefinitionRegistry registry) {
		super(registry);
	}

	@Override
	protected void registerDefaultFilters() {
		// 这里添加扫描的bean
		super.registerDefaultFilters();
		this.addIncludeFilter(new AnnotationTypeFilter(staticTempAnnotationClazz));
	}

	@Override
	protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
		return super.doScan(basePackages);
	}

	public Class<? extends Annotation> getSelfAnnotationClazz() {
		return selfAnnotationClazz;
	}

	public void setSelfAnnotationClazz(Class<? extends Annotation> selfAnnotationClazz) {
		this.selfAnnotationClazz = selfAnnotationClazz;
	}

	@Override
	protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
		return super.isCandidateComponent(metadataReader);
	}

	@Override
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		return super.isCandidateComponent(beanDefinition)
				&& beanDefinition.getMetadata().hasAnnotation(this.selfAnnotationClazz.getName());
	}
}
