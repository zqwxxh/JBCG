package builder.framework.asset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import cn.osworks.aos.builder.metainfo.vo.ColumnVO;
import cn.osworks.aos.builder.metainfo.vo.TableVO;
import cn.osworks.aos.core.typewrap.Dto;
import cn.osworks.aos.core.typewrap.Dtos;

import com.google.common.collect.Lists;

/**
 * 根据vm模板生成文件相关方法工具类
 * @author hhcao
 *
 */
public class BuilderUtils {
	
	/**
	 * 将TableVO转换为Dto
	 * @param tableVO
	 * @return
	 */
	public static Dto convertTableVO(TableVO tableVO) {
		Dto localDto = tableVO.toDto();
		// upname是后面生成的po的名字，去掉前缀T,构造成首字母大写，比如T_IS_FUNCTION解析成IsFunction,如果是视图，则名称为VIsFunction
		// 是否是视图
		boolean isview = false;
		String poname = "";
		String upname = StringUtils.lowerCase(tableVO.getName());
		if(upname.startsWith("v")){
			isview = true;
		}
		String[] names = upname.split("_");
		int i = 0;
		for(String name : names){
			if(i != 0){
				poname = poname + StringUtils.capitalize(name);
			}
			i++;
		}
		if(isview){
			poname = "V" + poname;
		}
		localDto.put("upname", poname);
		// 全小写名称，比如isfunction
		String lowerName = poname.toLowerCase();
		localDto.put("lowername", lowerName);
		// 仅仅首字母大写的名称，比如Isfunction,供set,get方法等使用
		StringBuilder sb = new StringBuilder(lowerName);
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		localDto.put("firstlowername", sb.toString());
		localDto.put("name", tableVO.getName());
		return localDto;
	}

	/**
	 * 将ColumnVO转换为Dto的列
	 * @param columnVOs 所有列信息
	 * @param isnotpk 是否不包含pk的列
	 * @return
	 */
	public static List<Dto> convertColumnVO(List<ColumnVO> columnVOs) {
		ArrayList localArrayList = Lists.newArrayList();
		for (Iterator localIterator = columnVOs.iterator(); localIterator
				.hasNext();) {
			ColumnVO columnVO = (ColumnVO) localIterator.next();
			Dto localDto = columnVO.toDto();

			String str = StringUtils.lowerCase(columnVO.getName());
			String upperstr = StringUtils.upperCase(columnVO.getName());
			// 首字母大写的名称
			localDto.put("upname", StringUtils.capitalize(str));
			// 全文大写的名称
			localDto.put("uppername", upperstr);
			// 全文小写的名称
			localDto.put("name", str);
			// 字段数据库长度
			localDto.put("length", columnVO.getLength());
			// 字段描述
			localDto.put("comment", columnVO.getComment() == null ? str : columnVO.getComment());
			// 是否为主键
			localDto.put("ispkey", columnVO.getIsPkey());
			// 对应的javatype，如String
			localDto.put("javatype", toJavaType(columnVO));
			// 对应的jdbctype，如VARCHAR
			localDto.put("jdbctype", toJdbcType(columnVO.getType()));
			// 对应的javatype，如java.lang.String
			localDto.put("javaalltype", toJavaAllType(columnVO));
			localArrayList.add(localDto);
		}
		return localArrayList;
	}
	

	/**
	 * 根据数据库类型匹配对应的java类型
	 * @param colDbType
	 * @return
	 */
	public static String toJavaType(ColumnVO columnvo) {
		String str = "String";
		String colDbType = columnvo.getType();
		if (StringUtils.indexOfIgnoreCase(colDbType, "date") != -1) {
			str = "Date";
		} else if ("timestamp".equalsIgnoreCase(colDbType)) {
			str = "Date";
		} else if ("numeric".equalsIgnoreCase(colDbType))
			str = "BigDecimal";
		else if ("number".equalsIgnoreCase(colDbType))
			str = "BigDecimal";
		else if (StringUtils.indexOfIgnoreCase(colDbType, "decimal") != -1)
			str = "BigDecimal";
		else if (StringUtils.indexOfIgnoreCase(colDbType, "int") != -1)
			str = "Integer";
		else if (StringUtils.indexOfIgnoreCase(colDbType, "byte") != -1)
			str = "byte[]";
		else if (StringUtils.indexOfIgnoreCase(colDbType, "blob") != -1)
			str = "byte[]";
		else if (StringUtils.indexOfIgnoreCase(colDbType, "binary") != -1) {
			str = "byte[]";
		}
		if(columnvo.getName().equalsIgnoreCase("id") && StringUtils.indexOfIgnoreCase(colDbType, "number") != -1)
			str = "Long";
		return str;
	}
	
	/**
	 * 根据数据库类型匹配对应的java类型(返回java的全路径，po的xml文件中使用)
	 * @param colDbType
	 * @return
	 */
	public static String toJavaAllType(ColumnVO columnvo) {
		String str = "java.lang.String";
		String colDbType = columnvo.getType();
		if (StringUtils.indexOfIgnoreCase(colDbType, "date") != -1) {
			str = "java.util.Date";
		} else if ("timestamp".equalsIgnoreCase(colDbType)) {
			str = "java.util.Date";
		} else if ("numeric".equalsIgnoreCase(colDbType))
			str = "java.math.BigDecimal";
		else if (StringUtils.indexOfIgnoreCase(colDbType, "decimal") != -1)
			str = "java.math.BigDecimal";
		else if (StringUtils.indexOfIgnoreCase(colDbType, "number") != -1)
			str = "java.math.BigDecimal";
		else if (StringUtils.indexOfIgnoreCase(colDbType, "int") != -1)
			str = "java.lang.Long";
		
		if(columnvo.getName().equalsIgnoreCase("id") && StringUtils.indexOfIgnoreCase(colDbType, "number") != -1)
			str = "java.lang.Long";
		
		return str;
	}

	/**
	 * 返回数据库列的jdbc类型
	 * @param colDbType
	 * @return
	 */
	public static String toJdbcType(String colDbType) {
		String str = "VARCHAR";
		if ("date".equalsIgnoreCase(colDbType))
			str = "DATE";
		else if ("timestamp".equalsIgnoreCase(colDbType))
			str = "TIMESTAMP";
		else if ("numeric".equalsIgnoreCase(colDbType))
			str = "NUMERIC";
		else if ("number".equalsIgnoreCase(colDbType))
			str = "NUMBER";
		else if (StringUtils.indexOf(colDbType, "int") != -1)
			str = "INTEGER";
		else if (StringUtils.indexOf(colDbType, "byte") != -1) {
			str = "BINARY";
		}
		return str;
	}

	/**
	 * 得到java类中需要导入的对象
	 * @param columnDtos
	 * @return
	 */
	public static Dto getImportDto(List<Dto> columnDtos) {
		Dto localDto = Dtos.newDto();
		for (Iterator localIterator = columnDtos.iterator(); localIterator
				.hasNext();) {
			Dto dto = (Dto) localIterator.next();
			String javatype = dto.getString("javatype");

			if ("Date".equalsIgnoreCase(javatype))
				localDto.put("data", Boolean.valueOf(true));
			else if ("Timestamp".equalsIgnoreCase(javatype))
				localDto.put("data", Boolean.valueOf(true));
			else if ("BigDecimal".equalsIgnoreCase(javatype)) {
				localDto.put("bigDecimal", Boolean.valueOf(true));
			}

		}

		return localDto;
	}

}