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

import com.fox2code.frl.FRLCore;
import com.fox2code.frl.network.NetworkChannel;
import com.fox2code.frl.network.NetworkChannelManager;
import net.minecraft.core.net.handler.PacketHandler;
import net.minecraft.core.net.packet.PacketCustomPayload;

import java.io.DataInputStream;
import java.io.IOException;

public final class PacketCustomPayloadInbound extends PacketCustomPayload {
	private NetworkChannel.DataExecutor overrideHandler;

	public PacketCustomPayloadInbound() {}

	@Override
	public void read(DataInputStream in) throws IOException {
		this.channel = readStringUTF8(in, 128);
		int length = in.readInt();
		if (length > 0 && length < 32768) {
			this.data = new byte[length];
			in.readFully(this.data, 0, length);
		}
		this.overrideHandler = NetworkChannelManager.handleDataPacketReception(FRLCore.isServer, this);
		if (this.overrideHandler != null) this.data = null;
	}

	@Override
	public void handlePacket(PacketHandler packetHandler) {
		if (this.overrideHandler != null) {
			this.overrideHandler.execute(packetHandler);
		} else {
			packetHandler.handleCustomPayload(this);
		}
	}
}
