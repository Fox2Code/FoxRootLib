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
package com.fox2code.frl.entity;

import com.fox2code.frl.FRLCore;
import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.block.entity.*;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.MobPathfinder;
import net.minecraft.core.entity.player.Player;
import net.minecraft.core.enums.EnumSleepStatus;
import net.minecraft.core.item.ItemStack;
import net.minecraft.core.net.command.TextFormatting;
import net.minecraft.core.player.inventory.container.Container;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import net.minecraft.core.player.inventory.menu.*;
import net.minecraft.core.util.helper.DamageType;
import net.minecraft.core.util.helper.GetSkinUrlThread;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.ICarriable;
import net.minecraft.core.world.World;
import net.minecraft.core.world.pathfinder.Path;
import net.minecraft.core.world.pos.TilePosc;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;
import org.joml.primitives.AABBdc;

import java.util.UUID;

/**
 * Base to create custom fake players to interact with mods with some {@link MobPathfinder}
 * features, please prefer using {@link FRLFakePlayerNPC} instead of extending this class
 * if you do not need to override methods.
 */
public abstract class FRLFakePlayerBase extends Player implements FRLPathingEntity {
	public static final UUID FAKE_PLAYER_UUID = new UUID(0, 0);
	private final FRLFakePlayerNet net;
	protected @Nullable Path pathToEntity;
	protected @Nullable Entity target;
	protected boolean hasAttacked;
	protected boolean hostile;
	private int lerpSteps;
	private double lerpX;
	private double lerpY;
	private double lerpZ;
	private double lerpYRot;
	private double lerpXRot;

	public FRLFakePlayerBase(@NotNull World world) {
		this(world, null, null, true);
	}

	public FRLFakePlayerBase(@NotNull World world,@Nullable String username,@Nullable UUID uuid) {
		this(world, username, uuid, true);
	}

	public FRLFakePlayerBase(@NotNull World world,@NotNull Player player) {
		this(world, player.username, player.uuid, true);
		this.playerConfig.config = player.playerConfig.config;
		for (int i = 0; i < ContainerInventory.ARMOR_INVENTORY_SIZE; i++) {
			ItemStack itemStack = player.inventory.armorInventory[i];
			if (itemStack != null && itemStack.itemID != 0) {
				this.inventory.armorInventory[i] = itemStack.copy();
			}
		}
		for (int i = 0; i < ContainerInventory.MAIN_INVENTORY_SIZE; i++) {
			ItemStack itemStack = player.inventory.mainInventory[i];
			if (itemStack != null && itemStack.itemID != 0) {
				this.inventory.mainInventory[i] = itemStack.copy();
			}
		}
	}

	public FRLFakePlayerBase(@NotNull World world,@Nullable String username,@Nullable UUID uuid, boolean netTracking) {
		super(world);
		this.username = username == null ? "FakePlayer" : username;
		this.uuid = uuid == null ? FAKE_PLAYER_UUID : uuid;
		this.footSize = 0.0F;
		this.sleepOffY = 0.25F;
		this.viewScale = 10.0F;
		if (FRLCore.isServer) {
			this.net = netTracking ?
				FRLFakePlayerNetServer.make(this) :
				FRLFakePlayerNet.NOOP;
		} else {
			this.net = FRLFakePlayerNet.NOOP;
			this.tryGetSkinUrl();
		}
	}

	@Override
	public @Nullable AABBdc getCollisionAABB() {
		return this.hasNoPhysics() ? null : super.getCollisionAABB();
	}

	public ItemStack getEquipmentInSlot(int i) {
		return i == 0 ? this.inventory.getCurrentItem() : this.inventory.armorInventory[i - 1];
	}

	@Override
	public boolean hurt(Entity attacker, int damage, DamageType type) {
		return !this.world.isClientSide && super.hurt(attacker, damage, type);
	}

	@Override
	public void lerpTo(double x, double y, double z, float yRot, float xRot, int ticks) {
		super.lerpTo(x, y, z, yRot, xRot, ticks);
		this.lerpX = x;
		this.lerpY = y;
		this.lerpZ = z;
		this.lerpYRot = yRot;
		this.lerpXRot = xRot;
		this.lerpSteps = ticks;
	}

	@Override
	public void tick() {
		if (FRLCore.isServer) {
			super.tick();
			this.net.tick();
		} else {
			this.sleepOffY = 0.0F;
			super.tick();
			this.walkAnimSpeedO = this.walkAnimSpeed;
			this.xd = this.x - this.xo;
			this.yd = this.y - this.yo;
			this.zd = this.z - this.zo;
			float f = MathHelper.sqrt(this.xd * this.xd + this.zd * this.zd) * 4.0F;
			if (f > 1.0F) {
				f = 1.0F;
			}

			this.walkAnimSpeed += (f - this.walkAnimSpeed) * 0.4F;
			this.walkAnimPos += this.walkAnimSpeed;
		}
	}

