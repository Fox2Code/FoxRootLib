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
package com.fox2code.frl.transform;

import com.fox2code.frl.transform.api.FRLPatchBlocksLength;
import com.fox2code.frl.transform.api.FRLPatchPlayerInstanceof;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

import java.lang.annotation.Annotation;

final class FRLMixinExtension implements IExtension, FRLTransformConstants, Opcodes {
	private static final int PATCHED_BLOCKS_LENGTH = 13999;

	@Override
	public boolean checkActive(MixinEnvironment environment) {
		return true;
	}

	@Override
	public void preApply(ITargetClassContext context) {}

	@Override
	public void postApply(ITargetClassContext context) {
		ClassNode classNode = context.getClassNode();
		if (hasAnnotation(classNode, FRLPatchBlocksLength.class)) {
			classNode.methods.forEach(FRLMixinExtension::patchBlocksLength);
		}
		if (hasAnnotation(classNode, FRLPatchPlayerInstanceof.class)) {
			classNode.methods.forEach(FRLMixinExtension::patchPlayerInstanceof);
		}
	}

	private static void patchBlocksLength(MethodNode methodNode) {
		if ((methodNode.access & Opcodes.ACC_ABSTRACT) != 0) return;
		AbstractInsnNode cur = methodNode.instructions.getFirst();
		do {
			AbstractInsnNode next;
			AbstractInsnNode end;
			if (cur.getOpcode() == Opcodes.GETSTATIC &&
				(next = cur.getNext()) != null &&
				next.getOpcode() == Opcodes.ARRAYLENGTH &&
				((FieldInsnNode) cur).owner.equals(Block) &&
				((FieldInsnNode) cur).name.equals("blocksList") &&
				(end = next.getNext()) != null) {
				methodNode.instructions.remove(cur);
				methodNode.instructions.remove(next);
				methodNode.instructions.insertBefore(end,
					FRLTransformHelper.getNumberInsn(PATCHED_BLOCKS_LENGTH));
				cur = end;
			}
		} while ((cur = cur.getNext()) != null);
	}

	private static void patchPlayerInstanceof(MethodNode methodNode) {
		if ((methodNode.access & Opcodes.ACC_ABSTRACT) != 0) return;
		AbstractInsnNode next = methodNode.instructions.getFirst();
		do {
			AbstractInsnNode cur = next;
			next = cur.getNext();
			if (cur.getOpcode() == Opcodes.INSTANCEOF &&
				cur instanceof TypeInsnNode typeInsnNode &&
				typeInsnNode.desc.equals(Player)) {
				methodNode.instructions.insertBefore(typeInsnNode,
					new MethodInsnNode(Opcodes.INVOKESTATIC,
						"com/fox2code/frl/utils/FRLSanityHelper",
						"isRealPlayer", "(L" + Entity + ";)Z", false));
				methodNode.instructions.remove(cur);
			}
		} while (next != null);
	}

	@Override
	public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {

	}

	private static boolean hasAnnotation(ClassNode classNode, Class<? extends Annotation> cls) {
		final String asmName = "L" + cls.getName().replace('.', '/') + ";";
		return classNode.invisibleAnnotations.stream().anyMatch(
			annotationNode -> asmName.equals(annotationNode.desc));
	}
}
