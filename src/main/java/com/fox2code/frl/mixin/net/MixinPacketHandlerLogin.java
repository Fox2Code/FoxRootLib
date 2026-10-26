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

import com.fox2code.frl.FRLConfig;
import com.fox2code.frl.FRLRegistry;
import com.fox2code.frl.event.server.ConnectingToServerEvent;
import com.fox2code.frl.ext.net.ḞRLPacketHandler;
import com.fox2code.frl.network.FRLNetConstants;
import com.fox2code.frl.network.auth2.FRLAuth2;
import com.fox2code.frl.network.ext.LoginHandlerState;
import com.fox2code.frl.server.FRLGateKeeper;
import com.fox2code.frl.utils.FRLEmptyArrays;
import net.minecraft.core.net.NetworkManager;
import net.minecraft.core.net.handler.PacketHandler;
import net.minecraft.core.net.packet.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.net.handler.PacketHandlerLogin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Socket;
import java.util.Objects;
import java.util.UUID;

@Mixin(value = PacketHandlerLogin.class, remap = false)
public abstract class MixinPacketHandlerLogin extends PacketHandler implements ḞRLPacketHandler {
	@Shadow public NetworkManager netManager;

	@Shadow public boolean finishedProcessing;

	@Shadow private String username;

	@Shadow private String serverId;

	@Shadow @Final private MinecraftServer mcServer;

	@Unique private LoginHandlerState frl$loginHandlerState;

	@Unique private FRLAuth2 frl$Auth2;

	@Unique private boolean frl$supportAuth2;

	@Shadow
	public abstract void handleInvalidPacket(@NotNull Packet packet);

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
			// Auth2 is a more secure auth system.
			this.frl$supportAuth2 = true;
			if (this.mcServer.onlineMode && FRLConfig.allowAuth2) {
				this.frl$loginHandlerState = LoginHandlerState.AUTH2;
				this.frl$Auth2 = new FRLAuth2(this.netManager);
				this.frl$Auth2.initializeAuth();
			} else {
				this.netManager.addToSendQueue(
						new PacketCustomPayload(
								FRLNetConstants.REPEAT_HANDSHAKE,
								FRLEmptyArrays.EMPTY_BYTE_ARRAY));
			}
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
		if (FRLConfig.forceAuth2) {
			this.kickUser(FRLConfig.modpackForceAuth2 ?
					"You are using an outdated version of the modpack, please update!" :
					"This server requires FoxRootLib Auth2 support to join!", true);
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
			return;
		}
		if (this.username != null && !this.username.equals(packetLogin.username) ||
				(this.frl$Auth2 != null && !Objects.equals(this.frl$Auth2.getUUID(), packetLogin.uuid)) ||
				(this.frl$supportAuth2 && !this.netManager.frl$hasFoxRootLib())) {
			this.kickUser("Login data tampering detected!", true);
			ci.cancel();
			return;
		}
		if (this.mcServer.onlineMode && !this.netManager.frl$useFullCrypto()) {
			if (this.frl$Auth2 != null) {
				this.kickUser("Failed to verify authentification!", true);
				ci.cancel();
				return;
			} else if (FRLConfig.forceAuth2) {
				this.kickUser("This server requires FoxRootLib Auth2 support to join!", true);
				ci.cancel();
				return;
			}
		} else if (this.frl$Auth2 != null) {
			if (this.frl$Auth2.isLoginStateInvalid()) {
				this.kickUser("Failed to verify authentification!", true);
				return;
			}
			this.serverId = this.frl$Auth2.getFinalServerId();
		}
		if (this.frl$Auth2 != null) {
			this.frl$Auth2 = null;
		} else if (this.frl$sendConnectingToServerEvent(packetLogin.username, packetLogin.uuid)) {
			ci.cancel();
		}
	}

	@Inject(method = "handlePingHandshake", at = @At(value = "HEAD"), cancellable = true)
	public void checkPingHandshakePacket(PacketPingHandshake packetPingHandshake, CallbackInfo ci) {
		if ("FRL".equals(packetPingHandshake.pingHostString) &&
				(this.frl$loginHandlerState == LoginHandlerState.HANDSHAKE ||
						this.frl$loginHandlerState == LoginHandlerState.AUTH2) &&
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
		if (this.frl$loginHandlerState == LoginHandlerState.HANDSHAKE && !this.finishedProcessing) {
			this.netManager.addToSendQueue(FRLRegistry.Server.getPreCompiledRegistryPacket());
		}
	}

	@Unique private boolean frl$checkStateChange(LoginHandlerState oldState, LoginHandlerState newState) {
		if (this.frl$loginHandlerState != oldState) {
			this.kickUser("Out of order protocol! (" + this.frl$loginHandlerState + " != " + oldState + ")", true);
			return true;
		} else {
			this.frl$loginHandlerState = newState;
			return false;
		}
	}

	@Unique private boolean frl$sendConnectingToServerEvent(String username, UUID uuid) {
		ConnectingToServerEvent connectingToServerEvent =
				new ConnectingToServerEvent(username, uuid, this.netManager,
						this.netManager.frl$getConnectionHostname(), this.frl$Auth2 != null);
		FRLGateKeeper.pushEvent(connectingToServerEvent);
		if (connectingToServerEvent.isCancelled()) {
			String kick = connectingToServerEvent.getKickMessage();
			if (kick == null) kick = "ConnectingToServerEvent was cancelled!";
			this.kickUser(kick, true);
			return true;
		}
		return false;
	}

	@Override
	public void handleCustomPayload(@NotNull PacketCustomPayload packetCustomPayload) {
		if (this.finishedProcessing || (this.frl$Auth2 != null &&
				this.frl$Auth2.isAuth2KeyPacket(packetCustomPayload))) {
			return;
		}
		if (this.frl$Auth2 == null || this.frl$loginHandlerState != LoginHandlerState.AUTH2) {
			this.handleInvalidPacket(packetCustomPayload);
			return;
		}
		if (FRLNetConstants.FRL_AUTH2.equals(packetCustomPayload.channel)) {
			if (this.frl$Auth2.onReceivePacket(packetCustomPayload)) {
				if (this.frl$Auth2.getKickMessage() != null) {
					this.kickUser(this.frl$Auth2.getKickMessage(), true);
					return;
				}
				this.username = this.frl$Auth2.getUsername();
				if (this.frl$sendConnectingToServerEvent(this.username, this.frl$Auth2.getUUID()) ||
						this.frl$checkStateChange(LoginHandlerState.AUTH2, LoginHandlerState.HANDSHAKE)) {
					this.frl$Auth2 = null; // Free the connection when denying login.
				} else {
					// Allow the client to make an auth request.
					this.netManager.addToSendQueue(FRLRegistry.Server.getPreCompiledRegistryPacket());
					this.frl$Auth2.authRequestAccepted();
				}
			}
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

	@Override
	public boolean frl$a2bh() {
		// Auth2 bypass hardened: Let Auth2 packets go through the hardened packet filter.
		return this.frl$Auth2 != null &&
				(this.frl$loginHandlerState == LoginHandlerState.AUTH2 ||
				this.frl$loginHandlerState == LoginHandlerState.HANDSHAKE);
	}
}
