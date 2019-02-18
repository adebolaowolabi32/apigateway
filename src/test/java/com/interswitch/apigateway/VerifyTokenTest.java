package com.interswitch.apigateway;

import com.interswitch.apigateway.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("dev")
public class VerifyTokenTest {
    @Value("${spring.security.oauth2.resourceserver.jwt.keyValue}")
    private String key;
    @Autowired
    SecurityConfig securityConfig;
    @Test
    public void verifyToken(){
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJsYXN0TmFtZSI6IlRlc3RlciIsImZpcnN0TG9naW4iOmZhbHNlLCJ1c2VyX25hbWUiOiJpc3d0ZXN0ZXIyQHlhaG9vLmNvbSIsIm1vYmlsZU5vIjoiMjM0ODA1NjczMTU3NiIsImNsaWVudF9pZCI6IklLSUFDNDQyMEQxM0FCRTU3RDc3OEZCNzI2M0ExN0QxM0I2MEE4QUU0MTM1IiwiZmlyc3ROYW1lIjoiVGVzdGVycyIsImVtYWlsVmVyaWZpZWQiOnRydWUsImF1ZCI6WyJwYXNzcG9ydCIsInByb2plY3QteC1tZXJjaGFudCJdLCJzY29wZSI6WyJwcm9maWxlIl0sIm1vYmlsZU5vVmVyaWZpZWQiOnRydWUsImp0aSI6IjBlZjhmY2QzLWIxN2YtNDk4MS1iOGRjLTVlNDM2YjY3MWNmNSIsImVtYWlsIjoiaXN3dGVzdGVyMkB5YWhvby5jb20iLCJwYXNzcG9ydElkIjoiRTI1MUYwRTktN0JDRi00Q0FFLThFOEItNTZERjI1RUQ4NUQwIn0.UI8t_L4htd5qYbWYlqkTdZATBwaJ7CIVjB--uYRgK3bouFI8qE7tr7lvNLOzMoyeWAyl3D_ZzVsFUIHe7QtzwKT5DbJqtmLDz9LDxB9R9VmWRIo6lsA0-9nUUupmKiUPadL54Np7rMPq2lE3Ygppwmu3DFhw8jImSQb-sX7G_2FEPlzpeZLyYJx3qi4ll2n9Xb4_r_TeI52Wdt2FX1ryclLhtmXPZ_gNp_DSHSspJsgrIkOfxIiiy0q3h9aV2oPoPoJNUlpwLxaVB0E5r9ytn2ITPw5tFppHCmKfxhhuAM0gBzcF2p-qXP6pKsATZAuD7Z58V1e_HpHosI2MdkU_VA";
        /*try {
            Algorithm algorithm = Algorithm.HMAC256("secret");
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            System.out.println(jwt.getPayload());
        } catch (JWTVerificationException exception){
            //Invalid signature/claims
        }*/
    }

}
