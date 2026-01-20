package com.scytheclient.itemtrails.client;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.scytheclient.itemtrails.mixin.client.PersistentProjectileEntityAccessor;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class ItemtrailsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("itemtrail")
                    .then(ClientCommandManager.argument("entity_id", IdentifierArgumentType.identifier())
                            .suggests((context, builder) -> CommandSource.suggestMatching(Registries.ENTITY_TYPE.getIds().stream().map(Identifier::toString), builder))
                            .then(ClientCommandManager.literal("enable")
                                    .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                            .executes(context -> {
                                                Identifier entityId = context.getArgument("entity_id", Identifier.class);
                                                boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                                ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
                                                
                                                ModConfig.TrailConfig trailConfig = config.trails.computeIfAbsent(entityId.toString(), k -> new ModConfig.TrailConfig());
                                                trailConfig.enabled = enabled;
                                                
                                                AutoConfig.getConfigHolder(ModConfig.class).save();
                                                context.getSource().sendFeedback(Text.literal("Set trail enabled for " + entityId + " to " + enabled));
                                                return 1;
                                            })))
                            .then(ClientCommandManager.literal("particle")
                                    .then(ClientCommandManager.argument("particle_id", IdentifierArgumentType.identifier())
                                            .suggests((context, builder) -> CommandSource.suggestMatching(Registries.PARTICLE_TYPE.getIds().stream().map(Identifier::toString), builder))
                                            .executes(context -> {
                                                Identifier entityId = context.getArgument("entity_id", Identifier.class);
                                                Identifier particleId = context.getArgument("particle_id", Identifier.class);
                                                
                                                if (!Registries.PARTICLE_TYPE.containsId(particleId)) {
                                                    context.getSource().sendError(Text.literal("Invalid particle ID: " + particleId));
                                                    return 0;
                                                }

                                                ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
                                                ModConfig.TrailConfig trailConfig = config.trails.computeIfAbsent(entityId.toString(), k -> new ModConfig.TrailConfig());
                                                trailConfig.particleId = particleId.toString();
                                                
                                                AutoConfig.getConfigHolder(ModConfig.class).save();
                                                context.getSource().sendFeedback(Text.literal("Set trail particle for " + entityId + " to " + particleId));
                                                return 1;
                                            })))));
        });

        ClientTickEvents.END_WORLD_TICK.register(world -> {
            if (world == null) return;
            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
            
            for (Entity entity : world.getEntities()) {
                if (entity instanceof ProjectileEntity) {
                    // Check if the projectile is on the ground
                    if (entity.isOnGround()) {
                        continue;
                    }
                    // For arrows and tridents (PersistentProjectileEntity), check inGround flag as well
                    if (entity instanceof PersistentProjectileEntity) {
                         // Use the accessor to check the protected inGround field
                         if (((PersistentProjectileEntityAccessor) entity).getInGround()) {
                             continue;
                         }
                    } else {
                        // For other projectiles like snowballs, they die on impact usually, but if they linger:
                        if (entity.getVelocity().lengthSquared() < 0.0001) {
                            continue;
                        }
                    }

                    String entityId = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
                    ModConfig.TrailConfig trailConfig = config.trails.get(entityId);
                    
                    if (trailConfig != null && trailConfig.enabled) {
                        try {
                            Identifier particleId = Identifier.of(trailConfig.particleId);
                            if (Registries.PARTICLE_TYPE.containsId(particleId)) {
                                 ParticleType<?> particleType = Registries.PARTICLE_TYPE.get(particleId);
                                 if (particleType instanceof ParticleEffect) {
                                     // Calculate position behind the projectile
                                     Vec3d velocity = entity.getVelocity();
                                     double x = entity.getX();
                                     double y = entity.getY();
                                     double z = entity.getZ();
                                     
                                     if (velocity.lengthSquared() > 0.0001) {
                                         Vec3d normalizedVelocity = velocity.normalize();
                                         // Offset slightly behind the entity based on velocity
                                         x -= normalizedVelocity.x * 0.5;
                                         y -= normalizedVelocity.y * 0.5;
                                         z -= normalizedVelocity.z * 0.5;
                                     }

                                     // Special handling for note particles to make them colorful
                                     if (particleType == ParticleTypes.NOTE) {
                                         // Random color (0 to 1)
                                         double noteColor = world.random.nextDouble();
                                         world.addParticle((ParticleEffect) particleType, x, y, z, noteColor, 0, 0);
                                     } else {
                                         world.addParticle((ParticleEffect) particleType, x, y, z, 0, 0, 0);
                                     }
                                 }
                            }
                        } catch (Exception ignored) {
                            // Invalid identifier in config
                        }
                    }
                }
            }
        });
    }
}
