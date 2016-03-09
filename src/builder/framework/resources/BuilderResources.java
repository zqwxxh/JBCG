package builder.framework.resources;

import org.apache.commons.lang3.StringUtils;

/**
 * 获取vm模板文件资源文件
 * @author hhcao
 * @date 2015年8月3日
 * @version 1.0
 */
public class BuilderResources
{
  private static String a;

  public BuilderResources(){
	  String str = StringUtils.substringBeforeLast(str = BuilderResources.class.getCanonicalName(), 
		      ".").replace(".", "/");
	  setBasePath("/" + str + "/");
  }

  public static String getBasePath()
  {
    return a;
  }

  public static void setBasePath(String basePath) {
    a = basePath;
  }
  
  public String getPoJavaVm(){
	  return a + "po.java.vm";
  }
  
  public String getMapperXmlVm(){
	  return a + "mapper.xml.vm";
  }
  
  public String getDaoifaceVm(){
	  return a + "daoiface.java.vm";
  }
  
  public String getDaoimplVm(){
	  return a + "daoimpl.java.vm";
  }
  
  public String getServiceifaceVm(){
	  return a + "serviceiface.java.vm";
  }
  
  public String getServiceimplVm(){
	  return a + "serviceimpl.java.vm";
  }
  
  public String getActionVm(){
	  return a + "action.java.vm";
  }
  
  public String getJsVm(){
	  return a + "js.java.vm";
  }
  
}