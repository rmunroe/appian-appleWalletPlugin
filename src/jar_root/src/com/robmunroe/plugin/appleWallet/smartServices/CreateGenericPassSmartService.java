package com.robmunroe.plugin.appleWallet.smartServices;

import com.appiancorp.suiteapi.common.Name;
import com.appiancorp.suiteapi.content.ContentService;
import com.appiancorp.suiteapi.process.exceptions.SmartServiceException;
import com.appiancorp.suiteapi.process.framework.Input;
import com.appiancorp.suiteapi.process.framework.MessageContainer;
import com.appiancorp.suiteapi.process.framework.Required;
import com.appiancorp.suiteapi.process.palette.PaletteInfo;
import com.appiancorp.suiteapi.security.external.SecureCredentialsStore;
import com.robmunroe.plugin.appleWallet.dataTypes.GenericPass;
import com.robmunroe.plugin.appleWallet.helpers.AppleWalletPluginHelper;
//import org.apache.log4j.Logger;

@PaletteInfo(paletteCategory = AppleWalletPluginHelper.SMARTSERVICE_PALLETCATEGORY, palette = AppleWalletPluginHelper.SMARTSERVICE_PALLET)
public class CreateGenericPassSmartService extends CreatePassSmartService {
//    private static final Logger LOG = Logger.getLogger(CreateGenericPassSmartService.class);

    // GenericPass CDT
    private GenericPass genericPass;


    public CreateGenericPassSmartService(ContentService contentService, SecureCredentialsStore scs) {
        super(contentService, scs);
    }


    @Override
    public void run() throws SmartServiceException {
        super.buildPass(this.genericPass);
    }

    @Override
    public void onSave(MessageContainer messages) {
    }

    @Override
    public void validate(MessageContainer messages) {
    }


    // Inputs

    @Input(required = Required.ALWAYS)
    @Name("genericPass")
    public void setGenericPass(GenericPass genericPass) {
        this.genericPass = genericPass;
    }

}
