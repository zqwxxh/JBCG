package builder.framework.asset;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import cn.osworks.aos.core.typewrap.Dto;

/**
 * 生成dao,service,action的xml配置文件
 * @author hhcao
 * @date 2015年8月5日
 * @version 1.0
 */
public class XmlUtils {

	private String springString = "applicationContext";
	private String sepChar = "-";
	private String strutsString = "struts";
	
	public void buildXml(Dto dto) throws Exception{
		// 包名,比如com.fiberhome.module.is
		String packagepath = dto.getString("package");
		// 得到前缀名，比如得到is，因为配置文件都是类似于applicationContext-is-action.xml这种，需要通过前缀匹配
		String index = packagepath.substring(packagepath.lastIndexOf(".") + 1 ,packagepath.length());
		// 生成dao xml文件
		this.doDao(dto, index);
		// 生成service xml文件
		this.doService(dto, index);
		// 生成action xml文件
		this.doAction(dto, index);
		// 生成struts xml文件
		this.doStruts(dto, index);
	}
	
	/**
	 * 生成dao xml文件
	 * @param dto
	 * @param index
	 * @throws IOException 
	 */
	private void doDao(Dto dto, String index) throws Exception{
		// 首先判断是否已经存在该xml文件
		String outPath = dto.getString("outPath") + "/cfg/spring/";
		FileUtils.forceMkdir(new File(outPath));
		String fileName = outPath + springString + sepChar + index + sepChar + "dao.xml";
		File file=new File(fileName);  
		SAXBuilder sb = new SAXBuilder();
		// 不验证dtd
		sb.setEntityResolver(new NoOpEntityResolver());
		// 如果文件不存在，则新建一个xml文件
		if(!file.exists())    
		{    
			Element beans,bean;           
			beans = new Element("beans");
			bean = new Element("bean");
			bean.setAttribute("id", dto.getString("lowername") + "Dao");
			bean.setAttribute("class", dto.getString("package") + ".dao.impl." + dto.getString("upname") + "DaoImpl");
			bean.setAttribute("parent", "baseDao");
			beans.addContent(bean);
	        Document doc = sb.build(new File("src/builder/framework/resources/spring.xml"));  
	        doc.setRootElement(beans);
	        XMLOutputter XMLOut = new XMLOutputter("  ",true,"UTF-8");                               
	        XMLOut.output(doc,new FileOutputStream(fileName));
		}
		// 存在文件，则新增一个dao的bean信息到文件中
		else{
	        Document doc = null;
	        try {
	            doc = sb.build(fileName);
	        } catch (Exception e) {
	            System.out.print(fileName + "文件读取异常");
	        }
	        Element root=doc.getRootElement();
	        // 先判断这个dao是否已经存在于这个xml文件中，存在则不处理
	        boolean isexist = false;
	        List<Element> elements = root.getChildren();
	        for(Element bean : elements){
	        	String id = bean.getAttribute("id").getValue();
	        	if((dto.getString("lowername") + "Dao").equals(id)){
	        		isexist = true;
	        	}
	        }
	        // 如果不存在，则插入新增的dao信息到xml文件中
	        if(!isexist){
	        	Element bean=new Element("bean");
				bean.setAttribute("id", dto.getString("lowername") + "Dao");
				bean.setAttribute("class", dto.getString("package") + ".dao.impl." + dto.getString("upname") + "DaoImpl");
				bean.setAttribute("parent", "baseDao");
		        root.addContent(bean);
		        XMLOutputter out=new XMLOutputter();
		        out.output(doc,new FileOutputStream(fileName));
	        }
		}
		
		System.out.println("xml文件[" + fileName + "生成成功。");
	}
	
