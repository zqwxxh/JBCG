package builder;

import java.sql.Connection;
import java.sql.SQLException;

import builder.framework.DaoBuilder;
import cn.osworks.aos.builder.asset.DriverManagerOpt;
import cn.osworks.aos.builder.metainfo.DBMetaInfoUtils;
import cn.osworks.aos.core.dao.asset.DBType;
import cn.osworks.aos.core.typewrap.Dto;
import cn.osworks.aos.core.typewrap.Dtos;

/**
 * 数据访问层代码生成器
 * 
 * 
 * @author hhcao
 * @date 2015年7月28日
 * @throws SQLException 
 */
public class DaoBuilderConsole {

	public static void main(String[] args) throws SQLException {
		//===================
		DriverManagerOpt driverOpt = new DriverManagerOpt();
		driverOpt.setDataBaseType(DBType.ORACLE); 
		driverOpt.setIp("10.16.6.6");
		driverOpt.setPort("1521");
		//数据库名或数据库实例名
		driverOpt.setCatalog("fhiis");
		driverOpt.setUserName("fhsys");
		driverOpt.setPassword("fhsys");
		//===================
		Dto dto = Dtos.newDto();
		//改为自己模块包名
		dto.put("package", "com.fiberhome.module.ces");
		//改为自己存放java相关文件的磁盘文件路径
		dto.put("outPath", "D:/workspace/fhisys/src/com/fiberhome/module/ces");
		//改为自己存放js相关文件的磁盘文件路径
		dto.put("jsoutPath", "D:/workspace/fhisys/WebRoot/com/fiberhome/module/ces");
		//注解author
		dto.put("author", "foaout13");
		//指定多张表请用逗号分隔；
		dto.put("tables", "t_ces_smslip_head");
		//===================
		Connection connection = DBMetaInfoUtils.newConnection(driverOpt);
		try {
			DaoBuilder.build(connection, dto);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		connection.close();
	}

}
