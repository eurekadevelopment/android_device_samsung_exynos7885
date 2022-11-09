#include <vector>

#pragma once

struct pair {
  unsigned int first;
  unsigned int second;
};

using middlestate_t = struct pair;

middlestate_t *saveMiddleState(const int value, const std::vector<int> &vec);
