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
package com.fox2code.frl.client;

import com.fox2code.foxevents.EventHandler;
import com.fox2code.frl.FRLCore;
import com.fox2code.frl.FRLRegistry;
import com.fox2code.frl.event.FRLEvents;
import com.fox2code.frl.event.lifecycle.GameStartedEvent;
import com.fox2code.frl.render.FRLModelDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.options.data.OptionsPages;

public final class FRLClientInitializer implements ClientModInitializer {
	public FRLClientInitializer() {}

	@Override
	public void onInitializeClient() {
		if (FRLCore.USE_HALPLIBE) {
			FRLClientHalpLibe.initClient();
		}
		FRLModelDispatcher.ensureModelsForBlock(FRLRegistry.MISSING_BLOCK);
		FRLModelDispatcher.ensureModelsForItem(FRLRegistry.MISSING_ITEM);
		FRLEvents.INSTANCE.registerEvents(this);
	}

	@EventHandler
	public void onGameStarted(GameStartedEvent event) {
		OptionsPages.register(new FRLOptionsPage());
	}
}
