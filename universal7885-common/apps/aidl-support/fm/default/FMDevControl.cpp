// Copyright (C) 2021 Eureka Team
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "FMDevControl.h"

#include <fm_slsi-impl.h>

#include <android/binder_manager.h>

#include <cassert>
#include <cerrno>

#include <fcntl.h>

#include "CommonMacro.h"

#include <aidl/vendor/eureka/hardware/audio_route/BnAudioRoute.h>

namespace aidl::vendor::eureka::hardware::fmradio {

::ndk::ScopedAStatus FMDevControl::open(void) {
	fd = fm_radio_slsi::open_device();
	assert(fd > 0);
	fm_radio_slsi::bootctrl(fd);
	return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus FMDevControl::getValue(GetType type, int *_aidl_return) {
	RETURN_IF_FAILED_LOCK;
	assert(fd > 0);
	switch (type) {
		case GetType::GET_TYPE_FM_FREQ:
			int64_t freq;
			fm_radio_slsi::get_frequency(fd, &freq);
			*_aidl_return = freq;
			break;
		case GetType::GET_TYPE_FM_UPPER_LIMIT:
			*_aidl_return = fm_radio_slsi::get_upperband_limit(fd);
			break;
		case GetType::GET_TYPE_FM_LOWER_LIMIT:
			*_aidl_return = fm_radio_slsi::get_lowerband_limit(fd);
			break;
		case GetType::GET_TYPE_FM_RMSSI:
			*_aidl_return = fm_radio_slsi::get_rmssi(fd);
			break;
		case GetType::GET_TYPE_FM_BEFORE_CHANNEL:
			*_aidl_return = fm_radio_slsi::before_channel(fd);
			break;
		case GetType::GET_TYPE_FM_NEXT_CHANNEL:
			*_aidl_return = fm_radio_slsi::next_channel(fd);
			break;
		case GetType::GET_TYPE_FM_SYSFS_IF:
			NOT_SUPPORTED;
		default:
			break;
	};
	lock.unlock();
	return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus FMDevControl::setValue(SetType type, int value) {
	RETURN_IF_FAILED_LOCK;
	assert(fd > 0);
	std::shared_ptr<audio_route::IAudioRoute> svc;
	switch (type) {
		case SetType::SET_TYPE_FM_FREQ:
			fm_radio_slsi::set_frequency(fd, value);
			break;
		case SetType::SET_TYPE_FM_MUTE:
			fm_radio_slsi::set_mute(fd, value);
			break;
		case SetType::SET_TYPE_FM_VOLUME:
			fm_radio_slsi::set_volume(fd, value);
			break;
		case SetType::SET_TYPE_FM_THREAD:
			fm_radio_slsi::fm_thread_set(fd, value);
			break;
		case SetType::SET_TYPE_FM_RMSSI:
			fm_radio_slsi::set_rssi(fd, value);
			break;
		case SetType::SET_TYPE_FM_SEARCH_CANCEL:
			fm_radio_slsi::stop_search(fd);
			break;
		case SetType::SET_TYPE_FM_SPEAKER_ROUTE:
			svc = audio_route::IAudioRoute::fromBinder(ndk::SpAIBinder(AServiceManager_waitForService("vendor.eureka.hardware.audio_route.IAudioRoute/default")));
			svc->setParam(value ? "routing=2": "routing=8");
			break;
		default:
			break;
	};
	lock.unlock();
	return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus FMDevControl::getFreqsList(std::vector<int> *_aidl_return){
	RETURN_IF_FAILED_LOCK;
	auto vec = fm_radio_slsi::get_freqs(fd);
	for (auto i : vec)
		_aidl_return->push_back(i);

	lock.unlock();
	return ::ndk::ScopedAStatus::ok();
}

::ndk::ScopedAStatus FMDevControl::close() {
	if (fd > 0) ::close(fd);
	fd = -1;
	return ::ndk::ScopedAStatus::ok();
}
} // namespace aidl::vendor::eureka::hardware::fmradio
