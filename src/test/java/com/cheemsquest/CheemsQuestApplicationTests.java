package com.cheemsquest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import({TestcontainersConfiguration.class, TestRandomConfiguration.class})
@SpringBootTest
class CheemsQuestApplicationTests {

	@Test
	void contextLoads() {
	}

}
