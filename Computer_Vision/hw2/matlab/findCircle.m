function [cx, cy, r] = findCircle(img, threshold)
    [imgY, imgX] = size(img);
    
    binarized = imbinarize(img, threshold);
%     imshow(binarized);

    % assume radius range by finding min/max white pixels in x/y
    % coordinates
    minY = imgY;
    maxY = 0;
    minX = imgX;
    maxX = 0;
    for y = 1:imgY
        for x = 1:imgX
            if binarized(y, x) > 0
                minY = min(y, minY);
                maxY = max(y, maxY);
                minX = min(x, minX);
                maxX = max(x, maxX);
            end
        end
    end
    
    rx = fix((maxX - minX)/2);
    ry = fix((maxY - minY)/2);
    
    error = 10;
    [centers, rad] = imfindcircles(binarized, [min(rx, ry) - error, max(rx, ry) + error]);
    
%     imshow(img);
%     viscircles(centers, rad, 'Color', 'b');
    cx = centers(1);
    cy = centers(2);
    r = rad;
end
