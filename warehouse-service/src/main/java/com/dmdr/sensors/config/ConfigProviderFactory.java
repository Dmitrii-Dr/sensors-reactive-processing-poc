package com.dmdr.sensors.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigProviderFactory {

    private static volatile ConfigProvider instance;

    public static ConfigProvider getInstance() {
        ConfigProvider result = instance;
        if (result == null) {
            synchronized (ConfigProviderFactory.class) {
                result = instance;
                if (result == null) {
                    result = new EnvConfigProvider();
                    instance = result;
                }
            }
        }
        return result;
    }

   /*
   //Replaces the shared provider e.g. for tests or a different config source.
    public static synchronized void init(ConfigProvider provider) {
        instance = Objects.requireNonNull(provider, "provider");
    }*/
}
