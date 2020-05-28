package de.questophant.backend;

public class DefaultResponse {

	public static final DefaultResponse SUCCESS = new DefaultResponse(true);

	boolean success;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public DefaultResponse(boolean success) {
		super();
		this.success = success;
	}

}
