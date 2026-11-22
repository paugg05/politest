package com.politest.politest.result;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/{lang:ca|es|en}/result")
class ResultController {

    private final ResultService results;

    ResultController(ResultService results) {
        this.results = results;
    }

    @GetMapping("/{id:[A-Za-z0-9_-]{8,24}}")
    String show(@PathVariable String lang, @PathVariable String id, Model model) {
        Map<String, Integer> scores = results.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("bars", results.bars(scores, lang));
        model.addAttribute("profile", results.profile(scores, lang));
        return "result";
    }
}
