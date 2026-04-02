package com.example.tandem_api;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TandemApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TandemApiApplication.class, args);
	}

//	@Bean
//	public ApplicationRunner mailCheck(JavaMailSender mailSender) {
//		return args -> {
//			SimpleMailMessage message = new SimpleMailMessage();
//			message.setTo("test@example.com");
//			message.setSubject("Test");
//			message.setText("Mail is working.");
//			mailSender.send(message);
//			System.out.println("Mail sent successfully");
//		};
//	}
}