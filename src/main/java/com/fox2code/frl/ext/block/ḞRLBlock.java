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
package com.fox2code.frl.ext.block;

public interface ḞRLBlock {
	int FRL_FLAG_INCOMPLETE_FLAG_DATA  = 0x01;
	int FRL_FLAG_FRL_ENERGY_COMPATIBLE = 0x02;
	int FRL_FLAG_CATALYST_NETWORK_TILE = 0x04;
	int FRL_FLAG_MAY_PROXY_TILE_ENTITY = 0x08;
	int FRL_FLAG_BLOCK_IS_AIR_VARIANT  = 0x10;

	default int frl$getFRLFlags() {
		throw new AssertionError("Implemented in Mixin");
	}

	default boolean frl$checkAnyFRLFlags(int flags) {
		throw new AssertionError("Implemented in Mixin");
	}

	default boolean frl$checkAllFRLFlags(int flags) {
		throw new AssertionError("Implemented in Mixin");
	}
}