	/**
	 * 生成service xml文件
	 * @param dto
	 * @param index
	 * @throws IOException 
	 */
	private void doService(Dto dto, String index) throws Exception{
		// 首先判断是否已经存在该xml文件
		String outPath = dto.getString("outPath") + "/cfg/spring/";
		FileUtils.forceMkdir(new File(outPath));
		String fileName = outPath + springString + sepChar + index + sepChar + "service.xml";
		File file=new File(fileName); 
		SAXBuilder sb = new SAXBuilder();
		// 不验证dtd
		sb.setEntityResolver(new NoOpEntityResolver());
		// 如果文件不存在，则新建一个xml文件
		if(!file.exists())    
		{    
			Element beans,bean,property;           
			beans = new Element("beans");
			bean = new Element("bean");
			property = new Element("property");
			bean.setAttribute("id", dto.getString("lowername") + "Service");
			bean.setAttribute("class", dto.getString("package") + ".service.impl." + dto.getString("upname") + "ServiceImpl");
			bean.setAttribute("parent", "baseService");
			property.setAttribute("name", dto.getString("lowername") + "Dao");
			property.setAttribute("ref", dto.getString("lowername") + "Dao");
			bean.addContent(property);
			beans.addContent(bean);
			Document doc = sb.build(new File("src/builder/framework/resources/spring.xml"));  
	        doc.setRootElement(beans);
	        XMLOutputter XMLOut = new XMLOutputter("  ",true,"UTF-8");                               
	        XMLOut.output(doc,new FileOutputStream(fileName));
		}
		// 存在文件，则新增一个service的bean信息到文件中
		else{
	        Document doc = null;
	        try {
	            doc = sb.build(fileName);
	        } catch (Exception e) {
	            System.out.print(fileName + "文件读取异常");
	        }
	        Element root=doc.getRootElement();
	        // 先判断这个service是否已经存在于这个xml文件中，存在则不处理
	        boolean isexist = false;
	        List<Element> elements = root.getChildren();
	        for(Element bean : elements){
	        	String id = bean.getAttribute("id").getValue();
	        	if((dto.getString("lowername") + "Service").equals(id)){
	        		isexist = true;
	        	}
	        }
	        // 如果不存在，则插入新增的service信息到xml文件中
	        if(!isexist){
	        	Element bean = new Element("bean");
	        	Element property = new Element("property");
				bean.setAttribute("id", dto.getString("lowername") + "Service");
				bean.setAttribute("class", dto.getString("package") + ".service.impl." + dto.getString("upname") + "ServiceImpl");
				bean.setAttribute("parent", "baseService");
				property.setAttribute("name", dto.getString("lowername") + "Dao");
				property.setAttribute("ref", dto.getString("lowername") + "Dao");
				bean.addContent(property);
		        root.addContent(bean);
		        XMLOutputter out=new XMLOutputter();
		        out.output(doc,new FileOutputStream(fileName));
	        }
		}
		
		System.out.println("xml文件[" + fileName + "生成成功。");
	}
	
	/**
	 * 生成action xml文件
	 * @param dto
	 * @param index
	 * @throws IOException 
	 */
	private void doAction(Dto dto, String index) throws Exception{
		// 首先判断是否已经存在该xml文件
		String outPath = dto.getString("outPath") + "/cfg/spring/";
		FileUtils.forceMkdir(new File(outPath));
		String fileName = outPath + springString + sepChar + index + sepChar + "action.xml";
		File file=new File(fileName); 
		SAXBuilder sb = new SAXBuilder();
		// 不验证dtd
		sb.setEntityResolver(new NoOpEntityResolver());
		// 如果文件不存在，则新建一个xml文件
		if(!file.exists())    
		{    
			Element beans,bean,property;           
			beans = new Element("beans");
			bean = new Element("bean");
			property = new Element("property");
			bean.setAttribute("id", dto.getString("lowername") + "Action");
			bean.setAttribute("class", dto.getString("package") + ".action." + dto.getString("upname") + "Action");
			bean.setAttribute("parent", "platformBaseAction");
			bean.setAttribute("scope", "prototype");
			property.setAttribute("name", dto.getString("lowername") + "Service");
			property.setAttribute("ref", dto.getString("lowername") + "Service");
			bean.addContent(property);
			beans.addContent(bean);
			Document doc = sb.build(new File("src/builder/framework/resources/spring.xml"));  
	        doc.setRootElement(beans);
	        XMLOutputter XMLOut = new XMLOutputter("  ",true,"UTF-8");                               
	        XMLOut.output(doc,new FileOutputStream(fileName));
		}
		// 存在文件，则新增一个action的bean信息到文件中
		else{
	        Document doc = null;
	        try {
	            doc = sb.build(fileName);
	        } catch (Exception e) {
	            System.out.print(fileName + "文件读取异常");
	        }
	        Element root=doc.getRootElement();
	        // 先判断这个action是否已经存在于这个xml文件中，存在则不处理
	        boolean isexist = false;
	        List<Element> elements = root.getChildren();
	        for(Element bean : elements){
	        	String id = bean.getAttribute("id").getValue();
	        	if((dto.getString("lowername") + "Action").equals(id)){
	        		isexist = true;
	        	}
	        }
	        // 如果不存在，则插入新增的action信息到xml文件中
	        if(!isexist){
	        	Element bean = new Element("bean");
	        	Element property = new Element("property");
				bean.setAttribute("id", dto.getString("lowername") + "Action");
				bean.setAttribute("class", dto.getString("package") + ".action." + dto.getString("upname") + "Action");
				bean.setAttribute("parent", "platformBaseAction");
				bean.setAttribute("scope", "prototype");
				property.setAttribute("name", dto.getString("lowername") + "Service");
				property.setAttribute("ref", dto.getString("lowername") + "Service");
				bean.addContent(property);
		        root.addContent(bean);
		        XMLOutputter out=new XMLOutputter();
		        out.output(doc,new FileOutputStream(fileName));
	        }
		}
		
		System.out.println("xml文件[" + fileName + "生成成功。");
	}
	
