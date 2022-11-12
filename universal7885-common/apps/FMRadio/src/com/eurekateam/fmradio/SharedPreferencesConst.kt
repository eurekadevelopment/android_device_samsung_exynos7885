package com.eurekateam.fmradio

object SharedPreferencesConst {
    const val PREF_VOLUME = "volume"
    private const val PREF_FAV_FREQ_FMT = "fav_%d"
    const val PREF_AUDIO_OUTPUT = "speaker"
    const val PREF_FREQ = "freq"

    fun assembleFavFreq(freq: Int) = String.format(PREF_FAV_FREQ_FMT, freq)
}
