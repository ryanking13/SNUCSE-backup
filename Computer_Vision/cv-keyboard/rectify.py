import argparse
import numpy as np
import cv2

KEY_SPACE = 32
KEY_ESC = 27


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('video', help='Target video path')
    parser.add_argument('-o', '--output', help='output file name (.avi)', default='output.avi')

    args = parser.parse_args()
    return args


def clicked(event, x, y, flags, state):
    if event == cv2.EVENT_LBUTTONDOWN and len(state['selected']) < 4:
        state['selected'].append((x, y))
        cv2.circle(state['img'], (x, y), 3, (0, 0, 255), -1)


def clear_points(state):
    state['selected'] = []
    state['img'] = cv2.copyMakeBorder(state['original_img'], 0, 0, 0, 0, cv2.BORDER_REPLICATE)


# code adapted from: https://www.pyimagesearch.com/2014/08/25/4-point-opencv-getperspective-transform-example/
def four_point_transform(pts):
	(tl, tr, bl, br) = pts
 
	# compute the width of the new image, which will be the
	# maximum distance between bottom-right and bottom-left
	# x-coordiates or the top-right and top-left x-coordinates
	widthA = np.sqrt(((br[0] - bl[0]) ** 2) + ((br[1] - bl[1]) ** 2))
	widthB = np.sqrt(((tr[0] - tl[0]) ** 2) + ((tr[1] - tl[1]) ** 2))
	maxWidth = max(int(widthA), int(widthB))
 
	# compute the height of the new image, which will be the
	# maximum distance between the top-right and bottom-right
	# y-coordinates or the top-left and bottom-left y-coordinates
	heightA = np.sqrt(((tr[0] - br[0]) ** 2) + ((tr[1] - br[1]) ** 2))
	heightB = np.sqrt(((tl[0] - bl[0]) ** 2) + ((tl[1] - bl[1]) ** 2))
	maxHeight = max(int(heightA), int(heightB))
 
	# now that we have the dimensions of the new image, construct
	# the set of destination points to obtain a "birds eye view",
	# (i.e. top-down view) of the image, again specifying points
	# in the top-left, top-right, bottom-right, and bottom-left
	# order
	dst = np.array([
		[0, 0],
		[maxWidth - 1, 0],
        [0, maxHeight - 1],
		[maxWidth - 1, maxHeight - 1]],
		dtype = "float32"
    )
 
	# compute the perspective transform matrix
	M = cv2.getPerspectiveTransform(pts, dst)
	return M, maxWidth, maxHeight


def get_warp(frame):
    """calculate warp matrix"""

    print("[*] Select target area in (TL, TR, BL, BR) order")
    print("[*] Reset: ESC, Done: SPACE")

    state = {
        'original_img': cv2.copyMakeBorder(frame, 0, 0, 0, 0, cv2.BORDER_REPLICATE),
        'img': cv2.copyMakeBorder(frame, 0, 0, 0, 0, cv2.BORDER_REPLICATE),
        'selected': [],
    }

    cv2.namedWindow('selection')
    cv2.setMouseCallback('selection', clicked, state)

    while True:
        cv2.imshow('selection', state['img'])

        key = cv2.waitKey(1) & 0xFF
        
        if key == KEY_ESC:
            clear_points(state)
        
        elif key == KEY_SPACE and len(state['selected']) >= 4:
            break

    selected = np.float32([
        list(state['selected'][0]),
        list(state['selected'][1]),
        list(state['selected'][2]),
        list(state['selected'][3]),
    ])

    warp, w, h = four_point_transform(selected)
    cv2.destroyAllWindows()
    return warp, w, h


def warp_frame(frame, warp, size):
    """rectify `frame` with given `warp` matrix, output size will be `size`"""
    warped = cv2.warpPerspective(frame, warp, size)
    return warped


def main():
    args = parse_args()

    input_path = args.video
    output_path = args.output

    if not output_path.lower().endswith('avi'):
        print('[-] Sorry, output file extension must be AVI')
        exit(1)

    video = cv2.VideoCapture(input_path)
    width = int(video.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(video.get(cv2.CAP_PROP_FRAME_HEIGHT))
    fps = video.get(cv2.CAP_PROP_FPS)
    fourcc = cv2.VideoWriter_fourcc(*'XVID')
    rectified_video = None

    warp = None
    while video.isOpened():
        frame = video.read()[1]

        # if there is no frame left, break
        if frame is None:
            break
        
        # if it is the first frame, generate tranformation matrix
        if warp is None:
            warp, w, h = get_warp(frame)
            rectified_video = cv2.VideoWriter(output_path, fourcc, fps, (w, h))
        
        new_frame = warp_frame(frame, warp, (w, h))
        rectified_video.write(new_frame)
    
    video.release()
    rectified_video.release()
    cv2.destroyAllWindows()


if __name__ == '__main__':
    main()