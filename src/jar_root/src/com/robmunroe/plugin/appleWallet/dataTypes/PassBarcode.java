package com.robmunroe.plugin.appleWallet.dataTypes;

import com.robmunroe.plugin.appleWallet.helpers.AppleWalletPluginHelper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

@XmlRootElement(namespace = AppleWalletPluginHelper.NAMESPACE, name = PassBarcode.LOCAL_PART)
@XmlType(
        namespace = AppleWalletPluginHelper.NAMESPACE,
        name = PassBarcode.LOCAL_PART,
        propOrder = {"format", "messageEncoding", "message"}
)
public class PassBarcode {
    public static final String LOCAL_PART = "PassBarcode";
    public static final QName QNAME = new QName(AppleWalletPluginHelper.NAMESPACE, LOCAL_PART);
    private static final long serialVersionUID = 1L;

    private String format; // (PKBarcodeFormat.PKBarcodeFormatPDF417);
    private String messageEncoding; // (StandardCharsets.ISO_8859_1);
    private String message; // ("123456789");

    public PassBarcode(String format, String messageEncoding, String message) {
        this();
        this.setFormat(format);
        this.setMessageEncoding(messageEncoding);
        this.setMessage(message);
    }

    public PassBarcode() {
    }

    @XmlElement
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @XmlElement
    public String getMessageEncoding() {
        return messageEncoding;
    }

    public void setMessageEncoding(String messageEncoding) {
        this.messageEncoding = messageEncoding;
    }

    @XmlElement
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
