package com.hantalk.koreanai

import android.app.Application

/**
 * Nothing heavy happens here on purpose: the optional model is loaded lazily
 * from the ViewModel so a cold start stays fast.
 */
class HanTalkApplication : Application()
