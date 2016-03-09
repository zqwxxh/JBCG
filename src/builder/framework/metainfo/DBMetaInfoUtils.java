package builder.framework.metainfo;

import cn.osworks.aos.builder.asset.DriverManagerOpt;
import cn.osworks.aos.builder.metainfo.vo.ColumnVO;
import cn.osworks.aos.builder.metainfo.vo.TableVO;
import cn.osworks.aos.core.asset.AOSListUtils;
import cn.osworks.aos.core.asset.AOSUtils;
import cn.osworks.aos.core.dao.asset.DBType;
import cn.osworks.aos.core.typewrap.Dto;
import cn.osworks.aos.core.typewrap.Dtos;

import com.google.common.collect.Lists;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

/**
 * 数据库元数据操作工具类，可以得到表，列的相关信息
 * @author hhcao
 *
 */
public class DBMetaInfoUtils {
	/**
	 * 获得数据库连接
	 * @param driverOpt
	 * @return
	 * @throws Exception
	 */
	public static Connection newConnection(DriverManagerOpt driverOpt)
			throws Exception {
		Connection connection = null;
		if (StringUtils.equalsIgnoreCase(driverOpt.getDataBaseType(), "mysql"))
			Class.forName("com.mysql.jdbc.Driver");
		else if (StringUtils.equalsIgnoreCase(driverOpt.getDataBaseType(),
				"postgresql"))
			Class.forName("org.postgresql.Driver");
		else if (StringUtils.equalsIgnoreCase(driverOpt.getDataBaseType(),
				"oracle"))
			Class.forName("oracle.jdbc.OracleDriver");
		else if (StringUtils.equalsIgnoreCase(driverOpt.getDataBaseType(),
				"microsoft sql server"))
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		else if (StringUtils
				.equalsIgnoreCase(driverOpt.getDataBaseType(), "h2"))
			Class.forName("org.h2.Driver");
		else
			throw new UnsupportedOperationException("你使用的数据库["
					+ driverOpt.getDataBaseType() + "]不被支持，请选择其他数据库产品。");
		try {

			Properties localObject2 = new Properties();
			localObject2.setProperty("user", driverOpt.getUserName());
			((Properties) localObject2).setProperty("password",
					driverOpt.getPassword());
			String str = "";
			if (StringUtils.equalsIgnoreCase("mysql",
					driverOpt.getDataBaseType())) {
				((Properties) localObject2).setProperty("remarks", "true");
				((Properties) localObject2).setProperty("useInformationSchema",
						"true");
				str = "jdbc:mysql://" + driverOpt.getIp() + ":"
						+ driverOpt.getPort() + "/" + driverOpt.getCatalog()
						+ "?useUnicode=true&characterEncoding=utf-8";
			} else if (StringUtils.equalsIgnoreCase("postgresql",
					driverOpt.getDataBaseType())) {
				str = "jdbc:postgresql://" + driverOpt.getIp() + ":"
						+ driverOpt.getPort() + "/" + driverOpt.getCatalog();
			} else if (StringUtils.equalsIgnoreCase("oracle",
					driverOpt.getDataBaseType())) {
				((Properties) localObject2).setProperty("remarksReporting",
						"true");
				str = "jdbc:oracle:thin:@" + driverOpt.getIp() + ":"
						+ driverOpt.getPort() + ":" + driverOpt.getCatalog();
			} else if (StringUtils.equalsIgnoreCase("microsoft sql server",
					driverOpt.getDataBaseType())) {
				str = "jdbc:sqlserver://" + driverOpt.getIp() + ":"
						+ driverOpt.getPort() + ";database="
						+ driverOpt.getCatalog();
			} else if (StringUtils.equalsIgnoreCase("h2",
					driverOpt.getDataBaseType())) {
				str = "jdbc:h2:" + driverOpt.getCatalog();
			}
			connection = DriverManager.getConnection(str,
					(Properties) localObject2);
		} catch (SQLException localSQLException) {
			throw localSQLException;
		}
		return connection;
	}

