package org.throwable.common.constants;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/30 1:10
 */
public enum ExchangeEnum {

	DIRECT,

	FANOUT,

	TOPIC,

	HEADERS;

	public static ExchangeEnum parse(String value) {
		for (ExchangeEnum each : ExchangeEnum.values()) {
			if (each.name().equalsIgnoreCase(value)) {
				return each;
			}
		}
		throw new UnsupportedOperationException("Unsupported ExchangeType for value:" + value);
	}

}
