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
package com.fox2code.frl.mixin.world;

import com.fox2code.frl.FRLCore;
import com.fox2code.frl.block.FRLTileProxyBlockLogic;
import com.fox2code.frl.event.world.WorldTickEvent;
import com.fox2code.frl.ext.world.ḞRLWorld;
import com.fox2code.frl.transform.api.FRLPatchPlayerInstanceof;
import com.fox2code.frl.world.FRLObjectPoolWorld;
import com.fox2code.frl.world.chunk.FRLStatefulChunkProvider;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.world.Dimension;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.chunk.provider.ChunkProvider;
import net.minecraft.core.world.pathfinder.Path;
import net.minecraft.core.world.pos.ChunkPosc;
import net.minecraft.core.world.pos.TilePosc;
import net.minecraft.core.world.save.DimensionData;
import net.minecraft.core.world.save.LevelData;
import net.minecraft.core.world.save.LevelStorage;
import net.minecraft.core.world.settings.WorldConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
@FRLPatchPlayerInstanceof
public abstract class MixinWorld implements ḞRLWorld {
	@Shadow
	@Final
	private @NotNull ChunkProvider chunkProvider;
	@Unique private FRLObjectPoolWorld frl$pool;
	@Unique private FRLStatefulChunkProvider frl$statefulChunkProvider;
	@Unique private WorldTickEvent frl$worldTickEvent;
	@Unique private boolean frl$networkMode;

	@Shadow
	public abstract int getHeightBlocks();

	@Shadow
	public abstract @NotNull Chunk getChunk(@NotNull ChunkPosc chunkPos);

	@Inject(
		method = "<init>(Lnet/minecraft/core/world/Dimension;Lnet/minecraft/core/world/save/LevelStorage;Lnet/minecraft/core/world/settings/WorldConfiguration;Lnet/minecraft/core/world/save/LevelData;Lnet/minecraft/core/world/save/DimensionData;)V",
		at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt()I")
	)
	public void onEarlyInit(
		Dimension dimension, LevelStorage levelStorage, WorldConfiguration worldConfiguration,
		LevelData levelData, DimensionData dimensionData, CallbackInfo ci) {
		if (this.frl$pool == null) {
			this.frl$pool = new FRLObjectPoolWorld(((World) (Object) this));
		}
		this.frl$worldTickEvent = new WorldTickEvent(((World) (Object) this));
	}

	@Inject(
		method = "<init>(Lnet/minecraft/core/world/Dimension;Lnet/minecraft/core/world/save/LevelStorage;Lnet/minecraft/core/world/settings/WorldConfiguration;Lnet/minecraft/core/world/save/LevelData;Lnet/minecraft/core/world/save/DimensionData;)V",
		at = @At(value = "RETURN")
	)
	public void onFullyInit(
		Dimension dimension, LevelStorage levelStorage, WorldConfiguration worldConfiguration,
		LevelData levelData, DimensionData dimensionData, CallbackInfo ci) {
		if (this.chunkProvider instanceof FRLStatefulChunkProvider statefulChunkProvider) {
			this.frl$statefulChunkProvider = statefulChunkProvider;
		}
	}

	@Inject(method = "tick", at = @At("HEAD"))
	public void onTickStart(CallbackInfo ci) {
		this.frl$worldTickEvent.callEvent();
	}

	@Unique private @Nullable Chunk frl$getCurrentChunkIfLoaded() {
		if (this.frl$pool.lastChunk != null) {
			return this.frl$pool.lastChunk;
		}
		if (this.frl$statefulChunkProvider != null) {
			return this.frl$pool.lastChunk =
				this.frl$statefulChunkProvider
					.frl$getChunkIfLoaded(this.frl$pool.chunkPos);
		}
		if (!this.chunkProvider.isChunkLoaded(this.frl$pool.chunkPos)) {
			return null;
		}
		return this.frl$pool.lastChunk =
			this.getChunk(this.frl$pool.chunkPos);
	}

	@Unique private @Nullable Chunk frl$getCurrentChunkIfExists() {
		if (this.frl$pool.lastChunk != null) {
			return this.frl$pool.lastChunk;
		}
		return this.frl$pool.lastChunk =
			this.frl$statefulChunkProvider == null ?
			this.getChunk(this.frl$pool.chunkPos) :
			this.frl$statefulChunkProvider
				.frl$getChunkIfExists(this.frl$pool.chunkPos);
	}

	@Unique private @Nullable Chunk frl$getCurrentChunk() {
		if (this.frl$pool.lastChunk != null) {
			return this.frl$pool.lastChunk;
		}
		if (this.frl$networkMode && this.frl$statefulChunkProvider != null) {
			return this.frl$pool.lastChunk =
				this.frl$statefulChunkProvider.frl$getChunkIfExists(this.frl$pool.chunkPos);
		}
		return this.frl$pool.lastChunk =
			this.getChunk(this.frl$pool.chunkPos);
	}

	@Override
	public WorldSource frl$fastWorldSource() {
		return this.frl$pool.fastWorldWrapper;
	}

	@Override
	public FRLObjectPoolWorld frl$getObjectPool() {
		return this.frl$pool;
	}

	@Override
	public boolean frl$isOnWorldThread() {
		return FRLCore.isOnMainGameThread();
	}

	@Override
	public void frl$networkMode(boolean networkMode) {
		this.frl$networkMode = networkMode;
	}

