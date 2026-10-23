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
package com.fox2code.frl.mixin.render.dragonfly;

import com.fox2code.frl.ext.render.dragonfly.ḞRLBlockModelMojangDataBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.render.texturepack.TexturePackList;
import net.minecraft.core.util.collection.NamespaceID;
import net.minecraft.core.util.helper.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.useless.dragonfly.DisplayPos;
import org.useless.dragonfly.data.block.mojang.BlockModelMojangData;
import org.useless.dragonfly.data.block.mojang.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(BlockModelMojangData.Builder.class)
public class MixinBlockModelMojangDataBuilder implements ḞRLBlockModelMojangDataBuilder {
	@Shadow
	@Nullable protected List<Element.@NotNull Builder> elements;

	@Shadow
	@Nullable protected String parent;

	@Shadow
	@Nullable protected Boolean ambientOcclusion;

	@Shadow
	@Nullable protected Integer renderLayer;

	@Shadow
	@Nullable protected Map<@NotNull String, @NotNull DisplayPos> displayPosMap;

	@Shadow
	@Nullable protected Map<@NotNull String, @NotNull String> textures;

	@Shadow
	@Nullable protected Object2IntMap<@NotNull Direction> particleIndices;

	@Override
	public BlockModelMojangData frl$buildWithNamespaceID(@NotNull TexturePackList texturePackList, @NotNull NamespaceID id) {
		List<Element> elementList = null;
		if (this.elements != null) {
			elementList = new ArrayList<>();

			for(Element.Builder b : this.elements) {
				elementList.add(b.build());
			}
		}

		return new BlockModelMojangData(texturePackList, id, this.parent, this.ambientOcclusion,
			this.renderLayer, this.displayPosMap, this.textures, this.particleIndices, elementList);
	}
}
