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
package com.fox2code.frl.block.tileentity;

import com.fox2code.frl.FRLEnergy;
import net.minecraft.core.util.helper.Direction;
import org.jetbrains.annotations.Nullable;

public interface FRLTileEntityEnergyDirectAccess {
	/**
	 * @param direction to get the energy from, if {@code null} it will bypass face checks.
	 * @param energy the amount of energy to transfer.
	 * @return how much energy has been transferred.
	 */
	default long frl$tryStealEnergy(@Nullable Direction direction, long energy) {
		return 0L;
	}

	default long frl$tryGiveEnergy(@Nullable Direction direction, long energy) {
		return 0L;
	}

	default long frl$tryGetEnergy() {
		return 0L;
	}

	default long frl$tryGetCapacity() {
		return 0L;
	}

	default @Nullable FRLEnergy.Archetype frl$tryGetArchetype() {
		return FRLEnergy.Archetype.CABLE;
	}
}
