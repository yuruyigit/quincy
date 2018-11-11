package com.protocol7.nettyquick.tls;

import static com.protocol7.nettyquick.utils.Hex.dehex;

public class OneRttAEAD {


    public static AEAD create(byte[] handshakeSecret, byte[] handshakeHash, boolean quic, boolean isClient) {
        String labelPrefix;
        if (quic) {
            labelPrefix = AEADUtil.QUIC_LABEL_PREFIX;
        } else {
            labelPrefix = AEADUtil.TLS_13_LABEL_PREFIX;
        }

        // derived_secret = HKDF-Expand-Label(
        //                key = handshake_secret,
        //                label = "derived",
        //                context = empty_hash,
        //                len = 32)
        byte[] derivedSecret = AEADUtil.expandLabel(handshakeSecret, "tls13 ","derived", AEADUtil.EMPTY_HASH, 32);

        //        master_secret = HKDF-Extract(
        //                salt=derived_secret,
        //                key=00...)
        byte[] masterSecret = AEADUtil.hkdf.extract(derivedSecret, dehex("0000000000000000000000000000000000000000000000000000000000000000"));

        // client_application_traffic_secret = HKDF-Expand-Label(
        //    key = master_secret,
        //    label = "c ap traffic",
        //    context = handshake_hash,
        //    len = 32)
        byte[] clientApplicationTrafficSecret = AEADUtil.expandLabel(masterSecret, "tls13 ","c ap traffic", handshakeHash, 32);

        // server_application_traffic_secret = HKDF-Expand-Label(
        //    key = master_secret,
        //    label = "s ap traffic",
        //    context = handshake_hash,
        //    len = 32)
        byte[] serverApplicationTrafficSecret = AEADUtil.expandLabel(masterSecret, "tls13 ","s ap traffic", handshakeHash, 32);

        // client_application_key = HKDF-Expand-Label(
        //    key = client_application_traffic_secret,
        //    label = "key",
        //    context = "",
        //    len = 16)
        byte[] clientApplicationKey = AEADUtil.expandLabel(clientApplicationTrafficSecret, labelPrefix, "key", new byte[0], 16);

        // server_application_key = HKDF-Expand-Label(
        //    key = server_application_traffic_secret,
        //    label = "key",
        //    context = "",
        //    len = 16)
        byte[] serverApplicationKey = AEADUtil.expandLabel(serverApplicationTrafficSecret, labelPrefix,"key", new byte[0], 16);


        // client_application_iv = HKDF-Expand-Label(
        //    key = client_application_traffic_secret,
        //    label = "iv",
        //    context = "",
        //    len = 12)
        byte[] clientApplicationIV = AEADUtil.expandLabel(clientApplicationTrafficSecret, labelPrefix,"iv", new byte[0], 12);


        // server_application_iv = HKDF-Expand-Label(
        //    key = server_application_traffic_secret,
        //    label = "iv",
        //    context = "",
        //    len = 12)
        byte[] serverApplicationIV = AEADUtil.expandLabel(serverApplicationTrafficSecret, labelPrefix,"iv", new byte[0], 12);

        if (isClient) {
            return new AEAD(clientApplicationKey, serverApplicationKey, clientApplicationIV, serverApplicationIV);
        } else {
            return new AEAD(serverApplicationKey, clientApplicationKey, serverApplicationIV, clientApplicationIV);
        }
    }

}
