package com.example.follower.screens.show_trace

import dagger.Subcomponent

@ShowTraceScope
@Subcomponent(modules = [ShowTraceModule::class])
interface ShowTraceComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): ShowTraceComponent
    }

    fun inject(activity: ShowTraceActivity)
}