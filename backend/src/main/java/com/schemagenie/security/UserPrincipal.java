package com.schemagenie.security;

/** Lightweight authenticated-user context attached to the request when a valid JWT is present. */
public class UserPrincipal {
    private final String userId;
    private final String email;

    public UserPrincipal(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public String getUserId() { return userId; }
    public String getEmail() { return email; }
}
