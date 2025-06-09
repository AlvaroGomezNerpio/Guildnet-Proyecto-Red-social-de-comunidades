package com.guildnet.backend.features.ping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @RestController
    public class HealthController {

        @GetMapping("/")
        public String hello() {
            return "Guildnet backend está funcionando";
        }
    }

}
