package builder.framework;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import builder.framework.asset.BuilderUtils;
import builder.framework.asset.XmlUtils;
import builder.framework.metainfo.DBMetaInfoUtils;
import builder.framework.resources.BuilderResources;
import cn.osworks.aos.builder.metainfo.vo.ColumnVO;
import cn.osworks.aos.builder.metainfo.vo.TableVO;
import cn.osworks.aos.core.asset.AOSUtils;
import cn.osworks.aos.core.exception.AOSException;
import cn.osworks.aos.core.typewrap.Dto;
import cn.osworks.aos.core.typewrap.Dtos;
import cn.osworks.aos.core.velocity.VelocityHelper;

/**
 * 自动生成模板文件
 * 
 * @author hhcao
 * @date 2015年7月28日
 * @version 1.0
 */
public class DaoBuilder {
	private static Log a = LogFactory.getLog(DaoBuilder.class);

	/**
	 * 自动生成PO对象
	 * 
	 * @param inDto
	 * @throws Exception
	 */
	public static void buildPO(Dto inDto) throws Exception {
		// 将TableVO对象转换成Dto对象
		Object localObject1 = BuilderUtils.convertTableVO((TableVO) inDto
				.get("tableVO"));
		// 将ColumnVO对象转换成Dto对象
		Object localObject2 = BuilderUtils.convertColumnVO((List) inDto
				.get("columnVOs"));
		// 视图VO
		TableVO viewVO = (TableVO) inDto.get("viewVO");
		List viewcolumnVOs = (List) inDto.get("viewcolumnVOs");

		// 表信息list，可能存在表和视图
		ArrayList tableList = new ArrayList();
		tableList.add(localObject1);
		// 列信息list，可能存在表和视图
		ArrayList columnList = new ArrayList();
		columnList.add(localObject2);
		// 如果存在视图，则也需要生成视图的pojo和xml配置文件
		if (viewVO != null) {
			tableList.add(BuilderUtils.convertTableVO(viewVO));
			columnList.add(BuilderUtils.convertColumnVO(viewcolumnVOs));
		}

		// 将表和对应视图生成文件
		for (int i = 0, n = tableList.size(); i < n; i++) {
			int index = i;
			localObject1 = tableList.get(index);
			localObject2 = columnList.get(index);
			if (localObject1 == null)
				continue;
			// PO对象的包名
			String str = inDto.getString("package") + ".pojo";
			// 构造解析vm模板文件需要的各种变量
			Dto localDto;
			(localDto = Dtos.newDto()).put("columnDtos", localObject2);
			localDto.put("tableDto", localObject1);
			localDto.put("package", str);
			// 是否有需要导入的类
			localDto.put("importDto",
					BuilderUtils.getImportDto((List) localObject2));
			localDto.put("author", inDto.getString("author"));
			localDto.put("sysdate", AOSUtils.getDateTimeStr());
			BuilderResources resource = new BuilderResources();
			// 解析模板文件
			localObject2 = VelocityHelper.mergeFileTemplate(
					resource.getPoJavaVm(), localDto);
			try {
				String outPath = inDto.getString("outPath") + "/pojo/";
				FileUtils.forceMkdir(new File(outPath));
				// 生成POjava文件
				FileOutputStream stream = new FileOutputStream(outPath
						+ ((Dto) localObject1).getString("upname") + "PO.java");
				stream.write(((StringWriter) localObject2).toString()
						.getBytes("UTF-8"));
				stream.close();
				System.out.println("PO文件[" + str + "."
						+ ((Dto) localObject1).getString("upname")
						+ "PO.java]生成成功。");
			} catch (Exception localException) {
				throw localException;
			}
		}

	}

