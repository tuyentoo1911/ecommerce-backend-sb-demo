package com.example.Service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import com.example.dto.request.AuthenticationRequest;
import com.example.javaspringboot.repository.UserRepository;
import com.example.exception.Errorcode;
import com.example.exception.AppException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.dto.response.AuthenticationResponse;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.crypto.MACVerifier;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import com.example.dto.request.IntrospectRequest;
import com.example.dto.request.LogoutRequest;
import com.example.dto.response.IntrospectResponse;
import java.text.ParseException;
import org.springframework.beans.factory.annotation.Value;
import com.example.javaspringboot.entity.User;
import java.util.StringJoiner;
import org.springframework.util.CollectionUtils;
import java.util.UUID;
import com.example.javaspringboot.repository.InvalidatedRepository;
import com.example.javaspringboot.entity.InvalidatedToken;
import com.example.dto.request.RefreshRequest;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedRepository invalidatedRepository;
    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;
    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;
    @NonFinal
    @Value("${jwt.refresh-duration}")
    protected long REFRESH_DURATION;

    public IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException {
        var token = request.getToken();
        boolean valid = true;
        try {
            verifyToken(token, false);
        } catch (AppException e) {
            valid = false;
        }
        return IntrospectResponse.builder()
                .valid(valid)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(Errorcode.USER_NOT_FOUND));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(Errorcode.INVALID_PASSWORD);
        }

        var token = generateToken(user);
        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .build();
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("self")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", generateScope(user))
                .build();

        Payload payload = new Payload(claims.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Error generating token", e);
            throw new RuntimeException("Error generating token", e);

        }
    }

    private String generateScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(stringJoiner::add);
        }
        return stringJoiner.toString();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try{
            var signedToken = verifyToken(request.getToken(), false);
            String jit = signedToken.getJWTClaimsSet().getJWTID();
        Date expirationTime = signedToken.getJWTClaimsSet().getExpirationTime();
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit) 
                .expirationTime(expirationTime)
                .build();
        invalidatedRepository.save(invalidatedToken);

        }catch(AppException exception){
            log.info("token already expired ");
        }
        
    }

  

    private SignedJWT verifyToken(String token, boolean isRefresh) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expirationTime = (isRefresh) ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESH_DURATION, ChronoUnit.SECONDS).toEpochMilli())
        : signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        if (!verified || expirationTime.before(new Date())) {
            throw new AppException(Errorcode.UNAUTHENTICATED);
        }
        if (invalidatedRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(Errorcode.UNAUTHENTICATED);
        }
        return signedJWT;
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedToken = verifyToken(request.getToken(), true);
        var jit = signedToken.getJWTClaimsSet().getJWTID();
        var expirationTime = signedToken.getJWTClaimsSet().getExpirationTime();
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expirationTime(expirationTime)
                .build();
        invalidatedRepository.save(invalidatedToken);

        var username = signedToken.getJWTClaimsSet().getSubject();
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Errorcode.USER_NOT_FOUND));

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .build();
    }
}
