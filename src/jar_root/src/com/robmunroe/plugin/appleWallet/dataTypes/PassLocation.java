package com.robmunroe.plugin.appleWallet.dataTypes;

import com.robmunroe.plugin.appleWallet.helpers.AppleWalletPluginHelper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

@XmlRootElement(namespace = AppleWalletPluginHelper.NAMESPACE, name = PassLocation.LOCAL_PART)
@XmlType(
        namespace = AppleWalletPluginHelper.NAMESPACE,
        name = PassLocation.LOCAL_PART,
        propOrder = {"altitude", "latitude", "longitude", "relevantText"}
)
public class PassLocation {
    public static final String LOCAL_PART = "PassLocation";
    public static final QName QNAME = new QName(AppleWalletPluginHelper.NAMESPACE, LOCAL_PART);
    private static final long serialVersionUID = 1L;


    private Double altitude;
    private Double latitude;
    private Double longitude;
    private String relevantText;

    public PassLocation(Double altitude, Double latitude, Double longitude, String relevantText) {
        this();
        this.setAltitude(altitude);
        this.setLatitude(latitude);
        this.setLongitude(longitude);
        this.setRelevantText(relevantText);
    }

    public PassLocation() {
    }

    @XmlElement
    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    @XmlElement
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @XmlElement
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @XmlElement
    public String getRelevantText() {
        return relevantText;
    }

    public void setRelevantText(String relevantText) {
        this.relevantText = relevantText;
    }
}