	/**
	 * 得到制定表名的相关信息
	 * @param databaseMetaData
	 * @param likeTableName
	 * @param type 查询视图或者表,TABLE为表，VIEW为视图
	 * @return
	 * @throws Exception
	 */
	public static List<TableVO> listTableVOs(DatabaseMetaData databaseMetaData,
			String likeTableName, String type) throws Exception {
		Object result = Lists.newArrayList();
		Object localObject1 = Lists.newArrayList();
		Object localObject2 = null;
		TableVO localObject3 = new TableVO();
		try {
			localObject2 = databaseMetaData.getTables(null, null, null,
					new String[] { type });
			while (((ResultSet) localObject2).next()) {
				localObject3 = new TableVO();
				localObject3.setOwner(((ResultSet) localObject2)
						.getString("TABLE_CAT"));
				((TableVO) localObject3).setName(((ResultSet) localObject2)
						.getString("TABLE_NAME").toUpperCase());
				String str = ((ResultSet) localObject2).getString("REMARKS");

				if (StringUtils.equalsIgnoreCase(
						DBType.getDatabaseId(databaseMetaData), "mysql")) {
					if (StringUtils.contains(str, ";"))
						str = StringUtils.substringBefore(str, ";");
					else {
						str = "";
					}
				}
				((TableVO) localObject3).setComment(str);
				((List) localObject1).add(localObject3);
			}
			
		} catch (SQLException localSQLException) {
			throw localSQLException;
		} finally{
			if (localObject2 != null)
				((ResultSet) localObject2).close();
		}
		if (AOSUtils.isNotEmpty(likeTableName)) {
			String sql = "SELECT * FROM :AOSList WHERE name LIKE :name";
			Dto dto = (Dto) Dtos.newDto("name", "%" + likeTableName
					+ "%");
			result = (List) AOSListUtils.select((List) localObject1,
					TableVO.class, sql, dto);
		}

		return (List<TableVO>) result;
	}

	/**
	 * 得到指定表的TableVO
	 * @param databaseMetaData
	 * @param tableName
	 * @param type 查询视图或者表,TABLE为表，VIEW为视图
	 * @return
	 * @throws Exception
	 */
	public static TableVO getTableVO(DatabaseMetaData databaseMetaData,
			String tableName, String type) throws Exception {
		TableVO tableVO = null;
		List<TableVO> tablVos = (List<TableVO>) listTableVOs(databaseMetaData,
				tableName, type);
		String str = "SELECT * FROM :AOSList WHERE name = :name";
		Dto dto = (Dto) Dtos.newDto("name", tableName);
		List<TableVO> databaseMetaDataList = (List) AOSListUtils.select(
				tablVos, TableVO.class, str, dto);
		tableName = null;
		if (AOSUtils.isNotEmpty(databaseMetaDataList)) {
			tableVO = (TableVO) databaseMetaDataList.get(0);
		}
		return tableVO;
	}

