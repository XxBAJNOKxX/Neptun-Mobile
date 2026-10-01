package com.example.core.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Biometrikus (ujjlenyomat / arcfelismerés) és eszköz-hitelesítés (PIN / minta / jelszó) zár kezelése.
 */
object BiometricLockHelper {

    private const val AUTHENTICATORS_WITH_CREDENTIAL =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL

    /** Elérhető-e biometrikus vagy eszköz-hitelesítés (PIN / minta / jelszó) az eszközön. */
    fun canAuthenticate(activity: FragmentActivity): Boolean {
        val manager = BiometricManager.from(activity)
        return manager.canAuthenticate(AUTHENTICATORS_WITH_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS ||
            manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun showPrompt(
        activity: FragmentActivity,
        title: String = "Neptun Mobile feloldása",
        subtitle: String = "Erősítsd meg az azonosságod a folytatáshoz",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val manager = BiometricManager.from(activity)
        val supportsDeviceCredential = manager.canAuthenticate(
            AUTHENTICATORS_WITH_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS

        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString())
                }
            }
        )

        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)

        if (supportsDeviceCredential) {
            builder.setAllowedAuthenticators(AUTHENTICATORS_WITH_CREDENTIAL)
        } else {
            builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .setNegativeButtonText("Mégse")
        }

        prompt.authenticate(builder.build())
    }
}
