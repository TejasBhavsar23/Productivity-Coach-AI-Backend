package com.productivitycoach.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for extracting the current authenticated user's details
 * from the Spring Security context.
 *
 * Controllers call this to get the user's ID without coupling to the
 * security layer implementation details.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the {@link UserDetailsImpl} of the currently authenticated user.
     * Call this only inside secured (authenticated) endpoints.
     *
     * @throws IllegalStateException if no authentication exists in the context
     */
    public static UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found in security context");
        }
        return (UserDetailsImpl) authentication.getPrincipal();
    }

    /**
     * Convenience method to directly get the current user's ID.
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
