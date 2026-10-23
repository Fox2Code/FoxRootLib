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
package com.fox2code.frl.compat.modmenu;

import com.fox2code.frl.uopt.UOptHelper;
import io.github.prospector.modmenu.api.ModMenuApi;
import io.github.prospector.modmenu.util.TriConsumer;
import net.minecraft.client.gui.Screen;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class UOptModMenu implements ModMenuApi {
	@Override
	public String getModId() {
		return UOptHelper.UOPT_MOD_ID;
	}

	@Override
	public Optional<Supplier<Screen>> getConfigScreen(Screen screen) {
		return Optional.empty();
	}

	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return ModMenuApi.super.getConfigScreenFactory();
	}

	@Override
	public void attachCustomBadges(TriConsumer<String, Integer, Integer> consumer) {
		consumer.accept("Magenta", 0xff7f007f, 0xffff00ff);
	}
}
