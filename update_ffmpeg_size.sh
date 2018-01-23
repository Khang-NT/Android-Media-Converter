#!/bin/bash
chmod +x "app/src/arm/assets/ffmpeg"
chmod +x "app/src/arm7/assets/ffmpeg"
chmod +x "app/src/arm8/assets/ffmpeg"
chmod +x "app/src/mips/assets/ffmpeg"
chmod +x "app/src/mips64/assets/ffmpeg"
chmod +x "app/src/x86/assets/ffmpeg"
chmod +x "app/src/x86_64/assets/ffmpeg"

stat -f%z "app/src/arm/assets/ffmpeg" > app/src/arm/assets/ffmpeg_size.txt
stat -f%z "app/src/arm7/assets/ffmpeg" > app/src/arm7/assets/ffmpeg_size.txt
stat -f%z "app/src/arm8/assets/ffmpeg" > app/src/arm8/assets/ffmpeg_size.txt
stat -f%z "app/src/mips/assets/ffmpeg" > app/src/mips/assets/ffmpeg_size.txt
stat -f%z "app/src/mips64/assets/ffmpeg" > app/src/mips64/assets/ffmpeg_size.txt
stat -f%z "app/src/x86/assets/ffmpeg" > app/src/x86/assets/ffmpeg_size.txt
stat -f%z "app/src/x86_64/assets/ffmpeg" > app/src/x86_64/assets/ffmpeg_size.txt
