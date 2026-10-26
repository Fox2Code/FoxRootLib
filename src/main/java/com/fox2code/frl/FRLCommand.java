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
package com.fox2code.frl;

import com.fox2code.frl.entity.FRLFakePlayerNPC;
import com.fox2code.frl.utils.FRLJomlUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilderLiteral;
import com.mojang.brigadier.builder.ArgumentBuilderRequired;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.net.command.CommandManager;
import net.minecraft.core.net.command.CommandSource;
import net.minecraft.core.net.command.arguments.ArgumentTypeEntity;
import net.minecraft.core.net.command.exceptions.CommandExceptions;
import net.minecraft.core.net.command.helpers.EntitySelector;
import net.minecraft.core.net.handler.PacketHandler;
import org.joml.primitives.AABBd;

import java.util.List;

final class FRLCommand implements CommandManager.CommandRegistry {
	@Override
	public void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(((ArgumentBuilderLiteral.<CommandSource>literal("frl")).requires(CommandSource::hasAdmin))
				.then(ArgumentBuilderLiteral.<CommandSource>literal("version").executes((c) -> {
					c.getSource().sendMessage("[FRL] Version: " + FRLCore.MOD_VERSION);
					return 1;
				})).then(ArgumentBuilderLiteral.<CommandSource>literal("net").then(
						ArgumentBuilderRequired.<CommandSource, EntitySelector>argument(
								"target", ArgumentTypeEntity.username()).executes((c) -> {
							EntitySelector selector = c.getArgument("target", EntitySelector.class);
							List<? extends Entity> entities = selector.get(c.getSource());
							if (entities == null || entities.isEmpty() ||
									!(entities.get(0) instanceof Player player)) {
								throw CommandExceptions.notInWorld().create();
							}
							PacketHandler packetHandler = player.frl$getPacketHandler();
							c.getSource().sendMessage("[FRL] FRL State: " +
									(packetHandler == null ? "Singleplayer" : player.frl$hasFoxRootLib() ?
											(packetHandler.frl$getNetworkManager().frl$useFullCrypto() ?
													"Auth2" : "Present") : "Absent"));
							return 1;
						})).executes((c) -> {
					Player sender = c.getSource().getSender();
					if (sender == null) {
						throw CommandExceptions.notInWorld().create();
					}
					PacketHandler packetHandler = sender.frl$getPacketHandler();
					sender.sendMessage("[FRL] FRL State: " +
							(packetHandler == null ? "Singleplayer" : sender.frl$hasFoxRootLib() ?
									(packetHandler.frl$getNetworkManager().frl$useFullCrypto() ?
											"Auth2" : "Present") : "Absent"));
					return 1;
				})).then(ArgumentBuilderLiteral.<CommandSource>literal("energy").executes((c) -> {
					c.getSource().sendMessage(
							"[FRL] Active energy system: " + FRLEnergy.ENERGY_SYSTEM_NAME);
					return 1;
				})).then(ArgumentBuilderLiteral.<CommandSource>literal("npc")
						.then(ArgumentBuilderLiteral.<CommandSource>literal("cloneme").executes((c) -> {
							Player player = c.getSource().getSender();
							if (player == null) {
								throw CommandExceptions.notInWorld().create();
							}
							// FRLFakePlayerNPC does not save by default
							FRLFakePlayerNPC newFakePlayer = new FRLFakePlayerNPC(player.world, player);
							newFakePlayer.moveTo(player.x, player.y, player.z, player.yRot, player.xRot);
							newFakePlayer.spawnInit();
							player.world.entityJoinedWorld(newFakePlayer);
							player.sendMessage("[FRL] Summoned a non persistent copy of yourself!");
							return 1;
						})).then(ArgumentBuilderLiteral.<CommandSource>literal("followme").executes((c) -> {
							Player player = c.getSource().getSender();
							if (player == null) {
								throw CommandExceptions.notInWorld().create();
							}
							AABBd aabBd = new AABBd(player.bb);
							FRLJomlUtils.expandBoundingBox(aabBd, 1.5, 0.5);
							int followCount = 0;
							for (Entity entity : player.world.getEntitiesWithinAABBExcludingEntity(player, aabBd)) {
								if (entity instanceof FRLFakePlayerNPC frlFakePlayerNPC) {
									frlFakePlayerNPC.setTarget(player);
									followCount++;
								}
							}
							player.sendMessage("[FRL] You made " + followCount + " NPCs follow you!");
							return 1;
						})).then(ArgumentBuilderLiteral.<CommandSource>literal("unfollowme").executes((c) -> {
							Player player = c.getSource().getSender();
							if (player == null) {
								throw CommandExceptions.notInWorld().create();
							}
							AABBd aabBd = new AABBd(player.bb);
							FRLJomlUtils.expandBoundingBox(aabBd, 9, 5);
							int unfollowCount = 0;
							for (Entity entity : player.world.getEntitiesWithinAABBExcludingEntity(player, aabBd)) {
								if (entity instanceof FRLFakePlayerNPC frlFakePlayerNPC && frlFakePlayerNPC.getTarget() == player) {
									frlFakePlayerNPC.setTarget(null);
									unfollowCount++;
								}
							}
							player.sendMessage("[FRL] You made " + unfollowCount + " NPCs stop following you!");
							return 1;
						}))
				));
	}
}
