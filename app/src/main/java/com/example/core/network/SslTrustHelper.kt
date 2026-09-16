package com.example.core.network

import android.util.Log
import okhttp3.OkHttpClient
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * SSL/TLS segédosztály régebbi Android verziókhoz (pl. Android 11 és korábbiak).
 *
 * Sok magyar felsőoktatási intézmény (pl. ELTE, BME, Corvinus, Debrecen, Pécs, Szeged stb. - GEANT tagok)
 * a HARICA és GEANT 2021-es és újabb kiadású gyökértanúsítványait (Root CA) használja.
 * Régebbi Android rendszereken (mint pl. a frissítéseket már nem kapó Xiaomi Redmi Note 8 Android 11-gyel)
 * ezek a modern CA tanúsítványok hiányoznak a beépített rendszer tanúsítványtárból, ami miatt
 * "Trust anchor for certification path not found" hiba keletkezik.
 *
 * Ez az osztály a rendszer alapértelmezett tanúsítványai mellé biztonságosan betölti:
 *  - HARICA TLS RSA Root CA 2021 & HARICA TLS ECC Root CA 2021
 *  - GEANT TLS RSA 1 & GEANT TLS ECC 1 (közbenső CA-k)
 *  - e-Szignó OV TLS CA 2023 (link cert a 2009-es megbízható Microsec gyökérhez)
 *  - Sectigo Public Server Authentication CA OV R36 (Semmelweis University)
 *  - Let's Encrypt ISRG Root X1 & ISRG Root X2
 * Valamint dedikáltan kezeli az Android felhasználói tanúsítványtárát (AndroidCAStore - pl. AdGuard, VPN-ek esetén).
 */
object SslTrustHelper {

    private const val TAG = "SslTrustHelper"

