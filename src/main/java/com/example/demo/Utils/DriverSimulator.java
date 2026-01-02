//package com.example.demo.Utils;
//
//import java.util.List;
//
//import org.springframework.data.geo.Point;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import com.example.demo.driver.entities.Driver;
//import com.example.demo.driver.repository.DriverRepository;
//
//@Component
//@EnableScheduling
//public class DriverSimulator {
//
//	private final DriverRepository driverRepository;
//	private final RedisTemplate<String, String> redisTemplate;
//
//	public DriverSimulator(DriverRepository driverRepository, RedisTemplate<String, String> redisTemplate) {
//		this.driverRepository = driverRepository;
//		this.redisTemplate = redisTemplate;
//	}
//
//	@Scheduled(fixedRate = 1000) // 1 update/sec
//	public void simulateDrivers() {
//		List<Driver> drivers = driverRepository.findByAssignedFalse();
//
//		for (Driver d : drivers) {
//
//			double lat = d.getLat() + randomOffset();
//			double lng = d.getLng() + randomOffset();
//
//			redisTemplate.opsForGeo().add("drivers:geo:available:" + d.getCity(), new Point(lng, lat), d.getId());
//
//		}
//	}
//
//	private double randomOffset() {
//		return (Math.random() - 0.5) / 1000;
//	}
//}
