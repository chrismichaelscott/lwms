package com.underiteration.lwms.config;

import com.underiteration.lwms.config.impl.SystemPropertiesConfigManager;

import java.util.Optional;

public interface ConfigManager {

	static ConfigManager instance() {
		return new SystemPropertiesConfigManager();
	}

	Optional<Object> get(String key);
	Optional<String> getString(String key);
	Optional<Integer> getInteger(String key) throws NumberFormatException;

}
