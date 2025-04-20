package am.shavigh.dbmigration.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class WebScrapingService {

    private static final Logger log = LoggerFactory.getLogger(WebScrapingService.class);


    public String getBreadcrumbsWithSelenium(String url) {
        var breadcrumbs = new StringBuilder();

        var driver = loadPageWithSelenium(url);
        if (driver == null) return breadcrumbs.toString();

        try {
            var breadcrumbElements = driver.findElements(By.cssSelector(
                    "nav.entry-breadcrumbs span.breadcrumb, nav.entry-breadcrumbs span.breadcrumb-current"
            ));

            for (var element : breadcrumbElements) {
                breadcrumbs.append(element.getText().trim());
            }

        } catch (Exception e) {
            log.error("Error extracting breadcrumbs with Selenium: {}", e.getMessage(), e);
        } finally {
            driver.quit();
        }

        return breadcrumbs.toString();
    }

    public String getNextLinkWithSelenium(String url) {
        var driver = loadPageWithSelenium(url);
        if (driver == null) return null;

        try {
            var nextPageElement = driver.findElement(By.partialLinkText("Հաջորդը"));
            return nextPageElement.getAttribute("href");
        } catch (Exception e) {
            log.error("Error extracting next page link with Selenium: {}", e.getMessage(), e);
            return null;
        } finally {
            driver.quit();
        }
    }

    private WebDriver loadPageWithSelenium(String url) {
        try {
            var decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
            var options = new ChromeOptions();
            options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");

            var driver = new ChromeDriver(options);
            var wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            driver.get(decodedUrl);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            return driver;
        } catch (Exception e) {
            log.error("Failed to load page with Selenium: {}", e.getMessage(), e);
            return null;
        }
    }
}
