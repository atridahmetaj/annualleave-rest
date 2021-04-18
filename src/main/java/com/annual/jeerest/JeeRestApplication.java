package com.annual.jeerest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(scanBasePackages = { "com.annual" })
@EnableJpaRepositories("com.annual.jeeshared.repository")
@EntityScan("com.annual")
@EnableSwagger2
public class JeeRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(JeeRestApplication.class, args);
	}

}
