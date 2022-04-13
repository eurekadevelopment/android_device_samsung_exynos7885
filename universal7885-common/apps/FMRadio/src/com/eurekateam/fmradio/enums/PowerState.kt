package com.eurekateam.fmradio.enums

/**
 * [FM_POWER_OFF] : Turns off fm audio
 *
 * [FM_POWER_ON] : Turns on fm audio
 *
 * This enum includes audio parameter that can be written to A-BOX
 */
enum class PowerState(val mAudioParam: String) {
    FM_POWER_ON("l_fmradio_mode=on"),
    FM_POWER_OFF("l_fmradio_mode=off")
}
