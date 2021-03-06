package com.protocol7.testcontainers.quicgo;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.protocol7.quincy.utils.Hex;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class QuicGoPacketParserTest {

  @Test
  public void parse() {
    final List<String> logs =
        List.of(
            "server <- Reading packet 0x2 (92 bytes) for connection 0x142de1af, Handshake\n",
            "server \tLong Header{Type: Handshake, DestConnectionID: 0x142de1af, SrcConnectionID: 0x5fec16f0569563feb5572630227285, PacketNumber: 0x2, PacketNumberLen: 4, Length: 65, Version: TLS dev version (WIP)}\n",
            "server \t<- &wire.CryptoFrame{Offset: 0x0, Data length: 0x24, Offset + Data length: 0x24}\n",
            "server -> Sending packet 0x2 (37 bytes) for connection 0x7245c5be, 1-RTT\n",
            "server \tShort Header{DestConnectionID: 0x5551cb7767435e519f01c1ea, PacketNumber: 0x2, PacketNumberLen: 2, KeyPhase: 0}");

    final List<QuicGoPacket> packets = QuicGoPacketParser.parse(logs);

    assertEquals(2, packets.size());
    final QuicGoPacket p1 = packets.get(0);
    assertEquals(true, p1.inbound);
    assertTrue(p1.longHeader);
    assertArrayEquals(Hex.dehex("142de1af"), p1.destinationConnectionId);
    assertArrayEquals(Hex.dehex("5fec16f0569563feb5572630227285"), p1.sourceConnectionId);
    assertEquals(2, p1.packetNumber);

    final QuicGoPacket p2 = packets.get(1);
    Assert.assertFalse(p2.inbound);
    Assert.assertFalse(p2.longHeader);
    assertArrayEquals(Hex.dehex("5551cb7767435e519f01c1ea"), p2.destinationConnectionId);
    assertNull(p2.sourceConnectionId);
    assertEquals(2, p2.packetNumber);
  }
}
