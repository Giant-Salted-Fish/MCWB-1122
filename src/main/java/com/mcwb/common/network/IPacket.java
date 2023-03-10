package com.mcwb.common.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPacket extends IMessage
{
	public default void handleServerSide( MessageContext ctx ) { }
	
	@SideOnly( Side.CLIENT )
	public default void handleClientSide( MessageContext ctx ) { }
}
