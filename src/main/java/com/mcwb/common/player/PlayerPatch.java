package com.mcwb.common.player;

import javax.annotation.Nullable;

import com.mcwb.common.MCWB;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.operation.IOperation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 * Additional patch that added to the player to do extra logic required by {@link MCWB}
 * 
 * @author Giant_Salted_Fish
 */
public class PlayerPatch implements ICapabilityProvider
{
	@CapabilityInject( PlayerPatch.class )
	private static final Capability< PlayerPatch > CAPABILITY = null;
	
	/**
	 * Host player of this patch
	 */
	protected final EntityPlayer player;
	
	/**
	 * Operation that is currently executing. {@link IOperation#NONE} if is idle.
	 */
	protected IOperation executing = IOperation.NONE;
	
	/// *** Stuffs for main hand item *** ///
	protected ItemStack mainStack = ItemStack.EMPTY;
	protected int mainStackId = IItem.VANILLA.stackId();
	protected IEquippedItem< ? > mainEquipped = IEquippedItem.VANILLA;
	
	/// *** Stuffs for off-hand item *** ///
	protected ItemStack offStack = ItemStack.EMPTY;
	protected int offStackId = IItem.VANILLA.stackId();
	protected IEquippedItem< ? > offEquipped = IEquippedItem.VANILLA;
	
	public PlayerPatch( EntityPlayer player ) { this.player = player; }
	
	public void tick()
	{
		final InventoryPlayer inv = this.player.inventory;
		
		/// *** Main hand stuff *** ///
		{
			final EnumHand hand = EnumHand.MAIN_HAND;
			final ItemStack stack = inv.getCurrentItem();
			final IItem item = IItemTypeHost.getItemOrDefault( stack );
			final int stackId = item.stackId();
			
			if( stackId != this.mainStackId )
			{
				this.mainStackId = stackId;
				this.mainEquipped = item.onTakeOut( this.mainEquipped, this.player, hand );
				this.executing = this.executing.onItemChange( this.mainEquipped );
			}
			else if( stack != this.mainStack )
			{
				this.mainStack = stack;
				this.mainEquipped = item.onStackUpdate( this.mainEquipped, this.player, hand );
				this.executing = this.executing.onStackUpdate( this.mainEquipped );
			}
			
			this.mainEquipped.tick( this.player, hand );
		}
		
		/// *** Off-hand stuff *** ///
		{
			final EnumHand hand = EnumHand.OFF_HAND;
			final ItemStack stack = inv.offHandInventory.get( 0 );
			final IItem item = IItemTypeHost.getItemOrDefault( stack );
			final int stackId = item.stackId();
			
			if( stackId != this.offStackId )
			{
				this.offStackId = stackId;
				this.offEquipped = item.onTakeOut( this.offEquipped, this.player, hand );
			}
			else if( stack != this.offStack )
			{
				this.offStack = stack;
				this.offEquipped = item.onStackUpdate( this.offEquipped, this.player, hand );
			}
			
			this.offEquipped.tick( this.player, hand );
		}
		
		this.executing = this.executing.tick();
	}
	
	public final IEquippedItem< ? > getEquipped( EnumHand hand ) {
		return hand == EnumHand.MAIN_HAND ? this.mainEquipped : this.offEquipped;
	}
	
	public final IOperation executing() { return this.executing; }
	
	public final IOperation tryLaunch( IOperation operation ) {
		return this.executing = this.executing.onOtherTryLaunch( operation );
	}
	
	public final IOperation ternimateExecuting() {
		return this.executing = this.executing.terminate();
	}
	
	public final IOperation toggleExecuting() { return this.executing = this.executing.toggle(); }
	
	@Override
	public final boolean hasCapability( Capability< ? > capability, @Nullable EnumFacing facing ) {
		return capability == CAPABILITY;
	}
	
	@Override
	public final < T > T getCapability( Capability< T > capability, @Nullable EnumFacing facing ) {
		return CAPABILITY.cast( this );
	}
	
	public static PlayerPatch get( EntityPlayer player ) {
		return player.getCapability( CAPABILITY, null );
	}
}
