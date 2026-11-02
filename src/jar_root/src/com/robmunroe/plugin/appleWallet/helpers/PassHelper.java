package com.robmunroe.plugin.appleWallet.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.reflect.TypeToken;
import com.robmunroe.plugin.appleWallet.dataTypes.PassBarcode;
import com.robmunroe.plugin.appleWallet.dataTypes.PassLocation;
import de.brendamour.jpasskit.PKBarcode;
import de.brendamour.jpasskit.PKField;
import de.brendamour.jpasskit.PKLocation;
import de.brendamour.jpasskit.PKPass;
import de.brendamour.jpasskit.PKRelevantDate;
import de.brendamour.jpasskit.enums.PKBarcodeFormat;
import de.brendamour.jpasskit.signing.PKInMemorySigningUtil;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PassHelper {
    /**
     * jpasskit writes an empty relevantDates array into every pass. This plugin never sets relevantDates, and Apple
     * doesn't document how Wallet treats an empty array alongside relevantDate, so it is left out as it always was.
     */
    private abstract static class OmitEmptyRelevantDates {
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        abstract List<PKRelevantDate> getRelevantDates();
    }


    public static PKInMemorySigningUtil newSigningUtil() {
        return new PKInMemorySigningUtil(new ObjectMapper().addMixIn(PKPass.class, OmitEmptyRelevantDates.class));
    }


    public static List<PKField> fieldsJsonToPKFields(String fieldsJson) {
        List<PKField> fields = new ArrayList<>();

        // Without this policy Gson parses every number as a Double, so 5 would display on the pass as 5.0
        Gson gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();
        List<Map<String, Object>> fieldMaps = gson.fromJson(fieldsJson, new TypeToken<ArrayList<Map<String, Object>>>(){}.getType());
        if (fieldMaps == null) return fields;

        for (Map<String, Object> fieldMap : fieldMaps) {
            Object value = fieldMap.get("value");
            if (value instanceof Map) {
                value = ((Map<?, ?>) value).get("id");
            }

            PKField field = PKField.builder()
                    .key(toTextOrNull(fieldMap.get("key")))
                    .label(toTextOrNull(fieldMap.get("label")))
                    .value(value == null ? "" : String.valueOf(value))
                    .build();

            fields.add(field);
        }

        return fields;
    }


    private static String toTextOrNull(Object value) {
        return value == null ? null : String.valueOf(value);
    }


    public static List<PKBarcode> buildBarcodes(List<PassBarcode> barcodes) {
        if (barcodes == null) return null;

        List<PKBarcode> pkBarcodes = new ArrayList<>();

        for (PassBarcode barcode : barcodes) {
            PKBarcode pkBarcode = PKBarcode.builder()
                    .format((PKBarcodeFormat.valueOf(barcode.getFormat())))
                    .messageEncoding(Charset.forName(barcode.getMessageEncoding()))
                    .message(barcode.getMessage())
                    .build();

            pkBarcodes.add(pkBarcode);
        }

        return pkBarcodes;
    }


    public static List<PKLocation> buildLocations(List<PassLocation> locations) {
      if (locations == null) return null;

      List<PKLocation> pkLocations = new ArrayList<>();

        for (PassLocation location : locations) {
            PKLocation pkLocation = PKLocation.builder()
                    .altitude(location.getAltitude())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .relevantText(location.getRelevantText())
                    .build();

            pkLocations.add(pkLocation);
        }

        return pkLocations;
    }
}
