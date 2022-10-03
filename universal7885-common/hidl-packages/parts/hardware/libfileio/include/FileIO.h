namespace FileIO {
  int readline(const char *path);

  template <typename T>
  void writeline(const char *path, const T data);
} // namespace FileIO
