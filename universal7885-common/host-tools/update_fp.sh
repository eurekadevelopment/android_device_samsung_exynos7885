#!/bin/bash

git config --local user.email "41898282+github-actions[bot]@users.noreply.github.com"
git config --local user.name "github-actions[bot]"

sudo apt update
sudo apt install -y libcurl4-openssl-dev clang
cd universal7885-common/host-tools/
clang++ BuildFpUpdater.cc -o updater -lcurl
./updater
git add ..
git commit -m "universal7885: Update fp [`date`]"
git push
