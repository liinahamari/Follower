package com.example.follower.di.modules

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import androidx.work.WorkManager
import com.example.follower.R
import com.example.follower.di.scopes.SettingsScope
import com.example.follower.helper.rx.BaseComposers
import com.example.follower.interactors.AutoTrackingSchedulingUseCase
import com.example.follower.interactors.SettingsPrefsInteractor
import dagger.Module
import dagger.Provides

@Module
class SettingsModule {
    @Provides
    @SettingsScope
    fun provideAutoTrackingSchedulingUseCase(prefs: SharedPreferences, ctx: Context, composers: BaseComposers, workManager: WorkManager): AutoTrackingSchedulingUseCase = AutoTrackingSchedulingUseCase(prefs, ctx, composers, workManager)

    @Provides
    @SettingsScope
    fun provideSettingsPrefsInteractor(baseComposers: BaseComposers, sharedPreferences: SharedPreferences, context: Context) = SettingsPrefsInteractor(baseComposers, sharedPreferences, context)

    @Provides
    @SettingsScope
    fun provideLoadingDialog(ctx: Context): Dialog = Dialog(ctx, R.style.DialogNoPaddingNoTitle).apply {
        setContentView(R.layout.dialog_saving)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}