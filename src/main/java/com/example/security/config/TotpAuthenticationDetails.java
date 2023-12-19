package com.example.security.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@Getter
class TotpAuthenticationDetails extends WebAuthenticationDetails {

    private final String verificationCode;

    TotpAuthenticationDetails(HttpServletRequest request) {
        super(request);
        verificationCode = request.getParameter("verificationCode");
    }

}
