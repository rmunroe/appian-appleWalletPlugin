package com.robmunroe.plugin.appleWallet.dataTypes;

import com.robmunroe.plugin.appleWallet.helpers.AppleWalletPluginHelper;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

@XmlRootElement(namespace = AppleWalletPluginHelper.NAMESPACE, name = PassImages.LOCAL_PART)
@XmlType(
        namespace = AppleWalletPluginHelper.NAMESPACE,
        name = PassImages.LOCAL_PART,
        propOrder = {
                "icon", "iconRetina", "iconRetinaHd",
                "logo", "logoRetina", "logoRetinaHd",
                "thumbnail", "thumbnailRetina", "thumbnailRetinaHd",
                "strip", "stripRetina", "stripRetinaHd",
                "background", "backgroundRetina", "backgroundRetinaHd",
                "footer", "footerRetina", "footerRetinaHd",
                "personalizationLogo", "personalizationLogoRetina", "personalizationLogoRetinaHd"
        }
)
public class PassImages {
    public static final String LOCAL_PART = "PassImages";
    public static final QName QNAME = new QName(AppleWalletPluginHelper.NAMESPACE, LOCAL_PART);
    private static final long serialVersionUID = 1L;

    private Long icon;
    private Long iconRetina;
    private Long iconRetinaHd;
    private Long logo;
    private Long logoRetina;
    private Long logoRetinaHd;
    private Long thumbnail;
    private Long thumbnailRetina;
    private Long thumbnailRetinaHd;
    private Long strip;
    private Long stripRetina;
    private Long stripRetinaHd;
    private Long background;
    private Long backgroundRetina;
    private Long backgroundRetinaHd;
    private Long footer;
    private Long footerRetina;
    private Long footerRetinaHd;
    private Long personalizationLogo;
    private Long personalizationLogoRetina;
    private Long personalizationLogoRetinaHd;

    public PassImages(
            Long icon, Long iconRetina, Long iconRetinaHd,
            Long logo, Long logoRetina, Long logoRetinaHd,
            Long thumbnail, Long thumbnailRetina, Long thumbnailRetinaHd,
            Long strip, Long stripRetina, Long stripRetinaHd,
            Long background, Long backgroundRetina, Long backgroundRetinaHd,
            Long footer, Long footerRetina, Long footerRetinaHd,
            Long personalizationLogo, Long personalizationLogoRetina, Long personalizationLogoRetinaHd
    ) {
        this();
        this.setIcon(icon);
        this.setIconRetina(iconRetina);
        this.setIconRetinaHd(iconRetinaHd);
        this.setLogo(logo);
        this.setLogoRetina(logoRetina);
        this.setLogoRetinaHd(logoRetinaHd);
        this.setThumbnail(thumbnail);
        this.setThumbnailRetina(thumbnailRetina);
        this.setThumbnailRetinaHd(thumbnailRetinaHd);
        this.setStrip(strip);
        this.setStripRetina(stripRetina);
        this.setStripRetinaHd(stripRetinaHd);
        this.setBackground(background);
        this.setBackgroundRetina(backgroundRetina);
        this.setBackgroundRetinaHd(backgroundRetinaHd);
        this.setFooter(footer);
        this.setFooterRetina(footerRetina);
        this.setFooterRetinaHd(footerRetinaHd);
        this.setPersonalizationLogo(personalizationLogo);
        this.setPersonalizationLogoRetina(personalizationLogoRetina);
        this.setPersonalizationLogoRetinaHd(personalizationLogoRetinaHd);
    }

    public PassImages() {
    }

    @XmlElement
    public Long getIcon() {
        return icon;
    }

    public void setIcon(Long icon) {
        this.icon = icon;
    }

    @XmlElement
    public Long getIconRetina() {
        return iconRetina;
    }

    public void setIconRetina(Long iconRetina) {
        this.iconRetina = iconRetina;
    }

