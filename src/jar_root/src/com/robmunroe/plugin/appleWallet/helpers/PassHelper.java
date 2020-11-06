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

import java.io.Serializable;
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
            PKField field = new PKField();
            field.setKey((String) fieldMap.get("key"));
            field.setLabel((String) fieldMap.get("label"));

            Object value = fieldMap.get("value");
            if (value instanceof LinkedTreeMap) {
                value = ((Map<String, Object>)fieldMap.get("value")).get("id");
            }
            field.setValue((Serializable) value);

            fields.add(field);
        }

        return fields;
    }


    public static List<PKBarcode> buildBarcodes(List<PassBarcode> barcodes) {
        List<PKBarcode> pkBarcodes = new ArrayList<>();

        for (PassBarcode barcode : barcodes) {
            PKBarcode pkBarcode = new PKBarcode();
            pkBarcode.setFormat(PKBarcodeFormat.valueOf(barcode.getFormat()));
            pkBarcode.setMessageEncoding(Charset.forName(barcode.getMessageEncoding()));
            pkBarcode.setMessage(barcode.getMessage());
            pkBarcodes.add(pkBarcode);
        }

        return pkBarcodes;
    }


    public static List<PKLocation> buildLocations(List<PassLocation> locations) {
        List<PKLocation> pkLocations = new ArrayList<>();

        for (PassLocation location : locations) {
            PKLocation pkLocation = new PKLocation();

            pkLocation.setAltitude(location.getAltitude());
            pkLocation.setLatitude(location.getLatitude());
            pkLocation.setLongitude(location.getLongitude());
            pkLocation.setRelevantText(location.getRelevantText());

            pkLocations.add(pkLocation);
        }

        return pkLocations;
    }
}
