package com.robmunroe.plugin.appleWallet.helpers;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.robmunroe.plugin.appleWallet.dataTypes.PassBarcode;
import com.robmunroe.plugin.appleWallet.dataTypes.PassLocation;
import de.brendamour.jpasskit.PKBarcode;
import de.brendamour.jpasskit.PKField;
import de.brendamour.jpasskit.PKLocation;
import de.brendamour.jpasskit.enums.PKBarcodeFormat;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PassHelper {
    public static List<PKField> fieldsJsonToPKFields(String fieldsJson) {
        List<PKField> fields = new ArrayList<>();

        Gson gson = new Gson();
        List<Map<String, Object>> fieldMaps = gson.fromJson(fieldsJson, new TypeToken<ArrayList<Map<String, Object>>>(){}.getType());
        for (Map<String, Object> fieldMap : fieldMaps) {
            Object value = fieldMap.get("value");
            if (value instanceof LinkedTreeMap) {
                value = ((Map<String, Object>)fieldMap.get("value")).get("id");
            }

            PKField field = PKField.builder()
                    .key((String) fieldMap.get("key"))
                    .label((String) fieldMap.get("label"))
                    .value(String.valueOf(value))
                    .build();

            fields.add(field);
        }

        return fields;
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
