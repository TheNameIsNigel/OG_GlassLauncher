package com.google.common.net;

import android.support.v4.internal.view.SupportMenu;
import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;
import com.google.common.primitives.UnsignedBytes;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Nullable;

@Beta
public final class InetAddresses {
    /* access modifiers changed from: private */
    public static final Inet4Address ANY4 = ((Inet4Address) forString("0.0.0.0"));
    private static final int IPV4_PART_COUNT = 4;
    private static final int IPV6_PART_COUNT = 8;
    private static final Inet4Address LOOPBACK4 = ((Inet4Address) forString("127.0.0.1"));

    private InetAddresses() {
    }

    private static Inet4Address getInet4Address(byte[] bytes) {
        Preconditions.checkArgument(bytes.length == 4, "Byte array has invalid length for an IPv4 address: %s != 4.", Integer.valueOf(bytes.length));
        return (Inet4Address) bytesToInetAddress(bytes);
    }

    public static InetAddress forString(String ipString) {
        byte[] addr = ipStringToBytes(ipString);
        if (addr != null) {
            return bytesToInetAddress(addr);
        }
        throw new IllegalArgumentException(String.format("'%s' is not an IP string literal.", new Object[]{ipString}));
    }

    public static boolean isInetAddress(String ipString) {
        return ipStringToBytes(ipString) != null;
    }

    private static byte[] ipStringToBytes(String ipString) {
        boolean hasColon = false;
        boolean hasDot = false;
        for (int i = 0; i < ipString.length(); i++) {
            char c = ipString.charAt(i);
            if (c == '.') {
                hasDot = true;
            } else if (c == ':') {
                if (hasDot) {
                    return null;
                }
                hasColon = true;
            } else if (Character.digit(c, 16) == -1) {
                return null;
            }
        }
        if (hasColon) {
            if (!hasDot || (ipString = convertDottedQuadToHex(ipString)) != null) {
                return textToNumericFormatV6(ipString);
            }
            return null;
        } else if (hasDot) {
            return textToNumericFormatV4(ipString);
        } else {
            return null;
        }
    }

