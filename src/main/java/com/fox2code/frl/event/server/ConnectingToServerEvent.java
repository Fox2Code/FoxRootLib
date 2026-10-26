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
package com.fox2code.frl.event.server;

import com.fox2code.foxevents.Event;
import net.minecraft.core.net.NetworkManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.UUID;

/**
 * Called before the server verifies the identity of the connection when connecting to the server.
 * <br/>
 * Note: Do not trust the values provided by this event as they are likely attacker controlled.
 */
public final class ConnectingToServerEvent extends Event implements Event.Cancellable {
	private final String username;
	private final UUID uuid;
	private final NetworkManager networkManager;
	private final String hostname;
	private final boolean withAuth2;
	private String kickMessage;

	public ConnectingToServerEvent(String username, UUID uuid, NetworkManager networkManager, String hostname, boolean withAuth2) {
		Objects.requireNonNull(username);
		Objects.requireNonNull(uuid);
		Objects.requireNonNull(networkManager);
		this.username = username;
		this.uuid = uuid;
		this.networkManager = networkManager;
		this.hostname = hostname;
		this.withAuth2 = withAuth2;
	}

	/**
	 * @return the address of the connection
	 */
	public SocketAddress getSocketAddress() {
		return this.networkManager.getRemoteAddress();
	}

	/**
	 * @return the username the connection claims wants to connect as.
	 */
	public @NotNull String getUsername() {
		return this.username;
	}

	/**
	 * @return the UUID the connection claims wants to connect as.
	 */
	public @NotNull UUID getClaimedUUID() {
		return this.uuid;
	}

	/**
	 * @return the hostname the player used to join the server
	 */
	public @Nullable String getHostname() {
		return this.hostname;
	}

	/**
	 * @return if the connection uses FoxRootLib (Aka. FRL)
	 */
	public boolean hasFoxRootLib() {
		return this.networkManager.frl$hasFoxRootLib();
	}

	/**
	 * @return the underlying network connection
	 */
	public @NotNull NetworkManager getNetworkManager() {
		return this.networkManager;
	}

	/**
	 * @return if the player connecting is connecting with Auth2
	 */
	public boolean isWithAuth2() {
		return this.withAuth2;
	}

	/**
	 * @return the kick message used to kick the player
	 */
	public String getKickMessage() {
		return this.kickMessage;
	}

	/**
	 * @param kickMessage to use when kicking the player
	 */
	public void kickPlayer(@Nullable String kickMessage) {
		this.kickMessage = kickMessage;
		this.setCancelled(true);
	}
}
