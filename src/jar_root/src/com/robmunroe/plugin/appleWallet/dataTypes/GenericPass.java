package com.robmunroe.plugin.appleWallet.dataTypes;

import com.robmunroe.plugin.appleWallet.helpers.AppleWalletPluginHelper;
import com.robmunroe.plugin.appleWallet.helpers.PassHelper;
import de.brendamour.jpasskit.PKBarcode;
import de.brendamour.jpasskit.PKField;
import de.brendamour.jpasskit.PKLocation;
import de.brendamour.jpasskit.PKPass;
import de.brendamour.jpasskit.passes.PKGenericPass;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import java.awt.*;
import java.util.Date;
import java.util.List;

@XmlRootElement(namespace = AppleWalletPluginHelper.NAMESPACE, name = GenericPass.LOCAL_PART)
@XmlType(
        namespace = AppleWalletPluginHelper.NAMESPACE,
        name = GenericPass.LOCAL_PART,
        propOrder = {
                "passTypeIdentifier",
                "organizationName",
                "teamIdentifier",
                "authenticationToken",
                "serialNumber",
                "description",
                "logoText",
                "foregroundColor",
                "backgroundColor",
                "labelColor",

                "maxDistance",
                "relevantDate",
                "expirationDate",
                "voided",
                "sharingProhibited",

                "headerFieldsJson",
                "primaryFieldsJson",
                "secondaryFieldsJson",
                "auxiliaryFieldsJson",
                "backFieldsJson",

                "locations",
                "barcodes"
        }
)
public class GenericPass implements PassType {
    public static final String LOCAL_PART = "GenericPass";
    public static final QName QNAME = new QName(AppleWalletPluginHelper.NAMESPACE, LOCAL_PART);
    private static final long serialVersionUID = 1L;

    private String passTypeIdentifier;
    private String organizationName;
    private String teamIdentifier;
    private String authenticationToken;
    private String serialNumber;
    private String description;
    private String logoText;
    private String foregroundColor;
    private String backgroundColor;
    private String labelColor;

    private Long maxDistance;
    private Date relevantDate;
    private Date expirationDate;
    private Boolean voided;
    private Boolean sharingProhibited;

    private String headerFieldsJson;
    private String primaryFieldsJson;
    private String secondaryFieldsJson;
    private String auxiliaryFieldsJson;
    private String backFieldsJson;

    private List<PassLocation> locations;
    private List<PassBarcode> barcodes;

    public GenericPass(String passTypeIdentifier,
                       String organizationName,
                       String teamIdentifier,
                       String authenticationToken,
                       String serialNumber,
                       String description,
                       String logoText,
                       String foregroundColor,
                       String backgroundColor,
                       String labelColor,

                       Long maxDistance,
                       Date relevantDate,
                       Date expirationDate,
                       Boolean voided,
                       Boolean sharingProhibited,

                       String headerFieldsJson,
                       String primaryFieldsJson,
                       String secondaryFieldsJson,
                       String auxiliaryFieldsJson,
                       String backFieldsJson,

                       List<PassLocation> locations,
                       List<PassBarcode> barcodes
    ) {
        setPassTypeIdentifier(passTypeIdentifier);
        setOrganizationName(organizationName);
        setTeamIdentifier(teamIdentifier);
        setAuthenticationToken(authenticationToken);
        setSerialNumber(serialNumber);
        setDescription(description);
        setLogoText(logoText);
        setForegroundColor(foregroundColor);
        setBackgroundColor(backgroundColor);
        setLabelColor(labelColor);

        setMaxDistance(maxDistance);
        setRelevantDate(relevantDate);
        setExpirationDate(expirationDate);
        setVoided(voided);
        setSharingProhibited(sharingProhibited);

        setHeaderFieldsJson(headerFieldsJson);
        setPrimaryFieldsJson(primaryFieldsJson);
        setSecondaryFieldsJson(secondaryFieldsJson);
        setAuxiliaryFieldsJson(auxiliaryFieldsJson);
        setBackFieldsJson(backFieldsJson);

        setLocations(locations);
        setBarcodes(barcodes);
    }

    public GenericPass() {
    }


