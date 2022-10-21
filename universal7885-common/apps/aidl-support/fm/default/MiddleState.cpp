#include "MiddleState.h"

middlestate_t *saveMiddleState(const int value, const std::vector<int> &vec)
{
	unsigned int i;
	for (i = 0; i < vec.size() - 1; i++) {
		const int first = vec[i] - value;
		const int second = vec[i + 1] - value;
		if (first * second < 0)
			break;
	}

	return new middlestate_t {i, i + 1};
}
