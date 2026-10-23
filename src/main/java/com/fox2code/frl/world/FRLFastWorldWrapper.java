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

import net.minecraft.core.block.Block;
import net.minecraft.core.block.BlockLogic;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.block.material.Material;
import net.minecraft.core.enums.LightLayer;
import net.minecraft.core.world.Dimension;
import net.minecraft.core.world.World;
import net.minecraft.core.world.WorldSource;
import net.minecraft.core.world.biome.Biome;
import net.minecraft.core.world.pos.TilePosc;
import net.minecraft.core.world.season.SeasonManager;
import net.minecraft.core.world.type.WorldType;
import net.minecraft.core.world.weather.WeatherManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FRLFastWorldWrapper implements WorldSource {
	public final World world;

	FRLFastWorldWrapper(World world) {
		this.world = world;
	}

	@Override
	public int getHeightBlocks() {
		return this.world.getHeightBlocks();
	}

	@Override
	public @NotNull Block<?> getBlockType(@NotNull TilePosc tilePosc) {
		return this.world.frl$fastGetBlockType(tilePosc.x(), tilePosc.y(), tilePosc.z());
	}

	@Override
	public @Nullable TileEntity getTileEntity(@NotNull TilePosc tilePosc) {
		return this.world.frl$fastGetTileEntity(tilePosc.x(), tilePosc.y(), tilePosc.z());
	}

	@Override
	public @Nullable <T> T getBlockLogic(@NotNull TilePosc tilePosc, @NotNull Class<T> logicClass) {
		Block<?> block = this.world.frl$fastGetBlockType(tilePosc.x(), tilePosc.y(), tilePosc.z());
		BlockLogic logic = block.getLogic();
		return (logicClass.isAssignableFrom(logic.getClass()) ? logicClass.cast(logic) : null);
	}

	@Override
	public float getBrightness(@NotNull TilePosc tilePosc, int lightEmission) {
		return this.world.getBrightness(tilePosc, lightEmission);
	}

	@Override
	public byte getLightIndex(@NotNull TilePosc tilePosc, int lightEmission) {
		return this.world.getLightIndex(tilePosc, lightEmission);
	}

	@Override
	public float getLightBrightness(@NotNull TilePosc tilePosc) {
		return this.world.getLightBrightness(tilePosc);
	}

	@Override
	public int getBlockData(@NotNull TilePosc tilePosc) {
		return this.world.frl$fastGetBlockMetadata(tilePosc.x(), tilePosc.y(), tilePosc.z());
	}

	@Override
	public @NotNull Material getBlockMaterial(@NotNull TilePosc tilePosc) {
		return this.world.getBlockMaterial(tilePosc);
	}

	@Override
	public boolean isBlockOpaqueCube(@NotNull TilePosc tilePosc) {
		return this.world.isBlockOpaqueCube(tilePosc);
	}

	@Override
	public boolean isBlockNormalCube(@NotNull TilePosc tilePosc) {
		return this.world.isBlockNormalCube(tilePosc);
	}

	@Override
	public double getBlockTemperature(@NotNull TilePosc tilePosc) {
		return this.world.getBlockTemperature(tilePosc);
	}

	@Override
	public double getBlockHumidity(@NotNull TilePosc tilePosc) {
		return this.world.getBlockHumidity(tilePosc);
	}

	@Override
	public double getBlockVariety(@NotNull TilePosc tilePosc) {
		return this.world.getBlockVariety(tilePosc);
	}

	@Override
	public @NotNull SeasonManager getSeasonManager() {
		return this.world.getSeasonManager();
	}

	@Override
	public @NotNull WeatherManager getWeatherManager() {
		return this.world.getWeatherManager();
	}

	@Override
	public @NotNull Biome getBlockBiome(@NotNull TilePosc tilePosc) {
		return this.world.getBlockBiome(tilePosc);
	}

	@Override
	public int getSavedLightValue(@NotNull LightLayer lightLayer, @NotNull TilePosc tilePosc) {
		return this.world.getSavedLightValue(lightLayer, tilePosc);
	}

	@Override
	public byte getSavedLightIndex(@NotNull TilePosc tilePosc) {
		return this.world.getSavedLightIndex(tilePosc);
	}

	@Override
	public @NotNull Dimension getDimension() {
		return this.world.getDimension();
	}

	@Override
	public @NotNull WorldType getWorldType() {
		return this.world.getWorldType();
	}

	@Override
	@Deprecated
	@SuppressWarnings("deprecation")
	public int getBlockId(int x, int y, int z) {
		return this.world.frl$fastGetBlockId(x, y, z);
	}

	@Override
	@Deprecated
	@SuppressWarnings("deprecation")
	public int getBlockMetadata(int x, int y, int z) {
		return this.world.frl$fastGetBlockMetadata(x, y, z);
	}
}
