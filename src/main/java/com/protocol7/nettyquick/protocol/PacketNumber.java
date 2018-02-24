package com.protocol7.nettyquick.protocol;

import java.util.Arrays;

import com.google.common.primitives.Longs;
import com.protocol7.nettyquick.utils.Bytes;
import com.protocol7.nettyquick.utils.Rnd;
import io.netty.buffer.ByteBuf;

public class PacketNumber implements Comparable<PacketNumber> {

  public static PacketNumber random() {
    return new PacketNumber(Rnd.rndLong(0, Varint.MAX));
  }

  public static PacketNumber read(final ByteBuf bb) {
    return new PacketNumber(bb.readLong());
  }

  private static byte[] pad(byte[] b) {
    return Bytes.concat(new byte[8-b.length], b);
  }

  public static PacketNumber read4(final ByteBuf bb) {
    return readn(bb, 4);
  }

  public static PacketNumber read2(final ByteBuf bb) {
    return readn(bb, 2);
  }

  public static PacketNumber read1(final ByteBuf bb) {
    return readn(bb, 1);
  }

  private static PacketNumber readn(final ByteBuf bb, int len) {
    byte[] b = new byte[len];
    bb.readBytes(b);
    return new PacketNumber(Longs.fromByteArray(pad(b)));
  }

  public static final PacketNumber MIN = new PacketNumber(0);

  private final Varint number;

  public PacketNumber(final long number) {
    this.number = new Varint(number);
  }

  public PacketNumber(final Varint number) {
    this.number = number;
  }

  public PacketNumber next() {
    return new PacketNumber(number.getValue() + 1);
  }

  public long asLong() {
    return number.getValue();
  }

  public Varint asVarint() {
    return number;
  }

  public void write(final ByteBuf bb) {
    bb.writeBytes(Longs.toByteArray(number.getValue()));
  }

  public void write4(final ByteBuf bb) {
    byte[] b = Longs.toByteArray(number.getValue());
    bb.writeBytes(Arrays.copyOfRange(b, 4, 8));
  }

  public void write2(final ByteBuf bb) {
    byte[] b = Longs.toByteArray(number.getValue());
    bb.writeBytes(Arrays.copyOfRange(b, 6, 8));
  }

  public void write1(final ByteBuf bb) {
    byte[] b = Longs.toByteArray(number.getValue());
    bb.writeBytes(Arrays.copyOfRange(b, 7, 8));
  }

  public void writeVarint(final ByteBuf bb) {
    asVarint().write(bb);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final PacketNumber that = (PacketNumber) o;

    return number.equals(that.number);
  }

  @Override
  public int hashCode() {
    return number.hashCode();
  }

  @Override
  public int compareTo(final PacketNumber o) {
    return this.number.compareTo(o.number);
  }

  @Override
  public String toString() {
    return number.toString();
  }
}