    private val BUNDLED_CERTIFICATES = listOf(
        // 1. HARICA TLS RSA Root CA 2021 (ELTE, BME, Corvinus, Debreceni Egyetem, PTE, SZTE stb.)
        """
        -----BEGIN CERTIFICATE-----
        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
        63ZTGI0RmLo=
        -----END CERTIFICATE-----
        """.trimIndent(),

        // 2. HARICA TLS ECC Root CA 2021
        """
        -----BEGIN CERTIFICATE-----
        MIICVDCCAdugAwIBAgIQZ3SdjXfYO2rbIvT/WeK/zjAKBggqhkjOPQQDAzBsMQsw
        CQYDVQQGEwJHUjE3MDUGA1UECgwuSGVsbGVuaWMgQWNhZGVtaWMgYW5kIFJlc2Vh
        cmNoIEluc3RpdHV0aW9ucyBDQTEkMCIGA1UEAwwbSEFSSUNBIFRMUyBFQ0MgUm9v
        dCBDQSAyMDIxMB4XDTIxMDIxOTExMDExMFoXDTQ1MDIxMzExMDEwOVowbDELMAkG
        A1UEBhMCR1IxNzA1BgNVBAoMLkhlbGxlbmljIEFjYWRlbWljIGFuZCBSZXNlYXJj
        aCBJbnN0aXR1dGlvbnMgQ0ExJDAiBgNVBAMMG0hBUklDQSBUTFMgRUNDIFJvb3Qg
        Q0EgMjAyMTB2MBAGByqGSM49AgEGBSuBBAAiA2IABDgI/rGgltJ6rK9JOtDA4MM7
        KKrxcm1lAEeIhPyaJmuqS7psBAqIXhfyVYf8MLA04jRYVxqEU+kw2anylnTDUR9Y
        STHMmE5gEYd103KUkE+bECUqqHgtvpBBWJAVcqeht6NCMEAwDwYDVR0TAQH/BAUw
        AwEB/zAdBgNVHQ4EFgQUyRtTgRL+BNUW0aq8mm+3oJUZbsowDgYDVR0PAQH/BAQD
        AgGGMAoGCCqGSM49BAMDA2cAMGQCMBHervjcToiwqfAircJRQO9gcS3ujwLEXQNw
        SaSS6sUUiHCm0w2wqsosQJz76YJumgIwK0eaB8bRwoF8yguWGEEbo/QwCZ61IygN
        nxS2PFOiTAZpffpskcYqSUXm7LcT4Tps
        -----END CERTIFICATE-----
        """.trimIndent(),

        // 3. GEANT TLS RSA 1 (közbenső CA az ELTE / GEANT intézményekhez)
        """
        -----BEGIN CERTIFICATE-----
        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
        3RfjV9IaUQGJ
        -----END CERTIFICATE-----
        """.trimIndent(),

        // 4. GEANT TLS ECC 1 (közbenső ECC CA)
        """
        -----BEGIN CERTIFICATE-----
        MIIDNzCCArygAwIBAgIQQv3c4SYWB+Gl5pNaQAFh3TAKBggqhkjOPQQDAzBsMQsw
        CQYDVQQGEwJHUjE3MDUGA1UECgwuSGVsbGVuaWMgQWNhZGVtaWMgYW5kIFJlc2Vh
        cmNoIEluc3RpdHV0aW9ucyBDQTEkMCIGA1UEAwwbSEFSSUNBIFRMUyBFQ0MgUm9v
        dCBDQSAyMDIxMB4XDTI1MDEwMzExMTQyMVoXDTM5MTIzMTExMTQyMFowYDELMAkG
        A1UEBhMCR1IxNzA1BgNVBAoMLkhlbGxlbmljIEFjYWRlbWljIGFuZCBSZXNlYXJj
        aCBJbnN0aXR1dGlvbnMgQ0ExGDAWBgNVBAMMD0dFQU5UIFRMUyBFQ0MgMTB2MBAG
        ByqGSM49AgEGBSuBBAAiA2IABANPWLwh0Za2UqtbLV7/qNRm78zsttgSuvhn73bU
        GtxETsVOEZeMUfMjgHw8EwrsSJI9oj0CgZQFFSEY1NJfcxA/NJiOYJUKPsFbpOrY
        dr0q4g+aBZsXWeh7bMCzx24g/aOCAS0wggEpMBIGA1UdEwEB/wQIMAYBAf8CAQAw
        HwYDVR0jBBgwFoAUyRtTgRL+BNUW0aq8mm+3oJUZbsowTQYIKwYBBQUHAQEEQTA/
        MD0GCCsGAQUFBzAChjFodHRwOi8vY3J0LmhhcmljYS5nci9IQVJJQ0EtVExTLVJv
        b3QtMjAyMS1FQ0MuY2VyMBEGA1UdIAQKMAgwBgYEVR0gADAdBgNVHSUEFjAUBggr
        BgEFBQcDAgYIKwYBBQUHAwEwQgYDVR0fBDswOTA3oDWgM4YxaHR0cDovL2NybC5o
        YXJpY2EuZ3IvSEFSSUNBLVRMUy1Sb290LTIwMjEtRUNDLmNybDAdBgNVHQ4EFgQU
        6ZkGjRcfq/uWGlrIW15dXuzanI8wDgYDVR0PAQH/BAQDAgGGMAoGCCqGSM49BAMD
        A2kAMGYCMQD2M1caaY2OwmthgmANUQg3LBLI0/2LiCdxa2zNq0G59wVzbjEk0cR/
        px52OegIwRACMQCk+iTmBlR6Xfv6igiiaFiPYfN2HfbcYLWbot5DZ2H1b4JVJV+V
        rga7uu50SDG9hf4=
        -----END CERTIFICATE-----
        """.trimIndent(),

        // 5. e-Szigno OV TLS CA 2023 (link cert a megbízható 2009-es gyökérhez: Milton Friedman, Szent Atanáz stb.)
        """
        -----BEGIN CERTIFICATE-----
        MIID8zCCAtugAwIBAgINASJg+EN4eqPPomYxCjANBgkqhkiG9w0BAQsFADCBgjEL
        MAkGA1UEBhMCSFUxETAPBgNVBAcMCEJ1ZGFwZXN0MRYwFAYDVQQKDA1NaWNyb3Nl
        YyBMdGQuMScwJQYDVQQDDB5NaWNyb3NlYyBlLVN6aWdubyBSb290IENBIDIwMDkx
        HzAdBgkqhkiG9w0BCQEWEGluZm9AZS1zemlnbm8uaHUwHhcNMjUwMjEyMTEwMDAw
        WhcNMjgwMjEyMTA1OTU5WjBzMQswCQYDVQQGEwJIVTERMA8GA1UEBwwIQnVkYXBl
        c3QxFjAUBgNVBAoMDU1pY3Jvc2VjIEx0ZC4xFzAVBgNVBGEMDlZBVEhVLTIzNTg0
        NDk3MSAwHgYDVQQDDBdlLVN6aWdubyBPViBUTFMgQ0EgMjAyMzBZMBMGByqGSM49
        AgEGCCqGSM49AwEHA0IABDObs48j6qr+cBX/mjgF9SY9touVURYdTGHti3k3YFK2
        qoqGD8KwnWo5e86ZOPQ+h90Q7gw+pkG3DolPx7kBnoCjggE/MIIBOzAPBgNVHRMB
        Af8EBTADAQH/MA4GA1UdDwEB/wQEAwIBBjAdBgNVHSUEFjAUBggrBgEFBQcDAQYI
        KwYBBQUHAwIwEQYDVR0gBAowCDAGBgRVHSAAMB0GA1UdDgQWBBSpRZ7VE5qyYBmh
        mYRElgCXFduBADAfBgNVHSMEGDAWgBTLD8bfQkPMPcu1SCOhGnqmKrs0aDA2BgNV
        HR8ELzAtMCugKaAnhiVodHRwOi8vY3JsLmUtc3ppZ25vLmh1L3Jvb3RjYTIwMDku
        Y3JsMG4GCCsGAQUFBwEBBGIwYDArBggrBgEFBQcwAYYfaHR0cDovL3Jvb3RvY3Nw
MjAwOS5lLXN6aWduby5odTAxBggrBgEFBQcwAoYlaHR0cDovL3d3dy5lLXN6aWdu
        by5odS9yb290Y2EyMDA5LmNydDANBgkqhkiG9w0BAQsFAAOCAQEAqRCti4sBtsfg
        208/VH+n1acVdCSniN9pkZWaiHsQZtp/lEsdrLX/53RVv2GtrFybVC28boznNiOd
        NSgNwQ9SBpgMn94uWcZCiSaK5DzajULn07Wd9/oeAdrp9etH0Q6e6mwtrDD+povu
        kNz2V4Cij55LX35twOoV/r5o273EBbj6K+kmz+EIk/5ojx0QZT5fGGHVLkAwIGu5
        5IVsMGAP7V+mLFiXUQd7QwiC1MkMDrYquKKeC7RxnUwFpD6NdUQx8+VhS0MkzyR9
        KfC2AIgXlWeSQ1YtmW2xSw2tVij6omObtM2/S3D82735ANlfGuywbV4vKXx5lWFz
        67cJ5ns5UQ==
        -----END CERTIFICATE-----
        """.trimIndent(),

        // 6. Sectigo Public Server Authentication CA OV R36 (Semmelweis University)
        """
        -----BEGIN CERTIFICATE-----
        MIIGTDCCBDSgAwIBAgIQLBo8dulD3d3/GRsxiQrtcTANBgkqhkiG9w0BAQwFADBf
        MQswCQYDVQQGEwJHQjEYMBYGA1UEChMPU2VjdGlnbyBMaW1pdGVkMTYwNAYDVQQD
        Ey1TZWN0aWdvIFB1YmxpYyBTZXJ2ZXIgQXV0aGVudGljYXRpb24gUm9vdCBSNDYw
        HhcNMjEwMzIyMDAwMDAwWhcNMzYwMzIxMjM1OTU5WjBgMQswCQYDVQQGEwJHQjEY
        MBYGA1UEChMPU2VjdGlnbyBMaW1pdGVkMTcwNQYDVQQDEy5TZWN0aWdvIFB1Ymxp
        YyBTZXJ2ZXIgQXV0aGVudGljYXRpb24gQ0EgT1YgUjM2MIIBojANBgkqhkiG9w0B
        AQEFAAOCAY8AMIIBigKCAYEApkMtJ3R06jo0fceI0M52B7K+TyMeGcv2BQ5AVc3j
        lYt76TvHIu/nNe22W/RJXX9rWUD/2GE6GF5x0V4bsY7K3IeJ8E7+KzG/TGboySfD
        u+F52jqQBbY62ofhYjMeiAbLI02+FqwHeM8uIrUtcX8b2RCxF358TB0NHVccAXZc
        FYgZndZCeXxjuca7pJJ20LLUnXtgXcjAE1vY4WvbReW0W6mkeZyNGdmpTcFs5Y+s
        yy6LtE5Zocji9J9NlNnReox2RWVyEXpA1ChZ4gqN+ZpVSIQ0HBorVFbBKyhdZyEX
        gZgNSNtBRwxqwIzJePJhYd4ZUhO1vk+/uP3nwDk0p95q/j7naXNCSvESnrHPypaB
        WRK066nKfPRPi9m9kIOhMdYfS8giFRTcdgL24Ycilj7ecAK9Trh0VbjwouJ4WH+x
        bt47u68ZFCD/ac55I0DNHkCpaPruj6e9Rmr7K46wZDAYXuEAqB7tGG/jd6JAA+H2
        O44CV98NRsU213f1kScIZntNAgMBAAGjggGBMIIBfTAfBgNVHSMEGDAWgBRWc1hk
        lfmSGrASKgRieaFAFYghSTAdBgNVHQ4EFgQU42Z0u3BojSxdTg6mSo+bNyKcgpIw
        DgYDVR0PAQH/BAQDAgGGMBIGA1UdEwEB/wQIMAYBAf8CAQAwHQYDVR0lBBYwFAYI
        KwYBBQUHAwEGCCsGAQUFBwMCMBsGA1UdIAQUMBIwBgYEVR0gADAIBgZngQwBAgIw
        VAYDVR0fBE0wSzBJoEegRYZDaHR0cDovL2NybC5zZWN0aWdvLmNvbS9TZWN0aWdv
        UHVibGljU2VydmVyQXV0aGVudGljYXRpb25Sb290UjQ2LmNybDCBhAYIKwYBBQUH
        AQEEeDB2ME8GCCsGAQUFBzAChkNodHRwOi8vY3J0LnNlY3RpZ28uY29tL1NlY3Rp
        Z29QdWJsaWNTZXJ2ZXJBdXRoZW50aWNhdGlvblJvb3RSNDYucDdjMCMGCCsGAQUF
        BzABhhdodHRwOi8vb2NzcC5zZWN0aWdvLmNvbTANBgkqhkiG9w0BAQwFAAOCAgEA
        BZXWDHWC3cubb/e1I1kzi8lPFiK/ZUoH09ufmVOrc5ObYH/XKkWUexSPqRkwKFKr
        7r8OuG+p7VNB8rifX6uopqKAgsvZtZsq7iAFw04To6vNcxeBt1Eush3cQ4b8nbQR
        MQLChgEAqwhuXp9P48T4QEBSksYav7+aFjNySsLYlPzNqVM3RNwvBdvp6vgDtGwc
        xlKQZVuuNVIaoYyls8swhxDeSHKpRdxRauTLZ+pl+wGvy0pnrLEJGSz9mOEmfbod
        e/XopR2NGqaHJ6bIjyxPu6UtyQGI26En7UAEozACrHz06Nx2jTAY9E6NeB6XuobE
        wLK025ZRmvglcURG1BrV24tGHHTgxCe8M3oGlpUSMTKQ2dkgljZVYt+gKdFtWELZ
        MuRdi+X3XsrR8LFz+aLUiDRfQqhmw3RxjIyVKvvu9UPYY1nsvxYmFnUSeM+2q1z/
        iPUry+xDY9MC6+IhleKT094VKdFVp7LXH42+wvU+17lRolQ2mK2N/nBLVBwaIhib
        QXw4VYKwB86Bc6eS6iqsc94KEgD/U4VsjmgfhK+Xp4NM+VYzTTa3QeV3p8xOM0cw
        q1p8oZFA+OBcz3FYWpDIe5j0NWKlw9hXsTyPY/HeZUV59akskSOSRSmDfe8wJDPX
        58uB9/7lud0G3x0pxQAcffP0ayKavNwDTw4UfJ34cEw=
        -----END CERTIFICATE-----
        """.trimIndent(),

        // 7. ISRG Root X1 (Let's Encrypt Root)
        """
        -----BEGIN CERTIFICATE-----
        MIIFazCCA1OgAwIBAgIRAIIQz7DSQONZRGPgu2OCiwAwDQYJKoZIhvcNAQELBQAw
        TzELMAkGA1UEBhMCVVMxKTAnBgNVBAoTIEludGVybmV0IFNlY3VyaXR5IFJlc2Vh
        cmNoIEdyb3VwMRUwEwYDVQQDEwxJU1JHIFJvb3QgWDEwHhcNMTUwNjA0MTEwNDM4
        WhcNMzUwNjA0MTEwNDM4WjBPMQswCQYDVQQGEwJVUzEpMCcGA1UEChMgSW50ZXJu
        ZXQgU2VjdXJpdHkgUmVzZWFyY2ggR3JvdXAxFTATBgNVBAMTDElTUkcgUm9vdCBY
        MTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAK3oJHP0FDfzm54rVygc
        h77ct984kIxuPOZXoHj3dcKi/vVqbvYATyjb3miGbESTtrFj/RQSa78f0uoxmyF+
        0TM8ukj13Xnfs7j/EvEhmkvBioZxaUpmZmyPfjxwv60pIgbz5MDmgK7iS4+3mX6U
        A5/TR5d8mUgjU+g4rk8Kb4Mu0UlXjIB0ttov0DiNewNwIRt18jA8+o+u3dpjq+sW
        T8KOEUt+zwvo/7V3LvSye0rgTBIlDHCNAymg4VMk7BPZ7hm/ELNKjD+Jo2FR3qyH
        B5T0Y3HsLuJvW5iB4YlcNHlsdu87kGJ55tukmi8mxdAQ4Q7e2RCOFvu396j3x+U
        CB5iPNgiV5+I3lg02dZ77DnKxHZu8A/lJBdiB3QW0KtZB6awBdpUKD9jf1b0SHzU
        vKBds0pjBqAlkd25HN7rOrFleaJ1/ctaJxQZBKT5ZPt0m9STJEadao0xAH0ahmbW
        nOlFuhjuefXKnEgV4We0+UXgVCwOPjdAvBbI+e0ocS3MFEvzG6uBQE3xDk3SzynT
        njh8BCNAw1FtxNrQHusEwMFxIt4I7mKZ9YIqioymCzLq9gwQbooMDQaHWBfEbwrb
        wqHyGO0aoSCqI3Haadr8faqU9GY/rOPNk3sgrDQoo//fb4hVC1CLQJ13hef4Y53C
        IrU7m2Ys6xt0nUW7/vGT1M0NPAgMBAAGjQjBAMA4GA1UdDwEB/wQEAwIBBjAPBgNV
        HRMBAf8EBTADAQH/MB0GA1UdDgQWBBR5tFnme7bl5AFzgAiIyBpY9umbbjANBgkq
        hkiG9w0BAQsFAAOCAgEAVR9YqbyyqFDQDLHYGmkgJykIrGF1XIpu+ILlaS/V9lZL
        ubhzEFnTIZd+50xx+7LSYK05qAvqFyFWhfFQDlnrzuBZ6brJFe+GnY+EgPbk6ZGQ
        3BebYhtF8GaV0nxvwuo77x/Py9auJ/GpsMiu/X1+mvoiBOv/2X/qkSsisRcOj/KK
        NFtY2PwByVS5uCbMiogziUwthDyC3+6WVwW6LLv3xLfHTjuCvjHIInNzktHCgKQ5
        ORAzI4JMPJ+GslWYHb4phowim57iaztXOoJwTdwJx4nLCgdNbOhdjsnvzqvHu7Ur
        TkXWStAmzOVyyghqpZXjFaH3pO3JLF+l+/+sKAIuvtd7u+Nxe5AW0wdeRlN8NwdC
        jNPElpzVmbUq4JUagEiuTDkHzsxHpFKVK7q4+63SM1N95R1NbdWhscdCb+ZAJzVc
        oyi3B43njTOQ5yOf+1CceWxG1bQVs5ZufpsMljq4Ui0/1lvh+wjChP4kqKOJ2qxq
        4RgqsahDYVvTH9w7jXbyLeiNdd8XM2w9U/t7y0Ff/9yi0GE44Za4rF2LN9d11TPA
        mRGunUHBcnWEvgJBQl9nJEiU0Zsnvgc/ubhPgXRR4Xq37Z0j4r7g1SgEEzwxA57d
        emyPxgcYxn/eR44/KJ4EBs+lVDR3veyJm+kXQ99b21/+jh5Xos1AnX5iItreGCc=
        -----END CERTIFICATE-----
        """.trimIndent(),

        // 8. ISRG Root X2 (Let's Encrypt ECDSA Root - Testnevelési Egyetem, Veszprémi Érseki Főiskola stb.)
        """
        -----BEGIN CERTIFICATE-----
        MIICGzCCAaGgAwIBAgIQQdKd0XLq7qeAwSxs6S+HUjAKBggqhkjOPQQDAzBPMQsw
        CQYDVQQGEwJVUzEpMCcGA1UEChMgSW50ZXJuZXQgU2VjdXJpdHkgUmVzZWFyY2gg
        R3JvdXAxFTATBgNVBAMTDElTUkcgUm9vdCBYMjAeFw0yMDA5MDQwMDAwMDBaFw00
        MDA5MTcxNjAwMDBaME8xCzAJBgNVBAYTAlVTMSkwJwYDVQQKEyBJbnRlcm5ldCBT
        ZWN1cml0eSBSZXNlYXJjaCBHcm91cDEVMBMGA1UEAxMMSVNSRyBSb290IFgyMHYw
        EAYHKoZIzj0CAQYFK4EEACIDYgAEzZvVn4CDCuwJSvMWSj5cz3es3mcFDR0HttwW
        +1qLFNvicWDEukWVEYmO6gbf9yoWHKS5xcUy4APgHoIYOIvXRdgKam7mAHf7AlF9
        ItgKbppbd9/w+kHsOdx1ymgHDB/qo0IwQDAOBgNVHQ8BAf8EBAMCAQYwDwYDVR0T
        AQH/BAUwAwEB/zAdBgNVHQ4EFgQUfEKWrt5LSDv6kviejM9ti6lyN5UwCgYIKoZI
        zj0EAwMDaAAwZQIwe3lORlCEwkSHRhtFcP9Ymd70/aTSVaYgLXTWNLxBo1BfASdW
        tL4ndQavEi51mI38AjEAi/V3bNTIZargCyzuFJ0nN6T5U6VR5CmD1/iQMVtCnwr1
        /q4AaOeMSQ+2b1tbFfLn
        -----END CERTIFICATE-----
        """.trimIndent()
    )

