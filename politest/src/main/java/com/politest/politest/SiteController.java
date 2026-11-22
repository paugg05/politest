package com.politest.politest;

import com.politest.politest.catalog.CatalogService;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class SiteController {

    private static final int FAQ_COUNT = 6;

    private final CatalogService catalog;
    private final MessageSource messages;
    private final String baseUrl;

    SiteController(CatalogService catalog, MessageSource messages, @Value("${app.base-url}") String baseUrl) {
        this.catalog = catalog;
        this.messages = messages;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @GetMapping({"/{lang:ca|es|en}", "/{lang:ca|es|en}/"})
    String home(@PathVariable String lang, Model model) {
        long count = catalog.countActiveQuestions();
        model.addAttribute("questionCount", count);
        model.addAttribute("axes", catalog.axes(lang));
        model.addAttribute("faqNumbers", List.of(1, 2, 3, 4, 5, 6));
        model.addAttribute("jsonLd", jsonLd(lang, count));
        return "home";
    }

    @GetMapping("/{lang:ca|es|en}/legal")
    String legal() {
        return "legal";
    }

    @GetMapping(value = "/robots.txt", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    String robots() {
        return """
                User-agent: *
                Allow: /
                Disallow: /*/quiz
                Disallow: /actuator

                Sitemap: %s/sitemap.xml
                """.formatted(baseUrl);
    }

    @GetMapping(value = "/sitemap.xml", produces = "application/xml;charset=UTF-8")
    @ResponseBody
    String sitemap() {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" ")
                .append("xmlns:xhtml=\"http://www.w3.org/1999/xhtml\">\n");
        for (String page : List.of("/", "/legal")) {
            for (String lang : WebConfig.SUPPORTED_LANGS) {
                xml.append("  <url>\n    <loc>").append(baseUrl).append('/').append(lang).append(page).append("</loc>\n");
                for (String alt : WebConfig.SUPPORTED_LANGS) {
                    xml.append("    <xhtml:link rel=\"alternate\" hreflang=\"").append(alt).append("\" href=\"")
                            .append(baseUrl).append('/').append(alt).append(page).append("\"/>\n");
                }
                xml.append("    <xhtml:link rel=\"alternate\" hreflang=\"x-default\" href=\"")
                        .append(baseUrl).append("/\"/>\n");
                xml.append("  </url>\n");
            }
        }
        xml.append("</urlset>\n");
        return xml.toString();
    }

    private String jsonLd(String lang, long count) {
        Locale locale = Locale.forLanguageTag(lang);
        Object[] args = {count};

        StringBuilder json = new StringBuilder();
        json.append("{\"@context\":\"https://schema.org\",\"@graph\":[");
        json.append("{\"@type\":\"WebApplication\"")
                .append(",\"name\":").append(str(msg("app.title", locale)))
                .append(",\"url\":").append(str(baseUrl + "/" + lang + "/"))
                .append(",\"description\":").append(str(messages.getMessage("home.meta.description", args, locale)))
                .append(",\"inLanguage\":").append(str(lang))
                .append(",\"applicationCategory\":\"EducationalApplication\"")
                .append(",\"operatingSystem\":\"Any\"")
                .append("}");
        json.append(",{\"@type\":\"FAQPage\",\"inLanguage\":").append(str(lang)).append(",\"mainEntity\":[");
        for (int i = 1; i <= FAQ_COUNT; i++) {
            if (i > 1) {
                json.append(',');
            }
            json.append("{\"@type\":\"Question\",\"name\":")
                    .append(str(messages.getMessage("home.faq." + i + ".q", args, locale)))
                    .append(",\"acceptedAnswer\":{\"@type\":\"Answer\",\"text\":")
                    .append(str(messages.getMessage("home.faq." + i + ".a", args, locale)))
                    .append("}}");
        }
        json.append("]}]}");
        return json.toString();
    }

    private String msg(String code, Locale locale) {
        return messages.getMessage(code, null, locale);
    }

    private static String str(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '<' -> sb.append("\\u003c");
                case '>' -> sb.append("\\u003e");
                case '&' -> sb.append("\\u0026");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}