	/**
	 * 得到指定表的相关列信息
	 * @param databaseMetaData
	 * @param equalTableName
	 * @return
	 * @throws Exception
	 */
	public static List<ColumnVO> listColumnVOs(
			DatabaseMetaData databaseMetaData, String equalTableName)
			throws Exception {
		ArrayList localArrayList = Lists.newArrayList();
		ColumnVO localColumnVO;
		Object localObject3;
		ResultSet localObject1 = null;
		try {
			localObject1 = databaseMetaData.getColumns(null, null,
					equalTableName, null);
			while (((ResultSet) localObject1).next()) {
				(localColumnVO = new ColumnVO())
						.setName(((ResultSet) localObject1)
								.getString("COLUMN_NAME"));
				localColumnVO.setComment(((ResultSet) localObject1)
						.getString("REMARKS"));
				localColumnVO.setDefaultValue(((ResultSet) localObject1)
						.getString("COLUMN_DEF"));
				String localObject2 = ((ResultSet) localObject1)
						.getString("TYPE_NAME");
				localColumnVO.setType((String) localObject2);
				if ((StringUtils.equalsIgnoreCase((CharSequence) localObject2,
						"varchar"))
						|| (StringUtils.equalsIgnoreCase(
								(CharSequence) localObject2, "char")))
					localColumnVO.setLength(Integer
							.valueOf(((ResultSet) localObject1)
									.getInt("CHAR_OCTET_LENGTH")));
				else {
					localColumnVO.setLength(Integer
							.valueOf(((ResultSet) localObject1)
									.getInt("COLUMN_SIZE")));
				}

				localObject3 = ((ResultSet) localObject1)
						.getString("IS_NULLABLE");
				if (StringUtils.equalsIgnoreCase("YES",
						(CharSequence) localObject3))
					localColumnVO.setNullAble(Boolean.valueOf(true));
				else {
					localColumnVO.setNullAble(Boolean.valueOf(false));
				}
				localColumnVO.setNumber(Integer
						.valueOf(((ResultSet) localObject1)
								.getInt("ORDINAL_POSITION")));
				localColumnVO.setScale(Integer
						.valueOf(((ResultSet) localObject1)
								.getInt("DECIMAL_DIGITS")));
				localColumnVO.setTablename(((ResultSet) localObject1)
						.getString("TABLE_NAME"));
				localArrayList.add(localColumnVO);
			}
		} catch (SQLException localSQLException) {
			throw localSQLException;
		}finally {
			if(localObject1!=null){
				localObject1.close();
			}
		}

		// 将主键列的IsPkey设置为true
		List pKColumnVOs = listPKColumnVOs(databaseMetaData, equalTableName);
		for (Object localObject2 = localArrayList.iterator(); ((Iterator) localObject2)
				.hasNext();) {
			localColumnVO = (ColumnVO) ((Iterator) localObject2).next();
			Lists.newArrayList();
			String sql = "SELECT * FROM :AOSList WHERE name = :name";
			Dto dto = Dtos.newDto("name", localColumnVO.getName());

			if (AOSUtils.isNotEmpty(localObject3 = (List) AOSListUtils.select(
					(List) pKColumnVOs, ColumnVO.class, sql, dto))) {
				localColumnVO.setIsPkey(Boolean.valueOf(true));
			} else{
				localColumnVO.setIsPkey(Boolean.valueOf(false));
			}
			// 如果是视图，则默认设置id列为主键列
			if(equalTableName.startsWith("V") && localColumnVO.getName().equals("ID")){
				localColumnVO.setIsPkey(Boolean.valueOf(true));
			}
		}

		return localArrayList;
	}

	/**
	 * 找到指定表的主键列信息
	 * @param databaseMetaData
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public static List<ColumnVO> listPKColumnVOs(
			DatabaseMetaData databaseMetaData, String tableName)
			throws Exception {
		ArrayList localArrayList = Lists.newArrayList();
		try {
			ResultSet rs = databaseMetaData.getPrimaryKeys(null, null,
					tableName);
			while (rs.next()) {

				ColumnVO columnVO = new ColumnVO();
				columnVO.setName(rs.getString("COLUMN_NAME"));
				columnVO.setIsPkey(true);
				localArrayList.add(columnVO);
			}
		} catch (SQLException localSQLException) {
			throw localSQLException;
		}
		return localArrayList;
	}

	/**
	 * 得到指定表中某列的列信息
	 * @param databaseMetaData
	 * @param tableName
	 * @param columnName
	 * @return
	 * @throws Exception
	 */
	public static ColumnVO getColumnVO(DatabaseMetaData databaseMetaData,
			String tableName, String columnName) throws Exception {
		List<ColumnVO> vos = listColumnVOs(databaseMetaData, tableName);
		String str = "SELECT * FROM :AOSList WHERE tablename = :tablename AND name = :name";

		Dto dto = Dtos.newDto("tablename", tableName);
		dto.put("name", columnName);
		List<ColumnVO> vosselect = (List) AOSListUtils.select(vos,
				ColumnVO.class, str, dto);
		ColumnVO columnVO = null;
		if (AOSUtils.isNotEmpty(databaseMetaData)) {
			columnVO = (ColumnVO) vosselect.get(0);
		}
		return columnVO;
	}
}