    private static byte[] textToNumericFormatV4(String ipString) {
        String[] address = ipString.split("\\.", 5);
        if (address.length != 4) {
            return null;
        }
        byte[] bytes = new byte[4];
        int i = 0;
        while (i < bytes.length) {
            try {
                bytes[i] = parseOctet(address[i]);
                i++;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return bytes;
    }

    private static byte[] textToNumericFormatV6(String ipString) {
        int partsLo;
        int partsHi;
        boolean z = true;
        String[] parts = ipString.split(":", 10);
        if (parts.length < 3 || parts.length > 9) {
            return null;
        }
        int skipIndex = -1;
        for (int i = 1; i < parts.length - 1; i++) {
            if (parts[i].length() == 0) {
                if (skipIndex >= 0) {
                    return null;
                }
                skipIndex = i;
            }
        }
        if (skipIndex >= 0) {
            partsHi = skipIndex;
            partsLo = (parts.length - skipIndex) - 1;
            if (parts[0].length() == 0 && partsHi - 1 != 0) {
                return null;
            }
            if (parts[parts.length - 1].length() == 0 && partsLo - 1 != 0) {
                return null;
            }
        } else {
            partsHi = parts.length;
            partsLo = 0;
        }
        int partsSkipped = 8 - (partsHi + partsLo);
        if (skipIndex < 0 ? partsSkipped != 0 : partsSkipped < 1) {
            z = false;
        }
        if (!z) {
            return null;
        }
        ByteBuffer rawBytes = ByteBuffer.allocate(16);
        int i2 = 0;
        while (i2 < partsHi) {
            try {
                rawBytes.putShort(parseHextet(parts[i2]));
                i2++;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        for (int i3 = 0; i3 < partsSkipped; i3++) {
            rawBytes.putShort(0);
        }
        for (int i4 = partsLo; i4 > 0; i4--) {
            rawBytes.putShort(parseHextet(parts[parts.length - i4]));
        }
        return rawBytes.array();
    }

    private static String convertDottedQuadToHex(String ipString) {
        int lastColon = ipString.lastIndexOf(58);
        String initialPart = ipString.substring(0, lastColon + 1);
        byte[] quad = textToNumericFormatV4(ipString.substring(lastColon + 1));
        if (quad == null) {
            return null;
        }
        String penultimate = Integer.toHexString(((quad[0] & UnsignedBytes.MAX_VALUE) << 8) | (quad[1] & UnsignedBytes.MAX_VALUE));
        return initialPart + penultimate + ":" + Integer.toHexString(((quad[2] & UnsignedBytes.MAX_VALUE) << 8) | (quad[3] & UnsignedBytes.MAX_VALUE));
    }

    private static byte parseOctet(String ipPart) {
        int octet = Integer.parseInt(ipPart);
        if (octet <= 255 && (!ipPart.startsWith("0") || ipPart.length() <= 1)) {
            return (byte) octet;
        }
        throw new NumberFormatException();
    }

    private static short parseHextet(String ipPart) {
        int hextet = Integer.parseInt(ipPart, 16);
        if (hextet <= 65535) {
            return (short) hextet;
        }
        throw new NumberFormatException();
    }

    private static InetAddress bytesToInetAddress(byte[] addr) {
        try {
            return InetAddress.getByAddress(addr);
        } catch (UnknownHostException e) {
            throw new AssertionError(e);
        }
    }

    public static String toAddrString(InetAddress ip) {
        Preconditions.checkNotNull(ip);
        if (ip instanceof Inet4Address) {
            return ip.getHostAddress();
        }
        Preconditions.checkArgument(ip instanceof Inet6Address);
        byte[] bytes = ip.getAddress();
        int[] hextets = new int[8];
        for (int i = 0; i < hextets.length; i++) {
            hextets[i] = Ints.fromBytes((byte) 0, (byte) 0, bytes[i * 2], bytes[(i * 2) + 1]);
        }
        compressLongestRunOfZeroes(hextets);
        return hextetsToIPv6String(hextets);
    }

    private static void compressLongestRunOfZeroes(int[] hextets) {
        int bestRunStart = -1;
        int bestRunLength = -1;
        int runStart = -1;
        for (int i = 0; i < hextets.length + 1; i++) {
            if (i >= hextets.length || hextets[i] != 0) {
                if (runStart >= 0) {
                    int runLength = i - runStart;
                    if (runLength > bestRunLength) {
                        bestRunStart = runStart;
                        bestRunLength = runLength;
                    }
                    runStart = -1;
                }
            } else if (runStart < 0) {
                runStart = i;
            }
        }
        if (bestRunLength >= 2) {
            Arrays.fill(hextets, bestRunStart, bestRunStart + bestRunLength, -1);
        }
    }

    private static String hextetsToIPv6String(int[] hextets) {
        StringBuilder buf = new StringBuilder(39);
        boolean lastWasNumber = false;
        for (int i = 0; i < hextets.length; i++) {
            boolean thisIsNumber = hextets[i] >= 0;
            if (thisIsNumber) {
                if (lastWasNumber) {
                    buf.append(':');
                }
                buf.append(Integer.toHexString(hextets[i]));
            } else if (i == 0 || lastWasNumber) {
                buf.append("::");
            }
            lastWasNumber = thisIsNumber;
        }
        return buf.toString();
    }

    public static String toUriString(InetAddress ip) {
        if (ip instanceof Inet6Address) {
            return "[" + toAddrString(ip) + "]";
        }
        return toAddrString(ip);
    }

    public static InetAddress forUriString(String hostAddr) {
        String ipString;
        int expectBytes;
        Preconditions.checkNotNull(hostAddr);
        if (!hostAddr.startsWith("[") || !hostAddr.endsWith("]")) {
            ipString = hostAddr;
            expectBytes = 4;
        } else {
            ipString = hostAddr.substring(1, hostAddr.length() - 1);
            expectBytes = 16;
        }
        byte[] addr = ipStringToBytes(ipString);
        if (addr != null && addr.length == expectBytes) {
            return bytesToInetAddress(addr);
        }
        throw new IllegalArgumentException(String.format("Not a valid URI IP literal: '%s'", new Object[]{hostAddr}));
    }

    public static boolean isUriInetAddress(String ipString) {
        try {
            forUriString(ipString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isCompatIPv4Address(Inet6Address ip) {
        if (!ip.isIPv4CompatibleAddress()) {
            return false;
        }
        byte[] bytes = ip.getAddress();
        return (bytes[12] == 0 && bytes[13] == 0 && bytes[14] == 0 && (bytes[15] == 0 || bytes[15] == 1)) ? false : true;
    }

    public static Inet4Address getCompatIPv4Address(Inet6Address ip) {
        Preconditions.checkArgument(isCompatIPv4Address(ip), "Address '%s' is not IPv4-compatible.", toAddrString(ip));
        return getInet4Address(Arrays.copyOfRange(ip.getAddress(), 12, 16));
    }

    public static boolean is6to4Address(Inet6Address ip) {
        byte[] bytes = ip.getAddress();
        if (bytes[0] == 32 && bytes[1] == 2) {
            return true;
        }
        return false;
    }

    public static Inet4Address get6to4IPv4Address(Inet6Address ip) {
        Preconditions.checkArgument(is6to4Address(ip), "Address '%s' is not a 6to4 address.", toAddrString(ip));
        return getInet4Address(Arrays.copyOfRange(ip.getAddress(), 2, 6));
    }

    @Beta
    public static final class TeredoInfo {
        private final Inet4Address client;
        private final int flags;
        private final int port;
        private final Inet4Address server;

        public TeredoInfo(@Nullable Inet4Address server2, @Nullable Inet4Address client2, int port2, int flags2) {
            boolean z;
            Preconditions.checkArgument(port2 >= 0 && port2 <= 65535, "port '%s' is out of range (0 <= port <= 0xffff)", Integer.valueOf(port2));
            if (flags2 < 0 || flags2 > 65535) {
                z = false;
            } else {
                z = true;
            }
            Preconditions.checkArgument(z, "flags '%s' is out of range (0 <= flags <= 0xffff)", Integer.valueOf(flags2));
            this.server = (Inet4Address) MoreObjects.firstNonNull(server2, InetAddresses.ANY4);
            this.client = (Inet4Address) MoreObjects.firstNonNull(client2, InetAddresses.ANY4);
            this.port = port2;
            this.flags = flags2;
        }

        public Inet4Address getServer() {
            return this.server;
        }

        public Inet4Address getClient() {
            return this.client;
        }

        public int getPort() {
            return this.port;
        }

        public int getFlags() {
            return this.flags;
        }
    }

    public static boolean isTeredoAddress(Inet6Address ip) {
        byte[] bytes = ip.getAddress();
        if (bytes[0] != 32 || bytes[1] != 1 || bytes[2] != 0) {
            return false;
        }
        if (bytes[3] == 0) {
            return true;
        }
        return false;
    }

    public static TeredoInfo getTeredoInfo(Inet6Address ip) {
        Preconditions.checkArgument(isTeredoAddress(ip), "Address '%s' is not a Teredo address.", toAddrString(ip));
        byte[] bytes = ip.getAddress();
        Inet4Address server = getInet4Address(Arrays.copyOfRange(bytes, 4, 8));
        int flags = ByteStreams.newDataInput(bytes, 8).readShort() & SupportMenu.USER_MASK;
        int port = (~ByteStreams.newDataInput(bytes, 10).readShort()) & SupportMenu.USER_MASK;
        byte[] clientBytes = Arrays.copyOfRange(bytes, 12, 16);
        for (int i = 0; i < clientBytes.length; i++) {
            clientBytes[i] = (byte) (~clientBytes[i]);
        }
        return new TeredoInfo(server, getInet4Address(clientBytes), port, flags);
    }

    public static boolean isIsatapAddress(Inet6Address ip) {
        if (isTeredoAddress(ip)) {
            return false;
        }
        byte[] bytes = ip.getAddress();
        if ((bytes[8] | 3) == 3 && bytes[9] == 0 && bytes[10] == 94 && bytes[11] == -2) {
            return true;
        }
        return false;
    }

    public static Inet4Address getIsatapIPv4Address(Inet6Address ip) {
        Preconditions.checkArgument(isIsatapAddress(ip), "Address '%s' is not an ISATAP address.", toAddrString(ip));
        return getInet4Address(Arrays.copyOfRange(ip.getAddress(), 12, 16));
    }

    public static boolean hasEmbeddedIPv4ClientAddress(Inet6Address ip) {
        if (isCompatIPv4Address(ip) || is6to4Address(ip)) {
            return true;
        }
        return isTeredoAddress(ip);
    }

    public static Inet4Address getEmbeddedIPv4ClientAddress(Inet6Address ip) {
        if (isCompatIPv4Address(ip)) {
            return getCompatIPv4Address(ip);
        }
        if (is6to4Address(ip)) {
            return get6to4IPv4Address(ip);
        }
        if (isTeredoAddress(ip)) {
            return getTeredoInfo(ip).getClient();
        }
        throw new IllegalArgumentException(String.format("'%s' has no embedded IPv4 address.", new Object[]{toAddrString(ip)}));
    }

    public static boolean isMappedIPv4Address(String ipString) {
        byte[] bytes = ipStringToBytes(ipString);
        if (bytes == null || bytes.length != 16) {
            return false;
        }
        for (int i = 0; i < 10; i++) {
            if (bytes[i] != 0) {
                return false;
            }
        }
        for (int i2 = 10; i2 < 12; i2++) {
            if (bytes[i2] != -1) {
                return false;
            }
        }
        return true;
    }

    public static Inet4Address getCoercedIPv4Address(InetAddress ip) {
        long addressAsLong;
        if (ip instanceof Inet4Address) {
            return (Inet4Address) ip;
        }
        byte[] bytes = ip.getAddress();
        boolean leadingBytesOfZero = true;
        int i = 0;
        while (true) {
            if (i >= 15) {
                break;
            } else if (bytes[i] != 0) {
                leadingBytesOfZero = false;
                break;
            } else {
                i++;
            }
        }
        if (leadingBytesOfZero && bytes[15] == 1) {
            return LOOPBACK4;
        }
        if (leadingBytesOfZero && bytes[15] == 0) {
            return ANY4;
        }
        Inet6Address ip6 = (Inet6Address) ip;
        if (hasEmbeddedIPv4ClientAddress(ip6)) {
            addressAsLong = (long) getEmbeddedIPv4ClientAddress(ip6).hashCode();
        } else {
            addressAsLong = ByteBuffer.wrap(ip6.getAddress(), 0, 8).getLong();
        }
        int coercedHash = Hashing.murmur3_32().hashLong(addressAsLong).asInt() | -536870912;
        if (coercedHash == -1) {
            coercedHash = -2;
        }
        return getInet4Address(Ints.toByteArray(coercedHash));
    }

    public static int coerceToInteger(InetAddress ip) {
        return ByteStreams.newDataInput(getCoercedIPv4Address(ip).getAddress()).readInt();
    }

    public static Inet4Address fromInteger(int address) {
        return getInet4Address(Ints.toByteArray(address));
    }

    public static InetAddress fromLittleEndianByteArray(byte[] addr) throws UnknownHostException {
        byte[] reversed = new byte[addr.length];
        for (int i = 0; i < addr.length; i++) {
            reversed[i] = addr[(addr.length - i) - 1];
        }
        return InetAddress.getByAddress(reversed);
    }

    public static InetAddress decrement(InetAddress address) {
        boolean z;
        byte[] addr = address.getAddress();
        int i = addr.length - 1;
        while (i >= 0 && addr[i] == 0) {
            addr[i] = -1;
            i--;
        }
        if (i >= 0) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z, "Decrementing %s would wrap.", address);
        addr[i] = (byte) (addr[i] - 1);
        return bytesToInetAddress(addr);
    }

    public static InetAddress increment(InetAddress address) {
        boolean z;
        byte[] addr = address.getAddress();
        int i = addr.length - 1;
        while (i >= 0 && addr[i] == -1) {
            addr[i] = 0;
            i--;
        }
        if (i >= 0) {
            z = true;
        } else {
            z = false;
        }
        Preconditions.checkArgument(z, "Incrementing %s would wrap.", address);
        addr[i] = (byte) (addr[i] + 1);
        return bytesToInetAddress(addr);
    }

    public static boolean isMaximum(InetAddress address) {
        byte[] addr = address.getAddress();
        for (byte b : addr) {
            if (b != -1) {
                return false;
            }
        }
        return true;
    }
}
