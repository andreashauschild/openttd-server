package de.litexo.security;

import de.litexo.api.ServiceRuntimeException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BasicAuth {

    private String password;
    private String userName;

    public BasicAuth(String authHeader) {

        try {
            // Authorization: Basic base64credentials
            String base64Credentials = authHeader.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            // credentials = username:password
            final String[] values = credentials.split(":", 2);
            if (values.length == 2) {
                this.userName = values[0].trim();
                this.password = values[1].trim();
            }
        } catch (IllegalArgumentException e) {
            throw new ServiceRuntimeException(e);
        }

    }

    /**
     * Gets the value of the password property.
     *
     * @return possible object is {@link String}
     */
    public String getPassword() {

        return password;
    }

    /**
     * Gets the value of the userName property.
     *
     * @return possible object is {@link String}
     */
    public String getUserName() {

        return userName;
    }
}
