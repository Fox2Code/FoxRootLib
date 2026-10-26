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
package com.fox2code.frl.network.auth2;

import com.fox2code.frl.network.FRLNetConstants;
import net.minecraft.core.net.packet.PacketCustomPayload;

import java.io.*;

final class FRLAuth2Channel {
	private FRLAuth2Channel() { throw new OutOfMemoryError(); }

	static PacketCustomPayload toBTA(FRLAuth2Packet packet, boolean isServer) {
		int packetId;
		if (isServer) {
			packetId = switch (packet.getClass().getSimpleName()) {
				case "ServerHelloAuth" -> 1;
				case "ServerAuthPass" -> 2;
				default -> throw new IllegalArgumentException();
			};
		} else {
			packetId = switch (packet.getClass().getSimpleName()) {
				case "ClientHelloAuth" -> 1;
				case "ClientAuthOffline" -> 2;
				default -> throw new IllegalArgumentException();
			};
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
		try {
			dataOutputStream.writeByte(packetId);
			packet.writePacket(dataOutputStream);
		} catch (IOException e) {
			throw new IOError(e);
		}
		return new PacketCustomPayload(FRLNetConstants.FRL_AUTH2, byteArrayOutputStream.toByteArray());
	}

	static FRLAuth2Packet fromBTA(PacketCustomPayload packetCustomPayload, boolean isServer) {
		int packetId = (packetCustomPayload.data[0] & 0x7F);
		if (packetId != (packetCustomPayload.data[0] & 0xFF)) {
			throw new IllegalArgumentException();
		}
		FRLAuth2Packet packet;
		if (isServer) {
			packet = switch (packetId) {
				case 1 -> new ClientHelloAuth();
				case 2 -> new ClientAuthOffline();
				default -> throw new IllegalArgumentException();
			};
		} else {
			packet = switch (packetId) {
				case 1 -> new ServerHelloAuth();
				case 2 -> new ServerAuthPass();
				default -> throw new IllegalArgumentException();
			};
		}
		DataInputStream dataInputStream = new DataInputStream(
				new ByteArrayInputStream(packetCustomPayload.data));
		try {
			dataInputStream.skipNBytes(1);
			packet.readPacket(dataInputStream);
		} catch (IOException e) {
			throw new IOError(e);
		}
		return packet;
	}
}
