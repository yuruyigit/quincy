package com.protocol7.nettyquick.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.protocol7.nettyquick.protocol.LongPacket;
import com.protocol7.nettyquick.protocol.PacketType;
import com.protocol7.nettyquick.protocol.Packet;
import com.protocol7.nettyquick.protocol.frames.Frame;
import com.protocol7.nettyquick.protocol.frames.StreamFrame;
import com.protocol7.nettyquick.protocol.packets.InitialPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientStateMachine {

  private final Logger log = LoggerFactory.getLogger(ClientStateMachine.class);

  private enum ClientState {
    BeforeInitial,
    InitialSent,
    Ready
  }

  private ClientState state = ClientState.BeforeInitial;
  private final ClientConnection connection;
  private final CompletableFuture<Void> handshakeFuture = new CompletableFuture();

  public ClientStateMachine(final ClientConnection connection) {
    this.connection = connection;
  }

  public CompletionStage<Void> handshake() {
    synchronized (this) {
      // send initial packet
      if (state == ClientState.BeforeInitial) {
        connection.sendPacket(InitialPacket.create(connection.getConnectionId()));
        state = ClientState.InitialSent;
        log.info("Client connection state inital sent");
      } else {
        throw new IllegalStateException("Can't handshake in state " + state);
      }
    }
    return handshakeFuture;
  }

  public void processPacket(Packet packet) {
    log.info("Client got {} in state {} with connection ID {}", packet.getPacketType(), state, packet.getConnectionId());

    synchronized (this) {
      // TODO validate connection ID
      if (packet.getPacketType() == PacketType.Handshake) {
        if (state == ClientState.InitialSent) {
          state = ClientState.Ready;
          handshakeFuture.complete(null);
          log.info("Client connection state ready");
        } else {
          log.warn("Got Handshake packet in an unexpected state");
        }
      } else if (state == ClientState.Ready) {
        for (Frame frame : packet.getPayload().getFrames()) {
          if (frame instanceof StreamFrame) {
            StreamFrame sf = (StreamFrame) frame;

            ClientStream stream = connection.getOrCreateStream(sf.getStreamId());
            stream.onData(sf.getData());
          }
        }
      } else {
        log.warn("Got packet in an unexpected state");
      }
    }
  }

}
