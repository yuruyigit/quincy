package com.protocol7.nettyquick.streams;

import com.google.common.collect.Sets;
import com.protocol7.nettyquick.protocol.PacketNumber;

import java.util.Optional;
import java.util.Set;

import static com.protocol7.nettyquick.streams.SendStateMachine.SendStreamState.*;

public class SendStateMachine {

  public SendStreamState getState() {
    return state;
  }

  public enum SendStreamState {
    Open,
    Send,
    DataSent,
    ResetSent,
    DataRecvd,
    ResetRecvd
  }

  private SendStreamState state = Open;
  private final Set<PacketNumber> outstandingStreamPackets = Sets.newConcurrentHashSet();
  private Optional<PacketNumber> outstandingResetPacket = Optional.empty();

  public void onStream(PacketNumber pn, boolean fin) {
    if (state == Open || state == Send) {
      if (fin) {
        state = DataSent;
      } else {
        state = Send;
      }
    } else {
      throw new IllegalStateException();
    }
  }

  public void onReset(PacketNumber pn) {
    if (state == Open || state == Send || state == DataSent) {
      state = ResetSent;
      outstandingResetPacket = Optional.of(pn);
    } else {
      throw new IllegalStateException();
    }
  }

  public void onAck(PacketNumber pn) {
    outstandingStreamPackets.remove(pn);

    if (state == DataSent && outstandingStreamPackets.isEmpty()) {
      state = DataRecvd;
    } else if (state == ResetSent) {
      if (outstandingResetPacket.isPresent() && outstandingResetPacket.get().equals(pn)) {
        state = ResetRecvd;
      } else {
        throw new IllegalStateException();
      }
    }
  }

  public boolean canSend() {
    return state == Open || state == Send;
  }

  public boolean canReset() {
    return state == Open || state == Send || state == DataSent;
  }
}
