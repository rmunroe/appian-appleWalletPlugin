package com.appiancorp.plugins;

/**
 * Test-only stand-in. The Appian SDK's Document constructor reports through this class, which isn't in the SDK jar.
 */
public final class PluginUsageLogger {
    private static final PluginUsageLogger INSTANCE = new PluginUsageLogger();

    public static PluginUsageLogger getInstance() {
        return INSTANCE;
    }

    public void logDeprecatedMethodCall(String method) {
    }
}
