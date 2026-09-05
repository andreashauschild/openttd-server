package de.litexo.services;

import de.litexo.model.external.OpenttdServer;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.repository.DefaultRepository;
import de.litexo.security.SecurityUtils;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Optional;

@Startup
@ApplicationScoped
public class ApplicationBootstrapService {
    @ConfigProperty(name = "server.initial.password")
    Optional<String> initialPassword;

    /**
     * Servers that were running when the application went down are started again. The flag is only set for servers the
     * admin explicitly started and never stopped, so after a host reboot or an image update the expected state is
     * "my servers are up". Set the environment variable SERVER_AUTO_START_RUNNING_SERVERS=false to opt out.
     */
    @ConfigProperty(name = "server.auto-start-running-servers", defaultValue = "true")
    boolean autoStartRunningServers;

    private static final Logger LOG = Logger.getLogger(ApplicationBootstrapService.class);

    @Inject
    DefaultRepository repository;

    @Inject
    OpenttdService service;

    @PostConstruct
    void init() {
        initDefaultServerConfig();

    }

    private void initDefaultServerConfig() {
        InternalOpenttdServerConfig openttdServerConfig = this.repository.getOpenttdServerConfig();
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

    void onStart(@Observes StartupEvent ev) {
        if (!this.autoStartRunningServers) {
            LOG.info("Auto start of previously running servers is disabled (server.auto-start-running-servers=false)");
            return;
        }
        for (OpenttdServer server : this.repository.getOpenttdServerConfig().getServers()) {
            if (!server.isLastKnownRunning()) {
                continue;
            }
            try {
                LOG.infof("Server '%s' was running before the last shutdown and will be started again", server.getName());
                this.service.startServer(server.getId());
            } catch (Exception e) {
                LOG.error("Failed to start server '" + server.getId() + "' automatically", e);
            }
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        LOG.info("The application is stopping. Every running OpenTTD server will be saved and stopped.");
        this.service.shutdownAll();
    }
}
