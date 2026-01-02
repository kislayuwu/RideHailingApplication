package com.example.demo.driver.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.driver.entities.Driver;

public interface DriverRepository extends JpaRepository<Driver, String> {
	@Override
	List<Driver> findAll();
	
	Optional<Driver> findByName(String driverName);
}
