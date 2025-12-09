package com.hse.leihsy;

import com.hse.leihsy.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.MOCK,
		properties = {
				"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
		}
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class LeihsyApplicationTests {

	@Test
	void contextLoads() {
		// Test that the application context loads successfully
	}

}
