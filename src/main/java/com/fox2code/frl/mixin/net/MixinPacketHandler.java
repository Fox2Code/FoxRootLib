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
package com.fox2code.frl.mixin.net;

import com.fox2code.frl.ext.net.ḞRLPacketHandler;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.handler.PacketHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PacketHandler.class)
public abstract class MixinPacketHandler implements ḞRLPacketHandler {
	@Override
	public boolean frl$isHardened() {
		return false;
	}

	@Override
	public boolean frl$hasFoxRootLib() {
		return this.frl$getNetworkManager().frl$hasFoxRootLib();
	}

	@Override
	public @Nullable Player frl$getPlayer() {
		return null;
	}
}