    @Override
    public PKPass getPass() {
        PKPass pass = new PKPass();
        pass.setPassTypeIdentifier(getPassTypeIdentifier());
        pass.setSerialNumber(getSerialNumber());
        pass.setTeamIdentifier(getTeamIdentifier());
        pass.setOrganizationName(getOrganizationName());
        pass.setDescription(getDescription());
        pass.setLogoText(getLogoText());

        if (StringUtils.isNotEmpty(getAuthenticationToken()))
            pass.setAuthenticationToken(getAuthenticationToken());

        if (StringUtils.isNotEmpty(getForegroundColor()))
            pass.setForegroundColorAsObject(Color.decode(getForegroundColor()));

        if (StringUtils.isNotEmpty(getBackgroundColor()))
            pass.setBackgroundColorAsObject(Color.decode(getBackgroundColor()));

        if (StringUtils.isNotEmpty(getLabelColor()))
            pass.setLabelColorAsObject(Color.decode(getLabelColor()));

        pass.setMaxDistance(getMaxDistance());
        pass.setRelevantDate(getRelevantDate());
        pass.setExpirationDate(getExpirationDate());
        pass.setVoided(getVoided());
        pass.setSharingProhibited(getSharingProhibited());

        List<PKBarcode> barcodes = PassHelper.buildBarcodes(getBarcodes());
        pass.setBarcodes(barcodes);

        List<PKLocation> locations = PassHelper.buildLocations(getLocations());
        pass.setLocations(locations);

        PKGenericPass generic = new PKGenericPass();

        if (StringUtils.isNotEmpty(getHeaderFieldsJson())) {
          List<PKField> headerFields = PassHelper.fieldsJsonToPKFields(getHeaderFieldsJson());
          generic.setHeaderFields(headerFields);
        }

        if (StringUtils.isNotEmpty(getPrimaryFieldsJson())) {
          List<PKField> primaryFields = PassHelper.fieldsJsonToPKFields(getPrimaryFieldsJson());
          generic.setPrimaryFields(primaryFields);
        }

        if (StringUtils.isNotEmpty(getSecondaryFieldsJson())) {
          List<PKField> secondaryFields = PassHelper.fieldsJsonToPKFields(getSecondaryFieldsJson());
          generic.setSecondaryFields(secondaryFields);
        }

        if (StringUtils.isNotEmpty(getAuxiliaryFieldsJson())) {
          List<PKField> auxiliaryFields = PassHelper.fieldsJsonToPKFields(getAuxiliaryFieldsJson());
          generic.setAuxiliaryFields(auxiliaryFields);
        }

        if (StringUtils.isNotEmpty(getBackFieldsJson())) {
          List<PKField> backFields = PassHelper.fieldsJsonToPKFields(getBackFieldsJson());
          generic.setBackFields(backFields);
        }

        pass.setGeneric(generic);

        return pass;
    }


    @XmlElement
    public String getPassTypeIdentifier() {
        return passTypeIdentifier;
    }

    public void setPassTypeIdentifier(String passTypeIdentifier) {
        this.passTypeIdentifier = passTypeIdentifier;
    }

    @XmlElement
    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    @XmlElement
    public String getTeamIdentifier() {
        return teamIdentifier;
    }

    public void setTeamIdentifier(String teamIdentifier) {
        this.teamIdentifier = teamIdentifier;
    }

    @XmlElement
    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public void setAuthenticationToken(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    @XmlElement
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement
    public String getLogoText() {
        return logoText;
    }

    public void setLogoText(String logoText) {
        this.logoText = logoText;
    }

    @XmlElement
    public String getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(String foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    @XmlElement
    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @XmlElement
    public String getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(String labelColor) {
        this.labelColor = labelColor;
    }

    @XmlElement
    public Long getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(Long maxDistance) {
        this.maxDistance = maxDistance;
    }

    @XmlElement
    public Date getRelevantDate() {
        return relevantDate;
    }

    public void setRelevantDate(Date relevantDate) {
        this.relevantDate = relevantDate;
    }

    @XmlElement
    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    @XmlElement
    public Boolean getVoided() {
        return voided;
    }

    public void setVoided(Boolean voided) {
        this.voided = voided;
    }

    @XmlElement
    public Boolean getSharingProhibited() {
        return sharingProhibited;
    }

    public void setSharingProhibited(Boolean sharingProhibited) {
        this.sharingProhibited = sharingProhibited;
    }

    @XmlElement
    public String getHeaderFieldsJson() {
        return headerFieldsJson;
    }

    public void setHeaderFieldsJson(String headerFieldsJson) {
        this.headerFieldsJson = headerFieldsJson;
    }

    @XmlElement
    public String getPrimaryFieldsJson() {
        return primaryFieldsJson;
    }

    public void setPrimaryFieldsJson(String primaryFieldsJson) {
        this.primaryFieldsJson = primaryFieldsJson;
    }

    @XmlElement
    public String getSecondaryFieldsJson() {
        return secondaryFieldsJson;
    }

    public void setSecondaryFieldsJson(String secondaryFieldsJson) {
        this.secondaryFieldsJson = secondaryFieldsJson;
    }

    @XmlElement
    public String getAuxiliaryFieldsJson() {
        return auxiliaryFieldsJson;
    }

    public void setAuxiliaryFieldsJson(String auxiliaryFieldsJson) {
        this.auxiliaryFieldsJson = auxiliaryFieldsJson;
    }

    @XmlElement
    public String getBackFieldsJson() {
        return backFieldsJson;
    }

    public void setBackFieldsJson(String backFieldsJson) {
        this.backFieldsJson = backFieldsJson;
    }

    @XmlElement
    public List<PassLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<PassLocation> locations) {
        this.locations = locations;
    }

    @XmlElement
    public List<PassBarcode> getBarcodes() {
        return barcodes;
    }

    public void setBarcodes(List<PassBarcode> barcodes) {
        this.barcodes = barcodes;
    }

}