    private fun logD(msg: String) {
        try { Log.d(TAG, msg) } catch (_: Throwable) {}
    }
    private fun logW(msg: String) {
        try { Log.w(TAG, msg) } catch (_: Throwable) {}
    }
    private fun logE(msg: String, tr: Throwable? = null) {
        try { Log.e(TAG, msg, tr) } catch (_: Throwable) {}
    }
    private fun logI(msg: String) {
        try { Log.i(TAG, msg) } catch (_: Throwable) {}
    }

    private val cachedTrustManager: X509TrustManager by lazy {
        createCompositeTrustManager()
    }

    fun getTrustManager(): X509TrustManager = cachedTrustManager

    fun configureOkHttpClient(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        return try {
            val trustManager = getTrustManager()
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
            builder.sslSocketFactory(sslContext.socketFactory, trustManager)
        } catch (e: Exception) {
            logE("Failed to configure custom SSLSocketFactory: ${e.message}", e)
            builder
        }
    }

    private fun createCompositeTrustManager(): X509TrustManager {
        // 1. Alapértelmezett platform TrustManager (Android rendszer beépített gyökértára)
        val defaultTm = try {
            val defaultTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            defaultTmf.init(null as KeyStore?)
            defaultTmf.trustManagers.firstOrNull { it is X509TrustManager } as? X509TrustManager
        } catch (e: Throwable) {
            logW("Could not initialize default platform TrustManager: ${e.message}")
            null
        }

        // 2. Beágyazott egyetemi / intézményi TrustManager (HARICA, GEANT, e-Szignó, ISRG)
        val bundledTm = try {
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            val certFactory = CertificateFactory.getInstance("X.509")

            BUNDLED_CERTIFICATES.forEachIndexed { index, pem ->
                try {
                    val cert = certFactory.generateCertificate(
                        ByteArrayInputStream(pem.trim().toByteArray(Charsets.UTF_8))
                    ) as X509Certificate
                    val alias = "bundled_cert_$index"
                    keyStore.setCertificateEntry(alias, cert)
                    logD("Loaded bundled certificate #$index: ${cert.subjectDN.name}")
                } catch (e: Throwable) {
                    logW("Could not load bundled certificate #$index: ${e.message}")
                }
            }

            val bundledTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            bundledTmf.init(keyStore)
            bundledTmf.trustManagers.firstOrNull { it is X509TrustManager } as? X509TrustManager
        } catch (e: Throwable) {
            logE("Failed to build bundled TrustManager: ${e.message}", e)
            null
        }

        // 3. AndroidCAStore TrustManager (felhasználói tanúsítványok, pl. AdGuard, egyetemi VPN-ek esetén)
        val userTm = try {
            val caStore = KeyStore.getInstance("AndroidCAStore")
            caStore.load(null, null)
            val caTmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            caTmf.init(caStore)
            caTmf.trustManagers.firstOrNull { it is X509TrustManager } as? X509TrustManager
        } catch (e: Throwable) {
            logD("AndroidCAStore not accessible or not supported in this environment: ${e.message}")
            null
        }

        val managers = listOfNotNull(defaultTm, bundledTm, userTm)
        if (managers.isEmpty()) {
            throw IllegalStateException("No X509TrustManager could be created")
        }

        return CompositeX509TrustManager(managers)
    }

