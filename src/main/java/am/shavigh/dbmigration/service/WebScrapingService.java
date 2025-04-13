package am.shavigh.dbmigration.service;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
public class WebScrapingService {

    private static final Logger log = LoggerFactory.getLogger(WebScrapingService.class);

    private final RestTemplate restTemplate;

    public WebScrapingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchWebpageContent(String url) {
        try {
            var htmlContent = getHtmlContent(url);
            var document = Jsoup.parse(htmlContent);

            var nextPageLink = document.select("a:containsOwn(Հաջորդը)").first();
            if (nextPageLink != null) {
                return nextPageLink.attr("href");
            }
            return null;

        } catch (Exception e) {
            log.error("An error occurred while fetching or parsing the webpage: " + e.getMessage());
            return null;
        }
    }

    private String getHtmlContent(String url) {
        var decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        var htmlContent = restTemplate.getForObject(decodedUrl, String.class);

        if (htmlContent == null) {
            log.error("html content is null");
            throw new RuntimeException("html content is null");
        }
        return htmlContent;
    }
}
