clear;
'start'

% folder containing data (a sequence of jpg images)
pathname = '../data/car';
% pathname = '../data/landing';


% find the images, initialize some variables
dirlist = dir(sprintf('%s/*.jpg', pathname));
nFrames = numel(dirlist);
W = eye(3,3);   % transform matrix
startFrame = 1;
nIter = 60;

% loop over the images in the video sequence
for i=startFrame:nFrames
    
%     img = read_image(sprintf('%s/%s', pathname, dirlist(i).name));
    img = read_image(sprintf('%s/%s', pathname, dirlist(i).name));
    img_size = size(img);
    
    % if this is the first image, this is the frame to mark a template on
    if (i == startFrame)
        %display the image
        hold off;
        imshow(img);
        hold on;
        drawnow;
        
        % ask the user to click where the template is
        title('Click on the upper left corner of the template region to track');
        [pt1x, pt1y] = ginput(1);
        title('Click on the lower right corner of the template region to track');
        [pt2x, pt2y] = ginput(1);
        template = img;
    else
        % template box from the current template box
        pt1x = templateBox(1,1);
        pt1y = templateBox(2,1);
        pt2x = templateBox(1,3);
        pt2y = templateBox(2,3);
    end
        
    [pt1x, pt1y] = checkBoundary(img_size, pt1x, pt1y);
    [pt2x, pt2y] = checkBoundary(img_size, pt2x, pt2y);


    % build a mask defining the extent of the template
    mask     = false(size(template));
    mask(pt1y:pt2y, pt1x:pt2x) = true;
    templateBox = [pt1x pt1x pt2x pt2x pt1x; pt1y pt2y pt2y pt1y pt1y];

    tic;
    
    % initialize the LK tracker for this template
    % if there is any exception, return Exception true
    affineLKContext = initAffineLKTracker(template, mask);
    if affineLKContext.Exception == true
        title('An exception occurred.');
        break;
    end
    
    %actually do the LK tracking to update transform for current frame
    W = affineTrackerMasked(img, template, mask, W, affineLKContext, nIter);
    
    ftime = toc;
    
    % draw the location of the template onto the current frame, display stuff
    templateBox = W \ [templateBox; ones(1,5)];
    templateBox = templateBox(1:2,:);
    
    hold off;
    imshow(img);
    hold on;
    plot(templateBox(1,:), templateBox(2,:), 'g', 'linewidth', 2);
    title(sprintf('frame #%g / %.2f FPS', i, 1./ftime));
    drawnow;
    
    template = img;
end


function img = read_image(fileName)
% read a new image, convert to double, convert to greyscale
    img = imread(fileName);
    if (ndims(img) == 3)
        img = rgb2gray(img);
    end

    img = double(img) / 255;
    return;
end

function [ptx, pty] = checkBoundary(img_size, ptx, pty)
    % remove decimals
    pty = round(pty);
    ptx = round(ptx);

    % bounds checking
    pty = min(img_size(1), pty);
    pty = max(1, pty);
    ptx = min(img_size(2), ptx);
    ptx = max(1, ptx);
end
