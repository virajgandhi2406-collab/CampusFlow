package com.campusflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CampusFlowApplicationTests {

    @Test
    @DisplayName("Spring ApplicationContext should load cleanly")
    void contextLoads() {
    }
}
