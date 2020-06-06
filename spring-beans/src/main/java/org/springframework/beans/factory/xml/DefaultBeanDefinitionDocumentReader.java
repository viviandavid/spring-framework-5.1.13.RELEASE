/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the {@link BeanDefinitionDocumentReader} interface that
 * reads bean definitions according to the "spring-beans" DTD and XSD format
 * (Spring's default XML bean definition format).
 *
 * <p>The structure, elements, and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). {@code <beans>} does not need to be the root
 * element of the XML document: this class will parse all bean definition elements
 * in the XML file, regardless of the actual root element.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 18.12.2003
 */
public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {

	public static final String BEAN_ELEMENT = BeanDefinitionParserDelegate.BEAN_ELEMENT;

	public static final String NESTED_BEANS_ELEMENT = "beans";

	public static final String ALIAS_ELEMENT = "alias";

	public static final String NAME_ATTRIBUTE = "name";

	public static final String ALIAS_ATTRIBUTE = "alias";

	public static final String IMPORT_ELEMENT = "import";

	public static final String RESOURCE_ATTRIBUTE = "resource";

	public static final String PROFILE_ATTRIBUTE = "profile";


	protected final Log logger = LogFactory.getLog(getClass());

	@Nullable
	private XmlReaderContext readerContext;

	@Nullable
	private BeanDefinitionParserDelegate delegate;


	/**
	 * This implementation parses bean definitions according to the "spring-beans" XSD
	 * (or DTD, historically).
	 * <p>Opens a DOM Document; then initializes the default settings
	 * specified at the {@code <beans/>} level; then parses the contained bean definitions.
	 */
	@Override
	public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
		this.readerContext = readerContext;
		/**
		 * 拿到了一个漂亮的DOM树，可以开始注册Bean了
		 * 注意这里只是一个配置文件哦，可以看到之前进来的是一个for循环
		 */
		doRegisterBeanDefinitions(doc.getDocumentElement());
	}

	/**
	 * Return the descriptor for the XML resource that this parser works on.
	 */
	protected final XmlReaderContext getReaderContext() {
		Assert.state(this.readerContext != null, "No XmlReaderContext available");
		return this.readerContext;
	}

	/**
	 * Invoke the {@link org.springframework.beans.factory.parsing.SourceExtractor}
	 * to pull the source metadata from the supplied {@link Element}.
	 */
	@Nullable
	protected Object extractSource(Element ele) {
		return getReaderContext().extractSource(ele);
	}


	/**
	 * Register each bean definition within the given root {@code <beans/>} element.
	 */
	@SuppressWarnings("deprecation")  // for Environment.acceptsProfiles(String...)
	protected void doRegisterBeanDefinitions(Element root) {
		// Any nested <beans> elements will cause recursion in this method. In
		// order to propagate and preserve <beans> default-* attributes correctly,
		// keep track of the current (parent) delegate, which may be null. Create
		// the new (child) delegate with a reference to the parent for fallback purposes,
		// then ultimately reset this.delegate back to its original (parent) reference.
		// this behavior emulates a stack of delegates without actually necessitating one.
		/**
		 * BeanDefinitionParserDelegate 负责解析 Bean 定义，
		 * 这里为什么要定义一个 parent? 看到后面就知道了，是递归问题，
		 *  因为 <beans /> 内部是可以定义 <beans /> 的，所以这个方法的 root 其实不一定就是 xml 的根节点，
		 *  也可以是嵌套在里面的 <beans /> 节点，从源码分析的角度，我们当做根节点就好了
		 */
		BeanDefinitionParserDelegate parent = this.delegate;
		this.delegate = createDelegate(getReaderContext(), root, parent);

		if (this.delegate.isDefaultNamespace(root)) {
			/**
			 * 根节点 <beans ... profile="dev" /> 中的 profile 是否是当前环境需要的，
			 * 如果当前环境配置的 profile 不包含此 profile，那就直接 return 了，不对此 <beans /> 解析
			 */
			String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
			if (StringUtils.hasText(profileSpec)) {
				String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
						profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
				// We cannot use Profiles.of(...) since profile expressions are not supported
				// in XML config. See SPR-12458 for details.
				if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Skipped XML bean definition file due to specified profiles [" + profileSpec +
								"] not matching: " + getReaderContext().getResource());
					}
					return;
				}
			}
		}

		/**
		 * 两个钩子，给子类用的。现在没有用到，直接跳过
		 */
		preProcessXml(root);
		/**
		 * default namespace
		 * 根级别的element，"import", "alias", "bean", "beans"
		 */
		parseBeanDefinitions(root, this.delegate);
		postProcessXml(root);

		this.delegate = parent;
	}

	protected BeanDefinitionParserDelegate createDelegate(
			XmlReaderContext readerContext, Element root, @Nullable BeanDefinitionParserDelegate parentDelegate) {

		BeanDefinitionParserDelegate delegate = new BeanDefinitionParserDelegate(readerContext);
		delegate.initDefaults(root, parentDelegate);
		return delegate;
	}

	/**
	 * Parse the elements at the root level in the document:
	 * "import", "alias", "bean".
	 * @param root the DOM root element of the document
	 */
	protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
		if (delegate.isDefaultNamespace(root)) {
			NodeList nl = root.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node instanceof Element) {
					Element ele = (Element) node;
					if (delegate.isDefaultNamespace(ele)) {
						/**
						 * 默认元素的解析 import alias bean beans
						 */
						parseDefaultElement(ele, delegate);
					}
					else {
						/**
						 * 这里就是通用元素解析
						 * 包括：<mvc />、<task />、<context />、<aop />等
						 */
						delegate.parseCustomElement(ele);
					}
				}
			}
		}
		else {
			delegate.parseCustomElement(root);
		}
	}

	private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
		// import 标签
		if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
			importBeanDefinitionResource(ele);
		}
		// alias 标签
		else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
			processAliasRegistration(ele);
		}
		// bean 标签
		else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
			/**
			 * 重点看一下bean标签的解析
			 */
			processBeanDefinition(ele, delegate);
		}
		// beans 标签
		else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
			// recurse
			doRegisterBeanDefinitions(ele);
		}
	}

	/**
	 * Parse an "import" element and load the bean definitions
	 * from the given resource into the bean factory.
	 */
	protected void importBeanDefinitionResource(Element ele) {
		String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
		if (!StringUtils.hasText(location)) {
			getReaderContext().error("Resource location must not be empty", ele);
			return;
		}

		// Resolve system properties: e.g. "${user.dir}"
		location = getReaderContext().getEnvironment().resolveRequiredPlaceholders(location);

		Set<Resource> actualResources = new LinkedHashSet<>(4);

		// Discover whether the location is an absolute or relative URI
		boolean absoluteLocation = false;
		try {
			absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
		}
		catch (URISyntaxException ex) {
			// cannot convert to an URI, considering the location relative
			// unless it is the well-known Spring prefix "classpath*:"
		}

		// Absolute or relative?
		if (absoluteLocation) {
			try {
				int importCount = getReaderContext().getReader().loadBeanDefinitions(location, actualResources);
				if (logger.isTraceEnabled()) {
					logger.trace("Imported " + importCount + " bean definitions from URL location [" + location + "]");
				}
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to import bean definitions from URL location [" + location + "]", ele, ex);
			}
		}
		else {
			// No URL -> considering resource location as relative to the current file.
			try {
				int importCount;
				Resource relativeResource = getReaderContext().getResource().createRelative(location);
				if (relativeResource.exists()) {
					importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);
					actualResources.add(relativeResource);
				}
				else {
					String baseLocation = getReaderContext().getResource().getURL().toString();
					importCount = getReaderContext().getReader().loadBeanDefinitions(
							StringUtils.applyRelativePath(baseLocation, location), actualResources);
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Imported " + importCount + " bean definitions from relative location [" + location + "]");
				}
			}
			catch (IOException ex) {
				getReaderContext().error("Failed to resolve current resource location", ele, ex);
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error(
						"Failed to import bean definitions from relative location [" + location + "]", ele, ex);
			}
		}
		Resource[] actResArray = actualResources.toArray(new Resource[0]);
		getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
	}

	/**
	 * Process the given alias element, registering the alias with the registry.
	 */
	protected void processAliasRegistration(Element ele) {
		String name = ele.getAttribute(NAME_ATTRIBUTE);
		String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
		boolean valid = true;
		if (!StringUtils.hasText(name)) {
			getReaderContext().error("Name must not be empty", ele);
			valid = false;
		}
		if (!StringUtils.hasText(alias)) {
			getReaderContext().error("Alias must not be empty", ele);
			valid = false;
		}
		if (valid) {
			try {
				getReaderContext().getRegistry().registerAlias(name, alias);
			}
			catch (Exception ex) {
				getReaderContext().error("Failed to register alias '" + alias +
						"' for bean with name '" + name + "'", ele, ex);
			}
			getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
		}
	}

	/**
	 * Process the given bean element, parsing the bean definition
	 * and registering it with the registry.
	 */
	protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
		/**
		 * 将 <bean /> 节点中的信息提取出来，然后封装到一个 BeanDefinitionHolder 中
		 * Property	 bean中的属性定义
		 * class	类的全限定名
		 * name	可指定 id、name(用逗号、分号、空格分隔)
		 * scope	作用域
		 * constructor arguments	指定构造参数
		 * properties	设置属性的值
		 * autowiring mode	no(默认值)、byName、byType、 constructor
		 * lazy-initialization mode	是否懒加载(如果被非懒加载的bean依赖了那么其实也就不能懒加载了)
		 * initialization method	bean 属性设置完成后，会调用这个方法
		 * destruction method	bean 销毁后的回调方法
		 *
		 *下面是一个bean的定义
		 * <bean id="exampleBean" name="name1, name2, name3" class="com.javadoop.ExampleBean"
		 *       scope="singleton" lazy-init="true" init-method="init" destroy-method="cleanup">
		 *
		 *     <!-- 可以用下面三种形式指定构造参数 -->
		 *   <constructor-arg type="int" value="7500000"/>
		 *     <constructor-arg name="years" value="7500000"/>
		 *     <constructor-arg index="0" value="7500000"/>
		 *
		 *     <!-- property 的几种情况 -->
		 *     <property name="beanOne">
		 *         <ref bean="anotherExampleBean"/>
		 *     </property>
		 *     <property name="beanTwo" ref="yetAnotherBean"/>
		 *     <property name="integerProperty" value="1"/>
		 * </bean>
		 *
		 * bdHolder实例 已经包含class name id alias等类的属性了
		 */
		BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
		if (bdHolder != null) {
			/**
			 * 如果有自定义属性的话，进行相应的解析
			 */
			bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
			try {
				// Register the final decorated instance.
				/**
				 * 这一步 将解析后的bdHolder进行注册
				 */
				BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to register bean definition with name '" +
						bdHolder.getBeanName() + "'", ele, ex);
			}
			// Send registration event.
			/**
			 * 注册完成以后，发送事件，这里先跳过
			 */
			getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
		}
	}


	/**
	 * Allow the XML to be extensible by processing any custom element types first,
	 * before we start to process the bean definitions. This method is a natural
	 * extension point for any other custom pre-processing of the XML.
	 * <p>The default implementation is empty. Subclasses can override this method to
	 * convert custom elements into standard Spring bean definitions, for example.
	 * Implementors have access to the parser's bean definition reader and the
	 * underlying XML resource, through the corresponding accessors.
	 * @see #getReaderContext()
	 */
	protected void preProcessXml(Element root) {
	}

	/**
	 * Allow the XML to be extensible by processing any custom element types last,
	 * after we finished processing the bean definitions. This method is a natural
	 * extension point for any other custom post-processing of the XML.
	 * <p>The default implementation is empty. Subclasses can override this method to
	 * convert custom elements into standard Spring bean definitions, for example.
	 * Implementors have access to the parser's bean definition reader and the
	 * underlying XML resource, through the corresponding accessors.
	 * @see #getReaderContext()
	 */
	protected void postProcessXml(Element root) {
	}

}
