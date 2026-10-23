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

import com.fox2code.frl.FRLCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.block.model.BlockModel;
import net.minecraft.client.render.block.model.BlockModelEmpty;
import net.minecraft.client.render.block.model.BlockModelStandard;
import net.minecraft.client.render.block.model.generic.*;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.ItemModelBlock;
import net.minecraft.client.render.item.model.ItemModelStandard;
import net.minecraft.client.render.texture.stitcher.IconCoordinate;
import net.minecraft.client.render.texture.stitcher.TextureRegistry;
import net.minecraft.client.render.texturepack.TexturePackList;
import net.minecraft.core.block.*;
import net.minecraft.core.item.Item;
import net.minecraft.core.item.block.ItemBlock;
import net.minecraft.core.util.collection.NamespaceID;
import org.jetbrains.annotations.NotNull;
import org.useless.dragonfly.data.block.mojang.BlockModelMojangData;

final class FRLModelLoader extends BlockModelMojangData.Cache {
	private FRLModelLoader() {}

	static BlockModelMojangData loadModelDataExt(NamespaceID namespaceID) {
		return loadModelDataExt(namespaceID, null);
	}

	static BlockModelMojangData loadModelDataExt(NamespaceID id, String ext) {
		String s = id.toString();
		String value = id.value();
		if (ext != null) {
			s += ext;
			value += ext;
		}
		TexturePackList texturePackList = Minecraft.getMinecraft().texturePackList;
		if (modelDataCache.containsKey(s)) {
			return modelDataCache.get(s);
		} else {
			String modelPath = String.format("/assets/%s/models/%s.json", id.namespace(), value);

			BlockModelMojangData.Builder data = loadFromStream(
				texturePackList.getResourceAsStream(modelPath));
			if (data == null) {
				FRLCore.LOGGER.warn("Could not locate block model data '{}'!", id);
				return null;
			} else {
				BlockModelMojangData model = data.frl$buildWithNamespaceID(texturePackList,
					ext == null ? id : new NamespaceID(id.namespace(), value));
				modelDataCache.put(model.modelId().toString(), model);
				return model;
			}
		}
	}

	private static IconCoordinate getBlockTexture(NamespaceID namespaceID) {
		return namespaceID.value().startsWith("block/") ? TextureRegistry.getTexture(namespaceID) :
			TextureRegistry.getTexture(namespaceID.namespace() + ":block/" + namespaceID.value());
	}

	static <T extends BlockLogic> BlockModel<T> provideBlockModel(@NotNull Block<T> block) {
		if (block.frl$checkAnyFRLFlags(Block.FRL_FLAG_BLOCK_IS_AIR_VARIANT)) {
			return new BlockModelEmpty<>(block);
		}
		BlockModelMojangData model = null;
		if (block.getLogic() instanceof BlockLogicSlab blockLogicSlab) {
			BlockModelMojangData lower = loadModelDataExt(blockLogicSlab.namespaceId(), "/lower");
			BlockModelMojangData upper = loadModelDataExt(blockLogicSlab.namespaceId(), "/upper");
			if (lower != null && upper != null) {
				BlockModelMojangData full = loadModelDataExt(blockLogicSlab.namespaceId(), "/full");
				if (full == null)
					full = loadModelDataExt(blockLogicSlab.modelBlock.namespaceId());
				if (full != null)
					return new BlockModelGenericSlab<>(block, lower, upper, full);
			}
		} else {
			model = loadModelDataExt(block.namespaceId());
		}
		if (model != null) {
			if (block.getLogic() instanceof BlockLogicAxisAligned) {
				return new BlockModelGenericAxis<>(block, model);
			} else if (block.getLogic() instanceof BlockLogicRotatable) {
				return new BlockModelGenericRotatable<>(block, model);
			} else if (block.getLogic() instanceof BlockLogicVeryRotatable) {
				return new BlockModelGenericVeryRotatable<>(block, model);
			} else if (block.getLogic() instanceof BlockLogicFullyRotatable) {
				return new BlockModelGenericFullyRotatable<>(block, model);
			} else if (block.getLogic() instanceof BlockLogicStairs) {
				return new BlockModelGenericStairs<>(block, model);
			}
		}
		if (model != null) {
			return new BlockModelGeneric<>(block, model);
		}
		return new BlockModelStandard<>(block).setAllTextures(
			getBlockTexture(block.namespaceId()));
	}

	static ItemModel provideItemBlockModel(Item item) {
		if (item instanceof ItemBlock<?> itemBlock &&
			!itemBlock.getBlock().frl$checkAnyFRLFlags(
				Block.FRL_FLAG_BLOCK_IS_AIR_VARIANT)) {
			return new ItemModelBlock(itemBlock);
		}
		return provideItemModel(item);
	}

	static ItemModel provideItemModel(Item item) {
		return new ItemModelStandard(item);
	}
}
