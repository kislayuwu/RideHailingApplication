package com.example.demo.notiification.channel;

import com.example.demo.notiification.entities.NotificationMessage;
import com.example.demo.notiification.entities.NotificationType;

public interface NotificationChannel {

	boolean supports(NotificationType type);

	void send(NotificationMessage message);
}
