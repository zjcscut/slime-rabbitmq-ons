package org.throwable.common.constants;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/5 14:36
 */
public enum  StatusCode {
	SUCCESS(2000, "success"),
	FAIL(4000, "fail"),
	ERROR(5000, "internal error");


	private Integer code;
	private String message;

	StatusCode(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public Integer getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
