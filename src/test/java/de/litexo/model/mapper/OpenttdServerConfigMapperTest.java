package de.litexo.model.mapper;

import de.litexo.api.ServiceRuntimeException;
import de.litexo.model.external.OpenttdServerConfigUpdate;
import de.litexo.model.internal.InternalOpenttdServerConfig;
import de.litexo.security.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenttdServerConfigMapperTest {

    private final OpenttdServerConfigMapper subject = new OpenttdServerConfigMapperImpl();

    private InternalOpenttdServerConfig config() {
        InternalOpenttdServerConfig config = new InternalOpenttdServerConfig();
        config.setAutoSaveMinutes(5);
        config.setNumberOfAutoSaveFilesToKeep(10);
        config.setNumberOfManuallySaveFilesToKeep(20);
        config.setPasswordSha256Hash(SecurityUtils.toSHA256("Password_1"));
        config.setPath("/tmp/openttd");
        return config;
    }

    @DisplayName("Test that a patch of one number leaves the other numbers untouched")
    @Test
    void test_partialPatchKeepsTheOtherValues() {
        InternalOpenttdServerConfig config = config();
        OpenttdServerConfigUpdate update = new OpenttdServerConfigUpdate();
        update.setAutoSaveMinutes(1);

        this.subject.patch(update, config);

        assertEquals(1, config.getAutoSaveMinutes());
        assertEquals(10, config.getNumberOfAutoSaveFilesToKeep());
        assertEquals(20, config.getNumberOfManuallySaveFilesToKeep());
    }

    @DisplayName("Test that an empty patch changes nothing")
    @Test
    void test_emptyPatchChangesNothing() {
        InternalOpenttdServerConfig config = config();

        this.subject.patch(new OpenttdServerConfigUpdate(), config);

        assertEquals(5, config.getAutoSaveMinutes());
        assertEquals(10, config.getNumberOfAutoSaveFilesToKeep());
        assertEquals(20, config.getNumberOfManuallySaveFilesToKeep());
        assertEquals(SecurityUtils.toSHA256("Password_1"), config.getPasswordSha256Hash());
        assertEquals("/tmp/openttd", config.getPath());
    }

    @DisplayName("Test that a full patch applies every number")
    @Test
    void test_fullPatchAppliesEveryValue() {
        InternalOpenttdServerConfig config = config();
        OpenttdServerConfigUpdate update = new OpenttdServerConfigUpdate();
        update.setAutoSaveMinutes(2);
        update.setNumberOfAutoSaveFilesToKeep(3);
        update.setNumberOfManuallySaveFilesToKeep(4);

        this.subject.patch(update, config);

        assertEquals(2, config.getAutoSaveMinutes());
        assertEquals(3, config.getNumberOfAutoSaveFilesToKeep());
        assertEquals(4, config.getNumberOfManuallySaveFilesToKeep());
    }

    @DisplayName("Test that zero is applied, it is a value and not an absent field")
    @Test
    void test_zeroIsApplied() {
        InternalOpenttdServerConfig config = config();
        OpenttdServerConfigUpdate update = new OpenttdServerConfigUpdate();
        update.setAutoSaveMinutes(0);

        this.subject.patch(update, config);

        assertEquals(0, config.getAutoSaveMinutes());
        assertEquals(10, config.getNumberOfAutoSaveFilesToKeep());
    }

    @DisplayName("Test that the password is replaced when the old one matches")
    @Test
    void test_passwordChangeWithCorrectOldPassword() {
        InternalOpenttdServerConfig config = config();
        OpenttdServerConfigUpdate update = new OpenttdServerConfigUpdate();
        update.setOldPassword("Password_1");
        update.setPassword("Password_2");

        this.subject.patch(update, config);

        assertEquals(SecurityUtils.toSHA256("Password_2"), config.getPasswordSha256Hash());
        // A pure password change must not touch the numbers
        assertEquals(5, config.getAutoSaveMinutes());
        assertEquals(10, config.getNumberOfAutoSaveFilesToKeep());
        assertEquals(20, config.getNumberOfManuallySaveFilesToKeep());
    }

    @DisplayName("Test that a wrong old password is rejected")
    @Test
    void test_passwordChangeWithWrongOldPassword() {
        InternalOpenttdServerConfig config = config();
        OpenttdServerConfigUpdate update = new OpenttdServerConfigUpdate();
        update.setOldPassword("wrong");
        update.setPassword("Password_2");

        ServiceRuntimeException exception = assertThrows(ServiceRuntimeException.class, () -> this.subject.patch(update, config));

        assertTrue(exception.getMessage().contains("Old password is not correct"));
        assertEquals(SecurityUtils.toSHA256("Password_1"), config.getPasswordSha256Hash());
    }

    @DisplayName("Test that 'toExternal' copies the numbers of the config")
    @Test
    void test_toExternalCopiesTheNumbers() {
        InternalOpenttdServerConfig config = config();

        assertEquals(5, this.subject.toExternal(config).getAutoSaveMinutes());
        assertEquals(10, this.subject.toExternal(config).getNumberOfAutoSaveFilesToKeep());
        assertEquals(20, this.subject.toExternal(config).getNumberOfManuallySaveFilesToKeep());
    }
}
