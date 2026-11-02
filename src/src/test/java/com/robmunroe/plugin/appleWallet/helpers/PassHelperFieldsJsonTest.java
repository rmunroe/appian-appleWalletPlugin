package com.robmunroe.plugin.appleWallet.helpers;

import de.brendamour.jpasskit.PKField;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PassHelperFieldsJsonTest {

    private static PKField onlyField(String json) {
        List<PKField> fields = PassHelper.fieldsJsonToPKFields(json);
        assertEquals(1, fields.size());
        return fields.get(0);
    }

    @Test
    void textValueIsUnchanged() {
        assertEquals("hello", onlyField("[{\"key\":\"k\",\"label\":\"l\",\"value\":\"hello\"}]").getValue());
    }

    @Test
    void wholeNumberDisplaysWithoutADecimalPoint() {
        assertEquals("5", onlyField("[{\"key\":\"k\",\"value\":5}]").getValue());
    }

    @Test
    void largeWholeNumberDisplaysInFullNotInScientificNotation() {
        assertEquals("10000000000", onlyField("[{\"key\":\"k\",\"value\":10000000000}]").getValue());
    }

    @Test
    void decimalNumberKeepsItsDecimalPlaces() {
        assertEquals("1.5", onlyField("[{\"key\":\"k\",\"value\":1.5}]").getValue());
    }

    @Test
    void booleanValueDisplaysAsText() {
        assertEquals("true", onlyField("[{\"key\":\"k\",\"value\":true}]").getValue());
    }

    @Test
    void objectValueUsesItsTextId() {
        assertEquals("abc", onlyField("[{\"key\":\"k\",\"value\":{\"id\":\"abc\"}}]").getValue());
    }

    @Test
    void objectValueWithANumericIdDisplaysWithoutADecimalPoint() {
        assertEquals("7", onlyField("[{\"key\":\"k\",\"value\":{\"id\":7}}]").getValue());
    }

    @Test
    void nullValueIsBlankNotTheWordNull() {
        assertEquals("", onlyField("[{\"key\":\"k\",\"value\":null}]").getValue());
    }

    @Test
    void missingValueIsBlankNotTheWordNull() {
        assertEquals("", onlyField("[{\"key\":\"k\"}]").getValue());
    }

    @Test
    void objectValueWithoutAnIdIsBlankNotTheWordNull() {
        assertEquals("", onlyField("[{\"key\":\"k\",\"value\":{\"other\":1}}]").getValue());
    }

    @Test
    void nullJsonGivesNoFieldsRatherThanFailing() {
        assertTrue(PassHelper.fieldsJsonToPKFields("null").isEmpty());
    }

    @Test
    void numericKeyAndLabelAreAccepted() {
        PKField field = onlyField("[{\"key\":1,\"label\":2,\"value\":\"v\"}]");

        assertEquals("1", field.getKey());
        assertEquals("2", field.getLabel());
    }

    @Test
    void missingLabelStaysUnsetSoItIsLeftOutOfThePass() {
        assertNull(onlyField("[{\"key\":\"k\",\"value\":\"v\"}]").getLabel());
    }
}
