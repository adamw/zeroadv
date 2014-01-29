#include <stdio.h>

#include <signal.h>
#include <errno.h>
#include <unistd.h>
#include <stdlib.h>

#include <sys/socket.h>

#include <bluetooth/bluetooth.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>

#include <zmq.h>

// START FROM hcitool.c (unchanged)

static volatile int signal_received = 0;

#define FLAGS_AD_TYPE 0x01
#define FLAGS_LIMITED_MODE_BIT 0x01
#define FLAGS_GENERAL_MODE_BIT 0x02

static void sigint_handler(int sig)
{
	signal_received = sig;
}

static int read_flags(uint8_t *flags, const uint8_t *data, size_t size)
{
	size_t offset;

	if (!flags || !data)
		return -EINVAL;

	offset = 0;
	while (offset < size) {
		uint8_t len = data[offset];
		uint8_t type;

		/* Check if it is the end of the significant part */
		if (len == 0)
			break;

		if (len + offset > size)
			break;

		type = data[offset + 1];

		if (type == FLAGS_AD_TYPE) {
			*flags = data[offset + 2];
			return 0;
		}

		offset += 1 + len;
	}

	return -ENOENT;
}

static int check_report_filter(uint8_t procedure, le_advertising_info *info)
{
	uint8_t flags;

	/* If no discovery procedure is set, all reports are treat as valid */
	if (procedure == 0)
		return 1;

	/* Read flags AD type value from the advertising report if it exists */
	if (read_flags(&flags, info->data, info->length))
		return 0;

	switch (procedure) {
	case 'l': /* Limited Discovery Procedure */
		if (flags & FLAGS_LIMITED_MODE_BIT)
			return 1;
		break;
	case 'g': /* General Discovery Procedure */
		if (flags & (FLAGS_LIMITED_MODE_BIT | FLAGS_GENERAL_MODE_BIT))
			return 1;
		break;
	default:
		fprintf(stderr, "Unknown discovery procedure\n");
	}

	return 0;
}

// END UNMODIFIED FROM hcitool.c

typedef struct {
	void (*fn)(uint8_t *, int, uint8_t, void *);
	void *cb_data;
} adv_callback;

static int scan_for_advertising_devices(int dd, uint8_t filter_type, adv_callback *callback)
{
	unsigned char buf[HCI_MAX_EVENT_SIZE], *ptr;
	struct hci_filter nf, of;
	struct sigaction sa;
	socklen_t olen;
	int len;

	olen = sizeof(of);
	if (getsockopt(dd, SOL_HCI, HCI_FILTER, &of, &olen) < 0) {
		printf("Could not get socket options\n");
		return -1;
	}

	hci_filter_clear(&nf);
	hci_filter_set_ptype(HCI_EVENT_PKT, &nf);
	hci_filter_set_event(EVT_LE_META_EVENT, &nf);

	if (setsockopt(dd, SOL_HCI, HCI_FILTER, &nf, sizeof(nf)) < 0) {
		printf("Could not set socket options\n");
		return -1;
	}

	memset(&sa, 0, sizeof(sa));
	sa.sa_flags = SA_NOCLDSTOP;
	sa.sa_handler = sigint_handler;
	sigaction(SIGINT, &sa, NULL);

	while (1) {
		evt_le_meta_event *meta;
		le_advertising_info *info;
		uint8_t rssi;

		while ((len = read(dd, buf, sizeof(buf))) < 0) {
			if (errno == EINTR && signal_received == SIGINT) {
				len = 0;
				goto done;
			}

			if (errno == EAGAIN || errno == EINTR)
				continue;
			goto done;
		}

		ptr = buf + (1 + HCI_EVENT_HDR_SIZE);
		len -= (1 + HCI_EVENT_HDR_SIZE);

		meta = (void *) ptr;

		if (meta->subevent != 0x02)
			goto done;

		/* Ignoring multiple reports */
		info = (le_advertising_info *) (meta->data + 1);
		if (check_report_filter(filter_type, info)) {			
			// the rssi is in the next byte after the packet
			rssi = info->data[info->length]; 
			callback->fn(info->data, info->length, rssi, callback->cb_data);
		}
	}

done:
	setsockopt(dd, SOL_HCI, HCI_FILTER, &of, sizeof(of));

	if (len < 0)
		return -1;

	return 0;
}

