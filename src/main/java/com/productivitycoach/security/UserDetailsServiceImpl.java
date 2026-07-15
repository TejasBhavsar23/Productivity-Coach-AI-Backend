package com.productivitycoach.security;

import com.productivitycoach.entity.User;
import com.productivitycoach.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of Spring Security's {@link UserDetailsService}.
 * Called automatically by the framework during authentication to load
 * the user record from the database by email.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user from the database using their email as the lookup key.
     * Spring Security passes in what it calls the "username", but in our
     * domain the unique identifier is the email address.
     *
     * @param email the user's email address
     * @return populated UserDetails object
     * @throws UsernameNotFoundException if no user exists with the given email
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email));
        return UserDetailsImpl.build(user);
    }
}
