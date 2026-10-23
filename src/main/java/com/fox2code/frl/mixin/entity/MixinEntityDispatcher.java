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
package com.fox2code.frl.mixin.entity;

import com.fox2code.frl.entity.FRLEntitySubDispatcher;
import com.mojang.nbt.tags.CompoundTag;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityDispatcher.class)
public abstract class MixinEntityDispatcher {
	@Inject(method = "entryForId(Ljava/lang/String;)Lnet/minecraft/core/entity/EntityDispatcher$EntityDispatcherEntry;",
		at = @At("HEAD"), cancellable = true)
	public void onGetEntryFromId(
		@NotNull String id, CallbackInfoReturnable<EntityDispatcher.EntityDispatcherEntry<?>> cir) {
		FRLEntitySubDispatcher.EntitySubDispatcherEntry<?> subDispatcherEntry =
			FRLEntitySubDispatcher.getSubDispatcher(id);
		if (subDispatcherEntry != null) {
			cir.setReturnValue(subDispatcherEntry);
		}
	}

	@Inject(method = "entryForClass",
		at = @At("HEAD"), cancellable = true)
	public <T extends Entity> void onGetEntryFromClass(
		@Nullable Class<T> entityClass, CallbackInfoReturnable<EntityDispatcher.EntityDispatcherEntry<T>> cir) {
		FRLEntitySubDispatcher.EntitySubDispatcherEntry<T> subDispatcherEntry =
			FRLEntitySubDispatcher.getSubDispatcher(entityClass);
		if (subDispatcherEntry != null) {
			cir.setReturnValue(subDispatcherEntry);
		}
	}

	@Inject(method = "createEntityFromNBT", at = @At("HEAD"), cancellable = true)
	public void onCreateEntityFromNBT(
		CompoundTag compoundTag, World world, CallbackInfoReturnable<Entity> cir) {
		FRLEntitySubDispatcher.EntitySubDispatcherEntry<?> subDispatcherEntry =
			FRLEntitySubDispatcher.getSubDispatcher(compoundTag.getStringOrDefault("frlSubId", "???"));
		Entity entity;
		if (subDispatcherEntry == null ||
			(entity = subDispatcherEntry.factory.createEntity(world)) == null) {
			return;
		}
		entity.load(compoundTag);
		cir.setReturnValue(entity);
	}
}
