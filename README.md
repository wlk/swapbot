# swapbot

A Bitmarket swapbot

# Building

```
sbt universal:packageBin
```

# Deployment

```
scp target/universal/swapbot-1.0.zip root@xyz2.varwise.com:~/
```

# Running

```
export NEW_RELIC_LICENSE_KEY=ABC
unzip swapbot-1.0.zip 
cd swapbot-1.0/bin
./swapbot 
```