	@Override
	public boolean frl$networkMode() {
		return this.frl$networkMode;
	}

	@Override
	public boolean frl$isInBounds(int x, int y, int z) {
		return x >= -32000000 && y >= 0 && z >= -32000000 &&
			x <= 32000000 && y < this.getHeightBlocks() && z <= 32000000;
	}

	@Override
	public int frl$fastGetBlockId(int x, int y, int z) {
		if (!this.frl$isInBounds(x, y, z)) {
			return 0;
		}
		this.frl$pool.setChunkTilePos(x, y, z);
		Chunk chunk = this.frl$getCurrentChunk();
		return chunk == null ? 0 : chunk.getBlockId(this.frl$pool.chunkTilePos);
	}

	@Override
	public int frl$fastGetBlockMetadata(int x, int y, int z) {
		if (!this.frl$isInBounds(x, y, z)) {
			return 0;
		}
		this.frl$pool.setChunkTilePos(x, y, z);
		Chunk chunk = this.frl$getCurrentChunk();
		return chunk == null ? 0 : chunk.getBlockData(this.frl$pool.chunkTilePos);
	}

	@Override
	public Block<?> frl$fastGetBlockType(int x, int y, int z) {
		if (!this.frl$isInBounds(x, y, z)) {
			return Blocks.AIR;
		}
		this.frl$pool.setChunkTilePos(x, y, z);
		Chunk chunk = this.frl$getCurrentChunk();
		return chunk == null ? Blocks.AIR : chunk.getBlock(this.frl$pool.chunkTilePos);
	}

	@Override
	public TileEntity frl$fastGetTileEntity(int x, int y, int z) {
		if (!this.frl$isInBounds(x, y, z)) {
			return null;
		}
		this.frl$pool.setChunkTilePos(x, y, z);
		// Generation of tile entities when generating the world is very rare, assumes it never happens for speed!
		Chunk chunk = this.frl$getCurrentChunkIfExists();
		return chunk == null ? null : chunk.getTileEntity(this.frl$pool.chunkTilePos);
	}

	@Override
	public TileEntity frl$fastGetTileEntityIfLoaded(TilePosc tilePosc) {
		return tilePosc == null ? null : this.frl$fastGetTileEntityIfLoaded(tilePosc.x(), tilePosc.y(), tilePosc.z());
	}

	@Override
	public TileEntity frl$fastGetTileEntityIfLoaded(int x, int y, int z) {
		if (!this.frl$isInBounds(x, y, z)) {
			return null;
		}
		this.frl$pool.setChunkTilePos(x, y, z);
		Chunk chunk = this.frl$getCurrentChunkIfLoaded();
		return chunk == null ? null : chunk.getTileEntity(this.frl$pool.chunkTilePos);
	}

	@Override
	public TileEntity frl$fastGetProxiedTileEntity(int x, int y, int z) {
		if (!this.frl$isInBounds(x, y, z)) {
			return null;
		}
		this.frl$pool.setChunkTilePos(x, y, z);
		// Generation of tile entities when generating the world is very rare, assumes it never happens for speed!
		Chunk chunk = this.frl$getCurrentChunkIfExists();
		if (chunk == null) return null;
		Block<?> block = chunk.getBlock(this.frl$pool.chunkTilePos);
		if (block.getLogic() instanceof FRLTileProxyBlockLogic frlTileProxyBlockLogic) {
			TileEntity tileEntity = frlTileProxyBlockLogic.getProxiedTileEntity((World) (Object) this, x, y, z);
			if (tileEntity != null) {
				return tileEntity;
			}
			if (block.isEntityTile) {
				// Other world methods might have been called, just reset chunkTilePos
				this.frl$pool.chunkTilePos.x = x & 0xf;
				this.frl$pool.chunkTilePos.y = y;
				this.frl$pool.chunkTilePos.z = z & 0xf;
				return chunk.getTileEntity(this.frl$pool.chunkTilePos);
			}
		} else if (block.isEntityTile) {
			return chunk.getTileEntity(this.frl$pool.chunkTilePos);
		}
		return null;
	}

	@Override
	public Path frl$fastGetEntityPathToXYZ(@NotNull Entity entity, int x, int y, int z, float distance) {
		return this.frl$pool.pathFinder.findPath(entity, x, y, z, distance);
	}

	@Override
	public Path frl$fastGetEntityPathToEntity(@NotNull Entity entity, Entity to, float distance) {
		return this.frl$pool.pathFinder.findPath(entity, to, distance);
	}

	/**
	 * @author Fox2Code
	 * @reason No other hook will work after what I did to this method.
	 */
	@Overwrite
	public boolean isAirBlock(@NotNull TilePosc tilePos) {
		return this.frl$fastGetBlockType(tilePos.x(), tilePos.y(), tilePos.z())
			.frl$checkAnyFRLFlags(Block.FRL_FLAG_BLOCK_IS_AIR_VARIANT);
	}

	/**
	 * @author Fox2Code
	 * @reason No other hook will work after what I did to this method.
	 */
	@Overwrite
	@Deprecated
	public final boolean isAirBlock(int x, int y, int z) {
		return this.frl$fastGetBlockType(x, y, z)
			.frl$checkAnyFRLFlags(Block.FRL_FLAG_BLOCK_IS_AIR_VARIANT);
	}
}