	/**
	 * 生成PO的hibernate映射文件
	 * 
	 * @param inDto
	 * @throws Exception
	 */
	public static void buildXmlMapper(Dto inDto) throws Exception {
		Object localObject1 = BuilderUtils.convertTableVO((TableVO) inDto
				.get("tableVO"));
		Object localObject2 = BuilderUtils.convertColumnVO((List) inDto
				.get("columnVOs"));

		// 视图VO
		TableVO viewVO = (TableVO) inDto.get("viewVO");
		List viewcolumnVOs = (List) inDto.get("viewcolumnVOs");

		// 表信息list，可能存在表和视图
		ArrayList tableList = new ArrayList();
		tableList.add(localObject1);
		// 列信息list，可能存在表和视图
		ArrayList columnList = new ArrayList();
		columnList.add(localObject2);
		// 如果存在视图，则也需要生成视图的pojo和xml配置文件
		if (viewVO != null) {
			tableList.add(BuilderUtils.convertTableVO(viewVO));
			columnList.add(BuilderUtils.convertColumnVO(viewcolumnVOs));
		}

		// 将表和对应视图生成文件
		for (int i = 0, n = tableList.size(); i < n; i++) {
			int index = i;
			localObject1 = tableList.get(index);
			localObject2 = columnList.get(index);
			if (localObject1 == null)
				continue;

			Dto localDto = Dtos.newDto();
			localDto.put("tableDto", localObject1);
			localDto.put("columnDtos", localObject2);
			localDto.put("package", inDto.getString("package"));
			localDto.put("author", inDto.getString("author"));
			localDto.put("sysdate", AOSUtils.getDateTimeStr());
			BuilderResources resource = new BuilderResources();
			Object result = VelocityHelper.mergeFileTemplate(
					resource.getMapperXmlVm(), localDto);
			try {
				String outPath = inDto.getString("outPath") + "/pojo/";
				FileUtils.forceMkdir(new File(outPath));
				FileOutputStream stream = new FileOutputStream(outPath
						+ ((Dto) localObject1).getString("upname") + ".hbm.xml");

				stream.write(((StringWriter) result).toString().getBytes("UTF-8"));
				stream.close();
				System.out.println("Mapper Java文件["
						+ ((Dto) localObject1).getString("upname")
						+ ".hbm.xml]生成成功。");
			} catch (Exception localException) {
				throw localException;
			}
		}
	}

	/**
	 * 生成DAO接口
	 * 
	 * @param inDto
	 * @throws Exception
	 */
	public static void buildDaoIface(Dto inDto) throws Exception {
		Object localObject1 = BuilderUtils.convertTableVO((TableVO) inDto
				.get("tableVO"));
		Dto localDto = Dtos.newDto();
		localDto.put("tableDto", localObject1);
		localDto.put("package", inDto.getString("package"));
		localDto.put("author", inDto.getString("author"));
		localDto.put("sysdate", AOSUtils.getDateTimeStr());
		BuilderResources resource = new BuilderResources();
		Object localObject2 = VelocityHelper.mergeFileTemplate(
				resource.getDaoifaceVm(), localDto);
		try {
			String outPath = inDto.getString("outPath") + "/dao/iface/";
			FileUtils.forceMkdir(new File(outPath));
			FileOutputStream stream = new FileOutputStream(outPath
					+ ((Dto) localObject1).getString("upname") + "Dao.java");

			stream.write(((StringWriter) localObject2).toString().getBytes("UTF-8"));
			stream.close();
			System.out.println("Dao接口 Java文件["
					+ ((Dto) localObject1).getString("upname")
					+ "Dao.java]生成成功。");
			return;
		} catch (Exception localException) {
			throw localException;
		}
	}

	/**
	 * 生成DAO实现类
	 * 
	 * @param inDto
	 * @throws Exception
	 */
	public static void buildDaoImpl(Dto inDto) throws Exception {
		Object localObject1 = BuilderUtils.convertTableVO((TableVO) inDto
				.get("tableVO"));
		Dto localDto = Dtos.newDto();
		localDto.put("tableDto", localObject1);
		localDto.put("package", inDto.getString("package"));
		localDto.put("author", inDto.getString("author"));
		localDto.put("sysdate", AOSUtils.getDateTimeStr());
		
		// 是否有视图
		String isview = "false";
		TableVO viewVO = (TableVO) inDto.get("viewVO");
		if (viewVO != null) {
			isview = "true";
		}
		localDto.put("isview", isview);
		
		// 表的主键是否是序列数字型，如果是数字，则dao的findxxxById方法传参必须传入long
		List<ColumnVO> columnVOs = (List) inDto.get("columnVOs");
		boolean ispknumber = false;
		for (ColumnVO pkcolumn : columnVOs) {
			if (pkcolumn.getIsPkey()
					&& "number".equalsIgnoreCase(pkcolumn.getType())) {
				ispknumber = true;
			}
		}
		localDto.put("ispknumber", ispknumber);
		
		
		BuilderResources resource = new BuilderResources();
		Object localObject2 = VelocityHelper.mergeFileTemplate(
				resource.getDaoimplVm(), localDto);
		try {
			String outPath = inDto.getString("outPath") + "/dao/impl/";
			FileUtils.forceMkdir(new File(outPath));
			FileOutputStream stream = new FileOutputStream(outPath
					+ ((Dto) localObject1).getString("upname") + "DaoImpl.java");

			stream.write(((StringWriter) localObject2).toString().getBytes("UTF-8"));
			stream.close();
			System.out.println("Dao实现类Java文件["
					+ ((Dto) localObject1).getString("upname")
					+ "DaoImpl.java]生成成功。");
			return;
		} catch (Exception localException) {
			throw localException;
		}
	}

