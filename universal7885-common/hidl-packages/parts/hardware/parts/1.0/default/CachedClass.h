template <class C>
static inline C *cachedClass(C* cached) {
	if (cached != nullptr) {
		return cached;
	}
	cached = new C();
	return cached;
}

#define USE_CACHED(cache) 		\
({					\
 	return cachedClass((cache));	\
})

