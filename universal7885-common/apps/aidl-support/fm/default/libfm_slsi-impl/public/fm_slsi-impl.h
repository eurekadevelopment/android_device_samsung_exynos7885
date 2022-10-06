#pragma once

#include <memory>

namespace fm_radio_slsi {

int open_device(void);
int get_frequency(const int fd, int64_t *channel);
int set_frequency(const int fd, int64_t channel);
int seek_frequency(int fd, unsigned int upward, unsigned int wrap_around,
                   unsigned int spacing);
int channel_search(const int fd, unsigned int upward, unsigned int wrap_around,
                   unsigned int spacing, int64_t *channel);
int set_mute(const int fd, bool mute);
int set_volume(int fd, int volume);
std::unique_ptr<int64_t[]> get_freqs(const int fd);
void fm_thread_set(const int fd, const bool enable);
unsigned int get_upperband_limit(int fd);
unsigned int get_lowerband_limit(int fd);
int64_t get_rmssi(int fd);
int set_rssi(int fd, int64_t rssi);
void bootctrl(const int fd);
void stop_search(const int fd);
} // namespace fm_radio_slsi
