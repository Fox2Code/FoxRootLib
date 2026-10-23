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

import com.fox2code.frl.event.lifecycle.GameStartingEvent;
import com.fox2code.frl.transform.FRLMixinPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.data.registry.Registries;
import org.slf4j.Logger;
import turniplabs.halplibe.HalpLibe;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Objects;

public final class FRLCore {
	public static EnvType envType = FabricLoader.getInstance().getEnvironmentType();
	public static boolean isClient = envType == EnvType.CLIENT;
	public static boolean isServer = envType == EnvType.SERVER;
	public static final boolean USE_HALPLIBE = FRLMixinPlugin.USE_HALPLIBE;
	public static final String MOD_ID = registerMod("frl");
	public static final String MOD_VERSION =
		FRLMixinPlugin.FRL_MOD_CONTAINER.getMetadata().getVersion().getFriendlyString();
	public static final Logger LOGGER = FRLMixinPlugin.LOGGER;
	public static final File frlDirectory = new File(
		FabricLoader.getInstance().getConfigDir().toFile(), "frl");
	private static Thread mainGameThread = null;

	private FRLCore() {}

	static {
		if (!frlDirectory.isDirectory() && !frlDirectory.mkdirs()) {
			throw new IOError(new IOException("Failed to create frl directory!"));
		}
	}

	public static String registerMod(String modId) {
		Objects.requireNonNull(modId, "The modId cannot be null!");
		if (USE_HALPLIBE) {
			return HalpLibe.registerMod(modId);
		}
		Registries.NAMESPACES.register(modId, modId);
		return modId;
	}

	public static boolean isOnMainGameThread() {
		return mainGameThread == Thread.currentThread();
	}

	static void init() {
		if (mainGameThread == null) {
			mainGameThread = Thread.currentThread();
			GameStartingEvent.INSTANCE.callEvent();
		}
	}

	public static void onGameLoadingNoHelpLibe() {
		if (USE_HALPLIBE) return;
		FRLRegistry.notifyGameStarting();
	}

	public static void onGameLoadedNoHelpLibe() {
		if (USE_HALPLIBE) return;
		FRLRegistry.notifyGameInitialized();
	}
}
