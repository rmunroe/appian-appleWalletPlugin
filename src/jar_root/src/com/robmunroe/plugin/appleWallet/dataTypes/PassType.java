package com.robmunroe.plugin.appleWallet.dataTypes;

import de.brendamour.jpasskit.PKPassBuilder;

public interface PassType {
    PKPassBuilder getPassBuilder();
}