	/**
	 * 生成Service接口
	 * 
	 * @param inDto
	 * @throws Exception
	 */
	public static void buildServiceIface(Dto inDto) throws Exception {
		Object localObject1 = BuilderUtils.convertTableVO((TableVO) inDto
				.get("tableVO"));
		Dto localDto = Dtos.newDto();
		localDto.put("tableDto", localObject1);
		localDto.put("package", inDto.getString("package"));
		localDto.put("author", inDto.getString("author"));
		localDto.put("sysdate", AOSUtils.getDateTimeStr());
		BuilderResources resource = new BuilderResources();
		Object localObject2 = VelocityHelper.mergeFileTemplate(
				resource.getServiceifaceVm(), localDto);
		try {
			String outPath = inDto.getString("outPath") + "/service/iface/";
			FileUtils.forceMkdir(new File(outPath));
			FileOutputStream stream = new FileOutputStream(outPath
					+ ((Dto) localObject1).getString("upname") + "Service.java");

			stream.write(((StringWriter) localObject2).toString().getBytes("UTF-8"));
			stream.close();
			System.out.println("Service接口 Java文件["
					+ ((Dto) localObject1).getString("upname")
					+ "Service.java]生成成功。");
			return;
		} catch (Exception localException) {
			throw localException;
		}
	}

	/**
	 * 生成Service实现类
	 * 
	 * @param inDto
	 * @throws Exception
	 */
	public static void buildServiceImpl(Dto inDto) throws Exception {
		Object localObject1 = BuilderUtils.convertTableVO((TableVO) inDto
				.get("tableVO"));
		Dto localDto = Dtos.newDto();
		localDto.put("tableDto", localObject1);
		localDto.put("package", inDto.getString("package"));
		localDto.put("author", inDto.getString("author"));
		localDto.put("sysdate", AOSUtils.getDateTimeStr());
		BuilderResources resource = new BuilderResources();
		Object localObject2 = VelocityHelper.mergeFileTemplate(
				resource.getServiceimplVm(), localDto);
		try {
			String outPath = inDto.getString("outPath") + "/service/impl/";
			FileUtils.forceMkdir(new File(outPath));
			FileOutputStream stream = new FileOutputStream(outPath
					+ ((Dto) localObject1).getString("upname")
					+ "ServiceImpl.java");

			stream.write(((StringWriter) localObject2).toString().getBytes("UTF-8"));
			stream.close();
			System.out.println("Service实现类 Java文件["
					+ ((Dto) localObject1).getString("upname")
					+ "ServiceImpl.java]生成成功。");
			return;
		} catch (Exception localException) {
			throw localException;
		}
	}

	/**
	 * 生成Action类
	 * 
	 * @param inDto
	 * @throws Exception
	 */
	public static void buildAction(Dto inDto) throws Exception {
		Object localObject1 = BuilderUtils.convertTableVO((TableVO) inDto
				.get("tableVO"));
		Dto localDto = Dtos.newDto();
		localDto.put("tableDto", localObject1);
		localDto.put("package", inDto.getString("package"));
		localDto.put("author", inDto.getString("author"));
		localDto.put("sysdate", AOSUtils.getDateTimeStr());
		
		// 是否有视图
		String isview = "false";
		TableVO viewVO = (TableVO) inDto.get("viewVO");
		if(viewVO != null){
			isview = "true";
		}
		localDto.put("isview", isview);
		
		BuilderResources resource = new BuilderResources();
		Object localObject2 = VelocityHelper.mergeFileTemplate(
				resource.getActionVm(), localDto);
		try {
			String outPath = inDto.getString("outPath") + "/action/";
			FileUtils.forceMkdir(new File(outPath));
			FileOutputStream stream = new FileOutputStream(outPath
					+ ((Dto) localObject1).getString("upname") + "Action.java");

			stream.write(((StringWriter) localObject2).toString().getBytes("UTF-8"));
			stream.close();
			System.out.println("Action实现类 Java文件["
					+ ((Dto) localObject1).getString("upname")
					+ "Action.java]生成成功。");
			return;
		} catch (Exception localException) {
			throw localException;
		}
	}

