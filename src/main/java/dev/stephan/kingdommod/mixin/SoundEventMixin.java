package dev.stephan.kingdommod.mixin;

import dev.stephan.kingdommod.ability.Ability;
import dev.stephan.kingdommod.ability.AbilityManager;
import dev.stephan.kingdommod.ability.TriggerType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class SoundEventMixin {

    @Inject(method = "onPlaySound", at = @At("HEAD"))
    private void onPlaySound(PlaySoundS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String soundId = packet.getSound().getIdAsString();

        if (!soundId.equals("minecraft:entity.arrow.hit")) return;

        Vec3d soundPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());

        boolean wasOurArrow = AbilityManager.trackedArrows.values().stream().anyMatch(box -> box.contains(soundPos));
        if (!wasOurArrow) return;
        if (client.world == null) return;

        Box searchArea = new Box(soundPos.x - 3, soundPos.y - 3, soundPos.z - 3,
                soundPos.x + 3, soundPos.y + 3, soundPos.z + 3);

        boolean hitLivingEntity = client.world.getEntitiesByClass(
                LivingEntity.class, searchArea, e -> e != client.player
        ).stream().anyMatch(e -> {
            Vec3d pos = e.getPos();
            double dx = pos.x - soundPos.x;
            double dz = pos.z - soundPos.z;
            double xzDist = dx * dx + dz * dz;
            double width = e.getWidth();
            double threshold = (width * width);
            return xzDist < Math.max(1.0, threshold);
        });

        if (hitLivingEntity) {
            ItemStack held = client.player.getMainHandStack();
            for (Ability ability : AbilityManager.ABILITIES) {
                if (ability.trigger != TriggerType.SHOOT_BOW) continue;
                if (!AbilityManager.hasAbility(held, ability.abilityName)) continue;
                ability.tryActivate();
            }
        }
    }
}