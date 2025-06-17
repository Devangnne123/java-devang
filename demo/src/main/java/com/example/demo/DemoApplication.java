package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public CommandLineRunner demoData(UserRepository userRepository) {
		return args -> {
			System.out.println("Checking database...");
			long count = userRepository.count();
			System.out.println("Current user count: " + count);

			if (count == 0) {
				System.out.println("Attempting to save user...");
				User user = new User();
				user.setName("Admin");
				user.setEmail("admin@gmail.com");
				user.setPassword("admin");
				user.setUserKey("ce422242c97a");

				User saved = userRepository.save(user);
				System.out.println("Saved user with ID: " + saved.getId());

				// Verify
				System.out.println("New count: " + userRepository.count());
			}
		};
	}
}