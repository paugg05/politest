package com.politest.politest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import org.springframework.web.servlet.LocaleResolver;

/**
 * Decide el idioma a partir del primer segmento de la URL:
 * /ca/..., /es/... o /en/... Si no hay ninguno, usa el idioma por defecto.
 */
class PathLocaleResolver implements LocaleResolver {

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        for (String lang : WebConfig.SUPPORTED_LANGS) {
            if (path.equals("/" + lang) || path.startsWith("/" + lang + "/")) {
                return Locale.forLanguageTag(lang);
            }
        }
        return Locale.forLanguageTag(WebConfig.DEFAULT_LANG);
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        throw new UnsupportedOperationException("El idioma se decide por la URL");
    }
}
