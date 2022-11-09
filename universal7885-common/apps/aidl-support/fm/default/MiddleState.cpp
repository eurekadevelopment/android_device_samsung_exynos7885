#include "MiddleState.h"

middlestate_t *saveMiddleState(const int value, const std::vector<int> &vec) {
  unsigned int i;

  // Bounds checking, we need at least 2 elements on the vector.
  // If it doesn't meet the requirements, return nullptr to keep it
  // as-is.
  if (vec.size() <= 1)
    return nullptr;

  for (i = 0; i < vec.size() - 1; i++) {
    const int first = vec[i] - value;
    const int second = vec[i + 1] - value;
    if (first * second < 0)
      break;
  }

  return new middlestate_t{i, i + 1};
}
