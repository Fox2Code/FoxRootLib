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
package com.fox2code.frl.compat.btwaila;

import com.fox2code.frl.FRLCore;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.entity.player.Player;
import toufoumaster.btwaila.entryplugins.waila.BTWailaPlugin;

import java.util.ArrayList;

public final class BTWailaCompat {
	private static final boolean HAS_CLIENT_BTWAILA = FRLCore.isClient &&
		FabricLoader.getInstance().isModLoaded("btwaila");
	private static ArrayList<Class<? extends Player>> playerClasses =
		HAS_CLIENT_BTWAILA ? new ArrayList<>() : null;

	private BTWailaCompat() {}

	public static void usePlayerTooltipFor(Class<? extends Player> playerClass) {
		playerClass.asSubclass(Player.class);
		if (HAS_CLIENT_BTWAILA) {
			if (playerClasses != null) {
				playerClasses.add(playerClass);
			} else {
				BTWailaPlugin.PLAYER.addClass(playerClass);
			}
		}
	}

	public static void notifyGameStarted() {
		ArrayList<Class<? extends Player>> playerClasses = BTWailaCompat.playerClasses;
		if (HAS_CLIENT_BTWAILA && playerClasses != null) {
			BTWailaCompat.playerClasses = null;
			for (Class<? extends Player> playerClass : playerClasses) {
				BTWailaPlugin.PLAYER.addClass(playerClass);
			}
		}
	}
}
