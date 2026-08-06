package com.genciptv.player.core.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import java.io.File
import com.genciptv.player.R

/**
 * Hands a downloaded APK to the system package installer.
 *
 * Signature enforcement stays with the platform — we never accept an APK the
 * installer would reject. What [validate] adds is a *readable reason*: left to
 * itself the installer only says "Uygulama yüklenmedi", which tells the user
 * nothing about a downgrade or a mismatched signing key.
 */
object ApkInstaller {

    private const val TAG = "GencIPTV/Update"
    private const val MIME_APK = "application/vnd.android.package-archive"

    /** Outcome of inspecting a downloaded APK before handing it over. */
    sealed interface Check {
        data object Ok : Check
        /** Turkish, user-facing. */
        data class Problem(val message: String) : Check
    }

    /**
     * Reads the downloaded APK's manifest and compares it with the running
     * app. Catches the three failures that all surface as the same opaque
     * system error: a corrupt download, a version downgrade, and an APK signed
     * with a different key.
     */
    @Suppress("DEPRECATION")
    fun validate(context: Context, file: File): Check {
        if (!file.exists() || file.length() <= 0L) {
            return Check.Problem(
                context.getString(R.string.install_error_missing_file)
            )
        }

        val pm = context.packageManager
        val sigFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            PackageManager.GET_SIGNATURES
        }

        val apk: PackageInfo = runCatching {
            pm.getPackageArchiveInfo(file.absolutePath, sigFlag)
        }.getOrNull() ?: return Check.Problem(
            context.getString(R.string.install_error_unreadable)
        )

        if (apk.packageName != context.packageName) {
            return Check.Problem(
                context.getString(R.string.install_error_wrong_package, apk.packageName)
            )
        }

        val installed = runCatching { pm.getPackageInfo(context.packageName, sigFlag) }.getOrNull()
        if (installed != null) {
            val newCode = PackageInfoCompat.getLongVersionCode(apk)
            val oldCode = PackageInfoCompat.getLongVersionCode(installed)
            if (newCode < oldCode) {
                return Check.Problem(
                    context.getString(R.string.install_error_downgrade, newCode, oldCode)
                )
            }
            if (!signaturesMatch(installed, apk)) {
                return Check.Problem(
                    context.getString(R.string.install_error_signature)
                )
            }
        }
        return Check.Ok
    }

    /**
     * Compares the signing certificates of the installed app and the APK. On
     * API 28+ the signer list lives in `signingInfo`; below that in the
     * deprecated `signatures` array.
     */
    @Suppress("DEPRECATION")
    private fun signaturesMatch(installed: PackageInfo, apk: PackageInfo): Boolean {
        fun certs(info: PackageInfo): Set<String> {
            val raw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val si = info.signingInfo
                when {
                    si == null -> info.signatures
                    si.hasMultipleSigners() -> si.apkContentsSigners
                    else -> si.signingCertificateHistory
                }
            } else {
                info.signatures
            }
            return raw?.mapNotNull { it?.toCharsString() }?.toSet().orEmpty()
        }

        val a = certs(installed)
        val b = certs(apk)
        // Unknown on either side → don't block; let the installer decide.
        if (a.isEmpty() || b.isEmpty()) return true
        return a.intersect(b).isNotEmpty()
    }

    /**
     * Whether this app may start a package install. Below API 26 the
     * permission is granted at install time, so there is nothing to ask for.
     */
    fun canInstall(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }

    /** Opens the per-app "install unknown apps" screen. No-op below API 26. */
    fun requestPermission(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        runCatching {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }.onFailure { e ->
            Log.w(TAG, "Could not open unknown-app-sources settings", e)
        }
    }

    /**
     * Launches the installer for [file]. Returns false when the intent could
     * not be started so the caller can surface an error instead of hanging on
     * a "ready to install" state forever.
     */
    fun install(context: Context, file: File): Boolean = runCatching {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, MIME_APK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    }.getOrElse { e ->
        Log.w(TAG, "Launching the package installer failed", e)
        false
    }
}
