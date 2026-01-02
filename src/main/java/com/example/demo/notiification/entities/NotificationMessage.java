package com.example.demo.notiification.entities;

public class NotificationMessage {

	private String userId;
	private NotificationType type;
	private String message;
	private Object data;

	public NotificationMessage(String userId, NotificationType type, String message, Object data) {
		super();
		this.userId = userId;
		this.type = type;
		this.message = message;
		this.data = data;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
