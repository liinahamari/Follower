@file:Suppress("unused", "PropertyName", "EnumEntryName")

package dev.liinahamari.follower.model

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder
import androidx.datastore.rxjava3.RxDataStore
import androidx.preference.PreferenceManager
import dev.liinahamari.follower.R
import dev.liinahamari.follower.di.modules.APP_CONTEXT
import dev.liinahamari.follower.ext.*
import dev.liinahamari.follower.helper.rx.BaseComposers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

private const val DEFAULT_AUTO_TRACKING_START_TIME = "09:00"
private const val DEFAULT_AUTO_TRACKING_END_TIME = "21:00"

private const val MIN_DISTANCE_BETWEEN_UPDATES = 10f
private const val MIN_TIME_INTERVAL_BETWEEN_UPDATES = 5L

private enum class LanguagesSupported { en, ru }

@ExperimentalCoroutinesApi
@Singleton
class PreferenceRepository(private val sharedPreferences: SharedPreferences, @Named(APP_CONTEXT) private val context: Context, baseComposers: BaseComposers) {
    private val dataStore: RxDataStore<Preferences> = RxPreferenceDataStoreBuilder(context, "preferences").build()

    // BOOLEAN PREFERENCES

    private val neverShowRateUsPref = booleanPreferencesKey(context.getString(R.string.pref_never_show_rate_app))
    val neverShowRateUs: Flowable<Boolean> = dataStore.data()
        .map { prefs: Preferences -> prefs[neverShowRateUsPref]!! }
        .onErrorReturnItem(false)
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateNeverShowRateUs(value: Boolean) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[neverShowRateUsPref] = value
                Single.just(this)
            }
        }
    }

    private val isFirstLaunchPref = booleanPreferencesKey(context.getString(R.string.pref_is_first_launch))
    val isFirstLaunch: Flowable<Boolean> = dataStore.data()
        .map { prefs: Preferences -> prefs[isFirstLaunchPref]!! }
        .onErrorReturnItem(false)
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateIsFirstLaunch(value: Boolean) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[isFirstLaunchPref] = value
                Single.just(this)
            }
        }
    }

    private val isIgnoringBatteryOptimizationsPref = booleanPreferencesKey(context.getString(R.string.pref_battery_optimization))
    val isIgnoringBatteryOptimizations: Flowable<Boolean> = dataStore.data()
        .map { prefs: Preferences -> prefs[isIgnoringBatteryOptimizationsPref]!! }
        .onErrorReturnItem(false)
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateIsIgnoringBatteryOptimizations(value: Boolean) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[isIgnoringBatteryOptimizationsPref] = value
                Single.just(this)
            }
        }
    }

    /*FIXME*/
    private val isAcraDisabledPref = booleanPreferencesKey(context.getString(R.string.pref_acra_disable))
    val isAcraDisabled: Flowable<Boolean> = dataStore.data()
        .map { prefs: Preferences -> prefs[isAcraDisabledPref]!! }
        .onErrorReturnItem(false)
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateIsAcraDisabled(value: Boolean) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[isAcraDisabledPref] = value
                Single.just(this)
            }
        }
    }

    private val isAutoTrackingEnabledPref = booleanPreferencesKey(context.getString(R.string.pref_enable_auto_tracking))
    val isAutoTrackingEnabled: Flowable<Boolean> = dataStore.data()
        .map { prefs: Preferences -> prefs[isAutoTrackingEnabledPref]!! }
        .onErrorReturnItem(false)
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateIsAutoTrackingEnabled(value: Boolean) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[isAutoTrackingEnabledPref] = value
                Single.just(this)
            }
        }
    }

    private val isBiometricProtectionEnabledPref = booleanPreferencesKey(context.getString(R.string.pref_enable_biometric_protection))
    val isBiometricProtectionEnabled: Flowable<Boolean> = dataStore.data()
        .map { prefs: Preferences -> prefs[isBiometricProtectionEnabledPref]!! }
        .onErrorReturnItem(false)
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateIsEnabledBiometricProtection(value: Boolean) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[isBiometricProtectionEnabledPref] = value
                Single.just(this)
            }
        }
    }

    private val isRootOkPref = booleanPreferencesKey(context.getString(R.string.pref_root_is_ok))
    val isRootOk: Flowable<Boolean> = dataStore.data()
        .map { prefs: Preferences -> prefs[isRootOkPref]!! }
        .onErrorReturnItem(false)
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateIsRootOk(value: Boolean) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[isRootOkPref] = value
                Single.just(this)
            }
        }
    }

    // END SECTION -- BOOLEAN PREFERENCES

    // STRING PREFERENCES

    /** Must be initialized in first launch in Application's subclass */
    private val uidPref = stringPreferencesKey(context.getString(R.string.pref_uid))
    val UID: Flowable<String> = dataStore.data()
        .map { prefs: Preferences -> prefs[uidPref]!! }
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateUid(value: String) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[uidPref] = value
                Single.just(this)
            }
        }
    }

    /** Represented as String cause preferences.xml doesn't apply int-array in ListPreference
     *  Must be initialized at first launch with
     *  @see AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM

     *  Must be one of follows:
     *  @see AppCompatDelegate.MODE_NIGHT_YES
     *  @see AppCompatDelegate.MODE_NIGHT_NO
     *  @see AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
     */
    //TODO if it != YES && != NO && != FOLLOW_SYSTEM, then FOLLOW SYSTEM
    private val themePref = stringPreferencesKey(context.getString(R.string.pref_theme))
    val theme: Flowable<Int> = dataStore.data()
        .map { prefs: Preferences -> prefs[themePref]!! }
        .map { it.toInt() }
        .onErrorReturnItem(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        .compose(baseComposers.applyFlowableSchedulers())

    val isDarkThemeEnabled = theme.map {
        return@map it == AppCompatDelegate.MODE_NIGHT_YES || (it == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM && context.deviceHasDarkThemeEnabled())
    }

    fun updateTheme(value: Int) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[themePref] = value.toString()
                Single.just(this)
            }
        }
    }

    /** Must be one of following:
     * @see LanguagesSupported.en
     * @see LanguagesSupported.ru
     * */
    private val languagePref = stringPreferencesKey(context.getString(R.string.pref_lang))
    val language: Flowable<Locale> = dataStore.data()
        .map { prefs: Preferences -> prefs[languagePref]!! }
        .map { Locale(it) }
        .onErrorResumeWith {
            val localeToPersist = Locale(context.resources.getStringArray(R.array.supported_locales).firstOrNull { it == Locale.getDefault().language } ?: Locale.UK.language)
            updateLanguage(localeToPersist.language)
            Single.just(localeToPersist)
        }
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateLanguage(value: String) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[languagePref] = value
                Single.just(this)
            }
        }
    }

    /** Value is String, as far as preferences.xml doesn't support Int values...
     * Value appeals to meters
     * */
    private val minDistanceBetweenUpdatesPref = stringPreferencesKey(context.getString(R.string.pref_min_distance))
    val minDistanceBetweenUpdates: Flowable<Float> = dataStore.data()
        .map { prefs: Preferences -> prefs[minDistanceBetweenUpdatesPref]!! }
        .map { it.toFloat() }
        .onErrorReturnItem(MIN_DISTANCE_BETWEEN_UPDATES)
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateMinDistanceBetweenUpdates(value: Float) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[minDistanceBetweenUpdatesPref] = value.toString()
                Single.just(this)
            }
        }
    }


    /** In seconds */
    private val minLocationUpdateIntervalPref = stringPreferencesKey(context.getString(R.string.pref_min_location_update_interval))
    val minLocationUpdateInterval: Flowable<Long> = dataStore.data()
        .map { prefs: Preferences -> prefs[minLocationUpdateIntervalPref]!! }
        .map { it.toLong() }
        .map { if(it == 0L) MIN_TIME_INTERVAL_BETWEEN_UPDATES else it }
        .map { it * 1000 }
        .onErrorReturnItem(MIN_TIME_INTERVAL_BETWEEN_UPDATES)
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateMinLocationUpdateInterval(value: Long) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[minLocationUpdateIntervalPref] = value.toString()
                Single.just(this)
            }
        }
    }

    /** Responsible for showing Track in TrackMapFragment

     * Must be one of following:
     * @see Context.getString(R.string.pref_line)
     * @see Context.getString(R.string.pref_marker_set)
     * */
    private val trackRepresentationPref = stringPreferencesKey(context.getString(R.string.pref_track_representation))
    val trackRepresentation: Flowable<String> = dataStore.data()
        .map { prefs: Preferences -> prefs[trackRepresentationPref]!! }
        .onErrorReturnItem(context.getString(R.string.pref_marker_set))
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateTrackRepresentation(value: String) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[trackRepresentationPref] = value
                Single.just(this)
            }
        }
    }

    /** Responsible for showing choice Dialog or represent Track as set of addresses or with the map.
     * Must be one of following:
     * @see Context.getString(R.string.title_always_ask)
     * @see Context.getString(R.string.title_map)
     * @see Context.getString(R.string.title_addresses_list)
     * */
    private val routeModePref = stringPreferencesKey(context.getString(R.string.pref_route_mode))
    val routeMode: Flowable<String> = dataStore.data()
        .map { prefs: Preferences -> prefs[routeModePref] }
        .onErrorReturnItem(context.getString(R.string.title_always_ask))
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateRouteMode(value: String) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[routeModePref] = value
                Single.just(this)
            }
        }
    }

    // END SECTION -- STRING PREFERENCES

    // INT PREFERENCES

    private val autoTrackingStartTimePref = intPreferencesKey(context.getString(R.string.pref_tracking_start_time))
    val autoTrackingStartTime: Flowable<Int?> = dataStore.data()
        .map { prefs: Preferences -> prefs[autoTrackingStartTimePref] }
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateAutoTrackingStartTime(value: Int) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[autoTrackingStartTimePref] = value
                Single.just(this)
            }
        }
    }

    private val autoTrackingStopTimePref = intPreferencesKey(context.getString(R.string.pref_tracking_stop_time))
    val autoTrackingStopTime: Flowable<Int?> = dataStore.data()
        .map { prefs: Preferences -> prefs[autoTrackingStopTimePref] }
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateAutoTrackingStopTime(value: Int) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[autoTrackingStopTimePref] = value
                Single.just(this)
            }
        }
    }

    /** Indicates how much times application was launched. Used for showing
     * @see dev.liinahamari.follower.screens.tracking_control.RateMyAppDialog
     * */
    private val appLaunchCounterPref = intPreferencesKey(context.getString(R.string.pref_app_launch_counter))
    val appLaunchCounter: Flowable<Int> = dataStore.data()
        .map { prefs: Preferences -> prefs[appLaunchCounterPref] }
        .onErrorReturnItem(1)
        .compose(baseComposers.applyFlowableSchedulers())

    fun updateAppLaunchCounter(value: Int) {
        dataStore.updateDataAsync { prefsIn ->
            with(prefsIn.toMutablePreferences()) {
                this[appLaunchCounterPref] = value
                Single.just(this)
            }
        }
    }

    // END SECTION -- INT PREFERENCES

    /*todo: to splash screen?*/
    fun applyDefaultPreferences() {
        isFirstLaunch.subscribe {
            if (it.not()) {
                updateIsFirstLaunch(true)
                updateIsIgnoringBatteryOptimizations(context.isIgnoringBatteryOptimizations())
                updateIsAcraDisabled(true)
                updateIsRootOk(false)
                updateUid(UUID.randomUUID().toString())
                updateAutoTrackingStartTime(hourlyTimeToMinutesFromMidnight(DEFAULT_AUTO_TRACKING_START_TIME))
                updateAutoTrackingStopTime(hourlyTimeToMinutesFromMidnight(DEFAULT_AUTO_TRACKING_END_TIME))

                PreferenceManager.setDefaultValues(context, R.xml.preferences, false)
            }
        }
    }
}