	@Override
	public void onDeath(Entity entityKilledBy) {
		if (!this.world.isClientSide) {
			super.onDeath(entityKilledBy);
		}
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!this.world.isClientSide) {
			return;
		}
		if (this.lerpSteps > 0) {
			double d = this.x + (this.lerpX - this.x) / (double)this.lerpSteps;
			double d1 = this.y + (this.lerpY - this.y) / (double)this.lerpSteps;
			double d2 = this.z + (this.lerpZ - this.z) / (double)this.lerpSteps;
			double d3 = MathHelper.normalizeRotation(this.lerpYRot - (double)this.yRot);
			this.yRot = (float)((double)this.yRot + d3 / (double)this.lerpSteps);
			this.xRot = (float)((double)this.xRot + (this.lerpXRot - (double)this.xRot) / (double)this.lerpSteps);
			--this.lerpSteps;
			this.setPos(d, d1, d2);
			this.setRot(this.yRot, this.xRot);
		}

		this.cameraVelocityOld = this.cameraVelocity;
		float horizontalSpeed = MathHelper.sqrt(this.xd * this.xd + this.zd * this.zd);
		float f1 = (float)Math.atan(-this.yd * 0.2) * 15.0F;
		if (horizontalSpeed > 0.1F) {
			horizontalSpeed = 0.1F;
		}

		if (!this.onGround || this.getHealth() <= 0) {
			horizontalSpeed = 0.0F;
		}

		if (this.onGround || this.getHealth() <= 0) {
			f1 = 0.0F;
		}

