package org.throwable.common.constants;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/30 22:02
 */
public enum RejectExecutionHandlerEnum {

	AbortPolicy,

	CallerRunsPolicy,

	DiscardPolicy,

	DiscardOldestPolicy;

	public static RejectExecutionHandlerEnum parse(String value) {
		for (RejectExecutionHandlerEnum each : RejectExecutionHandlerEnum.values()) {
			if (each.name().equalsIgnoreCase(value)) {
				return each;
			}
		}
		throw new UnsupportedOperationException("Unsupported RejectExecutionHandler for value:" + value);
	}
}
