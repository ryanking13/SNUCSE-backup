import argparse
import imutils
import cv2


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('video', help='Target video path')

    args = parser.parse_args();
    return args


def main():
    args = parse_args()

    input_path = args.video
    video = cv2.VideoCapture(input_path)
    trackers = cv2.MultiTracker_create()

    print('[*] Press `S` to pause and select targer area')
    print('[*] Press `SPACE` to resume')
    
    while video.isOpened():
        frame = video.read()[1]

        # if there is no frame left, break
        if frame is None:
            break
        
        frame = imutils.resize(frame, width=640)
        success, boxes = trackers.update(frame)

        for box in boxes:
            x, y, w, h = [int(v) for v in box]
            cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
        
        cv2.imshow('Frame', frame)
        key = cv2.waitKey(100) & 0xFF

        if key == ord('s'):
            box = cv2.selectROI('Frame', frame, fromCenter=False, showCrosshair=True)

            tracker = cv2.TrackerCSRT_create()
            trackers.add(tracker, frame, box)

if __name__ == '__main__':
    main()