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
package com.fox2code.frl.render;

import com.fox2code.foxevents.EventHolder;
import com.fox2code.frl.event.render.RegisterBlockModelEvent;
import com.fox2code.frl.event.render.RegisterItemModelEvent;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.client.render.block.model.BlockModel;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.ItemModelDispatcher;
import net.minecraft.core.block.*;
import net.minecraft.core.item.Item;

import java.util.Objects;

public final class FRLModelDispatcher {
	private static final EventHolder<RegisterBlockModelEvent> EVENT_BLOCK_MODEL =
		EventHolder.getHolderFromEvent(RegisterBlockModelEvent.class);
	private static final EventHolder<RegisterItemModelEvent> EVENT_ITEM_MODEL =
		EventHolder.getHolderFromEvent(RegisterItemModelEvent.class);
	public static final FRLBlockModelProvider<BlockLogic> DEFAULT_BLOCK_MODEL = FRLModelLoader::provideBlockModel;
	public static final FRLItemModelProvider<Item> DEFAULT_ITEM_MODEL = FRLModelLoader::provideItemModel;
	public static final FRLItemModelProvider<Item> DEFAULT_ITEM_BLOCK_MODEL = FRLModelLoader::provideItemBlockModel;

	private static final Int2ReferenceOpenHashMap<FRLBlockModelProvider<?>>
		blockModelDispatchers = new Int2ReferenceOpenHashMap<>();
	private static final Int2ReferenceOpenHashMap<FRLItemModelProvider<?>>
		itemModelDispatchers = new Int2ReferenceOpenHashMap<>();

	private FRLModelDispatcher() {}

	public static <T extends BlockLogic> void setBlockModel(
		Block<T> block, FRLBlockModelProvider<T> blockModelProvider) {
		Objects.requireNonNull(block);
		blockModelDispatchers.put(block.id(), blockModelProvider);
	}

	public static <T extends Item> void setItemModel(
		T item, FRLItemModelProvider<T> itemModelProvider) {
		Objects.requireNonNull(item);
		itemModelDispatchers.put(item.id, itemModelProvider);
	}

	public static void ensureModelsForBlock(Block<?> block) {
		Objects.requireNonNull(block);
		blockModelDispatchers.putIfAbsent(block.id(), DEFAULT_BLOCK_MODEL);
		itemModelDispatchers.putIfAbsent(block.id(), DEFAULT_ITEM_BLOCK_MODEL);
	}

	public static void ensureModelsForItem(Item item) {
		itemModelDispatchers.putIfAbsent(item.id, DEFAULT_ITEM_MODEL);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void registerBlockModels(BlockModelDispatcher blockModelDispatcher) {
		if (!EVENT_BLOCK_MODEL.isEmpty()) {
			EVENT_BLOCK_MODEL.callEvent(new RegisterBlockModelEvent(blockModelDispatcher));
		}
		blockModelDispatchers.int2ReferenceEntrySet().fastForEach(e -> {
			Block block = Blocks.blocksList[e.getIntKey()];
			if (block == null || blockModelDispatcher.hasDispatch(block)) return;
			BlockModel<?> blockModel = e.getValue().provide(block);
			if (blockModel == null) return;
			blockModelDispatcher.addDispatch(block, blockModel);
		});
	}

	@SuppressWarnings("unchecked")
	public static void registerItemModels(ItemModelDispatcher itemModelDispatcher) {
		if (!EVENT_ITEM_MODEL.isEmpty()) {
			EVENT_ITEM_MODEL.callEvent(new RegisterItemModelEvent(itemModelDispatcher));
		}
		itemModelDispatchers.int2ReferenceEntrySet().fastForEach(e -> {
			Item item = Item.itemsList[e.getIntKey()];
			if (item == null || itemModelDispatcher.hasDispatch(item)) return;
			ItemModel itemModel = ((FRLItemModelProvider<Item>) e.getValue()).provide(item);
			if (itemModel == null) return;
			itemModelDispatcher.addDispatch(item, itemModel);
		});
	}
}
