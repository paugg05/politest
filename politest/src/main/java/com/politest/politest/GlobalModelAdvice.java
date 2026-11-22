package com.politest.politest;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Datos que necesitan todas las plantillas: idioma, selector de idioma, SEO y datos del titular. */
@ControllerAdvice
class GlobalModelAdvice {

    private static final Map<String, String> OG_LOCALES = Map.of("ca", "ca_ES", "es", "es_ES", "en", "en_US");

    private final String baseUrl;
    private final String contactEmail;
    private final String hostingProvider;

    GlobalModelAdvice(@Value("${app.base-url}") String baseUrl,
                      @Value("${app.contact-email}") String contactEmail,
                      @Value("${app.hosting-provider}") String hostingProvider) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.contactEmail = contactEmail;
        this.hostingProvider = hostingProvider;
    }

    @ModelAttribute("lang")
    String lang(Locale locale) {
        return locale.getLanguage();
    }

    @ModelAttribute("langs")
    List<String> langs() {
        return WebConfig.SUPPORTED_LANGS;
    }

    @ModelAttribute("rest")
    String rest(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        for (String lang : WebConfig.SUPPORTED_LANGS) {
            String prefix = "/" + lang;
            if (path.equals(prefix)) {
                return "/";
            }
            if (path.startsWith(prefix + "/")) {
                return path.substring(prefix.length());
            }
        }
        return "/";
    }

    @ModelAttribute("baseUrl")
    String baseUrl() {
        return baseUrl;
    }

    @ModelAttribute("canonicalUrl")
    String canonicalUrl(Locale locale, HttpServletRequest request) {
        return baseUrl + "/" + locale.getLanguage() + rest(request);
    }

    @ModelAttribute("alternates")
    Map<String, String> alternates(HttpServletRequest request) {
        String rest = rest(request);
        Map<String, String> alternates = new LinkedHashMap<>();
        for (String lang : WebConfig.SUPPORTED_LANGS) {
            alternates.put(lang, baseUrl + "/" + lang + rest);
        }
        return alternates;
    }

    @ModelAttribute("ogLocale")
    String ogLocale(Locale locale) {
        return OG_LOCALES.getOrDefault(locale.getLanguage(), "es_ES");
    }

    @ModelAttribute("year")
    int year() {
        return Year.now().getValue();
    }


    @ModelAttribute("contactEmail")
    String contactEmail() {
        return contactEmail;
    }

    @ModelAttribute("hostingProvider")
    String hostingProvider() {
        return hostingProvider;
    }
}
