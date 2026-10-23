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
package com.fox2code.frl.mixin.uopt.world;

import com.fox2code.frl.ext.world.ḞRLWorld;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.world.MutableWorldSource;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(World.class)
public abstract class MixinWorld implements MutableWorldSource, ḞRLWorld {
	/**
	 * @author Fox2Code
	 * @reason No one will be hooking here, right?
	 */
	@Override
	@Overwrite
	public @NotNull Block<?> getBlockType(@NotNull TilePosc tilePosc) {
		return this.frl$fastGetBlockType(tilePosc.x(), tilePosc.y(), tilePosc.z());
	}

	/**
	 * @author Fox2Code
	 * @reason No one will be hooking here, right?
	 */
	@Override
	@Overwrite
	public @Nullable TileEntity getTileEntity(@NotNull TilePosc tilePosc) {
		return this.frl$fastGetTileEntity(tilePosc.x(), tilePosc.y(), tilePosc.z());
	}

	@Override
	@SuppressWarnings("deprecation")
	public @NotNull Block<?> getBlock(int x, int y, int z) {
		return this.frl$fastGetBlockType(x, y, z);
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getBlockId(int x, int y, int z) {
		return this.frl$fastGetBlockId(x, y, z);
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getBlockMetadata(int x, int y, int z) {
		return this.frl$fastGetBlockMetadata(x, y, z);
	}
}
