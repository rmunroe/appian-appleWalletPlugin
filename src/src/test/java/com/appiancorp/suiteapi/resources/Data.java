package com.appiancorp.suiteapi.resources;

/**
 * Test-only stand-in. The published SDK jars strip private constructors, so the SDK's copy of this enum can't
 * initialize. That breaks Mockito, which reads the {@code @Read} annotations on service interfaces such as
 * ContentService. A real site has the complete class, and this one is never shipped. The constants must match the
 * SDK's, or Mockito fails with EnumConstantNotPresentException.
 */
public enum Data {
    K_UNSPECIFIED_ENGINE,
    K_UNSPECIFIED_ENGINE_FORCE_PRIMARY,
    K_CHANNELS,
    K_CHANNELS_FORCE_PRIMARY,
    K_FORUMS,
    K_FORUMS_FORCE_PRIMARY,
    K_NOTIFY,
    K_NOTIFY_FORCE_PRIMARY,
    K_NOTIFY_EMAIL,
    K_NOTIFY_EMAIL_FORCE_PRIMARY,
    K_COLLAB_STAT,
    K_COLLAB_STAT_FORCE_PRIMARY,
    K_COLLAB,
    K_COLLAB_FORCE_PRIMARY,
    K_CONTENT,
    K_CONTENT_FORCE_PRIMARY,
    K_PORTAL,
    K_PORTAL_FORCE_PRIMARY,
    K_PERSONALIZATION,
    K_PERSONALIZATION_FORCE_PRIMARY,
    K_PROCESS_DESIGN,
    K_PROCESS_DESIGN_FORCE_PRIMARY,
    K_PROCESS_ANALYTICS,
    K_PROCESS_ANALYTICS_FORCE_PRIMARY,
    K_PROCESS_EXECUTION,
    K_PROCESS_EXECUTION_FORCE_PRIMARY,
    DISK_FILE,
    OBJECT_STORE,
    NONE,
    UNKNOWN;

    public boolean isK() {
        return name().startsWith("K_");
    }

    public boolean isPrimaryForce() {
        return name().endsWith("_FORCE_PRIMARY");
    }
}
