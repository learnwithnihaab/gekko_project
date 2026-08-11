package com.gekko.security;

import com.gekko.entity.Client;
import com.gekko.service.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * ClientAuthFilter - This filter enables API-key / secret style authentication for
 * requests that do not present a valid JWT. It checks headers X-Client-Id and X-Client-Secret
 * and if valid sets an authenticated principal in the SecurityContext.
 *
 * APIGEE may be configured to validate client credentials and forward a JWT; if APIGEE
 * already performs auth, this filter is not strictly necessary. We keep it so that
 * direct clients (or tests) may call Gekko with API key + secret.
 */
@Component
public class ClientAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ClientAuthFilter.class);

    private final ClientService clientService;

    public ClientAuthFilter(ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // If already authenticated (JWT), skip
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = request.getHeader("X-Client-Id");
        String clientSecret = request.getHeader("X-Client-Secret");

        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)) {
            // no client headers present; proceed without setting auth (security config will reject if required)
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Client c = clientService.findByApiKey(clientId);
            if (c != null && clientSecret.equals(c.getApiSecret())) {
                // create a simple Authentication object with ROLE_CLIENT authority
                GrantedAuthority auth = new SimpleGrantedAuthority("ROLE_CLIENT");
                Authentication authentication = new UsernamePasswordAuthenticationToken(c.getApiKey(), null, Collections.singleton(auth));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("Invalid client credentials for clientId={}", clientId);
            }
        } catch (Exception ex) {
            log.error("Error validating client credentials", ex);
        }

        filterChain.doFilter(request, response);
    }
}
