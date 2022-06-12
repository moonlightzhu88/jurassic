package com.jurassic.core.resource.xml;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 资源工厂配置文件的解析类
 * 
 * @author yzhu
 * 
 */
public class ResourceFactoryConfigParser {

	@SuppressWarnings("unchecked")
	public List<ResourceFactoryConfig> parseXml(URL url) throws Throwable {
		List<ResourceFactoryConfig> configs = new ArrayList<>();

		SAXReader reader = new SAXReader();
		Document document = reader.read(url);
		// 解析配置文件
		Element root = document.getRootElement();
		Iterator<Element> elements = root.elementIterator("resource");
		while (elements.hasNext()) {
			Element e = elements.next();
			Attribute id = e.attribute("id");
			Attribute clz = e.attribute("class");
			if (id == null || clz == null){
				continue;
			}
			ResourceFactoryConfig config = new ResourceFactoryConfig(
					id.getValue(), clz.getValue());
			Iterator<Element> properties = e.elementIterator("property");
			while (properties.hasNext()) {
				Element property = properties.next();
				Attribute name = property.attribute("name");
				config.setParam(name.getValue(), property.getText());
			}
			configs.add(config);
		}
		return configs;
	}

}
