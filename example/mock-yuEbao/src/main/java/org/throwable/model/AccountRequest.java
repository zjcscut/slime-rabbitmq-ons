package org.throwable.model;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/7/31 1:17
 */
public class AccountRequest {

	private Long userId;
	private Long amount;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}
}
