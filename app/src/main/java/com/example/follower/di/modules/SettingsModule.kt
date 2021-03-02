package com.example.follower.di.modules

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AlertDialog
import androidx.work.WorkManager
import com.example.follower.R
import com.example.follower.di.scopes.SettingsScope
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.screens.settings.AutoTrackingSchedulingUseCase
import com.example.follower.screens.settings.SettingsPrefsInteractor
import com.example.follower.screens.settings.BiometricAvailabilityValidationUseCase
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Named

const val DIALOG_RESET_TO_DEFAULTS = "reset"
const val DIALOG_LOADING = "loading"

@Module
class SettingsModule(private val activity: Activity, private val resetToDefaults: () -> Unit) {
    @Provides
    @SettingsScope
    @Named(DIALOG_RESET_TO_DEFAULTS)
    fun provideResetToDefaultsDialog(ctx: Context): Dialog = MaterialAlertDialogBuilder(activity).create()
        .apply {
            setTitle(ctx.getString(R.string.title_reset_to_defaults))
            setButton(AlertDialog.BUTTON_POSITIVE, ctx.getString(R.string.title_continue)) { dialog, _ -> resetToDefaults.invoke().also { dialog.dismiss() } }
            setButton(AlertDialog.BUTTON_NEGATIVE, ctx.getString(android.R.string.cancel)) { dialog, _ -> dialog.dismiss() }
        }

    @Provides
    @SettingsScope
    fun provideAutoTrackingSchedulingUseCase(prefs: SharedPreferences, ctx: Context, composers: BaseComposers, workManager: WorkManager): AutoTrackingSchedulingUseCase = AutoTrackingSchedulingUseCase(prefs, ctx, composers, workManager)

    @Provides
    @SettingsScope
    fun provideBiometricAvailabilityValidator(context: Context, composers: BaseComposers): BiometricAvailabilityValidationUseCase = BiometricAvailabilityValidationUseCase(context, composers)

    @Provides
    @SettingsScope
    fun provideSettingsPrefsInteractor(baseComposers: BaseComposers, sharedPreferences: SharedPreferences, context: Context) = SettingsPrefsInteractor(baseComposers, sharedPreferences, context)

    @Provides
    @SettingsScope
    @Named(DIALOG_LOADING)
    fun provideLoadingDialog(): Dialog = Dialog(activity, R.style.DialogNoPaddingNoTitle).apply {
        setContentView(R.layout.dialog_saving)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}