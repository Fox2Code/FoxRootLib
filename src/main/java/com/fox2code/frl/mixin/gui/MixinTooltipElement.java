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
package com.fox2code.frl.mixin.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.TooltipElement;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TooltipElement.class)
public abstract class MixinTooltipElement {
	@Shadow
	Minecraft mc;

	@Shadow
	@Final
	private static int SCREEN_MARGIN;

	@Shadow
	public abstract void render(CharSequence chars, int mouseX, int mouseY, int offsetX, int offsetY, int maxWidth, int maxHeight, boolean canOffset);

	/**
	 * @author Fox2Code
	 * @reason Fix bug
	 */
	@Overwrite
	public void render(CharSequence string, int mouseX, int mouseY, int offsetX, int offsetY) {
		this.render(string, mouseX, mouseY, offsetX, offsetY,
				this.mc.resolution.getScaledWidthScreenCoords() - SCREEN_MARGIN, -1, true);
	}
}