	/**
	 * 生成js文件
	 * 
	 * @param inDto
	 * @throws Exception
	 */
	public static void buildJs(Dto inDto) throws Exception {
		Object localObject1 = BuilderUtils.convertTableVO((TableVO) inDto
				.get("tableVO"));
		Dto localDto = Dtos.newDto();
		localDto.put("tableDto", localObject1);
		// js的package首字母大写
		localDto.put("package", inDto.getString("package").substring(0, 1)
				.toUpperCase()
				+ inDto.getString("package").substring(1));
		localDto.put("author", inDto.getString("author"));
		localDto.put("sysdate", AOSUtils.getDateTimeStr());
		BuilderResources resource = new BuilderResources();
		Object localObject2 = VelocityHelper.mergeFileTemplate(
				resource.getJsVm(), localDto);
		try {
			String outPath = inDto.getString("jsoutPath") + "/";
			FileUtils.forceMkdir(new File(outPath));
			FileOutputStream stream = new FileOutputStream(outPath
					+ ((Dto) localObject1).getString("upname") + ".js");

			stream.write(((StringWriter) localObject2).toString().getBytes("UTF-8"));
			stream.close();
			System.out.println("Js文件["
					+ ((Dto) localObject1).getString("upname") + ".js]生成成功。");
			return;
		} catch (Exception localException) {
			throw localException;
		}
	}

	/**
	 * 生成xml配置文件
	 * 
	 * @param inDto
	 * @throws Exception
	 */
	public static void buildXml(Dto inDto) throws Exception {
		Dto tableVO = BuilderUtils.convertTableVO((TableVO) inDto
				.get("tableVO"));
		tableVO.put("package", inDto.getString("package"));
		tableVO.put("outPath", inDto.getString("outPath"));
		try {
			XmlUtils xmlUtils = new XmlUtils();
			xmlUtils.buildXml(tableVO);
			return;
		} catch (Exception localException) {
			throw localException;
		}
	}

	/**
	 * 自动插入my_tabparam，my_tabcols，syspl_operate记录
	 * 
	 * @param inDto
	 * @param connection
	 * @throws Exception
	 */
	public static void buildDataBase(Dto inDto, Connection conn)
			throws Exception {
		Dto tableVO = BuilderUtils.convertTableVO((TableVO) inDto
				.get("tableVO"));
		// 是否有视图
		String isview = "false";
		TableVO viewVO = (TableVO) inDto.get("viewVO");
		if (viewVO != null) {
			isview = "true";
		}
		String upname = (String) tableVO.get("upname");
		// 全小写名称，比如tisfunction
		String lowername = (String) tableVO.get("lowername");
		String name = (String) tableVO.get("name");
		// moduleid为表名的小写
		String moduleid = "";
		if (isview == "true") {
			// 如果有视图，则moduleid为视图名
			moduleid = name.toLowerCase().replaceFirst("t", "v");
		}else{
			// 如果没有视图，则moduleid为表名
			moduleid = name.toLowerCase();
		}
		String author = inDto.getString("author");
		// 首字母大写的包名
		String Upperpackage = inDto.getString("package").substring(0, 1)
				.toUpperCase()
				+ inDto.getString("package").substring(1);
		StringBuffer sb = new StringBuffer();
		// 首先判断该moduleid是否已经存在，如果存在则不处理，不存在则新增
		boolean ismy_tabparamexist = false;
		String sql = "select * from my_tabparam where moduleid = '" + moduleid
				+ "'";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				ismy_tabparamexist = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		// 不存在，则插入my_tabparam表数据
		if (!ismy_tabparamexist) {
			sb = new StringBuffer();
			sb.append(
					"insert into my_tabparam(id,moduleid,classname,grid_id,grid_storeroot,grid_storeurl,")
					.append("grid_queryurl,grid_needquery,grid_addkey,form_labelwidth,form_columnnumber,grid_order,form_uploadurl,grid_needpageplug)")
					.append(" values(MY_TABPARAM_SEQ.NEXTVAL,'")
					// t_is_testauto
					.append(moduleid)
					.append("','")
					// Com.fiberhome.module.is.IsTestauto
					.append(Upperpackage + "." + upname)
					.append("','")
					// istestauto
					.append(lowername)
					.append("','")
					// istestautoList
					.append(lowername + "List")
					.append("','")
					// istestautoAction!findIsTestautoByCon.action
					.append(lowername + "Action!find" + upname + "ByCon.action")
					.append("',")
					.append("'")
					// istestautoAction!findIsTestautoByCon.action
					.append(lowername + "Action!find" + upname + "ByCon.action")
					.append("','Y|Y|Y','db','100','2','id  desc','uploadFile/','Y')");

			try {
				pstmt = (PreparedStatement) conn
						.prepareStatement(sb.toString());
				pstmt.executeUpdate();
				System.out.println("插入my_tabparam表成功");
			} catch (SQLException e) {
				System.out.println("插入my_tabparam表出错" + e.toString());
			} finally {
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}
			}
		}

