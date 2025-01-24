package com.learn.demoauthcourse.controllers;

import com.learn.demoauthcourse.models.ERole;
import com.learn.demoauthcourse.models.Role;
import com.learn.demoauthcourse.models.User;
import com.learn.demoauthcourse.payload.request.LoginRequest;
import com.learn.demoauthcourse.payload.request.RegisterRequest;
import com.learn.demoauthcourse.payload.response.MessageResponse;
import com.learn.demoauthcourse.payload.response.UserInfoResponse;
import com.learn.demoauthcourse.repository.RoleRepository;
import com.learn.demoauthcourse.repository.UserRepository;
import com.learn.demoauthcourse.security.jwt.JwtUtils;
import com.learn.demoauthcourse.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        // Si l'utilisateur existe déjà
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }
        // Si le mail est déjà utilisé.
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // On demande à Spring d'authentifier l'utilisateur avec son username + password
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        // On place l'authentification dans le context pour y avoir accès partout en cas de besoin.
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // On récupère l'utilisateur contenu dans l'objet authentication
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // On génère un cookie grâve aux informations fournis par notre utilisateur.
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        // On récupère les rôles de l'utilisateur pour les renvoyer
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());
        // On construit la réponse selon ce que l'on veut renvoyer au FRONT
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new UserInfoResponse(userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles));
    }
}
