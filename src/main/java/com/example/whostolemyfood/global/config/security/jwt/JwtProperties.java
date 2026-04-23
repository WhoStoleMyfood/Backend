package com.example.whostolemyfood.global.config.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;
    private Token token;

    @Getter
    @Setter
    public static class Token {
        private String header;
        private String prefix;
        private Access access;
        private Refresh refresh;

        @Getter
        @Setter
        public static class Access {
            private long minute;
        }

        @Getter
        @Setter
        public static class Refresh {
            private long minute;
        }
    }
}
