package dev.stephan.kingdommod.mixin;

import dev.stephan.kingdommod.ability.Ability;
import dev.stephan.kingdommod.ability.AbilityManager;
import dev.stephan.kingdommod.ability.TriggerType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class ArrowHitMixin {

    @Inject(method = "onEntityHit", at = @At("HEAD"))
    private void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        Entity hitEntity = entityHitResult.getEntity();
        System.out.println(hitEntity);
        PersistentProjectileEntity arrow = (PersistentProjectileEntity)(Object)this;

        MinecraftClient client = MinecraftClient.getInstance();
        if (arrow.getOwner() == client.player) {
            if (client.world == null || client.player == null) return;
            ItemStack held = client.player.getMainHandStack();
            for (Ability ability : AbilityManager.ABILITIES) {
                if (ability.trigger != TriggerType.SHOOT_BOW) continue;
                if (!AbilityManager.hasAbility(held, ability.abilityName)) continue;
                ability.tryActivate();
            }
        }
    }
}
