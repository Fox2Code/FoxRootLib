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
import net.minecraft.client.world.chunk.provider.ChunkProviderDynamic;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.chunk.ChunkLoader;
import net.minecraft.core.world.generate.chunk.ChunkGenerator;
import net.minecraft.core.world.pos.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(ChunkProviderDynamic.class)
public abstract class MixinChunkProviderDynamic implements FRLStatefulChunkProvider {
	@Shadow
	@Final
	private Chunk emptyChunk;
	@Shadow
	@Nullable public ChunkGenerator chunkGenerator;
	@Shadow
	@Final
	private @Nullable ChunkLoader chunkLoader;
	@Shadow
	@Final
	private @NotNull Map<@NotNull ChunkPos, @NotNull Chunk> chunkMap;
	@Shadow
	@Final
	private @NotNull World world;

	@Override
	public @Nullable Chunk frl$getChunkIfLoaded(@NotNull ChunkPos chunkPos) {
		Chunk chunk = this.chunkMap.get(chunkPos);
		return chunk == this.emptyChunk ? null : chunk;
	}

	@Override
	public @Nullable Chunk frl$getChunkIfExists(@NotNull ChunkPos chunkPos) {
		Chunk chunk = this.chunkMap.get(chunkPos);
		return chunk != null || this.chunkLoader == null ||
			!this.chunkLoader.chunkExists(this.world, chunkPos.x, chunkPos.z) ?
			chunk : this.prepareChunk(chunkPos, true);
	}

	@Override
	public @Nullable ChunkGenerator frl$getChunkGenerator() {
		return this.chunkGenerator;
	}
}
