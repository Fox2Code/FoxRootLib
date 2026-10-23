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
package com.fox2code.frl.event.render;

import com.fox2code.frl.event.FRLEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.core.item.Item;

/**
 * Called before {@link com.fox2code.frl.render.FRLModelDispatcher} register its models.
 */
@Environment(EnvType.CLIENT)
public final class RegisterItemModelEvent extends FRLEvent {
	private final ItemModelDispatcher itemModelDispatcher;

	public RegisterItemModelEvent(ItemModelDispatcher itemModelDispatcher) {
		this.itemModelDispatcher = itemModelDispatcher;
	}

	public boolean hasModel(Item item) {
		return this.itemModelDispatcher.hasDispatch(item);
	}

	public void registerModel(Item item, ItemModel itemModel) {
		this.itemModelDispatcher.addDispatch(item, itemModel);
	}
}
