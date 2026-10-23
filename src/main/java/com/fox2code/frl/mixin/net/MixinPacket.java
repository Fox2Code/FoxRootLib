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

import com.fox2code.frl.network.ext.PacketCustomPayloadInbound;
import com.fox2code.frl.network.ext.PacketCustomPayloadOutbound;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.net.packet.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Packet.class)
public class MixinPacket {
	@Shadow
	@Final
	private static Int2ObjectMap<Class<? extends Packet>> packetIdToClassMap;

	@Shadow
	@Final
	private static Object2IntMap<Class<? extends Packet>> packetClassToIdMap;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void onClInit(CallbackInfo ci) {
		// Use our custom logic to handle payload packets to
		// migrate load from the server thread to the network thread
		packetIdToClassMap.put(250, PacketCustomPayloadInbound.class);
		packetClassToIdMap.put(PacketCustomPayloadInbound.class, 250);
		packetClassToIdMap.put(PacketCustomPayloadOutbound.class, 250);
	}
}
