package com.annual.jeerest.controllers.authentication;

import com.annual.jeeshared.beans.ResetPasswordDTO;
import com.annual.jeeshared.beans.VerifyAccountDTO;
import com.annual.jeeshared.entity.User;
import com.annual.jeeshared.entity.VerificationToken;
import com.annual.jeeshared.security.beans.JwtRequest;
import com.annual.jeeshared.security.beans.JwtResponse;
import com.annual.jeeshared.security.config.JwtUserDetailsService;
import com.annual.jeeshared.service.EmailService;
import com.annual.jeeshared.service.UserService;
import com.annual.jeeshared.service.VerificationTokenService;
import com.annual.jeeshared.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;


@RestController
@CrossOrigin(origins = "http://localhost:4200/")
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private VerificationTokenService verificationTokenService;

    @Autowired
    UserService userService;

    @Autowired
    EmailService emailService;

    @PostMapping(value = "/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        try {
            authenticate(authenticationRequest.getEmail(), authenticationRequest.getPassword());
        } catch (Exception e) {
            throw new Exception("Bad credentials.");
        }

        User user = userService.getByEmail(authenticationRequest.getEmail());
        if (!user.isEnabled()) {
            // TODO there should be a better way to handle exceptions
            throw new Exception("This account is not activated yet.");
        }
        HttpHeaders httpHeader = new HttpHeaders();


        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
        final String token = jwtTokenUtil.generateToken(userDetails);
        httpHeader.add("Authorization", token);
        return ResponseEntity.ok(new JwtResponse(token,httpHeader, user));
    }

    @PostMapping(value = "/register")
    public User register(@Valid @RequestBody User user) {
        final String token = UUID.randomUUID().toString();
        user.setAdmin(userService.getById(1L));
        userService.save(user, true);
        emailService.sendVerificationEmail(user.getEmail(), token);
        verificationTokenService.create(userService.getByEmail(user.getEmail()), token);
        return user;
    }

    @PostMapping(value = "/forgotPassword")
    public void forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        User user = userService.getByEmail(email);

        String token = UUID.randomUUID().toString();
        verificationTokenService.create(user, token);

        emailService.sendResetPasswordEmail(email, token);
    }

    @PostMapping(value = "/resetPassword")
    public void resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        VerificationToken verificationToken = verificationTokenService.findByToken(resetPasswordDTO.getResetToken());
        User user = verificationToken.getUser();

        user.setPassword(resetPasswordDTO.getPassword());
        userService.save(user, true);

        verificationTokenService.delete(verificationToken); // tokens should be used only once
    }

    @PostMapping(value = "/verifyAccount")
    public User verifyAccount(@RequestBody VerifyAccountDTO body) throws Exception {
        VerificationToken verificationToken = verificationTokenService.findByToken(body.getToken());

        if (verificationToken == null || !verificationToken.isTokenValid())
            throw new Exception("The verification token does not exist or it has expired. Please try again.");

        User user = verificationToken.getUser();
        user.setEnabled(true);

        userService.save(user);
        verificationTokenService.delete(verificationToken); // tokens should be used only once

        return user;
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
