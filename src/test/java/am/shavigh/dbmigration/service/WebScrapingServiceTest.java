package am.shavigh.dbmigration.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebScrapingServiceTest {

    @Autowired
    private WebScrapingService webScrapingService;

    @Test
    void testGetBreadcrumbsWithSelenium() {
        String url = "http://shavigh.am/%d5%a3%d5%ac%d5%b8%d6%82%d5%ad-314-3/";
        String breadcrumbs = webScrapingService.getBreadcrumbsWithSelenium(url);
        System.out.println(breadcrumbs);
        assertNotNull(breadcrumbs);
        assertFalse(breadcrumbs.isEmpty());
    }

}