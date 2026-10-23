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
package com.fox2code.frl.network.ext;

import com.fox2code.frl.network.NetworkChannel;
import com.fox2code.frl.network.NetworkChannelManager;
import com.fox2code.frl.utils.FRLEmptyArrays;
import net.minecraft.core.net.packet.PacketCustomPayload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutputStream;
import java.io.IOException;

public final class PacketCustomPayloadOutbound<T extends NetworkChannel.DataHandle> extends PacketCustomPayload {
	private final @NotNull NetworkChannel<T> networkChannel;
	private final boolean isServer;
	private @Nullable T handle;

	public PacketCustomPayloadOutbound(
		@NotNull NetworkChannel<T> networkChannel, boolean isServer, @Nullable T handle) {
		super(networkChannel.getChannel(), FRLEmptyArrays.EMPTY_BYTE_ARRAY);
		this.networkChannel = networkChannel;
		this.isServer = isServer;
		this.handle = handle;
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		this.ensurePacketCompiled();
		super.write(out);
	}

	@Override
	public int getEstimatedSize() {
		return this.channel.length() + 4 +
			(handle == null ? this.data.length :
				handle.getEstimatedSize());
	}

	public void ensurePacketCompiled() throws IOException {
		if (this.handle == null) return;
		this.data = NetworkChannelManager.compileFor(this.networkChannel, this.isServer, this.handle);
		this.handle = null;
	}
}
