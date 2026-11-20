package com.example.toggleinteract.mixin;

import com.example.toggleinteract.ToggleInteractClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends ClientInput {
    @Inject(method = "tick", at = @At("TAIL"))
    private void toggleinteract$forceShift(CallbackInfo ci) {
        if (!ToggleInteractClient.disabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        if (!(mc.hitResult instanceof BlockHitResult)) return;
        if (mc.player.getMainHandItem().isEmpty()) return;
        if (!mc.options.keyUse.isDown()) return;

        Input p = this.keyPresses;
        this.keyPresses = new Input(p.forward(), p.backward(), p.left(),
                p.right(), p.jump(), true, p.sprint());
    }
}
