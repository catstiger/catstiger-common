package com.github.catstiger.common.web.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.github.catstiger.common.sql.JdbcTemplateProxy;
import com.github.catstiger.common.sql.Page;

public abstract class ModulesController extends BaseController {
	/**
	 * 页面传递的排序方向的字段名称
	 */
	public static final String PARAM_BTABLE_START = "offset";
	public static final String PARAM_BTABLE_LIMIT = "limit";
	public static final String PARAM_BTABLE_SORT = "sort";
	public static final String PARAM_BTABLE_ORDER = "order";
	// limit, offset, search, sort, order

	@Autowired
	protected JdbcTemplateProxy jdbcTemplate;

	
	/**
	 * 从bootstrap-table提交的参数中获取Page对象
	 * 
	 * @return
	 */
	protected Page tablePage() {
		String strStart = getRequest().getParameter(PARAM_BTABLE_START);
		int offset = NumberUtils.isCreatable(strStart) ? Integer.valueOf(strStart) : 0;
		String strLimit = getRequest().getParameter(PARAM_BTABLE_LIMIT);
		int limit = NumberUtils.isCreatable(strLimit) ? Integer.valueOf(strLimit) : 0;
		return new Page(offset, limit);
	}

	/**
	 * 从bootstrap-table提交的参数中，获取排序字段名
	 * 
	 * @return
	 */
	protected String tableSort() {
		return getRequest().getParameter(PARAM_BTABLE_SORT);
	}

	/**
	 * 从bootstrap-table提交的参数中，获取排序方向，ASC or DESC
	 * 
	 * @return
	 */
	protected String tableOrder() {
		return getRequest().getParameter(PARAM_BTABLE_ORDER);
	}

	/**
	 * 在AJAX访问的时候，构建一个success返回值
	 */
	protected Map<String, Object> success(Object data) {
		Map<String, Object> success = new HashMap<>(2);
		success.put("success", true);
		if (data != null) {
			success.put("data", data);
		}
		return success;
	}

	/**
	 * 在AJAX访问的时候，直接渲染一个success对象
	 */
	protected void renderSuccess(Object data) {
		renderJson(JSON.toJSONString(success(data)));
	}

	/**
	 * 在AJAX访问的时候，构建一个error返回值
	 */
	protected Map<String, Object> error(String errorMessage) {
		Map<String, Object> error = new HashMap<>(2);
		error.put("success", false);
		if (errorMessage != null) {
			error.put("errorMessage", errorMessage);
		}
		return error;
	}

	/**
	 * 在AJAX访问的时候，直接渲染一个success对象
	 */
	protected void renderError(String errorMessage) {
		renderJson(JSON.toJSONString(error(errorMessage)));
	}


	/**
	 * 发送错误状态到Response客户端，并清空输出缓存。
	 * 
	 * @param sc  the error status code, 相关常量在HttpServletResponse中定义
	 * @param msg 错误描述信息
	 */
	protected void sendError(int sc, String msg) {
		try {
			getResponse().sendError(sc, msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送未登录信息{@code HttpServletResponse#SC_FORBIDDEN}
	 */
	protected void unauthenticated() {
		String msg = "未登录或者登录过期";
		logger.debug(msg);
		sendError(HttpServletResponse.SC_FORBIDDEN, msg);
	}

}
