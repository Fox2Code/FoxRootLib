/*
 * MIT License
 *
 * Copyright (c) 2026 Fox2Code
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.fox2code.frl;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class FRLConfig {
	private static final String CONFIG_COMMENT = "FoxRootLib configuration file";
	private static final File frlConfig = new File(FRLCore.frlDirectory, "config.properties");
	private static final Properties defaultProperties;
	public static boolean allowAuth2 = true;
	public static boolean forceAuth2 = false;
	public static boolean modpackForceAuth2 = false;

	static {
		defaultProperties = new Properties();
		for (Field field : FRLConfig.class.getFields()) {
			if (field.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC)) {
				try {
					defaultProperties.put(field.getName(), String.valueOf(field.get(null)));
					// Get system properties for initial configuration.
					String val = System.getProperty("hypertale." + field.getName());
					if (val != null && !val.isEmpty()) {
						if (field.getType() == String.class) {
							field.set(null, val);
						} else if (field.getType() == boolean.class) {
							field.setBoolean(null, Boolean.parseBoolean(val));
						} else if (field.getType() == int.class) {
							try {
								field.setInt(null, Integer.parseInt(val));
							} catch (NumberFormatException ignored) {}
						}
					}
				} catch (IllegalAccessException ignored) {}
			}
		}
		fixupConfiguration();
	}

	public static void load() throws IOException {
		Properties properties;
		if (frlConfig.exists()) {
			properties = new Properties(defaultProperties);
			try (InputStreamReader inputStreamReader = new InputStreamReader(
					new BufferedInputStream(new FileInputStream(
							frlConfig)), StandardCharsets.UTF_8)) {
				properties.load(inputStreamReader);
			}

			for (Field field : FRLConfig.class.getFields()) {
				try {
					if (field.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC)) {
						String val = properties.getProperty(field.getName());
						if (field.getType() == String.class) {
							field.set(null, val);
						} else if (field.getType() == boolean.class) {
							field.setBoolean(null, Boolean.parseBoolean(val));
						} else if (field.getType() == int.class) {
							try {
								field.setInt(null, Integer.parseInt(val));
							} catch (NumberFormatException ignored) {}
						}
					}
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Failed to set field " + field.getName(), e);
				}
			}
		} else {
			if (!frlConfig.createNewFile()) {
				throw new IOException("Failed to create config file!");
			}
			properties = defaultProperties;
			try (OutputStreamWriter outputStreamWriter =
						 new OutputStreamWriter(new BufferedOutputStream(
								 new FileOutputStream(frlConfig)),
								 StandardCharsets.UTF_8)) {
				properties.store(outputStreamWriter, CONFIG_COMMENT);
			}
		}
		fixupConfiguration();
	}

	public static void save() throws IOException {
		if (!frlConfig.exists() &&
				!frlConfig.createNewFile()) {
			throw new IOException("Failed to create config file!");
		}
		fixupConfiguration();
		Properties properties = new Properties();
		for (Field field : FRLConfig.class.getFields()) {
			if (field.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC)) {
				try {
					properties.put(field.getName(), String.valueOf(field.get(null)));
				} catch (IllegalAccessException ignored) {}
			}
		}
		try (OutputStreamWriter outputStreamWriter =
					 new OutputStreamWriter(new BufferedOutputStream(
							 new FileOutputStream(frlConfig)),
							 StandardCharsets.UTF_8)) {
			properties.store(outputStreamWriter, CONFIG_COMMENT);
		}
	}

	private static void fixupConfiguration() {
		if (modpackForceAuth2) forceAuth2 = true;
	}
}
