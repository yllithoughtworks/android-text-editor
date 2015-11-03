#!/bin/bash

clear

rm classes.dex

echo "All files will be converted, $USER!"


FILES=*.jar

echo $FILES


for i in $FILES

do

      ~/Library/Android/sdk/build-tools/23.0.0/dx --dex --output=classes.dex $i

      echo "dx --dex --output=classes.dex $i"

      ~/Library/Android/sdk/build-tools/23.0.0/aapt add $i classes.dex

      rm classes.dex

done