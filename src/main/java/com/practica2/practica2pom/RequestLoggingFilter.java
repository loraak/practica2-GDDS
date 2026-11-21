package com.practica2.practica2pom;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        Instant inicio = Instant.now();
        int status = 500; // si algo truena y no se atrapa, se registra como 500
        try {
            chain.doFilter(req, res);
            status = res.getStatus();
        } finally {
            long ms = Instant.now().toEpochMilli() - inicio.toEpochMilli();
            MDC.put("method", req.getMethod());
            MDC.put("path", req.getRequestURI());
            MDC.put("statusCode", String.valueOf(status));
            MDC.put("timestamp", inicio.toString());
            MDC.put("requestTime", String.valueOf(ms));
            try {
                String msg = "{} {} -> {} ({} ms)";
                if (status >= 500)      log.error(msg, req.getMethod(), req.getRequestURI(), status, ms);
                else if (status >= 400) log.warn(msg, req.getMethod(), req.getRequestURI(), status, ms);
                else                    log.info(msg, req.getMethod(), req.getRequestURI(), status, ms);
            } finally {
                MDC.remove("method"); MDC.remove("path"); MDC.remove("statusCode");
                MDC.remove("timestamp"); MDC.remove("requestTime");
            }
        }
    }
}