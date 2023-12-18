package com.google.common.base;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;

@GwtCompatible
@Beta
public final class Utf8 {
    public static int encodedLength(CharSequence sequence) {
        int utf16Length = sequence.length();
        int utf8Length = utf16Length;
        int i = 0;
        while (i < utf16Length && sequence.charAt(i) < 128) {
            i++;
        }
        while (true) {
            if (i < utf16Length) {
                char c = sequence.charAt(i);
                if (c >= 2048) {
                    utf8Length += encodedLengthGeneral(sequence, i);
                    break;
                }
                utf8Length += (127 - c) >>> 31;
                i++;
            } else {
                break;
            }
        }
        if (utf8Length >= utf16Length) {
            return utf8Length;
        }
        throw new IllegalArgumentException("UTF-8 length does not fit in int: " + (((long) utf8Length) + 4294967296L));
    }

    private static int encodedLengthGeneral(CharSequence sequence, int start) {
        int utf16Length = sequence.length();
        int utf8Length = 0;
        int i = start;
        while (i < utf16Length) {
            char c = sequence.charAt(i);
            if (c < 2048) {
                utf8Length += (127 - c) >>> 31;
            } else {
                utf8Length += 2;
                if (55296 <= c && c <= 57343) {
                    if (Character.codePointAt(sequence, i) < 65536) {
                        throw new IllegalArgumentException("Unpaired surrogate at index " + i);
                    }
                    i++;
                }
            }
            i++;
        }
        return utf8Length;
    }

    public static boolean isWellFormed(byte[] bytes) {
        return isWellFormed(bytes, 0, bytes.length);
    }

    public static boolean isWellFormed(byte[] bytes, int off, int len) {
        int end = off + len;
        Preconditions.checkPositionIndexes(off, end, bytes.length);
        for (int i = off; i < end; i++) {
            if (bytes[i] < 0) {
                return isWellFormedSlowPath(bytes, i, end);
            }
        }
        return true;
    }

    private static boolean isWellFormedSlowPath(byte[] bytes, int off, int end) {
        int index = off;
        while (index < end) {
            int index2 = index + 1;
            byte byte1 = bytes[index];
            if (byte1 < 0) {
                if (byte1 < -32) {
                    if (index2 != end && byte1 >= -62) {
                        int index3 = index2 + 1;
                        if (bytes[index2] > -65) {
                            int i = index3;
                        } else {
                            index2 = index3;
                        }
                    }
                    return false;
                } else if (byte1 < -16) {
                    if (index2 + 1 >= end) {
                        return false;
                    }
                    int index4 = index2 + 1;
                    byte byte2 = bytes[index2];
                    if (byte2 > -65 || ((byte1 == -32 && byte2 < -96) || (byte1 == -19 && -96 <= byte2))) {
                        int i2 = index4;
                    } else {
                        index2 = index4 + 1;
                        if (bytes[index4] > -65) {
                        }
                    }
                    return false;
                } else if (index2 + 2 >= end) {
                    return false;
                } else {
                    int index5 = index2 + 1;
                    byte byte22 = bytes[index2];
                    if (byte22 > -65 || (((byte1 << Ascii.FS) + (byte22 + 112)) >> 30) != 0) {
                        int i3 = index5;
                    } else {
                        int index6 = index5 + 1;
                        if (bytes[index5] <= -65) {
                            int index7 = index6 + 1;
                            if (bytes[index6] > -65) {
                                int i4 = index7;
                            } else {
                                index2 = index7;
                            }
                        }
                    }
                    return false;
                }
            }
            index = index2;
        }
        return true;
    }

    private Utf8() {
    }
}