    @XmlElement
    public Long getIconRetinaHd() {
        return iconRetinaHd;
    }

    public void setIconRetinaHd(Long iconRetinaHd) {
        this.iconRetinaHd = iconRetinaHd;
    }

    @XmlElement
    public Long getLogo() {
        return logo;
    }

    public void setLogo(Long logo) {
        this.logo = logo;
    }

    @XmlElement
    public Long getLogoRetina() {
        return logoRetina;
    }

    public void setLogoRetina(Long logoRetina) {
        this.logoRetina = logoRetina;
    }

    @XmlElement
    public Long getLogoRetinaHd() {
        return logoRetinaHd;
    }

    public void setLogoRetinaHd(Long logoRetinaHd) {
        this.logoRetinaHd = logoRetinaHd;
    }

    @XmlElement
    public Long getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Long thumbnail) {
        this.thumbnail = thumbnail;
    }

    @XmlElement
    public Long getThumbnailRetina() {
        return thumbnailRetina;
    }

    public void setThumbnailRetina(Long thumbnailRetina) {
        this.thumbnailRetina = thumbnailRetina;
    }

    @XmlElement
    public Long getThumbnailRetinaHd() {
        return thumbnailRetinaHd;
    }

    public void setThumbnailRetinaHd(Long thumbnailRetinaHd) {
        this.thumbnailRetinaHd = thumbnailRetinaHd;
    }

    @XmlElement
    public Long getStrip() {
        return strip;
    }

    public void setStrip(Long strip) {
        this.strip = strip;
    }

    @XmlElement
    public Long getStripRetina() {
        return stripRetina;
    }

    public void setStripRetina(Long stripRetina) {
        this.stripRetina = stripRetina;
    }

    @XmlElement
    public Long getStripRetinaHd() {
        return stripRetinaHd;
    }

    public void setStripRetinaHd(Long stripRetinaHd) {
        this.stripRetinaHd = stripRetinaHd;
    }

    @XmlElement
    public Long getBackground() {
        return background;
    }

    public void setBackground(Long background) {
        this.background = background;
    }

    @XmlElement
    public Long getBackgroundRetina() {
        return backgroundRetina;
    }

    public void setBackgroundRetina(Long backgroundRetina) {
        this.backgroundRetina = backgroundRetina;
    }

    @XmlElement
    public Long getBackgroundRetinaHd() {
        return backgroundRetinaHd;
    }

    public void setBackgroundRetinaHd(Long backgroundRetinaHd) {
        this.backgroundRetinaHd = backgroundRetinaHd;
    }

    @XmlElement
    public Long getFooter() {
        return footer;
    }

    public void setFooter(Long footer) {
        this.footer = footer;
    }

    @XmlElement
    public Long getFooterRetina() {
        return footerRetina;
    }

    public void setFooterRetina(Long footerRetina) {
        this.footerRetina = footerRetina;
    }

    @XmlElement
    public Long getFooterRetinaHd() {
        return footerRetinaHd;
    }

    public void setFooterRetinaHd(Long footerRetinaHd) {
        this.footerRetinaHd = footerRetinaHd;
    }

    @XmlElement
    public Long getPersonalizationLogo() {
        return personalizationLogo;
    }

    public void setPersonalizationLogo(Long personalizationLogo) {
        this.personalizationLogo = personalizationLogo;
    }

    @XmlElement
    public Long getPersonalizationLogoRetina() {
        return personalizationLogoRetina;
    }

    public void setPersonalizationLogoRetina(Long personalizationLogoRetina) {
        this.personalizationLogoRetina = personalizationLogoRetina;
    }

    @XmlElement
    public Long getPersonalizationLogoRetinaHd() {
        return personalizationLogoRetinaHd;
    }

    public void setPersonalizationLogoRetinaHd(Long personalizationLogoRetinaHd) {
        this.personalizationLogoRetinaHd = personalizationLogoRetinaHd;
    }
}