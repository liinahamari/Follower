package dev.liinahamari.follower.di.modules

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.Module
import dagger.Provides
import dev.liinahamari.follower.R
import dev.liinahamari.follower.helper.rx.BaseComposers
import dev.liinahamari.follower.screens.settings.BiometricAvailabilityValidationUseCase
import dev.liinahamari.follower.screens.settings.PurgeCacheUseCase
import dev.liinahamari.follower.screens.settings.ResetPrefsToDefaultsInteractor
import dev.liinahamari.follower.screens.settings.SettingsScope
import javax.inject.Named

const val DIALOG_RESET_TO_DEFAULTS = "reset"
const val DIALOG_LOADING = "loading"
const val DIALOG_ROOT_DETECTED = "root_detected"

@Module
class SettingsModule(
    private val activity: Activity,
    private val resetToDefaults: () -> Unit,
    private val onAcceptDeviceRooted: () -> Unit,
    private val onDeclineDeviceRooted: () -> Unit
) {
    @Provides
    @SettingsScope
    @Named(DIALOG_RESET_TO_DEFAULTS)
    fun provideResetToDefaultsDialog(@Named(APP_CONTEXT) ctx: Context): Dialog = MaterialAlertDialogBuilder(activity).create()
        .apply {
            setTitle(ctx.getString(R.string.title_reset_to_defaults))
            setButton(AlertDialog.BUTTON_POSITIVE, ctx.getString(R.string.title_continue)) { dialog, _ -> resetToDefaults.invoke().also { dialog.dismiss() } }
            setButton(AlertDialog.BUTTON_NEGATIVE, ctx.getString(android.R.string.cancel)) { dialog, _ -> dialog.dismiss() }
        }

    @Provides
    @SettingsScope
    @Named(DIALOG_ROOT_DETECTED)
    fun provideRootDetectionDialog(@Named(APP_CONTEXT) ctx: Context): Dialog = MaterialAlertDialogBuilder(activity).create()
        .apply {
            setCancelable(false)
            setTitle(ctx.getString(R.string.title_root_detected))
            setMessage(ctx.getString(R.string.summary_root_detected))
            setButton(AlertDialog.BUTTON_POSITIVE, ctx.getString(R.string.title_i_accept_risks)) { dialog, _ ->
                onAcceptDeviceRooted.invoke()
                dialog.dismiss()
            }
            setButton(AlertDialog.BUTTON_NEGATIVE, ctx.getString(android.R.string.cancel)) { dialog, _ ->
                onDeclineDeviceRooted.invoke()
                dialog.dismiss()
            }
        }

    @Provides
    @SettingsScope
    fun provideBiometricAvailabilityValidator(@Named(APP_CONTEXT) context: Context, composers: BaseComposers): BiometricAvailabilityValidationUseCase = BiometricAvailabilityValidationUseCase(context, composers)

    @Provides
    @SettingsScope
    fun providePurgeCacheUseCase(@Named(APP_CONTEXT) context: Context, composers: BaseComposers): PurgeCacheUseCase = PurgeCacheUseCase(context, composers)

    @Provides
    @SettingsScope
    fun provideSettingsPrefsInteractor(baseComposers: BaseComposers, sharedPreferences: SharedPreferences, @Named(APP_CONTEXT) context: Context) = ResetPrefsToDefaultsInteractor(baseComposers, sharedPreferences, context)

    @Provides
    @SettingsScope
    @Named(DIALOG_LOADING)
    fun provideLoadingDialog(): Dialog = Dialog(activity, R.style.DialogNoPaddingNoTitle).apply {
        setContentView(R.layout.dialog_saving)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}