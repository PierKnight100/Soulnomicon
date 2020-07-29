package com.pier.snom.client.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class SoulFlameParticle extends SpriteTexturedParticle
{
    private final UUID soulPlayerUUID;
    private final IAnimatedSprite animatedSprite;

    private SoulFlameParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, @Nullable UUID soulPlayerUUID, IAnimatedSprite animatedSprite)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.motionX = this.motionX * (double) 0.01F + xSpeedIn;
        this.motionY = this.motionY * (double) 0.01F + ySpeedIn;
        this.motionZ = this.motionZ * (double) 0.01F + zSpeedIn;
        this.posX += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
        this.posY += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
        this.posZ += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
        this.maxAge = 20 + rand.nextInt(15);
        this.setSize(0.15F, 0.15F);
        this.soulPlayerUUID = soulPlayerUUID;
        this.animatedSprite = animatedSprite;
        this.particleScale *= 0.6F;

    }

    @Nonnull
    public IParticleRenderType getRenderType()
    {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }


    public float getScale(float partialTicks)
    {
        float f = getScalingF(partialTicks);
        return this.particleScale * f;
    }


    private float getScalingF(float partialTicks)
    {
        float age = this.age + partialTicks;

        float ageTimeStamp = maxAge * 0.55F;

        if(age <= ageTimeStamp)
            return 1F;

        float ageDifference = age - ageTimeStamp;
        float maxAgeDifference = (float) maxAge - ageTimeStamp;

        return Math.max(0F, 1F - (ageDifference / maxAgeDifference));
    }


    public int getBrightnessForRender(float partialTick)
    {
        float f = ((float) this.age + partialTick) / (float) this.maxAge;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        int i = super.getBrightnessForRender(partialTick);
        int j = i & 255;
        int k = i >> 16 & 255;
        j = j + (int) (f * 15.0F * 16.0F);
        if(j > 240)
        {
            j = 240;
        }

        return j | k << 16;
    }

    public void tick()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if(this.age++ >= this.maxAge)
        {
            this.setExpired();
        }
        else
        {
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.96F;
            this.motionY *= 0.96F;
            this.motionZ *= 0.96F;
            this.setSprite(animatedSprite.get(age % 11, 10));
        }

    }

    @Override
    public void renderParticle(@Nonnull IVertexBuilder buffer,@Nonnull ActiveRenderInfo renderInfo, float partialTicks)
    {
        PlayerEntity player = Minecraft.getInstance().player;

        float f = getScalingF(partialTicks) * 0.65F;


        if(player != null && !renderInfo.isThirdPerson() && renderInfo.getRenderViewEntity().getUniqueID().equals(soulPlayerUUID))
        {
            Vec3d eyePos = player.getEyePosition(partialTicks);
            Vec3d particlePos = new Vec3d(posX, posY, posZ);
            float distanceFromParticle = (float) eyePos.distanceTo(particlePos);
            if(distanceFromParticle <= 0.6F)
                f *= distanceFromParticle / 1.2F;

        }


        this.particleAlpha = f;
        super.renderParticle(buffer, renderInfo, partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<SoulPlayerParticleData>
    {

        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite p_i50823_1_)
        {
            this.spriteSet = p_i50823_1_;
        }

        public Particle makeParticle(@Nonnull SoulPlayerParticleData typeIn, @Nonnull World worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
        {
            SoulFlameParticle flameParticle = new SoulFlameParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, typeIn.getSoulPlayerUUID(), this.spriteSet);
            flameParticle.selectSpriteWithAge(this.spriteSet);
            return flameParticle;
        }
    }
}