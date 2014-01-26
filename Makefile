CC=gcc
CFLAGS=-Wall -lbluetooth

all:
	$(CC) $(CFLAGS) zeroadv.c -o zeroadv

clean:
	rm -rf zeroadv