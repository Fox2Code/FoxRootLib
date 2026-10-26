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
package com.fox2code.frl.client.gui;

import net.minecraft.client.gui.options.ScreenOptions;
import net.minecraft.client.gui.options.components.OptionsComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FRLOptionsComponentSpacer implements OptionsComponent {
	private final int height;

	public FRLOptionsComponentSpacer(int height) {
		this.height = height;
	}

	@Override
	public @Nullable String getTooltipTranslationKey() {
		return null;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public void render(@NotNull ScreenOptions screenOptions, int i, int i1, int i2, int i3, int i4) {}

	@Override
	public void onMouseClick(int i, int i1, int i2, int i3, int i4, int i5) {}

	@Override
	public void onMouseMove(int i, int i1, int i2, int i3, int i4) {}

	@Override
	public void onMouseRelease(int i, int i1, int i2, int i3, int i4, int i5) {}

	@Override
	public void onKeyPress(int i, char c) {}

	@Override
	public boolean matchesSearchTerm(String s) {
		return false;
	}
}
