package com.github.catstiger.common.sql.filter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.catstiger.common.util.Converters;
import com.github.catstiger.common.web.WebUtil;
import com.google.common.base.Converter;

public class QueryPart {
	private static Logger logger = LoggerFactory.getLogger(QueryPart.class);

	private Operator operator;

	private String fieldName;

	private Object value;

	private Object[] values;

	/**
	 * Operator, 字段名， value, 创建一个QueryPart对象
	 * 
	 */
	public QueryPart(Operator operator, String fieldName, Object value) {
		this.operator = operator;
		this.fieldName = fieldName;
		this.value = value;
	}

	/**
	 * 根据Operator, 字段名， values, 创建一个QueryPart对象
	 */
	public QueryPart(Operator operator, String fieldName, Object[] values) {
		this.operator = operator;
		this.fieldName = fieldName;
		this.values = values;
	}

	/**
	 * 解析Map对象中的元素，将符合条件（fieldName_Type_Operator）的元素转换为 QueryPart对象，并返回QueryPart对象的集合。
	 * 
	 * @param params 查询参数Map
	 * @return List of QueryPart
	 */
	public static List<QueryPart> parse(Map<String, Object> params) {
		if (params == null) {
			return Collections.emptyList();
		}
		List<QueryPart> parts = new ArrayList<QueryPart>(params.size());
		for (Entry<String, Object> entry : params.entrySet()) {
			// 过滤掉空值
			String key = entry.getKey();
			Object value = entry.getValue();
			if (StringUtils.isBlank((String) value)) {
				continue;
			}

			String[] names = StringUtils.split(key, "_");
			if (names.length != 3) {
				logger.warn("{} is not a valid query part name", key);
			}
			String fieldName = names[0];
			// 在Anything系统中，有带下划线字段，BorderForm.js,AnyCmpBuilder中将下划线变为了-，这里将字段中的-变为_
			if (fieldName.indexOf("-") >= 0) {
				fieldName = StringUtils.replace(fieldName, "-", "_");
			}
			Operator operator = Operator.valueOf(names[2]);
			String valueString = params.get(key).toString();
			if (valueString != null) {
				if (valueString.indexOf(",") >= 0 && key.endsWith(Operator.IN.toString())) { // IN查询的情况
					String[] values = StringUtils.split(valueString, ",");
					List<Object> vs = new ArrayList<Object>();
					for (int i = 0; i < values.length; i++) {
						Object object = convertObject(names[1], values[i]);
						vs.add(object);
					}
					QueryPart queryPart = new QueryPart(operator, fieldName, null);
					queryPart.values = vs.toArray();
					parts.add(queryPart);
				} else { // 普通情况
					Object object = convertObject(names[1], params.get(key).toString());
					parts.add(new QueryPart(operator, fieldName, object));
				}

			}

		}

		return parts;
	}

	/**
	 * 解析HttpServletRequest对象中的参数，将参数名称符合条件（Q_fieldName_Type_Operator） 的元素转换为QueryPart对象，并返回QueryPart对象的集合。
	 * 
	 * @param request HttpServletRequest
	 * @return List of QueryPart
	 */
	public static List<QueryPart> parse(HttpServletRequest request) {
		Map<String, Object> params = WebUtil.getParametersStartingWith(request, "Q_");

		return parse(params);
	}

	/**
	 * 为了迎合{@code Converter#from(Function, Function), 构造一个“backForward” 函数，实现Object到String的转换}
	 */
	private static String simpleConverter(Object o) {
		return (String) o;
	}

	private static final Map<DataType, Converter<String, Object>> converters = new HashMap<>(10);

	// 所有的Converter
	static {
		converters.put(DataType.S, Converter.from(s -> {
			return s;
		}, QueryPart::simpleConverter));

		converters.put(DataType.B, Converter.from(s -> {
			return Boolean.valueOf(s);
		}, QueryPart::simpleConverter));

		converters.put(DataType.BD, Converter.from(s -> {
			return new BigDecimal(s);
		}, QueryPart::simpleConverter));

		converters.put(DataType.DBL, Converter.from(s -> {
			return Double.valueOf(s);
		}, QueryPart::simpleConverter));

		converters.put(DataType.D, Converter.from(s -> {
			if (s.indexOf("T") > 0) {
				s = StringUtils.replace(s, "T", " ");
			}
			return Converters.parseDate(s);
		}, QueryPart::simpleConverter));

		converters.put(DataType.FT, Converter.from(s -> {
			return Float.valueOf(s);
		}, QueryPart::simpleConverter));

		converters.put(DataType.L, Converter.from(s -> {
			if (NumberUtils.isCreatable(s)) {
				return Long.valueOf(s);
			}
			return null;
		}, QueryPart::simpleConverter));

		converters.put(DataType.N, Converter.from(s -> {
			if (StringUtils.isNumeric(s)) {
				return Integer.valueOf(s);
			} else if (Boolean.TRUE.toString().equalsIgnoreCase(s)) {
				return Integer.valueOf(1);
			} else if (Boolean.FALSE.toString().equalsIgnoreCase(s)) {
				return Integer.valueOf(0);
			}
			return null;
		}, QueryPart::simpleConverter));

		converters.put(DataType.SN, Converter.from(s -> {
			return Short.valueOf(s);
		}, QueryPart::simpleConverter));

		converters.put(DataType.SN, Converter.from(s -> {
			return Converters.parseDate(s);
		}, QueryPart::simpleConverter));
	}

	private static Object convertObject(String typeAbbr, String paramValue) {
		if (StringUtils.isBlank(paramValue) || "null".equalsIgnoreCase(paramValue)) {
			return null;
		}

		Object value = paramValue;
		DataType type = DataType.valueOf(typeAbbr);
		if (converters.containsKey(type)) {
			value = converters.get(type).convert(paramValue);
		}
		return value;
	}

	public Operator getOperator() {
		return operator;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Object getValue() {
		return value;
	}

	public Object[] getValues() {
		return values;
	}

}
