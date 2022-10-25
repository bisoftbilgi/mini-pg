package com.bisoft.minipg;

import com.bisoft.minipg.helper.MiniPGLocalSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = MinipgApplication.class)
@TestPropertySource(locations = "classpath:application.properties")
class MinipgApplicationTests {

	@Autowired
	MiniPGLocalSettings miniPGLocalSettings;

	@Test
	void contextLoads() {
	}

}
