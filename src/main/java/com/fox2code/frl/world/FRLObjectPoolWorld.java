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
package com.fox2code.frl.world;

import com.fox2code.frl.compat.catalyst.core.CatalystCoreShim;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.pathfinder.PathFinder;
import net.minecraft.core.world.pos.ChunkPos;
import net.minecraft.core.world.pos.ChunkTilePos;
import net.minecraft.core.world.pos.TilePos;

public final class FRLObjectPoolWorld {
	public final TilePos tilePos;
	public final ChunkPos chunkPos;
	public final ChunkTilePos chunkTilePos;
	public final Object catalystVec3i;
	public final FRLFastWorldWrapper fastWorldWrapper;
	public final PathFinder pathFinder;
	public Chunk lastChunk;

	public FRLObjectPoolWorld(World world) {
		if (world == null || world.frl$getObjectPool() != null) {
			throw new AssertionError("This object can only be created by the mixin");
		}
		this.tilePos = new TilePos();
		this.chunkPos = new ChunkPos();
		this.chunkTilePos = new ChunkTilePos();
		this.catalystVec3i = CatalystCoreShim.CORE.makeVec3i();
		this.fastWorldWrapper = new FRLFastWorldWrapper(world);
		this.pathFinder = new PathFinder(this.fastWorldWrapper);
	}

	public void setChunkPos(int cx, int cz) {
		if (this.chunkPos.x != cx || this.chunkPos.z != cz) {
			this.chunkPos.x = cx;
			this.chunkPos.z = cz;
			this.lastChunk = null;
		}
	}

	public void setChunkTilePos(int x, int y, int z) {
		this.chunkTilePos.x = x & 0xf;
		this.chunkTilePos.y = y;
		this.chunkTilePos.z = z & 0xf;
		this.setChunkPos(x >> 4, z >> 4);
	}

	public void setTilePos(int x, int y, int z) {
		this.tilePos.x = x;
		this.tilePos.y = y;
		this.tilePos.z = z;
		this.setChunkTilePos(x, y, z);
	}
}
