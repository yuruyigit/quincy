package com.protocol7.nettyquick.server;

import com.protocol7.nettyquick.protocol.Packet;
import com.protocol7.nettyquick.streams.StreamListener;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.slf4j.MDC;


public class ServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

  private final Connections connections = new Connections();
  private final StreamListener streamHandler;

  public ServerHandler(final StreamListener streamHandler) {
    this.streamHandler = streamHandler;
  }

  @Override
  protected void channelRead0(final ChannelHandlerContext ctx, final DatagramPacket datagram) throws Exception {
    final ByteBuf bb = datagram.content();

    Packet packet = Packet.parse(bb);

    MDC.put("actor", "server");
    MDC.put("packetnumber", packet.getPacketNumber().toString());
    MDC.put("connectionid", packet.getConnectionId().toString());

    ServerConnection conn = connections.getOrCreate(packet.getConnectionId(), streamHandler, ctx.channel(), datagram.sender()); // TODO fix for when connId is omitted

    conn.onPacket(packet);
  }
}