#include <dlfcn.h>
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[]) {
  const char *path;
  void *handle = NULL;
  int ret = EXIT_FAILURE;

  if (argc <= 1) {
    printf("Please specify a module to load!\n");
    return ret;
  }

  path = argv[1];

  handle = dlopen(path, RTLD_NOW);
  if (handle == NULL) {
    const char *err_str = dlerror();
    printf("load: module=%s\n%s\n", path, err_str ? err_str : "unknown");
  } else {
    printf("load: module=%s %s\n", path, "Success!");
    ret = EXIT_SUCCESS;
  }
  return ret;
}
