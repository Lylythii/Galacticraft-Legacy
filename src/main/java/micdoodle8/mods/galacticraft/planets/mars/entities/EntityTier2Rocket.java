/*
 * Copyright (c) 2023 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */

package micdoodle8.mods.galacticraft.planets.mars.entities;

import java.util.List;
import java.util.Random;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityTieredRocket;
import micdoodle8.mods.galacticraft.api.tile.IFuelDock;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.Constants;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import micdoodle8.mods.galacticraft.core.tile.TileEntityLandingPad;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;
import micdoodle8.mods.galacticraft.planets.mars.items.MarsItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityTier2Rocket extends EntityTieredRocket
{

    public EntityTier2Rocket(World par1World)
    {
        super(par1World);
        this.setSize(1.2F, 4.5F);
    }

    public EntityTier2Rocket(World par1World, double par2, double par4, double par6, EnumRocketType rocketType)
    {
        super(par1World, par2, par4, par6);
        this.rocketType = rocketType;
        this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        this.setSize(1.2F, 4.5F);
    }

    public EntityTier2Rocket(World par1World, double par2, double par4, double par6, boolean reversed, EnumRocketType rocketType, NonNullList<ItemStack> inv)
    {
        this(par1World, par2, par4, par6, rocketType);
        this.stacks = inv;
    }

    @Override
    public double getYOffset()
    {
        return 1.5F;
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult target)
    {
        return new ItemStack(MarsItems.rocketMars, 1, this.rocketType.getIndex());
    }

    @Override
    public double getMountedYOffset()
    {
        return 1.6D;
    }

    @Override
    public float getRotateOffset()
    {
        return 1.25F;
    }

    @Override
    public double getOnPadYOffset()
    {
        return -0.2D;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        int i;

        if (this.timeUntilLaunch >= 100)
        {
            i = Math.abs(this.timeUntilLaunch / 100);
        } else
        {
            i = 1;
        }

        if ((this.getLaunched() || this.launchPhase == EnumLaunchPhase.IGNITED.ordinal() && this.rand.nextInt(i) == 0) && !ConfigManagerCore.disableSpaceshipParticles && this.hasValidFuel())
        {
            if (this.world.isRemote)
            {
                this.spawnParticles(this.getLaunched());
            }
        }

        if (this.launchPhase >= EnumLaunchPhase.LAUNCHED.ordinal() && this.hasValidFuel())
        {
            if (this.launchPhase == EnumLaunchPhase.LAUNCHED.ordinal())
            {
                double d = this.timeSinceLaunch / 150;

                if (this.world.provider instanceof IGalacticraftWorldProvider && ((IGalacticraftWorldProvider) this.world.provider).hasNoAtmosphere())
                {
                    d = Math.min(d * 1.2, 1.8);
                } else
                {
                    d = Math.min(d, 1.2);
                }

                if (d != 0.0)
                {
                    this.motionY = -d * 2.0D * Math.cos((this.rotationPitch - 180) / Constants.RADIANS_TO_DEGREES_D);
                }
            } else
            {
                this.motionY -= 0.008D;
            }

            double multiplier = 1.0D;

            if (this.world.provider instanceof IGalacticraftWorldProvider)
            {
                multiplier = ((IGalacticraftWorldProvider) this.world.provider).getFuelUsageMultiplier();

                if (multiplier <= 0)
                {
                    multiplier = 1;
                }
            }

            if (this.timeSinceLaunch % MathHelper.floor(2 * (1 / multiplier)) == 0)
            {
                this.removeFuel(1);
                if (!this.hasValidFuel())
                {
                    this.stopRocketSound();
                }
            }
        } else if (!this.hasValidFuel() && this.getLaunched() && !this.world.isRemote)
        {
            if (Math.abs(Math.sin(this.timeSinceLaunch / 1000)) / 10 != 0.0)
            {
                this.motionY -= Math.abs(Math.sin(this.timeSinceLaunch / 1000)) / 20;
            }
        }
    }

    @Override
    public void onTeleport(EntityPlayerMP player)
    {
        EntityPlayerMP playerBase = PlayerUtil.getPlayerBaseServerFromPlayer(player, false);

        if (playerBase != null)
        {
            GCPlayerStats stats = GCPlayerStats.get(playerBase);

            if (this.stacks == null || this.stacks.size() == 0)
            {
                stats.setRocketStacks(NonNullList.withSize(2, ItemStack.EMPTY));
            } else
            {
                stats.setRocketStacks(this.stacks);
            }

            stats.setRocketType(this.rocketType.getIndex());
            stats.setRocketItem(MarsItems.rocketMars);
            stats.setFuelLevel(this.fuelTank.getFluidAmount());
        }
    }

    protected void spawnParticles(boolean launched)
    {
        if (!this.isDead)
        {
            double sinPitch = Math.sin(this.rotationPitch / Constants.RADIANS_TO_DEGREES_D);
            double x1 = 2.9 * Math.cos(this.rotationYaw / Constants.RADIANS_TO_DEGREES_D) * sinPitch;
            double z1 = 2.9 * Math.sin(this.rotationYaw / Constants.RADIANS_TO_DEGREES_D) * sinPitch;
            double y1 = 2.9 * Math.cos((this.rotationPitch - 180) / Constants.RADIANS_TO_DEGREES_D);
            if (this.launchPhase == EnumLaunchPhase.LANDING.ordinal() && this.targetVec != null)
            {
                double modifier = this.posY - this.targetVec.getY();
                modifier = Math.min(Math.max(modifier, 80.0), 200.0);
                x1 *= modifier / 100.0D;
                y1 *= modifier / 100.0D;
                z1 *= modifier / 100.0D;
            }

            final double y = this.prevPosY + (this.posY - this.prevPosY) + y1 - this.motionY + (!this.getLaunched() ? 2.5D : 1D);
            ;

            final double x2 = this.posX + x1 - this.motionX;
            final double z2 = this.posZ + z1 - this.motionZ;
            final double x3 = x2 + x1 / 2D;
            final double y3 = y + y1 / 2D;
            final double z3 = z2 + z1 / 2D;
            Vector3 motionVec = new Vector3(x1, y1, z1);

            if (this.ticksExisted % 2 == 0 && !this.getLaunched())
                return;

            String flame = this.getLaunched() ? "launchFlameLaunched" : "launchFlameIdle";

            EntityLivingBase riddenByEntity = this.getPassengers().isEmpty() || !(this.getPassengers().get(0) instanceof EntityLivingBase) ? null : (EntityLivingBase) this.getPassengers().get(0);
            Object[] rider = new Object[]
            {riddenByEntity};
            Object[] none = new Object[]
            {};
            Random random = this.rand;
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x2 + 0.4 - random.nextDouble() / 10D, y, z2 + 0.4 - random.nextDouble() / 10D), motionVec, rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x2 - 0.4 + random.nextDouble() / 10D, y, z2 + 0.4 - random.nextDouble() / 10D), motionVec, rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x2 - 0.4 + random.nextDouble() / 10D, y, z2 - 0.4 + random.nextDouble() / 10D), motionVec, rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x2 + 0.4 - random.nextDouble() / 10D, y, z2 - 0.4 + random.nextDouble() / 10D), motionVec, rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x2, y, z2), motionVec, rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x2 + 0.4, y, z2), motionVec, rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x2 - 0.4, y, z2), motionVec, rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x2, y, z2 + 0.4D), motionVec, rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x2, y, z2 - 0.4D), motionVec, rider);
            // Larger flameball for T2 - positioned behind the smaller one
            double a = 4D;
            double bx = motionVec.x + 0.5D / a;
            double bz = motionVec.z + 0.5D / a;
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 + 0.2 - random.nextDouble() / 6D, y3 + 0.4, z3 + 0.2 - random.nextDouble() / 6D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 - 0.2 + random.nextDouble() / 6D, y3 + 0.4, z3 + 0.2 - random.nextDouble() / 6D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 - 0.2 + random.nextDouble() / 6D, y3 + 0.4, z3 - 0.2 + random.nextDouble() / 6D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 + 0.2 - random.nextDouble() / 6D, y3 + 0.4, z3 - 0.2 + random.nextDouble() / 6D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 + 0.2 - random.nextDouble() / 6D, y3 - 0.4, z3 + 0.2 - random.nextDouble() / 6D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 - 0.2 + random.nextDouble() / 6D, y3 - 0.4, z3 + 0.2 - random.nextDouble() / 6D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 - 0.2 + random.nextDouble() / 6D, y3 - 0.4, z3 - 0.2 + random.nextDouble() / 6D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 + 0.2 - random.nextDouble() / 6D, y3 - 0.4, z3 - 0.2 + random.nextDouble() / 6D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 + 0.7 - random.nextDouble() / 8D, y3, z3 + 0.7 - random.nextDouble() / 8D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 - 0.7 + random.nextDouble() / 8D, y3, z3 + 0.7 - random.nextDouble() / 8D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 - 0.7 + random.nextDouble() / 8D, y3, z3 - 0.7 + random.nextDouble() / 8D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 + 0.7 - random.nextDouble() / 8D, y3, z3 - 0.7 + random.nextDouble() / 8D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 + 0.7 - random.nextDouble() / 8D, y3, z3 - random.nextDouble() / 8D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 - 0.7 + random.nextDouble() / 8D, y3, z3 - random.nextDouble() / 8D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 + random.nextDouble() / 8D, y3, z3 + 0.7 + random.nextDouble() / 8D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle(flame, new Vector3(x3 - random.nextDouble() / 8D, y3, z3 - 0.7 + random.nextDouble() / 8D),
                new Vector3(bx - random.nextDouble() / a, motionVec.y, bz - random.nextDouble() / a), rider);
            GalacticraftCore.proxy.spawnParticle("blueflame", new Vector3(x2 - 0.8, y, z2), motionVec, none);
            GalacticraftCore.proxy.spawnParticle("blueflame", new Vector3(x2 + 0.8, y, z2), motionVec, none);
            GalacticraftCore.proxy.spawnParticle("blueflame", new Vector3(x2, y, z2 - 0.8), motionVec, none);
            GalacticraftCore.proxy.spawnParticle("blueflame", new Vector3(x2, y, z2 + 0.8), motionVec, none);
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer par1EntityPlayer)
    {
        return !this.isDead && par1EntityPlayer.getDistanceSq(this) <= 64.0D;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
    }

    @Override
    public boolean isDockValid(IFuelDock dock)
    {
        return dock instanceof TileEntityLandingPad;
    }

    @Override
    public int getRocketTier()
    {
        return 2;
    }

    @Override
    public int getFuelTankCapacity()
    {
        return 1500;
    }

    @Override
    public int getPreLaunchWait()
    {
        return 400;
    }

    @Override
    public float getCameraZoom()
    {
        return 15.0F;
    }

    @Override
    public boolean defaultThirdPerson()
    {
        return true;
    }

    @Override
    public List<ItemStack> getItemsDropped(List<ItemStack> droppedItems)
    {
        super.getItemsDropped(droppedItems);
        ItemStack rocket = new ItemStack(MarsItems.rocketMars, 1, this.rocketType.getIndex());
        rocket.setTagCompound(new NBTTagCompound());
        rocket.getTagCompound().setInteger("RocketFuel", this.fuelTank.getFluidAmount());
        droppedItems.add(rocket);
        return droppedItems;
    }

    @Override
    public float getRenderOffsetY()
    {
        return -0.1F;
    }
}
