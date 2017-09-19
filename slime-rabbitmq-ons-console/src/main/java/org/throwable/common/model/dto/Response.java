package org.throwable.common.model.dto;

import org.throwable.common.constants.StatusCode;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/5 14:33
 */
public class Response<T> {

	private Integer code;
	private String message;
	private T data;

	public Response() {
	}

	public Response(T data) {
		this.data = data;
		this.code = StatusCode.SUCCESS.getCode();
		this.message = StatusCode.SUCCESS.getMessage();
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

}
