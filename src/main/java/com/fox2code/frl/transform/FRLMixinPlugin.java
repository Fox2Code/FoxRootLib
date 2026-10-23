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
package com.fox2code.frl.transform;

import com.llamalad7.mixinextras.utils.MixinInternals;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class FRLMixinPlugin implements IMixinConfigPlugin {
	public static final ModContainer FRL_MOD_CONTAINER =
		FabricLoader.getInstance().getModContainer("frl").orElseThrow(AssertionError::new);
	public static final Logger LOGGER = LoggerFactory.getLogger("FoxRootLib");
	public static final boolean UNSAFE_OPTIMIZATIONS_ENABLED =
		FabricLoader.getInstance().isModLoaded("frl-uopt");
	public static final boolean USE_HALPLIBE =
		FabricLoader.getInstance().isModLoaded("halplibe");
	public static final boolean ENABLE_DEVELOPMENT_MIXINS =
		Boolean.getBoolean("frl.enable.dev.mixins") ||
			FabricLoader.getInstance().isDevelopmentEnvironment();

	static {
		if (UNSAFE_OPTIMIZATIONS_ENABLED) {
			FabricLoader.getInstance().getModContainer("frl-uopt").ifPresent(modContainer -> {
				if (modContainer.getMetadata() instanceof LoaderModMetadata loaderModMetadata) {
					loaderModMetadata.setVersion(FRL_MOD_CONTAINER.getMetadata().getVersion());
				}
			});
		}
		if (ENABLE_DEVELOPMENT_MIXINS) {
			LOGGER.info("FoxRootLib development mixins enabled!");
		}
	}

	@Override
	public void onLoad(String mixinPackage) {
		MixinInternals.registerExtension(new FRLMixinExtension());
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.startsWith("com.fox2code.frl.mixin.compat.")) {
			return FabricLoader.getInstance().isModLoaded(
				mixinClassName.substring(30).replace('.', '-'));
		}
		if (mixinClassName.startsWith("com.fox2code.frl.mixin.nohalplibe.")) {
			return !USE_HALPLIBE;
		}
		if (mixinClassName.startsWith("com.fox2code.frl.mixin.dev.")) {
			if (mixinClassName.startsWith("com.fox2code.frl.mixin.dev.compat.")) {
				return ENABLE_DEVELOPMENT_MIXINS &&
					FabricLoader.getInstance().isModLoaded(
						mixinClassName.substring(34).replace('.', '-'));
			}
			return ENABLE_DEVELOPMENT_MIXINS;
		}
		if (mixinClassName.startsWith("com.fox2code.frl.mixin.uopt.")) {
			if (mixinClassName.startsWith("com.fox2code.frl.mixin.uopt.compat.")) {
				return UNSAFE_OPTIMIZATIONS_ENABLED &&
					FabricLoader.getInstance().isModLoaded(
						mixinClassName.substring(35).replace('.', '-'));
			}
			return UNSAFE_OPTIMIZATIONS_ENABLED;
		}
		if (mixinClassName.startsWith("com.fox2code.frl.mixin.nuopt.")) {
			if (mixinClassName.startsWith("com.fox2code.frl.mixin.nuopt.compat.")) {
				return !UNSAFE_OPTIMIZATIONS_ENABLED &&
					FabricLoader.getInstance().isModLoaded(
						mixinClassName.substring(36).replace('.', '-'));
			}
			return !UNSAFE_OPTIMIZATIONS_ENABLED;
		}
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}
}
