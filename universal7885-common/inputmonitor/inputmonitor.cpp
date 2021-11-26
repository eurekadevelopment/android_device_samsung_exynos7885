/*
 * Copyright (C) 2021 Soo-Hean Na "Royna"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <linux/input.h>
#include <string.h>
#include <stdio.h>
#include <thread>
#include <glob.h>
#include <vector>
#include <chrono>
#include <thread>
#define LOG_TAG "InputMonitor"
#include <utils/Log.h>
#include <algorithm>

static const char *const evval[3] = {
    "RELEASED",
    "PRESSED ",
    "REPEATED"
};

using std::vector;
using std::string;
using namespace std::this_thread; // sleep_for, sleep_until
using namespace std::chrono; // nanoseconds, system_clock, seconds

vector<string> globVector(const string& pattern){
    glob_t glob_result;
    glob(pattern.c_str(), GLOB_TILDE, NULL, &glob_result);
    vector<string> files;
    for(unsigned int i = 0;i < glob_result.gl_pathc; ++i){
        files.push_back(string(glob_result.gl_pathv[i]));
    }
    globfree(&glob_result);
    return files;
}
int detect_totalevents(){
     vector<string> inputs = globVector("/sys/devices/virtual/input/");
     int count = 0;
     for (int i = 0; i < inputs.size(); i++){
     	if (inputs[i].rfind("input", 0) == 0) {
     		count++;
     	}
     }
     return count;
}    

char get_eventname(const char& sysfs_input){
     int fd;
     char name;
     ssize_t n;
     fd = open(&sysfs_input, O_RDONLY);
     if (fd == -1) {
        ALOGE("Cannot open %c: errno %s.", sysfs_input, strerror(errno));
        return EXIT_FAILURE;
     }
     n = read(fd, &name, sizeof name);
     if (n == (ssize_t) -1) {
     	ALOGE("Cannot read from %c.", sysfs_input);
     }
     return name;
}
     
void monitor_input(const char& dev_node, const char& sysfs_input)
{
    struct input_event ev;
    ssize_t n;
    int fd;
    bool fail;
    fd = open(&dev_node, O_RDONLY);
    if (fd == -1) {
        ALOGE("Cannot open %c: errno %s.", dev_node, strerror(errno));
    }
    fail = false;
    while (fd != -1) {
        n = read(fd, &ev, sizeof ev);
        if (n == (ssize_t)-1) {
            if (errno == EINTR)
                continue;
            else
            	fail = true;
                break;
        } else if (n != sizeof ev) {
            errno = EIO;
            break;
	}
	if (ev.type == EV_KEY && ev.value >= 0 && ev.value <= 2){
            ALOGI("%c (dev: %c) got keyevent %s (Keycode %d)", get_eventname(sysfs_input), dev_node, evval[ev.value], (int) ev.code);
        }
        sleep_until(system_clock::now() + milliseconds(100));

    }
    if (fail){
    	ALOGE("Monitoring %c failed, errno = %s", dev_node, strerror(errno));
    }
}
int main(){
	int totalevents = detect_totalevents();
	vector<string> eventlist = globVector("/sys/devices/virtual/input/");
	std::vector<string>::iterator position = std::find(eventlist.begin(), eventlist.end(), "mice");
	if (position != eventlist.end()) // == eventlist.end() means the element was not found
		eventlist.erase(position);
	vector<std::thread> threads;
	for (int k = 0; k < totalevents; k++){
		string number = eventlist[k].substr(5, 6);
		auto dev_node = "/dev/input/event" + number;
		auto sysfs_node = "/sys/devices/virtual/input/input" + number + "/name";
		threads.push_back(std::thread(monitor_input, *dev_node.c_str(), *sysfs_node.c_str()));
	}
	for (int k = 0; k < totalevents; k++){
		threads[k].join();
	}
}
	

