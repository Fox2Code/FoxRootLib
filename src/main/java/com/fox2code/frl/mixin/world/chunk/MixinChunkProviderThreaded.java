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
package com.fox2code.frl.mixin.world.chunk;

import com.fox2code.frl.world.chunk.FRLStatefulChunkProvider;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.chunk.ChunkLoader;
import net.minecraft.core.world.chunk.provider.ChunkProviderThreaded;
import net.minecraft.core.world.generate.chunk.ChunkGenerator;
import net.minecraft.core.world.pos.ChunkPos;
import net.minecraft.core.world.pos.ChunkPosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(ChunkProviderThreaded.class)
@SuppressWarnings("SynchronizeOnNonFinalField")
public abstract class MixinChunkProviderThreaded implements FRLStatefulChunkProvider {
	@Shadow
	@Final
	@NotNull protected ChunkLoader chunkLoader;
	@Shadow
	@Final
	@NotNull protected ChunkGenerator chunkGenerator;
	@Shadow
	@Final
	@NotNull protected Map<@NotNull ChunkPos, @NotNull Chunk> loadedChunks;
	@Shadow
	@NotNull protected Chunk lastQueriedChunk;
	@Shadow
	@NotNull protected ChunkPos lastQueriedChunkPos;
	@Shadow
	public abstract @NotNull Chunk provideChunk(@NotNull ChunkPosc chunkPos, boolean priority);

	@Shadow
	@Final
	@NotNull protected Map<@NotNull ChunkPos, @NotNull Chunk> preparedUndecoratedChunks;

	@Shadow
	@Final
	@NotNull protected World world;

	@Shadow
	protected abstract void addChunk(@NotNull Chunk chunk);

	@Override
	public @Nullable Chunk frl$getChunkIfLoaded(@NotNull ChunkPos chunkPos) {
		if (this.lastQueriedChunkPos.equals(chunkPos)) {
			return this.lastQueriedChunk;
		}
		Chunk chunk;
		synchronized (this.loadedChunks) {
			chunk = this.loadedChunks.get(chunkPos);
		}
		if (chunk == null) {
			synchronized (this.preparedUndecoratedChunks) {
				chunk = this.preparedUndecoratedChunks.get(chunkPos);
			}
		}
		return chunk;
	}

	@Override
	public @Nullable Chunk frl$getChunkIfExists(@NotNull ChunkPos chunkPos) {
		Chunk chunk = this.frl$getChunkIfLoaded(chunkPos);
		if (chunk != null) return chunk;
		if (!this.chunkLoader.chunkExists(this.world, chunkPos.x, chunkPos.z))
			return null;
		try {
			chunk = this.chunkLoader.loadChunk(this.world, chunkPos.x, chunkPos.z);
			if (chunk != null) this.addChunk(chunk);
		} catch (Exception ignored) {}
		return chunk;
	}

	@Override
	public @Nullable ChunkGenerator frl$getChunkGenerator() {
		return this.chunkGenerator;
	}
}
