package com.google.thirdparty.publicsuffix;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@GwtCompatible
class TrieParser {
    private static final Joiner PREFIX_JOINER = Joiner.on("");

    TrieParser() {
    }

    static ImmutableMap<String, PublicSuffixType> parseTrie(CharSequence encoded) {
        ImmutableMap.Builder<String, PublicSuffixType> builder = ImmutableMap.builder();
        int encodedLen = encoded.length();
        int idx = 0;
        while (idx < encodedLen) {
            idx += doParseTrieToBuilder(Lists.newLinkedList(), encoded.subSequence(idx, encodedLen), builder);
        }
        return builder.build();
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x005c A[EDGE_INSN: B:35:0x005c->B:20:0x005c ?: BREAK  , SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int doParseTrieToBuilder(java.util.List<java.lang.CharSequence> r10, java.lang.CharSequence r11, com.google.common.collect.ImmutableMap.Builder<java.lang.String, com.google.thirdparty.publicsuffix.PublicSuffixType> r12) {
        /*
            r9 = 58
            r8 = 33
            r7 = 63
            r6 = 44
            r5 = 0
            int r2 = r11.length()
            r3 = 0
            r0 = 0
        L_0x000f:
            if (r3 >= r2) goto L_0x001b
            char r0 = r11.charAt(r3)
            r4 = 38
            if (r0 == r4) goto L_0x001b
            if (r0 != r7) goto L_0x0060
        L_0x001b:
            java.lang.CharSequence r4 = r11.subSequence(r5, r3)
            java.lang.CharSequence r4 = reverse(r4)
            r10.add(r5, r4)
            if (r0 == r8) goto L_0x002a
            if (r0 != r7) goto L_0x0069
        L_0x002a:
            com.google.common.base.Joiner r4 = PREFIX_JOINER
            java.lang.String r1 = r4.join((java.lang.Iterable<?>) r10)
            int r4 = r1.length()
            if (r4 <= 0) goto L_0x003d
            com.google.thirdparty.publicsuffix.PublicSuffixType r4 = com.google.thirdparty.publicsuffix.PublicSuffixType.fromCode(r0)
            r12.put(r1, r4)
        L_0x003d:
            int r3 = r3 + 1
            if (r0 == r7) goto L_0x005c
            if (r0 == r6) goto L_0x005c
        L_0x0043:
            if (r3 >= r2) goto L_0x005c
            java.lang.CharSequence r4 = r11.subSequence(r3, r2)
            int r4 = doParseTrieToBuilder(r10, r4, r12)
            int r3 = r3 + r4
            char r4 = r11.charAt(r3)
            if (r4 == r7) goto L_0x005a
            char r4 = r11.charAt(r3)
            if (r4 != r6) goto L_0x0043
        L_0x005a:
            int r3 = r3 + 1
        L_0x005c:
            r10.remove(r5)
            return r3
        L_0x0060:
            if (r0 == r8) goto L_0x001b
            if (r0 == r9) goto L_0x001b
            if (r0 == r6) goto L_0x001b
            int r3 = r3 + 1
            goto L_0x000f
        L_0x0069:
            if (r0 == r9) goto L_0x002a
            if (r0 != r6) goto L_0x003d
            goto L_0x002a
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.thirdparty.publicsuffix.TrieParser.doParseTrieToBuilder(java.util.List, java.lang.CharSequence, com.google.common.collect.ImmutableMap$Builder):int");
    }

    private static CharSequence reverse(CharSequence s) {
        int length = s.length();
        if (length <= 1) {
            return s;
        }
        char[] buffer = new char[length];
        buffer[0] = s.charAt(length - 1);
        for (int i = 1; i < length; i++) {
            buffer[i] = s.charAt((length - 1) - i);
            if (Character.isSurrogatePair(buffer[i], buffer[i - 1])) {
                swap(buffer, i - 1, i);
            }
        }
        return new String(buffer);
    }

    private static void swap(char[] buffer, int f, int s) {
        char tmp = buffer[f];
        buffer[f] = buffer[s];
        buffer[s] = tmp;
    }
}
