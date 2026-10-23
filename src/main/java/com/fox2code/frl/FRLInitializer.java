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

import com.fox2code.frl.compat.btwaila.BTWailaCompat;
import com.fox2code.frl.entity.FRLEntitySubDispatcher;
import com.fox2code.frl.entity.FRLFakePlayerNPC;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.util.collection.NamespaceID;

import java.util.Objects;

public final class FRLInitializer implements ModInitializer {
	public static final FRLEntitySubDispatcher.EntitySubDispatcherEntry<FRLFakePlayerNPC> FAKE_PLAYER_NPC =
		FRLEntitySubDispatcher.registerSubDispatcher(FRLFakePlayerNPC.class,
			new NamespaceID("frl", "npc"), "minecraft:player", FRLFakePlayerNPC::new, false);

	public FRLInitializer() {}

	@Override
	public void onInitialize() {
		if (FRLCore.USE_HALPLIBE) {
			FRLCoreHalpLibe.initWithHlp();
		}
		Objects.requireNonNull(FAKE_PLAYER_NPC);
		BTWailaCompat.usePlayerTooltipFor(FRLFakePlayerNPC.class);
		CommandManager.registerCommand(new FRLCommand());
		FRLCore.LOGGER.info("FoxRootLib initialized successfully!");
	}
}
