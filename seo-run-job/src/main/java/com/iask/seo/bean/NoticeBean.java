package com.iask.seo.bean;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.functions.T;

public class NoticeBean {

	private String code;
	private String msg;
	
	private List<Map<String,Object>> data;
	
	private List<?> object;
	
	public List<Map<String, Object>> getData() {
		return data;
	}
	public void setData(List<Map<String, Object>> data) {
		this.data = data;
	}
	public NoticeBean() {
		super();
	}
	public NoticeBean(String code, String msg) {
		super();
		this.code = code;
		this.msg = msg;
	}
	public NoticeBean(String code, String msg,List<?> data) {
		super();
		this.code = code;
		this.msg = msg;
		this.object = data;
	}
	
//	public NoticeBean(String code, String msg,List<Map<String,Object>> data) {
//		super();
//		this.code = code;
//		this.msg = msg;
//		this.data = data;
//	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public List<?> getObject() {
		return object;
	}
	public void setObject(List<?> object) {
		this.object = object;
	}
	@Override
	public String toString() {
		return "NoticeBean [code=" + code + ", msg=" + msg + "]";
	}
	
}