		// 插入my_tabcols表数据
		// 首先判断该moduleid是否已经存在，如果存在则不处理，不存在则新增
		boolean ismy_tabcolsexist = false;
		sql = "select * from my_tabcols where moduleid = '" + moduleid + "'";
		try {
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				ismy_tabcolsexist = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		// 不存在，则插入my_tabcols表数据
		if (!ismy_tabcolsexist) {
			
			sb = new StringBuffer();
			sb.append(
					"insert into my_tabcols(id,grid_order,form_order,moduleid,fieldcode,fieldname,fieldname_en_us,grid_need,form_need,form_type,grid_hide,form_hide)")
					.append(" select my_tabcols_seq.nextval,rownum,rownum,")
					.append("'")
					.append(moduleid)
					.append("',")
					.append("fun_getfiledname(lower(col.column_name)),");
			// 视图无法取到关联表的列描述，所以关联表的列描述以列名代替fieldname
			sb.append("case when x.comments is null then fun_getfiledname(lower(col.column_name)) else x.comments end,");
			sb.append("fun_getfiledname(lower(col.column_name)),")
					.append("'Y','Y',")
					.append("case when instr(data_type,'DATE')>0 then 'dateField' when instr(data_type,'NUMBER')>0 then 'numberField' else 'textField' end, ")
					.append("case when upper(col.column_name) = 'ID' then 'Y' else 'N' END ,")
					.append("case when upper(col.column_name) = 'ID' then 'Y' else 'N' END ")
					.append("from user_tab_cols col")
					.append(" left join user_col_comments com on col.column_name = com.column_name")
					.append("  and col.table_name = com.table_name")
			.append("  left join (select * from user_col_comments col where col.table_name = upper('")
			.append(name).append("')) x")
			.append("   on x.column_name = col.column_name");
			// 有视图,则将视图的字段插入到my_tabcols表中
			if (isview == "true") {
				sb.append("  where col.table_name = upper('").append(name.toLowerCase().replaceFirst("t", "v"))
				.append("')");
			}else{
				sb.append("  where col.table_name = upper('").append(name)
				.append("')");
			}
			

			try {
				pstmt = (PreparedStatement) conn
						.prepareStatement(sb.toString());
				pstmt.executeUpdate();
				System.out.println("插入my_tabcols表成功");
			} catch (SQLException e) {
				System.out.println("插入my_tabcols表出错" + e.toString());
			} finally {
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}
			}
		}