		this.cameraVelocity += (horizontalSpeed - this.cameraVelocity) * 0.4F;
		this.cameraPitch += (f1 - this.cameraPitch) * 0.8F;
	}

	@Override
	public void setEquippedSlot(int slot, int itemID, int itemMeta, CompoundTag tag) {
		ItemStack itemstack = null;
		if (itemID >= 0) {
			itemstack = new ItemStack(itemID, 1, itemMeta, tag);
		}

		if (slot == 0) {
			this.inventory.mainInventory[this.inventory.getCurrentSlot()] = itemstack;
		} else {
			this.inventory.armorInventory[slot - 1] = itemstack;
		}
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("Score", this.score);
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.net.updateDimension(this.dimension);
		this.score = tag.getInteger("Score");
	}

	@Override
	public void displayContainerScreen(Container container) {
		this.containerMenu.onCraftGuiClosed(this);
		this.containerMenu = new MenuContainer(this.inventory, container);
		this.containerMenu.containerId = -1;
	}

	@Override
	public void displayChestScreen(Container container, double x, double y, double z) {
		this.displayContainerScreen(container);
	}

	@Override
	public void displayWorkbenchScreen(@NotNull TilePosc tilePos) {
		this.containerMenu.onCraftGuiClosed(this);
		this.containerMenu = new MenuCrafting(this.inventory, this.world, tilePos);
		this.containerMenu.containerId = -1;
	}

	@Override
	public void displayFurnaceScreen(TileEntityFurnace tileEntity) {
		this.containerMenu.onCraftGuiClosed(this);
		this.containerMenu = new MenuFurnace(this.inventory, tileEntity);
		this.containerMenu.containerId = -1;
	}

	@Override
	public void displayBlastFurnaceScreen(TileEntityFurnaceBlast tileEntity) {
		this.containerMenu.onCraftGuiClosed(this);
		this.containerMenu = new MenuFurnaceBlast(this.inventory, tileEntity);
		this.containerMenu.containerId = -1;
	}

	@Override
	public void displayTrommelScreen(TileEntityTrommel tileEntity) {
		this.containerMenu.onCraftGuiClosed(this);
		this.containerMenu = new MenuTrommel(this.inventory, tileEntity);
		this.containerMenu.containerId = -1;
	}

	@Override
	public void displayDispenserScreen(TileEntityDispenser tileEntity) {
		this.containerMenu.onCraftGuiClosed(this);
		this.containerMenu = new MenuDispenser(this.inventory, tileEntity);
		this.containerMenu.containerId = -1;
	}

	@Override
	public void displayActivatorScreen(TileEntityActivator tileEntity) {
		this.containerMenu.onCraftGuiClosed(this);
		this.containerMenu = new MenuActivator(this.inventory, tileEntity);
		this.containerMenu.containerId = -1;
	}

	@Override
	public void animate4() {

	}

	@Override
	public void sendMessage(boolean b, TextFormatting.@Nullable Base base, @NotNull String s) {

	}

	@Override
	public void sendMessageTranslated(boolean b, TextFormatting.@Nullable Base base, @NotNull String s, @NotNull String... strings) {

	}

	@Override
	public void swingItem() {
		if (this.world.isClientSide) {
			this.swingProgressInt = -1;
			this.isSwinging = true;
		} else if (!this.isSwinging) {
			this.swingProgressInt = -1;
			this.isSwinging = true;
			this.net.swingHand();
		}
	}

	@Override
	public void setHeldObject(@Nullable ICarriable heldObject) {
		super.setHeldObject(heldObject);
		this.net.notifyHeldObject(heldObject);
	}

	public EnumSleepStatus sleepInBedAt(int x, int y, int z) {
		EnumSleepStatus status = super.sleepInBedAt(x, y, z);
		if (status == EnumSleepStatus.OK) {
			this.net.notifySleep(x, y, z);
		}
		return status;
	}

	@Override
	public void wakeUpPlayer(boolean flag, boolean flag1) {
		if (this.isPlayerSleeping()) {
			this.net.notifyWakeUp();
		}
		super.wakeUpPlayer(flag, false);
	}

	@Override
	protected void updateAI() {
		this.hasAttacked = false;
		if (this.target != null) {
			double targetDist = this.distanceToSqr(this.target);
			if (this.pathToEntity == null && (this.hostile || targetDist > 3D)) {
				this.pathToEntity = this.world.frl$fastGetEntityPathToEntity(this, this.target, 16.0F);
			}
			if (!this.target.isAlive()) {
				this.target = null;
			} else if (this.hostile && targetDist <= 9F) {
				if (this.attackTime <= 0) {
					this.attackTime = 10;
					this.hasAttacked = true;
					this.swingItem();
					this.attackTargetEntityWithCurrentItem(this.target);
				}
			} else if (targetDist < 2.5D &&
				Math.max(Math.abs(this.x - target.x),
					Math.abs(this.z - this.target.z)) < 1.2D) {
				this.lookAt(this.target, 30.0F, 30.0F);
				this.pathToEntity = null;
			}
		}
		if (this.pathToEntity != null) {
			if (this.target != null && this.checkDoRecalculateEntityPath()) {
				this.pathToEntity = null;
				super.updateAI();
				return;
			}
			Vector3dc coordsForNextPath = this.pathToEntity.getPos(this);
			double d = this.bbWidth * 2.0F;

			while(coordsForNextPath != null && coordsForNextPath.distanceSquared(this.x, coordsForNextPath.y(), this.z) < d * d) {
				this.pathToEntity.next();
				if (this.pathToEntity.isDone()) {
					coordsForNextPath = null;
					this.pathToEntity = null;
				} else {
					coordsForNextPath = this.pathToEntity.getPos(this);
				}
			}

			this.isJumping = false;
			if (coordsForNextPath != null) {
				int yBlock = MathHelper.floor(this.bb.minY + (double)0.5F);
				d = coordsForNextPath.x() - this.x;
				double z1 = coordsForNextPath.z() - this.z;
				double y1 = coordsForNextPath.y() - (double)yBlock;
				float f2 = (float)(Math.atan2(z1, d) * (double)180.0F / Math.PI) - 90.0F;
				float f3 = f2 - this.yRot;
				this.moveForward = this.moveSpeed;

				while (f3 < -180.0F) {
					f3 += 360.0F;
				}

				while(f3 >= 180.0F) {
					f3 -= 360.0F;
				}

				if (f3 > 30.0F) {
					f3 = 30.0F;
				}

				if (f3 < -30.0F) {
					f3 = -30.0F;
				}

				this.yRot += f3;
				if (this.hasAttacked && this.target != null) {
					double d4 = this.target.x - this.x;
					double d5 = this.target.z - this.z;
					float f5 = this.yRot;
					this.yRot = (float)(Math.atan2(d5, d4) * (double)180.0F / Math.PI) - 90.0F;
					float f4 = (f5 - this.yRot + 90.0F) * (float)Math.PI / 180.0F;
					this.moveStrafing = -MathHelper.sin(f4) * this.moveForward * 1.0F;
					this.moveForward = MathHelper.cos(f4) * this.moveForward * 1.0F;
				}

				if (y1 > (double)0.0F) {
					this.isJumping = true;
				}
			}

			if (this.target != null) {
				this.lookAt(this.target, 30.0F, 30.0F);
			}

			if (this.random.nextFloat() < 0.8F && (this.isInWater() || this.isInLava())) {
				this.isJumping = true;
			}
		}
		super.updateAI();
	}

	@Override
	public boolean hasPath() {
		return this.pathToEntity != null;
	}

	@Override
	public void setPathToEntity(@Nullable Path path) {
		this.pathToEntity = path;
	}

	@Override
	public @Nullable Entity getTarget() {
		return this.target;
	}

	@Override
	public void setTarget(@Nullable Entity target) {
		this.target = target;
	}

	public @Nullable MenuAbstract getOpenedScreen() {
		return this.containerMenu == this.inventoryMenu ?
			null : this.containerMenu;
	}

	public boolean hasScreenOpen() {
		return this.getOpenedScreen() != null;
	}

	public abstract boolean checkDoRecalculateEntityPath();

	public void tryGetSkinUrl() {
		if ((this.uuid == null || this.uuid.version() != 0) && !this.username.isEmpty()) {
			new GetSkinUrlThread(this);
		}
	}

	static class FRLFakePlayerNet {
		static final FRLFakePlayerNet NOOP = new FRLFakePlayerNet();

		void updateDimension(int dimension) {}

		void tick() {}

		void swingHand() {}

		void notifyHeldObject(@Nullable ICarriable heldObject) {}

		void notifySleep(int x, int y, int z) {}

		void notifyWakeUp() {}
	}
}