static void cmd_lescan(int dev_id, adv_callback *callback)
{
	int err, dd;
	uint8_t own_type = 0x00;
	uint8_t scan_type = 0x01;
	uint8_t filter_type = 0;
	uint8_t filter_policy = 0x00;
	uint16_t interval = htobs(0x0010);
	uint16_t window = htobs(0x0010);
	uint8_t filter_dup = 1;

	if (dev_id < 0)
		dev_id = hci_get_route(NULL);

	dd = hci_open_dev(dev_id);
	if (dd < 0) {
		perror("Could not open device");
		exit(1);
	}

	err = hci_le_set_scan_parameters(dd, scan_type, interval, window,
						own_type, filter_policy, 1000);
	if (err < 0) {
		perror("Set scan parameters failed");
		exit(1);
	}

	err = hci_le_set_scan_enable(dd, 0x01, filter_dup, 1000);
	if (err < 0) {
		perror("Enable scan failed");
		exit(1);
	}

	printf("LE Scan ...\n");

	err = scan_for_advertising_devices(dd, filter_type, callback);
	if (err < 0) {
		perror("Could not receive advertising events");
		exit(1);
	}

	err = hci_le_set_scan_enable(dd, 0x00, filter_dup, 1000);
	if (err < 0) {
		perror("Disable scan failed");
		exit(1);
	}

	hci_close_dev(dd);
}

//

/*
static void adv_callback_print_fn(uint8_t *data, int data_length, uint8_t rssi, void *ignore) {
	int i;

	printf("ADV PACKET: ");
	for (i=0; i<data_length; i++) {
		printf("%x ", data[i]);
	}
	printf("\n");
 
	printf("RSSI: %d dBm\n", rssi);

	printf("\n");	
}
*/

static void verify_all_sent(int should_send, int sent) {
	if (sent < 0) {
		perror("Error when sending data to socket");
		exit(1);
	}
	if (sent >= 0 && sent < should_send) {
		perror("Not all bytes sent");
		exit(1);
	}
}

static void send_to_socket(void *socket, void *data, int data_length, int flags) {
	verify_all_sent(data_length, zmq_send(socket, data, data_length, flags));
}

typedef struct {
	void *socket;
	char *hostname;
	int hostname_length;
} adv_callback_zmq_data;

static void adv_callback_zmq_fn(uint8_t *data, int data_length, uint8_t rssi, 
				void *_cb_data) {
	adv_callback_zmq_data *cb_data = (adv_callback_zmq_data *) _cb_data;

	send_to_socket(cb_data->socket, cb_data->hostname, cb_data->hostname_length, ZMQ_SNDMORE);
	send_to_socket(cb_data->socket, data, data_length, ZMQ_SNDMORE);
	send_to_socket(cb_data->socket, &rssi, 1, 0);
}

//

static void report_zmq_version() {
	int major, minor, patch;
	zmq_version (&major, &minor, &patch);
	printf ("Running 0MQ version: %d.%d.%d\n", major, minor, patch);
}

int main(int argc, char** argv) {
	char hostname[100];
	gethostname(hostname, sizeof(hostname));
	printf("Hostname: %s\n", hostname);

	// flush stdout immediately
	setvbuf(stdout, NULL, _IONBF, 0);

	report_zmq_version();

	void *context = zmq_ctx_new();
	void *publisher = zmq_socket(context, ZMQ_PUB);
	int rc = zmq_bind(publisher, "tcp://*:8916");
	if (rc < 0) {
		perror("Bind failed");
		exit(1);
	}
	printf("Bound\n");

	//adv_callback adv_callback_print = { &adv_callback_print_fn, 0 };
	adv_callback_zmq_data zmq_cb_data = { publisher, hostname, strlen(hostname) };
	adv_callback adv_callback_zmq = { &adv_callback_zmq_fn, &zmq_cb_data };
	cmd_lescan(-1, &adv_callback_zmq);

	printf("Closing ...");
	zmq_close (publisher);
	zmq_ctx_destroy (context);

	return 0;
}