	/**
	 * 生成struts xml文件
	 * @param dto
	 * @param index
	 * @throws IOException 
	 */
	private void doStruts(Dto dto, String index) throws Exception{
		// 首先判断是否已经存在该xml文件
		String outPath = dto.getString("outPath") + "/cfg/struts/";
		FileUtils.forceMkdir(new File(outPath));
		String fileName = outPath + strutsString + sepChar + index + ".xml";
		File file=new File(fileName);    
		SAXBuilder sb = new SAXBuilder();
		// 不验证dtd
		sb.setEntityResolver(new NoOpEntityResolver());
		// 如果文件不存在，则新建一个xml文件
		if(!file.exists())    
		{    
			Element struts,package_,action,result,param;           
			struts = new Element("struts");
			package_ = new Element("package");
			action = new Element("action");
			result = new Element("result");
			param = new Element("param");
			package_.setAttribute("name", index);
			package_.setAttribute("extends", "plt_json");
			action.setAttribute("name",  dto.getString("lowername") + "Action");
			action.setAttribute("class",  dto.getString("lowername") + "Action");
			result.setAttribute("name", "success");
			result.setAttribute("type", "json");
			param.setAttribute("name", "root");
			param.setText("action");
			result.addContent(param);
			action.addContent(result);
			package_.addContent(action);
			struts.addContent(package_);
			Document doc = sb.build(new File("src/builder/framework/resources/struts.xml"));  
	        doc.setRootElement(struts);
	        XMLOutputter XMLOut = new XMLOutputter("  ",true,"UTF-8");                               
	        XMLOut.output(doc,new FileOutputStream(fileName));
		}
		// 存在文件，则新增一个struts的bean信息到文件中
		else{
	        Document doc = null;
	        try {
	            doc = sb.build(fileName);
	        } catch (Exception e) {
	            System.out.print(fileName + "文件读取异常");
	        }
	        Element root = doc.getRootElement();
	        // 先判断这个struts是否已经存在于这个xml文件中，存在则不处理
	        boolean isexist = false;
	        List<Element> packages = root.getChildren();
	        for(Element package_ : packages){
	        	List<Element> actions = package_.getChildren();
	        	for(Element bean : actions){
	        		String id = bean.getAttribute("name").getValue();
		        	if((dto.getString("lowername") + "Action").equals(id)){
		        		isexist = true;
		        	}
	        	}
	        }
	        // 如果不存在，则插入新增的struts信息到xml文件中
	        if(!isexist){
	        	Element action = new Element("action");
	        	Element result = new Element("result");
	        	Element param = new Element("param");
				action.setAttribute("name",  dto.getString("lowername") + "Action");
				action.setAttribute("class",  dto.getString("lowername") + "Action");
				result.setAttribute("name", "success");
				result.setAttribute("type", "json");
				param.setAttribute("name", "root");
				param.setText("action");
				result.addContent(param);
				action.addContent(result);
				packages.get(0).addContent(action);
		        XMLOutputter out=new XMLOutputter();
		        out.output(doc,new FileOutputStream(fileName));
	        }
		}
		
		System.out.println("xml文件[" + fileName + "生成成功。");
	}
	
	
}
