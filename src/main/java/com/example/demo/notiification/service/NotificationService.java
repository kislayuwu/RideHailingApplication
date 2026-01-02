package com.example.demo.notiification.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.notiification.channel.NotificationChannel;
import com.example.demo.notiification.entities.NotificationMessage;

@Service
public class NotificationService {

	private final List<NotificationChannel> channels;

	public NotificationService(List<NotificationChannel> channels) {
		this.channels = channels;
	}

	public void notify(NotificationMessage message) {

		channels.stream().filter(channel -> channel.supports(message.getType()))
				.forEach(channel -> channel.send(message));
	}
}