		// 插入syspl_operate表数据
		// 首先判断该moduleid是否已经存在，如果存在则不处理，不存在则新增
		boolean issyspl_operateexist = false;
		sql = "select * from syspl_operate where module = '" + moduleid + "'";
		try {
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				issyspl_operateexist = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
		// 不存在，则插入syspl_operate表数据
		if (!issyspl_operateexist) {
			sb = new StringBuffer();
			sb.append(
					"insert into syspl_operate (OPERATE_ID, OPERATE_NAME, OPERATE_NAME_EN, OPT_FUN_LINK, OPT_IMG_LINK, OPT_ORDER, OPT_GROUP, STATUS, CREATOR, CREATE_DATE, MODULE, ISPRIV, ISMENUITEM)")
					.append(" values(SYSPL_OPERATE_SEQ.NEXTVAL,?,?,?,?,?,1,'enable','")
					.append(author).append("',sysdate,?,'Y','0')");

			try {
				pstmt = (PreparedStatement) conn
						.prepareStatement(sb.toString());
				pstmt.setString(1, "填写");
				pstmt.setString(2, "Filling");
				pstmt.setString(3, "saveForm()");
				pstmt.setString(4, "table_add");
				pstmt.setInt(5, 1);
				pstmt.setString(6, moduleid);
				pstmt.addBatch();

				pstmt.setString(1, "修改");
				pstmt.setString(2, "Modify");
				pstmt.setString(3, "modifyForm()");
				pstmt.setString(4, "table_edit");
				pstmt.setInt(5, 2);
				pstmt.setString(6, moduleid);
				pstmt.addBatch();

				pstmt.setString(1, "删除");
				pstmt.setString(2, "Delete");
				pstmt.setString(3, "delRecord()");
				pstmt.setString(4, "table_delete");
				pstmt.setInt(5, 3);
				pstmt.setString(6, moduleid);
				pstmt.addBatch();

				pstmt.setString(1, "查看");
				pstmt.setString(2, "View");
				pstmt.setString(3, "viewForm()");
				pstmt.setString(4, "table");
				pstmt.setInt(5, 4);
				pstmt.setString(6, moduleid);
				pstmt.addBatch();
				pstmt.executeBatch();
				System.out.println("插入syspl_operate表成功");
			} catch (SQLException e) {
				System.out.println("插入syspl_operate表出错" + e.toString());
			} finally {
				if (pstmt != null) {
					pstmt.close();
				}
				if (rs != null) {
					rs.close();
				}
			}
		}

	}

	public static void build(Connection connection, Dto inDto) throws Exception {
		// 数据库元数据信息，可以得到数据库表，列名等
		DatabaseMetaData databaseMetaData = connection.getMetaData();
		// 需要进行生成代码的表名
		String localObject1;
		// 表名对应的TableVO对象
		Object localObject2;
		String[] tables = AOSUtils.trimAll(inDto.getString("tables"))
				.split(",");
		// 循环要进行处理的表名
		for (int i = 0, n = tables.length; i < n; i++) {

			localObject1 = tables[i].toUpperCase();
			if (AOSUtils.isEmpty(localObject2 = DBMetaInfoUtils.getTableVO(
					databaseMetaData, (String) localObject1, "TABLE"))) {
				throw new AOSException("表[" + (String) localObject1 + "]不存在。");
			}

			// 查询表是否有对应的视图，根据名称判断，比如t_is_role对应视图查找v_is_role
			String viewName = localObject1.replaceFirst("T", "V");
			TableVO viewVO = DBMetaInfoUtils.getTableVO(databaseMetaData,
					viewName, "VIEW");
			if (viewVO != null) {
				// 将视图信息和视图的所有列信息放入inDto对象中
				inDto.put("viewVO", viewVO);
				List<ColumnVO> viewcolumnVOs = DBMetaInfoUtils.listColumnVOs(
						databaseMetaData, viewName);
				inDto.put("viewcolumnVOs", viewcolumnVOs);
			}

			// 将表信息放入inDto对象中
			inDto.put("tableVO", localObject2);
			List<ColumnVO> columnVOs = DBMetaInfoUtils.listColumnVOs(
					databaseMetaData, (String) localObject1);
			List<ColumnVO> pkeyColumnVOs = DBMetaInfoUtils.listPKColumnVOs(
					databaseMetaData, (String) localObject1);
			
			// 将表中所有的列信息放入inDto对象中
			inDto.put("columnVOs", columnVOs);
			// 将表中所有的主键信息放入inDto对象中
			inDto.put("pkeyColumnVOs", pkeyColumnVOs);
			if (((List) pkeyColumnVOs).size() == 0) {
				throw new AOSException("请设置表[" + (String) localObject1
						+ "]的主键。");
			}
			// 自动生成PO对象
			buildPO(inDto);
			// 生成PO的hibernate映射文件
			buildXmlMapper(inDto);
			// 生成DAO接口
			buildDaoIface(inDto);
			// 生成DAO实现类
			buildDaoImpl(inDto);
			// 生成Service接口
			buildServiceIface(inDto);
			// 生成Service实现类
			buildServiceImpl(inDto);
			// 生成Action类
			buildAction(inDto);
			// 生成js文件
			buildJs(inDto);
			// 生成xml配置文件
			buildXml(inDto);
			// 自动插入my_tabparam，my_tabcols，syspl_operate记录
			buildDataBase(inDto, connection);

		}
	}
}