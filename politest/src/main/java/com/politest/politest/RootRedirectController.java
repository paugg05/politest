package com.politest.politest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Locale;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Redirige "/" al idioma preferido del navegador (ca, es o en). */
@Controller
class RootRedirectController {

    @GetMapping("/")
    String root(HttpServletRequest request) {
        for (Locale locale : Collections.list(request.getLocales())) {
            if (WebConfig.SUPPORTED_LANGS.contains(locale.getLanguage())) {
                return "redirect:/" + locale.getLanguage() + "/";
            }
        }
        return "redirect:/" + WebConfig.DEFAULT_LANG + "/";
    }
}
