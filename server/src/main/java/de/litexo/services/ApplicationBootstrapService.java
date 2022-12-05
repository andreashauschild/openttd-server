package de.litexo.services;

import de.litexo.model.OpenttdServerConfig;
import de.litexo.repository.DefaultRepository;
import de.litexo.security.SecurityUtils;
import io.quarkus.runtime.Startup;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

@Startup
@ApplicationScoped
public class ApplicationBootstrapService {
    @ConfigProperty(name = "server.initial.password")
    Optional<String> initialPassword;

    private static final Logger LOG = Logger.getLogger(ApplicationBootstrapService.class);

    @Inject
    DefaultRepository repository;

    @PostConstruct
    void init() {
        initDefaultServerConfig();

    }

    private void initDefaultServerConfig() {
        OpenttdServerConfig openttdServerConfig = this.repository.getOpenttdServerConfig();
        if (StringUtils.isBlank(openttdServerConfig.getPasswordSha256Hash())) {
            System.out.println();
            System.out.println("###########################################################################");
            if (this.initialPassword.isPresent()) {
                System.out.println(String.format("### Initial password was set as environment variable '%s' and will be the password\n### for the 'admin' user.", "server.initial.password"));
                openttdServerConfig.setPasswordSha256Hash(SecurityUtils.toSHA256(this.initialPassword.get()));
            } else {
                String password = SecurityUtils.generatePassword();
                System.out.println(String.format("### No initial password was set. A password for 'admin' will be generated.\n### Copy it NOW, because it will never be shown again.\n### Password: %s", password));
                openttdServerConfig.setPasswordSha256Hash(SecurityUtils.toSHA256(password));
            }
            this.repository.save(openttdServerConfig);
            System.out.println("###########################################################################");
            System.out.println();
        }
    }
}
