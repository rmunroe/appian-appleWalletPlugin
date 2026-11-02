package com.robmunroe.plugin.appleWallet.dataTypes;

import de.brendamour.jpasskit.PKPass;
import de.brendamour.jpasskit.PKPassBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Apple treats these as optional, so a CDT that leaves them unset must still produce a pass. */
class GenericPassOptionalFieldsTest {

    private static GenericPass passWithRequiredFieldsOnly() {
        GenericPass pass = new GenericPass();
        pass.setPassTypeIdentifier("pass.test.example");
        pass.setOrganizationName("Example Org");
        pass.setTeamIdentifier("TEAM123456");
        pass.setSerialNumber("SERIAL-001");
        pass.setDescription("Example description");
        return pass;
    }

    @Test
    void unsetDatesAndFlagsStillBuildAValidPass() {
        PKPassBuilder builder = passWithRequiredFieldsOnly().getPassBuilder();

        assertTrue(builder.isValid(), () -> "validation errors: " + builder.getValidationErrors());
        PKPass pass = builder.build();
        assertNull(pass.getRelevantDate());
        assertNull(pass.getExpirationDate());
        assertFalse(pass.isVoided());
        assertFalse(pass.isSharingProhibited());
    }

    @Test
    void flagsThatAreSetAreStillApplied() {
        GenericPass source = passWithRequiredFieldsOnly();
        source.setVoided(true);
        source.setSharingProhibited(true);

        PKPass pass = source.getPassBuilder().build();

        assertEquals(true, pass.isVoided());
        assertEquals(true, pass.isSharingProhibited());
    }
}
