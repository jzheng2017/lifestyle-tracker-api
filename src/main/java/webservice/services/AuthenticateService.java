package webservice.services;

import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import webservice.dto.CredentialDTO;
import webservice.dto.TokenDTO;
import webservice.entities.User;
import webservice.exceptions.BadCredentialsException;
import webservice.exceptions.ResourceNotFoundException;
import webservice.exceptions.UnauthorizedActionException;
import webservice.repositories.UserRepository;
import webservice.services.interfaces.HashService;
import webservice.util.JwtUtil;

/**
 * This service has been deprecated as of v1.0.0. The authentication process no longer uses this service.
 */
@Deprecated
@Service
public class AuthenticateService {

    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private HashService hashService;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Autowired
    public void setHashService(HashService hashService) {
        this.hashService = hashService;
    }

    /**
     * Authenticate the user
     *
     * @param credentials credentials of the user
     * @return user token if authentication successful
     */
    public TokenDTO authenticateUser(CredentialDTO credentials) {
        User user = userRepository.findByUsername(credentials.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!hashService.valid(credentials.getPassword(), user.getPassword())) { //checks for validity of password
            throw new BadCredentialsException("Invalid login information");
        }
        return new TokenDTO(jwtUtil.generateToken(user.getUsername()));
    }

    public Boolean authenticateToken(String token) {
        try {
            return jwtUtil.isTokenValid(token);
        } catch (JwtException ex) {
            throw new UnauthorizedActionException("Token invalid");
        }
    }

    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
}
