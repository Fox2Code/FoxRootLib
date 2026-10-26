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

import net.minecraft.core.net.packet.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

final class ClientHelloAuth extends FRLAuth2Packet {
	String clientPublicKey;
	String username;
	UUID uuid;
	byte[] challenge;
	byte[] sharedKey;

	ClientHelloAuth() {}

	ClientHelloAuth(String clientPublicKey, String username, UUID uuid, byte[] challenge, byte[] sharedKey) {
		this.clientPublicKey = clientPublicKey;
		this.username = username;
		this.uuid = uuid;
		this.challenge = challenge;
		this.sharedKey = sharedKey;
	}

	@Override
	public void writePacket(DataOutputStream dataOutputStream) throws IOException {
		writeStringUTF8(this.clientPublicKey, dataOutputStream);
		writeStringUTF8(this.username, dataOutputStream);
		writeUUID(this.uuid, dataOutputStream);
		dataOutputStream.write(this.challenge, 0, 512);
		dataOutputStream.writeShort(this.sharedKey.length);
		dataOutputStream.write(this.sharedKey, 0, this.sharedKey.length);
	}

	@Override
	public void readPacket(DataInputStream dataInputStream) throws IOException {
		this.clientPublicKey = readStringUTF8(dataInputStream, 8192);
		this.username = readStringUTF8(dataInputStream, 16);
		this.uuid = Packet.readUUID(dataInputStream);
		if (this.challenge == null) this.challenge = new byte[512];
		dataInputStream.readFully(this.challenge, 0, 512);
		this.sharedKey = new byte[dataInputStream.readUnsignedShort()];
		dataInputStream.readFully(this.sharedKey, 0, this.sharedKey.length);
	}

	@Override
	boolean execute(FRLAuth2 frlAuth2) {
		frlAuth2.serverReplyAuth(this);
		return true;
	}
}
