import sys
import requests
import concurrent.futures


STATUS_SUCCESS = 2000


def ls_devices():
    req = requests.get('http://127.0.0.1:9011/api/device/list')
    res = req.json()
    if res['status'] == STATUS_SUCCESS:
        return res['list']
    else:
        print('Failed to get device list. Status code:', res['status'])
        return []


def replay(serial_no, script_id):
    step_index = 1
    while True:
        url = 'http://127.0.0.1:9011/api/record/playback' \
              '?scriptId={script_id}&serialNo={serial_no}&fromStep={from_step}' \
              .format(script_id=script_id, serial_no=serial_no, from_step=step_index)
        req = requests.get(url)
        res = req.json()
        if res['status'] == STATUS_SUCCESS:
            res_data = res['data']
            if res_data['success']:
                print('Replay finished on device %s.' % serial_no)
                return True
            else:
                quit = input('Please manually operate step %d of script %d on %s ' \
                             '("q" to quit).' \
                             % (res_data['next_step'], script_id, serial_no))
                if quit == 'q':
                    print('Quit replaying on device %s.' % serial_no)
                    return False
                step_index = res_data['next_step'] + 1
        else:
            print('Failed to replay script %d on device %s. Status code %d.' \
                  % (script_id, serial_no, res['status']))
            return False


def batch_replay(serial_nos, script_id):
    with concurrent.futures.ThreadPoolExecutor(max_workers=len(serial_nos)) as executor:
        futures = [executor.submit(replay, sno, script_id) for sno in serial_nos]
    results = [f.result() for f in futures]
    print(results)


def main():
    if len(sys.argv) < 2:
        print("Usage: python replay.py <script_id>")
        return
    script_id = int(sys.argv[1])
    devices = ls_devices()
    print('Devices:')
    for dev in devices:
        print(dev)

    print('Starting replaying...')
    if len(devices) > 0:
        replay(devices[0]['serialNumber'], script_id)
    #serial_nos = [dev['serialNumber'] for dev in devices]
    #batch_replay(serial_nos, script_id)


if __name__ == '__main__':
    main()
