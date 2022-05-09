package pz.gr3.serwer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableJdbcHttpSession
public class Serwer {

	public static void main(String[] args) {
		SpringApplication.run(Serwer.class, args);
	}
}
