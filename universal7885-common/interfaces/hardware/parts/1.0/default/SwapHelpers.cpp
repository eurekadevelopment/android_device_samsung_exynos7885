#include <cstring>
#include <cerrno>
#include <fcntl.h>
#include <iostream>
#include <cstdio>
#include <sys/stat.h>
#include <sys/swap.h>
#include <sys/types.h>
#include <unistd.h>
#include <fstream>
#include <vector>
/* XXX This needs to be obtained from kernel headers. See b/9336527 */
struct linux_swap_header {
  char bootbits[1024]; /* Space for disklabel etc. */
  u_int32_t version;
  u_int32_t last_page;
  u_int32_t nr_badpages;
  unsigned char sws_uuid[16];
  unsigned char sws_volume[16];
  u_int32_t padding[117];
  u_int32_t badpages[1];
};
void mkfile(int filesize, std::string name){
    std::vector<char> empty(1024, 0);
    std::ofstream ofs(name, std::ios::binary | std::ios::out);

    for(int i = 0; i < 1024 * filesize; i++)
    {
        ofs.write(&empty[0], empty.size());
    }
}
#define MAGIC_SWAP_HEADER "SWAPSPACE2"
#define MAGIC_SWAP_HEADER_LEN 10
#define MIN_PAGES 10
int mkswap(std::string filename) {
  int err = 0;
  int fd;
  ssize_t len;
  off_t swap_size;
  int pagesize;
  struct linux_swap_header sw_hdr;
  fd = open(filename.c_str(), O_WRONLY | O_CLOEXEC);
  if (fd < 0) {
    err = errno;
    return err;
  }
  pagesize = getpagesize();
  /* Determine the length of the swap file */
  swap_size = lseek(fd, 0, SEEK_END);
  if (swap_size < MIN_PAGES * pagesize) {
    err = -ENOSPC;
    goto err;
  }
  if (lseek(fd, 0, SEEK_SET)) {
    err = errno;
    goto err;
  }
  memset(&sw_hdr, 0, sizeof(sw_hdr));
  sw_hdr.version = 1;
  sw_hdr.last_page = (swap_size / pagesize) - 1;
  len = write(fd, &sw_hdr, sizeof(sw_hdr));
  if (len != sizeof(sw_hdr)) {
    err = errno;
    goto err;
  }
  /* Write the magic header */
  if (lseek(fd, pagesize - MAGIC_SWAP_HEADER_LEN, SEEK_SET) < 0) {
    err = errno;
    goto err;
  }
  len = write(fd, MAGIC_SWAP_HEADER, MAGIC_SWAP_HEADER_LEN);
  if (len != MAGIC_SWAP_HEADER_LEN) {
    err = errno;
    goto err;
  }
  if (fsync(fd) < 0) {
    err = errno;
    goto err;
  }
err:
  close(fd);
  return err;
}
