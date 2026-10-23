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
package com.fox2code.frl.ext.world;

import com.fox2code.frl.world.FRLObjectPoolWorld;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.pathfinder.Path;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public interface ḞRLWorld {
	default WorldSource frl$fastWorldSource() { throw new AssertionError("Implemented in Mixin"); }

	default FRLObjectPoolWorld frl$getObjectPool() {
		throw new AssertionError("Implemented in Mixin");
	}

	default boolean frl$isOnWorldThread() { throw new AssertionError("Implemented in Mixin"); }

	default void frl$networkMode(boolean networkMode) {
		throw new AssertionError("Implemented in Mixin");
	}

	default boolean frl$networkMode() {
		throw new AssertionError("Implemented in Mixin");
	}

	default boolean frl$isInBounds(int x, int y, int z) {
		throw new AssertionError("Implemented in Mixin");
	}

	default int frl$fastGetBlockId(int x, int y, int z) {
		throw new AssertionError("Implemented in Mixin");
	}

	default int frl$fastGetBlockMetadata(int x, int y, int z) {
		throw new AssertionError("Implemented in Mixin");
	}

	default Block<?> frl$fastGetBlockType(int x, int y, int z) {
		throw new AssertionError("Implemented in Mixin");
	}

	default TileEntity frl$fastGetTileEntity(int x, int y, int z) {
		throw new AssertionError("Implemented in Mixin");
	}

	default TileEntity frl$fastGetTileEntityIfLoaded(TilePosc tilePosc) {
		throw new AssertionError("Implemented in Mixin");
	}

	default TileEntity frl$fastGetTileEntityIfLoaded(int x, int y, int z) {
		throw new AssertionError("Implemented in Mixin");
	}

	default TileEntity frl$fastGetProxiedTileEntity(int x, int y, int z) {
		throw new AssertionError("Implemented in Mixin");
	}

	default Path frl$fastGetEntityPathToXYZ(@NotNull Entity entity, int x, int y, int z, float distance) {
		throw new AssertionError("Implemented in Mixin");
	}

	default Path frl$fastGetEntityPathToEntity(@NotNull Entity entity, Entity to, float distance) {
		throw new AssertionError("Implemented in Mixin");
	}
}
