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

import com.fox2code.frl.FRLRegistry;
import com.fox2code.frl.event.server.ConnectingToServerEvent;
import com.fox2code.frl.ext.net.ḞRLPacketHandler;
import com.fox2code.frl.network.FRLNetConstants;
import com.fox2code.frl.network.ext.LoginHandlerState;
import com.fox2code.frl.server.FRLGateKeeper;
import com.fox2code.frl.utils.FRLEmptyArrays;
import net.minecraft.core.net.NetworkManager;
import net.minecraft.core.net.packet.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.net.handler.PacketHandlerLogin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Socket;

@Mixin(value = PacketHandlerLogin.class, remap = false)
public abstract class MixinPacketHandlerLogin implements ḞRLPacketHandler {
	@Shadow
	public NetworkManager netManager;

	@Shadow
	public boolean finishedProcessing;

	@Shadow
	private String username;

	@Unique private LoginHandlerState frl$loginHandlerState;

	@Shadow
	public abstract void kickUser(String s, boolean silent);

	@Inject(method = "<init>", at = @At(value = "TAIL"))
	public void onCreation(MinecraftServer minecraftserver, Socket socket, String s, CallbackInfo ci) {
		this.frl$loginHandlerState = LoginHandlerState.PRE_HANDSHAKE;
	}

	@Inject(method = "kickUser(Ljava/lang/String;Z)V", at = @At(value = "HEAD"))
	public void onKickUser(String s, boolean silent, CallbackInfo ci) {
		if (this.frl$loginHandlerState == LoginHandlerState.PING && silent && s.startsWith("§1")) {
			this.netManager.addToSendQueue(new PacketDisconnect("FRL\u0000" +
				FRLRegistry.Server.getRegistryPingData() + "\u0000"));
		}
		this.frl$loginHandlerState = LoginHandlerState.KICK;
	}

	@Inject(method = "handleHandshake", at = @At(value = "HEAD"), cancellable = true)
	public void checkHandshakePacket(PacketHandshake packetHandshake, CallbackInfo ci) {
		if (this.finishedProcessing) {
			ci.cancel();
			return;
		}
		// For future pre-auth FRL negotiations.
		if (packetHandshake.username.startsWith("FRL\u0000")) {
			if (this.frl$checkStateChange(
				LoginHandlerState.PRE_HANDSHAKE,
				LoginHandlerState.REPEAT_HANDSHAKE)) {
				ci.cancel();
				return;
			}
			this.netManager.addToSendQueue(
				new PacketCustomPayload(
					FRLNetConstants.REPEAT_HANDSHAKE,
					FRLEmptyArrays.EMPTY_BYTE_ARRAY));
			ci.cancel();
			return;
		}
		if (this.frl$loginHandlerState == LoginHandlerState.PRE_HANDSHAKE) {
			this.frl$loginHandlerState = LoginHandlerState.REPEAT_HANDSHAKE;
		}
		if (this.frl$checkStateChange(
			LoginHandlerState.REPEAT_HANDSHAKE, LoginHandlerState.HANDSHAKE)) {
			ci.cancel();
			return;
		}
		this.username = packetHandshake.username;
	}

	@Inject(method = "handleLogin", at = @At(value = "HEAD"), cancellable = true)
	public void checkLoginPacket(PacketLogin packetLogin, CallbackInfo ci) {
		if (this.finishedProcessing || this.frl$checkStateChange(
			LoginHandlerState.HANDSHAKE, LoginHandlerState.LOGIN)) {
			ci.cancel();
		}
		if (this.username != null && !this.username.equals(packetLogin.username)) {
			this.kickUser("Login data tampering detected!", true);
			ci.cancel();
		}
		ConnectingToServerEvent connectingToServerEvent =
			new ConnectingToServerEvent(packetLogin.username, packetLogin.uuid,
				this.netManager, this.netManager.frl$getConnectionHostname());
		FRLGateKeeper.pushEvent(connectingToServerEvent);
		if (connectingToServerEvent.isCancelled()) {
			String kick = connectingToServerEvent.getKickMessage();
			if (kick == null) kick = "ConnectingToServerEvent was cancelled!";
			this.kickUser(kick, true);
			ci.cancel();
		}
	}

	@Inject(method = "handlePingHandshake", at = @At(value = "HEAD"), cancellable = true)
	public void checkPingHandshakePacket(PacketPingHandshake packetPingHandshake, CallbackInfo ci) {
		if (this.username != null && "FRL".equals(packetPingHandshake.pingHostString) &&
			(this.frl$loginHandlerState == LoginHandlerState.HANDSHAKE ||
				this.frl$loginHandlerState == LoginHandlerState.LOGIN) &&
			!this.finishedProcessing && !this.netManager.frl$hasFoxRootLib()) {
			this.netManager.frl$notifyConnectionHostname(packetPingHandshake.hostname);
			this.netManager.frl$notifyPresence();
			ci.cancel();
		} else if (this.finishedProcessing || this.frl$checkStateChange(
			LoginHandlerState.PRE_HANDSHAKE, LoginHandlerState.PING)) {
			ci.cancel();
		} else {
			this.netManager.frl$notifyConnectionHostname(packetPingHandshake.hostname);
		}
	}

	@Inject(method = "handleHandshake", at = @At(value = "RETURN"))
	public void sendMessagesConfiguration(PacketHandshake packetHandshake, CallbackInfo ci) {
		if (!this.finishedProcessing) {
			this.netManager.addToSendQueue(FRLRegistry.Server.getPreCompiledRegistryPacket());
		}
	}

	@Unique private boolean frl$checkStateChange(LoginHandlerState oldState, LoginHandlerState newState) {
		if (this.frl$loginHandlerState != oldState) {
			this.kickUser("Out of order protocol!", true);
			return true;
		} else {
			this.frl$loginHandlerState = newState;
			return false;
		}
	}

	@Override
	public boolean frl$isHardened() {
		return true;
	}

	@Override
	public @NotNull NetworkManager frl$getNetworkManager() {
		return this.netManager;
	}
}