    private class CompositeX509TrustManager(
        private val trustManagers: List<X509TrustManager>
    ) : X509TrustManager {

        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            var lastException: CertificateException? = null
            for (tm in trustManagers) {
                try {
                    tm.checkClientTrusted(chain, authType)
                    return
                } catch (e: CertificateException) {
                    lastException = e
                }
            }
            throw lastException ?: CertificateException("Client certificate validation failed")
        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            // Conscrypt passes "GENERIC" for TLS 1.3 handshakes, which standard JSSE TrustManagers reject.
            // Normalize to the public key algorithm (e.g. RSA, EC) if "GENERIC" or blank.
            val normalizedAuthType = if (authType.equals("GENERIC", ignoreCase = true) || authType.isNullOrBlank()) {
                val algo = chain?.firstOrNull()?.publicKey?.algorithm ?: "RSA"
                if (algo.equals("EC", ignoreCase = true)) "ECDSA" else algo
            } else {
                authType
            }

            var lastException: Exception? = null
            for ((index, tm) in trustManagers.withIndex()) {
                try {
                    tm.checkServerTrusted(chain, normalizedAuthType)
                    if (index > 0) {
                        logI("Server certificate validated via secondary trust manager (#$index)")
                    }
                    return
                } catch (e: Exception) {
                    lastException = e
                }
            }
            logE("Server certificate rejected by all trust managers (${trustManagers.size})", lastException)
            if (lastException is CertificateException) {
                throw lastException
            } else {
                throw CertificateException("Server certificate rejected: ${lastException?.message}", lastException)
            }
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            val issuers = mutableListOf<X509Certificate>()
            for (tm in trustManagers) {
                val acc = tm.acceptedIssuers
                if (acc != null) {
                    issuers.addAll(acc)
                }
            }
            return issuers.toTypedArray()
        }
    }
}
