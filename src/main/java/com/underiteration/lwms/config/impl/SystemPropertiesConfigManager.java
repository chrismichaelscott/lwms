package com.underiteration.lwms.config.impl;

import com.underiteration.lwms.config.ConfigManager;

import java.util.Optional;

public class SystemPropertiesConfigManager implements ConfigManager {

	@Override
	public Optional<Object> get(String key) {

		return Optional.ofNullable(System.getProperty(key));
	}

	@Override
	public Optional<String> getString(String key) {

		return get(key).map(Object::toString);
	}

	@Override
	public Optional<Integer> getInteger(String key) throws NumberFormatException {

		return getString(key).map(Integer::valueOf);
	}
}
