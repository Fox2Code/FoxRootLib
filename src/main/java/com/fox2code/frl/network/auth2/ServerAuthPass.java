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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

final class ServerAuthPass extends FRLAuth2Packet {
	String serverId;

	ServerAuthPass() {}

	ServerAuthPass(String serverId) {
		this.serverId = serverId;
	}

	@Override
	boolean execute(FRLAuth2 frlAuth2) {
		return frlAuth2.clientFinishLogin(this);
	}

	@Override
	public void writePacket(DataOutputStream dataOutputStream) throws IOException {
		writeStringUTF8(this.serverId, dataOutputStream);
	}

	@Override
	public void readPacket(DataInputStream dataInputStream) throws IOException {
		this.serverId = readStringUTF8(dataInputStream, 16);
	}
}
