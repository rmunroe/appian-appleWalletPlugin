package com.robmunroe.plugin.appleWallet.smartServices;

import com.appiancorp.suiteapi.common.Name;
import com.appiancorp.suiteapi.common.exceptions.AppianException;
import com.appiancorp.suiteapi.content.ContentOutputStream;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.knowledge.Document;
import com.appiancorp.suiteapi.knowledge.DocumentDataType;
import com.appiancorp.suiteapi.knowledge.FolderDataType;
import com.appiancorp.suiteapi.process.exceptions.SmartServiceException;
import com.appiancorp.suiteapi.process.framework.AppianSmartService;
import com.appiancorp.suiteapi.process.framework.Input;
import com.appiancorp.suiteapi.process.framework.MessageContainer;
import com.appiancorp.suiteapi.process.framework.Required;
import com.appiancorp.suiteapi.security.external.SecureCredentialsStore;
import com.robmunroe.plugin.appleWallet.dataTypes.PassImages;
import com.robmunroe.plugin.appleWallet.dataTypes.PassType;
import com.robmunroe.plugin.appleWallet.helpers.AppleWalletPluginHelper;
import com.robmunroe.plugin.appleWallet.helpers.ContentHelper;
import de.brendamour.jpasskit.PKPassBuilder;
import de.brendamour.jpasskit.signing.PKInMemorySigningUtil;
import de.brendamour.jpasskit.signing.PKPassTemplateInMemory;
import de.brendamour.jpasskit.signing.PKSigningInformation;
import de.brendamour.jpasskit.signing.PKSigningInformationUtil;
import org.apache.commons.io.IOUtils;
//import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class CreatePassSmartService extends AppianSmartService {
//    private static final Logger LOG = Logger.getLogger(CreatePassSmartService.class);

    private final SecureCredentialsStore scs;
    private final ContentService contentService;

    // Input private vars
    private Long appleWWDRCA;
    private Long privateKey;
    private String passwordScsKey;
    private Long outputFolder;
    private String outputFilename;
    private PassImages passImages;

    // Output private vars
    private Long outputFile;
    private String errorTxt;
    private Boolean errorOccurred = false;


    public CreatePassSmartService(ContentService contentService, SecureCredentialsStore scs) {
        super();
        this.scs = scs;
        this.contentService = contentService;
    }

    protected void buildPass(PassType passType) {
        try {
            // Create a new Document for the Pass
            Document outputDocument = AppleWalletPluginHelper.createNewDocument(
                    this.outputFilename,
                    "Generated by the Apple Wallet Plugin",
                    this.outputFolder,
                    AppleWalletPluginHelper.PASS_EXTENSION
            );
            // Get an OutputStream for the new Document
            try (ContentOutputStream outputStream = AppleWalletPluginHelper.uploadDocumentForWriting(this.contentService, outputDocument)) {

                // Get the path to the Apple WWDRCA file
                String appleWWDRCAPath = AppleWalletPluginHelper.getPhysicalFilePath(
                        this.contentService,
                        this.appleWWDRCA
                );

                // Get the path to the private key
                String privateKeyPath = AppleWalletPluginHelper.getPhysicalFilePath(
                        this.contentService,
                        this.privateKey
                );

                // Get the password from the Secure Credential Store
                String privateKeyPassword = scs
                        .getSystemSecuredValues(this.passwordScsKey)
                        .get("password");

                PKInMemorySigningUtil signingUtil = new PKInMemorySigningUtil();
                PKSigningInformationUtil PKSigningInformationUtil = new PKSigningInformationUtil();

                PKSigningInformation pkSigningInformation = PKSigningInformationUtil
                        .loadSigningInformationFromPKCS12AndIntermediateCertificate(
                                privateKeyPath, privateKeyPassword, appleWWDRCAPath
                        );

                PKPassBuilder passBuilder = passType.getPassBuilder();

                if (passBuilder.isValid()) {
                    byte[] passZipAsByteArray = signingUtil.createSignedAndZippedPkPassArchive(
                            passBuilder.build(),
                            addImages(new PKPassTemplateInMemory()),
                            pkSigningInformation
                    );

                    IOUtils.copy(new ByteArrayInputStream(passZipAsByteArray), outputStream);

                    this.outputFile = outputStream.getContentId();
                } else {
                    this.errorOccurred = true;
                    this.errorTxt = "The provided arguments did not produce a valid Pass: " +
                            String.join("; ", passBuilder.getValidationErrors());
                }
            }

        } catch (Exception e) {
//            LOG.error(e.getMessage());
            errorOccurred = true;
            errorTxt = "The provided arguments did not produce a valid Pass: " + e.getLocalizedMessage();
        }
    }

    protected File getImageFile(Long imageId) throws AppianException {
        return new File(ContentHelper.getPhysicalFilePath(this.contentService, imageId));
    }

    protected void addImageFile(PKPassTemplateInMemory template, String path, Long imageId) throws AppianException, IOException {
        if (imageId != null && imageId > 0)
            template.addFile(path, new File(ContentHelper.getPhysicalFilePath(this.contentService, imageId)));
    }

    protected PKPassTemplateInMemory addImages(PKPassTemplateInMemory template) throws Exception {
        // Icon is REQUIRED
        if (this.passImages.getIcon() < 1) throw new Exception("Icon image is required");

        addImageFile(template, PKPassTemplateInMemory.PK_ICON, this.passImages.getIcon());
        addImageFile(template, PKPassTemplateInMemory.PK_ICON_RETINA, this.passImages.getIconRetina());
        addImageFile(template, PKPassTemplateInMemory.PK_ICON_RETINAHD, this.passImages.getIconRetinaHd());

        addImageFile(template, PKPassTemplateInMemory.PK_LOGO, this.passImages.getLogo());
        addImageFile(template, PKPassTemplateInMemory.PK_LOGO_RETINA, this.passImages.getLogoRetina());
        addImageFile(template, PKPassTemplateInMemory.PK_LOGO_RETINAHD, this.passImages.getLogoRetinaHd());

        addImageFile(template, PKPassTemplateInMemory.PK_THUMBNAIL, this.passImages.getThumbnail());
        addImageFile(template, PKPassTemplateInMemory.PK_THUMBNAIL_RETINA, this.passImages.getThumbnailRetina());
        addImageFile(template, PKPassTemplateInMemory.PK_THUMBNAIL_RETINAHD, this.passImages.getThumbnailRetinaHd());

        addImageFile(template, PKPassTemplateInMemory.PK_STRIP, this.passImages.getStrip());
        addImageFile(template, PKPassTemplateInMemory.PK_STRIP_RETINA, this.passImages.getStripRetina());
        addImageFile(template, PKPassTemplateInMemory.PK_STRIP_RETINAHD, this.passImages.getStripRetinaHd());

        addImageFile(template, PKPassTemplateInMemory.PK_BACKGROUND, this.passImages.getBackground());
        addImageFile(template, PKPassTemplateInMemory.PK_BACKGROUND_RETINA, this.passImages.getBackgroundRetina());
        addImageFile(template, PKPassTemplateInMemory.PK_BACKGROUND_RETINAHD, this.passImages.getBackgroundRetinaHd());

        addImageFile(template, PKPassTemplateInMemory.PK_FOOTER, this.passImages.getFooter());
        addImageFile(template, PKPassTemplateInMemory.PK_FOOTER_RETINA, this.passImages.getFooterRetina());
        addImageFile(template, PKPassTemplateInMemory.PK_FOOTER_RETINAHD, this.passImages.getFooterRetinaHd());

        addImageFile(template, PKPassTemplateInMemory.PK_PERSONALIZATION_LOGO, this.passImages.getPersonalizationLogo());
        addImageFile(template, PKPassTemplateInMemory.PK_PERSONALIZATION_LOGO_RETINA, this.passImages.getPersonalizationLogoRetina());
        addImageFile(template, PKPassTemplateInMemory.PK_PERSONALIZATION_LOGO_RETINAHD, this.passImages.getPersonalizationLogoRetinaHd());

        return template;
    }


    @Override
    public void run() throws SmartServiceException {
    }

    @Override
    public void onSave(MessageContainer messages) {
    }

    @Override
    public void validate(MessageContainer messages) {
    }


    // Inputs

    @Input(required = Required.ALWAYS)
    @Name("appleWWDRCA")
    @DocumentDataType
    public void setAppleWWDRCA(Long val) {
        this.appleWWDRCA = val;
    }

    @Input(required = Required.ALWAYS)
    @Name("privateKey")
    @DocumentDataType
    public void setPrivateKey(Long val) {
        this.privateKey = val;
    }

    @Input(required = Required.ALWAYS)
    @Name("passwordScsKey")
    public void setPasswordScsKey(String val) {
        this.passwordScsKey = val;
    }

    @Input(required = Required.ALWAYS)
    @Name("outputFolder")
    @FolderDataType
    public void setOutputFolder(Long val) {
        this.outputFolder = val;
    }

    @Input(required = Required.ALWAYS)
    @Name("outputFilename")
    public void setOutputFilename(String val) {
        this.outputFilename = val;
    }

    @Input(required = Required.ALWAYS)
    @Name("passImages")
    public void setPassImages(PassImages passImages) {
        this.passImages = passImages;
    }


    // Outputs

    @Name("outputFile")
    @DocumentDataType
    public Long getOutputFile() {
        return outputFile;
    }

    @Name("errorTxt")
    public String getErrorTxt() {
        return errorTxt;
    }

    @Name("errorOccurred")
    public Boolean getErrorOccurred() {
        return errorOccurred;
    }

}
