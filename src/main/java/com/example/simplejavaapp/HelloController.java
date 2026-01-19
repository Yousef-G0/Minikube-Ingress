/**
 * Simple REST controller that returns a greeting.
 * Author: Omar Alsarabi
 * Date: January 14, 2026
 */
package com.example.simplejavaapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Value("${your_name:World}")
    private String name;

    @GetMapping("/")
    public String hello() {
        return "hello " + name;
    }
}
